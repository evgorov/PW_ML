package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.rest.RestPuzzleSet;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadPuzzleSetsFromInternet implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadPuzzleSetsFromInternet.sessionKey";
    public static final @Nonnull String BF_PUZZLE_SETS = "LoadPuzzleSetsFromInternet.puzzleSets";
    public static final @Nonnull String BF_STATUS_CODE = "LoadPuzzleSetsFromInternet.statusCode";

    public final @Nonnull Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }

    public final @Nullable List<PuzzleSet> extractFromBundle(@Nullable Bundle bundle)
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

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            RestPuzzleSet.RestPuzzleSetsHolder data = loadPuzzleSets(sessionKey);
            ArrayList<PuzzleSet> sets = extractFromRest(data);
            env.dbw.putPuzzleSetList(sets);
            return packToBundle(sets, data.getHttpStatus().value());
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

    private @Nonnull ArrayList<PuzzleSet> extractFromRest(RestPuzzleSet.RestPuzzleSetsHolder data)
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

    private static @Nonnull Bundle packToBundle(@Nonnull ArrayList<PuzzleSet> sets, int status)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_PUZZLE_SETS, sets);
        bundle.putInt(BF_STATUS_CODE, status);
        return  bundle;
    }

    public static @Nullable Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        List<PuzzleSet> sets = env.dbw.getPuzzleSets();
        return packToBundle(new ArrayList<PuzzleSet>(sets), RestParams.SC_SUCCESS);
    }
}
