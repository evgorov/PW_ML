package com.ltst.przwrd.app;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 08.10.13.
 */
public class BcConnectorActivity extends SherlockFragmentActivity implements IBcConnectorOwner{

    private @Nonnull IBcConnector mBcConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBcConnector = new BcConnector(this);
    }

    @Nonnull
    @Override
    public IBcConnector getBcConnector() {
        return mBcConnector;
    }
}
