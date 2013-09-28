package com.ltst.przwrd.score;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.przwrd.tools.ParcelableTools;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScoreQueue implements Parcelable
{
    public List<Score> scoreQueue;

    public ScoreQueue(List<Score> scoreQueue)
    {
        this.scoreQueue = scoreQueue;
    }

    public static final Creator<ScoreQueue> CREATOR = new Creator<ScoreQueue>()
    {
        @Nullable
        @Override
        public ScoreQueue createFromParcel(Parcel source)
        {
            List<Score> queue = new ArrayList<Score>();
            source.readTypedList(queue, Score.CREATOR);
            return new ScoreQueue(queue);
        }

        @Override
        public ScoreQueue[] newArray(int size)
        {
            return new ScoreQueue[0];
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
        dest.writeTypedList(scoreQueue);
    }

    public static class Score implements Parcelable
    {
        public long id;
        public int score;
        public String puzzleId;

        public Score(long id, int score, String puzzleId)
        {
            this.id = id;
            this.score = score;
            this.puzzleId = puzzleId;
        }

        public static final Creator<Score> CREATOR = new Creator<Score>()
        {
            @Nullable
            @Override
            public Score createFromParcel(Parcel source)
            {
                long id = source.readLong();
                int score = source.readInt();
                String puzzleId = ParcelableTools.getNonnullString(source.readString());
                return new Score(id, score, puzzleId);
            }

            @Override
            public Score[] newArray(int size)
            {
                return new Score[0];
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
            dest.writeInt(score);
            dest.writeString(puzzleId);
        }
    }
}
