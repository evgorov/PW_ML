package com.ltst.przwrd.news;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.internal.cl;
import com.ltst.przwrd.tools.ParcelableTools;

import javax.annotation.Nullable;

public class News implements Parcelable
{
    public @Nullable String message1;
    public @Nullable String message2;
    public @Nullable String message3;
    public @Nullable String etagHash;
    public boolean closed;

    public News(String message1, String message2, String message3, String etagHash)
    {
        this.message1 = message1;
        this.message2 = message2;
        this.message3 = message3;
        this.etagHash = etagHash;
        closed = false;
    }

    public static Creator<News> CREATOR = new Creator<News>()
    {
        @Override
        public News createFromParcel(Parcel parcel)
        {
            String message1 = ParcelableTools.getNonnullString(parcel.readString());
            String message2 = ParcelableTools.getNonnullString(parcel.readString());
            String message3 = ParcelableTools.getNonnullString(parcel.readString());
            String hash = ParcelableTools.getNonnullString(parcel.readString());
            boolean closed = ParcelableTools.getBooleanFromInt(parcel.readInt());
            News news = new News(message1, message2, message3, hash);
            news.closed = closed;
            return news;
        }

        @Override
        public News[] newArray(int i)
        {
            return new News[0];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(message1);
        parcel.writeString(message2);
        parcel.writeString(message3);
        parcel.writeString(etagHash);
        parcel.writeInt(closed ? 1 : 0);
    }
}
