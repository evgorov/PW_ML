package com.ltst.prizeword.login.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 01.08.13.
 */
public class LoadUserDataFromDataBase implements DbService.IDbTask {

    public static final @Nonnull String BF_SESSION_KEY = "LoadUserDataFromDataBase.sessionKey";
    public static final @Nonnull String BF_USER_DATA = "LoadUserDataFromDataBase.userData";
    public static final @Nonnull String BF_IMAGE_DATA = "LoadUserDataFromDataBase.imageData";
    public static final @Nonnull String BF_USER_ID = "LoadUserDataFromDataBase.userId";

    public static @Nonnull Intent createIntentInsertImage(int user_id, @Nullable byte[] buffer)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_USER_ID, user_id);
        intent.putExtra(BF_IMAGE_DATA, buffer);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv dbTaskEnv) {
        return null;
    }
}
