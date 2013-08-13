package com.ltst.prizeword.crossword.view;

import android.view.View;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.HintsModel;

import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;

public class HintsManager implements View.OnClickListener
{
    private View mBuyHints_10;
    private View mBuyHints_20;
    private View mBuyHints_30;

    private HintsModel mHintsModel;

    public HintsManager(@Nonnull IBcConnector bcConnector, @Nonnull String sessionKey, View parentView)
    {
        mHintsModel = new HintsModel(bcConnector, sessionKey);
        mBuyHints_10 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_10_btn);
        mBuyHints_20 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_20_btn);
        mBuyHints_30 = parentView.findViewById(R.id.crossword_fragment_current_rest_buy_30_btn);
        mBuyHints_10.setOnClickListener(this);
        mBuyHints_20.setOnClickListener(this);
        mBuyHints_30.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.crossword_fragment_current_rest_buy_10_btn:
                mHintsModel.changeHints(10, null);
                break;
            case R.id.crossword_fragment_current_rest_buy_20_btn:
                mHintsModel.changeHints(20, null);
                break;
            case R.id.crossword_fragment_current_rest_buy_30_btn:
                mHintsModel.changeHints(30, null);
                break;
        }
    }
}
