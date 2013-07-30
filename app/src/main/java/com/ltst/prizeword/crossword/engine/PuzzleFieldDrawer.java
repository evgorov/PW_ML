package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;

import javax.annotation.Nonnull;

public class PuzzleFieldDrawer
{
    private @Nonnull BitmapManager mBitmapManager;
    private @Nonnull Context mContext;
    private @Nonnull PuzzleResources mInfo;

    private @Nonnull Rect mPuzzleRect;
    private int mTileWidth;
    private int mTileHeight;
    private @Nonnull NinePatchDrawable mFrameBorder;

    private int mDrawingOffsetX = 0;
    private int mDrawingOffsetY = 0;

    public PuzzleFieldDrawer(@Nonnull Context context, @Nonnull PuzzleResources info)
    {
        mContext = context;
        mInfo = info;
        mBitmapManager = new BitmapManager(context);
        mFrameBorder = (NinePatchDrawable) mContext.getResources().getDrawable(PuzzleResources.getBackgroundFrame());
        measureDimensions();
    }

    private void measureDimensions()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), PuzzleResources.getLetterEmpty(), options);
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

    public void enableScaling(float scaleWidth, float scaleHeight)
    {
        int scaledWidth = (int)(mPuzzleRect.width() * scaleWidth);
        int scaledHeight = (int)(mPuzzleRect.height() * scaleHeight);
        mDrawingOffsetX = scaledWidth/2 - mPuzzleRect.width()/2;
        mDrawingOffsetY = scaledHeight/2 - mPuzzleRect.height()/2;

        mPuzzleRect.right += mDrawingOffsetX * 2;
        mPuzzleRect.bottom += mDrawingOffsetY * 2;
    }

    public void loadResources()
    {
        mBitmapManager.addBitmap(PuzzleResources.getBackgroundTile());
        mBitmapManager.addBitmap(PuzzleResources.getCanvasBackgroundTileRes());
    }

    public void drawBackground(@Nonnull Canvas canvas)
    {
        // draw view bg
        RectF bgRectF = new RectF(mPuzzleRect);
        fillRectWithBitmap(canvas, bgRectF, PuzzleResources.getCanvasBackgroundTileRes());

        // draw puzzle bg
        int padding = mInfo.getPadding();
        int framePadding = mInfo.getFramePadding(mContext.getResources());
        int puzzlePadding = padding + framePadding;
        RectF puzzleBackgroundRect = new RectF(puzzlePadding + mPuzzleRect.left + mDrawingOffsetX,
                puzzlePadding + mPuzzleRect.top + mDrawingOffsetY,
                mPuzzleRect.right - puzzlePadding - mDrawingOffsetX,
                mPuzzleRect.bottom - puzzlePadding - mDrawingOffsetY);
        fillRectWithBitmap(canvas, puzzleBackgroundRect, PuzzleResources.getBackgroundTile());

        // draw frame
        Rect frameRect = new Rect(padding + mDrawingOffsetX + mPuzzleRect.left,
                padding + mDrawingOffsetY + mPuzzleRect.top,
                mPuzzleRect.right - padding - mDrawingOffsetX,
                mPuzzleRect.bottom - padding - mDrawingOffsetY);
        mFrameBorder.setBounds(frameRect);
        mFrameBorder.draw(canvas);
    }

    public void unloadResources()
    {
        mBitmapManager.recycle();
    }

    public int getWidth()
    {
        return mPuzzleRect.width();
    }

    public int getHeight()
    {
        return mPuzzleRect.height();
    }

    public int getActualWidth()
    {
        return mPuzzleRect.width() - mDrawingOffsetX * 2;
    }

    public int getActualHeight()
    {
        return mPuzzleRect.height() - mDrawingOffsetY * 2;
    }

    public int getCenterX()
    {
        return mPuzzleRect.width()/2;
    }

    public int getCenterY()
    {
        return mPuzzleRect.height()/2;
    }

    public void checkFocusPoint(@Nonnull Point p, @Nonnull Rect viewRect)
    {
        int halfWidth = viewRect.width()/2;
        int halfHeight = viewRect.height()/2;

        if(p.x - halfWidth < mPuzzleRect.left + mDrawingOffsetX)
            p.x = halfWidth + mDrawingOffsetX;
        if(p.x + halfWidth > mPuzzleRect.right - mDrawingOffsetX)
            p.x = mPuzzleRect.right - halfWidth - mDrawingOffsetX;
        if(p.y - halfHeight < mPuzzleRect.top + mDrawingOffsetY)
            p.y = halfHeight + mDrawingOffsetY;
        if(p.y + halfHeight > mPuzzleRect.bottom - mDrawingOffsetY)
            p.y = mPuzzleRect.bottom - halfHeight - mDrawingOffsetY;
    }

    private void fillRectWithBitmap(@Nonnull Canvas canvas, @Nonnull RectF rect, int res)
    {
        int tileWidth = mBitmapManager.getWidth(res);
        int tileHeight = mBitmapManager.getHeight(res);
        RectF tileRect = new RectF(rect.left, rect.top, tileWidth + rect.left, tileHeight + rect.top);

        while (tileRect.bottom < rect.bottom)
        {
            while (tileRect.right < rect.right)
            {
                mBitmapManager.drawResource(res, canvas, tileRect);
                tileRect.left += tileWidth;
                tileRect.right += tileWidth;
            }
            tileRect.right = rect.right;
            mBitmapManager.drawResource(res, canvas, tileRect);
            tileRect.left = rect.left;
            tileRect.right = tileWidth + rect.left;

            tileRect.top += tileHeight;
            tileRect.bottom += tileHeight;
        }

        tileRect.left = rect.left;
        tileRect.right = tileWidth + rect.left;
        tileRect.bottom = rect.bottom;
        while (tileRect.right < rect.right)
        {
            mBitmapManager.drawResource(res, canvas, tileRect);
            tileRect.left += tileWidth;
            tileRect.right += tileWidth;
        }
        tileRect.right = rect.right;
        mBitmapManager.drawResource(res, canvas, tileRect);
    }
}

