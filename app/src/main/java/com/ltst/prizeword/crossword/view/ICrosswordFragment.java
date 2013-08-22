package com.ltst.prizeword.crossword.view;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public interface ICrosswordFragment {

    public void buyCrosswordSet(String crosswordSetServerId);
    public void choicePuzzle(@Nonnull String setServerId, long puzzleId);
}
