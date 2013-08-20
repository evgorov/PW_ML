package com.ltst.prizeword.coefficients;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import javax.annotation.Nullable;

public interface ICoefficientsModel
{
    void updateFromDatabase();
    void updateFromInternet();
    @Nullable Coefficients getCoefficients();
    int calculateScore(PuzzleSetModel.PuzzleSetType setType, int timeSpent, int timeGiven);
}
