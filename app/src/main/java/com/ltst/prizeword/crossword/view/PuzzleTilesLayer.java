package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

public class PuzzleTilesLayer implements ICanvasLayer
{
    private int mPuzzleWidth;
    private int mPuzzleHeight;

    private @Nonnull Bitmap mEmptyLetter;
    private @Nonnull Bitmap mQuestionNormal;
    private @Nonnull Paint mPaint;
    private int mPadding = 0;
    private int mTileGap = 0;

    public PuzzleTilesLayer(@Nonnull Context context, int puzzleWidth, int puzzleHeight)
    {
        mPuzzleWidth = puzzleWidth;
        mPuzzleHeight = puzzleHeight;

        mEmptyLetter = BitmapFactory.decodeResource(context.getResources(), R.drawable.gamefield_tile_letter_empty);
        mQuestionNormal = BitmapFactory.decodeResource(context.getResources(), R.drawable.gamefield_tile_question_new);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public int getTileWidth()
    {
        return mEmptyLetter.getWidth();
    }

    public int getTileHeight()
    {
        return mEmptyLetter.getHeight();
    }

    public int getPadding()
    {
        return mPadding;
    }

    public void setPadding(int padding)
    {
        mPadding = padding;
    }

    public int getTileGap()
    {
        return mTileGap;
    }

    public void setTileGap(int tileGap)
    {
        mTileGap = tileGap;
    }

    @Override
    public void drawLayer(Canvas canvas)
    {
        int tileWidth = mEmptyLetter.getWidth();
        int tileHeight = mEmptyLetter.getHeight();
        RectF rect = new RectF(mPadding, mPadding, tileWidth + mPadding, tileHeight + mPadding);

        for (int i = 0; i < mPuzzleHeight; i++)
        {
            for (int j = 0; j < mPuzzleWidth; j++)
            {
                canvas.drawBitmap(mEmptyLetter, null, rect, mPaint);
                rect.left += tileWidth + mTileGap;
                rect.right += tileWidth + mTileGap;
            }
            rect.left = mPadding;
            rect.right = tileWidth + mPadding;
            rect.top += tileHeight + mTileGap;
            rect.bottom += tileHeight + mTileGap;
        }
    }

    @Override
    public void recycle()
    {
        mEmptyLetter.recycle();
        mQuestionNormal.recycle();
    }
}
