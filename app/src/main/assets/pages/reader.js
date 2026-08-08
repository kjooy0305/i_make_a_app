async function renderReader(container, params) {
  const id = params[0];
  if (!id) { Router.navigate('/'); return; }

  let scenes = [];
  let scenarioTitle = '';
  let scenarioInfo = '';
  let scenarioTriggers = [];
  let scenarioItems = [];
  const isBuiltin = id === '__doksupu__';

  if (isBuiltin) {
    scenes = DOKSUPU.scenes;
    scenarioTitle = DOKSUPU.title;
    scenarioInfo = DOKSUPU.info || '';
    scenarioTriggers = DOKSUPU.triggers || [];
    scenarioItems = DOKSUPU.items || [];
  } else {
    const scenario = await DB.get('scenarios', id);
    if (!scenario) { toast('시나리오를 찾을 수 없습니다'); Router.navigate('/'); return; }
    scenarioTitle = scenario.title;
    scenarioTriggers = scenario.triggers || [];
    scenarioItems = Array.isArray(scenario.items) ? scenario.items : [];
    scenes = scenario.scenes && scenario.scenes.length
      ? scenario.scenes
      : Utils.parseScript(scenario.script || '');
  }

  const sceneMap = Object.fromEntries(scenes.map(s => [s.num, s]));

  const progress = await DB.get('progress', id) || { scenarioId: id, lastScene: scenes[0]?.num || 1, history: [] };
  let currentNum = progress.lastScene;
  let history = progress.history || [];

  container.innerHTML = `
    <div style="display:flex;align-items:center;gap:8px;margin-bottom:12px;flex-wrap:wrap">
      <button class="btn btn-secondary btn-sm" onclick="Router.navigate('/')">← 홈</button>
      <h2 style="flex:1;color:var(--accent3)">${scenarioTitle}</h2>
      <button class="btn btn-secondary btn-sm" id="btn-gm-panel">🔧 GM</button>
      <button class="btn btn-secondary btn-sm" id="btn-dice-quick">🎲 d20</button>
    </div>
    ${scenarioInfo ? `<div class="card" style="font-size:12px;color:var(--text3);line-height:1.8;margin-bottom:12px">${scenarioInfo.split('|').map(s=>`• ${s.trim()}`).join('<br>')}</div>` : ''}

    <div class="num-input-row">
      <input id="scene-num-input" type="number" min="1" placeholder="씬 번호 입력" value="${currentNum}" />
      <button class="btn btn-primary" id="btn-go">이동</button>
      <button class="btn btn-secondary btn-sm" id="btn-back-scene" title="뒤로">↩</button>
    </div>

    <div id="scene-display"></div>
    <div id="choices-display" class="reader-choices" style="margin-top:12px"></div>

    <div class="card mt-16" style="padding:10px 14px">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px">
        <span style="font-size:12px;color:var(--text3)">이동 기록</span>
        <button class="btn btn-secondary btn-sm" id="btn-clear-hist" style="padding:2px 8px;font-size:11px">초기화</button>
      </div>
      <div id="history-display" style="font-size:12px;color:var(--text3);max-height:80px;overflow-y:auto"></div>
    </div>

    <div id="gm-panel" style="display:none" class="card mt-12">
      <h2>🔧 GM 패널 — 플래그 관리</h2>
      <p class="text-sm text-muted mb-8">플래그를 수동으로 설정하거나 해제할 수 있습니다.</p>
      <div id="inventory-panel" style="margin-bottom:12px"></div>
      <div id="flag-list"></div>
      <div style="display:flex;gap:8px;margin-top:8px">
        <input id="new-flag-input" placeholder="플래그 이름" style="flex:1">
        <button class="btn btn-primary btn-sm" id="btn-add-flag">설정</button>
      </div>
    </div>
  `;

  const sceneDisplay = container.querySelector('#scene-display');
  const choicesDisplay = container.querySelector('#choices-display');
  const historyDisplay = container.querySelector('#history-display');
  const numInput = container.querySelector('#scene-num-input');

  const COLOR_LABELS = ['', '정보', '주의', '위험', '특수'];

  function itemFlagKey(itemKey) {
    return `item:${id}:${itemKey}`;
  }

  function getItemName(itemKey) {
    const item = scenarioItems.find(it => it.key === itemKey);
    return item?.name || itemKey || '';
  }

  async function hasItem(itemKey) {
    return itemKey ? await Flags.check(itemFlagKey(itemKey)) : true;
  }

  async function giveItem(itemKey) {
    if (itemKey) await Flags.set(itemFlagKey(itemKey));
  }

  async function removeItem(itemKey) {
    if (itemKey) await Flags.clear(itemFlagKey(itemKey));
  }

  function getStatLabel(key) {
    if (key === 'hp') return 'HP';
    if (key === 'mp') return 'MP';
    if (key === 'san') return '이성(SAN)';
    if (key === 'luck') return '행운';
    const char = window.__readerCurrentChar;
    const stat = char?.statList?.find(s => s.key === key);
    if (stat) return stat.label;
    const bar = char?.customBars?.find(b => b.id === key || b.name === key);
    return bar?.name || key;
  }

  function renderHistory() {
    historyDisplay.innerHTML = history.length
      ? history.slice().reverse().slice(0, 30).map(n => `<span style="margin-right:4px;color:var(--accent3)">#${n}</span>`).join('→')
      : '<em>아직 없음</em>';
  }

  async function goToScene(num) {
    const scene = sceneMap[num];
    currentNum = num;
    if (history[history.length - 1] !== num) history.push(num);
    if (history.length > 50) history.shift();
    numInput.value = num;
    await DB.put('progress', { scenarioId: id, lastScene: num, history });
    await DB.put('settings', { key: 'lastPlayed', scenarioId: id, title: scenarioTitle, lastScene: num });
    renderHistory();

    // Auto-set flags on arrive
    if (scene && scene.flagsOnArrive) {
      for (const f of scene.flagsOnArrive) await Flags.set(f);
    }

    if (!scene) {
      sceneDisplay.innerHTML = `
        <div class="reader-scene" style="text-align:center;color:var(--text3)">
          <div style="font-size:32px;margin-bottom:12px">🔍</div>
          <p>씬 #${num}#이 없습니다.</p>
          <p class="text-sm mt-8">번호를 확인하거나 다른 씬으로 이동하세요.</p>
        </div>`;
      choicesDisplay.innerHTML = '';
      return;
    }

    sceneDisplay.innerHTML = `
      <div class="reader-scene">
        <div class="scene-num" style="margin-bottom:10px">#${scene.num}# ${COLOR_LABELS[scene.color - 1] || ''}</div>
        <div class="${Utils.getColorClass(scene.color)}">${escapeHtml(scene.text)}</div>
      </div>
    `;

    choicesDisplay.innerHTML = '';
    const currentChar = await DB.get('characters', 'default');
    window.__readerCurrentChar = currentChar;
    for (const choice of (scene.choices || [])) {
      const missingFlag = choice.need && !(await Flags.check(choice.need));
      const missingItem = choice.needItem && !(await hasItem(choice.needItem));
      const statValue = choice.needStatKey ? getCharStat(currentChar, choice.needStatKey) : null;
      const missingStat = choice.needStatKey && !evalTriggerOp(statValue, choice.needStatOp || '>=', choice.needStatValue ?? 0);
      const locked = missingFlag || missingItem || missingStat;
      const btn = document.createElement('button');
      btn.className = 'choice-btn' + (locked ? ' locked' : '');
      if (locked) btn.disabled = true;
      const lockReasons = [];
      if (missingFlag) lockReasons.push(choice.need);
      if (missingItem) lockReasons.push(`아이템: ${getItemName(choice.needItem)}`);
      if (missingStat) lockReasons.push(`${getStatLabel(choice.needStatKey)} ${choice.needStatOp || '>='} ${choice.needStatValue ?? 0}`);
      const badges = [];
      if (locked) badges.push(`<span style="color:var(--text3);font-size:11px">[🔒 ${escapeHtml(lockReasons.join(', '))}]</span>`);
      if (!locked && choice.needStatKey) badges.push(`<span style="color:var(--text3);font-size:11px">[${escapeHtml(getStatLabel(choice.needStatKey))} ${choice.needStatOp || '>='} ${choice.needStatValue ?? 0}]</span>`);
      if (!locked && choice.needItem) badges.push(`<span style="color:var(--text3);font-size:11px">[🎒 ${escapeHtml(getItemName(choice.needItem))}]</span>`);
      if (choice.giveItem) badges.push(`<span style="color:var(--text3);font-size:11px">[획득: ${escapeHtml(getItemName(choice.giveItem))}]</span>`);
      btn.innerHTML = `<strong style="color:var(--accent3)">→ #${choice.goto}#</strong>&nbsp; ${escapeHtml(choice.text)} ${badges.join(' ')}`;
      if (!locked) {
        btn.onclick = async () => {
          if (choice.sets) {
            for (const f of choice.sets) await Flags.set(f);
          }
          if (choice.giveItem) await giveItem(choice.giveItem);
          if (choice.consumeItem && choice.needItem) await removeItem(choice.needItem);
          goToScene(choice.goto);
        };
      }
      choicesDisplay.appendChild(btn);
    }

    if (!scene.choices || scene.choices.length === 0) {
      const hint = document.createElement('p');
      hint.className = 'text-muted text-sm text-center';
      hint.style.marginTop = '12px';
      hint.innerHTML = '— 씬 번호를 직접 입력해 이동하세요 —';
      choicesDisplay.appendChild(hint);
    }

    // Check stat triggers
    if (scenarioTriggers.length > 0) {
      const char = await DB.get('characters', 'default');
      if (char) {
        for (const t of scenarioTriggers) {
          if (t.goto === num) continue; // already at trigger destination, skip
          const val = getCharStat(char, t.statKey);
          if (val !== null && evalTriggerOp(val, t.op, t.value)) {
            const msg = t.label || `${t.statKey} ${t.op} ${t.value}`;
            toast(`⚡ 트리거 발동: ${msg}`, 2500);
            setTimeout(() => goToScene(t.goto), 1200);
            return;
          }
        }
      }
    }
  }

  function getCharStat(char, key) {
    if (!char || !key) return null;
    if (key === 'hp') return char.hp?.current ?? null;
    if (key === 'san') return char.san?.current ?? null;
    if (key === 'mp') return char.mp?.current ?? null;
    if (key === 'luck') return typeof char.luck === 'number' ? char.luck : null;
    if (char.statList) {
      const s = char.statList.find(st => st.key === key);
      if (s) return s.value;
    }
    if (char.stats && typeof char.stats[key] === 'number') return char.stats[key];
    if (char.customBars) {
      const b = char.customBars.find(b => b.name === key || b.id === key);
      if (b) return b.current;
    }
    return null;
  }

  function evalTriggerOp(val, op, threshold) {
    if (val === null || val === undefined || Number.isNaN(Number(val))) return false;
    val = Number(val);
    threshold = Number(threshold);
    if (op === '<=') return val <= threshold;
    if (op === '>=') return val >= threshold;
    if (op === '<')  return val < threshold;
    if (op === '>')  return val > threshold;
    if (op === '==' || op === '=') return val === threshold;
    return false;
  }

  // Navigation
  container.querySelector('#btn-go').onclick = () => {
    const n = parseInt(numInput.value);
    if (n > 0) goToScene(n);
  };
  numInput.addEventListener('keydown', e => {
    if (e.key === 'Enter') { const n = parseInt(numInput.value); if (n > 0) goToScene(n); }
  });
  container.querySelector('#btn-back-scene').onclick = () => {
    if (history.length >= 2) { history.pop(); goToScene(history[history.length - 1]); }
  };
  container.querySelector('#btn-clear-hist').onclick = async () => {
    history = []; await DB.put('progress', { scenarioId: id, lastScene: currentNum, history }); renderHistory();
  };

  // d20 quick roll
  container.querySelector('#btn-dice-quick').onclick = () => {
    const r = Dice.roll(20);
    const c = r === 20 ? '#a3e635' : r === 1 ? '#ef4444' : 'var(--accent3)';
    toast(`🎲 d20 = ${r}${r === 20 ? ' ⭐' : r === 1 ? ' 💀' : ''}`, 3000);
  };

  // GM Panel
  container.querySelector('#btn-gm-panel').onclick = () => {
    const panel = container.querySelector('#gm-panel');
    panel.style.display = panel.style.display === 'none' ? '' : 'none';
    if (panel.style.display !== 'none') renderFlagPanel();
  };

  async function renderFlagPanel() {
    const all = await Flags.getAll();
    const inventory = container.querySelector('#inventory-panel');
    if (scenarioItems.length) {
      const rows = [];
      for (const item of scenarioItems) {
        const owned = await hasItem(item.key);
        rows.push(`
          <div style="display:flex;align-items:center;gap:6px;margin-bottom:4px">
            <span style="flex:1;font-size:13px;color:${owned ? 'var(--accent3)' : 'var(--text3)'}">${owned ? '✓' : '○'} ${escapeHtml(item.name)}</span>
            <button class="btn ${owned ? 'btn-danger' : 'btn-primary'} btn-sm inv-toggle" data-item="${escapeHtml(item.key)}">${owned ? '제거' : '획득'}</button>
          </div>
        `);
      }
      inventory.innerHTML = `
        <h3 style="font-size:14px;color:var(--text2);margin-bottom:6px">🎒 아이템</h3>
        ${rows.join('')}
      `;
      inventory.querySelectorAll('.inv-toggle').forEach(btn => {
        btn.onclick = async () => {
          const key = btn.dataset.item;
          if (await hasItem(key)) await removeItem(key);
          else await giveItem(key);
          await renderFlagPanel();
          goToScene(currentNum);
        };
      });
    } else {
      inventory.innerHTML = '';
    }
    const list = container.querySelector('#flag-list');
    const visibleFlags = all.filter(f => !String(f.key).startsWith(`item:${id}:`));
    list.innerHTML = visibleFlags.length === 0
      ? '<p class="text-sm text-muted">설정된 플래그 없음</p>'
      : visibleFlags.map(f => `
        <div style="display:flex;align-items:center;gap:6px;margin-bottom:4px">
          <span style="flex:1;font-size:13px;color:var(--accent3)">✓ ${f.key}</span>
          <button class="btn btn-danger btn-sm" data-key="${f.key}" onclick="Flags.clear(this.dataset.key).then(()=>renderFlagPanel && renderFlagPanel())">해제</button>
        </div>`).join('');
    // Rebind renderFlagPanel for onclick above
    window.renderFlagPanel = renderFlagPanel;
  }

  container.querySelector('#btn-add-flag').onclick = async () => {
    const val = container.querySelector('#new-flag-input').value.trim();
    if (!val) return;
    await Flags.set(val);
    container.querySelector('#new-flag-input').value = '';
    renderFlagPanel();
    toast(`플래그 '${val}' 설정됨`);
    goToScene(currentNum); // refresh locked choices
  };

  renderHistory();
  goToScene(currentNum);
}

function escapeHtml(text) {
  return String(text || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\n/g,'<br>');
}

Router.register('/reader', renderReader);
