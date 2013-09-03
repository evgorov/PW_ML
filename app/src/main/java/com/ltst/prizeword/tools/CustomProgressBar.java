package com.ltst.prizeword.tools;

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
public class CustomProgressBar {

    private @Nonnull Context mContext;
    private @Nonnull LinearLayout mBackground;
    private @Nonnull LinearLayout mForeground;
    private int mWidth = 0;
    private int mProgress = 0;
    private int mMinimumWidth = 8;
    private int mMardginLeft = 1;
    private int mMardginTop = 0;
    private int mMardginRight = 1;
    private int mMardginBottom = 2;

    public CustomProgressBar(@Nonnull Context context, @Nonnull View view, int rBg, int rFg) {

        mContext = context;
        mBackground = (LinearLayout) view.findViewById(rBg);
        mForeground = (LinearLayout) view.findViewById(rFg);

        mBackground.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    public void setProgress(int progress)
    {
        mProgress = progress;
    }

    public void setMinimumWidth(int mMinimumWidth) {
        this.mMinimumWidth = mMinimumWidth;
    }

    public void repaint()
    {
        mBackground.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        mBackground.requestLayout();
    }

    public void setMardginLeft(int mMardginLeft) {
        this.mMardginLeft = mMardginLeft;
    }

    public void setMardginTop(int mMardginTop) {
        this.mMardginTop = mMardginTop;
    }

    public void setMardginRight(int mMardginRight) {
        this.mMardginRight = mMardginRight;
    }

    public void setMardginBottom(int mMardginBottom) {
        this.mMardginBottom = mMardginBottom;
    }

    private @Nonnull ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener =
            new ViewTreeObserver.OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {

            int minimum = (int) DimenTools.pxByDensity(mContext,mMinimumWidth);
            int margin = (int) DimenTools.pxByDensity(mContext,mMardginLeft);
            mWidth = mBackground.getWidth();

            int width = (mWidth == 0) ? 0 : mProgress * (mWidth - 2 * margin) / 100;
            width = (width > 0 && width < minimum) ? minimum : width;

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(mMardginLeft,mMardginTop,mMardginRight,mMardginBottom);
            mForeground.setLayoutParams(lp);
            mBackground.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    };


}
