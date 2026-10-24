package com.tweber.stickfighter.p032b;

import androidx.appcompat.widget.PopupMenu;
import android.view.View;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.m */
/* JADX INFO: loaded from: classes.dex */
class ViewOnLongClickListenerC0821m implements View.OnLongClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0825q f3853a;

    /* JADX INFO: renamed from: b */
    final /* synthetic */ C0819k f3854b;

    ViewOnLongClickListenerC0821m(C0819k c0819k, C0825q c0825q) {
        this.f3854b = c0819k;
        this.f3853a = c0825q;
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view, 5);
        popupMenu.getMenu().add(0, R.id.copy, 0, view.getContext().getString(R.string.copy));
        popupMenu.getMenu().add(0, R.id.delete, 1, view.getContext().getString(R.string.delete));
        popupMenu.setOnMenuItemClickListener(new C0822n(this));
        popupMenu.show();
        return true;
    }
}
