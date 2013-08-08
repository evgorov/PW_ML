package com.ltst.prizeword.crossword.engine;

import android.graphics.Point;

import com.ltst.prizeword.crossword.model.IOnePuzzleModel;
import com.ltst.prizeword.crossword.model.OnePuzzleModel;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleResourcesAdapter
{
    private @Nonnull IOnePuzzleModel mPuzzleModel;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nonnull PuzzleSet mPuzzleSet;
    private @Nullable Puzzle mPuzzle;
    private @Nullable IListenerVoid mPuzzleUpdater;
    private @Nonnull List<IListener<PuzzleResources>> mResourcesUpdaterList;
    private @Nullable PuzzleResources mResources;

    private @Nonnull IBitmapResourceModel mIBitmapResourceModel;
    private @Nullable AnswerLetterPointIterator mCurrentAnswerIterator;
    private boolean isInputMode;

    public PuzzleResourcesAdapter(@Nonnull IBcConnector bcConnector,
                                  @Nonnull String sessionKey,
                                  @Nonnull PuzzleSet puzzleSet)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mPuzzleSet = puzzleSet;
        mResourcesUpdaterList = new ArrayList<IListener<PuzzleResources>>();
        mIBitmapResourceModel = new BitmapResourceModel(mBcConnector);
        isInputMode = false;
    }

    public void updatePuzzle(@Nonnull String puzzleServerId)
    {
        mPuzzleModel = new OnePuzzleModel(mBcConnector, mSessionKey, puzzleServerId, mPuzzleSet.id);
        mPuzzleModel.updateDataByDb(updateHandler);
        mPuzzleModel.updateDataByInternet(updateHandler);
    }

    public void updatePuzzleStateByTap(int column, int row, @Nullable IListenerVoid confirmedHandler)
    {
        if (mResources == null)
        {
            return;
        }

        @Nullable PuzzleTileState state = mResources.getPuzzleState(column, row);
        if (state == null)
        {
            return;
        }

        if(state.hasQuestion)
        {
            if (confirmedHandler != null)
            {
                confirmedHandler.handle();
            }
            updateQuestionState(state, column, row, true);
            isInputMode = true;
            if (mCurrentAnswerIterator != null)
            {
                mCurrentAnswerIterator.reset();
            }

        }
    }

    private void updateQuestionState(@Nonnull PuzzleTileState state, int column, int row, final boolean isInput)
    {
        if (mResources == null)
        {
            return;
        }

        state.setQuestionState(isInput ? PuzzleTileState.QuestionState.QUESTION_INPUT :
                PuzzleTileState.QuestionState.QUESTION_EMPTY);
        int questionIndex = state.getQuestionIndex();
        List<PuzzleQuestion> questions = mResources.getPuzzleQuestions();
        if (questions == null)
        {
            return;
        }
        @Nullable PuzzleQuestion question = questions.get(questionIndex);
        if (question == null)
        {
            return;
        }

        Point answerStart = PuzzleTileState.ArrowType.positionToPoint(question.getAnswerPosition(), column, row);
        if (answerStart != null)
        {
            PuzzleTileState arrowTileState = mResources.getPuzzleState(answerStart.x, answerStart.y);
            if (arrowTileState == null)
            {
                return;
            }
            int arrowType = arrowTileState.getArrowByQuestionIndex(questionIndex);
            if(arrowType == PuzzleTileState.ArrowType.NO_ARROW)
                return;

            mCurrentAnswerIterator = new AnswerLetterPointIterator(answerStart,
                    PuzzleTileState.AnswerDirection.getDirectionByArrow(arrowType), question.answer);
            setLetterStateByPointIterator(mCurrentAnswerIterator, new IListener<PuzzleTileState>()
            {
                @Override
                public void handle(@Nullable PuzzleTileState puzzleTileState)
                {
                    if (puzzleTileState == null)
                    {
                        return;
                    }

                    puzzleTileState.setLetterState(isInput ?
                            PuzzleTileState.LetterState.LETTER_EMPTY_INPUT :
                            PuzzleTileState.LetterState.LETTER_EMPTY);
                }
            });
        }


    }

    public void cancelLastQuestionState(int column, int row)
    {
        if (mResources == null)
        {
            return;
        }

        @Nullable PuzzleTileState state = mResources.getPuzzleState(column, row);
        if (state == null)
        {
            return;
        }

        if(state.hasQuestion)
        {
            updateQuestionState(state, column, row, false);
            isInputMode = false;
            return;
        }
    }

    public void updateLetterCharacterState(@Nonnull String character)
    {
        if (mCurrentAnswerIterator == null || mResources == null)
            return;
        if(isInputMode)
        {
            if (mCurrentAnswerIterator.hasNext())
            {
                Point next = mCurrentAnswerIterator.next();
                @Nullable PuzzleTileState state = mResources.getPuzzleState(next.x, next.y);
                if (state != null)
                {
                    state.setInputLetter(character);
                }
            }
        }
    }

    public void deleteLetterByBackspace()
    {
        if(mCurrentAnswerIterator == null || !isInputMode || mResources == null)
            return;
        @Nullable Point last = mCurrentAnswerIterator.last();
        if(last == null)
        {
            return;
        }
        @Nullable PuzzleTileState state = mResources.getPuzzleState(last.x, last.y);
        if(state != null)
            state.setLetterState(PuzzleTileState.LetterState.LETTER_EMPTY_INPUT);
    }

    private void setLetterStateByPointIterator(@Nonnull AnswerLetterPointIterator iter, @Nonnull IListener<PuzzleTileState> handler)
    {
        if (mResources == null)
        {
            return;
        }
        while (iter.hasNext())
        {
            Point next = iter.next();
            @Nullable PuzzleTileState state = mResources.getPuzzleState(next.x, next.y);
            if (state != null)
            {
                handler.handle(state);
            }
        }
    }

    @Nonnull
    public IBitmapResourceModel getBitmapResourceModel()
    {
        return mIBitmapResourceModel;
    }

    @Nullable
    public PuzzleResources getResources()
    {
        return mResources;
    }

    public void updateResources()
    {
        if (mPuzzle == null || mResourcesUpdaterList == null)
        {
            return;
        }

        PuzzleSetModel.PuzzleSetType type = PuzzleSetModel.getPuzzleTypeByString(mPuzzleSet.type);
        mResources = new PuzzleResources(type, mPuzzle.questions);
        for (IListener<PuzzleResources> puzzleResourcesHandler : mResourcesUpdaterList)
        {
            puzzleResourcesHandler.handle(mResources);
        }
    }

    public void addResourcesUpdater(@Nullable IListener<PuzzleResources> resourcesUpdater)
    {
        mResourcesUpdaterList.add(resourcesUpdater);
    }

    public void setPuzzleUpdater(@Nullable IListenerVoid puzzleUpdater)
    {
        mPuzzleUpdater = puzzleUpdater;
    }

    private IListenerVoid updateHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if (mPuzzleUpdater != null)
            {
                mPuzzleUpdater.handle();
            }
            mPuzzle = mPuzzleModel.getPuzzle();
            updateResources();
        }
    };
}
