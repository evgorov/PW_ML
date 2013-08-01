package com.ltst.prizeword.login.model;

import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 01.08.13.
 */
public class UserImage implements Parcelable {

    public long id;
    public @Nonnull String key;
    public @Nullable byte[] image;

    public UserImage(long id, @Nonnull String key, @Nullable byte[] image) {
        this.id = id;
        this.key = key;
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeLong(id);
        dest.writeString(key);

        byte[] arr = image;
        if(arr != null)
        {
            dest.writeInt(arr.length);
            dest.writeByteArray(image);
        }
        else
        {
            dest.writeInt(0);
        }
    }
}
