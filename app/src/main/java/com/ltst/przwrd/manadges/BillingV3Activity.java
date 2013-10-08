package com.ltst.przwrd.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ltst.przwrd.app.BcConnectorActivity;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 08.10.13.
 */
public class BillingV3Activity extends BcConnectorActivity implements IManadges {

    private @Nonnull IIabHelper mManadgeHolder;
    private @Nonnull IBcConnector mBcConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBcConnector = getBcConnector();
        mManadgeHolder = new ManageHolder(this, mBcConnector);
        mManadgeHolder.instance();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Проверяем ответ, обработается он библиотекой контроля покупок In-App Billing;
        if (mManadgeHolder.onActivityResult(requestCode, resultCode, data))
        {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mManadgeHolder.dispose();
    }

    @Nonnull
    @Override
    public IManageHolder getManadgeHolder()
    {
        return (IManageHolder) mManadgeHolder;
    }
}
