package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.AppState
import com.kjooy0305.dayclean.model.DayArchive
import com.kjooy0305.dayclean.model.Disposition
import com.kjooy0305.dayclean.model.DoneRec
import com.kjooy0305.dayclean.model.Item
import com.kjooy0305.dayclean.model.Kind
import com.kjooy0305.dayclean.model.LateRec
import com.kjooy0305.dayclean.model.MemoRec
import java.time.LocalDate

/** 메모리를 txt로 내보내고 불러오는 형식. 사람이 읽을 수 있고, 불러올 때도 같은 글자를 쓴다. */
object MemoryText {
    const val HEADER = "하루 정리 기록 v1"
    private const val LATE = "미룬 것"
    private const val MEMO = "메모"
    private const val DONE = "해결한 내용"
    private const val LEFT = "남은 것"

    private class Remaining(val kind: Kind, val time: String, val text: String)
    private class Block(
        val date: LocalDate,
        val late: MutableList<LateRec> = ArrayList(),
        val memos: MutableList<MemoRec> = ArrayList(),
        val done: MutableList<DoneRec> = ArrayList(),
        val left: MutableList<Remaining> = ArrayList()
    )

    class ImportResult(val state: AppState, val days: Int, val entries: Int, val skipped: Int)

    // ---- 내보내기 ----

    /** 오늘 기록장에 있는 내용만. */
    fun exportToday(state: AppState, today: LocalDate): String {
        val sb = StringBuilder(HEADER).append('\n')
        liveBlock(state, today)?.let { writeBlock(sb, it) }
        return sb.toString()
    }

    /** 메모리에 있는 모든 날과, 아직 옮겨지지 않은 오늘 기록장. */
    fun exportAll(state: AppState, today: LocalDate): String {
        val sb = StringBuilder(HEADER).append('\n')
        liveBlock(state, today)?.let { writeBlock(sb, it) }
        for (d in state.archive.sortedByDescending { it.date }) {
            if (d.date == today) continue
            writeBlock(sb, Block(d.date, d.late.toMutableList(), d.memos.toMutableList(), d.done.toMutableList()))
        }
        return sb.toString()
    }

    private fun liveBlock(state: AppState, today: LocalDate): Block? {
        val items = state.items.filter { it.date == today && it.disposition != Disposition.DROP }
        if (items.isEmpty()) return null
        val b = Block(today)
        for (i in items.sortedBy { it.createdAt }) {
            when {
                i.kind == Kind.MEMO -> b.memos.add(MemoRec(Dates.hhmm(i.createdAt), i.text))
                i.done -> b.done.add(DoneRec(Dates.hhmm(i.createdAt), Dates.hhmm(i.doneAt ?: i.createdAt), i.kind, i.text))
                else -> b.left.add(Remaining(i.kind, Dates.hhmm(i.createdAt), i.text))
            }
        }
        return b
    }

    private fun writeBlock(sb: StringBuilder, b: Block) {
        sb.append('\n').append('[').append(b.date).append("]\n")
        if (b.late.isNotEmpty()) {
            sb.append(LATE).append('\n')
            b.late.forEach { sb.append("  ").append(it.time).append(" | ").append(esc(it.text)).append('\n') }
        }
        if (b.memos.isNotEmpty()) {
            sb.append(MEMO).append('\n')
            b.memos.forEach { sb.append("  ").append(it.time).append(" | ").append(esc(it.text)).append('\n') }
        }
        if (b.done.isNotEmpty()) {
            sb.append(DONE).append('\n')
            b.done.forEach {
                sb.append("  ").append(it.written).append(" | ").append(it.resolved).append(" | ").append(it.priority)
                    .append(" | ").append(esc(it.text)).append('\n')
            }
        }
        if (b.left.isNotEmpty()) {
            sb.append(LEFT).append('\n')
            b.left.forEach { sb.append("  ").append(it.kind.glyph).append(' ').append(it.time).append(" | ").append(esc(it.text)).append('\n') }
        }
    }

    // ---- 불러오기 ----

    /** 파일의 모든 내용을 오늘 기록장에 넣는다. 날짜는 무시하고, 이미 있는 항목은 건너뛴다. */
    fun importToday(state: AppState, text: String, today: LocalDate, nowMs: Long): ImportResult {
        val blocks = parse(text)
        var s = state
        var added = 0
        var skipped = 0
        for (b in blocks) {
            val r = addLive(s, b, today, nowMs)
            s = r.first; added += r.second; skipped += r.third
        }
        return ImportResult(s, blocks.size, added, skipped)
    }

    /** 날짜별로 메모리에 합친다. 오늘 날짜는 오늘 기록장에 넣고, 이미 있는 항목은 건너뛰고, 미래 날짜는 무시한다. */
    fun importAll(state: AppState, text: String, today: LocalDate, nowMs: Long): ImportResult {
        val blocks = parse(text)
        var s = state
        var added = 0
        var skipped = 0
        for (b in blocks) {
            when {
                b.date == today -> { val r = addLive(s, b, today, nowMs); s = r.first; added += r.second; skipped += r.third }
                b.date.isAfter(today) -> skipped += b.late.size + b.memos.size + b.done.size + b.left.size
                else -> {
                    val old = s.archive.firstOrNull { it.date == b.date }
                    val late = b.late + b.left.map { LateRec(it.time, it.text) }
                    val merged = Rollover.mergeArchive(
                        s.archive, DayArchive(b.date, late, b.memos, b.done))
                    val after = merged.firstOrNull { it.date == b.date }
                    val before = (old?.late?.size ?: 0) + (old?.memos?.size ?: 0) + (old?.done?.size ?: 0)
                    val now = (after?.late?.size ?: 0) + (after?.memos?.size ?: 0) + (after?.done?.size ?: 0)
                    added += now - before
                    skipped += (late.size + b.memos.size + b.done.size) - (now - before)
                    s = s.copy(archive = merged.sortedByDescending { it.date })
                }
            }
        }
        return ImportResult(s, blocks.size, added, skipped)
    }

    private fun addLive(state: AppState, b: Block, today: LocalDate, nowMs: Long): Triple<AppState, Int, Int> {
        var s = state
        var added = 0
        var skipped = 0
        fun exists(kind: Kind, text: String, createdAt: Long) =
            s.items.any { it.date == today && it.kind == kind && it.text == text && Dates.hhmm(it.createdAt) == Dates.hhmm(createdAt) }
        fun put(kind: Kind, textRaw: String, written: String, resolved: String?) {
            val created = Dates.atTime(today, written) ?: nowMs
            if (exists(kind, textRaw, created)) { skipped++; return }
            val (id, next) = s.alloc()
            var item = Item(id, kind, textRaw, today, created)
            if (resolved != null) item = item.copy(done = true, doneAt = Dates.atTime(today, resolved) ?: created)
            else item = item.copy(nextAlertAt = Reminders.initialAlert(item, next.settings, nowMs))
            s = next.copy(items = next.items + item)
            added++
        }
        b.memos.forEach { put(Kind.MEMO, it.text, it.time, null) }
        b.done.forEach { put(it.kind, it.text, it.written, it.resolved) }
        b.left.forEach { put(it.kind, it.text, it.time, null) }
        skipped += b.late.size   // 미룬 것은 지난 일의 기록이라 오늘 기록장에는 넣지 않는다
        return Triple(s, added, skipped)
    }

    private fun parse(text: String): List<Block> {
        val blocks = ArrayList<Block>()
        var cur: Block? = null
        var section = ""
        for (raw in text.lines()) {
            val line = raw.trimEnd()
            if (line.isBlank() || line.trim() == HEADER) continue
            val dm = Regex("^\\[(\\d{4}-\\d{2}-\\d{2})]$").matchEntire(line.trim())
            if (dm != null) {
                val date = runCatching { LocalDate.parse(dm.groupValues[1]) }.getOrNull()
                cur = if (date != null) Block(date).also { blocks.add(it) } else null
                section = ""
                continue
            }
            if (!raw.startsWith(" ") && !raw.startsWith("\t")) { section = line.trim(); continue }
            val b = cur ?: continue
            val parts = splitFields(line.trim())
            when (section) {
                LATE -> if (parts.size >= 2 && validTime(parts[0])) b.late.add(LateRec(parts[0], parts[1]))
                MEMO -> if (parts.size >= 2 && validTime(parts[0])) b.memos.add(MemoRec(parts[0], parts[1]))
                DONE -> if (parts.size >= 4 && validTime(parts[0]) && validTime(parts[1])) {
                    val kind = if (parts[2] == "지금 우선") Kind.NOW else Kind.LATER
                    b.done.add(DoneRec(parts[0], parts[1], kind, parts[3]))
                }
                LEFT -> {
                    val m = Regex("^([•\\-=])\\s+(\\d{1,2}:\\d{2})$").matchEntire(parts.firstOrNull() ?: "")
                    if (m != null && parts.size >= 2) {
                        val kind = Kind.of(m.groupValues[1])
                        if (kind != null && kind != Kind.MEMO) b.left.add(Remaining(kind, m.groupValues[2], parts[1]))
                    }
                }
            }
        }
        return blocks
    }

    private fun validTime(t: String) = Dates.parseHhmm(t) != null

    private fun esc(t: String): String = t.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n")

    /** 이스케이프되지 않은 | 로 나누고 각 조각을 원래 글자로 되돌린다. */
    private fun splitFields(line: String): List<String> {
        val out = ArrayList<String>()
        val cur = StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\\' && i + 1 < line.length -> {
                    val n = line[i + 1]
                    cur.append(if (n == 'n') '\n' else n)
                    i += 2
                }
                c == '|' -> { out.add(cur.toString().trim()); cur.setLength(0); i++ }
                else -> { cur.append(c); i++ }
            }
        }
        out.add(cur.toString().trim())
        return out
    }
}
