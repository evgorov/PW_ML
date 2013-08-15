package com.ltst.prizeword.InviteFiends.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

public class CustomListView extends ListView
{

    public CustomListView(Context context)
    {
        super(context);
    }


    public CustomListView(Context context, AttributeSet attrs)
    {

        super(context, attrs);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // HACK! TAKE THAT ANDROID!
        if (true)
        {
            // Calculate entire height by providing a very large height hint.
            // But do not use the highest 2 bits of this integer; those are
            // reserved for the MeasureSpec mode.
            int expandSpec = MeasureSpec.makeMeasureSpec(
                    Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        } else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
