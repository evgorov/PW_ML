package com.ltst.prizeword.manadges;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.billing.Base64;
import com.android.billing.Base64DecoderException;
import com.android.billing.IabHelper;
import com.android.billing.IabResult;
import com.android.billing.Inventory;
import com.android.billing.Purchase;
import com.android.billing.Security;
import com.ltst.prizeword.crossword.view.HintsManager;
import com.ltst.prizeword.navigation.INavigationActivity;
import com.ltst.prizeword.navigation.NavigationActivity;
import com.ltst.prizeword.tools.UUIDTools;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 28.08.13.
 */
public class ManageHolder implements IManageHolder, IIabHelper {

    final static public @Nonnull String BF_SKU          = "ManageHolder.sku";
    final static public @Nonnull String BF_JSON         = "ManageHolder.json";
    final static public @Nonnull String BF_SIGNATURE    = "ManageHolder.signature";

//    private final @Nonnull String APP_GOOGLE_PLAY_ID = "4a6bbda29147dab10d4928f5df3a2bfc3d9b0bdb";
    private final @Nonnull String APP_GOOGLE_PLAY_ID = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtspobFVSi6fZ6L3q5l64JVVcJaK19gVWllXQi5FxaN1V0Yti84O+Xzuw7fWWnrgleKLRNSMPrOd/rQrDAHhEm9kk7gq0PUzLwOzpqgnvWa9fsvQVc5jOi69O7B2Vn+KftNQ+VXReFXpEp4IA6DKIu3f0gNqha/szA2eq1uDyO+MXtU9Kpz2XeAedpVNSMn9OEDR2U4rN39GUqumg0NwidbpCkfhbmSGYoJOPAUOIXf5J1YIeR75pBV2GCUiT4d8fCGCv/UMunTbNkI+BjDov/hmzU4njk1sIlSSpz0a9pM4v6Q2dIrrKIrOsjSI7r+c/C2U2dqviAUZ96tYDS+bp7wIDAQAB";

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
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        return mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void instance()
    {
//        try{
        mHelper = new IabHelper(mContext,APP_GOOGLE_PLAY_ID);
//        }
//        catch (Exception e)
//        {
//            Log.e(e.getMessage());
//            Log.i("Can't use license public key of app"); //$NON-NLS-1$
//        }
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.e("Problem setting up in-app billing: " + result);
                }
                else
                {
//                    // Получаем список товаров, которые были куплены пользователем;
//                    mHelper.queryInventoryAsync(mGotInventoryListener);

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
    public void buyProduct(@Nonnull String sku)
    {
        // Start popup window. Покупка;

        if(sku != Strings.EMPTY){
            @Nonnull com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(sku);
            @Nonnull String token = (product.clientId==Strings.EMPTY || product.clientId==null)
                    ? UUIDTools.generateStringUUID()
                    : product.clientId;
            product.clientId = token;

            try
            {
                mHelper.flagEndAsync();
                mHelper.launchPurchaseFlow(mActivity, sku, ++REQUERT_CODE_COUNTER, mPurchaseFinishedListener, token);
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
        @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(sku);
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
        mHelper.queryInventoryAsync(true, mSkuContainer, mQueryFinishedListener);
    }

    @Override
    public String getPriceProduct(@Nonnull String sku)
    {
        com.ltst.prizeword.manadges.Purchase purchase = mIPurchaseSetModel.getPurchase(sku);
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

                @Nonnull List<String> list = new ArrayList<String>();
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
                {
                    list.add(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10);
                }
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
                {
                    list.add(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20);
                }
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
                {
                    list.add(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30);
                }
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
                {
                    list.add(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS);
                }
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE))
                {
                    list.add(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE);
                }
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED))
                {
                    list.add(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED);
                }
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_CANCEL))
                {
                    list.add(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_CANCEL);
                }
                // Состояние продукта: быд куплен на Google Play, но не восстановлен как продукт готовый к повторной покупке;
                // Восстанавливаем покупаемость товара;
                // Отправляем запрос на получие информации о продуктах приложения на Google Play;
                if(list.size()>0)
                {
                    mHelper.queryInventoryAsync(true, list, mResetConsumableListener);
                }
            }
        }
    };

    private IabHelper.QueryInventoryFinishedListener mResetConsumableListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure())
            {
                Log.e("Problem setting up in-app billing: " + result);
            }
            else
            {
//                // Восстанавливаем покупку подсказок, если по какой-дибо причине это небыло сделано ранее;
//                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
//                {
//                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10), mConsumeFinishedListener);
//                }
//                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
//                {
//                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20), mConsumeFinishedListener);
//                }
//                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
//                {
//                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30), mConsumeFinishedListener);
//                }
//                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
//                {
//                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS), mConsumeFinishedListener);
//                }
//                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_CANCEL))
//                {
//                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_CANCEL), mConsumeFinishedListener);
//                }
//                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED))
//                {
//                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED), mConsumeFinishedListener);
//                }
//                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE))
//                {
//                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE), mConsumeFinishedListener);
//                }


                @Nonnull List<Purchase> list = new ArrayList<Purchase>();
                for(@Nonnull String sku : mSkuContainer)
                {
                    if(inventory.hasPurchase(sku));
                    {
                        @Nullable Purchase purchase = inventory.getPurchase(sku);
                        if(purchase != null)
                        {
                            list.add(inventory.getPurchase(sku));
                        }
                    }
                }
                if(list.size()>0)
                {
                    mHelper.consumeAsync(list, mConsumeMyltyFinishedListener);
                }
            }
        }
    };

    private IabHelper.OnConsumeMultiFinishedListener mConsumeMyltyFinishedListener = new IabHelper.OnConsumeMultiFinishedListener()
    {
        @Override
        public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {

        }
    };

    // Получаем информацию о возобновлении покупаемости товара;
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener()
    {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                // provision the in-app purchase to the user
                // (for example, credit 50 gold coins to player's character)
                int responseState = purchase.getPurchaseState();
                String responseSku = purchase.getSku();
                String responseClientId = purchase.getDeveloperPayload();

                // Восстанавливаем состояние покупки в базе для Google Play как не купленная;
                @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(responseSku);
                product.googlePurchase = false;
                mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);
            }
            else {
                Log.e("Problem setting up in-app billing: " + result);
            }
        }
    };

    // Получаем информацию с Google Play о продуктах in-app;
    private IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isFailure()) {
                Log.e("Problem setting up in-app billing: " + result);
            }
            else
            {

//            @Nonnull List<String> list = new ArrayList<String>();
//            if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
//            {
//                list.add(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10);
//            }
//            if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
//            {
//                list.add(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20);
//            }
//            if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
//            {
//                list.add(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30);
//            }
//            if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
//            {
//                list.add(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS);
//            }
//            if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE))
//            {
//                list.add(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE);
//            }
//            if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED))
//            {
//                list.add(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED);
//            }
//            if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_CANCEL))
//            {
//                list.add(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_CANCEL);
//            }
//            // Состояние продукта: быд куплен на Google Play, но не восстановлен как продукт готовый к повторной покупке;
//            // Восстанавливаем покупаемость товара;
//            // Отправляем запрос на получие информации о продуктах приложения на Google Play;
//            if(list.size()>0)
//            {
//                mHelper.queryInventoryAsync(true, list, mResetConsumableListener);
//            }

                NavigationActivity.debug("before inventory query on google play: "+mSkuContainer.size());

                // Восстанавливаю покупаемость всех продуктов! REMOVE IN THE FUTURE!
                @Nonnull List<Purchase> list = new ArrayList<Purchase>();
                for(@Nonnull String sku : mSkuContainer)
                {
                    if(inventory.hasPurchase(sku));
                    {
                        @Nullable Purchase purchase = inventory.getPurchase(sku);
                        if(purchase != null)
                        {
                            list.add(inventory.getPurchase(sku));
                        }
                    }
                }
                if(list.size()>0)
                {
                    mHelper.consumeAsync(list, mConsumeMyltyFinishedListener);
                }

                @Nonnull ArrayList<com.ltst.prizeword.manadges.Purchase> purchases = new ArrayList<com.ltst.prizeword.manadges.Purchase>();
                for(@Nonnull String googleId : mSkuContainer)
                {
                    if(inventory.hasDetails(googleId))
                    {
                        @Nullable com.ltst.prizeword.manadges.Purchase purchase = mIPurchaseSetModel.getPurchase(googleId);
                        purchase.price = inventory.getSkuDetails(googleId).getPrice();
                        purchase.clientId = (purchase.clientId == null || purchase.clientId == Strings.EMPTY)
                                ? UUIDTools.generateStringUUID() : purchase.clientId;
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
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            int resultCode = result.getResponse();
            if (result.isFailure())
            {
                Log.e("Error purchasing: " + result);
                switch (resultCode)
                {
                    case 0:
                        // Sussessfull;
                        break;
                    case -1005:
                        // User canceled. (response: -1005:User cancelled);
                        break;
                    case -1008:
                        // IAB returned null purchaseData or dataSignature (response: -1008:Unknown error);
                        mINavigationActivity.sendMessage("Error purchasing: " + result);
                        break;
                    default:
                        mINavigationActivity.sendMessage("Unknown error: " + result);
                        break;
                }
                return;
            }

            // Восстанавливаем покупки подсказок;
            if(purchase != null)
            {
//                @Nonnull String sku = purchase.getSku();
//                if(sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
//                {
//                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//                }
//                if(sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
//                {
//                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//                }
//                if(sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
//                {
//                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//                }
//                if(sku.equals(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
//                {
//                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//                }
//                if(sku.equals(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED))
//                {
//                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//                }
//                if(sku.equals(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_CANCEL))
//                {
//                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//                }
//                if(sku.equals(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE))
//                {
//                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//                }
//                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }


            int responseState = purchase.getPurchaseState();
            String responseSku = purchase.getSku();
            String responseClientId = purchase.getDeveloperPayload();
            String responseSignature = purchase.getSignature();
            @Nonnull String responseJson = purchase.getOriginalJson();

//            verify(APP_GOOGLE_PLAY_ID, data, responseSignature);

            // Меняем состояние продукта, что он был куплен в Google PLay и следует совершить покупку на сервере и восстановить покупаемость, если надо;
            @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(responseSku);
            product.googlePurchase = true;
            product.serverPurchase = true;
            product.receipt_data = responseJson;
            product.signature = responseSignature;
            mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);

            verifyControllPurchases();
        }
    };

    @Override
    public void productBuyOnServer(@Nonnull String sku)
    {
        // Меняем состояние товара;
        @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(sku);
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
        @Nonnull com.ltst.prizeword.manadges.Purchase purchase = null;
        for(@Nonnull String sku : mSkuContainer)
        {
            purchase = mIPurchaseSetModel.getPurchase(sku);
            if(purchase!=null)
            {
                if(purchase.googlePurchase == true && purchase.serverPurchase == false
//                        && (
//                        sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10)
//                                ||sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20)
//                                ||sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30)
//                                ||sku.equals(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS)
//                                ||sku.equals(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_CANCEL)
//                                ||sku.equals(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED)
//                                ||sku.equals(HintsManager.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE)
//                            )
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
//                if(purchase.googlePurchase == true && purchase.serverPurchase == true)
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

    public static boolean verify(@Nonnull String base64EncodedPublicKey, String signedData, String signature)
    {
        @Nonnull PublicKey publicKey = Security.generatePublicKey(base64EncodedPublicKey);
        @Nonnull String SIGNATURE_ALGORITHM = "SHA1withRSA";
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(Base64.decode(signature))) {
                Log.e("Signature verification failed.");
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            Log.e("NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            Log.e("Invalid key specification.");
        } catch (SignatureException e) {
            Log.e("Signature exception.");
        } catch (Base64DecoderException e) {
            Log.e("Base64 decoding failed.");
        }
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
