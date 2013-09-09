package com.ltst.prizeword.manadges;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.android.billing.IabHelper;
import com.android.billing.IabResult;
import com.android.billing.Inventory;
import com.android.billing.Purchase;
import com.ltst.prizeword.tools.UUIDTools;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_10           = "hints10";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_20           = "hints20";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_30           = "hints30";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_SET_BRILLIANT      = "buy_set_brilliant";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_SET_GOLD           = "buy_set_gold";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_SET_SILVER         = "buy_set_silver";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_SET_SILVER2        = "buy_set_silver2";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_SUCCESS          = "android.test.purchased";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_CANCEL           = "android.test.canceled";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_REFUNDED         = "android.test.refunded";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE      = "android.test.unavailable";


    static private final int REQUEST_GOOGLE_PLAY_HINTS_10              = 100;
    static private final int REQUEST_GOOGLE_PLAY_HINTS_20              = 101;
    static private final int REQUEST_GOOGLE_PLAY_HINTS_30              = 102;
    static private final int REQUEST_GOOGLE_PLAY_SET_BRILLIANT         = 103;
    static private final int REQUEST_GOOGLE_PLAY_SET_GOLD              = 104;
    static private final int REQUEST_GOOGLE_PLAY_SET_SILVER            = 105;
    static private final int REQUEST_GOOGLE_PLAY_SET_SILVER2           = 106;
    static private final int REQUEST_GOOGLE_TEST_PRODUCT_SUCCESS       = 107;
    static private final int REQUEST_GOOGLE_TEST_PRODUCT_CANCEL        = 108;
    static private final int REQUEST_GOOGLE_TEST_PRODUCT_REFUNDED      = 109;
    static private final int REQUEST_GOOGLE_TEST_PRODUCT_UNAVAILABLE   = 200;

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
            mIabHelper.dispose();
        }
        mIabHelper = null;
        mIPurchaseSetModel.close();
    }

    static @Nonnull String extractProductId(ManadgeHolder.ManadgeProduct product)
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
            case test_success:      return GOOGLE_PLAY_TEST_PRODUCT_SUCCESS;
            case test_cancel:       return GOOGLE_PLAY_TEST_PRODUCT_CANCEL;
            case test_refunded:     return GOOGLE_PLAY_TEST_PRODUCT_REFUNDED;
            case test_unavailable:  return GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE;
            default: break;
        }
        return null;
    }

//    static @Nonnull ManadgeHolder.ManadgeProduct extractProduct(@Nonnull String googleId)
//    {
//        if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
//            return ManadgeProduct.hints10;
//        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
//            return ManadgeProduct.hints20;
//        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
//            return ManadgeProduct.hints30;
//        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_SET_BRILLIANT))
//            return ManadgeProduct.set_brilliant;
//        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_SET_GOLD))
//            return ManadgeProduct.set_gold;
//        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_SET_SILVER))
//            return ManadgeProduct.set_silver;
//        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_SET_SILVER2))
//            return ManadgeProduct.set_silver2;
//        else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
//            return ManadgeProduct.test_success;
//        else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_CANCEL))
//            return ManadgeProduct.test_cancel;
//        else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_REFUNDED))
//            return ManadgeProduct.test_refunded;
//        else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE))
//            return ManadgeProduct.test_unavailable;
//        return null;
//    }

    static int extractProductRequest(ManadgeHolder.ManadgeProduct product)
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
            try {
//                mIabHelper.flagEndAsync();
                mIabHelper.launchPurchaseFlow(mActivity, product_id, product_request, mPurchaseFinishedListener, token);
            } catch (Exception e)
            {
                Log.e(e.getMessage());
            }
        }
    }

    public void registerHandlerPriceProductsChange(@Nonnull IListenerVoid handler)
    {
        mHandlerReloadPriceList.add(handler);
    }

    public void reloadPrice()
    {
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

//        @Nonnull String googleId = extractProductId(product);
//        @Nonnull com.ltst.prizeword.manadges.Purchase purchase = mIPurchaseSetModel.getPurchase(googleId);
//        return purchase.price;
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

                        @Nonnull com.ltst.prizeword.manadges.Purchase purchase = mIPurchaseSetModel.getPurchase(prodict_id);
                        purchase.googlePurchase = false;
                        mIPurchaseSetModel.putPurchase(purchase, new IListenerVoid() {
                            @Override
                            public void handle() {

                            }
                        });

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
                {
                    mPrices.put(product,inventory.getSkuDetails(prodict_id).getPrice());
                    @Nullable com.ltst.prizeword.manadges.Purchase purchase = mIPurchaseSetModel.getPurchase(prodict_id);
                    if (purchase == null)
                    {
                        return;
                    }

                    purchase.price = inventory.getSkuDetails(prodict_id).getPrice();
                    mIPurchaseSetModel.putPurchase(purchase, new IListenerVoid() {
                        @Override
                        public void handle() {

                        }
                    });
                }
            }


            mIPurchaseSetModel.reloadPurchases(new IListenerVoid() {
                @Override
                public void handle() {
                    mReloadPricesHandler.handle();
                }
            });
        }
    };

    // Ответ с результатом выполненой покупки;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure())
            {
                Log.e("Error purchasing: " + result);
                return;
            }

            // Восстанавливаем возможность сделать повторную покупку продукта;
            mIabHelper.consumeAsync(purchase,mConsumeFinishedListener);

            int responseState = purchase.getPurchaseState();
            String responseGoogleId = purchase.getSku();
            String responseClientId = purchase.getDeveloperPayload();
            @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(responseGoogleId);
            if (product == null)
            {
                return;
            }

            product.googlePurchase = true;
            product.clientId = responseClientId;
            mIPurchaseSetModel.putPurchase(product, new IListenerVoid() {
                @Override
                public void handle() {

                }
            });
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
            for(IListenerVoid handle : mHandlerReloadPriceList)
            {
                handle.handle();
            }
        }
    };
}
