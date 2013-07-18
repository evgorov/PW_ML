package com.ltst.prizeword.db;

import com.ltst.prizeword.login.model.UserData;

import javax.annotation.Nonnull;

public interface IDbWriter
{
    void putUser(@Nonnull UserData user);
}
