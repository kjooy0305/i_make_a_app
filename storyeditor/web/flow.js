/* StoryEditor 분기도 화면 (ReactFlow)
 *  1열: 이전 이야기(회색) → 2열: 선택한 상위 이야기(굵은 검정 테두리) → 3열: 선택 가능 분기(클릭 버튼)
 *  노드/연결선 계산은 SEngine.buildFlow, 이 파일은 그리기와 입력 처리만 맡는다. */
(function () {
  'use strict';
  const R = window.React, RD = window.ReactDOM, RF = window.ReactFlow, E = window.SEngine;
  if (!R || !RD || !RF) { window.SFlow = null; return; }
  const h = R.createElement;
  const { Handle, Position, MarkerType } = RF;
  const ReactFlow = RF.default || RF.ReactFlow;
  const NO = 'nodrag nopan';

  const clip = (s, n) => { s = String(s || '').replace(/\s+/g, ' ').trim(); return s.length > n ? s.slice(0, n) + '…' : s; };

  /* ---------- 노드 ---------- */
  function PrevNode({ data }) {
    return h('div', { className: 'fn prev', title: 'P.' + data.n + ' 로 돌아가기', onClick: () => data.onBack(data.n) },
      h('div', { className: 'fn-tag' }, data.fromTrail ? '이전 이야기' : '상위 이야기'),
      h('div', { className: 'fn-ttl' }, 'P.' + data.n + ' · ' + clip(data.title, 26)),
      h(Handle, { type: 'source', position: Position.Right, isConnectable: false }));
  }
  function CurNode({ data }) {
    return h('div', { className: 'fn cur' },
      h(Handle, { type: 'target', position: Position.Left, isConnectable: false }),
      h('div', { className: 'fn-tag' }, '선택한 이야기' + (data.isEnd ? ' · 마지막' : '')),
      h('div', { className: 'fn-ttl' }, 'P.' + data.n + ' · ' + clip(data.title, 30)),
      h('div', { className: 'fn-txt' }, clip(data.text, 110) || '(본문이 비어 있어요)'),
      h('div', { className: 'fn-act ' + NO },
        h('button', { onClick: () => data.onEditPage() }, '본문 편집'),
        h('button', { onClick: () => data.onPlay() }, '▶ 여기서')),
      h(Handle, { type: 'source', position: Position.Right, isConnectable: false }));
  }
  function ChoiceNode({ data }) {
    return h('div', { className: 'fn choice' + (data.missing ? ' missing' : ''), onClick: () => data.onGo(data.ci) },
      h(Handle, { type: 'target', position: Position.Left, isConnectable: false }),
      h('div', { className: 'fn-row' },
        h('div', { className: 'fn-ttl grow' }, data.text || '(선택지 텍스트 없음)'),
        h('button', { className: 'fn-ed ' + NO, title: '이 분기 편집', onClick: (e) => { e.stopPropagation(); data.onEdit(data.ci); } }, '✎')),
      h('div', { className: 'fn-sub' }, data.missing ? '⚠ P.' + data.to + ' 없음 — 눌러서 새로 만들기' : '→ P.' + data.to + ' · ' + clip(data.targetLabel, 22)),
      data.extra.length ? h('div', { className: 'fn-sub' }, data.extra.map((x, i) => h('span', { key: i, className: 'fn-chip' + (x.missing ? ' bad' : '') }, (x.when ? x.when + ' ' : '') + '→ P.' + x.to))) : null,
      data.flags.length ? h('div', { className: 'fn-sub' }, data.flags.map((f, i) => h('span', { key: i, className: 'fn-chip' }, f))) : null);
  }
  function AddNode({ data }) {
    return h('div', { className: 'fn add', onClick: () => data.onAdd() },
      h(Handle, { type: 'target', position: Position.Left, isConnectable: false }),
      '＋ 분기 추가');
  }
  function EndNode() {
    return h('div', { className: 'fn end' }, h(Handle, { type: 'target', position: Position.Left, isConnectable: false }), '— 끝 —', h('div', { className: 'fn-sub' }, '마지막 페이지라 선택지가 없어요'));
  }
  const nodeTypes = { prev: PrevNode, cur: CurNode, choice: ChoiceNode, add: AddNode, end: EndNode };

  /* ---------- 분기 편집 패널 ---------- */
  function EditPanel({ pj, cur, ci, api }) {
    const page = E.pageOf(pj, cur), c = page && page.choices[ci];
    if (!c) return null;
    const pages = pj.pages.slice().sort((a, b) => a.n - b.n);
    const known = pages.some((p) => p.n === Number(c.base.next));
    return h('div', { className: 'fe-panel', key: cur + ':' + ci },
      h('div', { className: 'row' }, h('b', { className: 'grow' }, 'P.' + cur + ' · 분기 #' + (ci + 1) + ' 편집'),
        h('button', { onClick: () => api.closeEdit() }, '닫기')),
      h('div', { className: 'muted' }, '선택지 텍스트'),
      h('input', { type: 'text', defaultValue: c.text, placeholder: '선택지 텍스트', onChange: (e) => api.setText(ci, e.target.value) }),
      h('div', { className: 'muted', style: { marginTop: 8 } }, '이동할 페이지'),
      h('div', { className: 'row' },
        h('select', { className: 'grow', defaultValue: String(c.base.next), onChange: (e) => api.setNext(ci, parseInt(e.target.value, 10)) },
          !known ? h('option', { value: String(c.base.next) }, 'P.' + c.base.next + ' (없는 페이지)') : null,
          pages.map((p) => h('option', { key: p.n, value: String(p.n) }, 'P.' + p.n + ' · ' + clip(E.pageLabel(p), 24)))),
        h('button', { onClick: () => api.newPageFor(ci) }, '새 페이지로 연결')),
      h('div', { className: 'row', style: { marginTop: 10 } },
        h('button', { disabled: ci === 0, onClick: () => api.move(ci, -1) }, '▲ 위로'),
        h('button', { disabled: ci === page.choices.length - 1, onClick: () => api.move(ci, 1) }, '▼ 아래로'),
        h('button', { onClick: () => api.dup(ci) }, '복제'),
        h('button', { className: 'dng', onClick: () => api.del(ci) }, '삭제')),
      h('div', { className: 'muted', style: { marginTop: 6 } }, '조건·아이템·확률 분기 같은 고급 속성은 “본문 편집” 화면에서 설정해요.'));
  }

  /* ---------- 전체 ---------- */
  function Flow({ pj, cur, trail, editCi, api }) {
    const g = R.useMemo(() => {
      const base = E.buildFlow(pj, cur, trail);
      const nodes = base.nodes.map((n) => {
        const d = Object.assign({}, n.data);
        if (n.type === 'prev') d.onBack = api.back;
        if (n.type === 'cur') { d.onEditPage = api.editPage; d.onPlay = api.play; }
        if (n.type === 'choice') { d.onGo = api.go; d.onEdit = api.edit; }
        if (n.type === 'add') d.onAdd = api.add;
        return Object.assign({}, n, { data: d, draggable: false, selectable: false });
      });
      const edges = base.edges.map((e) => Object.assign({}, e, {
        markerEnd: { type: MarkerType.ArrowClosed, color: e.source === 'cur' ? '#111' : '#9aa0b4' },
        style: e.source === 'cur' ? { stroke: '#111', strokeWidth: 2 } : { stroke: '#b7bccb', strokeWidth: 2, strokeDasharray: '6 4' },
      }));
      return { nodes, edges };
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [pj, cur, trail, api, api.rev]);

    return h(R.Fragment, null,
      h('div', { className: 'flow-wrap' },
        h(ReactFlow, {
          key: cur + ':' + g.nodes.length + ':' + trail.length,
          nodes: g.nodes, edges: g.edges, nodeTypes,
          fitView: true, fitViewOptions: { padding: 0.15, maxZoom: 1 },
          minZoom: 0.3, maxZoom: 1.5,
          nodesDraggable: false, nodesConnectable: false, elementsSelectable: false,
          zoomOnScroll: false, panOnScroll: false, preventScrolling: false,
          proOptions: { hideAttribution: true },
        })),
      editCi != null ? h(EditPanel, { pj, cur, ci: editCi, api }) : null);
  }

  /* ---------- 마운트 ---------- */
  let root = null, host = null;
  function render(el, props) {
    if (host !== el) { unmount(); host = el; root = RD.createRoot(el); }
    root.render(h(Flow, props));
  }
  function unmount() { if (root) { root.unmount(); root = null; host = null; } }

  window.SFlow = { render, unmount };
})();
