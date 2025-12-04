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
        prefs = new XSharedPreferences("com.gaba.eskukap", "com.gaba.eskukap_preferences");
        prefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        prefs.reload();

        boolean fake = prefs.getBoolean("enable_fakephoto", true);
        boolean log = prefs.getBoolean("enable_logs", false);

        if (log) Log.i("CamYan", "Loaded into " + lpparam.packageName +" fake="+fake);

        if (!fake) return;

        Uri uri = FakePhotoProvider.CONTENT_URI;
        if (log) Log.i("CamYan", "Fake uri = " + uri);
    }
}
