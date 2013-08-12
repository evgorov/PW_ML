package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.rest.RestPuzzle;
import com.ltst.prizeword.rest.RestPuzzleQuestion;
import com.ltst.prizeword.rest.RestPuzzleUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadOnePuzzleFromInternet implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadOnePuzzleFromInternet.sessionKey";
    public static final @Nonnull String BF_PUZZLE_ID = "LoadOnePuzzleFromInternet.puzzleId";
    public static final @Nonnull String BF_SET_ID = "LoadOnePuzzleFromInternet.setId";

    public static final @Nonnull String BF_PUZZLE = "LoadOnePuzzleFromInternet.puzzle";
    public static final @Nonnull String BF_STATUS_CODE = "LoadOnePuzzleFromInternet.statusCode";

    public static final @Nonnull
    Intent createIntent(@Nonnull String sessionKey, @Nonnull String puzzleId, long setId)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_PUZZLE_ID, puzzleId);
        intent.putExtra(BF_SET_ID, setId);
        return intent;
    }
    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if (extras == null)
        {
            return null;
        }
        @Nonnull String sessionKey = extras.getString(BF_SESSION_KEY);
        @Nonnull String puzzleId = extras.getString(BF_PUZZLE_ID);
        long setId = extras.getLong(BF_SET_ID);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            RestPuzzle.RestPuzzleHolder holder = loadPuzzle(sessionKey, puzzleId);
            RestPuzzleUserData.RestPuzzleUserDataHolder dataHolder = loadPuzzleUserData(sessionKey, puzzleId);
            if (holder != null && dataHolder != null)
            {
                Puzzle puzzle = parsePuzzle(holder, dataHolder);
                if (puzzle != null)
                {
                    puzzle.setId = setId;
                    env.dbw.putPuzzle(puzzle);
                    return getFromDatabase(env, puzzleId);
                }
            }
        }
        return getFromDatabase(env, puzzleId);
    }

    private static Bundle packToBundle(@Nonnull Puzzle puzzle, int value)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BF_PUZZLE, puzzle);
        bundle.putInt(BF_STATUS_CODE, value);
        return bundle;
    }

    private @Nullable RestPuzzle.RestPuzzleHolder loadPuzzle(@Nonnull String sessionKey, @Nonnull String puzzleId)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getPuzzle(sessionKey, puzzleId);
        }
        catch (Throwable e)
        {
            Log.e(e.getMessage());
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private @Nullable RestPuzzleUserData.RestPuzzleUserDataHolder loadPuzzleUserData(@Nonnull String sessionKey, @Nonnull String puzzleId)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getPuzzleUserData(sessionKey, puzzleId);
        }
        catch (Throwable e)
        {
            Log.e(e.getMessage());
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private @Nullable Puzzle parsePuzzle(@Nonnull RestPuzzle.RestPuzzleHolder holder,
                                         @Nonnull RestPuzzleUserData.RestPuzzleUserDataHolder dataHolder)
    {
        RestPuzzle puzzle = holder.getPuzzle();
        RestPuzzleUserData puzzleUserData = dataHolder.getPuzzleUserData();
        if (puzzle != null)
        {
            @Nullable List<RestPuzzleQuestion> questionList = puzzle.getQuestions();
            @Nullable List<RestPuzzleUserData.RestSolvedQuestion> solvedQuestions = null;
            @Nullable HashSet<String> solvedQuestionsIdSet = null;
            if (puzzleUserData != null)
            {
                solvedQuestions = puzzleUserData.getSolvedQuestions();
                if (solvedQuestions != null)
                {
                    solvedQuestionsIdSet = RestPuzzleUserData.prepareQuestionIdsSet(solvedQuestions);
                }
            }
            List<PuzzleQuestion> questions = new ArrayList<PuzzleQuestion>(questionList.size());
            for (RestPuzzleQuestion restQ : questionList)
            {
                PuzzleQuestion q = new PuzzleQuestion(0, 0, restQ.getColumn(), restQ.getRow(), restQ.getQuestionText(),
                                    restQ.getAnswer(), restQ.getAnswerPosition(), false);
                RestPuzzleUserData.checkQuestionOnAnswered(q, solvedQuestionsIdSet);
                questions.add(q);
            }
            int timeLeft = puzzle.getTimeGiven();
            int score = 0;
            if (puzzleUserData != null)
            {
                timeLeft = puzzleUserData.getTimeLeft();
                score = puzzleUserData.getScore();
            }
            return new Puzzle(0, 0, puzzle.getPuzzleId(), puzzle.getName(), puzzle.getIssuedAt(),
                            puzzle.getBaseScore(), puzzle.getTimeGiven(),
                            timeLeft, score,
                            false, questions);
        }
        return null;
    }

    protected static @Nullable Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env, @Nonnull String puzzleId)
    {
        @Nullable Puzzle puzzle = env.dbw.getPuzzleByServerId(puzzleId);
        if (puzzle != null)
        {
            return packToBundle(puzzle, RestParams.SC_SUCCESS);
        }
        return null;
    }
}
