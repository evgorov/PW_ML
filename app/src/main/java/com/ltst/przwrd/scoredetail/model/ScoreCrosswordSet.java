package com.ltst.przwrd.scoredetail.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltst.przwrd.R;
import com.ltst.przwrd.crossword.model.PuzzleSetModel;
import com.ltst.przwrd.crossword.view.BadgeAdapter;
import com.ltst.przwrd.crossword.view.BadgeGridView;
import com.ltst.przwrd.tools.CustomProgressBar;
import com.ltst.przwrd.crossword.view.CrosswordPanelData;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScoreCrosswordSet
{
    private @Nonnull Context mContext;
    private @Nonnull LayoutInflater mInflater;

    private @Nonnull CrosswordSetType mCrosswordSetType;

    private @Nonnull View mRootView;
    private @Nonnull LinearLayout pTitleImage;
    private @Nonnull TextView pTitleText;

    private @Nonnull LinearLayout pCurrentCrosswordContaiter;
    private @Nonnull TextView pRatioText;
    private @Nonnull TextView pProgressText;
    private @Nonnull
    CustomProgressBar pProgress;
    private @Nonnull TextView pScoreText;

    private @Nonnull BadgeGridView pBadgeContainer;

    private @Nullable String mSetServerId = null;

    private @Nonnull LinearLayout mPanelItem;
    private @Nonnull LinearLayout mSplitter;

    public ScoreCrosswordSet(@Nonnull Context context)
    {

        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mRootView = mInflater.inflate(R.layout.score_crossword_panel, null, false);
        pTitleImage = (LinearLayout) mRootView.findViewById(R.id.score_crossword_panel_logo_image);
        pTitleText = (TextView) mRootView.findViewById(R.id.score_crossword_panel_title_text);

        pCurrentCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.score_crossword_fragment_current_container);
        pRatioText = (TextView) mRootView.findViewById(R.id.score_crossword_panel_ratio);
        pProgressText = (TextView) mRootView.findViewById(R.id.score_crossword_panel_percent);
        pProgress = new CustomProgressBar(context, mRootView, R.id.score_crossword_panel_progress_bg, R.id.score_crossword_panel_progress_fg);
        pScoreText = (TextView) mRootView.findViewById(R.id.score_crossword_panel);

        pBadgeContainer = (BadgeGridView) mRootView.findViewById(R.id.score_crossword_panel_badges_container);

        mPanelItem = (LinearLayout) mRootView.findViewById(R.id.score_crossword_panel_item);
        mSplitter = (LinearLayout) mRootView.findViewById(R.id.score_crossword_panel_splitter);
    }

    public @Nonnull View getView()
    {
        return mRootView;
    }

    public @Nonnull BadgeAdapter getAdapter()
    {
        return (BadgeAdapter) pBadgeContainer.getAdapter();
    }

    public void fillPanel(@Nonnull CrosswordPanelData data)
    {
//        mRootView.setVisibility(View.VISIBLE);
        if(!data.mBought)
        {
            mRootView.setVisibility(View.GONE);
        }

        if (data.mFirst)
            mPanelItem.setBackgroundResource(R.drawable.score_item_scan_bg);
        else
            mPanelItem.setBackgroundResource(R.drawable.score_item_bg);
        if (data.mLAst)
            mSplitter.setVisibility(View.GONE);
        else
            mSplitter.setVisibility(View.VISIBLE);

        if (data.mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_br));
            pTitleText.setText(R.string.puzzless_hint_brilliant_crossword);
        } else if (data.mType == PuzzleSetModel.PuzzleSetType.GOLD)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_au));
            pTitleText.setText(R.string.puzzless_hint_gold_crossword);
        } else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag));
            pTitleText.setText(R.string.puzzless_hint_silver_crossword);
        } else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER2)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag2));
            pTitleText.setText(R.string.puzzless_hint_silver2_crossword);
        } else if (data.mType == PuzzleSetModel.PuzzleSetType.FREE)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_fr));
            pTitleText.setText(R.string.puzzless_hint_free_crossword);
        }

        StringBuilder sbRatio = new StringBuilder();
        sbRatio.append(data.mResolveCount);
        sbRatio.append("/");
        sbRatio.append(data.mTotalCount);
        pRatioText.setText(sbRatio.toString());

        StringBuilder sbProgress = new StringBuilder();
        sbProgress.append(data.mProgress);
        sbProgress.append("%");
        pProgressText.setText(sbProgress.toString());
        pProgress.setProgress(data.mProgress);

        pScoreText.setText(String.valueOf(data.mScore));

        if (pBadgeContainer.getAdapter() == null)
            pBadgeContainer.setAdapter(new BadgeAdapter(mContext, data.mType));

        if (data.mMonth == Calendar.getInstance().get(Calendar.MONTH) + 1)
        {
            // Текущие наборы сетов сканвордов;
            mCrosswordSetType = CrosswordSetType.CURRENT;
            pTitleImage.setVisibility(View.VISIBLE);

            if (mSetServerId == null)
            {
                pBadgeContainer.setVisibility(View.VISIBLE);
            }

            if (data.mBought)
            {
                // Куплены;
            } else
            {
                // Некуплены;
            }
        } else
        {
            // Архивные наборы сетов сканвордов;
            mCrosswordSetType = CrosswordSetType.ARCHIVE;
            pTitleImage.setVisibility(View.VISIBLE);

            if (mSetServerId == null)
            {
                pBadgeContainer.setVisibility(View.VISIBLE);
            }
            if (data.mMonth == 0)
            {
            } else
            {
                DateFormatSymbols symbols = new DateFormatSymbols();
            }
        }
        mSetServerId = data.mServerId;
    }

    public void setVisibleMonth(boolean visible)
    {
    }

    public CrosswordSetType getCrosswordSetType()
    {
        return mCrosswordSetType;
    }

    public enum CrosswordSetType
    {
        CURRENT,
        ARCHIVE
    }
}
