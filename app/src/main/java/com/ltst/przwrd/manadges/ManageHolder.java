package com.ltst.przwrd.manadges;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.billing.IabHelper;
import com.android.billing.IabResult;
import com.android.billing.Inventory;
import com.android.billing.Purchase;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.DeviceDetails;
import com.ltst.przwrd.navigation.INavigationActivity;
import com.ltst.przwrd.navigation.NavigationActivity;
import com.ltst.przwrd.tools.UUIDTools;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 28.08.13.
 */
public class ManageHolder implements IManageHolder, IIabHelper {

    static public final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_10           = "prizeword.ltst.hints10";
    static public final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_20           = "prizeword.ltst.hints20";
    static public final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_30           = "prizeword.ltst.hints30";
    static public final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_SUCCESS          = "android.test.purchased";
    static public final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_CANCEL           = "android.test.canceled";
    static public final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_REFUNDED         = "android.test.refunded";
    static public final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE      = "android.test.unavailable";


    final static public @Nonnull String BF_SKU          = "ManageHolder.sku";
    final static public @Nonnull String BF_JSON         = "ManageHolder.json";
    final static public @Nonnull String BF_SIGNATURE    = "ManageHolder.signature";

    private final @Nonnull String APP_GOOGLE_PLAY_ID = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmBgW7Hq94XrHcX0dpOfMrIykHyjNfDPSH3SbkKyYq5rFVdbqvvRiB0YOPaQWzJwwuUAo2QvDDddnL3LzVBo07SqwAtIXgnJ4EAzDCng5QX+bEOMjelOUcC0DkRt9hGvlPmIwFLVWfQTKgemT9iyO4LckkGBdjGGPmUXd7jlZv4EB7+4vnc5CunzcKOjsnRmGQyajK/kCmA3cD3Xruig6sUl5pejOTi55Peshgl0w3khgshCEdP+vVSHNuqYta9JwUttIIidXNO1ztJ/hctwy5CbdWYYv/sn3Q0IrTSturG8SeC7IEP8oCgpbtIhjXZshrIiMUOjI3eZ1W/C8fkbyawIDAQAB";
    private final @Nonnull String DEVICE_ID;

    private @Nonnull IabHelper mHelper;
    private @Nonnull Activity mActivity;
    private @Nonnull Context mContext;
    private @Nonnull INavigationActivity mINavigationActivity;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull IPurchaseSetModel mIPurchaseSetModel;
    private @Nonnull List<String> mSkuContainer;
    private @Nonnull List<IListenerVoid> mHandlerReloadPriceList;
    private @Nonnull List<IListener<Bundle>> mHandlerBuyProductEventList;
    private @Nonnull IListenerVoid mNotifyInventoryHandler;
    private @Nonnull List<PurchasePrizeWord> mRestoreProducts;

    private int REQUERT_CODE_COUNTER;

    public ManageHolder(@Nonnull Activity activity, @Nonnull IBcConnector bcConnector) {
        mActivity = activity;
        mContext = (Context) activity;
        mINavigationActivity = (INavigationActivity) activity;
        mBcConnector = bcConnector;
        mIPurchaseSetModel = new PurchaseSetModel(mBcConnector);
        mHandlerReloadPriceList = new ArrayList<IListenerVoid>();
        mHandlerBuyProductEventList = new ArrayList<IListener<Bundle>>();
        mSkuContainer = new ArrayList<String>();
        REQUERT_CODE_COUNTER = 0;
        DEVICE_ID = DeviceDetails.getHashDevice(mContext);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        return mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void instance()
    {
        try
        {
            mHelper = new IabHelper(mContext,APP_GOOGLE_PLAY_ID);
        }
        catch (Exception e)
        {
            Log.e(e.getMessage());
            Log.i("Can't use license public key of app"); //$NON-NLS-1$
            Toast.makeText(mContext, "Was't instance in-app billing lib: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess())
                {
                    Log.e("Problem setting up in-app billing: " + result);
                    Toast.makeText(mContext, "Problem setting up in-app billing: " + result, Toast.LENGTH_LONG).show();
                }
                else
                {
//                    // Получаем список товаров, которые были куплены пользователем;
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                    verifyControllPurchases();
                }
            }
        });
        reloadInventoryFromDataBase();
    }

    @Override
    public void dispose()
    {
        if (mHelper != null)
        {
            mHelper.dispose();
        }
        mHelper = null;
        mIPurchaseSetModel.close();
    }

    @Override
    @Nonnull
    public List<PurchasePrizeWord> getRestoreProducts() {
        return mRestoreProducts;
    }

    @Override
    public void unregisterHandlers() {
        mHandlerReloadPriceList.clear();
        mHandlerBuyProductEventList.clear();
    }

    @Override
    public void buyProduct(@Nonnull String sku)
    {
        // Start popup window. Покупка;

        if(sku != Strings.EMPTY){
            @Nonnull PurchasePrizeWord product = mIPurchaseSetModel.getPurchase(sku);
            product.clientId = DEVICE_ID;

            try
            {
                mHelper.flagEndAsync();
                mHelper.launchPurchaseFlow(mActivity, sku, ++REQUERT_CODE_COUNTER, mBuyFinishedListener, DEVICE_ID);
                mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);
            }
            catch (Exception e)
            {
                Log.e(e.getMessage());
                mINavigationActivity.sendMessage(e.getMessage());
            }
        }
    }

    @Override
    public void uploadProduct(@Nonnull String sku) {
        // Меняем состояние продукта, что он был куплен в Google PLay и следует совершить покупку на сервере и восстановить покупаемость, если надо;
        @Nullable PurchasePrizeWord product = mIPurchaseSetModel.getPurchase(sku);
        product.googlePurchase = false;
        product.serverPurchase = true;
        product.receipt_data = "";
        product.signature = "";
        mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);

        verifyControllPurchases();
    }

    @Override
    public void registerHandlerPriceProductsChange(@Nonnull IListenerVoid handler)
    {
        mHandlerReloadPriceList.add(handler);
    }

    @Override
    public void registerHandlerBuyProductEvent(@Nonnull IListener<Bundle> handler)
    {
        mHandlerBuyProductEventList.add(handler);
    }

    @Override
    public void registerProduct(@Nonnull String sku)
    {
        if(sku == Strings.EMPTY || mSkuContainer.contains(sku))
            return;
        mSkuContainer.add(sku);
    }

    public void reloadInventoryFromDataBase()
    {
        mIPurchaseSetModel.reloadPurchases(mReloadPurchaseFromDataBase);
    }

    @Override
    public void reloadInventory(@Nonnull IListenerVoid handler)
    {
        // Отправляем запрос на получие информации о продуктах приложения на Google Play;
        mNotifyInventoryHandler = handler;
        try
        {
            mHelper.flagEndAsync();
            mHelper.queryInventoryAsync(true, mSkuContainer, mQueryFinishedListener);
        }
        catch(IllegalStateException e)
        {
            Log.e(e.getMessage());
        }
        catch (Exception e)
        {
            Log.e(e.getMessage());
        }
    }

    @Override
    public void restoreProducts(final @Nonnull IListenerVoid handler) {
        // Восстанавливаем покупки на устройстве;
        mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener()
        {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

                if (result.isFailure())
                {
                    Log.e("Problem setting up in-app billing: " + result);
                }
                else
                {
                    List<Purchase> purchases = inventory.getPurchasesList();
                    List<PurchasePrizeWord> purchasePrizeWords = new ArrayList<PurchasePrizeWord>(purchases.size());
                    for(Purchase purchase : purchases)
                    {
                        @Nonnull PurchasePrizeWord product = mIPurchaseSetModel.getPurchase(purchase.getSku());
                        product.clientId = DEVICE_ID;
                        product.receipt_data = purchase.getOriginalJson();
                        product.signature = purchase.getSignature();
                        product.serverPurchase = true;
                        purchasePrizeWords.add(product);
//                        mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);
                    }
                    mRestoreProducts = purchasePrizeWords;
                    handler.handle();
                }
            }
        }
        );
    }

    @Override
    public String getPriceProduct(@Nonnull String sku)
    {
        PurchasePrizeWord purchase = mIPurchaseSetModel.getPurchase(sku);
        if(purchase==null)
            return null;
        return purchase.price;
    }

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure())
            {
                    Log.e("Problem setting up in-app billing: " + result);
            }
            else
            {
                List<Purchase> purchases = inventory.getPurchasesList();
                for(Purchase purchase : purchases)
                {
                    @Nonnull String sku = purchase.getSku();
                    if( sku.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_10)
                        ||sku.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_20)
                        ||sku.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_30)
                        ||sku.equals(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS)
                        ||sku.equals(GOOGLE_PLAY_TEST_PRODUCT_CANCEL)
                        ||sku.equals(GOOGLE_PLAY_TEST_PRODUCT_REFUNDED)
                        ||sku.equals(GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE)
                        )
                    {
                        productBuyOnServer(sku);
                    }
                }
            }
        }
    };

    // Восстанавливаем покупаемость продукта;
    private IabHelper.QueryInventoryFinishedListener mResetConsumableListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure())
            {
                Log.e("Problem setting up in-app billing: " + result);
            }
            else
            {
                List<Purchase> purchases = inventory.getPurchasesList();
                if(purchases.size()>0)
                {
                    for(Purchase purchase : purchases)
                    {
                        NavigationActivity.debug("CONSUME: "+purchase.getSku());
                    }
                    mHelper.consumeAsync(purchases, mConsumeMyltyFinishedListener);
                }
            }
        }
    };

    private IabHelper.OnConsumeMultiFinishedListener mConsumeMyltyFinishedListener = new IabHelper.OnConsumeMultiFinishedListener()
    {
        @Override
        public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {

            for(Purchase purchase : purchases)
            {
                @Nonnull PurchasePrizeWord product = mIPurchaseSetModel.getPurchase(purchase.getSku());
                product.googlePurchase = false;
                mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);
            }
        }
    };

    // Получаем информацию с Google Play о продуктах in-app;
    private IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isFailure()) {
                Log.e("Problem setting up in-app billing: " + result);
                mINavigationActivity.sendMessage("Problem setting up in-app billing: " + result );
            }
            else
            {
                @Nonnull ArrayList<PurchasePrizeWord> purchases = new ArrayList<PurchasePrizeWord>();
                for(@Nonnull String googleId : mSkuContainer)
                {
                    if(inventory.hasDetails(googleId))
                    {
                        @Nullable PurchasePrizeWord purchase = mIPurchaseSetModel.getPurchase(googleId);
                        purchase.price = inventory.getSkuDetails(googleId).getPrice();
                        purchase.clientId = DEVICE_ID;
                        purchases.add(purchase);
                    }
                }

                // сохраняем цены в базу;
                mIPurchaseSetModel.putPurchases(purchases, mSavePurchasesToDataBase);
            }
            mNotifyInventoryHandler.handle();
        }
    };

    // Ответ с результатом выполненой покупки;
    private IabHelper.OnIabPurchaseFinishedListener mBuyFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            int resultCode = result.getResponse();
            if (result.isFailure())
            {
                parseResultCode(result, purchase);
                return;
            }

            int responseState = purchase.getPurchaseState();
            String responseSku = purchase.getSku();
            String responseClientId = purchase.getDeveloperPayload();
            String responseSignature = purchase.getSignature();
            @Nonnull String responseJson = purchase.getOriginalJson();

            // Меняем состояние продукта, что он был куплен в Google PLay и следует совершить покупку на сервере и восстановить покупаемость, если надо;
            @Nullable PurchasePrizeWord product = mIPurchaseSetModel.getPurchase(responseSku);
            product.googlePurchase = true;
            product.serverPurchase = true;
            product.receipt_data = responseJson;
            product.signature = responseSignature;
            mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);

            NavigationActivity.debug("RECEIPT_DATA:"+responseJson);
            NavigationActivity.debug("SIGNATURE:"+responseSignature);

            verifyControllPurchases();
        }
    };

    @Override
    public void productBuyOnServer(@Nonnull String sku)
    {
        // Меняем состояние товара;
        @Nullable PurchasePrizeWord product = mIPurchaseSetModel.getPurchase(sku);
        product.serverPurchase = false;
        mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);

        // Восстанавливаем покупаемость товара;
        verifyControllPurchases();
    }

    private @Nonnull IListener<Bundle> mHandlerBuyProductEvent = new IListener<Bundle>() {
        @Override
        public void handle(@Nullable Bundle bundle) {

            for(IListener<Bundle> handle : mHandlerBuyProductEventList)
            {
                handle.handle(bundle);
            }
        }
    };

    private @Nonnull IListenerVoid mHandlerPriceEvent = new IListenerVoid() {
        @Override
        public void handle() {

            NavigationActivity.debug("notify price: "+mHandlerReloadPriceList.size());
            for(IListenerVoid handle : mHandlerReloadPriceList)
            {
                handle.handle();
            }
        }
    };

    private @Nonnull IListenerVoid mReloadPurchaseFromDataBase = new IListenerVoid() {
        @Override
        public void handle() {

        }
    };

    private @Nonnull IListenerVoid mSavePurchasesToDataBase = new IListenerVoid() {
        @Override
        public void handle() {
            // уведзобляем подписчиков, что пришли цены;
            mHandlerPriceEvent.handle();
            verifyControllPurchases();
        }
    };

    private @Nonnull IListenerVoid mSaveOnePurchaseToDataBase = new IListenerVoid() {
        @Override
        public void handle() {

        }
    };

    private void verifyControllPurchases()
    {
        @Nonnull PurchasePrizeWord purchase = null;
        for(@Nonnull String sku : mSkuContainer)
        {
            purchase = mIPurchaseSetModel.getPurchase(sku);
            if(purchase!=null)
            {
                if(purchase.googlePurchase == true)
                {
//                        && purchase.serverPurchase == false
                    if (
                            sku.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_10)
                            ||sku.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_20)
                            ||sku.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_30)
                            ||sku.equals(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS)
                            ||sku.equals(GOOGLE_PLAY_TEST_PRODUCT_CANCEL)
                            ||sku.equals(GOOGLE_PLAY_TEST_PRODUCT_REFUNDED)
                            ||sku.equals(GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE)
                            )
                    {
                        // Состояние продукта: быд куплен на Google Play, но не восстановлен как продукт готовый к повторной покупке;
                        // Восстанавливаем покупаемость товара;
                        // Отправляем запрос на получие информации о продуктах приложения на Google Play;
                        try
                        {
                            @Nonnull List<String> list = new ArrayList<String>(1);
                            list.add(sku);
                            mHelper.queryInventoryAsync(true, list, mResetConsumableListener);
                        }
                        catch (IllegalStateException e)
                        {
                            Log.e(e.getMessage());
                        }
                        catch (Exception e)
                        {
                            Log.e(e.getMessage());
                        }
                    }
                    else
                    {
                        @Nullable PurchasePrizeWord product = mIPurchaseSetModel.getPurchase(sku);
                        product.googlePurchase = false;
                        mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);
                    }
                }
                if(purchase.serverPurchase == true)
                {
                    // Состояние продукта: был куплен на Google Play, но еще не прошел запрос покупки на сервере;
                    // Рассылаем уведомления подписчикам, что продукт был успешно куплен на GooglePlay;
                    @Nonnull String json = purchase.receipt_data;
                    @Nonnull String signature = purchase.signature;
                    @Nonnull Bundle bundle = packToBundle(sku,json,signature);
                    mHandlerBuyProductEvent.handle(bundle);
                }
            }
        }
    }

    private boolean parseResultCode(IabResult result, Purchase purchase)
    {
        int resultCode = result.getResponse();
        switch (resultCode)
        {
            case IabHelper.BILLING_RESPONSE_RESULT_OK:
//                OK/-1001:Remote exception during initialization/" +
                return true;
            case IabHelper.IABHELPER_ERROR_BASE:
                break;
            case IabHelper.IABHELPER_REMOTE_EXCEPTION:
                break;
            case IabHelper.IABHELPER_BAD_RESPONSE:
//                Bad response received/" +
                break;
            case IabHelper.IABHELPER_VERIFICATION_FAILED:
//                PurchasePrizeWord signature verification failed/" +
                break;
            case IabHelper.IABHELPER_SEND_INTENT_FAILED:
//                Send intent failed/" +
                break;
            case IabHelper.IABHELPER_USER_CANCELLED:
//                User cancelled/" +
                break;
            case IabHelper.IABHELPER_UNKNOWN_PURCHASE_RESPONSE:
//                Unknown purchase response/" +
                break;
            case IabHelper.IABHELPER_MISSING_TOKEN:
//                Missing token/" +
                break;
            case IabHelper.IABHELPER_UNKNOWN_ERROR:
//                Unknown error/" +
                mINavigationActivity.sendMessage("Error purchasing: " + result);

                break;
            case IabHelper.IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE:
//                    Subscriptions not available/" +
                break;
            case IabHelper.IABHELPER_INVALID_CONSUMPTION:
//                    Invalid consumption attempt").split("/");
                break;
            case IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED:
                break;
            case IabHelper.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                break;
            case IabHelper.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
                break;
            case IabHelper.BILLING_RESPONSE_RESULT_ERROR:
                break;
            case IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
                mINavigationActivity.sendMessage(mContext.getResources().getString(R.string.manage_purshase_already_owned));
                break;
            case IabHelper.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
                break;
            default:
                mINavigationActivity.sendMessage("Unknown error: " + result);
                break;
        }
        Log.e("Error purchasing: " + result);
        return false;
    }

    static public Bundle packToBundle(@Nonnull String sku, @Nonnull String json, @Nonnull String signature)
    {
        @Nonnull Bundle bundle = new Bundle();
        bundle.putString(BF_SKU, sku);
        bundle.putString(BF_JSON, json);
        bundle.putString(BF_SIGNATURE, signature);
        return bundle;
    }

    static public @Nonnull String extractFromBundleSKU(@Nonnull Bundle bundle)
    {
        @Nonnull String sku = bundle.getString(BF_SKU);
        return sku;
    }

    static public @Nonnull String extractFromBundleJson(@Nonnull Bundle bundle)
    {
        @Nonnull String googleId = bundle.getString(BF_JSON);
        return googleId;
    }

    static public @Nonnull String extractFromBundleSignature(@Nonnull Bundle bundle)
    {
        @Nonnull String googleId = bundle.getString(BF_SIGNATURE);
        return googleId;
    }

}
