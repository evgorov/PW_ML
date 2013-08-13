package com.ltst.prizeword.crossword.view;

/**
 * Created by cosic on 13.08.13.
 */
public class BadgeData {

    final static public int TYPE_BRILLIANT = 1;
    final static public int TYPE_GOLD = 2;
    final static public int TYPE_SILVER = 3;
    final static public int TYPE_FREE = 4;

    final static public int STATUS_RESOLVED = 6;
    final static public int STATUS_UNRESOLVED = 7;

    public int mType;
    public int mStatus;
    public int mProgress;
    public int mScore;
    public int mNumber;

    public BadgeData() {
    }

}
