package com.ltst.prizeword.crossword.view;

import android.graphics.Canvas;

public interface ICanvasLayer
{
    public void drawLayer(Canvas canvas);
    public void recycle();
}
