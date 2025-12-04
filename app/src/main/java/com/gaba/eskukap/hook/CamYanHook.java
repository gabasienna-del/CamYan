package com.gaba.eskukap.hook;

import android.net.Uri;
import android.util.Log;

import com.gaba.eskukap.provider.FakePhotoProvider;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static XSharedPreferences prefs;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        prefs = new XSharedPreferences("com.gaba.eskukap",
                "com.gaba.eskukap_preferences");
        prefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        prefs.reload();

        boolean enableFake = prefs.getBoolean("enable_fakephoto", true);
        boolean enableLogs = prefs.getBoolean("enable_logs", false);
        String uriStr = prefs.getString("fake_photo_uri", null);

        if (enableLogs) {
            Log.i("CamYan", "Loaded into: " + lpparam.packageName
                    + " fake=" + enableFake
                    + " uri=" + uriStr);
        }

        if (!enableFake) return;

        // Здесь пока только демонстрация: доступ к CONTENT_URI
        Uri providerUri = FakePhotoProvider.CONTENT_URI;
        if (enableLogs) {
            Log.i("CamYan", "FakePhotoProvider CONTENT_URI = " + providerUri);
        }

        // TODO: сюда можно добавлять безопасные хуки под конкретные свои приложения.
        // Не стоит использовать это для обхода защит, верификаций и т.п.
    }
}
