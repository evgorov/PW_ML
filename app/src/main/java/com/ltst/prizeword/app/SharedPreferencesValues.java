package com.ltst.prizeword.app;

import android.content.Context;

import javax.annotation.Nonnull;

public class SharedPreferencesValues
{
    public static final @Nonnull String SP_SESSION_KEY = "sessionKey";

    public static final @Nonnull String NO_SESSION_KEY = "NO_SESSION_KEY";

    public static String getSessionKey(@Nonnull Context context)
    {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return  helper.getString(SP_SESSION_KEY, NO_SESSION_KEY);
    }
}
