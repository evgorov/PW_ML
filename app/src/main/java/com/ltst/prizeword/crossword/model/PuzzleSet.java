package com.ltst.prizeword.crossword.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class PuzzleSet implements Parcelable
{
    public long id;
    public @Nonnull String serverId;
    public @Nonnull String name;
    public boolean isBought;
    public @Nonnull String type;
    public int month;
    public int year;
    public @Nonnull String createdAt;
    public boolean isPublished;
    public @Nonnull List<String> puzzlesId;

    public PuzzleSet(long id,
                     @Nonnull String serverId,
                     @Nonnull String name,
                     boolean bought,
                     @Nonnull String type,
                     int month,
                     int year,
                     @Nonnull String createdAt,
                     boolean published,
                     @Nonnull List<String> puzzlesId)
    {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
        this.isBought = bought;
        this.type = type;
        this.month = month;
        this.year = year;
        this.createdAt = createdAt;
        this.isPublished = published;
        this.puzzlesId = puzzlesId;
        Collections.sort(this.puzzlesId);
    }

    //==== Parcelable implementation ==========================================
    public static Creator<PuzzleSet> CREATOR = new Creator<PuzzleSet>()
    {
        public PuzzleSet createFromParcel(Parcel source)
        {
            long id = source.readLong();
            @Nonnull String serverId = ParcelableTools.getNonnullString(source.readString());
            @Nonnull String name = ParcelableTools.getNonnullString(source.readString());
            boolean bought = ParcelableTools.getBooleanFromInt(source.readInt());
            @Nonnull String type = ParcelableTools.getNonnullString(source.readString());
            int month = source.readInt();
            int year = source.readInt();
            @Nonnull String createdAt = ParcelableTools.getNonnullString(source.readString());
            boolean published = ParcelableTools.getBooleanFromInt(source.readInt());
            @Nonnull List<String> puzzlesId = new ArrayList<String>();
            source.readStringList(puzzlesId);

            return new PuzzleSet(id, serverId, name, bought,
                                type, month, year, createdAt,
                                published, puzzlesId);
        }

        public PuzzleSet[] newArray(int size)
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
        dest.writeString(serverId);
        dest.writeString(name);
        dest.writeInt(isBought ? 1 : 0);
        dest.writeString(type);
        dest.writeInt(month);
        dest.writeInt(year);
        dest.writeString(createdAt);
        dest.writeInt(isPublished ? 1 : 0);
        dest.writeStringList(puzzlesId);
    }

}
