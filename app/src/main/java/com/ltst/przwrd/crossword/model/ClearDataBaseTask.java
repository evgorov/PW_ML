package com.ltst.przwrd.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 27.08.13.
 */
public class ClearDataBaseTask implements DbService.IDbTask {

    static final public @Nonnull Intent createIntent()
    {
        Intent intent = new Intent();
        return intent;
    }


    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env) {
        env.dbw.clearDb();
        return null;
    }

}
