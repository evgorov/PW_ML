package com.ltst.prizeword.crossword.engine;

import android.graphics.Point;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnswerLetterPointIterator implements Iterator<Point>
{
    public static final char SKIP_LETTER_CHARACTER = '_';
    public static final char NOT_SKIP_LETTER_CHARACTER = '+';
    private @Nonnull Point mStartPoint;
    private @Nonnull Point mPoint;
    private int mDirection;
    private @Nonnull String mAnswer;
    private int currentLetterIndex = 0;

    public AnswerLetterPointIterator(@Nonnull Point start,
                                     int direction,
                                     @Nonnull String answer)
    {
        mStartPoint = new Point(start.x, start.y);
        mPoint = new Point(start.x, start.y);
        mDirection = direction;
        mAnswer = answer;
    }

    @Override
    public boolean hasNext()
    {
        return currentLetterIndex >= 0 && currentLetterIndex < mAnswer.length();
    }

    @Override
    @Nullable
    public Point next()
    {
        if(hasNext())
        {
            if(currentLetterIndex == 0)
            {
                currentLetterIndex ++;
                return mPoint;
            }

            offsetPointByDirection(mPoint);
            currentLetterIndex ++;
            return mPoint;
        }
        else
            return null;
    }

    public @Nullable Point last()
    {
        boolean needToDecreaseLetterIndex = true;
        if(currentLetterIndex >= mAnswer.length())
        {
            currentLetterIndex = mAnswer.length() - 1;
            needToDecreaseLetterIndex = false;
        }
        if(hasNext())
        {
            if (needToDecreaseLetterIndex)
            {
                currentLetterIndex --;
            }
            if(currentLetterIndex <= 0)
            {
                currentLetterIndex = 0;
                return mPoint;
            }
            Point ret = new Point(mPoint);
            negateOffsetPointByDirection(mPoint);
            return ret;
        }
        else
            return null;
    }

    public @Nonnull Point current()
    {
        return mPoint;
    }

    public void reset()
    {
        currentLetterIndex = 0;
        mPoint.set(mStartPoint.x, mStartPoint.y);
    }

    public void offsetPointByDirection(@Nonnull Point p, int widthOffset, int heightOffset)
    {
        switch (mDirection)
        {
            case PuzzleTileState.AnswerDirection.DOWN:
                p.offset(0, heightOffset);
                break;
            case PuzzleTileState.AnswerDirection.UP:
                p.offset(0, -heightOffset);
                break;
            case PuzzleTileState.AnswerDirection.RIGHT:
                p.offset(widthOffset, 0);
                break;
            case PuzzleTileState.AnswerDirection.LEFT:
                p.offset(-widthOffset, 0);
                break;
        }
    }

    private void offsetPointByDirection(@Nonnull Point p)
    {
        offsetPointByDirection(p, 1, 1);
    }

    private void negateOffsetPointByDirection(@Nonnull Point p)
    {
        offsetPointByDirection(p, -1, -1);
    }

    public char getCurrentLetter()
    {
        int index = currentLetterIndex - 1;
        if(index < 0 || index >= mAnswer.length())
            return PuzzleResources.LETTER_UNKNOWN;
        return mAnswer.charAt(currentLetterIndex - 1);
    }

    @Override
    public void remove()
    {

    }
}
