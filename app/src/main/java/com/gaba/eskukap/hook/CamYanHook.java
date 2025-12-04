package com.gaba.eskukap.hook;

import android.content.Context;
import android.net.Uri;

import com.gaba.eskukap.provider.FakePhotoProvider;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook {

    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {

        // тут укажем где нужно подменять фото
        if (!lpparam.packageName.equals("com.yandex.mobile")) return;

        XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onCreate",
                android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context ctx = (Context) param.thisObject;
                        Uri uri = FakePhotoProvider.getFakeImage(ctx);
                        // дальше добавим подмену, пока просто получаем Uri
                    }
                }
        );
    }
}
