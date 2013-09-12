package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public interface ICrosswordFragment {

    public void choicePuzzle(@Nonnull String setServerId, long puzzleId);
}
