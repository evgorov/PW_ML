package com.ltst.prizeword.db;

import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserImage;
import com.ltst.prizeword.login.model.UserProvider;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDbWriter extends IDbReader
{
    void putUser(@Nonnull UserData user, @Nullable List<UserProvider> providers);
    void putUserImage(@Nullable byte[] buffer);

    void putPuzzleSetList(@Nonnull List<PuzzleSet> list);
    void putPuzzleSet(@Nonnull PuzzleSet set);

    void putPuzzle(@Nonnull Puzzle puzzle);

    void setQuestionAnswered(long questionId, boolean answered);
}
