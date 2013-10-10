package com.ltst.przwrd.crossword.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.app.SharedPreferencesHelper;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;
import com.ltst.przwrd.rest.RestParams;
import com.ltst.przwrd.rest.RestPuzzle;
import com.ltst.przwrd.rest.RestPuzzleQuestion;
import com.ltst.przwrd.rest.RestPuzzleSet;
import com.ltst.przwrd.rest.RestPuzzleTotalSet;
import com.ltst.przwrd.rest.RestPuzzleUserData;
import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import org.omich.velo.log.Log;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadPuzzleSetsFromInternet implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY              = "LoadPuzzleSetsFromInternet.sessionKey";
    public static final @Nonnull String BF_PUZZLE_SETS              = "LoadPuzzleSetsFromInternet.puzzleSets";
    public static final @Nonnull String BF_ONE_PUZZLE_SET_SERVER_ID = "LoadPuzzleSetsFromInternet.onePuzzleSetServerId";
    public static final @Nonnull String BF_PUZZLES_AT_SET           = "LoadPuzzleSetsFromInternet.puzzles";
    public static final @Nonnull String BF_STATUS_CODE              = "LoadPuzzleSetsFromInternet.statusCode";
    public static final @Nonnull String BF_VOLUME_PUZZLE            = "LoadPuzzleSetsFromInternet.volumePuzzle";

    private static final @Nonnull String BF_SET_SERVER_ID           = "LoadPuzzleSetsFromInternet.setServerId";
    private static final @Nonnull String BF_RECEIPT_DATA            = "LoadPuzzleSetsFromInternet.receiptData";
    private static final @Nonnull String BF_SIGNATURE               = "LoadPuzzleSetsFromInternet.signature";

    private static final @Nonnull String VOLUME_SHORT    = "short";
    private static final @Nonnull String VOLUME_LONG     = "long";
    private static final @Nonnull String VOLUME_SORT     = "sort";
    private static final @Nonnull String VOLUME_CURR     = "current";
    private static final @Nonnull String VOLUME_BUY      = "buy";
    private static final @Nonnull String VOLUME_ONE      = "one";
    private static final @Nonnull String VOLUME_SYNC     = "sync";

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
    @Nonnull
    Intent createOneSetIntent(@Nonnull String sessionKey, @Nonnull String puzzleSetServerId)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_ONE_PUZZLE_SET_SERVER_ID, puzzleSetServerId);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_ONE);
        return intent;
    }

    public static final @Nonnull Intent createBuyCrosswordSetIntent(@Nonnull String sessionKey,
                                                                    @Nonnull String setServerId,
                                                                    @Nonnull String receiptData,
                                                                    @Nonnull String signature){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_SET_SERVER_ID, setServerId);
        intent.putExtra(BF_RECEIPT_DATA, receiptData);
        intent.putExtra(BF_SIGNATURE, signature);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_BUY);
        return intent;
    }

    public static final
    @Nonnull
    Intent createSyncIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_VOLUME_PUZZLE, VOLUME_SYNC);
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
        }
        else
        {
            @Nullable String sessionKey = env.extras.getString(BF_SESSION_KEY);
            @Nullable String volumePuzzle = env.extras.getString(BF_VOLUME_PUZZLE);
            if (volumePuzzle == null || sessionKey == null)
            {
                return null;
            }

            if (volumePuzzle.equals(VOLUME_SHORT))
            {
                long currentTime = SharedPreferencesHelper.getInstance(env.context).getLong(SharedPreferencesValues.SP_CURRENT_DATE, 0);
                Calendar calnow = Calendar.getInstance();
                calnow.setTimeInMillis(currentTime);
                calnow.add(Calendar.MONTH,1);

                int app_release_year = Integer.valueOf(env.context.getResources().getString(R.string.app_release_year));
                int app_release_month = Integer.valueOf(env.context.getResources().getString(R.string.app_release_month));
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.MONTH, app_release_month);
                cal.set(Calendar.YEAR, app_release_year);

                while(calnow.get(Calendar.YEAR) >= cal.get(Calendar.YEAR) && calnow.get(Calendar.MONTH) >= cal.get(Calendar.MONTH))
                {
                    if(env.ci.isCancelled())
                        return null;
                    int year = calnow.get(Calendar.YEAR);
                    int month = calnow.get(Calendar.MONTH);
                    getFromServerShortSet(sessionKey, year, month, env);
                    calnow.add(Calendar.MONTH,-1);
                }
                return getFromDatabase(env);
            }
            else if (volumePuzzle.equals(VOLUME_LONG))
            {
                long currentTime = SharedPreferencesHelper.getInstance(env.context).getLong(SharedPreferencesValues.SP_CURRENT_DATE, 0);
                Calendar calnow = Calendar.getInstance();
                calnow.setTimeInMillis(currentTime);
                calnow.add(Calendar.MONTH,1);

                int app_release_year = Integer.valueOf(env.context.getResources().getString(R.string.app_release_year));
                int app_release_month = Integer.valueOf(env.context.getResources().getString(R.string.app_release_month));
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.MONTH, app_release_month);
                cal.set(Calendar.YEAR, app_release_year);

                while(calnow.get(Calendar.YEAR) >= cal.get(Calendar.YEAR) && calnow.get(Calendar.MONTH) >= cal.get(Calendar.MONTH))
                {
                    if(env.ci.isCancelled())
                        return null;
                    int year = calnow.get(Calendar.YEAR);
                    int month = calnow.get(Calendar.MONTH);
                    getFromServerLongSet(sessionKey, year, month, env);
                    calnow.add(Calendar.MONTH,-1);
                }
                return getFromDatabase(env);
            }
            else if (volumePuzzle.equals(VOLUME_CURR))
            {

                long currentTime = SharedPreferencesHelper.getInstance(env.context).getLong(SharedPreferencesValues.SP_CURRENT_DATE, 0);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(currentTime);
                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);

                getFromServerLongSet(sessionKey, year, month, env);
                return getFromDatabase(env);
            }
            else if (volumePuzzle.equals(VOLUME_SORT))
            {
                return getFromDatabase(env);
            }
            else if (volumePuzzle.equals(VOLUME_ONE))
            {
                @Nonnull String puzzleOneSetServerId = env.extras.getString(BF_ONE_PUZZLE_SET_SERVER_ID);
                return getFromDatabase(puzzleOneSetServerId,env);
            }
            else if (volumePuzzle.equals(VOLUME_SYNC))
            {
                long currentTime = SharedPreferencesHelper.getInstance(env.context).getLong(SharedPreferencesValues.SP_CURRENT_DATE, 0);
                Calendar calnow = Calendar.getInstance();
                calnow.setTimeInMillis(currentTime);
                calnow.add(Calendar.MONTH,1);

                int app_release_year = Integer.valueOf(env.context.getResources().getString(R.string.app_release_year));
                int app_release_month = Integer.valueOf(env.context.getResources().getString(R.string.app_release_month));
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.MONTH, app_release_month);
                cal.set(Calendar.YEAR, app_release_year);

                while(calnow.get(Calendar.YEAR) >= cal.get(Calendar.YEAR) && calnow.get(Calendar.MONTH) >= cal.get(Calendar.MONTH))
                {
                    if(env.ci.isCancelled())
                        return null;
                    int year = calnow.get(Calendar.YEAR);
                    int month = calnow.get(Calendar.MONTH);
                    getFromServerLongSet(sessionKey, year, month, env);
                    calnow.add(Calendar.MONTH,-1);
                }
                return getFromDatabase(env);
//                long currentTime = SharedPreferencesHelper.getInstance(env.context).getLong(SharedPreferencesValues.SP_CURRENT_DATE, 0);
//                Calendar calnow = Calendar.getInstance();
//                calnow.setTimeInMillis(currentTime);
//                calnow.add(Calendar.MONTH,1);
//
//                int year = calnow.get(Calendar.YEAR);
//                int month = calnow.get(Calendar.MONTH);
//
//                // Обновляем короткий сет, а потом пробегаем пробегаем по пазлам, находящимся в базе, обновляя PuzzleUserData;
//                getFromServerShortSet(sessionKey, year, month, env);
//
//                List<PuzzleSet> sets = env.dbw.getPuzzleSetsByDate(year, month);
//                List<Puzzle> puzzles = null;
//
//                for (PuzzleSet puzzleSet : sets)
//                {
//                    if(!puzzleSet.isBought)
//                        continue;
//
//                    puzzles = env.dbw.getPuzzlesBySetId(puzzleSet.id);
//                    for(Puzzle puzzle : puzzles)
//                    {
//                        @Nullable RestPuzzleUserData.RestPuzzleUserDataHolder restPuzzleUserDataHolder = LoadOnePuzzleFromInternet.loadPuzzleUserData(env.context, sessionKey, puzzle.serverId);
//                        if(restPuzzleUserDataHolder == null)
//                            continue;
//
//                        RestPuzzleUserData restPuzzleUserData = restPuzzleUserDataHolder.getPuzzleUserData();
//
//                        @Nullable List<RestPuzzleUserData.RestSolvedQuestion> solvedQuestions = null;
//                        @Nullable HashSet<String> solvedQuestionsIdSet = null;
//                        if (restPuzzleUserData != null)
//                        {
//                            solvedQuestions = restPuzzleUserData.getSolvedQuestions();
//                            if (solvedQuestions != null)
//                            {
//                                solvedQuestionsIdSet = RestPuzzleUserData.prepareQuestionIdsSet(solvedQuestions);
//                            }
//                        }
//
//                        List<PuzzleQuestion> questions = new ArrayList<PuzzleQuestion>(puzzle.questions.size());
//                        for (PuzzleQuestion q : puzzle.questions)
//                        {
//                            RestPuzzleUserData.checkQuestionOnAnswered(q, solvedQuestionsIdSet);
//                            questions.add(q);
//                        }
//                        puzzle.questions = questions;
//                        if(puzzle.score == 0)
//                            puzzle.score = restPuzzleUserData.getScore();
//                        if(puzzle.timeLeft < restPuzzleUserData.getTimeLeft())
//                            puzzle.timeLeft = restPuzzleUserData.getTimeLeft();
//
//                        env.dbw.putPuzzle(puzzle);
//                    }
//                }
//                return getFromDatabase(year, month, env);
            }
            else if(volumePuzzle.equals(VOLUME_BUY))
            {
                @Nullable String setServerId =  env.extras.getString(BF_SET_SERVER_ID);
                @Nullable String receiptData =  env.extras.getString(BF_RECEIPT_DATA);
                @Nullable String signature =    env.extras.getString(BF_SIGNATURE);
                if (setServerId == Strings.EMPTY || receiptData == Strings.EMPTY || signature == Strings.EMPTY)
                {
                    return null;
                }

                RestPuzzleTotalSet.RestPuzzleOneSetHolder data = buyRestPuzzleSetFromInternet(env.context, sessionKey, setServerId, receiptData, signature);
                if(data != null)
                {
                    if(data.getHttpStatus() == HttpStatus.valueOf(RestParams.SC_SUCCESS))
                    {
                        RestPuzzleTotalSet set = data.getPuzzleSet();
                        if(set != null)
                        {
                            RestPuzzleTotalSet.RestPuzzleSetsHolder dataset = new RestPuzzleTotalSet.RestPuzzleSetsHolder();
                            dataset.setPuzzleSets(new ArrayList<RestPuzzleTotalSet>());
                            dataset.addPuzzleSet(set);
                            dataset.setHttpStatus(data.getHttpStatus());

                                @Nonnull List<PuzzleTotalSet> sets = extractFromTotalRest(env, sessionKey, dataset);
                                env.dbw.putPuzzleTotalSetList(sets);
                                return getFromDatabase(env);
                        }
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
        }
        return getFromDatabase(env);
    }

    private
    @Nullable
    RestPuzzleSet.RestPuzzleSetsHolder loadPuzzleSets(@Nonnull Context context, @Nonnull String sessionKey, int year, int month)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.getPublishedSets(sessionKey, year, month);
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

    private @Nullable
    RestPuzzleTotalSet.RestPuzzleOneSetHolder buyRestPuzzleSetFromInternet(@Nonnull Context context, @Nonnull String sessionKey, @Nonnull String setServerId, @Nonnull String receiptData, @Nonnull String signature)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.postBuySet(sessionKey, setServerId, receiptData, signature);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    @Nonnull
    public static ArrayList<PuzzleSet> extractFromRest(@Nonnull RestPuzzleSet.RestPuzzleSetsHolder data)
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
    @Nullable
    ArrayList<PuzzleTotalSet> extractFromTotalRest(@Nonnull DbService.DbTaskEnv env, @Nonnull String sessionKey, @Nonnull RestPuzzleTotalSet.RestPuzzleSetsHolder data)
    {
        @Nonnull Context context = env.context;
        @Nonnull ArrayList<PuzzleTotalSet> sets = new ArrayList<PuzzleTotalSet>(data.getPuzzleSets().size());
        @Nonnull List<RestPuzzleTotalSet> listRestPuzzleTotalSets = data.getPuzzleSets();
        for (RestPuzzleTotalSet restPuzzleSet : listRestPuzzleTotalSets)
        {
            @Nonnull List<Puzzle> puzzles = new ArrayList<Puzzle>();
            @Nonnull List<RestPuzzle> listRestPuzzles = restPuzzleSet.getPuzzles();
            for (RestPuzzle restPuzzle : listRestPuzzles)
            {
                if(env.ci.isCancelled()) return null;
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
    Bundle packToBundle(@Nonnull ArrayList<PuzzleSet> sets, @Nonnull HashMap<String, List<Puzzle>> mapPuzzles, int status)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_PUZZLE_SETS, sets);
        bundle.putInt(BF_STATUS_CODE, status);
        bundle.putSerializable(BF_PUZZLES_AT_SET, mapPuzzles);
        return bundle;
    }

    public static
    @Nullable
    Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        List<PuzzleSet> sets = env.dbw.getPuzzleSets();
        List<Puzzle> puzzles = null;
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = new HashMap<String, List<Puzzle>>();
        for (PuzzleSet puzzleSet : sets)
        {
            puzzles = env.dbw.getPuzzlesBySetId(puzzleSet.id);
            mapPuzzles.put(puzzleSet.serverId, puzzles);
        }
        return packToBundle(new ArrayList<PuzzleSet>(sets), mapPuzzles, RestParams.SC_SUCCESS);
    }

    public static
    @Nullable
    Bundle getFromDatabase(@Nonnull String puzzleOneSetServerId, @Nonnull DbService.DbTaskEnv env)
    {
        PuzzleSet set = env.dbw.getPuzzleSetByServerId(puzzleOneSetServerId);
        List<Puzzle> puzzles = null;
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = new HashMap<String, List<Puzzle>>();
        puzzles = env.dbw.getPuzzlesBySetId(set.id);
        mapPuzzles.put(set.serverId, puzzles);
        ArrayList<PuzzleSet> sets = new ArrayList<PuzzleSet>();
        sets.add(set);
        return packToBundle(sets, mapPuzzles, RestParams.SC_SUCCESS);
    }

    public static
    @Nullable
    Bundle getFromDatabase(int year, int month, @Nonnull DbService.DbTaskEnv env)
    {
        List<PuzzleSet> sets = env.dbw.getPuzzleSetsByDate(year, month);
        List<Puzzle> puzzles = null;
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = new HashMap<String, List<Puzzle>>();
        for (PuzzleSet puzzleSet : sets)
        {
            puzzles = env.dbw.getPuzzlesBySetId(puzzleSet.id);
            mapPuzzles.put(puzzleSet.serverId, puzzles);
        }
        return packToBundle(new ArrayList<PuzzleSet>(sets), mapPuzzles, RestParams.SC_SUCCESS);
    }

//    public static
//    @Nullable
//    Bundle getSolvedFromDatabase(@Nonnull DbService.DbTaskEnv env)
//    {
//        List<PuzzleSet> sets = env.dbw.getPuzzleSets();
//        List<Puzzle> puzzles = null;
//        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = new HashMap<String, List<Puzzle>>();
//        for (PuzzleSet puzzleSet : sets)
//        {
//            puzzles = env.dbw.getSolvedPuzzlesBySetId(puzzleSet.id);
//            mapPuzzles.put(puzzleSet.serverId, puzzles);
//        }
//        return packToBundle(new ArrayList<PuzzleSet>(sets), mapPuzzles, RestParams.SC_SUCCESS);
//    }

    private void getFromServerShortSet(@Nonnull String sessionKey, int year, int month, @Nonnull DbService.DbTaskEnv env)
    {
//        @Nullable RestPuzzleTotalSet.RestPuzzleSetsHolder data = loadPuzzleTotalSets(env.context, sessionKey, year, month);
//        if (data != null)
//        {
//            @Nullable List<PuzzleTotalSet> sets = extractFromTotalRest(env, sessionKey, data);
//            if(sets == null)
//                return;
//            env.dbw.putPuzzleTotalSetList(sets);
//        }
        @Nullable RestPuzzleSet.RestPuzzleSetsHolder data = loadPuzzleSets(env.context, sessionKey, year, month);
        if (data != null)
        {
            ArrayList<PuzzleSet> sets = extractFromRest(data);
            env.dbw.putPuzzleSetList(sets);
        }
    }

    private void getFromServerLongSet(@Nonnull String sessionKey, int year, int month, @Nonnull DbService.DbTaskEnv env)
    {
        @Nullable RestPuzzleTotalSet.RestPuzzleSetsHolder data = loadPuzzleTotalSets(env.context, sessionKey, year, month);
        if (data != null)
        {
            @Nullable List<PuzzleTotalSet> sets = extractFromTotalRest(env, sessionKey, data);
            if(sets == null)
                return;
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
