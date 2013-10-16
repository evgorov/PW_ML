package com.ltst.przwrd.tools;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 29.07.13.
 */
public class BitmapAsyncTask extends AsyncTask<Bitmap, Void, byte[]> {

    // Переводим Bitmap в bytearray. Для больших фото процедура довольно длительная.

    private @Nonnull IBitmapAsyncTask mIBitmapConverterResult;

    public BitmapAsyncTask(@Nonnull IBitmapAsyncTask mIBitmapConverterResult) {
        this.mIBitmapConverterResult = mIBitmapConverterResult;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected byte[] doInBackground(Bitmap... bitmaps)
    {
        int width = 0;
        int height = 0;
        int min = 0;
        int max = 0;
        int start = 0;
        if (bitmaps != null)
        {
            for(Bitmap bitmap: bitmaps)
            {
                if (bitmap!=null)
                {
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    min = height < width ? height : width;
                    max = height > width ? height : width;
                    start = (max-min)/2;
                    if(height < width)
                    {
                        bitmap = Bitmap.createBitmap(bitmap, start, 0, min, min);
                    }
                    else
                    {
                        bitmap = Bitmap.createBitmap(bitmap, 0, start, min, min);
                    }

                    return convertBitmapToByte(bitmap);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(byte[] buffer)
    {
        super.onPostExecute(buffer);
        mIBitmapConverterResult.bitmapConvertToByte(buffer);
    }

    public static byte[] convertBitmapToByte(@Nullable Bitmap bitmap)
    {
        if(bitmap == null)
        {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] buffer = stream.toByteArray();
        return buffer;
    }

}
