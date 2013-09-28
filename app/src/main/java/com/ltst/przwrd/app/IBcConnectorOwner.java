package com.ltst.przwrd.app;

import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;

public interface IBcConnectorOwner
{
    @Nonnull IBcConnector getBcConnector ();
}
