package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesHelper;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.manadges.IManadges;
import com.ltst.prizeword.manadges.IManageHolder;
import com.ltst.prizeword.manadges.ManageHolder;
import com.ltst.prizeword.sounds.SoundsWork;
import com.ltst.prizeword.tools.AnimationTools;
import com.ltst.prizeword.tools.CustomProgressBar;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import java.util.Calendar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 21.08.13.
 */
public class CrosswordSet {

    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_SUCCESS          = "android.test.purchased";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_CANCEL           = "android.test.canceled";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_REFUNDED         = "android.test.refunded";
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE      = "android.test.unavailable";

    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull Context mContext;
    private @Nonnull LayoutInflater mInflater;

    private @Nonnull CrosswordSetType mCrosswordSetType;

    private @Nonnull View mRootView;
    private @Nonnull RelativeLayout pMonthBackground;
    private @Nonnull TextView pMonthText;
    private @Nonnull LinearLayout pTitleImage;
    private @Nonnull TextView pTitleText;
    private @Nonnull ToggleButton pSwitcher;

    private @Nonnull LinearLayout pCurrentCrosswordContaiter;
    private @Nonnull TextView pRatioText;
    private @Nonnull TextView pProgressText;
    private @Nonnull CustomProgressBar pProgress;
    private @Nonnull TextView pScoreText;

    private @Nonnull LinearLayout pBuyCrosswordContaiter;
    private @Nonnull TextView pBuyCountText;
    private @Nonnull TextView pBuyScore;
    private @Nonnull LinearLayout pBuyButton;
    private @Nonnull TextView pBuyPrice;

    private @Nonnull BadgeGridView pBadgeContainer;
    private @Nonnull RelativeLayout mLayout;

    private @Nullable String mSetServerId = null;
    private @Nonnull PuzzleSetModel.PuzzleSetType mPuzzleSetType;
    private boolean mExpanding;

    private static @Nonnull IManadges mIManadges;
    private static @Nonnull IManageHolder mIManageHolder;



    public CrosswordSet(@Nonnull Context context, @Nonnull ICrosswordFragment iCrosswordFragment) {

        mExpanding = false;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mICrosswordFragment = iCrosswordFragment;

        mIManadges = (IManadges) context;
        mIManageHolder = mIManadges.getManadgeHolder();
        mIManageHolder.registerHandlerBuyProductEvent(mManadgeBuyProductIListener);
        mIManageHolder.registerHandlerPriceProductsChange(mManadgePriceListener);

        mRootView =  mInflater.inflate(R.layout.crossword_panel, null, false);
        mLayout = (RelativeLayout) mRootView.findViewById(R.id.crossword_123);
        pMonthBackground = (RelativeLayout) mRootView.findViewById(R.id.crossword_panel_splitter_month_bg);
        pMonthText = (TextView) mRootView.findViewById(R.id.crossword_panel_splitter_month_text);
        pTitleImage = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_logo_image);
        pTitleText = (TextView) mRootView.findViewById(R.id.crossword_panel_title_text);
        pSwitcher = (ToggleButton) mRootView.findViewById(R.id.crossword_panel_switcher);

        pCurrentCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_current_crossword_container);
        pRatioText = (TextView) mRootView.findViewById(R.id.crossword_panel_ratio);
        pProgressText = (TextView) mRootView.findViewById(R.id.crossword_panel_percent);
        pProgress = new CustomProgressBar(context, mRootView, R.id.crossword_panel_progress_bg, R.id.crossword_panel_progress_fg);
        pScoreText = (TextView) mRootView.findViewById(R.id.crossword_panel_score);

        pBuyCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_crossword_container);
        pBuyCountText = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_count);
        pBuyScore = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_score);
        pBuyButton = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_button);
        pBuyPrice = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_price);

        pBadgeContainer = (BadgeGridView) mRootView.findViewById(R.id.crossword_panel_badges_container);

        pMonthBackground.setVisibility(View.GONE);

        pSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                pBadgeContainer.setVisibility(b ? View.GONE : View.VISIBLE);
                expandingBadgeContainer(!b);
            }
        });

        pBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundsWork.buySet(mContext);
                if (mSetServerId != null)
                {
                    mIManageHolder.buyProduct(mSetServerId);
//                    mIManageHolder.buyProduct(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS);
                }
            }
        });

        pBadgeContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long puzzleId) {
                SoundsWork.interfaceBtnMusic(mContext);
                if (mSetServerId != null)
                {
                    mICrosswordFragment.choicePuzzle(mSetServerId, puzzleId);
                }
            }
        });

    }

    public @Nonnull View getView()
    {
        return mRootView;
    }

    public @Nonnull BadgeAdapter getAdapter(){
        return (BadgeAdapter) pBadgeContainer.getAdapter();
    }

    public void fillPanel(@Nonnull CrosswordPanelData data)
    {
//        mRootView.setVisibility(View.VISIBLE);
        mSetServerId = data.mServerId;
        mIManageHolder.registerProduct(mSetServerId);
        mManadgePriceListener.handle();
        mPuzzleSetType = data.mType;

        if (data.mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_br));
            pTitleText.setText(R.string.puzzless_hint_brilliant_crossword);
            pTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.puzzles_badge_title_text_size));
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.GOLD)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_au));
            pTitleText.setText(R.string.puzzless_hint_gold_crossword);
            pTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.puzzles_badge_title_text_size));
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag));
            pTitleText.setText(R.string.puzzless_hint_silver_crossword);
            pTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.puzzles_badge_title_text_size));
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER2)
        {
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag2));
            pTitleText.setText(R.string.puzzless_hint_silver2_crossword);
            pTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.puzzles_badge_title_long_text_size));
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.FREE)
        {
            pBuyPrice.setText(R.string.buy_free);
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_fr));
            pTitleText.setText(R.string.puzzless_hint_free_crossword);
            pTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.puzzles_badge_title_text_size));
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

        pBuyCountText.setText(String.valueOf(data.mBuyCount));

        pBuyScore.setText(String.valueOf(data.mBuyScore));

        if(pBadgeContainer.getAdapter() == null)
            pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType));

        if(isPuzzleInCurrentMonth(data.mYear, data.mMonth))
        {
            // Текущие наборы сетов сканвордов;
            mCrosswordSetType = CrosswordSetType.CURRENT;
            pTitleImage.setVisibility(View.VISIBLE);
            pSwitcher.setVisibility(View.GONE);
//            pMonthBackground.setVisibility(View.GONE);

            if(mSetServerId == null)
            {
                expandingBadgeContainer(true);
//                pBadgeContainer.setVisibility(View.VISIBLE);
            }

            if(data.mBought)
            {
                // Куплены;
                pBuyCrosswordContaiter.setVisibility(View.GONE);
                expandingBadgeContainer(true);
            }
            else
            {
                // Некуплены;
                pCurrentCrosswordContaiter.setVisibility(View.GONE);
//                pBadgeContainer.setVisibility(View.GONE);
                expandingBadgeContainer(false);
            }
        }
        else
        {
            if(!data.mBought)
            {
                mRootView.setVisibility(View.GONE);
            }

            // Архивные наборы сетов сканвордов;
            mCrosswordSetType = CrosswordSetType.ARCHIVE;
            pTitleImage.setVisibility(View.VISIBLE);
            pBuyCrosswordContaiter.setVisibility(View.GONE);

            if(mSetServerId == null)
            {
//                pBadgeContainer.setVisibility(View.GONE);
                expandingBadgeContainer(false);
            }
            if(data.mMonth == 0)
            {
            }
            else
            {
//                DateFormatSymbols symbols = new DateFormatSymbols();
//                pMonthText.setText(symbols.getMonths()[data.mMonth-1]);
                pMonthText.setText(mContext.getResources().getStringArray(
                        R.array.menu_group_months_at_imenit_padezh)[data.mMonth-1]);
            }
        }
    }

    public void setVisibleMonth(boolean visible)
    {
        pMonthBackground.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public CrosswordSetType getCrosswordSetType()
    {
        return mCrosswordSetType;
    }

    private void expandingBadgeContainer(boolean expand)
    {
        mExpanding = expand;

        if(mExpanding)
        {
            AnimationTools.expand(mLayout, pBadgeContainer);
            SoundsWork.openSet(mContext);
        }
        else
        {
            AnimationTools.collapse(mLayout, pBadgeContainer);
            SoundsWork.closeSet(mContext);
        }
    }

    private @Nonnull
    IListener<Bundle> mManadgeBuyProductIListener = new IListener<Bundle>() {
        @Override
        public void handle(@Nullable Bundle bundle) {

            final @Nonnull String googleId = ManageHolder.extractFromBundleSKU(bundle);
            final @Nonnull String json = ManageHolder.extractFromBundleJson(bundle);
            final @Nonnull String signature = ManageHolder.extractFromBundleSignature(bundle);
            if(googleId.equals(mSetServerId))
//            if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
            {

                final IPuzzleSetModel iPuzzleSetModel = mICrosswordFragment.getPuzzleSetModel();
                if(iPuzzleSetModel != null)
                {
                    iPuzzleSetModel.buyCrosswordSet(googleId, json, signature, new IListenerVoid() {

                        @Override
                        public void handle() {

//                        @Nullable List<PuzzleSet> sets = mPuzzleSetModel.getPuzzleSets();
//                        @Nullable HashMap<String, List<Puzzle>> mapPuzzles = mPuzzleSetModel.getPuzzlesSet();

                            if(iPuzzleSetModel.isAnswerState())
                            {
                                mIManageHolder.productBuyOnServer(googleId);
                                mICrosswordFragment.updateCurrentSet();
                            }
                        }
                    });
                }
            }
        }
    };

    private @Nonnull
    IListenerVoid mManadgePriceListener = new IListenerVoid() {

        @Override
        public void handle() {
            @Nonnull String mBuyPrice = mIManageHolder.getPriceProduct(mSetServerId);
            pBuyPrice.setText(mBuyPrice);
        }
    };

    public boolean isPuzzleInCurrentMonth(int year, int month)
    {
        long currentTime = SharedPreferencesHelper.getInstance(mContext).getLong(SharedPreferencesValues.SP_CURRENT_DATE, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        return currentYear == year && currentMonth == month;
    }

        public enum CrosswordSetType{
        CURRENT,
        ARCHIVE
    }

}
