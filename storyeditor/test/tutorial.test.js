const test = require('node:test');
const assert = require('node:assert');
const E = require('../web/engine.js');
const T = require('../web/tutorial.js');

const pj = T.build();

// 결정적 난수 (재현 가능한 무작위 걷기)
function rng(seed) { let a = seed >>> 0; return () => { a |= 0; a = (a + 0x6D2B79F5) | 0; let t = Math.imul(a ^ (a >>> 15), 1 | a); t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t; return ((t ^ (t >>> 14)) >>> 0) / 4294967296; }; }

test('튜토리얼: 고정 id/버전, 페이지·묶음 구조', () => {
  assert.strictEqual(pj.id, 'tutorial');
  assert.strictEqual(pj.tutorialVersion, T.VERSION);
  const nums = pj.pages.map((p) => p.n);
  assert.strictEqual(new Set(nums).size, nums.length, '페이지 번호 중복 없음');
  assert.ok(E.pageOf(pj, 0), '0페이지(시작)');
  pj.pages.forEach((p) => { assert.ok(p.body.length > 0 && p.body.length <= E.BODY_MAX, 'P.' + p.n + ' 본문 길이 ' + p.body.length); });
  pj.groups.forEach((g) => assert.ok(E.pagesOfGroup(pj, g.id).length > 0, '빈 묶음 없음: ' + g.name));
  const names = pj.groups.map((g) => g.name);
  assert.strictEqual(new Set(names).size, names.length, '묶음 이름 중복 없음');
  pj.pages.forEach((p) => { if (p.group) assert.strictEqual(E.groupOf(pj, p.group).folder, E.folderOf(p), 'P.' + p.n + ': 페이지와 묶음의 폴더가 같음'); });
});

test('튜토리얼: 검사(validate)에 문제가 없음', () => {
  assert.deepStrictEqual(E.validate(pj), []);
});

test('튜토리얼: 모든 기능이 한 번 이상 들어 있음 (새 기능을 만들면 FEATURES 와 튜토리얼에 추가!)', () => {
  const f = T.features(pj);
  const missing = Object.keys(f).filter((k) => !f[k]);
  assert.deepStrictEqual(missing, [], '튜토리얼에 빠진 기능: ' + missing.join(', '));
});

test('튜토리얼: 처음부터 수료까지 안내대로 진행할 수 있음 (모든 업적 달성)', () => {
  const st = E.initState(pj);
  let r = 0;
  const go = (textPart, rand, rand2) => {
    const page = E.pageOf(pj, st.page);
    const v = E.visibleChoices(pj, st, page).find((x) => x.choice.text.includes(textPart));
    assert.ok(v, 'P.' + st.page + ' 에 “' + textPart + '” 선택지가 보이지 않음');
    assert.ok(v.enabled, 'P.' + st.page + ' “' + textPart + '” 가 비활성: 필요 ' + v.missing.join(','));
    const res = E.choose(pj, st, v.choice, rand, rand2);
    assert.strictEqual(res.error, undefined, 'P.' + page.n + ' “' + textPart + '” → ' + res.error);
    return res;
  };
  const at = (n) => assert.strictEqual(st.page, n, '현재 P.' + st.page + ' 이어야 할 곳 P.' + n);
  // 허브 비밀 길은 아직 안 보임, 수료는 비활성
  const hub = E.visibleChoices(pj, st, E.pageOf(pj, 0));
  assert.ok(!hub.some((x) => x.choice.text.includes('숨겨진 길')), '비밀 메모 없으면 숨겨진 길은 안 보임');
  assert.strictEqual(hub.find((x) => x.choice.text.includes('수료하기')).enabled, false);
  go('1. 페이지'); at(10); go('다음 페이지로'); at(11); go('왼쪽'); at(12); go('도장을 받고'); at(0);
  go('2. 아이템'); at(20); go('상자를 연다'); at(21);
  assert.ok(!E.visibleChoices(pj, st, E.pageOf(pj, 21)).some((x) => x.choice.text.includes('열쇠 없이')), '열쇠가 있으면 “열쇠 없이” 는 안 보임');
  assert.strictEqual(E.visibleChoices(pj, st, E.pageOf(pj, 21)).find((x) => x.choice.text.includes('황금 문')).enabled, false, '황금 문은 항상 비활성');
  go('구리 열쇠로 문을'); at(22); go('2단원 마무리'); at(26); go('도장을 받고'); at(0);
  go('3. 상태'); at(30); go('행상인'); at(31); assert.strictEqual(st.stats.money, 120);
  go('비싼 약'); at(32); assert.strictEqual(st.stats.money, 20); go('업적을 받고'); at(0);
  go('4. 확률', 0); at(40); go('주사위', 0); at(41); go('정리하고'); at(44); go('도장을 받고'); at(0);   // 확률 0 → 첫 분기(대성공)
  go('5. 아이템'); at(50); go('망치'); go('녹슨 열쇠'); go('금고 앞으로'); at(51); go('금고를 연다'); at(52); go('5단원 마무리'); at(56); go('도장을 받고'); at(0);
  go('6. 폴더'); at(60); go('서브 이벤트'); at(200); go('안쪽으로'); at(201); go('튜토리얼로'); at(61); go('도장을 받고'); at(0);
  // 랜덤 이벤트 1~3개: 난수열 0.99 → 3개, 이후 항상 남은 후보의 첫 번째 → 길고양이 → 낯선 상인 → 폐건물
  go('7. 랜덤'); at(70);
  go('1~3개', 0, (() => { const v = [0.99, 0, 0, 0]; let i = 0; return () => v[Math.min(i++, 3)]; })()); at(100);
  assert.strictEqual(st.run.total, 3);
  go('고양이와 인사'); at(110);
  assert.ok(E.pageOf(pj, 110).isEnd && E.canContinue(st), '마지막 페이지 이야기는 계속 버튼으로 이어짐');
  assert.strictEqual(E.continueRun(pj, st).error, undefined); at(120);
  go('손전등을 챙기고'); at(72);                              // 마지막 이야기 뒤 → 묶음 “7-2” 의 시작 페이지
  assert.strictEqual(st.run, null);
  go('다음'); at(73); go('도장을 받고'); at(0);
  assert.strictEqual(E.have(st, '낡은 손전등'), 1);
  // 비밀 메모 → 숨겨진 길 보임
  go('숨겨진 길'); at(91); go('허브로'); at(0);
  go('수료하기'); at(90);
  assert.ok(E.pageOf(pj, 90).isEnd);
  assert.deepStrictEqual(st.achv.slice().sort(), ['a_all', 'a_first', 'a_luck', 'a_stat'], '업적 4개 모두');
});

test('튜토리얼: 무작위로 마구 눌러도 막다른 길·오류 없이 모든 페이지에 닿을 수 있음', () => {
  const all = new Set(pj.pages.map((p) => p.n));
  const visited = new Set();
  let stuck = null, error = null;
  for (let w = 0; w < 400 && visited.size < all.size; w++) {
    const rand = rng(1000 + w);
    const st = E.initState(pj);
    for (let step = 0; step < 1500; step++) {
      const page = E.pageOf(pj, st.page);
      visited.add(page.n);
      if (page.isEnd) {
        if (E.canContinue(st)) { const r = E.continueRun(pj, st); if (r.error) { error = r.error + ' (continue @P.' + page.n + ')'; break; } continue; }
        st.page = 0; st.run = null;                         // 끝 → 허브에서 계속 (아이템 유지: 커버리지용)
        continue;
      }
      const ok = E.visibleChoices(pj, st, page).filter((x) => x.enabled);
      if (!ok.length) { stuck = 'P.' + page.n; break; }
      const pick = ok[Math.floor(rand() * ok.length)].choice;
      const res = E.choose(pj, st, pick, rand() * 100, rand);
      if (res.error) { error = res.error + ' (P.' + page.n + ' “' + pick.text + '”)'; break; }
    }
    if (stuck || error) break;
  }
  assert.strictEqual(stuck, null, '막다른 길: ' + stuck);
  assert.strictEqual(error, null, '오류: ' + error);
  const unreached = [...all].filter((n) => !visited.has(n));
  assert.deepStrictEqual(unreached, [], '닿지 못한 페이지: ' + unreached.join(', '));
});

test('튜토리얼: JSON(스토리 배열) 내보내기 → 가져오기 라운드트립이 깨지지 않음', () => {
  const arr = JSON.parse(JSON.stringify(E.toStoryArray(pj)));
  const copy = T.build();
  E.applyStoryArray(copy, arr);
  assert.deepStrictEqual(E.validate(copy), []);
  assert.deepStrictEqual(T.features(copy), T.features(pj));
});

test('튜토리얼: 프로젝트 가져오기(normalize)는 일반 프로젝트로 취급', () => {
  const imp = E.normalizeProject(JSON.parse(JSON.stringify(pj)));
  assert.notStrictEqual(imp.id, 'tutorial');
  assert.strictEqual(imp.tutorialVersion, undefined);
  assert.deepStrictEqual(E.validate(imp), []);
});
