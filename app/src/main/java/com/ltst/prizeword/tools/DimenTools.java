package com.ltst.prizeword.tools;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by cosic on 27.08.13.
 */
public class DimenTools {

    static public float dpFromPx(Context context, float px)
    {
        return px / context.getResources().getDisplayMetrics().density;
    }


    static public float pxFromDp(Context context, float dp)
    {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    static public float pxByDensity(Context context, int px)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }
}
