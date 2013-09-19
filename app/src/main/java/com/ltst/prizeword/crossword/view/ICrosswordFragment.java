package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.crossword.model.HintsModel;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public interface ICrosswordFragment {

    public void choicePuzzle(@Nonnull String setServerId, long puzzleId);
    public void updateCurrentSet();
    public void updateOneSet(@Nonnull String puzzleSetServerId);
    HintsModel getHintsModel();
    IPuzzleSetModel getPuzzleSetModel();
}
