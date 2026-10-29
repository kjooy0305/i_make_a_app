(function () {
  'use strict';
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var esc = function (v) { return String(v).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; }); };
  var AUDIO = /\.(mp3|m4a|flac|wav|ogg|aac)$/i;

  /* ================= 저장소 (IndexedDB, 안 되면 메모리) ================= */
  var Store = (function () {
    var db = null, mem = null;
    function open() {
      return new Promise(function (res) {
        try {
          var rq = indexedDB.open('musiclist', 1);
          rq.onupgradeneeded = function () { rq.result.createObjectStore('meta', { keyPath: 'key' }); };
          rq.onsuccess = function () { db = rq.result; res(true); };
          rq.onerror = function () { res(false); };
        } catch (e) { res(false); }
      });
    }
    function load() {
      return new Promise(function (res) {
        if (!db) { res(mem); return; }
        var r = db.transaction('meta', 'readonly').objectStore('meta').get('state');
        r.onsuccess = function () { res(r.result ? r.result.value : null); };
        r.onerror = function () { res(null); };
      });
    }
    function save(v) {
      mem = v;
      if (!db) return;
      try { db.transaction('meta', 'readwrite').objectStore('meta').put({ key: 'state', value: v }); } catch (e) { /* 무시 */ }
    }
    return { open: open, load: load, save: save };
  })();

  /* ================= 상태 ================= */
  var S = { screen: 'home', sheet: null, tracks: [], bundles: {}, nb: 1, next: 1, q: '', sort: 'new', open: {}, fmt: 1,
    files: [], cur: null, all: { same: null, sim: null }, sel: null, confirmDel: false, toast: '' };
  function persist() { Store.save({ tracks: S.tracks, bundles: S.bundles, nb: S.nb, next: S.next, fmt: S.fmt }); }

  /* ================= 이름 비교 ================= */
  var norm = function (n) { return String(n).toLowerCase().replace(/[\s_\-.,'"!?]+/g, ''); };
  var base = function (n) { return norm(String(n).replace(/[\(\[（【][^\)\]）】]*[\)\]）】]/g, '')); };
  function lev(a, b) {
    var m = a.length, n = b.length, d = [], i, j;
    if (!m) return n; if (!n) return m;
    for (j = 0; j <= n; j++) d[j] = j;
    for (i = 1; i <= m; i++) {
      var prev = d[0]; d[0] = i;
      for (j = 1; j <= n; j++) { var t = d[j]; d[j] = Math.min(d[j] + 1, d[j - 1] + 1, prev + (a[i - 1] === b[j - 1] ? 0 : 1)); prev = t; }
    }
    return d[n];
  }
  function similar(a, b) {
    var x = base(a), y = base(b);
    if (!x || !y) return false;
    if (x === y) return true;
    var s = x.length <= y.length ? x : y, l = s === x ? y : x;
    if (s.length >= 2 && l.indexOf(s) === 0) return true;
    return 1 - lev(x, y) / Math.max(x.length, y.length) >= 0.8;
  }
  function conflictOf(name) {
    var n = norm(name), i, t;
    for (i = 0; i < S.tracks.length; i++) { t = S.tracks[i]; if (norm(t.name) === n) return { kind: 'same', target: t }; }
    for (i = 0; i < S.tracks.length; i++) { t = S.tracks[i]; if (similar(t.name, name)) return { kind: 'sim', target: t }; }
    return null;
  }

  /* ================= ID3 가수 읽기 ================= */
  function decodeText(bytes, enc) {
    try {
      var s;
      if (enc === 1 || enc === 2) {
        var be = enc === 2, st = 0;
        if (bytes[0] === 0xFF && bytes[1] === 0xFE) { be = false; st = 2; } else if (bytes[0] === 0xFE && bytes[1] === 0xFF) { be = true; st = 2; }
        s = new TextDecoder(be ? 'utf-16be' : 'utf-16le').decode(bytes.subarray(st));
      } else if (enc === 3) s = new TextDecoder('utf-8').decode(bytes);
      else {
        var hi = false, i; for (i = 0; i < bytes.length; i++) if (bytes[i] > 127) { hi = true; break; }
        s = new TextDecoder(hi ? 'euc-kr' : 'latin1').decode(bytes);
      }
      return s.replace(/\u0000+$/g, '').replace(/\u0000/g, ', ').trim();
    } catch (e) { return ''; }
  }
  async function readArtist(file) {
    try {
      var head = new Uint8Array(await file.slice(0, 10).arrayBuffer());
      if (head[0] !== 0x49 || head[1] !== 0x44 || head[2] !== 0x33) return '';
      var ver = head[3], size = ((head[6] & 127) << 21) | ((head[7] & 127) << 14) | ((head[8] & 127) << 7) | (head[9] & 127);
      var buf = new Uint8Array(await file.slice(10, 10 + Math.min(size, 262144)).arrayBuffer());
      var p = 0, idLen = ver === 2 ? 3 : 4, hLen = ver === 2 ? 6 : 10;
      while (p + hLen <= buf.length) {
        var id = String.fromCharCode.apply(null, buf.subarray(p, p + idLen));
        if (!/^[A-Z0-9]+$/.test(id)) break;
        var fs;
        if (ver === 2) fs = (buf[p + 3] << 16) | (buf[p + 4] << 8) | buf[p + 5];
        else if (ver === 4) fs = ((buf[p + 4] & 127) << 21) | ((buf[p + 5] & 127) << 14) | ((buf[p + 6] & 127) << 7) | (buf[p + 7] & 127);
        else fs = (buf[p + 4] * 16777216) + (buf[p + 5] << 16) + (buf[p + 6] << 8) + buf[p + 7];
        if (id === 'TPE1' || id === 'TP1') return decodeText(buf.subarray(p + hLen + 1, p + hLen + fs), buf[p + hLen]);
        p += hLen + fs;
        if (fs <= 0) break;
      }
    } catch (e) { /* 태그 없음 */ }
    return '';
  }

  /* ================= 목록 만들기 ================= */
  var find = function (id) { return S.tracks.filter(function (t) { return t.id === id; })[0]; };
  var artistOf = function (t) { return t.artist || '가수 미상'; };
  function units() {
    var q = S.q.trim().toLowerCase(), byB = {}, out = [];
    S.tracks.forEach(function (t) {
      var hit = !q || (t.name + ' ' + (t.artist || '')).toLowerCase().indexOf(q) >= 0;
      if (t.bundle && S.bundles[t.bundle]) { var g = byB[t.bundle] = byB[t.bundle] || { b: t.bundle, all: [], items: [] }; g.all.push(t); if (hit) g.items.push(t); }
      else if (hit) out.push({ single: t });
    });
    Object.keys(byB).forEach(function (k) { if (byB[k].items.length) out.push(byB[k]); });
    var key = function (u) { var t = u.single || u.all[0]; return S.sort === 'title' ? (u.single ? t.name : S.bundles[u.b]) : (t.artist || '힣'); };
    var tm = function (u) { return u.single ? u.single.t : Math.max.apply(null, u.all.map(function (x) { return x.t; })); };
    out.sort(function (a, b) { return S.sort === 'new' ? tm(b) - tm(a) : String(key(a)).localeCompare(String(key(b)), 'ko'); });
    return out;
  }
  function flat() {
    var r = [];
    units().forEach(function (u) { if (u.single) r.push(u.single); else u.items.forEach(function (t) { r.push(t); }); });
    return r;
  }
  var fmtLine = function (t, f) { return f === 2 ? artistOf(t) + ' - ' + t.name : f === 3 ? t.name + (t.ext || '.mp3') : t.name; };
  var textLines = function () { return flat().map(function (t) { return fmtLine(t, S.fmt); }); };
  var mb = function (n) { return (n / 1048576).toFixed(1) + 'MB'; };

  /* ================= 화면 조각 ================= */
  function row(t, sub) {
    return '<div class="track' + (sub ? ' sub' : '') + '" data-a="edit" data-id="' + t.id + '"><div class="cover"></div><div><b>' + esc(t.name) + '</b><small>' + esc(artistOf(t)) + '</small></div><div class="meta">' + mb(t.size) + '</div></div>';
  }
  function homeList() {
    var us = units();
    if (!us.length) return '<div class="empty">' + (S.tracks.length ? '찾는 이름이 없어요' : '아직 음악이 없어요<br>업로드 탭에서 파일을 추가하세요') + '</div>';
    return us.map(function (u) {
      if (u.single) return row(u.single, false);
      var open = S.open[u.b] || !!S.q.trim();
      var h = '<button class="bundle" data-a="tog" data-b="' + u.b + '" aria-expanded="' + open + '"><span class="stack"><span class="cover"></span><span class="cover"></span><span class="cover"></span></span>' +
        '<span><b>' + esc(S.bundles[u.b]) + ' 계열<span class="badge">' + u.all.length + '곡</span></b><small>비슷한 노래 묶음 · ' + (open ? '탭해서 접기' : '탭해서 펼치기') + '</small></span><span class="chev"></span></button>';
      if (open) h += u.items.map(function (t) { return row(t, true); }).join('');
      return h;
    }).join('');
  }
  function homeScreen() {
    var bn = Object.keys(S.bundles).length;
    return '<div class="top"><h2>음악 이름 관리</h2><span class="count">' + S.tracks.length + '곡 · 묶음 ' + bn + '</span></div>' +
      '<div class="search"><b>검색</b><input id="q" type="text" placeholder="이름·가수 검색" value="' + esc(S.q) + '"></div>' +
      '<div class="sorts">' + [['new', '최근 추가'], ['title', '제목순'], ['artist', '가수순']].map(function (o) { return '<button class="chip" data-a="sort" data-v="' + o[0] + '" aria-pressed="' + (S.sort === o[0]) + '">' + o[1] + '</button>'; }).join('') + '</div>' +
      '<div class="tools"><button class="btn sm" data-a="copy">이름 복사</button><button class="btn sm main" data-a="txt">목록 txt 추출</button><button class="btn sm" data-a="goup">＋ 추가</button></div>' +
      '<div class="sw"><div class="scroll">' + homeList() + '</div><div class="fade">↓ 아래로 스크롤</div></div>';
  }
  function upScreen() {
    var h = '<div class="top"><h2>음악 업로드</h2></div><div class="sw"><div class="scroll">';
    h += '<div class="drop"><div class="big">⇪</div><b style="color:var(--text)">음악 파일을 골라 이름을 추가해요</b><button class="btn main" data-a="pick">파일 선택</button><small>여러 곡을 한 번에 고를 수 있어요. 파일은 복사하지 않고 이름만 저장해요.</small></div>';
    h += '<input type="file" id="file" multiple accept="audio/*,.mp3,.m4a,.flac,.wav,.ogg,.aac" style="display:none">';
    if (S.files.length) {
      h += '<h3>선택한 파일 ' + S.files.length + '개</h3>' + S.files.map(function (f) {
        var c = conflictOf(f.name), k = c ? c.kind : 'new', lab = { same: ['같은 이름', 'dup'], sim: ['비슷한 이름', 'dup'], 'new': ['새 파일', 'ok'] }[k];
        return '<div class="file"><b>' + esc(f.name) + esc(f.ext) + '</b><span class="tag ' + lab[1] + '">' + lab[0] + '</span><small>' + mb(f.size) + (f.artist ? ' · ' + esc(f.artist) : '') + '</small></div>';
      }).join('');
    } else h += '<div class="empty">선택한 파일이 없어요<br>파일 선택을 눌러 보세요</div>';
    h += '</div><div class="fade">↓ 아래로 스크롤</div></div>';
    h += '<div class="foot"><div class="row2"><button class="btn" data-a="clear"' + (S.files.length ? '' : ' disabled') + '>비우기</button><button class="btn main" data-a="start"' + (S.files.length ? '' : ' disabled') + '>업로드 시작</button></div></div>';
    return h;
  }
  function opt(v, cur, ic, title, desc, cls) {
    return '<button class="opt ' + (cls || '') + '" role="radio" aria-checked="' + (cur === v) + '" data-a="choose" data-v="' + v + '"><span class="ic">' + ic + '</span><span><b>' + title + '</b><small>' + desc + '</small></span></button>';
  }
  function dupSheet() {
    var c = S.cur, f = c.file, old = c.target, sim = c.kind === 'sim', cur = c.res || (sim ? 'diff' : 'copy');
    var left = S.files.length - 1;
    var h = '<div class="sheet"><div class="grab"></div><h4>' + (sim ? '비슷한 이름이 있어요' : '같은 이름이 있어요') + '</h4><p>남은 파일 ' + left + '개</p>';
    h += '<div class="cmp"><div><em>이미 있는 파일</em><b>' + esc(old.name) + '</b><small>' + mb(old.size) + '<br>' + esc(artistOf(old)) + '</small></div><div class="nw"><em>새로 올릴 파일</em><b>' + esc(f.name) + '</b><small>' + mb(f.size) + '<br>' + esc(f.artist || '가수 미상') + '</small></div></div>';
    if (sim) {
      h += '<div class="q">이 파일은 다른 음악인가요?</div>';
      h += opt('diff', cur, '≈', '다른 음악이에요', '비슷한 노래 묶음에 같이 넣어요 (이름은 그대로)');
      h += opt('over', cur, '⇄', '같은 음악이에요 · 덮어쓰기', '기존 이름·정보를 새 파일 것으로 바꿔요');
      h += opt('skip', cur, '✕', '같은 음악이에요 · 지우기', '새 파일은 올리지 않고 버려요', 'danger');
    } else {
      h += opt('copy', cur, '+', '따로 저장', '이름 뒤에 (1)을 붙여 둘 다 보관해요');
      h += opt('over', cur, '⇄', '덮어쓰기', '기존 정보를 새 파일 것으로 바꿔요');
      h += opt('skip', cur, '✕', '지우기', '새 파일은 올리지 않고 버려요', 'danger');
    }
    if (left > 0) h += '<button class="chk" role="checkbox" aria-checked="' + !!S.allOn + '" data-a="all"><i>' + (S.allOn ? '✓' : '') + '</i> 남은 ' + (sim ? '비슷한 이름' : '같은 이름') + ' 파일도 같은 방식으로 처리</button>';
    h += '<div class="row2"><button class="btn" data-a="cancel">취소</button><button class="btn main" data-a="ok">확인</button></div></div>';
    return h;
  }
  function txtSheet() {
    var L = textLines(), head = '음악 목록 (' + L.length + '곡)', ex = flat()[0];
    var e = function (n) { return ex ? fmtLine(ex, n) : ''; };
    return '<div class="sheet"><div class="grab"></div><h4>목록 텍스트 추출</h4><p>지금 보이는 ' + L.length + '곡을 텍스트로 만들어요. 비슷한 노래 묶음은 나란히 적혀요.</p><div class="muted2">형식</div>' +
      opt(1, S.fmt, '1', '이름만', esc(e(1))) + opt(2, S.fmt, '2', '가수 - 이름', esc(e(2))) + opt(3, S.fmt, '3', '이름 (확장자 포함)', esc(e(3))) +
      '<div class="muted2">미리보기</div><div class="pre">' + esc(head + '\n' + L.join('\n')) + '</div>' +
      '<div class="row2"><button class="btn" data-a="copy2">이름 복사</button><button class="btn main" data-a="save"' + (L.length ? '' : ' disabled') + '>txt 파일로 저장</button></div></div>';
  }
  function editSheet() {
    var t = find(S.sel); if (!t) return '';
    return '<div class="sheet"><div class="grab"></div><h4>곡 정보</h4>' +
      '<div class="muted2">이름</div><input class="in" id="en" type="text" value="' + esc(t.name) + '">' +
      '<div class="muted2">가수</div><input class="in" id="ea" type="text" value="' + esc(t.artist || '') + '" placeholder="가수 미상">' +
      (t.bundle ? '<button class="opt" data-a="unbundle"><span class="ic">≈</span><span><b>묶음에서 빼기</b><small>' + esc(S.bundles[t.bundle] || '') + ' 계열에서 따로 나와요</small></span></button>' : '') +
      '<button class="opt danger" data-a="del"><span class="ic">✕</span><span><b>' + (S.confirmDel ? '정말 삭제할까요? 한 번 더 누르세요' : '삭제') + '</b><small>목록에서만 지워요. 기기의 파일은 그대로예요</small></span></button>' +
      '<div class="row2"><button class="btn" data-a="cancel">닫기</button><button class="btn main" data-a="saveedit">저장</button></div></div>';
  }
  function view() {
    var h = '<div class="screen">' + (S.screen === 'home' ? homeScreen() : upScreen());
    if (S.sheet) h += '<div class="dim" data-a="dimclick">' + (S.sheet === 'txt' ? txtSheet() : S.sheet === 'edit' ? editSheet() : dupSheet()) + '</div>';
    if (S.toast) h += '<div class="toast">' + esc(S.toast) + '</div>';
    h += '</div><div class="nav"><button data-a="gohome" aria-current="' + (S.screen === 'home') + '"><span>☰</span>목록</button><button data-a="goup" aria-current="' + (S.screen === 'up') + '"><span>⇪</span>업로드</button></div>';
    return h;
  }

  /* ================= 동작 ================= */
  var tt = null;
  function toast(msg) { S.toast = msg; draw(); clearTimeout(tt); tt = setTimeout(function () { S.toast = ''; draw(); }, 2200); }

  // 파일을 차례로 처리: 충돌이 없으면 바로 추가, 충돌이 있으면 시트를 띄우고 기다린다
  var stats = { added: 0, skipped: 0 };
  function mkTrack(f, name) { return { id: S.next++, name: name, artist: f.artist || '', size: f.size, ext: f.ext, bundle: null, t: Date.now() + S.next }; }
  function uniqueCopy(name) {
    var n = 1, cand; do { cand = name + ' (' + n + ')'; n++; } while (S.tracks.some(function (t) { return norm(t.name) === norm(cand); }));
    return cand;
  }
  function apply(c, res) {
    var f = c.file, t0 = c.target;
    if (res === 'skip') { stats.skipped++; return; }
    if (res === 'over') { t0.name = f.name; t0.artist = f.artist || t0.artist; t0.size = f.size; t0.ext = f.ext; t0.t = Date.now(); stats.added++; return; }
    var n = mkTrack(f, res === 'copy' ? uniqueCopy(f.name) : f.name);
    if (res === 'diff') {
      if (!t0.bundle || !S.bundles[t0.bundle]) { var k = 'b' + (S.nb++); S.bundles[k] = t0.name; t0.bundle = k; }
      n.bundle = t0.bundle; S.open[t0.bundle] = true;
    }
    S.tracks.push(n); stats.added++;
  }
  function run() {
    while (S.files.length) {
      var f = S.files[0], c = conflictOf(f.name);
      if (!c) { S.files.shift(); S.tracks.push(mkTrack(f, f.name)); stats.added++; continue; }
      var pre = S.all[c.kind];
      if (pre) { S.files.shift(); apply({ file: f, target: c.target }, pre); continue; }
      S.cur = { file: f, kind: c.kind, target: c.target, res: null }; S.sheet = 'ask'; S.allOn = false; draw(); return;
    }
    S.cur = null; S.sheet = null; S.screen = 'home'; S.sort = 'new'; S.q = ''; S.all = { same: null, sim: null };
    persist();
    var msg = stats.added + '곡을 추가했어요' + (stats.skipped ? ' · ' + stats.skipped + '곡은 올리지 않았어요' : '');
    stats = { added: 0, skipped: 0 };
    toast(msg);
  }
  async function addFiles(list) {
    var arr = Array.prototype.slice.call(list).filter(function (f) { return AUDIO.test(f.name); }), skipped = list.length - arr.length;
    if (!arr.length) { toast('음악 파일이 아니에요'); return; }
    for (var i = 0; i < arr.length; i++) {
      var m = arr[i].name.match(/^(.*?)(\.[^.]+)$/);
      var name = m ? m[1] : arr[i].name, ext = m ? m[2].toLowerCase() : '.mp3';
      if (S.files.some(function (x) { return norm(x.name) === norm(name); })) continue;
      S.files.push({ name: name, ext: ext, size: arr[i].size, artist: await readArtist(arr[i]) });
    }
    S.screen = 'up'; draw();
    if (skipped) toast('음악 파일만 추가했어요 (' + skipped + '개 제외)');
  }
  function copyText(text) {
    var done = function (ok) { toast(ok ? '복사했어요' : '복사에 실패했어요'); };
    var fallback = function () {
      try { var ta = document.createElement('textarea'); ta.value = text; document.body.appendChild(ta); ta.select(); var ok = document.execCommand('copy'); ta.remove(); done(ok); } catch (e) { done(false); }
    };
    if (navigator.clipboard && navigator.clipboard.writeText) navigator.clipboard.writeText(text).then(function () { done(true); }, fallback); else fallback();
  }
  function saveTxt() {
    var L = textLines(); if (!L.length) { toast('추출할 목록이 없어요'); return; }
    var text = '음악 목록 (' + L.length + '곡)\n' + L.join('\n') + '\n', d = new Date(), p = function (v) { return String(v).padStart(2, '0'); };
    var name = '음악목록_' + d.getFullYear() + p(d.getMonth() + 1) + p(d.getDate()) + '.txt';
    if (window.AndroidBridge && window.AndroidBridge.saveText) { window.AndroidBridge.saveText(name, text); S.sheet = null; draw(); return; }
    try {
      var url = URL.createObjectURL(new Blob([text], { type: 'text/plain;charset=utf-8' })), a = document.createElement('a');
      a.href = url; a.download = name; document.body.appendChild(a); a.click(); a.remove(); setTimeout(function () { URL.revokeObjectURL(url); }, 2000);
      S.sheet = null; draw();
    } catch (e) { copyText(text); }
  }
  function act(el, ev) {
    var a = el.dataset.a;
    switch (a) {
      case 'gohome': S.screen = 'home'; S.sheet = null; break;
      case 'goup': S.screen = 'up'; S.sheet = null; break;
      case 'sort': S.sort = el.dataset.v; break;
      case 'tog': S.open[el.dataset.b] = !S.open[el.dataset.b]; break;
      case 'copy': { var L = flat().map(function (t) { return t.name; }); if (!L.length) { toast('복사할 목록이 없어요'); return; } copyText(L.join('\n')); return; }
      case 'copy2': copyText(textLines().join('\n')); return;
      case 'save': saveTxt(); return;
      case 'txt': S.sheet = 'txt'; break;
      case 'pick': { var inp = $('#file'); if (inp) inp.click(); return; }
      case 'clear': S.files = []; break;
      case 'start': stats = { added: 0, skipped: 0 }; S.all = { same: null, sim: null }; run(); return;
      case 'choose':
        if (S.sheet === 'txt') { S.fmt = parseInt(el.dataset.v, 10); persist(); } else S.cur.res = el.dataset.v;
        break;
      case 'all': S.allOn = !S.allOn; break;
      case 'cancel': S.sheet = null; S.cur = null; S.confirmDel = false; persist(); if (stats.added || stats.skipped) { toast(stats.added + '곡을 추가하고 멈췄어요'); stats = { added: 0, skipped: 0 }; return; } break;
      case 'dimclick': if (!ev.target.closest('.sheet')) { S.sheet = null; S.cur = null; S.confirmDel = false; persist(); } break;
      case 'ok': {
        var c = S.cur, res = c.res || (c.kind === 'sim' ? 'diff' : 'copy');
        if (S.allOn) S.all[c.kind] = res;
        S.files.shift(); apply(c, res); persist(); run(); return;
      }
      case 'edit': S.sel = parseInt(el.dataset.id, 10); S.sheet = 'edit'; S.confirmDel = false; break;
      case 'saveedit': {
        var t = find(S.sel), nm = $('#en').value.trim();
        if (!nm) { toast('이름을 입력하세요'); return; }
        t.name = nm; t.artist = $('#ea').value.trim(); S.sheet = null; persist(); toast('저장했어요'); return;
      }
      case 'unbundle': {
        var t2 = find(S.sel), b = t2.bundle; t2.bundle = null;
        var rest = S.tracks.filter(function (x) { return x.bundle === b; });
        if (rest.length < 2) { rest.forEach(function (x) { x.bundle = null; }); delete S.bundles[b]; }
        S.sheet = null; persist(); toast('묶음에서 뺐어요'); return;
      }
      case 'del':
        if (!S.confirmDel) { S.confirmDel = true; break; }
        var t3 = find(S.sel), b3 = t3.bundle;
        S.tracks = S.tracks.filter(function (x) { return x.id !== t3.id; });
        if (b3) { var r3 = S.tracks.filter(function (x) { return x.bundle === b3; }); if (r3.length < 2) { r3.forEach(function (x) { x.bundle = null; }); delete S.bundles[b3]; } }
        S.sheet = null; S.confirmDel = false; persist(); toast('삭제했어요'); return;
    }
    draw();
  }

  /* ================= 그리기 ================= */
  var root = $('#app');
  function more() {
    var sc = $('.scroll', root), sw = $('.sw', root); if (!sc || !sw) return;
    sw.classList.toggle('more', sc.scrollHeight - sc.scrollTop - sc.clientHeight > 6);
  }
  function draw() {
    var keep = {};
    ['.scroll', '.sheet', '.pre'].forEach(function (q) { var n = $(q, root); if (n) keep[q] = n.scrollTop; });
    var ae = document.activeElement, focus = ae && ae.id === 'q' ? ae.selectionStart : null;
    var fileEl = $('#file', root), keepFile = fileEl ? fileEl : null;
    root.innerHTML = view();
    ['.scroll', '.sheet', '.pre'].forEach(function (q) { var n = $(q, root); if (n && keep[q] != null) n.scrollTop = keep[q]; });
    if (focus != null) { var i = $('#q', root); i.focus(); i.setSelectionRange(focus, focus); }
    more();
  }
  root.addEventListener('click', function (e) { var b = e.target.closest('[data-a]'); if (b && root.contains(b)) act(b, e); });
  root.addEventListener('input', function (e) { if (e.target.id === 'q') { S.q = e.target.value; draw(); } });
  root.addEventListener('change', function (e) { if (e.target.id === 'file') { var fl = e.target.files; addFiles(fl).then(function () { e.target.value = ''; }); } });
  root.addEventListener('scroll', function (e) { if (e.target.classList && e.target.classList.contains('scroll')) more(); }, true);
  window.addEventListener('resize', more);

  // 안드로이드 뒤로 가기: 시트가 열려 있으면 닫고 true, 아니면 false
  window.onAppBack = function () {
    if (S.sheet) { S.sheet = null; S.cur = null; S.confirmDel = false; persist(); draw(); return true; }
    if (S.screen !== 'home') { S.screen = 'home'; draw(); return true; }
    return false;
  };

  (async function init() {
    var ok = await Store.open();
    var st = await Store.load();
    if (st && Array.isArray(st.tracks)) { S.tracks = st.tracks; S.bundles = st.bundles || {}; S.nb = st.nb || 1; S.next = st.next || 1; S.fmt = st.fmt || 1; }
    draw();
    if (!ok) toast('저장소를 쓸 수 없어 이번 실행 동안만 보관돼요');
  })();
})();
