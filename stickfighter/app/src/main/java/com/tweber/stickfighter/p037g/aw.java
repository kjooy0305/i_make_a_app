package com.tweber.stickfighter.p037g;

import android.widget.SeekBar;

/* JADX INFO: loaded from: classes.dex */
class aw implements SeekBar.OnSeekBarChangeListener {

    /* JADX INFO: renamed from: a */
    final /* synthetic */ ae f4072a;

    aw(ae aeVar) {
        this.f4072a = aeVar;
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        this.f4072a.f4017D.setText(String.valueOf(this.f4072a.f4032d[i]));
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
