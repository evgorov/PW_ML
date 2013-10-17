package com.ltst.przwrd.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.ltst.przwrd.crossword.model.Puzzle;
import com.ltst.przwrd.crossword.model.PuzzleQuestion;
import com.ltst.przwrd.crossword.model.PuzzleSet;
import com.ltst.przwrd.crossword.model.PuzzleTotalSet;
import com.ltst.przwrd.login.model.UserData;
import com.ltst.przwrd.login.model.UserImage;
import com.ltst.przwrd.login.model.UserProvider;
import com.ltst.przwrd.manadges.PurchasePrizeWord;
import com.ltst.przwrd.navigation.NavigationActivity;
import com.ltst.przwrd.news.News;
import com.ltst.przwrd.score.Coefficients;
import com.ltst.przwrd.score.ScoreQueue;

import org.omich.velo.db.DbHelper;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.ltst.przwrd.db.SQLiteHelper.ColsCoefficients;
import static com.ltst.przwrd.db.SQLiteHelper.ColsImages;
import static com.ltst.przwrd.db.SQLiteHelper.ColsProviders;
import static com.ltst.przwrd.db.SQLiteHelper.ColsPurchases;
import static com.ltst.przwrd.db.SQLiteHelper.ColsPuzzleQuestions;
import static com.ltst.przwrd.db.SQLiteHelper.ColsPuzzleSets;
import static com.ltst.przwrd.db.SQLiteHelper.ColsPuzzles;
import static com.ltst.przwrd.db.SQLiteHelper.ColsScoreQueue;
import static com.ltst.przwrd.db.SQLiteHelper.ColsUsers;
import static com.ltst.przwrd.db.SQLiteHelper.ID_USER;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_COEFFICIENTS;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_IMAGES;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_NEWS;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_POST_SCORE_QUEUE;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_PROVIDERS;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_PURCHASES;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_PUZZLES;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_PUZZLE_QUESTIONS;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_PUZZLE_SETS;
import static com.ltst.przwrd.db.SQLiteHelper.TNAME_USERS;

public class DbWriter extends  DbReader implements IDbWriter
{
    public final @Nonnull SQLiteDatabase mDb;

    public DbWriter(@Nonnull SQLiteHelper helper) throws DbException
    {
        super(helper, true);
        mDb = getDb();
    }

    @Override
    public void putUser(@Nonnull UserData user, @Nullable List<UserProvider> providers)
    {
        @Nullable UserData exitingUser = getUserById(SQLiteHelper.ID_USER);
        if(exitingUser == null)
        {
            putNewUser(user, providers);
        }
        else
            updateExistingUser(exitingUser.id, user, providers);
    }

    private void putNewUser(@Nonnull UserData user, @Nullable List<UserProvider> providers)
    {
        final ContentValues cvUser = mUserDataContentValuesCreator.createObjectContentValues(user);
        final @Nullable List<ContentValues> cvProviders = createContentValuesList(providers, mUserProviderContentValuesCreator);

        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                long created_id = mDb.insert(TNAME_USERS, null, cvUser);

                if (cvProviders != null && created_id != -1)
                {
                    for (Iterator<ContentValues> iterator = cvProviders.iterator(); iterator.hasNext(); )
                    {
                        ContentValues cvProv = iterator.next();
                        cvProv.put(ColsProviders.USER_ID, created_id);
                        mDb.insert(TNAME_PROVIDERS, null, cvProv);
                    }
                }
            }
        });

    }

    @Override
    public void putUserImage(@Nullable byte[] buffer)
    {
        @Nullable UserData existingUser = getUserById(ID_USER);
        if (existingUser == null)
        {
            return;
        }

        @Nonnull UserImage userImage = new UserImage(-1, existingUser.previewUrl, buffer);
        final ContentValues cv = mUserPicContentValuesCreator.createObjectContentValues(userImage);
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.insert(TNAME_IMAGES, null, cv);
            }
        });
    }

    private void updateExistingUser(final long id, @Nonnull UserData user, @Nullable List<UserProvider> providers)
    {
        @Nullable UserData existingUser = getUserById(id);
        @Nullable List<UserProvider> existingProviders = getUserProvidersByUserId(id);
        if (existingUser == null)
        {
            return;
        }
        final ContentValues cvUser = mUserDataContentValuesCreator.createObjectContentValues(user);
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.update(TNAME_USERS, cvUser, ColsUsers.ID + "=" + id, null);
            }
        });

        // Удаляем все провайдеры текущего пользователя;
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.delete(TNAME_PROVIDERS, ColsProviders.USER_ID + "=" + id, null);
            }
        });

        // Доавляем новые провайдеры текущему пользователю;
        for(@Nullable UserProvider provider: providers){
            if(provider == null) continue;
            provider.userId = id;
            final @Nullable UserProvider prov = provider;
            final ContentValues cvProviders = mUserProviderContentValuesCreator.createObjectContentValues(prov);
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
//                    mDb.update(TNAME_PROVIDERS, cvProviders, ColsProviders.USER_ID + "=" + id +" AND " + ColsProviders.NAME + "=" + prov.name, null);
                    mDb.insert(TNAME_PROVIDERS, null, cvProviders);
                }
            });
        }
    }

    @Override
    public void putPuzzleSet(@Nonnull PuzzleSet set)
    {
        final ContentValues values = mPuzzleSetContentValuesCreator.createObjectContentValues(set);
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                long id = mDb.insert(TNAME_PUZZLE_SETS, null, values);
                Log.i("Successfully inserted PUZZLE_SET with id: " + id);
            }
        });
    }

    @Override
    public void putPuzzle(@Nonnull Puzzle puzzle)
    {
        @Nonnull List<PuzzleQuestion> puzzleQuestions = new ArrayList<PuzzleQuestion>(puzzle.questions.size());
        for(PuzzleQuestion pq : puzzle.questions)
        {
            pq.puzzleId = puzzle.id;
            puzzleQuestions.add(pq);
        }

        final @Nullable Puzzle existingPuzzle = getPuzzleByServerId(puzzle.serverId);
        final ContentValues values = mPuzzleContentValuesCreator.createObjectContentValues(puzzle);
        final List<ContentValues> questionCv = createContentValuesList(puzzleQuestions, mPuzzleQuestionContentValuesCreator);

        if (existingPuzzle == null) // новый кроссворд
        {
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    long id = mDb.insert(TNAME_PUZZLES, null, values);
                    if(id != -1)
                        for (ContentValues questionValues : questionCv)
                        {
                            questionValues.put(ColsPuzzleQuestions.PUZZLE_ID, id);
                            long row2 = mDb.insert(TNAME_PUZZLE_QUESTIONS, null, questionValues);
                            int k = 1;
                        }
                }
            });
        }
        else // нужно обновить старые кроссворды и вопросы к ним (для синхронизации правильных ответов)
        {
            final @Nullable List<PuzzleQuestion> existingQuestions = puzzle.questions;
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    int rows = mDb.update(TNAME_PUZZLES, values, ColsPuzzles.ID + "=" + existingPuzzle.id, null);
                    if (existingQuestions == null)
                    {
                        return;
                    }

//                    NavigationActivity.debug("-------------------------------------------------------");
//                    NavigationActivity.debug("puzzleId = " + existingPuzzle.id);
//                    List<PuzzleQuestion> questions = getQuestionsByPuzzleId(existingPuzzle.id);
//                    for(PuzzleQuestion question: questions)
//                    {
//                        NavigationActivity.debug(
//                                "id = "+question.id
//                                        +" column="+question.column+" row="+question.row
//                                        +" answerPosition="+question.answerPosition+" isAnswered="+question.isAnswered
//                                        +" questionText="+question.questionText+" answer="+question.answer
//                        );
//                    }
//                    NavigationActivity.debug("-------------------------------------------------------");

                    int questionIndex = 0;
                    for (ContentValues contentValues : questionCv)
                    {
                        @Nullable PuzzleQuestion existingQuestion = existingQuestions.get(questionIndex);
                        if (existingQuestion == null)
                            continue;

                        contentValues.put(ColsPuzzleQuestions.PUZZLE_ID, existingPuzzle.id);
                        int rows2 = mDb.update(TNAME_PUZZLE_QUESTIONS, contentValues,
                                ColsPuzzleQuestions.PUZZLE_ID   + "=" + existingPuzzle.id + " AND " +
                                ColsPuzzleQuestions.COLUMN      + "=" + existingQuestion.column + " AND " +
                                ColsPuzzleQuestions.ROW         + "=" + existingQuestion.row  + " AND " +
                                ColsPuzzleQuestions.IS_ANSWERED + "=" + "0" , null);

                        questionIndex ++;
                    }
                }
            });
        }

    }

    @Override public void putFriendsImage(@Nonnull String url, @Nonnull byte[] bytes)
    {

    }

    @Override
    public void putPuzzleSetList(final @Nonnull List<PuzzleSet> list)
    {
        for (final PuzzleSet puzzleSet : list)
        {
            final PuzzleSet existingSet = getPuzzleSetByServerId(puzzleSet.serverId);
            if(existingSet == null)
            {
                DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
                {
                    @Override
                    public void handle()
                    {
                        ContentValues values = mPuzzleSetContentValuesCreator.createObjectContentValues(puzzleSet);
                        mDb.insert(TNAME_PUZZLE_SETS, null, values);
                    }
                });
            }
            else
            {
                DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
                {
                    @Override
                    public void handle()
                    {
                        ContentValues values = mPuzzleSetContentValuesCreator.createObjectContentValues(puzzleSet);
                        int count = mDb.update(TNAME_PUZZLE_SETS, values, ColsPuzzleSets.ID+"="+existingSet.id,null);
                        int k = 1;
                    }
                });
            }
        }

    }

    @Override
    public void putPuzzleTotalSetList(@Nonnull List<PuzzleTotalSet> list) {

        for(PuzzleTotalSet puzzleTotalSet : list)
        {
            final @Nonnull PuzzleTotalSet fPuzzleTotalSet = puzzleTotalSet;
            final @Nullable PuzzleSet existingSet = getPuzzleSetByServerId(puzzleTotalSet.serverId);

            @Nonnull List<String> puzzlesIds = new ArrayList<String>();
            for(Puzzle puzzle : puzzleTotalSet.puzzles)
            {
                puzzlesIds.add(puzzle.serverId);
//                putPuzzle(puzzle);
            }
            final @Nonnull PuzzleSet puzzleSet = new PuzzleSet(puzzleTotalSet.id,puzzleTotalSet.serverId, puzzleTotalSet.name,
                    puzzleTotalSet.isBought, puzzleTotalSet.type, puzzleTotalSet.month, puzzleTotalSet.year,
                    puzzleTotalSet.createdAt, puzzleTotalSet.isPublished, puzzlesIds);

            if(existingSet == null)
            {
                DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
                {
                    @Override
                    public void handle()
                    {
                        ContentValues values = mPuzzleSetContentValuesCreator.createObjectContentValues(puzzleSet);
                        long id = mDb.insert(TNAME_PUZZLE_SETS, null, values);
                        if(id != -1)
                        {
                            for(Puzzle puzzle : fPuzzleTotalSet.puzzles)
                            {
                                puzzle.setId = id;
                                putPuzzle(puzzle);
                            }
                        }
                    }
                });
            }
            else
            {
                DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
                {
                    @Override
                    public void handle()
                    {
                        ContentValues values = mPuzzleSetContentValuesCreator.createObjectContentValues(puzzleSet);
                        int rows = mDb.update(TNAME_PUZZLE_SETS, values, ColsPuzzleSets.SERVER_ID + "='" + puzzleSet.serverId+"'", null);
                        if(existingSet.id != -1)
                        {
                            for(Puzzle puzzle : fPuzzleTotalSet.puzzles)
                            {
                                puzzle.setId = existingSet.id;
                                putPuzzle(puzzle);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void setQuestionAnswered(final long questionId, final boolean answered)
    {
        final ContentValues values = new ContentValues();
        values.put(ColsPuzzleQuestions.IS_ANSWERED, answered);

        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.update(TNAME_PUZZLE_QUESTIONS, values, ColsPuzzleQuestions.ID + "=" + questionId, null);
            }
        });
    }

    @Override
    public void setQuestionAnswered(final long[] questionsIdArray, boolean answered)
    {
        if (questionsIdArray == null)
        {
            return;
        }

        final ContentValues values = new ContentValues();
        values.put(ColsPuzzleQuestions.IS_ANSWERED, answered);

        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                for (int i = 0; i < questionsIdArray.length; i++)
                {
                    long questionId = questionsIdArray[i];
                    mDb.update(TNAME_PUZZLE_QUESTIONS, values, ColsPuzzleQuestions.ID + "=" + questionId, null);
                }
            }
        });
    }

    @Override
    public void putCoefficients(@Nonnull Coefficients coefficients)
    {
        final ContentValues values = mCoefficientsContentValuesCreator.createObjectContentValues(coefficients);
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.delete(TNAME_COEFFICIENTS, null, null);
                mDb.insert(TNAME_COEFFICIENTS, null, values);
            }
        });
    }

    @Override
    public void putScoreToQueue(@Nonnull ScoreQueue.Score score)
    {
        final ContentValues values = mScoreContentValuesCreator.createObjectContentValues(score);
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.insert(TNAME_POST_SCORE_QUEUE, null, values);
            }
        });
    }

    @Override
    public void clearScoreQueue()
    {
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.delete(TNAME_POST_SCORE_QUEUE, null, null);
            }
        });
    }

    @Override
    public void putPurchases(@Nullable ArrayList<PurchasePrizeWord> purchases) {
        if(purchases == null) return;
        for(PurchasePrizeWord purchase : purchases){
            putPurchase(purchase);
        }
    }

    @Override
    public void putPurchase(@Nullable PurchasePrizeWord purchase) {
        if(purchase == null) return;

        NavigationActivity.debug("DB put product: " + purchase.googleId + " state: " + purchase.googlePurchase + " " + purchase.serverPurchase);

        final @Nonnull ContentValues values = mPurchaseValuesCreator.createObjectContentValues(purchase);
        final @Nullable PurchasePrizeWord existsPurchase = getPurchaseByGoogleId(purchase.googleId);

        if(existsPurchase == null)
        {
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    long id = mDb.insert(TNAME_PURCHASES, null, values);
                    int k = 1;
                }
            });
        }
        else
        {
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    int k = mDb.update(TNAME_PURCHASES, values, ColsPurchases.ID + "=" + existsPurchase.id, null);
                    int p = 1;
                }
            });
        }
    }

    @Override
    public void changeHintsCount(int hintsDelta)
    {
        UserData userData = getUserById(SQLiteHelper.ID_USER);
        ArrayList<UserProvider> userProviders = getUserProvidersByUserId(SQLiteHelper.ID_USER);
        if (userData != null && userProviders != null)
        {
            userData.hints += hintsDelta;
            putUser(userData, userProviders);
        }
    }

    @Override
    public void updateNews(@Nullable News news)
    {
        @Nullable News existingNews = getNews();
        final ContentValues values = mNewsValuesCreator.createObjectContentValues(news);
        if(existingNews == null)
        {
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    long created_id = mDb.insert(TNAME_NEWS, null, values);
                }
            });
        }
        else
        {
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    mDb.update(TNAME_NEWS, values, ColsPurchases.ID + "=" + SQLiteHelper.ID_USER,
                            null);
                }
            });

        }
    }

    @Override
    public void clearDb() {
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                int count =0;
                count = mDb.delete(TNAME_PUZZLE_SETS, null, null);
                count = mDb.delete(TNAME_PUZZLES, null, null);
                count = mDb.delete(TNAME_PUZZLE_QUESTIONS, null, null);
                count = mDb.delete(TNAME_IMAGES, null, null);
                count = mDb.delete(TNAME_PROVIDERS, null, null);
                count = mDb.delete(TNAME_USERS, null, null);
                count = mDb.delete(TNAME_COEFFICIENTS, null, null);
                count = mDb.delete(TNAME_POST_SCORE_QUEUE, null, null);
                count = mDb.delete(TNAME_PURCHASES, null, null);
                count = mDb.delete(TNAME_NEWS, null, null);
                count = 0;
            }
        });
    }

    //===== ContentValues creators =======================

    private ContentValuesCreator<UserData> mUserDataContentValuesCreator = new ContentValuesCreator<UserData>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable UserData user)
        {
            ContentValues cvUser = new ContentValues();
            if (user != null)
            {
                cvUser.put(ColsUsers.ID, SQLiteHelper.ID_USER);
                cvUser.put(ColsUsers.NAME, user.name);
                cvUser.put(ColsUsers.SURNAME, user.surname);
                cvUser.put(ColsUsers.EMAIL, user.email);
                cvUser.put(ColsUsers.BIRTHDATE, user.bithdate);
                cvUser.put(ColsUsers.CITY, user.city);
                cvUser.put(ColsUsers.SOLVED, user.solved);
                cvUser.put(ColsUsers.POSITION, user.position);
                cvUser.put(ColsUsers.MONTH_SCORE, user.monthScore);
                cvUser.put(ColsUsers.HIGH_SCORE, user.highScore);
                cvUser.put(ColsUsers.DYNAMICS, user.dynamics);
                cvUser.put(ColsUsers.HINTS, user.hints);
                cvUser.put(ColsUsers.PREVIEW_URL, user.previewUrl);
            }
            return cvUser;
        }
    };

    private ContentValuesCreator<UserImage> mUserPicContentValuesCreator = new ContentValuesCreator<UserImage>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable UserImage image)
        {
            ContentValues cvUser = new ContentValues();
            if (image != null)
            {
                cvUser.put(ColsImages.KEY, image.key);
                cvUser.put(ColsImages.IMAGE, image.image);
            }
            return cvUser;
        }
    };

    private ContentValuesCreator<UserProvider> mUserProviderContentValuesCreator = new ContentValuesCreator<UserProvider>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable UserProvider prov)
        {
            ContentValues cv = new ContentValues();
            cv.put(ColsProviders.NAME, prov.name);
            cv.put(ColsProviders.PROVIDER_ID, prov.providerId);
            cv.put(ColsProviders.TOKEN, prov.providerToken);
            cv.put(ColsProviders.USER_ID, prov.userId);
            return cv;
        }
    };

    private ContentValuesCreator<PuzzleSet> mPuzzleSetContentValuesCreator = new ContentValuesCreator<PuzzleSet>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable PuzzleSet object)
        {
            ContentValues cv = new ContentValues();
            cv.put(ColsPuzzleSets.SERVER_ID, object.serverId);
            cv.put(ColsPuzzleSets.NAME, object.name);
            cv.put(ColsPuzzleSets.IS_BOUGHT, object.isBought);
            cv.put(ColsPuzzleSets.TYPE, object.type);
            cv.put(ColsPuzzleSets.MONTH, object.month);
            cv.put(ColsPuzzleSets.YEAR, object.year);
            cv.put(ColsPuzzleSets.CREATED_AT, object.createdAt);
            cv.put(ColsPuzzleSets.IS_PUBLISHED, object.isPublished);
            StringBuilder builder = new StringBuilder();
            for (String s : object.puzzlesId)
            {
                builder.append(s);
                if(object.puzzlesId.indexOf(s) != object.puzzlesId.size() - 1)
                    builder.append(SET_PUZZLE_IDS_SEPARATOR);
            }
            cv.put(ColsPuzzleSets.PUZZLES_SERVER_IDS, builder.toString());
            return cv;
        }
    };

    private ContentValuesCreator<Puzzle> mPuzzleContentValuesCreator = new ContentValuesCreator<Puzzle>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable Puzzle object)
        {
            ContentValues cv = new ContentValues();
            cv.put(ColsPuzzles.SET_ID, object.setId);
            cv.put(ColsPuzzles.SERVER_ID, object.serverId);
            cv.put(ColsPuzzles.NAME, object.name);
            cv.put(ColsPuzzles.ISSUED_AT, object.issuedAt);
            cv.put(ColsPuzzles.BASE_SCORE, object.baseScore);
            cv.put(ColsPuzzles.TIME_GIVEN, object.timeGiven);
            cv.put(ColsPuzzles.TIME_LEFT, object.timeLeft);
            cv.put(ColsPuzzles.SCORE, object.score);
            cv.put(ColsPuzzles.IS_SOLVED, object.isSolved);
            return cv;
        }
    };

    private ContentValuesCreator<PuzzleQuestion> mPuzzleQuestionContentValuesCreator = new ContentValuesCreator<PuzzleQuestion>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable PuzzleQuestion object)
        {
            ContentValues cv  = new ContentValues();
            cv.put(ColsPuzzleQuestions.COLUMN, object.column);
            cv.put(ColsPuzzleQuestions.ROW, object.row);
            cv.put(ColsPuzzleQuestions.QUESTION_TEXT, object.questionText);
            cv.put(ColsPuzzleQuestions.ANSWER, object.answer);
            cv.put(ColsPuzzleQuestions.ANSWER_POSITION, object.answerPosition);
            cv.put(ColsPuzzleQuestions.IS_ANSWERED, object.isAnswered);
            return cv;
        }
    };

    private ContentValuesCreator<Coefficients> mCoefficientsContentValuesCreator = new ContentValuesCreator<Coefficients>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable Coefficients object)
        {
            ContentValues cv  = new ContentValues();
            cv.put(ColsCoefficients.ID, object.id);
            cv.put(ColsCoefficients.TIME_BONUS, object.timeBonus);
            cv.put(ColsCoefficients.FRIEND_BONUS, object.friendBonus);
            cv.put(ColsCoefficients.FREE_BASE_SCORE, object.freeBaseScore);
            cv.put(ColsCoefficients.GOLD_BASE_SCORE, object.goldBaseScore);
            cv.put(ColsCoefficients.BRILLIANT_BASE_SCORE, object.brilliantBaseScore);
            cv.put(ColsCoefficients.SILVER1_BASE_SCORE, object.silver1BaseScore);
            cv.put(ColsCoefficients.SILVER2_BASE_SCORE, object.silver2BaseScore);

            return cv;
        }
    };

    private ContentValuesCreator<ScoreQueue.Score> mScoreContentValuesCreator  = new ContentValuesCreator<ScoreQueue.Score>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable ScoreQueue.Score object)
        {
            ContentValues cv  = new ContentValues();
            cv.put(ColsScoreQueue.SCORE, object.score);
            cv.put(ColsScoreQueue.PUZZLE_ID, object.puzzleId);
            return cv;
        }
    };

    private ContentValuesCreator<PurchasePrizeWord> mPurchaseValuesCreator  = new ContentValuesCreator<PurchasePrizeWord>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable PurchasePrizeWord object)
        {
            ContentValues cv  = new ContentValues();
//            cv.put(ColsPurchases.ID, object.id);
            cv.put(ColsPurchases.CLIENT_ID,         object.clientId);
            cv.put(ColsPurchases.GOOGLE_ID,         object.googleId);
            cv.put(ColsPurchases.PRICE,             object.price);
            cv.put(ColsPurchases.GOOGLE_PURCHASE,   object.googlePurchase);
            cv.put(ColsPurchases.SERVER_PURCHASE,   object.serverPurchase);
            cv.put(ColsPurchases.RECEIPT_DATA,      object.receipt_data);
            cv.put(ColsPurchases.SIGNATURE,         object.signature);
            return cv;
        }
    };

    private ContentValuesCreator<News> mNewsValuesCreator = new ContentValuesCreator<News>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable News object)
        {
            ContentValues cv  = new ContentValues();
            cv.put(SQLiteHelper.ColsNews.ID, SQLiteHelper.ID_USER);
            cv.put(SQLiteHelper.ColsNews.MESSAGE_1, object.message1);
            cv.put(SQLiteHelper.ColsNews.MESSAGE_2, object.message2);
            cv.put(SQLiteHelper.ColsNews.MESSAGE_3, object.message3);
            cv.put(SQLiteHelper.ColsNews.CLOSED, object.closed);
            cv.put(SQLiteHelper.ColsNews.ETAG_HASH, object.etagHash);
            return cv;
        }
    };

    // ========================================================

    private <T> List<ContentValues> createContentValuesList(@Nullable List<T> objectList, @Nonnull ContentValuesCreator<T> creator)
    {
        @Nullable List<ContentValues> cvList = null;
        if (objectList != null)
        {
            cvList = new ArrayList<ContentValues>(objectList.size());
            for (T object : objectList)
            {
                ContentValues cv = creator.createObjectContentValues(object);
                cvList.add(cv);
            }
        }
        return cvList;
    }
    public interface ContentValuesCreator<T>
    {
        public ContentValues createObjectContentValues(@Nullable T object);

    }
}
