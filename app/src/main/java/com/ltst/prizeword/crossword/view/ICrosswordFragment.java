package com.ltst.prizeword.crossword.view;

import com.actionbarsherlock.R;
import com.ltst.prizeword.crossword.model.HintsModel;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public interface ICrosswordFragment {

    public void choicePuzzle(@Nonnull String setServerId, long puzzleId);
    public void purchaseResult(boolean result);
    HintsModel getHintsModel();
    IPuzzleSetModel getPuzzleSetModel();
    void waitLoader(boolean wait);
}
