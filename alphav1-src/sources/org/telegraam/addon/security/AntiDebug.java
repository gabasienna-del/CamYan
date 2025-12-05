package org.telegraam.addon.security;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Metadata;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.ranges.IntRange;
import kotlin.text.StringsKt;

/* compiled from: AntiDebug.kt */
@Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\bÆ\u0002\u0018\u00002\u00020\u0001:\u0001\u001dB\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u0002J\u0010\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u0004J\u0018\u0010\r\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0004H\u0002J\b\u0010\u0011\u001a\u00020\bH\u0002J\b\u0010\u0012\u001a\u00020\bH\u0002J\u0010\u0010\u0013\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u0004H\u0002J\u0010\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0004H\u0002J\b\u0010\u0016\u001a\u00020\bH\u0002J\u0006\u0010\u0017\u001a\u00020\u0018J\u0010\u0010\u0019\u001a\u00020\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\u001bJ\u0006\u0010\u001c\u001a\u00020\u000bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001e"}, d2 = {"Lorg/telegraam/addon/security/AntiDebug;", "", "()V", "TAG", "", "running", "Ljava/util/concurrent/atomic/AtomicBoolean;", "classExists", "", "name", "enforceOrDie", "", "reason", "hasFridaTcp", "ports", "Lkotlin/ranges/IntRange;", "file", "hasFridaUnixSockets", "hasTracerOnAnyThread", "hasTracerPid", "statusPath", "killNow", "mapsContainFrida", "scan", "Lorg/telegraam/addon/security/AntiDebug$Result;", "startWatchdog", "periodMs", "", "stopWatchdog", "Result", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class AntiDebug {
    private static final String TAG = "AntiDebug";
    public static final AntiDebug INSTANCE = new AntiDebug();
    private static final AtomicBoolean running = new AtomicBoolean(false);

    private AntiDebug() {
    }

    /* compiled from: AntiDebug.kt */
    @Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u001f\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001BE\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\u0006\u0010\n\u001a\u00020\u0003¢\u0006\u0002\u0010\u000bJ\t\u0010\u0017\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0018\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0019\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001a\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001b\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001c\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001d\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001e\u001a\u00020\u0003HÆ\u0003JY\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u0003HÆ\u0001J\u0013\u0010 \u001a\u00020\u00032\b\u0010!\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\"\u001a\u00020#HÖ\u0001J\b\u0010$\u001a\u00020%H\u0016R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\t\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\b\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\rR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\rR\u0011\u0010\u0007\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\rR\u0011\u0010\u0013\u001a\u00020\u00038F¢\u0006\u0006\u001a\u0004\b\u0014\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\rR\u0011\u0010\n\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\r¨\u0006&"}, d2 = {"Lorg/telegraam/addon/security/AntiDebug$Result;", "", "debugger", "", "tracerAnyThread", "fridaTcp", "fridaTcp6", "fridaUnix", "fridaMaps", "fridaClass", "xposedClass", "(ZZZZZZZZ)V", "getDebugger", "()Z", "getFridaClass", "getFridaMaps", "getFridaTcp", "getFridaTcp6", "getFridaUnix", "suspicious", "getSuspicious", "getTracerAnyThread", "getXposedClass", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    /* loaded from: classes3.dex */
    public static final /* data */ class Result {

        /* renamed from: debugger, reason: from kotlin metadata and from toString */
        private final boolean dbg;
        private final boolean fridaClass;

        /* renamed from: fridaMaps, reason: from kotlin metadata and from toString */
        private final boolean maps;

        /* renamed from: fridaTcp, reason: from kotlin metadata and from toString */
        private final boolean tcp;

        /* renamed from: fridaTcp6, reason: from kotlin metadata and from toString */
        private final boolean tcp6;

        /* renamed from: fridaUnix, reason: from kotlin metadata and from toString */
        private final boolean unix;

        /* renamed from: tracerAnyThread, reason: from kotlin metadata and from toString */
        private final boolean tracer;
        private final boolean xposedClass;

        /* renamed from: component1, reason: from getter */
        public final boolean getDbg() {
            return this.dbg;
        }

        /* renamed from: component2, reason: from getter */
        public final boolean getTracer() {
            return this.tracer;
        }

        /* renamed from: component3, reason: from getter */
        public final boolean getTcp() {
            return this.tcp;
        }

        /* renamed from: component4, reason: from getter */
        public final boolean getTcp6() {
            return this.tcp6;
        }

        /* renamed from: component5, reason: from getter */
        public final boolean getUnix() {
            return this.unix;
        }

        /* renamed from: component6, reason: from getter */
        public final boolean getMaps() {
            return this.maps;
        }

        /* renamed from: component7, reason: from getter */
        public final boolean getFridaClass() {
            return this.fridaClass;
        }

        /* renamed from: component8, reason: from getter */
        public final boolean getXposedClass() {
            return this.xposedClass;
        }

        public final Result copy(boolean debugger, boolean tracerAnyThread, boolean fridaTcp, boolean fridaTcp6, boolean fridaUnix, boolean fridaMaps, boolean fridaClass, boolean xposedClass) {
            return new Result(debugger, tracerAnyThread, fridaTcp, fridaTcp6, fridaUnix, fridaMaps, fridaClass, xposedClass);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Result)) {
                return false;
            }
            Result result = (Result) other;
            return this.dbg == result.dbg && this.tracer == result.tracer && this.tcp == result.tcp && this.tcp6 == result.tcp6 && this.unix == result.unix && this.maps == result.maps && this.fridaClass == result.fridaClass && this.xposedClass == result.xposedClass;
        }

        public int hashCode() {
            return (((((((((((((Boolean.hashCode(this.dbg) * 31) + Boolean.hashCode(this.tracer)) * 31) + Boolean.hashCode(this.tcp)) * 31) + Boolean.hashCode(this.tcp6)) * 31) + Boolean.hashCode(this.unix)) * 31) + Boolean.hashCode(this.maps)) * 31) + Boolean.hashCode(this.fridaClass)) * 31) + Boolean.hashCode(this.xposedClass);
        }

        public Result(boolean debugger, boolean tracerAnyThread, boolean fridaTcp, boolean fridaTcp6, boolean fridaUnix, boolean fridaMaps, boolean fridaClass, boolean xposedClass) {
            this.dbg = debugger;
            this.tracer = tracerAnyThread;
            this.tcp = fridaTcp;
            this.tcp6 = fridaTcp6;
            this.unix = fridaUnix;
            this.maps = fridaMaps;
            this.fridaClass = fridaClass;
            this.xposedClass = xposedClass;
        }

        public final boolean getDebugger() {
            return this.dbg;
        }

        public final boolean getTracerAnyThread() {
            return this.tracer;
        }

        public final boolean getFridaTcp() {
            return this.tcp;
        }

        public final boolean getFridaTcp6() {
            return this.tcp6;
        }

        public final boolean getFridaUnix() {
            return this.unix;
        }

        public final boolean getFridaMaps() {
            return this.maps;
        }

        public final boolean getFridaClass() {
            return this.fridaClass;
        }

        public final boolean getXposedClass() {
            return this.xposedClass;
        }

        public final boolean getSuspicious() {
            return this.dbg || this.tracer || this.tcp || this.tcp6 || this.unix || this.maps || this.fridaClass || this.xposedClass;
        }

        public String toString() {
            return "dbg=" + this.dbg + ", tracer=" + this.tracer + ", tcp=" + this.tcp + ", tcp6=" + this.tcp6 + ", unix=" + this.unix + ", maps=" + this.maps + ", fridaClass=" + this.fridaClass + ", xposedClass=" + this.xposedClass;
        }
    }

    public static /* synthetic */ void enforceOrDie$default(AntiDebug antiDebug, String str, int i, Object obj) {
        if ((i & 1) != 0) {
            str = "manual";
        }
        antiDebug.enforceOrDie(str);
    }

    public final void enforceOrDie(String reason) {
        Intrinsics.checkNotNullParameter(reason, "reason");
        Result r = scan();
        if (r.getSuspicious()) {
            Log.e(TAG, "Suspicious (" + reason + "): " + r);
            killNow("detected:" + reason + "|" + r);
        }
    }

    public static /* synthetic */ void startWatchdog$default(AntiDebug antiDebug, long j, int i, Object obj) {
        if ((i & 1) != 0) {
            j = 1500;
        }
        antiDebug.startWatchdog(j);
    }

    public final void startWatchdog(final long periodMs) {
        if (running.compareAndSet(false, true)) {
            Thread $this$startWatchdog_u24lambda_u241 = new Thread(new Runnable() { // from class: org.telegraam.addon.security.AntiDebug$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    AntiDebug.startWatchdog$lambda$0(periodMs);
                }
            }, "AntiDebugWatchdog");
            $this$startWatchdog_u24lambda_u241.setDaemon(true);
            $this$startWatchdog_u24lambda_u241.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startWatchdog$lambda$0(long $periodMs) {
        while (running.get()) {
            try {
                Result r = INSTANCE.scan();
                if (r.getSuspicious()) {
                    Log.e(TAG, "Watchdog detected: " + r);
                    INSTANCE.killNow("watchdog|" + r);
                }
                Thread.sleep($periodMs);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public final void stopWatchdog() {
        running.set(false);
    }

    public final Result scan() {
        return new Result(Debug.isDebuggerConnected() || Debug.waitingForDebugger(), hasTracerOnAnyThread(), hasFridaTcp(new IntRange(27042, 27050), "/proc/net/tcp"), hasFridaTcp(new IntRange(27042, 27050), "/proc/net/tcp6"), hasFridaUnixSockets(), mapsContainFrida(), classExists("re.frida.Server") || classExists("com.frida.Agent") || classExists("frida.gadget"), classExists("de.robv.android.xposed.XposedBridge") || classExists("org.lsposed.lspd.models.IpcData"));
    }

    private final boolean hasTracerOnAnyThread() {
        File tasksDir = new File("/proc/self/task");
        File[] list = tasksDir.listFiles();
        if (list == null) {
            return hasTracerPid("/proc/self/status");
        }
        for (File t : list) {
            String path = t.getAbsolutePath() + "/status";
            if (hasTracerPid(path)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v0, types: [T, java.lang.String] */
    private final boolean hasTracerPid(String statusPath) {
        BufferedReader bufferedReader;
        BufferedReader br;
        Ref.ObjectRef line;
        T t;
        try {
            bufferedReader = new BufferedReader(new FileReader(statusPath));
            try {
                br = bufferedReader;
                line = new Ref.ObjectRef();
            } finally {
            }
        } catch (Throwable th) {
        }
        do {
            ?? readLine = br.readLine();
            line.element = readLine;
            if (readLine == 0) {
                CloseableKt.closeFinally(bufferedReader, null);
                return false;
            }
            t = line.element;
            Intrinsics.checkNotNull(t);
        } while (!StringsKt.startsWith$default((String) t, "TracerPid:", false, 2, (Object) null));
        T t2 = line.element;
        Intrinsics.checkNotNull(t2);
        String v = StringsKt.trim((CharSequence) StringsKt.substringAfter$default((String) t2, "TracerPid:", (String) null, 2, (Object) null)).toString();
        boolean z = !Intrinsics.areEqual(v, "0");
        CloseableKt.closeFinally(bufferedReader, null);
        return z;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0102 A[LOOP:0: B:9:0x003c->B:29:0x0102, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:30:0x0115 A[EDGE_INSN: B:30:0x0115->B:31:0x0115 BREAK  A[LOOP:0: B:9:0x003c->B:29:0x0102], SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r4v0 */
    /* JADX WARN: Type inference failed for: r4v1 */
    /* JADX WARN: Type inference failed for: r4v11 */
    /* JADX WARN: Type inference failed for: r4v14 */
    /* JADX WARN: Type inference failed for: r4v15 */
    /* JADX WARN: Type inference failed for: r4v17 */
    /* JADX WARN: Type inference failed for: r4v2, types: [java.nio.charset.Charset] */
    /* JADX WARN: Type inference failed for: r4v3 */
    /* JADX WARN: Type inference failed for: r4v4 */
    /* JADX WARN: Type inference failed for: r4v5 */
    /* JADX WARN: Type inference failed for: r4v6 */
    /* JADX WARN: Type inference failed for: r4v7 */
    /* JADX WARN: Type inference failed for: r4v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final boolean hasFridaTcp(kotlin.ranges.IntRange r29, java.lang.String r30) {
        /*
            Method dump skipped, instructions count: 305
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegraam.addon.security.AntiDebug.hasFridaTcp(kotlin.ranges.IntRange, java.lang.String):boolean");
    }

    /* JADX WARN: Removed duplicated region for block: B:33:0x00fc A[LOOP:0: B:11:0x0062->B:33:0x00fc, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00f9 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final boolean hasFridaUnixSockets() {
        /*
            Method dump skipped, instructions count: 300
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegraam.addon.security.AntiDebug.hasFridaUnixSockets():boolean");
    }

    /* JADX WARN: Removed duplicated region for block: B:33:0x00fc A[LOOP:0: B:11:0x0062->B:33:0x00fc, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00f9 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final boolean mapsContainFrida() {
        /*
            Method dump skipped, instructions count: 300
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegraam.addon.security.AntiDebug.mapsContainFrida():boolean");
    }

    private final boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    private final void killNow(final String reason) {
        try {
            Log.e(TAG, "KILL NOW: " + reason);
        } catch (Throwable th) {
        }
        try {
            Process.killProcess(Process.myPid());
        } catch (Throwable th2) {
        }
        try {
            Runtime.getRuntime().exit(0);
        } catch (Throwable th3) {
        }
        try {
            new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: org.telegraam.addon.security.AntiDebug$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    AntiDebug.killNow$lambda$13(reason);
                }
            });
        } catch (Throwable th4) {
        }
        while (true) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void killNow$lambda$13(String reason) {
        Intrinsics.checkNotNullParameter(reason, "$reason");
        throw new RuntimeException("AntiDebug: suspicious environment: " + reason);
    }
}
