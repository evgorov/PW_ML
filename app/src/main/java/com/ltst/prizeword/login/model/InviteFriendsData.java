package com.ltst.prizeword.login.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.omich.velo.constants.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InviteFriendsData implements Parcelable
{
    public final @Nonnull String provider;
    public final @Nonnull String id;
    public final @Nonnull String name;
    public final @Nonnull String email;
    public final boolean inviteSend;
    public final boolean inviteUsed;
    public final @Nonnull String inviteAt;
    public final @Nullable byte[] pngImage;

    public InviteFriendsData(@Nonnull String provider, @Nonnull String id, @Nonnull String name, @Nonnull String email, boolean inviteSend, boolean inviteUsed, @Nonnull String inviteAt, @Nullable byte[] pngImage)
    {
        this.provider = provider;
        this.id = id;
        this.name = getNonnullString(name);
        this.email = getNonnullString(email);
        this.inviteSend = inviteSend;
        this.inviteUsed = inviteUsed;
        this.inviteAt = getNonnullString(inviteAt);
        this.pngImage = pngImage;
    }

    //====Parcelable implementation =====================================================
    public static Creator<InviteFriendsData> CREATOR = new Creator<InviteFriendsData>()
    {
        @Override public InviteFriendsData createFromParcel(Parcel source)
        {
            return new InviteFriendsData(source);
        }

        @Override public InviteFriendsData[] newArray(int size)
        {
            return null;
        }
    };

    InviteFriendsData(Parcel source)
    {
        provider = source.readString();
        id = source.readString();
        name = getNonnullString(source.readString());
        email = getNonnullString(source.readString());

        boolean[] arr = new boolean[2];
        source.readBooleanArray(arr);
        inviteSend = arr[0];
        inviteUsed = arr[1];

        inviteAt = getNonnullString(source.readString());

        int bytesLength = source.readInt();

        if (bytesLength > 0)
        {
            pngImage = new byte[bytesLength];
            source.readByteArray(pngImage);
        } else
        {
            pngImage = null;
        }
    }

    @Override public int describeContents()
    {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flag)
    {
        dest.writeString(provider);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        boolean[] arr = new boolean[]{inviteSend,inviteUsed};
        dest.writeBooleanArray(arr);
        dest.writeString(inviteAt);

        byte[] arrByte = pngImage;
        if(arrByte!=null){
            dest.writeInt(arr.length);
            dest.writeByteArray(pngImage);
        }
        else{
            dest.writeInt(0);
        }
    }

    //===============================================================
    private static @Nonnull String getNonnullString(@Nullable String string)
    {
        return string != null ? string : Strings.EMPTY;
    }
}
