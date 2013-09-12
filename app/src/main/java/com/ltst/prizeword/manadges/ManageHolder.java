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
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.HintsModel;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.tools.UUIDTools;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 28.08.13.
 */
public class ManageHolder implements IManageHolder, IIabHelper {

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

//    private final @Nonnull String APP_GOOGLE_PLAY_ID = "4a6bbda29147dab10d4928f5df3a2bfc3d9b0bdb";
    private final @Nonnull String APP_GOOGLE_PLAY_ID = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtspobFVSi6fZ6L3q5l64JVVcJaK19gVWllXQi5FxaN1V0Yti84O+Xzuw7fWWnrgleKLRNSMPrOd/rQrDAHhEm9kk7gq0PUzLwOzpqgnvWa9fsvQVc5jOi69O7B2Vn+KftNQ+VXReFXpEp4IA6DKIu3f0gNqha/szA2eq1uDyO+MXtU9Kpz2XeAedpVNSMn9OEDR2U4rN39GUqumg0NwidbpCkfhbmSGYoJOPAUOIXf5J1YIeR75pBV2GCUiT4d8fCGCv/UMunTbNkI+BjDov/hmzU4njk1sIlSSpz0a9pM4v6Q2dIrrKIrOsjSI7r+c/C2U2dqviAUZ96tYDS+bp7wIDAQAB";

    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_10           = "hints10";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_20           = "hints21";
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
    private @Nonnull List<IListener<ManageHolder.ManadgeProduct>> mHandlerBuyProductEventList;
    private @Nonnull String mSessionKey;
    private @Nonnull HintsModel mHintsModel;
    private @Nonnull BuyCrosswordSetModel mBuyCrosswordSetModel;


    public ManageHolder(@Nonnull Activity activity, @Nonnull IBcConnector bcConnector) {

        mActivity = activity;
        mContext = (Context) activity;
        mIManadges = (IManadges) activity;
        mBcConnector = bcConnector;
        mIPurchaseSetModel = new PurchaseSetModel(mBcConnector);
        mPrices = new HashMap<ManadgeProduct, String>();
        mHandlerReloadPriceList = new ArrayList<IListenerVoid>();
        mHandlerBuyProductEventList = new ArrayList<IListener<ManageHolder.ManadgeProduct>>();

//        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);


//        // Ответ о покупке;
//        mIabHelper.queryInventoryAsync(mGotInventoryListener);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        return mIabHelper.handleActivityResult(requestCode, resultCode, data);
    }

    public void instance()
    {
//        try{
        mIabHelper = new IabHelper(mContext,APP_GOOGLE_PLAY_ID);
//        }
//        catch (Exception e)
//        {
//            Log.e(e.getMessage());
//            Log.i("Can't use license public key of app"); //$NON-NLS-1$
//        }
        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    // Получаем цены с сервера;
                    reloadPoductsFromGooglePlay();
                }
            }
        });
        reloadProductsFromDataBase();
    }

    @Override
    public void resume()
    {
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mHintsModel = new HintsModel(mBcConnector, mSessionKey);
        mBuyCrosswordSetModel = new BuyCrosswordSetModel(mBcConnector, mSessionKey);
    }

    @Override
    public void pause()
    {
        mHintsModel.close();
        mBuyCrosswordSetModel.close();
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

    static @Nonnull String extractProductId(ManageHolder.ManadgeProduct product)
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

    static int extractProductRequest(ManageHolder.ManadgeProduct product)
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

    static @Nonnull ManageHolder.ManadgeProduct extractProduct(@Nonnull String googleId)
    {
        if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
            return ManadgeProduct.hints10;
        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
            return ManadgeProduct.hints20;
        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
            return ManadgeProduct.hints30;
        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_SET_BRILLIANT))
            return ManadgeProduct.set_brilliant;
        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_SET_GOLD))
            return ManadgeProduct.set_gold;
        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_SET_SILVER))
            return ManadgeProduct.set_silver;
        else if(googleId.equals(GOOGLE_PLAY_PRODUCT_ID_SET_SILVER2))
            return ManadgeProduct.set_silver2;
        else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
            return ManadgeProduct.test_success;
        else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_CANCEL))
            return ManadgeProduct.test_cancel;
        else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_REFUNDED))
            return ManadgeProduct.test_refunded;
        else if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE))
            return ManadgeProduct.test_unavailable;
        return null;
    }

    @Override
    public void buyCrosswordSet(@Nonnull ManageHolder.ManadgeProduct product, @Nonnull String crosswordSetServerId)
    {

    }


    public void buyProduct(ManageHolder.ManadgeProduct product)
    {
        // Start popup window. Покупка;
        @Nonnull String product_id = extractProductId(product);
        int  product_request = extractProductRequest(product);
        @Nonnull String token = UUIDTools.generateStringUUID();
        if(product_id!=null && product_request!=0){
            try {
                mIabHelper.flagEndAsync();
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

    public void registerHandlerBuyProductEvent(@Nonnull IListener<ManadgeProduct> handler)
    {
        mHandlerBuyProductEventList.add(handler);
    }

    public void reloadProductsFromDataBase()
    {
        mIPurchaseSetModel.reloadPurchases(mReloadPurchaseFromDataBase);
    }

    public void reloadPoductsFromGooglePlay()
    {
        // Отправляем запрос на получие информации о продуктах приложения на Google Play;
        List<String> additionalSkuList = new ArrayList<String>();
        for(ManadgeProduct product : ManadgeProduct.values())
        {
            additionalSkuList.add(extractProductId(product));
        }
        mIabHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
    }

    public String getPriceProduct(ManageHolder.ManadgeProduct product)
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

                        @Nonnull com.ltst.prizeword.manadges.Purchase purchase = mIPurchaseSetModel.getPurchase(prodict_id);
                        purchase.googlePurchase = false;
                        mIPurchaseSetModel.putOnePurchase(purchase, new IListenerVoid() {
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

    IabHelper.OnConsumeMultiFinishedListener mConsumeMyltyFinishedListener = new IabHelper.OnConsumeMultiFinishedListener()
    {
        @Override
        public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {

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

////            Восстанавливаем возможность сделать повторную покупку продукта;
//            Purchase pc = null;
//            List<Purchase> lst = new ArrayList<Purchase>(3);
//            pc = inventory.getPurchase(extractProductId(hints10));
//            lst.add(pc);
//            pc = inventory.getPurchase(extractProductId(hints20));
//            lst.add(pc);
//            pc = inventory.getPurchase(extractProductId(hints30));
//            lst.add(pc);
//            mIabHelper.consumeAsync(lst, mConsumeMyltyFinishedListener);

            @Nonnull ArrayList<com.ltst.prizeword.manadges.Purchase> purchases = new ArrayList<com.ltst.prizeword.manadges.Purchase>();
            @Nonnull String prodict_id = null;
            for(ManadgeProduct product : ManadgeProduct.values())
            {
                prodict_id = extractProductId(product);
                if(inventory.hasDetails(prodict_id))
                {
                    @Nonnull String price = inventory.getSkuDetails(prodict_id).getPrice();
                    mPrices.put(product, price);
                    @Nullable com.ltst.prizeword.manadges.Purchase purchase = mIPurchaseSetModel.getPurchase(prodict_id);
                    purchase.price = inventory.getSkuDetails(prodict_id).getPrice();
                    purchase.clientId = UUIDTools.generateStringUUID();
                    purchases.add(purchase);
                }
            }

            // сохраняем цены в базу;
            mIPurchaseSetModel.putPurchases(purchases, mSavePurchasesToDataBase);
        }
    };

    // Ответ с результатом выполненой покупки;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
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

//                mIabHelper.flagEndAsync();
                return;
            }

            // Восстанавливаем возможность сделать повторную покупку продукта;
            mIabHelper.consumeAsync(purchase,mConsumeFinishedListener);

            int responseState = purchase.getPurchaseState();
            String responseGoogleId = purchase.getSku();
            String responseClientId = purchase.getDeveloperPayload();
            String responseSignature = purchase.getSignature();
            @Nonnull String responseJson = purchase.getOriginalJson();

//            verify(APP_GOOGLE_PLAY_ID, data, responseSignature);

            @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(responseGoogleId);
            product.googlePurchase = true;
            product.clientId = responseClientId;
            mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);

            ManageHolder.ManadgeProduct prod = extractProduct(responseGoogleId);
            if(prod !=null)
            {
                switch (prod)
                {
                    case test_success:
                    case hints10:
                    case hints20:
                    case hints30:
                        changeHintsCount(prod);
                        break;
                    case set_brilliant:
                    case set_gold:
                    case set_silver:
                    case set_silver2:
                    case set_free:
                        changeCrosswordSet(prod, responseGoogleId, responseJson, responseSignature);
                        break;
//                    case test_success:
//                        break;
                    case test_cancel:
                        break;
                    case test_refunded:
                        break;
                    case test_unavailable:
                        break;
                    default:break;
                }

            }
        }
    };

    private void changeHintsCount(final ManageHolder.ManadgeProduct prod)
    {
        int count = 0;
        switch (prod)
        {
            case test_success:
            case hints10:
                count = 10;
                break;
            case hints20:
                count = 20;
                break;
            case hints30:
                count = 30;
                break;
            default:
                return;
        }

        mHintsModel.changeHints(count, new IListenerVoid() {
            @Override
            public void handle() {

                // Меняем состояние товара;
                @Nonnull String googleId = extractProductId(prod);
                @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(googleId);
                product.googlePurchase = false;
                mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);

                // Рассылаем уведомления, что покупка прошла успешно;
                mBuyProductEventHandler.handle(prod);
            }
        });
    }

    private void changeCrosswordSet(final @Nonnull ManageHolder.ManadgeProduct prod,
                                    @Nonnull String responseGoogleId,
                                    @Nonnull String responseJson,
                                    @Nonnull String responseSignature)
    {
        switch (prod)
        {
            case set_brilliant:
                break;
            case set_gold:
                break;
            case set_silver:
                break;
            case set_silver2:
                break;
            case set_free:
                break;
            default:
                return;
        }

        mBuyCrosswordSetModel.buyCrosswordSet(responseGoogleId, responseJson, responseSignature, new IListenerVoid(){

            @Override
            public void handle() {

                // Меняем состояние товара;
                @Nonnull String googleId = extractProductId(prod);
                @Nullable com.ltst.prizeword.manadges.Purchase product = mIPurchaseSetModel.getPurchase(googleId);
                product.googlePurchase = false;
                mIPurchaseSetModel.putOnePurchase(product, mSaveOnePurchaseToDataBase);

                // Рассылаем уведомления, что покупка прошла успешно;
                mBuyProductEventHandler.handle(prod);

            }
        });

    }

    @Nonnull IListener<ManageHolder.ManadgeProduct> mBuyProductEventHandler = new IListener<ManadgeProduct>() {
        @Override
        public void handle(@Nullable ManadgeProduct manadgeProduct) {

            for(IListener<ManageHolder.ManadgeProduct> handle : mHandlerBuyProductEventList)
            {
                Log.d("PRICE SEND DONE!");
                handle.handle(manadgeProduct);
            }
        }
    };

    @Nonnull IListenerVoid mReloadPurchaseFromDataBase = new IListenerVoid() {
        @Override
        public void handle() {

        }
    };

    @Nonnull IListenerVoid mSavePurchasesToDataBase = new IListenerVoid() {
        @Override
        public void handle() {

        }
    };

    @Nonnull IListenerVoid mSaveOnePurchaseToDataBase = new IListenerVoid() {
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

    public ManageHolder.ManadgeProduct extractManadgeProductFromPuzzleSetType(PuzzleSetModel.PuzzleSetType type)
    {
        switch (type)
        {
            case BRILLIANT: return ManageHolder.ManadgeProduct.set_brilliant;
            case GOLD: return ManageHolder.ManadgeProduct.set_gold;
            case SILVER: return ManageHolder.ManadgeProduct.set_silver;
            case SILVER2: return ManageHolder.ManadgeProduct.set_silver2;
            case FREE: return ManageHolder.ManadgeProduct.set_free;
            default:break;
        }
        return null;
    }

}
