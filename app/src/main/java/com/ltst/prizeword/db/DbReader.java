package com.ltst.prizeword.db;

import android.database.sqlite.SQLiteDatabase;

import com.ltst.prizeword.login.model.UserData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DbReader implements IDbReader
{
    public final @Nonnull SQLiteDatabase mDb;

    public DbReader(@Nonnull SQLiteHelper helper) throws DbException
    {
        mDb = helper.createReadableSQLiteDatabase();
        SQLiteHelper.configureSQLiteDatabase(mDb);
    }

    protected @Nonnull IDbReader getReader()
    {
        return this;
    }

    @Nullable
    @Override
    public UserData getUserByEmail(@Nonnull String email)
    {

        return null;
    }
}
