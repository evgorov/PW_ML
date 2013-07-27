package com.ltst.prizeword.tools;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.omich.velo.handlers.IListenerVoid;

import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 26.07.13.
 */
public class BitmapTools {

    private  @Nonnull String THREAD_NAME = BitmapTools.class.getName();
    private byte[] mBuffer;
    Bitmap mBitmap;

    public BitmapTools(){
    }

    public byte[] getBuffer(){
        return this.mBuffer;
    }

    public void convertBitmapToBytearray(final @Nonnull Bitmap bitmap, final @Nonnull IListenerVoid handler){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                convert(bitmap);
                handler.handle();
            }
        });
        thread.setName(THREAD_NAME);
        thread.start();
    }

    public void convert(@Nonnull Bitmap bitmap){
        this.mBitmap = bitmap;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);
        mBuffer = stream.toByteArray();
    }
}
