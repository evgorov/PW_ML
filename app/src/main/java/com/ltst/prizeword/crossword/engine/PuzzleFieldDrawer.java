package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.view.animation.DecelerateInterpolator;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.view.PuzzleManager;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

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
    private @Nullable Rect mUnscaledPuzzleRect;
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
    private @Nullable List<PuzzleTileState> mInputTileList;
    private volatile int mInputAlpha = 0;
    private @Nullable IListenerVoid mPostInvalidateHandler;
    private boolean mIsAnimating = false;
    private boolean mAnimateBlink = true;
    private boolean mAnimateInput = false;

    public PuzzleFieldDrawer(@Nonnull Context context, @Nonnull PuzzleResourcesAdapter adapter,
                             @Nonnull IListener<Rect> invalidateHandler)
    {
        mContext = context;
        mAdapter = adapter;
        mInvalidateHandler = invalidateHandler;
        mBitmapManager = new BitmapManager(context, mAdapter.getBitmapResourceModel());
        mLetterBitmapManager = new LetterBitmapManager(mContext, mAdapter.getBitmapResourceModel());

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

    // ====== init/deinit =====================

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
        mUnscaledPuzzleRect = new Rect(mPuzzleRect);
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
        if (mPuzzleRect == null || mUnscaledPuzzleRect == null)
        {
            return;
        }

        int scaledWidth = (int)(mUnscaledPuzzleRect.width() * scaleWidth);
        int scaledHeight = (int)(mUnscaledPuzzleRect.height() * scaleHeight);
        mDrawingOffsetX = scaledWidth/2 - mUnscaledPuzzleRect.width()/2;
        mDrawingOffsetY = scaledHeight/2 - mUnscaledPuzzleRect.height()/2;

        mPuzzleRect.right = mUnscaledPuzzleRect.right + mDrawingOffsetX * 2;
        mPuzzleRect.bottom = mUnscaledPuzzleRect.bottom + mDrawingOffsetY * 2;
    }

    public void disableScaling()
    {
        if (mPuzzleRect == null || mUnscaledPuzzleRect == null)
        {
            return;
        }
        mPuzzleRect.set(mUnscaledPuzzleRect);
        mDrawingOffsetX = 0;
        mDrawingOffsetY = 0;
    }

    public void loadResources()
    {
        measureDimensions();
        measureText();

        // load background tiles
        mBackgroundResourcesLoader.load();

        // load basic puzzle tiles
        mPuzzleTilesResourcesLoader.load();

        // load arrows
        mArrowsResourcesLoader.load();

        // load letters
        mLetterResourcesLoader.load();
    }

    public void unloadResources()
    {
        mBitmapManager.recycle();
        mLetterBitmapManager.recycle();
    }

    // ====== public accessable data =====================================

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

    public int getTileWidth()
    {
        return mTileWidth;
    }

    public int getTileHeight()
    {
        return mTileHeight;
    }
    // ==== tools =================================================

    public boolean checkFocusPoint(@Nonnull Point p, @Nonnull Rect viewRect)
    {
        if (mPuzzleRect == null)
        {
            return false;
        }

        boolean offBorder = false;

        int halfWidth = viewRect.width()/2;
        int halfHeight = viewRect.height()/2;

        if(p.x - halfWidth < mPuzzleRect.left + mDrawingOffsetX)
        {
            p.x = halfWidth + mDrawingOffsetX;
            offBorder = true;
        }
        if(p.x + halfWidth > mPuzzleRect.right - mDrawingOffsetX)
        {
            p.x = mPuzzleRect.right - halfWidth - mDrawingOffsetX;
            offBorder = true;
        }
        if(p.y - halfHeight < mPuzzleRect.top + mDrawingOffsetY)
        {
            p.y = halfHeight + mDrawingOffsetY;
            offBorder = true;
        }
        if(p.y + halfHeight > mPuzzleRect.bottom - mDrawingOffsetY)
        {
            p.y = mPuzzleRect.bottom - halfHeight - mDrawingOffsetY;
            offBorder = true;
        }

        return offBorder;
    }

    public void convertPointFromScreenCoordsToTilesAreaCoords(@Nonnull PointF p)
    {
        if (mResources == null)
        {
            return;
        }

        int framePadding = mResources.getFramePadding(mContext.getResources());
        int padding = mResources.getPadding();

        float x = p.x - (mDrawingOffsetX + 4 * framePadding + 2 * padding);
        float y = p.y - (mDrawingOffsetY + 4 * framePadding + 2 * padding);
        p.set(x, y);
    }

    public void convertPointFromTilesAreaCoordsToScreenCoords(@Nonnull PointF p)
    {
        if (mResources == null)
        {
            return;
        }

        int framePadding = mResources.getFramePadding(mContext.getResources());
        int padding = mResources.getPadding();

        float x = p.x + (mDrawingOffsetX + 4 * framePadding + 2 * padding);
        float y = p.y + (mDrawingOffsetY + 4 * framePadding + 2 * padding);
        p.set(x, y);
    }

    public void createInputRectList(@Nullable List<PuzzleTileState> stateList, @Nonnull IListenerVoid postInvalidate)
    {
        if(stateList == null || stateList.isEmpty())
            return;
        mInputTileList = new ArrayList<PuzzleTileState>(stateList);
        mPostInvalidateHandler = postInvalidate;
    }

    public @Nullable Rect getPuzzleTileRect(int row, int column)
    {
        if (mResources == null)
        {
            return null;
        }
        int framePadding = mResources.getFramePadding(mContext.getResources());
        int padding = mResources.getPadding();
        int tileGap = mResources.getTileGap();
        int x = mDrawingOffsetX + 2 * framePadding + padding;
        int y = mDrawingOffsetY + 2 * framePadding + padding;
        int left = x + mTileWidth * (column - 1) + tileGap * (column - 1);
        int right = x + mTileWidth * column + tileGap * (column - 1);
        int top = y + mTileHeight * (row - 1)+ tileGap * (row - 1);
        int bottom = y + mTileHeight * row + tileGap * (row - 1);

        return new Rect(left, top, right, bottom);
    }

    // ====== drawing =====================================

    public void drawBackground(@Nonnull Canvas canvas)
    {
        if (mPuzzleRect == null || mResources == null || !mBackgroundResourcesLoader.isLoaded())
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
        if (mPuzzleRect == null || mResources == null || !mPuzzleTilesResourcesLoader.isLoaded())
        {
            return;
        }

        int cols = mResources.getPuzzleColumnsCount();
        int rows = mResources.getPuzzleRowsCount();
        @Nullable PuzzleTileState[][] stateMatrix = mResources.getStateMatrix();
        if (stateMatrix == null)
        {
            return;
        }

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
                    drawLetterByState(canvas, rect, state);
                    int letterState = state.getLetterState();
                    if(state.hasArrows && (letterState == PuzzleTileState.LetterState.LETTER_CORRECT ||
                                letterState == PuzzleTileState.LetterState.LETTER_EMPTY))
                    {
                        drawArrow(state.getFirstArrow(), canvas, rect);
                        drawArrow(state.getSecondArrow(), canvas, rect);
                    }
                }

                if (state.hasQuestion)
                {
                    List<PuzzleQuestion> questions = mResources.getPuzzleQuestions();
                    if (questions == null)
                    {
                        return;
                    }
                    @Nonnull String question = questions.get(questionsIndex).questionText;
                    drawQuestionByState(canvas, rect, question, state);
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

    private void drawQuestionByState(@Nonnull Canvas canvas, @Nonnull RectF rect,
                                     @Nonnull String question, @Nonnull PuzzleTileState state)
    {
        if (mResources == null)
        {
            return;
        }

        int questionRes = 0;
        switch (state.getQuestionState())
        {
            case PuzzleTileState.QuestionState.QUESTION_EMPTY:
                questionRes = PuzzleResources.getQuestionEmpty();
                break;
            case PuzzleTileState.QuestionState.QUESTION_INPUT:
                questionRes = PuzzleResources.getQuestionInput();
                break;
            case PuzzleTileState.QuestionState.QUESTION_WRONG:
                questionRes = PuzzleResources.getQuestionWrong();
                break;
            case PuzzleTileState.QuestionState.QUESTION_CORRECT:
                questionRes = mResources.getQuestionCorrect();
                break;
        }
        mBitmapManager.drawResource(questionRes, canvas, rect);
        drawQuestionText(canvas, question, rect);
    }

    private void drawLetterByState(@Nonnull Canvas canvas, @Nonnull RectF rect,
                                   @Nonnull PuzzleTileState state)
    {
        if (mResources == null)
            return;

        switch (state.getLetterState())
        {
            case PuzzleTileState.LetterState.LETTER_EMPTY:
            {
                int letterRes = PuzzleResources.getLetterEmpty();
                mBitmapManager.drawResource(letterRes, canvas, rect);
            }
                break;
            case PuzzleTileState.LetterState.LETTER_EMPTY_INPUT:
            {
                int letterRes = PuzzleResources.getLetterEmptyInput();
                mBitmapManager.drawResource(letterRes, canvas, rect);
            }
                break;
            case PuzzleTileState.LetterState.LETTER_INPUT:
            {
                int letterRes = PuzzleResources.getLetterTilesInput();
                char letter = state.getInputLetter().charAt(0);
                boolean knownSymbol = mLetterBitmapManager.drawLetter(letterRes, letter, canvas, rect);
                if(!knownSymbol)
                {
                    mLetterBitmapManager.drawLetter(letterRes, LetterBitmapManager.UNKNOWN_CHARACTER, canvas, rect);
                }
            }
                break;
            case PuzzleTileState.LetterState.LETTER_CORRECT:
            {
                int letterRes = mResources.getLetterTilesCorrect();
                char letter = state.getInputLetter().charAt(0);
                mLetterBitmapManager.drawLetter(letterRes, letter, canvas, rect);
            }
                break;
            case PuzzleTileState.LetterState.LETTER_WRONG:
            {
                int letterRes = PuzzleResources.getLetterTilesWrong();
                char letter = state.getInputLetter().charAt(0);
                mLetterBitmapManager.drawLetter(letterRes, letter, canvas, rect);
            }
            break;
        }

    }

    private void drawQuestionText(@Nonnull Canvas canvas, @Nonnull String question, @Nonnull RectF tileRect)
    {
        if (mResources == null)
        {
            return;
        }

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

    public void drawArrow(int arrow, final @Nonnull Canvas canvas, final @Nonnull RectF rect)
    {
        if (!mArrowsResourcesLoader.isLoaded())
            return;

        final int arrowResource = PuzzleResources.getArrowResource(arrow);
        if(arrowResource != PuzzleTileState.ArrowType.NO_ARROW)
        {
            if(mBitmapManager.hasResource(arrowResource))
            {
                mBitmapManager.drawResource(arrowResource, canvas, rect);
            }
        }
    }

    public void drawCurrentInputWithAnimation(final @Nonnull Canvas canvas)
    {

        if(mInputTileList != null)
        {
            final int saveCount = canvas.save();
            IListenerVoid finishAnimHandler = new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    canvas.restoreToCount(saveCount);
                    mInputTileList.clear();
                    mInputTileList = null;
                    mPostInvalidateHandler = null;
                    mIsAnimating = false;
                    mAnimateBlink = true;
                    mAnimateInput = false;
                }
            };

            for (PuzzleTileState state : mInputTileList)
            {
                Paint p1 = new Paint();
                p1.setAntiAlias(true);
                @Nullable Rect rect = getPuzzleTileRect(state.row, state.column);
                if (rect == null)
                {
                    break;
                }
                int letterRes = PuzzleResources.getLetterTilesInput();
                char letter = state.getInputLetter().charAt(0);
                RectF rectf = new RectF(rect);
                if(mAnimateBlink)
                {
                    p1.setShadowLayer(10, 5, 5, Color.BLACK);
                    p1.setColor(Color.WHITE);
                    p1.setAlpha(mInputAlpha);
                    canvas.drawRoundRect(rectf, 5.0f, 5.0f, p1);
                }
                else
                if(mAnimateInput)
                {
                    p1.setColor(Color.WHITE);
                    p1.setAlpha(255);
                    canvas.drawRoundRect(rectf, 5.0f, 5.0f, p1);

                    p1.setAlpha(mInputAlpha);
                    p1.setShadowLayer(10, 5, 5, Color.BLACK);
                    mLetterBitmapManager.drawLetter(letterRes, letter, canvas, rectf, p1);
                }
                else
                {
                    p1.setAlpha(mInputAlpha);
                    p1.setShadowLayer(10, 5, 5, Color.BLACK);
                    mLetterBitmapManager.drawLetter(letterRes, letter, canvas, rectf, p1);
                }
            }

            if(!mIsAnimating)
            {
                ScaleCorrectInputAnimationThread anim = new ScaleCorrectInputAnimationThread(finishAnimHandler);
                PuzzleManager.mExecutor.execute(anim);
            }
        }
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

    // =================== Resource loaders ===================

    private ResourcesLoader mBackgroundResourcesLoader = new ResourcesLoader()
    {
        @Override
        public void loadResource(final @Nonnull IListenerVoid loadingFinishedHandler)
        {
            mBitmapManager.addBitmap(PuzzleResources.getCanvasBackgroundTileRes(), null);
            mBitmapManager.addBitmap(PuzzleResources.getBackgroundTile(), new
                    IListenerVoid()
                    {
                        @Override
                        public void handle()
                        {
                            mInvalidateHandler.handle(mPuzzleRect);
                            loadingFinishedHandler.handle();
                        }
                    });
        }
    };

    private ResourcesLoader mPuzzleTilesResourcesLoader = new ResourcesLoader()
    {
        @Override
        public void loadResource(final @Nonnull IListenerVoid loadingFinishedHandler)
        {
            if (mResources == null)
            {
                return;
            }
            mBitmapManager.addBitmap(PuzzleResources.getLetterEmpty(), null);
            mBitmapManager.addBitmap(PuzzleResources.getLetterEmptyInput(), null);
            mBitmapManager.addBitmap(PuzzleResources.getQuestionInput(), null);
            mBitmapManager.addBitmap(mResources.getQuestionCorrect(), null);
            mBitmapManager.addBitmap(PuzzleResources.getQuestionWrong(), null);
            mBitmapManager.addBitmap(PuzzleResources.getQuestionEmpty(), new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    mInvalidateHandler.handle(mPuzzleRect);
                    loadingFinishedHandler.handle();
                }
            });
        }
    };

    private ResourcesLoader mArrowsResourcesLoader = new ResourcesLoader()
    {
        @Override
        public void loadResource(final @Nonnull IListenerVoid loadingFinishedHandler)
        {
            int[] arrowTypes = PuzzleTileState.ArrowType.getArrowTypesArray();
            for (int i = 0; i < arrowTypes.length - 1; i++)
            {
                int type = arrowTypes[i];
                int res = PuzzleResources.getArrowResource(type);
                mBitmapManager.addBitmap(res, null);
                type |= PuzzleTileState.ArrowType.ARROW_DONE;
                res = PuzzleResources.getArrowResource(type);
                mBitmapManager.addBitmap(res, null);
            }
            int type = arrowTypes[arrowTypes.length - 1];
            int lastRes = PuzzleResources.getArrowResource(type);
            mBitmapManager.addBitmap(lastRes, null);
            type |= PuzzleTileState.ArrowType.ARROW_DONE;
            lastRes = PuzzleResources.getArrowResource(type);
            mBitmapManager.addBitmap(lastRes, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    mInvalidateHandler.handle(mPuzzleRect);
                    loadingFinishedHandler.handle();
                }
            });
        }
    };

    private ResourcesLoader mLetterResourcesLoader = new ResourcesLoader()
    {
        @Override
        public void loadResource(final @Nonnull IListenerVoid loadingFinishedHandler)
        {
            if (mResources == null)
            {
                return;
            }
            mLetterBitmapManager.addTileResource(PuzzleResources.getLetterTilesInput(), mTileWidth, mTileHeight, null);
            mLetterBitmapManager.addTileResource(mResources.getLetterTilesCorrect(), mTileWidth, mTileHeight, null);
            mLetterBitmapManager.addTileResource(PuzzleResources.getLetterTilesWrong(), mTileWidth, mTileHeight, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    mInvalidateHandler.handle(mPuzzleRect);
                    loadingFinishedHandler.handle();
                }
            });
        }
    };

    private class ScaleCorrectInputAnimationThread extends Thread
    {
        private static final int SEC = 1000;
        private static final int DURATION = 1000;
        private static final int FPS = 30;
        private static final int ANIMATION_STEPS = 3;
        private static final int DURATION_INTERVAL = (int)((float)DURATION/ANIMATION_STEPS/(float)SEC * FPS);
        private static final long FPS_INTERVAL = SEC / FPS;
        final @Nonnull IListenerVoid finishHandler;
        final float interpolatorStep = 1.0f/(float)DURATION_INTERVAL;
        private int MAX_ALPHA = 255;

        private ScaleCorrectInputAnimationThread(@Nonnull IListenerVoid finishHandler)
        {
            this.finishHandler = finishHandler;
        }

        @Override
        public void run()
        {
            if (mPostInvalidateHandler == null || mInputTileList == null)
            {
                return;
            }
            float interpolator = 0;
            DecelerateInterpolator easeout = new DecelerateInterpolator(2.0f);

            MAX_ALPHA = 200;
            synchronized (this)
            {
                mIsAnimating = true;
                mAnimateBlink = true;
                while(true)
                {
                    interpolator += interpolatorStep;
                    float interpolationValue = easeout.getInterpolation(interpolator);

                    int currentAlpha = (int)(interpolationValue * MAX_ALPHA);
                    if(currentAlpha >= MAX_ALPHA || interpolator >= 1)
                        break;
                    mInputAlpha = currentAlpha;

                    mPostInvalidateHandler.handle();
                    try
                    {
                        ScaleCorrectInputAnimationThread.sleep(FPS_INTERVAL);
                    }
                    catch (InterruptedException e)
                    {
                        Log.e(e.getMessage());
                    }
                }
                mAnimateBlink = false;
                mAnimateInput = true;
                MAX_ALPHA = 255;
                interpolator = 0;
                while(true)
                {
                    interpolator += interpolatorStep;
                    float interpolationValue = easeout.getInterpolation(interpolator);

                    int currentAlpha = (int)(interpolationValue * MAX_ALPHA);
                    if(currentAlpha >= MAX_ALPHA || interpolator >= 1)
                        break;
                    mInputAlpha = currentAlpha;

                    mPostInvalidateHandler.handle();
                    try
                    {
                        ScaleCorrectInputAnimationThread.sleep(FPS_INTERVAL);
                    }
                    catch (InterruptedException e)
                    {
                        Log.e(e.getMessage());
                    }
                }
                mAnimateInput = false;
                interpolator = 0;
                while(true)
                {
                    interpolator += interpolatorStep;
                    float interpolationValue = easeout.getInterpolation(interpolator);
                    int currentAlpha = MAX_ALPHA - (int)(interpolationValue * MAX_ALPHA);
                    if(currentAlpha <= 0 || interpolator >= 1)
                    {
                        break;
                    }
                    mInputAlpha = currentAlpha;

                    mPostInvalidateHandler.handle();
                    try
                    {
                        ScaleCorrectInputAnimationThread.sleep(FPS_INTERVAL);
                    }
                    catch (InterruptedException e)
                    {
                        Log.e(e.getMessage());
                    }
                }

                finishHandler.handle();
            }
        }
    }
}

