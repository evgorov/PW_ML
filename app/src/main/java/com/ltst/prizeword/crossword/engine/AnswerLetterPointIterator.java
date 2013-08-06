package com.ltst.prizeword.crossword.engine;

import android.graphics.Point;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnswerLetterPointIterator implements Iterator<Point>
{
    private @Nonnull Point mPoint;
    private int mDirection;
    private @Nonnull String mAnswer;
    private int currentLetterIndex = 0;

    public AnswerLetterPointIterator(@Nonnull Point start,
                                     int direction,
                                     @Nonnull String answer)
    {
        mPoint = start;
        mDirection = direction;
        mAnswer = answer;
    }

    @Override
    public boolean hasNext()
    {
        return currentLetterIndex < mAnswer.length();
    }

    @Override
    @Nullable
    public Point next()
    {
        if(currentLetterIndex < mAnswer.length())
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

    @Override
    public void remove()
    {

    }
}
