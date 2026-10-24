package com.kjooy0305.stickfighter.data

import com.kjooy0305.stickfighter.model.Animation
import com.kjooy0305.stickfighter.model.Figure
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** 앱 전용 폴더에 JSON 파일로 저장한다. 권한이 필요 없다. */
class Repository(root: File) {
    private val animationDir = File(root, "animations").also { it.mkdirs() }
    private val figureDir = File(root, "figures").also { it.mkdirs() }

    fun newId(): String = UUID.randomUUID().toString()

    // ---- 애니메이션 ----

    /** 최근에 고친 순서. 깨진 파일은 건너뛴다. */
    fun animations(): List<Animation> =
        (animationDir.listFiles { f -> f.extension == "json" } ?: emptyArray())
            .mapNotNull { runCatching { Json.animationFrom(JSONObject(it.readText())) }.getOrNull() }
            .sortedByDescending { it.updatedAt }

    fun animation(id: String): Animation? =
        runCatching { Json.animationFrom(JSONObject(File(animationDir, "$id.json").readText())) }.getOrNull()

    fun save(animation: Animation, now: Long = System.currentTimeMillis()): Animation {
        val saved = animation.copy(updatedAt = now)
        write(File(animationDir, "${saved.id}.json"), Json.toJson(saved).toString())
        return saved
    }

    fun deleteAnimation(id: String) {
        File(animationDir, "$id.json").delete()
    }

    // ---- 내 피규어 ----

    fun figures(): List<Figure> =
        (figureDir.listFiles { f -> f.extension == "json" } ?: emptyArray())
            .sortedBy { it.lastModified() }
            .mapNotNull { runCatching { Json.figureFrom(JSONObject(it.readText())) }.getOrNull() }

    /** 같은 id의 피규어가 있으면 덮어쓴다. */
    fun saveFigure(figure: Figure) {
        write(File(figureDir, "${figure.id}.json"), Json.toJson(figure).toString())
    }

    fun deleteFigure(id: Long) {
        File(figureDir, "$id.json").delete()
    }

    // 쓰는 도중 꺼져도 기존 파일이 깨지지 않게 임시 파일에 쓴 뒤 바꾼다.
    private fun write(target: File, text: String) {
        val tmp = File(target.parentFile, target.name + ".tmp")
        tmp.writeText(text)
        if (!tmp.renameTo(target)) {
            target.writeText(text)
            tmp.delete()
        }
    }
}
