package com.ltst.przwrd.manadges;

import org.omich.velo.handlers.IListenerVoid;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 29.08.13.
 */
public interface IPurchaseSetModel {

    void reloadPurchases(@Nonnull IListenerVoid handler);
    void putOnePurchase(@Nonnull Purchase purchase, @Nonnull IListenerVoid handler);
    @Nonnull Purchase getPurchase(@Nullable String googleId);
    void close();
    void putPurchases(@Nonnull ArrayList<Purchase> purchases, @Nonnull IListenerVoid handler);
}
