package com.ltst.prizeword.crossword.view;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 14.08.13.
 */
public class CrosswordPanelData {

    final static public int KIND_CURRENT = 1;
    final static public int KIND_ARCHIVE = 2;
    final static public int KIND_BUY = 3;

    final static public int TYPE_BRILLIANT = 1;
    final static public int TYPE_GOLD = 2;
    final static public int TYPE_SILVER = 3;
    final static public int TYPE_SILVER2 = 4;
    final static public int TYPE_FREE = 5;

    public int mKind;
    public int mType;
    public int mResolveCount;
    public int mTotalCount;
    public int mProgress;
    public int mScore;
    public @Nonnull String mMonth;
    public @Nullable BadgeData[] mBadgeData;

    public CrosswordPanelData() {
    }
}
