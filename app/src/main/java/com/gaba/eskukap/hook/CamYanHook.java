package com.gaba.eskukap.hook;

import android.content.Context;
import android.net.Uri;
import com.gaba.eskukap.provider.FakePhotoProvider;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CamYanHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Пока пусто — просто проверка что модуль грузится
        if(lpparam.packageName.equals("com.yandex.mobile")) {
            // тестовый вывод
            android.util.Log.d("CamY", "LSPosed Hook Loaded!");
        }
    }

    public static Uri getImage(Context ctx){
        return FakePhotoProvider.CONTENT_URI;
    }
}
