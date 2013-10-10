package com.ltst.przwrd.crossword.view;

import com.ltst.przwrd.crossword.model.HintsModel;
import com.ltst.przwrd.crossword.model.IPuzzleSetModel;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public interface ICrosswordFragment {

    public void choicePuzzle(@Nonnull String setServerId, @Nonnull String puzzleServerId);
    public void updateAllSets();
    public void updateCurrentSet();
    public void updateOneSet(@Nonnull String puzzleSetServerId);
    public void purchaseResult(boolean result);
    public void setHintCount(int hints);
    HintsModel getHintsModel();
    IPuzzleSetModel getPuzzleSetModel();
    void waitLoader(boolean wait);
}
