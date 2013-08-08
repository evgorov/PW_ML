package com.ltst.prizeword.crossword.engine;

import android.graphics.Point;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnswerLetterPointIterator implements Iterator<Point>
{
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
            }g
            needToDecreaseLetterIndex = true;
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

    public void reset()
    {
        currentLetterIndex = 0;
        mPoint.set(mStartPoint.x, mStartPoint.y);
    }

    private void offsetPointByDirection(@Nonnull Point p)
    {
        switch (mDirection)
        {
            case PuzzleTileState.AnswerDirection.DOWN:
                p.offset(0, 1);
                break;
            case PuzzleTileState.AnswerDirection.UP:
                p.offset(0, -1);
                break;
            case PuzzleTileState.AnswerDirection.RIGHT:
                p.offset(1, 0);
                break;
            case PuzzleTileState.AnswerDirection.LEFT:
                p.offset(-1, 0);
                break;
        }
    }

    private void negateOffsetPointByDirection(@Nonnull Point p)
    {
        switch (mDirection)
        {
            case PuzzleTileState.AnswerDirection.DOWN:
                p.offset(0, -1);
                break;
            case PuzzleTileState.AnswerDirection.UP:
                p.offset(0, 1);
                break;
            case PuzzleTileState.AnswerDirection.RIGHT:
                p.offset(-1, 0);
                break;
            case PuzzleTileState.AnswerDirection.LEFT:
                p.offset(1, 0);
                break;
        }
    }

    @Override
    public void remove()
    {

    }
}
