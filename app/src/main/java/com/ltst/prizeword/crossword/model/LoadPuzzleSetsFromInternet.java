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
import com.ltst.prizeword.rest.RestPuzzleSet;
import com.ltst.prizeword.rest.RestPuzzleTotalSet;
import com.ltst.prizeword.rest.RestPuzzleUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadPuzzleSetsFromInternet implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadPuzzleSetsFromInternet.sessionKey";
    public static final @Nonnull String BF_PUZZLE_SETS = "LoadPuzzleSetsFromInternet.puzzleSets";
    public static final @Nonnull String BF_PUZZLES_AT_SET = "LoadPuzzleSetsFromInternet.puzzles";
    public static final @Nonnull String BF_HINTS_COUNT = "LoadPuzzleSetsFromInternet.hintsCount";
    public static final @Nonnull String BF_STATUS_CODE = "LoadPuzzleSetsFromInternet.statusCode";
    public static final @Nonnull String BF_VOLUME_PUZZLE = "LoadPuzzleSetsFromInternet.volumePuzzle";

    private static final @Nonnull String VOLUME_SHORT = "short";
    private static final @Nonnull String VOLUME_LONG = "long";

    public static final @Nonnull Intent createShortIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_SHORT);
        return intent;
    }

    public static final @Nonnull Intent createLongIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_LONG);
        return intent;
    }

    public static final @Nullable List<PuzzleSet> extractFromBundle(@Nullable Bundle bundle)
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
        @Nonnull String sessionKey = env.extras.getString(BF_SESSION_KEY);
        @Nonnull String volumePuzzle = env.extras.getString(BF_VOLUME_PUZZLE);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            if(volumePuzzle.equals(VOLUME_SHORT))
            {
                @Nullable RestPuzzleSet.RestPuzzleSetsHolder data = loadPuzzleSets(sessionKey);
                if (data != null)
                {
                    ArrayList<PuzzleSet> sets = extractFromRest(data);
                    env.dbw.putPuzzleSetList(sets);
                    return getFromDatabase(env);
                }
            }
            else if(volumePuzzle.equals(VOLUME_LONG))
            {
                @Nullable RestPuzzleTotalSet.RestPuzzleSetsHolder data = loadPuzzleTotalSets(sessionKey);
                if (data != null)
                {
                    @Nonnull List<PuzzleTotalSet> sets = extractFromTotalRest(data);
                    env.dbw.putPuzzleTotalSetList(sets);
                    return getFromDatabase(env);
                }
            }
        }
        return getFromDatabase(env);
    }

    private @Nullable RestPuzzleSet.RestPuzzleSetsHolder loadPuzzleSets(@Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getPublishedSets(sessionKey);
        }
        catch (Throwable e)
        {
            Log.e(e.getMessage());
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private @Nullable RestPuzzleTotalSet.RestPuzzleSetsHolder loadPuzzleTotalSets(@Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getTotalPublishedSets(sessionKey);
        }
        catch (Throwable e)
        {
            Log.e(e.getMessage());
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private @Nonnull ArrayList<PuzzleSet> extractFromRest(@Nonnull RestPuzzleSet.RestPuzzleSetsHolder data)
    {
        ArrayList<PuzzleSet> sets = new ArrayList<PuzzleSet>(data.getPuzzleSets().size());
        for (RestPuzzleSet restPuzzleSet : data.getPuzzleSets())
        {
            PuzzleSet set = new PuzzleSet(0,restPuzzleSet.getId(), restPuzzleSet.getName(), restPuzzleSet.isBought(),
                    restPuzzleSet.getType(), restPuzzleSet.getMonth(), restPuzzleSet.getYear(),
                    restPuzzleSet.getCreatedAt(), restPuzzleSet.isPublished(), restPuzzleSet.getPuzzles());
            sets.add(set);
        }
        return sets;
    }

    private @Nonnull ArrayList<PuzzleTotalSet> extractFromTotalRest(@Nonnull RestPuzzleTotalSet.RestPuzzleSetsHolder data)
    {
        ArrayList<PuzzleTotalSet> sets = new ArrayList<PuzzleTotalSet>(data.getPuzzleSets().size());
        for (RestPuzzleTotalSet restPuzzleSet : data.getPuzzleSets())
        {
            @Nullable HashSet<String> solvedQuestionsIdSet = null;
            @Nonnull List<Puzzle> puzzles = new ArrayList<Puzzle>();
            for(RestPuzzle restPuzzle : restPuzzleSet.getPuzzles())
            {
                int score = 0;
                @Nullable List<RestPuzzleQuestion> questionList = restPuzzle.getQuestions();
                List<PuzzleQuestion> questions = new ArrayList<PuzzleQuestion>(questionList.size());
                for (RestPuzzleQuestion restQ : questionList)
                {
                    PuzzleQuestion q = new PuzzleQuestion(0, 0, restQ.getColumn(), restQ.getRow(), restQ.getQuestionText(),
                            restQ.getAnswer(), restQ.getAnswerPosition(), false);
                    RestPuzzleUserData.checkQuestionOnAnswered(q, solvedQuestionsIdSet);
                    questions.add(q);
                }
                puzzles.add(new Puzzle(0, 0, restPuzzle.getPuzzleId(), restPuzzle.getName(), restPuzzle.getIssuedAt(),
                    restPuzzle.getBaseScore(), restPuzzle.getTimeGiven(),
                    restPuzzle.getTimeGiven(), score,
                    false, questions));
            }

            PuzzleTotalSet set = new PuzzleTotalSet(0,restPuzzleSet.getId(), restPuzzleSet.getName(), restPuzzleSet.isBought(),
                    restPuzzleSet.getType(), restPuzzleSet.getMonth(), restPuzzleSet.getYear(),
                    restPuzzleSet.getCreatedAt(), restPuzzleSet.isPublished(), puzzles);
            sets.add(set);
        }
        return sets;
    }

    private static @Nonnull Bundle packToBundle(@Nonnull ArrayList<PuzzleSet> sets, int hintsCount, @Nonnull HashMap<String,List<Puzzle> > mapPuzzles, int status)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_PUZZLE_SETS, sets);
        bundle.putInt(BF_STATUS_CODE, status);
        bundle.putInt(BF_HINTS_COUNT, hintsCount);
        bundle.putSerializable(BF_PUZZLES_AT_SET, mapPuzzles);
        return  bundle;
    }

    public static @Nullable Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        List<PuzzleSet> sets = env.dbw.getPuzzleSets();
        int hintsCount = env.dbw.getUserHintsCount();
        List<Puzzle> puzzles = null;
        @Nonnull HashMap<String,List<Puzzle> > mapPuzzles = new HashMap<String, List<Puzzle>>();
        for(PuzzleSet puzzleSet : sets)
        {
            puzzles = env.dbw.getPuzzlesBySetId(puzzleSet.id);
            mapPuzzles.put(puzzleSet.serverId,puzzles);
        }
        return packToBundle(new ArrayList<PuzzleSet>(sets), hintsCount, mapPuzzles, RestParams.SC_SUCCESS);
    }
}
