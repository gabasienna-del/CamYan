package com.gaba.eskukap;

import android.view.View;
import android.view.ViewGroup;
import android.view.TextureView;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static void scanViews(View root) {
        try {
            if (root instanceof TextureView) {
                XposedBridge.log("Eskukap: TextureView FOUND -> " + root.getClass().getName());
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

        XposedBridge.log("Eskukap: hook active - scanning layouts");

        XposedHelpers.findAndHookMethod(
                "android.view.LayoutInflater",
                lpparam.classLoader,
                "inflate",
                int.class, ViewGroup.class, boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        View v = (View) param.getResult();
                        if (v != null) scanViews(v);
                    }
                }
        );
    }
}
