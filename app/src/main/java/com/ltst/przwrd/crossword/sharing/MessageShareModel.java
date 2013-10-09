package com.ltst.przwrd.crossword.sharing;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.app.ModelUpdater;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageShareModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private boolean mIsDestroyed;
    private @Nonnull VkSharer mVkSharer;

    public MessageShareModel(@Nonnull IBcConnector bcConnector, @Nonnull String sessionKey)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mVkSharer = new VkSharer();
    }

    public void close()
    {
        if(mIsDestroyed)
            return;

        mVkSharer.close();
        mIsDestroyed = true;
    }

    public void shareMessageToVk(@Nonnull String message, @Nullable IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mVkSharer.setIntent(VkShareTask.createIntent(mSessionKey, message));
        mVkSharer.update(handler);
    }

    private class VkSharer extends ModelUpdater<IBcTask.BcTaskEnv>
    {
        private @Nullable Intent mIntent;

        public void setIntent(@Nullable Intent intent)
        {
            mIntent = intent;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nullable
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return VkShareTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<IBcTask.BcTaskEnv>> getServiceClass()
        {
            return BcService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            // nothing to do
        }
    }

}
