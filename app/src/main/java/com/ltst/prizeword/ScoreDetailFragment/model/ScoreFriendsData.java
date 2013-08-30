package com.ltst.prizeword.scoredetailfragment.model;

import android.os.Parcel;

import com.ltst.prizeword.invitefriends.model.InviteFriendsData;
import com.ltst.prizeword.tools.ParcelableTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScoreFriendsData extends InviteFriendsData
{
    public final @Nonnull String mTypeData;

    public ScoreFriendsData(@Nonnull String firstName, @Nonnull String lastName,
                            @Nullable String deactivated, int online, long userId,
                            @Nonnull int[] lists, @Nonnull String id, @Nonnull String userpic,
                            @Nonnull String status, @Nullable byte[] pngImage,
                            @Nonnull String providerName, @Nonnull String typeData)
    {
        super(firstName, lastName, deactivated, online, userId, lists, id, userpic, status, pngImage, providerName);
        mTypeData = typeData;
    }

    public ScoreFriendsData(Parcel source)
    {
        super(source);
        this.mTypeData = ParcelableTools.getNonnullString(source.readString());
    }

    @Override public void writeToParcel(Parcel dest, int flag)
    {
        super.writeToParcel(dest, flag);
        dest.writeString(mTypeData);
    }
}
