package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;


import com.ltst.prizeword.tools.decoding.BitmapDecoder;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LetterBitmapManager
{
    public static final char UNKNOWN_CHARACTER = '-';
    private static final @Nonnull String mAlphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ- ";
    private @Nonnull HashMap<String, BitmapEntity> mLetters;
    private @Nonnull Paint mPaint;
    private @Nonnull Context mContext;
    private @Nonnull IBitmapResourceModel mBitmapResourceModel;

    public LetterBitmapManager(@Nonnull Context context, @Nonnull IBitmapResourceModel model)
    {
        mContext = context;
        mBitmapResourceModel = model;
        mLetters = new HashMap<String, BitmapEntity>();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void addTileResource(final int lettersResource, int letterWidth, int letterHeight, final @Nullable IListenerVoid loadingHandler)
    {
        mBitmapResourceModel.loadTileBitmapEntityList(lettersResource, letterWidth, letterHeight, new IListener<List<BitmapEntity>>()
        {
            @Override
            public void handle(@Nullable List<BitmapEntity> bitmapEntities)
            {
                synchronized (this)
                {
                    if (bitmapEntities == null)
                    {
                        return;
                    }

                    int length = mAlphabet.length();
                    if(bitmapEntities.size() != length)
                        return;
                    for (int i = 0; i < length; i++)
                    {
                        char letter = mAlphabet.charAt(i);
                        BitmapEntity entity = bitmapEntities.get(i);
                        String key = getLetterResourceKey(lettersResource, letter);
                        mLetters.put(key, entity);
                    }
                    if (loadingHandler != null)
                    {
                        loadingHandler.handle();
                    }
                }
            }
        });
    }

    public boolean drawLetter(int resource, char letter, @Nonnull Canvas canvas, @Nonnull RectF rect)
    {
        return drawLetter(resource, letter, canvas, rect, mPaint);
    }

    public boolean drawLetter(int resource, char letter, @Nonnull Canvas canvas, @Nonnull RectF rect, @Nonnull Paint paint)
    {
        @Nullable BitmapEntity bm = mLetters.get(getLetterResourceKey(resource, letter));
        if (bm == null)
        {
            return false;
        }
        bm.draw(canvas, rect, paint);
        return true;
    }

    private @Nonnull String getLetterResourceKey(int resource, char letter)
    {
        return String.valueOf(resource) + "_" + letter;
    }

    public void recycle()
    {
        for (BitmapEntity bitmapEntity : mLetters.values())
        {
            bitmapEntity.recycle();
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        recycle();
        super.finalize();
    }
}
