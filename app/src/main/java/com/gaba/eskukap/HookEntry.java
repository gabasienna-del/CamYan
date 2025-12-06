package com.gaba.eskukap;

import android.view.View;
import android.view.ViewGroup;
import android.view.TextureView;
import android.view.SurfaceView;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static String viewInfo(View v) {
        StringBuilder sb = new StringBuilder();
        sb.append(v.getClass().getName());
        sb.append(" @").append(Integer.toHexString(System.identityHashCode(v)));

        try {
            int id = v.getId();
            if (id != View.NO_ID) {
                Resources res = v.getResources();
                String name = res.getResourceEntryName(id);
                sb.append(" id=").append(name);
            }
        } catch (Throwable ignored) {}
        return sb.toString();
    }

    private static void scanViews(View root) {
        if (root == null) return;
        try {
            String cls = root.getClass().getName().toLowerCase();

            if (root instanceof TextureView) {
                XposedBridge.log("Eskukap: TextureView FOUND -> " + viewInfo(root));
            }
            if (root instanceof SurfaceView && !(root instanceof GLSurfaceView)) {
                XposedBridge.log("Eskukap: SurfaceView FOUND -> " + viewInfo(root));
            }
            if (root instanceof GLSurfaceView) {
                XposedBridge.log("Eskukap: GLSurfaceView FOUND -> " + viewInfo(root));
            }
            // любые кастомные camera/preview view
            if (cls.contains("preview") || cls.contains("camera")) {
                XposedBridge.log("Eskukap: Camera-like View FOUND -> " + viewInfo(root));
            }

            if (root instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) root;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    scanViews(vg.getChildAt(i));
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("ru.yandex.taximeter")) return;

        XposedBridge.log("Eskukap: hook active - scanning layouts (Surface/Texture/Preview)");

        XposedHelpers.findAndHookMethod(
                "android.view.LayoutInflater",
                lpparam.classLoader,
                "inflate",
                int.class, ViewGroup.class, boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            View v = (View) param.getResult();
                            if (v != null) scanViews(v);
                        } catch (Throwable ignored) {}
                    }
                }
        );
    }
}
