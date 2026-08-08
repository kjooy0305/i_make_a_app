const DEFAULT_CHAR = {
  id: 'default',
  name: '', age: '', occupation: '', gender: '',
  backstory: '',
  statList: [
    { key: 'str', label: '근력 STR', value: 10 },
    { key: 'dex', label: '민첩 DEX', value: 10 },
    { key: 'con', label: '체력 CON', value: 10 },
    { key: 'int', label: '지능 INT', value: 10 },
    { key: 'wis', label: '지혜 WIS', value: 10 },
    { key: 'cha', label: '매력 CHA', value: 10 },
    { key: 'pow', label: '의지 POW', value: 10 },
    { key: 'edu', label: '교육 EDU', value: 10 },
  ],
  hp: { current: 10, max: 10 },
  mp: { current: 10, max: 10 },
  san: { current: 50, max: 50 },
  luck: 50,
  uniqueSkill: '',
  skills: '',
  bodyDamage: {},
  statusEffects: [],
  customBars: [],
  bodyModel: 'male',
  portrait: null,
  notes: ''
};

const BODY_PARTS = [
  { key: 'head',  label: '머리' },
  { key: 'torso', label: '몸통' },
  { key: 'larm',  label: '왼팔' },
  { key: 'rarm',  label: '오른팔' },
  { key: 'lhand', label: '왼손' },
  { key: 'rhand', label: '오른손' },
  { key: 'lleg',  label: '왼다리' },
  { key: 'rleg',  label: '오른다리' },
  { key: 'lfoot', label: '왼발' },
  { key: 'rfoot', label: '오른발' },
];

const DMG_LEVELS = ['정상', '경상', '중상', '중증', '위독', '절단'];
const DMG_COLORS = ['var(--text3)', '#a3e635', '#f59e0b', '#f97316', '#ef4444', '#a21caf'];

const STATUS_LIST = [
  '출혈', '기절', '공포', '중독', '발광', '속박', '시력상실', '청력상실',
  '골절', '화상', '피로', '굶주림', '탈수', '저체온', '마비', '절단'
];

const COLOR_PRESETS = [
  '#ef4444', '#f97316', '#f59e0b', '#a3e635',
  '#10b981', '#22d3ee', '#60a5fa', '#a78bfa',
  '#f472b6', '#e879f9', '#fbbf24', '#94a3b8'
];

async function renderCharacter(container) {
  let char = await DB.get('characters', 'default') || { ...DEFAULT_CHAR };

  // Migration: old stats object → statList array
  if (!char.statList) {
    const legacyLabels = {
      str: '근력 STR', dex: '민첩 DEX', con: '체력 CON', int: '지능 INT',
      wis: '지혜 WIS', cha: '매력 CHA', pow: '의지 POW', edu: '교육 EDU'
    };
    char.statList = char.stats
      ? Object.entries(char.stats).map(([k, v]) => ({ key: k, label: legacyLabels[k] || k.toUpperCase(), value: v }))
      : DEFAULT_CHAR.statList.map(s => ({ ...s }));
  }

  char.hp = { ...DEFAULT_CHAR.hp, ...char.hp };
  char.mp = { ...DEFAULT_CHAR.mp, ...char.mp };
  char.san = { ...DEFAULT_CHAR.san, ...char.san };
  char.bodyDamage = char.bodyDamage || {};
  char.statusEffects = char.statusEffects || [];
  char.customBars = char.customBars || [];
  char.bodyModel = char.bodyModel || 'male';

  const lastPlayed = await DB.get('settings', 'lastPlayed');

  let autoSave = null;
  function scheduleAutoSave() {
    clearTimeout(autoSave);
    autoSave = setTimeout(() => DB.put('characters', char), 1000);
  }

  container.innerHTML = `
    ${lastPlayed ? `
      <div style="margin-bottom:12px">
        <button class="btn btn-primary w-100" id="btn-continue-story" style="justify-content:center;gap:10px;padding:12px">
          <span style="font-size:18px">▶</span>
          <span><strong>${lastPlayed.title}</strong> — 씬 #${lastPlayed.lastScene}# 이어서 플레이</span>
        </button>
      </div>
    ` : ''}
    <div style="display:flex;gap:8px;margin-bottom:12px;align-items:center">
      <div class="tabs" style="flex:1;margin:0">
        <div class="tab active" data-tab="basic">기본 정보</div>
        <div class="tab" data-tab="stats">능력치</div>
        <div class="tab" data-tab="body">신체 상태</div>
        <div class="tab" data-tab="notes">메모</div>
      </div>
      <button class="btn btn-secondary btn-sm" id="btn-export-sheet" style="flex-shrink:0">📄 내보내기</button>
    </div>
    <div id="tab-basic" class="tab-content"></div>
    <div id="tab-stats" class="tab-content" style="display:none"></div>
    <div id="tab-body" class="tab-content" style="display:none"></div>
    <div id="tab-notes" class="tab-content" style="display:none"></div>
    <div style="margin-top:16px;display:flex;gap:8px">
      <button class="btn btn-primary flex-1" id="btn-save-char">저장</button>
      <button class="btn btn-danger btn-sm" id="btn-reset-char">초기화</button>
    </div>
  `;

  if (lastPlayed) {
    container.querySelector('#btn-continue-story').onclick = () =>
      Router.navigate(`/reader/${lastPlayed.scenarioId}`);
  }

  container.querySelectorAll('.tab').forEach(tab => {
    tab.onclick = () => {
      container.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      container.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');
      tab.classList.add('active');
      container.querySelector(`#tab-${tab.dataset.tab}`).style.display = '';
    };
  });

  renderBasicTab(container.querySelector('#tab-basic'), char, scheduleAutoSave);
  renderStatsTab(container.querySelector('#tab-stats'), char, scheduleAutoSave);
  renderBodyTab(container.querySelector('#tab-body'), char, scheduleAutoSave);
  renderNotesTab(container.querySelector('#tab-notes'), char, scheduleAutoSave);

  container.querySelector('#btn-save-char').onclick = async () => {
    await DB.put('characters', char);
    toast('캐릭터가 저장되었습니다');
  };
  container.querySelector('#btn-reset-char').onclick = async () => {
    if (!confirm('캐릭터를 초기화할까요?')) return;
    char = { ...DEFAULT_CHAR, statList: DEFAULT_CHAR.statList.map(s => ({ ...s })), customBars: [] };
    await DB.put('characters', char);
    toast('초기화됨');
    Router.resolve();
  };
  container.querySelector('#btn-export-sheet').onclick = () => exportCharacterSheet(char);
}

function renderBasicTab(el, char, save) {
  el.innerHTML = `
    <div class="card">
      <div style="display:flex;gap:14px;align-items:flex-start">
        <div style="flex-shrink:0">
          <div id="portrait-box" style="
            width:100px;height:133px;border-radius:8px;border:2px dashed var(--border);
            background:var(--bg3);display:flex;flex-direction:column;align-items:center;
            justify-content:center;cursor:pointer;overflow:hidden;position:relative;
          ">
            ${char.portrait
              ? `<img src="${char.portrait}" style="width:100%;height:100%;object-fit:cover">`
              : `<div style="text-align:center;color:var(--text3);font-size:11px;pointer-events:none">
                   <div style="font-size:26px;margin-bottom:4px">🖼</div>
                   <div>초상화<br>클릭 업로드</div>
                 </div>`
            }
          </div>
          <input id="portrait-input" type="file" accept="image/*" style="display:none">
          <p style="font-size:10px;color:var(--text3);text-align:center;margin-top:4px">100×133px</p>
        </div>
        <div style="flex:1;min-width:0">
          <div class="field"><label>캐릭터 이름</label><input id="c-name" value="${escHtml(char.name||'')}"></div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px">
            <div class="field"><label>나이</label><input id="c-age" value="${escHtml(char.age||'')}"></div>
            <div class="field"><label>직업</label><input id="c-occ" value="${escHtml(char.occupation||'')}"></div>
            <div class="field"><label>성별</label><input id="c-gen" value="${escHtml(char.gender||'')}"></div>
            <div class="field"><label>행운</label><input id="c-luck" type="number" value="${char.luck||50}" min="0" max="99"></div>
          </div>
        </div>
      </div>
      <div class="field" style="margin-top:8px"><label>배경 이야기</label><textarea id="c-bg" style="min-height:60px">${escHtml(char.backstory||'')}</textarea></div>
      <div class="field"><label>고유 스킬</label><input id="c-skill" placeholder="예: 오컬트 지식, 탐정 직감..." value="${escHtml(char.uniqueSkill||'')}"></div>
    </div>

    <div class="card">
      <h2>기본 바 스텟</h2>
      ${renderFixedBar('hp', 'HP', '#ef4444', char.hp)}
      ${renderFixedBar('mp', 'MP', '#60a5fa', char.mp)}
      ${renderFixedBar('san', '이성(SAN)', '#a3e635', char.san)}
    </div>

    <div class="card" id="custom-bars-card">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px">
        <h2 style="margin:0">커스텀 바 스텟</h2>
        <button class="btn btn-secondary btn-sm" id="btn-show-add-bar">+ 바 추가</button>
      </div>
      <div id="custom-bars-list"></div>
      <div id="add-bar-form" style="display:none;margin-top:12px;padding:12px;background:var(--bg);border-radius:8px;border:1px solid var(--border)">
        <div style="display:flex;gap:8px;margin-bottom:8px;flex-wrap:wrap">
          <div class="field" style="flex:2;margin:0;min-width:100px"><label>스텟 이름</label><input id="new-bar-name" placeholder="예: 기력, 애정도..."></div>
          <div class="field" style="width:75px;margin:0"><label>최대값</label><input id="new-bar-max" type="number" value="100" min="1"></div>
          <div class="field" style="width:75px;margin:0"><label>현재값</label><input id="new-bar-cur" type="number" value="100" min="0"></div>
        </div>
        <div style="margin-bottom:10px">
          <label style="display:block;margin-bottom:6px;font-size:12px">색상 선택</label>
          <div style="display:flex;gap:6px;flex-wrap:wrap" id="color-presets">
            ${COLOR_PRESETS.map((c, i) => `
              <div class="color-preset-btn${i===6?' selected':''}" data-color="${c}"
                style="width:26px;height:26px;border-radius:6px;background:${c};cursor:pointer;
                border:2px solid ${i===6?'white':'transparent'};flex-shrink:0"></div>
            `).join('')}
          </div>
        </div>
        <div style="display:flex;gap:8px">
          <button class="btn btn-secondary btn-sm" id="btn-cancel-add-bar">취소</button>
          <button class="btn btn-primary flex-1" id="btn-confirm-add-bar">추가</button>
        </div>
      </div>
    </div>
  `;

  // Portrait upload
  const portraitBox = el.querySelector('#portrait-box');
  const portraitInput = el.querySelector('#portrait-input');
  portraitBox.onclick = () => portraitInput.click();
  portraitInput.onchange = e => {
    const file = e.target.files[0];
    if (!file) return;
    const fr = new FileReader();
    fr.onload = ev => {
      char.portrait = ev.target.result;
      portraitBox.innerHTML = `<img src="${ev.target.result}" style="width:100%;height:100%;object-fit:cover">`;
      save();
    };
    fr.readAsDataURL(file);
  };

  // Field bindings
  const fieldMap = { name:'name', age:'age', occ:'occupation', gen:'gender', bg:'backstory', skill:'uniqueSkill' };
  for (const [f, key] of Object.entries(fieldMap)) {
    const inp = el.querySelector(`#c-${f}`);
    if (inp) inp.oninput = () => { char[key] = inp.value; save(); };
  }
  const luckInp = el.querySelector('#c-luck');
  if (luckInp) luckInp.oninput = () => { char.luck = parseInt(luckInp.value)||0; save(); };

  // Fixed bar bindings
  ['hp','mp','san'].forEach(stat => {
    const cur = el.querySelector(`#${stat}-cur`);
    const max = el.querySelector(`#${stat}-max`);
    const fill = el.querySelector(`#${stat}-fill`);
    if (!cur) return;
    const update = () => {
      char[stat].current = parseInt(cur.value)||0;
      char[stat].max = parseInt(max.value)||1;
      fill.style.width = Math.min(100, char[stat].current / char[stat].max * 100) + '%';
      save();
    };
    cur.oninput = update; max.oninput = update;
  });

  // Custom bars
  function renderCustomBars() {
    const list = el.querySelector('#custom-bars-list');
    if (!char.customBars.length) {
      list.innerHTML = '<p class="text-muted text-sm">추가된 스텟이 없습니다</p>';
      return;
    }
    list.innerHTML = char.customBars.map(bar => `
      <div class="bar-row" data-bar-id="${bar.id}" style="margin-bottom:10px;align-items:center">
        <div style="width:14px;height:14px;border-radius:4px;background:${bar.color};flex-shrink:0;border:1px solid rgba(255,255,255,0.2)"></div>
        <input class="cb-name" value="${escHtml(bar.name)}" style="width:80px;font-size:13px;font-weight:bold;color:var(--accent3)">
        <input class="cb-cur" type="number" value="${bar.current}" min="0" style="width:55px;text-align:center">
        <span style="font-size:12px;color:var(--text3)"> / </span>
        <input class="cb-max" type="number" value="${bar.max}" min="1" style="width:55px;text-align:center">
        <div class="bar-track" style="flex:1">
          <div class="cb-fill bar-fill" style="background:${bar.color};width:${Math.min(100, bar.current/Math.max(bar.max,1)*100)}%"></div>
        </div>
        <button class="btn btn-danger btn-sm cb-del" style="padding:3px 8px;flex-shrink:0">✕</button>
      </div>
    `).join('');

    list.querySelectorAll('[data-bar-id]').forEach(row => {
      const bar = char.customBars.find(b => b.id === row.dataset.barId);
      if (!bar) return;
      const curInp = row.querySelector('.cb-cur');
      const maxInp = row.querySelector('.cb-max');
      const nameInp = row.querySelector('.cb-name');
      const fill = row.querySelector('.cb-fill');
      nameInp.oninput = () => { bar.name = nameInp.value; save(); };
      curInp.oninput = () => {
        bar.current = parseInt(curInp.value)||0;
        fill.style.width = Math.min(100, bar.current/Math.max(bar.max,1)*100)+'%';
        save();
      };
      maxInp.oninput = () => {
        bar.max = parseInt(maxInp.value)||1;
        fill.style.width = Math.min(100, bar.current/Math.max(bar.max,1)*100)+'%';
        save();
      };
      row.querySelector('.cb-del').onclick = () => {
        if (!confirm(`'${bar.name}' 스텟을 삭제할까요?`)) return;
        char.customBars = char.customBars.filter(b => b.id !== bar.id);
        save(); renderCustomBars();
      };
    });
  }
  renderCustomBars();

  // Add bar form
  let selectedColor = COLOR_PRESETS[6];
  el.querySelectorAll('.color-preset-btn').forEach(btn => {
    btn.onclick = () => {
      el.querySelectorAll('.color-preset-btn').forEach(b => {
        b.style.borderColor = 'transparent'; b.classList.remove('selected');
      });
      btn.style.borderColor = 'white'; btn.classList.add('selected');
      selectedColor = btn.dataset.color;
    };
  });
  el.querySelector('#btn-show-add-bar').onclick = () => {
    el.querySelector('#add-bar-form').style.display = '';
    el.querySelector('#new-bar-name').focus();
  };
  el.querySelector('#btn-cancel-add-bar').onclick = () => {
    el.querySelector('#add-bar-form').style.display = 'none';
  };
  el.querySelector('#btn-confirm-add-bar').onclick = () => {
    const name = el.querySelector('#new-bar-name').value.trim();
    const max = parseInt(el.querySelector('#new-bar-max').value) || 100;
    const current = parseInt(el.querySelector('#new-bar-cur').value) ?? max;
    if (!name) { toast('스텟 이름을 입력하세요'); return; }
    char.customBars.push({ id: Utils.genId(), name, current, max, color: selectedColor });
    save(); renderCustomBars();
    el.querySelector('#add-bar-form').style.display = 'none';
    el.querySelector('#new-bar-name').value = '';
    el.querySelector('#new-bar-max').value = '100';
    el.querySelector('#new-bar-cur').value = '100';
  };
}

function renderFixedBar(id, label, color, data) {
  const pct = Math.min(100, data.current / Math.max(data.max, 1) * 100);
  return `
    <div class="bar-row" style="margin-bottom:8px">
      <span class="bar-label" style="font-weight:bold;color:${color};min-width:70px">${label}</span>
      <input id="${id}-cur" type="number" value="${data.current}" min="0" style="width:58px;text-align:center">
      <span style="font-size:12px;color:var(--text3)"> / </span>
      <input id="${id}-max" type="number" value="${data.max}" min="1" style="width:58px;text-align:center">
      <div class="bar-track" style="flex:1">
        <div id="${id}-fill" class="bar-fill" style="background:${color};width:${pct}%"></div>
      </div>
    </div>
  `;
}

function renderStatsTab(el, char, save) {
  function renderStats() {
    el.innerHTML = `
      <div class="card">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px">
          <h2 style="margin:0">기본 능력치</h2>
          <button class="btn btn-secondary btn-sm" id="btn-add-stat">+ 능력치 추가</button>
        </div>
        <div id="stat-grid" class="stat-grid">
          ${char.statList.map(s => `
            <div class="stat-box" data-stat-key="${escHtml(s.key)}" style="position:relative;padding-top:18px">
              <button class="stat-del-btn" data-key="${escHtml(s.key)}" title="삭제"
                style="position:absolute;top:3px;right:3px;background:rgba(239,68,68,0.15);
                border:none;color:#ef4444;cursor:pointer;font-size:10px;padding:1px 5px;
                border-radius:3px;line-height:1.4">✕</button>
              <input class="stat-label-inp" value="${escHtml(s.label)}"
                style="width:100%;font-size:11px;text-align:center;background:transparent;border:none;
                border-bottom:1px solid var(--border);color:var(--text2);margin-bottom:6px;padding:0">
              <input class="stat-val-inp" type="number" value="${s.value}" min="0" max="999">
            </div>
          `).join('')}
        </div>

        <div id="add-stat-form" style="display:none;margin-top:12px;padding:10px;background:var(--bg);border-radius:8px;border:1px solid var(--border)">
          <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:flex-end">
            <div class="field" style="flex:2;margin:0;min-width:120px">
              <label>이름</label><input id="new-stat-label" placeholder="예: 행운 LUK, 감각 SEN...">
            </div>
            <div class="field" style="width:70px;margin:0">
              <label>초기값</label><input id="new-stat-val" type="number" value="10" min="0">
            </div>
            <button class="btn btn-secondary btn-sm" id="btn-cancel-stat">취소</button>
            <button class="btn btn-primary btn-sm" id="btn-confirm-stat">추가</button>
          </div>
        </div>
      </div>

      <div class="card">
        <h2>스킬 목록</h2>
        <textarea id="c-skills" style="min-height:140px;font-size:13px;font-family:monospace"
          placeholder="스킬명: 레벨&#10;예: 도서관 조사: 60&#10;심리학: 40&#10;은닉: 35">${escHtml(char.skills||'')}</textarea>
      </div>
    `;

    el.querySelectorAll('[data-stat-key]').forEach(box => {
      const key = box.dataset.statKey;
      const stat = char.statList.find(s => s.key === key);
      if (!stat) return;
      box.querySelector('.stat-label-inp').oninput = e => { stat.label = e.target.value; save(); };
      box.querySelector('.stat-val-inp').oninput = e => { stat.value = parseInt(e.target.value)||0; save(); };
      box.querySelector('.stat-del-btn').onclick = () => {
        if (!confirm(`'${stat.label}' 능력치를 삭제할까요?`)) return;
        char.statList = char.statList.filter(s => s.key !== key);
        save(); renderStats();
      };
    });

    el.querySelector('#c-skills').oninput = e => { char.skills = e.target.value; save(); };

    el.querySelector('#btn-add-stat').onclick = () => {
      el.querySelector('#add-stat-form').style.display = '';
      el.querySelector('#new-stat-label').focus();
    };
    el.querySelector('#btn-cancel-stat').onclick = () => {
      el.querySelector('#add-stat-form').style.display = 'none';
    };
    el.querySelector('#btn-confirm-stat').onclick = () => {
      const label = el.querySelector('#new-stat-label').value.trim();
      const value = parseInt(el.querySelector('#new-stat-val').value) || 10;
      if (!label) { toast('이름을 입력하세요'); return; }
      char.statList.push({ key: 'c_' + Utils.genId().slice(0,6), label, value });
      save(); renderStats();
    };
  }
  renderStats();
}

const BODY_MODELS = {
  male: {
    label: '남자',
    image: 'KakaoTalk_20260622_225219722.png',
    aspect: '632 / 726',
    parts: [
      { key:'head',  label:'머리',    lp:'50%', tp:'11%' },
      { key:'lhand', label:'왼손',    lp:'12%', tp:'18%' },
      { key:'larm',  label:'왼팔',    lp:'29%', tp:'25%' },
      { key:'rarm',  label:'오른팔',  lp:'71%', tp:'25%' },
      { key:'rhand', label:'오른손',  lp:'88%', tp:'18%' },
      { key:'torso', label:'몸통',    lp:'50%', tp:'34%' },
      { key:'lleg',  label:'왼다리',  lp:'42%', tp:'69%' },
      { key:'rleg',  label:'오른다리',lp:'58%', tp:'69%' },
      { key:'lfoot', label:'왼발',    lp:'36%', tp:'95%' },
      { key:'rfoot', label:'오른발',  lp:'64%', tp:'95%' },
    ]
  },
  female: {
    label: '여자',
    image: 'KakaoTalk_20260622_225219722_01.png',
    aspect: '624 / 746',
    parts: [
      { key:'head',  label:'머리',    lp:'50%', tp:'12%' },
      { key:'lhand', label:'왼손',    lp:'12%', tp:'21%' },
      { key:'larm',  label:'왼팔',    lp:'28%', tp:'27%' },
      { key:'rarm',  label:'오른팔',  lp:'72%', tp:'27%' },
      { key:'rhand', label:'오른손',  lp:'88%', tp:'21%' },
      { key:'torso', label:'몸통',    lp:'50%', tp:'34%' },
      { key:'lleg',  label:'왼다리',  lp:'42%', tp:'70%' },
      { key:'rleg',  label:'오른다리',lp:'58%', tp:'70%' },
      { key:'lfoot', label:'왼발',    lp:'35%', tp:'96%' },
      { key:'rfoot', label:'오른발',  lp:'65%', tp:'96%' },
    ]
  }
};

function renderBodyTab(el, char, save) {
  function renderParts() {
    const model = BODY_MODELS[char.bodyModel] || BODY_MODELS.male;
    el.innerHTML = `
      <div class="card">
        <h2>신체 피해 상태</h2>
        <p class="text-sm text-muted mb-8" style="text-align:center">부위를 클릭해 피해 단계를 올립니다</p>
        <div style="display:flex;gap:6px;justify-content:center;margin-bottom:10px">
          ${Object.entries(BODY_MODELS).map(([key, data]) => `
            <button class="btn btn-sm ${char.bodyModel === key ? 'btn-primary' : 'btn-secondary'} body-model-btn" data-model="${key}">
              ${data.label}
            </button>
          `).join('')}
        </div>

        <div style="position:relative;width:100%;max-width:430px;aspect-ratio:${model.aspect};margin:0 auto;overflow:visible">
          <img src="${model.image}" alt="${model.label} 신체" style="position:absolute;inset:0;width:100%;height:100%;object-fit:contain;pointer-events:none">
          ${model.parts.map(p => {
            const lvl = char.bodyDamage[p.key] || 0;
            const color = DMG_COLORS[lvl];
            const bg = lvl === 0
              ? 'rgba(30,30,50,0.78)'
              : `rgba(${hexToRgb(color)},0.18)`;
            return `
              <div class="body-icon-btn" data-part="${p.key}" style="
                position:absolute;left:${p.lp};top:${p.tp};
                transform:translate(-50%,-50%);
                background:${bg};
                border:2px solid ${color};
                border-radius:6px;padding:4px 8px;
                min-width:54px;
                text-align:center;cursor:pointer;
                white-space:nowrap;user-select:none;
                transition:border-color 0.15s,background 0.15s;
                z-index:1;
              ">
                <div class="bi-label" style="font-size:13px;font-weight:bold;color:${color};line-height:1.3">${p.label}</div>
                <div class="bi-dmg" style="font-size:11px;color:${color};opacity:0.85;line-height:1.2">${DMG_LEVELS[lvl]}</div>
              </div>
            `;
          }).join('')}
        </div>

        <!-- Damage level legend -->
        <div style="display:flex;gap:5px;flex-wrap:wrap;justify-content:center;margin-top:10px">
          ${DMG_LEVELS.map((name, i) => `
            <span style="font-size:10px;padding:2px 7px;border-radius:10px;
              background:rgba(0,0,0,0.2);border:1px solid ${DMG_COLORS[i]};
              color:${DMG_COLORS[i]}">${name}</span>
          `).join('')}
        </div>

        <button class="btn btn-secondary btn-sm" id="btn-reset-body" style="margin-top:10px;width:100%">
          모든 부위 초기화
        </button>
      </div>

      <div class="card">
        <h2>상태이상</h2>
        <div id="status-tags" class="status-tags"></div>
        <div style="margin-top:12px">
          <select id="status-select">
            <option value="">상태이상 추가...</option>
            ${STATUS_LIST.map(s => `<option value="${s}">${s}</option>`).join('')}
          </select>
        </div>
      </div>
    `;

    el.querySelectorAll('.body-model-btn').forEach(btn => {
      btn.onclick = () => {
        char.bodyModel = btn.dataset.model;
        save();
        renderParts();
      };
    });

    // Icon click — cycle damage level
    el.querySelectorAll('.body-icon-btn').forEach(btn => {
      btn.onclick = () => {
        const p = btn.dataset.part;
        char.bodyDamage[p] = ((char.bodyDamage[p] || 0) + 1) % DMG_LEVELS.length;
        const lvl = char.bodyDamage[p];
        const color = DMG_COLORS[lvl];
        const bg = lvl === 0 ? 'rgba(30,30,50,0.7)' : `rgba(${hexToRgb(color)},0.18)`;
        btn.style.borderColor = color;
        btn.style.background = bg;
        btn.querySelector('.bi-label').style.color = color;
        btn.querySelector('.bi-dmg').style.color = color;
        btn.querySelector('.bi-dmg').textContent = DMG_LEVELS[lvl];
        save();
      };
    });

    el.querySelector('#btn-reset-body').onclick = () => {
      BODY_PARTS.forEach(p => { char.bodyDamage[p.key] = 0; });
      save(); renderParts();
    };

    function renderStatusTags() {
      const tags = el.querySelector('#status-tags');
      tags.innerHTML = char.statusEffects.map(s => `
        <span class="status-tag active">
          ${s} <span class="remove-status" data-s="${s}">×</span>
        </span>
      `).join('') || '<span class="text-muted text-sm">없음</span>';
      tags.querySelectorAll('.remove-status').forEach(btn => {
        btn.onclick = () => {
          char.statusEffects = char.statusEffects.filter(x => x !== btn.dataset.s);
          save(); renderStatusTags();
        };
      });
    }

    el.querySelector('#status-select').onchange = e => {
      const val = e.target.value;
      if (val && !char.statusEffects.includes(val)) {
        char.statusEffects.push(val); save(); renderStatusTags();
      }
      e.target.value = '';
    };
    renderStatusTags();
  }
  renderParts();
}

function hexToRgb(hex) {
  const r = parseInt(hex.slice(1,3),16);
  const g = parseInt(hex.slice(3,5),16);
  const b = parseInt(hex.slice(5,7),16);
  return `${r},${g},${b}`;
}

function renderNotesTab(el, char, save) {
  el.innerHTML = `
    <div class="card">
      <h2>메모</h2>
      <textarea id="c-notes" style="min-height:220px"
        placeholder="자유롭게 메모를 작성하세요...">${escHtml(char.notes||'')}</textarea>
    </div>
  `;
  el.querySelector('#c-notes').oninput = e => { char.notes = e.target.value; save(); };
}

function exportCharacterSheet(char) {
  const statPairs = [];
  const sl = char.statList || [];
  for (let i = 0; i < sl.length; i += 2) {
    const a = sl[i], b = sl[i+1];
    statPairs.push(`<tr>
      <td class="lbl">${escHtml(a.label)}</td><td class="val">${a.value}</td>
      ${b ? `<td class="lbl">${escHtml(b.label)}</td><td class="val">${b.value}</td>` : '<td></td><td></td>'}
    </tr>`);
  }

  const allBars = [
    { label:'HP', data: char.hp, color:'#dc2626' },
    { label:'이성(SAN)', data: char.san, color:'#16a34a' },
    { label:'MP', data: char.mp, color:'#2563eb' },
    ...(char.customBars||[]).map(b => ({ label: b.name, data: { current: b.current, max: b.max }, color: b.color }))
  ];

  const leftParts = BODY_PARTS.slice(0, 5);
  const rightParts = BODY_PARTS.slice(5);

  const win = window.open('', '_blank', 'width=820,height=960');
  if (!win) { toast('팝업 차단을 해제해주세요'); return; }
  win.document.write(`<!DOCTYPE html>
<html><head><meta charset="UTF-8"><title>${escHtml(char.name||'캐릭터')} 시트</title>
<style>
  *{box-sizing:border-box;margin:0;padding:0}
  body{font-family:'Malgun Gothic','Apple SD Gothic Neo',sans-serif;background:#f0ede8;color:#1a1a1a;padding:20px}
  .sheet{max-width:700px;margin:0 auto;background:#fff;padding:28px;border-radius:10px;box-shadow:0 4px 16px rgba(0,0,0,.12)}
  h1{text-align:center;color:#4c1d95;font-size:20px;border-bottom:3px solid #4c1d95;padding-bottom:10px;margin-bottom:18px}
  .header{display:flex;gap:16px;margin-bottom:16px}
  .portrait{width:100px;height:133px;border:2px solid #bbb;border-radius:6px;overflow:hidden;flex-shrink:0;display:flex;align-items:center;justify-content:center;background:#e5e5e5;font-size:11px;color:#888;text-align:center}
  .portrait img{width:100%;height:100%;object-fit:cover}
  .info-grid{display:grid;grid-template-columns:auto 1fr auto 1fr;gap:5px 10px;font-size:13px;align-content:start}
  .lbl{color:#555;font-weight:bold;white-space:nowrap}
  .sec-title{background:#4c1d95;color:#fff;padding:5px 12px;border-radius:5px;font-size:13px;font-weight:bold;margin:14px 0 8px}
  table{width:100%;border-collapse:collapse;font-size:13px}
  td{padding:5px 8px;border:1px solid #ddd}
  td.lbl{color:#555;font-weight:bold;background:#f9f9f9;width:30%}
  td.val{font-weight:bold;text-align:center;width:12%;color:#4c1d95}
  .bars{display:flex;flex-wrap:wrap;gap:10px}
  .bar-item{flex:1;min-width:130px}
  .bar-lbl{font-size:12px;color:#555;margin-bottom:2px}
  .bar-val{font-weight:bold;font-size:15px}
  .bar-track{height:9px;background:#e5e5e5;border-radius:4px;margin-top:3px;overflow:hidden}
  .bar-fill{height:100%;border-radius:4px}
  .skills-box{white-space:pre-wrap;font-size:12px;border:1px solid #ddd;padding:8px;border-radius:4px;min-height:50px;background:#fafafa}
  .status-box{display:flex;flex-wrap:wrap;gap:4px}
  .stag{background:#fee2e2;color:#991b1b;padding:2px 8px;border-radius:10px;font-size:12px}
  .backstory{font-size:12px;color:#444;margin-top:6px;line-height:1.6}
  .print-btn{display:block;margin:20px auto 0;padding:10px 28px;background:#4c1d95;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:bold}
  @media print{.print-btn{display:none}body{padding:0;background:#fff}.sheet{box-shadow:none;border-radius:0;max-width:100%}}
</style></head><body>
<div class="sheet">
  <h1>📜 캐릭터 시트</h1>
  <div class="header">
    <div class="portrait">${char.portrait?`<img src="${char.portrait}">`:'초상화 없음'}</div>
    <div style="flex:1">
      <div class="info-grid">
        <span class="lbl">이름</span><span>${escHtml(char.name||'—')}</span>
        <span class="lbl">나이</span><span>${escHtml(char.age||'—')}</span>
        <span class="lbl">직업</span><span>${escHtml(char.occupation||'—')}</span>
        <span class="lbl">성별</span><span>${escHtml(char.gender||'—')}</span>
        <span class="lbl">행운</span><span>${char.luck}</span>
        <span class="lbl">고유스킬</span><span>${escHtml(char.uniqueSkill||'—')}</span>
      </div>
      ${char.backstory?`<div class="backstory">${escHtml(char.backstory)}</div>`:''}
    </div>
  </div>

  <div class="sec-title">바 스텟</div>
  <div class="bars">
    ${allBars.map(b=>`
      <div class="bar-item">
        <div class="bar-lbl">${escHtml(b.label)}</div>
        <div class="bar-val" style="color:${b.color}">${b.data.current} / ${b.data.max}</div>
        <div class="bar-track"><div class="bar-fill" style="width:${Math.min(100,b.data.current/Math.max(b.data.max,1)*100)}%;background:${b.color}"></div></div>
      </div>
    `).join('')}
  </div>

  <div class="sec-title">기본 능력치</div>
  <table>${statPairs.join('')}</table>

  <div class="sec-title">신체 피해</div>
  <div style="display:flex;gap:8px">
    <table style="flex:1">
      ${leftParts.map(p=>{const l=char.bodyDamage[p.key]||0;return`<tr><td class="lbl">${p.label}</td><td style="color:${DMG_COLORS[l]};font-weight:bold;text-align:center">${DMG_LEVELS[l]}</td></tr>`;}).join('')}
    </table>
    <table style="flex:1">
      ${rightParts.map(p=>{const l=char.bodyDamage[p.key]||0;return`<tr><td class="lbl">${p.label}</td><td style="color:${DMG_COLORS[l]};font-weight:bold;text-align:center">${DMG_LEVELS[l]}</td></tr>`;}).join('')}
    </table>
  </div>

  ${char.skills?`<div class="sec-title">스킬</div><div class="skills-box">${escHtml(char.skills)}</div>`:''}
  ${char.statusEffects&&char.statusEffects.length?`<div class="sec-title">상태이상</div><div class="status-box">${char.statusEffects.map(s=>`<span class="stag">${s}</span>`).join('')}</div>`:''}
  ${char.notes?`<div class="sec-title">메모</div><div class="skills-box">${escHtml(char.notes)}</div>`:''}

  <button class="print-btn" onclick="window.print()">🖨 인쇄 / PDF 저장</button>
</div></body></html>`);
  win.document.close();
}

function escHtml(str) {
  return String(str||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

Router.register('/character', renderCharacter);
