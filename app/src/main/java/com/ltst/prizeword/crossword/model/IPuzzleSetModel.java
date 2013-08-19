package com.ltst.prizeword.crossword.model;

import org.omich.velo.handlers.IListenerVoid;

import java.util.List;

import javax.annotation.Nonnull;

public interface IPuzzleSetModel
{
    void updateDataByDb(@Nonnull IListenerVoid handler);
    void updateDataByInternet(@Nonnull IListenerVoid handler);
    void updateTotalDataByInternet(@Nonnull IListenerVoid handler);
    @Nonnull List<PuzzleSet> getPuzzleSets();
    int getHintsCount();
}
