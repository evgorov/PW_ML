package com.ltst.prizeword.app;

import android.content.Context;

import javax.annotation.Nonnull;

public class SharedPreferencesValues
{
    public static final @Nonnull String SP_SESSION_KEY = "sessionKey";

    public static final @Nonnull String NO_SESSION_KEY = "NO_SESSION_KEY";

    public static final @Nonnull String SP_MUSIC_SWITCH = "musicSwitch";
    public static final @Nonnull String SP_SOUND_SWITCH = "soundSwitch";
    public static final @Nonnull String SP_CURRENT_DATE = "date";

    public static final @Nonnull String SP_HINTS_TO_CHANGE = "hintsToChange";

    public static String getSessionKey(@Nonnull Context context)
    {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return  helper.getString(SP_SESSION_KEY, NO_SESSION_KEY);
    }
    public static boolean getMusicSwitch(@Nonnull Context context){
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return  helper.getBoolean(SP_MUSIC_SWITCH, true);
    }
    public static boolean getSoundSwitch(@Nonnull Context context){
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return  helper.getBoolean(SP_SOUND_SWITCH,true);
    }
}
