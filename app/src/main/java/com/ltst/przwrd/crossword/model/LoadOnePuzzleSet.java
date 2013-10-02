package com.ltst.przwrd.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.rest.RestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 02.10.13.
 */
public class LoadOnePuzzleSet implements DbService.IDbTask {

    public static final @Nonnull String BF_PUZZLESET_SERVER_ID = "LoadOnePuzzleSet.puzzleSetServerId";
    public static final @Nonnull String BF_PUZZLE_SETS              = "LoadPuzzleSetsFromInternet.puzzleSets";

    public static final
    @Nonnull
    Intent createIntent(@Nonnull String puzzleSetServerId)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_PUZZLESET_SERVER_ID, puzzleSetServerId);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env) {

        @Nonnull String puzzleSetServerId = env.extras.getString(BF_PUZZLESET_SERVER_ID);
        PuzzleSet puzzleSet = env.dbw.getPuzzleSetByServerId(puzzleSetServerId);
        return packToBundle(puzzleSet, RestParams.SC_SUCCESS);
    }

    private static
    @Nonnull
    Bundle packToBundle(@Nonnull PuzzleSet puzzleSet, int status)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BF_PUZZLE_SETS, puzzleSet);
        return bundle;
    }


}
