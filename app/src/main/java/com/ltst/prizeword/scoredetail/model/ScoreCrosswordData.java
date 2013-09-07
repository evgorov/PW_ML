package com.ltst.prizeword.scoredetail.model;

import android.os.Parcel;

import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.tools.ParcelableTools;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class ScoreCrosswordData extends PuzzleSet
{
    public final @Nonnull String mTypeData;

    public ScoreCrosswordData(long id, @Nonnull String serverId, @Nonnull String name, boolean bought,
                              @Nonnull String type, int month, int year,
                              @Nonnull String createdAt, boolean published,
                              @Nonnull List<String> puzzlesId, @Nonnull String typeData)
    {
        super(id, serverId, name, bought, type, month, year, createdAt, published, puzzlesId);
        mTypeData = typeData;
    }

    public static Creator<PuzzleSet> CREATOR = new Creator<PuzzleSet>()
    {
        public ScoreCrosswordData createFromParcel(Parcel source)
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
            @Nonnull String typeData = ParcelableTools.getNonnullString(source.readString());

            return new ScoreCrosswordData(id, serverId, name, bought,
                    type, month, year, createdAt,
                    published, puzzlesId, typeData);
        }

        public ScoreCrosswordData[] newArray(int size)
        {
            return null;
        }
    };

    @Override public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeString(mTypeData);
    }
}
