package com.ltst.prizeword.crossword.view;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.billing.IabHelper;
import com.android.billing.IabResult;
import com.android.billing.Inventory;
import com.android.billing.Purchase;
import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.HintsModel;
import com.ltst.prizeword.navigation.NavigationActivity;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HintsManager implements View.OnClickListener
{
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_HINTS_10 = "hints10";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_HINTS_20 = "hints20";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_HINTS_30 = "hints30";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_SET_BRILLIANT = "buy_set_brilliant";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_SET_GOLD = "buy_set_gold";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_SET_SILVER = "buy_set_silver";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_SET_SILVER2 = "buy_set_silver2";

    private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_OK = "android.test.purchased";
    private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_CANCEL = "android.test.canceled";
    private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_REFUNDED = "android.test.refunded";
    private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE = "android.test.unavailable";


    private final int REQUEST_GOOGLE_PLAY_HINTS_10 = 10;
    private final int REQUEST_GOOGLE_PLAY_HINTS_20 = 11;
    private final int REQUEST_GOOGLE_PLAY_HINTS_30 = 12;
    private final int REQUEST_GOOGLE_PLAY_SET_BRILLIANT = 13;
    private final int REQUEST_GOOGLE_PLAY_SET_GOLD = 14;
    private final int REQUEST_GOOGLE_PLAY_SET_SILVER = 15;
    private final int REQUEST_GOOGLE_PLAY_SET_SILVER2 = 16;
    private final int REQUEST_GOOGLE_TEST_PRODUCT_OK = 17;
    private final int REQUEST_GOOGLE_TEST_PRODUCT_CANCEL = 18;
    private final int REQUEST_GOOGLE_TEST_PRODUCT_UNAVAILABLE = 19;

    private @Nonnull View mBuyHints_10;
    private @Nonnull View mBuyHints_20;
    private @Nonnull View mBuyHints_30;
    private @Nonnull TextView mPriceHints_10;
    private @Nonnull TextView mPriceHints_20;
    private @Nonnull TextView mPriceHints_30;

    private HintsModel mHintsModel;
    private @Nullable IListenerInt mHintChangeListener;
    private @Nonnull IabHelper mIabHelper;
    private @Nonnull Context mContext;
    private @Nonnull Activity mActivity;

    public HintsManager(@Nonnull Context context, @Nonnull IabHelper iabHelper, @Nonnull IBcConnector bcConnector, @Nonnull String sessionKey, View parentView)
    {
        mContext = context;
        mActivity = (Activity) context;

        mIabHelper = iabHelper;
        mHintsModel = new HintsModel(bcConnector, sessionKey);

        mPriceHints_10 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_10_price);
        mPriceHints_20 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_20_price);
        mPriceHints_30 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_30_price);

        mBuyHints_10 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_10_btn);
        mBuyHints_20 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_20_btn);
        mBuyHints_30 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_30_btn);

        mBuyHints_10.setOnClickListener(this);
        mBuyHints_20.setOnClickListener(this);
        mBuyHints_30.setOnClickListener(this);

//        // Получаем информацию о продуктах приложения на Google Play;
//        List<String> additionalSkuList = new ArrayList<String>(3);
//        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_HINTS_10);
//        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_HINTS_20);
//        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_HINTS_30);
//        mIabHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);

//        // Покупка;
//        mIabHelper.launchPurchaseFlow(this, GOOGLE_PLAY_PRODUCT_HINTS_10, REQUEST_GOOGLE_PLAY_HINTS_10,
//                mPurchaseFinishedListener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");

//        // Ответ о покупке;
//        mIabHelper.queryInventoryAsync(mGotInventoryListener);

    }

    public void setHintChangeListener(@Nullable IListenerInt hintChangeListener)
    {
        mHintChangeListener = hintChangeListener;
    }

    @Override
    public void onClick(View v)
    {

        // Покупка;
        mIabHelper.launchPurchaseFlow(mActivity, GOOGLE_PLAY_TEST_PRODUCT_OK, REQUEST_GOOGLE_TEST_PRODUCT_OK,
                mPurchaseFinishedListener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
//        mIabHelper.launchPurchaseFlow(mActivity, GOOGLE_PLAY_PRODUCT_HINTS_10, REQUEST_GOOGLE_PLAY_HINTS_10,
//                mPurchaseFinishedListener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");

        int count = 0;
        switch (v.getId())
        {
            case R.id.crossword_fragment_current_rest_buy_10_btn:
                count = 10;
                break;
            case R.id.crossword_fragment_current_rest_buy_20_btn:
                count = 20;
                break;
            case R.id.crossword_fragment_current_rest_buy_30_btn:
                count = 30;
                break;
        }
//        if (count != 0)
//        {
//            changeHintsCount(count);
//        }
    }

    private void changeHintsCount(final int count)
    {
        mHintsModel.changeHints(count, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                if (mHintChangeListener != null)
                {
                    mHintChangeListener.handle(count);
                }
            }
        });
    }

    public void reloadPrices()
    {
        // Получаем информацию о продуктах приложения на Google Play;
        List<String> additionalSkuList = new ArrayList<String>();
        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_HINTS_10);
        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_HINTS_20);
        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_HINTS_30);
        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_SET_BRILLIANT);
        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_SET_GOLD);
        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_SET_SILVER);
        additionalSkuList.add(GOOGLE_PLAY_PRODUCT_SET_SILVER2);
        additionalSkuList.add(GOOGLE_PLAY_TEST_PRODUCT_OK);
        mIabHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure())
            {

            }
            else
            {
                // does the user have the premium upgrade?
                if(inventory.hasPurchase(GOOGLE_PLAY_PRODUCT_HINTS_10))
                {
                    // Восстанавливаем для продукта возможность покупки;
                    mIabHelper.consumeAsync(inventory.getPurchase(GOOGLE_PLAY_PRODUCT_HINTS_10),mConsumeFinishedListener);
                }
                if(inventory.hasPurchase(GOOGLE_PLAY_PRODUCT_HINTS_20))
                {
                    // Восстанавливаем для продукта возможность покупки;
                    mIabHelper.consumeAsync(inventory.getPurchase(GOOGLE_PLAY_PRODUCT_HINTS_20),mConsumeFinishedListener);
                }
                if(inventory.hasPurchase(GOOGLE_PLAY_PRODUCT_HINTS_30))
                {
                    // Восстанавливаем для продукта возможность покупки;
                    mIabHelper.consumeAsync(inventory.getPurchase(GOOGLE_PLAY_PRODUCT_HINTS_30),mConsumeFinishedListener);
                }

                // update UI accordingly
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener()
    {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                // provision the in-app purchase to the user
                // (for example, credit 50 gold coins to player's character)
            }
            else {
                // handle error
            }
        }
    };

    IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            // Получаем информацию с Google Play о продуктах in-app;
            if (result.isFailure()) {
                // handle error
                return;
            }
            @Nonnull String set_brilliant = null;
            @Nonnull String set_gold = null;
            @Nonnull String set_silver = null;
            @Nonnull String set_silver2 = null;
            @Nonnull String testproduct1 = null;

            if(inventory.hasDetails(GOOGLE_PLAY_PRODUCT_HINTS_10))
                mPriceHints_10.setText(inventory.getSkuDetails(GOOGLE_PLAY_PRODUCT_HINTS_10).getPrice());
            if(inventory.hasDetails(GOOGLE_PLAY_PRODUCT_HINTS_20))
                mPriceHints_20.setText(inventory.getSkuDetails(GOOGLE_PLAY_PRODUCT_HINTS_20).getPrice());
            if(inventory.hasDetails(GOOGLE_PLAY_PRODUCT_HINTS_30))
                mPriceHints_30.setText(inventory.getSkuDetails(GOOGLE_PLAY_PRODUCT_HINTS_30).getPrice());
            if(inventory.hasDetails(GOOGLE_PLAY_PRODUCT_SET_BRILLIANT))
                set_brilliant = inventory.getSkuDetails(GOOGLE_PLAY_PRODUCT_SET_BRILLIANT).getPrice();
            if(inventory.hasDetails(GOOGLE_PLAY_PRODUCT_SET_GOLD))
                set_gold = inventory.getSkuDetails(GOOGLE_PLAY_PRODUCT_SET_GOLD).getPrice();
            if(inventory.hasDetails(GOOGLE_PLAY_PRODUCT_SET_SILVER))
                set_silver = inventory.getSkuDetails(GOOGLE_PLAY_PRODUCT_SET_SILVER).getPrice();
            if(inventory.hasDetails(GOOGLE_PLAY_PRODUCT_SET_SILVER2))
                set_silver2 = inventory.getSkuDetails(GOOGLE_PLAY_PRODUCT_SET_SILVER2).getPrice();
            if(inventory.hasDetails(GOOGLE_PLAY_TEST_PRODUCT_OK))
                testproduct1 = inventory.getSkuDetails(GOOGLE_PLAY_TEST_PRODUCT_OK).getPrice();

            // Возобновляем покупку;
            if (inventory.hasPurchase(GOOGLE_PLAY_TEST_PRODUCT_OK))
            {
                mIabHelper.consumeAsync(inventory.getPurchase(GOOGLE_PLAY_TEST_PRODUCT_OK), null);
            }
            // update the UI
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure())
            {
                Log.d(NavigationActivity.LOG_TAG, "Error purchasing: " + result);
                return;
            }

            else if (purchase.getSku().equals(GOOGLE_PLAY_PRODUCT_HINTS_10)) {
                // consume the gas and update the UI
            }
            else if (purchase.getSku().equals(GOOGLE_PLAY_PRODUCT_HINTS_20)) {
                // give user access to premium content and update the UI
            }
            else if (purchase.getSku().equals(GOOGLE_PLAY_PRODUCT_HINTS_30)) {
                // give user access to premium content and update the UI
            }
            else
            {

            }
        }
    };
}
