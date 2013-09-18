package com.ltst.prizeword.navigation;

import android.content.Intent;

/**
 * Created by cosic on 18.09.13.
 */
public interface INavigationFragmentListener {
    void onNavigationActivityResult(int requestCode, int resultCode, Intent data);
}
