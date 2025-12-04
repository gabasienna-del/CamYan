package com.gaba.eskukap.hook;

import android.net.Uri;
import android.content.Context;

import com.gaba.eskukap.provider.FakePhotoProvider;

public class CamYanHook {

    public static Uri getHookedUri(Context context) {
        // Получаем фейковое фото из FakePhotoProvider
        Uri providerUri = FakePhotoProvider.getFakeImage(context);

        if (providerUri != null) {
            return providerUri;    // <-- Возвращаем выбранное фото
        }

        return null; // если не выбрано
    }
}
