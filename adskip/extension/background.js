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

async function trustedClick(tabId, x, y) {
  var target = { tabId: tabId };
  await attach(target);
  try {
    await send(target, 'Input.dispatchMouseEvent', { type: 'mouseMoved', x: x, y: y });
    await send(target, 'Input.dispatchMouseEvent', { type: 'mousePressed', x: x, y: y, button: 'left', clickCount: 1 });
    await send(target, 'Input.dispatchMouseEvent', { type: 'mouseReleased', x: x, y: y, button: 'left', clickCount: 1 });
  } finally {
    await detach(target);
  }
  return true;
}

chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {
  if (!msg || msg.type !== 'trustedClick' || !sender.tab) return;
  var tabId = sender.tab.id;
  queue = queue.then(function () { return trustedClick(tabId, msg.x, msg.y); })
    .then(function () { sendResponse({ ok: true }); })
    .catch(function (e) { sendResponse({ ok: false, error: String(e && e.message || e) }); });
  return true;
});
