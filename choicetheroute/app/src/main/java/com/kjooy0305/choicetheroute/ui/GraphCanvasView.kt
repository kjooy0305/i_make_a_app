package com.kjooy0305.choicetheroute.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import com.kjooy0305.choicetheroute.model.BranchEdge
import com.kjooy0305.choicetheroute.model.BranchNode
import com.kjooy0305.choicetheroute.model.NodeColors
import com.kjooy0305.choicetheroute.model.NodeMetrics
import kotlin.math.*

class GraphCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // ── Data ──────────────────────────────────────────────────────
    var nodes: List<BranchNode> = emptyList()
        set(v) { field = v; rebuildVisible(); invalidate() }
    var edges: List<BranchEdge> = emptyList()
        set(v) { field = v; rebuildVisible(); invalidate() }
    var nodeLayers: Map<String, Int> = emptyMap()
        set(v) { field = v; invalidate() }

    /** 하위 분기 표시 기준 노드. null이면 필터 없음. */
    var focusRootId: String? = null
        set(v) { field = v; rebuildVisible(); invalidate() }

    /** 기준 노드에서 몇 단계 뒤까지 보일지. 0이면 전체 표시. */
    var focusDepth: Int = 0
        set(v) { field = v; rebuildVisible(); invalidate() }

    // 노드와 엣지 선택은 상호 배타적이다. 서로의 setter를 부르면 재진입하면서
    // 방금 넣은 값을 다시 지워 버리므로, 뒤에 있는 저장소를 직접 손댄다.
    private var selNodeId: String? = null
    private var selEdgeId: String? = null

    var selectedId: String?
        get() = selNodeId
        set(v) {
            selNodeId = v
            if (v != null) selEdgeId = null
            onSelectionChanged?.invoke(nodes.find { it.id == v })
            invalidate()
        }

    var selectedEdgeId: String?
        get() = selEdgeId
        set(v) {
            selEdgeId = v
            if (v != null) selNodeId = null
            onEdgeSelectionChanged?.invoke(edges.find { it.id == v })
            invalidate()
        }

    var connectMode = false
        set(v) { field = v; if (!v) connectFromId = null; invalidate() }
    private var connectFromId: String? = null

    // ── Callbacks ─────────────────────────────────────────────────
    var onSelectionChanged: ((BranchNode?) -> Unit)? = null
    var onEdgeSelectionChanged: ((BranchEdge?) -> Unit)? = null
    var onNodeMoved: ((id: String, x: Float, y: Float) -> Unit)? = null
    var onMoveEnd: (() -> Unit)? = null
    var onConnect: ((fromId: String, toId: String) -> Unit)? = null
    var onNodeEdit: ((BranchNode) -> Unit)? = null
    var onAddStandaloneNode: ((x: Float, y: Float) -> Unit)? = null

    // ── Viewport ──────────────────────────────────────────────────
    private var vx = 0f
    private var vy = 0f
    private var vs = 1f

    private companion object {
        /** 역방향 곡선을 순방향 곡선과 겹치지 않게 밀어내는 거리 */
        const val BACK_DETOUR = 96f
    }

    // ── Node geometry ─────────────────────────────────────────────
    private val NH = NodeMetrics.HEIGHT
    private val NR = NodeMetrics.CORNER

    /** 범례는 확대·축소와 무관한 화면 좌표에 그리므로 dp로 맞춘다 */
    private val dp = resources.displayMetrics.density

    private fun widthOf(n: BranchNode) = NodeMetrics.widthOf(n)

    // ── Depth filter ──────────────────────────────────────────────
    /** null이면 전부 표시. 아니면 이 집합에 든 노드만 그린다. */
    private var visibleIds: Set<String>? = null

    private fun rebuildVisible() {
        val root = focusRootId
        if (root == null || focusDepth <= 0) { visibleIds = null; return }
        val succ = HashMap<String, MutableList<String>>()
        for (e in edges) {
            if (e.fromId == e.toId) continue
            succ.getOrPut(e.fromId) { mutableListOf() }.add(e.toId)
        }
        val seen = linkedSetOf(root)
        var frontier = listOf(root)
        repeat(focusDepth) {
            val next = mutableListOf<String>()
            for (id in frontier) {
                for (n in succ[id] ?: emptyList()) if (seen.add(n)) next.add(n)
            }
            frontier = next
        }
        visibleIds = seen
    }

    private fun isVisible(id: String) = visibleIds?.contains(id) ?: true

    // ── Edge geometry cache (filled each draw, used for hit-test) ──
    private data class EdgeGeo(
        val sx: Float, val sy: Float,
        val ex: Float, val ey: Float,
        val cpx1: Float, val cpy1: Float,
        val cpx2: Float, val cpy2: Float,
        val straight: Boolean
    )
    private val edgeGeoCache = mutableMapOf<String, EdgeGeo>()

    // ── Paints ────────────────────────────────────────────────────
    private val pBg   = Paint().apply { color = 0xFF080C14.toInt() }
    private val pDot  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0F1828.toInt() }

    private val pNodeFill   = Paint().apply { color = 0xFF111B2B.toInt(); isAntiAlias = true }
    private val pStartFill  = Paint().apply { color = 0xFF0A2218.toInt(); isAntiAlias = true }
    private val pNodeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1D2D44.toInt(); style = Paint.Style.STROKE; strokeWidth = 1.8f
    }
    private val pStartBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1A5C3A.toInt(); style = Paint.Style.STROKE; strokeWidth = 2.2f
    }
    private val pSelBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3A8AEE.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val pConnFromBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFB300.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val pConnTgtBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4DCEA0.toInt(); style = Paint.Style.STROKE; strokeWidth = 2.5f
        pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
    }

    private val pTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFDDE4F0.toInt(); textSize = NodeMetrics.TITLE_SIZE; textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val pStartLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4DCEA0.toInt(); textSize = 22f; textAlign = Paint.Align.CENTER
    }

    private val pFwdEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3A8AEE.toInt(); style = Paint.Style.STROKE; strokeWidth = 2.8f
        strokeCap = Paint.Cap.ROUND
    }
    private val pBackEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE84040.toInt(); style = Paint.Style.STROKE; strokeWidth = 2.4f
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(11f, 6f), 0f)
    }
    private val pSelfEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFB300.toInt(); style = Paint.Style.STROKE; strokeWidth = 2.4f
        strokeCap = Paint.Cap.ROUND
    }
    // 선택된 엣지 하이라이트 (배경 글로우)
    private val pSelEdgeGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x55FFB300; style = Paint.Style.STROKE; strokeWidth = 10f
    }

    private val pArrowFwd  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF3A8AEE.toInt() }
    private val pArrowBack = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE84040.toInt() }
    private val pArrowSelf = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFB300.toInt() }
    private val pArrowSelGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x55FFB300 }

    private val pLegendFwd = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3A8AEE.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val pLegendBack = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE84040.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }
    private val pLegendSelf = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFB300.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val pLegendText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8899B4.toInt(); textSize = 36f
    }

    // ── Touch state ───────────────────────────────────────────────
    private val gd  = GestureDetector(context, GestureListener())
    private val sgd = ScaleGestureDetector(context, ScaleListener())
    private var isScaling  = false
    private var dragNodeId: String? = null
    private var dragDownX  = 0f
    private var dragDownY  = 0f
    private var didDrag    = false

    // ── Coordinate helpers ────────────────────────────────────────
    private fun cx(sx: Float) = (sx - vx) / vs
    private fun cy(sy: Float) = (sy - vy) / vs

    private fun findNodeAt(canvasX: Float, canvasY: Float): BranchNode? =
        nodes.lastOrNull { n ->
            isVisible(n.id) &&
            canvasX >= n.x && canvasX <= n.x + widthOf(n) &&
            canvasY >= n.y && canvasY <= n.y + NH
        }

    // ── Edge hit-test ─────────────────────────────────────────────
    private fun bezierPt(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val mt = 1 - t
        return mt*mt*mt*p0 + 3*mt*mt*t*p1 + 3*mt*t*t*p2 + t*t*t*p3
    }

    private fun distToSegment(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): Float {
        val abx = bx - ax; val aby = by - ay
        val len2 = abx*abx + aby*aby
        if (len2 == 0f) return hypot(px - ax, py - ay)
        val t = ((px - ax)*abx + (py - ay)*aby) / len2
        val tc = t.coerceIn(0f, 1f)
        return hypot(px - (ax + tc*abx), py - (ay + tc*aby))
    }

    private fun distToGeo(px: Float, py: Float, geo: EdgeGeo): Float {
        if (geo.straight) return distToSegment(px, py, geo.sx, geo.sy, geo.ex, geo.ey)
        var minD = Float.MAX_VALUE
        for (i in 0..24) {
            val t = i / 24f
            val bx = bezierPt(t, geo.sx, geo.cpx1, geo.cpx2, geo.ex)
            val by = bezierPt(t, geo.sy, geo.cpy1, geo.cpy2, geo.ey)
            val d = hypot(px - bx, py - by)
            if (d < minD) minD = d
        }
        return minD
    }

    private fun findEdgeAt(canvasX: Float, canvasY: Float): BranchEdge? {
        val threshold = 20f / vs  // 화면 20px → 캔버스 좌표계
        var best: BranchEdge? = null
        var bestDist = threshold
        for (edge in edges) {
            val geo = edgeGeoCache[edge.id] ?: continue
            val d = distToGeo(canvasX, canvasY, geo)
            if (d < bestDist) { bestDist = d; best = edge }
        }
        return best
    }

    // ── Touch ─────────────────────────────────────────────────────
    override fun onTouchEvent(event: MotionEvent): Boolean {
        sgd.onTouchEvent(event)
        if (!isScaling) gd.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragDownX = event.x; dragDownY = event.y; didDrag = false
                dragNodeId = findNodeAt(cx(event.x), cy(event.y))?.id
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragNodeId != null && didDrag) onMoveEnd?.invoke()
                dragNodeId = null; isScaling = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> { dragNodeId = null; isScaling = true }
        }
        return true
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent) = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (didDrag) return true
            val tapped = findNodeAt(cx(e.x), cy(e.y))
            if (connectMode) {
                val from = connectFromId
                if (tapped != null && from != null) onConnect?.invoke(from, tapped.id)
                return true
            }
            if (tapped != null) {
                selectedId = if (selectedId == tapped.id) null else tapped.id
            } else {
                // 노드 아니면 엣지 탐색
                val edge = findEdgeAt(cx(e.x), cy(e.y))
                selectedEdgeId = if (edge != null && selectedEdgeId == edge.id) null else edge?.id
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (connectMode) return true
            val tapped = findNodeAt(cx(e.x), cy(e.y))
            if (tapped != null) onNodeEdit?.invoke(tapped)
            else onAddStandaloneNode?.invoke(cx(e.x), cy(e.y))
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            if (isScaling) return true
            val totalDist = hypot(e2.x - dragDownX, e2.y - dragDownY)
            if (totalDist > 8f) didDrag = true
            if (dragNodeId != null && didDrag && !connectMode) {
                val node = nodes.find { it.id == dragNodeId } ?: return true
                onNodeMoved?.invoke(dragNodeId!!, node.x - dx/vs, node.y - dy/vs)
            } else {
                vx -= dx; vy -= dy; invalidate()
            }
            return true
        }
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            val f = d.scaleFactor; val fx = d.focusX; val fy = d.focusY
            vx = fx - (fx - vx)*f; vy = fy - (fy - vy)*f
            vs = (vs*f).coerceIn(0.15f, 8f); invalidate()
            return true
        }
    }

    // ── Draw ──────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pBg)
        drawGrid(canvas)
        canvas.save()
        canvas.translate(vx, vy)
        canvas.scale(vs, vs)
        edgeGeoCache.clear()
        drawEdges(canvas)
        drawNodes(canvas)
        canvas.restore()
        drawLegend(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        val sp = 60f * vs
        val ox = ((vx % sp) + sp) % sp
        val oy = ((vy % sp) + sp) % sp
        var x = ox
        while (x < width) {
            var y = oy; while (y < height) { canvas.drawCircle(x, y, 1.5f, pDot); y += sp }
            x += sp
        }
    }

    private fun drawEdges(canvas: Canvas) {
        val nm = nodes.associateBy { it.id }
        for (edge in edges) {
            val f = nm[edge.fromId] ?: continue
            val t = nm[edge.toId]   ?: continue
            if (!isVisible(f.id) || !isVisible(t.id)) continue
            if (f.id == t.id) { drawSelfLoop(canvas, f, edge); continue }
            val fL = nodeLayers[f.id] ?: 0
            val tL = nodeLayers[t.id] ?: 0
            val isForward = tL > fL || (tL == fL && t.x > f.x)
            drawDirectedEdge(canvas, edge, f, t, isForward)
        }
    }

    /** 중심에서 (dx,dy) 방향으로 나갈 때 상자 테두리와 만나는 점 */
    private fun boxEdgePoint(cx: Float, cy: Float, w: Float, dx: Float, dy: Float): Pair<Float, Float> {
        if (dx == 0f && dy == 0f) return cx to cy
        val tx = if (dx != 0f) (w / 2) / abs(dx) else Float.MAX_VALUE
        val ty = if (dy != 0f) (NH / 2) / abs(dy) else Float.MAX_VALUE
        val t = min(tx, ty)
        return (cx + dx * t) to (cy + dy * t)
    }

    /**
     * 곡선은 S자(수평 흐름) 또는 세로 S자로 그린다.
     * 시작점에서는 면의 법선 방향으로 빠져나가고, 도착점에는 같은 방향으로 들어간다.
     * 덕분에 아래로 가는 선은 좌우 반전 S, 위로 가는 선은 S 모양이 된다.
     */
    private fun calcEdgeGeo(f: BranchNode, t: BranchNode, straight: Boolean, forward: Boolean): EdgeGeo {
        val fw = widthOf(f); val tw = widthOf(t)
        val fcx = f.x + fw / 2; val fcy = f.y + NH / 2
        val tcx = t.x + tw / 2; val tcy = t.y + NH / 2
        val dx = tcx - fcx; val dy = tcy - fcy

        if (straight) {
            val (sx, sy) = boxEdgePoint(fcx, fcy, fw, dx, dy)
            val (ex, ey) = boxEdgePoint(tcx, tcy, tw, -dx, -dy)
            return EdgeGeo(sx, sy, ex, ey, sx, sy, ex, ey, true)
        }

        // 역방향 선은 같은 두 노드를 잇는 순방향 선과 정확히 겹치므로 옆으로 비켜 준다
        val detour = if (forward) 0f else BACK_DETOUR

        // 세로 거리가 가로보다 뚜렷하게 크면 위/아래 면을, 아니면 좌/우 면을 앵커로
        val vertical = abs(dy) > abs(dx) * 1.35f
        return if (vertical) {
            val down = dy >= 0f
            val sYv = if (down) f.y + NH else f.y
            val eYv = if (down) t.y else t.y + NH
            val reach = (abs(eYv - sYv) * 0.55f).coerceAtLeast(46f) * (if (down) 1f else -1f)
            EdgeGeo(fcx, sYv, tcx, eYv, fcx + detour, sYv + reach, tcx + detour, eYv - reach, false)
        } else {
            val right = dx >= 0f
            val sXh = if (right) f.x + fw else f.x
            val eXh = if (right) t.x else t.x + tw
            val reach = (abs(eXh - sXh) * 0.55f).coerceAtLeast(46f) * (if (right) 1f else -1f)
            EdgeGeo(sXh, fcy, eXh, tcy, sXh + reach, fcy + detour, eXh - reach, tcy + detour, false)
        }
    }

    private fun drawDirectedEdge(canvas: Canvas, edge: BranchEdge, f: BranchNode, t: BranchNode, forward: Boolean) {
        val geo = calcEdgeGeo(f, t, edge.isStraight, forward)
        edgeGeoCache[edge.id] = geo

        val selected = (edge.id == selectedEdgeId)
        val edgePaint  = if (forward) pFwdEdge  else pBackEdge
        val arrowPaint = if (forward) pArrowFwd else pArrowBack
        val angle =
            if (geo.straight) atan2(geo.ey - geo.sy, geo.ex - geo.sx)
            else atan2(geo.ey - geo.cpy2, geo.ex - geo.cpx2)

        // 선택된 엣지: 글로우를 먼저 깔고 그 위에 본선
        if (selected) {
            if (geo.straight) canvas.drawLine(geo.sx, geo.sy, geo.ex, geo.ey, pSelEdgeGlow)
            else canvas.drawPath(pathOf(geo), pSelEdgeGlow)
            drawArrow(canvas, geo.ex, geo.ey, angle, pArrowSelGlow)
        }

        if (geo.straight) canvas.drawLine(geo.sx, geo.sy, geo.ex, geo.ey, edgePaint)
        else canvas.drawPath(pathOf(geo), edgePaint)
        drawArrow(canvas, geo.ex, geo.ey, angle, arrowPaint)
    }

    private fun pathOf(geo: EdgeGeo) = Path().apply {
        moveTo(geo.sx, geo.sy)
        cubicTo(geo.cpx1, geo.cpy1, geo.cpx2, geo.cpy2, geo.ex, geo.ey)
    }

    private fun drawSelfLoop(canvas: Canvas, node: BranchNode, edge: BranchEdge) {
        val cx = node.x + widthOf(node) * 0.5f; val top = node.y
        val r = 36f
        val geo = EdgeGeo(
            sx = cx - r*0.5f, sy = top,
            ex = cx + r*0.5f, ey = top,
            cpx1 = cx - r*2.5f, cpy1 = top - r*2.8f,
            cpx2 = cx + r*2.5f, cpy2 = top - r*2.8f,
            straight = false
        )
        edgeGeoCache[edge.id] = geo
        val path = pathOf(geo)
        val angle = atan2(geo.ey - geo.cpy2, geo.ex - geo.cpx2)
        if (edge.id == selectedEdgeId) {
            canvas.drawPath(path, pSelEdgeGlow)
            drawArrow(canvas, geo.ex, geo.ey, angle, pArrowSelGlow)
        }
        canvas.drawPath(path, pSelfEdge)
        drawArrow(canvas, geo.ex, geo.ey, angle, pArrowSelf)
    }

    private fun drawArrow(canvas: Canvas, x: Float, y: Float, angle: Float, paint: Paint) {
        val sz = 30f; val w = 0.55f
        val bx = x - sz*cos(angle); val by = y - sz*sin(angle)
        val px = -sin(angle)*sz*w; val py = cos(angle)*sz*w
        val p = Path().apply { moveTo(x, y); lineTo(bx + px, by + py); lineTo(bx - px, by - py); close() }
        canvas.drawPath(p, paint)
    }

    /** 색을 지정한 노드용 Paint는 색마다 한 번만 만들어 재사용한다 */
    private val tintFills = HashMap<Int, Paint>()
    private val tintBorders = HashMap<Int, Paint>()

    private fun fillFor(node: BranchNode): Paint = when {
        node.color != NodeColors.NONE -> tintFills.getOrPut(node.color) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = NodeColors.tint(node.color) }
        }
        node.isStart -> pStartFill
        else -> pNodeFill
    }

    private fun tintBorderFor(color: Int): Paint = tintBorders.getOrPut(color) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; style = Paint.Style.STROKE; strokeWidth = 2.6f
        }
    }

    private fun drawNodes(canvas: Canvas) {
        val titleOffset = -(pTitle.descent() + pTitle.ascent()) / 2
        for (node in nodes) {
            if (!isVisible(node.id)) continue
            val w = widthOf(node)
            val rect = RectF(node.x, node.y, node.x + w, node.y + NH)
            canvas.drawRoundRect(rect, NR, NR, fillFor(node))
            val border = when {
                connectMode && node.id == connectFromId -> pConnFromBorder
                connectMode -> pConnTgtBorder
                node.id == selectedId -> pSelBorder
                node.color != NodeColors.NONE -> tintBorderFor(node.color)
                node.isStart -> pStartBorder
                else -> pNodeBorder
            }
            canvas.drawRoundRect(rect, NR, NR, border)

            val centerX = node.x + w / 2
            if (node.isStart) {
                canvas.drawText(NodeMetrics.displayTitle(node.title), centerX, node.y + 34f, pTitle)
                canvas.drawText("● 시작", centerX, node.y + 60f, pStartLabel)
            } else {
                canvas.drawText(NodeMetrics.displayTitle(node.title), centerX, node.y + NH / 2 + titleOffset, pTitle)
            }
        }
    }

    private fun drawLegend(canvas: Canvas) {
        // 하단 액션 패널에 가리지 않도록 좌측 상단에 그린다
        val lx = 12f * dp
        var ly = 22f * dp
        val step = 24f * dp
        val lineW = 22f * dp
        val gap = 8f * dp
        val lift = 5f * dp
        canvas.drawLine(lx, ly - lift, lx + lineW, ly - lift, pLegendFwd)
        canvas.drawText("순방향", lx + lineW + gap, ly, pLegendText); ly += step
        canvas.drawLine(lx, ly - lift, lx + lineW, ly - lift, pLegendBack)
        canvas.drawText("역방향", lx + lineW + gap, ly, pLegendText); ly += step
        canvas.drawLine(lx, ly - lift, lx + lineW, ly - lift, pLegendSelf)
        canvas.drawText("자기참조", lx + lineW + gap, ly, pLegendText)
    }

    init {
        // 범례 굵기·크기는 화면 밀도에 맞춘다 (이전에는 px 고정이라 고밀도 기기에서 잘게 보였다)
        pLegendText.textSize = 15f * dp
        val lw = 2.5f * dp
        pLegendFwd.strokeWidth = lw
        pLegendBack.strokeWidth = lw
        pLegendBack.pathEffect = DashPathEffect(floatArrayOf(7f * dp, 4f * dp), 0f)
        pLegendSelf.strokeWidth = lw
    }

    // ── Public API ────────────────────────────────────────────────
    fun startConnectFrom(nodeId: String) { connectMode = true; connectFromId = nodeId; invalidate() }

    fun getCanvasCenter(): Pair<Float, Float> = Pair((width/2f - vx)/vs, (height/2f - vy)/vs)

    fun centerView() {
        if (nodes.isEmpty()) return
        post {
            val shown = nodes.filter { isVisible(it.id) }.ifEmpty { nodes }
            val minX = shown.minOf { it.x }; val maxX = shown.maxOf { it.x + widthOf(it) }
            val minY = shown.minOf { it.y }; val maxY = shown.maxOf { it.y + NH }
            val cx = (minX + maxX)/2; val cy = (minY + maxY)/2
            val scaleX = width*0.85f / (maxX - minX + 120f)
            val scaleY = height*0.8f  / (maxY - minY + 120f)
            vs = scaleX.coerceAtMost(scaleY).coerceIn(0.3f, 1.4f)
            vx = width/2f - cx*vs; vy = height/2f - cy*vs
            invalidate()
        }
    }
}
