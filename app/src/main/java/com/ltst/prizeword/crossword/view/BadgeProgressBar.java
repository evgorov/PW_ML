package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.ltst.prizeword.tools.DimenTools;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 22.08.13.
 */
public class BadgeProgressBar {

    @Nonnull Context mContext;
    @Nonnull LinearLayout mBackground;
    @Nonnull LinearLayout mForeground;
    int mWidth = 0;
    int mProgress = 0;

    public BadgeProgressBar(@Nonnull Context context, @Nonnull View view, int rBg, int rFg) {

        mContext = context;
        mBackground = (LinearLayout) view.findViewById(rBg);
        mForeground = (LinearLayout) view.findViewById(rFg);

        mBackground.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        int minimum = (int) DimenTools.pxByDensity(mContext,8);
                        int margin = (int) DimenTools.pxByDensity(mContext,1);
                        mWidth = mBackground.getWidth();

                        int width = (mWidth == 0) ? 0 : mProgress * (mWidth - 2 * margin) / 100;
                        width = (width > 0 && width < minimum) ? minimum : width;

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
                        lp.setMargins(margin,0,margin,2);
                        mForeground.setLayoutParams(lp);
                        mBackground.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
    }

    public void setProgress(int progress)
    {
        mProgress = progress;
    }


}
