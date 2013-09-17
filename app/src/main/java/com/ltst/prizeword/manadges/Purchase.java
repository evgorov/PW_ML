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
    public @Nonnull String price;
    public boolean googlePurchase;
    public boolean serverPurchase;
    public @Nonnull String receipt_data;
    public @Nonnull String signature;

    public Purchase()
    {

    }

    public Purchase(long id, @Nonnull String clientId, @Nonnull String googleId,
                    @Nonnull String price, boolean googlePurchase, boolean serverPurchase,
                    @Nonnull String receipt_data, @Nonnull String signature)
    {
        this.id = id;
        this.clientId = clientId;
        this.googleId = googleId;
        this.price = price;
        this.googlePurchase = googlePurchase;
        this.serverPurchase = serverPurchase;
        this.receipt_data = receipt_data;
        this.signature = signature;
    }

    public static Creator<Purchase> CREATOR = new Creator<Purchase>() {
        @Nullable
        @Override
        public Purchase createFromParcel(Parcel source) {
            long id = source.readLong();
            @Nonnull String clientId = ParcelableTools.getNonnullString(source.readString());
            @Nonnull String googleId = ParcelableTools.getNonnullString(source.readString());;
            @Nonnull String price = ParcelableTools.getNonnullString(source.readString());;
            boolean googlePurchase = ParcelableTools.getBooleanFromInt(source.readInt());
            boolean serverPurchase = ParcelableTools.getBooleanFromInt(source.readInt());
            @Nonnull String receipt_data = ParcelableTools.getNonnullString(source.readString());;
            @Nonnull String signature = ParcelableTools.getNonnullString(source.readString());;

            return new Purchase(id,clientId,googleId,price, googlePurchase,serverPurchase,receipt_data,signature);
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
        dest.writeString(price);
        dest.writeInt(googlePurchase ? 1 : 0);
        dest.writeInt(serverPurchase ? 1 : 0);
        dest.writeString(receipt_data);
        dest.writeString(signature);
    }
}
