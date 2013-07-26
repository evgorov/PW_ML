package com.ltst.prizeword.crossword.view;

import android.graphics.Matrix;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class PuzzleViewInformation
{
    public static final byte STATE_QUESTION = 1;
    public static final byte STATE_LETTER = 2;

    private static final int DEFAULT_CELL_WIDTH = 14;
    private static final int DEFAULT_CELL_HEIGHT = 20;
    private static final int DEFAULT_PADDING = 16;
    private static final int DEFAULT_TILE_GAP = 4;

    private int mPuzzleCellWidth;
    private int mPuzzleCellHeigth;
    private int mPadding = DEFAULT_PADDING;
    private int mTileGap = DEFAULT_TILE_GAP;

    private @Nullable PuzzleSetModel.PuzzleSetType mSetType;
    private @Nullable List<PuzzleQuestion> mPuzzleQuestions;
    private @Nullable byte[][] mStateMatrix;

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
        mStateMatrix = new byte[mPuzzleCellWidth][mPuzzleCellHeigth];
        for (int i = 0; i < mPuzzleCellWidth; i++)
        {
            for (int j = 0; j < mPuzzleCellHeigth; j++)
            {
                mStateMatrix[i][j] = STATE_LETTER;
            }
        }

        for (PuzzleQuestion question : mPuzzleQuestions)
        {
            mStateMatrix[question.column - 1][question.row - 1] = STATE_QUESTION;
        }
    }

    public @Nullable byte[][] getStateMatrix()
    {
        if (mStateMatrix != null)
        {
            return mStateMatrix;
        }
        return null;
    }

    // в порядке обхода клеток
    public @Nullable List<String> getQuestionsList()
    {
        List<String> list = null;
        if (mPuzzleQuestions != null)
        {
            list = new ArrayList<String>();
            for (PuzzleQuestion question : mPuzzleQuestions)
            {
                list.add(question.quesitonText);
            }
        }
        return list;
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

    public int getTileGap()
    {
        return mTileGap;
    }
}