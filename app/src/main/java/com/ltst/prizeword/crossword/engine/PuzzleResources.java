package com.ltst.prizeword.crossword.engine;

import android.content.res.Resources;

import android.graphics.Point;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ltst.prizeword.crossword.engine.PuzzleTileState.*;

import org.omich.velo.handlers.IListener;

public class PuzzleResources
{
    private static final int DEFAULT_CELL_WIDTH = 14;
    private static final int DEFAULT_CELL_HEIGHT = 20;
    private static final int DEFAULT_PADDING = 16;
    private static final int DEFAULT_TILE_GAP = 4;

    private int mPuzzleColumnsCount;
    private int mPuzzleRowsCount;
    private int mPadding = DEFAULT_PADDING;
    private int mTileGap = DEFAULT_TILE_GAP;
    private int mFramePadding = DEFAULT_PADDING;

    private @Nullable PuzzleSetModel.PuzzleSetType mSetType;
    private @Nullable List<PuzzleQuestion> mPuzzleQuestions;
    private @Nullable PuzzleTileState[][] mStateMatrix;

    public PuzzleResources(@Nullable PuzzleSetModel.PuzzleSetType setType,
                           @Nullable List<PuzzleQuestion> puzzleQuestions)
    {
        this(DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, setType, puzzleQuestions);
    }

    public PuzzleResources(int puzzleCellWidth, int puzzleCellHeigth,
                           @Nullable PuzzleSetModel.PuzzleSetType setType,
                           @Nullable List<PuzzleQuestion> puzzleQuestions)
    {
        mPuzzleColumnsCount = puzzleCellWidth;
        mPuzzleRowsCount = puzzleCellHeigth;
        mSetType = setType;
        mPuzzleQuestions = puzzleQuestions;
        initStateMatrix();
    }

    private void initStateMatrix()
    {
        mStateMatrix = new PuzzleTileState[mPuzzleColumnsCount][mPuzzleRowsCount];
        for (int i = 0; i < mPuzzleColumnsCount; i++)
        {
            for (int j = 0; j < mPuzzleRowsCount; j++)
            {
                mStateMatrix[i][j] = new PuzzleTileState();
                mStateMatrix[i][j].setLetterState(LetterState.LETTER_EMPTY);
            }
        }

        for (PuzzleQuestion question : mPuzzleQuestions)
        {
            int col = question.column - 1;
            int row = question.row - 1;
            int arrowType = question.getAnswerPosition();
            mStateMatrix[col][row].setQuestionState(QuestionState.QUESTION_EMPTY);
            Point p = ArrowType.positionToPoint(arrowType, col, row);
            if (p != null)
            {
                mStateMatrix[p.x][p.y].addArrow(arrowType);
            }
        }
    }

    public @Nullable PuzzleTileState getPuzzleState(int column, int row)
    {
        if(column < 0 || column >= mPuzzleColumnsCount)
            return null;
        if(row < 0 || row >= mPuzzleRowsCount)
            return null;
        return mStateMatrix[column][row];
    }

    public @Nullable PuzzleTileState[][] getStateMatrix()
    {
        if (mStateMatrix != null)
        {
            return mStateMatrix;
        }
        return null;
    }

    @Nullable
    public List<PuzzleQuestion> getPuzzleQuestions()
    {
        return mPuzzleQuestions;
    }

    public static int getArrowResource(int type)
    {
        boolean isDone = (type & ArrowType.ARROW_DONE) == ArrowType.ARROW_DONE;
        type &= ArrowType.ARROW_TYPE_MASK;
        switch (type)
        {
            case ArrowType.NORTH_LEFT:
                if (!isDone)
                    return R.drawable.gamefield_tile_arrow_north_left;
                else
                    return R.drawable.gamefield_tile_arrow_north_left_done;
            case ArrowType.NORTH_RIGHT:
                if (!isDone)
                    return R.drawable.gamefield_tile_arrow_north_right;
                else
                    return R.drawable.gamefield_tile_arrow_north_right_done;
            case ArrowType.NORTH_TOP:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_north_up;
                else
                    return R.drawable.gamefield_tile_arrow_north_up_done;
            case ArrowType.EAST_BOTTOM:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_east_down;
                else
                    return R.drawable.gamefield_tile_arrow_east_down_done;
            case ArrowType.EAST_TOP:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_east_up;
                else
                    return R.drawable.gamefield_tile_arrow_east_up_done;
            case ArrowType.EAST_RIGHT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_east_right;
                else
                    return R.drawable.gamefield_tile_arrow_east_right_done;
            case ArrowType.SOUTH_BOTTOM:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_south_down;
                else
                    return R.drawable.gamefield_tile_arrow_south_down_done;
            case ArrowType.SOUTH_LEFT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_south_left;
                else
                    return R.drawable.gamefield_tile_arrow_south_left_done;
            case ArrowType.SOUTH_RIGHT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_south_right;
                else
                    return R.drawable.gamefield_tile_arrow_south_right_done;
            case ArrowType.WEST_BOTTOM:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_west_down;
                else
                    return R.drawable.gamefield_tile_arrow_west_down_done;
            case ArrowType.WEST_LEFT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_west_left;
                else
                    return R.drawable.gamefield_tile_arrow_west_left_done;
            case ArrowType.WEST_TOP:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_west_up;
                else
                    return R.drawable.gamefield_tile_arrow_west_up_done;
            case ArrowType.NORTH_EAST_BOTTOM:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_northeast_down;
                else
                    return R.drawable.gamefield_tile_arrow_northeast_down_done;
            case ArrowType.NORTH_EAST_LEFT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_northeast_left;
                else
                    return R.drawable.gamefield_tile_arrow_northeast_left_done;
            case ArrowType.NORTH_EAST_RIGHT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_northeast_right;
                else
                    return R.drawable.gamefield_tile_arrow_northeast_right_done;
            case ArrowType.NORTH_EAST_TOP:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_northeast_up;
                else
                    return R.drawable.gamefield_tile_arrow_northeast_up_done;
            case ArrowType.NORTH_WEST_BOTTOM:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_northwest_down;
                else
                    return R.drawable.gamefield_tile_arrow_northwest_down_done;
            case ArrowType.NORTH_WEST_LEFT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_northwest_left;
                else
                    return R.drawable.gamefield_tile_arrow_northwest_left_done;
            case ArrowType.NORTH_WEST_RIGHT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_northwest_right;
                else
                    return R.drawable.gamefield_tile_arrow_northwest_right_done;
            case ArrowType.NORTH_WEST_TOP:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_northwest_up;
                else
                    return R.drawable.gamefield_tile_arrow_northwest_up_done;
            case ArrowType.SOUTH_EAST_TOP:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_southeast_up;
                else
                    return R.drawable.gamefield_tile_arrow_southeast_up_done;
            case ArrowType.SOUTH_EAST_BOTTOM:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_southeast_down;
                else
                    return R.drawable.gamefield_tile_arrow_southeast_down_done;
            case ArrowType.SOUTH_EAST_LEFT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_southeast_left;
                else
                    return R.drawable.gamefield_tile_arrow_southeast_left_done;
            case ArrowType.SOUTH_EAST_RIGHT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_southeast_right;
                else
                    return R.drawable.gamefield_tile_arrow_southeast_right_done;
            case ArrowType.SOUTH_WEST_TOP:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_southwest_up;
                else
                    return R.drawable.gamefield_tile_arrow_southwest_up_done;
            case ArrowType.SOUTH_WEST_BOTTOM:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_southwest_down;
                else
                    return R.drawable.gamefield_tile_arrow_southwest_down_done;
            case ArrowType.SOUTH_WEST_LEFT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_southwest_left;
                else
                    return R.drawable.gamefield_tile_arrow_southwest_left_done;
            case ArrowType.SOUTH_WEST_RIGHT:
                if(!isDone)
                    return R.drawable.gamefield_tile_arrow_southwest_right;
                else
                    return R.drawable.gamefield_tile_arrow_southwest_right_done;
        }
        return 0;
    }

    public void setPadding(int padding)
    {
        mPadding = padding;
    }

    public void setTileGap(int tileGap)
    {
        mTileGap = tileGap;
    }

    public static int getOverlayLetterCorrect()
    {
        return R.drawable.gamefield_tile_letter_correct_input_overlay;
    }

    public static int getQuestionEmpty()
    {
        return R.drawable.gamefield_tile_question_new;
    }

    public static int getQuestionInput()
    {
        return R.drawable.gamefield_tile_question_input;
    }

    public static int getQuestionWrong()
    {
        return R.drawable.gamefield_tile_question_wrong;
    }

    public int getQuestionCorrect()
    {
        if (mSetType == PuzzleSetModel.PuzzleSetType.FREE)
            return R.drawable.gamefield_tile_question_correct_free;
        if (mSetType == PuzzleSetModel.PuzzleSetType.SILVER)
            return R.drawable.gamefield_tile_question_correct_silver;
        if (mSetType == PuzzleSetModel.PuzzleSetType.GOLD)
            return R.drawable.gamefield_tile_question_correct_gold;
        if (mSetType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
            return R.drawable.gamefield_tile_question_correct_brilliant;
        return 0;
    }

    public static int getLetterEmpty()
    {
        return R.drawable.gamefield_tile_letter_empty;
    }

    public static int getLetterEmptyInput()
    {
        return R.drawable.gamefield_tile_letter_empty_input;
    }

    public int getLetterTilesCorrect()
    {
        if (mSetType == PuzzleSetModel.PuzzleSetType.FREE)
            return R.drawable.gamefield_tile_letters_correct_free;
        if (mSetType == PuzzleSetModel.PuzzleSetType.SILVER)
            return R.drawable.gamefield_tile_letters_correct_silver;
        if (mSetType == PuzzleSetModel.PuzzleSetType.GOLD)
            return R.drawable.gamefield_tile_letters_correct_gold;
        if (mSetType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
            return R.drawable.gamefield_tile_letters_correct_brilliant;
        return 0;
    }

    public static int getLetterTilesInput()
    {
        return R.drawable.gamefield_tile_letters_input;
    }

    public static int getLetterTilesWrong()
    {
        return R.drawable.gamefield_tile_letters_wrong;
    }

    public static int getBackgroundTile()
    {
        return R.drawable.bg_sand_tile2x;
    }

    public static int getBackgroundFrame()
    {
        return R.drawable.gamefield_border;
    }

    public int getPuzzleColumnsCount()
    {
        return mPuzzleColumnsCount;
    }

    public int getPuzzleRowsCount()
    {
        return mPuzzleRowsCount;
    }

    public int getPadding()
    {
        return mPadding;
    }

    public int getFramePadding(@Nonnull Resources res)
    {
        mFramePadding = res.getDimensionPixelSize(R.dimen.frame_border_offset);
        return mFramePadding;
    }

    public int getTileGap()
    {
        return mTileGap;
    }

    public static int getCanvasBackgroundTileRes()
    {
        return R.drawable.bg_dark_tile;
    }
}
