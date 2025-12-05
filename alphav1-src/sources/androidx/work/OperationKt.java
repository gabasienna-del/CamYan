package androidx.work;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Operation;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.jvm.internal.InlineMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CancellableContinuationImpl;

/* compiled from: Operation.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0015\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0086Hø\u0001\u0000¢\u0006\u0002\u0010\u0003\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u0004"}, d2 = {"await", "Landroidx/work/Operation$State$SUCCESS;", "Landroidx/work/Operation;", "(Landroidx/work/Operation;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "work-runtime_release"}, k = 2, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class OperationKt {
    /* JADX WARN: Failed to find 'out' block for switch in B:7:0x0022. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:11:0x002d  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x003b  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0025  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final java.lang.Object await(androidx.work.Operation r13, kotlin.coroutines.Continuation<? super androidx.work.Operation.State.SUCCESS> r14) {
        /*
            boolean r0 = r14 instanceof androidx.work.OperationKt$await$1
            if (r0 == 0) goto L14
            r0 = r14
            androidx.work.OperationKt$await$1 r0 = (androidx.work.OperationKt$await$1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r14 = r0.label
            int r14 = r14 - r2
            r0.label = r14
            goto L19
        L14:
            androidx.work.OperationKt$await$1 r0 = new androidx.work.OperationKt$await$1
            r0.<init>(r14)
        L19:
            r14 = r0
            java.lang.Object r0 = r14.result
            java.lang.Object r1 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r2 = r14.label
            switch(r2) {
                case 0: goto L3b;
                case 1: goto L2d;
                default: goto L25;
            }
        L25:
            java.lang.IllegalStateException r13 = new java.lang.IllegalStateException
            java.lang.String r14 = "call to 'resume' before 'invoke' with coroutine"
            r13.<init>(r14)
            throw r13
        L2d:
            r13 = 0
            r1 = 0
            r2 = 0
            java.lang.Object r3 = r14.L$0
            com.google.common.util.concurrent.ListenableFuture r3 = (com.google.common.util.concurrent.ListenableFuture) r3
            kotlin.ResultKt.throwOnFailure(r0)
            r4 = r2
            r2 = r0
            goto Lae
        L3b:
            kotlin.ResultKt.throwOnFailure(r0)
            r2 = 0
            com.google.common.util.concurrent.ListenableFuture r3 = r13.getResult()
            java.lang.String r13 = "result"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r3, r13)
            r13 = r3
            r3 = 0
            boolean r4 = r13.isDone()
            if (r4 == 0) goto L62
        L52:
            java.lang.Object r1 = r13.get()     // Catch: java.util.concurrent.ExecutionException -> L57
            goto Lb1
        L57:
            r13 = move-exception
            java.lang.Throwable r1 = r13.getCause()
            if (r1 != 0) goto L61
            r1 = r13
            java.lang.Throwable r1 = (java.lang.Throwable) r1
        L61:
            throw r1
        L62:
            r4 = 0
            r14.L$0 = r13
            r5 = 1
            r14.label = r5
            r6 = r14
            kotlin.coroutines.Continuation r6 = (kotlin.coroutines.Continuation) r6
            r7 = 0
            kotlinx.coroutines.CancellableContinuationImpl r8 = new kotlinx.coroutines.CancellableContinuationImpl
            kotlin.coroutines.Continuation r9 = kotlin.coroutines.intrinsics.IntrinsicsKt.intercepted(r6)
            r8.<init>(r9, r5)
            r5 = r8
            r5.initCancellability()
            r8 = r5
            kotlinx.coroutines.CancellableContinuation r8 = (kotlinx.coroutines.CancellableContinuation) r8
            r9 = 0
            androidx.work.ListenableFutureKt$await$2$1 r10 = new androidx.work.ListenableFutureKt$await$2$1
            r10.<init>(r8, r13)
            java.lang.Runnable r10 = (java.lang.Runnable) r10
            androidx.work.DirectExecutor r11 = androidx.work.DirectExecutor.INSTANCE
            java.util.concurrent.Executor r11 = (java.util.concurrent.Executor) r11
            r13.addListener(r10, r11)
            androidx.work.ListenableFutureKt$await$2$2 r10 = new androidx.work.ListenableFutureKt$await$2$2
            r10.<init>(r13)
            kotlin.jvm.functions.Function1 r10 = (kotlin.jvm.functions.Function1) r10
            r8.invokeOnCancellation(r10)
            java.lang.Object r13 = r5.getResult()
            java.lang.Object r5 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            if (r13 != r5) goto La7
            r5 = r14
            kotlin.coroutines.Continuation r5 = (kotlin.coroutines.Continuation) r5
            kotlin.coroutines.jvm.internal.DebugProbesKt.probeCoroutineSuspended(r5)
        La7:
            if (r13 != r1) goto Laa
            return r1
        Laa:
            r1 = r3
            r12 = r2
            r2 = r13
            r13 = r12
        Lae:
            r1 = r2
            r2 = r13
        Lb1:
            java.lang.String r13 = "result.await()"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r1, r13)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.work.OperationKt.await(androidx.work.Operation, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private static final Object await$$forInline(Operation $this$await, Continuation<? super Operation.State.SUCCESS> continuation) {
        Object obj;
        ListenableFuture result = $this$await.getResult();
        Intrinsics.checkNotNullExpressionValue(result, "result");
        ListenableFuture $this$await$iv = result;
        if ($this$await$iv.isDone()) {
            try {
                obj = $this$await$iv.get();
            } catch (ExecutionException e$iv) {
                Throwable cause = e$iv.getCause();
                if (cause != null) {
                    throw cause;
                }
                throw e$iv;
            }
        } else {
            InlineMarker.mark(0);
            Continuation<? super Operation.State.SUCCESS> uCont$iv$iv = continuation;
            CancellableContinuationImpl cancellable$iv$iv = new CancellableContinuationImpl(IntrinsicsKt.intercepted(uCont$iv$iv), 1);
            cancellable$iv$iv.initCancellability();
            CancellableContinuationImpl cancellableContinuation$iv = cancellable$iv$iv;
            $this$await$iv.addListener(new ListenableFutureKt$await$2$1(cancellableContinuation$iv, $this$await$iv), DirectExecutor.INSTANCE);
            cancellableContinuation$iv.invokeOnCancellation(new ListenableFutureKt$await$2$2($this$await$iv));
            Unit unit = Unit.INSTANCE;
            Object result2 = cancellable$iv$iv.getResult();
            if (result2 == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                DebugProbesKt.probeCoroutineSuspended(continuation);
            }
            InlineMarker.mark(1);
            obj = result2;
        }
        Intrinsics.checkNotNullExpressionValue(obj, "result.await()");
        return obj;
    }
}
