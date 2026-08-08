async function renderHome(container) {
  const scenarios = await DB.getAll('scenarios');
  const progress = await DB.getAll('progress');
  const progMap = Object.fromEntries(progress.map(p => [p.scenarioId, p]));

  container.innerHTML = `
    <div style="text-align:center;padding:24px 0 16px">
      <div style="font-size:48px;margin-bottom:8px">📜</div>
      <h1 style="font-size:22px;color:var(--accent3);margin-bottom:6px">스크립트 TRPG 작성 앱</h1>
      <p style="color:var(--text3);font-size:14px">시나리오를 선택하거나 새로 만드세요</p>
    </div>
    <div id="scenario-list"></div>
    <div style="display:flex;gap:8px;margin-top:12px">
      <button class="btn btn-primary flex-1" id="btn-new">+ 새 시나리오 만들기</button>
      <button class="btn btn-secondary" id="btn-manage">관리</button>
    </div>
    <div class="card mt-16" style="border-color:#3d2b5e;background:linear-gradient(135deg,#1a1a2e 0%,#16162a 100%)">
      <h2 style="color:#a78bfa">💕 독스프 솔로 플레이</h2>
      <p style="font-size:13px;color:var(--text3);line-height:1.6;margin-bottom:10px">
        1인용 인터랙티브 스토리.<br>
        8명의 캐릭터 중 3명과 함께 꿈 속에 갇힙니다.<br>
        연인과의 호감도를 높이면 히든 루트가 열립니다.
      </p>
      <button class="btn btn-primary" onclick="Router.navigate('/solo')" style="width:100%">▶ 솔로 플레이 시작</button>
    </div>
    <div class="card mt-16">
      <div style="font-size:12px;color:var(--text3);text-align:center">v22</div>
    </div>
  `;

  const listEl = container.querySelector('#scenario-list');

  // Built-in scenario
  const builtinCard = createScenarioCard({
    id: '__doksupu__',
    title: '독스프',
    desc: '크툴루 TRPG 밀실 탈출 시나리오 (내장)',
    icon: '🔍',
    builtin: true
  }, progMap['__doksupu__']);
  listEl.appendChild(builtinCard);

  for (const s of scenarios) {
    listEl.appendChild(createScenarioCard(s, progMap[s.id]));
  }

  if (scenarios.length === 0) {
    const hint = document.createElement('p');
    hint.className = 'text-muted text-sm mt-8';
    hint.textContent = '아직 사용자 시나리오가 없습니다. 새로 만들어보세요!';
    listEl.appendChild(hint);
  }

  container.querySelector('#btn-new').onclick = showNewScenarioModal;
  container.querySelector('#btn-manage').onclick = () => Router.navigate('/scenarios');
}

function createScenarioCard(scenario, progress) {
  const card = document.createElement('div');
  card.className = 'scenario-card';
  const prog = progress ? `씬 #${progress.lastScene} 진행 중` : '처음 시작';
  card.innerHTML = `
    <div class="scenario-icon">${scenario.icon || '📜'}</div>
    <div class="scenario-info">
      <div class="scenario-title">${scenario.title}</div>
      <div class="scenario-desc">${scenario.desc || ''}</div>
      <div style="margin-top:6px;display:flex;gap:6px;flex-wrap:wrap">
        ${scenario.builtin ? '<span class="scenario-badge">내장</span>' : ''}
        <span class="scenario-badge" style="background:#1e3a5f;color:#93c5fd">${prog}</span>
      </div>
    </div>
    <div style="display:flex;flex-direction:column;gap:6px">
      <button class="btn btn-primary btn-sm" data-action="play">▶ 플레이</button>
      ${!scenario.builtin ? '<button class="btn btn-secondary btn-sm" data-action="edit">✏ 편집</button>' : ''}
    </div>
  `;
  card.querySelector('[data-action="play"]').onclick = (e) => {
    e.stopPropagation();
    Router.navigate(`/reader/${scenario.id}`);
  };
  const editBtn = card.querySelector('[data-action="edit"]');
  if (editBtn) {
    editBtn.onclick = (e) => {
      e.stopPropagation();
      Router.navigate(`/editor/${scenario.id}`);
    };
  }
  return card;
}

function showNewScenarioModal() {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.innerHTML = `
    <div class="modal">
      <h3>새 시나리오 만들기</h3>
      <div class="field">
        <label>제목</label>
        <input id="new-title" placeholder="시나리오 제목" />
      </div>
      <div class="field">
        <label>설명 (선택)</label>
        <input id="new-desc" placeholder="간단한 설명" />
      </div>
      <div class="field">
        <label>아이콘 이모지</label>
        <input id="new-icon" placeholder="📜" maxlength="4" style="width:80px" />
      </div>
      <div class="modal-actions">
        <button class="btn btn-secondary" id="cancel-new">취소</button>
        <button class="btn btn-primary" id="confirm-new">만들기</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);
  overlay.querySelector('#cancel-new').onclick = () => overlay.remove();
  overlay.querySelector('#confirm-new').onclick = async () => {
    const title = overlay.querySelector('#new-title').value.trim();
    if (!title) { toast('제목을 입력하세요'); return; }
    const id = Utils.genId();
    await DB.put('scenarios', {
      id,
      title,
      desc: overlay.querySelector('#new-desc').value.trim(),
      icon: overlay.querySelector('#new-icon').value.trim() || '📜',
      script: '',
      createdAt: Date.now()
    });
    overlay.remove();
    toast('시나리오가 만들어졌습니다');
    Router.navigate(`/editor/${id}`);
  };
}

Router.register('/', renderHome);
