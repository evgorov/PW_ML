package com.ltst.prizeword.db;

import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;

public class DbReader
{
    public final @Nonnull SQLiteDatabase mDb;

    public DbReader(@Nonnull SQLiteHelper helper) throws DbException
    {
        mDb = helper.createReadableSQLiteDatabase();
        SQLiteHelper.configureSQLiteDatabase(mDb);
    }
}
