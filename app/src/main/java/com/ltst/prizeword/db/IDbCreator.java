package com.ltst.prizeword.db;

import android.database.sqlite.SQLiteDatabase;

import javax.annotation.Nonnull;

public interface IDbCreator
{
    @Nonnull DbReader createDbReader () throws DbException;
    @Nonnull DbWriter createDbWriter () throws DbException;
}
