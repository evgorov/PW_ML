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

    public static final @Nonnull String TNAME_PUZZLE_SETS  = "puzzleSets";
    public static final @Nonnull String TNAME_PUZZLES      = "puzzles";
    public static final @Nonnull String TNAME_PUZZLE_QUESTIONS  = "puzzleQuestions";

    public static final @Nonnull String TNAME_IMAGES            = "images";
    public static final @Nonnull String TNAME_PROVIDERS         = "providers";
    public static final @Nonnull String TNAME_USERS             = "users";

    private static final String CREATE_PUZZLE_SETS_QUERY = "create table "
            + TNAME_PUZZLE_SETS + "("
            + ColsPuzzleSets.ID             + " integer not null primary key autoincrement, "
            + ColsPuzzleSets.SERVER_ID      + " text not null unique,  "
            + ColsPuzzleSets.NAME           + " text not null,  "
            + ColsPuzzleSets.IS_BOUGHT      + " boolean not null default false, "
            + ColsPuzzleSets.TYPE           + " text not null, "
            + ColsPuzzleSets.MONTH          + " integer not null default 0, "
            + ColsPuzzleSets.YEAR           + " integer not null default 0, "
            + ColsPuzzleSets.CREATED_AT     + " text not null, "
            + ColsPuzzleSets.IS_PUBLISHED   + " boolean not null default false, "
            + ColsPuzzleSets.PUZZLES_SERVER_IDS  + " text not null)";

    private static final String CREATE_PUZZLES_QUERY = "create table "
            + TNAME_PUZZLES + "("
            + ColsPuzzles.ID             + " integer not null primary key autoincrement, "
            + ColsPuzzles.SET_ID         + " integer not null,  "
            + ColsPuzzles.SERVER_ID      + " text not null unique, "
            + ColsPuzzles.NAME           + " text not null, "
            + ColsPuzzles.ISSUED_AT      + " text not null, "
            + ColsPuzzles.BASE_SCORE     + " integer not null, "
            + ColsPuzzles.TIME_GIVEN     + " integer not null, "
            + ColsPuzzles.TIME_LEFT      + " integer not null, "
            + ColsPuzzles.SCORE          + " integer not null default 0, "
            + ColsPuzzles.IS_SOLVED      + " boolean not null default false, "
            + " foreign key (" + ColsPuzzles.SET_ID + ") references "
            + TNAME_PUZZLE_SETS + " (" + ColsPuzzleSets.ID + ") on delete cascade)";

    private static final String CREATE_PUZZLE_QUESTIONS_QUERY = "create table "
            + TNAME_PUZZLE_QUESTIONS + "("
            + ColsPuzzleQuestions.ID                + " integer not null primary key autoincrement, "
            + ColsPuzzleQuestions.PUZZLE_ID + " integer not null, "
            + ColsPuzzleQuestions.COLUMN            + " integer not null, "
            + ColsPuzzleQuestions.ROW               + " integer not null, "
            + ColsPuzzleQuestions.QUESTION_TEXT     + " text not null, "
            + ColsPuzzleQuestions.ANSWER            + " text not null, "
            + ColsPuzzleQuestions.ANSWER_POSITION   + " text not null, "
            + " foreign key (" + ColsPuzzleQuestions.PUZZLE_ID + ") references "
            + TNAME_PUZZLES + " (" + ColsPuzzles.ID + ") on delete cascade)";

    private static final String CREATE_IMAGES_QUERY = "create table "
            + TNAME_IMAGES + "("
            + ColsImages.ID                + " integer not null primary key autoincrement, "
            + ColsImages.KEY               + " text not null unique, "
            + ColsImages.IMAGE             + " blob)";

    private static final String CREATE_USERS_QUERY = "create table "
            + TNAME_USERS + "("
            + ColsUsers.ID                  + " integer not null primary key autoincrement, "
            + ColsUsers.NAME                + " text not null, "
            + ColsUsers.SURNAME             + " text not null, "
            + ColsUsers.EMAIL               + " text not null unique, "
            + ColsUsers.BIRTHDATE           + " text not null default \"\", "
            + ColsUsers.CITY                + " text not null, "
            + ColsUsers.SOLVED              + " integer not null default 0, "
            + ColsUsers.POSITION            + " integer not null default 0, "
            + ColsUsers.MONTH_SCORE         + " integer not null default 0, "
            + ColsUsers.HIGH_SCORE          + " integer not null default 0, "
            + ColsUsers.DYNAMICS            + " integer not null default 0, "
            + ColsUsers.HINTS               + " integer not null default 0, "
            + ColsUsers.PREVIEW_URL	        + " text not null, "
            + ColsUsers.PREVIEW_KEY	        + " text not null)";

    private static final String CREATE_PROVIDERS_QUERY = "create table "
            + TNAME_PROVIDERS + "("
            + ColsProviders.ID              + " integer not null primary key autoincrement, "
            + ColsProviders.NAME	        + " text not null, "
            + ColsProviders.USER_ID	        + " integer not null, "
            + ColsProviders.PROVIDER_ID	    + " text not null, "
            + ColsProviders.TOKEN	        + " text not null,"
            + " foreign key (" + ColsProviders.USER_ID + ") references "
            + TNAME_USERS + " (" + ColsUsers.ID + ") on delete cascade)";

    public static final class ColsPuzzleSets
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String SERVER_ID       = "serverId";
        public static final @Nonnull String NAME            = "name";
        public static final @Nonnull String IS_BOUGHT       = "bought";
        public static final @Nonnull String TYPE            = "type";
        public static final @Nonnull String MONTH           = "month";
        public static final @Nonnull String YEAR            = "year";
        public static final @Nonnull String CREATED_AT      = "createdAt";
        public static final @Nonnull String IS_PUBLISHED       = "published";
        public static final @Nonnull String PUZZLES_SERVER_IDS  = "puzzlesServerIds";
    }

    public static final class ColsPuzzles
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String SET_ID          = "setId";
        public static final @Nonnull String SERVER_ID       = "serverId";
        public static final @Nonnull String NAME            = "name";
        public static final @Nonnull String ISSUED_AT       = "issuedAt";
        public static final @Nonnull String BASE_SCORE      = "baseScore";
        public static final @Nonnull String TIME_GIVEN      = "timeGiven";
        public static final @Nonnull String TIME_LEFT       = "timeLeft";
        public static final @Nonnull String SCORE           = "score";
        public static final @Nonnull String IS_SOLVED       = "isSolved";
    }

    public static final class ColsPuzzleData
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String PUZZLE_ID       = "puzzleId";
        public static final @Nonnull String SET_ID          = "setId";
        public static final @Nonnull String NAME            = "name";
        public static final @Nonnull String WIDTH           = "width";
        public static final @Nonnull String HEIGHT          = "height";
    }

    public static final class ColsPuzzleQuestions
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String PUZZLE_ID = "puzzleDataId";
        public static final @Nonnull String COLUMN          = "column";
        public static final @Nonnull String ROW             = "row";
        public static final @Nonnull String QUESTION_TEXT   = "questionText";
        public static final @Nonnull String ANSWER          = "answer";
        public static final @Nonnull String ANSWER_POSITION = "answerPosition";
    }

    public static final class ColsUsers
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String NAME            = "name";
        public static final @Nonnull String SURNAME         = "surname";
        public static final @Nonnull String EMAIL           = "email";
        public static final @Nonnull String BIRTHDATE       = "birthdate";
        public static final @Nonnull String CITY            = "city";
        public static final @Nonnull String SOLVED          = "solved";
        public static final @Nonnull String POSITION        = "position";
        public static final @Nonnull String MONTH_SCORE     = "monthScore";
        public static final @Nonnull String HIGH_SCORE      = "highScore";
        public static final @Nonnull String DYNAMICS        = "dynamics";
        public static final @Nonnull String HINTS           = "hints";
        public static final @Nonnull String PREVIEW_URL     = "previewUrl";
        public static final @Nonnull String PREVIEW_KEY     = "previewKey";
    }

    public static final class ColsProviders
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String NAME            = "provider_name";
        public static final @Nonnull String PROVIDER_ID     = "provider_id";
        public static final @Nonnull String TOKEN           = "provider_token";
        public static final @Nonnull String USER_ID         = "user_id";
    }

    public static final class ColsImages
    {
        public static final @Nonnull String ID              = "_id";
        public static final @Nonnull String KEY             = "key";
        public static final @Nonnull String IMAGE           = "image";
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
        return new DbReader(this, false);
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
        db.execSQL(CREATE_USERS_QUERY);
        db.execSQL(CREATE_PROVIDERS_QUERY);
        db.execSQL(CREATE_IMAGES_QUERY);
        db.execSQL(CREATE_PUZZLE_SETS_QUERY);
        db.execSQL(CREATE_PUZZLES_QUERY);
        db.execSQL(CREATE_PUZZLE_QUESTIONS_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
