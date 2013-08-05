package com.ltst.prizeword.login.model;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.navigation.NavigationActivity;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 24.07.13.
 */
public class ResetUserDataOnServerTask implements DbService.IDbTask {

    public static final @Nonnull String BF_SESSION_KEY = "LoadUserDataFromInternetTask.sessionKey";
    public static final @Nonnull String BF_USER_DATA = LoadUserDataFromInternetTask.BF_USER_DATA;
    public static final @Nonnull String BF_USER_PIC = "ResetUserDataOnServerTask.userPic";
    public static final @Nonnull String BF_USER_NAME = "ResetUserDataOnServerTask.userName";

    public static @Nonnull
    Intent createUserPicIntent(@Nonnull String sessionKey, byte[] userPic)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_USER_PIC, userPic);
        return intent;
    }

    public static @Nonnull
    Intent createUserName(@Nonnull String sessionKey, @Nonnull String name)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_USER_NAME, name);
        return intent;
    }

    @Override
    public Bundle execute(DbService.DbTaskEnv env) {
        Bundle extras = env.extras;
        if(extras == null)
            return null;

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            String sessionKey = extras.getString(BF_SESSION_KEY);
            byte[] userPic = extras.getByteArray(BF_USER_PIC);
            String userName = extras.getString(BF_USER_NAME);

            IRestClient client = RestClient.create();
            RestUserData response = null;

            if(sessionKey != Strings.EMPTY && userPic != null){
                response = client.resetUserPic(sessionKey, userPic);
            }
            if(sessionKey != Strings.EMPTY && userName != null){
                response = client.resetUserName(sessionKey, userName);
            }

            if(response != null)
            {
                @Nonnull UserData userData = LoadUserDataFromInternetTask.parseUserData(response);
                @Nullable List<UserProvider> providerData = LoadUserDataFromInternetTask.parseProviderData(response);
                env.dbw.putUser(userData, providerData);

                if( userPic!=null && userData.previewUrl!=null )
                {
                    env.dbw.putUserImage(userPic);
                }

                return LoadUserDataFromInternetTask.getUserDataFromDatabase(env, userData.email);
            }
        }
        return null;
    }

}
