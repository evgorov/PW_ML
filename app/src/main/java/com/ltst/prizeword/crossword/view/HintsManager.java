package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.ltst.prizeword.R;
import com.ltst.prizeword.manadges.IManageHolder;
import com.ltst.prizeword.sounds.SoundsWork;
import com.ltst.prizeword.manadges.IManadges;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HintsManager implements View.OnClickListener
{
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_10           = "hints10";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_20           = "hints21";
    static private final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_30           = "hints30";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_SUCCESS          = "android.test.purchased";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_CANCEL           = "android.test.canceled";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_REFUNDED         = "android.test.refunded";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE      = "android.test.unavailable";


    private @Nonnull TextView mHintsCountView;
    private @Nonnull View mBuyHints_10;
    private @Nonnull View mBuyHints_20;
    private @Nonnull View mBuyHints_30;
    private @Nonnull TextView mPriceHints_10;
    private @Nonnull TextView mPriceHints_20;
    private @Nonnull TextView mPriceHints_30;

    private @Nonnull IManageHolder mIManageHolder;

    private @Nonnull IManadges mIManadges;
    private @Nonnull Context mContext;

    private int mHintsCount;

    public HintsManager(@Nonnull Context context, View parentView)
    {
        mContext = context;

        mIManadges = (IManadges) context;
        mIManageHolder = mIManadges.getManadgeHolder();
        mIManageHolder.registerHandlerPriceProductsChange(mReloadPriceProductHandler);
        mIManageHolder.registerHandlerBuyProductEvent(mManadgeBuyProductIListener);

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
//              mIManageHolder.buyProduct(ManageHolder.ManadgeProduct.test_success);
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
        mPriceHints_10.setText(mIManageHolder.getPriceProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_10));
        mPriceHints_20.setText(mIManageHolder.getPriceProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_20));
        mPriceHints_30.setText(mIManageHolder.getPriceProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_30));
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
    IListener<String> mManadgeBuyProductIListener = new IListener<String>() {
        @Override
        public void handle(@Nullable String hintsId) {

            int count = 0;
            if(hintsId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
            {
                count = 10;
            }
            else if(hintsId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_20))
            {
                count = 20;
            }
            else if(hintsId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_30))
            {
                count = 30;
            }
//            else if(hintsId.equals(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
//            {
//            }
            else
                return;
            setHintsCount(getHintsCount()+count);
        }
    };
}
