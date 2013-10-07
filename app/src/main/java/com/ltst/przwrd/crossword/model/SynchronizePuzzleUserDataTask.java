package com.ltst.przwrd.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;
import com.ltst.przwrd.rest.RestPuzzleUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SynchronizePuzzleUserDataTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "SynchronizePuzzleUserDataTask.sessionKey";
    public static final @Nonnull String BF_PUZZLES = "SynchronizePuzzleUserDataTask.puzzles";
    public static final @Nonnull String BF_PUZZLES_BUNDLE = "SynchronizePuzzleUserDataTask.puzzlesBundle";
    public static final @Nonnull String BF_NOT_SYNCED_PUZZLE_IDS = "SynchronizePuzzleUserDataTask.notSyncedPuzzleIds";

    public static final @Nonnull Intent createIntent(@Nonnull String sessionKey, @Nonnull ArrayList<Puzzle> puzzles)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_PUZZLES, puzzles);
        intent.putExtra(BF_PUZZLES_BUNDLE, bundle);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv bcTaskEnv)
    {
        Bundle extras = bcTaskEnv.extras;
        if (extras == null)
        {
            return null;
        }
        @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
        @Nullable Bundle puzzlesBundle = extras.getBundle(BF_PUZZLES_BUNDLE);
        if (puzzlesBundle == null)
        {
            return null;
        }
        @Nullable ArrayList<Puzzle> puzzles = puzzlesBundle.getParcelableArrayList(BF_PUZZLES);

        if(!BcTaskHelper.isNetworkAvailable(bcTaskEnv.context))
        {
            bcTaskEnv.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            bcTaskEnv.context.getString(R.string.msg_no_internet)));
            ArrayList<String> puzzlesNotSynced = new ArrayList<String>();
            for (Puzzle puzzle : puzzles)
            {
                puzzlesNotSynced.add(puzzle.serverId);
            }
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(BF_NOT_SYNCED_PUZZLE_IDS, puzzlesNotSynced);
            return bundle;
        }
        else
        if (sessionKey!= null && puzzles!= null)
        {
            ArrayList<String> puzzlesNotSynced = new ArrayList<String>();
            IRestClient client = RestClient.create(bcTaskEnv.context);
            for (Puzzle puzzle : puzzles)
            {
                @Nullable RestPuzzleUserData.RestPuzzleUserDataHolder restPuzzleUserDataHolder =
                        LoadOnePuzzleFromInternet.loadPuzzleUserData(bcTaskEnv.context, sessionKey, puzzle.serverId);
                if(restPuzzleUserDataHolder != null)
                {
                    RestPuzzleUserData restPuzzleUserData = restPuzzleUserDataHolder.getPuzzleUserData();
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

                    if (puzzle.questions != null)
                    {
                        List<PuzzleQuestion> questions = new ArrayList<PuzzleQuestion>(puzzle.questions.size());
                        for (PuzzleQuestion q : puzzle.questions)
                        {
                            RestPuzzleUserData.checkQuestionOnAnswered(q, solvedQuestionsIdSet);
                            questions.add(q);
                        }
                        puzzle.questions = questions;
                        bcTaskEnv.dbw.putPuzzle(puzzle);
                    }
                }

                String jsonPuzzleUserData = UpdatePuzzleUserDataOnServerTask.parseJsonUserData(puzzle);
                try
                {
                    client.putPuzzleUserData(sessionKey, puzzle.serverId, jsonPuzzleUserData);
                }
                catch (Throwable e)
                {
                    puzzlesNotSynced.add(puzzle.serverId);
                    Log.e(e.getMessage());
                    Log.i("Can not sync puzzle user data: " + puzzle.serverId); //$NON-NLS-1$
                }
            }
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(BF_NOT_SYNCED_PUZZLE_IDS, puzzlesNotSynced);
            return bundle;
        }
        return null;
    }
}
