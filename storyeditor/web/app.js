/* StoryEditor 화면: 프로젝트 목록 / 페이지·선택지 에디터 / 미리보기 / 검사 */
(function () {
  'use strict';
  const E = window.SEngine;
  const $ = (s, r) => (r || document).querySelector(s);
  const esc = (s) => String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const COLORS = ['#6d8cff', '#a78bfa', '#f472b6', '#fb923c', '#facc15', '#4ade80', '#2dd4bf', '#94a3b8'];
  const KEY = 'storyeditor.projects.v1';

  const S = { projects: [], view: 'home', pid: null, pn: 0, q: '', side: false, memOnly: false, trail: [], editCi: null, folder: 'main' };

  /* ================= 저장 ================= */
  function load() {
    try { const v = JSON.parse(localStorage.getItem(KEY) || '[]'); if (Array.isArray(v)) S.projects = v; }
    catch (e) { S.memOnly = true; }
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
      h += '<div class="grid">' + S.projects.slice().sort((a, b) => b.updatedAt - a.updatedAt).map((p) =>
        '<div class="card" style="margin:0"><div class="cover" style="background:' + COLORS[p.color % COLORS.length] + '"></div>' +
        '<div class="row"><b class="grow" style="word-break:break-all">' + esc(p.name) + '</b>' + (p.tag ? '<span class="tag">' + esc(p.tag) + '</span>' : '') + '</div>' +
        '<div class="muted">페이지 ' + p.pages.length + '개 · 업적 ' + p.achievements.length + '개 · ' + fmtDate(p.updatedAt) + '</div>' +
        '<div class="row" style="margin-top:10px"><button class="pri sm" data-act="open" data-id="' + p.id + '">편집</button><button class="sm" data-act="play" data-id="' + p.id + '">▶ 미리보기</button>' +
        '<button class="sm" data-act="export" data-id="' + p.id + '">내보내기</button><button class="sm" data-act="dup" data-id="' + p.id + '">복제</button><button class="sm dng" data-act="delproj" data-id="' + p.id + '">삭제</button></div></div>'
      ).join('') + '</div>';
    }
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
    pj.pages = [p0, p1, p2, p3, p4, p5];
    gamble.base.achv = 'a_end'; gamble.base._p = ['achv'];
    return pj;
  }

  /* ================= 에디터 ================= */
  function sideHtml(pj) {
    const q = S.q.trim().toLowerCase();
    const list = E.pagesIn(pj, S.folder).filter((p) => !q || String(p.n) === q || p.body.toLowerCase().includes(q) || (p.title || '').toLowerCase().includes(q));
    const tabs = '<div class="ftabs">' + E.FOLDERS.map((f) => '<button class="ft' + (f.id === S.folder ? ' on' : '') + '" data-act="folder" data-folder="' + f.id + '">' + esc(f.name) + ' <span>' + E.pagesIn(pj, f.id).length + '</span></button>').join('') + '</div>';
    const cur = E.FOLDERS.find((f) => f.id === S.folder);
    return tabs + '<div class="row" style="margin-bottom:6px"><input type="text" id="pq" class="grow" placeholder="페이지 번호·본문 검색" value="' + esc(S.q) + '"><button class="pri sm" data-act="addpage" title="' + esc(cur.name) + '에 페이지 추가">＋</button></div>' +
      (list.length ? list.map((p) => '<div class="pi' + (p.n === S.pn ? ' on' : '') + (p.isEnd ? ' end' : '') + '" data-act="gopage" data-n="' + p.n + '"><b>P.' + p.n + '</b><span>' + esc(E.pageLabel(p)) + '</span></div>').join('')
        : '<div class="muted" style="padding:8px">' + (q ? '검색 결과 없음' : esc(cur.name) + '가 비어 있어요.<br>＋ 로 첫 페이지를 만들어 보세요.') + '</div>');
  }
  function renderEditor() {
    const pj = proj();
    if (!pj) return go('home');
    const cur = E.pageOf(pj, S.pn);
    if (cur) S.folder = E.folderOf(cur);              // 다른 폴더의 페이지로 이동하면 탭도 따라간다
    else {
      const first = E.pagesIn(pj, S.folder)[0];
      S.pn = first ? first.n : -1;                    // -1 = 이 폴더에 페이지 없음
    }
    $('#main').innerHTML = '<div class="ed' + (S.side ? '' : ' noside') + '"><div class="side" id="side">' + sideHtml(pj) + '</div><div id="pageBox"></div></div>';
    renderPage();
  }
  function renderSide() { const pj = proj(); const el = $('#side'); if (pj && el) { const sc = el.scrollTop; el.innerHTML = sideHtml(pj); el.scrollTop = sc; } }

  function renderPage() {
    const pj = proj(), p = page(), box = $('#pageBox');
    if (!box) return;
    if (!p) { box.innerHTML = '<div class="card">이 폴더에 페이지가 없어요. 왼쪽의 ＋ 로 추가하세요.' + (window.innerWidth <= 760 ? '<br><span class="muted">(☰ 페이지 버튼으로 목록을 열 수 있어요)</span>' : '') + '</div>'; return; }
    let h = '<div class="card"><div class="row"><h3 style="margin:0" class="grow">P.' + p.n + (p.n === 0 ? ' <span class="muted">(시작)</span>' : '') + '</h3>' +
      '<label class="muted">폴더 <select data-f="folder" style="width:auto;padding:3px 6px">' + E.FOLDERS.map((f) => '<option value="' + f.id + '"' + (f.id === E.folderOf(p) ? ' selected' : '') + '>' + esc(f.name) + '</option>').join('') + '</select></label>' +
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

  function outcomeHtml(pj, o, ci, bi) {
    const d = 'data-ci="' + ci + '" data-bi="' + bi + '"';
    o._p = o._p || [];
    // 값이 있는 속성은 항상 보이게
    if (o.gain && !o._p.includes('gain')) o._p.push('gain');
    if (o.lose && !o._p.includes('lose')) o._p.push('lose');
    if (o.achv && !o._p.includes('achv')) o._p.push('achv');
    Object.keys(o.stats || {}).forEach((id) => { if (o.stats[id] && !o._p.includes('stat:' + id)) o._p.push('stat:' + id); });
    let h = '<div class="oc"><div class="row"><label class="muted">이동 P.</label><input type="number" min="0" ' + d + ' data-f="next" value="' + esc(o.next) + '"></div>';
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

  function onField(el) {
    const p = page(); if (!p) return;
    const f = el.dataset.f;
    if (el.dataset.ci === undefined) {
      if (f === 'body') {
        p.body = el.value;
        const cnt = $('#cnt'); if (cnt) { cnt.textContent = p.body.length; cnt.classList.toggle('over', p.body.length > E.BODY_MAX); }
        touch(); renderSideDebounced(); return;
      }
      if (f === 'folder') { p.folder = el.value; S.folder = el.value; touch(); renderEditor(); toast('“' + E.FOLDERS.find((x) => x.id === el.value).name + '”로 옮겼어요'); return; }
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
      if (f === 'next') o.next = el.value === '' ? '' : Math.max(0, parseInt(el.value, 10) || 0);
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
      '<span class="grow muted">🎒 ' + (items.length ? items.join(', ') : '없음') + '</span><span class="muted">🏆 ' + st.achv.length + '/' + pj.achievements.length + '</span></div>';
    h += '<div class="play-body">' + (p.body.trim() ? esc(p.body) : '<span class="muted">(본문이 비어 있어요)</span>') + '</div>';
    if (fx.length) h += '<div>' + fx.map((f) => '<span class="chip ' + f.kind + '">' + esc(f.kind === 'lose' ? '− ' + f.text : f.kind === 'gain' ? '+ ' + f.text : '🏆 ' + f.text.replace('업적 달성: ', '')) + '</span>').join('') + '</div>';
    if (p.isEnd) {
      h += '<div class="card" style="text-align:center"><div class="muted" style="margin-bottom:8px">— 끝 —</div><button class="pri" data-pv="restart">다시 시작</button> <button data-pv="close">종료</button></div>';
    } else {
      const vis = E.visibleChoices(pj, st, p);
      h += vis.length ? vis.map((v) => '<button class="pc" data-pv="choice" data-id="' + v.choice.id + '"' + (v.enabled ? '' : ' disabled') + '>' + esc(v.choice.text || '(텍스트 없음)') + (v.enabled ? '' : '<small>필요: ' + esc(v.missing.join(', ')) + '</small>') + '</button>').join('') : '<div class="muted">보이는 선택지가 없어요.</div>';
    }
    PV.m.el.firstChild.innerHTML = h;
    const body = PV.m.el.querySelector('.play-body'); if (body) body.scrollTop = 0;
  }
  function onPlayClick(e) {
    const b = e.target.closest('[data-pv]'); if (!b || !PV) return;
    const a = b.dataset.pv;
    if (a === 'close') { PV.m.close(); PV = null; return; }
    if (a === 'restart') { const st = E.initState(PV.pj); st.page = 0; PV.st = st; PV.fx = []; renderPlay(); return; }
    if (a === 'choice') {
      const p = E.pageOf(PV.pj, PV.st.page);
      const ch = p && p.choices.find((c) => c.id === b.dataset.id); if (!ch) return;
      const r = E.choose(PV.pj, PV.st, ch);
      PV.fx = r.fx;
      if (r.error) toast(r.error);
      renderPlay();
    }
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
      setNext(ci, n) { const p = page(); if (p && p.choices[ci] && n >= 0) { p.choices[ci].base.next = n; touch(); flowRefresh(); } },
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
        const c = E.clone(t); c.id = E.uid('p'); c.name = t.name + ' (복사)'; c.createdAt = c.updatedAt = Date.now();
        S.projects.push(c); saveNow(); render(); toast('복제했어요'); break;
      }
      case 'delproj': {
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
    if (t.dataset.f) onField(t);
  });
  document.addEventListener('input', (e) => {
    const t = e.target;
    if (t.id === 'pq') { S.q = t.value; const pos = t.selectionStart; renderSide(); const q = $('#pq'); if (q) { q.focus(); q.setSelectionRange(pos, pos); } return; }
    if (t.dataset && t.dataset.f && t.type !== 'checkbox' && t.tagName !== 'SELECT') onField(t);
  });

  /* ================= 시작 ================= */
  load();
  render();
})();
