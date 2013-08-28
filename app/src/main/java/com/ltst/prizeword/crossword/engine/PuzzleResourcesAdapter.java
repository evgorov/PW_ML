package com.ltst.prizeword.crossword.engine;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleResourcesAdapter
{
    public static final @Nonnull String BF_PUZZLE = "PuzzleResourcesAdapter.puzzle";
    public static final @Nonnull String BF_RESOURCES = "PuzzleResourcesAdapter.resources";

    private @Nonnull IOnePuzzleModel mPuzzleModel;
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
            mPuzzleModel = new OnePuzzleModel(mBcConnector, mSessionKey, mPuzzle.serverId, mPuzzleSet.id);
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
        mCurrentInputQuestionIndex = -1;
        if (mCurrentInputPuzzleStates != null)
        {
            mCurrentInputPuzzleStates.clear();
            mCurrentInputPuzzleStates = null;
        }
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
        mPuzzleModel.updatePuzzleUserData();
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
            return;
        }
    }

    public void updateLetterCharacterState(@Nonnull String character, @Nonnull Point inputTranslatePoint, @Nonnull Rect tileRect, @Nonnull IListenerVoid correctAnswerHandler)
    {
        if (mCurrentAnswerIterator == null || mResources == null || mCurrentInputPuzzleStates == null)
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
                        if(!mCurrentAnswerIterator.isLast())
                        {
                            mCurrentAnswerIterator.offsetPointByDirection(inputTranslatePoint, tileRect.width(), tileRect.height());
                        }
                        if (mCurrentInputBuffer != null)
                        {
                            mCurrentInputBuffer.append(AnswerLetterPointIterator.SKIP_LETTER_CHARACTER);
                            next = mCurrentAnswerIterator.next();
                        }
                    }
                    else
                    {
                        state.setInputLetter(character);
                        mCurrentInputPuzzleStates.add(state);
                        if (mCurrentInputBuffer != null)
                        {
                            mCurrentInputBuffer.append(character);
                            boolean correct = checkAnswer(correctAnswerHandler);
                            if(!correct)
                            {
                                mCurrentAnswerIterator.offsetPointByDirection(inputTranslatePoint, tileRect.width(), tileRect.height());
                            }
                            break;
                        }
                    }
                }
            }

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
            checkCurrectCrossingQuestions();
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
        if (mCurrentAnswerIterator == null || mResources == null)
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
                    puzzleTileState.hasInputLetter = false;
                    puzzleTileState.setLetterState(PuzzleTileState.LetterState.LETTER_EMPTY);
                }
            }
        });
        checkCurrectCrossingQuestions();
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

    public void deleteLetterByBackspace(@Nonnull Point inputTranslatePoint, @Nonnull Rect tileRect)
    {
        if(mCurrentAnswerIterator == null || !isInputMode || mResources == null ||
                mCurrentInputBuffer == null || mCurrentInputPuzzleStates == null)
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
                    if(mCurrentInputBuffer.length() > 1 || !mCurrentAnswerIterator.isFirst())
                    {
                        mCurrentInputBuffer.deleteCharAt(mCurrentInputBuffer.length() - 1);
                        mCurrentAnswerIterator.offsetPointByDirection(inputTranslatePoint, -tileRect.width(), -tileRect.height());
                    }
                    else break;
                }
                else
                {
                    state.setLetterState(PuzzleTileState.LetterState.LETTER_EMPTY_INPUT);
                    if(mCurrentInputBuffer.length() >= 1)
                    {
                        mCurrentInputBuffer.deleteCharAt(mCurrentInputBuffer.length() - 1);
                        mCurrentAnswerIterator.offsetPointByDirection(inputTranslatePoint, -tileRect.width(), -tileRect.height());
                    }
                    if(mCurrentInputPuzzleStates.size() >= 1)
                    {
                        mCurrentInputPuzzleStates.remove(mCurrentInputPuzzleStates.size() - 1);
                    }
                    break;
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
}
