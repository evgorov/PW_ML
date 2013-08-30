package com.ltst.prizeword.manadges;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.actionbarsherlock.internal.widget.IcsLinearLayout;
import com.android.billing.IabHelper;
import com.android.billing.IabResult;
import com.android.billing.Inventory;
import com.android.billing.Purchase;
import com.ltst.prizeword.navigation.NavigationActivity;
import com.ltst.prizeword.tools.UUIDTools;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 28.08.13.
 */
public class ManadgeHolder {

    public enum ManadgeProduct{
        hints10,
        hints20,
        hints30,
        set_brilliant,
        set_gold,
        set_silver,
        set_silver2,
        set_free,
        test_success,
        test_cancel,
        test_refunded,
        test_unavailable
    };

    private final @Nonnull String APP_GOOGLE_PLAY_ID = "4a6bbda29147dab10d4928f5df3a2bfc3d9b0bdb";

    private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_10           = "hints10";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_20           = "hints20";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_30           = "hints30";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_SET_BRILLIANT      = "buy_set_brilliant";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_SET_GOLD           = "buy_set_gold";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_SET_SILVER         = "buy_set_silver";
    private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_SET_SILVER2        = "buy_set_silver2";
    private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_SUCCESS          = "android.test.purchased";
    private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_CANCEL           = "android.test.canceled";
    private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_REFUNDED         = "android.test.refunded";
    private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE      = "android.test.unavailable";


    private final int REQUEST_GOOGLE_PLAY_HINTS_10              = 100;
    private final int REQUEST_GOOGLE_PLAY_HINTS_20              = 101;
    private final int REQUEST_GOOGLE_PLAY_HINTS_30              = 102;
    private final int REQUEST_GOOGLE_PLAY_SET_BRILLIANT         = 103;
    private final int REQUEST_GOOGLE_PLAY_SET_GOLD              = 104;
    private final int REQUEST_GOOGLE_PLAY_SET_SILVER            = 105;
    private final int REQUEST_GOOGLE_PLAY_SET_SILVER2           = 106;
    private final int REQUEST_GOOGLE_TEST_PRODUCT_SUCCESS       = 107;
    private final int REQUEST_GOOGLE_TEST_PRODUCT_CANCEL        = 108;
    private final int REQUEST_GOOGLE_TEST_PRODUCT_REFUNDED      = 109;
    private final int REQUEST_GOOGLE_TEST_PRODUCT_UNAVAILABLE   = 200;

    private @Nonnull IabHelper mIabHelper;
    private @Nonnull Activity mActivity;
    private @Nonnull Context mContext;
    private @Nonnull IManadges mIManadges;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull IPurchaseSetModel mIPurchaseSetModel;
    private @Nonnull HashMap<ManadgeProduct,String> mPrices;
    private @Nonnull List<IListenerVoid> mHandlerReloadPriceList;


    public ManadgeHolder(@Nonnull Activity activity, @Nonnull IBcConnector bcConnector) {

        mActivity = activity;
        mContext = (Context) activity;
        mIManadges = (IManadges) activity;
        mBcConnector = bcConnector;
        mIPurchaseSetModel = new PurchaseSetModel(mBcConnector);
        mPrices = new HashMap<ManadgeProduct, String>();
        mHandlerReloadPriceList = new ArrayList<IListenerVoid>();

//        // Ответ о покупке;
//        mIabHelper.queryInventoryAsync(mGotInventoryListener);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        return mIabHelper.handleActivityResult(requestCode, resultCode, data);
    }

    public void instance()
    {
        Log.d(NavigationActivity.LOG_TAG, "INSTANCE");
        mIabHelper = new IabHelper(mContext,APP_GOOGLE_PLAY_ID);
        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener()
        {
            public void onIabSetupFinished(IabResult result)
            {
                if (result.isSuccess())
                {
                    // Получаем цены с сервера;
                    reloadPrice();
                }
            }
        });
        mIPurchaseSetModel.reloadPurchases(mReloadPurchaseFromDataBase);
    }

    public void dispose()
    {
        if (mIabHelper != null)
        {
            Log.d(NavigationActivity.LOG_TAG, "DESPOSE");
            mIabHelper.dispose();
        }
        mIabHelper = null;
    }

    @Nonnull String extractProductId(ManadgeHolder.ManadgeProduct product)
    {
        switch (product)
        {
            case hints10:           return GOOGLE_PLAY_PRODUCT_ID_HINTS_10;
            case hints20:           return GOOGLE_PLAY_PRODUCT_ID_HINTS_20;
            case hints30:           return GOOGLE_PLAY_PRODUCT_ID_HINTS_30;
            case set_brilliant:     return GOOGLE_PLAY_PRODUCT_ID_SET_BRILLIANT;
            case set_gold:          return GOOGLE_PLAY_PRODUCT_ID_SET_GOLD;
            case set_silver:        return GOOGLE_PLAY_PRODUCT_ID_SET_SILVER;
            case set_silver2:       return GOOGLE_PLAY_PRODUCT_ID_SET_SILVER2;
            case test_success:  return GOOGLE_PLAY_TEST_PRODUCT_SUCCESS;
            case test_cancel:       return GOOGLE_PLAY_TEST_PRODUCT_CANCEL;
            case test_refunded:     return GOOGLE_PLAY_TEST_PRODUCT_REFUNDED;
            case test_unavailable:  return GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE;
            default: break;
        }
        return null;
    }

    int extractProductRequest(ManadgeHolder.ManadgeProduct product)
    {
        switch (product)
        {
            case hints10:           return REQUEST_GOOGLE_PLAY_HINTS_10;
            case hints20:           return REQUEST_GOOGLE_PLAY_HINTS_20;
            case hints30:           return REQUEST_GOOGLE_PLAY_HINTS_30;
            case set_brilliant:     return REQUEST_GOOGLE_PLAY_SET_BRILLIANT;
            case set_gold:          return REQUEST_GOOGLE_PLAY_SET_GOLD;
            case set_silver:        return REQUEST_GOOGLE_PLAY_SET_SILVER;
            case set_silver2:       return REQUEST_GOOGLE_PLAY_SET_SILVER2;
            case test_success:           return REQUEST_GOOGLE_TEST_PRODUCT_SUCCESS;
            case test_cancel:       return REQUEST_GOOGLE_TEST_PRODUCT_CANCEL;
            case test_refunded:     return REQUEST_GOOGLE_TEST_PRODUCT_REFUNDED;
            case test_unavailable:  return REQUEST_GOOGLE_TEST_PRODUCT_UNAVAILABLE;
            default: break;
        }
        return 0;
    }

    public void buyProduct(ManadgeHolder.ManadgeProduct product)
    {
        // Start popup window. Покупка;
        @Nonnull String product_id = extractProductId(product);
        int  product_request = extractProductRequest(product);
        @Nonnull String token = UUIDTools.generateStringUUID();
        if(product_id!=null && product_request!=0){
            Log.d(NavigationActivity.LOG_TAG, "Generate token: "+token);
            mIabHelper.launchPurchaseFlow(mActivity, product_id, product_request, mPurchaseFinishedListener, token);
        }
    }

    public void registerHandlerPriceProductsChange(@Nonnull IListenerVoid handler)
    {
        Log.d(NavigationActivity.LOG_TAG, "ADD HANDLER");
        mHandlerReloadPriceList.add(handler);
    }

    public void reloadPrice()
    {
        Log.d(NavigationActivity.LOG_TAG, "RELOAD PRICES");
        // Отправляем запрос на получие информации о продуктах приложения на Google Play;
        List<String> additionalSkuList = new ArrayList<String>();
        for(ManadgeProduct product : ManadgeProduct.values()) {
            additionalSkuList.add(extractProductId(product));
        }
        mIabHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
    }

    public String getPriceProduct(ManadgeHolder.ManadgeProduct product)
    {
        if(!mPrices.containsKey(product))
            return null;
        return mPrices.get(product);
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

                @Nonnull String prodict_id = null;
                for(ManadgeProduct product : ManadgeProduct.values())
                {
                    prodict_id = extractProductId(product);
                    if(inventory.hasPurchase(prodict_id))
                    {
                        // Восстанавливаем для продукта возможность покупки;
                        mIabHelper.consumeAsync(inventory.getPurchase(prodict_id),mConsumeFinishedListener);
                    }
                }

                // update UI accordingly
            }
        }
    };

    // Получаем информацию о возобновлении покупаемости товара;
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener()
    {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                // provision the in-app purchase to the user
                // (for example, credit 50 gold coins to player's character)
                int responseState = purchase.getPurchaseState();
                String responseGoogleId = purchase.getSku();
                String responseClientId = purchase.getDeveloperPayload();
            }
            else {
                // handle error
            }
        }
    };

    // Получаем информацию с Google Play о продуктах in-app;
    IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isFailure()) {
                // handle error
                return;
            }

            @Nonnull String prodict_id = null;
            for(ManadgeProduct product : ManadgeProduct.values())
            {
                prodict_id = extractProductId(product);
                if(inventory.hasDetails(prodict_id))
                    mPrices.put(product,inventory.getSkuDetails(prodict_id).getPrice());
            }
            mReloadPricesHandler.handle();
        }
    };

    // Ответ с результатом выполненой покупки;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure())
            {
                Log.d(NavigationActivity.LOG_TAG, "Error purchasing: " + result);
                return;
            }

            // Восстанавливаем возможность сделать повторную покупку продукта;
            mIabHelper.consumeAsync(purchase,mConsumeFinishedListener);

            int responseState = purchase.getPurchaseState();
            String responseGoogleId = purchase.getSku();
            String responseClientId = purchase.getDeveloperPayload();
        }
    };

    @Nonnull IListenerVoid mReloadPurchaseFromDataBase = new IListenerVoid() {
        @Override
        public void handle() {

        }
    };

    @Nonnull IListenerVoid mReloadPricesHandler = new IListenerVoid() {
        @Override
        public void handle() {
            Log.d(NavigationActivity.LOG_TAG, "SEND RELOAD PRICE!");
            Log.d(NavigationActivity.LOG_TAG, "COUNT HANDLER: "+mHandlerReloadPriceList.size());

            for(IListenerVoid handle : mHandlerReloadPriceList)
            {
                handle.handle();
            }
        }
    };
}
