package com.ltst.prizeword.rating.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UsersList implements Parcelable
{
    public @Nonnull User me;
    public @Nonnull List<User> otherUsers;

    public UsersList(@Nonnull User me, @Nonnull List<User> otherUsers)
    {
        this.me = me;
        this.otherUsers = otherUsers;
    }

    //==== Parcelable implementation ==========================================
    public static Creator<UsersList> CREATOR = new Creator<UsersList>()
    {
        @org.jetbrains.annotations.Nullable
        @Override
        public UsersList createFromParcel(Parcel source)
        {
            @Nonnull User me = source.readParcelable(User.class.getClassLoader());
            @Nonnull List<User> users = new ArrayList<User>();
            source.readTypedList(users, User.CREATOR);
            return new UsersList(me, users);
        }

        @Override
        public UsersList[] newArray(int size)
        {
            return new UsersList[0];
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
        dest.writeParcelable(me, 0);
        dest.writeTypedList(otherUsers);
    }

    public static class User implements Parcelable
    {
        public long idLong;
        public @Nonnull String id;
        public @Nonnull String name;
        public @Nonnull String surname;
        public @Nonnull String email;
        public @Nonnull String city;
        public int solved;
        public int position;
        public int monthScore;
        public int highScore;
        public int dynamics;
        public final @Nonnull String previewUrl;
        public final @Nullable byte[] pngImage;
        public boolean me;


        public User(long idLong,
                    @Nonnull String id,
                    @Nonnull String name,
                    @Nonnull String surname,
                    @Nonnull String city,
                    int solved,
                    int position,
                    int monthScore,
                    int highScore,
                    int dynamics,
                    @Nonnull String previewUrl,
                    @Nullable byte[] pngImage)
        {
            this.idLong = idLong;
            this.id = id;
            this.name = name;
            this.surname = surname;
            this.city = city;
            this.solved = solved;
            this.position = position;
            this.monthScore = monthScore;
            this.highScore = highScore;
            this.dynamics = dynamics;
            this.previewUrl = previewUrl;
            this.pngImage = pngImage;
            me = false;
        }

        //==== Parcelable implementation ==========================================
        public static Creator<User> CREATOR = new Creator<User>()
        {
            public User createFromParcel(Parcel source)
            {
                long idLong = source.readLong();
                String id = ParcelableTools.getNonnullString(source.readString());
                String name = ParcelableTools.getNonnullString(source.readString());
                String surname = ParcelableTools.getNonnullString(source.readString());
                String city = ParcelableTools.getNonnullString(source.readString());
                int solved = source.readInt();
                int position = source.readInt();
                int monthScore = source.readInt();
                int highScore = source.readInt();
                int dynamics = source.readInt();

                String previewUrl = ParcelableTools.getNonnullString(source.readString());
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
                return new User(idLong, id, name,
                        surname, city,
                        solved, position, monthScore, highScore,
                        dynamics, previewUrl, pngImage);
            }

            public User[] newArray(int size)
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
            dest.writeLong(idLong);
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(surname);
            dest.writeString(city);
            dest.writeInt(solved);
            dest.writeInt(position);
            dest.writeInt(monthScore);
            dest.writeInt(highScore);
            dest.writeInt(dynamics);
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

    }
}
