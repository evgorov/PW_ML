package com.ltst.przwrd.db;

import javax.annotation.Nonnull;

public interface IDbCreator
{
    @Nonnull DbReader createDbReader () throws DbException;
    @Nonnull DbWriter createDbWriter () throws DbException;
}
