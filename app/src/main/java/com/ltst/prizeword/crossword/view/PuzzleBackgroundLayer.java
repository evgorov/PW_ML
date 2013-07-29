package com.ltst.prizeword.crossword.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;

import com.ltst.prizeword.tools.BitmapHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleBackgroundLayer implements ICanvasLayer
{
    private @Nonnull Rect mDrawingRect;
    private @Nonnull Rect mPuzzleRect;
    private int mBgTileResource;
    private int mFramePadding;
    private int mPadding;
    private int mDrawingMargin;

    private @Nullable Bitmap mBgTileBitmap;
    private @Nonnull Paint mPaint;
    private @Nonnull NinePatchDrawable mFrameBorder;

    private int mScreenRatio;
    private @Nonnull Resources mResources;

    public PuzzleBackgroundLayer(@Nonnull Resources res,
                                 @Nonnull Rect puzzleRect,
                                 @Nonnull Rect drawingRect,
                                 int bgTileResource,
                                 int bgFrameResource,
                                 int viewPadding,
                                 int framePadding,
                                 int screenRatio)
    {
        mResources = res;
        mDrawingRect = drawingRect;
        mPuzzleRect = puzzleRect;
        mBgTileResource = bgTileResource;
        mScreenRatio = screenRatio;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mFrameBorder = (NinePatchDrawable) res.getDrawable(bgFrameResource);
        mFramePadding = framePadding;
        mPadding = viewPadding;
        loadBgTileBitmap();
    }

    private void loadBgTileBitmap()
    {
        mBgTileBitmap = BitmapHelper.loadBitmapInSampleSize(mResources, mBgTileResource, mScreenRatio);
    }

    @Override
    public void drawLayer (@Nonnull Canvas canvas, @Nonnull Rect viewport)
    {

//        if (mBgTileBitmap == null || mBgTileBitmap.isRecycled())
        if(mBgTileBitmap == null)
        {
            return;
//            loadBgTileBitmap();
        }
        if(!mBgTileBitmap.isRecycled())
        {

//            int width = viewport.width();
//            int height = viewport.height();
//            int horOffset = (mDrawingRect.width() - width)/2;
//            int verOffset = (mDrawingRect.height() - height)/2;
//            width -= 2 * mFramePadding;
//            height -= 2 * mFramePadding;
//            Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//
//            Canvas mainCanvas = new Canvas(bm);
//            PuzzleBackgroundLayer.fillBackgroundByDrawable(mResources, mainCanvas, mBgTileBitmap);
//            canvas.drawBitmap(bm, horOffset + mDrawingRect.left + mFramePadding,
//                                  verOffset + mDrawingRect.top + mFramePadding, mPaint);
//            bm.recycle();
//            Rect frameRect = new Rect(horOffset + mDrawingRect.left,
//                                      verOffset + mDrawingRect.top,
//                                      mDrawingRect.right - horOffset,
//                                      mDrawingRect.bottom - verOffset);
//            mFrameBorder.setBounds(frameRect);
//            mFrameBorder.draw(canvas);

            int topFrameOffset = Math.abs(viewport.top - mPuzzleRect.top);
            int leftFrameOffset = Math.abs(viewport.left - mPuzzleRect.left);
            int rightFrameOffset = Math.abs(viewport.right - mPuzzleRect.right);
            int bottomFrameOffset = Math.abs(viewport.bottom - mPuzzleRect.bottom);
            int frameBgPadding = mPadding + mFramePadding;
            int bgTop = (topFrameOffset < frameBgPadding) ?
                    frameBgPadding - topFrameOffset :
                    mDrawingRect.top + frameBgPadding;
            int bgLeft = (leftFrameOffset < frameBgPadding) ?
                    frameBgPadding - leftFrameOffset :
                    mDrawingRect.left + frameBgPadding;
            int bgRight = (rightFrameOffset < frameBgPadding) ?
                    mDrawingRect.right - (frameBgPadding - rightFrameOffset):
                    mDrawingRect.right;
            int bgBottom = (bottomFrameOffset < frameBgPadding) ?
                    mDrawingRect.bottom - (frameBgPadding - bottomFrameOffset):
                    mDrawingRect.bottom;

            Rect bgRect = new Rect(bgLeft, bgTop, bgRight, bgBottom);
            Bitmap bm = Bitmap.createBitmap(bgRect.width(), bgRect.height(), Bitmap.Config.ARGB_8888);
            Canvas mainCanvas = new Canvas(bm);
            PuzzleBackgroundLayer.fillBackgroundByDrawable(mResources, mainCanvas, mBgTileBitmap);
            canvas.drawBitmap(bm, null, bgRect, mPaint);
            bm.recycle();

            canvas.save();
            canvas.translate(- viewport.left/2, - viewport.top/2);

            Rect frameRect = new Rect(mPadding + mPuzzleRect.left,
                                        mPadding + mPuzzleRect.top,
                                        mPuzzleRect.right - mPadding,
                                        mPuzzleRect.bottom - mPadding);
            mFrameBorder.setBounds(frameRect);
            mFrameBorder.draw(canvas);

            canvas.restore();
        }
//        recycle();
    }

    @Override
    public void recycle()
    {
        if (mBgTileBitmap != null)
        {
            mBgTileBitmap.recycle();
        }
    }

    public static void fillBackgroundByDrawable(@Nonnull Resources res, @Nonnull Canvas canvas, @Nonnull Bitmap tileBitmap)
    {
        Rect rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        BitmapDrawable drawable = new BitmapDrawable(res, tileBitmap);
        drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        drawable.setBounds(rect);
        drawable.draw(canvas);
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
