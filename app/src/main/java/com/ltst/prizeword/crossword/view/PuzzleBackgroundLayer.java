package com.ltst.prizeword.crossword.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleBackgroundLayer implements ICanvasLayer
{
    private int mWidth;
    private int mHeight;
    private int mBgTileResource;

    private @Nonnull Bitmap mBgTileBitmap;
    private @Nonnull Paint mPaint;

    public PuzzleBackgroundLayer(@Nonnull Resources res,
                                 int width,
                                 int height,
                                 int bgTileResource)
    {
        mWidth = width;
        mHeight = height;
        mBgTileResource = bgTileResource;
        Bitmap bitmap = BitmapFactory.decodeResource(res, mBgTileResource);
        mBgTileBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    public void drawLayer(Canvas canvas)
    {
        PuzzleBackgroundLayer.fillBackgroundWithTile(canvas, mBgTileBitmap, mPaint);
    }

    @Override
    public void recycle()
    {
        mBgTileBitmap.recycle();
    }

    public static void fillBackgroundWithTile(@Nonnull Canvas canvas, @Nonnull Bitmap tileBitmap, @Nullable Paint paint)
    {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int tileWidth = tileBitmap.getWidth();
        int tileHeight = tileBitmap.getHeight();
        RectF rect = new RectF(0, 0, tileWidth, tileHeight);

        while (rect.bottom < height)
        {
            while (rect.right < width)
            {
                canvas.drawBitmap(tileBitmap, null, rect, paint);
                rect.left += tileWidth;
                rect.right += tileWidth;
            }
            rect.right = width;
            canvas.drawBitmap(tileBitmap, null, rect, paint);
            rect.left = 0;
            rect.right = tileWidth;

            rect.top += tileHeight;
            rect.bottom += tileHeight;
        }

        rect.right = tileWidth;
        rect.bottom = height;
        while (rect.right < width)
        {
            canvas.drawBitmap(tileBitmap, null, rect, paint);
            rect.left += tileWidth;
            rect.right += tileWidth;
        }
        rect.right = width;
        canvas.drawBitmap(tileBitmap, null, rect, paint);
    }
}
