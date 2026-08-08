package com.maybeitok.epubmaker

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var language: String
        get() = prefs.getString("language", "ko") ?: "ko"
        set(v) = prefs.edit().putString("language", v).apply()

    var openMode: String  // "editor" or "viewer"
        get() = prefs.getString("open_mode", "editor") ?: "editor"
        set(v) = prefs.edit().putString("open_mode", v).apply()

    // Viewer settings
    var viewerNavMode: String
        get() = prefs.getString("viewer_nav_mode", "scroll") ?: "scroll"
        set(v) = prefs.edit().putString("viewer_nav_mode", v).apply()

    var viewerReverseNav: Boolean
        get() = prefs.getBoolean("viewer_reverse_nav", false)
        set(v) = prefs.edit().putBoolean("viewer_reverse_nav", v).apply()

    var viewerFontSize: Int
        get() = prefs.getInt("viewer_font_size", 18)
        set(v) = prefs.edit().putInt("viewer_font_size", v).apply()

    var viewerScrollLocked: Boolean
        get() = prefs.getBoolean("viewer_scroll_locked", false)
        set(v) = prefs.edit().putBoolean("viewer_scroll_locked", v).apply()

    var viewerPreventImageClip: Boolean
        get() = prefs.getBoolean("viewer_prevent_image_clip", true)
        set(v) = prefs.edit().putBoolean("viewer_prevent_image_clip", v).apply()
}
