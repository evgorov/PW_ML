package com.ltst.prizeword.crossword.view;

import android.view.View;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.HintsModel;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HintsManager implements View.OnClickListener
{
    private View mBuyHints_10;
    private View mBuyHints_20;
    private View mBuyHints_30;

    private HintsModel mHintsModel;
    private @Nullable IListenerInt mHintChangeListener;

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

    public void setHintChangeListener(@Nullable IListenerInt hintChangeListener)
    {
        mHintChangeListener = hintChangeListener;
    }

    @Override
    public void onClick(View v)
    {
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
        if (count != 0)
        {
            changeHintsCount(count);
        }
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
