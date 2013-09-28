package com.ltst.przwrd.login.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.omich.velo.constants.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UserData implements Parcelable
{
    public long id;
    public @Nonnull String name;
    public @Nonnull String surname;
    public @Nonnull String email;
    public @Nonnull String bithdate;
    public @Nonnull String city;
    public int solved;
    public int position;
    public int monthScore;
    public int highScore;
    public int dynamics;
    public int hints;
    public final @Nonnull String previewUrl;
    public final @Nullable byte[] pngImage;

    public UserData(long id,
                    @Nonnull String name,
                    @Nonnull String surname,
                    @Nonnull String email,
                    @Nonnull String bithdate,
                    @Nonnull String city,
                    int solved,
                    int position,
                    int monthScore,
                    int highScore,
                    int dynamics,
                    int hints,
                    @Nonnull String previewUrl,
                    @Nullable byte[] pngImage)
    {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.bithdate = bithdate;
        this.city = city;
        this.solved = solved;
        this.position = position;
        this.monthScore = monthScore;
        this.highScore = highScore;
        this.dynamics = dynamics;
        this.hints = hints;
        this.previewUrl = previewUrl;
        this.pngImage = pngImage;
    }

    //==== Parcelable implementation ==========================================
    public static Creator<UserData> CREATOR = new Creator<UserData>()
    {
        public UserData createFromParcel(Parcel source)
        {
            long id = source.readLong();
            String name = getNonnullString(source.readString());
            String surname = getNonnullString(source.readString());
            String email = getNonnullString(source.readString());
            String provider = getNonnullString(source.readString());
            String birthdate = getNonnullString(source.readString());
            String city = getNonnullString(source.readString());
            int solved = source.readInt();
            int position = source.readInt();
            int monthScore = source.readInt();
            int highScore = source.readInt();
            int dynamics = source.readInt();
            int hints = source.readInt();

            String previewUrl = getNonnullString(source.readString());
            byte[] pngImage = null;
            int bytesLength = source.readInt();
            if(bytesLength > 0)
            {
                pngImage = new byte[bytesLength];
                source.readByteArray(pngImage);
            }
            else
            {
                pngImage = null;
            }
            return new UserData(id, name, surname,
                                email, birthdate, city,
                                solved, position, monthScore, highScore,
                                dynamics, hints, previewUrl, pngImage);
        }

        public UserData[] newArray(int size)
        {
            return null;
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
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(email);
        dest.writeString(bithdate);
        dest.writeString(city);
        dest.writeInt(solved);
        dest.writeInt(position);
        dest.writeInt(monthScore);
        dest.writeInt(highScore);
        dest.writeInt(dynamics);
        dest.writeInt(hints);
        dest.writeString(previewUrl);

        byte[] arr = pngImage;
        if(arr != null)
        {
            dest.writeInt(arr.length);
            dest.writeByteArray(pngImage);
        }
        else
        {
            dest.writeInt(0);
        }

    }

    //=========================================================================
    private static @Nonnull String getNonnullString(@Nullable String string)
    {
        return string != null ? string : Strings.EMPTY;
    }
}
