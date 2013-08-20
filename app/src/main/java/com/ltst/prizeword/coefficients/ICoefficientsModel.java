package com.ltst.prizeword.coefficients;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICoefficientsModel
{
    void updateFromDatabase(@Nonnull IListenerVoid handler);
    void updateFromInternet(@Nonnull IListenerVoid handler);
    @Nullable Coefficients getCoefficients();
}
