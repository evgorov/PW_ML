package com.ltst.prizeword.swipe;

import android.view.GestureDetector;
import android.view.MotionEvent;

import javax.annotation.Nonnull;

public class TouchDetector extends GestureDetector.SimpleOnGestureListener{

	private @Nonnull ITouchInterface mTouchInterface;
	private @Nonnull SwipeDetector mSwipeDetector;

	public TouchDetector(@Nonnull ITouchInterface tInterface) {
		this.mTouchInterface = tInterface;
		this.mSwipeDetector = new SwipeDetector();
	}

	public boolean onDown(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			if (mSwipeDetector.isSwipeDown(e1, e2, velocityY)) {
				// SWIPE DOWN;
				mTouchInterface.notifySwipe(ITouchInterface.SwipeMethod.SWIPE_DOWN);
			} else if (mSwipeDetector.isSwipeUp(e1, e2, velocityY)) {
				// SWIPE UP;
				mTouchInterface.notifySwipe(ITouchInterface.SwipeMethod.SWIPE_UP);
			}else if (mSwipeDetector.isSwipeLeft(e1, e2, velocityX)) {
				// SWIPE LEFT;
				mTouchInterface.notifySwipe(ITouchInterface.SwipeMethod.SWIPE_LEFT);
			} else if (mSwipeDetector.isSwipeRight(e1, e2, velocityX)) {
				// SWIPE RIGHT;
				mTouchInterface.notifySwipe(ITouchInterface.SwipeMethod.SWIPE_RIGHT);
			} else {
				return false;
			}
		} catch (Exception e) {

		} //for now, ignore
		return true;
	}

}
