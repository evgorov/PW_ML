package com.ltst.przwrd.crossword.engine;

import android.graphics.Point;
import android.util.SparseIntArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleTileState
{
    public boolean hasQuestion;
    public boolean hasLetter;
    public boolean hasArrows;
    public boolean hasInputLetter;
    public int column;
    public int row;

    private int questionState;
    private int questionIndex;
    private int letterState;
    private SparseIntArray arrowsState;
    private boolean hasFirstArrow;
    private String mInputLetter;

    public PuzzleTileState()
    {
        hasQuestion = false;
        hasLetter = false;
        hasArrows = false;
        questionState = 0;
        letterState = 0;
        arrowsState = new SparseIntArray();
    }

    public void setQuestionState(int questionState)
    {
        this.questionState = questionState;
        hasQuestion = true;
        hasLetter = false;
        hasInputLetter = false;
    }

    public int getQuestionIndex()
    {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex)
    {
        this.questionIndex = questionIndex;
    }

    public void setLetterState(int letterState)
    {
        this.letterState = letterState;
        hasLetter = true;
        hasInputLetter = false;
        hasQuestion = false;
    }

    public String getInputLetter()
    {
        return mInputLetter;
    }

    public void setInputLetter(String inputLetter)
    {
        mInputLetter = inputLetter;
        setLetterState(LetterState.LETTER_INPUT);
        hasInputLetter = true;
    }

    public void setLetterCorrect(boolean correct)
    {
        letterState = correct ? LetterState.LETTER_CORRECT : LetterState.LETTER_WRONG;
    }

    public int getQuestionState()
    {
        return questionState;
    }

    public int getLetterState()
    {
        return letterState;
    }

    public void addArrow(int arrow, int questionIndex)
    {
        if(!hasFirstArrow)
        {
            arrowsState.append(questionIndex, arrow);
            hasFirstArrow = true;
        }
        else
            arrowsState.append(questionIndex, arrow);
        hasArrows = true;
    }


    public int getFirstArrow()
    {
        if(hasInputLetter)
            return arrowsState.valueAt(0) | ArrowType.ARROW_DONE;
        else
            return arrowsState.valueAt(0);
    }

    public int getSecondArrow()
    {
        if(hasInputLetter)
            return arrowsState.valueAt(1) | ArrowType.ARROW_DONE;
        else
            return arrowsState.valueAt(1);
    }

    public int getArrowByQuestionIndex(int questionIndex)
    {
        if(arrowsState.indexOfKey(questionIndex) >= 0)
        {
            if(hasInputLetter)
                return arrowsState.get(questionIndex) | ArrowType.ARROW_DONE;
            else
                return arrowsState.get(questionIndex);
        }
        else return ArrowType.NO_ARROW;
    }

    public void removeArrowByQuestionIndex(int questionIndex)
    {
        int index = arrowsState.indexOfKey(questionIndex);
        if(index >= 0)
            arrowsState.removeAt(index);
        else
            return;
        if(arrowsState.size() == 0)
            hasArrows = false;
    }

    public void removeArrows()
    {
        hasArrows = false;
        arrowsState.clear();
    }

    public static class QuestionState
    {
        public static final int QUESTION_EMPTY = 0x00000001;
        public static final int QUESTION_CORRECT = 0x00000010;
        public static final int QUESTION_WRONG = 0x000000011;
        public static final int QUESTION_INPUT = 0x000000100;
    }

    public static class LetterState
    {
        public static final int LETTER_EMPTY        = 0x00000001;
        public static final int LETTER_EMPTY_INPUT  = 0x00000010;
        public static final int LETTER_INPUT        = 0x00000011;
        public static final int LETTER_CORRECT      = 0x00000101;
        public static final int LETTER_WRONG        = 0x00000111;
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

        @Nonnull static int[] getArrowTypesArray()
        {
            int[] res = new int[28];
            res[0] = NORTH_LEFT;
            res[1] = NORTH_TOP;
            res[2] = NORTH_RIGHT;
            res[3] = NORTH_EAST_RIGHT;
            res[4] = NORTH_EAST_LEFT;
            res[5] = NORTH_EAST_BOTTOM;
            res[6] = NORTH_EAST_TOP;
            res[7] = EAST_BOTTOM;
            res[8] = EAST_RIGHT;
            res[9] = EAST_TOP;
            res[10] = SOUTH_EAST_BOTTOM;
            res[11] = SOUTH_EAST_LEFT;
            res[12] = SOUTH_EAST_RIGHT;
            res[13] = SOUTH_EAST_TOP;
            res[14] = SOUTH_BOTTOM;
            res[15] = SOUTH_RIGHT;
            res[16] = SOUTH_LEFT;
            res[17] = SOUTH_WEST_BOTTOM;
            res[18] = SOUTH_WEST_LEFT;
            res[19] = SOUTH_WEST_RIGHT;
            res[20] = SOUTH_WEST_TOP;
            res[21] = WEST_BOTTOM;
            res[22] = WEST_TOP;
            res[23] = WEST_LEFT;
            res[24] = NORTH_WEST_BOTTOM;
            res[25] = NORTH_WEST_RIGHT;
            res[26] = NORTH_WEST_LEFT;
            res[27] = NORTH_WEST_TOP;
            return res;
        }
    }

    public static class AnswerDirection
    {
        public static final int UP = 1;
        public static final int DOWN = 2;
        public static final int RIGHT = 3;
        public static final int LEFT = 4;

        public static int getDirectionByArrow(int arrowType)
        {
            switch (arrowType & ArrowType.ARROW_TYPE_MASK)
            {
                case ArrowType.NORTH_WEST_TOP:
                case ArrowType.NORTH_EAST_TOP:
                case ArrowType.NORTH_TOP:
                case ArrowType.EAST_TOP:
                case ArrowType.WEST_TOP:
                case ArrowType.SOUTH_EAST_TOP:
                case ArrowType.SOUTH_WEST_TOP:
                    return UP;
                case ArrowType.EAST_BOTTOM:
                case ArrowType.WEST_BOTTOM:
                case ArrowType.NORTH_WEST_BOTTOM:
                case ArrowType.NORTH_EAST_BOTTOM:
                case ArrowType.SOUTH_EAST_BOTTOM:
                case ArrowType.SOUTH_WEST_BOTTOM:
                case ArrowType.SOUTH_BOTTOM:
                    return DOWN;
                case ArrowType.EAST_RIGHT:
                case ArrowType.NORTH_EAST_RIGHT:
                case ArrowType.SOUTH_EAST_RIGHT:
                case ArrowType.NORTH_RIGHT:
                case ArrowType.SOUTH_RIGHT:
                case ArrowType.SOUTH_WEST_RIGHT:
                case ArrowType.NORTH_WEST_RIGHT:
                    return RIGHT;
                case ArrowType.WEST_LEFT:
                case ArrowType.NORTH_WEST_LEFT:
                case ArrowType.SOUTH_WEST_LEFT:
                case ArrowType.NORTH_EAST_LEFT:
                case ArrowType.SOUTH_EAST_LEFT:
                case ArrowType.NORTH_LEFT:
                case ArrowType.SOUTH_LEFT:
                    return LEFT;
            }
            return 0;
        }
    }

}
