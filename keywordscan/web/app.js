/* 소설 키워드 분석기 UI */
(function () {
  'use strict';
  const E = window.KSEngine;
  const $ = (s) => document.querySelector(s);
  const esc = (s) => String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const uid = (p) => p + Date.now().toString(36) + Math.random().toString(36).slice(2, 7);
  const TONES = { like: '호감', normal: '일반', dislike: '불호' };

  /* ================= 저장소 (IndexedDB, 실패 시 메모리) ================= */
  const DB = (function () {
    let db = null;
    const mem = { novels: new Map(), texts: new Map(), meta: new Map() };
    function open() {
      return new Promise((resolve) => {
        try {
          const req = indexedDB.open('keywordscan', 1);
          req.onupgradeneeded = () => {
            const d = req.result;
            d.createObjectStore('novels', { keyPath: 'id' });
            d.createObjectStore('texts', { keyPath: 'id' });
            d.createObjectStore('meta', { keyPath: 'key' });
          };
          req.onsuccess = () => { db = req.result; resolve(true); };
          req.onerror = () => resolve(false);
        } catch (e) { resolve(false); }
      });
    }
    function tx(store, mode, fn) {
      return new Promise((resolve, reject) => {
        const t = db.transaction(store, mode);
        const st = t.objectStore(store);
        const r = fn(st);
        t.oncomplete = () => resolve(r && r.result);
        t.onerror = () => reject(t.error);
        t.onabort = () => reject(t.error);
      });
    }
    const keyOf = (store) => (store === 'meta' ? 'key' : 'id');
    return {
      open,
      async all(store) { return db ? tx(store, 'readonly', (s) => s.getAll()) : Array.from(mem[store].values()); },
      async get(store, key) { return db ? tx(store, 'readonly', (s) => s.get(key)) : mem[store].get(key); },
      async put(store, val) { if (db) await tx(store, 'readwrite', (s) => s.put(val)); else mem[store].set(val[keyOf(store)], val); },
      async del(store, key) { if (db) await tx(store, 'readwrite', (s) => s.delete(key)); else mem[store].delete(key); },
    };
  })();

  /* ================= 상태 ================= */
  const S = {
    tab: 'lib',
    keywords: [],
    novels: [],
    kwTab: 'upper',
    editing: null,
    filter: { q: '', upper: new Set(), mode: 'or', noDislike: false, sort: 'new' },
    open: new Set(),
    queue: [],
    running: false,
  };

  /* ================= 유틸 ================= */
  let toastTimer;
  function toast(msg) {
    const t = $('#toast');
    t.textContent = msg; t.style.display = 'block';
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { t.style.display = 'none'; }, 2600);
  }
  function fmtSize(n) { return n > 1048576 ? (n / 1048576).toFixed(1) + 'MB' : Math.max(1, Math.round(n / 1024)) + 'KB'; }
  function fmtChars(n) { return n >= 10000 ? (n / 10000).toFixed(1) + '만자' : n + '자'; }
  function modal(html) {
    const root = $('#modalRoot');
    const el = document.createElement('div');
    el.className = 'modal';
    el.innerHTML = '<div>' + html + '</div>';
    root.appendChild(el);
    return { el, close() { el.remove(); } };
  }
  function ask(message, okLabel) {
    return new Promise((resolve) => {
      const m = modal('<p>' + esc(message) + '</p><div class="row" style="justify-content:flex-end"><button class="btn sec" data-x="0">취소</button><button class="btn" data-x="1">' + esc(okLabel || '확인') + '</button></div>');
      m.el.addEventListener('click', (e) => {
        const b = e.target.closest('[data-x]');
        if (b) { m.close(); resolve(b.dataset.x === '1'); }
      });
    });
  }
  async function copyText(text) {
    try { await navigator.clipboard.writeText(text); toast('복사했어요'); return true; } catch (e) { /* 폴백 */ }
    try {
      const ta = document.createElement('textarea');
      ta.value = text; document.body.appendChild(ta); ta.select();
      const ok = document.execCommand('copy'); ta.remove();
      toast(ok ? '복사했어요' : '복사 실패'); return ok;
    } catch (e) { toast('복사 실패'); return false; }
  }

  /* ================= 키워드 도우미 ================= */
  const kwById = (id) => S.keywords.find((k) => k.id === id);
  const upperKws = () => S.keywords.filter((k) => k.level === 'upper');
  const lowerKws = () => S.keywords.filter((k) => k.level === 'lower');

  function defaultKeywords() {
    const mk = (level, name, synonyms, threshold, tone) => ({ id: uid('k'), level, name, synonyms, threshold, tone: tone || 'normal' });
    return [
      mk('upper', '로맨스판타지', ['로판'], 5), mk('upper', '무협', ['무림', '강호'], 5), mk('upper', '현대판타지', ['헌터', '게이트'], 5),
      mk('lower', '회귀', ['회귀했', '회귀자'], 10, 'like'), mk('lower', '빙의', ['빙의했', '빙의자'], 10, 'normal'),
      mk('lower', '집착남', ['남주가 집착함', '집착하는 남자주인공'], 5, 'like'), mk('lower', '네토라레', ['NTR'], 1, 'dislike'),
    ];
  }
  async function saveKeywords() { await DB.put('meta', { key: 'keywords', value: S.keywords }); }

  // 저장된 분석 결과가 현재 키워드와 맞는지
  function isStale(novel) {
    return S.keywords.some((k) => {
      const r = novel.results && novel.results[k.id];
      return !r || r.sig !== E.termsSignature(k);
    });
  }
  const staleCount = () => S.novels.filter(isStale).length;

  function hitsOf(novel, list) {
    return list.map((k) => {
      const r = (novel.results || {})[k.id];
      const count = r ? r.count : 0;
      return { kw: k, name: k.name, count, status: E.statusOf(count, k.threshold), segs: r ? r.segs : [], density: E.densityPer10k(count, novel.totalChars) };
    });
  }
  const tagName = (novel) => E.tagFileName(novel.title, hitsOf(novel, upperKws()).map((h) => ({ name: h.name, status: h.status })));

  /* ================= 큐 (순차 처리) ================= */
  function enqueue(item) {
    item.key = uid('q'); item.status = '대기'; item.progress = 0;
    S.queue.push(item);
    render();
    runQueue();
  }
  async function runQueue() {
    if (S.running) return;
    S.running = true;
    try {
      for (;;) {
        const item = S.queue.find((q) => q.status === '대기');
        if (!item) break;
        item.status = '진행'; renderQueuePart();
        try {
          if (item.kind === 'new') await processNew(item); else await processRescan(item);
          item.status = '완료'; item.progress = 1;
        } catch (e) {
          item.status = '오류'; item.error = (e && e.message) || String(e);
        }
        item.file = null;
        renderQueuePart();
        S.novels = await DB.all('novels');
        updateStat(); if (S.tab === 'lib') renderLib();
      }
    } finally { S.running = false; render(); }
  }
  const yieldFor = (item) => {
    let last = 0;
    return (p) => new Promise((res) => {
      item.progress = 0.1 + p * 0.9;
      const now = Date.now();
      if (now - last > 200) { last = now; renderQueuePart(); }
      setTimeout(res, 0);
    });
  };
  async function scanWith(item, text) {
    const kws = S.keywords.slice();
    const matcher = E.buildMatcher(kws);
    const r = await E.scanText(text, matcher, { yieldFn: yieldFor(item) });
    const results = {};
    kws.forEach((k, i) => { results[k.id] = { count: r.counts[i], segs: r.segs[i], sig: E.termsSignature(k) }; });
    return { totalChars: r.totalChars, results };
  }
  async function processNew(item) {
    const f = item.file;
    const buf = new Uint8Array(await f.arrayBuffer());
    item.progress = 0.05; renderQueuePart();
    const text = await E.fileToText(item.name, buf);
    if (!text.replace(/\s/g, '')) throw new Error('읽을 수 있는 텍스트가 없습니다');
    const { totalChars, results } = await scanWith(item, text);
    const id = uid('n');
    const ext = (/\.(txt|epub)$/i.exec(item.name) || ['.txt'])[0].toLowerCase();
    await DB.put('texts', { id, text });
    await DB.put('novels', { id, fileName: item.name, title: E.baseTitle(item.name), ext, size: f.size, addedAt: Date.now(), totalChars, results });
    if (item.replaceId) { await DB.del('novels', item.replaceId); await DB.del('texts', item.replaceId); }
  }
  async function processRescan(item) {
    const novel = await DB.get('novels', item.novelId);
    const rec = await DB.get('texts', item.novelId);
    if (!novel) return;
    if (!rec) throw new Error('저장된 본문이 없습니다. 파일을 다시 업로드해 주세요');
    const { totalChars, results } = await scanWith(item, rec.text);
    novel.totalChars = totalChars; novel.results = results;
    await DB.put('novels', novel);
  }

  /* ================= 업로드 + 유사 파일명 경고 ================= */
  function askDuplicate(file, sims, remaining) {
    return new Promise((resolve) => {
      const top = sims[0];
      const ex = top.item;
      const list = sims.slice(0, 3).map((s) => '<div class="muted">· ' + esc(s.item.fileName) + ' (' + Math.round(s.score * 100) + '%)</div>').join('');
      const m = modal(
        '<h3>⚠️ 중복 의심 파일</h3><div class="muted">이미 비슷한 이름의 소설이 있어요.</div>' +
        '<div class="cmp"><div><b>새 파일</b><br>' + esc(file.name) + '<br><span class="muted">' + fmtSize(file.size) + '</span></div>' +
        '<div><b>기존 파일</b><br>' + esc(ex.fileName) + '<br><span class="muted">' + (ex.pending ? '이번 업로드 목록' : fmtSize(ex.size) + ' · ' + fmtChars(ex.totalChars || 0) + ' · ' + new Date(ex.addedAt).toLocaleDateString()) + '</span></div></div>' +
        (sims.length > 1 ? '<div class="muted">그 밖의 유사 파일</div>' + list : '') +
        (remaining > 0 ? '<label class="ck" style="margin:8px 0"><input type="checkbox" id="dupAll"> 남은 ' + remaining + '개 중복 의심 파일에도 같은 선택 적용</label>' : '') +
        '<div class="row" style="margin-top:10px"><button class="btn" data-x="replace">덮어쓰기</button><button class="btn sec" data-x="rename">이름 변경 후 추가</button><button class="btn sec" data-x="cancel">업로드 취소</button></div>');
      m.el.addEventListener('click', (e) => {
        const b = e.target.closest('[data-x]');
        if (!b) return;
        const all = !!(m.el.querySelector('#dupAll') || {}).checked;
        m.close(); resolve({ action: b.dataset.x, all });
      });
    });
  }
  function askRename(def) {
    return new Promise((resolve) => {
      const m = modal('<h3>새 이름</h3><input type="text" id="rn" value="' + esc(def) + '"><div class="row" style="margin-top:10px;justify-content:flex-end"><button class="btn sec" data-x="0">취소</button><button class="btn" data-x="1">추가</button></div>');
      m.el.addEventListener('click', (e) => {
        const b = e.target.closest('[data-x]');
        if (!b) return;
        const v = m.el.querySelector('#rn').value.trim();
        m.close(); resolve(b.dataset.x === '1' && v ? v : null);
      });
    });
  }
  let handling = false;
  async function handleFiles(fileList) {
    if (handling) { toast('이전 업로드 확인 중이에요'); return; }
    handling = true;
    try {
      const all = Array.from(fileList);
      const files = all.filter((f) => /\.(txt|epub)$/i.test(f.name));
      if (all.length - files.length) toast('TXT·ePub만 추가할 수 있어요 (' + (all.length - files.length) + '개 제외)');
      const accepted = [];
      let applyAll = null;
      for (let i = 0; i < files.length; i++) {
        const f = files[i];
        const pool = S.novels.map((n) => ({ fileName: n.fileName, size: n.size, totalChars: n.totalChars, addedAt: n.addedAt, id: n.id }))
          .concat(accepted.map((a) => ({ fileName: a.name, pending: true, ref: a })));
        const sims = E.findSimilar(f.name, pool);
        let name = f.name, replaceId = null, skip = false;
        if (sims.length) {
          let choice = applyAll;
          if (!choice) {
            choice = await askDuplicate(f, sims, files.length - i - 1);
            if (choice.all) applyAll = choice;
          }
          const target = sims[0].item;
          if (choice.action === 'cancel') skip = true;
          else if (choice.action === 'replace') {
            if (target.pending) accepted.splice(accepted.indexOf(target.ref), 1); else replaceId = target.id;
          } else {
            const ext = (/\.(txt|epub)$/i.exec(f.name) || [''])[0];
            const base = E.baseTitle(f.name);
            let proposed = base + ' (2)';
            const taken = new Set(pool.map((p) => E.baseTitle(p.fileName)));
            for (let n = 2; taken.has(proposed); n++) proposed = base + ' (' + n + ')';
            if (choice.all) name = proposed + ext;
            else {
              const v = await askRename(proposed);
              if (!v) skip = true; else name = v + ext;
            }
          }
        }
        if (skip) continue;
        accepted.push({ file: f, name, replaceId });
      }
      accepted.forEach((a) => enqueue({ kind: 'new', file: a.file, name: a.name, replaceId: a.replaceId, label: a.name }));
      if (accepted.length) { S.tab = 'up'; render(); toast(accepted.length + '개를 대기열에 추가했어요'); }
    } finally { handling = false; }
  }

  function enqueueRescan(onlyStale) {
    const targets = S.novels.filter((n) => !onlyStale || isStale(n))
      .filter((n) => !S.queue.some((q) => q.kind === 'rescan' && q.novelId === n.id && q.status === '대기'));
    targets.forEach((n) => enqueue({ kind: 'rescan', novelId: n.id, label: n.fileName }));
    if (targets.length) { toast(targets.length + '개 재분석을 시작해요'); S.tab = 'up'; render(); } else toast('재분석할 파일이 없어요');
  }

  /* ================= 화면: 라이브러리 ================= */
  function filteredNovels() {
    const f = S.filter;
    const uppers = upperKws();
    let list = S.novels.filter((n) => {
      if (f.q && !n.fileName.toLowerCase().includes(f.q.toLowerCase())) return false;
      if (f.upper.size) {
        const hit = (id) => ((n.results || {})[id] || { count: 0 }).count > 0;
        const ids = Array.from(f.upper).filter((id) => uppers.some((u) => u.id === id));
        if (ids.length) {
          const ok = f.mode === 'and' ? ids.every(hit) : ids.some(hit);
          if (!ok) return false;
        }
      }
      if (f.noDislike) {
        const bad = lowerKws().some((k) => k.tone === 'dislike' && E.statusOf(((n.results || {})[k.id] || {}).count || 0, k.threshold) === '!');
        if (bad) return false;
      }
      return true;
    });
    if (f.sort === 'title') list.sort((a, b) => a.title.localeCompare(b.title, 'ko'));
    else list.sort((a, b) => b.addedAt - a.addedAt);
    return list;
  }
  const distHtml = (segs) => {
    const mx = Math.max.apply(null, segs.concat([1]));
    return '<span class="dist">' + segs.map((v) => '<i class="' + (v ? '' : 'z') + '" style="height:' + Math.max(8, Math.round((v / mx) * 100)) + '%"></i>').join('') + '</span>';
  };
  function novelCard(n) {
    const upHits = hitsOf(n, upperKws()).filter((h) => h.count > 0);
    const loHits = hitsOf(n, lowerKws()).filter((h) => h.count > 0);
    const open = S.open.has(n.id);
    const chip = (h, cls) => '<span class="chip ' + cls + (h.status === '!' ? ' bang' : '') + '"><b>' + esc(h.name) + '</b>' + (h.status ? '(' + h.status + ')' : '') + ' <span class="muted">' + h.count + '회</span></span>';
    let h = '<div class="card" data-id="' + n.id + '">';
    h += '<div class="row"><div class="grow title" data-act="toggle" data-id="' + n.id + '">' + esc(n.title) + '</div><span class="muted">' + n.ext.slice(1).toUpperCase() + ' · ' + fmtChars(n.totalChars) + '</span></div>';
    h += '<div class="tagname">' + esc(tagName(n)) + '</div>';
    h += upHits.length ? upHits.map((x) => chip(x, 'up')).join('') : '<span class="muted">상위 키워드 없음</span>';
    ['like', 'normal', 'dislike'].forEach((tone) => {
      const g = loHits.filter((x) => x.kw.tone === tone);
      if (!g.length) return;
      h += '<div class="tn ' + tone + '">' + (tone === 'like' ? '💚' : tone === 'dislike' ? '🚫' : '▫️') + ' ' + TONES[tone] + ' 키워드</div>';
      h += g.map((x) => chip(x, tone)).join('');
    });
    if (isStale(n)) h += '<div class="muted" style="margin-top:6px">⚠️ 키워드가 바뀌어 재분석이 필요해요</div>';
    if (open) {
      h += '<h4>상세 (만자당 빈도 · 구간 분포)</h4>';
      const rows = upHits.concat(loHits);
      h += rows.length ? rows.map((x) => {
        const d = E.distributionOf(x.segs);
        return '<div class="kwrow"><div class="grow"><b>' + esc(x.name) + '</b> <span class="muted">' + x.count + '회 · 1만자당 ' + x.density.toFixed(2) + '회 · 임계 ' + x.kw.threshold + '</span></div><div class="muted">' + d.kind + (d.kind === '집중' ? ' ' + (d.peak * 10) + '~' + (d.peak * 10 + 10) + '%' : '') + '</div>' + distHtml(x.segs) + '</div>';
      }).join('') : '<div class="muted">발견된 키워드가 없어요</div>';
      h += '<div class="row" style="margin-top:10px"><button class="btn sm" data-act="copytag" data-id="' + n.id + '">태그 파일명 복사</button><button class="btn sm sec" data-act="rescan" data-id="' + n.id + '">재분석</button><button class="btn sm dng" data-act="del" data-id="' + n.id + '">삭제</button></div>';
    }
    return h + '</div>';
  }
  function renderLib() {
    const f = S.filter;
    const list = filteredNovels();
    const ups = upperKws();
    let h = '<div class="row"><input type="text" class="grow" id="q" placeholder="제목 검색" value="' + esc(f.q) + '"><select id="sort" style="width:auto"><option value="new"' + (f.sort === 'new' ? ' selected' : '') + '>최근순</option><option value="title"' + (f.sort === 'title' ? ' selected' : '') + '>제목순</option></select></div>';
    h += '<div style="margin-top:8px">' + (ups.length ? ups.map((u) => '<span class="chip' + (f.upper.has(u.id) ? ' sel' : '') + '" data-act="fup" data-id="' + u.id + '">' + esc(u.name) + '</span>').join('') : '<span class="muted">키워드 탭에서 상위 키워드를 추가하세요</span>') + '</div>';
    h += '<div class="row" style="margin-top:4px"><label class="ck"><input type="radio" name="mode" value="or"' + (f.mode === 'or' ? ' checked' : '') + '> 하나라도</label><label class="ck"><input type="radio" name="mode" value="and"' + (f.mode === 'and' ? ' checked' : '') + '> 모두 포함</label><label class="ck"><input type="checkbox" id="nod"' + (f.noDislike ? ' checked' : '') + '> 불호 키워드(!) 제외</label></div>';
    h += '<div class="row" style="margin:8px 0"><span class="muted grow">' + list.length + '/' + S.novels.length + '개</span>' + (list.length ? '<button class="btn sm sec" data-act="copyall">태그 파일명 목록 복사</button>' : '') + '</div>';
    h += list.length ? list.map(novelCard).join('') : '<div class="card muted" style="text-align:center">' + (S.novels.length ? '조건에 맞는 소설이 없어요' : '업로드 탭에서 TXT·ePub 파일을 추가하세요') + '</div>';
    $('#main').innerHTML = h;
  }

  /* ================= 화면: 업로드 / 큐 ================= */
  function queueHtml() {
    if (!S.queue.length) return '<div class="muted" style="text-align:center;margin-top:14px">대기열이 비어 있어요</div>';
    return S.queue.slice().reverse().map((q) => {
      const cls = q.status === '오류' ? 'err' : q.status === '완료' ? 'ok' : 'muted';
      return '<div class="card" style="padding:9px"><div class="row"><div class="grow" style="word-break:break-all">' + (q.kind === 'new' ? '📥 ' : '🔄 ') + esc(q.label) + '</div><span class="' + cls + '">' + q.status + '</span></div>' +
        (q.status === '진행' ? '<div class="bar"><i style="width:' + Math.round(q.progress * 100) + '%"></i></div>' : '') +
        (q.error ? '<div class="err muted">' + esc(q.error) + '</div>' : '') + '</div>';
    }).join('');
  }
  function renderQueuePart() { const el = $('#queueBox'); if (el) el.innerHTML = queueHtml(); updateNav(); }
  function renderUp() {
    const stale = staleCount();
    let h = '<div class="drop" id="drop">📂 여기에 TXT·ePub 파일을 끌어다 놓거나<br>아래 버튼으로 여러 개를 한 번에 선택하세요<br><br><button class="btn" data-act="pick">파일 선택</button></div>';
    h += '<input type="file" id="file" multiple accept=".txt,.epub,text/plain,application/epub+zip" style="display:none">';
    h += '<div class="muted">100개 이상도 한 번에 올릴 수 있어요. 한 파일씩 차례로 처리해서 메모리를 아껴요.</div>';
    if (stale) h += '<div class="banner">키워드가 바뀌어 재분석이 필요한 소설이 ' + stale + '개 있어요.<div style="margin-top:8px"><button class="btn sm" data-act="rescanstale">필요한 것만 재분석</button></div></div>';
    h += '<div class="row" style="margin-top:10px"><h4 class="grow" style="margin:0">대기열</h4>' + (S.novels.length ? '<button class="btn sm sec" data-act="rescanall">전체 재분석</button>' : '') + '<button class="btn sm sec" data-act="clearq">완료 지우기</button></div>';
    h += '<div id="queueBox">' + queueHtml() + '</div>';
    $('#main').innerHTML = h;
  }

  /* ================= 화면: 키워드 관리 ================= */
  function renderKw() {
    const lvl = S.kwTab;
    const list = S.keywords.filter((k) => k.level === lvl);
    const ed = S.editing;
    let h = '<div class="seg"><button data-act="kwtab" data-v="upper" class="' + (lvl === 'upper' ? 'on' : '') + '">상위 키워드</button><button data-act="kwtab" data-v="lower" class="' + (lvl === 'lower' ? 'on' : '') + '">하위 키워드</button></div>';
    h += '<div class="muted">' + (lvl === 'upper' ? '검색·목록에 대표로 보여줄 단어 (예: 로맨스판타지, 무협)' : '소설 본문에서 실제로 찾아낼 세부 요소 (예: 회귀, 빙의, 집착남)') + '</div>';
    const staleN = staleCount();
    if (staleN) h += '<div class="banner">키워드 변경으로 재분석이 필요한 소설: ' + staleN + '개 <button class="btn sm" data-act="rescanstale" style="margin-left:6px">재분석</button></div>';
    h += '<div class="card"><b>' + (ed ? '키워드 수정' : '키워드 추가') + '</b>';
    h += '<div style="margin-top:8px"><input type="text" id="kn" placeholder="키워드 이름" value="' + esc(ed ? ed.name : '') + '"></div>';
    h += '<div style="margin-top:8px"><input type="text" id="ks" placeholder="유의어 (쉼표로 구분) 예: 남주가 집착함, 집착하는 남자주인공" value="' + esc(ed ? ed.synonyms.join(', ') : '') + '"></div>';
    h += '<div class="row" style="margin-top:8px"><div class="grow"><div class="muted">(!) 임계 횟수</div><input type="number" id="kt" min="1" value="' + (ed ? ed.threshold : 5) + '"></div>';
    if (lvl === 'lower') h += '<div class="grow"><div class="muted">성향</div><select id="ktone">' + Object.keys(TONES).map((t) => '<option value="' + t + '"' + ((ed ? ed.tone : 'normal') === t ? ' selected' : '') + '>' + TONES[t] + '</option>').join('') + '</select></div>';
    h += '</div><div class="row" style="margin-top:10px">' + (ed ? '<button class="btn sec" data-act="kwcancel">취소</button>' : '') + '<button class="btn" data-act="kwsave">' + (ed ? '저장' : '추가') + '</button></div></div>';
    h += list.length ? list.map((k) => '<div class="card" style="padding:9px"><div class="row"><div class="grow"><b>' + esc(k.name) + '</b> ' + (k.level === 'lower' ? '<span class="chip ' + k.tone + '">' + TONES[k.tone] + '</span>' : '') + '<div class="muted">' + (k.synonyms.length ? '유의어: ' + esc(k.synonyms.join(', ')) + ' · ' : '') + '임계 ' + k.threshold + '회</div></div><button class="btn sm sec" data-act="kwedit" data-id="' + k.id + '">수정</button><button class="btn sm dng" data-act="kwdel" data-id="' + k.id + '">삭제</button></div></div>').join('') : '<div class="muted" style="text-align:center;margin:12px">등록된 키워드가 없어요</div>';
    h += '<div class="row" style="margin-top:12px"><button class="btn sm sec" data-act="kwexport">키워드 내보내기</button><button class="btn sm sec" data-act="kwimport">키워드 가져오기</button></div>';
    $('#main').innerHTML = h;
  }
  async function saveKwForm() {
    const name = $('#kn').value.trim();
    if (!name) { toast('이름을 입력하세요'); return; }
    const syn = $('#ks').value.split(',').map((s) => s.trim()).filter(Boolean);
    const th = Math.max(1, parseInt($('#kt').value, 10) || 1);
    const tone = S.kwTab === 'lower' ? $('#ktone').value : 'normal';
    if (S.keywords.some((k) => k.level === S.kwTab && k.name === name && (!S.editing || k.id !== S.editing.id))) { toast('이미 있는 이름이에요'); return; }
    if (S.editing) Object.assign(S.editing, { name, synonyms: syn, threshold: th, tone });
    else S.keywords.push({ id: uid('k'), level: S.kwTab, name, synonyms: syn, threshold: th, tone });
    S.editing = null;
    await saveKeywords();
    toast('저장했어요');
    renderKw(); updateStat();
  }

  /* ================= 렌더 / 네비 ================= */
  function updateStat() { $('#stat').textContent = S.novels.length + '개 소설 · 키워드 ' + S.keywords.length + '개'; }
  function updateNav() {
    const pending = S.queue.filter((q) => q.status === '대기' || q.status === '진행').length;
    $('#nav').innerHTML = [['lib', '📚 라이브러리'], ['up', '📥 업로드'], ['kw', '🏷 키워드']].map(([t, l]) =>
      '<button data-tab="' + t + '" class="' + (S.tab === t ? 'on' : '') + '">' + l + (t === 'up' && pending ? '<span class="badge">' + pending + '</span>' : '') + '</button>').join('');
  }
  function render() {
    updateNav(); updateStat();
    if (S.tab === 'lib') renderLib(); else if (S.tab === 'up') renderUp(); else renderKw();
  }

  /* ================= 이벤트 ================= */
  document.addEventListener('click', async (e) => {
    const tabBtn = e.target.closest('[data-tab]');
    if (tabBtn) { S.tab = tabBtn.dataset.tab; render(); $('#main').scrollTop = 0; return; }
    const a = e.target.closest('[data-act]');
    if (!a) return;
    const id = a.dataset.id;
    switch (a.dataset.act) {
      case 'toggle': S.open.has(id) ? S.open.delete(id) : S.open.add(id); renderLib(); break;
      case 'fup': S.filter.upper.has(id) ? S.filter.upper.delete(id) : S.filter.upper.add(id); renderLib(); break;
      case 'copytag': { const n = S.novels.find((x) => x.id === id); if (n) copyText(tagName(n) + n.ext); break; }
      case 'copyall': copyText(filteredNovels().map((n) => tagName(n) + n.ext).join('\n')); break;
      case 'rescan': { const n = S.novels.find((x) => x.id === id); if (n) enqueue({ kind: 'rescan', novelId: id, label: n.fileName }); S.tab = 'up'; render(); break; }
      case 'del': {
        if (await ask('이 소설과 분석 결과를 삭제할까요?', '삭제')) {
          await DB.del('novels', id); await DB.del('texts', id);
          S.novels = S.novels.filter((n) => n.id !== id); S.open.delete(id); render();
        }
        break;
      }
      case 'pick': { const f = $('#file'); if (f) f.click(); break; }
      case 'rescanstale': enqueueRescan(true); break;
      case 'rescanall': enqueueRescan(false); break;
      case 'clearq': S.queue = S.queue.filter((q) => q.status === '대기' || q.status === '진행'); renderUp(); updateNav(); break;
      case 'kwtab': S.kwTab = a.dataset.v; S.editing = null; renderKw(); break;
      case 'kwsave': saveKwForm(); break;
      case 'kwcancel': S.editing = null; renderKw(); break;
      case 'kwedit': S.editing = kwById(id); renderKw(); window.scrollTo(0, 0); $('#main').scrollTop = 0; break;
      case 'kwdel':
        if (await ask('"' + kwById(id).name + '" 키워드를 삭제할까요?', '삭제')) {
          S.keywords = S.keywords.filter((k) => k.id !== id); S.filter.upper.delete(id);
          await saveKeywords(); renderKw(); updateStat();
        }
        break;
      case 'kwexport': {
        const json = JSON.stringify(S.keywords, null, 1);
        const m = modal('<h3>키워드 내보내기</h3><textarea readonly id="jx">' + esc(json) + '</textarea><div class="row" style="margin-top:10px;justify-content:flex-end"><button class="btn sec" data-x="c">닫기</button><button class="btn" data-x="cp">복사</button></div>');
        m.el.addEventListener('click', (ev) => { const b = ev.target.closest('[data-x]'); if (!b) return; if (b.dataset.x === 'cp') copyText(json); else m.close(); });
        break;
      }
      case 'kwimport': {
        const m = modal('<h3>키워드 가져오기</h3><div class="muted">내보내기로 복사한 JSON을 붙여넣으세요. 현재 키워드가 모두 교체됩니다.</div><textarea id="ji"></textarea><div class="row" style="margin-top:10px;justify-content:flex-end"><button class="btn sec" data-x="c">취소</button><button class="btn" data-x="ok">가져오기</button></div>');
        m.el.addEventListener('click', async (ev) => {
          const b = ev.target.closest('[data-x]'); if (!b) return;
          if (b.dataset.x === 'c') { m.close(); return; }
          try {
            const arr = JSON.parse(m.el.querySelector('#ji').value);
            if (!Array.isArray(arr)) throw new Error('배열이 아닙니다');
            S.keywords = arr.filter((k) => k && k.name && (k.level === 'upper' || k.level === 'lower')).map((k) => ({
              id: k.id || uid('k'), level: k.level, name: String(k.name), synonyms: Array.isArray(k.synonyms) ? k.synonyms.map(String) : [],
              threshold: Math.max(1, parseInt(k.threshold, 10) || 1), tone: TONES[k.tone] ? k.tone : 'normal',
            }));
            await saveKeywords(); m.close(); toast('키워드 ' + S.keywords.length + '개를 가져왔어요'); renderKw(); updateStat();
          } catch (err) { toast('JSON 형식이 올바르지 않아요'); }
        });
        break;
      }
    }
  });
  document.addEventListener('input', (e) => {
    if (e.target.id === 'q') {
      S.filter.q = e.target.value; const pos = e.target.selectionStart; renderLib();
      const q = $('#q'); q.focus(); q.setSelectionRange(pos, pos);
    }
  });
  document.addEventListener('change', (e) => {
    const t = e.target;
    if (t.id === 'sort') { S.filter.sort = t.value; renderLib(); }
    else if (t.name === 'mode') { S.filter.mode = t.value; renderLib(); }
    else if (t.id === 'nod') { S.filter.noDislike = t.checked; renderLib(); }
    else if (t.id === 'file') { handleFiles(t.files); t.value = ''; }
  });
  ['dragover', 'dragenter'].forEach((ev) => document.addEventListener(ev, (e) => {
    if (S.tab !== 'up') return; e.preventDefault();
    const d = $('#drop'); if (d) d.classList.add('over');
  }));
  ['dragleave', 'drop'].forEach((ev) => document.addEventListener(ev, (e) => {
    if (ev === 'dragleave' && e.relatedTarget) return;
    const d = $('#drop'); if (d) d.classList.remove('over');
  }));
  document.addEventListener('drop', (e) => {
    e.preventDefault();
    if (S.tab !== 'up') { S.tab = 'up'; render(); }
    if (e.dataTransfer && e.dataTransfer.files.length) handleFiles(e.dataTransfer.files);
  });

  /* ================= 시작 ================= */
  (async function init() {
    const ok = await DB.open();
    if (!ok) toast('저장소를 쓸 수 없어 이번 실행 동안만 보관돼요');
    const rec = await DB.get('meta', 'keywords');
    if (rec && Array.isArray(rec.value)) S.keywords = rec.value;
    else { S.keywords = defaultKeywords(); await saveKeywords(); }
    S.novels = await DB.all('novels');
    render();
  })();
})();
