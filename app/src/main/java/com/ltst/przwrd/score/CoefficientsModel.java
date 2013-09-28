package com.ltst.przwrd.score;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.crossword.model.PuzzleSetModel;
import com.ltst.przwrd.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoefficientsModel implements ICoefficientsModel
{
    private @Nonnull String mSessionKey;
    private @Nonnull IBcConnector mBcConnector;
    private @Nullable Coefficients mCoefficients;
    private boolean mIsDestroyed;

    public CoefficientsModel(@Nonnull String sessionKey, @Nonnull IBcConnector bcConnector)
    {
        mSessionKey = sessionKey;
        mBcConnector = bcConnector;
    }

    @Override
    public void updateFromDatabase()
    {
        if(mIsDestroyed)
            return;
        mDbUpdater.update(null);
    }

    @Override
    public void updateFromInternet(@Nullable IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mInternetUpdater.update(handler);
    }

    @Override
    public void close()
    {
        Log.i("CoefficientsModel.destroy() begin"); //$NON-NLS-1$
        if(mIsDestroyed)
        {
            Log.w("CoefficientsModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mDbUpdater.close();
        mInternetUpdater.close();

        mIsDestroyed = true;
        Log.i("CoefficientsModel.destroy() end"); //$NON-NLS-1$
    }

    @Nullable
    @Override
    public Coefficients getCoefficients()
    {
        return mCoefficients;
    }

    @Override
    public int getBaseScore(PuzzleSetModel.PuzzleSetType setType)
    {
        if (mCoefficients == null)
        {
            return 0;
        }
        int baseScore = 0;
        if (setType == PuzzleSetModel.PuzzleSetType.FREE)
            baseScore = mCoefficients.freeBaseScore;
        if (setType == PuzzleSetModel.PuzzleSetType.SILVER)
            baseScore = mCoefficients.silver1BaseScore;
        if (setType == PuzzleSetModel.PuzzleSetType.SILVER2)
            baseScore = mCoefficients.silver2BaseScore;
        if (setType == PuzzleSetModel.PuzzleSetType.GOLD)
            baseScore = mCoefficients.goldBaseScore;
        if (setType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
            baseScore = mCoefficients.brilliantBaseScore;

        if(baseScore == 0)
            return 0;

        return baseScore;
    }

    @Override
    public int getBonusScore(int timeSpent, int timeGiven)
    {
        if (mCoefficients == null)
        {
            return 0;
        }
        return mCoefficients.timeBonus * (timeGiven - timeSpent)/timeSpent;
    }

    private Updater mDbUpdater = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadCoefficientsFromDbTask.createIntent();
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadCoefficientsFromDbTask.class;
        }
    };

    private Updater mInternetUpdater = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadCoefficientsFromInternetTask.createIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadCoefficientsFromInternetTask.class;
        }
    };

    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
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
            if (result == null)
            {
                return;
            }

            mCoefficients = result.getParcelable(LoadCoefficientsFromInternetTask.BF_COEFFICIENTS);
        }
    }
}
