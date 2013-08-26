package com.ltst.prizeword.crossword.engine;

import android.content.res.Resources;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ltst.prizeword.crossword.engine.PuzzleTileState.*;
import org.omich.velo.handlers.IListener;
import com.ltst.prizeword.crossword.wordcheck.WordCompletenessChecker.*;
import com.ltst.prizeword.crossword.wordcheck.WordsGraph;
import com.ltst.prizeword.tools.ParcelableTools;

public class PuzzleResources implements Parcelable
{
    private static final int DEFAULT_CELL_WIDTH = 14;
    private static final int DEFAULT_CELL_HEIGHT = 20;
    private static final int DEFAULT_PADDING = 16;
    private static final int DEFAULT_TILE_GAP = 4;
    public static final char LETTER_UNKNOWN = '-';

    private int mPuzzleColumnsCount;
    private int mPuzzleRowsCount;
    private int mPadding = DEFAULT_PADDING;
    private int mTileGap = DEFAULT_TILE_GAP;
    private int mFramePadding = DEFAULT_PADDING;

    private @Nullable PuzzleSetModel.PuzzleSetType mSetType;
    private @Nullable List<PuzzleQuestion> mPuzzleQuestions;
    private @Nullable PuzzleTileState[][] mStateMatrix;
    private @Nullable WordsGraph mWordsGraph;


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

        if (mPuzzleQuestions == null)
        {
            return;
        }

        mStateMatrix = new PuzzleTileState[mPuzzleColumnsCount][mPuzzleRowsCount];

        for (int i = 0; i < mPuzzleColumnsCount; i++)
        {
            for (int j = 0; j < mPuzzleRowsCount; j++)
            {
                mStateMatrix[i][j] = new PuzzleTileState();
                mStateMatrix[i][j].column = i + 1;
                mStateMatrix[i][j].row = j + 1;
                mStateMatrix[i][j].setLetterState(LetterState.LETTER_EMPTY);
            }
        }

        final HashMap<LetterCell, CrossingQuestionsPair> mCrossingQuestions
                = new HashMap<LetterCell, CrossingQuestionsPair>
                (mPuzzleColumnsCount * mPuzzleRowsCount);


        for (PuzzleQuestion question : mPuzzleQuestions)
        {
            final int index = mPuzzleQuestions.indexOf(question);

            final IListener<AnswerLetterPointIterator> crossingQuestionsFiller = new IListener<AnswerLetterPointIterator>()
            {
                @Override
                public void handle(@Nullable AnswerLetterPointIterator iterator)
                {
                    if (iterator == null)
                        return;

                    Point current = iterator.current();
                    LetterCell letterCell = new LetterCell(current.x, current.y);
                    if (mCrossingQuestions.containsKey(letterCell))
                    {
                        mCrossingQuestions.get(letterCell).putIndex(index);
                    }
                    else
                    {
                        CrossingQuestionsPair pair = new CrossingQuestionsPair();
                        pair.putIndex(index);
                        mCrossingQuestions.put(letterCell, pair);
                    }
                }
            };

            int col = question.column - 1;
            int row = question.row - 1;
            int arrowType = question.getAnswerPosition();
            Point p = ArrowType.positionToPoint(arrowType, col, row);
            if(question.isAnswered)
            {
                mStateMatrix[col][row].setQuestionState(QuestionState.QUESTION_CORRECT);
                if (p != null)
                {
                    final AnswerLetterPointIterator letterIterator = new AnswerLetterPointIterator(p,
                            PuzzleTileState.AnswerDirection.getDirectionByArrow(arrowType), question.answer);
                    PuzzleResourcesAdapter.setLetterStateByPointIterator(this, letterIterator, new IListener<PuzzleTileState>()
                    {
                        @Override
                        public void handle(@Nullable PuzzleTileState puzzleTileState)
                        {
                            if(puzzleTileState == null)
                            {
                                return;
                            }
                            String letter = String.valueOf(letterIterator.getCurrentLetter()).toUpperCase();
                            puzzleTileState.setInputLetter(letter);
                            puzzleTileState.setLetterCorrect(true);

                            crossingQuestionsFiller.handle(letterIterator);
                        }
                    });
                }
            }
            else
            {
                mStateMatrix[col][row].setQuestionState(QuestionState.QUESTION_EMPTY);
                if (p != null)
                {
                    mStateMatrix[p.x][p.y].addArrow(arrowType, index);
                    final AnswerLetterPointIterator letterIterator = new AnswerLetterPointIterator(p,
                            PuzzleTileState.AnswerDirection.getDirectionByArrow(arrowType), question.answer);
                    PuzzleResourcesAdapter.setLetterStateByPointIterator(this, letterIterator, new IListener<PuzzleTileState>()
                    {
                        @Override
                        public void handle(@Nullable PuzzleTileState puzzleTileState)
                        {
                            if(puzzleTileState == null)
                            {
                                return;
                            }

                            crossingQuestionsFiller.handle(letterIterator);
                        }
                    });

                }
            }
            mStateMatrix[col][row].setQuestionIndex(index);
        }
        mWordsGraph = new WordsGraph(mCrossingQuestions);
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

    @Nullable
    public WordsGraph getWordsGraph()
    {
        return mWordsGraph;
    }

    public void setQuestionCorrect(int index, boolean correct)
    {
        if (mPuzzleQuestions == null || mStateMatrix == null)
            return;
        if(index < 0 || index >= mPuzzleQuestions.size())
            return;
        PuzzleQuestion q = mPuzzleQuestions.get(index);
        q.isAnswered = correct;
        int column = q.column - 1;
        int row = q.row - 1;
        mStateMatrix[column][row].setQuestionState(QuestionState.QUESTION_CORRECT);
        Point answerStart = PuzzleTileState.ArrowType.positionToPoint(q.getAnswerPosition(), column, row);
        if (answerStart != null)
        {
            mStateMatrix[answerStart.x][answerStart.y].removeArrowByQuestionIndex(index);
        }
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
        if (mSetType == PuzzleSetModel.PuzzleSetType.SILVER || mSetType == PuzzleSetModel.PuzzleSetType.SILVER2)
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
        if (mSetType == PuzzleSetModel.PuzzleSetType.SILVER || mSetType == PuzzleSetModel.PuzzleSetType.SILVER2)
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
        return R.drawable.bg_sand_tile;
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

    // ==== Parcelable implementation ==================================

    public static Creator<PuzzleResources> CREATOR = new Creator<PuzzleResources>()
    {
        public PuzzleResources createFromParcel(Parcel source)
        {
            int cols = source.readInt();
            int rows = source.readInt();
            PuzzleSetModel.PuzzleSetType type =
                    PuzzleSetModel.PuzzleSetType.valueOf(ParcelableTools.getNonnullString(source.readString()));
            List<PuzzleQuestion> questions = new ArrayList<PuzzleQuestion>();
            source.readTypedList(questions, PuzzleQuestion.CREATOR);
            return new PuzzleResources(cols, rows, type, questions);
        }

        public PuzzleResources[] newArray(int size)
        {
            return null;
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mPuzzleColumnsCount);
        dest.writeInt(mPuzzleRowsCount);
        dest.writeString(mSetType.name());
        dest.writeTypedList(mPuzzleQuestions);
    }
}
