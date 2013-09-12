package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.ltst.prizeword.R;
import com.ltst.prizeword.manadges.IManageHolder;
import com.ltst.prizeword.manadges.ManageHolder;
import com.ltst.prizeword.sounds.SoundsWork;
import com.ltst.prizeword.manadges.IManadges;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HintsManager implements View.OnClickListener
{
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
        mIManageHolder.registerHandlerBuyProductEvent(mBuyProductEventHandler);

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
              mIManageHolder.buyProduct(ManageHolder.ManadgeProduct.hints10);
                break;
            case R.id.crossword_fragment_current_rest_buy_20_btn:
                mIManageHolder.buyProduct(ManageHolder.ManadgeProduct.hints20);
                break;
            case R.id.crossword_fragment_current_rest_buy_30_btn:
                mIManageHolder.buyProduct(ManageHolder.ManadgeProduct.hints30);
                break;
        }
    }

    public void close()
    {
    }

    private void setPrice()
    {
        mPriceHints_10.setText(mIManageHolder.getPriceProduct(ManageHolder.ManadgeProduct.hints10));
        mPriceHints_20.setText(mIManageHolder.getPriceProduct(ManageHolder.ManadgeProduct.hints20));
        mPriceHints_30.setText(mIManageHolder.getPriceProduct(ManageHolder.ManadgeProduct.hints30));
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
    IListener<ManageHolder.ManadgeProduct> mBuyProductEventHandler = new IListener<ManageHolder.ManadgeProduct>() {
        @Override
        public void handle(@Nullable ManageHolder.ManadgeProduct manadgeProduct) {

            int count = 0;
            switch (manadgeProduct)
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

            setHintsCount(getHintsCount()+count);
        }
    };
}
