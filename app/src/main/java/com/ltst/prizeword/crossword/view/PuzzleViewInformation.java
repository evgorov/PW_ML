package com.ltst.prizeword.crossword.view;

import android.graphics.Point;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.util.List;

import javax.annotation.Nullable;

import com.ltst.prizeword.crossword.model.PuzzleQuestion.ArrowType;

public class PuzzleViewInformation
{
    public static final int STATE_MASK = 0x00000011;
    public static final int STATE_QUESTION = 0x00000001;
    public static final int STATE_LETTER = 0x00000010;

    private static final int DEFAULT_CELL_WIDTH = 14;
    private static final int DEFAULT_CELL_HEIGHT = 20;
    private static final int DEFAULT_PADDING = 16;
    private static final int DEFAULT_TILE_GAP = 4;

    private int mPuzzleCellWidth;
    private int mPuzzleCellHeigth;
    private int mPadding = DEFAULT_PADDING;
    private int mTileGap = DEFAULT_TILE_GAP;
    private int mFramePadding = DEFAULT_PADDING/2;

    private @Nullable PuzzleSetModel.PuzzleSetType mSetType;
    private @Nullable List<PuzzleQuestion> mPuzzleQuestions;
    private @Nullable int[][] mStateMatrix;

    public PuzzleViewInformation(@Nullable PuzzleSetModel.PuzzleSetType setType,
                                 @Nullable List<PuzzleQuestion> puzzleQuestions)
    {
        this(DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT, setType, puzzleQuestions);
    }

    public PuzzleViewInformation(int puzzleCellWidth, int puzzleCellHeigth,
                                 @Nullable PuzzleSetModel.PuzzleSetType setType,
                                 @Nullable List<PuzzleQuestion> puzzleQuestions)
    {
        mPuzzleCellWidth = puzzleCellWidth;
        mPuzzleCellHeigth = puzzleCellHeigth;
        mSetType = setType;
        mPuzzleQuestions = puzzleQuestions;
        initStateMatrix();
    }

    private void initStateMatrix()
    {
        mStateMatrix = new int[mPuzzleCellWidth][mPuzzleCellHeigth];
        for (int i = 0; i < mPuzzleCellWidth; i++)
        {
            for (int j = 0; j < mPuzzleCellHeigth; j++)
            {
                mStateMatrix[i][j] = STATE_LETTER;
            }
        }

        for (PuzzleQuestion question : mPuzzleQuestions)
        {
            int col = question.column - 1;
            int row = question.row - 1;
            int arrowType = question.getAnswerPosition();
            mStateMatrix[col][row] = STATE_QUESTION;
            Point p = ArrowType.positionToPoint(arrowType, col, row);
            if (p != null)
            {
                mStateMatrix[p.x][p.y] &= arrowType;
            }
        }
    }

    public @Nullable int[][] getStateMatrix()
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
        return R.drawable.gamefield_tile_arrow_north_up;
    }

    public void setPadding(int padding)
    {
        mPadding = padding;
    }

    public void setTileGap(int tileGap)
    {
        mTileGap = tileGap;
    }

    public int getLetterEmpty()
    {
        return R.drawable.gamefield_tile_letter_empty;
    }

    public int getLetterEmptyInput()
    {
        return R.drawable.gamefield_tile_letter_empty_input;
    }

    public int getOverlayLetterCorrect()
    {
        return R.drawable.gamefield_tile_letter_correct_input_overlay;
    }

    public int getQuestionEmpty()
    {
        return R.drawable.gamefield_tile_question_new;
    }

    public int getQuestionInput()
    {
        return R.drawable.gamefield_tile_question_input;
    }

    public int getQuestionWrong()
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

    public int getBackgroundTile()
    {
        return R.drawable.bg_sand_tile2x;
    }

    public int getBackgroundFrame()
    {
        return R.drawable.gamefield_border;
    }

    public int getPuzzleCellWidth()
    {
        return mPuzzleCellWidth;
    }

    public int getPuzzleCellHeight()
    {
        return mPuzzleCellHeigth;
    }

    public int getPadding()
    {
        return mPadding;
    }

    public int getFramePadding()
    {
        return mFramePadding;
    }

    public int getTileGap()
    {
        return mTileGap;
    }

}
