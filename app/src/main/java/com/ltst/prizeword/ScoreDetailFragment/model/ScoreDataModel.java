package com.ltst.prizeword.ScoreDetailFragment.model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.dowloading.BgImageDownloader;
import com.ltst.prizeword.dowloading.LoadImageTask;

import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ArraySlowSource;
import org.omich.velo.lists.ILoadingQueue;
import org.omich.velo.lists.ISlowSource;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScoreDataModel implements IScoreDatalModel
{
    private boolean mIsDestroyed;
    private @Nonnull String mSessionKey;
    private @Nonnull Source mSource;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull BgImageDownloader mDownloader;
    private @Nonnull Updater mInvitedFriendsUpdater;

    public ScoreDataModel(@Nonnull String mSessionKey, @Nonnull IBcConnector mBcConnector)
    {
        this.mSessionKey = mSessionKey;
        BgImageDownloader downloader = new BgImageDownloader.SimpleBgImageDownloader(mBcConnector, LoadImageTask.class);
        this.mDownloader = downloader;
        this.mSource = new Source(new ArrayList<ISlowSource.Item<ScoreFriendsData, Bitmap>>(), downloader);
        this.mBcConnector = mBcConnector;
        this.mInvitedFriendsUpdater = new Updater();
    }

    public void close()
    {
        Log.i("UsersListModel.destroy() begin"); //$NON-NLS-1$
        if (mIsDestroyed)
        {
            Log.w("UsersListModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mInvitedFriendsUpdater.close();

        mSource.close();
        mDownloader.destroy();

        mIsDestroyed = true;
        Log.i("UsersListModel.destroy() end"); //$NON-NLS-1$
    }

    public void pauseLoading()
    {
        mSource.pauseResource(false);
    }

    public void resumeLoading()
    {
        mSource.resumeResource();
    }

    @Override public void updateDataByDb(@Nonnull IListenerVoid handler)
    {

    }

    @Override public void updateDataByInternet(@Nonnull IListenerVoid handler)
    {
        mInvitedFriendsUpdater.update(handler);
    }

    @Nonnull @Override public List<PuzzleSet> getPuzzleSets()
    {
        return null;
    }

    @Nonnull @Override public HashMap<String, List<Puzzle>> getPuzzlesSet()
    {
        return null;
    }

    @Nonnull @Override public ISlowSource<ScoreFriendsData, Bitmap> getSource()
    {
        return mSource;
    }

    private class Updater extends ModelUpdater<IBcTask.BcTaskEnv>
    {
        //==== ModelUpdater ===================================================
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadInvitedFriendsFromInternetTask.createIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return LoadInvitedFriendsFromInternetTask.class;
        }

        @Override
        protected @Nonnull IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Override
        protected @Nonnull Class<BcService> getServiceClass()
        {
            return BcService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            Source source = mSource;
            BgImageDownloader downloader = mDownloader;
            if (mIsDestroyed)
                return;

            List<ISlowSource.Item<ScoreFriendsData, Bitmap>> list = LoadInvitedFriendsFromInternetTask.extractFriendsList(result);

            if (list != null)
            {
                source.close();
                mSource = new Source(list, downloader);
            }
        }
    }

    /**
     * Передаётся в адаптер, отвечает за поочерёдную подгрузку картинок.
     */
    private static class Source extends ArraySlowSource<ScoreFriendsData, Bitmap, String>
    {
        private Source(@Nonnull Collection<Item<ScoreFriendsData, Bitmap>> items, @Nonnull ILoadingQueue.IBgDownloader<String, Bitmap> bgDownloader)
        {
            super(items, bgDownloader);
        }

        @Override
        protected long getItemId(ScoreFriendsData friend, int position)
        {
            return 0;
        }

        @Nullable
        @Override
        protected String getSlowParam(ScoreFriendsData friend, int position)
        {
            return friend.userpic;
        }

        @Override
        protected void destroyQuickItem(@Nonnull ScoreFriendsData friend)
        {

        }

        @Override
        protected void destroySlowItem(@Nonnull Bitmap slowItem)
        {
            slowItem.recycle();
        }
    }
}
