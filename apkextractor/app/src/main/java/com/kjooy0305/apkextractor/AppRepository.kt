package com.kjooy0305.apkextractor

import android.content.Context
import android.content.pm.ApplicationInfo
import java.io.File
import java.util.Locale

object AppRepository {

    @Suppress("DEPRECATION")
    fun load(context: Context): List<AppItem> {
        val pm = context.packageManager
        return pm.getInstalledPackages(0).mapNotNull { info ->
            val app = info.applicationInfo ?: return@mapNotNull null
            if (app.sourceDir.isNullOrEmpty()) return@mapNotNull null
            val splits = app.splitSourceDirs?.toList().orEmpty()
            val size = (listOf(app.sourceDir) + splits).sumOf { File(it).length() }
            val system = app.flags and ApplicationInfo.FLAG_SYSTEM != 0 &&
                app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0
            AppItem(
                label = pm.getApplicationLabel(app).toString(),
                packageName = info.packageName,
                versionName = info.versionName ?: "?",
                sourceDir = app.sourceDir,
                splitDirs = splits,
                sizeBytes = size,
                isSystem = system,
                icon = runCatching { pm.getApplicationIcon(app) }.getOrNull()
            )
        }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
}
