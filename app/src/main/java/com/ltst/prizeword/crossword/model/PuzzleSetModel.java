package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleSetModel implements IPuzzleSetModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nullable List<PuzzleSet> mPuzzleSetList;

    public PuzzleSetModel(@Nonnull IBcConnector bcConnector, @Nonnull String sessionKey)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
    }

    @Override
    public void updateDataByDb(@Nonnull IListenerVoid handler)
    {
        mPuzzleSetsDbUpdater.update(handler);
    }

    @Override
    public @Nonnull List<PuzzleSet> getPuzzleSets()
    {
        if (mPuzzleSetList != null)
        {
            return mPuzzleSetList;
        }
        return new ArrayList<PuzzleSet>();
    }

    @Override
    public void updateDataByInternet(@Nonnull IListenerVoid handler)
    {
        mPuzzleSetsInternetUpdater.update(handler);
    }

    // ==== updaters =====================================

    private Updater mPuzzleSetsInternetUpdater = new Updater()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromInternet.createIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadPuzzleSetsFromInternet.class;
        }
    };

    private Updater mPuzzleSetsDbUpdater = new Updater()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromDatabase.createIntent();
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadPuzzleSetsFromDatabase.class;
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
            mPuzzleSetList = LoadPuzzleSetsFromInternet.extractFromBundle(result);
        }
    }


    public enum PuzzleSetType
    {
        BRILLIANT,
        GOLD,
        SILVER,
        FREE
    }

    public static final @Nonnull String BRILLIANT = "brilliant";
    public static final @Nonnull String GOLD = "gold";
    public static final @Nonnull String SILVER = "silver";
    public static final @Nonnull String FREE = "free";

    public static @Nullable PuzzleSetType getPuzzleTypeByString(@Nonnull String type)
    {
        if (type.startsWith(FREE))
            return PuzzleSetType.FREE;
        if (type.startsWith(GOLD))
            return PuzzleSetType.GOLD;
        if (type.startsWith(SILVER))
            return PuzzleSetType.SILVER;
        if (type.startsWith(BRILLIANT))
            return PuzzleSetType.BRILLIANT;
        return null;
    }
}
