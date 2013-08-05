package com.ltst.prizeword.db;

import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserImage;
import com.ltst.prizeword.login.model.UserProvider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDbReader
{
    @Nullable UserData getUserById(long id);
//    @Nullable UserData getUserByEmail(@Nonnull String email);
    @Nullable public UserImage getUserImage(long user_id);
    @Nullable ArrayList<UserProvider> getUserProvidersByUserId(long id);

    @Nullable PuzzleSet getPuzzleSetByServerId(@Nonnull String serverId);
    @Nullable Puzzle getPuzzleById(long id);
    @Nullable List<PuzzleSet> getPuzzleSets();

    @Nullable PuzzleSet getPuzzleSetById(long id);
    @Nullable Puzzle getPuzzleByServerId(@Nonnull String serverId);
    @Nullable List<Puzzle> getPuzzleListBySetId(long setId);

    @Nullable List<PuzzleQuestion> getQuestionsByPuzzleId(long puzzleId);
}
