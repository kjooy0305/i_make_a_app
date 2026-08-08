// Parse script text into scene array
// Supports editor format and exported pure text:
//   #1# [color:2] text
//   > [goto:2] [need:key] [sets:key1,key2] [needStat:hp] [needOp:>=] [needVal:5] [needItem:key] [giveItem:key] [consumeItem] choice
//   > choice text -> #2#
function parseScript(text) {
  const lines = text.split('\n');
  const scenes = [];
  let current = null;
  const pushCurrent = () => {
    if (!current) return;
    current.text = String(current.text || '').trimEnd();
    scenes.push(current);
  };

  for (const line of lines) {
    if (!current && line.match(/^===.*===$/)) continue;
    const sceneMatch = line.match(/^#(\d+)#(?:\s*\[color:(\d)\])?(.*)$/);
    if (sceneMatch) {
      pushCurrent();
      const meta = sceneMatch[3] || '';
      const sceneText = meta.replace(/\s*\[flags:[^\]]+\]/g, '').trim();
      current = {
        num: parseInt(sceneMatch[1]),
        color: parseInt(sceneMatch[2] || '1'),
        text: sceneText,
        choices: []
      };
    } else if (current && line.match(/^>\s*\[goto:(\d+)\](.*)$/)) {
      const m = line.match(/^>\s*\[goto:(\d+)\](.*)$/);
      const meta = m[2] || '';
      const need = meta.match(/\[need:([^\]]+)\]/);
      const sets = meta.match(/\[sets:([^\]]+)\]/);
      const needStat = meta.match(/\[needStat:([^\]]+)\]/);
      const needOp = meta.match(/\[needOp:([^\]]+)\]/);
      const needVal = meta.match(/\[needVal:([^\]]+)\]/);
      const needItem = meta.match(/\[needItem:([^\]]+)\]/);
      const giveItem = meta.match(/\[giveItem:([^\]]+)\]/);
      const consumeItem = /\[consumeItem\]/.test(meta);
      const text = meta.replace(/\s*\[(?:need|sets|needStat|needOp|needVal|needItem|giveItem):[^\]]+\]/g, '').replace(/\s*\[consumeItem\]/g, '').trim();
      current.choices.push({
        goto: parseInt(m[1]),
        need: need ? need[1] : null,
        sets: sets ? splitList(sets[1]) : [],
        needStatKey: needStat ? needStat[1] : null,
        needStatOp: needOp ? needOp[1] : null,
        needStatValue: needVal ? Number(needVal[1]) : null,
        needItem: needItem ? needItem[1] : null,
        giveItem: giveItem ? giveItem[1] : null,
        consumeItem,
        text
      });
    } else if (current && line.match(/^>\s*(.+?)\s*(?:→|->)\s*#(\d+)#\s*$/)) {
      const m = line.match(/^>\s*(.+?)\s*(?:→|->)\s*#(\d+)#\s*$/);
      current.choices.push({
        goto: parseInt(m[2]),
        need: null,
        sets: [],
        text: m[1].trim()
      });
    } else if (current) {
      current.text += (current.text ? '\n' : '') + line;
    }
  }
  pushCurrent();
  return scenes;
}

function splitMetadata(text) {
  const markers = [
    '=== MAKE123_EDITOR_SETTINGS ===',
    '=== MAKE123_TRIGGERS ===',
    '=== MAKE123_ITEMS ==='
  ];
  const source = String(text || '');
  let cut = source.length;
  for (const marker of markers) {
    const index = source.indexOf(marker);
    if (index >= 0) cut = Math.min(cut, index);
  }
  return { scriptText: source.slice(0, cut).trimEnd(), metaText: source.slice(cut) };
}

function readJsonSection(metaText, marker, fallback) {
  const start = String(metaText || '').indexOf(marker);
  if (start < 0) return fallback;
  const rest = metaText.slice(start + marker.length);
  const next = rest.search(/\n=== MAKE123_[A-Z_]+ ===/);
  const raw = (next >= 0 ? rest.slice(0, next) : rest).trim();
  if (!raw) return fallback;
  try {
    return JSON.parse(raw);
  } catch (err) {
    console.warn(`Failed to parse ${marker}`, err);
    return fallback;
  }
}

function extractScenarioMetadata(text) {
  const split = splitMetadata(text);
  return {
    scriptText: split.scriptText,
    editorSettings: readJsonSection(split.metaText, '=== MAKE123_EDITOR_SETTINGS ===', null),
    triggers: readJsonSection(split.metaText, '=== MAKE123_TRIGGERS ===', []),
    items: readJsonSection(split.metaText, '=== MAKE123_ITEMS ===', [])
  };
}

function splitList(value) {
  return String(value || '')
    .split(',')
    .map(item => item.trim())
    .filter(Boolean);
}

function getTitleFromScript(text, fallback = '가져온 시나리오') {
  const firstTitle = String(text || '').split('\n').find(line => line.match(/^===\s*.+?\s*===$/));
  if (!firstTitle) return fallback;
  return firstTitle.replace(/^===\s*/, '').replace(/\s*===$/, '').trim() || fallback;
}

function getSceneNum(scene) {
  const match = String(scene?.num ?? '').match(/\d+/);
  return match ? Number(match[0]) : Number.MAX_SAFE_INTEGER;
}

function sortScenesByNum(scenes) {
  return [...(scenes || [])]
    .map((scene, index) => ({ scene, index }))
    .sort((a, b) => {
      const diff = getSceneNum(a.scene) - getSceneNum(b.scene);
      return diff || a.index - b.index;
    })
    .map(item => item.scene);
}

// Export as pure text (no color/format metadata)
function scenesToPureText(title, scenes) {
  let out = `=== ${title} ===\n\n`;

  for (const s of sortScenesByNum(scenes)) {
    out += `#${s.num}#\n`;
    out += `${s.text || ''}\n`;

    for (const c of s.choices || []) {
      out += `> ${c.text} → #${c.goto}#\n`;
    }

    out += '\n';
  }

  return out;
}

function exportPureText(scenario) {
  const scenes = Array.isArray(scenario.scenes)
    ? scenario.scenes
    : parseScript(scenario.script || '');

  return scenesToPureText(scenario.title, scenes);
}

function createScenarioFromText(text, filename = '') {
  const meta = extractScenarioMetadata(text);
  const scenes = parseScript(meta.scriptText).map(s => ({ ...s, collapsed: false }));
  if (!scenes.length) {
    throw new Error('SCENARIO_PARSE_EMPTY');
  }
  const titleFromFile = filename.replace(/\.[^.]+$/, '').trim();
  const title = getTitleFromScript(meta.scriptText, titleFromFile || '가져온 시나리오');
  const items = Array.isArray(meta.items) ? meta.items.filter(item => item && item.key && item.name) : [];
  const triggers = Array.isArray(meta.triggers) ? meta.triggers.filter(t => t && t.statKey && t.op && t.goto) : [];
  const editorSettings = {
    showTriggers: Boolean(meta.editorSettings?.showTriggers || triggers.length),
    showItems: Boolean(meta.editorSettings?.showItems || items.length),
    showChoiceConditions: Boolean(
      meta.editorSettings?.showChoiceConditions ||
      scenes.some(scene => (scene.choices || []).some(hasChoiceCondition))
    )
  };
  return {
    id: genId(),
    title,
    desc: `${new Date().toLocaleDateString('ko-KR')} 텍스트 파일에서 가져옴`,
    icon: '📥',
    script: scenesToScript(scenes),
    scenes,
    sortMode: 'num',
    rangeMin: Math.min(...scenes.map(s => s.num)),
    rangeMax: Math.max(...scenes.map(s => s.num)),
    triggers,
    items,
    editorSettings,
    createdAt: Date.now()
  };
}

function hasChoiceCondition(choice) {
  return Boolean(
    choice?.need ||
    (Array.isArray(choice?.sets) && choice.sets.length) ||
    choice?.needStatKey ||
    choice?.needItem ||
    choice?.giveItem ||
    choice?.consumeItem
  );
}

function scenesToScript(scenes) {
  return (scenes || []).map(s => {
    let line = `#${s.num}# [color:${s.color || 1}] ${s.text || ''}`;
    for (const c of s.choices || []) {
      line += `\n> [goto:${c.goto}]${c.need ? ' [need:' + c.need + ']' : ''}${Array.isArray(c.sets) && c.sets.length ? ' [sets:' + c.sets.join(',') + ']' : ''}${c.needStatKey ? ' [needStat:' + c.needStatKey + ']' : ''}${c.needStatOp ? ' [needOp:' + c.needStatOp + ']' : ''}${c.needStatValue !== null && c.needStatValue !== undefined ? ' [needVal:' + c.needStatValue + ']' : ''}${c.needItem ? ' [needItem:' + c.needItem + ']' : ''}${c.giveItem ? ' [giveItem:' + c.giveItem + ']' : ''}${c.consumeItem ? ' [consumeItem]' : ''} ${c.text || ''}`;
    }
    return line;
  }).join('\n\n');
}

function exportScenarioText(scenario) {
  const scenes = Array.isArray(scenario.scenes)
    ? scenario.scenes
    : parseScript(scenario.script || '');
  const settings = scenario.editorSettings || {};
  const hasTriggers = Array.isArray(scenario.triggers) && scenario.triggers.length > 0;
  const hasItems = Array.isArray(scenario.items) && scenario.items.length > 0;
  const hasConditions = scenes.some(scene => (scene.choices || []).some(hasChoiceCondition));
  const exportSettings = {
    showTriggers: Boolean(settings.showTriggers || hasTriggers),
    showItems: Boolean(settings.showItems || hasItems),
    showChoiceConditions: Boolean(settings.showChoiceConditions || hasConditions)
  };

  let out = `=== ${scenario.title || '시나리오'} ===\n\n${scenesToScript(scenes)}\n`;
  out += `\n=== MAKE123_EDITOR_SETTINGS ===\n${JSON.stringify(exportSettings, null, 2)}\n`;
  if (hasTriggers) {
    out += `\n=== MAKE123_TRIGGERS ===\n${JSON.stringify(scenario.triggers, null, 2)}\n`;
  }
  if (hasItems) {
    out += `\n=== MAKE123_ITEMS ===\n${JSON.stringify(scenario.items, null, 2)}\n`;
  }
  return out;
}

function downloadText(filename, text) {
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function getColorClass(color) {
  return `color-${color || 1}`;
}

function genId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 6);
}

function toast(msg, duration = 2000) {
  let el = document.querySelector('.toast');
  if (!el) {
    el = document.createElement('div');
    el.className = 'toast';
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.classList.add('show');
  clearTimeout(el._t);
  el._t = setTimeout(() => el.classList.remove('show'), duration);
}

function el(tag, attrs = {}, ...children) {
  const e = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === 'class') e.className = v;
    else if (k === 'style') Object.assign(e.style, v);
    else if (k.startsWith('on')) e.addEventListener(k.slice(2).toLowerCase(), v);
    else e.setAttribute(k, v);
  }
  for (const c of children) {
    if (c == null) continue;
    e.appendChild(typeof c === 'string' ? document.createTextNode(c) : c);
  }
  return e;
}

window.Utils = { parseScript, sortScenesByNum, scenesToPureText, exportPureText, exportScenarioText, createScenarioFromText, scenesToScript, downloadText, getColorClass, genId, toast, el };
window.toast = toast;
window.el = el;
