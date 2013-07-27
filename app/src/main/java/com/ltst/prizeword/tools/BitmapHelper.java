package com.ltst.prizeword.tools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import javax.annotation.Nonnull;

public class BitmapHelper
{
    public static @Nonnull
    Bitmap loadBitmapInSampleSize(@Nonnull Resources res, int resId, int sampleSize)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, options);
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }
}
