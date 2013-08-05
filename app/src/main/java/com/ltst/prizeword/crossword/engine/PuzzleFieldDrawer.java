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

import org.omich.velo.handlers.IListener;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleFieldDrawer
{
    private @Nonnull BitmapManager mBitmapManager;
    private @Nonnull LetterBitmapManager mLetterBitmapManager;
    private @Nonnull Context mContext;
    private @Nullable PuzzleResources mResources;
    private @Nonnull PuzzleResourcesAdapter mAdapter;

    private @Nullable Rect mPuzzleRect;
    private int mTileWidth;
    private int mTileHeight;
    private int mTileTextPadding;
    private @Nonnull NinePatchDrawable mFrameBorder;

    private int mDrawingOffsetX = 0;
    private int mDrawingOffsetY = 0;

    private int mFontSize;
    private int mTextHeight;
    private @Nonnull Paint mPaint;
    private IListener<Rect> mInvalidateHandler;


    public PuzzleFieldDrawer(@Nonnull Context context, @Nonnull PuzzleResourcesAdapter adapter,
                             @Nonnull IListener<Rect> invalidateHandler)
    {
        mContext = context;
        mAdapter = adapter;
        mInvalidateHandler = invalidateHandler;
        mBitmapManager = new BitmapManager(context, mAdapter.getBitmapResourceModel());
        mLetterBitmapManager = new LetterBitmapManager(mContext);

        mFrameBorder = (NinePatchDrawable) mContext.getResources().getDrawable(PuzzleResources.getBackgroundFrame());

        mAdapter.addResourcesUpdater(new IListener<PuzzleResources>()
        {
            @Override
            public void handle(@Nullable PuzzleResources puzzleResources)
            {
                if (puzzleResources != null)
                {
                    mResources = puzzleResources;
                    loadResources();
                }
            }
        });
    }

    private void measureDimensions()
    {
        if (mResources == null)
        {
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), PuzzleResources.getLetterEmpty(), options);
        mTileWidth = options.outWidth;
        mTileHeight = options.outHeight;

        int padding = mResources.getPadding();
        int framePadding = mResources.getFramePadding(mContext.getResources());
        int cellWidth = mResources.getPuzzleColumnsCount();
        int cellHeight = mResources.getPuzzleRowsCount();
        int tileGap = mResources.getTileGap();
        int puzzleWidth = 2 * (padding + 2 * framePadding) + cellWidth * mTileWidth + (cellWidth - 1) * tileGap;
        int puzzleHeight = 2 * (padding + 2 * framePadding) + cellHeight * mTileHeight + (cellHeight - 1) * tileGap;
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
        if (mPuzzleRect == null)
        {
            return;
        }

        int scaledWidth = (int)(mPuzzleRect.width() * scaleWidth);
        int scaledHeight = (int)(mPuzzleRect.height() * scaleHeight);
        mDrawingOffsetX = scaledWidth/2 - mPuzzleRect.width()/2;
        mDrawingOffsetY = scaledHeight/2 - mPuzzleRect.height()/2;

        mPuzzleRect.right += mDrawingOffsetX * 2;
        mPuzzleRect.bottom += mDrawingOffsetY * 2;
    }

    public void loadResources()
    {
        measureDimensions();
        measureText();

        mBitmapManager.addBitmap(PuzzleResources.getCanvasBackgroundTileRes(), mInvalidateHandler, mPuzzleRect);
        mBitmapManager.addBitmap(PuzzleResources.getBackgroundTile(), mInvalidateHandler, mPuzzleRect);
        mBitmapManager.addBitmap(PuzzleResources.getLetterEmpty(), mInvalidateHandler, mPuzzleRect);
        mBitmapManager.addBitmap(PuzzleResources.getQuestionEmpty(), mInvalidateHandler, mPuzzleRect);

//        mLetterBitmapManager.addTileResource(mResources.getLetterTilesCorrect(), mTileWidth, mTileHeight);
//        mLetterBitmapManager.addTileResource(PuzzleResources.getLetterTilesWrong(), mTileWidth, mTileHeight);
    }

    public void drawBackground(@Nonnull Canvas canvas)
    {
        if (mPuzzleRect == null || mResources == null)
        {
            return;
        }
        // draw view bg
        RectF bgRectF = new RectF(mPuzzleRect);
        fillRectWithBitmap(canvas, bgRectF, PuzzleResources.getCanvasBackgroundTileRes());

        // draw puzzle bg
        int padding = mResources.getPadding();
        int framePadding = mResources.getFramePadding(mContext.getResources());
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
        if (mPuzzleRect == null || mResources == null)
        {
            return;
        }

        int cols = mResources.getPuzzleColumnsCount();
        int rows = mResources.getPuzzleRowsCount();
        PuzzleTileState[][] stateMatrix = mResources.getStateMatrix();
        int framePadding = mResources.getFramePadding(mContext.getResources());
        int padding = mResources.getPadding();
        int tileGap = mResources.getTileGap();

        RectF rect = new RectF(mDrawingOffsetX + 2 * framePadding + padding,
                mDrawingOffsetY + 2 * framePadding + padding,
                mTileWidth + mDrawingOffsetX + 2 * framePadding + padding,
                mTileHeight + mDrawingOffsetY + 2 * framePadding + padding);

        int questionsIndex = 0;
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                PuzzleTileState state = stateMatrix[j][i];
                if (state.hasLetter)
                {
                    mBitmapManager.drawResource(PuzzleResources.getLetterEmpty(), canvas, rect);
                    if(state.hasArrows)
                    {
                        int arrowResource = PuzzleResources.getArrowResource(state.getFirstArrow());
                        if(arrowResource != PuzzleTileState.ArrowType.NO_ARROW)
                        {
                            if(!mBitmapManager.hasResource(arrowResource))
                            {
                                mBitmapManager.addBitmap(arrowResource, mInvalidateHandler, mPuzzleRect);
                            }
                            mBitmapManager.drawResource(arrowResource, canvas, rect);
                        }
                        arrowResource = PuzzleResources.getArrowResource(state.getSecondArrow());
                        if(arrowResource != PuzzleTileState.ArrowType.NO_ARROW)
                        {
                            if(!mBitmapManager.hasResource(arrowResource))
                            {
                                mBitmapManager.addBitmap(arrowResource, mInvalidateHandler, mPuzzleRect);
                            }
                            mBitmapManager.drawResource(arrowResource, canvas, rect);
                        }
                    }
                }

                if (state.hasQuestion)
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
        if (mResources == null)
        {
            return;
        }

        List<PuzzleQuestion> questions = mResources.getPuzzleQuestions();
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
        if (mPuzzleRect == null)
        {
            return 0;
        }
        return mPuzzleRect.width();
    }

    public int getHeight()
    {
        if (mPuzzleRect == null)
        {
            return 0;
        }
        return mPuzzleRect.height();
    }

    public int getActualWidth()
    {
        if (mPuzzleRect == null)
        {
            return 0;
        }
        return mPuzzleRect.width() - mDrawingOffsetX * 2;
    }

    public int getActualHeight()
    {
        if (mPuzzleRect == null)
        {
            return 0;
        }
        return mPuzzleRect.height() - mDrawingOffsetY * 2;
    }

    public int getCenterX()
    {
        if (mPuzzleRect == null)
        {
            return 0;
        }
        return mPuzzleRect.width()/2;
    }

    public int getCenterY()
    {
        if (mPuzzleRect == null)
        {
            return 0;
        }
        return mPuzzleRect.height()/2;
    }

    public void checkFocusPoint(@Nonnull Point p, @Nonnull Rect viewRect)
    {
        if (mPuzzleRect == null)
        {
            return;
        }

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
