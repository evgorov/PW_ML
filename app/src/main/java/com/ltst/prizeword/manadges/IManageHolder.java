package com.ltst.prizeword.manadges;

import android.content.Intent;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 10.09.13.
 */
public interface IManageHolder {

    void buyProduct(ManageHolder.ManadgeProduct product);
    void buyCrosswordSet(@Nonnull ManageHolder.ManadgeProduct product, @Nonnull String crosswordSetServerId);
    void registerHandlerPriceProductsChange(@Nonnull IListenerVoid handler);
    void registerHandlerBuyProductEvent(@Nonnull IListener<ManageHolder.ManadgeProduct> handler);
    String getPriceProduct(ManageHolder.ManadgeProduct product);
}
