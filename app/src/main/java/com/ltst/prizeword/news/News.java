package com.ltst.prizeword.news;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import javax.annotation.Nullable;

public class News implements Parcelable
{
    public @Nullable String message1;
    public @Nullable String message2;
    public @Nullable String message3;

    public News(@Nullable String message1,@Nullable String message2,@Nullable String message3)
    {
        this.message1 = message1;
        this.message2 = message2;
        this.message3 = message3;
    }

    public static Creator<News> CREATOR = new Creator<News>()
    {
        @Override public News createFromParcel(Parcel parcel)
        {
            String message1= ParcelableTools.getNonnullString(parcel.readString());
            String message2= ParcelableTools.getNonnullString(parcel.readString());
            String message3= ParcelableTools.getNonnullString(parcel.readString());
            return new News(message1,message2,message3);
        }

        @Override public News[] newArray(int i)
        {
            return new News[0];
        }
    };

    @Override public int describeContents()
    {
        return 0;
    }

    @Override public void writeToParcel(Parcel parcel, int i)
    {
         parcel.writeString(message1);
         parcel.writeString(message2);
         parcel.writeString(message3);
    }
}
