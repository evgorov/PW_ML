package com.ltst.prizeword.score;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nullable;

public interface ICoefficientsModel
{
    void updateFromDatabase();
    void updateFromInternet(@Nullable IListenerVoid handler);
    @Nullable Coefficients getCoefficients();
    int getBaseScore(PuzzleSetModel.PuzzleSetType setType);
    int getBonusScore(int timeSpent, int timeGiven);
}
