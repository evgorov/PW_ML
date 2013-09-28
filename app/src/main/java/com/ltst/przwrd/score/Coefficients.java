package com.ltst.przwrd.score;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.Nullable;

public class Coefficients implements Parcelable
{
    public long id;
    public int timeBonus;
    public int friendBonus;
    public int freeBaseScore;
    public int goldBaseScore;
    public int brilliantBaseScore;
    public int silver1BaseScore;
    public int silver2BaseScore;

    public Coefficients(long id, int timeBonus, int friendBonus, int freeBaseScore, int goldBaseScore, int brilliantBaseScore, int silver1BaseScore, int silver2BaseScore)
    {
        this.id = id;
        this.timeBonus = timeBonus;
        this.friendBonus = friendBonus;
        this.freeBaseScore = freeBaseScore;
        this.goldBaseScore = goldBaseScore;
        this.brilliantBaseScore = brilliantBaseScore;
        this.silver1BaseScore = silver1BaseScore;
        this.silver2BaseScore = silver2BaseScore;
    }

    public static Creator<Coefficients> CREATOR = new Creator<Coefficients>()
    {
        @Nullable
        @Override
        public Coefficients createFromParcel(Parcel source)
        {
            long id = source.readLong();
            int timeBonus = source.readInt();
            int friendBonus = source.readInt();
            int freeBaseScore = source.readInt();
            int goldBaseScore = source.readInt();
            int brilliantBaseScore = source.readInt();
            int silver1BaseScore = source.readInt();
            int silver2BaseScore = source.readInt();
            return new Coefficients(id, timeBonus, friendBonus, freeBaseScore, goldBaseScore, brilliantBaseScore, silver1BaseScore, silver2BaseScore);
        }

        @Override
        public Coefficients[] newArray(int size)
        {
            return new Coefficients[0];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(id);
        dest.writeInt(timeBonus);
        dest.writeInt(friendBonus);
        dest.writeInt(freeBaseScore);
        dest.writeInt(goldBaseScore);
        dest.writeInt(brilliantBaseScore);
        dest.writeInt(silver1BaseScore);
        dest.writeInt(silver2BaseScore);
    }
}
