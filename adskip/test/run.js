// 실제 Chromium(Playwright)으로 content.js 를 가짜 플레이어 페이지에 넣어 검증한다.
//   node adskip/test/run.js [playwright 모듈 경로]
// 1) 버튼 있는 광고: 버튼이 뜨면 바로 눌린다 (그 전엔 영상 끝으로 보내 더 빨리 끝남)
// 2) 버튼 없는 광고: 영상 끝으로 보내 끝낸다
// 3) 배너 칸이 숨겨진다, 음소거가 원래대로 돌아온다, 횟수가 쌓인다
const path = require('path');
const fs = require('fs');
const pwPath = process.argv[2] || 'playwright';
const { chromium } = require(pwPath);
const exe = process.env.CHROMIUM || undefined;
const page_url = 'file://' + path.join(__dirname, 'fake-player.html');
const content = fs.readFileSync(path.join(__dirname, '..', 'extension', 'content.js'), 'utf8');

function assert(c, m) { if (!c) { console.error('FAIL: ' + m); process.exitCode = 1; } else console.log('ok: ' + m); }

(async () => {
  const b = await chromium.launch(exe ? { executablePath: exe } : {});
  const ctx = await b.newContext({ viewport: { width: 800, height: 600 } });
  const page = await ctx.newPage();
  const errors = [];
  page.on('pageerror', e => errors.push(String(e)));
  page.on('console', m => { if (m.type() === 'error') errors.push(m.text()); });
  await page.goto(page_url);
  await page.addScriptTag({ content });
  await page.waitForFunction(() => !!window.__adskip);

  // 배너 숨김
  const bannerHidden = await page.evaluate(() => getComputedStyle(document.querySelector('ytd-ad-slot-renderer')).display === 'none');
  assert(bannerHidden, '배너 칸(ytd-ad-slot-renderer) 숨김');

  // 1) 버튼 있는 광고 — 빨리감기를 끄고 버튼 클릭만 검증
  await page.evaluate(() => window.__adskip.setConfig({ fastForward: false }));
  await page.evaluate(() => window.startAd(1500));
  await page.waitForFunction(() => window.events.some(e => e.startsWith('ad-ended')), null, { timeout: 8000 }).catch(() => {});
  let ev = await page.evaluate(() => window.events);
  console.log('   events:', ev.join(' | '));
  assert(ev.includes('skip-clicked'), '건너뛰기 버튼이 눌림');
  assert(ev.some(e => e.startsWith('ad-ended:click')), '버튼 클릭으로 광고 종료');
  const clickedAt = ev.findIndex(e => e === 'skip-clicked'), shownAt = ev.findIndex(e => e === 'skip-button-shown');
  assert(clickedAt > shownAt, '버튼이 뜬 다음에 눌렀다(안 보이는 버튼은 누르지 않음)');
  await page.waitForTimeout(400);
  let stats = await page.evaluate(() => window.__adskip.getStats());
  assert(stats.count === 1, '건너뛴 횟수 1 (실제 ' + stats.count + ')');
  const muted1 = await page.evaluate(() => document.getElementById('v').muted);
  assert(muted1 === false, '광고 끝난 뒤 음소거 원복');

  // 2) 버튼 없는 광고 — 빨리감기로 끝내기
  await page.evaluate(() => { window.events.length = 0; window.__adskip.setConfig({ fastForward: true }); });
  const t0 = Date.now();
  await page.evaluate(() => window.startAd(0));
  await page.waitForFunction(() => window.events.some(e => e.startsWith('ad-ended')), null, { timeout: 8000 }).catch(() => {});
  const dt = Date.now() - t0;
  ev = await page.evaluate(() => window.events);
  console.log('   events:', ev.join(' | '), ' (' + dt + 'ms)');
  assert(ev.some(e => e.startsWith('ad-ended:seek-to-end') || e.startsWith('ad-ended:ended')), '버튼 없는 6초 광고를 끝으로 보내 종료');
  assert(dt < 4000, '6초 광고가 4초 안에 끝남 (' + dt + 'ms)');
  await page.waitForTimeout(400);
  stats = await page.evaluate(() => window.__adskip.getStats());
  assert(stats.count === 2, '건너뛴 횟수 2 (실제 ' + stats.count + ')');


  // 2a) 영상을 끝으로 보낸 뒤 뜨는 종료 화면의 '건너뛰기' 버튼(광고 표시 없음)도 누른다
  await page.evaluate(() => { window.events.length = 0; window.endCardMode = true; });
  await page.evaluate(() => window.startAd(0));
  await page.waitForFunction(() => window.events.includes('endcard-skip-clicked') || window.events.includes('endcard-timeout'), null, { timeout: 8000 }).catch(() => {});
  ev = await page.evaluate(() => window.events);
  console.log('   events:', ev.join(' | '));
  assert(ev.includes('endcard-shown') && ev.includes('endcard-skip-clicked') && !ev.includes('endcard-timeout'), '종료 화면의 건너뛰기 버튼을 자동으로 누름');
  await page.evaluate(() => { window.endCardMode = false; });
  await page.waitForTimeout(500);

  // 2b) '이어서 시청하시겠어요?' 창 → [예] 누르고 재생 재개
  await page.evaluate(() => { window.events.length = 0; });
  await page.evaluate(() => window.showPauseDialog());
  await page.waitForFunction(() => window.events.includes('pause-dialog-yes'), null, { timeout: 5000 }).catch(() => {});
  await page.waitForTimeout(600);
  ev = await page.evaluate(() => window.events);
  console.log('   events:', ev.join(' | '));
  assert(ev.includes('pause-dialog-yes') && !ev.includes('pause-dialog-no'), '일시중지 확인 창에서 [예]를 누름');
  const playing = await page.evaluate(() => { const v = document.getElementById('v'); return !v.paused; });
  assert(playing, '멈춘 영상이 다시 재생됨');
  await page.evaluate(() => { const v = document.getElementById('v'); v.pause(); v.removeAttribute('src'); v.load(); });

  // 3) 꺼짐 상태에서는 아무것도 안 함
  await page.evaluate(() => { window.events.length = 0; window.__adskip.setConfig({ enabled: false }); });
  await page.evaluate(() => window.startAd(500));
  await page.waitForTimeout(1500);
  ev = await page.evaluate(() => window.events);
  assert(!ev.includes('skip-clicked') && !ev.some(e => e.startsWith('ad-ended')), '꺼짐이면 손대지 않음');
  const bannerShown = await page.evaluate(() => getComputedStyle(document.querySelector('ytd-ad-slot-renderer')).display !== 'none');
  assert(bannerShown, '꺼짐이면 배너 CSS 도 제거');

  await page.screenshot({ path: path.join(process.env.SHOT_DIR || __dirname, 'fake-player.png') });
  assert(errors.length === 0, '페이지 오류 없음 ' + JSON.stringify(errors));
  await b.close();
  console.log(process.exitCode ? 'SOME TESTS FAILED' : 'ALL PASSED');
})();
