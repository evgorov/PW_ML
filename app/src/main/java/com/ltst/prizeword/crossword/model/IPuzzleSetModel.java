package com.ltst.prizeword.crossword.model;

import org.omich.velo.handlers.IListenerVoid;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPuzzleSetModel
{
    void updateDataByDb(@Nonnull IListenerVoid handler);
    void updateCurrentSets(@Nonnull IListenerVoid handler);
    void updateTotalDataByDb(@Nonnull IListenerVoid handler);
    void updateDataByInternet(@Nonnull IListenerVoid handler);
    void updateTotalDataByInternet(@Nonnull IListenerVoid handler);
    @Nonnull List<PuzzleSet> getPuzzleSets();
    @Nonnull HashMap<String, List<Puzzle>> getPuzzlesSet();
    int getHintsCount();
    public void synchronizePuzzleUserData();
    void buyCrosswordSet(@Nonnull String setServerId, @Nonnull String receiptData, @Nonnull String signature, @Nullable IListenerVoid handler);
    void close();
}
