package com.ltst.przwrd.swipe;

public interface ITouchInterface {

	public enum SwipeMethod{
		SWIPE_RIGHT,
		SWIPE_LEFT,
		SWIPE_DOWN,
		SWIPE_UP,
	}
	public void notifySwipe(SwipeMethod swipe);
	
}
