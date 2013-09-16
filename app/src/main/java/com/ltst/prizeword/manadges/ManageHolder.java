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
    private @Nonnull IManadges mIManadges;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull IPurchaseSetModel mIPurchaseSetModel;
    private @Nonnull List<String> mGoogleIdContainer;
    private @Nonnull List<IListenerVoid> mHandlerReloadPriceList;
    private @Nonnull List<IListener<Bundle>> mHandlerBuyProductEventList;

    private int REQUERT_CODE_COUNTER;


    public ManageHolder(@Nonnull Activity activity, @Nonnull IBcConnector bcConnector) {
        mActivity = activity;
        mContext = (Context) activity;
        mIManadges = (IManadges) activity;
        mBcConnector = bcConnector;
        mIPurchaseSetModel = new PurchaseSetModel(mBcConnector);
        mHandlerReloadPriceList = new ArrayList<IListenerVoid>();
        mHandlerBuyProductEventList = new ArrayList<IListener<Bundle>>();
        mGoogleIdContainer = new ArrayList<String>();
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
                    // Получаем список товаров, которые были куплены пользователем;
                    mHelper.queryInventoryAsync(mGotInventoryListener);
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
        @Nonnull String token = UUIDTools.generateStringUUID();
        if(sku != Strings.EMPTY){
            try {
                mHelper.flagEndAsync();
                mHelper.launchPurchaseFlow(mActivity, sku, ++REQUERT_CODE_COUNTER, mPurchaseFinishedListener, token);
            } catch (Exception e)
            {
                Log.e(e.getMessage());
            }
        }
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
        if(sku == Strings.EMPTY || mGoogleIdContainer.contains(sku))
            return;
        mGoogleIdContainer.add(sku);
    }

    public void reloadInventoryFromDataBase()
    {
        mIPurchaseSetModel.reloadPurchases(mReloadPurchaseFromDataBase);
    }

    @Override
    public void reloadInventory()
    {
        // Отправляем запрос на получие информации о продуктах приложения на Google Play;
        mHelper.queryInventoryAsync(true, mGoogleIdContainer, mQueryFinishedListener);
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
                // Восстанавливаем покупку подсказок, если по какой-дибо причине это небыло сделано ранее;
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
                {
                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10), mConsumeFinishedListener);
                }
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
                {
                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20), mConsumeFinishedListener);
                }
                if(inventory.hasPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
                {
                    mHelper.consumeAsync(inventory.getPurchase(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30), mConsumeFinishedListener);
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
                String responseGoogleId = purchase.getSku();
                String responseClientId = purchase.getDeveloperPayload();
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
                return;
            }

            @Nonnull ArrayList<com.ltst.prizeword.manadges.Purchase> purchases = new ArrayList<com.ltst.prizeword.manadges.Purchase>();
            for(@Nonnull String googleId : mGoogleIdContainer)
            {
                if(inventory.hasDetails(googleId))
                {
                    @Nullable com.ltst.prizeword.manadges.Purchase purchase = mIPurchaseSetModel.getPurchase(googleId);
                    purchase.price = inventory.getSkuDetails(googleId).getPrice();
                    purchase.clientId = UUIDTools.generateStringUUID();
                    purchases.add(purchase);
                }
            }

            // сохраняем цены в базу;
            mIPurchaseSetModel.putPurchases(purchases, mSavePurchasesToDataBase);
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
                    case -1005:
                        // User canceled. (response: -1005:User cancelled);
                        break;
                    case -1008:
                        // IAB returned null purchaseData or dataSignature (response: -1008:Unknown error);
                        break;
                    default:
                        break;
                }

//                mHelper.flagEndAsync();
                return;
            }

            // Восстанавливаем покупки подсказок;
            if(purchase != null)
            {
                @Nonnull String sku = purchase.getSku();
                if(sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
                {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                }
                if(sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
                {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                }
                if(sku.equals(HintsManager.GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
                {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                }
            }


            int responseState = purchase.getPurchaseState();
            String responseSku = purchase.getSku();
            String responseClientId = purchase.getDeveloperPayload();
            String responseSignature = purchase.getSignature();
            @Nonnull String responseJson = purchase.getOriginalJson();

//            verify(APP_GOOGLE_PLAY_ID, data, responseSignature);

            @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(responseSku);
            product.googlePurchase = true;
            product.clientId = responseClientId;
            mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);

            // рассылаем уведомления, что продукт куплен на GooglePlay;
            notifyBuyOnServer(responseSku, responseJson, responseSignature);
        }
    };

    @Override
    public void productBuyOnGooglePlay(@Nonnull String sku)
    {
        // Меняем состояние товара;
        @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(sku);
        product.googlePurchase = true;
        mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);
    }

    @Override
    public void productBuyOnServer(@Nonnull String sku)
    {
        // Меняем состояние товара;
        @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(sku);
        product.googlePurchase = false;
        mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);
    }

    private void notifyBuyOnServer(final @Nonnull String responseSku,
                                   final @Nonnull String responseJson,
                                   final @Nonnull String responseSignature)
    {
        // Рассылаем уведомления, что покупка прошла успешно;
        @Nonnull Bundle bundle = packToBundle(responseSku,responseJson,responseSignature);
        mBuyProductEventHandler.handle(bundle);
    }

    private @Nonnull IListener<Bundle> mBuyProductEventHandler = new IListener<Bundle>() {
        @Override
        public void handle(@Nullable Bundle bundle) {

            for(IListener<Bundle> handle : mHandlerBuyProductEventList)
            {
                handle.handle(bundle);
            }
        }
    };

    private @Nonnull IListenerVoid mPriceEventHandler = new IListenerVoid() {
        @Override
        public void handle() {

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
            mPriceEventHandler.handle();
        }
    };

    private @Nonnull IListenerVoid mSaveOnePurchaseToDataBase = new IListenerVoid() {
        @Override
        public void handle() {

        }
    };

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
