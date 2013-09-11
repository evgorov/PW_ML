package com.ltst.prizeword.crossword.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.rest.RestPuzzle;
import com.ltst.prizeword.rest.RestPuzzleQuestion;
import com.ltst.prizeword.rest.RestPuzzleSet;
import com.ltst.prizeword.rest.RestPuzzleTotalSet;
import com.ltst.prizeword.rest.RestPuzzleUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadPuzzleSetsFromInternet implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY          = "LoadPuzzleSetsFromInternet.sessionKey";
    public static final @Nonnull String BF_PUZZLE_SETS          = "LoadPuzzleSetsFromInternet.puzzleSets";
    public static final @Nonnull String BF_PUZZLES_AT_SET       = "LoadPuzzleSetsFromInternet.puzzles";
    public static final @Nonnull String BF_HINTS_COUNT          = "LoadPuzzleSetsFromInternet.hintsCount";
    public static final @Nonnull String BF_STATUS_CODE          = "LoadPuzzleSetsFromInternet.statusCode";
    public static final @Nonnull String BF_VOLUME_PUZZLE        = "LoadPuzzleSetsFromInternet.volumePuzzle";

    private static final @Nonnull String VOLUME_SHORT = "short";
    private static final @Nonnull String VOLUME_LONG  = "long";
    private static final @Nonnull String VOLUME_SORT  = "sort";
    private static final @Nonnull String VOLUME_CURR  = "current";

    public static final
    @Nonnull
    Intent createShortIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_SHORT);
        return intent;
    }

    public static final
    @Nonnull
    Intent createLongIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_LONG);
        return intent;
    }
    public static final
    @Nonnull
    Intent createSortIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_SORT);
        return intent;
    }

    public static final
    @Nonnull
    Intent createCurrentSetsIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_CURR);
        return intent;
    }

    public static final
    @Nullable
    List<PuzzleSet> extractFromBundle(@Nullable Bundle bundle)
    {
        if (bundle == null)
        {
            return null;
        }
        return bundle.getParcelableArrayList(BF_PUZZLE_SETS);
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        if (!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        } else
        {
            @Nullable String sessionKey = env.extras.getString(BF_SESSION_KEY);
            @Nullable String volumePuzzle = env.extras.getString(BF_VOLUME_PUZZLE);
            if (volumePuzzle == null || sessionKey == null)
            {
                return null;
            }

            if (volumePuzzle.equals(VOLUME_SHORT))
            {
                @Nullable RestPuzzleSet.RestPuzzleSetsHolder data = loadPuzzleSets(env.context, sessionKey);
                if (data != null)
                {
                    ArrayList<PuzzleSet> sets = extractFromRest(data);
                    env.dbw.putPuzzleSetList(sets);
                    return getFromDatabase(env);
                }
            }
            else if (volumePuzzle.equals(VOLUME_LONG))
            {
                int app_release_year = Integer.valueOf(env.context.getResources().getString(R.string.app_release_year));
                int app_release_month = Integer.valueOf(env.context.getResources().getString(R.string.app_release_month));

                Calendar calnow = Calendar.getInstance();

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.MONTH, app_release_month);
                cal.set(Calendar.YEAR, app_release_year);

                while(cal.before(calnow))
                {
                    getFromServer(sessionKey,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), env);
                    cal.add(Calendar.MONTH,1);
                }
                return getFromDatabase(env);
            }
            else if (volumePuzzle.equals(VOLUME_CURR))
            {
                Calendar cal = Calendar.getInstance();
                getFromServer(sessionKey,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1, env);
                return getFromDatabase(env);
            }
            else if (volumePuzzle.equals(VOLUME_SORT))
            {
                return getFromDatabase(env);
            }
        }
        return getFromDatabase(env);
    }

    private
    @Nullable
    RestPuzzleSet.RestPuzzleSetsHolder loadPuzzleSets(@Nonnull Context context, @Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.getPublishedSets(sessionKey);
        } catch (Throwable e)
        {
            Log.e(e.getMessage());
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private
    @Nullable
    RestPuzzleTotalSet.RestPuzzleSetsHolder loadPuzzleTotalSets(@Nonnull Context context, @Nonnull String sessionKey, int year, int month)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.getTotalPublishedSets(sessionKey, year, month);
        } catch (Throwable e)
        {
            Log.e(e.getMessage());
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private
    @Nonnull
    ArrayList<PuzzleSet> extractFromRest(@Nonnull RestPuzzleSet.RestPuzzleSetsHolder data)
    {
        ArrayList<PuzzleSet> sets = new ArrayList<PuzzleSet>(data.getPuzzleSets().size());
        for (RestPuzzleSet restPuzzleSet : data.getPuzzleSets())
        {
            PuzzleSet set = new PuzzleSet(0, restPuzzleSet.getId(), restPuzzleSet.getName(), restPuzzleSet.isBought(),
                    restPuzzleSet.getType(), restPuzzleSet.getMonth(), restPuzzleSet.getYear(),
                    restPuzzleSet.getCreatedAt(), restPuzzleSet.isPublished(), restPuzzleSet.getPuzzles());
            sets.add(set);
        }
        return sets;
    }

    private
    @Nonnull
    ArrayList<PuzzleTotalSet> extractFromTotalRest(@Nonnull Context context, @Nonnull String sessionKey, @Nonnull RestPuzzleTotalSet.RestPuzzleSetsHolder data)
    {
        @Nonnull ArrayList<PuzzleTotalSet> sets = new ArrayList<PuzzleTotalSet>(data.getPuzzleSets().size());
        @Nonnull List<RestPuzzleTotalSet> listRestPuzzleTotalSets = data.getPuzzleSets();
        for (RestPuzzleTotalSet restPuzzleSet : listRestPuzzleTotalSets)
        {
            @Nonnull List<Puzzle> puzzles = new ArrayList<Puzzle>();
            @Nonnull List<RestPuzzle> listRestPuzzles = restPuzzleSet.getPuzzles();
            for (RestPuzzle restPuzzle : listRestPuzzles)
            {
                @Nonnull String puzzleServerId = restPuzzle.getPuzzleId();
                @Nullable RestPuzzleUserData.RestPuzzleUserDataHolder restPuzzleUserDataHolder = LoadOnePuzzleFromInternet.loadPuzzleUserData(context, sessionKey, puzzleServerId);
                @Nonnull Puzzle puzzle = parsePuzzle(restPuzzle, restPuzzleUserDataHolder);
                puzzles.add(puzzle);
            }

            @Nonnull PuzzleTotalSet set = new PuzzleTotalSet(0, restPuzzleSet.getId(), restPuzzleSet.getName(), restPuzzleSet.isBought(),
                    restPuzzleSet.getType(), restPuzzleSet.getMonth(), restPuzzleSet.getYear(),
                    restPuzzleSet.getCreatedAt(), restPuzzleSet.isPublished(), puzzles);

            sets.add(set);
        }
        return sets;
    }

    private static
    @Nonnull
    Bundle packToBundle(@Nonnull ArrayList<PuzzleSet> sets, int hintsCount, @Nonnull HashMap<String, List<Puzzle>> mapPuzzles, int status)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_PUZZLE_SETS, sets);
        bundle.putInt(BF_STATUS_CODE, status);
        bundle.putInt(BF_HINTS_COUNT, hintsCount);
        bundle.putSerializable(BF_PUZZLES_AT_SET, mapPuzzles);
        return bundle;
    }

    public static
    @Nullable
    Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        List<PuzzleSet> sets = env.dbw.getPuzzleSets();
        int hintsCount = env.dbw.getUserHintsCount();
        List<Puzzle> puzzles = null;
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = new HashMap<String, List<Puzzle>>();
        for (PuzzleSet puzzleSet : sets)
        {
            puzzles = env.dbw.getPuzzlesBySetId(puzzleSet.id);
            mapPuzzles.put(puzzleSet.serverId, puzzles);
        }
        return packToBundle(new ArrayList<PuzzleSet>(sets), hintsCount, mapPuzzles, RestParams.SC_SUCCESS);
    }
    public static
    @Nullable
    Bundle getSolvedFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        List<PuzzleSet> sets = env.dbw.getPuzzleSets();
        int hintsCount = env.dbw.getUserHintsCount();
        List<Puzzle> puzzles = null;
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = new HashMap<String, List<Puzzle>>();
        for (PuzzleSet puzzleSet : sets)
        {
            puzzles = env.dbw.getSolvedPuzzlesBySetId(puzzleSet.id);
            mapPuzzles.put(puzzleSet.serverId, puzzles);
        }
        return packToBundle(new ArrayList<PuzzleSet>(sets), hintsCount, mapPuzzles, RestParams.SC_SUCCESS);
    }

    private void getFromServer(@Nonnull String sessionKey, int year, int month, @Nonnull DbService.DbTaskEnv env)
    {
        @Nullable RestPuzzleTotalSet.RestPuzzleSetsHolder data = loadPuzzleTotalSets(env.context, sessionKey,year,month);
        if (data != null)
        {
            @Nonnull List<PuzzleTotalSet> sets = extractFromTotalRest(env.context, sessionKey, data);
            env.dbw.putPuzzleTotalSetList(sets);
        }
    }


    static public
    @Nonnull
    Puzzle parsePuzzle(@Nonnull RestPuzzle restPuzzle, @Nullable RestPuzzleUserData.RestPuzzleUserDataHolder restPuzzleUserDataHolder)
    {
        @Nullable RestPuzzleUserData restPuzzleUserData = null;
        if (restPuzzleUserDataHolder != null)
        {
            restPuzzleUserData = restPuzzleUserDataHolder.getPuzzleUserData();
        }
        @Nullable List<RestPuzzleQuestion> questionList = restPuzzle.getQuestions();
        @Nullable List<RestPuzzleUserData.RestSolvedQuestion> solvedQuestions = null;
        @Nullable HashSet<String> solvedQuestionsIdSet = null;
        if (restPuzzleUserData != null)
        {
            solvedQuestions = restPuzzleUserData.getSolvedQuestions();
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
        int timeLeft = restPuzzle.getTimeGiven();
        int score = 0;
        if (restPuzzleUserData != null)
        {
            timeLeft = restPuzzleUserData.getTimeLeft();
            score = restPuzzleUserData.getScore();
        }
        return new Puzzle(0, 0, restPuzzle.getPuzzleId(), restPuzzle.getName(), restPuzzle.getIssuedAt(),
                restPuzzle.getBaseScore(), restPuzzle.getTimeGiven(),
                timeLeft, score,
                false, questions);
    }
}
