package org.telegraam.addon.xposed;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import androidx.constraintlayout.widget.ConstraintLayout;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.io.ByteStreamsKt;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.telegraam.addon.provider.FakePhotoProvider;
import org.telegraam.addon.xposed.HookEntry;

/* compiled from: HookEntry.kt */
@Metadata(d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016¨\u0006\b"}, d2 = {"Lorg/telegraam/addon/xposed/HookEntry;", "Lde/robv/android/xposed/IXposedHookLoadPackage;", "()V", "handleLoadPackage", "", "lpparam", "Lde/robv/android/xposed/callbacks/XC_LoadPackage$LoadPackageParam;", "Companion", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes6.dex */
public final class HookEntry implements IXposedHookLoadPackage {
    private static final Uri PHOTO_URI;
    private static final Uri STATE_URI;
    private static final String TAG = "lasthope/Hook";
    private static volatile byte[] fakeBytes;
    private static final ConcurrentHashMap.KeySetView<String, Boolean> patched;

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final String AUTH = FakePhotoProvider.AUTH;

    /* compiled from: HookEntry.kt */
    @Metadata(d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u0014\u0010\u0013\u001a\u00020\u00102\n\u0010\u0014\u001a\u0006\u0012\u0002\b\u00030\u0015H\u0002J\u0010\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e¢\u0006\u0002\n\u0000RN\u0010\u000b\u001aB\u0012\f\u0012\n \r*\u0004\u0018\u00010\u00040\u0004\u0012\f\u0012\n \r*\u0004\u0018\u00010\u000e0\u000e \r* \u0012\f\u0012\n \r*\u0004\u0018\u00010\u00040\u0004\u0012\f\u0012\n \r*\u0004\u0018\u00010\u000e0\u000e\u0018\u00010\f0\fX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0017"}, d2 = {"Lorg/telegraam/addon/xposed/HookEntry$Companion;", "", "()V", "AUTH", "", "PHOTO_URI", "Landroid/net/Uri;", "STATE_URI", "TAG", "fakeBytes", "", "patched", "Ljava/util/concurrent/ConcurrentHashMap$KeySetView;", "kotlin.jvm.PlatformType", "", "ensureFakeBytes", "", "cl", "Ljava/lang/ClassLoader;", "hookProvidersOn", "clazz", "Ljava/lang/Class;", "isEnabled", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    /* loaded from: classes6.dex */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX WARN: Code restructure failed: missing block: B:20:0x0044, code lost:
        
            if (r6.getInt(0) == 1) goto L22;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public final boolean isEnabled(java.lang.ClassLoader r11) {
            /*
                r10 = this;
                r0 = 0
                java.lang.String r1 = "android.app.ActivityThread"
                java.lang.Class r1 = de.robv.android.xposed.XposedHelpers.findClass(r1, r11)     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                java.lang.String r2 = "currentApplication"
                java.lang.Object[] r3 = new java.lang.Object[r0]     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                java.lang.Object r1 = de.robv.android.xposed.XposedHelpers.callStaticMethod(r1, r2, r3)     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                boolean r2 = r1 instanceof android.app.Application     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                r3 = 0
                if (r2 == 0) goto L19
                android.app.Application r1 = (android.app.Application) r1     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                goto L1a
            L19:
                r1 = r3
            L1a:
                if (r1 != 0) goto L1d
                return r0
            L1d:
                android.content.ContentResolver r4 = r1.getContentResolver()     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                if (r4 != 0) goto L24
                return r0
            L24:
                android.net.Uri r5 = org.telegraam.addon.xposed.HookEntry.access$getSTATE_URI$cp()     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                r8 = 0
                r9 = 0
                r6 = 0
                r7 = 0
                android.database.Cursor r2 = r4.query(r5, r6, r7, r8, r9)     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                if (r2 == 0) goto L58
                r5 = r2
                java.io.Closeable r5 = (java.io.Closeable) r5     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                r6 = r5
                android.database.Cursor r6 = (android.database.Cursor) r6     // Catch: java.lang.Throwable -> L4d
                r7 = 0
                boolean r8 = r6.moveToFirst()     // Catch: java.lang.Throwable -> L4d
                if (r8 == 0) goto L47
                int r8 = r6.getInt(r0)     // Catch: java.lang.Throwable -> L4d
                r9 = 1
                if (r8 != r9) goto L47
                goto L48
            L47:
                r9 = r0
            L48:
                kotlin.io.CloseableKt.closeFinally(r5, r3)     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                r0 = r9
                goto L58
            L4d:
                r3 = move-exception
                throw r3     // Catch: java.lang.Throwable -> L4f
            L4f:
                r6 = move-exception
                kotlin.io.CloseableKt.closeFinally(r5, r3)     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
                throw r6     // Catch: java.lang.Throwable -> L54 java.lang.SecurityException -> L56
            L54:
                r1 = move-exception
                goto L58
            L56:
                r1 = move-exception
            L58:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: org.telegraam.addon.xposed.HookEntry.Companion.isEnabled(java.lang.ClassLoader):boolean");
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final void ensureFakeBytes(ClassLoader cl) {
            ContentResolver cr;
            InputStream openInputStream;
            if (HookEntry.fakeBytes != null) {
                return;
            }
            try {
                Object callStaticMethod = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", cl), "currentApplication", new Object[0]);
                Application ctx = callStaticMethod instanceof Application ? (Application) callStaticMethod : null;
                if (ctx != null && (cr = ctx.getContentResolver()) != null && (openInputStream = cr.openInputStream(HookEntry.PHOTO_URI)) != null) {
                    InputStream inputStream = openInputStream;
                    try {
                        InputStream ins = inputStream;
                        Companion companion = HookEntry.INSTANCE;
                        HookEntry.fakeBytes = ByteStreamsKt.readBytes(ins);
                        Unit unit = Unit.INSTANCE;
                        CloseableKt.closeFinally(inputStream, null);
                    } finally {
                    }
                }
            } catch (SecurityException e) {
            } catch (Throwable th) {
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final void hookProvidersOn(final Class<?> clazz) {
            String name = clazz.getName();
            if (HookEntry.patched.add(name)) {
                Method[] declaredMethods = clazz.getDeclaredMethods();
                Intrinsics.checkNotNullExpressionValue(declaredMethods, "getDeclaredMethods(...)");
                for (Method m : declaredMethods) {
                    Intrinsics.checkNotNull(m);
                    if (HookEntry$Companion$$ExternalSyntheticBackport0.m(m) == 0 && Intrinsics.areEqual(m.getReturnType(), byte[].class)) {
                        m.setAccessible(true);
                        XposedBridge.hookMethod(m, new XC_MethodReplacement() { // from class: org.telegraam.addon.xposed.HookEntry$Companion$hookProvidersOn$1
                            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) {
                                boolean isEnabled;
                                Intrinsics.checkNotNullParameter(param, "param");
                                try {
                                    HookEntry.Companion companion = HookEntry.INSTANCE;
                                    ClassLoader classLoader = clazz.getClassLoader();
                                    Intrinsics.checkNotNullExpressionValue(classLoader, "getClassLoader(...)");
                                    isEnabled = companion.isEnabled(classLoader);
                                    if (!isEnabled) {
                                        return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, (Object[]) null);
                                    }
                                    HookEntry.Companion companion2 = HookEntry.INSTANCE;
                                    ClassLoader classLoader2 = clazz.getClassLoader();
                                    Intrinsics.checkNotNullExpressionValue(classLoader2, "getClassLoader(...)");
                                    companion2.ensureFakeBytes(classLoader2);
                                    byte[] fb = HookEntry.fakeBytes;
                                    if (fb == null) {
                                        return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, (Object[]) null);
                                    }
                                    byte[] copyOf = Arrays.copyOf(fb, fb.length);
                                    Intrinsics.checkNotNullExpressionValue(copyOf, "copyOf(...)");
                                    return copyOf;
                                } catch (Throwable th) {
                                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, (Object[]) null);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    static {
        Uri parse = Uri.parse("content://" + AUTH + "/photo");
        Intrinsics.checkNotNullExpressionValue(parse, "parse(...)");
        PHOTO_URI = parse;
        Uri parse2 = Uri.parse("content://" + AUTH + "/state");
        Intrinsics.checkNotNullExpressionValue(parse2, "parse(...)");
        STATE_URI = parse2;
        patched = ConcurrentHashMap.newKeySet();
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        Intrinsics.checkNotNullParameter(lpparam, "lpparam");
        Class saver = XposedHelpers.findClassIfExists("com.example.flutter_protector_sdk.photo.a", lpparam.classLoader);
        if (saver == null) {
            return;
        }
        XposedBridge.hookAllMethods(saver, "e", new XC_MethodHook() { // from class: org.telegraam.addon.xposed.HookEntry$handleLoadPackage$1
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                Class c;
                boolean z;
                Intrinsics.checkNotNullParameter(param, "param");
                try {
                    Object[] args = param.args;
                    if (args == null) {
                        return;
                    }
                    Class target = null;
                    int length = args.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        Object a = args[i];
                        if (a != null && (c = a.getClass()) != null) {
                            Object[] declaredMethods = c.getDeclaredMethods();
                            Intrinsics.checkNotNullExpressionValue(declaredMethods, "getDeclaredMethods(...)");
                            Object[] $this$any$iv = declaredMethods;
                            int length2 = $this$any$iv.length;
                            int i2 = 0;
                            while (true) {
                                if (i2 < length2) {
                                    Object element$iv = $this$any$iv[i2];
                                    Method it = (Method) element$iv;
                                    z = true;
                                    if (HookEntry$Companion$$ExternalSyntheticBackport0.m(it) == 0 && Intrinsics.areEqual(it.getReturnType(), byte[].class)) {
                                        break;
                                    } else {
                                        i2++;
                                    }
                                } else {
                                    z = false;
                                    break;
                                }
                            }
                            if (z) {
                                target = c;
                                break;
                            }
                        }
                        i++;
                    }
                    if (target != null) {
                        HookEntry.INSTANCE.hookProvidersOn(target);
                    }
                } catch (Throwable th) {
                }
            }
        });
    }
}
