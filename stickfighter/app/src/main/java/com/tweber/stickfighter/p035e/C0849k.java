package com.tweber.stickfighter.p035e;

import android.widget.SeekBar;

/* JADX INFO: renamed from: com.tweber.stickfighter.e.k */
/* JADX INFO: loaded from: classes.dex */
class C0849k implements SeekBar.OnSeekBarChangeListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ C0843e f3952a;

    C0849k(C0843e c0843e) {
        this.f3952a = c0843e;
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        this.f3952a.f3940f.setText(this.f3952a.f3936b[i]);
        this.f3952a.f3943i = this.f3952a.f3935a[i];
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
