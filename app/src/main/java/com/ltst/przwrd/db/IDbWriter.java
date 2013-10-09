package com.ltst.przwrd.db;

import com.ltst.przwrd.crossword.model.Puzzle;
import com.ltst.przwrd.crossword.model.PuzzleSet;
import com.ltst.przwrd.crossword.model.PuzzleTotalSet;
import com.ltst.przwrd.login.model.UserData;
import com.ltst.przwrd.login.model.UserProvider;
import com.ltst.przwrd.manadges.Purchase;
import com.ltst.przwrd.news.News;
import com.ltst.przwrd.score.Coefficients;
import com.ltst.przwrd.score.ScoreQueue;

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

    void updateNews(@Nullable News news);
}
