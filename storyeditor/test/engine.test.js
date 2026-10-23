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
