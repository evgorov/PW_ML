package com.ltst.przwrd.crossword.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.przwrd.R;
import com.ltst.przwrd.crossword.model.HintsModel;
import com.ltst.przwrd.manadges.IManageHolder;
import com.ltst.przwrd.manadges.ManageHolder;
import com.ltst.przwrd.navigation.NavigationActivity;
import com.ltst.przwrd.sounds.SoundsWork;
import com.ltst.przwrd.manadges.IManadges;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HintsManager implements View.OnClickListener
{
    static public final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_10           = "prizeword.ltst.hints10";
    static public final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_20           = "prizeword.ltst.hints20";
    static public final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_30           = "prizeword.ltst.hints30";
    static public final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_SUCCESS          = "android.test.purchased";
    static public final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_CANCEL           = "android.test.canceled";
    static public final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_REFUNDED         = "android.test.refunded";
    static public final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE      = "android.test.unavailable";


    private @Nonnull TextView mHintsCountView;
    private @Nonnull View mBuyHints_10;
    private @Nonnull View mBuyHints_20;
    private @Nonnull View mBuyHints_30;
    private @Nonnull TextView mPriceHints_10;
    private @Nonnull TextView mPriceHints_20;
    private @Nonnull TextView mPriceHints_30;

    private @Nonnull IManadges mIManadges;
    private @Nonnull IManageHolder mIManageHolder;
    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull Context mContext;

    private int mHintsCount;

    public HintsManager(@Nonnull Context context, @Nonnull SherlockFragment fragment, View parentView)
    {
        mContext = context;

        mIManadges = (IManadges) context;
        mIManageHolder = mIManadges.getManadgeHolder();
        mIManageHolder.registerHandlerPriceProductsChange(mReloadPriceProductHandler);
        mIManageHolder.registerHandlerBuyProductEvent(mManadgeBuyProductIListener);
        mIManageHolder.registerProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_10);
        mIManageHolder.registerProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_20);
        mIManageHolder.registerProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_30);
        mICrosswordFragment = (ICrosswordFragment) fragment;

        mHintsCountView = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_count);

        mPriceHints_10 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_10_price);
        mPriceHints_20 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_20_price);
        mPriceHints_30 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_30_price);

        mBuyHints_10 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_10_btn);
        mBuyHints_20 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_20_btn);
        mBuyHints_30 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_30_btn);

        mBuyHints_10.setOnClickListener(this);
        mBuyHints_20.setOnClickListener(this);
        mBuyHints_30.setOnClickListener(this);
        setPrice();
    }

    @Override
    public void onClick(View v)
    {
        SoundsWork.interfaceBtnMusic(mContext);
        // Покупка;

        switch (v.getId())
        {
            case R.id.crossword_fragment_current_rest_buy_10_btn:
//              mIManageHolder.buyProduct(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS);
              mIManageHolder.buyProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_10);
                break;
            case R.id.crossword_fragment_current_rest_buy_20_btn:
                mIManageHolder.buyProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_20);
                break;
            case R.id.crossword_fragment_current_rest_buy_30_btn:
                mIManageHolder.buyProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_30);
                break;
        }
    }

    public void close()
    {
    }

    private void setPrice()
    {
        String priceHints10 = mIManageHolder.getPriceProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_10);
        String priceHints20 = mIManageHolder.getPriceProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_20);
        String priceHints30 = mIManageHolder.getPriceProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_30);
        mPriceHints_10.setText(priceHints10);
        mPriceHints_20.setText(priceHints20);
        mPriceHints_30.setText(priceHints30);
        NavigationActivity.debug("set price hint");
    }

    public int getHintsCount()
    {
        return mHintsCount;
    }

    public void setHintsCount(int count)
    {
        mHintsCount = count;
        mHintsCountView.setText(String.valueOf(count));
    }

    @Nonnull
    IListenerVoid mReloadPriceProductHandler = new IListenerVoid() {
        @Override
        public void handle() {
            setPrice();
        }
    };

    @Nonnull
    IListener<Bundle> mManadgeBuyProductIListener = new IListener<Bundle>() {
        @Override
        public void handle(@Nullable Bundle bundle) {

            int count = 0;
            final @Nonnull String googleId = ManageHolder.extractFromBundleSKU(bundle);
            if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
            {
                count = 10;
            }
            else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
            {
                count = 20;
            }
            else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
            {
                count = 30;
            }
//            else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
//            {
//                count = 10;
//            }
            else
                return;

            HintsModel hintsModel = mICrosswordFragment.getHintsModel();
            if(hintsModel != null)
            {
                hintsModel.changeHints(count, new IListenerVoid() {
                    @Override
                    public void handle() {

                        // Меняем состояние товара;
                        mIManageHolder.productBuyOnServer(googleId);
                    }
                });

                setHintsCount(getHintsCount()+count);
            }
        }
    };
}