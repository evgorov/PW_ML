package com.ltst.prizeword.db;

import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;

public class DbWriter implements IDbWriter
{
    public final @Nonnull SQLiteDatabase mDb;

    public DbWriter(@Nonnull SQLiteHelper helper) throws DbException
    {
        mDb = helper.createWritableSQLiteDatabase();
        SQLiteHelper.configureSQLiteDatabase(mDb);
    }
}
