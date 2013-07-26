package com.ltst.prizeword.crossword.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.SparseArray;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;

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

    private @Nonnull Resources mResources;
    private @Nonnull Bitmap mEmptyLetter;
    private @Nonnull Bitmap mQuestionNormal;
    private @Nonnull Paint mPaint;
    private int mPadding = 0;
    private int mTileGap = 0;
    private @Nullable int[][] mStateMatrix;
    private @Nullable List<PuzzleQuestion> mQuestions;
    private @Nonnull SparseArray<Bitmap> mArrows;

    public PuzzleTilesLayer(@Nonnull Resources res, int puzzleWidth, int puzzleHeight)
    {
        mPuzzleWidth = puzzleWidth;
        mPuzzleHeight = puzzleHeight;
        mResources = res;
        mArrows = new SparseArray<Bitmap>();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mFontSize = res.getDimensionPixelSize(R.dimen.puzzle_question_font_size);
        mPaint.setTextSize(mFontSize);
        mPaint.setStyle(Paint.Style.FILL);
        Typeface tf = Typeface.create("Helvetica",Typeface.BOLD);
        mPaint.setTypeface(tf);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        mTextHeight = (int) mPaint.getTextSize();

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
        mTileTextPadding = mEmptyLetter.getWidth()/6;
        mQuestionNormal = BitmapFactory.decodeResource(mResources, emptyQuestionResId);
    }

    public void setStateMatrix(@Nullable int[][] stateMatrix)
    {
        mStateMatrix = stateMatrix;
    }

    public void setQuestions(@Nullable List<PuzzleQuestion> questions)
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
                int initState = mStateMatrix[j][i];
                int state = mStateMatrix[j][i] & PuzzleViewInformation.STATE_MASK;
                if (state == PuzzleViewInformation.STATE_LETTER)
                {
                    canvas.drawBitmap(mEmptyLetter, null, rect, mPaint);
                    int arrow = mStateMatrix[j][i] & PuzzleQuestion.ArrowType.ARROW_TYPE_MASK;
                    if(arrow != PuzzleQuestion.ArrowType.NO_ARROW)
                    {
                        int res = PuzzleViewInformation.getArrowResource(arrow);
                        Bitmap arrowBitmap = mArrows.get(arrow);
                        if(arrowBitmap == null)
                        {
                            arrowBitmap = BitmapFactory.decodeResource(mResources, res);
                            mArrows.append(arrow, arrowBitmap);
                        }
                        canvas.drawBitmap(arrowBitmap, null, rect, mPaint);
                    }
                }

                if (state == PuzzleViewInformation.STATE_QUESTION)
                {
                    canvas.drawBitmap(mQuestionNormal, null, rect, mPaint);
                    drawQuestionText(canvas, questionsIndex, rect);
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

    private void drawQuestionText(@Nonnull Canvas canvas, int questionsIndex, @Nonnull RectF tileRect)
    {
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
        mEmptyLetter.recycle();
        mQuestionNormal.recycle();
        int key = 0;
        for(int i = 0; i < mArrows.size(); i++)
        {
            key = mArrows.keyAt(i);
            Bitmap bm = mArrows.get(key);
            bm.recycle();
        }
    }
}
