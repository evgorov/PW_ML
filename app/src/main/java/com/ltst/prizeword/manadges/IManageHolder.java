package com.ltst.prizeword.manadges;

import android.content.Intent;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 10.09.13.
 */
public interface IManageHolder {

//    void instance();
//    void dispose();
//    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    void buyProduct(ManageHolder.ManadgeProduct product);
    void registerHandlerPriceProductsChange(@Nonnull IListenerVoid handler);
    void registerHandlerBuyProductEvent(@Nonnull IListenerVoid handler);
    String getPriceProduct(ManageHolder.ManadgeProduct product);

}
