package com.ltst.prizeword.navigation;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by cosic on 07.08.13.
 */
public class NavigationDrawerLayout extends DrawerLayout {

    public NavigationDrawerLayout(Context context) {
        super(context);
    }

    public NavigationDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NavigationDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try
        {
            return super.onTouchEvent(event);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void openDrawer(View drawerView) {
        super.openDrawer(drawerView);
    }

    @Override
    public void closeDrawer(View drawerView) {
        super.closeDrawer(drawerView);
    }
}
