package com.tweber.stickfighter.p037g;

import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import com.tweber.stickfighter.activities.R;
import com.tweber.stickfighter.activities.SequencePlayerActivity;
import com.tweber.stickfighter.p036f.C0866b;
import com.tweber.stickfighter.p036f.EnumC0867c;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: loaded from: classes.dex */
class ab implements View.OnClickListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ aa f4008a;

    ab(aa aaVar) {
        this.f4008a = aaVar;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (C0924v.m4932a().m4939f().m4538a() < 2) {
            Snackbar.make(view, this.f4008a.getString(R.string.minimum_frames_to_play_video), 0).show();
            return;
        }
        de.greenrobot.event.EventBus.getDefault().post(new C0866b(EnumC0867c.FRAME_DRAW));
        view.getContext().startActivity(new Intent(view.getContext(), (Class<?>) SequencePlayerActivity.class));
    }
}
