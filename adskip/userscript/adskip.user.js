// ==UserScript==
// @name         광고 건너뛰기 (AdSkip)
// @namespace    https://kjooy0305.github.io/i_make_a_app/adskip/
// @version      1.3.1
// @description  유튜브 영상 광고가 나오면 '건너뛰기' 버튼을 대신 누르고, 버튼이 없는 광고는 끝으로 넘기고, '이어서 시청하시겠어요?' 창도 자동으로 확인합니다.
// @author       kjooy0305
// @match        *://www.youtube.com/*
// @match        *://m.youtube.com/*
// @match        *://music.youtube.com/*
// @match        *://youtube.com/*
// @match        *://www.youtube-nocookie.com/*
// @icon         https://kjooy0305.github.io/i_make_a_app/adskip/icon.png
// @downloadURL  https://kjooy0305.github.io/i_make_a_app/adskip/userscript/adskip.user.js
// @updateURL    https://kjooy0305.github.io/i_make_a_app/adskip/userscript/adskip.user.js
// @grant        GM_getValue
// @grant        GM_setValue
// @run-at       document-start
// ==/UserScript==

/* 광고 건너뛰기 (AdSkip) — 유튜브 페이지 안에서 돌아가는 본체.
 *
 * 원리 (안드로이드 접근성 서비스와 같은 생각을 웹에 옮긴 것):
 *  1. 유튜브 플레이어(#movie_player)는 광고가 나올 때 'ad-showing' 클래스를 붙인다. 그걸 신호로 삼는다.
 *  2. 광고 중이면 (a) '광고 건너뛰기' 버튼을 찾아 눌러 주고 (크롬 계열은 디버거 API 로 진짜 클릭)
 *                 (b) 버튼이 아직 없으면 광고 <video>를 16배속 + 끝 직전으로 보내 광고를 바로 끝낸다.
 *  3. 광고가 끝나면 소리 설정을 원래대로 되돌린다.
 *  4. 배너·추천 광고 칸은 CSS 로 숨긴다(선택).
 *  5. "동영상이 일시중지되었습니다. 이어서 시청하시겠어요?" 확인 창이 뜨면 [예]를 눌러 주고 멈춘 영상을 다시 재생한다.
 * 화면 안에서만 동작하고 어디에도 데이터를 보내지 않는다.
 */
(function () {
  'use strict';
  if (window.__adskipLoaded) return;
  window.__adskipLoaded = true;

  var DEFAULTS = { enabled: true, fastForward: true, hideBanners: true, badge: true, autoResume: true };
  var cfg = Object.assign({}, DEFAULTS);
  var stats = { count: 0 };

  /* ---------- 저장소: 확장(chrome.storage) → 유저스크립트(GM_*) → localStorage ---------- */
  var store = (function () {
    var hasChrome = typeof chrome !== 'undefined' && chrome.storage && chrome.storage.local;
    var hasGM = typeof GM_getValue === 'function' && typeof GM_setValue === 'function';
    function lsGet(k, d) { try { var v = localStorage.getItem('adskip.' + k); return v == null ? d : JSON.parse(v); } catch (e) { return d; } }
    function lsSet(k, v) { try { localStorage.setItem('adskip.' + k, JSON.stringify(v)); } catch (e) {} }
    return {
      load: function (cb) {
        if (hasChrome) {
          chrome.storage.local.get(['cfg', 'stats'], function (r) { cb((r && r.cfg) || {}, (r && r.stats) || {}); });
          chrome.storage.onChanged.addListener(function (ch) {
            if (ch.cfg) { cfg = Object.assign({}, DEFAULTS, ch.cfg.newValue || {}); applyCss(); }
          });
        } else if (hasGM) {
          cb(GM_getValue('cfg', {}), GM_getValue('stats', {}));
        } else {
          cb(lsGet('cfg', {}), lsGet('stats', {}));
        }
      },
      saveStats: function () {
        if (hasChrome) chrome.storage.local.set({ stats: stats });
        else if (hasGM) GM_setValue('stats', stats);
        else lsSet('stats', stats);
      }
    };
  })();

  /* ---------- 유틸 ---------- */
  function player() { return document.querySelector('#movie_player, .html5-video-player'); }
  function isAd(p) { return !!p && (p.classList.contains('ad-showing') || p.classList.contains('ad-interrupting')); }
  function visible(el) {
    if (!el || !el.isConnected) return false;
    var r = el.getBoundingClientRect();
    if (r.width === 0 && r.height === 0) return false;
    var cs = getComputedStyle(el);
    // pointer-events:none 이어도 JS click() 은 먹으므로 보지 않는다. 투명(opacity 0)은 아직 뜨는 중이라 다음 틱에 다시 본다.
    return cs.visibility !== 'hidden' && cs.display !== 'none' && parseFloat(cs.opacity || '1') > 0.05;
  }

  var SKIP_SEL = [
    '.ytp-skip-ad-button', '.ytp-ad-skip-button', '.ytp-ad-skip-button-modern',
    '.ytp-ad-skip-button-slot button', '.ytp-ad-skip-button-container button',
    '.videoAdUiSkipButton', 'button[id^="skip-button"]',
    'button[class*="skip-ad"]', 'button[class*="ad-skip"]', 'button[class*="skip_ad"]', '[class*="skip-button"] button'
  ].join(',');
  var SKIP_TEXT = /^(광고\s*건너뛰기|건너뛰기|skip\s*ads?|skip)$/i;
  var OVERLAY_SEL = '.ytp-ad-overlay-close-button, .ytp-ad-overlay-close-container, .ytp-ad-overlay-close-container button';

  /* 유튜브는 스크립트가 만든 클릭(isTrusted=false)을 건너뛰기 버튼에서 무시한다(2025~).
   * 그래서 확장(크롬 계열)에서는 배경 워커에 부탁해 디버거 API 로 '진짜' 마우스 클릭을 버튼 가운데 좌표에 보낸다.
   * 파이어폭스·유저스크립트에는 그런 길이 없어 element.click() 만 하고(안 먹을 수 있음), 대신 빨리감기로 끝낸다. */
  var canTrusted = typeof chrome !== 'undefined' && chrome.runtime && typeof chrome.runtime.sendMessage === 'function' && !!chrome.runtime.id && window.top === window;
  var trustedBusy = false;
  function pressButton(el) {
    try { el.click(); } catch (e) {}
    if (!canTrusted || trustedBusy) return;
    // 좌표는 여기서 재지 않는다. 디버거가 붙는 순간 크롬 위에 "디버깅 중" 띠가 생기며 화면이 아래로 밀리므로,
    // 배경 워커가 붙은 뒤에 이 표식(data-adskip-target)을 가진 요소의 위치를 다시 재서 누른다.
    var old = document.querySelectorAll('[data-adskip-target]');
    for (var i = 0; i < old.length; i++) old[i].removeAttribute('data-adskip-target');
    el.setAttribute('data-adskip-target', '1');
    trustedBusy = true;
    var done = function () { trustedBusy = false; try { el.removeAttribute('data-adskip-target'); } catch (e) {} };
    var guard = setTimeout(done, 3000);
    try {
      chrome.runtime.sendMessage({ type: 'trustedClick' }, function (res) { void chrome.runtime.lastError; clearTimeout(guard); done(); });
    } catch (e) { clearTimeout(guard); done(); }
  }

  /* 건너뛰기 버튼을 셀렉터 → 글자 순서로 찾아 클릭. (접근성 서비스의 아이디 → 글자 순서와 같다) */
  function clickSkip(p) {
    var scope = p || document;
    var list = scope.querySelectorAll(SKIP_SEL);
    for (var i = 0; i < list.length; i++) if (visible(list[i])) { pressButton(list[i]); return 'button'; }
    var btns = scope.querySelectorAll('button, [role="button"], .ytp-button');
    for (var j = 0; j < btns.length; j++) {
      var b = btns[j];
      var t = (b.textContent || '').trim();
      var a = b.getAttribute('aria-label') || '';
      if ((SKIP_TEXT.test(t) || SKIP_TEXT.test(a)) && visible(b)) { pressButton(b); return 'text'; }
    }
    // 글자만 있고 button 이 아닌 요소(div/span)면 가장 가까운 클릭 가능한 조상을 누른다 (광고 UI 안쪽만 훑는다: 비용)
    var adUi = scope.querySelector('.ytp-ad-module, .video-ads, .ytp-skip-ad, .ytp-ad-skip-button-slot');
    var all = (adUi || (scope === document ? null : scope) || { querySelectorAll: function () { return []; } }).querySelectorAll('div, span');
    for (var k = 0; k < all.length; k++) {
      var el = all[k];
      if (el.childElementCount === 0 && SKIP_TEXT.test((el.textContent || '').trim()) && visible(el)) {
        var target = el.closest('button, [role="button"], a, [onclick], .ytp-button') || el;
        pressButton(target);
        return 'text';
      }
    }
    return null;
  }

  /* 광고 영상을 빨리 끝내기.
   * 실제 유튜브로 재 본 결과(2026-11): 정확히 끝(duration)으로 보내면 유튜브가 광고를 끝내지 않고
   * 빈 화면으로 광고 시간이 다 될 때까지 붙잡아 둔다. 끝 '직전'(duration-0.3)으로 보내고 16배속으로 남은 0.3초를
   * 흘려보내면 0.5~1.5초 안에 광고가 끝난다. 광고 중엔 음소거했다가 끝나면 되돌린다. */
  var savedMuted = null;
  function fastForward(p) {
    var v = p.querySelector('video');
    if (!v) return false;
    if (savedMuted === null) { savedMuted = v.muted; v.muted = true; }
    try { if (v.playbackRate < 16) v.playbackRate = 16; } catch (e) {}
    if (isFinite(v.duration) && v.duration > 1 && v.currentTime < v.duration - 1) {
      try { v.currentTime = v.duration - 0.3; } catch (e) {}
      return true;
    }
    return false;
  }
  function restoreAudio(p) {
    if (savedMuted === null) return;
    var v = p && p.querySelector('video');
    if (v) { v.muted = savedMuted; try { v.playbackRate = 1; } catch (e) {} }
    savedMuted = null;
  }

  function closeOverlays(p) {
    var list = (p || document).querySelectorAll(OVERLAY_SEL);
    for (var i = 0; i < list.length; i++) if (visible(list[i])) { list[i].click(); return true; }
    return false;
  }

  /* ---------- 배너 숨기기 CSS ---------- */
  var CSS_ID = 'adskip-css';
  var BANNER_CSS = [
    'ytd-ad-slot-renderer', 'ytd-promoted-sparkles-web-renderer', 'ytd-display-ad-renderer',
    'ytd-in-feed-ad-layout-renderer', 'ytd-banner-promo-renderer', 'ytd-statement-banner-renderer',
    'ytd-player-legacy-desktop-watch-ads-renderer', '#masthead-ad', '#player-ads', 'ytd-promoted-video-renderer',
    'ytm-promoted-video-renderer', 'ytm-companion-ad-renderer', 'ytd-rich-item-renderer:has(ytd-ad-slot-renderer)',
    'ytd-rich-section-renderer:has(ytd-statement-banner-renderer)', '.ytp-ad-overlay-slot', '.ytd-mealbar-promo-renderer'
  ].join(',\n') + ' { display: none !important; }';
  function applyCss() {
    var el = document.getElementById(CSS_ID);
    if (cfg.enabled && cfg.hideBanners) {
      if (!el) { el = document.createElement('style'); el.id = CSS_ID; el.textContent = BANNER_CSS; (document.head || document.documentElement).appendChild(el); }
    } else if (el) el.remove();
  }

  /* ---------- 작은 배지 ("광고 건너뜀") ---------- */
  var badgeTimer = null;
  function showBadge(text) {
    if (!cfg.badge) return;
    var b = document.getElementById('adskip-badge');
    if (!b) {
      b = document.createElement('div');
      b.id = 'adskip-badge';
      b.style.cssText = 'position:fixed;left:50%;bottom:72px;transform:translateX(-50%);z-index:2147483647;' +
        'background:#000;color:#fff;font:13px/1 system-ui,sans-serif;padding:8px 12px;border-radius:999px;' +
        'opacity:0;transition:opacity .2s;pointer-events:none';
      document.documentElement.appendChild(b);
    }
    b.textContent = text;
    b.style.opacity = '1';
    clearTimeout(badgeTimer);
    badgeTimer = setTimeout(function () { b.style.opacity = '0'; }, 1600);
  }

  /* ---------- "이어서 시청하시겠어요?" 창 자동 확인 ----------
   * 유튜브는 한참 조작이 없으면 영상을 멈추고 확인 창(yt-confirm-dialog)을 띄운다.
   * 창의 글자가 '일시중지/이어서 시청/paused/continue watching' 이면 [예] 버튼을 누르고, 영상이 멈춰 있으면 play(). */
  var DIALOG_SEL = 'yt-confirm-dialog-renderer, tp-yt-paper-dialog, ytd-popup-container, .ytp-pause-overlay, dialog, [role="dialog"], ytm-confirm-dialog-renderer, .dialog-container';
  var PAUSE_TEXT = /(일시\s*중지|이어서\s*시청|계속\s*시청|video paused|continue watching|still watching)/i;
  var YES_TEXT = /^(예|네|확인|이어서 시청|계속 시청|계속|yes|ok|continue|continue watching)$/i;
  var lastResumeAt = 0;
  function autoResume() {
    if (!cfg.autoResume) return false;
    var now = Date.now();
    if (now - lastResumeAt < 1500) return false;
    var dialogs = document.querySelectorAll(DIALOG_SEL);
    for (var i = 0; i < dialogs.length; i++) {
      var d = dialogs[i];
      if (!visible(d) || !PAUSE_TEXT.test(d.textContent || '')) continue;
      // 진짜 <button> 을 먼저 보고(유튜브는 #confirm-button 안에 button 이 있다), 없으면 role=button/링크.
      var btns = d.querySelectorAll('#confirm-button button, button, [role="button"], a.yt-simple-endpoint, #confirm-button');
      for (var j = 0; j < btns.length; j++) {
        var b = btns[j];
        var label = ((b.textContent || '').trim() || b.getAttribute('aria-label') || '').trim();
        if (YES_TEXT.test(label) && visible(b)) {
          var inner = b.tagName !== 'BUTTON' ? b.querySelector('button') : null;
          (inner || b).click();
          lastResumeAt = now;
          var v = document.querySelector('#movie_player video, .html5-video-player video, video');
          setTimeout(function () { try { if (v && v.paused && !v.ended) v.play(); } catch (e) {} }, 300);
          showBadge('멈춘 영상 이어서 재생');
          return true;
        }
      }
    }
    return false;
  }

  /* ---------- 한 번의 점검 ---------- */
  var inAd = false, acted = false, lastClickAt = 0;
  function tick() {
    if (!cfg.enabled) return;
    autoResume();
    var p = player();
    if (!p) return;
    var ad = isAd(p);
    // 건너뛰기 버튼은 광고 표시(ad-showing)가 빠진 종료 화면에도 남아 있을 수 있으니 상태와 상관없이 보이면 누른다.
    var now = Date.now();
    var how = null;
    if (now - lastClickAt > 800) { how = clickSkip(p); if (how) lastClickAt = now; }
    if (ad) {
      if (!inAd) { inAd = true; acted = false; }
      if (!how && cfg.fastForward) { if (fastForward(p)) how = 'seek'; }
      if (how) acted = true;
    } else {
      if (how && !inAd) { stats.count = (stats.count || 0) + 1; store.saveStats(); showBadge('광고 건너뜀 · ' + stats.count + '회'); }
      if (inAd) {
        inAd = false;
        restoreAudio(p);
        if (acted) {
          stats.count = (stats.count || 0) + 1;
          store.saveStats();
          showBadge('광고 건너뜀 · ' + stats.count + '회');
        }
      }
      // 광고 중이 아니어도 걸려 있는 오버레이 배너는 닫는다
      if (Date.now() - lastClickAt > 800 && closeOverlays(p)) lastClickAt = Date.now();
    }
  }

  /* ---------- 시작: 설정 읽고, 변화 감지 + 주기 점검 ---------- */
  store.load(function (c, s) {
    cfg = Object.assign({}, DEFAULTS, c || {});
    stats = Object.assign({ count: 0 }, s || {});
    applyCss();
    // 점검은 아무리 바빠도 100ms 에 한 번. (예전엔 DOM 이 바뀔 때마다 rAF 로 돌려서 탭이 앞에 있으면
    // 유튜브가 버벅이고 광고 타이머까지 멈췄다. 뒤에 있으면 rAF 가 안 돌아 오히려 잘 됐던 이유.)
    var tickTimer = null, lastTick = 0;
    function requestTick() {
      if (tickTimer) return;
      var wait = Math.max(0, 100 - (Date.now() - lastTick));
      tickTimer = setTimeout(function () { tickTimer = null; lastTick = Date.now(); tick(); }, wait);
    }
    var mo = new MutationObserver(requestTick);
    function observe() {
      if (document.documentElement) mo.observe(document.documentElement, { subtree: true, attributes: true, attributeFilter: ['class', 'style'], childList: true });
    }
    observe();
    setInterval(requestTick, 300);
    document.addEventListener('yt-navigate-finish', function () { setTimeout(requestTick, 100); });
    // 유저스크립트 / 테스트용 공개 API (설정 바꾸기, 상태 보기)
    window.__adskip = {
      tick: tick,
      getStats: function () { return stats; },
      getConfig: function () { return cfg; },
      setConfig: function (c) { cfg = Object.assign({}, cfg, c); applyCss(); }
    };
  });
})();
