package com.ltst.prizeword.tools;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;

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
    protected byte[] doInBackground(Bitmap... bitmaps) {
        for(Bitmap bitmap: bitmaps){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] buffer = stream.toByteArray();
            return buffer;
        }
        return null;
    }

    @Override
    protected void onPostExecute(byte[] buffer) {
        super.onPostExecute(buffer);
        mIBitmapConverterResult.bitmapConvertToByte(buffer);
    }
}
