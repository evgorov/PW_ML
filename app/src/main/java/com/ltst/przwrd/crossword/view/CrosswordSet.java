package com.ltst.przwrd.crossword.view;

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

import com.ltst.przwrd.R;
import com.ltst.przwrd.app.SharedPreferencesHelper;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.crossword.model.IPuzzleSetModel;
import com.ltst.przwrd.crossword.model.PuzzleSetModel;
import com.ltst.przwrd.manadges.IManadges;
import com.ltst.przwrd.manadges.IManageHolder;
import com.ltst.przwrd.manadges.ManageHolder;
import com.ltst.przwrd.sounds.SoundsWork;
import com.ltst.przwrd.tools.AnimationTools;
import com.ltst.przwrd.tools.CustomProgressBar;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import java.util.Calendar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 21.08.13.
 */
public class CrosswordSet {

    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_SUCCESS          = ManageHolder.GOOGLE_PLAY_TEST_PRODUCT_SUCCESS;
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_CANCEL           = ManageHolder.GOOGLE_PLAY_TEST_PRODUCT_CANCEL;
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_REFUNDED         = ManageHolder.GOOGLE_PLAY_TEST_PRODUCT_REFUNDED;
    static private final @Nonnull String GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE      = ManageHolder.GOOGLE_PLAY_TEST_PRODUCT_UNAVAILABLE;

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

    private static @Nonnull IManadges mIManadges;

    private static @Nonnull IManageHolder mIManageHolder;
    private @Nonnull CrosswordSetData mCrosswordSetData;

    private boolean flgOneRegister = false;
    private boolean mExpanding;

    public CrosswordSet(@Nonnull Context context, @Nonnull ICrosswordFragment iCrosswordFragment) {

        mExpanding = false;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mICrosswordFragment = iCrosswordFragment;

        mIManadges = (IManadges) context;
        mIManageHolder = mIManadges.getManadgeHolder();

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
                if (mCrosswordSetData.mServerId != null)
                {
                    if(mCrosswordSetData.mType != PuzzleSetModel.PuzzleSetType.FREE)
                    {
                        mIManageHolder.buyProduct(mCrosswordSetData.mServerId);
//                    mIManageHolder.buyProduct(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS);
                    }
                    else
                    {
                        mIManageHolder.uploadProduct(mCrosswordSetData.mServerId);
                    }
                }
            }
        });

        pBadgeContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long puzzleId) {
                SoundsWork.interfaceBtnMusic(mContext);
                if (mCrosswordSetData.mServerId != null)
                {
                    BadgeAdapter adapter = getAdapter();
                    BadgeData data = (BadgeData) adapter.getItem(position);
                    mICrosswordFragment.choicePuzzle(mCrosswordSetData.mServerId, data.mServerId);
                }
            }
        });

    }

    public @Nullable CrosswordSetData getCrosswordSetData()
    {
        return mCrosswordSetData;
    }

    public @Nonnull View getView()
    {
        return mRootView;
    }

    public @Nonnull BadgeAdapter getAdapter(){
        return (BadgeAdapter) pBadgeContainer.getAdapter();
    }

    public void fillPanel(@Nonnull CrosswordSetData data)
    {
//        mRootView.setVisibility(View.VISIBLE);
        mCrosswordSetData = data;
        mIManageHolder.registerProduct(mCrosswordSetData.mServerId);
        mManadgePriceListener.handle();

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

        pBuyCountText.setText(String.valueOf(data.mTotalCount));

        pBuyScore.setText(String.valueOf(data.mBuyScore));

        if(pBadgeContainer.getAdapter() == null)
            pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType));

        if(isPuzzleInCurrentMonth(data.mYear, data.mMonth))
        {
            // Текущие наборы сетов сканвордов;

            if(!flgOneRegister)
            {
                mIManageHolder.registerHandlerBuyProductEvent(mManadgeBuyProductIListener);
                mIManageHolder.registerHandlerPriceProductsChange(mManadgePriceListener);
                flgOneRegister = true;
            }

            mCrosswordSetType = CrosswordSetType.CURRENT;
            pTitleImage.setVisibility(View.VISIBLE);
            pSwitcher.setVisibility(View.GONE);
//            pMonthBackground.setVisibility(View.GONE);

            if(mCrosswordSetData.mServerId == null)
            {
                expandingBadgeContainer(true);
//                pBadgeContainer.setVisibility(View.VISIBLE);
            }

            if(data.mBought)
            {
                // Куплены;
                pBuyCrosswordContaiter.setVisibility(View.GONE);
                pCurrentCrosswordContaiter.setVisibility(View.VISIBLE);
                expandingBadgeContainer(true);
            }
            else
            {
                // Некуплены;
                pBuyCrosswordContaiter.setVisibility(View.VISIBLE);
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

            if(mCrosswordSetData.mServerId == null)
            {
                expandingBadgeContainer(false);
            }
            if(data.mMonth == 0)
            {
            }
            else
            {
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
            if(googleId.equals(mCrosswordSetData.mServerId))
//            if(googleId.equals(GOOGLE_PLAY_TEST_PRODUCT_SUCCESS))
            {

                mICrosswordFragment.waitLoader(true);
                final IPuzzleSetModel iPuzzleSetModel = mICrosswordFragment.getPuzzleSetModel();
                if(iPuzzleSetModel != null)
                {
                    iPuzzleSetModel.buyCrosswordSet(googleId, json, signature, new IListenerVoid() {

                        @Override
                        public void handle() {

                            if(iPuzzleSetModel.isAnswerState())
                            {
                                mIManageHolder.productBuyOnServer(googleId);
                                mICrosswordFragment.updateOneSet(mCrosswordSetData.mServerId);
                                mICrosswordFragment.purchaseResult(true);
                            }
                            else
                            {
                                mICrosswordFragment.purchaseResult(false);
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
            if(mCrosswordSetData.mType != PuzzleSetModel.PuzzleSetType.FREE)
            {
                @Nonnull String mBuyPrice = mIManageHolder.getPriceProduct(mCrosswordSetData.mServerId);
                pBuyPrice.setText(mBuyPrice);
            }
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
