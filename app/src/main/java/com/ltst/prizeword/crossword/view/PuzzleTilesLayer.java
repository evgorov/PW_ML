package com.ltst.prizeword.crossword.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleTilesLayer implements ICanvasLayer
{
    private final int FONT_SIZE = 22;

    private int mPuzzleWidth;
    private int mPuzzleHeight;

    private @Nonnull Resources mResources;
    private @Nonnull Bitmap mEmptyLetter;
    private @Nonnull Bitmap mQuestionNormal;
    private @Nonnull Paint mPaint;
    private int mPadding = 0;
    private int mTileGap = 0;
    private @Nullable byte[][] mStateMatrix;
    private @Nullable List<String> mQuestions;

    public PuzzleTilesLayer(@Nonnull Resources res, int puzzleWidth, int puzzleHeight)
    {
        mPuzzleWidth = puzzleWidth;
        mPuzzleHeight = puzzleHeight;
        mResources = res;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(FONT_SIZE);
    }

    public void initBitmaps(int emptyLetterResId,
                            int inputLetterResId,
                            int correctLetterOverlayResId,
                            int emptyQuestionResId,
                            int inputQuestionResId,
                            int wrongQuestionResId,
                            int correctQuesitonResId)
    {
        mEmptyLetter = BitmapFactory.decodeResource(mResources, emptyLetterResId);
        mQuestionNormal = BitmapFactory.decodeResource(mResources, emptyQuestionResId);
    }

    public void setStateMatrix(@Nullable byte[][] stateMatrix)
    {
        mStateMatrix = stateMatrix;
    }

    public void setQuestions(@Nullable List<String> questions)
    {
        mQuestions = questions;
    }

    public int getTileWidth()
    {
        return mEmptyLetter.getWidth();
    }

    public int getTileHeight()
    {
        return mEmptyLetter.getHeight();
    }

    public void setPadding(int padding)
    {
        mPadding = padding;
    }

    public void setTileGap(int tileGap)
    {
        mTileGap = tileGap;
    }

    @Override
    public void drawLayer(Canvas canvas)
    {
        if (mStateMatrix == null)
        {
            return;
        }

        int tileWidth = mEmptyLetter.getWidth();
        int tileHeight = mEmptyLetter.getHeight();
        RectF rect = new RectF(mPadding, mPadding, tileWidth + mPadding, tileHeight + mPadding);

        int questionsIndex = 0;
        for (int i = 0; i < mPuzzleHeight; i++)
        {
            for (int j = 0; j < mPuzzleWidth; j++)
            {
                if (mStateMatrix[j][i] == PuzzleViewInformation.STATE_LETTER)
                    canvas.drawBitmap(mEmptyLetter, null, rect, mPaint);
                if (mStateMatrix[j][i] == PuzzleViewInformation.STATE_QUESTION)
                {
                    canvas.drawBitmap(mQuestionNormal, null, rect, mPaint);
                    String question = mQuestions.get(questionsIndex);
                    
                    canvas.drawText(question, rect.left + tileWidth/2, rect.top + tileHeight/2, mPaint);
                    questionsIndex++;
                }

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
