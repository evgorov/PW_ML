package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 20.09.13.
 */
public class LoadHintsTask implements DbService.IDbTask {

    public static final @Nonnull String BF_SESSION_KEY              = "LoadHintsTask.sessionKey";

    public static final @Nonnull String BF_HINTS_COUNT              = "LoadHintsTask.hintsCount";

    public static final
    @Nonnull
    Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env) {

            @Nullable String sessionKey = env.extras.getString(BF_SESSION_KEY);
            return getFromDatabase(env);
    }

    public static
    @Nullable
    Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        int hintsCount = env.dbw.getUserHintsCount();
        return packToBundle(hintsCount);
    }

    private static
    @Nonnull
    Bundle packToBundle(int hintsCount)
    {
        Bundle bundle = new Bundle();
        bundle.putInt(BF_HINTS_COUNT, hintsCount);
        return bundle;
    }
}
