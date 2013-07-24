package com.ltst.prizeword.crossword.model;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

public interface IOnePuzzleModel
{
    void updateDataByDb(@Nonnull IListenerVoid handler);
    void updateDataByInternet(@Nonnull IListenerVoid handler);
}
