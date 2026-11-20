// 실제 유튜브에서 확장을 설치한 채 광고가 어떻게 처리되는지 본다 (광고가 안 나오면 다른 영상을 시도).
//   CHROMIUM=<chrome> node adskip/test/run-real.js <playwright 경로> [fastForward=0|1]
// fastForward=0 이면 빨리감기를 꺼서 '진짜 클릭'만으로 건너뛰기 버튼이 먹는지 검증한다.
const path = require('path');
const fs = require('fs');
const os = require('os');
const pwPath = process.argv[2] || 'playwright';
const ff = process.argv[3] !== '0';
const { chromium } = require(pwPath);
const ext = path.join(__dirname, '..', 'extension');
function assert(c, m) { if (!c) { console.error('FAIL: ' + m); process.exitCode = 1; } else console.log('ok: ' + m); }
(async () => {
  const userDir = fs.mkdtempSync(path.join(os.tmpdir(), 'adskip-real-'));
  const ctx = await chromium.launchPersistentContext(userDir, {
    headless: true, channel: 'chromium', executablePath: process.env.CHROMIUM || undefined, locale: 'ko-KR',
    viewport: { width: 1280, height: 800 },
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
    args: ['--disable-extensions-except=' + ext, '--load-extension=' + ext, '--autoplay-policy=no-user-gesture-required', '--lang=ko-KR'],
  });
  // 확장 id 를 알아내 설정을 바꾼다
  const diag = await ctx.newPage();
  await diag.goto('chrome://extensions/');
  const exts = await diag.evaluate(() => new Promise(r => chrome.management.getAll(r)));
  const me = exts.find(e => /AdSkip/.test(e.name));
  assert(!!me, '확장 설치됨: ' + (me && me.id));
  const popup = await ctx.newPage();
  await popup.goto('chrome-extension://' + me.id + '/popup.html');
  await popup.evaluate((ff) => new Promise(r => chrome.storage.local.set({ cfg: { enabled: true, fastForward: ff, hideBanners: true, badge: true, autoResume: true }, stats: { count: 0 } }, r)), ff);
  console.log('   config fastForward=' + ff);
  await diag.close(); await popup.close();

  const page = await ctx.newPage();
  const vids = ['kJQP7kiw5Fk', 'JGwWNGJdvx8', 'RgKAFK5djSk', '9bZkp7q19f0', 'OPf0YbXqDm0', 'CevxZvSJLk8'];
  let seenAd = false, result = null;
  for (const id of vids) {
    await page.goto('https://www.youtube.com/watch?v=' + id, { waitUntil: 'domcontentloaded' }).catch(() => {});
    await page.waitForTimeout(2000);
    await page.evaluate(() => { const v = document.querySelector('video'); v && v.play().catch(() => {}); });
    const snap = () => page.evaluate(() => {
      const p = document.querySelector('#movie_player'); const v = document.querySelector('video');
      const cls = p ? (p.className.match(/ad-showing|ad-interrupting/g) || []).join(' ') : 'no-player';
      const b = document.querySelector('.ytp-skip-ad-button, .ytp-ad-skip-button-modern, .ytp-ad-skip-button');
      const r = b && b.getBoundingClientRect();
      return { cls, t: v ? +v.currentTime.toFixed(1) : null, dur: v ? +v.duration.toFixed(1) : null, pod: (document.querySelector('.ytp-ad-pod-index') || {}).textContent || '', btn: !!(b && r.width > 0 && getComputedStyle(b).display !== 'none') };
    });
    let s = await snap();
    console.log(id, JSON.stringify(s));
    if (!/ad-showing/.test(s.cls)) continue;
    seenAd = true;
    // 페이지 안에서 50ms 마다 기록: 버튼이 처음 보인 시각, 광고 표시가 사라진 시각 (밖에서 보면 순간을 놓친다)
    await page.evaluate(() => {
      window.__rec = { start: performance.now(), btnAt: null, endAt: null, pods: [] };
      window.__recTimer = setInterval(() => {
        const p = document.querySelector('#movie_player'); if (!p) return;
        const b = document.querySelector('.ytp-skip-ad-button, .ytp-ad-skip-button-modern, .ytp-ad-skip-button');
        const r = b && b.getBoundingClientRect();
        const shown = !!(b && r.width > 0 && getComputedStyle(b).display !== 'none');
        const pod = (document.querySelector('.ytp-ad-pod-index') || {}).textContent || '';
        if (pod && window.__rec.pods[window.__rec.pods.length - 1] !== pod) window.__rec.pods.push(pod);
        if (shown && window.__rec.btnAt === null) window.__rec.btnAt = performance.now() - window.__rec.start;
        if (!p.classList.contains('ad-showing') && window.__rec.endAt === null) { window.__rec.endAt = performance.now() - window.__rec.start; clearInterval(window.__recTimer); }
      }, 50);
    });
    const t0 = Date.now();
    let btnAt = null, endAt = null, lastDur = s.dur;
    for (let i = 0; i < 90; i++) {
      await page.waitForTimeout(250);
      s = await snap();
      if (s.btn && btnAt === null) { btnAt = Date.now(); console.log('  +' + ((btnAt - t0) / 1000).toFixed(1) + 's 건너뛰기 버튼 보임 ' + JSON.stringify(s)); }
      if (i % 8 === 0) console.log('  +' + ((Date.now() - t0) / 1000).toFixed(1) + 's ' + JSON.stringify(s));
      if (!/ad-showing/.test(s.cls)) { endAt = Date.now(); console.log('  +' + ((endAt - t0) / 1000).toFixed(1) + 's 광고 끝 ' + JSON.stringify(s)); break; }
      lastDur = s.dur;
    }
    const rec = await page.evaluate(() => window.__rec);
    console.log('  page record:', JSON.stringify(rec));
    result = { btnAt: rec.btnAt === null ? null : rec.btnAt / 1000, endAt: rec.endAt === null ? null : rec.endAt / 1000, lastDur };
    break;
  }
  if (!seenAd) { console.log('광고를 만나지 못함 (검증 불가)'); await ctx.close(); return; }
  assert(result.endAt !== null, '광고가 끝남 (' + result.endAt + 's)');
  if (!ff) {
    assert(result.btnAt !== null, '건너뛰기 버튼이 떴음 (' + result.btnAt + 's)');
    if (result.btnAt !== null && result.endAt !== null) assert(result.endAt - result.btnAt < 2.5, '버튼이 뜬 뒤 2.5초 안에 진짜 클릭으로 넘어감 (' + (result.endAt - result.btnAt).toFixed(1) + 's)');
  } else {
    assert(result.endAt !== null && result.endAt < 6, '빨리감기로 6초 안에 광고 전체 종료 (' + result.endAt + 's)');
  }
  await page.screenshot({ path: path.join(process.env.SHOT_DIR || __dirname, 'real.png') });
  await ctx.close();
  console.log(process.exitCode ? 'SOME TESTS FAILED' : 'ALL PASSED');
})();
