package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 14.08.13.
 */
public class CrosswordPanelData {

    final static public int KIND_CURRENT = 1;
    final static public int KIND_ARCHIVE = 2;
    final static public int KIND_BUY = 3;

    public int mKind = 0;
    public @Nonnull PuzzleSetModel.PuzzleSetType mType = null;
    public int mResolveCount = 0;
    public int mTotalCount = 0;
    public int mProgress = 0;
    public int mScore = 0;
    public int mBuyCount = 0;
    public int mBuyScore = 0;

    public @Nonnull String mMonth = null;
    public @Nullable BadgeData[] mBadgeData = null;

    public CrosswordPanelData() {
    }
}
