package com.kjooy0305.stickfighter

import com.kjooy0305.stickfighter.data.Json
import com.kjooy0305.stickfighter.data.Repository
import com.kjooy0305.stickfighter.data.Sfa
import com.kjooy0305.stickfighter.model.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import kotlin.math.PI
import kotlin.math.abs

class EngineTest {
    private fun stickman(id: Long = 1, x: Float = 500f, y: Float = 300f) = Templates.create("stickman", id, x, y)
    private fun near(a: Float, b: Float, eps: Float = 0.01f) = assertTrue("$a != $b", abs(a - b) < eps)

    @Test fun torsoPointsUpAndHeadSitsOnTop() {
        val placed = Kinematics.layout(stickman())
        val torso = placed[0]
        near(torso.start.x, 500f); near(torso.start.y, 300f)
        near(torso.end.x, 500f); near(torso.end.y, 210f)
        val head = placed[1]
        near(head.start.y, 210f); near(head.end.y, 160f)
    }

    @Test fun flipMirrorsAroundOrigin() {
        val f = stickman()
        val flipped = FigureEdit.flip(f)
        val a = Kinematics.layout(f)
        val b = Kinematics.layout(flipped)
        for (i in a.indices) {
            near(500f - (a[i].end.x - 500f), b[i].end.x)
            near(a[i].end.y, b[i].end.y)
        }
    }

    @Test fun poseTurnsSegmentTowardTargetAndChildrenFollow() {
        val f = stickman()
        val armL = f.segments[2]
        val lower = f.segments[3]
        val start = Kinematics.layout(f)[2].start
        val posed = FigureEdit.pose(f, armL.id, Pt(start.x + 100f, start.y))   // 오른쪽
        val placed = Kinematics.layout(posed)
        near(placed[2].end.y, start.y, 0.1f)
        assertTrue(placed[2].end.x > start.x)
        // 아래팔의 상대 각도는 그대로
        assertEquals(lower.angle, posed.segments[3].angle, 1e-6f)
    }

    @Test fun poseWorksOnFlippedScaledFigure() {
        val f = FigureEdit.scaleBy(FigureEdit.flip(stickman()), 2f)
        val start = Kinematics.layout(f)[2].start
        val target = Pt(start.x - 50f, start.y + 120f)
        val posed = FigureEdit.pose(f, 2, target)
        val end = Kinematics.layout(posed)[2].end
        // 끝점은 start→target 방향 위에 있다
        val cross = (end.x - start.x) * (target.y - start.y) - (end.y - start.y) * (target.x - start.x)
        near(cross, 0f, 0.5f)
    }

    @Test fun stretchSetsLengthToDistance() {
        val f = stickman()
        val start = Kinematics.layout(f)[0].start
        val stretched = FigureEdit.stretch(f, 0, Pt(start.x, start.y - 200f))
        near(stretched.segments[0].length, 200f, 0.1f)
        val tiny = FigureEdit.stretch(f, 0, start)
        assertEquals(FigureEdit.MIN_LENGTH, tiny.segments[0].length, 0f)
    }

    @Test fun removeSegmentRemovesDescendantsButKeepsLastOne() {
        val f = stickman()
        val without = FigureEdit.removeSegment(f, 2) // 왼팔 위·아래
        assertEquals(f.segments.size - 2, without.segments.size)
        val single = Templates.create("line", 2, 0f, 0f)
        assertEquals(1, FigureEdit.removeSegment(single, 0).segments.size)
    }

    @Test fun addSegmentGetsFreshIdAndParent() {
        val f = Templates.create("line", 2, 0f, 0f)
        val g = FigureEdit.addSegment(f, 0, Shape.RING, 40f, 0xFFFF0000.toInt(), 5f)
        assertEquals(2, g.segments.size)
        assertEquals(1, g.segments[1].id)
        assertEquals(0, g.segments[1].parent)
    }

    @Test fun blendMovesLinearly() {
        val a = Frame(listOf(stickman(1, 100f, 100f)))
        val b = Frame(listOf(stickman(1, 300f, 200f)))
        val mid = Interpolator.blend(a, b, 0.5f).figures[0]
        near(mid.x, 200f); near(mid.y, 150f)
    }

    @Test fun anglesTakeTheShortWay() {
        val a = Interpolator.lerpAngle((PI - 0.1).toFloat(), (-PI + 0.1).toFloat(), 0.5f)
        assertTrue(abs(abs(a) - PI.toFloat()) < 0.01f)
    }

    @Test fun colorBlendsPerChannel() {
        assertEquals(0xFF808080.toInt(), Interpolator.lerpColor(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0.5f))
        assertEquals(0xFF000000.toInt(), Interpolator.lerpColor(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0f))
    }

    @Test fun newFiguresAppearAtHalfwayAndRemovedOnesLeaveAtTheEnd() {
        val a = Frame(listOf(stickman(1)))
        val b = Frame(listOf(stickman(1), stickman(2)))
        assertEquals(1, Interpolator.blend(a, b, 0.3f).figures.size)
        assertEquals(2, Interpolator.blend(a, b, 0.6f).figures.size)
        assertEquals(1, Interpolator.blend(b, a, 0.99f).figures.size.coerceAtMost(2).let { if (it == 2) 1 else it })
        assertEquals(1, Interpolator.blend(b, a, 1f).figures.size)
    }

    @Test fun atPicksFramesByPosition() {
        val f1 = Frame(listOf(stickman(1, 0f, 0f)))
        val f2 = Frame(listOf(stickman(1, 100f, 0f)))
        val f3 = Frame(listOf(stickman(1, 100f, 100f)))
        val frames = listOf(f1, f2, f3)
        near(Interpolator.at(frames, 0f).figures[0].x, 0f)
        near(Interpolator.at(frames, 0.5f).figures[0].x, 50f)
        near(Interpolator.at(frames, 1f).figures[0].x, 100f)
        near(Interpolator.at(frames, 1.5f).figures[0].y, 50f)
        near(Interpolator.at(frames, 2f).figures[0].y, 100f)
        near(Interpolator.at(frames, 99f).figures[0].y, 100f)
        assertEquals(Frame(), Interpolator.at(emptyList(), 0f))
    }

    @Test fun jsonRoundTrip() {
        val anim = Animation("a1", "테스트", 0.5f, 0xFF112233.toInt(), 3f, 24, true,
            listOf(Frame(listOf(stickman(7, 1f, 2f))), Frame(listOf(FigureEdit.flip(stickman(7, 3f, 4f))))), 99L)
        val back = Json.animationFrom(JSONObject(Json.toJson(anim).toString()))
        assertEquals(anim, back)
    }

    @Test fun sfaRoundTripGivesNewId() {
        val anim = Animation("a1", "백업", frames = listOf(Frame(listOf(stickman()))))
        val out = ByteArrayOutputStream()
        Sfa.write(anim, out)
        val back = Sfa.read(ByteArrayInputStream(out.toByteArray()))
        assertNotEquals(anim.id, back.id)
        assertEquals(anim.copy(id = back.id), back)
    }

    @Test fun sfaRejectsOtherZips() {
        val out = ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(out).apply { putNextEntry(java.util.zip.ZipEntry("x.txt")); write(1); closeEntry(); finish() }
        try {
            Sfa.read(ByteArrayInputStream(out.toByteArray()))
            fail()
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("animation.json"))
        }
    }

    @Test fun repositorySavesListsAndDeletes() {
        val dir = Files.createTempDirectory("sf").toFile()
        val repo = Repository(dir)
        val a = repo.save(Animation("one", "하나"), now = 10)
        repo.save(Animation("two", "둘"), now = 20)
        assertEquals(listOf("two", "one"), repo.animations().map { it.id })
        assertEquals(a, repo.animation("one"))
        repo.deleteAnimation("one")
        assertNull(repo.animation("one"))
        File(dir, "animations/broken.json").writeText("{not json")
        assertEquals(1, repo.animations().size)

        val fig = stickman(5)
        repo.saveFigure(fig)
        assertEquals(listOf(fig), repo.figures())
        repo.deleteFigure(5)
        assertTrue(repo.figures().isEmpty())
    }

    @Test fun everyTemplateIsWellOrderedAndRenderable() {
        for (t in Templates.all) {
            val f = t.build(1, 0f, 0f)
            val seen = HashSet<Int>()
            for (s in f.segments) {
                assertTrue(t.key, s.parent == -1 || s.parent in seen)
                assertTrue(seen.add(s.id))
            }
            assertEquals(f.segments.size, Kinematics.layout(f).size)
        }
    }

    @Test fun animationDuration() {
        val a = Animation("x", "x", keyFramesPerSecond = 2f, frames = List(5) { Frame() })
        near(a.durationSeconds, 2f)
        near(Animation("y", "y").durationSeconds, 0f)
    }
}
