package com.ltst.prizeword.db;

import com.ltst.prizeword.manadges.Purchase;
import com.ltst.prizeword.score.Coefficients;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserImage;
import com.ltst.prizeword.login.model.UserProvider;
import com.ltst.prizeword.score.ScoreQueue;

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
    @Nullable List<Puzzle> getPuzzles(List<String> serverIds);
    @Nullable List<Puzzle> getPuzzlesBySetId(long setId);

    @Nullable Purchase getPurchaseByGoogleId(@Nonnull String googleId);
    @Nullable ArrayList<Purchase> getPurchases();

    @Nullable List<PuzzleQuestion> getQuestionsByPuzzleId(long puzzleId);
    int getUserHintsCount();

    @Nullable Coefficients getCoefficients();
    @Nullable ScoreQueue getScoreQueue();
}
