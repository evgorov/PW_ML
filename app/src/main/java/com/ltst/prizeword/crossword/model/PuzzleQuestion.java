package com.ltst.prizeword.crossword.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import javax.annotation.Nonnull;

public class PuzzleQuestion implements Parcelable
{
    public long id;
    public long puzzleId;
    public int column;
    public int row;
    public @Nonnull String quesitonText;
    public @Nonnull String answer;
    public @Nonnull String answerPosition;

    public PuzzleQuestion(long id, long puzzleId, int column, int row, @Nonnull String quesitonText, @Nonnull String answer, @Nonnull String answerPosition)
    {
        this.id = id;
        this.puzzleId = puzzleId;
        this.column = column;
        this.row = row;
        this.quesitonText = quesitonText;
        this.answer = answer;
        this.answerPosition = answerPosition;
    }

    //==== Parcelable implementation ==========================================
    public static Creator<PuzzleQuestion> CREATOR = new Creator<PuzzleQuestion>()
    {
        public PuzzleQuestion createFromParcel(Parcel source)
        {
            long id = source.readLong();
            long puzzleId = source.readLong();
            int col = source.readInt();
            int row = source.readInt();
            @Nonnull String text = ParcelableTools.getNonnullString(source.readString());
            @Nonnull String answer = ParcelableTools.getNonnullString(source.readString());
            @Nonnull String answerPosition = ParcelableTools.getNonnullString(source.readString());
            return new PuzzleQuestion(id, puzzleId, col, row, text, answer, answerPosition);
        }

        public PuzzleQuestion[] newArray(int size)
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
        dest.writeLong(puzzleId);
        dest.writeInt(column);
        dest.writeInt(row);
        dest.writeString(quesitonText);
        dest.writeString(answer);
        dest.writeString(answerPosition);
    }

}
