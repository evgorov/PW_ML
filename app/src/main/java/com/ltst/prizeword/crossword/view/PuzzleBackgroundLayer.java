package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import javax.annotation.Nonnull;

public class PuzzleBackgroundLayer implements ICanvasLayer
{
    private int mWidth;
    private int mHeight;
    private int mBgTileResource;

    private @Nonnull Bitmap mBgTileBitmap;
    private @Nonnull Paint mPaint;

    public PuzzleBackgroundLayer(@Nonnull Context context,
                                 int width,
                                 int height,
                                 int bgTileResource)
    {
        mWidth = width;
        mHeight = height;
        mBgTileResource = bgTileResource;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), mBgTileResource);
        mBgTileBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    public void drawLayer(Canvas canvas)
    {
        int tileWidth = mBgTileBitmap.getWidth();
        int tileHeight = mBgTileBitmap.getHeight();
        RectF rect = new RectF(0, 0, tileWidth, tileHeight);

        while (rect.bottom < mHeight)
        {
            while (rect.right < mWidth)
            {
                canvas.drawBitmap(mBgTileBitmap, null, rect, mPaint);
                rect.left += tileWidth;
                rect.right += tileWidth;
            }
            rect.right = mWidth;
            canvas.drawBitmap(mBgTileBitmap, null, rect, mPaint);
            rect.left = 0;
            rect.right = tileWidth;

            rect.top += tileHeight;
            rect.bottom += tileHeight;
        }

        rect.right = tileWidth;
        rect.bottom = mHeight;
        while (rect.right < mWidth)
        {
            canvas.drawBitmap(mBgTileBitmap, null, rect, mPaint);
            rect.left += tileWidth;
            rect.right += tileWidth;
        }
        rect.right = mWidth;
        canvas.drawBitmap(mBgTileBitmap, null, rect, mPaint);

        canvas.drawText("right: " + rect.right + " bottom: " + rect.bottom, 100, 700, mPaint);
    }

    @Override
    public void recycle()
    {
        mBgTileBitmap.recycle();
    }
}
