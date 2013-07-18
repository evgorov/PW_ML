package com.ltst.prizeword.login.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.omich.velo.constants.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UserProvider implements Parcelable
{
    public long id;
    public @Nonnull String name;
    public @Nonnull String providerId;
    public @Nonnull String providerToken;
    public long userId;

    public UserProvider(long id,
                        @Nonnull String name,
                        @Nonnull String providerId,
                        @Nonnull String providerToken,
                        long userId)
    {
        this.id = id;
        this.name = name;
        this.providerId = providerId;
        this.providerToken = providerToken;
        this.userId = userId;
    }

    //==== Parcelable implementation ==========================================
    public static Creator<UserProvider> CREATOR = new Creator<UserProvider>()
    {
        public UserProvider createFromParcel(Parcel source)
        {
            long id = source.readLong();
            String name = getNonnullString(source.readString());
            String providerId = getNonnullString(source.readString());
            String providerToken = getNonnullString(source.readString());
            long userId = source.readLong();
            return new UserProvider(id, name, providerId, providerToken, userId);
        }

        public UserProvider[] newArray(int size)
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
        dest.writeString(providerId);
        dest.writeString(providerToken);
        dest.writeLong(userId);
    }

    //=========================================================================
    private static @Nonnull String getNonnullString(@Nullable String string)
    {
        return string != null ? string : Strings.EMPTY;
    }
}
