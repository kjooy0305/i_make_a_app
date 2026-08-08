'use strict';

const SOLO_CHARS = [
  { id:'jisu',    name:'김지수',   role:'어릴 적 친구', emoji:'🌸', color:'#f472b6',
    desc:'항상 곁에 있어준 따뜻한 친구. 위기에서 더 강해진다.',
    intro:'언제나처럼 환하게 웃고 있습니다. "또 이상한 꿈 꾸는 거야? 걱정 마, 같이 있을게."',
    talk:['\'사실 너한테 하고 싶은 말이 있었어.\' 지수는 조심스럽게 말을 꺼냅니다.',
          '\'어릴 때부터 항상 네 옆에 있고 싶었어. 꿈이니까 솔직하게 말할게.\''],
    help:'지수가 두 팔을 벌려 악몽의 시선을 끌어당깁니다. \'내가 막을게!\' 용감한 행동에 악몽이 멈칫합니다.',
    love:'\'꿈이라도... 이 감정은 진짜야.\'',
    hidden_end:'지수와 손을 맞잡고 눈을 감았습니다. 잠에서 깨어났을 때, 당신의 손 위에 지수의 손이 포개져 있었습니다. 꿈은 끝났지만, 이것은 시작이었습니다.',
  },
  { id:'seohyun', name:'이서현',   role:'연구원', emoji:'📚', color:'#60a5fa',
    desc:'차갑지만 분석력이 뛰어난 연구원. 비밀이 많다.',
    intro:'냉정하게 주변을 분석하고 있습니다. "꿈이야. 패닉하지 마. 논리적으로 출구가 있어."',
    talk:['\'꿈의 구조를 분석했어.\' 서현이 설명하다 멈춥니다. \'근데 솔직히, 지금 너 보는 게 더 좋아.\'',
          '\'효율적으로 말할게. 나는 오래전부터 너를 특별하게 생각해왔어.\''],
    help:'서현이 꿈의 논리를 분석해 악몽의 약점을 찾아냅니다. \'지금! 저기를 공격해!\'',
    love:'\'계산 다 했어. 변수는 너야. 내 감정의 변수.\'',
    hidden_end:'서현이 계산해낸 탈출 방정식. 그 마지막 변수는 당신이었습니다. 눈을 떴을 때 서현에게 메시지가 와 있었습니다. \'계산대로야. — 서현\'',
  },
  { id:'minjun',  name:'박민준',   role:'요리사', emoji:'🍳', color:'#fb923c',
    desc:'쾌활하고 낙천적인 요리사. 요리로 모두를 달랜다.',
    intro:'꿈 속에서도 코를 킁킁거립니다. "뭔가 요리 냄새 나지 않아? 주방 있으면 뭐든 만들어줄게!"',
    talk:['\'사실 나 너한테 요리 해주고 싶어서 항상 이유 찾았거든.\' 민준이 쑥스럽게 웃습니다.',
          '\'꿈이니까 솔직히 말해도 되지? 너랑 같이 밥 먹고 싶어서 매번 핑계 댔어.\''],
    help:'민준이 꿈 속 재료를 모아 악몽에 던집니다. 엉뚱하지만 효과적입니다.',
    love:'\'깨어나면 제일 맛있는 거 해줄게. 지금은... 이 순간이 제일 달콤하지만.\'',
    hidden_end:'민준의 손에서 뭔가 따뜻한 것이 전해졌습니다. 눈을 떴을 때 침대 옆에 도시락이 놓여 있었습니다. 메모가 붙어 있었습니다. \'잘 먹어. — 민준\'',
  },
  { id:'ara',     name:'최아라',   role:'화가', emoji:'🎨', color:'#a78bfa',
    desc:'신비롭고 조용한 화가. 꿈 속에서 유독 강해진다.',
    intro:'꿈 속 풍경을 경이롭게 바라봅니다. "여기... 아름다워. 그리고 싶어."',
    talk:['\'내가 그린 그림 중에 가장 많이 그린 게 뭔지 알아?\' 아라가 수줍게 웃습니다. \'너야.\'',
          '\'꿈이니까 보여줄 수 있어.\' 아라가 손을 들자, 허공에 당신의 얼굴이 그려집니다.'],
    help:'아라가 꿈의 공간을 그림으로 재구성합니다. 악몽이 미로 속에 갇혀 버립니다.',
    love:'\'현실에선 말 못 했는데, 꿈이니까 괜찮지?\' 아라가 당신의 손 위에 작은 꽃을 그립니다.',
    hidden_end:'아라가 그린 꿈과 현실을 잇는 통로. 잠에서 깨자 손등에 작은 꽃 모양 잉크 자국이 남아 있었습니다.',
  },
  { id:'hajun',   name:'정하준',   role:'경호원', emoji:'🛡️', color:'#94a3b8',
    desc:'과묵하고 신중한 보호자. 절대 포기하지 않는다.',
    intro:'이미 주변을 경계하며 서 있습니다. "파악 완료. 당신 옆에 있겠습니다."',
    talk:['\'항상 지키는 게 일이었는데...\' 하준이 잠시 말을 멈춥니다. \'당신을 지킬 때만 진심이에요.\'',
          '\'경호원이 이런 말 하면 안 되겠지만... 당신이 특별합니다. 오래전부터.\''],
    help:'하준이 몸으로 악몽을 막아섭니다. 흔들리지 않는 등이 믿음직스럽습니다.',
    love:'\'꿈이기 때문에 말할 수 있어요.\' 하준이 처음으로 당신의 눈을 똑바로 바라봅니다. \'좋아합니다.\'',
    hidden_end:'하준이 마지막까지 당신 곁에 있었습니다. 잠에서 깨자 문 앞에 하준의 재킷이 걸려 있었습니다. 문자 한 통이 와 있었습니다. \'돌아왔군요. — 하준\'',
  },
  { id:'yuri',    name:'한유리',   role:'간호사', emoji:'💊', color:'#34d399',
    desc:'밝고 친절한 치료사. 상처를 낫게 하는 손.',
    intro:'걱정스럽게 당신의 상태를 확인합니다. "다친 데 없어? 꿈이라도 상처는 남을 수 있어."',
    talk:['\'항상 남 챙기는데...\' 유리가 쓸쓸하게 웃습니다. \'나도 누군가한테 챙김 받고 싶어.\'',
          '\'그게 너였으면 좋겠다고, 오래전부터 생각해왔어.\' 유리가 조심스럽게 말합니다.'],
    help:'유리가 다친 이들을 치료하며 악몽의 분노를 다른 곳으로 돌립니다.',
    love:'\'마음도 치료해줄 수 있어. 내가.\' 유리가 살며시 당신의 손을 감쌉니다.',
    hidden_end:'유리와 함께 꿈에서 빠져나왔습니다. 눈을 떴을 때 유리가 옆에서 걱정스럽게 내려다보고 있었습니다. \'다행이야. 깨어났어.\'',
  },
  { id:'taeyang', name:'오태양',   role:'탐정', emoji:'🔍', color:'#fbbf24',
    desc:'의심 많지만 의리 있는 탐정. 단서를 잘 찾는다.',
    intro:'메모장을 꺼내 기록하고 있습니다. "꿈이라도 단서는 남아. 나가는 법 찾겠어."',
    talk:['\'탐정이라서 뭐든 의심하는데...\' 태양이 메모장을 덮습니다. \'너만큼은 한 번도 의심 안 해봤어.\'',
          '\'증거 다 모았어. 내가 너를 특별하게 생각한다는 증거.\''],
    help:'태양이 악몽의 행동 패턴을 분석합니다. \'3초 후에 왼쪽! 지금!\'',
    love:'\'증거가 충분해.\' 태양이 진지하게 말합니다. \'나는 너를 좋아해.\'',
    hidden_end:'태양이 찾아낸 탈출구. 단서는 처음부터 당신이었습니다. 눈을 떴을 때 손에 메모 한 장이 쥐어져 있었습니다. \'사건 해결. — 태양\'',
  },
  { id:'chaewon', name:'신채원',   role:'음악가', emoji:'🎵', color:'#e879f9',
    desc:'몽환적인 음악가. 꿈의 세계를 누구보다 잘 이해한다.',
    intro:'꿈 속 선율을 듣는 듯 눈을 감고 있습니다. "들려? 이 꿈은 노래를 갖고 있어."',
    talk:['\'너를 떠올리며 만든 곡이 있어.\' 채원이 조용히 말합니다. \'들어줄 수 있어?\'',
          '채원이 허공을 연주합니다. 아름다운 선율이 꿈 속을 채웁니다. 당신을 위한 곡입니다.'],
    help:'채원이 노래를 부릅니다. 꿈의 세계가 흔들리며 악몽이 주춤합니다.',
    love:'\'꿈이 끝나도 이 노래는 남아 있을 거야.\' 채원이 귓가에 조용히 노래합니다.',
    hidden_end:'채원의 노래가 꿈과 현실의 경계를 흐릿하게 만들었습니다. 잠에서 깨도 선율이 귓가에 남아 있었습니다. 그리고 채원의 목소리가 들렸습니다. \'일어났어?\'',
  },
];

const SOLO_HIDDEN_THRESHOLD = 65;

function soloGetLover(gs) {
  let best = null, bestVal = -1;
  for (const c of gs.companions) {
    const v = gs.affection[c.id] || 0;
    if (v > bestVal) { bestVal = v; best = c; }
  }
  return { char: best, val: bestVal };
}

function soloAffHtml(gs) {
  return gs.companions.map(c => {
    const v = gs.affection[c.id] || 0;
    return `<div style="flex:1;min-width:0;text-align:center">
      <div style="font-size:11px;color:${c.color};white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${c.emoji} ${c.name}</div>
      <div style="background:var(--bg3);border-radius:3px;height:5px;overflow:hidden;margin:2px 0">
        <div style="width:${v}%;height:100%;background:${c.color};transition:width 0.4s"></div>
      </div>
      <div style="font-size:10px;color:var(--text3)">${v}</div>
    </div>`;
  }).join('');
}

function soloStatHtml(gs) {
  if (!gs.char) return '';
  const { hp, san, mp, statusEffects } = gs.char;
  const bh = hp || { current: 10, max: 10 };
  const bs = san || { current: 50, max: 50 };
  const bm = mp || { current: 10, max: 10 };
  const sfx = (statusEffects || []).slice(0, 5);
  const bar = (label, color, cur, max) => {
    const pct = Math.min(100, cur / Math.max(max, 1) * 100);
    return `<div style="display:flex;align-items:center;gap:3px">
      <span style="font-size:10px;color:${color};font-weight:bold;white-space:nowrap">${label}</span>
      <div style="width:36px;background:var(--bg3);border-radius:2px;height:4px;flex-shrink:0;overflow:hidden">
        <div style="width:${pct}%;height:100%;background:${color}"></div>
      </div>
      <span style="font-size:9px;color:var(--text3);white-space:nowrap">${cur}/${max}</span>
    </div>`;
  };
  return `<div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;padding:4px 10px;background:var(--bg2);border-radius:6px;border:1px solid var(--border);margin-top:4px">
    ${bar('HP','#ef4444',bh.current,bh.max)}
    ${bar('SAN','#a3e635',bs.current,bs.max)}
    ${bar('MP','#60a5fa',bm.current,bm.max)}
    ${sfx.map(s=>`<span style="font-size:9px;padding:1px 5px;border-radius:8px;background:rgba(239,68,68,0.15);color:#ef4444;border:1px solid rgba(239,68,68,0.3);white-space:nowrap">${s}</span>`).join('')}
  </div>`;
}

function soloFinalHtml(gs) {
  return `<div style="margin-top:16px;padding:12px;background:var(--bg2);border-radius:8px">
    <div style="font-size:12px;color:var(--text3);margin-bottom:8px">최종 호감도</div>
    ${gs.companions.map(c => {
      const v = gs.affection[c.id] || 0;
      return `<div style="display:flex;align-items:center;gap:8px;margin-bottom:6px">
        <span style="color:${c.color};min-width:64px;font-size:13px">${c.emoji} ${c.name}</span>
        <div style="flex:1;background:var(--bg3);border-radius:4px;height:8px;overflow:hidden">
          <div style="width:${v}%;height:100%;background:${c.color};border-radius:4px"></div>
        </div>
        <span style="color:var(--text3);font-size:12px;min-width:24px;text-align:right">${v}</span>
      </div>`;
    }).join('')}
  </div>`;
}

const SOLO_SCENES = {
  intro: {
    title: '💤 꿈 속의 저택',
    text: gs => {
      const [c0, c1, c2] = gs.companions;
      return `<p>눈을 떴을 때, 당신은 낯선 곳에 서 있었습니다.</p>
      <p style="margin-top:8px">익숙한 저택의 윤곽이지만, 벽은 물처럼 흔들리고 창문 너머로는 보라빛 하늘에 별이 가득합니다. <strong style="color:var(--accent3)">꿈입니다.</strong></p>
      <p style="margin-top:8px">하지만 당신만이 아닙니다.</p>
      <div style="margin-top:12px;display:flex;flex-direction:column;gap:8px">
        ${[c0,c1,c2].map(c=>`<div style="padding:8px 12px;background:var(--bg2);border-left:3px solid ${c.color};border-radius:4px">
          <span style="color:${c.color};font-weight:bold">${c.emoji} ${c.name}</span>
          <div style="font-size:13px;color:var(--text2);margin-top:3px">${c.intro}</div>
        </div>`).join('')}
      </div>
      <p style="margin-top:12px;color:var(--text3);font-size:13px">이 꿈에서 나가는 방법을 찾아야 합니다.</p>`;
    },
    choices: gs => [
      { text: '"다 같이 탐색해보자"고 제안한다', goto:'hall',
        effect:()=>{ gs.companions.forEach(c=>{ gs.affection[c.id]+=5; }); } },
      { text: '혼자 조용히 주변을 살펴본다', goto:'hall' },
    ],
  },

  hall: {
    title: '중앙 홀',
    text: ()=>`<p>중앙 홀은 꿈 속에서도 웅장합니다. 촛불이 스스로 켜지고, 계단은 어디로도 이어지지 않습니다.</p>
      <p style="margin-top:8px">세 사람이 제각각 다른 곳을 바라보고 있습니다. 누구에게 먼저 다가갈까요?</p>`,
    choices: gs => {
      const [c0,c1,c2] = gs.companions;
      return [
        { text:`${c0.emoji} ${c0.name}에게 다가간다`, goto:'talk',
          effect:()=>{ gs.talkTarget=0; gs.affection[c0.id]+=10; } },
        { text:`${c1.emoji} ${c1.name}에게 다가간다`, goto:'talk',
          effect:()=>{ gs.talkTarget=1; gs.affection[c1.id]+=10; } },
        { text:`${c2.emoji} ${c2.name}에게 다가간다`, goto:'talk',
          effect:()=>{ gs.talkTarget=2; gs.affection[c2.id]+=10; } },
        { text:'도서관 쪽으로 혼자 향한다', goto:'library' },
      ];
    },
  },

  talk: {
    title: '단둘이',
    text: gs => {
      const c = gs.companions[gs.talkTarget];
      return `<p><span style="color:${c.color}">${c.name}</span>과 단둘이 이야기할 기회가 생겼습니다.</p>
      <div style="margin-top:10px;padding:12px;background:var(--bg2);border-left:3px solid ${c.color};border-radius:4px;font-size:13px;line-height:1.7">
        ${c.emoji} ${c.talk[0]}
      </div>
      <p style="margin-top:10px;font-size:14px;line-height:1.7">${c.talk[1]}</p>`;
    },
    choices: gs => {
      const c = gs.companions[gs.talkTarget];
      return [
        { text:'진심 어린 대답을 한다', goto:'library', special:true,
          effect:()=>{ gs.affection[c.id]+=15; } },
        { text:'어색하게 웃으며 화제를 돌린다', goto:'library',
          effect:()=>{ gs.affection[c.id]+=3; } },
      ];
    },
  },

  library: {
    title: '꿈 속의 도서관',
    text: ()=>`<p>홀 한쪽에 작은 도서관이 있습니다. 책들이 스스로 열리고 닫히며 속삭입니다.</p>
      <p style="margin-top:8px">한 책이 유독 밝게 빛납니다. 열어보니 이 꿈에 대한 이야기가 적혀 있습니다:</p>
      <div style="margin-top:10px;padding:12px;background:var(--bg2);border-radius:8px;border:1px solid var(--border);font-style:italic;color:var(--text2);line-height:1.9;font-size:13px">
        "이 꿈에는 출구가 있다. 저택 지하에 빛의 문이 열린다.<br>
        단, 조건이 있다 — 악몽을 반드시 이겨내야 한다.<br>
        그리고, 진심으로 연결된 두 마음이 있다면, 더 아름다운 길이 열릴 것이다."
      </div>
      <p style="margin-top:10px;color:var(--text3);font-size:13px">그때였습니다. 저택 어딘가에서 무거운 발소리가 들려오기 시작합니다...</p>`,
    choices: gs => [
      { text:'동료들에게 달려가 책의 내용을 알린다', goto:'nightmare_start',
        effect:()=>{ gs.companions.forEach(c=>{ gs.affection[c.id]+=5; }); } },
      { text:'책을 챙겨 혼자 분석한다', goto:'nightmare_start',
        effect:()=>{ gs.flags.hasBook=true; } },
    ],
  },

  nightmare_start: {
    onEnter: gs => {
      if (!gs.char) return;
      gs.char.san.current = Math.max(0, gs.char.san.current - 10);
      if (!gs.char.statusEffects.includes('공포')) gs.char.statusEffects.push('공포');
    },
    title: '😱 악몽의 등장',
    text: ()=>`<p>복도 끝에서 형체를 알 수 없는 것이 다가옵니다.</p>
      <p style="margin-top:8px"><strong style="color:#ef4444">악몽</strong>. 크툴루의 꿈 속에서 자라난 공포의 집합체. 빛을 흡수하며 느리게, 하지만 분명하게 다가옵니다.</p>
      <p style="margin-top:8px">세 동료의 얼굴에 두려움이 서립니다. 하지만 아무도 물러서지 않습니다.</p>`,
    choices: gs => {
      const [c0,c1,c2] = gs.companions;
      return [
        { text:`${c0.emoji} ${c0.name}을 믿고 함께 맞선다`, goto:'nightmare_help',
          effect:()=>{ gs.talkTarget=0; gs.affection[c0.id]+=12; } },
        { text:`${c1.emoji} ${c1.name}을 믿고 함께 맞선다`, goto:'nightmare_help',
          effect:()=>{ gs.talkTarget=1; gs.affection[c1.id]+=12; } },
        { text:`${c2.emoji} ${c2.name}을 믿고 함께 맞선다`, goto:'nightmare_help',
          effect:()=>{ gs.talkTarget=2; gs.affection[c2.id]+=12; } },
        { text:'셋 모두와 힘을 합친다', goto:'nightmare_help', special:true,
          effect:()=>{ gs.flags.teamwork=true; gs.companions.forEach(c=>{ gs.affection[c.id]+=7; }); } },
      ];
    },
  },

  nightmare_help: {
    onEnter: gs => {
      if (!gs.char) return;
      gs.char.hp.current = Math.max(0, gs.char.hp.current - 5);
    },
    title: '악몽을 넘어서',
    text: gs => {
      let body;
      if (gs.flags.teamwork) {
        const ns = gs.companions.map(c=>`<span style="color:${c.color}">${c.name}</span>`).join(', ');
        body = `${ns} 모두 동시에 움직였습니다. 각자의 방식으로, 하지만 하나가 되어 악몽에 맞섰습니다.`;
      } else {
        const c = gs.companions[gs.talkTarget];
        body = `<span style="color:${c.color}">${c.name}</span>이/가 앞으로 나섭니다.<br>
          <em style="color:${c.color}">${c.help}</em>`;
      }
      return `<p>${body}</p>
        <p style="margin-top:10px">악몽이 흔들립니다. 균열이 생기더니, 한순간 산산조각 납니다.</p>
        <p style="margin-top:8px">꿈 속에 고요함이 돌아왔습니다. 네 사람은 함께 숨을 고릅니다.</p>`;
    },
    choices: gs => [
      { text:'잠시 쉬며 서로의 상태를 확인한다', goto:'aftermath',
        effect:()=>{
          gs.companions.forEach(c=>{ gs.affection[c.id]+=5; });
          if (gs.char) {
            const hasYuri = gs.companions.some(c=>c.id==='yuri');
            gs.char.hp.current = Math.min(gs.char.hp.max, gs.char.hp.current + (hasYuri ? 12 : 4));
            gs.char.san.current = Math.min(gs.char.san.max, gs.char.san.current + 8);
            gs.char.statusEffects = (gs.char.statusEffects||[]).filter(s=>s!=='공포');
          }
        }
      },
      { text:'바로 지하 출구를 향해 이동한다', goto:'portal' },
    ],
  },

  aftermath: {
    title: '고요한 순간',
    text: gs => {
      const lover = soloGetLover(gs);
      if (lover.val >= 35) {
        const c = lover.char;
        return `<p>악몽이 사라진 저택은 기묘하게 평화롭습니다.</p>
          <p style="margin-top:8px">다른 동료들이 잠시 자리를 비운 사이, <span style="color:${c.color}">${c.name}</span>이/가 당신 곁에 앉습니다.</p>
          <div style="margin-top:10px;padding:12px;background:var(--bg2);border-left:3px solid ${c.color};border-radius:4px;font-size:13px;line-height:1.7">
            ${c.emoji} <em>${c.love}</em>
          </div>
          <p style="margin-top:10px">심장이 빠르게 뜁니다. 꿈이지만, 이 감정은 진짜입니다.</p>`;
      }
      return `<p>악몽이 사라진 저택은 기묘하게 평화롭습니다.</p>
        <p style="margin-top:8px">네 사람은 아무 말 없이 잠시 앉아 있었습니다. 함께라는 것만으로 충분한 순간입니다.</p>
        <p style="margin-top:8px;color:var(--text3);font-size:13px">하지만 꿈에서 나가야 합니다. 지하에 빛의 문이 있다고 했습니다.</p>`;
    },
    choices: gs => {
      const lover = soloGetLover(gs);
      const list = [{ text:'지하로 내려간다', goto:'portal' }];
      if (lover.val >= 35) {
        list.unshift({ text:`${lover.char.emoji} ${lover.char.name}에게 솔직한 마음을 전한다`, goto:'portal', special:true,
          effect:()=>{ gs.affection[lover.char.id]+=20; } });
      }
      return list;
    },
  },

  portal: {
    title: '⭐ 빛의 문',
    text: gs => {
      const lover = soloGetLover(gs);
      const canH = lover.val >= SOLO_HIDDEN_THRESHOLD;
      return `<p>지하에 내려가자 거대한 빛의 문이 서 있습니다. 문 너머로는 현실의 빛이 보입니다.</p>
        ${canH ? `<div style="margin-top:12px;padding:12px;background:rgba(167,139,250,0.1);border:1px solid #a78bfa;border-radius:8px">
          <p style="color:#a78bfa">✨ 문 옆에 두 번째 문이 보입니다. 보랏빛으로 빛나는 작은 문.</p>
          <p style="margin-top:6px;color:${lover.char.color};font-style:italic">"저 문은... 함께 가는 길인 것 같아." — ${lover.char.name}</p>
        </div>` : ''}
        <p style="margin-top:10px;color:var(--text3);font-size:13px">어떻게 할까요?</p>`;
    },
    choices: gs => {
      const lover = soloGetLover(gs);
      const canH = lover.val >= SOLO_HIDDEN_THRESHOLD;
      const list = [
        { text:'모두 함께 빛의 문으로 들어간다', goto:'end_good' },
        { text: lover.val >= 20 ? '혼자 먼저 문으로 들어간다' : '동료들을 두고 혼자 문으로 들어간다',
          goto: lover.val >= 20 ? 'end_solo' : 'end_bad' },
      ];
      if (canH) {
        list.unshift({ text:`${lover.char.emoji} ${lover.char.name}과 보랏빛 문으로 함께 들어간다 ✨`,
          goto:'end_hidden', special:true });
      }
      return list;
    },
  },

  end_good: {
    onEnter: gs => {
      if (!gs.char) return;
      gs.char.san.current = Math.min(gs.char.san.max, gs.char.san.current + 10);
      gs.char.hp.current = Math.min(gs.char.hp.max, gs.char.hp.current + 5);
      gs.char.statusEffects = gs.char.statusEffects.filter(s=>s!=='공포');
    },
    title: '🌅 함께하는 귀환',
    text: gs => {
      const ns = gs.companions.map(c=>`<span style="color:${c.color}">${c.name}</span>`).join(', ');
      return `<p>네 사람은 손을 맞잡고 빛의 문을 통과했습니다.</p>
        <p style="margin-top:8px">잠에서 깨어났을 때, 당신은 자신의 방에 있었습니다.</p>
        <p style="margin-top:8px">잠시 후, 메시지가 연달아 왔습니다. ${ns} 모두 안전하게 깨어났다고.</p>
        <div style="margin-top:12px;padding:12px;background:var(--bg2);border-radius:8px;text-align:center;color:var(--accent3)">
          꿈은 끝났지만, 그 안에서 쌓은 인연은 현실에서도 이어집니다.
        </div>
        ${soloFinalHtml(gs)}`;
    },
    choices: ()=>[{ text:'🔄 처음부터 다시 하기', goto:'__restart__' }],
  },

  end_hidden: {
    onEnter: gs => {
      if (!gs.char) return;
      gs.char.san.current = Math.min(gs.char.san.max, gs.char.san.current + 20);
      gs.char.hp.current = Math.min(gs.char.hp.max, gs.char.hp.current + 10);
      gs.char.statusEffects = gs.char.statusEffects.filter(s=>s!=='공포');
    },
    title: '💜 히든 루트 — 꿈과 현실 사이',
    text: gs => {
      const { char: c } = soloGetLover(gs);
      return `<p>보랏빛 문을 열었을 때, 부드러운 빛이 두 사람을 감쌌습니다.</p>
        <p style="margin-top:8px">꿈도 아니고 현실도 아닌 따뜻한 공간. 두 사람만의 순간.</p>
        <div style="margin-top:10px;padding:12px;border:1px solid ${c.color};background:rgba(0,0,0,0.2);border-radius:8px;line-height:1.7">
          <span style="color:${c.color};font-weight:bold">${c.emoji} ${c.name}</span><br>
          <em style="color:var(--text2)">${c.love}</em>
        </div>
        <p style="margin-top:12px;line-height:1.8">${c.hidden_end}</p>
        <div style="margin-top:16px;padding:14px;background:rgba(167,139,250,0.12);border-radius:8px;text-align:center">
          <div style="font-size:22px;margin-bottom:6px">✨</div>
          <div style="color:#a78bfa;font-weight:bold;font-size:15px">히든 루트 달성</div>
          <div style="font-size:12px;color:var(--text3);margin-top:4px">${c.name}와의 호감도 ${gs.affection[c.id]}로 숨겨진 결말을 보았습니다.</div>
        </div>
        ${soloFinalHtml(gs)}`;
    },
    choices: ()=>[{ text:'🔄 처음부터 다시 하기', goto:'__restart__' }],
  },

  end_bad: {
    onEnter: gs => {
      if (!gs.char) return;
      gs.char.san.current = Math.max(0, gs.char.san.current - 15);
      if (!gs.char.statusEffects.includes('공포')) gs.char.statusEffects.push('공포');
    },
    title: '😔 꿈 속에 남겨진',
    text: gs => `<p>동료들을 두고 혼자 문을 통과했습니다. 잠에서 깨어났지만, 동료들의 소식이 없습니다.</p>
      <p style="margin-top:8px;color:var(--text3)">꿈 속에서 쌓은 인연이 너무 적었습니다.</p>
      <div style="margin-top:12px;padding:12px;background:var(--bg2);border-radius:8px;font-size:13px;color:var(--text3)">
        💡 동료들과 더 많은 대화를 나누고 호감도를 높이면 다른 결말이 열립니다.
      </div>
      ${soloFinalHtml(gs)}`,
    choices: ()=>[{ text:'🔄 처음부터 다시 하기', goto:'__restart__' }],
  },

  end_solo: {
    onEnter: gs => {
      if (!gs.char) return;
      gs.char.san.current = Math.max(0, gs.char.san.current - 5);
    },
    title: '🚪 혼자만의 귀환',
    text: gs => `<p>당신은 혼자 빛의 문으로 들어갔습니다. 잠에서 깨어나자 동료들도 각자 깨어났다는 소식이 들렸습니다.</p>
      <p style="margin-top:8px;color:var(--text3);font-size:13px">안전하게 돌아왔지만... 조금 더 시간을 보낼 수 있었는데 하는 아쉬움이 남습니다.</p>
      <div style="margin-top:12px;padding:12px;background:var(--bg2);border-radius:8px;font-size:13px;color:var(--text3)">
        💡 호감도가 높은 동료와 함께라면 더 특별한 결말이 열립니다. (히든 루트: 호감도 ${SOLO_HIDDEN_THRESHOLD} 이상)
      </div>
      ${soloFinalHtml(gs)}`,
    choices: ()=>[{ text:'🔄 처음부터 다시 하기', goto:'__restart__' }],
  },
};

async function renderSoloPlay(container) {
  const SAVE_KEY = 'solo_save';
  let gs = null;

  // 캐릭터 시트 스텟 로드
  let char = await DB.get('characters', 'default');
  if (!char) char = { ...DEFAULT_CHAR, id: 'default' };
  char.hp = char.hp || { current: 10, max: 10 };
  char.mp = char.mp || { current: 10, max: 10 };
  char.san = char.san || { current: 50, max: 50 };
  char.statusEffects = char.statusEffects || [];
  char.bodyDamage = char.bodyDamage || {};

  const pick3 = () => [...SOLO_CHARS].sort(() => Math.random() - 0.5).slice(0, 3);

  const loadSave = () => {
    try {
      const raw = localStorage.getItem(SAVE_KEY);
      if (!raw) return null;
      const data = JSON.parse(raw);
      const companions = (data.companionIds || []).map(id => SOLO_CHARS.find(c => c.id === id)).filter(Boolean);
      if (companions.length !== 3) return null;
      return { ...data, companions };
    } catch { return null; }
  };

  const persist = () => {
    if (!gs) return;
    localStorage.setItem(SAVE_KEY, JSON.stringify({
      companionIds: gs.companions.map(c => c.id),
      affection: gs.affection,
      scene: gs.scene,
      flags: gs.flags,
      talkTarget: gs.talkTarget,
      history: gs.history,
    }));
    if (gs.char) DB.put('characters', gs.char).catch(() => {});
  };

  const goTo = (sceneId, effect) => {
    if (sceneId === '__restart__') {
      localStorage.removeItem(SAVE_KEY);
      renderSelection(pick3());
      return;
    }
    if (effect) effect();
    gs.scene = sceneId;
    gs.history.push(sceneId);
    const scene = SOLO_SCENES[sceneId];
    if (scene && scene.onEnter) scene.onEnter(gs);
    persist();
    renderScene();
  };

  const renderScene = () => {
    const scene = SOLO_SCENES[gs.scene];
    if (!scene) { Router.navigate('/'); return; }
    const choices = scene.choices ? scene.choices(gs) : [];

    container.innerHTML = `
      <div style="display:flex;gap:6px;align-items:flex-start;margin-bottom:10px">
        <button class="btn btn-secondary btn-sm" id="solo-home" style="flex-shrink:0">← 홈</button>
        <div style="flex:1;min-width:0">
          <div style="display:flex;gap:8px;padding:5px 10px;background:var(--bg2);border-radius:8px;border:1px solid var(--border)">
            ${soloAffHtml(gs)}
          </div>
          ${soloStatHtml(gs)}
        </div>
      </div>
      <div class="card">
        ${scene.title ? `<h3 style="color:var(--accent3);margin-bottom:12px;font-size:16px">${scene.title}</h3>` : ''}
        <div style="line-height:1.8;font-size:14px;color:var(--text1)">${scene.text(gs)}</div>
      </div>
      <div style="display:flex;flex-direction:column;gap:8px;margin-top:10px">
        ${choices.map((ch, i) => `
          <button class="btn ${ch.special ? 'btn-primary' : 'btn-secondary'}" data-ci="${i}"
            style="text-align:left;padding:10px 14px;white-space:normal;line-height:1.5;width:100%">
            ${ch.text}
          </button>`).join('')}
      </div>`;

    container.querySelector('#solo-home').onclick = () => {
      if (confirm('진행 상황은 자동 저장됩니다. 홈으로 이동할까요?')) Router.navigate('/');
    };
    container.querySelectorAll('[data-ci]').forEach(btn => {
      btn.onclick = () => {
        const ch = choices[+btn.dataset.ci];
        goTo(ch.goto, ch.effect);
      };
    });
  };

  const renderSelection = (companions) => {
    container.innerHTML = `
      <div style="text-align:center;padding:16px 0 8px">
        <div style="font-size:40px;margin-bottom:8px">💕</div>
        <h2 style="color:var(--accent3)">독스프 솔로 플레이</h2>
        <p style="color:var(--text3);font-size:13px;margin-top:6px;line-height:1.7">
          꿈 속에서 함께할 동료 3명이 선택되었습니다.<br>
          대화하고 호감도를 쌓으면 히든 루트가 열립니다.
        </p>
      </div>
      <div style="display:flex;flex-direction:column;gap:10px;margin:16px 0">
        ${companions.map(c => `
          <div class="card" style="border-color:${c.color};padding:14px;display:flex;gap:12px;align-items:center">
            <div style="font-size:30px;flex-shrink:0">${c.emoji}</div>
            <div style="flex:1;min-width:0">
              <div style="font-size:15px;font-weight:bold;color:${c.color}">${c.name}</div>
              <div style="font-size:12px;color:var(--text3)">${c.role}</div>
              <div style="font-size:12px;color:var(--text2);margin-top:3px">${c.desc}</div>
            </div>
          </div>`).join('')}
      </div>
      <div style="display:flex;gap:8px">
        <button class="btn btn-secondary flex-1" id="solo-reroll">🔀 다시 뽑기</button>
        <button class="btn btn-primary flex-1" id="solo-start">💤 꿈 속으로</button>
      </div>`;

    container.querySelector('#solo-reroll').onclick = () => renderSelection(pick3());
    container.querySelector('#solo-start').onclick = () => {
      gs = {
        companions,
        affection: Object.fromEntries(companions.map(c => [c.id, 0])),
        scene: 'intro', flags: {}, talkTarget: 0, history: ['intro'],
        char,
      };
      persist();
      renderScene();
    };
  };

  const saved = loadSave();
  if (saved && !saved.scene.startsWith('end_')) {
    gs = { ...saved, char };
    container.innerHTML = `
      <div style="text-align:center;padding:40px 20px">
        <div style="font-size:36px;margin-bottom:12px">💤</div>
        <h3 style="color:var(--accent3);margin-bottom:8px">이전 꿈이 남아있습니다</h3>
        <p style="color:var(--text3);font-size:13px;margin-bottom:20px">
          ${saved.companions.map(c=>c.name).join(', ')}와 함께한 꿈
        </p>
        <div style="display:flex;gap:8px;justify-content:center">
          <button class="btn btn-secondary" id="solo-new">새 게임</button>
          <button class="btn btn-primary" id="solo-resume">이어하기 ▶</button>
        </div>
      </div>`;
    container.querySelector('#solo-resume').onclick = () => renderScene();
    container.querySelector('#solo-new').onclick = () => {
      localStorage.removeItem(SAVE_KEY);
      renderSelection(pick3());
    };
  } else {
    renderSelection(pick3());
  }
}

Router.register('/solo', renderSoloPlay);
