package com.ltst.przwrd.db;

import com.ltst.przwrd.crossword.model.Puzzle;
import com.ltst.przwrd.crossword.model.PuzzleQuestion;
import com.ltst.przwrd.crossword.model.PuzzleSet;
import com.ltst.przwrd.login.model.UserData;
import com.ltst.przwrd.login.model.UserImage;
import com.ltst.przwrd.login.model.UserProvider;
import com.ltst.przwrd.manadges.PurchasePrizeWord;
import com.ltst.przwrd.news.News;
import com.ltst.przwrd.score.Coefficients;
import com.ltst.przwrd.score.ScoreQueue;

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

    @Nullable List<PuzzleSet> getPuzzleSetsByDate(int year, int month);

    @Nullable List<Puzzle> getSolvedPuzzlesBySetId(long id);

    @Nullable PuzzleSet getPuzzleSetById(long id);

    @Nullable Puzzle getPuzzleByServerId(@Nonnull String serverId);

    @Nullable List<Puzzle> getPuzzleListBySetId(long setId);

    @Nullable List<Puzzle> getPuzzles(List<String> serverIds);

    @Nullable List<Puzzle> getPuzzlesBySetId(long setId);

    @Nullable
    PurchasePrizeWord getPurchaseByGoogleId(@Nonnull String googleId);
    @Nullable ArrayList<PurchasePrizeWord> getPurchases();

    @Nullable List<PuzzleQuestion> getQuestionsByPuzzleId(long puzzleId);

    int getUserHintsCount();

    @Nullable Coefficients getCoefficients();

    @Nullable ScoreQueue getScoreQueue();

    @Nullable News getNews();
}
