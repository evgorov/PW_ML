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

    private @Nullable Point mCurrentQuestionPoint;
    private @Nullable AnswerLetterPointIterator mCurrentAnswerIterator;
    private @Nullable String mCurrentAnswer;
    private @Nullable StringBuffer mCurrentInputBuffer;
    private int mCurrentInputQuestionIndex = -1;
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

    private void setInputMode()
    {
        isInputMode = true;
        mCurrentInputBuffer = new StringBuffer();
    }
    private void resetInputMode()
    {
        isInputMode = false;
        mCurrentAnswer = null;
        mCurrentInputBuffer = null;
        mCurrentQuestionPoint = null;
        mCurrentInputQuestionIndex = -1;
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
            if(updateQuestionState(state, column, row, true))
            {
                if (confirmedHandler != null)
                {
                    confirmedHandler.handle();
                }
                setInputMode();
                mCurrentQuestionPoint = new Point(column, row);
                if (mCurrentAnswerIterator != null)
                {
                    mCurrentAnswerIterator.reset();
                }
            }

        }
    }

    private boolean updateQuestionState(@Nonnull PuzzleTileState state, int column, int row, final boolean isInput)
    {
        if (mResources == null)
        {
            return false;
        }

        int questionIndex = state.getQuestionIndex();
        List<PuzzleQuestion> questions = mResources.getPuzzleQuestions();
        if (questions == null)
        {
            return false;
        }
        @Nullable PuzzleQuestion question = questions.get(questionIndex);
        if (question == null)
        {
            return false;
        }

        if(question.correct)
            return false;

        state.setQuestionState(isInput ? PuzzleTileState.QuestionState.QUESTION_INPUT :
                PuzzleTileState.QuestionState.QUESTION_EMPTY);

        Point answerStart = PuzzleTileState.ArrowType.positionToPoint(question.getAnswerPosition(), column, row);
        if (answerStart != null)
        {
            PuzzleTileState arrowTileState = mResources.getPuzzleState(answerStart.x, answerStart.y);
            if (arrowTileState == null)
            {
                return false;
            }
            int arrowType = arrowTileState.getArrowByQuestionIndex(questionIndex);
            if(arrowType == PuzzleTileState.ArrowType.NO_ARROW)
                return false;

            mCurrentAnswerIterator = new AnswerLetterPointIterator(answerStart,
                    PuzzleTileState.AnswerDirection.getDirectionByArrow(arrowType), question.answer);
            if(isInput)
            {
                mCurrentAnswer = question.answer;
                mCurrentInputQuestionIndex = questionIndex;
                final StringBuffer answerWithSkippedBuffer = new StringBuffer();
                setLetterStateByPointIterator(mCurrentAnswerIterator, new IListener<PuzzleTileState>()
                {
                    @Override
                    public void handle(@Nullable PuzzleTileState puzzleTileState)
                    {
                        if(puzzleTileState == null)
                            return;
                        if(puzzleTileState.getLetterState() != PuzzleTileState.LetterState.LETTER_CORRECT)
                        {
                            answerWithSkippedBuffer.append(AnswerLetterPointIterator.NOT_SKIP_LETTER_CHARACTER);
                        }
                        else
                        {
                            answerWithSkippedBuffer.append(AnswerLetterPointIterator.SKIP_LETTER_CHARACTER);
                        }
                    }
                });
                int letterIndex = 0;
                while (letterIndex < mCurrentAnswer.length())
                {
                    if(answerWithSkippedBuffer.charAt(letterIndex) == AnswerLetterPointIterator.NOT_SKIP_LETTER_CHARACTER)
                    {
                        answerWithSkippedBuffer.setCharAt(letterIndex, mCurrentAnswer.charAt(letterIndex));
                    }
                    letterIndex++;
                }
                mCurrentAnswer = answerWithSkippedBuffer.toString().toLowerCase();

                mCurrentAnswerIterator.reset();
            }
            else
                mCurrentAnswer = null;

            setLetterStateByPointIterator(mCurrentAnswerIterator, new IListener<PuzzleTileState>()
            {
                @Override
                public void handle(@Nullable PuzzleTileState puzzleTileState)
                {
                    if (puzzleTileState == null)
                    {
                        return;
                    }

                    if(puzzleTileState.getLetterState() != PuzzleTileState.LetterState.LETTER_CORRECT)
                    {
                        puzzleTileState.setLetterState(isInput ?
                            PuzzleTileState.LetterState.LETTER_EMPTY_INPUT :
                            PuzzleTileState.LetterState.LETTER_EMPTY);
                    }
                }
            });
        }

        return true;
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
            resetInputMode();
            return;
        }
    }

    public void updateLetterCharacterState(@Nonnull String character, @Nonnull IListenerVoid correctAnswerHandler)
    {
        if (mCurrentAnswerIterator == null || mResources == null)
            return;
        if(isInputMode)
        {
            Point next = mCurrentAnswerIterator.next();
            while(true)
            {
                if(next == null)
                    break;
                @Nullable PuzzleTileState state = mResources.getPuzzleState(next.x, next.y);
                if (state != null)
                {
                    int letterState = state.getLetterState();
                    if(letterState == PuzzleTileState.LetterState.LETTER_CORRECT)
                    {
                        if (mCurrentInputBuffer != null)
                        {
                            mCurrentInputBuffer.append(AnswerLetterPointIterator.SKIP_LETTER_CHARACTER);
                            next = mCurrentAnswerIterator.next();
                        }
                    }
                    else
                    {
                        state.setInputLetter(character);
                        if (mCurrentInputBuffer != null)
                        {
                            mCurrentInputBuffer.append(character);
                            checkAnswer(correctAnswerHandler);
                            break;
                        }
                    }
                }
            }

        }
    }

    private void checkAnswer(@Nonnull IListenerVoid correctAnswerHandler)
    {
        if (mCurrentAnswer == null || mCurrentInputBuffer == null || mCurrentAnswerIterator == null)
        {
            return;
        }

        if(mCurrentInputBuffer.length() < mCurrentAnswer.length())
            return;
        String inputAnswer = mCurrentInputBuffer.toString().toLowerCase();
        if(inputAnswer.equals(mCurrentAnswer))
        {
            mCurrentAnswerIterator.reset();
            Point p = mCurrentAnswerIterator.next();
            if (p != null)
            {
                @Nullable PuzzleTileState state = mResources.getPuzzleState(p.x, p.y);
                if (state != null && mCurrentInputQuestionIndex >= 0)
                {
                    state.removeArrowByQuestionIndex(mCurrentInputQuestionIndex);
                }
            }
            mCurrentAnswerIterator.reset();
            setLetterStateByPointIterator(mCurrentAnswerIterator, new IListener<PuzzleTileState>()
            {
                @Override
                public void handle(@Nullable PuzzleTileState puzzleTileState)
                {
                    if (puzzleTileState == null)
                    {
                        return;
                    }
                    puzzleTileState.setLetterCorrect(true);
                }
            });
            setCurrentQuestionCorrect(true);
            resetInputMode();
            correctAnswerHandler.handle();
        }
    }

    private void setCurrentQuestionCorrect(boolean correct)
    {
        if (mCurrentQuestionPoint == null || mResources == null)
            return;

        @Nullable PuzzleTileState state = mResources.getPuzzleState(mCurrentQuestionPoint.x, mCurrentQuestionPoint.y);
        if (state == null)
        {
            return;
        }

        if(state.hasQuestion)
        {
            state.setQuestionState(correct ? PuzzleTileState.QuestionState.QUESTION_CORRECT :
                    PuzzleTileState.QuestionState.QUESTION_WRONG);
            int questionIndex = state.getQuestionIndex();
            mResources.setQuestionCorrect(questionIndex, correct);
        }
    }

    public void deleteLetterByBackspace()
    {
        if(mCurrentAnswerIterator == null || !isInputMode || mResources == null || mCurrentInputBuffer == null)
            return;
        @Nullable Point last = mCurrentAnswerIterator.last();
        while(true)
        {
            if(last == null)
            {
                break;
            }
            @Nullable PuzzleTileState state = mResources.getPuzzleState(last.x, last.y);
            if(state != null)
            {

                int letterState = state.getLetterState();
                if(letterState == PuzzleTileState.LetterState.LETTER_CORRECT)
                {
                    last = mCurrentAnswerIterator.last();
                    if(mCurrentInputBuffer.length() >= 1)
                        mCurrentInputBuffer.deleteCharAt(mCurrentInputBuffer.length() - 1);
                }
                else
                {
                    state.setLetterState(PuzzleTileState.LetterState.LETTER_EMPTY_INPUT);
                    if(mCurrentInputBuffer.length() >= 1)
                        mCurrentInputBuffer.deleteCharAt(mCurrentInputBuffer.length() - 1);
                    break;
                }
            }
        }
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
