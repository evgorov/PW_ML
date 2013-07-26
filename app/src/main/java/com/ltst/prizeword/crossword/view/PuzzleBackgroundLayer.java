package com.ltst.prizeword.crossword.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;

import com.ltst.prizeword.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleBackgroundLayer implements ICanvasLayer
{
    private int mWidth;
    private int mHeight;
    private int mBgTileResource;
    private int mFramePadding;

    private @Nonnull Bitmap mBgTileBitmap;
    private @Nonnull Paint mPaint;
    private @Nonnull NinePatchDrawable mFrameBorder;

    public PuzzleBackgroundLayer(@Nonnull Resources res,
                                 int width,
                                 int height,
                                 int bgTileResource,
                                 int bgFrameResource,
                                 int framePadding)
    {
        mWidth = width;
        mHeight = height;
        mBgTileResource = bgTileResource;
        Bitmap bitmap = BitmapFactory.decodeResource(res, mBgTileResource);
        mBgTileBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mFrameBorder = (NinePatchDrawable) res.getDrawable(bgFrameResource);
        mFramePadding = framePadding;
    }

    @Override
    public void drawLayer(Canvas canvas)
    {
        Bitmap bm = Bitmap.createBitmap(mWidth - 2 * mFramePadding, mHeight - 2*mFramePadding, Bitmap.Config.ARGB_8888);
        Canvas mainCanvas = new Canvas(bm);
        PuzzleBackgroundLayer.fillBackgroundWithTile(mainCanvas, mBgTileBitmap, mPaint);
        canvas.drawBitmap(bm, mFramePadding, mFramePadding, mPaint);
        bm.recycle();

        Rect rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        mFrameBorder.setBounds(rect);
        mFrameBorder.draw(canvas);
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
