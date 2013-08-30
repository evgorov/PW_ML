package com.ltst.prizeword.crossword.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.HintsModel;
import com.ltst.prizeword.manadges.IManadges;
import com.ltst.prizeword.manadges.ManadgeHolder;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HintsManager implements View.OnClickListener
{

    private @Nonnull View mBuyHints_10;
    private @Nonnull View mBuyHints_20;
    private @Nonnull View mBuyHints_30;
    private @Nonnull TextView mPriceHints_10;
    private @Nonnull TextView mPriceHints_20;
    private @Nonnull TextView mPriceHints_30;

    private HintsModel mHintsModel;
    private @Nullable IListenerInt mHintChangeListener;
    private @Nonnull IManadges mIManadges;
    private @Nonnull Context mContext;
    private @Nonnull Activity mActivity;

    public HintsManager(@Nonnull Context context, @Nonnull IManadges iManadges, @Nonnull IBcConnector bcConnector, @Nonnull String sessionKey, View parentView)
    {
        mContext = context;
        mActivity = (Activity) context;

        mIManadges = iManadges;
        mHintsModel = new HintsModel(bcConnector, sessionKey);

        mPriceHints_10 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_10_price);
        mPriceHints_20 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_20_price);
        mPriceHints_30 = (TextView) parentView.findViewById(R.id.crossword_fragment_current_rest_buy_30_price);

        mPriceHints_10.setText(mIManadges.getPriceProduct(ManadgeHolder.ManadgeProduct.hints10));
        mPriceHints_20.setText(mIManadges.getPriceProduct(ManadgeHolder.ManadgeProduct.hints20));
        mPriceHints_30.setText(mIManadges.getPriceProduct(ManadgeHolder.ManadgeProduct.hints30));

        mBuyHints_10 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_10_btn);
        mBuyHints_20 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_20_btn);
        mBuyHints_30 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_30_btn);

        mBuyHints_10.setOnClickListener(this);
        mBuyHints_20.setOnClickListener(this);
        mBuyHints_30.setOnClickListener(this);

    }

    public void setHintChangeListener(@Nullable IListenerInt hintChangeListener)
    {
        mHintChangeListener = hintChangeListener;
    }

    @Override
    public void onClick(View v)
    {
        // Покупка;
        mIManadges.buyProduct(ManadgeHolder.ManadgeProduct.test_success);

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

}
