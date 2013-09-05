package com.ltst.prizeword.tools;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout.LayoutParams;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 02.09.13.
 */
public class AnimationTools {

    public static void expand(final @Nonnull View viewContainer, final @Nonnull View viewContained) {
        viewContainer.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//        final int targtetHeight = v.getMeasuredHeight();
        final int targtetHeight = viewContained.getHeight();

        viewContainer.getLayoutParams().height = 0;
        viewContainer.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                viewContainer.getLayoutParams().height = interpolatedTime == 1
                        ? LayoutParams.WRAP_CONTENT
                        : (int)(targtetHeight * interpolatedTime);
                viewContainer.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targtetHeight / viewContainer.getContext().getResources().getDisplayMetrics().density));
        viewContainer.startAnimation(a);
    }

    public static void collapse(final @Nonnull View viewContainer, final @Nonnull View viewContained) {

        final int initialHeight = viewContainer.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    viewContainer.setVisibility(View.GONE);
                }else{
                    viewContainer.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    viewContainer.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / viewContainer.getContext().getResources().getDisplayMetrics().density));
        viewContainer.startAnimation(a);
    }

}
