package com.ltst.prizeword.crossword.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.SparseArray;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.engine.PuzzleResources;
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
    private @Nullable int[][] mStateMatrix;
    private @Nullable List<PuzzleQuestion> mQuestions;
    private int mScreenRatio;
    private @Nonnull SparseArray<Bitmap> mArrows;



    public PuzzleTilesLayer(@Nonnull Resources res, @Nonnull Rect drawingRect,
                            int puzzleWidth, int puzzleHeight, int screenRatio)
    {
        mPuzzleWidth = puzzleWidth;
        mPuzzleHeight = puzzleHeight;
        mResources = res;
        mScreenRatio = screenRatio;
        mDrawingRect = drawingRect;
        mArrows = new SparseArray<Bitmap>();

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

    public void setStateMatrix(@Nullable int[][] stateMatrix)
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

        Rect frameRect = new Rect(horOffset + mDrawingRect.left,
                verOffset + mDrawingRect.top,
                mDrawingRect.right - horOffset,
                mDrawingRect.bottom - verOffset);

        float difFactor = 10f;
        int preferredTileWidth = (frameRect.width() - mTileGap * (mPuzzleWidth - 1) - mPadding * 4)/mPuzzleWidth;
        float difX = Math.abs(tileWidth - preferredTileWidth);
        if(difX > difFactor || difX < difFactor)
        {
            tileWidth = preferredTileWidth;
        }

        int preferredTileHeight = (frameRect.height() - mTileGap * (mPuzzleHeight -1) - mPadding * 4)/mPuzzleHeight;
        float difY = Math.abs(tileHeight - preferredTileHeight);
        if(difY > difFactor || difY < difFactor)
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
                int initState = mStateMatrix[j][i];
                int state = mStateMatrix[j][i] & PuzzleResources.STATE_MASK;
                if (state == PuzzleResources.STATE_LETTER)
                {
                    canvas.drawBitmap(mEmptyLetter, null, rect, mPaint);
                    int arrow = mStateMatrix[j][i] & PuzzleQuestion.ArrowType.ARROW_TYPE_MASK;
                    if(arrow != PuzzleQuestion.ArrowType.NO_ARROW)
                    {
                        int res = PuzzleResources.getArrowResource(arrow);
                        Bitmap arrowBitmap = mArrows.get(arrow);
                        if(arrowBitmap == null)
                        {
                            arrowBitmap = BitmapFactory.decodeResource(mResources, res);
                            mArrows.append(arrow, arrowBitmap);
                        }
                        canvas.drawBitmap(arrowBitmap, null, rect, mPaint);
                    }
                }

                if (state == PuzzleResources.STATE_QUESTION)
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
                PuzzleResources.getLetterEmpty(), mScreenRatio);
        mQuestionNormal = BitmapHelper.loadBitmapInSampleSize(mResources,
                PuzzleResources.getQuestionEmpty(), mScreenRatio);
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
        int key = 0;
        for(int i = 0; i < mArrows.size(); i++)
        {
            key = mArrows.keyAt(i);
            Bitmap bm = mArrows.get(key);
            bm.recycle();
        }
    }
}
