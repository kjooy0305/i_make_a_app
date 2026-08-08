async function renderDicePanel(container) {
  container.innerHTML = `
    <h2 style="color:var(--accent3);margin-bottom:16px">🎲 주사위</h2>
    <div class="card">
      <div class="dice-grid" id="dice-grid"></div>
    </div>
    <div class="card" id="result-card" style="text-align:center;min-height:120px">
      <p class="text-muted text-sm">주사위를 굴려보세요</p>
    </div>
    <div class="card">
      <h2>굴리기 기록</h2>
      <div id="dice-history" style="font-size:13px;max-height:200px;overflow-y:auto"></div>
    </div>
    <div class="card">
      <h2>복수 굴리기</h2>
      <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap">
        <input id="multi-count" type="number" value="2" min="1" max="20" style="width:70px">
        <span style="color:var(--text3)">개</span>
        <select id="multi-type">
          ${Dice.DICE_TYPES.map(d => `<option value="${d}">d${d}</option>`).join('')}
        </select>
        <button class="btn btn-primary" id="btn-multi">굴리기</button>
      </div>
      <div id="multi-result" class="mt-8" style="font-size:13px;color:var(--text2)"></div>
    </div>
  `;

  const grid = container.querySelector('#dice-grid');
  const resultCard = container.querySelector('#result-card');

  Dice.DICE_TYPES.forEach(sides => {
    const btn = document.createElement('button');
    btn.className = 'dice-btn';
    btn.innerHTML = `<span class="dice-icon">${Dice.DICE_ICONS[sides]}</span>d${sides}`;
    btn.onclick = () => rollAndShow(sides);
    grid.appendChild(btn);
  });

  function rollAndShow(sides) {
    const result = Dice.roll(sides);
    Dice.history.add({ sides, result, ts: Date.now() });

    const isMax = result === sides;
    const isMin = result === 1;
    const color = isMax ? '#a3e635' : isMin ? '#ef4444' : 'var(--accent3)';
    const label = isMax ? '⭐ 최대!' : isMin ? '💀 펌블!' : '';

    resultCard.innerHTML = `
      <div style="color:var(--text3);font-size:12px;margin-bottom:4px">d${sides}</div>
      <div class="dice-result" style="color:${color}">${result}</div>
      ${label ? `<div style="color:${color};font-size:14px;font-weight:bold">${label}</div>` : ''}
    `;

    renderHistory();
  }

  function renderHistory() {
    const hist = Dice.history.get();
    const el = container.querySelector('#dice-history');
    if (hist.length === 0) { el.innerHTML = '<span class="text-muted text-sm">없음</span>'; return; }
    el.innerHTML = hist.map(h => {
      const isMax = h.result === h.sides;
      const isMin = h.result === 1;
      const color = isMax ? '#a3e635' : isMin ? '#ef4444' : 'var(--text2)';
      return `<div style="padding:4px 0;border-bottom:1px solid var(--border);display:flex;justify-content:space-between">
        <span style="color:var(--text3)">d${h.sides}</span>
        <span style="color:${color};font-weight:bold">${h.result}</span>
      </div>`;
    }).join('');
  }

  container.querySelector('#btn-multi').onclick = () => {
    const count = parseInt(container.querySelector('#multi-count').value) || 1;
    const sides = parseInt(container.querySelector('#multi-type').value);
    const { rolls, total } = Dice.rollMultiple(sides, count);
    Dice.history.add({ sides, result: total, ts: Date.now(), multi: true });
    container.querySelector('#multi-result').innerHTML = `
      <strong style="color:var(--accent3)">${count}d${sides} = ${total}</strong>
      <span style="color:var(--text3);margin-left:8px">(${rolls.join(', ')})</span>
    `;
    renderHistory();
  };

  renderHistory();
}

Router.register('/dice', renderDicePanel);
