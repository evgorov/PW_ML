package com.ltst.prizeword.crossword.model;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IOnePuzzleModel
{
    void updateDataByDb(@Nonnull IListenerVoid handler);
    void updateDataByInternet(@Nonnull IListenerVoid handler);
    @Nullable Puzzle getPuzzle();
    void setQuestionAnswered(@Nonnull PuzzleQuestion q, boolean answered);
    void updatePuzzleUserData();
    void putAnsweredQuestionsToDb(@Nullable IListenerVoid handler);
    void close();
}
