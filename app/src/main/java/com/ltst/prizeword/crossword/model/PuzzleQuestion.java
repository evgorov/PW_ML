package com.ltst.prizeword.crossword.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import javax.annotation.Nonnull;

import static com.ltst.prizeword.crossword.engine.PuzzleTileState.*;

public class PuzzleQuestion implements Parcelable
{
    public long id;
    public long puzzleId;
    public int column;
    public int row;
    public @Nonnull String questionText;
    public @Nonnull String answer;
    public @Nonnull String answerPosition;
    public boolean isAnswered;

    public PuzzleQuestion(long id, long puzzleId, int column, int row,
                          @Nonnull String quesitonText,
                          @Nonnull String answer,
                          @Nonnull String answerPosition,
                          boolean is_answered)
    {
        this.id = id;
        this.puzzleId = puzzleId;
        this.column = column;
        this.row = row;
        this.questionText = quesitonText;
        this.answer = answer;
        this.answerPosition = answerPosition;
        isAnswered = is_answered;
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
            boolean correct = ParcelableTools.getBooleanFromInt(source.readInt());
            PuzzleQuestion q = new PuzzleQuestion(id, puzzleId, col, row, text, answer, answerPosition, correct);
            return q;
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
        dest.writeString(questionText);
        dest.writeString(answer);
        dest.writeString(answerPosition);
        dest.writeInt(isAnswered ? 1 : 0);
    }

    public int getAnswerPosition()
    {
        if(answerPosition.equals(ANSPOS_NORTH_LEFT))
            return ArrowType.NORTH_LEFT;
        if(answerPosition.equals(ANSPOS_NORTH_TOP))
            return ArrowType.NORTH_TOP;
        if(answerPosition.equals(ANSPOS_NORTH_RIGHT))
            return ArrowType.NORTH_RIGHT;
        if(answerPosition.equals(ANSPOS_NORTH_EAST_LEFT))
            return ArrowType.NORTH_EAST_LEFT;
        if(answerPosition.equals(ANSPOS_NORTH_EAST_BOTTOM))
            return ArrowType.NORTH_EAST_BOTTOM;
        if(answerPosition.equals(ANSPOS_NORTH_EAST_RIGHT))
            return ArrowType.NORTH_EAST_RIGHT;
        if(answerPosition.equals(ANSPOS_NORTH_EAST_TOP))
            return ArrowType.NORTH_EAST_TOP;
        if(answerPosition.equals(ANSPOS_EAST_TOP))
            return ArrowType.EAST_TOP;
        if(answerPosition.equals(ANSPOS_EAST_RIGHT))
            return ArrowType.EAST_RIGHT;
        if(answerPosition.equals(ANSPOS_EAST_BOTTOM))
            return ArrowType.EAST_BOTTOM;
        if(answerPosition.equals(ANSPOS_SOUTH_EAST_LEFT))
            return ArrowType.SOUTH_EAST_LEFT;
        if(answerPosition.equals(ANSPOS_SOUTH_EAST_TOP))
            return ArrowType.SOUTH_EAST_TOP;
        if(answerPosition.equals(ANSPOS_SOUTH_EAST_BOTTOM))
            return ArrowType.SOUTH_EAST_BOTTOM;
        if(answerPosition.equals(ANSPOS_SOUTH_EAST_RIGHT))
            return ArrowType.SOUTH_EAST_RIGHT;
        if(answerPosition.equals(ANSPOS_SOUTH_LEFT))
            return ArrowType.SOUTH_LEFT;
        if(answerPosition.equals(ANSPOS_SOUTH_RIGHT))
            return ArrowType.SOUTH_RIGHT;
        if(answerPosition.equals(ANSPOS_SOUTH_BOTTOM))
            return ArrowType.SOUTH_BOTTOM;
        if(answerPosition.equals(ANSPOS_SOUTH_WEST_TOP))
            return ArrowType.SOUTH_WEST_TOP;
        if(answerPosition.equals(ANSPOS_SOUTH_WEST_RIGHT))
            return ArrowType.SOUTH_WEST_RIGHT;
        if(answerPosition.equals(ANSPOS_SOUTH_WEST_BOTTOM))
            return ArrowType.SOUTH_WEST_BOTTOM;
        if(answerPosition.equals(ANSPOS_SOUTH_WEST_LEFT))
            return ArrowType.SOUTH_WEST_LEFT;
        if(answerPosition.equals(ANSPOS_WEST_LEFT))
            return ArrowType.WEST_LEFT;
        if(answerPosition.equals(ANSPOS_WEST_TOP))
            return ArrowType.WEST_TOP;
        if(answerPosition.equals(ANSPOS_WEST_BOTTOM))
            return ArrowType.WEST_BOTTOM;
        if(answerPosition.equals(ANSPOS_NORTH_WEST_RIGHT))
            return ArrowType.NORTH_WEST_RIGHT;
        if(answerPosition.equals(ANSPOS_NORTH_WEST_BOTTOM))
            return ArrowType.NORTH_WEST_BOTTOM;
        if(answerPosition.equals(ANSPOS_NORTH_WEST_LEFT))
            return ArrowType.NORTH_WEST_LEFT;
        if(answerPosition.equals(ANSPOS_NORTH_WEST_TOP))
            return ArrowType.NORTH_WEST_TOP;
        return 0;
    }

    private static final @Nonnull String ANSPOS_NORTH_LEFT = "north:left";
    private static final @Nonnull String ANSPOS_NORTH_TOP = "north:top";
    private static final @Nonnull String ANSPOS_NORTH_RIGHT = "north:right";

    private static final @Nonnull String ANSPOS_NORTH_EAST_LEFT = "north-east:left";
    private static final @Nonnull String ANSPOS_NORTH_EAST_BOTTOM = "north-east:bottom";
    private static final @Nonnull String ANSPOS_NORTH_EAST_TOP = "north-east:top";
    private static final @Nonnull String ANSPOS_NORTH_EAST_RIGHT = "north-east:right";

    private static final @Nonnull String ANSPOS_EAST_TOP = "east:top";
    private static final @Nonnull String ANSPOS_EAST_RIGHT = "east:right";
    private static final @Nonnull String ANSPOS_EAST_BOTTOM = "east:bottom";

    private static final @Nonnull String ANSPOS_SOUTH_EAST_LEFT = "south-east:left";
    private static final @Nonnull String ANSPOS_SOUTH_EAST_TOP = "south-east:top";
    private static final @Nonnull String ANSPOS_SOUTH_EAST_RIGHT = "south-east:right";
    private static final @Nonnull String ANSPOS_SOUTH_EAST_BOTTOM = "south-east:bottom";

    private static final @Nonnull String ANSPOS_SOUTH_LEFT = "south:left";
    private static final @Nonnull String ANSPOS_SOUTH_RIGHT = "south:right";
    private static final @Nonnull String ANSPOS_SOUTH_BOTTOM = "south:bottom";

    private static final @Nonnull String ANSPOS_SOUTH_WEST_TOP = "south-west:top";
    private static final @Nonnull String ANSPOS_SOUTH_WEST_RIGHT = "south-west:right";
    private static final @Nonnull String ANSPOS_SOUTH_WEST_BOTTOM = "south-west:bottom";
    private static final @Nonnull String ANSPOS_SOUTH_WEST_LEFT = "south-west:left";

    private static final @Nonnull String ANSPOS_WEST_LEFT = "west:left";
    private static final @Nonnull String ANSPOS_WEST_TOP = "west:top";
    private static final @Nonnull String ANSPOS_WEST_BOTTOM = "west:bottom";

    private static final @Nonnull String ANSPOS_NORTH_WEST_RIGHT = "north-west:right";
    private static final @Nonnull String ANSPOS_NORTH_WEST_BOTTOM = "north-west:bottom";
    private static final @Nonnull String ANSPOS_NORTH_WEST_LEFT = "north-west:left";
    private static final @Nonnull String ANSPOS_NORTH_WEST_TOP = "north-west:top";

}
