package com.ltst.prizeword.scoredetail.model;

import android.graphics.Bitmap;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ISlowSource;

import javax.annotation.Nonnull;

public interface IScoreDataModel
{
    void updateDataByInternet(@Nonnull IListenerVoid handler);
    @Nonnull ISlowSource<ScoreFriendsData,Bitmap> getSource();
}
