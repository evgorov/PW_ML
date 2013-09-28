package com.ltst.przwrd.manadges;

import android.content.Intent;

/**
 * Created by cosic on 10.09.13.
 */
public interface IIabHelper {
    void instance();
    void dispose();
    boolean onActivityResult(int requestCode, int resultCode, Intent data);
}
