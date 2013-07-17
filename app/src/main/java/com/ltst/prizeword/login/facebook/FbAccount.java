package com.ltst.prizeword.login.facebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 17.07.13.
 */
public class FbAccount {

    public static final @Nonnull
    String LOG_TAG = "facebook";

    public static final @Nonnull String ACCOUNT_ACCESS_TOKEN = "access_token";
    public static final @Nonnull String ACCOUNT_USER_ID = "user_id";

    public String access_token;
    public long user_id;

    public void save(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ACCOUNT_ACCESS_TOKEN+FbAccount.class.getName(), access_token);
        editor.putLong(ACCOUNT_USER_ID+FbAccount.class.getName(), user_id);
        editor.commit();
    }

    public void restore(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        access_token=prefs.getString(ACCOUNT_ACCESS_TOKEN+FbAccount.class.getName(), null);
        user_id=prefs.getLong(ACCOUNT_USER_ID+FbAccount.class.getName(), 0);
    }
}
