/* StoryEditor 화면: 프로젝트 목록 / 페이지·선택지 에디터 / 미리보기 / 검사 */
(function () {
  'use strict';
  const E = window.SEngine;
  const $ = (s, r) => (r || document).querySelector(s);
  const esc = (s) => String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const COLORS = ['#6d8cff', '#a78bfa', '#f472b6', '#fb923c', '#facc15', '#4ade80', '#2dd4bf', '#94a3b8'];
  const KEY = 'storyeditor.projects.v1';
  const MODE_KEY = 'storyeditor.listmode';

  const S = { projects: [], view: 'home', pid: null, pn: 0, q: '', side: false, memOnly: false, trail: [], editCi: null, folder: 'main', listMode: 'num', collapsed: new Set() };

  /* ================= 저장 ================= */
  function load() {
    try {
      const v = JSON.parse(localStorage.getItem(KEY) || '[]');
      if (Array.isArray(v)) S.projects = v;
      // 묶음 기능 이전에 만든 프로젝트 보정
      S.projects.forEach((pj) => { if (!Array.isArray(pj.groups)) pj.groups = []; pj.pages.forEach((pg) => { if (typeof pg.group !== 'string') pg.group = ''; }); });
    }
    catch (e) { S.memOnly = true; }
  }
  // 튜토리얼은 항상 있다. 저장된 버전이 낮으면 새 내용으로 교체한다 (기능이 추가되면 VERSION 이 올라감)
  function ensureTutorial() {
    const T = window.STutorial;
    if (!T) return;
    const i = S.projects.findIndex((p) => p.id === T.ID);
    if (i >= 0 && (S.projects[i].tutorialVersion || 0) >= T.VERSION) return;
    const t = T.build();
    if (i >= 0) { t.createdAt = S.projects[i].createdAt || t.createdAt; S.projects[i] = t; S.tutUpdated = true; }
    else S.projects.unshift(t);
    saveNow();
  }
  let saveTimer;
  function save() {
    clearTimeout(saveTimer);
    saveTimer = setTimeout(saveNow, 250);
  }
  function saveNow() {
    clearTimeout(saveTimer);
    try { localStorage.setItem(KEY, JSON.stringify(S.projects)); }
    catch (e) { if (!S.memOnly) { S.memOnly = true; toast('저장 공간을 쓸 수 없어요. 내보내기로 꼭 백업하세요'); } }
  }
  window.addEventListener('pagehide', saveNow);
  document.addEventListener('visibilitychange', () => { if (document.hidden) saveNow(); });

  const proj = () => S.projects.find((p) => p.id === S.pid) || null;
  const page = () => { const pj = proj(); return pj ? E.pageOf(pj, S.pn) : null; };
  const touch = () => { const pj = proj(); if (pj) pj.updatedAt = Date.now(); save(); };

  /* ================= 유틸 ================= */
  let toastTimer;
  function toast(msg) {
    const t = $('#toast'); t.textContent = msg; t.style.display = 'block';
    clearTimeout(toastTimer); toastTimer = setTimeout(() => { t.style.display = 'none'; }, 2600);
  }
  function modal(html, cls) {
    const el = document.createElement('div');
    el.className = 'modal' + (cls ? ' ' + cls : '');
    el.innerHTML = '<div>' + html + '</div>';
    $('#modalRoot').appendChild(el);
    return { el, close() { el.remove(); } };
  }
  function ask(msg, okLabel) {
    return new Promise((res) => {
      const m = modal('<p style="margin-top:0;white-space:pre-wrap">' + esc(msg) + '</p><div class="row" style="justify-content:flex-end"><button data-x="0">취소</button><button class="pri" data-x="1">' + esc(okLabel || '확인') + '</button></div>');
      m.el.addEventListener('click', (e) => { const b = e.target.closest('[data-x]'); if (b) { m.close(); res(b.dataset.x === '1'); } });
    });
  }
  const fmtDate = (t) => new Date(t).toLocaleDateString();
  const firstLine = (s) => (s || '').split('\n').find((l) => l.trim()) || '(비어 있음)';

  /* ================= 헤더 / 라우팅 ================= */
  function go(view, pid, pn) {
    saveNow();
    S.view = view; if (pid !== undefined) S.pid = pid; if (pn !== undefined) S.pn = pn;
    render();
  }
  function renderHeader() {
    const hd = $('#hd');
    if (S.view === 'home') {
      hd.innerHTML = '<div class="ttl">📖 나만의 이야기 에디터</div><button data-act="import">가져오기</button><button class="pri" data-act="newproj">＋ 새 프로젝트</button>';
    } else if (S.view === 'flow') {
      const pj = proj();
      hd.innerHTML = '<button data-act="flowexit">‹ 편집</button><div class="ttl">🔀 ' + esc(pj.name) + '</div>' +
        '<button data-act="json">{ } JSON</button><button class="pri" data-act="playhere">▶ 여기서</button>';
    } else {
      const pj = proj();
      hd.innerHTML = '<button data-act="home">‹ 목록</button><div class="ttl">' + esc(pj.name) + '</div>' +
        '<button class="tgl-side" data-act="toggleside">☰ 페이지</button>' +
        '<button data-act="flow">🔀 분기도</button><button data-act="achvs">🏆 업적</button><button data-act="check">검사</button><button data-act="settings">⚙</button><button class="pri" data-act="play0">▶ 미리보기</button>';
    }
  }
  function render() {
    if (S.view !== 'flow' && window.SFlow) window.SFlow.unmount();
    renderHeader();
    $('#main').className = S.view === 'flow' ? 'flowmode' : '';
    if (S.view === 'home') renderHome(); else if (S.view === 'flow') renderFlow(); else renderEditor();
  }

  /* ================= 홈 ================= */
  function renderHome() {
    let h = '';
    if (S.memOnly) h += '<div class="hint">⚠ 이 기기에서 저장 공간을 쓸 수 없어요. 앱을 닫기 전에 꼭 내보내기로 백업하세요.</div>';
    if (!S.projects.length) {
      h += '<div class="card" style="text-align:center;padding:30px 14px"><div style="font-size:15px">아직 프로젝트가 없어요</div><div class="muted" style="margin:6px 0 14px">페이지와 선택지를 이어서 나만의 분기형 이야기를 써 보세요.</div><button class="pri" data-act="newproj">＋ 새 프로젝트</button> <button data-act="sample">예시 프로젝트 만들기</button></div>';
    } else {
      const isTut = (p) => p.id === (window.STutorial && window.STutorial.ID);
      const order = S.projects.slice().sort((a, b) => (isTut(b) - isTut(a)) || (b.updatedAt - a.updatedAt));
      h += '<div class="grid">' + order.map((p) => {
        const tut = isTut(p);
        return '<div class="card' + (tut ? ' tut' : '') + '" style="margin:0"><div class="cover" style="background:' + COLORS[p.color % COLORS.length] + '"></div>' +
          '<div class="row"><b class="grow" style="word-break:break-all">' + esc(p.name) + '</b>' + (p.tag ? '<span class="tag">' + esc(p.tag) + '</span>' : '') + '</div>' +
          '<div class="muted">페이지 ' + p.pages.length + '개 · 업적 ' + p.achievements.length + '개 · ' + (tut ? '고정 예시 · 기능이 추가되면 자동 갱신' : fmtDate(p.updatedAt)) + '</div>' +
          '<div class="row" style="margin-top:10px">' + (tut ? '<button class="pri sm" data-act="play" data-id="' + p.id + '">▶ 튜토리얼 시작</button><button class="sm" data-act="open" data-id="' + p.id + '">편집기로 보기</button>' : '<button class="pri sm" data-act="open" data-id="' + p.id + '">편집</button><button class="sm" data-act="play" data-id="' + p.id + '">▶ 미리보기</button>') +
          '<button class="sm" data-act="export" data-id="' + p.id + '">내보내기</button><button class="sm" data-act="dup" data-id="' + p.id + '">' + (tut ? '복제해서 실험' : '복제') + '</button>' +
          (tut ? '<button class="sm" data-act="tutreset" data-id="' + p.id + '">원래대로</button>' : '<button class="sm dng" data-act="delproj" data-id="' + p.id + '">삭제</button>') + '</div></div>';
      }).join('') + '</div>';
    }
    h += '<div class="muted" style="margin-top:14px;text-align:center">처음이라면 위의 📘 튜토리얼부터 시작해 보세요 · <button class="sm ghost" data-act="sample">＋ 간단한 예시 프로젝트 추가</button></div>';
    $('#main').innerHTML = h;
  }

  function projectForm(pj, onOk) {
    const isNew = !pj;
    const cur = pj || { name: '', tag: '', color: 0 };
    const m = modal('<h3 style="margin-top:0">' + (isNew ? '새 프로젝트' : '프로젝트 설정') + '</h3>' +
      '<div class="muted">이름</div><input type="text" id="pfName" value="' + esc(cur.name) + '" maxlength="40">' +
      '<div class="muted" style="margin-top:8px">태그 (선택)</div><input type="text" id="pfTag" value="' + esc(cur.tag) + '" maxlength="20">' +
      '<div class="muted" style="margin:8px 0 4px">커버 색</div><div id="pfColors">' + COLORS.map((c, i) => '<span class="sw' + (i === cur.color ? ' on' : '') + '" data-c="' + i + '" style="background:' + c + '"></span>').join('') + '</div>' +
      (isNew ? '' : statsFormHtml(pj)) +
      '<div class="row" style="justify-content:flex-end;margin-top:14px"><button data-x="0">취소</button><button class="pri" data-x="1">저장</button></div>');
    let color = cur.color;
    m.el.addEventListener('click', (e) => {
      const sw = e.target.closest('[data-c]');
      if (sw) { color = +sw.dataset.c; m.el.querySelectorAll('.sw').forEach((s) => s.classList.toggle('on', +s.dataset.c === color)); return; }
      if (e.target.closest('[data-addstat]')) {
        const box = m.el.querySelector('#statBox');
        box.insertAdjacentHTML('beforeend', statRowHtml({ id: E.uid('s'), name: '', init: 0, max: 0 }));
        return;
      }
      const rm = e.target.closest('[data-rmstat]');
      if (rm) { rm.closest('.statrow').remove(); return; }
      const b = e.target.closest('[data-x]'); if (!b) return;
      if (b.dataset.x === '0') return m.close();
      const name = $('#pfName', m.el).value.trim() || '이름 없는 이야기';
      const stats = isNew ? null : Array.from(m.el.querySelectorAll('.statrow')).map((r) => ({
        id: r.dataset.id, name: r.querySelector('.sn').value.trim(), init: Math.max(0, parseInt(r.querySelector('.si').value, 10) || 0), max: Math.max(0, parseInt(r.querySelector('.sm').value, 10) || 0),
      })).filter((s) => s.name);
      m.close(); onOk({ name, tag: $('#pfTag', m.el).value.trim(), color, stats });
    });
    setTimeout(() => $('#pfName', m.el).focus(), 30);
  }
  const statRowHtml = (s) => '<div class="prop statrow" data-id="' + esc(s.id) + '"><input type="text" class="sn" placeholder="이름" value="' + esc(s.name) + '"><input type="number" class="si" min="0" title="시작값" value="' + s.init + '"><input type="number" class="sm" min="0" title="최대치 (0=제한 없음)" value="' + s.max + '"><button class="x" data-rmstat="1">✕</button></div>';
  const statsFormHtml = (pj) => '<div class="muted" style="margin:14px 0 4px">상태값 (이름 · 시작값 · 최대치, 최대치 0 = 제한 없음)</div><div id="statBox">' + pj.stats.map(statRowHtml).join('') + '</div><button class="sm" data-addstat="1" style="margin-top:6px">＋ 상태값 추가</button>';

  /* ================= 가져오기 / 내보내기 ================= */
  function exportProject(pj) {
    const blob = new Blob([JSON.stringify(pj, null, 1)], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = (pj.name || 'story').replace(/[\\/:*?"<>|]/g, '_') + '.json';
    document.body.appendChild(a); a.click(); a.remove();
    setTimeout(() => URL.revokeObjectURL(a.href), 4000);
    toast('내보냈어요');
  }
  function importText(text) {
    let data;
    try { data = JSON.parse(text); } catch (e) { toast('JSON 파일이 아니에요'); return; }
    const list = Array.isArray(data) ? data : data && Array.isArray(data.projects) ? data.projects : [data];
    let n = 0;
    list.forEach((raw) => { try { S.projects.push(E.normalizeProject(raw)); n++; } catch (e) { /* 건너뜀 */ } });
    if (!n) { toast('올바른 프로젝트 파일이 아니에요'); return; }
    saveNow(); render(); toast(n + '개를 가져왔어요');
  }

  /* ================= 예시 ================= */
  function samplePj() {
    const pj = E.newProject('예시: 낡은 열쇠', '예시', 1);
    pj.achievements.push({ id: 'a_key', name: '열쇠 수집가' }, { id: 'a_end', name: '무사 탈출' });
    const P = (n, body, extra) => Object.assign(E.newPage(n), { body }, extra || {});
    const C = (text, next, extra) => { const c = E.newChoice(next); c.text = text; return Object.assign(c, extra || {}); };
    const p0 = P(0, '낡은 창고에서 눈을 떴다. 문은 잠겨 있고, 바닥에는 녹슨 열쇠가 떨어져 있다.');
    const k = C('열쇠를 줍는다', 1); k.base.gain = '낡은 열쇠'; k.base._p = ['gain', 'achv']; k.base.achv = 'a_key';
    p0.choices = [k, C('열쇠를 무시하고 문을 민다', 2)];
    const p1 = P(1, '열쇠가 손에 들어왔다. 어딘가에 맞는 자물쇠가 있을 것이다.');
    const open = C('문을 열쇠로 연다', 3, { enable: '낡은 열쇠' });
    const push = C('열쇠 없이 문을 민다', 2, { show: '!낡은 열쇠' });
    p1.choices = [open, push];
    const p2 = P(2, '문은 꿈쩍도 하지 않는다. 어깨가 욱신거린다.');
    const back = C('다시 바닥을 살핀다', 1); back.base.stats = { hp: -1 }; back.base._p = ['stat:hp'];
    p2.choices = [back];
    const p3 = P(3, '자물쇠가 돌아가고, 문이 끼익 열렸다. 바깥 공기가 차갑다.');
    const gamble = C('어두운 복도를 달린다', 4, { branchType: 'prob' });
    gamble.branches = [Object.assign(E.newOutcome(5), { when: 30, gain: '찰과상' , _p: ['gain']})];
    p3.choices = [gamble];
    const p4 = P(4, '복도 끝의 비상구를 찾았다. 당신은 무사히 빠져나왔다.', { isEnd: true });
    const p5 = P(5, '발을 헛디뎌 넘어졌지만 그래도 비상구에 닿았다.', { isEnd: true });
    p4.isEnd = true; p5.isEnd = true;
    // 스토리 묶음과 랜덤 이벤트 예시
    pj.groups = [
      { id: 'g_main', name: '1장 · 창고 탈출', folder: 'main', start: null },
      { id: 'g_cat', name: '길고양이', folder: 'random', start: null },
      { id: 'g_shop', name: '낯선 상인', folder: 'random', start: null },
    ];
    [p0, p1, p2, p3, p4, p5].forEach((pg) => { pg.group = 'g_main'; });
    const r1 = P(6, '복도 구석에서 길고양이가 울고 있다. 품에 안아 올리자 목걸이에서 작은 열쇠가 떨어졌다.', { folder: 'random', group: 'g_cat' });
    r1.choices = [C('열쇠를 챙기고 다음으로', 3)]; r1.choices[0].base.mode = 'continue';
    r1.choices[0].base.gain = '고양이 열쇠'; r1.choices[0].base._p = ['gain'];
    const r2 = P(7, '어둠 속에서 낯선 상인이 손짓한다. “이 지도, 싸게 줄게.”', { folder: 'random', group: 'g_shop' });
    r2.choices = [C('지도를 받고 다음으로', 3)]; r2.choices[0].base.mode = 'continue';
    r2.choices[0].base.gain = '낡은 지도'; r2.choices[0].base._p = ['gain'];
    const odd = C('수상한 소리를 따라간다', 3); odd.base.mode = 'random'; odd.base.pick = { min: 1, max: 2 }; odd.base.after = { mode: 'page', next: 4, group: '' };
    p3.choices.push(odd);
    pj.pages = [p0, p1, p2, p3, p4, p5, r1, r2];
    gamble.base.achv = 'a_end'; gamble.base._p = ['achv'];
    return pj;
  }

  /* ================= 에디터 ================= */
  // 현재 폴더에 속한 묶음 (페이지가 다른 폴더의 묶음 id 를 갖고 있으면 묶음 없음으로 취급)
  const folderGroupOf = (pj, p) => { const g = E.groupOf(pj, p.group); return g && g.folder === E.folderOf(p) ? g : null; };

  function sideHtml(pj) {
    const q = S.q.trim().toLowerCase();
    const byFolder = S.listMode === 'folder';
    const matches = (p) => !q || String(p.n) === q || p.body.toLowerCase().includes(q) || (p.title || '').toLowerCase().includes(q) || E.groupNameOf(pj, p).toLowerCase().includes(q);
    const modes = '<div class="seg"><button data-act="mode" data-mode="num" class="' + (!byFolder ? 'on' : '') + '">번호순</button><button data-act="mode" data-mode="folder" class="' + (byFolder ? 'on' : '') + '">폴더</button></div>';
    const tabs = byFolder ? '<div class="ftabs">' + E.FOLDERS.map((f) => '<button class="ft' + (f.id === S.folder ? ' on' : '') + '" data-act="folder" data-folder="' + f.id + '">' + esc(f.name) + ' <span>' + E.pagesIn(pj, f.id).length + '</span></button>').join('') + '</div>' : '';
    const cur = E.FOLDERS.find((f) => f.id === S.folder);
    const addTitle = byFolder ? cur.name + '에 페이지 추가' : '페이지 추가';
    const pageRow = (p, chips) => '<div class="pi' + (p.n === S.pn ? ' on' : '') + (p.isEnd ? ' end' : '') + '" data-act="gopage" data-n="' + p.n + '"><b>P.' + p.n + '</b><span>' + esc(E.pageLabel(p)) + '</span>' + (chips || '') + '</div>';
    let list = '';
    if (!byFolder) {
      const all = pj.pages.slice().sort((a, b) => a.n - b.n).filter(matches);
      list = all.length ? all.map((p) => {
        const gn = E.groupNameOf(pj, p);
        return pageRow(p, '<em class="fchip">' + esc(E.folderShort(E.folderOf(p))) + (gn ? ' · ' + esc(gn) : '') + '</em>');
      }).join('') : '<div class="muted" style="padding:8px">' + (q ? '검색 결과 없음' : '페이지가 없어요.') + '</div>';
    } else if (q) {
      const found = E.pagesIn(pj, S.folder).filter(matches);
      list = found.length ? found.map((p) => { const gn = E.groupNameOf(pj, p); return pageRow(p, gn ? '<em class="fchip">' + esc(gn) + '</em>' : ''); }).join('') : '<div class="muted" style="padding:8px">검색 결과 없음</div>';
    } else {
      const inFolder = E.pagesIn(pj, S.folder);
      const groups = E.groupsIn(pj, S.folder);
      const ungrouped = inFolder.filter((p) => !folderGroupOf(pj, p));
      groups.forEach((g) => {
        const ps = inFolder.filter((p) => p.group === g.id);
        const open = !S.collapsed.has(g.id);
        list += '<div class="gh" data-act="gtoggle" data-g="' + g.id + '"><span class="car">' + (open ? '▾' : '▸') + '</span><b>' + esc(g.name) + '</b><em>' + ps.length + '</em>' +
          '<button class="sm" data-act="gadd" data-g="' + g.id + '" title="이 묶음에 페이지 추가">＋</button><button class="sm" data-act="gedit" data-g="' + g.id + '" title="묶음 이름·시작 페이지·삭제">✎</button></div>';
        if (open) list += '<div class="gbody">' + (ps.length ? ps.map((p) => pageRow(p, p.n === E.groupStart(pj, g) ? '<em class="fchip">시작</em>' : '')).join('') : '<div class="muted" style="padding:6px 8px">비어 있어요. 위의 ＋ 로 페이지를 추가하세요.</div>') + '</div>';
      });
      if (ungrouped.length) {
        if (groups.length) list += '<div class="gh plain"><b>묶음 없음</b><em>' + ungrouped.length + '</em></div>';
        list += ungrouped.map((p) => pageRow(p)).join('');
      }
      if (!list) list = '<div class="muted" style="padding:8px">' + esc(cur.name) + '가 비어 있어요.<br>＋ 로 첫 페이지를, ＋ 묶음 으로 스토리 묶음을 만들어 보세요.</div>';
    }
    return modes + tabs + '<div class="row" style="margin-bottom:6px"><input type="text" id="pq" class="grow" placeholder="페이지 번호·본문·묶음 검색" value="' + esc(S.q) + '"><button class="pri sm" data-act="addpage" title="' + esc(addTitle) + '">＋</button>' +
      (byFolder ? '<button class="sm" data-act="gnew" title="' + esc(cur.name) + '에 스토리 묶음 만들기">＋ 묶음</button>' : '') + '</div>' + list;
  }
  function renderEditor() {
    const pj = proj();
    if (!pj) return go('home');
    const cur = E.pageOf(pj, S.pn);
    if (cur) S.folder = E.folderOf(cur);              // 다른 폴더의 페이지로 이동하면 폴더 탭도 따라간다
    else if (S.listMode === 'folder') {
      const first = E.pagesIn(pj, S.folder)[0];
      S.pn = first ? first.n : -1;                    // -1 = 이 폴더에 페이지 없음
    } else {
      const all = pj.pages.slice().sort((a, b) => a.n - b.n);
      S.pn = all.length ? all[0].n : -1;
    }
    const tutBar = window.STutorial && pj.id === window.STutorial.ID ? '<div class="hint tutbar">📘 고정 튜토리얼이에요. 기능이 추가되면 새 내용으로 자동 갱신되고, 고친 내용은 사라져요. 실험은 홈에서 “복제해서 실험”을 쓰세요.</div>' : '';
    $('#main').innerHTML = tutBar + '<div class="ed' + (S.side ? '' : ' noside') + '"><div class="side" id="side">' + sideHtml(pj) + '</div><div id="pageBox"></div></div>';
    renderPage();
  }
  function renderSide() { const pj = proj(); const el = $('#side'); if (pj && el) { const sc = el.scrollTop; el.innerHTML = sideHtml(pj); el.scrollTop = sc; } }

  function renderPage() {
    const pj = proj(), p = page(), box = $('#pageBox');
    if (!box) return;
    if (!p) { box.innerHTML = '<div class="card">' + (S.listMode === 'folder' ? '이 폴더에 ' : '') + '페이지가 없어요. 왼쪽의 ＋ 로 추가하세요.' + (window.innerWidth <= 760 ? '<br><span class="muted">(☰ 페이지 버튼으로 목록을 열 수 있어요)</span>' : '') + '</div>'; return; }
    let h = '<div class="card"><div class="row"><h3 style="margin:0" class="grow">P.' + p.n + (p.n === 0 ? ' <span class="muted">(시작)</span>' : '') + '</h3>' +
      '<label class="muted">폴더 <select data-f="folder" style="width:auto;padding:3px 6px">' + E.FOLDERS.map((f) => '<option value="' + f.id + '"' + (f.id === E.folderOf(p) ? ' selected' : '') + '>' + esc(f.name) + '</option>').join('') + '</select></label>' +
      '<label class="muted">묶음 <select data-f="group" style="width:auto;max-width:150px;padding:3px 6px"><option value="">(없음)</option>' + E.groupsIn(pj, E.folderOf(p)).map((g) => '<option value="' + g.id + '"' + (g.id === p.group ? ' selected' : '') + '>' + esc(g.name) + '</option>').join('') + '<option value="__new">＋ 새 묶음 만들기…</option></select></label>' +
      '<label class="muted"><input type="checkbox" data-f="isEnd"' + (p.isEnd ? ' checked' : '') + '> 마지막 페이지</label>' +
      '<button class="sm" data-act="playhere">▶ 여기서</button><button class="sm" data-act="duppage">복제</button><button class="sm dng" data-act="delpage">삭제</button></div>' +
      '<input type="text" data-f="title" placeholder="제목 (선택, 분기도에 표시돼요)" value="' + esc(p.title || '') + '" maxlength="40" style="margin-top:8px">' +
      '<textarea data-f="body" placeholder="본문을 입력하세요" style="margin-top:8px">' + esc(p.body) + '</textarea>' +
      '<div class="muted"><span class="cnt' + (p.body.length > E.BODY_MAX ? ' over' : '') + '" id="cnt">' + p.body.length + '</span> / ' + E.BODY_MAX + '자</div></div>';
    if (p.isEnd) {
      h += '<div class="hint">마지막 페이지에서는 선택지를 쓰지 않아요. 미리보기에서 “다시 시작 / 종료”가 표시됩니다.' + (p.choices.length ? ' (기존 선택지 ' + p.choices.length + '개는 보관되지만 사용되지 않아요)' : '') + '</div>';
    } else {
      h += p.choices.map((c, i) => choiceHtml(pj, p, c, i)).join('');
      h += '<button data-act="addchoice" style="width:100%">＋ 선택지 추가</button>';
    }
    box.innerHTML = h;
  }



  // 랜덤 이벤트: 뽑을 개수(범위)와 모두 끝난 뒤 이어지는 곳
  function pickAfterHtml(pj, o, d) {
    const pk = E.pickRange(o), a = o.after && o.after.mode ? o.after : { mode: 'none', next: 0, group: '' };
    const n = E.randomEntries(pj, o).length;
    let h = '<div class="rrow"><label class="muted">뽑을 개수</label><input type="number" min="1" ' + d + ' data-f="pickmin" value="' + pk.min + '"><span>~</span><input type="number" min="1" ' + d + ' data-f="pickmax" value="' + pk.max + '"><span class="muted">개' + (pk.min === pk.max ? '' : ' (매번 이 범위에서 무작위)') + (pk.max > n && n ? ' · ⚠ 후보가 ' + n + '개뿐이라 최대 ' + n + '개' : '') + ' · 겹치지 않게</span></div>';
    h += '<div class="rrow"><label class="muted">모두 끝나면</label><select ' + d + ' data-f="aftermode" style="width:auto"><option value="none"' + (a.mode === 'none' ? ' selected' : '') + '>이어지는 곳 없음 (마지막 이야기가 끝나면 종료)</option><option value="page"' + (a.mode === 'page' ? ' selected' : '') + '>페이지로 이어짐</option><option value="group"' + (a.mode === 'group' ? ' selected' : '') + '>스토리 묶음으로 이어짐</option></select>';
    if (a.mode === 'page') h += '<label class="muted">P.</label><input type="number" min="0" ' + d + ' data-f="afternext" value="' + esc(a.next) + '">';
    if (a.mode === 'group') h += '<select class="grow" ' + d + ' data-f="aftergroup"><option value="">(묶음 선택)</option>' + E.FOLDERS.map((f) => { const gs = E.groupsIn(pj, f.id); return gs.length ? '<optgroup label="' + esc(f.name) + '">' + gs.map((g) => '<option value="' + g.id + '"' + (g.id === a.group ? ' selected' : '') + '>' + esc(g.name) + '</option>').join('') + '</optgroup>' : ''; }).join('') + '</select>';
    return h + '</div><div class="muted" style="margin:0 0 0 4px">각 이야기는 <b>마지막 페이지</b>로 끝내거나, 끝에 <b>“다음 이야기로”</b> 선택지를 두면 다음 차례로 넘어가요.</div>';
  }

  // 선택지 결과의 이동 방식: 페이지 번호 / 스토리 묶음 / 랜덤 이벤트 / 다음 이야기로
  function destHtml(pj, o, d) {
    const m = o.mode || 'page';
    let h = '<div class="row"><label class="muted">이동</label><select ' + d + ' data-f="mode" style="width:auto"><option value="page"' + (m === 'page' ? ' selected' : '') + '>페이지 번호</option><option value="group"' + (m === 'group' ? ' selected' : '') + '>스토리 묶음</option><option value="random"' + (m === 'random' ? ' selected' : '') + '>🎲 랜덤 이벤트</option><option value="continue"' + (m === 'continue' ? ' selected' : '') + '>⏭ 다음 이야기로</option></select>';
    if (m === 'continue') return h + '</div><div class="muted" style="margin:2px 0 0 4px">진행 중인 랜덤 이벤트에서 뽑힌 <b>다음 이야기</b>로 넘어가요. 모두 끝났으면 랜덤 이벤트에서 정한 “이어지는 곳”으로 가요. (효과·아이템은 그대로 적용돼요)</div></div>';
    if (m === 'page') h += '<label class="muted">P.</label><input type="number" min="0" ' + d + ' data-f="next" value="' + esc(o.next) + '">';
    if (m === 'group') {
      const sel = E.groupOf(pj, o.group), st = sel ? E.groupStart(pj, sel) : null;
      h += '<select class="grow" ' + d + ' data-f="group"><option value="">(묶음 선택)</option>' +
        E.FOLDERS.map((f) => { const gs = E.groupsIn(pj, f.id); return gs.length ? '<optgroup label="' + esc(f.name) + '">' + gs.map((g) => '<option value="' + g.id + '"' + (g.id === o.group ? ' selected' : '') + '>' + esc(g.name) + ' (' + E.pagesOfGroup(pj, g.id).length + '페이지)</option>').join('') + '</optgroup>' : ''; }).join('') + '</select>';
      h += '</div><div class="muted" style="margin:2px 0 0 4px">' + (!pj.groups.length ? '아직 묶음이 없어요. 폴더 보기에서 “＋ 묶음”으로 만드세요.' : sel ? (st == null ? '⚠ 이 묶음에는 페이지가 없어요' : '→ 시작 P.' + st) : '이동할 묶음을 고르세요');
    }
    if (m === 'random') {
      const rg = E.groupsIn(pj, 'random'), n = E.randomEntries(pj, o).length;
      h += '</div><div class="pool">' + (rg.length ? rg.map((g) => '<label><input type="checkbox" ' + d + ' data-f="pool" data-g="' + g.id + '"' + ((o.pool || []).includes(g.id) ? ' checked' : '') + '> ' + esc(g.name) + ' <span class="muted">(' + E.pagesOfGroup(pj, g.id).length + ')</span></label>').join('') : '<div class="muted">랜덤 이벤트 폴더에 묶음이 없어요.</div>') + '</div>' +
        '<div class="muted" style="margin:2px 0 0 4px">' + ((o.pool || []).length ? '체크한 묶음' : '체크 없음 = 랜덤 이벤트 폴더 전체(묶음 + 묶음 없는 페이지)') + ' 중에서 뽑아요 · 현재 후보 ' + n + '개' + (n ? '' : ' ⚠') + '</div>' + pickAfterHtml(pj, o, d) + '</div>';
      return h;
    }
    return h + '</div>';
  }

  function outcomeHtml(pj, o, ci, bi) {
    const d = 'data-ci="' + ci + '" data-bi="' + bi + '"';
    o._p = o._p || [];
    // 값이 있는 속성은 항상 보이게
    if (o.gain && !o._p.includes('gain')) o._p.push('gain');
    if (o.lose && !o._p.includes('lose')) o._p.push('lose');
    if (o.achv && !o._p.includes('achv')) o._p.push('achv');
    Object.keys(o.stats || {}).forEach((id) => { if (o.stats[id] && !o._p.includes('stat:' + id)) o._p.push('stat:' + id); });
    let h = '<div class="oc">' + destHtml(pj, o, d);
    o._p.forEach((k) => {
      const rm = '<button class="x" ' + d + ' data-act="rmprop" data-k="' + esc(k) + '">✕</button>';
      if (k === 'gain') h += '<div class="prop"><label>얻는 아이템</label><input type="text" ' + d + ' data-f="gain" placeholder="칼, 열쇠*2, @히든" value="' + esc(o.gain) + '">' + rm + '</div>';
      else if (k === 'lose') h += '<div class="prop"><label>잃는 아이템</label><input type="text" ' + d + ' data-f="lose" placeholder="칼, 열쇠*2" value="' + esc(o.lose) + '">' + rm + '</div>';
      else if (k === 'achv') h += '<div class="prop"><label>업적 달성</label><select ' + d + ' data-f="achv"><option value="">(선택)</option>' + pj.achievements.map((a) => '<option value="' + a.id + '"' + (a.id === o.achv ? ' selected' : '') + '>' + esc(a.name || '(이름 없음)') + '</option>').join('') + '</select>' + rm + '</div>';
      else if (k.startsWith('stat:')) {
        const s = pj.stats.find((x) => x.id === k.slice(5));
        if (s) h += '<div class="prop"><label>' + esc(s.name) + ' 변화</label><input type="number" ' + d + ' data-f="' + esc(k) + '" value="' + (o.stats[s.id] || 0) + '">' + rm + '</div>';
      }
    });
    const opts = [];
    if (!o._p.includes('gain')) opts.push('<option value="gain">얻는 아이템</option>');
    if (!o._p.includes('lose')) opts.push('<option value="lose">잃는 아이템</option>');
    if (!o._p.includes('achv')) opts.push('<option value="achv">업적 달성</option>');
    pj.stats.forEach((s) => { if (!o._p.includes('stat:' + s.id)) opts.push('<option value="stat:' + s.id + '">' + esc(s.name) + ' 변화</option>'); });
    if (opts.length) h += '<select ' + d + ' data-act="addprop" style="width:auto;margin-top:4px;font-size:12px"><option value="">＋ 속성 추가…</option>' + opts.join('') + '</select>';
    return h + '</div>';
  }

  function choiceHtml(pj, p, c, i) {
    const d = 'data-ci="' + i + '"';
    let h = '<div class="cc" data-ci="' + i + '"><div class="row"><b class="muted">#' + (i + 1) + '</b><input type="text" class="grow" ' + d + ' data-f="text" placeholder="선택지 텍스트를 입력하세요" value="' + esc(c.text) + '">' +
      '<button class="sm" ' + d + ' data-act="cup"' + (i === 0 ? ' disabled' : '') + '>▲</button><button class="sm" ' + d + ' data-act="cdown"' + (i === p.choices.length - 1 ? ' disabled' : '') + '>▼</button>' +
      '<button class="sm" ' + d + ' data-act="cdup">복제</button><button class="sm dng" ' + d + ' data-act="cdel">삭제</button></div>';
    h += '<div class="row" style="margin-top:6px"><label class="muted"><input type="checkbox" ' + d + ' data-act="tglshow"' + (c.show !== null ? ' checked' : '') + '> 노출 조건</label>' +
      '<label class="muted"><input type="checkbox" ' + d + ' data-act="tglenable"' + (c.enable !== null ? ' checked' : '') + '> 활성 조건</label></div>';
    if (c.show !== null) h += '<div class="prop"><label>노출 조건</label><input type="text" ' + d + ' data-f="show" placeholder="없으면 숨김: 말린 뱀, 돈>=100, !열쇠" value="' + esc(c.show) + '"></div>';
    if (c.enable !== null) h += '<div class="prop"><label>활성 조건</label><input type="text" ' + d + ' data-f="enable" placeholder="없으면 비활성: 순수한 얼굴, 호기심" value="' + esc(c.enable) + '"></div>';
    const bt = c.branchType;
    c.branches.forEach((b, j) => {
      h += '<div class="br"><div class="row"><b class="muted" style="color:var(--purple)">분기 ' + (j + 1) + '</b>' +
        (bt === 'prob'
          ? '<input type="number" min="0" max="100" ' + d + ' data-bi="' + j + '" data-f="when" value="' + esc(b.when) + '"><span class="muted">%</span>'
          : '<input type="text" class="grow" ' + d + ' data-bi="' + j + '" data-f="when" placeholder="보유 아이템 (쉼표=모두 있어야)" value="' + esc(b.when) + '">') +
        '<span class="grow"></span>' +
        (bt === 'item' ? '<button class="sm" ' + d + ' data-bi="' + j + '" data-act="bup"' + (j === 0 ? ' disabled' : '') + '>▲</button><button class="sm" ' + d + ' data-bi="' + j + '" data-act="bdown"' + (j === c.branches.length - 1 ? ' disabled' : '') + '>▼</button>' : '') +
        '<button class="sm dng" ' + d + ' data-bi="' + j + '" data-act="bdel">삭제</button></div>' + outcomeHtml(pj, b, i, j) + '</div>';
    });
    h += '<div style="margin-top:8px"><div class="muted">' + (bt ? '기본 설정 ' + (bt === 'prob' ? '(확률 <span class="baseprob">' + E.baseProb(c) + '</span>%)' : '(위 분기를 모두 통과하지 못했을 때)') : '기본 설정') + '</div>' + outcomeHtml(pj, c.base, i, -1) + '</div>';
    if (bt !== 'item') h += '<button class="sm" ' + d + ' data-act="addbranch" data-t="prob" style="margin-top:8px">＋ 확률 분기</button> ';
    if (bt !== 'prob') h += '<button class="sm" ' + d + ' data-act="addbranch" data-t="item" style="margin-top:8px">＋ 아이템 분기</button>';
    if (bt === 'prob' && E.branchSum(c) > 100) h += '<div class="muted" style="color:var(--bad)">확률 합이 100%를 넘어요</div>';
    return h + '</div>';
  }

  function targetOutcome(c, bi) { return bi < 0 ? c.base : c.branches[bi]; }

  function onField(el, ev) {
    const p = page(); if (!p) return;
    const f = el.dataset.f;
    if (el.dataset.ci === undefined) {
      if (f === 'body') {
        p.body = el.value;
        const cnt = $('#cnt'); if (cnt) { cnt.textContent = p.body.length; cnt.classList.toggle('over', p.body.length > E.BODY_MAX); }
        touch(); renderSideDebounced(); return;
      }
      if (f === 'group') {
        const pj = proj();
        if (el.value === '__new') { groupForm(pj, null, E.folderOf(p), (ng) => { p.group = ng.id; touch(); renderEditor(); }, () => renderPage()); return; }
        p.group = el.value; touch(); renderEditor(); return;
      }
      if (f === 'folder') { p.folder = el.value; p.group = ''; S.folder = el.value; touch(); renderEditor(); toast('“' + E.FOLDERS.find((x) => x.id === el.value).name + '”로 옮겼어요'); return; }
      if (f === 'title') { p.title = el.value; touch(); renderSideDebounced(); return; }
      if (f === 'isEnd') { p.isEnd = el.checked; touch(); renderPage(); renderSide(); }
      return;
    }
    const c = p.choices[+el.dataset.ci]; if (!c) return;
    const bi = el.dataset.bi === undefined ? -1 : +el.dataset.bi;
    if (f === 'text') c.text = el.value;
    else if (f === 'show') c.show = el.value;
    else if (f === 'enable') c.enable = el.value;
    else {
      const o = targetOutcome(c, bi); if (!o) return;
      if (f === 'mode') { o.mode = el.value; touch(); renderPage(); return; }
      else if (f === 'group') { o.group = el.value; touch(); renderPage(); return; }
      else if (f === 'pickmin' || f === 'pickmax') {
        o.pick = o.pick || { min: 1, max: 1 };
        o.pick[f === 'pickmin' ? 'min' : 'max'] = Math.max(1, parseInt(el.value, 10) || 1);
        if (o.pick.min > o.pick.max) { if (f === 'pickmin') o.pick.max = o.pick.min; else o.pick.min = o.pick.max; }
        touch(); if (ev === 'change') renderPage(); return;
      }
      else if (f === 'aftermode') { o.after = o.after || { mode: 'none', next: 0, group: '' }; o.after.mode = el.value; touch(); renderPage(); return; }
      else if (f === 'afternext') { o.after = o.after || { mode: 'page', next: 0, group: '' }; o.after.next = Math.max(0, parseInt(el.value, 10) || 0); touch(); return; }
      else if (f === 'aftergroup') { o.after = o.after || { mode: 'group', next: 0, group: '' }; o.after.group = el.value; touch(); return; }
      else if (f === 'pool') {
        const box = el.closest('.oc');
        o.pool = Array.from(box.querySelectorAll('[data-f=pool]')).filter((x) => x.checked).map((x) => x.dataset.g);
        touch(); renderPage(); return;
      }
      else if (f === 'next') o.next = el.value === '' ? '' : Math.max(0, parseInt(el.value, 10) || 0);
      else if (f === 'when') {
        if (c.branchType === 'prob') {
          o.when = Math.max(0, Math.min(100, parseInt(el.value, 10) || 0));
          const card = el.closest('.cc'), bp = card && card.querySelector('.baseprob');
          if (bp) bp.textContent = E.baseProb(c);
        } else o.when = el.value;
      } else if (f === 'gain' || f === 'lose' || f === 'achv') o[f] = el.value;
      else if (f.startsWith('stat:')) { o.stats = o.stats || {}; o.stats[f.slice(5)] = parseInt(el.value, 10) || 0; }
    }
    touch();
  }
  let sideTimer;
  function renderSideDebounced() { clearTimeout(sideTimer); sideTimer = setTimeout(renderSide, 400); }

  function newPageNo() { return E.lastPageNo(proj()) + 1; }
  function addPage(from) {
    const pj = proj(); const n = newPageNo();
    const p = from ? Object.assign(E.clone(from), { n }) : E.newPage(n, S.folder);
    if (from) p.choices.forEach((c) => { c.id = E.uid('c'); });
    pj.pages.push(p); S.pn = n; touch(); renderEditor();
    toast('P.' + n + ' 페이지를 추가했어요');
  }

  /* ================= 미리보기 ================= */
  let PV = null;
  function startPlay(pj, n) {
    if (!pj.pages.length || !E.pageOf(pj, n == null ? 0 : n)) { toast('시작할 페이지가 없어요'); return; }
    const st = E.initState(pj); st.page = n == null ? 0 : n;
    PV = { pj, st, fx: [], from: st.page, m: modal('', 'full') };
    PV.m.el.addEventListener('click', onPlayClick);
    renderPlay();
  }
  function renderPlay() {
    const { pj, st, fx } = PV, p = E.pageOf(pj, st.page);
    const items = Object.keys(st.items).filter((k) => !k.startsWith('@')).map((k) => esc(k) + (st.items[k] > 1 ? ' ×' + st.items[k] : ''));
    let h = '<div class="row"><b class="grow">' + esc(pj.name) + ' <span class="muted">P.' + st.page + ' · 미리보기 (저장 안 됨)</span></b><button class="sm" data-pv="restart">처음부터</button><button class="sm" data-pv="close">닫기</button></div>' +
      '<div class="status">' + pj.stats.map((s) => '<span>' + esc(s.name) + ' <b>' + st.stats[s.id] + (s.max ? '/' + s.max : '') + '</b></span>').join('') +
      (st.run ? '<span class="chip rand">🎲 ' + st.run.index + '/' + st.run.total + (st.run.queue.length ? '' : st.run.after ? ' · 마지막' : ' · 마지막') + '</span>' : '') + '<span class="grow muted">🎒 ' + (items.length ? items.join(', ') : '없음') + '</span><span class="muted">🏆 ' + st.achv.length + '/' + pj.achievements.length + '</span></div>';
    h += '<div class="play-scroll"><div class="play-body">' + (p.body.trim() ? esc(p.body) : '<span class="muted">(본문이 비어 있어요)</span>') + '</div>';
    if (fx.length) h += '<div>' + fx.map((f) => '<span class="chip ' + f.kind + '">' + esc(f.kind === 'lose' ? '− ' + f.text : f.kind === 'gain' ? '+ ' + f.text : f.kind === 'rand' ? '🎲 ' + f.text : '🏆 ' + f.text.replace('업적 달성: ', '')) + '</span>').join('') + '</div>';
    if (p.isEnd && E.canContinue(st)) {
      const more = st.run.queue.length;
      h += '<div class="card" style="text-align:center"><div class="muted" style="margin-bottom:8px">— 이야기 끝 —</div><button class="pri" data-pv="cont">' + (more ? '다음 이야기로 ▶ (' + (st.run.index + 1) + '/' + st.run.total + ')' : '이어서 ▶') + '</button></div>';
    } else if (p.isEnd) {
      h += '<div class="card" style="text-align:center"><div class="muted" style="margin-bottom:8px">— 끝 —</div><button class="pri" data-pv="restart">다시 시작</button> <button data-pv="close">종료</button></div>';
    } else {
      const vis = E.visibleChoices(pj, st, p);
      h += vis.length ? vis.map((v) => '<button class="pc" data-pv="choice" data-id="' + v.choice.id + '"' + (v.enabled ? '' : ' disabled') + '>' + esc(v.choice.text || '(텍스트 없음)') + (v.enabled ? '' : '<small>필요: ' + esc(v.missing.join(', ')) + '</small>') + '</button>').join('') : '<div class="muted">보이는 선택지가 없어요.</div>';
    }
    h += '</div>';                                    // .play-scroll: 본문 + 효과 칩 + 선택지가 한 덩어리로 같이 스크롤
    PV.m.el.firstChild.innerHTML = h;
    const sc = PV.m.el.querySelector('.play-scroll'); if (sc) sc.scrollTop = 0;
  }
  function onPlayClick(e) {
    const b = e.target.closest('[data-pv]'); if (!b || !PV) return;
    const a = b.dataset.pv;
    if (a === 'close') { PV.m.close(); PV = null; return; }
    if (a === 'restart') { const st = E.initState(PV.pj); st.page = 0; PV.st = st; PV.fx = []; renderPlay(); return; }
    if (a === 'cont') {
      const r = E.continueRun(PV.pj, PV.st);
      PV.fx = r.fx;
      if (r.error) toast(r.error);
      renderPlay(); return;
    }
    if (a === 'choice') {
      const p = E.pageOf(PV.pj, PV.st.page);
      const ch = p && p.choices.find((c) => c.id === b.dataset.id); if (!ch) return;
      const r = E.choose(PV.pj, PV.st, ch);
      PV.fx = r.fx;
      if (r.error) toast(r.error);
      renderPlay();
    }
  }



  /* ================= 스토리 묶음 ================= */
  // g 가 없으면 새로 만든다. onDone(g) 은 저장 후 호출.
  function groupForm(pj, g, folder, onDone, onCancel) {
    const isNew = !g;
    const ps = g ? E.pagesOfGroup(pj, g.id) : [];
    const m = modal('<h3 style="margin-top:0">' + (isNew ? '새 스토리 묶음' : '스토리 묶음 수정') + ' <span class="muted">' + esc(E.FOLDERS.find((f) => f.id === (g ? g.folder : folder)).name) + '</span></h3>' +
      '<div class="muted">제목</div><input type="text" id="gfName" maxlength="40" placeholder="예: 1장 · 창고 탈출" value="' + esc(g ? g.name : '') + '">' +
      (isNew ? '' : '<div class="muted" style="margin-top:8px">시작 페이지 (이 묶음으로 이동할 때 처음 열리는 페이지)</div><select id="gfStart"><option value="">자동 (가장 낮은 번호)</option>' + ps.map((p) => '<option value="' + p.n + '"' + (p.n === g.start ? ' selected' : '') + '>P.' + p.n + ' · ' + esc(E.pageLabel(p)) + '</option>').join('') + '</select>') +
      '<div class="row" style="justify-content:' + (isNew ? 'flex-end' : 'space-between') + ';margin-top:14px">' + (isNew ? '' : '<button class="dng" data-x="del">묶음 삭제</button>') + '<span class="row"><button data-x="0">취소</button><button class="pri" data-x="1">저장</button></span></div>');
    m.el.addEventListener('click', async (e) => {
      const b = e.target.closest('[data-x]'); if (!b) return;
      if (b.dataset.x === '0') { m.close(); if (onCancel) onCancel(); return; }
      if (b.dataset.x === 'del') {
        const n = ps.length;
        if (!await ask('“' + g.name + '” 묶음을 삭제할까요?\n' + (n ? '안의 페이지 ' + n + '개는 지워지지 않고 “묶음 없음”으로 남아요.' : ''), '삭제')) return;
        pj.groups = pj.groups.filter((x) => x.id !== g.id);
        pj.pages.forEach((p) => { if (p.group === g.id) p.group = ''; });
        touch(); saveNow(); m.close(); renderEditor(); toast('묶음을 삭제했어요 (이 묶음을 가리키던 선택지는 검사에서 확인하세요)'); return;
      }
      const name = $('#gfName', m.el).value.trim();
      if (!name) { toast('제목을 입력하세요'); return; }
      if ((pj.groups || []).some((x) => x.name === name && (!g || x.id !== g.id))) { toast('같은 이름의 묶음이 이미 있어요'); return; }
      pj.groups = pj.groups || [];
      let target = g;
      if (isNew) { target = { id: E.uid('g'), name, folder, start: null }; pj.groups.push(target); }
      else { target.name = name; const st = $('#gfStart', m.el).value; target.start = st === '' ? null : parseInt(st, 10); }
      touch(); saveNow(); m.close();
      if (onDone) onDone(target); else renderEditor();
    });
    setTimeout(() => $('#gfName', m.el).focus(), 30);
  }

  /* ================= 분기도 (ReactFlow) ================= */
  function flowProps() {
    const pj = proj();
    const refresh = () => { saveNow(); flowRefresh(); };
    const goTo = (n) => { S.trail.push(S.pn); if (S.trail.length > 30) S.trail.shift(); S.pn = n; S.editCi = null; };
    const api = {
      rev: Date.now(),
      back(n) { const i = S.trail.lastIndexOf(n); S.trail = i >= 0 ? S.trail.slice(0, i) : []; S.pn = n; S.editCi = null; flowRefresh(); },
      goPage(to) {
        to = Number(to);
        if (!(to >= 0)) return;
        if (!E.pageOf(pj, to)) { pj.pages.push(E.newPage(to, E.folderOf(page()))); touch(); toast('P.' + to + ' 페이지를 새로 만들었어요'); }
        goTo(to); flowRefresh();
      },
      edit(ci) { S.editCi = ci; flowRefresh(); },
      closeEdit() { S.editCi = null; flowRefresh(); },
      add() {
        const p = page(); if (!p) return;
        const n = newPageNo(); pj.pages.push(E.newPage(n, E.folderOf(p)));
        const c = E.newChoice(n); c.text = '새 선택지'; p.choices.push(c);
        S.editCi = p.choices.length - 1; touch(); refresh();
      },
      editPage() { S.view = 'edit'; render(); },
      play() { startPlay(pj, S.pn); },
      setText(ci, v) { const p = page(); if (p && p.choices[ci]) { p.choices[ci].text = v; touch(); flowRefresh(); } },
      setMode(ci, m) { const p = page(); if (p && p.choices[ci]) { p.choices[ci].base.mode = m; touch(); flowRefresh(); } },
      setGroup(ci, id) { const p = page(); if (p && p.choices[ci]) { p.choices[ci].base.group = id; touch(); flowRefresh(); } },
      togglePool(ci, id, on) {
        const p = page(); if (!p || !p.choices[ci]) return;
        const b = p.choices[ci].base; const set = new Set(b.pool || []);
        if (on) set.add(id); else set.delete(id);
        b.pool = Array.from(set); touch(); flowRefresh();
      },
      setPick(ci, min, max) { const p = page(); if (!p || !p.choices[ci]) return; const b = p.choices[ci].base; b.pick = { min: Math.max(1, min || 1), max: Math.max(1, max || 1) }; if (b.pick.min > b.pick.max) b.pick.max = b.pick.min; touch(); flowRefresh(); },
      setAfter(ci, mode, val) {
        const p = page(); if (!p || !p.choices[ci]) return; const b = p.choices[ci].base;
        b.after = b.after || { mode: 'none', next: 0, group: '' }; b.after.mode = mode;
        if (mode === 'page') b.after.next = Math.max(0, parseInt(val, 10) || 0);
        if (mode === 'group') b.after.group = val || '';
        touch(); flowRefresh();
      },
      setNext(ci, n) { const p = page(); if (p && p.choices[ci] && n >= 0) { p.choices[ci].base.next = n; p.choices[ci].base.mode = 'page'; touch(); flowRefresh(); } },
      newPageFor(ci) {
        const p = page(); if (!p || !p.choices[ci]) return;
        const n = newPageNo(); pj.pages.push(E.newPage(n, E.folderOf(p))); p.choices[ci].base.next = n; touch(); refresh(); toast('P.' + n + ' 페이지를 만들어 연결했어요');
      },
      move(ci, d) { const p = page(), j = ci + d; if (p && p.choices[j]) { [p.choices[ci], p.choices[j]] = [p.choices[j], p.choices[ci]]; S.editCi = j; touch(); refresh(); } },
      dup(ci) { const p = page(); if (!p || !p.choices[ci]) return; const c = E.clone(p.choices[ci]); c.id = E.uid('c'); p.choices.splice(ci + 1, 0, c); S.editCi = ci + 1; touch(); refresh(); },
      del(ci) { const p = page(); if (!p) return; p.choices.splice(ci, 1); S.editCi = null; touch(); refresh(); },
    };
    return { pj, cur: S.pn, trail: S.trail.slice(), editCi: S.editCi, api };
  }
  function flowRefresh() {
    const el = $('#flowRoot'); if (!el || !window.SFlow || !proj()) return;
    window.SFlow.render(el, flowProps());
  }
  function renderFlow() {
    const pj = proj();
    if (!pj) return go('home');
    if (!window.SFlow) { toast('분기도 라이브러리를 불러오지 못했어요'); S.view = 'edit'; return render(); }
    if (!E.pageOf(pj, S.pn)) S.pn = pj.pages.length ? pj.pages[0].n : 0;
    if (!E.pageOf(pj, S.pn)) { toast('페이지가 없어요'); S.view = 'edit'; return render(); }
    $('#main').innerHTML = '<div id="flowRoot"></div>';
    flowRefresh();
  }
  function showJson() {
    const pj = proj();
    const m = modal('<h3 style="margin-top:0">스토리 JSON</h3><div class="muted">페이지를 <code>{ id, title, text, choices:[{ text, to }], end }</code> 배열로 쓰고 “적용”하면 추가·수정돼요. 같은 id 는 덮어쓰고, 배열에 없는 페이지는 그대로 둬요. 선택지의 조건·아이템 같은 고급 속성은 순서대로 유지돼요.</div>' +
      '<textarea id="jsonTa" spellcheck="false" style="min-height:300px;font-family:monospace;font-size:12px;margin-top:8px">' + esc(JSON.stringify(E.toStoryArray(pj), null, 2)) + '</textarea>' +
      '<div class="row" style="justify-content:flex-end;margin-top:10px"><button data-x="0">닫기</button><button class="pri" data-x="1">적용</button></div>');
    m.el.addEventListener('click', (e) => {
      const b = e.target.closest('[data-x]'); if (!b) return;
      if (b.dataset.x === '0') return m.close();
      let arr;
      try { arr = JSON.parse($('#jsonTa', m.el).value); } catch (err) { toast('JSON 형식 오류: ' + err.message); return; }
      try {
        const r = E.applyStoryArray(pj, arr);
        touch(); saveNow(); m.close();
        if (!E.pageOf(pj, S.pn) && pj.pages.length) S.pn = pj.pages[0].n;
        flowRefresh(); toast('적용했어요 (추가 ' + r.added + ' · 수정 ' + r.updated + ')');
      } catch (err) { toast(err.message); }
    });
  }

  /* ================= 검사 / 업적 ================= */
  function showCheck() {
    const pj = proj(); const list = E.validate(pj);
    const m = modal('<h3 style="margin-top:0">검사 결과</h3>' + (list.length ? '<div class="muted">' + list.length + '건 — 누르면 해당 페이지로 이동해요</div>' + list.map((x) => '<div class="issue" data-n="' + (x.page == null ? '' : x.page) + '">' + (x.page == null ? '' : '<b>P.' + x.page + '</b> ') + esc(x.msg) + '</div>').join('') : '<p>문제가 없어요 👍</p>') + '<div class="row" style="justify-content:flex-end;margin-top:12px"><button data-x="1">닫기</button></div>');
    m.el.addEventListener('click', (e) => {
      const it = e.target.closest('.issue');
      if (it && it.dataset.n !== '') { S.pn = +it.dataset.n; m.close(); renderEditor(); return; }
      if (e.target.closest('[data-x]')) m.close();
    });
  }
  function showAchvs() {
    const pj = proj();
    const body = () => pj.achievements.map((a) => '<div class="prop" data-id="' + a.id + '"><input type="text" class="an" value="' + esc(a.name) + '" placeholder="업적 이름"><button class="sm dng" data-del="' + a.id + '">삭제</button></div>').join('') || '<div class="muted">아직 업적이 없어요</div>';
    const m = modal('<h3 style="margin-top:0">🏆 업적</h3><div class="muted">이름이 겹치지 않게 해 주세요. 선택지 속성의 “업적 달성”에서 연결합니다.</div><div id="achBox">' + body() + '</div><div class="row" style="justify-content:space-between;margin-top:10px"><button data-add="1">＋ 업적 추가</button><button class="pri" data-x="1">닫기</button></div>');
    const refill = () => { $('#achBox', m.el).innerHTML = body(); };
    m.el.addEventListener('input', (e) => {
      if (!e.target.classList.contains('an')) return;
      const a = pj.achievements.find((x) => x.id === e.target.closest('.prop').dataset.id); if (a) { a.name = e.target.value; touch(); }
    });
    m.el.addEventListener('click', (e) => {
      if (e.target.closest('[data-add]')) { pj.achievements.push({ id: E.uid('a'), name: '' }); touch(); refill(); const l = m.el.querySelectorAll('.an'); l[l.length - 1].focus(); return; }
      const del = e.target.closest('[data-del]');
      if (del) { pj.achievements = pj.achievements.filter((a) => a.id !== del.dataset.del); touch(); refill(); return; }
      if (e.target.closest('[data-x]')) { m.close(); renderPage(); }
    });
  }

  /* ================= 클릭 / 입력 이벤트 ================= */
  document.addEventListener('click', async (e) => {
    const a = e.target.closest('[data-act]');
    if (!a || a.tagName === 'SELECT' || a.type === 'checkbox') return;
    const act = a.dataset.act, id = a.dataset.id;
    const pj = proj(), p = page();
    const ci = a.dataset.ci === undefined ? -1 : +a.dataset.ci;
    const bi = a.dataset.bi === undefined ? -1 : +a.dataset.bi;
    switch (act) {
      case 'home': go('home'); break;
      case 'newproj': projectForm(null, (v) => { const np = E.newProject(v.name, v.tag, v.color); S.projects.push(np); saveNow(); go('edit', np.id, 0); }); break;
      case 'sample': { const sp = samplePj(); S.projects.push(sp); saveNow(); render(); break; }
      case 'open': go('edit', id, 0); break;
      case 'play': { const t = S.projects.find((x) => x.id === id); if (t) startPlay(t, 0); break; }
      case 'play0': startPlay(pj, 0); break;
      case 'playhere': startPlay(pj, S.pn); break;
      case 'export': { const t = S.projects.find((x) => x.id === id); if (t) exportProject(t); break; }
      case 'import': $('#importFile').click(); break;
      case 'dup': {
        const t = S.projects.find((x) => x.id === id); if (!t) break;
        const c = E.clone(t); c.id = E.uid('p'); c.name = (t.tutorialVersion ? '튜토리얼 실험' : t.name + ' (복사)'); c.tag = t.tutorialVersion ? '' : c.tag; delete c.tutorialVersion; c.createdAt = c.updatedAt = Date.now();
        S.projects.push(c); saveNow(); render(); toast('복제했어요'); break;
      }
      case 'tutreset': {
        const T = window.STutorial;
        if (T && await ask('📘 튜토리얼을 원래 내용으로 되돌릴까요?\n지금까지 고친 내용은 사라져요. (내 작품에는 영향이 없어요)', '되돌리기')) {
          const i = S.projects.findIndex((x) => x.id === T.ID); const t = T.build();
          if (i >= 0) S.projects[i] = t; else S.projects.unshift(t);
          saveNow(); render(); toast('튜토리얼을 원래대로 되돌렸어요');
        }
        break;
      }
      case 'delproj': {
        if (id === (window.STutorial && window.STutorial.ID)) { toast('튜토리얼은 삭제할 수 없어요. “원래대로”로 복구하거나 “복제해서 실험”을 쓰세요'); break; }
        const t = S.projects.find((x) => x.id === id);
        if (t && await ask('“' + t.name + '” 프로젝트를 삭제할까요?\n되돌릴 수 없어요.', '삭제')) { S.projects = S.projects.filter((x) => x.id !== id); saveNow(); render(); }
        break;
      }
      case 'settings': projectForm(pj, (v) => {
        pj.name = v.name; pj.tag = v.tag; pj.color = v.color;
        if (v.stats) pj.stats = v.stats;
        touch(); saveNow(); render();
      }); break;
      case 'flow': S.trail = []; S.editCi = null; S.view = 'flow'; render(); break;
      case 'flowexit': S.view = 'edit'; render(); break;
      case 'json': showJson(); break;
      case 'achvs': showAchvs(); break;
      case 'check': showCheck(); break;
      case 'toggleside': S.side = !S.side; renderEditor(); break;
      case 'mode': {
        S.listMode = a.dataset.mode; S.q = '';
        try { localStorage.setItem(MODE_KEY, S.listMode); } catch (e) { /* 저장 못 해도 동작 */ }
        if (S.listMode === 'folder' && p) S.folder = E.folderOf(p);
        renderEditor(); break;
      }
      case 'gtoggle': { const g = a.dataset.g; if (S.collapsed.has(g)) S.collapsed.delete(g); else S.collapsed.add(g); renderSide(); break; }
      case 'gnew': groupForm(pj, null, S.folder, (ng) => { S.collapsed.delete(ng.id); renderEditor(); toast('“' + ng.name + '” 묶음을 만들었어요. 묶음의 ＋ 로 페이지를 추가하세요'); }); break;
      case 'gedit': { const g = E.groupOf(pj, a.dataset.g); if (g) groupForm(pj, g, g.folder); break; }
      case 'gadd': {
        const g = E.groupOf(pj, a.dataset.g); if (!g) break;
        const n = newPageNo(); const np = E.newPage(n, g.folder, g.id); pj.pages.push(np);
        S.collapsed.delete(g.id); S.pn = n; touch(); renderEditor(); toast('“' + g.name + '”에 P.' + n + ' 페이지를 추가했어요'); break;
      }
      case 'folder': {
        S.folder = a.dataset.folder; S.q = '';
        const first = E.pagesIn(pj, S.folder)[0]; S.pn = first ? first.n : -1;
        renderEditor(); break;
      }
      case 'gopage': S.pn = +a.dataset.n; if (window.innerWidth <= 760) S.side = false; renderEditor(); window.scrollTo(0, 0); $('#main').scrollTop = 0; break;
      case 'addpage': addPage(); break;
      case 'duppage': if (p) addPage(p); break;
      case 'delpage':
        if (p && await ask('P.' + p.n + ' 페이지를 삭제할까요?\n다른 페이지의 번호는 바뀌지 않아요.', '삭제')) {
          pj.pages = pj.pages.filter((x) => x.n !== p.n);
          S.pn = -1; touch(); renderEditor();   // renderEditor 가 같은 폴더의 첫 페이지를 고른다
        }
        break;
      case 'addchoice': if (p) { p.choices.push(E.newChoice(newPageNo())); touch(); renderPage(); } break;
      case 'cup': case 'cdown': {
        const j = act === 'cup' ? ci - 1 : ci + 1;
        if (p && p.choices[j]) { [p.choices[ci], p.choices[j]] = [p.choices[j], p.choices[ci]]; touch(); renderPage(); }
        break;
      }
      case 'cdup': if (p) { const c = E.clone(p.choices[ci]); c.id = E.uid('c'); p.choices.splice(ci + 1, 0, c); touch(); renderPage(); } break;
      case 'cdel': if (p) { p.choices.splice(ci, 1); touch(); renderPage(); } break;
      case 'addbranch': {
        const c = p && p.choices[ci]; if (!c) break;
        c.branchType = a.dataset.t;
        const o = E.newOutcome(c.base.next);
        o.when = c.branchType === 'prob' ? Math.min(10, E.baseProb(c)) : '';
        c.branches.push(o); touch(); renderPage(); break;
      }
      case 'bdel': { const c = p && p.choices[ci]; if (c) { c.branches.splice(bi, 1); if (!c.branches.length) c.branchType = null; touch(); renderPage(); } break; }
      case 'bup': case 'bdown': {
        const c = p && p.choices[ci]; const j = act === 'bup' ? bi - 1 : bi + 1;
        if (c && c.branches[j]) { [c.branches[bi], c.branches[j]] = [c.branches[j], c.branches[bi]]; touch(); renderPage(); }
        break;
      }
      case 'rmprop': {
        const c = p && p.choices[ci]; const o = c && targetOutcome(c, bi); if (!o) break;
        const k = a.dataset.k;
        o._p = (o._p || []).filter((x) => x !== k);
        if (k === 'gain' || k === 'lose' || k === 'achv') o[k] = '';
        else if (k.startsWith('stat:')) delete o.stats[k.slice(5)];
        touch(); renderPage(); break;
      }
    }
  });

  document.addEventListener('change', (e) => {
    const t = e.target;
    if (t.id === 'importFile') {
      const f = t.files && t.files[0]; if (!f) return;
      const r = new FileReader(); r.onload = () => importText(String(r.result)); r.onerror = () => toast('파일을 읽지 못했어요');
      r.readAsText(f); t.value = ''; return;
    }
    if (t.dataset.act === 'addprop') {
      const p = page(), c = p && p.choices[+t.dataset.ci], o = c && targetOutcome(c, +t.dataset.bi);
      if (!o || !t.value) return;
      o._p = o._p || []; if (!o._p.includes(t.value)) o._p.push(t.value);
      if (t.value.startsWith('stat:')) { o.stats = o.stats || {}; if (!o.stats[t.value.slice(5)]) o.stats[t.value.slice(5)] = 1; }
      touch(); renderPage(); return;
    }
    if (t.dataset.act === 'tglshow' || t.dataset.act === 'tglenable') {
      const p = page(), c = p && p.choices[+t.dataset.ci]; if (!c) return;
      const k = t.dataset.act === 'tglshow' ? 'show' : 'enable';
      c[k] = t.checked ? '' : null; touch(); renderPage(); return;
    }
    if (t.dataset.f) onField(t, 'change');
  });
  document.addEventListener('input', (e) => {
    const t = e.target;
    if (t.id === 'pq') { S.q = t.value; const pos = t.selectionStart; renderSide(); const q = $('#pq'); if (q) { q.focus(); q.setSelectionRange(pos, pos); } return; }
    if (t.dataset && t.dataset.f && t.type !== 'checkbox' && t.tagName !== 'SELECT') onField(t);
  });

  /* ================= 시작 ================= */
  try { const m = localStorage.getItem(MODE_KEY); if (m === 'num' || m === 'folder') S.listMode = m; } catch (e) { /* 기본값 사용 */ }
  load();
  ensureTutorial();
  render();
  if (S.tutUpdated) toast('📘 튜토리얼이 새 내용으로 갱신됐어요');
})();
