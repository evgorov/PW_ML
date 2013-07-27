package com.ltst.prizeword.crossword.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.tools.BitmapHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleTilesLayer implements ICanvasLayer
{
    private int mFontSize;
    private int mTextHeight;

    private int mPuzzleWidth;
    private int mPuzzleHeight;
    private int mTileTextPadding;

    private @Nonnull Rect mDrawingRect;
    private @Nonnull Resources mResources;
    private @Nonnull Bitmap mEmptyLetter;
    private @Nonnull Bitmap mQuestionNormal;
    private @Nonnull Paint mPaint;
    private int mPadding = 0;
    private int mTileGap = 0;
    private @Nullable byte[][] mStateMatrix;
    private @Nullable List<PuzzleQuestion> mQuestions;
    private int mScreenRatio;



    public PuzzleTilesLayer(@Nonnull Resources res, @Nonnull Rect drawingRect,
                            int puzzleWidth, int puzzleHeight, int screenRatio)
    {
        mPuzzleWidth = puzzleWidth;
        mPuzzleHeight = puzzleHeight;
        mResources = res;
        mScreenRatio = screenRatio;
        mDrawingRect = drawingRect;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mFontSize = res.getDimensionPixelSize(R.dimen.puzzle_question_font_size)/screenRatio;
        mPaint.setTextSize(mFontSize);
        mPaint.setStyle(Paint.Style.FILL);
        Typeface tf = Typeface.create("Helvetica",Typeface.BOLD);
        mPaint.setTypeface(tf);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        mTextHeight = (int) mPaint.getTextSize();
    }

    public void initTileTextPadding(int tileWidth)
    {
        mTileTextPadding = tileWidth/6/mScreenRatio;
    }

    public void setStateMatrix(@Nullable byte[][] stateMatrix)
    {
        mStateMatrix = stateMatrix;
    }

    public void setQuestions(@Nullable List<PuzzleQuestion> questions)
    {
        mQuestions = questions;
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
    public void drawLayer(@Nonnull Canvas canvas, @Nonnull Rect viewport)
    {
        if (mStateMatrix == null)
        {
            return;
        }

        if(mEmptyLetter == null || mQuestionNormal == null)
        {
            loadBitmaps();
        }
        if(mEmptyLetter.isRecycled() || mQuestionNormal.isRecycled())
        {
            loadBitmaps();
        }

        int width = viewport.width();
        int height = viewport.height();
        int horOffset = (mDrawingRect.width() - width)/2;
        int verOffset = (mDrawingRect.height() - height)/2;
        width -= 2 * mPadding;
        height -= 2 * mPadding;

        int tileWidth = mEmptyLetter.getWidth();
        int tileHeight = mEmptyLetter.getHeight();

        int preferredTileWidth = (width - mTileGap * (mPuzzleWidth - 1) - mPadding * 2)/mPuzzleWidth;

        if(tileWidth > preferredTileWidth)
        {
            tileWidth = preferredTileWidth;
        }

        int preferredTileHeight = (height - mTileGap * (mPuzzleHeight -1) - mPadding * 2)/mPuzzleHeight;

        if(tileHeight > preferredTileHeight)
        {
            tileHeight = preferredTileHeight;
        }

        RectF rect = new RectF(2*mPadding + horOffset,
                               2*mPadding + verOffset,
                               tileWidth + 2*mPadding + horOffset,
                               tileHeight + 2*mPadding + verOffset);

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
                    drawQuestionText(canvas, questionsIndex, rect);
                    questionsIndex++;
                }

                rect.left += tileWidth + mTileGap;
                rect.right += tileWidth + mTileGap;
            }
            rect.left = 2*mPadding + horOffset;
            rect.right = tileWidth + 2*mPadding + horOffset;
            rect.top += tileHeight + mTileGap;
            rect.bottom += tileHeight + mTileGap;
        }

        recycle();
    }

    private void loadBitmaps()
    {
        mEmptyLetter = BitmapHelper.loadBitmapInSampleSize(mResources,
                PuzzleViewInformation.getLetterEmpty(), mScreenRatio);
        mQuestionNormal = BitmapHelper.loadBitmapInSampleSize(mResources,
                PuzzleViewInformation.getQuestionEmpty(), mScreenRatio);
    }

    private void drawQuestionText(@Nonnull Canvas canvas, int questionsIndex, @Nonnull RectF tileRect)
    {
        if (mQuestions == null)
        {
            return;
        }

        String question = mQuestions.get(questionsIndex).questionText;
        RectF textRect = new RectF(tileRect.left + mTileTextPadding,
                tileRect.top + mTileTextPadding,
                tileRect.right - mTileTextPadding,
                tileRect.bottom - mTileTextPadding);
        int textWidth = (int)(textRect.right - textRect.left);
        int textHeight = (int)(textRect.bottom - textRect.top);
        List<String> filledText = fillTextInWidth(question, textWidth);
        int lineCount = filledText.size();
        int totalLineHeight = lineCount * mTextHeight;
        int startCoord = (textHeight - totalLineHeight)/2;
        int lineIndex = 0;
        for (String s : filledText)
        {
            canvas.drawText(s, textRect.left + textWidth/2,
                    textRect.top + startCoord + mTextHeight * (lineIndex + 1),
                    mPaint);
            lineIndex++;
        }
    }

    private @Nonnull List<String> fillTextInWidth(@Nonnull String text, int width)
    {
        List<String> strings = new ArrayList<String>();
        int start = 0;
        int end = text.length() - 1;
        while(start < end)
        {
            int measured = mPaint.breakText(text.substring(start, end), true, width, null);
            for (int i = measured; i >= start; i--)
            {
                char letter = text.charAt(i);
                if(letter == ' ')
                {
                    measured = i;
                    break;
                }
            }
            strings.add(text.substring(start, start + measured + 1));
            start += measured + 1;
        }
        return strings;
    }


    @Override
    public void recycle()
    {
        if (mEmptyLetter != null)
        {
            mEmptyLetter.recycle();
        }
        if (mQuestionNormal != null)
        {
            mQuestionNormal.recycle();
        }
    }
}
