package com.ltst.prizeword.coefficients;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadCoefficientsFromDbTask extends LoadCoefficientsFromInternetTask
{
    public static final Intent createIntent()
    {
        return new Intent();
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        return getFromDatabase(env);
    }
}
