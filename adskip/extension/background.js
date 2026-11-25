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

async function measure(target) {
  var res = await send(target, 'Runtime.evaluate', { expression: POS_EXPR, returnByValue: true });
  return (res && res.result && res.result.value) || null;
}
/* 누르려는 좌표에 정말 그 건너뛰기 버튼(또는 그 안쪽)이 있는지. 아니면 누르지 않는다. */
function hitExpr(x, y) {
  return "(function(x,y){var e=document.querySelector('[data-adskip-target]');if(!e)return false;" +
    "var h=document.elementFromPoint(x,y);return !!h&&(h===e||e.contains(h));})(" + x + "," + y + ")";
}

async function trustedClick(tabId) {
  var target = { tabId: tabId };
  await attach(target);
  try {
    await sleep(120); // "디버깅 중" 띠가 뜨며 화면이 내려올 시간을 준 뒤 좌표를 잰다
    var pos = await measure(target);
    if (!pos) return false;
    // 마우스를 먼저 버튼 위로 옮기고, 위치가 가만히 있는지 다시 잰다.
    // 유튜브는 마우스가 플레이어 위에서 움직이면 아래쪽 컨트롤 바를 띄우면서 건너뛰기 버튼을 위로 밀어 올린다.
    // 그러면 원래 버튼 자리가 컨트롤 바 맨 오른쪽, 즉 '전체화면' 버튼 자리가 된다.
    // (예전엔 옮기자마자 처음 잰 좌표를 눌러서, 광고를 몇 초 보다 건너뛸 때 전체화면이 되곤 했다.)
    var steady = false;
    for (var i = 0; i < 8 && !steady; i++) {
      await send(target, 'Input.dispatchMouseEvent', { type: 'mouseMoved', x: pos.x, y: pos.y });
      await sleep(120);
      var next = await measure(target);
      if (!next) return false;
      steady = Math.abs(next.x - pos.x) < 1 && Math.abs(next.y - pos.y) < 1;
      pos = next;
    }
    if (!steady) return false;
    // 누르기 직전 마지막 확인: 그 자리에 건너뛰기 버튼이 없으면 누르지 않는다(콘텐츠 스크립트가 0.8초 뒤 다시 부탁한다).
    var hit = await send(target, 'Runtime.evaluate', { expression: hitExpr(pos.x, pos.y), returnByValue: true });
    if (!(hit && hit.result && hit.result.value)) return false;
    // 딱 한 번만 누른다. 접힌 탭은 유튜브가 화면을 안 그려 광고가 끝난 뒤에도 버튼이 남아 보이므로,
    // 두 번째 클릭은 본영상 위를 눌러 일시정지시킨다.
    await send(target, 'Input.dispatchMouseEvent', { type: 'mousePressed', x: pos.x, y: pos.y, button: 'left', clickCount: 1 });
    await send(target, 'Input.dispatchMouseEvent', { type: 'mouseReleased', x: pos.x, y: pos.y, button: 'left', clickCount: 1 });
    return true;
  } finally {
    await detach(target);
  }
}

chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {
  if (!msg || msg.type !== 'trustedClick' || !sender.tab) return;
  var tabId = sender.tab.id;
  queue = queue.then(function () { return trustedClick(tabId); })
    .then(function (clicked) { sendResponse({ ok: true, clicked: !!clicked }); })
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
