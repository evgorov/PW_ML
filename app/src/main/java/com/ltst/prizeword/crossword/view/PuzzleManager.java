package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;

import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleManager
{
    private Rect mPuzzleRect;
    private Rect mPuzzleViewRect;
    private int mPuzzleToScreenRatio;
    private Matrix mMatrix;

    private Rect mDrawingRect;
    private @Nullable Canvas mDrawingCanvas;
    private @Nullable Bitmap mDrawingBitmap;

    private int mTileWidth;
    private int mTileHeight;
    private Point mFocusPoint;

    private @Nullable PuzzleBackgroundLayer mBgLayer;
    private @Nonnull Paint mPaint;

    private @Nonnull PuzzleViewInformation mInfo;
    private @Nonnull Context mContext;
    private boolean isRecycled;

    public PuzzleManager(@Nonnull Context context, @Nonnull PuzzleViewInformation info)
    {
        mContext = context;
        mInfo = info;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        isRecycled = true;
        mMatrix = new Matrix();
        measureDimensions();
    }

    private void measureDimensions()
    {
        Log.i("measuring dims");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), mInfo.getLetterEmpty(), options);
        mTileWidth = options.outWidth;
        mTileHeight = options.outHeight;

        int padding = mInfo.getPadding();
        int framePadding = mInfo.getFramePadding(mContext.getResources());
        int cellWidth = mInfo.getPuzzleColumnsCount();
        int cellHeight = mInfo.getPuzzleRowsCount();
        int tileGap = mInfo.getTileGap();
        int puzzleWidth = 2 * (padding + framePadding) + cellWidth * mTileWidth + (cellWidth - 1) * tileGap;
        int puzzleHeight = 2 * (padding + framePadding) + cellHeight * mTileHeight + (cellHeight - 1) * tileGap;
        mPuzzleRect = new Rect(0, 0, puzzleWidth, puzzleHeight);
    }

    public boolean isRecycled()
    {
        return isRecycled;
    }

    public void setPuzzleViewRect(Rect puzzleViewRect)
    {
        mPuzzleViewRect = puzzleViewRect;
        mPuzzleToScreenRatio = Math.min(mPuzzleRect.width()/mPuzzleViewRect.width(),
                                        mPuzzleRect.height()/mPuzzleViewRect.height());
        Log.i("Setting view rect: "+ mPuzzleViewRect.width() + " " + mPuzzleViewRect.height() + " " + mPuzzleToScreenRatio);

        int padding = mInfo.getPadding() / mPuzzleToScreenRatio;
        int framePadding = mInfo.getFramePadding(mContext.getResources()) / mPuzzleToScreenRatio;
        int cols = mInfo.getPuzzleColumnsCount();
        int rows = mInfo.getPuzzleRowsCount();
        int tileGap = mInfo.getTileGap() / mPuzzleToScreenRatio;
        int drawingWidth = 2 * (padding + framePadding) + cols * mTileWidth/mPuzzleToScreenRatio + (cols - 1) * tileGap + 2 * mTileWidth;
        int drawingHeight = 2 * (padding + framePadding) + rows * mTileHeight/mPuzzleToScreenRatio + (rows - 1) * tileGap + 2 * mTileHeight;
        mDrawingRect = new Rect(0, 0, drawingWidth, drawingHeight);

        Log.i("drawing dims: " + drawingWidth + " " + drawingHeight);
        mDrawingBitmap = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888);
        mDrawingCanvas = new Canvas(mDrawingBitmap);
        mFocusPoint = new Point(drawingWidth/2, drawingHeight/2);

        mBgLayer = new PuzzleBackgroundLayer(mContext.getResources(), mDrawingRect,
                mInfo.getBackgroundTile(), mInfo.getBackgroundFrame(), padding + framePadding, mPuzzleToScreenRatio);

        isRecycled = false;
    }

    private void fillDrawingCanvasWithBg()
    {
        if (mDrawingCanvas == null)
        {
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = mPuzzleToScreenRatio;
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mInfo.getCanvasBackgroundTileRes(), options);
        PuzzleBackgroundLayer.fillBackgroundByDrawable(mContext.getResources(), mDrawingCanvas, bitmap);
        bitmap.recycle();

//        mPaint.setColor(Color.GREEN);
//        mDrawingCanvas.drawCircle(0,0,100,mPaint);
//        mDrawingCanvas.drawCircle(mFocusPoint.x, mFocusPoint.y, 50, mPaint);
//        mPaint.setColor(Color.RED);
//        mDrawingCanvas.drawRect(0,0,50,50, mPaint);
    }

    private void drawPuzzleTilesBg()
    {
        if (mBgLayer != null)
        {
            float widthScale = (float)mPuzzleViewRect.width()/(float)mDrawingRect.width();
            float heightScale = (float)mPuzzleViewRect.height()/(float)mDrawingRect.height();
            float scale = Math.min(widthScale, heightScale);
            mBgLayer.drawLayer(mDrawingCanvas, scale);
        }
    }

    private void configureBounds()
    {
        mMatrix.reset();
        float translateX = mFocusPoint.x - mPuzzleViewRect.width()/2;
        float translateY = mFocusPoint.y - mPuzzleViewRect.height()/2;
        mMatrix.postTranslate(-translateX, -translateY);
    }

    public void drawPuzzle(@Nonnull Canvas screenCanvas)
    {
        if (mPuzzleViewRect == null || mDrawingBitmap == null || mDrawingCanvas == null)
        {
            return;
        }
        if (!isRecycled)
        {
            fillDrawingCanvasWithBg();
            drawPuzzleTilesBg();

            int saveCount = screenCanvas.getSaveCount();
            screenCanvas.save();

            configureBounds();
            screenCanvas.concat(mMatrix);
            screenCanvas.drawBitmap(mDrawingBitmap, 0, 0, mPaint);

            screenCanvas.restoreToCount(saveCount);
        }
    }



    public void recycle()
    {
        isRecycled = true;
        if (mBgLayer != null)
        {
            mBgLayer.recycle();
        }
        if (mDrawingBitmap != null)
        {
            mDrawingBitmap.recycle();
        }
    }
}
