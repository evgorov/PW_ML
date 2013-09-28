package com.ltst.przwrd.crossword.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.ltst.przwrd.R;
import com.ltst.przwrd.sounds.SoundsWork;
import com.ltst.przwrd.tools.FlipAnimation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlipNumberAnimator implements Animation.AnimationListener
{
    private @Nonnull Context mContext;
    private @Nonnull ViewGroup mRootView;
    private @Nonnull ViewGroup mRootBackView;
    @Nullable View decThousandsMiddle;
    @Nullable View thousandsMiddle;
    @Nullable View hundredsMiddle;
    @Nullable View tensMiddle;
    @Nullable View lowerThanTenMiddle;

    @Nullable View decThousandsTop;
    @Nullable View thousandsTop;
    @Nullable View hundredsTop;
    @Nullable View tensTop;
    @Nullable View lowerThanTenTop;

    @Nullable View decThousandsBottom;
    @Nullable View thousandsBottom;
    @Nullable View hundredsBottom;
    @Nullable View tensBottom;
    @Nullable View lowerThanTenBottom;

    private @Nonnull FlipAnimation animDecThousands;
    private @Nonnull FlipAnimation animThousands;
    private @Nonnull FlipAnimation animHundreds;
    private @Nonnull FlipAnimation animTens;
    private @Nonnull FlipAnimation animLowerThanTen;

    int countDecThousands;
    int countThousands;
    int countHundreds;
    int countTens;
    int countLowerThanTen;

    int countEndsAnimation = 0;

    public FlipNumberAnimator(@Nonnull Context context, @Nonnull ViewGroup rootView)
    {
        mContext = context;
        mRootView = rootView;

        decThousandsMiddle = (View) mRootView.findViewById(R.id.middle_view);
        thousandsMiddle = (View) mRootView.findViewById(R.id.middle_view1);
        hundredsMiddle = (View) mRootView.findViewById(R.id.middle_view2);
        tensMiddle = (View) mRootView.findViewById(R.id.middle_view3);
        lowerThanTenMiddle = (View) mRootView.findViewById(R.id.middle_view4);

        decThousandsTop = (View) mRootView.findViewById(R.id.top_view);
        thousandsTop = (View) mRootView.findViewById(R.id.top_view1);
        hundredsTop = (View) mRootView.findViewById(R.id.top_view2);
        tensTop = (View) mRootView.findViewById(R.id.top_view3);
        lowerThanTenTop = (View) mRootView.findViewById(R.id.top_view4);

        decThousandsBottom = (View) mRootView.findViewById(R.id.bottom_view);
        thousandsBottom = (View) mRootView.findViewById(R.id.bottom_view1);
        hundredsBottom = (View) mRootView.findViewById(R.id.bottom_view2);
        tensBottom = (View) mRootView.findViewById(R.id.bottom_view3);
        lowerThanTenBottom = (View) mRootView.findViewById(R.id.bottom_view4);

        animDecThousands = new FlipAnimation(decThousandsTop, decThousandsBottom, decThousandsMiddle, 1);
        animThousands = new FlipAnimation(thousandsTop, thousandsBottom, thousandsMiddle, 2);
        animHundreds = new FlipAnimation(hundredsTop, hundredsBottom, hundredsMiddle, 3);
        animTens = new FlipAnimation(tensTop, tensBottom, tensMiddle, 4);
        animLowerThanTen = new FlipAnimation(lowerThanTenTop, lowerThanTenBottom, lowerThanTenMiddle, 5);

        countDecThousands = 1;
        countThousands = 1;
        countHundreds = 1;
        countTens = 1;
        countLowerThanTen = 1;

        animDecThousands.setAnimationListener(this);
        animThousands.setAnimationListener(this);
        animHundreds.setAnimationListener(this);
        animTens.setAnimationListener(this);
        animLowerThanTen.setAnimationListener(this);
    }

    public void startAnimation(int score)
    {
        if (score < 0)
            return;
        setScoreToView(score);
        flipCard();
    }

    private void flipCard()
    {
        animDecThousands.setCountIter(countDecThousands);
        countDecThousands = animDecThousands.getCountIter();
        decThousandsMiddle.startAnimation(animDecThousands);
        countDecThousands++;

        animThousands.setCountIter(countThousands);
        countThousands = animThousands.getCountIter();
        thousandsMiddle.startAnimation(animThousands);
        countThousands++;

        animHundreds.setCountIter(countHundreds);
        countHundreds = animHundreds.getCountIter();
        hundredsMiddle.startAnimation(animHundreds);
        countHundreds++;

        animTens.setCountIter(countTens);
        countTens = animTens.getCountIter();
        tensMiddle.startAnimation(animTens);
        countTens++;

        animLowerThanTen.setCountIter(countLowerThanTen);
        countLowerThanTen = animLowerThanTen.getCountIter();
        lowerThanTenMiddle.startAnimation(animLowerThanTen);
        countLowerThanTen++;
    }

    private void setScoreToView(int score)
    {
        final int decThousands = score / 10000;
        score -= decThousands * 10000;
        int thousands = score / 1000;
        score -= thousands * 1000;
        int hundreds = score / 100;
        score -= hundreds * 100;
        int tens = score / 10;
        score -= tens * 10;
        int lowerTen = score;

        rotateFlip(animDecThousands, 1, decThousands);
        rotateFlip(animThousands, 1, thousands);
        rotateFlip(animHundreds, 1, hundreds);
        rotateFlip(animTens, 1, tens);
        rotateFlip(animLowerThanTen, 1, lowerTen);
    }

    private void rotateFlip(Animation animation, int countTurn, int aim)
    {

        animation.setRepeatCount(countTurn * 10 + aim - 1);
    }

    @Override public void onAnimationStart(Animation animation)
    {
        SoundsWork.scoreAllCountSound(mContext);
    }

    @Override public void onAnimationEnd(Animation animation)
    {
        if (((FlipAnimation) animation).getId() == 1 || ((FlipAnimation) animation).getId() == 2 ||
                ((FlipAnimation) animation).getId() == 3 || ((FlipAnimation) animation).getId() == 4 ||
                ((FlipAnimation) animation).getId() == 5)
            countEndsAnimation++;
        if (countEndsAnimation == 5)
        {
            SoundsWork.releaseMPALL();
            countEndsAnimation = 0;
        }
    }

    @Override public void onAnimationRepeat(Animation animation)
    {
        if (((FlipAnimation) animation).getId() == 1)
        {
            animDecThousands.setCountIter(countDecThousands);
            countDecThousands = animDecThousands.getCountIter();
            countDecThousands++;
        }
        if (((FlipAnimation) animation).getId() == 2)
        {
            animThousands.setCountIter(countThousands);
            countThousands = animThousands.getCountIter();
            countThousands++;
        }
        if (((FlipAnimation) animation).getId() == 3)
        {
            animHundreds.setCountIter(countHundreds);
            countHundreds = animHundreds.getCountIter();
            countHundreds++;
        }
        if (((FlipAnimation) animation).getId() == 4)
        {
            animTens.setCountIter(countTens);
            countTens = animTens.getCountIter();
            countTens++;
        }
        if (((FlipAnimation) animation).getId() == 5)
        {
            animLowerThanTen.setCountIter(countLowerThanTen);
            countLowerThanTen = animLowerThanTen.getCountIter();
            countLowerThanTen++;
        }
    }
}
