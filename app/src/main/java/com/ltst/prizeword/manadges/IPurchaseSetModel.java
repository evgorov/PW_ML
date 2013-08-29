package com.ltst.prizeword.manadges;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 29.08.13.
 */
public interface IPurchaseSetModel {

    void reloadPurchases(@Nonnull IListenerVoid handler);
    void updatePurchase(ManadgeHolder.ManadgeProduct product, @Nonnull IListenerVoid handler);
}
