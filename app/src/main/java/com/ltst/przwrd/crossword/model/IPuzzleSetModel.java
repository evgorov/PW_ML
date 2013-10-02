package com.ltst.przwrd.crossword.model;

import org.omich.velo.handlers.IListenerVoid;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPuzzleSetModel
{
    void updateCurrentSets(@Nonnull IListenerVoid handler);
    void updateOneSet(@Nonnull String puzzleSetServerId, @Nonnull IListenerVoid handler);
    void updateTotalDataByDb(@Nonnull IListenerVoid handler);
    void updateDataByInternet(@Nonnull IListenerVoid handler);
    void updateTotalDataByInternet(@Nonnull IListenerVoid handler);
    void updateHints(@Nonnull IListenerVoid handler);
    @Nonnull List<PuzzleSet> getPuzzleSets();
    @Nonnull HashMap<String, List<Puzzle>> getPuzzlesSet();
    int getHintsCount();
    void synchronizePuzzleUserData();
    void updateSync(@Nonnull IListenerVoid handler);
    void buyCrosswordSet(@Nonnull String setServerId, @Nonnull String receiptData, @Nonnull String signature, @Nullable IListenerVoid handler);
    void loadOnePuzzleSetFromDB(@Nonnull String setServerId, @Nonnull IListenerVoid handler);
    PuzzleSet getOnePuzzleSet();
    boolean isAnswerState();
    void close();
}
