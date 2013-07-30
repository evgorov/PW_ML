package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class PuzzleFieldDrawer
{
    private @Nonnull BitmapManager mBitmapManager;
    private @Nonnull Context mContext;
    private @Nonnull PuzzleResources mInfo;

    private @Nonnull Rect mPuzzleRect;
    private int mTileWidth;
    private int mTileHeight;
    private int mTileTextPadding;
    private @Nonnull NinePatchDrawable mFrameBorder;

    private int mDrawingOffsetX = 0;
    private int mDrawingOffsetY = 0;

    private int mFontSize;
    private int mTextHeight;
    private @Nonnull Paint mPaint;


    public PuzzleFieldDrawer(@Nonnull Context context, @Nonnull PuzzleResources info)
    {
        mContext = context;
        mInfo = info;
        mBitmapManager = new BitmapManager(context);
        mFrameBorder = (NinePatchDrawable) mContext.getResources().getDrawable(PuzzleResources.getBackgroundFrame());
        measureDimensions();
        measureText();
    }

    private void measureDimensions()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), PuzzleResources.getLetterEmpty(), options);
        mTileWidth = options.outWidth;
        mTileHeight = options.outHeight;

        int padding = mInfo.getPadding();
        int framePadding = mInfo.getFramePadding(mContext.getResources());
        int cellWidth = mInfo.getPuzzleColumnsCount();
        int cellHeight = mInfo.getPuzzleRowsCount();
        int tileGap = mInfo.getTileGap();
        int puzzleWidth = padding + 2 * framePadding + cellWidth * mTileWidth + (cellWidth - 1) * tileGap;
        int puzzleHeight = padding + 2 * framePadding + cellHeight * mTileHeight + (cellHeight - 1) * tileGap;
        mPuzzleRect = new Rect(0, 0, puzzleWidth, puzzleHeight);
    }

    private void measureText()
    {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mFontSize = mContext.getResources().getDimensionPixelSize(R.dimen.puzzle_question_font_size);
        mPaint.setTextSize(mFontSize);
        mPaint.setStyle(Paint.Style.FILL);
        Typeface tf = Typeface.create("Helvetica",Typeface.BOLD);
        mPaint.setTypeface(tf);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        mTextHeight = (int) mPaint.getTextSize();
        mTileTextPadding = mTileWidth/6;
    }

    public void enableScaling(float scaleWidth, float scaleHeight)
    {
        int scaledWidth = (int)(mPuzzleRect.width() * scaleWidth);
        int scaledHeight = (int)(mPuzzleRect.height() * scaleHeight);
        mDrawingOffsetX = scaledWidth/2 - mPuzzleRect.width()/2;
        mDrawingOffsetY = scaledHeight/2 - mPuzzleRect.height()/2;

        mPuzzleRect.right += mDrawingOffsetX * 2;
        mPuzzleRect.bottom += mDrawingOffsetY * 2;
    }

    public void loadResources()
    {
        mBitmapManager.addBitmap(PuzzleResources.getBackgroundTile());
        mBitmapManager.addBitmap(PuzzleResources.getCanvasBackgroundTileRes());
        mBitmapManager.addBitmap(PuzzleResources.getLetterEmpty());
        mBitmapManager.addBitmap(PuzzleResources.getQuestionEmpty());
    }

    public void drawBackground(@Nonnull Canvas canvas)
    {
        // draw view bg
        RectF bgRectF = new RectF(mPuzzleRect);
        fillRectWithBitmap(canvas, bgRectF, PuzzleResources.getCanvasBackgroundTileRes());

        // draw puzzle bg
        int padding = mInfo.getPadding();
        int framePadding = mInfo.getFramePadding(mContext.getResources());
        int puzzlePadding = padding + framePadding;
        RectF puzzleBackgroundRect = new RectF(puzzlePadding + mPuzzleRect.left + mDrawingOffsetX,
                puzzlePadding + mPuzzleRect.top + mDrawingOffsetY,
                mPuzzleRect.right - puzzlePadding - mDrawingOffsetX,
                mPuzzleRect.bottom - puzzlePadding - mDrawingOffsetY);
        fillRectWithBitmap(canvas, puzzleBackgroundRect, PuzzleResources.getBackgroundTile());

        // draw frame
        Rect frameRect = new Rect(padding + mDrawingOffsetX + mPuzzleRect.left,
                padding + mDrawingOffsetY + mPuzzleRect.top,
                mPuzzleRect.right - padding - mDrawingOffsetX,
                mPuzzleRect.bottom - padding - mDrawingOffsetY);
        mFrameBorder.setBounds(frameRect);
        mFrameBorder.draw(canvas);
    }

    public void drawPuzzles(@Nonnull Canvas canvas)
    {
        int cols = mInfo.getPuzzleColumnsCount() - 1;
        int rows = mInfo.getPuzzleRowsCount() - 1;
        int[][] stateMatrix = mInfo.getStateMatrix();
        int framePadding = mInfo.getFramePadding(mContext.getResources());
        int padding = mInfo.getPadding();
        int tileGap = mInfo.getTileGap();

        RectF rect = new RectF(mDrawingOffsetX + 2 * framePadding + padding,
                mDrawingOffsetY + 2 * framePadding + padding,
                mTileWidth + mDrawingOffsetX + 2 * framePadding + padding,
                mTileHeight + mDrawingOffsetY + 2 * framePadding + padding);

        int questionsIndex = 0;
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                int state = stateMatrix[j][i] & PuzzleResources.STATE_MASK;
                if (state == PuzzleResources.STATE_LETTER)
                {
                    mBitmapManager.drawResource(PuzzleResources.getLetterEmpty(), canvas, rect);
                    int arrow = stateMatrix[j][i] & PuzzleQuestion.ArrowType.ARROW_TYPE_MASK;
                    if(arrow != PuzzleQuestion.ArrowType.NO_ARROW)
                    {
//                        int res = PuzzleResources.getArrowResource(arrow);
//                        Bitmap arrowBitmap = mArrows.get(arrow);
//                        if(arrowBitmap == null)
//                        {
//                            arrowBitmap = BitmapFactory.decodeResource(mResources, res);
//                            mArrows.append(arrow, arrowBitmap);
//                        }
//                        canvas.drawBitmap(arrowBitmap, null, rect, mPaint);
                    }
                }

                if (state == PuzzleResources.STATE_QUESTION)
                {
                    mBitmapManager.drawResource(PuzzleResources.getQuestionEmpty(), canvas, rect);
                    drawQuestionText(canvas, questionsIndex, rect);
                    questionsIndex++;
                }

                rect.left += mTileWidth + tileGap;
                rect.right += mTileWidth + tileGap;
            }
            rect.left = mDrawingOffsetX + 2 * framePadding + padding;
            rect.right = mTileWidth + mDrawingOffsetX + 2 * framePadding + padding;
            rect.top += mTileHeight + tileGap;
            rect.bottom += mTileHeight + tileGap;
        }
    }

    private void drawQuestionText(@Nonnull Canvas canvas, int questionsIndex, @Nonnull RectF tileRect)
    {
        List<PuzzleQuestion> questions = mInfo.getPuzzleQuestions();
        if (questions == null)
        {
            return;
        }

        String question = questions.get(questionsIndex).questionText;
        RectF textRect = new RectF(tileRect.left + mTileTextPadding,
                tileRect.top + mTileTextPadding,
                tileRect.right - mTileTextPadding,
                tileRect.bottom - mTileTextPadding);
        int textWidth = (int)(textRect.right - textRect.left);
        int textHeight = (int)(textRect.bottom - textRect.top);
        List<String> filledText = fillTextInWidth(question, textWidth);
        int lineCount = filledText.size();
        int totalLineHeight = lineCount * mTextHeight;
        int startCoord = (textHeight - totalLineHeight)/2;
        int lineIndex = 0;
        for (String s : filledText)
        {
            canvas.drawText(s, textRect.left + textWidth/2,
                    textRect.top + startCoord + mTextHeight * (lineIndex + 1),
                    mPaint);
            lineIndex++;
        }
    }

    public void unloadResources()
    {
        mBitmapManager.recycle();
    }

    public int getWidth()
    {
        return mPuzzleRect.width();
    }

    public int getHeight()
    {
        return mPuzzleRect.height();
    }

    public int getActualWidth()
    {
        return mPuzzleRect.width() - mDrawingOffsetX * 2;
    }

    public int getActualHeight()
    {
        return mPuzzleRect.height() - mDrawingOffsetY * 2;
    }

    public int getCenterX()
    {
        return mPuzzleRect.width()/2;
    }

    public int getCenterY()
    {
        return mPuzzleRect.height()/2;
    }

    public void checkFocusPoint(@Nonnull Point p, @Nonnull Rect viewRect)
    {
        int halfWidth = viewRect.width()/2;
        int halfHeight = viewRect.height()/2;

        if(p.x - halfWidth < mPuzzleRect.left + mDrawingOffsetX)
            p.x = halfWidth + mDrawingOffsetX;
        if(p.x + halfWidth > mPuzzleRect.right - mDrawingOffsetX)
            p.x = mPuzzleRect.right - halfWidth - mDrawingOffsetX;
        if(p.y - halfHeight < mPuzzleRect.top + mDrawingOffsetY)
            p.y = halfHeight + mDrawingOffsetY;
        if(p.y + halfHeight > mPuzzleRect.bottom - mDrawingOffsetY)
            p.y = mPuzzleRect.bottom - halfHeight - mDrawingOffsetY;
    }

    private void fillRectWithBitmap(@Nonnull Canvas canvas, @Nonnull RectF rect, int res)
    {
        int tileWidth = mBitmapManager.getWidth(res);
        int tileHeight = mBitmapManager.getHeight(res);
        RectF tileRect = new RectF(rect.left, rect.top, tileWidth + rect.left, tileHeight + rect.top);

        while (tileRect.bottom < rect.bottom)
        {
            while (tileRect.right < rect.right)
            {
                mBitmapManager.drawResource(res, canvas, tileRect);
                tileRect.left += tileWidth;
                tileRect.right += tileWidth;
            }
            tileRect.right = rect.right;
            mBitmapManager.drawResource(res, canvas, tileRect);
            tileRect.left = rect.left;
            tileRect.right = tileWidth + rect.left;

            tileRect.top += tileHeight;
            tileRect.bottom += tileHeight;
        }

        tileRect.left = rect.left;
        tileRect.right = tileWidth + rect.left;
        tileRect.bottom = rect.bottom;
        while (tileRect.right < rect.right)
        {
            mBitmapManager.drawResource(res, canvas, tileRect);
            tileRect.left += tileWidth;
            tileRect.right += tileWidth;
        }
        tileRect.right = rect.right;
        mBitmapManager.drawResource(res, canvas, tileRect);
    }

    private @Nonnull List<String> fillTextInWidth(@Nonnull String text, int width)
    {
        List<String> strings = new ArrayList<String>();
        int start = 0;
        int end = text.length() - 1;
        while(start < end)
        {
            int measured = mPaint.breakText(text.substring(start, end), true, width, null);
            for (int i = measured; i >= start; i--)
            {
                char letter = text.charAt(i);
                if(letter == ' ')
                {
                    measured = i;
                    break;
                }
            }
            strings.add(text.substring(start, start + measured + 1));
            start += measured + 1;
        }
        return strings;
    }
}

