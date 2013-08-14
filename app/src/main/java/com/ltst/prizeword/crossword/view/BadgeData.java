package com.ltst.prizeword.crossword.view;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 13.08.13.
 */
public class BadgeData {

    final static public int TYPE_BRILLIANT = 1;
    final static public int TYPE_GOLD = 2;
    final static public int TYPE_SILVER = 3;
    final static public int TYPE_SILVER2 = 4;
    final static public int TYPE_FREE = 5;

    final static public int STATUS_RESOLVED = 6;
    final static public int STATUS_UNRESOLVED = 7;

    public int mStatus = 0;
    public int mProgress = 0;
    public int mScore = 0;
    public int mNumber = 0;

    public BadgeData() {
    }

}
