package com.ltst.przwrd.crossword.sharing;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VkShareTask implements IBcTask
{
    public static final @Nonnull String BF_SESSION_KEY = "VkShareTask.sessionKey";
    public static final @Nonnull String BF_MESSAGE = "VkShareTask.message";

    public static final @Nonnull Intent createIntent(@Nonnull String sessionKey, @Nonnull String message)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_MESSAGE, message);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull BcTaskEnv env)
    {
        Bundle extras = env.extras;
        if (extras == null)
        {
            return null;
        }

        @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
        @Nullable String message = extras.getString(BF_MESSAGE);
        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            if (sessionKey != null && message != null)
            {
                IRestClient client = RestClient.create(env.context);
                client.shareMessageToVk(sessionKey, message);
            }

        }
        return null;
    }
}
