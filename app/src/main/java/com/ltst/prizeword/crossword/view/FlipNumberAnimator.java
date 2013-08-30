package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlipNumberAnimator
{
    private @Nonnull Context mContext;
    private @Nonnull ViewGroup mRootView;
    @Nullable TextView decThousandsTv;
    @Nullable TextView thousandsTv;
    @Nullable TextView hundredsTv;
    @Nullable TextView tensTv;
    @Nullable TextView lowerThanTenTv;

    public FlipNumberAnimator(@Nonnull Context context, @Nonnull ViewGroup rootView)
    {
        mContext = context;
        mRootView = rootView;
        if(rootView.getChildCount() != 5)
            return;
        decThousandsTv = (TextView) mRootView.getChildAt(0);
        thousandsTv = (TextView) mRootView.getChildAt(1);
        hundredsTv = (TextView) mRootView.getChildAt(2);
        tensTv = (TextView) mRootView.getChildAt(3);
        lowerThanTenTv = (TextView) mRootView.getChildAt(4);
    }

    public void startAnimation(int score)
    {
        if(score < 0)
            return;
        setScoreToView(score);
    }

    private void setScoreToView(int score)
    {
        assert  decThousandsTv != null &&
                thousandsTv != null &&
                hundredsTv != null &&
                tensTv != null &&
                lowerThanTenTv != null;

        final int decThousands = score/10000;
        score -= decThousands * 10000;
        int thousands = score/1000;
        score -= thousands * 1000;
        int hundreds = score/100;
        score -= hundreds * 100;
        int tens = score/10;
        score -= tens * 10;
        int lowerTen = score;

        decThousandsTv.setText(String.valueOf(decThousands));
        thousandsTv.setText(String.valueOf(thousands));
        hundredsTv.setText(String.valueOf(hundreds));
        tensTv.setText(String.valueOf(tens));
        lowerThanTenTv.setText(String.valueOf(lowerTen));
    }

}
