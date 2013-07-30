package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import javax.annotation.Nonnull;

public class PuzzleFieldDrawer
{
    private @Nonnull BitmapManager mBitmapManager;
    private @Nonnull Context mContext;
    private @Nonnull PuzzleResources mInfo;

    private @Nonnull Rect mPuzzleRect;
    private int mTileWidth;
    private int mTileHeight;

    public PuzzleFieldDrawer(@Nonnull Context context, @Nonnull PuzzleResources info)
    {
        mContext = context;
        mInfo = info;
        mBitmapManager = new BitmapManager(context);
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

    public void loadResources()
    {
        mBitmapManager.addBitmap(PuzzleResources.getBackgroundTile());
        mBitmapManager.addBitmap(mInfo.getCanvasBackgroundTileRes());
    }

    public void unloadResources()
    {
        mBitmapManager.recycle();
    }
}
