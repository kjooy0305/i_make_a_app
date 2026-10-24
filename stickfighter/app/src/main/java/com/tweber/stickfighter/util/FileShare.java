package com.tweber.stickfighter.util;

import android.content.Context;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.tweber.stickfighter.StickFighterApplication;
import java.io.File;

/** Hands exported files to other apps through a content:// URI (file:// URIs are blocked since Android 7). */
public final class FileShare {
    private FileShare() {
    }

    public static Uri uri(File file) {
        Context context = StickFighterApplication.m4490a();
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }
}
