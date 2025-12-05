package androidx.work;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: CoroutineWorker.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "androidx.work.CoroutineWorker$startWork$1", f = "CoroutineWorker.kt", i = {}, l = {68}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: classes.dex */
final class CoroutineWorker$startWork$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    int label;
    final /* synthetic */ CoroutineWorker this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public CoroutineWorker$startWork$1(CoroutineWorker coroutineWorker, Continuation<? super CoroutineWorker$startWork$1> continuation) {
        super(2, continuation);
        this.this$0 = coroutineWorker;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new CoroutineWorker$startWork$1(this.this$0, continuation);
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((CoroutineWorker$startWork$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:2:0x0006. Please report as an issue. */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object $result) {
        CoroutineWorker$startWork$1 coroutineWorker$startWork$1;
        Throwable t;
        CoroutineWorker$startWork$1 coroutineWorker$startWork$12;
        Object doWork;
        Object $result2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                coroutineWorker$startWork$1 = this;
                try {
                    coroutineWorker$startWork$1.label = 1;
                    doWork = coroutineWorker$startWork$1.this$0.doWork(coroutineWorker$startWork$1);
                } catch (Throwable th) {
                    t = th;
                    coroutineWorker$startWork$12 = coroutineWorker$startWork$1;
                    coroutineWorker$startWork$12.this$0.getFuture$work_runtime_release().setException(t);
                    return Unit.INSTANCE;
                }
                if (doWork == coroutine_suspended) {
                    return coroutine_suspended;
                }
                $result2 = $result;
                $result = doWork;
                try {
                    ListenableWorker.Result result = (ListenableWorker.Result) $result;
                    coroutineWorker$startWork$1.this$0.getFuture$work_runtime_release().set(result);
                } catch (Throwable th2) {
                    CoroutineWorker$startWork$1 coroutineWorker$startWork$13 = coroutineWorker$startWork$1;
                    t = th2;
                    $result = $result2;
                    coroutineWorker$startWork$12 = coroutineWorker$startWork$13;
                    coroutineWorker$startWork$12.this$0.getFuture$work_runtime_release().setException(t);
                    return Unit.INSTANCE;
                }
                return Unit.INSTANCE;
            case 1:
                coroutineWorker$startWork$12 = this;
                try {
                    ResultKt.throwOnFailure($result);
                    coroutineWorker$startWork$1 = coroutineWorker$startWork$12;
                    $result2 = $result;
                    ListenableWorker.Result result2 = (ListenableWorker.Result) $result;
                    coroutineWorker$startWork$1.this$0.getFuture$work_runtime_release().set(result2);
                } catch (Throwable th3) {
                    t = th3;
                    coroutineWorker$startWork$12.this$0.getFuture$work_runtime_release().setException(t);
                    return Unit.INSTANCE;
                }
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
