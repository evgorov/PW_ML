package com.ltst.prizeword.crossword.model;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import com.ltst.prizeword.tools.ParcelableTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleQuestion implements Parcelable
{
    public long id;
    public long puzzleId;
    public int column;
    public int row;
    public @Nonnull String questionText;
    public @Nonnull String answer;
    public @Nonnull String answerPosition;

    public PuzzleQuestion(long id, long puzzleId, int column, int row, @Nonnull String quesitonText, @Nonnull String answer, @Nonnull String answerPosition)
    {
        this.id = id;
        this.puzzleId = puzzleId;
        this.column = column;
        this.row = row;
        this.questionText = quesitonText;
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
        dest.writeString(questionText);
        dest.writeString(answer);
        dest.writeString(answerPosition);
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
        return 0;
    }

    //    north:left, north:top, north:right,
//    north-east:left, north-east:bottom,
//    east:top, east:right, east:bottom,
//    south-east:left, south-east:top,
//    south:left, south:right, south:bottom,
//    south-west:top, south-west:right,
//    west:left, west:top, west:bottom,
//    north-west:right, north-west:bottom
    public static class ArrowType
    {
        public static final int ARROW_TYPE_MASK     = 0x01111100;
        public static final int NO_ARROW            = 0x00000000;

        public static final int NORTH_LEFT          = 0x00000100;
        public static final int NORTH_TOP           = 0x00001000;
        public static final int NORTH_RIGHT         = 0x00001100;
        public static final int NORTH_EAST_LEFT     = 0x00010100;
        public static final int NORTH_EAST_BOTTOM   = 0x00011000;
        public static final int EAST_TOP            = 0x00011100;
        public static final int EAST_RIGHT          = 0x00100000;
        public static final int EAST_BOTTOM         = 0x00100100;
        public static final int SOUTH_EAST_LEFT     = 0x00101000;
        public static final int SOUTH_EAST_TOP      = 0x00101100;
        public static final int SOUTH_LEFT          = 0x00110000;
        public static final int SOUTH_RIGHT         = 0x00110100;
        public static final int SOUTH_BOTTOM        = 0x00111000;
        public static final int SOUTH_WEST_TOP      = 0x00111100;
        public static final int SOUTH_WEST_RIGHT    = 0x01000000;
        public static final int WEST_LEFT           = 0x01000100;
        public static final int WEST_TOP            = 0x01001000;
        public static final int WEST_BOTTOM         = 0x01001100;
        public static final int NORTH_WEST_RIGHT    = 0x01010000;
        public static final int NORTH_WEST_BOTTOM   = 0x01010100;

        public static @Nullable Point positionToPoint(int type, int col, int row)
        {
            Point p = null;
            switch (type)
            {
                case NORTH_RIGHT:
                case NORTH_TOP:
                case NORTH_LEFT:
                    p = new Point(col, row - 1);
                    break;
                case NORTH_EAST_BOTTOM:
                case NORTH_EAST_LEFT:
                    p = new Point(col + 1, row - 1);
                    break;
                case EAST_TOP:
                case EAST_RIGHT:
                case EAST_BOTTOM:
                    p = new Point(col + 1, row);
                    break;
                case SOUTH_EAST_LEFT:
                case SOUTH_EAST_TOP:
                    p = new Point(col + 1, row + 1);
                    break;
                case SOUTH_LEFT:
                case SOUTH_RIGHT:
                case SOUTH_BOTTOM:
                    p = new Point(col, row + 1);
                    break;
                case SOUTH_WEST_TOP:
                case SOUTH_WEST_RIGHT:
                    p = new Point(col - 1, row + 1);
                    break;
                case WEST_BOTTOM:
                case WEST_LEFT:
                case WEST_TOP:
                    p = new Point(col - 1, row);
                    break;
                case NORTH_WEST_BOTTOM:
                case NORTH_WEST_RIGHT:
                    p = new Point(col - 1, row - 1);
                    break;
            }
//            if (p != null)
//            {
//                if(p.x >= col) p.x = col - 1;
//                if(p.y >= row) p.y = row - 1;
//                if(p.x < 0) p.x = 0;
//                if(p.y < 0) p.y = 0;
//            }
            return p;
        }
    }

    private static final @Nonnull String ANSPOS_NORTH_LEFT = "north:left";
    private static final @Nonnull String ANSPOS_NORTH_TOP = "north:top";
    private static final @Nonnull String ANSPOS_NORTH_RIGHT = "north:right";

    private static final @Nonnull String ANSPOS_NORTH_EAST_LEFT = "north-east:left";
    private static final @Nonnull String ANSPOS_NORTH_EAST_BOTTOM = "north-east:bottom";

    private static final @Nonnull String ANSPOS_EAST_TOP = "east:top";
    private static final @Nonnull String ANSPOS_EAST_RIGHT = "east:right";
    private static final @Nonnull String ANSPOS_EAST_BOTTOM = "east:bottom";

    private static final @Nonnull String ANSPOS_SOUTH_EAST_LEFT = "south-east:left";
    private static final @Nonnull String ANSPOS_SOUTH_EAST_TOP = "south-east:top";

    private static final @Nonnull String ANSPOS_SOUTH_LEFT = "south:left";
    private static final @Nonnull String ANSPOS_SOUTH_RIGHT = "south:right";
    private static final @Nonnull String ANSPOS_SOUTH_BOTTOM = "south:bottom";

    private static final @Nonnull String ANSPOS_SOUTH_WEST_TOP = "south-west:top";
    private static final @Nonnull String ANSPOS_SOUTH_WEST_RIGHT = "south-west:right";

    private static final @Nonnull String ANSPOS_WEST_LEFT = "west:left";
    private static final @Nonnull String ANSPOS_WEST_TOP = "west:top";
    private static final @Nonnull String ANSPOS_WEST_BOTTOM = "west:bottom";

    private static final @Nonnull String ANSPOS_NORTH_WEST_RIGHT = "north-west:right";
    private static final @Nonnull String ANSPOS_NORTH_WEST_BOTTOM = "north-west:bottom";

}
