package com.ltst.prizeword.manadges;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 29.08.13.
 */
public class Purchase implements Parcelable {

    public long id;
    public @Nonnull String clientId;
    public @Nonnull String googleId;
    public boolean googlePurchase;
    public boolean googleResetPurchase;
    public boolean serverPurchase;

    public Purchase()
    {

    }

    public Purchase(long id, @Nonnull String clientId, @Nonnull String googleId,
                    boolean googlePurchase, boolean googleResetPurchase, boolean serverPurchase)
    {
        this.id = id;
        this.clientId = clientId;
        this.googleId = googleId;
        this.googlePurchase = googlePurchase;
        this.googleResetPurchase = googleResetPurchase;
        this.serverPurchase = serverPurchase;
    }

    public static Creator<Purchase> CREATOR = new Creator<Purchase>() {
        @Nullable
        @Override
        public Purchase createFromParcel(Parcel source) {
            long id = source.readLong();
            @Nonnull String clientId = ParcelableTools.getNonnullString(source.readString());
            @Nonnull String googleId = ParcelableTools.getNonnullString(source.readString());;
            boolean googlePurchase = ParcelableTools.getBooleanFromInt(source.readInt());
            boolean googleResetPurchase = ParcelableTools.getBooleanFromInt(source.readInt());
            boolean serverPurchase = ParcelableTools.getBooleanFromInt(source.readInt());

            return new Purchase(id,clientId,googleId,googlePurchase,googleResetPurchase,serverPurchase);
        }

        @Override
        public Purchase[] newArray(int size) {
            return null;
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(clientId);
        dest.writeString(googleId);
        dest.writeInt(googlePurchase ? 1 : 0);
        dest.writeInt(googleResetPurchase ? 1 : 0);
        dest.writeInt(serverPurchase ? 1 : 0);
    }
}
