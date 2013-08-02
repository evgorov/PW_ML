package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;


import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LetterBitmapManager
{
    private static final @Nonnull String mAlphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ- ";
    private @Nonnull HashMap<String, BitmapEntity> mLetters;
    private @Nonnull Paint mPaint;
    private @Nonnull Context mContext;

    public LetterBitmapManager(@Nonnull Context context, int lettersResource, int letterWidth, int letterHeight)
    {
        mContext = context;
        mLetters = new HashMap<String, BitmapEntity>();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        decodeBitmaps();
    }

    private void decodeBitmaps()
    {
        int length = mAlphabet.length();
        for (int i = 0; i < length; i++)
        {
            char letter = mAlphabet.charAt(i);
//                BitmapEntity entity = new BitmapEntity(bm);
//                String key = getLetterResourceKey(mDecoder.getResourceId(), letter);
//                mLetters.put(key, entity);
        }
    }

    public void addTileResource(int lettersResource, int letterWidth, int letterHeight)
    {
//        mDecoder = new BitmapDecoder(mContext, lettersResource, letterWidth, letterHeight);
//        decodeBitmaps();
    }

    public void drawLetter(int resource, char letter, @Nonnull Canvas canvas, @Nonnull RectF rect)
    {
        @Nullable BitmapEntity bm = mLetters.get(getLetterResourceKey(resource, letter));
        if (bm == null)
        {
            return;
        }
        bm.draw(canvas, rect, mPaint);
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
