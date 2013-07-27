package com.ltst.prizeword.login;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import static android.view.View.OnClickListener;

public class RulesFragment extends SherlockActivity implements OnClickListener
{
    private @Nonnull int[] mImages;
    private @Nonnull int[] mTexts;
    private @Nonnull ImageView mSimpleImage;
    private @Nonnull TextView mSimpleText;
    private @Nonnull Button mButton;
    private @Nonnull Animation mAnimationIn;
    private @Nonnull Animation mAnimationOut;

    public static @Nonnull android.content.Intent createIntent(@Nonnull Context context)
    {
        Intent intent = new Intent(context, RulesFragment.class);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        mImages = new int[]{R.drawable.rules_page_12x, R.drawable.rules_page_22x,
                R.drawable.rules_page_32x, R.drawable.rules_page_42x, R.drawable.rules_page_52x, R.drawable.rules_page_62x, R.drawable.rules_page_72x, R.drawable.rules_page_82x, R.drawable.rules_page_92x
                , R.drawable.rules_page_102x, R.drawable.rules_page_112x, R.drawable.rules_page_122x};
        mTexts = new  int[]{R.string.rules_text_1,R.string.rules_text_2,R.string.rules_text_3,R.string.rules_text_4,R.string.rules_text_5,R.string.rules_text_6,R.string.rules_text_7,R.string.rules_text_8,
                R.string.rules_text_9,R.string.rules_text_10,R.string.rules_text_11,R.string.rules_text_12,};

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_fragment_layout);

        mButton = (Button)findViewById(R.id.rules_close);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        LinearLayout indicator = (LinearLayout) findViewById(R.id.rules_indicator);

       LinearLayout.LayoutParams params;

        for (int i = 0; i < mImages.length; i++)
        {
            mSimpleImage = new ImageView(this);
            mSimpleImage.setImageResource(R.drawable.rules_pagecontrol_empty);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,8,0);
            mSimpleImage.setLayoutParams(params);
            mSimpleImage.setId(i);

            indicator.addView(mSimpleImage);
        }

        mSimpleImage = (ImageView) findViewById(0);
        mSimpleImage.setImageResource(R.drawable.rules_pagecontrol_full);

        mSimpleText = (TextView)findViewById(R.id.rules_text);
        mSimpleText.setText(mTexts[0]);
        mAnimationIn = AnimationUtils.loadAnimation(this,R.anim.rules_alpha_text_in);
        mAnimationOut = AnimationUtils.loadAnimation(this,R.anim.rules_alpha_text_out);


        List<View> pages = new ArrayList<View>();
        LayoutInflater inflater = LayoutInflater.from(this);
        View page;
        ImageView imageView;
        for (int i = 0; i < mImages.length; i++)
        {
            page = inflater.inflate(R.layout.rules_simple_image, null);
            imageView = (ImageView) page.findViewById(R.id.image_view);
            imageView.setImageResource(mImages[i]);
            pages.add(page);
        }

        RulesFragmentAdapter pagerAdapter = new RulesFragmentAdapter(pages);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override public void onPageScrolled(int i, float v, int i2)
            {

            }

            @Override public void onPageSelected(int position)
            {   mAnimationOut.reset();
                mSimpleText.clearAnimation();
                mSimpleText.startAnimation(mAnimationOut);

                for (int i = 0; i < mImages.length; i++)
                {
                    mSimpleImage = (ImageView) findViewById(i);
                    if (i == position)
                        mSimpleImage.setImageResource(R.drawable.rules_pagecontrol_full);
                    else
                        mSimpleImage.setImageResource(R.drawable.rules_pagecontrol_empty);

                }

                mSimpleText.setText(mTexts[position]);

                mAnimationIn.reset();
                mSimpleText.clearAnimation();
                mSimpleText.startAnimation(mAnimationIn);
            }

            @Override public void onPageScrollStateChanged(int i)
            {

            }
        });

        mButton.setOnClickListener(this);
    }

    @Override public void onClick(View v)
    {
        switch(v.getId()){
            case R.id.rules_close:
                onBackPressed();
            break;
        }
    }
}
