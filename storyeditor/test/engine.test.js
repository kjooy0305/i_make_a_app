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
