package com.ltst.przwrd.app;

import android.content.Context;

import javax.annotation.Nonnull;

public class SharedPreferencesValues {
    public static final @Nonnull String SP_SESSION_KEY = "sessionKey";
    public static final @Nonnull String SP_SESSION_KEY_2 = "sessionKey2";
    public static final @Nonnull String SP_FACEBOOK_TOKEN = "facebookToken";

    public static final @Nonnull String NO_SESSION_KEY = "NO_SESSION_KEY";

    public static final @Nonnull String SP_MUSIC_SWITCH = "musicSwitch";
    public static final @Nonnull String SP_SOUND_SWITCH = "soundSwitch";
    public static final @Nonnull String SP_CURRENT_DATE = "date";

    public static final @Nonnull String SP_HINTS_TO_CHANGE = "hintsToChange";

    public static final @Nonnull String SP_NOTIFICATIONS = "notifications";

    public static final @Nonnull String SP_FIRST_LAUNCH = "firstLaunch";

    public static String getSessionKey(@Nonnull Context context) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return helper.getString(SP_SESSION_KEY, NO_SESSION_KEY);
    }

    public static boolean getMusicSwitch(@Nonnull Context context) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return helper.getBoolean(SP_MUSIC_SWITCH, true);
    }

    public static boolean getSoundSwitch(@Nonnull Context context) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return helper.getBoolean(SP_SOUND_SWITCH, true);
    }

    public static boolean getNotificationsSwitch(@Nonnull Context context) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return helper.getBoolean(SP_NOTIFICATIONS, false);
    }

    public static void setNotifications(@Nonnull Context context, boolean value) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        helper.putBoolean(SP_NOTIFICATIONS, value).commit();
    }

    public static void setFacebookToken(@Nonnull Context context, @Nonnull String value) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        helper.putString(SP_FACEBOOK_TOKEN, value).commit();
    }

    public static String getFacebookToken(@Nonnull Context context) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return helper.getString(SP_FACEBOOK_TOKEN, null);
    }

    public static void setFirstLaunchFlag(@Nonnull Context context, boolean value) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        helper.putBoolean(SP_FIRST_LAUNCH, value).commit();
    }

    public static boolean getFirstLaunchFlag(@Nonnull Context context) {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context);
        return helper.getBoolean(SP_FIRST_LAUNCH, true);
    }

}
