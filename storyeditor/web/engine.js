/* StoryEditor 엔진: 데이터 모델 / 조건 평가 / 선택지 분기 / 효과 적용 / 검사
 * 브라우저(window.SEngine)와 Node(module.exports) 양쪽에서 동작한다. */
(function (root, factory) {
  if (typeof module === 'object' && module.exports) module.exports = factory();
  else root.SEngine = factory();
})(typeof self !== 'undefined' ? self : this, function () {
  'use strict';

  const BODY_MAX = 1000;
  // 페이지를 묶는 폴더 (페이지 번호는 폴더와 상관없이 프로젝트 전체에서 하나로 이어진다)
  const FOLDERS = [
    { id: 'main', name: '메인 이벤트', short: '메인' },
    { id: 'sub', name: '서브 이벤트', short: '서브' },
    { id: 'random', name: '랜덤 이벤트', short: '랜덤' },
  ];
  const folderOf = (p) => (p && FOLDERS.some((f) => f.id === p.folder) ? p.folder : 'main');
  const folderShort = (id) => (FOLDERS.find((f) => f.id === id) || FOLDERS[0]).short;
  const pagesIn = (pj, folder) => pj.pages.filter((p) => folderOf(p) === folder).sort((a, b) => a.n - b.n);

  /* ---------- 생성 ---------- */
  const uid = (p) => (p || 'x') + Math.random().toString(36).slice(2, 9) + Date.now().toString(36).slice(-3);

  function defaultStats() {
    return [
      { id: 'hp', name: '체력', init: 5, max: 5 },
      { id: 'mental', name: '멘탈', init: 5, max: 5 },
      { id: 'money', name: '돈', init: 0, max: 0 },
    ];
  }
  const newOutcome = (next) => ({ next, mode: 'page', group: '', pool: [], pick: { min: 1, max: 1 }, after: { mode: 'none', next: 0, group: '' }, gain: '', lose: '', achv: '', stats: {} });
  const newChoice = (next) => ({
    id: uid('c'), text: '', show: null, enable: null,
    base: newOutcome(next), branchType: null, branches: [],
  });
  const newPage = (n, folder, group) => ({ n, folder: folder || 'main', group: group || '', title: '', body: '', isEnd: false, choices: [] });
  function newProject(name, tag, color) {
    return {
      id: uid('p'), name: name || '새 이야기', tag: tag || '', color: color || 0,
      createdAt: Date.now(), updatedAt: Date.now(),
      stats: defaultStats(), achievements: [], groups: [], pages: [newPage(0)],
    };
  }
  const clone = (o) => JSON.parse(JSON.stringify(o));

  function lastPageNo(pj) { return pj.pages.reduce((m, p) => Math.max(m, p.n), -1); }
  const pageOf = (pj, n) => pj.pages.find((p) => p.n === n) || null;

  /* ---------- 아이템 문자열 ("a, b*3, @hidden") ---------- */
  function parseItems(str) {
    const out = [];
    String(str || '').split(',').forEach((raw) => {
      let t = raw.trim();
      if (!t) return;
      let qty = 1;
      const m = /^(.*?)\s*\*\s*(\d+)$/.exec(t);
      if (m) { t = m[1].trim(); qty = Math.max(1, parseInt(m[2], 10)); }
      if (!t) return;
      out.push({ name: t, qty, hidden: t.startsWith('@') });
    });
    return out;
  }

  /* ---------- 상태 ---------- */
  function initState(pj) {
    const stats = {};
    pj.stats.forEach((s) => { stats[s.id] = s.init; });
    return { page: 0, items: {}, stats, achv: [], log: [], run: null };
  }
  const have = (st, name) => st.items[name] || 0;
  function addItem(st, name, qty) {
    st.items[name] = have(st, name) + qty;
  }
  function loseItem(st, name, qty) {
    const left = have(st, name) - qty;
    if (left > 0) st.items[name] = left; else delete st.items[name];
  }

  /* ---------- 조건 ("a, b*2, !c, 돈>=100") ---------- */
  const CMP = /^(.+?)\s*(>=|<=|==|>|<)\s*(-?\d+)$/;
  function findStat(pj, name) { return pj.stats.find((s) => s.name === name || s.id === name) || null; }

  function checkTerm(pj, st, raw) {
    let t = raw.trim();
    if (!t) return true;
    let neg = false;
    if (t.startsWith('!')) { neg = true; t = t.slice(1).trim(); }
    let ok;
    const c = CMP.exec(t);
    const stat = c && findStat(pj, c[1].trim());
    if (c && stat) {
      const v = st.stats[stat.id], n = parseInt(c[3], 10);
      ok = c[2] === '>=' ? v >= n : c[2] === '<=' ? v <= n : c[2] === '>' ? v > n : c[2] === '<' ? v < n : v === n;
    } else {
      const it = parseItems(t)[0];
      ok = it ? have(st, it.name) >= it.qty : true;
    }
    return neg ? !ok : ok;
  }
  // 쉼표로 구분된 항목이 모두 만족해야 true
  function checkCond(pj, st, str) {
    return String(str || '').split(',').every((t) => checkTerm(pj, st, t));
  }
  // 활성 조건에서 부족한 항목 목록 (비활성 표시용)
  function missingTerms(pj, st, str) {
    return String(str || '').split(',').map((t) => t.trim()).filter((t) => t && !checkTerm(pj, st, t));
  }


  /* ---------- 스토리 묶음 (제목이 있는 페이지 묶음) ----------
   * pj.groups = [{ id, name, folder, start }], page.group = 묶음 id ('' = 묶음 없음)
   * 선택지 결과(outcome)의 이동 방식: mode 'page'(번호) | 'group'(묶음의 시작 페이지) | 'random'(랜덤 이벤트 목록 중 하나) */
  const groupOf = (pj, id) => (id && (pj.groups || []).find((g) => g.id === id)) || null;
  const groupsIn = (pj, folder) => (pj.groups || []).filter((g) => g.folder === folder);
  const pagesOfGroup = (pj, id) => pj.pages.filter((p) => p.group === id).sort((a, b) => a.n - b.n);
  function groupStart(pj, g) {
    const ps = pagesOfGroup(pj, g.id);
    if (!ps.length) return null;
    return ps.some((p) => p.n === g.start) ? g.start : ps[0].n;
  }
  const groupNameOf = (pj, p) => { const g = p && groupOf(pj, p.group); return g ? g.name : ''; };
  const poolKey = (o) => (o.pool && o.pool.length ? o.pool.slice().sort().join(',') : 'all');

  // 랜덤 이벤트 후보 [{ label, to, group }]. pool 이 비어 있으면 랜덤 이벤트 폴더 전체(묶음 + 묶음 없는 페이지)
  function randomEntries(pj, o) {
    const out = [];
    const add = (g) => { const st = groupStart(pj, g); if (st != null) out.push({ label: g.name, to: st, group: g.id }); };
    if (o.pool && o.pool.length) o.pool.forEach((id) => { const g = groupOf(pj, id); if (g) add(g); });
    else {
      groupsIn(pj, 'random').forEach(add);
      pagesIn(pj, 'random').filter((p) => !groupOf(pj, p.group)).forEach((p) => out.push({ label: pageLabel(p), to: p.n, group: '' }));
    }
    return out;
  }
  // 한 결과가 갈 수 있는 페이지 번호들
  function targetsOf(pj, o) {
    const m = o.mode || 'page';
    if (m === 'group') { const g = groupOf(pj, o.group); const st = g ? groupStart(pj, g) : null; return st == null ? [] : [st]; }
    if (m === 'random') {
      const out = randomEntries(pj, o).map((e) => e.to);
      const a = afterOf(o), d = a && resolveDest(pj, { mode: a.mode, next: a.next, group: a.group });
      if (d && !out.includes(d.to)) out.push(d.to);
      return out;
    }
    if (m === 'continue') return [];
    return [Number(o.next)];
  }
  // 실제 이동 지점 결정. rand01: 0 이상 1 미만 (랜덤 이벤트에서만 사용)
  function resolveDest(pj, o, rand01) {
    const m = o.mode || 'page';
    if (m === 'random') {
      const list = randomEntries(pj, o);
      if (!list.length) return null;
      const r = rand01 == null ? Math.random() : rand01;
      return list[Math.min(list.length - 1, Math.floor(r * list.length))];
    }
    if (m === 'group') { const g = groupOf(pj, o.group); const st = g ? groupStart(pj, g) : null; return st == null ? null : { to: st, label: g.name, group: g.id }; }
    return { to: Number(o.next), label: '', group: '' };
  }


  /* ---------- 랜덤 이벤트 진행: 후보에서 여러 개를 겹치지 않게 뽑아 차례로 → 끝나면 지정한 곳으로 ----------
   * outcome.pick = { min, max } 개수 범위(매번 이 범위에서 무작위), outcome.after = { mode:'none'|'page'|'group', next, group }
   * 진행 상태는 st.run = { queue: 남은 이야기, total, index, after } */
  const toInt = (v, d) => { const n = parseInt(v, 10); return Number.isFinite(n) && n >= 1 ? n : d; };
  function pickRange(o) {
    const min = toInt(o.pick && o.pick.min, 1);
    return { min, max: Math.max(min, toInt(o.pick && o.pick.max, min)) };
  }
  function afterOf(o) {
    const a = o.after;
    return a && (a.mode === 'page' || a.mode === 'group') ? { mode: a.mode, next: a.next, group: a.group || '' } : null;
  }
  const makeRng = (r) => (typeof r === 'function' ? r : () => (r == null ? Math.random() : r));

  function buildRun(pj, o, rng) {
    const entries = randomEntries(pj, o);
    if (!entries.length) return null;
    const { min, max } = pickRange(o);
    const cap = Math.min(max, entries.length), lo = Math.min(min, cap);
    const n = lo === cap ? cap : lo + Math.min(cap - lo, Math.floor(rng() * (cap - lo + 1)));
    const pool = entries.slice(), order = [];
    for (let i = 0; i < n; i++) order.push(pool.splice(Math.min(pool.length - 1, Math.floor(rng() * pool.length)), 1)[0]);
    return { order, after: afterOf(o) };
  }
  const canContinue = (st) => !!(st && st.run && (st.run.queue.length || st.run.after));

  // 다음 이야기로: 남은 이야기가 있으면 그 이야기, 없으면 지정한 이어지는 곳
  function continueRun(pj, st) {
    const run = st.run, fx = [];
    if (!run) return { fx, error: '진행 중인 랜덤 이벤트가 없어요' };
    if (run.queue.length) {
      const e = run.queue.shift();
      run.index++;
      const t = pageOf(pj, e.to);
      if (!t) return { fx, error: 'P.' + e.to + ' 페이지가 없어요' };
      st.page = t.n;
      fx.push({ kind: 'rand', text: '랜덤 이벤트 (' + run.index + '/' + run.total + '): ' + e.label });
      return { fx, dest: e };
    }
    if (run.after) {
      const d = resolveDest(pj, { mode: run.after.mode, next: run.after.next, group: run.after.group });
      st.run = null;
      if (!d || !pageOf(pj, d.to)) return { fx, error: '이어지는 곳을 찾을 수 없어요' };
      st.page = d.to;
      fx.push({ kind: 'rand', text: '랜덤 이벤트 끝 — 이어지는 이야기' + (d.label ? ': ' + d.label : '') });
      return { fx, dest: d, finished: true };
    }
    st.run = null;
    return { fx, error: '이어갈 이야기가 없어요' };
  }

  /* ---------- 선택지 결과 계산 ---------- */
  const branchSum = (ch) => ch.branches.reduce((a, b) => a + (Number(b.when) || 0), 0);
  const baseProb = (ch) => Math.max(0, 100 - branchSum(ch));

  // rand: 0 이상 100 미만 난수 (테스트에서 주입)
  function resolveChoice(pj, st, ch, rand) {
    if (ch.branchType === 'prob') {
      let r = rand == null ? Math.random() * 100 : rand, acc = 0;
      for (const b of ch.branches) {
        acc += Number(b.when) || 0;
        if (r < acc) return b;
      }
      return ch.base;
    }
    if (ch.branchType === 'item') {
      for (const b of ch.branches) if (checkCond(pj, st, b.when)) return b;
      return ch.base;
    }
    return ch.base;
  }

  function clampStat(pj, id, v) {
    const s = pj.stats.find((x) => x.id === id);
    if (!s) return v;
    if (s.max > 0 && v > s.max) v = s.max;
    return v < 0 ? 0 : v;
  }

  // 효과 적용. 보이는 변화 목록(+/-)을 돌려준다.
  function applyOutcome(pj, st, o) {
    const fx = [];
    parseItems(o.gain).forEach((it) => {
      addItem(st, it.name, it.qty);
      if (!it.hidden) fx.push({ kind: 'gain', text: it.name + (it.qty > 1 ? ' ×' + it.qty : '') });
    });
    parseItems(o.lose).forEach((it) => {
      loseItem(st, it.name, it.qty);
      if (!it.hidden) fx.push({ kind: 'lose', text: it.name + (it.qty > 1 ? ' ×' + it.qty : '') });
    });
    Object.keys(o.stats || {}).forEach((id) => {
      const d = Number(o.stats[id]) || 0;
      if (!d) return;
      const s = pj.stats.find((x) => x.id === id);
      if (!s) return;
      st.stats[id] = clampStat(pj, id, st.stats[id] + d);
      fx.push({ kind: d > 0 ? 'gain' : 'lose', text: s.name + ' ' + (d > 0 ? '+' : '') + d });
    });
    if (o.achv) {
      const a = pj.achievements.find((x) => x.id === o.achv);
      if (a && !st.achv.includes(a.id)) { st.achv.push(a.id); fx.push({ kind: 'achv', text: '업적 달성: ' + a.name }); }
    }
    return fx;
  }

  function visibleChoices(pj, st, page) {
    return page.choices
      .filter((c) => !c.show || checkCond(pj, st, c.show))
      .map((c) => {
        const miss = c.enable ? missingTerms(pj, st, c.enable) : [];
        return { choice: c, enabled: miss.length === 0, missing: miss };
      });
  }

  // 선택지를 눌렀을 때: 결과 선택 → 효과 적용 → 이동. 이동할 곳이 없으면 error.
  // rand: 확률 분기용(0~100), rand2: 랜덤 이벤트용 난수(0~1 숫자 또는 () => 0~1 함수) — 테스트에서 주입
  function choose(pj, st, ch, rand, rand2) {
    const o = resolveChoice(pj, st, ch, rand);
    const fx = applyOutcome(pj, st, o);
    const m = o.mode || 'page';
    if (m === 'continue') {
      const r = continueRun(pj, st);
      r.fx.forEach((f) => fx.push(f));
      return r.error ? { fx, error: r.error } : { fx, outcome: o, dest: r.dest };
    }
    if (m === 'random') {
      const run = buildRun(pj, o, makeRng(rand2));
      if (!run) return { fx, error: '랜덤 이벤트 목록이 비어 있어요' };
      const first = run.order[0];
      const target = pageOf(pj, first.to);
      if (!target) return { fx, error: 'P.' + first.to + ' 페이지가 없어요' };
      const total = run.order.length;
      st.run = total > 1 || run.after ? { queue: run.order.slice(1), total, index: 1, after: run.after } : null;
      fx.push({ kind: 'rand', text: '랜덤 이벤트' + (total > 1 ? ' (1/' + total + ')' : '') + ': ' + first.label });
      st.page = target.n;
      return { fx, outcome: o, dest: first, run: total };
    }
    const d = resolveDest(pj, o, null);
    if (!d) return { fx, error: m === 'group' ? '연결된 스토리 묶음에 페이지가 없어요' : 'P.' + o.next + ' 페이지가 없어요' };
    const target = pageOf(pj, d.to);
    if (!target) return { fx, error: 'P.' + d.to + ' 페이지가 없어요' };
    st.page = target.n;
    return { fx, outcome: o, dest: d };
  }

  /* ---------- 검사 ---------- */
  function validate(pj) {
    const out = [];
    const warn = (n, msg) => out.push({ page: n, msg });
    if (!pj.pages.length) warn(null, '페이지가 하나도 없어요');
    if (!pageOf(pj, 0)) warn(null, '0페이지(시작)가 없어요');
    const names = {};
    pj.achievements.forEach((a) => { names[a.name] = (names[a.name] || 0) + 1; });
    Object.keys(names).forEach((k) => { if (names[k] > 1) warn(null, '업적 이름이 겹쳐요: ' + k); });
    const gn = {};
    (pj.groups || []).forEach((g) => { gn[g.name] = (gn[g.name] || 0) + 1; });
    Object.keys(gn).forEach((k) => { if (gn[k] > 1) warn(null, '스토리 묶음 이름이 겹쳐요: ' + k); });
    pj.pages.forEach((p) => {
      if (p.group && !groupOf(pj, p.group)) warn(p.n, '삭제된 스토리 묶음에 속해 있어요');
      if (!p.body.trim()) warn(p.n, '본문이 비어 있어요');
      if (p.body.length > BODY_MAX) warn(p.n, '본문이 ' + BODY_MAX + '자를 넘어요');
      if (!p.isEnd && !p.choices.length) warn(p.n, '선택지가 없어요 (마지막 페이지로 지정하거나 선택지를 추가하세요)');
      p.choices.forEach((c, i) => {
        const tag = '선택지 ' + (i + 1);
        if (!c.text.trim()) warn(p.n, tag + ' 텍스트가 비어 있어요');
        [c.base].concat(c.branches).forEach((o, j) => {
          const where = tag + (j ? ' 분기 ' + j : '');
          const m = o.mode || 'page';
          if (m === 'page' && !pageOf(pj, Number(o.next))) warn(p.n, where + ': P.' + o.next + ' 페이지가 없어요');
          if (m === 'group') {
            const g = groupOf(pj, o.group);
            if (!g) warn(p.n, where + ': 연결된 스토리 묶음이 없어요');
            else if (groupStart(pj, g) == null) warn(p.n, where + ': 스토리 묶음 “' + g.name + '”에 페이지가 없어요');
          }
          if (m === 'random') {
            const n = randomEntries(pj, o).length;
            if (!n) warn(p.n, where + ': 랜덤 이벤트 목록이 비어 있어요');
            else if (pickRange(o).max > n) warn(p.n, where + ': 뽑을 개수(최대 ' + pickRange(o).max + ')보다 후보가 적어요 (후보 ' + n + '개까지만 나와요)');
            const a = afterOf(o);
            if (a) {
              const d = resolveDest(pj, { mode: a.mode, next: a.next, group: a.group });
              if (!d || !pageOf(pj, d.to)) warn(p.n, where + ': 랜덤 이벤트가 끝난 뒤 이어지는 곳을 찾을 수 없어요');
            }
          }
          if (o.achv && !pj.achievements.some((a) => a.id === o.achv)) warn(p.n, tag + ': 삭제된 업적이 연결돼 있어요');
        });
        if (c.branchType === 'prob' && branchSum(c) > 100) warn(p.n, tag + ': 확률 합이 100%를 넘어요');
      });
    });
    return out;
  }


  /* ---------- 분기도 (3열: 이전 이야기 / 현재 / 선택 분기) ---------- */
  const FLOW = { W: 250, COL: 380, ROW: 140 };
  const firstLine = (s) => String(s || '').split('\n').find((l) => l.trim()) || '';
  const pageLabel = (p) => (p && (p.title || firstLine(p.body))) || '(비어 있음)';
  const outcomesOf = (c) => [c.base].concat(c.branches || []);

  // 현재 페이지로 이어지는 상위 페이지 번호 목록
  function parentsOf(pj, n) {
    const set = new Set();
    pj.pages.forEach((p) => {
      if (p.n === n) return;
      p.choices.forEach((c) => outcomesOf(c).forEach((o) => { if (targetsOf(pj, o).includes(n)) set.add(p.n); }));
    });
    return Array.from(set).sort((a, b) => a - b);
  }

  // 한 선택지가 가질 수 있는 도착지(기본 + 분기)를 펼친다.
  // 일반/묶음: { to, tag, groupName? } / 랜덤 이벤트: { rand:true, key, entries, tag }
  function destinationsOf(pj, c) {
    const out = [];
    const one = (o, tag) => {
      const m = o.mode || 'page';
      if (m === 'continue') out.push({ cont: true, key: 'cont', tag });
      else if (m === 'random') {
        const pick = pickRange(o), a = afterOf(o), ad = a ? resolveDest(pj, { mode: a.mode, next: a.next, group: a.group }) : null;
        out.push({
          rand: true, entries: randomEntries(pj, o), tag, pick,
          after: a ? { to: ad ? ad.to : -1, label: ad ? (ad.label || pageLabel(pageOf(pj, ad.to))) : '(없음)', missing: !ad || !pageOf(pj, ad.to) } : null,
          key: 'rand:' + poolKey(o) + ':' + pick.min + '-' + pick.max + ':' + (a ? a.mode + (a.mode === 'group' ? a.group : a.next) : '-'),
        });
      }
      else if (m === 'group') {
        const g = groupOf(pj, o.group), st = g ? groupStart(pj, g) : null;
        out.push({ to: st == null ? -1 : st, tag, groupName: g ? g.name : '', groupMissing: st == null, groupId: o.group });
      } else out.push({ to: Number(o.next), tag });
    };
    one(c.base, c.branchType ? '기본' : '');
    (c.branches || []).forEach((b) => one(b, c.branchType === 'prob' ? '확률 ' + (Number(b.when) || 0) + '%' : '조건 ' + (b.when || '')));
    return out;
  }

  // 노드 높이 추정치 (실제 크기는 화면에서 측정되므로 겹치지 않을 만큼만 넉넉히)
  const H = { prev: 58, cur: 160, target: (rows) => 62 + rows * 34, rand: (rows, entries, extra) => 86 + rows * 34 + entries * 26 + (extra || 0), add: 46, end: 66, cont: (rows) => 62 + rows * 34, gap: 26 };
  function stackY(heights) {
    const total = heights.reduce((a, b) => a + b, 0) + H.gap * Math.max(0, heights.length - 1);
    let y = -total / 2;
    return heights.map((h) => { const top = y; y += h + H.gap; return top; });
  }

  // 페이지 단위 분기도. 도착 페이지가 같은 선택지들은 한 노드로 합친다.
  // trail: 지나온 페이지 번호(오래된 것 → 최근). 비어 있으면 상위 페이지를 대신 보여준다.
  function buildFlow(pj, cur, trail, maxPrev) {
    const page = pageOf(pj, cur);
    if (!page) return { nodes: [], edges: [] };
    maxPrev = maxPrev || 4;
    const fromTrail = (trail || []).length > 0;
    const prevNs = (fromTrail ? trail.slice(-maxPrev) : parentsOf(pj, cur).slice(0, maxPrev)).filter((n, i, a) => pageOf(pj, n) && a.indexOf(n) === i);
    const nodes = [], edges = [];

    const py = stackY(prevNs.map(() => H.prev));
    prevNs.forEach((n, i) => {
      nodes.push({ id: 'prev-' + n, type: 'prev', position: { x: 0, y: py[i] }, data: { n, title: pageLabel(pageOf(pj, n)), fromTrail, folder: folderShort(folderOf(pageOf(pj, n))), groupName: groupNameOf(pj, pageOf(pj, n)) } });
      edges.push({ id: 'e-prev-' + n, source: 'prev-' + n, target: 'cur', type: 'default' });
    });

    nodes.push({ id: 'cur', type: 'cur', position: { x: FLOW.COL, y: -H.cur / 2 }, data: { n: page.n, title: pageLabel(page), text: page.body, isEnd: page.isEnd, folder: folderShort(folderOf(page)), groupName: groupNameOf(pj, page) } });

    const X3 = FLOW.COL * 2;
    if (page.isEnd) {
      nodes.push({ id: 'end', type: 'end', position: { x: X3, y: -H.end / 2 }, data: {} });
      edges.push({ id: 'e-end', source: 'cur', target: 'end', type: 'default' });
      return { nodes, edges };
    }

    // 도착지별로 선택지를 모은다 (처음 나온 순서 유지). 랜덤 이벤트는 후보 목록이 같으면 한 노드로 합친다.
    const groups = [];
    const byKey = new Map();
    page.choices.forEach((c, ci) => {
      const flags = [];
      if (c.show) flags.push('노출 조건');
      if (c.enable) flags.push('활성 조건');
      const seenHere = new Set();
      destinationsOf(pj, c).forEach((d) => {
        const key = d.rand || d.cont ? d.key : d.groupMissing ? 'missing-group:' + d.groupId : 'to-' + d.to;
        let g = byKey.get(key);
        if (!g) {
          if (d.cont) g = { kind: 'cont', key, items: [] };
          else if (d.rand) g = { kind: 'rand', key, entries: d.entries.map((e) => ({ label: e.label, to: e.to })), empty: !d.entries.length, pick: d.pick, after: d.after, items: [] };
          else {
            const target = pageOf(pj, d.to);
            g = { kind: 'page', key, to: d.to, title: target ? pageLabel(target) : '', folder: target ? folderShort(folderOf(target)) : '', groupName: groupNameOf(pj, target), missing: !target, missingGroup: !!d.groupMissing, self: d.to === page.n, items: [] };
          }
          byKey.set(key, g); groups.push(g);
        }
        // 같은 선택지의 분기가 같은 곳으로 가는 경우 한 줄로 합친다
        const ik = ci + ':' + key;
        const tag = d.groupName ? [d.tag, '묶음 ' + d.groupName].filter(Boolean) : d.tag ? [d.tag] : [];
        if (seenHere.has(ik)) { const it = g.items.find((x) => x.ci === ci); tag.forEach((t) => { if (!it.tags.includes(t)) it.tags.push(t); }); return; }
        seenHere.add(ik);
        g.items.push({ ci, text: c.text, tags: tag, flags });
      });
    });

    const heights = groups.map((g) => (g.kind === 'rand' ? H.rand(g.items.length, g.entries.length, 56 + (g.after ? 30 : 0)) : g.kind === 'cont' ? H.cont(g.items.length) : H.target(g.items.length))).concat([H.add]);
    const ys = stackY(heights);
    groups.forEach((g, i) => {
      const id = g.kind === 'rand' ? 'rand-' + g.key : g.kind === 'cont' ? 'cont' : g.missingGroup ? g.key : 'to-' + g.to;
      nodes.push({ id, type: g.kind === 'rand' ? 'rand' : g.kind === 'cont' ? 'cont' : 'target', position: { x: X3, y: ys[i] }, data: g });
      const choiceCount = new Set(g.items.map((x) => x.ci)).size;
      edges.push({
        id: 'e-' + id, source: 'cur', target: id, type: 'default',
        label: choiceCount > 1 ? '선택지 ' + choiceCount + '개' : (g.items[0].text || '').slice(0, 14),
      });
    });
    nodes.push({ id: 'add', type: 'add', position: { x: X3, y: ys[groups.length] }, data: {} });
    return { nodes, edges };
  }

  /* ---------- 단순 스토리 JSON 배열 (사람이 쓰기 쉬운 형식) ----------
   * [{ "id": 0, "title": "시작", "group": "1장", "folder": "main", "text": "본문", "end": false,
   *    "choices": [ { "text": "문을 연다", "to": 1 },
   *                 { "text": "1장으로", "group": "묶음 이름" },
   *                 { "text": "운에 맡긴다", "random": true } ] }]   // random: true(랜덤 폴더 전체) 또는 ["묶음1","묶음2"] */
  function toStoryArray(pj) {
    return pj.pages.slice().sort((a, b) => a.n - b.n).map((p) => {
      const o = { id: p.n, title: p.title || '' };
      const gn = groupNameOf(pj, p);
      if (gn) o.group = gn;
      o.text = p.body;
      if (folderOf(p) !== 'main') o.folder = folderOf(p);
      if (p.isEnd) o.end = true;
      else o.choices = p.choices.map((c) => {
        const b = c.base, m = b.mode || 'page';
        if (m === 'group') { const g = groupOf(pj, b.group); return g ? { text: c.text, group: g.name } : { text: c.text, to: Number(b.next) }; }
        if (m === 'continue') return { text: c.text, continue: true };
        if (m === 'random') {
          const names = (b.pool || []).map((id) => (groupOf(pj, id) || {}).name).filter(Boolean);
          const o = { text: c.text, random: names.length ? names : true };
          const pk = pickRange(b), a = afterOf(b);
          if (pk.min !== 1 || pk.max !== 1) o.pick = [pk.min, pk.max];
          if (a) { const ag = a.mode === 'group' ? groupOf(pj, a.group) : null; o.after = a.mode === 'group' ? (ag ? { group: ag.name } : { to: Number(a.next) }) : { to: Number(a.next) }; }
          return o;
        }
        return { text: c.text, to: Number(b.next) };
      });
      return o;
    });
  }
  // 같은 id 는 덮어쓰고(선택지의 조건·아이템 등 고급 속성은 순서대로 유지), 새 id 는 추가한다. 배열에 없는 페이지는 그대로 둔다.
  // 검사를 모두 통과해야만 변경한다.
  function applyStoryArray(pj, arr) {
    if (!Array.isArray(arr)) throw new Error('JSON 배열이 아니에요 ([ ... ] 형태)');
    const seen = new Set();
    const folderFor = (it) => it.folder || (pageOf(pj, it.id) ? folderOf(pageOf(pj, it.id)) : 'main');
    const known = new Set((pj.groups || []).map((g) => g.name));
    arr.forEach((it) => { if (it && typeof it.group === 'string' && it.group) known.add(it.group); });
    arr.forEach((it, i) => {
      if (!it || typeof it !== 'object') throw new Error((i + 1) + '번째 항목이 객체가 아니에요');
      if (!Number.isInteger(it.id) || it.id < 0) throw new Error((i + 1) + '번째 항목: id 는 0 이상의 정수여야 해요');
      if (seen.has(it.id)) throw new Error('id 가 겹쳐요: ' + it.id);
      seen.add(it.id);
      if (it.folder != null && !FOLDERS.some((f) => f.id === it.folder)) throw new Error('id ' + it.id + ': folder 는 ' + FOLDERS.map((f) => f.id).join(' / ') + ' 중 하나여야 해요');
      if (it.group != null && typeof it.group !== 'string') throw new Error('id ' + it.id + ': group 은 묶음 이름(문자열)이어야 해요');
      if (it.choices != null && !Array.isArray(it.choices)) throw new Error('id ' + it.id + ': choices 는 배열이어야 해요');
      (it.choices || []).forEach((c, j) => {
        const w = 'id ' + it.id + ' 선택지 ' + (j + 1) + ': ';
        if (!c || typeof c !== 'object') throw new Error(w + '객체가 아니에요');
        if (c.continue != null && c.continue !== true) throw new Error(w + 'continue 는 true 여야 해요');
        if (c.random != null) {
          if (c.pick != null && !(Array.isArray(c.pick) && c.pick.length === 2 && c.pick.every((n) => Number.isInteger(n) && n >= 1) && c.pick[0] <= c.pick[1])) throw new Error(w + 'pick 은 [최소, 최대] (1 이상, 최소 ≤ 최대) 형태여야 해요');
          if (c.after != null && !(c.after && ((Number.isInteger(c.after.to) && c.after.to >= 0) || (typeof c.after.group === 'string' && known.has(c.after.group))))) throw new Error(w + 'after 는 { to: 번호 } 또는 { group: 묶음이름 } 이어야 해요');
          if (c.random !== true && !(Array.isArray(c.random) && c.random.length && c.random.every((n) => known.has(n)))) throw new Error(w + 'random 은 true 이거나 존재하는 묶음 이름 배열이어야 해요');
        } else if (c.continue === true) {
          // 다음 이야기로 — 추가 검사 없음
        } else if (c.group != null) {
          if (!known.has(c.group)) throw new Error(w + '스토리 묶음 “' + c.group + '” 을 찾을 수 없어요');
        } else if (!Number.isInteger(c.to) || c.to < 0) throw new Error(w + 'to 는 0 이상의 정수여야 해요 (또는 group / random)');
      });
    });
    pj.groups = pj.groups || [];
    const gid = (name, folder) => {
      let g = pj.groups.find((x) => x.name === name);
      if (!g) { g = { id: uid('g'), name, folder, start: null }; pj.groups.push(g); }
      return g.id;
    };
    let added = 0, updated = 0;
    arr.forEach((it) => {
      let p = pageOf(pj, it.id);
      if (p) updated++; else { p = newPage(it.id); pj.pages.push(p); added++; }
      if (it.folder) p.folder = it.folder;
      if (typeof it.group === 'string') p.group = it.group ? gid(it.group, folderFor(it)) : '';
      p.title = String(it.title || '');
      p.body = String(it.text == null ? '' : it.text);
      p.isEnd = !!it.end;
    });
    arr.forEach((it) => {
      const p = pageOf(pj, it.id);
      if (p.isEnd && !it.choices) return;
      p.choices = (it.choices || []).map((c, j) => {
        const ch = p.choices[j] || newChoice(0);
        ch.text = String(c.text || '');
        const b = ch.base;
        if (c.continue === true) b.mode = 'continue';
        else if (c.random != null) {
          b.mode = 'random'; b.pool = c.random === true ? [] : c.random.map((n) => gid(n, 'random'));
          b.pick = c.pick ? { min: c.pick[0], max: c.pick[1] } : { min: 1, max: 1 };
          b.after = c.after ? (typeof c.after.group === 'string' ? { mode: 'group', next: 0, group: gid(c.after.group, 'main') } : { mode: 'page', next: c.after.to, group: '' }) : { mode: 'none', next: 0, group: '' };
        }
        else if (c.group != null) { b.mode = 'group'; b.group = gid(c.group, 'main'); }
        else { b.mode = 'page'; b.next = c.to; }
        return ch;
      });
    });
    return { added, updated };
  }

  /* ---------- 가져오기 정리 ---------- */
  function normalizeProject(raw) {
    if (!raw || typeof raw !== 'object' || !Array.isArray(raw.pages)) throw new Error('올바른 프로젝트 파일이 아니에요');
    const pj = Object.assign(newProject(), raw);
    pj.id = uid('p');
    delete pj.tutorialVersion;                       // 가져온 프로젝트는 항상 일반 프로젝트
    pj.stats = Array.isArray(raw.stats) && raw.stats.length ? raw.stats : defaultStats();
    pj.achievements = Array.isArray(raw.achievements) ? raw.achievements : [];
    pj.groups = Array.isArray(raw.groups) ? raw.groups : [];
    pj.pages = raw.pages.map((p) => Object.assign(newPage(p.n | 0), p, {
      choices: (p.choices || []).map((c) => Object.assign(newChoice(0), c, { base: Object.assign(newOutcome(0), c.base), branches: (c.branches || []).map((b) => Object.assign(newOutcome(0), b)) })),
    }));
    return pj;
  }

  return {
    BODY_MAX, FOLDERS, folderOf, folderShort, pagesIn,
    groupOf, groupsIn, pagesOfGroup, groupStart, groupNameOf, randomEntries, targetsOf, resolveDest,
    pickRange, afterOf, buildRun, canContinue, continueRun, uid, clone, defaultStats, newOutcome, newChoice, newPage, newProject,
    lastPageNo, pageOf, parseItems, initState, have, addItem, loseItem,
    checkCond, missingTerms, branchSum, baseProb, resolveChoice, applyOutcome,
    visibleChoices, choose, validate, normalizeProject,
    FLOW, parentsOf, pageLabel, destinationsOf, buildFlow, toStoryArray, applyStoryArray,
  };
});
