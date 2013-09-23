package com.ltst.prizeword.db;

import com.ltst.prizeword.manadges.Purchase;
import com.ltst.prizeword.score.Coefficients;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleTotalSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserProvider;
import com.ltst.prizeword.score.ScoreQueue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDbWriter extends IDbReader
{
    void putUser(@Nonnull UserData user, @Nullable List<UserProvider> providers);
    void putUserImage(@Nullable byte[] buffer);

    void putPuzzleSetList(@Nonnull List<PuzzleSet> list);
    void putPuzzleTotalSetList(@Nonnull List<PuzzleTotalSet> list);
    void putPuzzleSet(@Nonnull PuzzleSet set);

    void putPuzzle(@Nonnull Puzzle puzzle);

    void putFriendsImage(@Nonnull String url, @Nonnull byte[] bytes);
    void setQuestionAnswered(long questionId, boolean answered);
    void setQuestionAnswered(long[] questionId, boolean answered);

    void putCoefficients(@Nonnull Coefficients coefficients);
    void putScoreToQueue(@Nonnull ScoreQueue.Score score);
    void clearScoreQueue();
    void changeHintsCount(int hintsDelta);

    void putPurchase(@Nullable Purchase purchase);
    void putPurchases(@Nullable ArrayList<Purchase> purchases);

    void clearDb();
}
