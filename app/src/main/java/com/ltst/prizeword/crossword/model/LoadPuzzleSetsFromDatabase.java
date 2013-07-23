package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadPuzzleSetsFromDatabase extends LoadPuzzleSetsFromInternet
{
    public static final @Nonnull Intent createIntent()
    {
        Intent intent = new Intent();
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        return getFromDatabase(env);
    }
}
