package com.tweber.stickfighter.p037g;

import android.widget.SeekBar;

/* JADX INFO: loaded from: classes.dex */
class ax implements SeekBar.OnSeekBarChangeListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4073a;

    ax(ae aeVar) {
        this.f4073a = aeVar;
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        this.f4073a.f4019F.setText(this.f4073a.f4034f[i]);
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
