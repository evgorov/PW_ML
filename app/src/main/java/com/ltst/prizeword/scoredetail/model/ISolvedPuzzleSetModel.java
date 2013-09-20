package com.ltst.prizeword.scoredetail.model;

import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;

import org.omich.velo.handlers.IListenerVoid;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 20.09.13.
 */
public interface ISolvedPuzzleSetModel {
    void updateDataByDb(@Nonnull IListenerVoid handler);
    void updateTotalDataByDb(@Nonnull IListenerVoid handler);
    void updateDataByInternet(@Nonnull IListenerVoid handler);
    void updateTotalDataByInternet(@Nonnull IListenerVoid handler);
    @Nonnull
    List<PuzzleSet> getPuzzleSets();
    @Nonnull
    HashMap<String, List<Puzzle>> getPuzzlesSet();
    void close();
}
