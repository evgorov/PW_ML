package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.news.News;
import com.ltst.prizeword.swipe.ITouchInterface;
import com.ltst.prizeword.swipe.TouchDetector;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 20.09.13.
 */
public class NewsHolder {

    private @Nonnull Context mContext;
    private @Nonnull View mViewCrossword;
    private @Nonnull LayoutInflater mInflater;

    private @Nonnull List<String> mNewsList;
    private @Nonnull RelativeLayout mNewsLayout;
    private @Nonnull GestureDetector mGestureDetector;
    private @Nonnull LinearLayout mNewsIndicatorLayout;
    private @Nonnull ImageView mSimpleImage;
    private @Nonnull TextView mNewsSimpleText;
    private @Nonnull ImageView mNewsCloseBtn;
    private @Nonnull ITouchInterface mITouchInterface;
    private @Nonnull View mRoot;

    private int mIndicatorPosition;

    public NewsHolder(@Nonnull Context context, @Nonnull SherlockFragment fragment,
                                   @Nonnull LayoutInflater inflater, @Nonnull View view)
    {
        this.mInflater = inflater;
        this.mViewCrossword = view;
        this.mContext = context;
        this.mITouchInterface = (ITouchInterface) fragment;
        this.mRoot = view;

        mNewsList = new ArrayList<String>();
        mNewsLayout = (RelativeLayout) view.findViewById(R.id.news_layout);
        mGestureDetector = new GestureDetector(mContext, new TouchDetector(mITouchInterface));
        mNewsIndicatorLayout = (LinearLayout) view.findViewById(R.id.news_indicator_layout);
        mNewsSimpleText = (TextView) view.findViewById(R.id.news_simple_text);
        mNewsCloseBtn = (ImageView) view.findViewById(R.id.news_close_btn);

        mNewsLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override public boolean onTouch(View view, MotionEvent motionEvent)
            {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mNewsCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewsLayout.setVisibility(View.GONE);
            }
        });

        mNewsLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (view.getId()) {
                    case R.id.news_layout:
                        mSimpleImage = (ImageView) view.findViewById(mIndicatorPosition);
                        if(mSimpleImage != null)
                        {
                            mSimpleImage.setImageResource(R.drawable.puzzles_news_page);
                            if (mIndicatorPosition < mNewsList.size() - 1)
                                mIndicatorPosition++;
                            else if (mIndicatorPosition == mNewsList.size() - 1)
                                mIndicatorPosition = 0;
                            mSimpleImage = (ImageView) view.findViewById(mIndicatorPosition);
                            mSimpleImage.setImageResource(R.drawable.puzzles_news_page_current);
                            mNewsSimpleText.setText(mNewsList.get(mIndicatorPosition));
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void notifySwipe(ITouchInterface.SwipeMethod swipe)
    {
        mSimpleImage = (ImageView) mRoot.findViewById(mIndicatorPosition);
        mSimpleImage.setImageResource(R.drawable.puzzles_news_page);
        if (swipe.equals(ITouchInterface.SwipeMethod.SWIPE_RIGHT))
        {
            if (mIndicatorPosition > 0)
                mIndicatorPosition--;
        } else if (swipe.equals(ITouchInterface.SwipeMethod.SWIPE_LEFT))
        {
            if (mIndicatorPosition < mNewsList.size() - 1)
                mIndicatorPosition++;
            else if (mIndicatorPosition == mNewsList.size() - 1)
                mIndicatorPosition = 0;
        }
        mSimpleImage = (ImageView) mRoot.findViewById(mIndicatorPosition);
        mSimpleImage.setImageResource(R.drawable.puzzles_news_page_current);
        mNewsSimpleText.setText(mNewsList.get(mIndicatorPosition));
    }

    public void fillNews(News news)
    {
        if (news != null)
        {
            if (news.message1 == null && news.message2 == null && news.message3 == null)
            {
                mNewsLayout.setVisibility(View.GONE);
            } else
            {
                mNewsList.clear();
                mNewsList.add(news.message1);
                mNewsList.add(news.message2);
                mNewsList.add(news.message3);

                mNewsIndicatorLayout.removeAllViewsInLayout();
                LinearLayout.LayoutParams params;
                for (int i = 0; i < mNewsList.size(); i++)
                {
                    mSimpleImage = new ImageView(mContext);
                    mSimpleImage.setImageResource(R.drawable.puzzles_news_page);
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (i != 0)
                        params.setMargins(4, 0, 0, 0);
                    mSimpleImage.setLayoutParams(params);
                    mSimpleImage.setId(i);

                    mNewsIndicatorLayout.addView(mSimpleImage);
                }
                mIndicatorPosition = 0;
                mSimpleImage = (ImageView) mRoot.findViewById(mIndicatorPosition);
                mSimpleImage.setImageResource(R.drawable.puzzles_news_page_current);
                mNewsSimpleText.setText(mNewsList.get(mIndicatorPosition));
            }
        }
    }
}
