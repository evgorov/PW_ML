package com.ltst.prizeword.invitefriends.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InviteFriendsData implements Parcelable
{
    public static final @Nonnull String NO_PROVIDER = "no_provider";

    public final @Nonnull String firstName;
    public final @Nonnull String lastName;
    public final @Nullable String deactivated;
    public final int online;
    public final long userId;
    public final @Nonnull int[] lists;
    public final @Nonnull String id;
    public final @Nonnull String userpic;
    public final @Nonnull String status;
    public final @Nullable byte[] pngImage;
    public final @Nonnull String providerName;

    public InviteFriendsData(@Nonnull String firstName, @Nonnull String lastName,
                             @Nullable String deactivated, int online, long userId, @Nonnull int[] lists,
                             @Nonnull String id, @Nonnull String userpic, @Nonnull String status,@Nullable byte[] pngImage,
                             @Nonnull String providerName)
    {
        this.firstName = ParcelableTools.getNonnullString(firstName);
        this.lastName = ParcelableTools.getNonnullString(lastName);
        this.deactivated = ParcelableTools.getNonnullString(deactivated);
        this.online = online;
        this.userId = userId;
        this.lists = lists;
        this.id = ParcelableTools.getNonnullString(id);
        this.userpic = ParcelableTools.getNonnullString(userpic);
        this.status = ParcelableTools.getNonnullString(status);
        this.pngImage = pngImage;
        this.providerName = providerName;
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
        firstName = ParcelableTools.getNonnullString(source.readString());
        lastName = ParcelableTools.getNonnullString(source.readString());
        deactivated = ParcelableTools.getNonnullString(source.readString());
        online = source.readInt();
        userId = source.readLong();

        int bytesLength = source.readInt();
        lists = new int[bytesLength];
        source.readIntArray(lists);

        id = ParcelableTools.getNonnullString(source.readString());

        userpic = ParcelableTools.getNonnullString(source.readString());
        status = ParcelableTools.getNonnullString(source.readString());
        bytesLength = source.readInt();

        if (bytesLength > 0)
        {
            pngImage = new byte[bytesLength];
            source.readByteArray(pngImage);
        } else
        {
            pngImage = null;
        }

        providerName = ParcelableTools.getNonnullString(source.readString());
    }

    @Override public int describeContents()
    {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flag)
    {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(deactivated);
        dest.writeInt(online);
        dest.writeLong(userId);
        dest.writeIntArray(lists);
        dest.writeString(id);
        dest.writeString(userpic);
        byte[] arrByte = pngImage;
        if (arrByte != null)
        {
            dest.writeInt(arrByte.length);
            dest.writeByteArray(pngImage);
        } else
        {
            dest.writeInt(0);
        }

        dest.writeString(providerName);
    }
}
