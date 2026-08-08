async function renderScenarioList(container) {
  const scenarios = await DB.getAll('scenarios');

  function render() {
    container.innerHTML = `
      <div style="display:flex;align-items:center;gap:8px;margin-bottom:16px">
        <button class="btn btn-secondary btn-sm" onclick="Router.navigate('/')">← 홈</button>
        <h2 style="flex:1;color:var(--accent3)">시나리오 관리</h2>
        <input id="import-scenario-file" type="file" accept=".txt,text/plain" style="display:none">
        <button class="btn btn-secondary btn-sm" id="btn-import-s">📥 가져오기</button>
        <button class="btn btn-primary btn-sm" id="btn-new-s">+ 새 시나리오</button>
      </div>

      <div class="card" style="font-size:12px;color:var(--text3);line-height:1.7">
        <strong style="color:var(--accent3)">백업 안내</strong><br>
        Chrome에서 사이트 데이터까지 삭제하면 IndexedDB에 저장된 작성 기록도 삭제될 수 있습니다.
        중요한 시나리오는 텍스트로 내보낸 뒤 보관하고, 필요할 때 이 화면에서 다시 가져오세요.
      </div>

      <div class="card">
        <h2>📦 내장 시나리오</h2>
        <div class="scenario-card" style="cursor:default">
          <div class="scenario-icon">🔍</div>
          <div class="scenario-info">
            <div class="scenario-title">독스프</div>
            <div class="scenario-desc">크툴루 TRPG 밀실 탈출 시나리오</div>
          </div>
          <span class="scenario-badge">내장</span>
        </div>
      </div>

      <div class="card">
        <h2>📝 사용자 시나리오 (${scenarios.length}개)</h2>
        <div id="user-scenario-list"></div>
      </div>
    `;

    const list = container.querySelector('#user-scenario-list');
    if (scenarios.length === 0) {
      list.innerHTML = '<p class="text-muted text-sm">아직 없습니다</p>';
    } else {
      scenarios.forEach(s => {
        const row = document.createElement('div');
        row.className = 'scenario-card';
        row.innerHTML = `
          <div class="scenario-icon">${s.icon||'📜'}</div>
          <div class="scenario-info">
            <div class="scenario-title">${s.title}</div>
            <div class="scenario-desc">${s.desc||'설명 없음'}</div>
          </div>
          <div style="display:flex;flex-direction:column;gap:4px;min-width:86px">
            <button class="btn btn-primary btn-sm" data-id="${s.id}" data-action="edit">✏ 편집</button>
            <button class="btn btn-secondary btn-sm" data-id="${s.id}" data-action="export">↗ 내보내기</button>
            <button class="btn btn-danger btn-sm" data-id="${s.id}" data-action="delete">🗑 삭제</button>
          </div>
        `;
        list.appendChild(row);
      });
    }

    container.querySelector('#btn-new-s').onclick = () => {
      showNewScenarioModal && showNewScenarioModal();
    };

    const importInput = container.querySelector('#import-scenario-file');
    container.querySelector('#btn-import-s').onclick = () => importInput.click();
    importInput.onchange = async () => {
      const file = importInput.files && importInput.files[0];
      importInput.value = '';
      if (!file) return;
      try {
        const text = await file.text();
        const scenario = Utils.createScenarioFromText(text, file.name);
        await DB.put('scenarios', scenario);
        scenarios.push(scenario);
        toast(`"${scenario.title}" 가져오기 완료`);
        render();
      } catch (err) {
        if (err && err.message === 'SCENARIO_PARSE_EMPTY') {
          toast('가져올 씬을 찾지 못했습니다. #1# 형식의 텍스트인지 확인하세요.', 3500);
        } else {
          console.error(err);
          toast('파일을 가져오지 못했습니다', 3000);
        }
      }
    };

    container.querySelectorAll('[data-action="edit"]').forEach(btn => {
      btn.onclick = () => Router.navigate(`/editor/${btn.dataset.id}`);
    });
    container.querySelectorAll('[data-action="export"]').forEach(btn => {
      btn.onclick = async () => {
        const s = scenarios.find(x => x.id === btn.dataset.id);
        if (!s) return;
        Utils.downloadText(`${s.title}.txt`, Utils.exportScenarioText(s));
        toast('내보내기 완료');
      };
    });
    container.querySelectorAll('[data-action="delete"]').forEach(btn => {
      btn.onclick = async () => {
        const s = scenarios.find(x => x.id === btn.dataset.id);
        if (!s || !confirm(`"${s.title}" 시나리오를 삭제할까요?`)) return;
        await DB.delete('scenarios', s.id);
        scenarios.splice(scenarios.indexOf(s), 1);
        toast('삭제됨');
        render();
      };
    });
  }

  render();
}

Router.register('/scenarios', renderScenarioList);
