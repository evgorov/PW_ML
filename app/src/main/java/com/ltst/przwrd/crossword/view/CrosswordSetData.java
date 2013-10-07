package com.ltst.przwrd.crossword.view;

import com.ltst.przwrd.crossword.model.PuzzleSetModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 14.08.13.
 */
public class CrosswordSetData {

    final static public int KIND_CURRENT = 1;
    final static public int KIND_ARCHIVE = 2;

    public long mId = 0;
    public @Nonnull String mServerId = null;
    public int mKind = 0;
    public @Nonnull PuzzleSetModel.PuzzleSetType mType = null;
    public boolean mBought = false;
    public int mResolveCount = 0;
    public int mTotalCount = 0;
    public int mProgress = 0;
    public int mScore = 0;
    public int mBuyCount = 0;
    public int mBuyScore = 0;

    public int mMonth = 0;
    public int mYear = 1900;
    public @Nullable BadgeData[] mBadgeData = null;

    public boolean mFirst = false;
    public boolean mLAst = false;

    public CrosswordSetData() {
    }
}
