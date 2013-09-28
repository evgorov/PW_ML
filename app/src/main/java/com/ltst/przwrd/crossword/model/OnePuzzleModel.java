package com.ltst.przwrd.crossword.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.rest.RestParams;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OnePuzzleModel implements IOnePuzzleModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nonnull String mPuzzleServerId;

    private boolean mIsDestroyed;
    private @Nullable Puzzle mPuzzle;
    private long mSetId;
    private @Nonnull Context mContext;

    public OnePuzzleModel(@Nonnull Context context,
                        @Nonnull IBcConnector bcConnector,
                          @Nonnull String sessionKey,
                          @Nonnull String puzzleServerId,
                          long setId)
    {
        mContext = context;
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mPuzzleServerId = puzzleServerId;
        mSetId = setId;
    }

    @Nullable
    @Override
    public Puzzle getPuzzle()
    {
        return mPuzzle;
    }

    @Override
    public void close()
    {
        Log.i("OnePuzzleModel.destroy() begin"); //$NON-NLS-1$
        if(mIsDestroyed)
        {
            Log.w("OnePuzzleModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mPuzzleDbUpdater.close();
        mPuzzleInternetUpdater.close();
        mPuzzleUserDataUpdater.close();
        mSetQuestionAnsweredUpdater.close();

        mIsDestroyed = true;
        Log.i("OnePuzzleModel.destroy() end"); //$NON-NLS-1$
    }

    @Override
    public void updateDataByDb(@Nonnull IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mPuzzleDbUpdater.update(handler);
    }

    @Override
    public void updateDataByInternet(@Nonnull IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mPuzzleInternetUpdater.update(handler);
    }

    @Override
    public void setQuestionAnswered(@Nonnull PuzzleQuestion q, boolean answered)
    {
        if(mIsDestroyed)
            return;
        mSetQuestionAnsweredUpdater.setIntent(SetQuestionAnsweredTask.createSingleIntent(q.id, answered));
        mSetQuestionAnsweredUpdater.update(null);
    }

    @Override
    public void putAnsweredQuestionsToDb(@Nullable IListenerVoid handler)
    {
        if(mIsDestroyed || mPuzzle == null)
            return;
        if (mPuzzle.questions == null)
            return;

        List<Long> answeredQuestionIdList = new ArrayList<Long>();
        for (PuzzleQuestion question : mPuzzle.questions)
        {
            if(question.isAnswered)
            {
                answeredQuestionIdList.add(question.id);
            }
        }
        long[] idArray = new long[answeredQuestionIdList.size()];
        int index = 0;
        for (Long id : answeredQuestionIdList)
        {
            idArray[index] = id;
            index ++;
        }
        mSetQuestionAnsweredUpdater.setIntent(SetQuestionAnsweredTask.createMultipleIntent(idArray, true));
        mSetQuestionAnsweredUpdater.update(handler);
    }

    @Override
    public void updatePuzzleUserData()
    {
        if (mIsDestroyed)
            return;
        mPuzzleUserDataUpdater.update(null);
    }

    // ==== updaters ==================================================

    private SetQuestionAnsweredUpdater mSetQuestionAnsweredUpdater = new SetQuestionAnsweredUpdater();
    private PuzzleUserDataUpdater mPuzzleUserDataUpdater = new PuzzleUserDataUpdater();

    private Updater mPuzzleInternetUpdater = new Updater()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadOnePuzzleFromInternet.createIntent(mSessionKey, mPuzzleServerId, mSetId);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadOnePuzzleFromInternet.class;
        }
    };

    private Updater mPuzzleDbUpdater = new Updater()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadOnePuzzleFromDatabase.createIntent(mPuzzleServerId);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadOnePuzzleFromDatabase.class;
        }
    };

    private class SetQuestionAnsweredUpdater extends ModelUpdater<DbService.DbTaskEnv>
    {
        private @Nullable Intent mIntent;

        public void setIntent(@Nullable Intent intent)
        {
            mIntent = intent;
        }

        @Nullable
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return SetQuestionAnsweredTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            // ничего делать не надо
        }
    }

    private class PuzzleUserDataUpdater extends ModelUpdater<IBcTask.BcTaskEnv>
    {
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
            if (mPuzzle == null)
            {
                return null;
            }

            ArrayList<PuzzleQuestion> questions = (ArrayList<PuzzleQuestion>) mPuzzle.questions;
            return UpdatePuzzleUserDataOnServerTask.createIntent(mSessionKey,
                    mPuzzleServerId,
                    mPuzzle.timeLeft,
                    mPuzzle.score,
                    questions);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return UpdatePuzzleUserDataOnServerTask.class;
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
            if (result == null)
            {
                return;
            }

            int status = result.getInt(UpdatePuzzleUserDataOnServerTask.BF_STATUS);
            if(status == RestParams.SC_ERROR)
            {
                PuzzleUserDataSynchronizer syncer = new PuzzleUserDataSynchronizer(mContext);
                syncer.addPuzzleId(mPuzzleServerId);
            }
        }
    }

    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }
            int status = result.getInt(LoadOnePuzzleFromInternet.BF_STATUS_CODE);

            if(status == RestParams.SC_SUCCESS)
            {
                mPuzzle = result.getParcelable(LoadOnePuzzleFromInternet.BF_PUZZLE);
            }

        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }
    }

}
