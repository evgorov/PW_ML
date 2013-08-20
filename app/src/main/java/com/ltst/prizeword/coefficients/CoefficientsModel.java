package com.ltst.prizeword.coefficients;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoefficientsModel implements ICoefficientsModel
{
    private @Nonnull String mSessionKey;
    private @Nonnull IBcConnector mBcConnector;
    private @Nullable Coefficients mCoefficients;

    public CoefficientsModel(@Nonnull String sessionKey, @Nonnull IBcConnector bcConnector)
    {
        mSessionKey = sessionKey;
        mBcConnector = bcConnector;
    }

    @Override
    public void updateFromDatabase()
    {
        mDbUpdater.update(null);
    }

    @Override
    public void updateFromInternet()
    {
        mInternetUpdater.update(null);
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
