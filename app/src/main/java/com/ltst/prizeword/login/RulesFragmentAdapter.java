package com.ltst.prizeword.login;

import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ltst.prizeword.R;


public class RulesFragmentAdapter extends FragmentPagerAdapter
{
    private int[] Images;

    private int mCount;

    public RulesFragmentAdapter(FragmentManager fm){
        super(fm);
        Images = new int[]{R.drawable.rules_page_12x, R.drawable.rules_page_22x, R.drawable.rules_page_32x, R.drawable.rules_page_42x, R.drawable.rules_page_52x, R.drawable.rules_page_62x, R.drawable.rules_page_72x, R.drawable.rules_page_82x, R.drawable.rules_page_92x, R.drawable.rules_page_102x, R.drawable.rules_page_112x, R.drawable.rules_page_122x};
        mCount = Images.length;

    }


    @Override public Fragment getItem(int position)
    {
        return new RulesSlideFragment(Images[position]);
    }

    @Override public int getCount()
    {
        return mCount;
    }
}
