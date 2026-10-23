/* StoryEditor 엔진: 데이터 모델 / 조건 평가 / 선택지 분기 / 효과 적용 / 검사
 * 브라우저(window.SEngine)와 Node(module.exports) 양쪽에서 동작한다. */
(function (root, factory) {
  if (typeof module === 'object' && module.exports) module.exports = factory();
  else root.SEngine = factory();
})(typeof self !== 'undefined' ? self : this, function () {
  'use strict';

  const BODY_MAX = 1000;

  /* ---------- 생성 ---------- */
  const uid = (p) => (p || 'x') + Math.random().toString(36).slice(2, 9) + Date.now().toString(36).slice(-3);

  function defaultStats() {
    return [
      { id: 'hp', name: '체력', init: 5, max: 5 },
      { id: 'mental', name: '멘탈', init: 5, max: 5 },
      { id: 'money', name: '돈', init: 0, max: 0 },
    ];
  }
  const newOutcome = (next) => ({ next, gain: '', lose: '', achv: '', stats: {} });
  const newChoice = (next) => ({
    id: uid('c'), text: '', show: null, enable: null,
    base: newOutcome(next), branchType: null, branches: [],
  });
  const newPage = (n) => ({ n, title: '', body: '', isEnd: false, choices: [] });
  function newProject(name, tag, color) {
    return {
      id: uid('p'), name: name || '새 이야기', tag: tag || '', color: color || 0,
      createdAt: Date.now(), updatedAt: Date.now(),
      stats: defaultStats(), achievements: [], pages: [newPage(0)],
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
    return { page: 0, items: {}, stats, achv: [], log: [] };
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

  // 선택지를 눌렀을 때: 결과 선택 → 효과 적용 → 이동. 이동할 페이지가 없으면 error.
  function choose(pj, st, ch, rand) {
    const o = resolveChoice(pj, st, ch, rand);
    const fx = applyOutcome(pj, st, o);
    const target = pageOf(pj, Number(o.next));
    if (!target) return { fx, error: 'P.' + o.next + ' 페이지가 없어요' };
    st.page = target.n;
    return { fx, outcome: o };
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
    pj.pages.forEach((p) => {
      if (!p.body.trim()) warn(p.n, '본문이 비어 있어요');
      if (p.body.length > BODY_MAX) warn(p.n, '본문이 ' + BODY_MAX + '자를 넘어요');
      if (!p.isEnd && !p.choices.length) warn(p.n, '선택지가 없어요 (마지막 페이지로 지정하거나 선택지를 추가하세요)');
      p.choices.forEach((c, i) => {
        const tag = '선택지 ' + (i + 1);
        if (!c.text.trim()) warn(p.n, tag + ' 텍스트가 비어 있어요');
        [c.base].concat(c.branches).forEach((o, j) => {
          if (!pageOf(pj, Number(o.next))) warn(p.n, tag + (j ? ' 분기 ' + j : '') + ': P.' + o.next + ' 페이지가 없어요');
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
      p.choices.forEach((c) => outcomesOf(c).forEach((o) => { if (Number(o.next) === n) set.add(p.n); }));
    });
    return Array.from(set).sort((a, b) => a - b);
  }

  // 한 선택지가 가질 수 있는 도착 페이지(기본 + 분기)를 [{ to, tag }] 로 펼친다.
  function destinationsOf(c) {
    const out = [{ to: Number(c.base.next), tag: c.branchType ? '기본' : '' }];
    (c.branches || []).forEach((b) => {
      out.push({ to: Number(b.next), tag: c.branchType === 'prob' ? '확률 ' + (Number(b.when) || 0) + '%' : '조건 ' + (b.when || '') });
    });
    return out;
  }

  // 노드 높이 추정치 (실제 크기는 화면에서 측정되므로 겹치지 않을 만큼만 넉넉히)
  const H = { prev: 58, cur: 160, target: (rows) => 62 + rows * 34, add: 46, end: 66, gap: 26 };
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
      nodes.push({ id: 'prev-' + n, type: 'prev', position: { x: 0, y: py[i] }, data: { n, title: pageLabel(pageOf(pj, n)), fromTrail } });
      edges.push({ id: 'e-prev-' + n, source: 'prev-' + n, target: 'cur', type: 'default' });
    });

    nodes.push({ id: 'cur', type: 'cur', position: { x: FLOW.COL, y: -H.cur / 2 }, data: { n: page.n, title: pageLabel(page), text: page.body, isEnd: page.isEnd } });

    const X3 = FLOW.COL * 2;
    if (page.isEnd) {
      nodes.push({ id: 'end', type: 'end', position: { x: X3, y: -H.end / 2 }, data: {} });
      edges.push({ id: 'e-end', source: 'cur', target: 'end', type: 'default' });
      return { nodes, edges };
    }

    // 도착 페이지별로 선택지를 모은다 (처음 나온 순서 유지)
    const groups = [];
    const byTo = new Map();
    page.choices.forEach((c, ci) => {
      const flags = [];
      if (c.show) flags.push('노출 조건');
      if (c.enable) flags.push('활성 조건');
      const seenHere = new Set();
      destinationsOf(c).forEach((d) => {
        let g = byTo.get(d.to);
        if (!g) {
          const target = pageOf(pj, d.to);
          g = { to: d.to, title: target ? pageLabel(target) : '', missing: !target, self: d.to === page.n, items: [] };
          byTo.set(d.to, g); groups.push(g);
        }
        // 같은 선택지의 분기가 같은 페이지로 가는 경우 한 줄로 합친다
        const key = ci + ':' + d.to;
        if (seenHere.has(key)) { const it = g.items.find((x) => x.ci === ci); if (it && d.tag && !it.tags.includes(d.tag)) it.tags.push(d.tag); return; }
        seenHere.add(key);
        g.items.push({ ci, text: c.text, tags: d.tag ? [d.tag] : [], flags });
      });
    });

    const heights = groups.map((g) => H.target(g.items.length)).concat([H.add]);
    const ys = stackY(heights);
    groups.forEach((g, i) => {
      nodes.push({ id: 'to-' + g.to, type: 'target', position: { x: X3, y: ys[i] }, data: g });
      const choiceCount = new Set(g.items.map((x) => x.ci)).size;
      edges.push({
        id: 'e-to-' + g.to, source: 'cur', target: 'to-' + g.to, type: 'default',
        label: choiceCount > 1 ? '선택지 ' + choiceCount + '개' : (g.items[0].text || '').slice(0, 14),
      });
    });
    nodes.push({ id: 'add', type: 'add', position: { x: X3, y: ys[groups.length] }, data: {} });
    return { nodes, edges };
  }

  /* ---------- 단순 스토리 JSON 배열 (사람이 쓰기 쉬운 형식) ----------
   * [{ "id": 0, "title": "시작", "text": "본문", "end": false,
   *    "choices": [{ "text": "문을 연다", "to": 1 }] }]                       */
  function toStoryArray(pj) {
    return pj.pages.slice().sort((a, b) => a.n - b.n).map((p) => {
      const o = { id: p.n, title: p.title || '', text: p.body };
      if (p.isEnd) o.end = true;
      else o.choices = p.choices.map((c) => ({ text: c.text, to: Number(c.base.next) }));
      return o;
    });
  }
  // 같은 id 는 덮어쓰고(선택지의 조건·아이템 등 고급 속성은 순서대로 유지), 새 id 는 추가한다. 배열에 없는 페이지는 그대로 둔다.
  function applyStoryArray(pj, arr) {
    if (!Array.isArray(arr)) throw new Error('JSON 배열이 아니에요 ([ ... ] 형태)');
    const seen = new Set();
    arr.forEach((it, i) => {
      if (!it || typeof it !== 'object') throw new Error((i + 1) + '번째 항목이 객체가 아니에요');
      if (!Number.isInteger(it.id) || it.id < 0) throw new Error((i + 1) + '번째 항목: id 는 0 이상의 정수여야 해요');
      if (seen.has(it.id)) throw new Error('id 가 겹쳐요: ' + it.id);
      seen.add(it.id);
      if (it.choices != null && !Array.isArray(it.choices)) throw new Error('id ' + it.id + ': choices 는 배열이어야 해요');
      (it.choices || []).forEach((c, j) => {
        if (!c || !Number.isInteger(c.to) || c.to < 0) throw new Error('id ' + it.id + ' 선택지 ' + (j + 1) + ': to 는 0 이상의 정수여야 해요');
      });
    });
    let added = 0, updated = 0;
    arr.forEach((it) => {
      let p = pageOf(pj, it.id);
      if (p) updated++; else { p = newPage(it.id); pj.pages.push(p); added++; }
      p.title = String(it.title || '');
      p.body = String(it.text == null ? '' : it.text);
      p.isEnd = !!it.end;
      if (!p.isEnd || it.choices) {
        const list = it.choices || [];
        p.choices = list.map((c, j) => {
          const ch = p.choices[j] || newChoice(c.to);
          ch.text = String(c.text || '');
          ch.base.next = c.to;
          return ch;
        });
      }
    });
    return { added, updated };
  }

  /* ---------- 가져오기 정리 ---------- */
  function normalizeProject(raw) {
    if (!raw || typeof raw !== 'object' || !Array.isArray(raw.pages)) throw new Error('올바른 프로젝트 파일이 아니에요');
    const pj = Object.assign(newProject(), raw);
    pj.id = uid('p');
    pj.stats = Array.isArray(raw.stats) && raw.stats.length ? raw.stats : defaultStats();
    pj.achievements = Array.isArray(raw.achievements) ? raw.achievements : [];
    pj.pages = raw.pages.map((p) => Object.assign(newPage(p.n | 0), p, {
      choices: (p.choices || []).map((c) => Object.assign(newChoice(0), c, { base: Object.assign(newOutcome(0), c.base), branches: (c.branches || []).map((b) => Object.assign(newOutcome(0), b)) })),
    }));
    return pj;
  }

  return {
    BODY_MAX, uid, clone, defaultStats, newOutcome, newChoice, newPage, newProject,
    lastPageNo, pageOf, parseItems, initState, have, addItem, loseItem,
    checkCond, missingTerms, branchSum, baseProb, resolveChoice, applyOutcome,
    visibleChoices, choose, validate, normalizeProject,
    FLOW, parentsOf, pageLabel, destinationsOf, buildFlow, toStoryArray, applyStoryArray,
  };
});
