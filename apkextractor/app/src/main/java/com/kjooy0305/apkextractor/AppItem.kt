package com.kjooy0305.apkextractor

import android.graphics.drawable.Drawable

data class AppItem(
    val label: String,
    val packageName: String,
    val versionName: String,
    val sourceDir: String,
    val splitDirs: List<String>,
    val sizeBytes: Long,
    val isSystem: Boolean,
    var icon: Drawable? = null
) {
    val isSplit: Boolean get() = splitDirs.isNotEmpty()
}
