package com.ltst.prizeword.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import javax.annotation.Nonnull;

public class SQLiteHelper extends SQLiteOpenHelper implements IDbCreator
{
    private static final String DATABASE_NAME = "app.db";
    private static final String TEST_DATABASE_NAME = "testapp.db";
    private static final int    DATABASE_VERSION = 1;

    private static final @Nonnull String TNAME_PUZZLE_SETS  = "puzzleSets";
    private static final @Nonnull String TNAME_PUZZLES      = "puzzles";

    private static final String CREATE_PUZZLE_SETS_QUERY = "create query "
            + TNAME_PUZZLE_SETS + "("
            + ColsPuzzleSets.ID             + " integer not null primary key autoincrement, "
            + ColsPuzzleSets.NAME           + " text not null,  "
            + ColsPuzzleSets.IS_BOUGHT      + " boolean not null default false, "
            + ColsPuzzleSets.TYPE           + " text not null, "
            + ColsPuzzleSets.SOLVED_COUNT   + " integer not null default 0, "
            + ColsPuzzleSets.TOTAL_COUNT    + " integer not null,  "
            + ColsPuzzleSets.PERCENT        + " integer not null default 0, "
            + ColsPuzzleSets.SCORE          + " integer not null default 0)";

    private static final String CREATE_PUZZLES_QUERY = "create query "
            + TNAME_PUZZLES + "("
            + ColsPuzzles.ID             + " integer not null primary key autoincrement, "
            + ColsPuzzles.SET_ID         + " integer not null,  "
            + ColsPuzzles.NAME           + " text not null, "
            + ColsPuzzles.ISSUED_AT      + " text not null, "
            + ColsPuzzles.PROGRESS       + " integer not null default 0,  "
            + ColsPuzzles.BASE_SCORE     + " integer not null, "
            + ColsPuzzles.TIME_GIVEN     + " integer not null, "
            + ColsPuzzles.TIME_LEFT      + " integer not null, "
            + ColsPuzzles.SCORE          + " integer not null default 0, "
            + ColsPuzzles.IS_SOLVED      + " boolean not null default false, "
            + " foreign key (" + ColsPuzzles.SET_ID + ") references "
            + TNAME_PUZZLE_SETS + " (" + ColsPuzzleSets.ID + ") on delete cascade)";

    private static final class ColsPuzzleSets
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String NAME            = "name";
        public static final @Nonnull String IS_BOUGHT       = "bought";
        public static final @Nonnull String TYPE            = "type";
        public static final @Nonnull String SOLVED_COUNT    = "solvedCount";
        public static final @Nonnull String TOTAL_COUNT     = "totalCount";
        public static final @Nonnull String PERCENT         = "percent";
        public static final @Nonnull String SCORE           = "score";
    }

    private static final class ColsPuzzles
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String SET_ID          = "setId";
        public static final @Nonnull String NAME            = "name";
        public static final @Nonnull String ISSUED_AT       = "issuedAt";
        public static final @Nonnull String PROGRESS        = "progress";
        public static final @Nonnull String BASE_SCORE      = "baseScore";
        public static final @Nonnull String TIME_GIVEN      = "timeGiven";
        public static final @Nonnull String TIME_LEFT       = "timeLeft";
        public static final @Nonnull String SCORE           = "score";
        public static final @Nonnull String IS_SOLVED       = "isSolved";
    }

    // ===============================================

    private final @Nonnull Context mContext;

    public SQLiteHelper(@Nonnull Context context, boolean inProduction)
    {
        super(context, inProduction ? DATABASE_NAME : TEST_DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public static void configureSQLiteDatabase(@Nonnull SQLiteDatabase db)
    {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Nonnull
    @Override
    public DbReader createDbReader() throws DbException
    {
        return new DbReader(this);
    }

    @Nonnull
    @Override
    public DbWriter createDbWriter() throws DbException
    {
        return new DbWriter(this);
    }

    @Nonnull
    public SQLiteDatabase createWritableSQLiteDatabase() throws DbException
    {
        SQLiteDatabase db2 = null;
        try
        {
            db2 = getWritableDatabase();
        }
        catch(SQLiteException e)
        {
            //Если ловим исключение, то db2 - null, тогда мы бросим другое исключение ниже.
        }

        if(db2 != null)
        {
            return db2;
        }

        throw new DbException(DbException.HELPER_CREATED_NULL_DATABASE);
    }

    @Nonnull
    public SQLiteDatabase createReadableSQLiteDatabase() throws DbException
    {
        SQLiteDatabase db = null;
        try
        {
            db = getReadableDatabase();
        }
        catch(SQLiteException e)
        {
            //Если ловим исключение, то db - null, тогда мы пытаемся создать db2.
        }

        if(db != null)
        {
            return db;
        }

        SQLiteDatabase db2 = null;
        try
        {
            db2 = getWritableDatabase();
        }
        catch(SQLiteException e)
        {
            //Если ловим исключение, то db2 - null, тогда мы бросим другое исключение ниже.
        }

        if(db2 != null)
        {
            return db2;
        }

        throw new DbException(DbException.HELPER_CREATED_NULL_DATABASE);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_PUZZLE_SETS_QUERY);
        db.execSQL(CREATE_PUZZLES_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
