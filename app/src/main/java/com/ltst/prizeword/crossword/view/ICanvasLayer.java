package com.ltst.prizeword.crossword.view;

import android.graphics.Canvas;
import android.graphics.Rect;

import javax.annotation.Nonnull;

public interface ICanvasLayer
{
    public void drawLayer(@Nonnull Canvas canvas, @Nonnull Rect viewport);
    public void recycle();
}
