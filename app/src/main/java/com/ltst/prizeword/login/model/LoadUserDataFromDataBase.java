package com.ltst.prizeword.login.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.db.SQLiteHelper;
import com.ltst.prizeword.rest.RestUserData;

import java.util.ArrayList;
import java.util.List;

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
    public static final @Nonnull String BF_PROVIDERS = "LoadUserDataFromDataBase.userProviders";
    private static final @Nonnull String BF_STATUS = "LoadUserDataFromDataBase.status";

    private static final int STATUS_LOAD_PROVIDERS = 1;
    private static final int STATUS_LOAD_USERIMAGE = 2;

    public static @Nonnull Intent createIntentInsertImage(int user_id, @Nullable byte[] buffer)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_USER_ID, user_id);
        intent.putExtra(BF_IMAGE_DATA, buffer);
        return intent;
    }

    public static @Nonnull Intent createIntentLoadingProviders(long user_id)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_STATUS, user_id);
        intent.putExtra(BF_STATUS, STATUS_LOAD_PROVIDERS);
        return intent;
    }

    public static @Nonnull Intent createIntentLoadingImage(long user_id)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_USER_ID, user_id);
        intent.putExtra(BF_STATUS, STATUS_LOAD_USERIMAGE);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env) {
        Bundle extras = env.extras;
        if(extras == null)
            return null;

        long user_id = extras.getLong(BF_USER_ID);
        int status = extras.getInt(BF_STATUS);
        if(user_id >= 0){
            switch (status){
                case STATUS_LOAD_PROVIDERS:
                    return  getUserProviderFromDB(env, user_id);
                case STATUS_LOAD_USERIMAGE:
                    return  getUserImageFromDB(env, user_id);
                default:
                    break;
            }
        }
        return  null;
    }

    public static @Nullable Bundle getUserProviderFromDB(@Nonnull DbService.DbTaskEnv env, long user_id)
    {
        Bundle bundle = new Bundle();
        @Nullable ArrayList<UserProvider> data = env.dbw.getUserProvidersByUserId(SQLiteHelper.ID_USER);
        bundle.putParcelableArrayList(BF_PROVIDERS, data);
        return bundle;
    }

    public static @Nullable Bundle getUserImageFromDB(@Nonnull DbService.DbTaskEnv env, long user_id)
    {
        Bundle bundle = new Bundle();
        @Nullable UserImage data = env.dbw.getUserImage(SQLiteHelper.ID_USER);
        bundle.putByteArray(BF_IMAGE_DATA, data.image);
        return bundle;
    }
}
