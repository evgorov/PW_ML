package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 12.08.13.
 */
public interface ICrosswordFragment {

    public void choicePuzzle(@Nonnull String setServerId, long puzzleId);
    public void updateCurrentSet();
}
