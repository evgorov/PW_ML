package com.ltst.prizeword.coefficients;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import javax.annotation.Nullable;

public interface ICoefficientsModel
{
    void updateFromDatabase();
    void updateFromInternet();
    @Nullable Coefficients getCoefficients();
    int getBaseScore(PuzzleSetModel.PuzzleSetType setType);
    int getBonusScore(int timeSpent, int timeGiven);
}
