package com.ltst.prizeword.manadges;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 28.08.13.
 */
public interface IManadges {

    void buyProduct(ManadgeHolder.ManadgeProduct product);
    void reloadPriceProducts(@Nonnull IListenerVoid handler);
    String getPriceProduct(ManadgeHolder.ManadgeProduct product);

}
