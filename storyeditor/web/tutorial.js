/* StoryEditor 튜토리얼 프로젝트 (고정 예시)
 * - 모든 기능을 직접 눌러 보며 익히는 단원형 이야기. 앱을 열 때마다 목록 맨 위에 항상 있다.
 * - 기능이 추가되면: ① 여기에 단원(또는 페이지)을 추가하고 ② FEATURES 에 감지 함수를 추가하고 ③ VERSION 을 올린다.
 *   (VERSION 이 올라가면 저장돼 있던 튜토리얼이 자동으로 새 내용으로 바뀐다. 테스트: node --test test/tutorial.test.js)
 * 브라우저(window.STutorial)와 Node(module.exports) 양쪽에서 동작한다. */
(function (root, factory) {
  if (typeof module === 'object' && module.exports) module.exports = factory(require('./engine.js'));
  else root.STutorial = factory(root.SEngine);
})(typeof self !== 'undefined' ? self : this, function (E) {
  'use strict';

  const ID = 'tutorial';
  const VERSION = 1;

  /* ---------- 만들기 도우미 ---------- */
  // 선택지 결과 하나를 만든다. spec: { to | group | random:{pool,min,max,after:{page}|{group}} | cont, gain, lose, achv, stats }
  function outcome(spec, ids) {
    const o = E.newOutcome(spec.to == null ? 0 : spec.to);
    if (spec.group) { o.mode = 'group'; o.group = ids.group(spec.group); }
    if (spec.cont) o.mode = 'continue';
    if (spec.random) {
      const r = spec.random;
      o.mode = 'random';
      o.pool = (r.pool || []).map(ids.group);
      o.pick = { min: r.min || 1, max: r.max || r.min || 1 };
      if (r.after && r.after.page != null) o.after = { mode: 'page', next: r.after.page, group: '' };
      if (r.after && r.after.group) o.after = { mode: 'group', next: 0, group: ids.group(r.after.group) };
    }
    o.gain = spec.gain || ''; o.lose = spec.lose || ''; o.achv = spec.achv ? ids.achv(spec.achv) : '';
    o.stats = Object.assign({}, spec.stats || {});
    return o;
  }
  // 선택지: text + 결과(위 outcome 필드) + show/enable 조건 + prob(확률 분기) / items(아이템 분기)
  function choice(spec, ids) {
    const c = E.newChoice(spec.to == null ? 0 : spec.to);
    c.text = spec.text;
    c.show = spec.show == null ? null : spec.show;
    c.enable = spec.enable == null ? null : spec.enable;
    c.base = outcome(spec, ids);
    if (spec.prob) { c.branchType = 'prob'; c.branches = spec.prob.map((b) => Object.assign(outcome(b, ids), { when: b.p })); }
    if (spec.items) { c.branchType = 'item'; c.branches = spec.items.map((b) => Object.assign(outcome(b, ids), { when: b.when })); }
    return c;
  }

  /* ---------- 내용 ---------- */
  const GROUPS = [
    { key: 'hub', name: '0. 튜토리얼 허브', folder: 'main' },
    { key: 'l1', name: '1. 페이지와 선택지', folder: 'main' },
    { key: 'l2', name: '2. 아이템과 조건', folder: 'main' },
    { key: 'l3', name: '3. 상태값과 업적', folder: 'main' },
    { key: 'l4', name: '4. 확률 분기', folder: 'main' },
    { key: 'l5', name: '5. 아이템 분기', folder: 'main' },
    { key: 'l6', name: '6. 폴더와 스토리 묶음', folder: 'main' },
    { key: 'l7', name: '7. 랜덤 이벤트', folder: 'main' },
    { key: 'l7end', name: '7-2. 랜덤 이벤트가 끝난 뒤', folder: 'main' },
    { key: 'end', name: '8. 수료', folder: 'main' },
    { key: 'sub', name: '서브 · 비밀의 방', folder: 'sub', start: 200 },
    { key: 'cat', name: '랜덤 · 길고양이', folder: 'random' },
    { key: 'shop', name: '랜덤 · 낯선 상인', folder: 'random' },
    { key: 'ruin', name: '랜덤 · 폐건물', folder: 'random' },
    { key: 'market', name: '랜덤 · 시장', folder: 'random' },
  ];
  const ACHVS = [
    { key: 'first', name: '첫 걸음' },
    { key: 'stat', name: '상태 관리자' },
    { key: 'luck', name: '행운아' },
    { key: 'all', name: '튜토리얼 수료' },
  ];
  const STAMPS = '도장1, 도장2, 도장3, 도장4, 도장5, 도장6, 도장7';

  // [번호, { title, group, folder, end, body }, [선택지...]]
  const PAGES = [
    /* ───── 0. 허브 ───── */
    [0, { title: '튜토리얼 허브', group: 'hub', body:
      '📘 튜토리얼에 오신 것을 환영해요!\n\n이 프로젝트는 에디터의 모든 기능을 눌러 보며 익히는 “고정 예시”예요. 단원을 마치면 이 허브로 돌아오고, 도장을 모두 모으면 수료할 수 있어요.\n\n💡 지금 보이는 글이 “본문”(페이지 P.0)이고, 아래 버튼 하나하나가 “선택지”예요. 에디터에서 같은 구조를 열어 보세요.\n🔀 위쪽 “분기도” 버튼을 누르면 이 페이지에서 갈 수 있는 곳이 한눈에 보여요.\n\n※ 기능이 추가되면 이 튜토리얼도 자동으로 새 단원이 생겨요. 실험은 홈에서 “복제”한 뒤에 하는 게 안전해요.' }, [
      { text: '1. 페이지와 선택지', to: 10 },
      { text: '2. 아이템과 조건', to: 20 },
      { text: '3. 상태값과 업적', to: 30 },
      { text: '4. 확률 분기', group: 'l4' },
      { text: '5. 아이템 분기', group: 'l5' },
      { text: '6. 폴더와 스토리 묶음', group: 'l6' },
      { text: '7. 랜덤 이벤트', group: 'l7' },
      { text: '🔒 숨겨진 길 (비밀 메모가 있으면 보여요)', show: '@비밀 메모', to: 91 },
      { text: '🎓 수료하기 (도장 7개 필요)', enable: STAMPS, to: 90, achv: 'all' },
    ]],
    [91, { title: '숨겨진 길', group: 'hub', body:
      '🗝 숨겨진 길을 찾았어요!\n\n이 선택지에는 “노출 조건”으로 @비밀 메모(히든 아이템)가 걸려 있어요. 히든 아이템은 이름 앞에 @를 붙이면 상태줄에는 보이지 않지만 조건에는 쓸 수 있어요.\n\n2단원에서 상자를 열어 비밀 메모를 얻어야만 이 길이 보여요.' }, [
      { text: '허브로 돌아간다', to: 0 },
    ]],

    /* ───── 1. 페이지와 선택지 ───── */
    [10, { title: '1단원 · 페이지', group: 'l1', body:
      '📖 1단원 · 페이지와 선택지\n\n한 페이지는 게임에서 한 번에 보이는 화면이에요. “본문”(최대 1000자)과 하나 이상의 “선택지”로 이루어져요.\n\n• 페이지는 P.번호로 구분돼요 (이 페이지는 P.10).\n• 선택지의 “이동 P.”에 넘어갈 페이지 번호를 적어요.\n• 새 선택지의 기본값은 “현재 마지막 페이지 + 1”이라 이어 쓰기 편해요.\n• 페이지에 “제목”을 적으면 분기도에서 제목으로 보여요.' }, [
      { text: '다음 페이지로', to: 11 },
      { text: '허브로 돌아가기', to: 0 },
    ]],
    [11, { title: '두 갈래 길', group: 'l1', body:
      '🛤 두 갈래 길\n\n아래 두 선택지는 사실 같은 페이지(P.12)로 이어져요.\n\n🔀 “분기도”로 이 페이지를 보면, 같은 페이지로 모이는 선택지는 한 노드 안에 줄로 합쳐져서 보여요. 선택지가 달라도 도착 페이지가 같으면 이렇게 표시돼요.' }, [
      { text: '왼쪽 길로 간다', to: 12 },
      { text: '오른쪽 길로 간다', to: 12 },
    ]],
    [12, { title: '합류점', group: 'l1', body:
      '🏁 합류점이에요.\n\n마지막으로 “마지막 페이지”를 볼까요? 페이지 카드의 “마지막 페이지” 체크를 켜면 선택지를 쓸 수 없고, 게임에서는 “다시 시작 / 종료”가 나와요.\n\n아래 “막다른 길 구경”을 누르면 마지막 페이지(P.13)를 볼 수 있어요. 구경하고 나면 처음부터 다시 시작해야 해요.' }, [
      { text: '도장을 받고 허브로 (업적 “첫 걸음”)', to: 0, gain: '도장1', achv: 'first' },
      { text: '막다른 길 구경 (마지막 페이지)', to: 13 },
    ]],
    [13, { title: '마지막 페이지 예시', group: 'l1', end: true, body:
      '■ 여기는 “마지막 페이지”예요.\n\n선택지 대신 “다시 시작 / 종료”가 나왔죠? 이야기를 여기서 끝내고 싶을 때 이렇게 지정해요.\n\n(다시 시작을 누르면 허브에서 새로 시작해요. 아이템과 도장도 처음 상태로 돌아가요.)' }, []],

    /* ───── 2. 아이템과 조건 ───── */
    [20, { title: '2단원 · 아이템', group: 'l2', body:
      '🎒 2단원 · 아이템과 조건\n\n선택지의 “＋ 속성 추가…”에서 “얻는 아이템 / 잃는 아이템”을 고를 수 있어요.\n• 쉼표(,)로 여러 개: 구리 열쇠, 금화\n• 수량: 금화*5 (5개)\n• 히든: @비밀 메모 (상태줄에 안 보이지만 조건에 쓸 수 있어요)\n\n아래 첫 선택지는 “구리 열쇠, 금화*5, @비밀 메모”를 한꺼번에 얻어요. 미리보기 위쪽 🎒 칸을 보세요.' }, [
      { text: '상자를 연다 (아이템 얻기)', to: 21, gain: '구리 열쇠, 금화*5, @비밀 메모' },
      { text: '상자는 무시하고 문으로 간다', to: 21 },
    ]],
    [21, { title: '조건이 걸린 문', group: 'l2', body:
      '🚪 조건이 걸린 문 앞이에요.\n\n선택지의 ⋮ 영역에서 두 가지 조건을 켤 수 있어요.\n• 노출 조건: 없으면 선택지가 아예 안 보여요.\n• 활성 조건: 없으면 보이지만 눌러지지 않고, “필요: …”가 표시돼요.\n• 조건 문법: 쉼표(모두 있어야), 열쇠*2(수량), !열쇠(없어야), 돈>=100(상태 비교)\n\n지금 가진 아이템에 따라 아래 선택지가 달라 보여요. 상자를 열었는지 안 열었는지 비교해 보세요!' }, [
      { text: '구리 열쇠로 문을 연다 (활성 조건: 구리 열쇠)', to: 22, enable: '구리 열쇠' },
      { text: '금화 5개를 내고 지름길로 간다 (활성 조건: 금화*5, 금화 5개를 잃어요)', to: 23, enable: '금화*5', lose: '금화*5' },
      { text: '황금 문을 연다 (활성 조건: 구리 열쇠*2 → 항상 비활성)', to: 22, enable: '구리 열쇠*2' },
      { text: '📜 비밀 메모를 읽는다 (노출 조건: @비밀 메모)', to: 24, show: '@비밀 메모' },
      { text: '열쇠 없이 문을 민다 (노출 조건: !구리 열쇠)', to: 25, show: '!구리 열쇠' },
    ]],
    [22, { title: '문이 열렸다', group: 'l2', body: '✅ 구리 열쇠로 문이 열렸어요.\n\n“활성 조건”은 아이템을 쓰지 않고 있기만 하면 통과해요. 아이템을 소모하려면 “잃는 아이템”을 같이 설정해요.' }, [
      { text: '2단원 마무리로', to: 26 },
    ]],
    [23, { title: '지름길', group: 'l2', body: '💰 금화 5개를 내고 지름길로 왔어요.\n\n이 선택지는 “활성 조건: 금화*5”와 “잃는 아이템: 금화*5”를 함께 썼어요. 상태줄의 금화가 줄어든 걸 확인하세요.' }, [
      { text: '2단원 마무리로', to: 26 },
    ]],
    [24, { title: '비밀 메모', group: 'l2', body: '📜 “허브에 숨겨진 길이 있다…”\n\n@비밀 메모는 히든 아이템이라 상태줄에 안 보이지만, 허브의 “🔒 숨겨진 길” 선택지를 보이게 만들어요.' }, [
      { text: '2단원 마무리로', to: 26 },
    ]],
    [25, { title: '꿈쩍도 안 한다', group: 'l2', body: '🙅 문은 꿈쩍도 하지 않아요.\n\n이 선택지는 “노출 조건: !구리 열쇠”라서 열쇠가 없을 때만 보였어요. (열쇠를 얻으려면 상자부터 열어야 해요)' }, [
      { text: '문 앞으로 돌아간다', to: 21 },
      { text: '처음(상자)으로 돌아간다', to: 20 },
    ]],
    [26, { title: '2단원 마무리', group: 'l2', body: '🎉 2단원 완료! 도장을 받아요.\n\n도장도 아이템이에요. 허브의 “수료하기”는 “활성 조건”으로 도장 7개를 요구해요.' }, [
      { text: '도장을 받고 허브로', to: 0, gain: '도장2' },
    ]],

    /* ───── 3. 상태값과 업적 ───── */
    [30, { title: '3단원 · 상태값', group: 'l3', body:
      '❤ 3단원 · 상태값과 업적\n\n체력 / 멘탈 / 돈 같은 “상태값”은 선택지 속성의 “체력 변화” 등으로 바꿔요. (이름·시작값·최대치는 ⚙ 프로젝트 설정에서 고쳐요)\n\n• 값은 0 아래로 내려가지 않고, 최대치를 넘지 않아요.\n• 조건에서는 돈>=100 처럼 비교할 수 있어요.\n\n아래 두 선택지를 눌러 위쪽 상태줄의 숫자 변화를 확인해 보세요.' }, [
      { text: '수상한 버섯을 먹는다 (체력 -1)', to: 31, stats: { hp: -1 } },
      { text: '행상인에게 동전을 받는다 (돈 +120)', to: 31, stats: { money: 120 } },
    ]],
    [31, { title: '약 가게', group: 'l3', body:
      '💊 약 가게예요.\n\n첫 선택지는 “활성 조건: 돈>=100”이에요. 돈이 100보다 적으면 비활성으로 보여요. (앞에서 동전을 받았다면 눌러 볼 수 있어요)\n\n약을 사면 돈이 100 줄고 체력이 2 늘어요. 한 선택지에 상태 변화를 여러 개 겹쳐 쓸 수 있어요.' }, [
      { text: '비싼 약을 산다 (활성 조건: 돈>=100)', to: 32, enable: '돈>=100', stats: { money: -100, hp: 2 } },
      { text: '치료를 포기한다', to: 32 },
    ]],
    [32, { title: '업적', group: 'l3', body:
      '🏆 업적 달성!\n\n업적은 왼쪽 아래 “🏆 업적”에서 만들고, 선택지 속성의 “업적 달성”으로 연결해요. 미리보기에서는 달성하는 순간 칩으로 알려줘요.\n(한 업적을 여러 선택지에서 달성할 수는 있지만, 한 결과에서는 업적 하나만 달성해요)' }, [
      { text: '업적을 받고 허브로 (업적 “상태 관리자”)', to: 0, gain: '도장3', achv: 'stat' },
    ]],

    /* ───── 4. 확률 분기 ───── */
    [40, { title: '4단원 · 확률 분기', group: 'l4', body:
      '🎲 4단원 · 확률 분기\n\n같은 선택지를 눌러도 결과가 달라지는 “분기”예요. 선택지 안의 “＋ 확률 분기”로 추가해요.\n• 분기마다 확률(%)을 적고, 나머지 확률은 “기본 설정”이 자동으로 가져가요. (합은 100%를 넘으면 안 돼요)\n• 분기마다 다른 페이지로 보내거나 다른 속성을 줄 수 있어요.\n\n아래 선택지는 50% 대성공 / 25% 작은 불운 / 나머지 무난한 결과예요.' }, [
      { text: '주사위를 굴린다 (50% / 25% / 나머지 25%)', to: 43, prob: [
        { p: 50, to: 41, gain: '행운 부적', achv: 'luck' },
        { p: 25, to: 42, stats: { mental: -2 } },
      ] },
      { text: '허브로 돌아가기', to: 0 },
    ]],
    [41, { title: '대성공', group: 'l4', body: '✨ 대성공! (50%)\n\n이 분기는 “행운 부적”을 얻고 업적 “행운아”를 달성해요. 분기마다 속성을 따로 줄 수 있다는 걸 보여 주는 예시예요.' }, [
      { text: '한 번 더 굴린다', to: 40 },
      { text: '정리하고 넘어간다', to: 44 },
    ]],
    [42, { title: '작은 불운', group: 'l4', body: '😵 작은 불운… (25%)\n\n이 분기는 멘탈이 2 줄어요. 상태줄을 확인하세요.' }, [
      { text: '한 번 더 굴린다', to: 40 },
      { text: '정리하고 넘어간다', to: 44 },
    ]],
    [43, { title: '무난한 결과', group: 'l4', body: '🙂 무난한 결과예요. (나머지 25%, “기본 설정”)\n\n분기 어디에도 걸리지 않으면 기본 설정대로 움직여요.' }, [
      { text: '한 번 더 굴린다', to: 40 },
      { text: '정리하고 넘어간다', to: 44 },
    ]],
    [44, { title: '4단원 마무리', group: 'l4', body: '🎉 4단원 완료!\n\n💡 팁: 선택지의 글자를 다르게 보여 주고 싶다면 분기 말고 “노출 조건”을 쓰세요. 분기는 “똑같이 보이는 선택지의 예측 못 한 결과”를 위한 기능이에요.' }, [
      { text: '도장을 받고 허브로', to: 0, gain: '도장4' },
    ]],

    /* ───── 5. 아이템 분기 ───── */
    [50, { title: '5단원 · 아이템 분기', group: 'l5', body:
      '🧰 5단원 · 아이템 분기\n\n“＋ 아이템 분기”는 가진 아이템에 따라 결과가 갈려요.\n• 위에서부터 차례로 검사해서 처음 맞는 분기가 적용돼요. (▲▼로 순서를 바꿔요)\n• 어느 분기도 안 맞으면 “기본 설정”으로 가요.\n• 쉼표로 쓰면 “모두 있어야” 해요.\n\n먼저 도구 상자에서 아이템을 모아 보세요. 모으는 조합에 따라 금고 결과가 달라져요.' }, [
      { text: '망치를 집는다', to: 50, gain: '망치' },
      { text: '녹슨 열쇠를 집는다', to: 50, gain: '녹슨 열쇠' },
      { text: '금고 앞으로 간다', to: 51 },
    ]],
    [51, { title: '금고', group: 'l5', body:
      '🔐 금고 앞이에요.\n\n“금고를 연다” 선택지의 분기 순서는 이래요.\n① 망치 + 녹슨 열쇠 → P.52\n② 녹슨 열쇠 → P.53\n③ 망치 → P.54\n그 외(기본 설정) → P.55\n\n💡 순서가 중요해요! ②를 ①보다 위에 두면 둘 다 가진 사람도 ②에서 걸려 버려요. 직접 ▲▼로 바꿔서 확인해 보세요.' }, [
      { text: '금고를 연다 (아이템 분기)', to: 55, items: [
        { when: '망치, 녹슨 열쇠', to: 52 },
        { when: '녹슨 열쇠', to: 53 },
        { when: '망치', to: 54 },
      ] },
      { text: '도구 상자로 돌아간다', to: 50 },
    ]],
    [52, { title: '완벽하게 열기', group: 'l5', body: '🔓 열쇠와 망치를 모두 써서 완벽하게 열었어요. (분기 ①)' }, [
      { text: '5단원 마무리로', to: 56 },
    ]],
    [53, { title: '열쇠로 열기', group: 'l5', body: '🗝 열쇠만으로 금고를 열었어요. (분기 ②)' }, [
      { text: '5단원 마무리로', to: 56 },
    ]],
    [54, { title: '부수기', group: 'l5', body: '🔨 망치로 금고를 부쉈어요. (분기 ③)' }, [
      { text: '5단원 마무리로', to: 56 },
    ]],
    [55, { title: '맨손', group: 'l5', body: '🤲 맨손으로는 열 수 없어요. (기본 설정)\n\n도구 상자로 돌아가서 아이템을 더 모아 보세요.' }, [
      { text: '도구 상자로 돌아간다', to: 50 },
    ]],
    [56, { title: '5단원 마무리', group: 'l5', body: '🎉 5단원 완료!' }, [
      { text: '도장을 받고 허브로', to: 0, gain: '도장5' },
    ]],

    /* ───── 6. 폴더와 스토리 묶음 ───── */
    [60, { title: '6단원 · 폴더와 묶음', group: 'l6', body:
      '📁 6단원 · 폴더와 스토리 묶음\n\n• 폴더: 메인 / 서브 / 랜덤 이벤트. 왼쪽 목록 위의 “번호순 / 폴더” 전환으로 볼 수 있어요. 페이지 번호는 폴더와 상관없이 하나로 이어져요.\n• 스토리 묶음: 폴더 안에서 제목을 붙여 페이지를 모아요. (폴더 보기의 “＋ 묶음”)\n• 이동 방식: 선택지 이동을 “페이지 번호” 대신 “스토리 묶음”으로 고르면 그 묶음의 시작 페이지로 가요. 시작 페이지는 묶음의 ✎에서 바꿔요.\n\n아래 선택지는 서브 이벤트 폴더의 “비밀의 방” 묶음으로 이동해요. (허브의 4~7단원 선택지도 묶음 이동이에요)' }, [
      { text: '서브 이벤트 “비밀의 방”으로 이동 (묶음 이동)', group: 'sub' },
      { text: '건너뛰고 허브로', to: 0 },
    ]],
    [200, { title: '비밀의 방', folder: 'sub', group: 'sub', body:
      '🚪 서브 이벤트 폴더의 “비밀의 방” 묶음이에요.\n\n이 묶음의 시작 페이지는 P.200으로 정해 두었어요. 폴더가 달라도 선택지로 자유롭게 오갈 수 있어요.' }, [
      { text: '안쪽으로 들어간다', to: 201 },
    ]],
    [201, { title: '방 안쪽', folder: 'sub', group: 'sub', body: '🕯 묶음 안의 두 번째 페이지예요.\n\n다음 선택지는 번호로 메인 폴더의 P.61로 돌아가요. 이동 방식은 선택지마다 번호 / 묶음 / 랜덤 이벤트 중 자유롭게 섞어 쓸 수 있어요.' }, [
      { text: '튜토리얼로 돌아간다', to: 61 },
    ]],
    [61, { title: '6단원 마무리', group: 'l6', body: '🎉 6단원 완료! 서브 폴더에서 메인 폴더로 돌아왔어요.' }, [
      { text: '도장을 받고 허브로', to: 0, gain: '도장6' },
    ]],

    /* ───── 7. 랜덤 이벤트 ───── */
    [70, { title: '7단원 · 랜덤 이벤트', group: 'l7', body:
      '🎲 7단원 · 랜덤 이벤트\n\n선택지의 이동 방식을 “🎲 랜덤 이벤트”로 하면 랜덤 이벤트 폴더의 묶음 중에서 무작위로 뽑아 이야기가 시작돼요.\n• 후보: 체크한 묶음, 체크가 없으면 랜덤 폴더 전체(지금은 4개)\n• 뽑을 개수: 1~3처럼 범위로 정하면 매번 개수가 무작위. 후보에서 겹치지 않게 뽑아요.\n• 모두 끝나면: 이어지는 곳(페이지 / 묶음)을 정해요.\n• 각 이야기는 “마지막 페이지”로 끝내거나 “⏭ 다음 이야기로” 선택지로 다음 차례로 넘겨요. 두 방식을 모두 써 놨어요.\n\n미리보기 상태줄의 🎲 1/3 같은 표시로 진행도를 볼 수 있어요.' }, [
      { text: '🎲 랜덤 이벤트 1개 (이어지는 곳: 페이지 P.72)', to: 72, random: { min: 1, max: 1, after: { page: 72 } } },
      { text: '🎲 랜덤 이벤트 1~3개 (이어지는 곳: 묶음 “7-2”)', to: 72, random: { min: 1, max: 3, after: { group: 'l7end' } } },
      { text: '허브로 돌아가기', to: 0 },
    ]],
    [72, { title: '이어지는 곳', group: 'l7end', body:
      '🏁 랜덤 이벤트가 모두 끝났어요.\n\n뽑힌 이야기를 다 본 뒤에는 이렇게 “이어지는 곳”으로 와요. 첫 번째 선택지는 “페이지 P.72”로, 두 번째는 “스토리 묶음 7-2”(시작 페이지 P.72)로 이어지게 설정했어요.\n\n이야기가 몇 개 나왔는지는 매번 달라요. 두 번째 선택지를 여러 번 눌러서 1개, 2개, 3개가 모두 나오는지 확인해 보세요.' }, [
      { text: '다음', to: 73 },
    ]],
    [73, { title: '7단원 마무리', group: 'l7end', body: '🎉 7단원 완료!\n\n💡 후보를 “체크한 묶음”만으로 제한하면 그 묶음들 중에서만 뽑아요. 후보보다 많은 개수를 적으면 후보 수까지만 나오고, 검사 기능이 알려 줘요.' }, [
      { text: '도장을 받고 허브로', to: 0, gain: '도장7' },
    ]],
    // 랜덤 이야기들 (랜덤 이벤트 폴더)
    [100, { title: '길고양이', folder: 'random', group: 'cat', body:
      '🐈 [랜덤 이야기 1/4 · 길고양이]\n\n골목 구석에서 길고양이가 울고 있어요. 쓰다듬자 가르릉거리며 멘탈이 편안해졌어요.\n\n💡 이 이야기는 “⏭ 다음 이야기로” 선택지로 끝나요. (효과도 함께 적용돼요: 멘탈 +1)' }, [
      { text: '고양이와 인사하고 다음 이야기로', cont: true, stats: { mental: 1 } },
    ]],
    [110, { title: '낯선 상인', folder: 'random', group: 'shop', end: true, body:
      '🧳 [랜덤 이야기 2/4 · 낯선 상인]\n\n어둠 속에서 낯선 상인이 손짓해요. “이 지도, 싸게 줄게.”\n\n💡 이 이야기는 “마지막 페이지”로 끝나요. 랜덤 이벤트 진행 중에는 “다시 시작 / 종료” 대신 “다음 이야기로 ▶”(마지막에는 “이어서 ▶”) 버튼이 나와요.' }, []],
    [120, { title: '폐건물', folder: 'random', group: 'ruin', body:
      '🏚 [랜덤 이야기 3/4 · 폐건물]\n\n무너진 건물 안에서 낡은 손전등을 주웠어요. (아이템 획득)\n\n💡 “다음 이야기로” 선택지에도 아이템·상태 효과를 줄 수 있어요.' }, [
      { text: '손전등을 챙기고 다음 이야기로', cont: true, gain: '낡은 손전등' },
    ]],
    [130, { title: '시장 (1/2)', folder: 'random', group: 'market', body:
      '🏪 [랜덤 이야기 4/4 · 시장]\n\n북적이는 시장이에요. 이 이야기는 페이지가 두 장이에요. (여러 페이지짜리 이야기도 묶음으로 만들 수 있어요)' }, [
      { text: '안쪽으로 들어간다', to: 131 },
    ]],
    [131, { title: '시장 (2/2)', folder: 'random', group: 'market', end: true, body: '🍎 시장 구경을 마쳤어요.\n\n이 페이지도 “마지막 페이지”라서 랜덤 이벤트 진행 중에는 다음 이야기로 넘어가요.' }, []],

    /* ───── 8. 수료 ───── */
    [90, { title: '수료', group: 'end', end: true, body:
      '🎓 튜토리얼을 모두 마쳤어요! (업적 “튜토리얼 수료”)\n\n더 알아 두면 좋은 기능\n• 🔀 분기도: 이야기 구조를 노드로 보고 ✎로 바로 고치기, 휠·핀치로 확대/축소\n• { } JSON: 페이지를 배열로 한 번에 보기·추가·수정\n• 검사: 비어 있는 본문, 끊긴 연결, 확률 초과 등을 한 번에 찾기\n• 내보내기/가져오기: 홈에서 JSON 파일로 백업\n\n새 기능이 생기면 이 튜토리얼에 단원이 추가돼요. “다시 시작”을 눌러 처음부터 다시 볼 수 있어요.' }, []],
  ];

  /* ---------- 프로젝트 만들기 ---------- */
  function build() {
    const pj = E.newProject('📘 튜토리얼 · 기능 체험', '고정', 1);
    pj.id = ID;
    pj.tutorialVersion = VERSION;
    const gid = {}; const aid = {};
    pj.groups = GROUPS.map((g) => { gid[g.key] = 'g_' + g.key; return { id: 'g_' + g.key, name: g.name, folder: g.folder, start: g.start == null ? null : g.start }; });
    pj.achievements = ACHVS.map((a) => { aid[a.key] = 'a_' + a.key; return { id: 'a_' + a.key, name: a.name }; });
    const ids = { group: (k) => { if (!gid[k]) throw new Error('튜토리얼: 알 수 없는 묶음 ' + k); return gid[k]; }, achv: (k) => { if (!aid[k]) throw new Error('튜토리얼: 알 수 없는 업적 ' + k); return aid[k]; } };
    pj.pages = PAGES.map(([n, o, cs]) => {
      const p = E.newPage(n, o.folder || 'main', o.group ? ids.group(o.group) : '');
      p.title = o.title || ''; p.body = o.body; p.isEnd = !!o.end;
      p.choices = (cs || []).map((c) => choice(c, ids));
      return p;
    });
    return pj;
  }

  /* ---------- 기능 목록: 새 기능을 만들면 여기에 감지 함수를 추가한다 (테스트가 튜토리얼에 빠진 기능을 알려 준다) ---------- */
  const allChoices = (pj) => pj.pages.reduce((a, p) => a.concat(p.choices.map((c) => ({ p, c }))), []);
  const allOutcomes = (pj) => allChoices(pj).reduce((a, x) => a.concat([x.c.base], x.c.branches), []);
  const FEATURES = {
    '페이지 제목': (pj) => pj.pages.some((p) => p.title),
    '마지막 페이지': (pj) => pj.pages.some((p) => p.isEnd),
    '여러 선택지가 한 페이지로 합류': (pj) => pj.pages.some((p) => new Set(p.choices.map((c) => c.base.next)).size < p.choices.length),
    '폴더 3종 사용': (pj) => ['main', 'sub', 'random'].every((f) => pj.pages.some((p) => E.folderOf(p) === f)),
    '스토리 묶음': (pj) => pj.groups.length >= 5,
    '묶음 시작 페이지 지정': (pj) => pj.groups.some((g) => g.start != null),
    '여러 페이지짜리 랜덤 묶음': (pj) => pj.groups.some((g) => g.folder === 'random' && E.pagesOfGroup(pj, g.id).length >= 2),
    '노출 조건': (pj) => allChoices(pj).some((x) => x.c.show),
    '노출 조건 부정(!)': (pj) => allChoices(pj).some((x) => x.c.show && x.c.show.includes('!')),
    '활성 조건': (pj) => allChoices(pj).some((x) => x.c.enable),
    '수량 조건(*)': (pj) => allChoices(pj).some((x) => x.c.enable && x.c.enable.includes('*')),
    '상태값 비교 조건': (pj) => allChoices(pj).some((x) => x.c.enable && />=|<=|>|</.test(x.c.enable)),
    '얻는 아이템': (pj) => allOutcomes(pj).some((o) => o.gain),
    '잃는 아이템': (pj) => allOutcomes(pj).some((o) => o.lose),
    '아이템 수량(*)': (pj) => allOutcomes(pj).some((o) => /\*\d/.test(o.gain + o.lose)),
    '히든 아이템(@)': (pj) => allOutcomes(pj).some((o) => o.gain.includes('@')),
    '상태값 변화': (pj) => allOutcomes(pj).some((o) => Object.keys(o.stats || {}).some((k) => o.stats[k])),
    '업적': (pj) => allOutcomes(pj).some((o) => o.achv),
    '확률 분기': (pj) => allChoices(pj).some((x) => x.c.branchType === 'prob'),
    '아이템 분기': (pj) => allChoices(pj).some((x) => x.c.branchType === 'item'),
    '묶음으로 이동': (pj) => allOutcomes(pj).some((o) => o.mode === 'group'),
    '랜덤 이벤트': (pj) => allOutcomes(pj).some((o) => o.mode === 'random'),
    '뽑을 개수 고정': (pj) => allOutcomes(pj).some((o) => o.mode === 'random' && o.pick.min === o.pick.max),
    '뽑을 개수 범위(1~3)': (pj) => allOutcomes(pj).some((o) => o.mode === 'random' && o.pick.min < o.pick.max),
    '이어지는 곳: 페이지': (pj) => allOutcomes(pj).some((o) => o.mode === 'random' && o.after.mode === 'page'),
    '이어지는 곳: 묶음': (pj) => allOutcomes(pj).some((o) => o.mode === 'random' && o.after.mode === 'group'),
    '다음 이야기로(선택지)': (pj) => allOutcomes(pj).some((o) => o.mode === 'continue'),
    '다음 이야기로(마지막 페이지)': (pj) => pj.pages.some((p) => E.folderOf(p) === 'random' && p.isEnd),
  };
  function features(pj) {
    const out = {};
    Object.keys(FEATURES).forEach((k) => { out[k] = !!FEATURES[k](pj); });
    return out;
  }

  return { ID, VERSION, build, features, FEATURES, PAGES, GROUPS };
});
