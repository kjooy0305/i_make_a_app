package com.tweber.stickfighter.p039i;

import com.tweber.stickfighter.p034d.C0836a;
import com.tweber.stickfighter.p034d.InterfaceC0837b;
import com.tweber.stickfighter.p036f.C0874j;
import com.tweber.stickfighter.p036f.EnumC0867c;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;

/* JADX INFO: renamed from: com.tweber.stickfighter.i.b */
/* JADX INFO: loaded from: classes.dex */
public class AsyncTaskC0927b extends AbstractAsyncTaskC0926a {

    /* JADX INFO: renamed from: a */
    C0924v f4255a;

    public AsyncTaskC0927b(C0924v c0924v) {
        this.f4255a = c0924v;
    }

    @Override // com.tweber.stickfighter.p039i.AbstractAsyncTaskC0926a
    /* JADX INFO: renamed from: a */
    protected EnumC0867c mo4940a() {
        return EnumC0867c.DELETE_SEQUENCE;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.tweber.stickfighter.p039i.AbstractAsyncTaskC0931f
    /* JADX INFO: renamed from: c, reason: merged with bridge method [inline-methods] */
    public C0874j mo4943d() {
        InterfaceC0837b interfaceC0837bM4547a = C0836a.m4547a();
        try {
            interfaceC0837bM4547a.mo4561a(this.f4255a);
            int iMo4562b = C0836a.m4547a().mo4562b(this.f4255a.m4935b());
            for (int i = 0; i < iMo4562b; i++) {
                if (isCancelled()) {
                    interfaceC0837bM4547a.mo4574e();
                    return new C0874j(this.f4255a, false, "User canceled delete");
                }
                C0918p c0918pM4539a = this.f4255a.m4939f().m4539a(0);
                this.f4255a.m4939f().m4545c(0);
                interfaceC0837bM4547a.mo4572c(c0918pM4539a);
            }
            interfaceC0837bM4547a.mo4568b(this.f4255a);
            return new C0874j(this.f4255a, true, null);
        } catch (Exception e) {
            return new C0874j(this.f4255a, false, e.getMessage());
        }
    }
}
