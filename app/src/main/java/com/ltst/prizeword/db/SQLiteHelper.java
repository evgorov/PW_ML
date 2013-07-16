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

    private static final @Nonnull String TNAME_PUZZLES = "puzzles"

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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
