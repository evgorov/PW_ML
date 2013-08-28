package com.ltst.prizeword.ScoreDetailFragment.model;

import android.graphics.Bitmap;

import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.invitefriends.model.InviteFriendsData;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ISlowSource;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

public interface IScoreDatalModel
{
    void updateDataByDb(@Nonnull IListenerVoid handler);
    void updateDataByInternet(@Nonnull IListenerVoid handler);
    @Nonnull List<PuzzleSet> getPuzzleSets();
    @Nonnull HashMap<String, List<Puzzle>> getPuzzlesSet();
    @Nonnull ISlowSource<ScoreFriendsData,Bitmap> getSource();
}
