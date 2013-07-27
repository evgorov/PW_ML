package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleManager
{
    private Rect mPuzzleRect;
    private Rect mPuzzleViewRect;
    private int mPuzzleToScreenRatio;

    private int mTileWidth;
    private int mTileHeight;

    private @Nonnull PuzzleViewInformation mInfo;
    private @Nonnull Context mContext;

    public PuzzleManager(@Nonnull Context context, @Nonnull PuzzleViewInformation info)
    {
        mContext = context;
        mInfo = info;
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
        int framePadding = mInfo.getFramePadding();
        int cellWidth = mInfo.getPuzzleColumnsCount();
        int cellHeight = mInfo.getPuzzleRowsCount();
        int tileGap = mInfo.getTileGap();
        int puzzleWidth = 2 * (padding + framePadding) + cellWidth * mTileWidth + (cellWidth - 1) * tileGap;
        int puzzleHeight = 2 * (padding + framePadding) + cellHeight * mTileHeight + (cellHeight - 1) * tileGap;
        mPuzzleRect = new Rect(0, 0, puzzleWidth, puzzleHeight);
    }

    public void setPuzzleViewRect(Rect puzzleViewRect)
    {
        mPuzzleViewRect = puzzleViewRect;
        mPuzzleToScreenRatio = Math.min(mPuzzleRect.width()/mPuzzleViewRect.width(),
                                        mPuzzleRect.height()/mPuzzleViewRect.height());
        Log.i("Setting view rect");
    }

    public void drawPuzzle(@Nonnull Canvas screenCanvas)
    {
        if (mPuzzleViewRect == null)
        {
            return;
        }
        Log.i("Drawing");
        Paint p = new Paint();
        p.setColor(Color.GREEN);
        screenCanvas.drawRect(mPuzzleViewRect, p);
    }
}
