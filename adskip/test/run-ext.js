// 확장 프로그램 자체(manifest.json + content.js)를 Chromium 에 설치해 끝까지 검증한다.
// youtube.com 요청을 가짜 플레이어 페이지로 돌려서, 확장의 content_scripts 매칭이 실제로 주입되는지 본다.
//   node adskip/test/run-ext.js [playwright 모듈 경로]
const path = require('path');
const fs = require('fs');
const os = require('os');
const pwPath = process.argv[2] || 'playwright';
const { chromium } = require(pwPath);
const ext = process.env.EXT_DIR ? path.resolve(process.env.EXT_DIR) : path.join(__dirname, '..', 'extension');
function assert(c, m) { if (!c) { console.error('FAIL: ' + m); process.exitCode = 1; } else console.log('ok: ' + m); }
(async () => {
  const userDir = fs.mkdtempSync(path.join(os.tmpdir(), 'adskip-ext-'));
  const ctx = await chromium.launchPersistentContext(userDir, {
    headless: true,
    channel: 'chromium',
    executablePath: process.env.CHROMIUM || undefined,
    args: ['--disable-extensions-except=' + ext, '--load-extension=' + ext],
  });
  const diag = await ctx.newPage();
  await diag.goto('chrome://extensions/');
  await diag.waitForTimeout(500);
  const ids = await diag.evaluate(() => new Promise(r => chrome.management ? chrome.management.getAll(r) : r('no chrome.management')));
  console.log('   extensions:', JSON.stringify(ids && ids.map ? ids.map(e => [e.name, e.enabled]) : ids));
  await diag.close();
  const page = await ctx.newPage();
  const errors = [];
  page.on('pageerror', e => errors.push(String(e)));
  await page.route('https://www.youtube.com/**', route => {
    const u = new URL(route.request().url());
    if (u.pathname.endsWith('/ad.webm')) return route.fulfill({ path: path.join(__dirname, 'ad.webm'), contentType: 'video/webm' });
    return route.fulfill({ path: path.join(__dirname, 'fake-player.html'), contentType: 'text/html' });
  });
  await page.goto('https://www.youtube.com/watch?v=test');
  const injected = await page.waitForFunction(() => !!document.getElementById('adskip-css'), null, { timeout: 5000 }).then(() => true).catch(() => false);
  assert(injected, '확장이 youtube.com 페이지에 content.js 를 주입함 (배너 숨김 CSS 가 DOM 에 있음; 콘텐츠 스크립트는 격리 월드라 window 변수로는 못 봄)');
  if (injected) {
    await page.evaluate(() => window.startAd(1200));
    await page.waitForFunction(() => window.events.some(e => e.startsWith('ad-ended')), null, { timeout: 8000 }).catch(() => {});
    const ev = await page.evaluate(() => window.events);
    console.log('   events:', ev.join(' | '));
    assert(ev.some(e => e.startsWith('ad-ended')), '설치된 확장이 광고를 끝냄');
    await page.waitForTimeout(500);
    const badge = await page.evaluate(() => { const b = document.getElementById('adskip-badge'); return b && b.textContent; });
    assert(!!badge && badge.indexOf('광고 건너뜀') === 0, '배지 표시: ' + badge);
  }
  assert(errors.length === 0, '페이지 오류 없음 ' + JSON.stringify(errors));
  await ctx.close();
  console.log(process.exitCode ? 'SOME TESTS FAILED' : 'ALL PASSED');
})();
