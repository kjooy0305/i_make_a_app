package com.tweber.stickfighter.p033c;

import androidx.collection.LruCache;
import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p038h.C0918p;

/* JADX INFO: renamed from: com.tweber.stickfighter.c.b */
/* JADX INFO: loaded from: classes.dex */
class C0835b extends LruCache<Integer, C0918p> {

    /* JADX INFO: renamed from: a */
    final long f3885a;

    public C0835b(long j) {
        super(50);
        this.f3885a = j;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.collection.LruCache
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public C0918p create(Integer num) {
        return C0836a.m4547a().mo4564b(this.f3885a, num.intValue());
    }
}
