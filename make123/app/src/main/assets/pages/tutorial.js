function renderTutorial(container) {
  const sections = [
    {
      id: 'start', icon: '🚀', title: '시작하기 전에 — 준비 과정',
      open: true,
      body: `
        <p style="line-height:1.8;margin-bottom:12px">
          이 앱은 <strong>텍스트 기반 TRPG 시나리오</strong>를 씬(장면) 단위로 작성하고,
          플레이어가 직접 읽으며 선택지를 고르는 게임을 만드는 도구입니다.
        </p>
        <div class="tut-box info">
          <div class="tut-box-title">📋 작성 흐름</div>
          <ol style="line-height:2;padding-left:16px">
            <li><strong>캐릭터 시트 설정</strong> — 스텟·HP·SAN 초기값 지정</li>
            <li><strong>시나리오 생성</strong> — 홈에서 새 시나리오 만들기</li>
            <li><strong>씬 작성</strong> — 번호별 장면 내용과 선택지 작성</li>
            <li><strong>트리거 설정</strong> — HP 0 → 사망씬, SAN 0 → 발광씬 연결</li>
            <li><strong>플레이</strong> — 리더 화면에서 씬을 선택하며 진행</li>
          </ol>
        </div>
        <div class="tut-box warn">
          <div class="tut-box-title">⚠️ 시작 전 필수 준비</div>
          <ul style="line-height:2;padding-left:16px">
            <li>캐릭터 시트에서 <strong>능력치 초기값</strong> 입력</li>
            <li><strong>HP / SAN / MP</strong> 최대값 설정 (계산식은 아래 참고)</li>
            <li>시나리오에 <strong>사망 씬, 발광 씬</strong> 번호 결정 후 트리거 연결</li>
            <li>각 씬마다 <strong>선택지 이동 번호</strong>가 실제로 존재하는지 확인</li>
          </ul>
        </div>
      `
    },
    {
      id: 'stats', icon: '📊', title: '스텟 가이드 — 능력치 역할',
      body: `
        <p style="margin-bottom:12px;color:var(--text3);font-size:13px">CoC 6판 기준. 7판은 각 수치에 ×5 적용.</p>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px">
          ${[
            ['STR 근력', '#ef4444', '문 부수기·물건 들기·근접 전투·물리적 저항. 피해 보너스(DB) 계산에 사용.'],
            ['DEX 민첩', '#f97316', '회피·도주·정밀 동작. 전투 행동 순서 결정. 신체 기술 스킬 기반.'],
            ['CON 체력', '#f59e0b', 'HP 최대값 기준 (CON/2). 독·병·극한 환경 저항. 생존력의 핵심.'],
            ['INT 지능', '#a3e635', '단서 발견·암호 해독·아이디어 판정 (INT×5). 오컬트·학술 스킬 기반.'],
            ['WIS 지혜', '#10b981', '직감·위험 감지. 일부 시스템에서 심리 저항에 활용.'],
            ['CHA 매력', '#22d3ee', 'NPC 설득·협박·교섭. 소셜 스킬 전반. 동료 결속에 영향.'],
            ['POW 의지', '#a78bfa', 'SAN 저항(POW×5 = 초기 SAN). MP 최대값 = POW. 마법 저항.'],
            ['EDU 교육', '#fbbf24', '보유 스킬 포인트 = EDU×20. 학술·전문 지식. 자료 조사 스킬 기반.'],
          ].map(([name, color, desc]) => `
            <div style="background:var(--card);border:1px solid var(--border);border-radius:8px;padding:10px;border-left:4px solid ${color}">
              <div style="font-weight:bold;color:${color};margin-bottom:4px">${name}</div>
              <div style="font-size:12px;color:var(--text2);line-height:1.6">${desc}</div>
            </div>
          `).join('')}
        </div>
        <div class="tut-box info" style="margin-top:12px">
          <div class="tut-box-title">💡 3d6 주사위 생성 (CoC 6판 기준)</div>
          <p style="font-size:13px;line-height:1.8">각 능력치를 3개의 6면체 주사위(3~18)로 생성. CON·STR·SIZ·POW·DEX·APP·INT = 3d6. EDU = 3d6+3.</p>
        </div>
      `
    },
    {
      id: 'bars', icon: '❤️', title: '바 스텟 — HP / SAN / MP 계산',
      body: `
        <div style="display:flex;flex-direction:column;gap:10px">
          ${[
            ['HP 체력', '#ef4444', 'CON / 2 (소수점 올림)', 'HP가 0이 되면 사망. 트리거로 사망씬에 연결해야 함. 독스프 기준: HP 최소 4~6 권장.'],
            ['이성 SAN', '#a3e635', 'POW × 5 (최대 99)', '0이 되면 영구 발광. 트리거로 발광씬에 연결. SAN 체크 때마다 주사위로 손실.'],
            ['MP 마력', '#60a5fa', 'POW (6판 기준)', '주문·마법 사용 시 소비. 시나리오에 마법이 없으면 생략 가능.'],
          ].map(([name, color, formula, desc]) => `
            <div style="background:var(--card);border:1px solid var(--border);border-radius:8px;padding:12px;border-left:4px solid ${color}">
              <div style="display:flex;align-items:center;gap:10px;margin-bottom:6px">
                <span style="font-weight:bold;color:${color};font-size:15px">${name}</span>
                <span style="background:var(--bg3);border-radius:5px;padding:2px 10px;font-size:12px;color:var(--accent3);font-family:monospace">${formula}</span>
              </div>
              <div style="font-size:13px;color:var(--text2);line-height:1.6">${desc}</div>
            </div>
          `).join('')}
        </div>
        <div class="tut-box warn" style="margin-top:12px">
          <div class="tut-box-title">📐 독스프 권장 초기값 예시</div>
          <div style="font-family:monospace;font-size:13px;line-height:2">
            CON 12 → HP 최대 6 / POW 10 → SAN 최대 50 / MP 최대 10
          </div>
        </div>
      `
    },
    {
      id: 'trigger', icon: '⚡', title: '트리거 시스템 — 자동 씬 이동',
      body: `
        <p style="line-height:1.8;margin-bottom:12px">
          트리거는 <strong>스텟이 특정 수치에 도달하면 자동으로 지정 씬으로 이동</strong>하는 기능입니다.
          리더 화면에서 씬을 이동할 때마다 캐릭터 시트의 현재값을 검사합니다.
        </p>
        <div class="tut-box info">
          <div class="tut-box-title">⚙️ 설정 방법</div>
          <ol style="line-height:2;padding-left:16px">
            <li>시나리오 편집기에서 <strong>트리거 설정</strong> 섹션 펼치기</li>
            <li>스텟 키 입력 (<code>hp</code> / <code>san</code> / <code>mp</code> / <code>luck</code>)</li>
            <li>조건 선택 (<code>≤</code> 이하 / <code>≥</code> 이상 / <code>=</code> 같음)</li>
            <li>수치와 이동할 씬 번호 입력</li>
          </ol>
        </div>
        <div style="display:flex;flex-direction:column;gap:8px;margin-top:12px">
          ${[
            ['hp', '≤', '0', '사망씬 번호', '기본 사망 조건. 캐릭터 HP가 0 이하가 되면 발동.'],
            ['san', '≤', '0', '발광씬 번호', '완전 이성 붕괴. SAN이 0 이하면 자동 이동.'],
            ['san', '≤', '10', '공포반응씬', '임계 SAN에서 특수 이벤트 씬으로 분기.'],
            ['luck', '≤', '0', '불행씬 번호', '커스텀 스텟 사용 예시.'],
          ].map(([key, op, val, dest, desc]) => `
            <div style="background:var(--card);border:1px solid var(--border);border-radius:8px;padding:10px">
              <div style="font-family:monospace;font-size:13px;color:var(--accent3);margin-bottom:4px">
                <strong>${key}</strong> ${op} ${val} → 씬 <strong>[${dest}]</strong>
              </div>
              <div style="font-size:12px;color:var(--text3)">${desc}</div>
            </div>
          `).join('')}
        </div>
        <div class="tut-box danger" style="margin-top:12px">
          <div class="tut-box-title">🔴 주의사항</div>
          <ul style="line-height:2;padding-left:16px;font-size:13px">
            <li>트리거는 씬 이동 시 <strong>캐릭터 시트의 현재값</strong>을 읽습니다</li>
            <li>리더에서 스텟을 직접 수정하지 않으므로, 씬 내용에 따라 <strong>플레이어가 캐릭터 탭에서 직접 값을 조정</strong>해야 합니다</li>
            <li>트리거 목적지 씬은 <strong>다시 트리거를 발동시키지 않아야</strong> 합니다 (무한 루프 방지)</li>
          </ul>
        </div>
      `
    },
    {
      id: 'character', icon: '👤', title: '캐릭터 개성 — 최소 설정 가이드',
      body: `
        <p style="line-height:1.8;margin-bottom:10px">스토리에 몰입하려면 최소한의 개성 설정이 필요합니다.</p>
        <div class="tut-box info">
          <div class="tut-box-title">✅ 어떤 시나리오에서도 필수</div>
          <ul style="line-height:2;padding-left:16px">
            <li><strong>이름 / 나이 / 직업</strong> — 기본 정체성</li>
            <li><strong>HP / SAN 초기값</strong> — 생존 기준점</li>
            <li><strong>고유 스킬 1가지</strong> — 캐릭터만의 특기 (탐정 직감, 의학 지식 등)</li>
          </ul>
        </div>
        <div style="display:flex;flex-direction:column;gap:8px;margin-top:10px">
          <div style="font-size:13px;font-weight:bold;color:var(--text2);margin-bottom:2px">장르별 추가 권장 설정</div>
          ${[
            ['🔍 공포·CoC', '크툴루 신화 인지 여부, 과거의 트라우마 또는 상실 경험, 현실 도피 이유 (왜 여기 있는가?)'],
            ['⚔️ 판타지', '종족·계급·신앙, 동료와의 관계, 전투 스타일 또는 전문 기술'],
            ['💕 로맨스', '주요 감정선 (무엇을 원하는가?), 연인 후보와의 첫인상, 숨기고 싶은 비밀'],
            ['🕵️ 미스터리', '수사 동기, 핵심 단서에 대한 사전 지식 수준, 용의자와의 관계'],
          ].map(([genre, items]) => `
            <div style="background:var(--card);border:1px solid var(--border);border-radius:8px;padding:10px">
              <div style="font-weight:bold;color:var(--accent3);margin-bottom:4px">${genre}</div>
              <div style="font-size:12px;color:var(--text2);line-height:1.7">${items}</div>
            </div>
          `).join('')}
        </div>
        <div class="tut-box warn" style="margin-top:12px">
          <div class="tut-box-title">💡 독스프 권장 캐릭터 설정</div>
          <ul style="line-height:2;padding-left:16px;font-size:13px">
            <li>직업: 의사·연구자·탐정 계열 유리 (탐색·의학 스킬 활용)</li>
            <li>배경: 왜 이 악몽에 갇혔는가? (꿈에 자주 시달리는가? 심리적 약점?)</li>
            <li>특기: 약학 or 의학 — 독 스프의 성분 분석에 도움</li>
            <li>SAN 시작값: 40~60 사이 권장 (너무 낮으면 금방 발광)</li>
          </ul>
        </div>
      `
    },
    {
      id: 'scene', icon: '📝', title: '씬 작성법 — 구조와 팁',
      body: `
        <div style="display:flex;flex-direction:column;gap:10px">
          ${[
            ['씬 색상', '정보(파랑)·주의(노랑)·위험(빨강)·특수(연두)로 분위기 구분. 색상만 봐도 긴장감 전달.'],
            ['씬 번호 배치', '중심 방 1~9, 각 구역 10번 단위로 구분하면 관리하기 쉽습니다. 랜덤 씬 추가로 미사용 번호 활용.'],
            ['선택지 설계', '최소 2개 이상. 판정 결과에 따른 분기(성공/실패 별도 씬)를 권장. 조건 플래그로 특정 씬 잠금 가능.'],
            ['flagsOnArrive', '씬에 도달하면 자동 설정되는 플래그. 아이템 획득·상황 변화를 추적할 때 사용.'],
            ['차이 나는 선택지', '"조사한다"와 "그냥 지나친다"처럼 결과가 달라야 의미 있습니다. 같은 곳으로 가면 무의미.'],
            ['막힌 씬 처리', '선택지가 없는 씬(엔딩)에는 안내 문구("— 씬 번호를 직접 입력하세요 —")가 자동 표시됩니다.'],
          ].map(([title, desc]) => `
            <div style="background:var(--card);border:1px solid var(--border);border-radius:8px;padding:10px">
              <div style="font-weight:bold;color:var(--accent3);margin-bottom:4px">▸ ${title}</div>
              <div style="font-size:13px;color:var(--text2);line-height:1.6">${desc}</div>
            </div>
          `).join('')}
        </div>
        <div class="tut-box info" style="margin-top:12px">
          <div class="tut-box-title">📐 권장 씬 구성 템플릿</div>
          <pre style="font-size:12px;line-height:1.8;white-space:pre-wrap">[장소명 — 상황 키워드]

상황 묘사. 감각 정보(냄새·소리·빛)를 포함하면 몰입도↑

📍 발견 가능한 것:
  • 아이템 A: 간단한 설명
  • 이상한 점: 단서

🎲 [스킬명 판정]
  성공: 결과 A
  실패: 결과 B</pre>
        </div>
      `
    },
    {
      id: 'doksupu', icon: '🔍', title: '독스프 예시 — 이 시스템으로 만든 시나리오',
      body: `
        <p style="line-height:1.8;margin-bottom:12px">
          내장 시나리오 <strong>독스프</strong>는 CoC 크툴루 TRPG 밀실 탈출 시나리오입니다.
          <strong>씬 1이 모든 이야기의 시작</strong>으로, 처음 열면 항상 씬 1부터 시작하세요.
        </p>
        <div style="display:flex;flex-direction:column;gap:8px">
          ${[
            ['씬 구성', '총 약 70개 씬. 중앙(1~9) → 동서남북 각 구역(10번 단위) → 독 제조(51~56) → 엔딩(59~68)+배드(62~66)+발광(67)'],
            ['트리거', 'HP ≤ 0 → 씬 65 (즉사), SAN ≤ 0 → 씬 67 (발광 엔딩) 자동 연결'],
            ['플래그 활용', 'knows_soup, has_poison, soup_ready 등 핵심 아이템·지식을 플래그로 추적. 씬 도달 시 자동 설정.'],
            ['분기 엔딩', '베스트(소녀 생존)·노멀(탈출)·배드(시간초과)·즉사·발광 5종류 엔딩'],
            ['스텟 사용', 'CON 판정(독 저항), 탐색/관찰(단서 발견), 이성 체크(SAN 손실) 등 CoC 스텟 전반 활용'],
          ].map(([title, desc]) => `
            <div style="background:var(--card);border:1px solid var(--border);border-radius:8px;padding:10px;border-left:3px solid var(--accent3)">
              <div style="font-weight:bold;color:var(--accent3);margin-bottom:4px">▸ ${title}</div>
              <div style="font-size:13px;color:var(--text2);line-height:1.6">${desc}</div>
            </div>
          `).join('')}
        </div>
        <div style="margin-top:14px;text-align:center">
          <button class="btn btn-primary" onclick="Router.navigate('/reader/__doksupu__')">🔍 독스프 바로 플레이</button>
        </div>
      `
    }
  ];

  container.innerHTML = `
    <div style="display:flex;align-items:center;gap:8px;margin-bottom:16px">
      <button class="btn btn-secondary btn-sm" onclick="Router.navigate('/')">← 홈</button>
      <h2 style="flex:1;color:var(--accent3)">📖 시나리오 작성 튜토리얼</h2>
    </div>

    <style>
      .tut-section { border:1px solid var(--border); border-radius:10px; margin-bottom:10px; overflow:hidden; }
      .tut-header { display:flex; align-items:center; gap:10px; padding:12px 16px; background:var(--card); cursor:pointer; user-select:none; }
      .tut-header:hover { background:var(--bg3); }
      .tut-icon { font-size:18px; }
      .tut-title { font-weight:bold; color:var(--text2); flex:1; }
      .tut-arrow { color:var(--text3); transition:transform 0.2s; }
      .tut-body { padding:16px; background:var(--bg2); border-top:1px solid var(--border); }
      .tut-box { border-radius:8px; padding:12px 14px; margin-bottom:8px; }
      .tut-box.info { background:rgba(59,130,246,0.08); border:1px solid rgba(59,130,246,0.25); }
      .tut-box.warn { background:rgba(245,158,11,0.08); border:1px solid rgba(245,158,11,0.25); }
      .tut-box.danger { background:rgba(239,68,68,0.08); border:1px solid rgba(239,68,68,0.25); }
      .tut-box-title { font-weight:bold; margin-bottom:6px; font-size:13px; }
      .tut-box.info .tut-box-title { color:#60a5fa; }
      .tut-box.warn .tut-box-title { color:#f59e0b; }
      .tut-box.danger .tut-box-title { color:#ef4444; }
      code { background:var(--bg3); border-radius:3px; padding:1px 5px; font-size:12px; color:var(--accent3); }
      pre { background:var(--bg3); border-radius:6px; padding:10px; border:1px solid var(--border); }
    </style>

    ${sections.map(sec => `
      <div class="tut-section" id="sec-${sec.id}">
        <div class="tut-header" onclick="toggleTutSection('${sec.id}')">
          <span class="tut-icon">${sec.icon}</span>
          <span class="tut-title">${sec.title}</span>
          <span class="tut-arrow" id="arrow-${sec.id}" style="transform:rotate(${sec.open?'180':'0'}deg)">▼</span>
        </div>
        <div class="tut-body" id="body-${sec.id}" style="display:${sec.open?'block':'none'}">
          ${sec.body}
        </div>
      </div>
    `).join('')}

    <div style="text-align:center;margin-top:16px;padding:12px;background:var(--card);border-radius:10px;border:1px solid var(--border)">
      <p style="color:var(--text3);font-size:13px;margin-bottom:10px">독스프로 직접 체험해보세요</p>
      <button class="btn btn-primary" onclick="Router.navigate('/reader/__doksupu__')">🔍 독스프 플레이하기</button>
    </div>
  `;

  window.toggleTutSection = (id) => {
    const body = container.querySelector(`#body-${id}`);
    const arrow = container.querySelector(`#arrow-${id}`);
    const open = body.style.display !== 'none';
    body.style.display = open ? 'none' : 'block';
    arrow.style.transform = open ? 'rotate(0deg)' : 'rotate(180deg)';
  };
}

Router.register('/tutorial', renderTutorial);
