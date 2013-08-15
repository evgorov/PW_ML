package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.omich.velo.constants.Strings;

import java.text.DateFormatSymbols;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public class CrosswordFragmentHolder {

    private static @Nonnull LayoutInflater mInflater;
    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull View mViewCrossword;
    private static @Nonnull Context mContext;
    public @Nonnull CrosswordPanelCurrentHolder mCrosswordPanelCurrent;
    public @Nonnull CrosswordPanelArchiveHolder mCrosswordPanelArchive;
    private @Nonnull HashMap<String, CrosswordSet> mListCrosswordSet;

    public CrosswordFragmentHolder(@Nonnull Context context, @Nonnull SherlockFragment fragment,
                                   @Nonnull LayoutInflater inflater, @Nonnull View view)
    {
        this.mInflater = inflater;
        this.mICrosswordFragment = (ICrosswordFragment) fragment;
        this.mViewCrossword = view;
        this.mContext = context;

        mListCrosswordSet = new HashMap<String, CrosswordSet>();
        mCrosswordPanelCurrent = new CrosswordPanelCurrentHolder(view);
        mCrosswordPanelArchive = new CrosswordPanelArchiveHolder(view);
    }

    // ================== CROSSWORD PANELS ======================

    static private class CrosswordPanelCurrentHolder {

        @Nonnull public TextView mMonthTV;
        @Nonnull public TextView mRestDaysTV;
        @Nonnull public LinearLayout mRestPanelLL;
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordPanelCurrentHolder(@Nonnull View view){

            mMonthTV = (TextView) view.findViewById(R.id.crossword_fragment_current_month);
            mRestDaysTV = (TextView) view.findViewById(R.id.crossword_fragment_current_remain_count_days);
            mRestPanelLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_remain_panel);
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_container);
        }
    }

    static private class CrosswordPanelArchiveHolder {
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordPanelArchiveHolder(@Nonnull View view){
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_archive_container);
        }
    }

    private CrosswordSet getCrosswordSet(@Nonnull String serverId)
    {
        return (serverId != Strings.EMPTY && mListCrosswordSet.containsKey(serverId)) ? mListCrosswordSet.get(serverId) : new CrosswordSet();
    }

    // ================== CROSSWORD PANELS ITEM ======================

    private static class CrosswordSet
    {
        @Nonnull View mRootView;
        @Nonnull LinearLayout pMonthBackground;
        @Nonnull TextView pMonthText;
        @Nonnull ImageView pTitleImage;
        @Nonnull TextView pTitleText;
        @Nonnull ToggleButton pSwitcher;

        @Nonnull LinearLayout pCurrentCrosswordContaiter;
        @Nonnull TextView pRatioText;
        @Nonnull TextView pProgressText;
        @Nonnull LinearLayout pProgressBackground;
        @Nonnull LinearLayout pProgressForeround;
        @Nonnull TextView pScoreText;

        @Nonnull LinearLayout pBuyCrosswordContaiter;
        @Nonnull TextView pBuyCountText;
        @Nonnull TextView pBuyScore;
        @Nonnull LinearLayout pBuyButton;
        @Nonnull TextView pBuyPrice;

        @Nonnull BadgeGridView pBadgeContainer;
        
        
        private CrosswordSet() {
            mRootView =  mInflater.inflate(R.layout.crossword_panel, null, false);
            pMonthBackground = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_splitter_month_bg);
            pMonthText = (TextView) mRootView.findViewById(R.id.crossword_panel_splitter_month_text);
            pTitleImage = (ImageView) mRootView.findViewById(R.id.crossword_panel_logo_image);
            pTitleText = (TextView) mRootView.findViewById(R.id.crossword_panel_title_text);
            pSwitcher = (ToggleButton) mRootView.findViewById(R.id.crossword_panel_switcher);

            pCurrentCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_current_crossword_container);
            pRatioText = (TextView) mRootView.findViewById(R.id.crossword_panel_ratio);
            pProgressText = (TextView) mRootView.findViewById(R.id.crossword_panel_percent);
            pProgressBackground = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_progress_bg);
            pProgressForeround = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_progress_fg);
            pScoreText = (TextView) mRootView.findViewById(R.id.crossword_panel_score);

            pBuyCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_crossword_container);
            pBuyCountText = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_count);
            pBuyScore = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_score);
            pBuyButton = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_button);
            pBuyPrice = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_price);

            pBadgeContainer = (BadgeGridView) mRootView.findViewById(R.id.crossword_panel_badges_container);
        }
    }

    public void addPanel(@Nonnull PuzzleSet set)
    {
        CrosswordPanelData data = new CrosswordPanelData();
        data.mId = set.id;
        data.mServerId = set.serverId;
        data.mKind = CrosswordPanelData.KIND_CURRENT;
        data.mType = PuzzleSetModel.getPuzzleTypeByString(set.type);
        data.mBought = set.isBought;
        data.mMonth = set.month;
        data.mYear = set.year;

        CrosswordSet crosswordSet = getCrosswordSet(data.mServerId);
        fillPanel(crosswordSet, data);
    }

    private void fillPanel(@Nonnull CrosswordSet crosswordSet, @Nonnull CrosswordPanelData data)
    {

        @Nonnull Bitmap bitmap = null;

        if (data.mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
        {
            crosswordSet.pBuyPrice.setText(R.string.buy_three_dollar);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_br);
            crosswordSet.pTitleText.setText(R.string.puzzless_hint_brilliant_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.GOLD)
        {
            crosswordSet.pBuyPrice.setText(R.string.buy_two_dollar);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_au);
            crosswordSet.pTitleText.setText(R.string.puzzless_hint_gold_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER)
        {
            crosswordSet.pBuyPrice.setText(R.string.buy_two_dollar);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_ag);
            crosswordSet.pTitleText.setText(R.string.puzzless_hint_silver_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.FREE)
        {
            crosswordSet.pBuyPrice.setText(R.string.buy_free);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_fr);
            crosswordSet.pTitleText.setText(R.string.puzzless_hint_free_crossword);
        }
        crosswordSet.pTitleImage.setImageBitmap(bitmap);
//        bitmap.recycle();

        StringBuilder sbRatio = new StringBuilder();
        sbRatio.append(data.mResolveCount);
        sbRatio.append("/");
        sbRatio.append(data.mTotalCount);
        crosswordSet.pRatioText.setText(sbRatio.toString());

        StringBuilder sbProgress = new StringBuilder();
        sbProgress.append(data.mProgress);
        sbProgress.append("%");
        crosswordSet.pProgressText.setText(sbProgress.toString());

        crosswordSet.pScoreText.setText(String.valueOf(data.mScore));

        crosswordSet.pBuyCountText.setText(String.valueOf(data.mBuyCount));

        crosswordSet.pBuyScore.setText(String.valueOf(data.mBuyScore));

        if(data.mKind == CrosswordPanelData.KIND_CURRENT)
        {
            // Текущие наборы сетов сканвордов;
            if(data.mBought)
            {
                // Куплены;
                crosswordSet.pSwitcher.setVisibility(View.GONE);
                crosswordSet.pBuyCrosswordContaiter.setVisibility(View.GONE);
                crosswordSet.pMonthBackground.setVisibility(View.GONE);

                if(data.mBadgeData != null){
                    crosswordSet.pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType,data.mBadgeData));
                }
            }
            else
            {
                // Некуплены;
                crosswordSet.pSwitcher.setVisibility(View.GONE);
                crosswordSet.pCurrentCrosswordContaiter.setVisibility(View.GONE);
                crosswordSet.pMonthBackground.setVisibility(View.GONE);
                crosswordSet.pBadgeContainer.setVisibility(View.GONE);
            }
            // Если такого сета еще нет, то сохраняем его и добавляем на панель;
            if(!mListCrosswordSet.containsKey(data.mServerId))
            {
                mListCrosswordSet.put(data.mServerId, crosswordSet);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSet.mRootView);
            }
        }
        else
        {
            // Архивные наборы сетов сканвордов;
            crosswordSet.pTitleImage.setVisibility(View.GONE);
            crosswordSet.pBuyCrosswordContaiter.setVisibility(View.GONE);
            if(data.mMonth == 0)
            {
                crosswordSet.pMonthBackground.setVisibility(View.GONE);
            }
            else
            {
                DateFormatSymbols symbols = new DateFormatSymbols();
                crosswordSet.pMonthText.setText(symbols.getMonths()[data.mMonth-1]);
            }

            if(data.mBadgeData != null){
                crosswordSet.pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType,data.mBadgeData));
            }

            // Если такого сета еще нет, то сохраняем его и добавляем на панель;
            if(!mListCrosswordSet.containsKey(data.mServerId))
            {
                mListCrosswordSet.put(data.mServerId, crosswordSet);
                mCrosswordPanelArchive.mCrosswordsContainerLL.addView(crosswordSet.mRootView);
            }

            final @Nonnull CrosswordSet crosswordSetFinal = crosswordSet;
            final @Nonnull CrosswordPanelData dataFinal = data;
            crosswordSet.pSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    crosswordSetFinal.pBadgeContainer.setVisibility(b ? View.VISIBLE : View.GONE);
                }
            });

            crosswordSet.pBuyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mICrosswordFragment.buyCrosswordSet(dataFinal.mServerId);
                }
            });

            crosswordSet.pBadgeContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    mICrosswordFragment.choiceCrossword();
                }
            });
        }



    }

}