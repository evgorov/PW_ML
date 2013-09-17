package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;

import com.ltst.prizeword.app.SharedPreferencesHelper;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IOnePuzzleModel;
import com.ltst.prizeword.crossword.model.OnePuzzleModel;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.crossword.wordcheck.WordCompletenessChecker;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleResourcesAdapter
{
    public static final @Nonnull String BF_PUZZLE = "PuzzleResourcesAdapter.puzzle";
    public static final @Nonnull String BF_RESOURCES = "PuzzleResourcesAdapter.resources";

    private @Nullable IOnePuzzleModel mPuzzleModel;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nonnull PuzzleSet mPuzzleSet;
    private @Nullable Puzzle mPuzzle;
    private @Nullable IListenerVoid mPuzzleUpdater;
    private @Nullable IListenerVoid mPuzzleSolvedHandler;
    private @Nullable IListenerVoid mPuzzleStateHandler;
    private @Nonnull List<IListener<PuzzleResources>> mResourcesUpdaterList;

    private @Nullable PuzzleResources mResources;

    private @Nonnull IBitmapResourceModel mIBitmapResourceModel;

    private @Nullable Point mCurrentQuestionPoint;
    private @Nullable AnswerLetterPointIterator mCurrentAnswerIterator;
    private @Nullable String mCurrentAnswer;
    private @Nullable StringBuffer mCurrentInputBuffer;
    private int mCurrentInputQuestionIndex = -1;
    private boolean isInputMode;
    private @Nullable List<PuzzleTileState> mCurrentInputPuzzleStates;
    private @Nullable Point mCurrentTileFocusPoint;
    private @Nonnull Context mContext;

    public PuzzleResourcesAdapter(@Nonnull Context context, @Nonnull IBcConnector bcConnector,
                                  @Nonnull String sessionKey,
                                  @Nonnull PuzzleSet puzzleSet)
    {
        mContext = context;
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mPuzzleSet = puzzleSet;
        mResourcesUpdaterList = new ArrayList<IListener<PuzzleResources>>();
        mIBitmapResourceModel = new BitmapResourceModel(mBcConnector);
        isInputMode = false;
    }

    public void close()
    {
        mPuzzleModel.close();
    }

    public void updatePuzzle(@Nonnull String puzzleServerId)
    {
        mPuzzleModel = new OnePuzzleModel(mContext, mBcConnector, mSessionKey, puzzleServerId, mPuzzleSet.id);
        mPuzzleModel.updateDataByDb(updateHandler);
        mPuzzleModel.updateDataByInternet(updateHandler);
    }

    public void saveState(@Nonnull Bundle bundle)
    {
        bundle.putParcelable(BF_PUZZLE, mPuzzle);
        bundle.putParcelable(BF_RESOURCES, mResources);
    }

    public void restoreState(@Nonnull Bundle bundle)
    {
        mPuzzle = bundle.getParcelable(BF_PUZZLE);
        mResources = bundle.getParcelable(BF_RESOURCES);
        if (mPuzzle != null)
        {
            mPuzzleModel = new OnePuzzleModel(mContext, mBcConnector, mSessionKey, mPuzzle.serverId, mPuzzleSet.id);
        }
        for (IListener<PuzzleResources> puzzleResourcesHandler : mResourcesUpdaterList)
        {
            puzzleResourcesHandler.handle(mResources);
        }
    }

    private void setInputMode()
    {
        isInputMode = true;
        mCurrentInputBuffer = new StringBuffer();
    }

    public boolean isInputMode()
    {
        return isInputMode;
    }

    private void resetInputMode()
    {
        isInputMode = false;
        mCurrentAnswer = null;
        mCurrentInputBuffer = null;
        mCurrentQuestionPoint = null;
//        mCurrentTileFocusPoint = null;
        mCurrentInputQuestionIndex = -1;
        if (mCurrentInputPuzzleStates != null)
        {
            mCurrentInputPuzzleStates.clear();
            mCurrentInputPuzzleStates = null;
        }
    }

    public boolean isPuzzleInCurrentMonth()
    {
        long currentTime = SharedPreferencesHelper.getInstance(mContext).getLong(SharedPreferencesValues.SP_CURRENT_DATE, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        return currentMonth == mPuzzleSet.month;
    }

    public int getTimeLeft()
    {
        if (mPuzzle == null)
        {
            return 0;
        }
        return mPuzzle.timeLeft;
    }

    public int getTimeGiven()
    {
        if (mPuzzle == null)
        {
            return 0;
        }
        return mPuzzle.timeGiven;
    }

    public void setTimeLeft(int timeLeft)
    {
        if (mPuzzle != null)
        {
            mPuzzle.timeLeft = timeLeft;
        }
    }

    public void setScore(int score)
    {
        if (mPuzzle != null && score >= 0)
        {
            mPuzzle.score = score;
        }
    }

    public int getSolvedQuestionsPercent()
    {
        if (mPuzzle == null)
        {
            return 0;
        }
        return mPuzzle.solvedPercent;
    }

    public boolean isPuzzleSolved()
    {
        if (mPuzzle == null)
        {
            return false;
        }
        return mPuzzle.isSolved;
    }

    @Nullable
    public List<PuzzleTileState> getCurrentInputPuzzleStates()
    {
        return mCurrentInputPuzzleStates;
    }

    @Nullable
    public Point getCurrentTileFocusPoint()
    {
        return mCurrentTileFocusPoint;
    }

    public void setPuzzleStateHandler(@Nullable IListenerVoid puzzleStateHandler)
    {
        mPuzzleStateHandler = puzzleStateHandler;
    }

    public void setPuzzleSolvedHandler(@Nonnull IListenerVoid puzzleSolvedHandler)
    {
        mPuzzleSolvedHandler = puzzleSolvedHandler;
    }

    public @Nullable IListenerVoid getOnInputAnimationEndHandler()
    {
        if (mCurrentAnswerIterator == null)
        {
            return null;
        }

        return new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mCurrentAnswerIterator.reset();
                setLetterStateByPointIterator(mResources, mCurrentAnswerIterator, new IListener<PuzzleTileState>()
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
                            String letter = String.valueOf(mCurrentAnswerIterator.getCurrentLetter()).toUpperCase();
                            puzzleTileState.setInputLetter(letter);
                            puzzleTileState.setLetterCorrect(true);
                        }
                    }
                });
                setCurrentQuestionCorrect(true);
                resetInputMode();
            }
        };
    }

    @Nonnull
    public IBitmapResourceModel getBitmapResourceModel()
    {
        return mIBitmapResourceModel;
    }

    public @Nullable String getPuzzleName()
    {
        if (mPuzzle == null)
            return null;
        return mPuzzle.name;
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
            mPuzzle = mPuzzleModel.getPuzzle();
            if (mPuzzleUpdater != null)
            {
                mPuzzleUpdater.handle();
            }
            if(mPuzzle != null && !mPuzzle.isSolved)
                updateResources();
        }
    };

    public void updatePuzzleUserData()
    {
        if (mPuzzleModel != null)
        {
            mPuzzleModel.updatePuzzleUserData();
        }
    }

    public void updatePuzzleUserData(@Nonnull IListenerVoid handler)
    {
        if (mPuzzleModel != null)
        {
            mPuzzleModel.updatePuzzleUserData(handler);
        }
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
            resetInputMode();
            if(updateQuestionState(state, column, row, true))
            {
                setInputMode();

                mCurrentQuestionPoint = new Point(column, row);
                if (mCurrentAnswerIterator == null || mCurrentInputBuffer == null)
                {
                    return;
                }
                scrollIteratorToFirstInput();
                mCurrentTileFocusPoint = new Point(mCurrentAnswerIterator.current());

                if (confirmedHandler != null)
                {
                    confirmedHandler.handle();
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

        if(question.isAnswered)
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
                if (mCurrentInputPuzzleStates == null)
                {
                    mCurrentInputPuzzleStates = new ArrayList<PuzzleTileState>();
                }

                mCurrentAnswer = question.answer;
                mCurrentInputQuestionIndex = questionIndex;
                final StringBuffer answerWithSkippedBuffer = new StringBuffer();
                setLetterStateByPointIterator(mResources, mCurrentAnswerIterator, new IListener<PuzzleTileState>()
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

            setLetterStateByPointIterator(mResources, mCurrentAnswerIterator, new IListener<PuzzleTileState>()
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
        }
    }

    public void updateLetterCharacterState(@Nonnull String character,  @Nonnull IListenerVoid correctAnswerHandler)
    {
        if (mCurrentAnswerIterator == null || mResources == null || mCurrentInputPuzzleStates == null
        || mCurrentTileFocusPoint == null || mCurrentAnswer == null)
            return;

        boolean characterSet = false;
        if(isInputMode)
        {
            Point next = mCurrentAnswerIterator.current();
            while(true)
            {
                if(next == null)
                    break;
                Log.i("Next: " + next.x + ":" + next.y + " " + mCurrentInputBuffer.toString());
                @Nullable PuzzleTileState state = mResources.getPuzzleState(next.x, next.y);
                if (state != null)
                {
                    if(!state.hasInputLetter && characterSet)
                        break;
                    int letterState = state.getLetterState();
                    if(letterState == PuzzleTileState.LetterState.LETTER_CORRECT)
                    {
                        if (mCurrentInputBuffer != null)
                        {
                            mCurrentInputBuffer.append(AnswerLetterPointIterator.SKIP_LETTER_CHARACTER);
                            next = mCurrentAnswerIterator.next();
                        }
                        if(!mCurrentAnswerIterator.isLast())
                        {
                            mCurrentTileFocusPoint = new Point(mCurrentAnswerIterator.current());
                        }
                    }
                    else if(letterState == PuzzleTileState.LetterState.LETTER_INPUT)
                    {
                        if(!mCurrentAnswerIterator.isLast())
                        {
                            next = mCurrentAnswerIterator.next();
                        }
                        if(!mCurrentAnswerIterator.isLast())
                        {
                            mCurrentTileFocusPoint = new Point(mCurrentAnswerIterator.current());
                        }
                        else next = null;
                    }
                    else
                    {
                        if (mCurrentInputBuffer != null && mCurrentInputBuffer.length() < mCurrentAnswer.length())
                        {
                            state.setInputLetter(character);
                            characterSet = true;
                            mCurrentInputPuzzleStates.add(state);
                            mCurrentInputBuffer.append(character);
                            boolean correct = checkAnswer(correctAnswerHandler);
                            if(!mCurrentAnswerIterator.isLast())
                            {
                                next = mCurrentAnswerIterator.next();
                            }
                            if(!correct && !mCurrentAnswerIterator.isLast())
                            {
                                mCurrentTileFocusPoint = new Point(mCurrentAnswerIterator.current());
                            }
                            else next = null;
                        }
                        else next = null;
                    }
                }
                else
                {
                    next = null;
                }
            }

        }
    }

    public void deleteLetterByBackspace()
    {
        if(mCurrentAnswerIterator == null || !isInputMode || mResources == null ||
                mCurrentInputBuffer == null || mCurrentInputPuzzleStates == null || mCurrentTileFocusPoint == null)
            return;
        @Nullable Point last = mCurrentAnswerIterator.current();
        boolean letterDeleted = false;
        while(true)
        {
            if(last == null)
            {
                break;
            }
            Log.i("Last: " + last.x + ":" + last.y + " " + mCurrentInputBuffer.toString());
            @Nullable PuzzleTileState state = mResources.getPuzzleState(last.x, last.y);
            if(state != null)
            {
                int letterState = state.getLetterState();
                if(letterState == PuzzleTileState.LetterState.LETTER_CORRECT)
                {
                    if(mCurrentInputBuffer.length() >= 1 || !mCurrentAnswerIterator.isFirst())
                    {
                        last = mCurrentAnswerIterator.last();
                        mCurrentInputBuffer.deleteCharAt(mCurrentInputBuffer.length() - 1);
                        mCurrentTileFocusPoint = new Point(mCurrentAnswerIterator.current());
                    }
                    else
                    {
                        scrollIteratorToFirstInput();
                        break;
                    }
                }
                else
                if(letterState == PuzzleTileState.LetterState.LETTER_INPUT)
                {
                    if(letterDeleted)
                        break;
                    if(mCurrentInputBuffer.length() >= 1)
                    {
                        state.setLetterState(PuzzleTileState.LetterState.LETTER_EMPTY_INPUT);
                        mCurrentInputBuffer.deleteCharAt(mCurrentInputBuffer.length() - 1);
                        letterDeleted = true;
                        last = mCurrentAnswerIterator.last();
                        mCurrentTileFocusPoint = new Point(mCurrentAnswerIterator.current());
                    }
                    if(mCurrentInputPuzzleStates.size() >= 1)
                    {
                        mCurrentInputPuzzleStates.remove(mCurrentInputPuzzleStates.size() - 1);
                    }
                    else
                    {
                        scrollIteratorToFirstInput();
                        last = null;
                    }
                }
                else
                if(letterState == PuzzleTileState.LetterState.LETTER_EMPTY_INPUT)
                {
                    if(letterDeleted)
                        break;
                    if(mCurrentAnswerIterator.isFirst())
                        break;
                    last = mCurrentAnswerIterator.last();
                    mCurrentTileFocusPoint = new Point(mCurrentAnswerIterator.current());
                }
            }
            else
                last = null;
        }
    }

    private boolean checkAnswer(@Nonnull IListenerVoid correctAnswerHandler)
    {
        if (mCurrentAnswer == null || mCurrentInputBuffer == null || mCurrentAnswerIterator == null)
        {
            return false;
        }

        String inputAnswer = mCurrentInputBuffer.toString().toLowerCase().replaceAll("_", "");
        String currentAnswer = mCurrentAnswer.replaceAll("_", "");
        if(inputAnswer.equals(currentAnswer))
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
            setCurrentQuestionCorrect(true);
            for (PuzzleTileState state : mCurrentInputPuzzleStates)
            {
                state.setLetterCorrect(true);
            }

            checkCurrectCrossingQuestions();

            for (PuzzleTileState state : mCurrentInputPuzzleStates)
            {
                state.hasInputLetter = false;
                state.setLetterState(PuzzleTileState.LetterState.LETTER_EMPTY);
            }

            correctAnswerHandler.handle();

            return true;
        }
        else
            return false;

    }

    private void checkCurrectCrossingQuestions()
    {
        if(mCurrentInputQuestionIndex < 0 || mResources == null)
            return;
        final @Nullable List<PuzzleQuestion> questions = mResources.getPuzzleQuestions();
        if (questions == null)
        {
            return;
        }

        WordCompletenessChecker.checkWords(mCurrentInputQuestionIndex, mResources,
        new IListenerInt()
        {
            @Override
            public void handle(int i)
            {
                mResources.setQuestionCorrect(i, true);
                mPuzzleModel.setQuestionAnswered(questions.get(i), true);
            }
        });
    }

    public void setCurrentQuestionCorrect(@Nullable IListenerVoid handler)
    {
        if (mCurrentAnswerIterator == null || mResources == null || mCurrentQuestionPoint == null)
        {
            return;
        }

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

        mCurrentInputPuzzleStates = new ArrayList<PuzzleTileState>();
        setLetterStateByPointIterator(mResources, mCurrentAnswerIterator, new IListener<PuzzleTileState>()
        {
            @Override
            public void handle(@Nullable PuzzleTileState puzzleTileState)
            {
                if (puzzleTileState == null)
                {
                    return;
                }

                if (puzzleTileState.getLetterState() != PuzzleTileState.LetterState.LETTER_CORRECT)
                {
                    mCurrentInputPuzzleStates.add(puzzleTileState);
                    String letter = String.valueOf(mCurrentAnswerIterator.getCurrentLetter()).toUpperCase();
                    puzzleTileState.setInputLetter(letter);
                    puzzleTileState.setLetterCorrect(true);
                }
            }
        });
        checkCurrectCrossingQuestions();

        for (PuzzleTileState puzzleTileState : mCurrentInputPuzzleStates)
        {
            puzzleTileState.hasInputLetter = false;
            puzzleTileState.setLetterState(PuzzleTileState.LetterState.LETTER_EMPTY);
        }

        setCurrentQuestionCorrect(true);

        if (handler != null)
        {
            handler.handle();
        }
    }

    public void setCurrentQuestionWrong()
    {
        if (mCurrentAnswerIterator == null || mResources == null)
        {
            return;
        }
        setCurrentQuestionCorrect(false);
        mCurrentAnswerIterator.reset();
        setLetterStateByPointIterator(mResources, mCurrentAnswerIterator, new IListener<PuzzleTileState>()
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
                    if(puzzleTileState.hasInputLetter)
                        puzzleTileState.setLetterCorrect(false);
                    else
                        puzzleTileState.setLetterState(PuzzleTileState.LetterState.LETTER_EMPTY);
                }
            }
        });
    }

    private void setCurrentQuestionCorrect(boolean correct)
    {
        if (mCurrentQuestionPoint == null || mResources == null || mPuzzle == null)
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
            if(!correct)
                return;

            int index = state.getQuestionIndex();
            List<PuzzleQuestion> qList = mResources.getPuzzleQuestions();
            if (qList != null)
            {
                if(index >= 0 && index < qList.size())
                {
                    PuzzleQuestion q = qList.get(index);
                    q.isAnswered = true;
                    mPuzzleModel.setQuestionAnswered(q, true);
                    mPuzzle.countSolvedPercent();
                    if (mPuzzleStateHandler != null)
                    {
                        mPuzzleStateHandler.handle();
                    }
                    if(isPuzzleSolved() && mPuzzleSolvedHandler != null)
                    {
                        mPuzzleSolvedHandler.handle();
                    }

                    updatePuzzleUserData();
                }
            }
        }
    }

    public static void setLetterStateByPointIterator(@Nullable PuzzleResources resources,
                                                     @Nonnull AnswerLetterPointIterator iter,
                                                     @Nonnull IListener<PuzzleTileState> handler)
    {
        if (resources == null)
        {
            return;
        }
        while (iter.hasNext())
        {
            Point next = iter.next();
            @Nullable PuzzleTileState state = resources.getPuzzleState(next.x, next.y);
            handler.handle(state);
        }
    }

    private void scrollIteratorToFirstInput()
    {
        if (mCurrentAnswerIterator == null || mResources == null || mCurrentInputBuffer == null)
        {
            return;
        }

        mCurrentAnswerIterator.reset();
        while (mCurrentAnswerIterator.hasNext())
        {
            Point next = mCurrentAnswerIterator.next();
            @Nullable PuzzleTileState tileState = mResources.getPuzzleState(next.x, next.y);
            if(tileState != null && !tileState.hasInputLetter)
                break;
            else
                mCurrentInputBuffer.append(AnswerLetterPointIterator.SKIP_LETTER_CHARACTER);
        }
    }
}
