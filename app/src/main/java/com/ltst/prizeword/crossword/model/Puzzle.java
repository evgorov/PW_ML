package com.ltst.prizeword.crossword.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Puzzle implements Parcelable
{
    public long id;
    public long setId;
    public @Nonnull String serverId;
    public @Nonnull String name;
    public @Nonnull String issuedAt;
    public int baseScore;
    public int timeGiven;
    public int timeLeft;
    public int score;
    public boolean isSolved;
    public @Nullable List<PuzzleQuestion> questions;
    public int solvedPercent;

    public Puzzle(long id,
                  long setId,
                  @Nonnull String serverId,
                  @Nonnull String name,
                  @Nonnull String issuedAt,
                  int baseScore,
                  int timeGiven,
                  int timeLeft,
                  int score,
                  boolean solved,
                  @Nullable List<PuzzleQuestion> questions)
    {
        this.id = id;
        this.setId = setId;
        this.serverId = serverId;
        this.name = name;
        this.issuedAt = issuedAt;
        this.baseScore = baseScore;
        this.timeGiven = timeGiven;
        this.timeLeft = timeLeft;
        this.score = score;
        this.isSolved = solved;
        this.questions = questions;
        solvedPercent = 0;
        countSolvedPercent();
    }

    //==== Parcelable implementation ==========================================
    public static Creator<Puzzle> CREATOR = new Creator<Puzzle>()
    {
        public Puzzle createFromParcel(Parcel source)
        {
            long id = source.readLong();
            long setId = source.readLong();
            @Nonnull String serverId = ParcelableTools.getNonnullString(source.readString());
            @Nonnull String name = ParcelableTools.getNonnullString(source.readString());
            @Nonnull String issuedAt = ParcelableTools.getNonnullString(source.readString());
            int baseScore = source.readInt();
            int timeGiven = source.readInt();
            int timeLeft = source.readInt();
            int score = source.readInt();
            boolean solved = ParcelableTools.getBooleanFromInt(source.readInt());
            @Nonnull List<PuzzleQuestion> questions = new ArrayList<PuzzleQuestion>();
            source.readTypedList(questions, PuzzleQuestion.CREATOR);

            return new Puzzle(id, setId, serverId, name, issuedAt,
                          baseScore, timeGiven, timeLeft, score,
                          solved, questions);
        }

        public Puzzle[] newArray(int size)
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
        dest.writeLong(setId);
        dest.writeString(serverId);
        dest.writeString(name);
        dest.writeString(issuedAt);
        dest.writeInt(baseScore);
        dest.writeInt(timeGiven);
        dest.writeInt(timeLeft);
        dest.writeInt(score);
        dest.writeInt(isSolved ? 1 : 0);
        dest.writeTypedList(questions);
        dest.writeInt(solvedPercent);
    }

    public void countSolvedPercent()
    {
        if (questions == null)
        {
            return;
        }
        int solved = 0;
        for (PuzzleQuestion question : questions)
        {
            if (question.isAnswered)
                solved ++;
        }
        solvedPercent = (int)((float)solved/(float)questions.size() * 100);
        if(solvedPercent == 100)
            isSolved = true;
    }
}
