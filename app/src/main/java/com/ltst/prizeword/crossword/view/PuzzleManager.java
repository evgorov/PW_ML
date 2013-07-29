package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.ltst.prizeword.tools.BitmapHelper;

import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleManager
{
    private Rect mPuzzleRect;
    private Rect mPuzzleViewRect;
    private Rect mViewport;
    private int mPuzzleToScreenRatio;
    private Matrix mMatrix;

    private Rect mDrawingRect;
    private @Nullable Canvas mDrawingCanvas;
    private @Nullable Bitmap mDrawingBitmap;
    private int mDrawingMarginX;
    private int mDrawingMarginY;

    private int mTileWidth;
    private int mTileHeight;
    private Point mFocusDrawPoint;
    private Point mFocusViewPoint;

    private @Nullable PuzzleBackgroundLayer mBgLayer;
    private @Nullable PuzzleTilesLayer mTilesLayer;
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
        BitmapFactory.decodeResource(mContext.getResources(), PuzzleViewInformation.getLetterEmpty(), options);
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

        // compute drawing dimensions
        int padding = mInfo.getPadding();
        int framePadding = mInfo.getFramePadding(mContext.getResources()) ;
        int cols = mInfo.getPuzzleColumnsCount();
        int rows = mInfo.getPuzzleRowsCount();
        int tileGap = mInfo.getTileGap();
        mDrawingMarginX = 2*mTileWidth;
        mDrawingMarginY = 2*mTileHeight;
        int drawingWidth = 2 * (padding + framePadding) + cols * mTileWidth + (cols - 1) * tileGap + mDrawingMarginX;
        int drawingHeight = 2 * (padding + framePadding) + rows * mTileHeight + (rows - 1) * tileGap + mDrawingMarginY;
        mDrawingRect = new Rect(0, 0, drawingWidth, drawingHeight);

        Log.i("drawing dims: " + drawingWidth + " " + drawingHeight);
        mDrawingBitmap = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888);
        mDrawingCanvas = new Canvas(mDrawingBitmap);
        mFocusDrawPoint = new Point(mPuzzleRect.width()/2, mPuzzleRect.height()/2);
        mFocusViewPoint = new Point(mPuzzleViewRect.width()/2, mPuzzleViewRect.height()/2);

        // init bg layer
        mBgLayer = new PuzzleBackgroundLayer(mContext.getResources(), mPuzzleRect, mDrawingRect,
                PuzzleViewInformation.getBackgroundTile(), PuzzleViewInformation.getBackgroundFrame(), padding, framePadding, mPuzzleToScreenRatio);
//        mBgLayer.setDrawingMarginX(mDrawingMarginX/2);
//        mBgLayer.setDrawingMarginY(mDrawingMarginY/2);
        // init tiles layer
//        mTilesLayer = new PuzzleTilesLayer(mContext.getResources(), mDrawingRect,
//                                           cols, rows, mPuzzleToScreenRatio);
//        mTilesLayer.setPadding(padding + framePadding);
//        mTilesLayer.setTileGap(tileGap);
//        mTilesLayer.setStateMatrix(mInfo.getStateMatrix());
//        mTilesLayer.setQuestions(mInfo.getPuzzleQuestions());
//        mTilesLayer.initTileTextPadding(mTileWidth);

        // compute scaled viewport
//        float widthScale = (float)mPuzzleViewRect.width()/(float)mDrawingRect.width();
//        float heightScale = (float)mPuzzleViewRect.height()/(float)mDrawingRect.height();
//        float scale = Math.min(widthScale, heightScale);
//        mViewport = new Rect(0, 0, (int)(drawingWidth * scale), (int) (drawingHeight * scale));
        mViewport = new Rect(0, 0, drawingWidth, drawingHeight);
//        int translateX = (mPuzzleRect.width() - mDrawingRect.width());
//        int translateY = (mPuzzleRect.height() - mDrawingRect.height());
//        mViewport.set(translateX, translateY, drawingWidth + translateX, drawingHeight + translateY);

        isRecycled = false;
    }

    private int curScrollX = 0;
    private int curScrollY = 0;
    private boolean mNeedRedraw = true;

    public void onScrollEvent(int offsetX, int offsetY)
    {
        curScrollX += Math.abs(offsetX);
        curScrollY += Math.abs(offsetY);

        if(curScrollX > mDrawingMarginX || curScrollY > mDrawingMarginY)
        {
            mNeedRedraw = true;
            curScrollX = 0;
            curScrollY = 0;
        }

        mFocusDrawPoint.x -= offsetX;
        mFocusDrawPoint.y -= offsetY;
        Rect drawingRectWithMargin = mDrawingRect;
        drawingRectWithMargin.left += mDrawingMarginX;
        drawingRectWithMargin.right -= mDrawingMarginX;
        drawingRectWithMargin.top += mDrawingMarginY;
        drawingRectWithMargin.bottom -= mDrawingMarginY;

        checkFocusPoint(mFocusDrawPoint, mDrawingRect, mPuzzleRect);
        Log.i("TOUCH", "draw x: " + mFocusDrawPoint.x + " y: " + mFocusDrawPoint.y);

        mFocusViewPoint.x -= offsetX;
        mFocusViewPoint.y -= offsetY;
        checkFocusPoint(mFocusViewPoint, mPuzzleViewRect, mDrawingRect);
        Log.i("TOUCH", "view x: " + mFocusViewPoint.x + " y: " + mFocusViewPoint.y);

    }

    private void fillDrawingCanvasWithBg()
    {
        if (mDrawingCanvas == null)
        {
            return;
        }
        Bitmap bitmap = BitmapHelper.loadBitmapInSampleSize(mContext.getResources(),
                mInfo.getCanvasBackgroundTileRes(), mPuzzleToScreenRatio);
        PuzzleBackgroundLayer.fillBackgroundByDrawable(mContext.getResources(), mDrawingCanvas, bitmap);
        bitmap.recycle();

    }

    private void drawPuzzleTilesBg()
    {
        if (mBgLayer != null)
        {
            mBgLayer.drawLayer(mDrawingCanvas, mViewport);
        }
    }

    private void drawPuzzleTiles()
    {
        if (mTilesLayer != null)
        {
            mTilesLayer.drawLayer(mDrawingCanvas, mViewport);
        }
    }

    private void configureMatrix()
    {
        mMatrix.reset();
        float translateX = mFocusViewPoint.x - mPuzzleViewRect.width()/2;
        float translateY = mFocusViewPoint.y - mPuzzleViewRect.height()/2;
        mMatrix.postTranslate(-translateX, -translateY);

//        mMatrix.postTranslate(-(mDrawingRect.width() - mPuzzleViewRect.width()), -(mDrawingRect.height() - mPuzzleViewRect.height()));
//        mMatrix.postTranslate(-(mDrawingRect.width() - mPuzzleViewRect.width()), 0);
    }

    private void configureViewport()
    {
        int viewLeft = mFocusDrawPoint.x - mDrawingRect.width()/2;
        int viewTop = mFocusDrawPoint.y - mDrawingRect.height()/2;
        int viewRight = mFocusDrawPoint.x + mDrawingRect.width()/2;
        int viewBottom = mFocusDrawPoint.y + mDrawingRect.height()/2;
        mViewport.set(viewLeft, viewTop, viewRight, viewBottom);
//        mViewport.set(mPuzzleRect.width() - mDrawingRect.width(),
//                mPuzzleRect.height() - mDrawingRect.height(),
//                mPuzzleRect.width(), mPuzzleRect.height());
    }

    public void drawPuzzle(@Nonnull Canvas screenCanvas)
    {
        if (mPuzzleViewRect == null || mDrawingBitmap == null || mDrawingCanvas == null)
        {
            return;
        }
        if (!isRecycled)
        {
            configureViewport();
            if(mNeedRedraw)
            {
                fillDrawingCanvasWithBg();
                drawPuzzleTilesBg();
//                drawPuzzleTiles();
                mNeedRedraw = false;
            }

            int saveCount = screenCanvas.getSaveCount();
            screenCanvas.save();

            configureMatrix();
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
        if (mTilesLayer != null)
        {
            mTilesLayer.recycle();
        }
        if (mDrawingBitmap != null)
        {
            mDrawingBitmap.recycle();
        }
    }

    private void checkFocusPoint(@Nonnull Point p, @Nonnull Rect focusRect, @Nonnull Rect viewRect)
    {
        int halfWidth = focusRect.width()/2;
        int halfHeight = focusRect.height()/2;

        if(p.x - halfWidth < viewRect.left)
            p.x = halfWidth;
        if(p.x + halfWidth > viewRect.right)
            p.x = viewRect.right - halfWidth;
        if(p.y - halfHeight < viewRect.top)
            p.y = halfHeight;
        if(p.y + halfHeight > viewRect.bottom)
            p.y = viewRect.bottom - halfHeight;
    }
}
