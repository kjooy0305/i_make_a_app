package com.tweber.stickfighter.p032b;

import androidx.appcompat.widget.PopupMenu;
import android.view.MenuItem;
import com.tweber.stickfighter.activities.R;

/* JADX INFO: renamed from: com.tweber.stickfighter.b.n */
/* JADX INFO: loaded from: classes.dex */
class C0822n implements PopupMenu.OnMenuItemClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ViewOnLongClickListenerC0821m f3855a;

    C0822n(ViewOnLongClickListenerC0821m viewOnLongClickListenerC0821m) {
        this.f3855a = viewOnLongClickListenerC0821m;
    }

    @Override // androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.copy /* 2131493111 */:
                this.f3855a.f3854b.m4522b(this.f3855a.f3853a);
                break;
            case R.id.delete /* 2131493112 */:
                this.f3855a.f3854b.m4519a(this.f3855a.f3853a);
                break;
        }
        return true;
    }
}
