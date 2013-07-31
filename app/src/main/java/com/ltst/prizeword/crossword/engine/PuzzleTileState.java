package com.ltst.prizeword.crossword.engine;

import android.graphics.Point;

import javax.annotation.Nullable;

public class PuzzleTileState
{
    public boolean hasQuestion;
    public boolean hasLetter;
    public boolean hasArrows;

    private int questionState;
    private int letterState;
    private int[] arrowsState;
    private boolean hasFirstArrow;
    private boolean hasInputLetter;

    public PuzzleTileState()
    {
        hasQuestion = false;
        hasLetter = false;
        hasArrows = false;
        questionState = 0;
        letterState = 0;
        arrowsState = new int[2];
    }

    public void setQuestionState(int questionState)
    {
        this.questionState = questionState;
        hasQuestion = true;
        hasLetter = false;
        hasInputLetter = false;
    }

    public void setLetterState(int letterState)
    {
        this.letterState = letterState;
        hasLetter = true;
        hasQuestion = false;
    }

    public int getQuestionState()
    {
        return questionState;
    }

    public int getLetterState()
    {
        return letterState;
    }

    public void addArrow(int arrow)
    {
        if(!hasFirstArrow)
        {
            arrowsState[0] = arrow;
            hasFirstArrow = true;
        }
        else
            arrowsState[1] = arrow;
        hasArrows = true;
    }


    public int getFirstArrow()
    {
        if(hasInputLetter)
            return arrowsState[0] | ArrowType.ARROW_DONE;
        else
            return arrowsState[0];
    }

    public int getSecondArrow()
    {
        if(hasInputLetter)
            return arrowsState[1] | ArrowType.ARROW_DONE;
        else
            return arrowsState[1];
    }

    public static class QuestionState
    {
        public static final int QUESTION_EMPTY = 0x00000001;
        public static final int QUESTION_CORRECT = 0x00000010;
        public static final int QUESTION_WRONG = 0x000000011;
    }

    public static class LetterState
    {
        public static final int LETTER_EMPTY        = 0x00000001;
        public static final int LETTER_EMPTY_INPUT  = 0x00000010;
    }

    //    north:left, north:top, north:right,
//    north-east:left, north-east:bottom,
//    east:top, east:right, east:bottom,
//    south-east:left, south-east:top,
//    south:left, south:right, south:bottom,
//    south-west:top, south-west:right,
//    west:left, west:top, west:bottom,
//    north-west:right, north-west:bottom
    public static class ArrowType
    {
        public static final int ARROW_TYPE_MASK     = 0x01111100;
        public static final int NO_ARROW            = 0x00000000;
        public static final int ARROW_DONE          = 0x10000000;

        public static final int NORTH_LEFT          = 0x00000100;
        public static final int NORTH_TOP           = 0x00001000;
        public static final int NORTH_RIGHT         = 0x00001100;
        public static final int NORTH_EAST_LEFT     = 0x00010100;
        public static final int NORTH_EAST_BOTTOM   = 0x00011000;
        public static final int EAST_TOP            = 0x00011100;
        public static final int EAST_RIGHT          = 0x00100000;
        public static final int EAST_BOTTOM         = 0x00100100;
        public static final int SOUTH_EAST_LEFT     = 0x00101000;
        public static final int SOUTH_EAST_TOP      = 0x00101100;
        public static final int SOUTH_LEFT          = 0x00110000;
        public static final int SOUTH_RIGHT         = 0x00110100;
        public static final int SOUTH_BOTTOM        = 0x00111000;
        public static final int SOUTH_WEST_TOP      = 0x00111100;
        public static final int SOUTH_WEST_RIGHT    = 0x01000000;
        public static final int WEST_LEFT           = 0x01000100;
        public static final int WEST_TOP            = 0x01001000;
        public static final int WEST_BOTTOM         = 0x01001100;
        public static final int NORTH_WEST_RIGHT    = 0x01010000;
        public static final int NORTH_WEST_BOTTOM   = 0x01010100;
        public static final int NORTH_EAST_TOP      = 0x01011000;
        public static final int NORTH_WEST_TOP      = 0x01011100;
        public static final int SOUTH_EAST_BOTTOM   = 0x01100000;
        public static final int SOUTH_WEST_BOTTOM   = 0x01100100;
        public static final int NORTH_EAST_RIGHT    = 0x01101000;
        public static final int NORTH_WEST_LEFT     = 0x01101100;
        public static final int SOUTH_EAST_RIGHT    = 0x01110000;
        public static final int SOUTH_WEST_LEFT     = 0x01110100;

        public static @Nullable
        Point positionToPoint(int type, int col, int row)
        {
            Point p = null;
            switch (type)
            {
                case NORTH_RIGHT:
                case NORTH_TOP:
                case NORTH_LEFT:
                    p = new Point(col, row - 1);
                    break;
                case NORTH_EAST_BOTTOM:
                case NORTH_EAST_LEFT:
                case NORTH_EAST_TOP:
                case NORTH_EAST_RIGHT:
                    p = new Point(col + 1, row - 1);
                    break;
                case EAST_TOP:
                case EAST_RIGHT:
                case EAST_BOTTOM:
                    p = new Point(col + 1, row);
                    break;
                case SOUTH_EAST_LEFT:
                case SOUTH_EAST_TOP:
                case SOUTH_EAST_RIGHT:
                case SOUTH_EAST_BOTTOM:
                    p = new Point(col + 1, row + 1);
                    break;
                case SOUTH_LEFT:
                case SOUTH_RIGHT:
                case SOUTH_BOTTOM:
                    p = new Point(col, row + 1);
                    break;
                case SOUTH_WEST_TOP:
                case SOUTH_WEST_RIGHT:
                case SOUTH_WEST_LEFT:
                case SOUTH_WEST_BOTTOM:
                    p = new Point(col - 1, row + 1);
                    break;
                case WEST_BOTTOM:
                case WEST_LEFT:
                case WEST_TOP:
                    p = new Point(col - 1, row);
                    break;
                case NORTH_WEST_BOTTOM:
                case NORTH_WEST_RIGHT:
                case NORTH_WEST_TOP:
                case NORTH_WEST_LEFT:
                    p = new Point(col - 1, row - 1);
                    break;
            }
            return p;
        }
    }

}
