const test = require('node:test');
const assert = require('node:assert');
const E = require('../web/engine.js');

function proj() {
  const pj = E.newProject('t');
  for (let i = 1; i <= 6; i++) pj.pages.push(E.newPage(i));
  pj.achievements.push({ id: 'a1', name: '첫 업적' });
  return pj;
}
const out = (o) => Object.assign(E.newOutcome(3), o);

test('아이템 문자열: 수량·히든', () => {
  assert.deepStrictEqual(E.parseItems('리트리버*4, @몰래 , 칼'), [
    { name: '리트리버', qty: 4, hidden: false },
    { name: '@몰래', qty: 1, hidden: true },
    { name: '칼', qty: 1, hidden: false },
  ]);
});

test('얻기/잃기 + 히든은 효과에 안 보임', () => {
  const pj = proj(), st = E.initState(pj);
  const fx = E.applyOutcome(pj, st, out({ gain: '리트리버*4, @비밀', lose: '리트리버' }));
  assert.strictEqual(st.items['리트리버'], 3);
  assert.strictEqual(st.items['@비밀'], 1);
  assert.deepStrictEqual(fx.map((f) => f.text), ['리트리버 ×4', '리트리버']);
  E.applyOutcome(pj, st, out({ lose: '리트리버*9' }));
  assert.strictEqual(E.have(st, '리트리버'), 0);
});

test('조건: 모두 있어야 / 부정 / 상태 비교', () => {
  const pj = proj(), st = E.initState(pj);
  E.addItem(st, '순수한 얼굴', 1);
  assert.strictEqual(E.checkCond(pj, st, '순수한 얼굴, 호기심'), false);
  assert.deepStrictEqual(E.missingTerms(pj, st, '순수한 얼굴, 호기심'), ['호기심']);
  E.addItem(st, '호기심', 1);
  assert.strictEqual(E.checkCond(pj, st, '순수한 얼굴, 호기심'), true);
  assert.strictEqual(E.checkCond(pj, st, '!말린 뱀'), true);
  assert.strictEqual(E.checkCond(pj, st, '돈>=1'), false);
  st.stats.money = 100;
  assert.strictEqual(E.checkCond(pj, st, '돈>=100, 체력>3'), true);
  assert.strictEqual(E.checkCond(pj, st, '호기심*2'), false);
});

test('확률 분기: 50% → P4, 25% → P5, 나머지 → P3', () => {
  const pj = proj(), st = E.initState(pj);
  const ch = E.newChoice(3);
  ch.base = out({ next: 3 });
  ch.branchType = 'prob';
  ch.branches = [{ when: 50, ...out({ next: 4, gain: '공간감각' }) }, { when: 25, ...out({ next: 5 }) }];
  assert.strictEqual(E.baseProb(ch), 25);
  assert.strictEqual(E.resolveChoice(pj, st, ch, 0).next, 4);
  assert.strictEqual(E.resolveChoice(pj, st, ch, 49.9).next, 4);
  assert.strictEqual(E.resolveChoice(pj, st, ch, 50).next, 5);
  assert.strictEqual(E.resolveChoice(pj, st, ch, 75).next, 3);
  const r = E.choose(pj, st, ch, 10);
  assert.strictEqual(st.page, 4);
  assert.strictEqual(E.have(st, '공간감각'), 1);
  assert.strictEqual(r.error, undefined);
});

test('아이템 분기: 위에서부터 차례로 (좋은 순서 / 나쁜 순서)', () => {
  const pj = proj();
  const mk = (order) => {
    const ch = E.newChoice(3);
    ch.base = out({ next: 3 });
    ch.branchType = 'item';
    ch.branches = order;
    return ch;
  };
  const good = mk([
    { when: '튼튼한 위장, 미쳐버림', ...out({ next: 4 }) },
    { when: '미쳐버림', ...out({ next: 5 }) },
    { when: '튼튼한 위장', ...out({ next: 6 }) },
  ]);
  const bad = mk([
    { when: '튼튼한 위장', ...out({ next: 4 }) },
    { when: '미쳐버림', ...out({ next: 5 }) },
    { when: '튼튼한 위장, 미쳐버림', ...out({ next: 6 }) },
  ]);
  const has = (...names) => { const st = E.initState(pj); names.forEach((n) => E.addItem(st, n, 1)); return st; };
  assert.strictEqual(E.resolveChoice(pj, has('튼튼한 위장', '미쳐버림'), good).next, 4);
  assert.strictEqual(E.resolveChoice(pj, has('미쳐버림'), good).next, 5);
  assert.strictEqual(E.resolveChoice(pj, has('튼튼한 위장'), good).next, 6);
  assert.strictEqual(E.resolveChoice(pj, has(), good).next, 3);
  assert.strictEqual(E.resolveChoice(pj, has('튼튼한 위장', '미쳐버림'), bad).next, 4);
});

test('선택지 노출/활성', () => {
  const pj = proj(), st = E.initState(pj);
  const p = pj.pages[0];
  const a = E.newChoice(1); a.text = 'a'; a.show = '말린 뱀';
  const b = E.newChoice(1); b.text = 'b'; b.enable = '열쇠, 지도';
  p.choices = [a, b];
  let v = E.visibleChoices(pj, st, p);
  assert.deepStrictEqual(v.map((x) => x.choice.text), ['b']);
  assert.strictEqual(v[0].enabled, false);
  E.addItem(st, '말린 뱀', 1); E.addItem(st, '열쇠', 1);
  v = E.visibleChoices(pj, st, p);
  assert.strictEqual(v.length, 2);
  assert.deepStrictEqual(v[1].missing, ['지도']);
});

test('상태 변화는 0~최대치로 제한, 업적은 한 번만', () => {
  const pj = proj(), st = E.initState(pj);
  E.applyOutcome(pj, st, out({ stats: { hp: -9 } }));
  assert.strictEqual(st.stats.hp, 0);
  E.applyOutcome(pj, st, out({ stats: { hp: 99 } }));
  assert.strictEqual(st.stats.hp, 5);
  assert.strictEqual(E.applyOutcome(pj, st, out({ achv: 'a1' })).length, 1);
  assert.strictEqual(E.applyOutcome(pj, st, out({ achv: 'a1' })).length, 0);
});

test('없는 페이지로 이동하면 error, 이동 안 함', () => {
  const pj = proj(), st = E.initState(pj);
  const ch = E.newChoice(99);
  assert.ok(E.choose(pj, st, ch).error);
  assert.strictEqual(st.page, 0);
});

test('검사: 빈 본문·끊긴 링크·확률 초과', () => {
  const pj = proj();
  const c = E.newChoice(42); c.text = 'x';
  c.branchType = 'prob'; c.branches = [{ when: 70, ...out({ next: 1 }) }, { when: 60, ...out({ next: 1 }) }];
  pj.pages[0].choices = [c];
  const msgs = E.validate(pj).map((x) => x.msg).join('\n');
  assert.match(msgs, /본문이 비어/);
  assert.match(msgs, /P\.42/);
  assert.match(msgs, /100%를 넘어/);
});

test('가져오기 정리 + 라운드트립', () => {
  const pj = proj();
  const c = E.newChoice(1); c.text = 'go'; pj.pages[0].choices.push(c);
  const re = E.normalizeProject(JSON.parse(JSON.stringify(pj)));
  assert.notStrictEqual(re.id, pj.id);
  assert.strictEqual(re.pages[0].choices[0].text, 'go');
  assert.throws(() => E.normalizeProject({}));
});

function flowProj() {
  const pj = E.newProject('f');
  const mk = (n, body, tos, isEnd) => {
    const p = E.newPage(n); p.body = body; p.isEnd = !!isEnd;
    p.choices = (tos || []).map((to, i) => { const c = E.newChoice(to); c.text = '선택' + i; return c; });
    return p;
  };
  pj.pages = [mk(0, '시작', [1, 2]), mk(1, '하나', [3, 3, 4]), mk(2, '둘', [3]), mk(3, '셋', [], true), mk(4, '넷', [0])];
  return pj;
}

test('분기도: 3열 배치 / 노드 종류 / 연결선 (페이지 단위)', () => {
  const pj = flowProj();
  const g = E.buildFlow(pj, 1, [0]);   // P.1 의 선택지: →3, →3, →4  (두 선택지가 P.3 으로 합류)
  const by = (t) => g.nodes.filter((n) => n.type === t);
  assert.strictEqual(by('prev').length, 1);
  assert.strictEqual(by('cur').length, 1);
  assert.strictEqual(by('target').length, 2, '도착 페이지 2개(P.3, P.4)로 합쳐져야 함');
  assert.strictEqual(by('add').length, 1);
  assert.ok(by('prev')[0].position.x < by('cur')[0].position.x);
  assert.ok(by('cur')[0].position.x < by('target')[0].position.x);
  assert.ok(g.edges.some((e) => e.source === 'prev-0' && e.target === 'cur'));
  assert.strictEqual(g.edges.filter((e) => e.source === 'cur').length, 2);
  assert.ok(g.edges.every((e) => g.nodes.some((n) => n.id === e.source) && g.nodes.some((n) => n.id === e.target)));
  // 합류: P.3 노드 하나에 선택지 2개, 간선 라벨에 개수
  const p3 = g.nodes.find((n) => n.id === 'to-3');
  assert.deepStrictEqual(p3.data.items.map((i) => i.ci), [0, 1]);
  assert.strictEqual(g.edges.find((e) => e.id === 'e-to-3').label, '선택지 2개');
  assert.strictEqual(g.edges.find((e) => e.id === 'e-to-4').label, '선택2');
});

test('분기도: 같은 열의 노드는 세로로 겹치지 않고 가운데 정렬', () => {
  const pj = flowProj();
  const g = E.buildFlow(pj, 1, [0]);
  const right = g.nodes.filter((n) => n.position.x === E.FLOW.COL * 2).sort((a, b) => a.position.y - b.position.y);
  const hs = { target: (n) => 62 + n.data.items.length * 34, add: () => 46 };
  for (let i = 1; i < right.length; i++) {
    const prev = right[i - 1];
    assert.ok(right[i].position.y >= prev.position.y + hs[prev.type](prev), '겹침');
  }
  const top = right[0].position.y, last = right[right.length - 1];
  const bottom = last.position.y + hs[last.type](last);
  assert.ok(Math.abs(top + bottom) < 1, '세로 중앙(0) 기준 대칭');
});

test('분기도: 확률/아이템 분기와 한 선택지의 여러 도착 페이지', () => {
  const pj = flowProj();
  const c = pj.pages[0].choices[0];            // P.0 선택0 → 기본 P.1
  c.branchType = 'prob';
  c.branches = [Object.assign(E.newOutcome(2), { when: 30 }), Object.assign(E.newOutcome(2), { when: 20 })];
  const g = E.buildFlow(pj, 0, []);
  const to2 = g.nodes.find((n) => n.id === 'to-2');
  // P.2: 선택1(기본 P.2) + 선택0 의 분기 2개 → 선택0 은 한 줄로 합쳐지고 태그 2개
  assert.strictEqual(to2.data.items.length, 2);
  const it0 = to2.data.items.find((i) => i.ci === 0);
  assert.deepStrictEqual(it0.tags, ['확률 30%', '확률 20%']);
  assert.ok(g.nodes.find((n) => n.id === 'to-1').data.items[0].tags.includes('기본'));
});

test('분기도: trail 이 비면 상위 페이지를 이전 이야기로, 마지막 페이지는 끝 노드', () => {
  const pj = flowProj();
  assert.deepStrictEqual(E.parentsOf(pj, 3), [1, 2]);
  const g = E.buildFlow(pj, 3, []);
  assert.deepStrictEqual(g.nodes.filter((n) => n.type === 'prev').map((n) => n.data.n), [1, 2]);
  assert.strictEqual(g.nodes.filter((n) => n.type === 'target').length, 0);
  assert.strictEqual(g.nodes.filter((n) => n.type === 'end').length, 1);
  assert.strictEqual(E.buildFlow(pj, 99, []).nodes.length, 0);
});

test('분기도: 없는 페이지 표시 / 이전 노드는 최대 4개, 중복 제거', () => {
  const pj = flowProj();
  pj.pages[0].choices[0].base.next = 77;
  const g = E.buildFlow(pj, 0, [4, 1, 2, 1, 4, 2]);
  assert.ok(g.nodes.find((n) => n.id === 'to-77').data.missing);
  assert.ok(g.nodes.filter((n) => n.type === 'prev').length <= 4);
  const ids = g.nodes.map((n) => n.id);
  assert.strictEqual(new Set(ids).size, ids.length);
});

test('스토리 배열: 내보내기 → 적용 라운드트립, 고급 속성 유지', () => {
  const pj = flowProj();
  pj.pages[0].choices[0].base.gain = '열쇠';
  const arr = E.toStoryArray(pj);
  assert.strictEqual(arr.length, 5);
  assert.deepStrictEqual(arr[0].choices[0], { text: '선택0', to: 1 });
  assert.strictEqual(arr[3].end, true);
  arr[0].choices[0].text = '바뀐 텍스트';
  arr[0].choices[0].to = 4;
  arr.push({ id: 9, title: '새 이야기', text: '추가됨', choices: [{ text: '돌아가기', to: 0 }] });
  const r = E.applyStoryArray(pj, arr);
  assert.deepStrictEqual(r, { added: 1, updated: 5 });
  assert.strictEqual(pj.pages[0].choices[0].text, '바뀐 텍스트');
  assert.strictEqual(pj.pages[0].choices[0].base.next, 4);
  assert.strictEqual(pj.pages[0].choices[0].base.gain, '열쇠');
  assert.strictEqual(E.pageOf(pj, 9).title, '새 이야기');
});

test('스토리 배열: 잘못된 입력은 거부하고 아무것도 바꾸지 않음', () => {
  const pj = flowProj();
  const before = JSON.stringify(pj);
  assert.throws(() => E.applyStoryArray(pj, {}), /배열/);
  assert.throws(() => E.applyStoryArray(pj, [{ id: 1.5 }]), /정수/);
  assert.throws(() => E.applyStoryArray(pj, [{ id: 1 }, { id: 1 }]), /겹쳐/);
  assert.throws(() => E.applyStoryArray(pj, [{ id: 1, choices: [{ text: 'x' }] }]), /to/);
  assert.strictEqual(JSON.stringify(pj), before);
});

test('폴더: 기본은 메인, 폴더별 목록, 잘못된 값은 메인으로 취급', () => {
  const pj = flowProj();
  pj.pages[1].folder = 'sub'; pj.pages[2].folder = 'random'; pj.pages[3].folder = '엉뚱한값';
  assert.deepStrictEqual(E.pagesIn(pj, 'main').map((p) => p.n), [0, 3, 4]);
  assert.deepStrictEqual(E.pagesIn(pj, 'sub').map((p) => p.n), [1]);
  assert.deepStrictEqual(E.pagesIn(pj, 'random').map((p) => p.n), [2]);
  assert.strictEqual(E.newPage(9).folder, 'main');
  assert.strictEqual(E.newPage(9, 'sub').folder, 'sub');
  // 폴더가 달라도 링크·분기도는 그대로 동작하고 노드에 폴더 이름이 실림
  const g = E.buildFlow(pj, 0, []);
  assert.strictEqual(g.nodes.find((n) => n.id === 'to-1').data.folder, '서브');
  assert.strictEqual(g.nodes.find((n) => n.id === 'to-2').data.folder, '랜덤');
  assert.strictEqual(E.validate(pj).filter((x) => /페이지가 없어요/.test(x.msg)).length, 0);
});

test('폴더: 스토리 JSON 에 포함·적용, 잘못된 폴더는 거부', () => {
  const pj = flowProj();
  pj.pages[1].folder = 'sub';
  const arr = E.toStoryArray(pj);
  assert.strictEqual(arr[1].folder, 'sub');
  assert.strictEqual(arr[0].folder, undefined, '메인은 생략');
  arr.push({ id: 20, folder: 'random', text: '랜덤', choices: [{ text: 'x', to: 0 }] });
  E.applyStoryArray(pj, arr);
  assert.strictEqual(E.pageOf(pj, 20).folder, 'random');
  assert.strictEqual(E.pageOf(pj, 1).folder, 'sub');
  assert.throws(() => E.applyStoryArray(pj, [{ id: 21, folder: 'nope' }]), /folder/);
  assert.strictEqual(E.pageOf(pj, 21), null);
});

/* ---------- 스토리 묶음 / 랜덤 이벤트 ---------- */
function groupProj() {
  const pj = E.newProject('g');
  const mk = (n, folder, group, body) => { const p = E.newPage(n, folder, group); p.body = body || ('본문' + n); p.isEnd = n >= 10; return p; };
  pj.groups = [
    { id: 'gA', name: '1장 창고', folder: 'main', start: null },
    { id: 'gR1', name: '길고양이', folder: 'random', start: null },
    { id: 'gR2', name: '낯선 상인', folder: 'random', start: 12 },
  ];
  pj.pages = [
    mk(0, 'main', ''), mk(1, 'main', 'gA'), mk(2, 'main', 'gA'),
    mk(10, 'random', 'gR1'), mk(11, 'random', 'gR1'), mk(12, 'random', 'gR2'), mk(13, 'random', 'gR2'),
    mk(14, 'random', ''), // 묶음 없는 랜덤 페이지
  ];
  pj.pages[0].isEnd = false;
  return pj;
}
const mkChoice = (mode, extra) => { const c = E.newChoice(0); c.text = 'c'; Object.assign(c.base, { mode }, extra); return c; };

test('묶음: 시작 페이지는 지정값 → 없으면 가장 낮은 번호', () => {
  const pj = groupProj();
  assert.strictEqual(E.groupStart(pj, E.groupOf(pj, 'gA')), 1);
  assert.strictEqual(E.groupStart(pj, E.groupOf(pj, 'gR1')), 10);
  assert.strictEqual(E.groupStart(pj, E.groupOf(pj, 'gR2')), 12);
  E.groupOf(pj, 'gR2').start = 99;                       // 묶음에 없는 번호면 무시
  assert.strictEqual(E.groupStart(pj, E.groupOf(pj, 'gR2')), 12);
  pj.groups.push({ id: 'gE', name: '빈 묶음', folder: 'sub', start: null });
  assert.strictEqual(E.groupStart(pj, E.groupOf(pj, 'gE')), null);
  assert.deepStrictEqual(E.pagesOfGroup(pj, 'gA').map((p) => p.n), [1, 2]);
});

test('묶음으로 이동: 선택지를 누르면 묶음의 시작 페이지로', () => {
  const pj = groupProj(), st = E.initState(pj);
  const c = mkChoice('group', { group: 'gA', gain: '열쇠' });
  pj.pages[0].choices = [c];
  const r = E.choose(pj, st, c);
  assert.strictEqual(r.error, undefined);
  assert.strictEqual(st.page, 1);
  assert.strictEqual(E.have(st, '열쇠'), 1);
  // 비어 있거나 없는 묶음은 이동하지 않고 오류
  const st2 = E.initState(pj);
  assert.match(E.choose(pj, st2, mkChoice('group', { group: 'nope' })).error, /묶음/);
  assert.strictEqual(st2.page, 0);
});

test('랜덤 이벤트: 목록 중 하나 (난수 주입), 기본은 랜덤 폴더 전체', () => {
  const pj = groupProj();
  const c = mkChoice('random');
  const labels = E.randomEntries(pj, c.base).map((e) => e.label + ':' + e.to);
  assert.deepStrictEqual(labels, ['길고양이:10', '낯선 상인:12', '본문14:14'], '묶음 2개 + 묶음 없는 랜덤 페이지 1개');
  const pick = (r) => { const st = E.initState(pj); const res = E.choose(pj, st, c, 0, r); return { page: st.page, fx: res.fx }; };
  assert.strictEqual(pick(0).page, 10);
  assert.strictEqual(pick(0.34).page, 12);
  assert.strictEqual(pick(0.999).page, 14);
  assert.ok(pick(0).fx.some((f) => f.kind === 'rand' && /길고양이/.test(f.text)));
  // 균등하게 뽑히는지: 난수 없이 많이 돌려 세 곳 모두 나오는지만 확인
  const seen = new Set(); for (let i = 0; i < 200; i++) seen.add(pick(undefined).page);
  assert.deepStrictEqual([...seen].sort((a, b) => a - b), [10, 12, 14]);
});

test('랜덤 이벤트: 일부 묶음만 지정 / 비어 있으면 오류', () => {
  const pj = groupProj();
  const c = mkChoice('random', { pool: ['gR2'] });
  const st = E.initState(pj);
  assert.deepStrictEqual(E.randomEntries(pj, c.base).map((e) => e.to), [12]);
  assert.strictEqual(E.choose(pj, st, c, 0, 0.9).error, undefined);
  assert.strictEqual(st.page, 12);
  pj.pages = pj.pages.filter((p) => p.folder !== 'random');
  const st2 = E.initState(pj);
  assert.match(E.choose(pj, st2, mkChoice('random')).error, /비어/);
});

test('묶음/랜덤 연결도 상위 페이지·검사·분기도에 반영', () => {
  const pj = groupProj();
  const c1 = mkChoice('group', { group: 'gA' });
  const c2 = mkChoice('random');
  pj.pages[0].choices = [c1, c2];
  assert.deepStrictEqual(E.parentsOf(pj, 1), [0]);
  assert.deepStrictEqual(E.parentsOf(pj, 12), [0]);
  assert.strictEqual(E.validate(pj).some((x) => /없어요|비어 있어요/.test(x.msg) && x.page === 0), false);
  const g = E.buildFlow(pj, 0, []);
  const dest = g.nodes.find((n) => n.id === 'to-1');
  assert.ok(dest && dest.data.items[0].tags.includes('묶음 1장 창고'));
  const rand = g.nodes.find((n) => n.type === 'rand');
  assert.ok(rand, '랜덤 이벤트 노드');
  assert.strictEqual(rand.data.entries.length, 3);
  assert.ok(g.edges.some((e) => e.target === rand.id));
  // 깨진 연결은 검사에 걸림
  pj.pages[0].choices.push(mkChoice('group', { group: 'ghost' }));
  assert.ok(E.validate(pj).some((x) => /연결된 스토리 묶음이 없어요/.test(x.msg)));
});

test('분기도: 서로 다른 랜덤 풀은 각각 노드, 같은 풀은 합쳐짐', () => {
  const pj = groupProj();
  pj.pages[0].choices = [mkChoice('random'), mkChoice('random'), mkChoice('random', { pool: ['gR1'] })];
  const g = E.buildFlow(pj, 0, []);
  const rands = g.nodes.filter((n) => n.type === 'rand');
  assert.strictEqual(rands.length, 2);
  assert.strictEqual(rands.find((n) => n.data.items.length === 2) !== undefined, true);
  const ids = g.nodes.map((n) => n.id);
  assert.strictEqual(new Set(ids).size, ids.length);
});

test('스토리 JSON: 묶음·랜덤 이벤트 왕복', () => {
  const pj = groupProj();
  pj.pages[0].choices = [mkChoice('group', { group: 'gA' }), mkChoice('random', { pool: ['gR1', 'gR2'] }), mkChoice('random')];
  pj.pages[0].choices.forEach((c, i) => { c.text = '선택' + i; });
  const arr = E.toStoryArray(pj);
  assert.strictEqual(arr.find((x) => x.id === 1).group, '1장 창고');
  assert.deepStrictEqual(arr[0].choices[0], { text: '선택0', group: '1장 창고' });
  assert.deepStrictEqual(arr[0].choices[1], { text: '선택1', random: ['길고양이', '낯선 상인'] });
  assert.deepStrictEqual(arr[0].choices[2], { text: '선택2', random: true });
  // 새 묶음을 JSON 으로 만들고 연결
  const next = JSON.parse(JSON.stringify(arr));
  next.push({ id: 30, group: '새 묶음', folder: 'sub', text: '새', end: true });
  next[0].choices.push({ text: '새로', group: '새 묶음' });
  const r = E.applyStoryArray(pj, next);
  assert.strictEqual(r.added, 1);
  const ng = pj.groups.find((g) => g.name === '새 묶음');
  assert.ok(ng && ng.folder === 'sub');
  assert.strictEqual(E.pageOf(pj, 30).group, ng.id);
  assert.strictEqual(pj.pages[0].choices[3].base.mode, 'group');
  assert.strictEqual(pj.pages[0].choices[3].base.group, ng.id);
  assert.strictEqual(pj.pages[0].choices[0].base.group, 'gA', '기존 묶음 id 유지');
});

test('스토리 JSON: 없는 묶음 이름/잘못된 random 은 거부하고 변경 없음', () => {
  const pj = groupProj();
  const before = JSON.stringify(pj);
  assert.throws(() => E.applyStoryArray(pj, [{ id: 0, text: 'x', choices: [{ text: 'a', group: '없는 묶음' }] }]), /찾을 수 없어요/);
  assert.throws(() => E.applyStoryArray(pj, [{ id: 0, text: 'x', choices: [{ text: 'a', random: ['없는'] }] }]), /random/);
  assert.throws(() => E.applyStoryArray(pj, [{ id: 0, text: 'x', choices: [{ text: 'a' }] }]), /to/);
  assert.strictEqual(JSON.stringify(pj), before);
});

test('가져오기: 옛 프로젝트(묶음 없음, mode 없음)도 정상 동작', () => {
  const old = { name: 'old', pages: [{ n: 0, body: 'a', choices: [{ id: 'c1', text: 'go', base: { next: 1 } }] }, { n: 1, body: 'b', isEnd: true, choices: [] }] };
  const pj = E.normalizeProject(old);
  assert.deepStrictEqual(pj.groups, []);
  const st = E.initState(pj);
  assert.strictEqual(E.choose(pj, st, pj.pages[0].choices[0]).error, undefined);
  assert.strictEqual(st.page, 1);
  assert.deepStrictEqual(E.validate(pj), []);
});
