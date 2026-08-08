// Scene structure: { num, color, text, choices: [{goto, need, needStatKey, needStatOp, needStatValue, needItem, giveItem, consumeItem, text}] }

async function renderEditor(container, params) {
  const id = params[0];
  if (!id) { Router.navigate('/'); return; }

  const scenario = await DB.get('scenarios', id);
  if (!scenario) { toast('시나리오를 찾을 수 없습니다'); Router.navigate('/'); return; }
  const character = await DB.get('characters', 'default');

  // Load scenes: use structured array if exists, else parse from script text
  let scenes = scenario.scenes
    ? scenario.scenes
    : Utils.parseScript(scenario.script || '').map(s => ({ ...s, collapsed: false }));

  scenes = scenes.map(s => ({
    color: 1,
    text: '',
    choices: [],
    collapsed: false,
    ...s,
    choices: Array.isArray(s.choices) ? s.choices.map(ch => ({
      need: null,
      sets: [],
      needStatKey: null,
      needStatOp: '>=',
      needStatValue: null,
      needItem: null,
      giveItem: null,
      consumeItem: false,
      text: '',
      ...ch,
      sets: Array.isArray(ch.sets) ? ch.sets : []
    })) : []
  }));

  if (!scenes.length) scenes = [];

  let searchQuery = '';
  let sortMode = scenario.sortMode || 'created'; // 'created' | 'num'
  let rangeMin = scenario.rangeMin ?? 1;
  let rangeMax = scenario.rangeMax ?? 100;
  let viewRangeMin = scenario.viewRangeMin ?? '';
  let viewRangeMax = scenario.viewRangeMax ?? '';
  let viewMode = scenario.viewMode || 'cards'; // 'cards' | 'slides'
  let triggers = scenario.triggers ? scenario.triggers.map(t => ({ ...t })) : [];
  let items = Array.isArray(scenario.items) ? scenario.items.map(item => ({ ...item })) : [];
  let editorSettings = {
    showTriggers: scenario.editorSettings?.showTriggers ?? triggers.length > 0,
    showItems: scenario.editorSettings?.showItems ?? items.length > 0,
    showChoiceConditions: scenario.editorSettings?.showChoiceConditions ?? scenes.some(scene =>
      (scene.choices || []).some(choiceHasCondition)
    )
  };
  let autoSaveTimer = null;

  function scheduleAutoSave() {
    clearTimeout(autoSaveTimer);
    autoSaveTimer = setTimeout(saveAll, 1500);
  }

  async function saveAll() {
    const script = scenesToScript(scenes);
    await DB.put('scenarios', { ...scenario, scenes, script, sortMode, rangeMin, rangeMax, viewRangeMin, viewRangeMax, viewMode, triggers, items, editorSettings });
  }

  function choiceHasCondition(choice) {
    return Boolean(
      choice?.need ||
      (Array.isArray(choice?.sets) && choice.sets.length) ||
      choice?.needStatKey ||
      choice?.needItem ||
      choice?.giveItem ||
      choice?.consumeItem
    );
  }

  function normalizeItemKey(value) {
    return String(value || '').trim().replace(/\s+/g, '_').replace(/[^\w가-힣-]/g, '').toLowerCase();
  }

  function getItemName(key) {
    const item = items.find(it => it.key === key);
    return item?.name || key || '';
  }

  function splitCsv(value) {
    return String(value || '')
      .split(',')
      .map(item => item.trim())
      .filter(Boolean);
  }

  function itemOptions(selected = '') {
    return ['<option value="">아이템 없음</option>'].concat(items.map(item => {
      return `<option value="${escHtml(item.key)}"${item.key === selected ? ' selected' : ''}>${escHtml(item.name)}</option>`;
    })).join('');
  }

  function statConditionOptions(selected = '') {
    const stats = [
      { key: '', label: '스텟 조건 없음' },
      { key: 'hp', label: 'HP' },
      { key: 'mp', label: 'MP' },
      { key: 'san', label: '이성(SAN)' },
      { key: 'luck', label: '행운' },
      ...((character?.statList || []).map(s => ({ key: s.key, label: s.label }))),
      ...((character?.customBars || []).map(b => ({ key: b.id, label: b.name })))
    ];
    return stats.map(stat => (
      `<option value="${escHtml(stat.key)}"${stat.key === selected ? ' selected' : ''}>${escHtml(stat.label)}</option>`
    )).join('');
  }

  function pickRandomNum() {
    const existing = new Set(scenes.map(s => s.num));
    const available = [];
    for (let i = rangeMin; i <= rangeMax; i++) {
      if (!existing.has(i)) available.push(i);
    }
    if (available.length === 0) return null;
    return available[Math.floor(Math.random() * available.length)];
  }

  container.innerHTML = `
    <div style="display:flex;align-items:center;gap:8px;margin-bottom:12px;flex-wrap:wrap">
      <button class="btn btn-secondary btn-sm" onclick="Router.navigate('/')">← 홈</button>
      <h2 style="flex:1;color:var(--accent3);min-width:80px">${scenario.title}</h2>
      <button class="btn btn-secondary btn-sm" id="btn-graph">🗺 그래프</button>
      <button class="btn btn-secondary btn-sm" id="btn-export">↗ 내보내기</button>
      <button class="btn btn-primary btn-sm" id="btn-save">💾 저장</button>
    </div>

    <div style="display:flex;gap:6px;margin-bottom:8px;align-items:center;flex-wrap:wrap;background:var(--card);border:1px solid var(--border);border-radius:10px;padding:10px 12px">
      <span style="font-size:12px;color:var(--text3);white-space:nowrap">랜덤 범위</span>
      <input id="range-min" type="number" min="1" value="${rangeMin}" style="width:70px;text-align:center">
      <span style="font-size:12px;color:var(--text3)">~</span>
      <input id="range-max" type="number" min="1" value="${rangeMax}" style="width:70px;text-align:center">
      <div style="flex:1"></div>
      <button class="btn btn-primary" id="btn-add-scene">🎲 씬 추가</button>
    </div>

    <div style="background:var(--card);border:1px solid var(--border);border-radius:10px;margin-bottom:8px;padding:10px 14px">
      <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap">
        <span style="font-size:13px;font-weight:bold;color:var(--text2);margin-right:4px">⚙ 설정</span>
        <label style="display:flex;align-items:center;gap:5px;margin:0;font-size:12px;color:var(--text3)">
          <input id="set-show-triggers" type="checkbox" style="width:auto"${editorSettings.showTriggers ? ' checked' : ''}>
          트리거 설정 표시
        </label>
        <label style="display:flex;align-items:center;gap:5px;margin:0;font-size:12px;color:var(--text3)">
          <input id="set-show-items" type="checkbox" style="width:auto"${editorSettings.showItems ? ' checked' : ''}>
          아이템 설정 표시
        </label>
        <label style="display:flex;align-items:center;gap:5px;margin:0;font-size:12px;color:var(--text3)">
          <input id="set-show-choice-conditions" type="checkbox" style="width:auto"${editorSettings.showChoiceConditions ? ' checked' : ''}>
          선택지 조건 표시
        </label>
      </div>
    </div>

    ${editorSettings.showTriggers ? `
    <div style="background:var(--card);border:1px solid var(--border);border-radius:10px;margin-bottom:8px;overflow:hidden">
      <div id="trigger-header" style="display:flex;align-items:center;gap:8px;padding:10px 14px;cursor:pointer;user-select:none" onclick="document.getElementById('trigger-body').style.display=document.getElementById('trigger-body').style.display==='none'?'block':'none';document.getElementById('trigger-arrow').style.transform=document.getElementById('trigger-body').style.display==='none'?'':'rotate(180deg)'">
        <span style="font-size:13px;font-weight:bold;color:var(--text2)">⚡ 트리거 설정</span>
        <span style="font-size:11px;color:var(--text3)">(스텟 조건 → 자동 씬 이동)</span>
        <div style="flex:1"></div>
        <span id="trigger-arrow" style="color:var(--text3);font-size:13px;transition:transform 0.2s">▼</span>
      </div>
      <div id="trigger-body" style="display:none;padding:10px 14px;border-top:1px solid var(--border)">
        <p style="font-size:12px;color:var(--text3);margin-bottom:10px">스텟 키: <code>hp</code> / <code>san</code> / <code>mp</code> / <code>luck</code> / 커스텀 스텟 키</p>
        <div id="trigger-list"></div>
        <div style="display:flex;gap:6px;margin-top:8px;flex-wrap:wrap;align-items:flex-end">
          <div style="display:flex;flex-direction:column;gap:2px">
            <span style="font-size:11px;color:var(--text3)">스텟</span>
            <input id="tr-key" placeholder="hp" style="width:70px">
          </div>
          <div style="display:flex;flex-direction:column;gap:2px">
            <span style="font-size:11px;color:var(--text3)">조건</span>
            <select id="tr-op" style="width:65px">
              <option value="<=">≤ 이하</option>
              <option value=">=">≥ 이상</option>
              <option value="==">= 같음</option>
              <option value="<">&lt; 미만</option>
              <option value=">">&gt; 초과</option>
            </select>
          </div>
          <div style="display:flex;flex-direction:column;gap:2px">
            <span style="font-size:11px;color:var(--text3)">수치</span>
            <input id="tr-val" type="number" value="0" style="width:60px;text-align:center">
          </div>
          <div style="display:flex;flex-direction:column;gap:2px">
            <span style="font-size:11px;color:var(--text3)">이동 씬#</span>
            <input id="tr-goto" type="number" min="1" placeholder="씬번호" style="width:75px;text-align:center">
          </div>
          <div style="display:flex;flex-direction:column;gap:2px;flex:1;min-width:100px">
            <span style="font-size:11px;color:var(--text3)">설명 (선택)</span>
            <input id="tr-label" placeholder="예: HP 0 → 사망">
          </div>
          <button class="btn btn-primary btn-sm" id="btn-add-trigger">추가</button>
        </div>
      </div>
    </div>
    ` : ''}

    ${editorSettings.showItems ? `
    <div style="background:var(--card);border:1px solid var(--border);border-radius:10px;margin-bottom:8px;overflow:hidden">
      <div id="item-header" style="display:flex;align-items:center;gap:8px;padding:10px 14px;cursor:pointer;user-select:none" onclick="document.getElementById('item-body').style.display=document.getElementById('item-body').style.display==='none'?'block':'none';document.getElementById('item-arrow').style.transform=document.getElementById('item-body').style.display==='none'?'':'rotate(180deg)'">
        <span style="font-size:13px;font-weight:bold;color:var(--text2)">🎒 아이템 설정</span>
        <span style="font-size:11px;color:var(--text3)">(선택지 잠금/획득에 사용)</span>
        <div style="flex:1"></div>
        <span id="item-arrow" style="color:var(--text3);font-size:13px;transition:transform 0.2s">▼</span>
      </div>
      <div id="item-body" style="display:none;padding:10px 14px;border-top:1px solid var(--border)">
        <div id="item-list"></div>
        <div style="display:flex;gap:6px;margin-top:8px;flex-wrap:wrap;align-items:flex-end">
          <div style="display:flex;flex-direction:column;gap:2px;flex:1;min-width:160px">
            <span style="font-size:11px;color:var(--text3)">아이템 이름</span>
            <input id="item-name" placeholder="예: 낡은 열쇠">
          </div>
          <button class="btn btn-primary btn-sm" id="btn-add-item">추가</button>
        </div>
      </div>
    </div>
    ` : ''}

    <div style="display:flex;gap:6px;margin-bottom:8px;flex-wrap:wrap;align-items:center">
      <input id="search-num" type="number" placeholder="번호 검색..." min="1" style="width:140px;flex-shrink:0">
      <button class="btn btn-secondary btn-sm" id="btn-clear-search">✕</button>
      <div style="flex:1"></div>
      <span style="font-size:12px;color:var(--text3)">보기:</span>
      <button class="btn btn-sm ${viewMode==='cards'?'btn-primary':'btn-secondary'}" id="view-cards">카드</button>
      <button class="btn btn-sm ${viewMode==='slides'?'btn-primary':'btn-secondary'}" id="view-slides">슬라이드</button>
      <span style="font-size:12px;color:var(--text3)">정렬:</span>
      <button class="btn btn-sm ${sortMode==='created'?'btn-primary':'btn-secondary'}" id="sort-created">생성순</button>
      <button class="btn btn-sm ${sortMode==='num'?'btn-primary':'btn-secondary'}" id="sort-num">번호순</button>
      <button class="btn btn-secondary btn-sm" id="btn-collapse-all">모두 접기</button>
      <button class="btn btn-secondary btn-sm" id="btn-expand-all">모두 펼치기</button>
    </div>

    <div style="display:flex;gap:8px;margin-bottom:12px;align-items:center;flex-wrap:wrap;background:var(--card);border:1px solid var(--border);border-radius:10px;padding:10px 12px;overflow:visible">
      <span style="font-size:12px;color:var(--text3);white-space:nowrap;flex-shrink:0">보기 범위</span>
      <input id="view-range-min" type="number" min="1" placeholder="시작 번호" value="${viewRangeMin}" style="width:110px;max-width:calc(50vw - 64px);text-align:center;flex-shrink:0">
      <span style="font-size:12px;color:var(--text3);flex-shrink:0">~</span>
      <input id="view-range-max" type="number" min="1" placeholder="끝 번호" value="${viewRangeMax}" style="width:110px;max-width:calc(50vw - 64px);text-align:center;flex-shrink:0">
      <button class="btn btn-secondary btn-sm" id="btn-clear-view-range" style="flex-shrink:0">전체</button>
      <span style="font-size:11px;color:var(--text3);line-height:1.4">입력한 번호 범위의 씬만 아래 작성 목록에 표시됩니다.</span>
    </div>

    <div id="scene-list"></div>

    <div id="empty-hint" style="display:none;text-align:center;padding:40px;color:var(--text3)">
      <div style="font-size:40px;margin-bottom:12px">📜</div>
      <p>씬이 없습니다. "🎲 씬 추가"를 눌러 시작하세요.</p>
    </div>
  `;

  const sceneList = container.querySelector('#scene-list');

  function getVisible() {
    let list = searchQuery
      ? scenes.filter(s => String(s.num).includes(searchQuery))
      : [...scenes];
    const min = parseInt(viewRangeMin);
    const max = parseInt(viewRangeMax);
    if (!Number.isNaN(min)) list = list.filter(s => s.num >= min);
    if (!Number.isNaN(max)) list = list.filter(s => s.num <= max);
    if (sortMode === 'num') list.sort((a, b) => a.num - b.num);
    return list;
  }

  function updateSortButtons() {
    container.querySelector('#sort-created').className = `btn btn-sm ${sortMode==='created'?'btn-primary':'btn-secondary'}`;
    container.querySelector('#sort-num').className = `btn btn-sm ${sortMode==='num'?'btn-primary':'btn-secondary'}`;
  }

  function updateViewButtons() {
    container.querySelector('#view-cards').className = `btn btn-sm ${viewMode==='cards'?'btn-primary':'btn-secondary'}`;
    container.querySelector('#view-slides').className = `btn btn-sm ${viewMode==='slides'?'btn-primary':'btn-secondary'}`;
  }

  function bindEditorSettingToggle(id, key) {
    const input = container.querySelector(id);
    if (!input) return;
    input.onchange = async () => {
      editorSettings[key] = input.checked;
      await saveAll();
      renderEditor(container, params);
    };
  }

  function renderAll() {
    sceneList.innerHTML = '';
    const visible = getVisible();
    container.querySelector('#empty-hint').style.display =
      scenes.length === 0 ? '' : 'none';
    if (scenes.length > 0 && visible.length === 0) {
      sceneList.innerHTML = `
        <div style="text-align:center;padding:32px;color:var(--text3);background:var(--card);border:1px solid var(--border);border-radius:12px">
          <div style="font-size:28px;margin-bottom:8px">🔎</div>
          <p>현재 검색/보기 범위에 해당하는 씬이 없습니다.</p>
        </div>
      `;
      return;
    }
    if (viewMode === 'slides') renderSlideView(visible);
    else visible.forEach(s => sceneList.appendChild(buildSceneCard(s)));
  }

  function renderSlideView(visible) {
    const wrap = document.createElement('div');
    wrap.style.cssText = 'display:flex;gap:14px;overflow-x:auto;scroll-snap-type:x mandatory;padding:4px 2px 14px;scroll-behavior:smooth;';

    visible.forEach((scene, index) => {
      const slide = document.createElement('div');
      slide.dataset.num = scene.num;
      slide.style.cssText = 'scroll-snap-align:center;flex:0 0 min(100%, 680px);border:1px solid var(--border);border-radius:12px;background:var(--card);overflow:hidden;';
      slide.innerHTML = `
        <div style="display:flex;align-items:center;gap:8px;padding:10px 14px;border-bottom:1px solid var(--border);background:var(--bg2);flex-wrap:wrap">
          <span style="font-weight:bold;color:var(--accent3)">#${scene.num}#</span>
          <span style="font-size:12px;color:var(--text3)">${index + 1} / ${visible.length}</span>
          <div style="flex:1"></div>
          <button class="btn btn-secondary btn-sm slide-prev">←</button>
          <button class="btn btn-secondary btn-sm slide-next">→</button>
        </div>
        <div style="padding:16px">
          <textarea class="slide-scene-text" style="min-height:46vh;font-size:16px;line-height:1.85">${escHtml(scene.text || '')}</textarea>
          <div style="display:flex;align-items:center;justify-content:space-between;margin:14px 0 8px;gap:8px">
            <span style="font-size:13px;color:var(--text2);font-weight:bold">선택지 ${(scene.choices || []).length}개</span>
            <button class="btn btn-secondary btn-sm slide-add-choice">+ 선택지 추가</button>
          </div>
          <div class="slide-choice-list" style="display:flex;flex-direction:column;gap:6px"></div>
        </div>
      `;

      slide.querySelector('.slide-scene-text').addEventListener('input', e => {
        scene.text = e.target.value;
        scheduleAutoSave();
      });
      slide.querySelector('.slide-prev').onclick = () => {
        const prev = wrap.children[Math.max(0, index - 1)];
        if (prev) prev.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
      };
      slide.querySelector('.slide-next').onclick = () => {
        const next = wrap.children[Math.min(visible.length - 1, index + 1)];
        if (next) next.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
      };

      const choiceList = slide.querySelector('.slide-choice-list');
      function renderSlideChoices() {
        choiceList.innerHTML = '';
        if (!scene.choices || scene.choices.length === 0) {
          choiceList.innerHTML = '<p style="font-size:13px;color:var(--text3)">선택지가 없습니다.</p>';
          return;
        }
        scene.choices.forEach((ch, idx) => {
          const row = document.createElement('div');
          row.style.cssText = 'display:flex;gap:6px;align-items:center;flex-wrap:wrap;padding:10px 12px;border:1px solid var(--border);border-radius:8px;background:var(--bg2);';
          row.innerHTML = `
            <span style="font-size:12px;color:var(--text3);white-space:nowrap">→ #</span>
            <input class="ch-goto" type="number" min="1" value="${ch.goto || ''}" placeholder="이동 번호" style="width:80px">
            <input class="ch-text" value="${escHtml(ch.text || '')}" placeholder="선택지 텍스트" style="flex:1;min-width:140px">
            ${editorSettings.showChoiceConditions ? `
            <input class="ch-need-flag" value="${escHtml(ch.need || '')}" placeholder="필요 플래그" style="width:130px;font-size:12px">
            <input class="ch-sets-flags" value="${escHtml((ch.sets || []).join(', '))}" placeholder="설정 플래그" style="width:150px;font-size:12px">
            <select class="ch-need-stat" title="필요 스텟" style="width:150px;font-size:12px">${statConditionOptions(ch.needStatKey || '')}</select>
            <select class="ch-need-stat-op" title="스텟 조건" style="width:74px;font-size:12px">
              ${['<=','>=','==','<','>'].map(op => `<option value="${op}"${(ch.needStatOp || '>=') === op ? ' selected' : ''}>${op}</option>`).join('')}
            </select>
            <input class="ch-need-stat-value" type="number" value="${ch.needStatValue ?? ''}" placeholder="수치" style="width:72px;font-size:12px;text-align:center">
            <select class="ch-need-item" title="필요 아이템" style="width:150px;font-size:12px">${itemOptions(ch.needItem || '')}</select>
            <select class="ch-give-item" title="획득 아이템" style="width:150px;font-size:12px">${itemOptions(ch.giveItem || '')}</select>
            <label style="display:flex;align-items:center;gap:4px;font-size:12px;color:var(--text3);white-space:nowrap">
              <input class="ch-consume-item" type="checkbox"${ch.consumeItem ? ' checked' : ''}> 필요 아이템 소모
            </label>
            ` : ''}
            <button class="btn btn-danger btn-sm ch-del">✕</button>
          `;
          row.querySelector('.ch-goto').oninput = e => { ch.goto = parseInt(e.target.value) || 0; scheduleAutoSave(); };
          row.querySelector('.ch-text').oninput = e => { ch.text = e.target.value; scheduleAutoSave(); };
          if (editorSettings.showChoiceConditions) {
            row.querySelector('.ch-need-flag').oninput = e => { ch.need = e.target.value.trim() || null; scheduleAutoSave(); };
            row.querySelector('.ch-sets-flags').oninput = e => { ch.sets = splitCsv(e.target.value); scheduleAutoSave(); };
            row.querySelector('.ch-need-stat').onchange = e => { ch.needStatKey = e.target.value || null; scheduleAutoSave(); };
            row.querySelector('.ch-need-stat-op').onchange = e => { ch.needStatOp = e.target.value || '>='; scheduleAutoSave(); };
            row.querySelector('.ch-need-stat-value').oninput = e => { ch.needStatValue = e.target.value === '' ? null : Number(e.target.value); scheduleAutoSave(); };
            row.querySelector('.ch-need-item').onchange = e => { ch.needItem = e.target.value || null; scheduleAutoSave(); };
            row.querySelector('.ch-give-item').onchange = e => { ch.giveItem = e.target.value || null; scheduleAutoSave(); };
            row.querySelector('.ch-consume-item').onchange = e => { ch.consumeItem = e.target.checked; scheduleAutoSave(); };
          }
          row.querySelector('.ch-del').onclick = () => {
            scene.choices.splice(idx, 1);
            renderSlideChoices();
            scheduleAutoSave();
          };
          choiceList.appendChild(row);
        });
      }
      renderSlideChoices();

      slide.querySelector('.slide-add-choice').onclick = () => {
        scene.choices.push({ goto: 0, need: null, sets: [], needStatKey: null, needStatOp: '>=', needStatValue: null, needItem: null, giveItem: null, consumeItem: false, text: '' });
        renderSlideChoices();
        scheduleAutoSave();
      };

      wrap.appendChild(slide);
    });

    sceneList.appendChild(wrap);
  }

  function buildSceneCard(scene) {
    const card = document.createElement('div');
    card.dataset.num = scene.num;
    card.style.cssText = 'border-radius:12px;border:1px solid var(--border);margin-bottom:8px;overflow:hidden;';

    const COLOR_NAMES = ['기본', '정보', '주의', '위험', '특수'];
    const COLOR_HEX = ['#6b7280', '#3b82f6', '#f59e0b', '#ef4444', '#a3e635'];

    function rerender() {
      const newCard = buildSceneCard(scene);
      card.replaceWith(newCard);
    }

    card.innerHTML = `
      <div class="scene-header" style="
        display:flex;align-items:center;gap:8px;padding:10px 14px;
        background:var(--card);cursor:pointer;user-select:none;
        border-left:4px solid ${COLOR_HEX[scene.color - 1]};
      ">
        <span style="font-weight:bold;color:var(--accent3);font-size:15px;min-width:40px">#${scene.num}#</span>
        <span style="font-size:11px;padding:2px 8px;border-radius:10px;background:var(--bg3);color:${COLOR_HEX[scene.color - 1]}">${COLOR_NAMES[scene.color - 1]}</span>
        <span style="flex:1;font-size:13px;color:var(--text3);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
          ${scene.text ? scene.text.slice(0, 60).replace(/\n/g, ' ') + (scene.text.length > 60 ? '…' : '') : '(내용 없음)'}
        </span>
        <span style="font-size:12px;color:var(--text3)">${scene.choices.length ? scene.choices.length + '개 선택지' : ''}</span>
        <span class="collapse-icon" style="color:var(--text3);font-size:14px">${scene.collapsed ? '▶' : '▼'}</span>
      </div>
      <div class="scene-body" style="display:${scene.collapsed ? 'none' : 'block'};padding:14px;background:var(--bg2);border-top:1px solid var(--border);">
        <div style="display:flex;gap:6px;margin-bottom:10px;align-items:center;flex-wrap:wrap">
          <span style="font-size:12px;color:var(--text3)">색상:</span>
          ${[1,2,3,4,5].map(c => `
            <button class="color-pick-btn${c === scene.color ? ' active' : ''}" data-c="${c}"
              title="${COLOR_NAMES[c-1]}" style="background:${COLOR_HEX[c-1]}"></button>
          `).join('')}
          <div style="flex:1"></div>
          ${sortMode === 'created' ? `
            <button class="btn btn-secondary btn-sm scene-move-up" title="위로">↑</button>
            <button class="btn btn-secondary btn-sm scene-move-down" title="아래로">↓</button>
          ` : ''}
          <button class="btn btn-danger btn-sm scene-delete">🗑 씬 삭제</button>
        </div>

        <div class="field">
          <label>씬 내용</label>
          <textarea class="scene-text" style="min-height:100px;font-size:14px;line-height:1.7">${escHtml(scene.text || '')}</textarea>
        </div>
        <div class="choices-section">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
            <span style="font-size:13px;color:var(--text2);font-weight:bold">선택지</span>
            <button class="btn btn-secondary btn-sm btn-add-choice">+ 선택지 추가</button>
          </div>
          <div class="choice-list"></div>
        </div>
      </div>
    `;

    // Header click → toggle collapse
    card.querySelector('.scene-header').addEventListener('click', e => {
      if (e.target.closest('button')) return;
      scene.collapsed = !scene.collapsed;
      rerender();
      scheduleAutoSave();
    });

    // Color buttons
    card.querySelectorAll('.color-pick-btn').forEach(btn => {
      btn.addEventListener('click', e => {
        e.stopPropagation();
        scene.color = parseInt(btn.dataset.c);
        rerender();
        scheduleAutoSave();
      });
    });

    // Text
    const textArea = card.querySelector('.scene-text');
    textArea.addEventListener('input', () => {
      scene.text = textArea.value;
      scheduleAutoSave();
    });

    // Delete scene
    card.querySelector('.scene-delete').addEventListener('click', e => {
      e.stopPropagation();
      if (!confirm(`씬 #${scene.num}#을 삭제할까요?`)) return;
      scenes = scenes.filter(s => s !== scene);
      renderAll();
      scheduleAutoSave();
    });

    // Move up/down (생성순 모드에서만 표시됨)
    const upBtn = card.querySelector('.scene-move-up');
    const downBtn = card.querySelector('.scene-move-down');
    if (upBtn) upBtn.addEventListener('click', e => {
      e.stopPropagation();
      const i = scenes.indexOf(scene);
      if (i > 0) { [scenes[i-1], scenes[i]] = [scenes[i], scenes[i-1]]; renderAll(); scheduleAutoSave(); }
    });
    if (downBtn) downBtn.addEventListener('click', e => {
      e.stopPropagation();
      const i = scenes.indexOf(scene);
      if (i < scenes.length - 1) { [scenes[i], scenes[i+1]] = [scenes[i+1], scenes[i]]; renderAll(); scheduleAutoSave(); }
    });

    // Choices
    const choiceList = card.querySelector('.choice-list');
    function renderChoices() {
      choiceList.innerHTML = '';
      scene.choices.forEach((ch, idx) => {
        const row = document.createElement('div');
        row.style.cssText = 'display:flex;gap:6px;margin-bottom:6px;align-items:center;flex-wrap:wrap;';
        row.innerHTML = `
          <span style="font-size:12px;color:var(--text3);white-space:nowrap">→ #</span>
          <input class="ch-goto" type="number" min="1" value="${ch.goto || ''}"
            placeholder="이동 번호" style="width:80px">
          <input class="ch-text" value="${escHtml(ch.text || '')}"
            placeholder="선택지 텍스트" style="flex:1;min-width:120px">
          ${editorSettings.showChoiceConditions ? `
          <input class="ch-need-flag" value="${escHtml(ch.need || '')}"
            placeholder="필요 플래그" style="width:130px;font-size:12px">
          <input class="ch-sets-flags" value="${escHtml((ch.sets || []).join(', '))}"
            placeholder="설정 플래그" style="width:150px;font-size:12px">
          <select class="ch-need-stat" title="필요 스텟" style="width:150px;font-size:12px">
            ${statConditionOptions(ch.needStatKey || '')}
          </select>
          <select class="ch-need-stat-op" title="스텟 조건" style="width:74px;font-size:12px">
            ${['<=','>=','==','<','>'].map(op => `<option value="${op}"${(ch.needStatOp || '>=') === op ? ' selected' : ''}>${op}</option>`).join('')}
          </select>
          <input class="ch-need-stat-value" type="number" value="${ch.needStatValue ?? ''}"
            placeholder="수치" style="width:72px;font-size:12px;text-align:center">
          <select class="ch-need-item" title="필요 아이템" style="width:150px;font-size:12px">
            ${itemOptions(ch.needItem || '')}
          </select>
          <select class="ch-give-item" title="획득 아이템" style="width:150px;font-size:12px">
            ${itemOptions(ch.giveItem || '')}
          </select>
          <label style="display:flex;align-items:center;gap:4px;font-size:12px;color:var(--text3);white-space:nowrap">
            <input class="ch-consume-item" type="checkbox"${ch.consumeItem ? ' checked' : ''}> 필요 아이템 소모
          </label>
          ` : ''}
          <button class="btn btn-danger btn-sm ch-del">✕</button>
        `;
        row.querySelector('.ch-goto').oninput = e => { ch.goto = parseInt(e.target.value) || 0; scheduleAutoSave(); };
        row.querySelector('.ch-text').oninput = e => { ch.text = e.target.value; scheduleAutoSave(); };
        if (editorSettings.showChoiceConditions) {
          row.querySelector('.ch-need-flag').oninput = e => { ch.need = e.target.value.trim() || null; scheduleAutoSave(); };
          row.querySelector('.ch-sets-flags').oninput = e => { ch.sets = splitCsv(e.target.value); scheduleAutoSave(); };
          row.querySelector('.ch-need-stat').onchange = e => { ch.needStatKey = e.target.value || null; scheduleAutoSave(); };
          row.querySelector('.ch-need-stat-op').onchange = e => { ch.needStatOp = e.target.value || '>='; scheduleAutoSave(); };
          row.querySelector('.ch-need-stat-value').oninput = e => { ch.needStatValue = e.target.value === '' ? null : Number(e.target.value); scheduleAutoSave(); };
          row.querySelector('.ch-need-item').onchange = e => { ch.needItem = e.target.value || null; scheduleAutoSave(); };
          row.querySelector('.ch-give-item').onchange = e => { ch.giveItem = e.target.value || null; scheduleAutoSave(); };
          row.querySelector('.ch-consume-item').onchange = e => { ch.consumeItem = e.target.checked; scheduleAutoSave(); };
        }
        row.querySelector('.ch-del').onclick = () => {
          scene.choices.splice(idx, 1);
          renderChoices();
          scheduleAutoSave();
        };
        choiceList.appendChild(row);
      });
    }
    renderChoices();

    card.querySelector('.btn-add-choice').addEventListener('click', e => {
      e.stopPropagation();
      scene.choices.push({ goto: 0, need: null, sets: [], needStatKey: null, needStatOp: '>=', needStatValue: null, needItem: null, giveItem: null, consumeItem: false, text: '' });
      renderChoices();
      scheduleAutoSave();
    });

    return card;
  }

  bindEditorSettingToggle('#set-show-triggers', 'showTriggers');
  bindEditorSettingToggle('#set-show-items', 'showItems');
  bindEditorSettingToggle('#set-show-choice-conditions', 'showChoiceConditions');

  // Range inputs
  container.querySelector('#range-min').oninput = e => {
    rangeMin = parseInt(e.target.value) || 1;
    scheduleAutoSave();
  };
  container.querySelector('#range-max').oninput = e => {
    rangeMax = parseInt(e.target.value) || 100;
    scheduleAutoSave();
  };

  // Add scene — #1# for first scene, random thereafter
  container.querySelector('#btn-add-scene').onclick = () => {
    const num = scenes.length === 0 ? 1 : pickRandomNum();
    if (num === null) {
      toast(`${rangeMin}~${rangeMax} 범위의 번호가 모두 사용 중입니다`);
      return;
    }
    scenes.push({ num, color: 1, text: '', choices: [], collapsed: false });
    searchQuery = '';
    container.querySelector('#search-num').value = '';
    renderAll();
    scheduleAutoSave();
    toast(`씬 #${num}# 추가됨`);
  };

  // Sort buttons
  container.querySelector('#sort-created').onclick = () => {
    sortMode = 'created'; updateSortButtons(); renderAll(); scheduleAutoSave();
  };
  container.querySelector('#sort-num').onclick = () => {
    sortMode = 'num'; updateSortButtons(); renderAll(); scheduleAutoSave();
  };

  // Search
  const searchInput = container.querySelector('#search-num');
  searchInput.addEventListener('input', () => {
    searchQuery = searchInput.value.trim();
    renderAll();
  });
  container.querySelector('#btn-clear-search').onclick = () => {
    searchQuery = '';
    searchInput.value = '';
    renderAll();
  };
  const viewMinInput = container.querySelector('#view-range-min');
  const viewMaxInput = container.querySelector('#view-range-max');
  viewMinInput.addEventListener('input', () => {
    viewRangeMin = viewMinInput.value.trim();
    renderAll();
    scheduleAutoSave();
  });
  viewMaxInput.addEventListener('input', () => {
    viewRangeMax = viewMaxInput.value.trim();
    renderAll();
    scheduleAutoSave();
  });
  container.querySelector('#btn-clear-view-range').onclick = () => {
    viewRangeMin = '';
    viewRangeMax = '';
    viewMinInput.value = '';
    viewMaxInput.value = '';
    renderAll();
    scheduleAutoSave();
  };

  container.querySelector('#view-cards').onclick = () => {
    viewMode = 'cards';
    updateViewButtons();
    renderAll();
    scheduleAutoSave();
  };
  container.querySelector('#view-slides').onclick = () => {
    viewMode = 'slides';
    updateViewButtons();
    renderAll();
    scheduleAutoSave();
  };

  // Collapse all / expand all
  container.querySelector('#btn-collapse-all').onclick = () => {
    scenes.forEach(s => s.collapsed = true);
    renderAll();
  };
  container.querySelector('#btn-expand-all').onclick = () => {
    scenes.forEach(s => s.collapsed = false);
    renderAll();
  };

  // Save
  container.querySelector('#btn-save').onclick = async () => {
    await saveAll();
    toast('저장되었습니다');
  };

  // Graph
  container.querySelector('#btn-graph').onclick = () => showSceneGraph(scenes);

  // Export
  container.querySelector('#btn-export').onclick = () => {
    const text = Utils.exportScenarioText({ ...scenario, scenes, triggers, items, editorSettings });
    Utils.downloadText(`${scenario.title}.txt`, text);
    toast('내보내기 완료');
  };

  // Item list render
  function renderItems() {
    const list = container.querySelector('#item-list');
    if (!list) return;
    if (!items.length) {
      list.innerHTML = '<p style="font-size:12px;color:var(--text3)">등록된 아이템이 없습니다.</p>';
      return;
    }
    list.innerHTML = items.map((item, i) => `
      <div style="display:flex;align-items:center;gap:8px;padding:6px 0;border-bottom:1px solid var(--border);flex-wrap:wrap">
        <span style="font-size:12px;color:var(--accent3);flex:1;min-width:160px">
          <strong>${escHtml(item.name)}</strong>
        </span>
        <button class="btn btn-danger btn-sm" data-ii="${i}" style="padding:2px 8px;font-size:11px">삭제</button>
      </div>
    `).join('');
    list.querySelectorAll('[data-ii]').forEach(btn => {
      btn.onclick = () => {
        const item = items[parseInt(btn.dataset.ii)];
        if (!confirm(`아이템 '${item.name}'을 삭제할까요? 선택지 연결은 유지됩니다.`)) return;
        items.splice(parseInt(btn.dataset.ii), 1);
        scheduleAutoSave();
        renderItems();
        renderAll();
      };
    });
  }
  if (editorSettings.showItems) {
    renderItems();

    container.querySelector('#btn-add-item').onclick = () => {
      const nameInput = container.querySelector('#item-name');
      const name = nameInput.value.trim();
      if (!name) { toast('아이템 이름을 입력하세요'); return; }
      let key = normalizeItemKey(name);
      if (!key) key = 'item_' + Utils.genId().slice(0, 6);
      if (items.some(item => item.key === key)) key = `${key}_${Utils.genId().slice(0, 4)}`;
      items.push({ key, name });
      nameInput.value = '';
      scheduleAutoSave();
      renderItems();
      renderAll();
      toast(`아이템 추가됨: ${name}`);
    };
  }

  // Trigger list render
  function renderTriggers() {
    const list = container.querySelector('#trigger-list');
    if (!list) return;
    if (!triggers.length) {
      list.innerHTML = '<p style="font-size:12px;color:var(--text3)">트리거가 없습니다.</p>';
      return;
    }
    list.innerHTML = triggers.map((t, i) => `
      <div style="display:flex;align-items:center;gap:8px;padding:6px 0;border-bottom:1px solid var(--border);flex-wrap:wrap">
        <span style="font-size:12px;color:var(--accent3);font-family:monospace;flex:1;min-width:160px">
          <strong>${t.statKey}</strong> ${t.op} ${t.value} → 씬 #${t.goto}#
        </span>
        ${t.label ? `<span style="font-size:11px;color:var(--text3)">${t.label}</span>` : ''}
        <button class="btn btn-danger btn-sm" data-ti="${i}" style="padding:2px 8px;font-size:11px">삭제</button>
      </div>
    `).join('');
    list.querySelectorAll('[data-ti]').forEach(btn => {
      btn.onclick = () => {
        triggers.splice(parseInt(btn.dataset.ti), 1);
        scheduleAutoSave(); renderTriggers();
      };
    });
  }
  if (editorSettings.showTriggers) {
    renderTriggers();

    container.querySelector('#btn-add-trigger').onclick = () => {
      const key = container.querySelector('#tr-key').value.trim();
      const op = container.querySelector('#tr-op').value;
      const value = parseFloat(container.querySelector('#tr-val').value) || 0;
      const goto = parseInt(container.querySelector('#tr-goto').value);
      const label = container.querySelector('#tr-label').value.trim();
      if (!key) { toast('스텟 키를 입력하세요 (hp, san, mp 등)'); return; }
      if (!goto || goto < 1) { toast('이동할 씬 번호를 입력하세요'); return; }
      triggers.push({ id: Utils.genId(), statKey: key, op, value, goto, label });
      container.querySelector('#tr-key').value = '';
      container.querySelector('#tr-label').value = '';
      scheduleAutoSave(); renderTriggers();
      toast(`트리거 추가됨: ${key} ${op} ${value} → 씬 #${goto}#`);
    };
  }

  renderAll();
}

function scenesToScript(scenes) {
  return scenes.map(s => {
    let line = `#${s.num}# [color:${s.color}] ${s.text || ''}`;
    for (const c of s.choices || []) {
      line += `\n> [goto:${c.goto}]${c.need ? ' [need:' + c.need + ']' : ''}${Array.isArray(c.sets) && c.sets.length ? ' [sets:' + c.sets.join(',') + ']' : ''}${c.needStatKey ? ' [needStat:' + c.needStatKey + ']' : ''}${c.needStatOp ? ' [needOp:' + c.needStatOp + ']' : ''}${c.needStatValue !== null && c.needStatValue !== undefined ? ' [needVal:' + c.needStatValue + ']' : ''}${c.needItem ? ' [needItem:' + c.needItem + ']' : ''}${c.giveItem ? ' [giveItem:' + c.giveItem + ']' : ''}${c.consumeItem ? ' [consumeItem]' : ''} ${c.text || ''}`;
    }
    return line;
  }).join('\n\n');
}

function escHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function showSceneGraph(scenes) {
  if (!scenes.length) { toast('씬이 없습니다'); return; }

  const sceneMap = new Map(scenes.map(s => [s.num, s]));

  // Build adjacency (deduplicated gotos per scene)
  const adjSet = new Map();
  for (const s of scenes) {
    adjSet.set(s.num, new Set());
    for (const c of s.choices || []) {
      if (c.goto && sceneMap.has(c.goto)) adjSet.get(s.num).add(c.goto);
    }
  }

  // BFS layer (column) assignment
  const layer = new Map();
  function bfsFrom(start, startL) {
    if (layer.has(start)) return;
    layer.set(start, startL);
    const q = [start]; let h = 0;
    while (h < q.length) {
      const cur = q[h++], curL = layer.get(cur);
      for (const nxt of (adjSet.get(cur) || new Set())) {
        if (!layer.has(nxt)) { layer.set(nxt, curL + 1); q.push(nxt); }
      }
    }
  }

  if (sceneMap.has(1)) bfsFrom(1, 0);

  const pointed = new Set([...adjSet.values()].flatMap(s => [...s]));
  const sortedNums = [...sceneMap.keys()].sort((a, b) => a - b);
  const getMaxL = () => (layer.size ? Math.max(...layer.values()) : -1);

  // BFS from unvisited roots (not pointed to by anyone)
  for (const num of sortedNums) {
    if (!layer.has(num) && !pointed.has(num)) {
      bfsFrom(num, getMaxL() + 1);
    }
  }
  // Assign remaining (isolated or in pure cycles)
  for (const num of sortedNums) {
    if (!layer.has(num)) layer.set(num, getMaxL() + 1);
  }

  // Group nodes per column, sort by num within column
  const layerNodes = new Map();
  for (const [num, l] of layer) {
    if (!layerNodes.has(l)) layerNodes.set(l, []);
    layerNodes.get(l).push(num);
  }
  for (const arr of layerNodes.values()) arr.sort((a, b) => a - b);

  // Compute node positions (horizontal layout: column = layer)
  const NW = 90, NH = 44, CGAP = 70, RGAP = 22, PAD = 40;
  const nodePos = new Map();
  let svgW = 0, svgH = 0;
  for (const [l, nums] of layerNodes) {
    nums.forEach((num, ri) => {
      const x = PAD + l * (NW + CGAP), y = PAD + ri * (NH + RGAP);
      nodePos.set(num, { x, y, cx: x + NW / 2, cy: y + NH / 2 });
      svgW = Math.max(svgW, x + NW + PAD);
      svgH = Math.max(svgH, y + NH + PAD);
    });
  }

  // Classify edges
  const COLOR_HEX = ['#6b7280', '#3b82f6', '#f59e0b', '#ef4444', '#a3e635'];
  const seen = new Set();
  const fwdEdges = [], backEdges = [], selfLoops = [];
  for (const s of scenes) {
    const lF = layer.get(s.num);
    for (const t of (adjSet.get(s.num) || new Set())) {
      const key = `${s.num}->${t}`;
      if (seen.has(key)) continue;
      seen.add(key);
      if (t === s.num) selfLoops.push(s.num);
      else if (layer.get(t) > lF) fwdEdges.push({ f: s.num, t });
      else backEdges.push({ f: s.num, t });
    }
  }

  const BASE_BACK_Y = svgH + 8;
  const totalH = BASE_BACK_Y + backEdges.length * 22 + PAD;
  const paths = [];

  // Self-loops (yellow arc above node)
  for (const num of selfLoops) {
    const p = nodePos.get(num);
    const x1 = p.x + NW * 0.3, x2 = p.x + NW * 0.7, y = p.y;
    paths.push(`<path d="M${x1},${y} C${x1},${y-34} ${x2},${y-34} ${x2},${y}" fill="none" stroke="#f59e0b" stroke-width="2" stroke-dasharray="4,2" marker-end="url(#arw-y)"/>`);
  }

  // Forward edges (purple bezier, right→left of next column)
  for (const { f, t } of fwdEdges) {
    const sp = nodePos.get(f), tp = nodePos.get(t);
    const x1 = sp.x + NW, y1 = sp.cy, x2 = tp.x, y2 = tp.cy, mx = (x1 + x2) / 2;
    paths.push(`<path d="M${x1},${y1} C${mx},${y1} ${mx},${y2} ${x2},${y2}" fill="none" stroke="#7c5cbf" stroke-width="1.8" marker-end="url(#arw-p)"/>`);
  }

  // Back/same-layer edges (red dashed, arc below graph)
  backEdges.forEach(({ f, t }, i) => {
    const sp = nodePos.get(f), tp = nodePos.get(t);
    const arcY = BASE_BACK_Y + i * 22 + 11;
    paths.push(`<path d="M${sp.cx},${sp.y+NH} C${sp.cx},${arcY} ${tp.cx},${arcY} ${tp.cx},${tp.y+NH}" fill="none" stroke="#ef4444" stroke-width="1.5" stroke-dasharray="5,3" marker-end="url(#arw-r)"/>`);
  });

  // Nodes
  const nodeEls = scenes.map(s => {
    const p = nodePos.get(s.num);
    if (!p) return '';
    const hex = COLOR_HEX[(s.color || 1) - 1];
    return `<g class="sg-node" data-num="${s.num}" style="cursor:pointer">
      <rect class="sg-rect" x="${p.x}" y="${p.y}" width="${NW}" height="${NH}" rx="8" fill="var(--card)" stroke="${hex}" stroke-width="2.5"/>
      <text x="${p.cx}" y="${p.cy-5}" text-anchor="middle" font-size="13" font-weight="bold" fill="${hex}">#${s.num}#</text>
      <text x="${p.cx}" y="${p.cy+10}" text-anchor="middle" font-size="10" fill="var(--text3)">${(s.choices||[]).length}개 선택지</text>
    </g>`;
  });

  // Modal
  const modal = document.createElement('div');
  modal.style.cssText = 'position:fixed;inset:0;z-index:9000;background:rgba(0,0,0,0.88);display:flex;flex-direction:column;';
  modal.innerHTML = `
    <div style="display:flex;align-items:center;gap:10px;padding:11px 16px;background:var(--card);border-bottom:1px solid var(--border);flex-shrink:0;flex-wrap:wrap">
      <span style="font-size:14px;font-weight:bold;color:var(--accent3)">🗺 씬 흐름 그래프</span>
      <span style="font-size:11px;color:var(--text3)">
        <span style="color:#7c5cbf">──</span> 순방향 &nbsp;
        <span style="color:#ef4444">╌╌</span> 역방향 &nbsp;
        <span style="color:#f59e0b">⟳</span> 자기참조 &nbsp;
        · 노드 클릭 시 해당 씬으로 이동
      </span>
      <div style="flex:1"></div>
      <button class="btn btn-secondary btn-sm" id="sg-zi">＋</button>
      <button class="btn btn-secondary btn-sm" id="sg-zo">－</button>
      <button class="btn btn-secondary btn-sm" id="sg-zr">1:1</button>
      <button class="btn btn-secondary btn-sm" id="sg-cl">✕ 닫기</button>
    </div>
    <div id="sg-scroll" style="flex:1;overflow:auto;padding:20px;background:var(--bg)">
      <div id="sg-wrap" style="display:inline-block;transform-origin:top left">
        <svg xmlns="http://www.w3.org/2000/svg" width="${svgW}" height="${totalH}" style="display:block">
          <defs>
            <marker id="arw-p" markerWidth="7" markerHeight="7" refX="5" refY="3" orient="auto"><path d="M0,0 L0,6 L7,3 z" fill="#7c5cbf"/></marker>
            <marker id="arw-r" markerWidth="7" markerHeight="7" refX="5" refY="3" orient="auto"><path d="M0,0 L0,6 L7,3 z" fill="#ef4444"/></marker>
            <marker id="arw-y" markerWidth="7" markerHeight="7" refX="5" refY="3" orient="auto"><path d="M0,0 L0,6 L7,3 z" fill="#f59e0b"/></marker>
          </defs>
          ${paths.join('')}
          ${nodeEls.join('')}
        </svg>
      </div>
    </div>
  `;
  document.body.appendChild(modal);

  const wrap = modal.querySelector('#sg-wrap');
  let scale = 1;
  const setScale = v => { scale = Math.max(0.2, Math.min(4, v)); wrap.style.transform = `scale(${scale})`; };
  modal.querySelector('#sg-zi').onclick = () => setScale(scale + 0.2);
  modal.querySelector('#sg-zo').onclick = () => setScale(scale - 0.2);
  modal.querySelector('#sg-zr').onclick = () => setScale(1);

  const closeModal = () => { modal.remove(); document.removeEventListener('keydown', escFn); };
  modal.querySelector('#sg-cl').onclick = closeModal;
  const escFn = e => { if (e.key === 'Escape') closeModal(); };
  document.addEventListener('keydown', escFn);

  modal.querySelectorAll('.sg-node').forEach(node => {
    node.addEventListener('mouseenter', () => node.querySelector('.sg-rect').style.opacity = '0.75');
    node.addEventListener('mouseleave', () => node.querySelector('.sg-rect').style.opacity = '');
    node.addEventListener('click', () => {
      const num = parseInt(node.dataset.num);
      closeModal();
      setTimeout(() => {
        const card = document.querySelector(`[data-num="${num}"]`);
        if (card) card.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 100);
    });
  });
}

Router.register('/editor', renderEditor);
