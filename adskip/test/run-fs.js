// 전체화면 버그 시험: (1) 마우스를 올리자 컨트롤 바가 나타나 건너뛰기 버튼이 밀릴 때 클릭이 전체화면 버튼 등에 빗나가지 않는지, (2) 건너뛰기 직후 전체화면 요청·더블클릭이 막히는지.
//   CHROMIUM=<chrome> node adskip/test/run-fs.js <playwright 경로>
const path = require('path');
const fs = require('fs');
const os = require('os');
const { chromium } = require(process.argv[2] || 'playwright');
const ext = process.env.EXT_DIR ? path.resolve(process.env.EXT_DIR) : path.join(__dirname, '..', 'extension');
function assert(c, m) { if (!c) { console.error('FAIL: ' + m); process.exitCode = 1; } else console.log('ok: ' + m); }
(async () => {
  const ctx = await chromium.launchPersistentContext(fs.mkdtempSync(path.join(os.tmpdir(), 'adskip-fs-')), {
    headless: true, channel: 'chromium', executablePath: process.env.CHROMIUM || undefined, viewport: { width: 900, height: 600 },
    args: ['--disable-extensions-except=' + ext, '--load-extension=' + ext],
  });
  // 빨리감기를 꺼서 '진짜 클릭' 경로만 시험한다
  const d = await ctx.newPage(); await d.goto('chrome://extensions/');
  const me = (await d.evaluate(() => new Promise(r => chrome.management.getAll(r)))).find(e => /광고 건너뛰기/.test(e.name));
  const pp = await ctx.newPage(); await pp.goto('chrome-extension://' + me.id + '/popup.html');
  await pp.evaluate(() => new Promise(r => chrome.storage.local.set({ cfg: { enabled: true, fastForward: false, hideBanners: true, badge: true, autoResume: true }, stats: { count: 0 } }, r)));
  await d.close(); await pp.close();

  async function open() {
    const page = await ctx.newPage();
    await page.route('https://www.youtube.com/**', route => {
      const u = new URL(route.request().url());
      if (u.pathname.endsWith('/ad.webm')) return route.fulfill({ path: path.join(__dirname, 'ad.webm'), contentType: 'video/webm' });
      return route.fulfill({ path: path.join(__dirname, 'fake-player.html'), contentType: 'text/html' });
    });
    await page.goto('https://www.youtube.com/watch?v=test');
    await page.waitForFunction(() => !!document.getElementById('adskip-css'), null, { timeout: 5000 });
    return page;
  }
  const events = (page) => page.evaluate(() => window.events);

  // (1) 마우스를 올리면 컨트롤 바가 나타나 건너뛰기 버튼이 위로 밀리는 광고
  let page = await open();
  await page.evaluate(() => { window.controlsOnMove = true; window.requireTrusted = true; window.startAd(800); });
  await page.waitForFunction(() => window.events.some(e => e.startsWith('ad-ended')), null, { timeout: 9000 }).catch(() => {});
  await page.waitForTimeout(600);
  let ev = await events(page); console.log('   (1) events:', ev.join(' | '));
  assert(ev.includes('skip-clicked'), '(1) 버튼이 밀려도 건너뛰기 버튼을 정확히 누름');
  assert(!ev.some(e => e.startsWith('stray-click')), '(1) 영상·빈 곳을 잘못 누르지 않음');
  assert(!ev.includes('fullscreen-button-clicked') && !ev.includes('fullscreen-entered'), '(1) 컨트롤 바의 전체화면 버튼을 누르지 않아 전체화면이 되지 않음');
  await page.close();

  // (2) 건너뛰기 직후 전체화면 요청 → 취소돼야 함
  page = await open();
  await page.evaluate(() => { window.fsAfterSkip = true; window.requireTrusted = true; window.startAd(800); });
  await page.waitForFunction(() => window.events.some(e => e.startsWith('ad-ended')), null, { timeout: 9000 }).catch(() => {});
  await page.waitForTimeout(2500);
  ev = await events(page); console.log('   (2) events:', ev.join(' | '));
  const fsNow = await page.evaluate(() => !!document.fullscreenElement);
  assert(ev.includes('skip-clicked'), '(2) 건너뛰기 버튼이 눌림');
  assert(!fsNow, '(2) 건너뛰기 직후 요청된 전체화면이 취소됨(지금 전체화면 아님)');
  await page.close();

  // (3) 건너뛰기 직후 더블클릭 → 막혀야 함 / (4) 시간이 지난 뒤 사용자의 더블클릭은 정상 동작
  page = await open();
  await page.evaluate(() => { window.requireTrusted = true; window.startAd(800); });
  await page.waitForFunction(() => window.events.some(e => e.startsWith('ad-ended')), null, { timeout: 9000 }).catch(() => {});
  await page.waitForTimeout(300);
  await page.mouse.dblclick(120, 120);
  await page.waitForTimeout(500);
  ev = await events(page); console.log('   (3) events:', ev.join(' | '));
  assert(!ev.includes('dblclick-on-player') && !(await page.evaluate(() => !!document.fullscreenElement)), '(3) 건너뛰기 직후 더블클릭 전체화면이 막힘');
  await page.waitForTimeout(6000);
  await page.mouse.dblclick(120, 120);
  await page.waitForTimeout(600);
  ev = await events(page);
  assert(ev.includes('dblclick-on-player'), '(4) 6초 뒤 사용자의 더블클릭은 그대로 동작(전체화면 요청이 전달됨)');
  await ctx.close();
  console.log(process.exitCode ? 'SOME TESTS FAILED' : 'ALL PASSED');
})();
