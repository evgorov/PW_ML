package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.ltst.prizeword.crossword.engine.PuzzleResources;
import com.ltst.prizeword.crossword.engine.PuzzleResourcesAdapter;
import com.ltst.prizeword.tools.FixedInputConnection;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerBoolean;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleView extends View
{
    public static final @Nonnull String BF_SCALED = "PuzzleView.scaled";
    public static final @Nonnull String BF_KEYBOARD_STATUS = "PuzzleView.keyboardStatus";

    private @Nonnull Context mContext;
    private @Nullable Rect mViewScreenRect;
    private @Nullable PuzzleManager mPuzzleManager;

    private @Nonnull GestureDetector mGestureDetector;
    private @Nonnull ScaleGestureDetector mScaleGestureDetector;
    private @Nonnull KeyboardListener mKeyboardListener;
    private static final float MIN_SCALE_FACTOR_DETECTABLE = 0.2f;
    private boolean mScaled = true;

    private @Nonnull PuzzleResourcesAdapter mAdapter;
    private boolean mKeyboardOpened;

    public PuzzleView(Context context)
    {
        this(context, null);
    }

    public PuzzleView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PuzzleView(@Nonnull Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        mGestureDetector = new GestureDetector(context, new GestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        mKeyboardListener = new KeyboardListener();
        setFocusable(true);
        setFocusableInTouchMode(true);
        mKeyboardOpened = false;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        editorInfo.actionLabel = null;
        editorInfo.inputType   = InputType.TYPE_NULL;
        editorInfo.imeOptions  = EditorInfo.IME_ACTION_NONE;

        return new FixedInputConnection(this, false);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }


    public void setAdapter(@Nonnull PuzzleResourcesAdapter adapter)
    {
        recycle();
        mAdapter = adapter;
        mPuzzleManager = new PuzzleManager(mContext, mAdapter, new IListener<Rect>()
        {
            @Override
            public void handle(@Nullable Rect rect)
            {
                if (rect != null)
                {
                    invalidate(rect);
                }
            }
        });
        mAdapter.addResourcesUpdater(new IListener<PuzzleResources>()
        {
            @Override
            public void handle(@Nullable PuzzleResources puzzleResources)
            {
                if (mViewScreenRect != null && mPuzzleManager != null)
                {
                    mPuzzleManager.setPuzzleViewRect(mViewScreenRect);
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mViewScreenRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
        if (mPuzzleManager != null)
        {
            mPuzzleManager.setPuzzleViewRect(mViewScreenRect);
        }
        invalidate(mViewScreenRect);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void saveState(@Nonnull Bundle bundle)
    {
        bundle.putBoolean(BF_SCALED, mScaled);
        bundle.putBoolean(BF_KEYBOARD_STATUS, mKeyboardOpened);
        if (mPuzzleManager != null)
        {
            mPuzzleManager.saveState(bundle);
        }
    }

    public void  restoreState(@Nonnull Bundle bundle)
    {
        mScaled = bundle.getBoolean(BF_SCALED);
        mKeyboardOpened = bundle.getBoolean(BF_KEYBOARD_STATUS);
        if(mKeyboardOpened)
            hideKeyboard();
        if (mPuzzleManager != null)
        {
            mPuzzleManager.restoreState(bundle);
        }

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (canvas == null)
            return;
        if (mPuzzleManager != null)
        {
            mPuzzleManager.drawPuzzle(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(@Nonnull MotionEvent event)
    {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);

        return true;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event)
    {
        return mKeyboardListener.onKey(this, event.getKeyCode(), event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        return mKeyboardListener.onKey(this, event.getKeyCode(), event);
    }

    public void recycle()
    {
        if (mPuzzleManager != null)
        {
            mPuzzleManager.recycle();
            mPuzzleManager = null;
        }
        hideKeyboard();
    }

    public void cancelInput()
    {
        if (mPuzzleManager == null)
        {
            return;
        }
        if(mAdapter.isInputMode() && mKeyboardOpened)
        {
            mPuzzleManager.cancelLastQuestion();
            hideKeyboard();
        }
    }

    public void openKeyboard()
    {
        if(mKeyboardOpened)
            return;
        if (this.requestFocus())
        {
            InputMethodManager imm = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
            mKeyboardOpened = true;
        }
    }

    public void hideKeyboard()
    {
        if(mKeyboardOpened)
        {
            this.clearFocus();
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
            mKeyboardOpened = false;
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            if (mPuzzleManager != null && mViewScreenRect != null)
            {
                mPuzzleManager.onScrollEvent(distanceX, distanceY);
                invalidate(mViewScreenRect);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            PointF p = new PointF(e.getX(), e.getY());
            if (mPuzzleManager != null && mViewScreenRect != null)
            {
                boolean handled = mPuzzleManager.onTapEvent(PuzzleView.this, p, new IListenerVoid(){
                    @Override
                    public void handle()
                    {
                        openKeyboard();
                    }
                });

                if(!handled && !mScaled)
                {
                    mPuzzleManager.onScaleEvent(PuzzleView.this, p);
                    mScaled = true;
                }
            }
            return true;
        }
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            if(mKeyboardOpened)
                return false;
            float scaleFactor = detector.getScaleFactor();
            if(scaleFactor >= 1 + MIN_SCALE_FACTOR_DETECTABLE && !mScaled)
            {
                if (mPuzzleManager != null && mViewScreenRect != null)
                {
                    mPuzzleManager.onScaleEvent(PuzzleView.this, null);
                    mScaled = true;
                }
                return true;
            }
            if(scaleFactor <= 1 - MIN_SCALE_FACTOR_DETECTABLE && mScaled)
            {
                if (mPuzzleManager != null && mViewScreenRect != null)
                {
                    mPuzzleManager.onScaleEvent(PuzzleView.this, null);
                    mScaled = false;
                }
                return true;
            }
            return false;
        }
    }

    private class KeyboardListener implements OnKeyListener
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (mPuzzleManager == null)
            {
                return false;
            }
            if (event.getAction() == KeyEvent.ACTION_UP
                    || event.getAction() == KeyEvent.ACTION_MULTIPLE)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_BACK:
                        if(!mKeyboardOpened)
                            return false;
                    case KeyEvent.KEYCODE_ENTER:
                        mPuzzleManager.onKeyEvent(PuzzleView.this, event, null);
                        hideKeyboard();
                        return true;
                    default:
                        mPuzzleManager.onKeyEvent(PuzzleView.this, event, new IListenerBoolean(){
                            @Override
                            public void handle(boolean b)
                            {
                                if(b)
                                    openKeyboard();
                                else
                                    hideKeyboard();
                            }
                        });
                        return true;
                }
            }
            else return false;
        }
    }

}
