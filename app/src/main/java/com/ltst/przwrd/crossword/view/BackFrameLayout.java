package com.ltst.przwrd.crossword.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 05.09.13.
 */
public class BackFrameLayout extends FrameLayout{

    private @Nonnull IListenerVoid mSizeChangeListener;

    public BackFrameLayout(Context context) {
        super(context);
    }

    public BackFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnResizeListener(@Nonnull IListenerVoid handler)
    {
        mSizeChangeListener = handler;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(mSizeChangeListener!=null)
        {
            mSizeChangeListener.handle();
        }
    }
}
