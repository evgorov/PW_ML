package com.ltst.prizeword.login;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ltst.prizeword.R;

import java.util.List;


public class RulesFragmentAdapter extends PagerAdapter
{
    private int[] Images;
    private int mCount;
    List<View> pages = null;

    public RulesFragmentAdapter(List<View> pages){
        /*Images = new int[]{R.drawable.rules_page_12x, R.drawable.rules_page_22x, R.drawable.rules_page_32x, R.drawable.rules_page_42x, R.drawable.rules_page_52x, R.drawable.rules_page_62x, R.drawable.rules_page_72x, R.drawable.rules_page_82x, R.drawable.rules_page_92x, R.drawable.rules_page_102x, R.drawable.rules_page_112x, R.drawable.rules_page_122x};
        mCount = Images.length;*/
        this.pages= pages;

    }
    @Override
    public Object instantiateItem(ViewGroup collection, int position){
        /*ImageView im = new ImageView(context);
        im.setImageResource(position);
        ((ViewPager)collection).addView(im,0);
       return im;*/
        View v = pages.get(position);
        collection.addView(v,0);
        return v;
    }
    @Override
    public void destroyItem(ViewGroup collection, int position,Object view){
        collection.removeView((View)view);
    }

    @Override public int getCount()
    {
        return mCount;
    }

    @Override public boolean isViewFromObject(View view, Object o)
    {
        return view.equals(o);
    }
}
