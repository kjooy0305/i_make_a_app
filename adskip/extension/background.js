/* 배경 워커: 콘텐츠 스크립트가 "이 좌표를 진짜로 클릭해 줘" 하고 부탁하면
 * chrome.debugger 로 탭에 잠깐 붙어 Input.dispatchMouseEvent 를 보낸다.
 * 이렇게 보낸 클릭은 isTrusted=true 라서 유튜브가 사람 클릭과 구분하지 못한다.
 * (일반 element.click() 은 isTrusted=false 라 유튜브 건너뛰기 버튼이 무시한다.)
 * 붙어 있는 동안 크롬 위에 "디버깅 중" 띠가 잠깐 보이고, 클릭 직후 바로 뗀다. */
var queue = Promise.resolve();

function send(target, method, params) {
  return new Promise(function (resolve, reject) {
    chrome.debugger.sendCommand(target, method, params, function (res) {
      if (chrome.runtime.lastError) reject(new Error(chrome.runtime.lastError.message)); else resolve(res);
    });
  });
}
function attach(target) {
  return new Promise(function (resolve, reject) {
    chrome.debugger.attach(target, '1.3', function () {
      var e = chrome.runtime.lastError;
      if (e && !/already attached/i.test(e.message)) reject(new Error(e.message)); else resolve();
    });
  });
}
function detach(target) {
  return new Promise(function (resolve) { chrome.debugger.detach(target, function () { void chrome.runtime.lastError; resolve(); }); });
}

var sleep = function (ms) { return new Promise(function (r) { setTimeout(r, ms); }); };
/* 콘텐츠 스크립트가 표식을 붙여 둔 버튼의 '지금' 화면 위치. 디버거가 붙으면 위에 띠가 생겨 화면이 내려오므로 붙은 뒤에 잰다. */
var POS_EXPR = "(function(){var e=document.querySelector('[data-adskip-target]');if(!e)return null;" +
  "var r=e.getBoundingClientRect();var cs=getComputedStyle(e);" +
  "if(r.width===0||r.height===0||cs.display==='none'||cs.visibility==='hidden')return null;" +
  "return {x:r.left+r.width/2,y:r.top+r.height/2};})()";

async function trustedClick(tabId) {
  var target = { tabId: tabId };
  await attach(target);
  try {
    await sleep(60);
    for (var attempt = 0; attempt < 3; attempt++) {
      var res = await send(target, 'Runtime.evaluate', { expression: POS_EXPR, returnByValue: true });
      var pos = res && res.result && res.result.value;
      if (!pos) return attempt > 0; // 버튼이 사라졌으면 (앞선 클릭이 먹었으면) 끝
      await send(target, 'Input.dispatchMouseEvent', { type: 'mouseMoved', x: pos.x, y: pos.y });
      await send(target, 'Input.dispatchMouseEvent', { type: 'mousePressed', x: pos.x, y: pos.y, button: 'left', clickCount: 1 });
      await send(target, 'Input.dispatchMouseEvent', { type: 'mouseReleased', x: pos.x, y: pos.y, button: 'left', clickCount: 1 });
      await sleep(150); // 띠가 뜨며 화면이 밀렸으면 다음 번엔 새 좌표로
    }
    return true;
  } finally {
    await detach(target);
  }
}

chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {
  if (!msg || msg.type !== 'trustedClick' || !sender.tab) return;
  var tabId = sender.tab.id;
  queue = queue.then(function () { return trustedClick(tabId); })
    .then(function () { sendResponse({ ok: true }); })
    .catch(function (e) { sendResponse({ ok: false, error: String(e && e.message || e) }); });
  return true;
});

/* 보험: 30초마다 모든 탭에 '한 번 살펴봐' 신호. 유튜브가 아닌 탭은 받을 사람이 없어 오류가 나지만 무시한다.
 * (뒤에 있는 탭은 setInterval 이 1분까지 늦어질 수 있어, 타이머가 아닌 메시지로 깨운다.) */
chrome.alarms.create('adskip-poke', { periodInMinutes: 0.5 });
chrome.alarms.onAlarm.addListener(function (a) {
  if (a.name !== 'adskip-poke') return;
  chrome.tabs.query({}, function (tabs) {
    (tabs || []).forEach(function (t) {
      try { chrome.tabs.sendMessage(t.id, { type: 'poke' }, function () { void chrome.runtime.lastError; }); } catch (e) {}
    });
  });
});
