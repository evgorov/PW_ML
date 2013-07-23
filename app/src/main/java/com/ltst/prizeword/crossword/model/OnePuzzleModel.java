package com.ltst.prizeword.crossword.model;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

public class OnePuzzleModel implements IOnePuzzleModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nonnull String mPuzzleServerId;

    public OnePuzzleModel(@Nonnull IBcConnector bcConnector,
                          @Nonnull String sessionKey,
                          @Nonnull String puzzleServerId)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mPuzzleServerId = puzzleServerId;
    }


    @Override
    public void updateDataByDb(@Nonnull IListenerVoid handler)
    {

    }

    @Override
    public void updateDataByInternet(@Nonnull IListenerVoid handler)
    {

    }

}
