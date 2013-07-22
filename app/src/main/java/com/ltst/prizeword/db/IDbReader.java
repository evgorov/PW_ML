package com.ltst.prizeword.db;

import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserProvider;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDbReader
{
    @Nullable UserData getUserById(long id);
    @Nullable UserData getUserByEmail(@Nonnull String email);
    @Nullable List<UserProvider> getUserProvidersByUserId(long id);

    @Nullable PuzzleSet getPuzzleSetById(long id);
    @Nullable PuzzleSet getPuzzleSetByServerId(@Nonnull String serverId);
}
