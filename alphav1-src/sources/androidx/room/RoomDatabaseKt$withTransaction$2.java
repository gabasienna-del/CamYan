package androidx.room;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* JADX INFO: Access modifiers changed from: package-private */
/* JADX INFO: Add missing generic type declarations: [R] */
/* compiled from: RoomDatabaseExt.kt */
@Metadata(d1 = {"\u0000\b\n\u0002\b\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\u00020\u0002H\u008a@"}, d2 = {"<anonymous>", "R", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 7, 1}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@DebugMetadata(c = "androidx.room.RoomDatabaseKt$withTransaction$2", f = "RoomDatabaseExt.kt", i = {0}, l = {59}, m = "invokeSuspend", n = {"transactionElement"}, s = {"L$0"})
/* loaded from: classes.dex */
public final class RoomDatabaseKt$withTransaction$2<R> extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super R>, Object> {
    final /* synthetic */ Function1<Continuation<? super R>, Object> $block;
    final /* synthetic */ RoomDatabase $this_withTransaction;
    private /* synthetic */ Object L$0;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    public RoomDatabaseKt$withTransaction$2(RoomDatabase roomDatabase, Function1<? super Continuation<? super R>, ? extends Object> function1, Continuation<? super RoomDatabaseKt$withTransaction$2> continuation) {
        super(2, continuation);
        this.$this_withTransaction = roomDatabase;
        this.$block = function1;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        RoomDatabaseKt$withTransaction$2 roomDatabaseKt$withTransaction$2 = new RoomDatabaseKt$withTransaction$2(this.$this_withTransaction, this.$block, continuation);
        roomDatabaseKt$withTransaction$2.L$0 = obj;
        return roomDatabaseKt$withTransaction$2;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super R> continuation) {
        return ((RoomDatabaseKt$withTransaction$2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:2:0x0006. Please report as an issue. */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Object invokeSuspend(Object $result) {
        RoomDatabaseKt$withTransaction$2 roomDatabaseKt$withTransaction$2;
        TransactionElement transactionElement;
        Throwable th;
        Throwable th2;
        RoomDatabaseKt$withTransaction$2 roomDatabaseKt$withTransaction$22;
        TransactionElement transactionElement2;
        Object $result2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                roomDatabaseKt$withTransaction$2 = this;
                CoroutineScope $this$withContext = (CoroutineScope) roomDatabaseKt$withTransaction$2.L$0;
                CoroutineContext.Element element = $this$withContext.getCoroutineContext().get(TransactionElement.INSTANCE);
                Intrinsics.checkNotNull(element);
                transactionElement = (TransactionElement) element;
                transactionElement.acquire();
                try {
                    roomDatabaseKt$withTransaction$2.$this_withTransaction.beginTransaction();
                    try {
                        Function1<Continuation<? super R>, Object> function1 = roomDatabaseKt$withTransaction$2.$block;
                        roomDatabaseKt$withTransaction$2.L$0 = transactionElement;
                        roomDatabaseKt$withTransaction$2.label = 1;
                        Object invoke = function1.invoke(roomDatabaseKt$withTransaction$2);
                        if (invoke == coroutine_suspended) {
                            return coroutine_suspended;
                        }
                        $result2 = $result;
                        $result = invoke;
                        try {
                            roomDatabaseKt$withTransaction$2.$this_withTransaction.setTransactionSuccessful();
                            try {
                                roomDatabaseKt$withTransaction$2.$this_withTransaction.endTransaction();
                                transactionElement.release();
                                return $result;
                            } catch (Throwable th3) {
                                th = th3;
                                transactionElement.release();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            TransactionElement transactionElement3 = transactionElement;
                            th2 = th4;
                            $result = $result2;
                            roomDatabaseKt$withTransaction$22 = roomDatabaseKt$withTransaction$2;
                            transactionElement2 = transactionElement3;
                            try {
                                roomDatabaseKt$withTransaction$22.$this_withTransaction.endTransaction();
                                throw th2;
                            } catch (Throwable th5) {
                                th = th5;
                                transactionElement = transactionElement2;
                                transactionElement.release();
                                throw th;
                            }
                        }
                    } catch (Throwable th6) {
                        th2 = th6;
                        roomDatabaseKt$withTransaction$22 = roomDatabaseKt$withTransaction$2;
                        transactionElement2 = transactionElement;
                        roomDatabaseKt$withTransaction$22.$this_withTransaction.endTransaction();
                        throw th2;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    transactionElement.release();
                    throw th;
                }
            case 1:
                roomDatabaseKt$withTransaction$22 = this;
                transactionElement2 = (TransactionElement) roomDatabaseKt$withTransaction$22.L$0;
                try {
                    ResultKt.throwOnFailure($result);
                    transactionElement = transactionElement2;
                    roomDatabaseKt$withTransaction$2 = roomDatabaseKt$withTransaction$22;
                    $result2 = $result;
                    roomDatabaseKt$withTransaction$2.$this_withTransaction.setTransactionSuccessful();
                    roomDatabaseKt$withTransaction$2.$this_withTransaction.endTransaction();
                    transactionElement.release();
                    return $result;
                } catch (Throwable th8) {
                    th2 = th8;
                    roomDatabaseKt$withTransaction$22.$this_withTransaction.endTransaction();
                    throw th2;
                }
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
