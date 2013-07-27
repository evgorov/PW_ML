package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadOnePuzzleFromDatabase extends LoadOnePuzzleFromInternet
{
    public static final
    @Nonnull
    Intent createIntent(@Nonnull String puzzleId)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_PUZZLE_ID, puzzleId);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        @Nonnull String puzzleId = env.extras.getString(BF_PUZZLE_ID);
        return getFromDatabase(env, puzzleId);
    }
}
