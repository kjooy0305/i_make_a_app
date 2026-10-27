package com.kjooy0305.dayclean.logic

import com.kjooy0305.dayclean.model.DayArchive
import com.kjooy0305.dayclean.model.DoneRec
import com.kjooy0305.dayclean.model.LateRec
import com.kjooy0305.dayclean.model.MemoRec

enum class DoneOrder { WRITTEN, RESOLVED }

/** 메모리 검색: 날짜(10.24, 10-24, 2026-10-24)나 내용의 일부. 띄어 쓴 말은 모두 맞아야 한다. */
object MemorySearch {
    class DayHits(val archive: DayArchive, val late: List<LateRec>, val memos: List<MemoRec>, val done: List<DoneRec>) {
        val count: Int get() = late.size + memos.size + done.size
    }

    class Result(val days: List<DayHits>, val tokens: List<String>) {
        val count: Int get() = days.sumOf { it.count }
    }

    fun tokens(query: String): List<String> = query.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }

    private fun dateForms(a: DayArchive): String {
        val d = a.date
        val mm = "%02d".format(d.monthValue)
        val dd = "%02d".format(d.dayOfMonth)
        return listOf("${d.year}-$mm-$dd", "${d.year}.$mm.$dd", "$mm-$dd", "$mm.$dd", "$mm$dd", "${d.year}$mm$dd",
            Dates.dow(d.dayOfWeek) + "요일").joinToString(" ")
    }

    private fun tokenMatches(tok: String, hay: String): Boolean =
        listOf(tok, tok.replace('.', '-').replace('/', '-'), tok.replace('-', '.').replace('/', '.')).any { hay.contains(it) }

    fun search(archive: List<DayArchive>, query: String, newestFirst: Boolean, order: DoneOrder): Result {
        val toks = tokens(query)
        val out = ArrayList<DayHits>()
        for (day in archive) {
            val forms = dateForms(day)
            fun ok(section: String, text: String): Boolean {
                val hay = "$forms $section $text".lowercase()
                return toks.all { tokenMatches(it, hay) }
            }
            val late = day.late.filter { ok("미룬 것", it.text) }
            val memos = day.memos.filter { ok("메모", it.text) }
            val done = day.done.filter { ok("해결한 내용 ${it.priority}", it.text) }
                .sortedBy { if (order == DoneOrder.WRITTEN) it.written else it.resolved }
            val hits = DayHits(day, late, memos, done)
            if (hits.count > 0) out.add(hits)
        }
        out.sortBy { it.archive.date }
        if (newestFirst) out.reverse()
        return Result(out, toks)
    }
}
