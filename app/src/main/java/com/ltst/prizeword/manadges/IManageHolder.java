package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 10.09.13.
 */
public interface IManageHolder {

    void buyProduct(@Nonnull String sku);
    void uploadProduct(@Nonnull String sku);
    void registerHandlerPriceProductsChange(@Nonnull IListenerVoid handler);
    void registerHandlerBuyProductEvent(@Nonnull IListener<Bundle> handler);
    void registerProduct(@Nonnull String googleId);
    String getPriceProduct(@Nonnull String googleId);
    void productBuyOnServer(@Nonnull String googleId);
    void reloadInventory(@Nonnull IListenerVoid handler);
}


