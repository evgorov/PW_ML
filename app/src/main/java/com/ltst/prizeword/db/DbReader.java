package com.ltst.prizeword.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ltst.prizeword.manadges.Purchase;
import com.ltst.prizeword.score.Coefficients;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserImage;
import com.ltst.prizeword.login.model.UserProvider;
import com.ltst.prizeword.score.ScoreQueue;

import org.omich.velo.db.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.ltst.prizeword.db.SQLiteHelper.*;

public class DbReader implements IDbReader
{
    protected static final String SET_PUZZLE_IDS_SEPARATOR = "|";
    protected static final String REGEXP_SHIELD = "\\";

    private static final @Nonnull String[] FIELDS_P_USER =
    {
            ColsUsers.ID,
            ColsUsers.NAME,
            ColsUsers.SURNAME,
            ColsUsers.EMAIL,
            ColsUsers.BIRTHDATE,
            ColsUsers.CITY,
            ColsUsers.SOLVED,
            ColsUsers.POSITION,
            ColsUsers.MONTH_SCORE,
            ColsUsers.HIGH_SCORE,
            ColsUsers.DYNAMICS,
            ColsUsers.HINTS,
            ColsUsers.PREVIEW_URL
    };

    private static final @Nonnull String[] FIELDS_P_USER_PROVIDERS =
    {
            ColsProviders.ID,
            ColsProviders.NAME,
            ColsProviders.PROVIDER_ID,
            ColsProviders.TOKEN,
            ColsProviders.USER_ID
    };

    private static final @Nonnull String[] FIELDS_P_PUZZLE_SETS =
    {
            ColsPuzzleSets.ID,
            ColsPuzzleSets.SERVER_ID,
            ColsPuzzleSets.NAME,
            ColsPuzzleSets.IS_BOUGHT,
            ColsPuzzleSets.TYPE,
            ColsPuzzleSets.MONTH,
            ColsPuzzleSets.YEAR,
            ColsPuzzleSets.CREATED_AT,
            ColsPuzzleSets.IS_PUBLISHED,
            ColsPuzzleSets.PUZZLES_SERVER_IDS
    };

    public static final @Nonnull String[] FIELDS_P_PUZZLES =
    {
            ColsPuzzles.ID,
            ColsPuzzles.SET_ID,
            ColsPuzzles.SERVER_ID,
            ColsPuzzles.NAME,
            ColsPuzzles.ISSUED_AT,
            ColsPuzzles.BASE_SCORE,
            ColsPuzzles.TIME_GIVEN,
            ColsPuzzles.TIME_LEFT,
            ColsPuzzles.SCORE,
            ColsPuzzles.IS_SOLVED
    };

    public static final @Nonnull String[] FIELDS_P_PUZZLE_QUESTIONS =
    {
            ColsPuzzleQuestions.ID,
            ColsPuzzleQuestions.PUZZLE_ID,
            ColsPuzzleQuestions.COLUMN,
            ColsPuzzleQuestions.ROW,
            ColsPuzzleQuestions.QUESTION_TEXT,
            ColsPuzzleQuestions.ANSWER,
            ColsPuzzleQuestions.ANSWER_POSITION,
            ColsPuzzleQuestions.IS_ANSWERED
    };

    public static final @Nonnull String[] FIELDS_P_IMAGES =
    {
            ColsImages.ID,
            ColsImages.KEY,
            ColsImages.IMAGE
    };

    public static final @Nonnull String[] FIELDS_P_COEFFICIENTS =
    {
            ColsCoefficients.ID,
            ColsCoefficients.TIME_BONUS,
            ColsCoefficients.FRIEND_BONUS,
            ColsCoefficients.FREE_BASE_SCORE,
            ColsCoefficients.GOLD_BASE_SCORE,
            ColsCoefficients.BRILLIANT_BASE_SCORE,
            ColsCoefficients.SILVER1_BASE_SCORE,
            ColsCoefficients.SILVER2_BASE_SCORE
    };

    public static final @Nonnull String[] FIELDS_P_SCORE_QUEUE =
    {
            ColsScoreQueue.ID,
            ColsScoreQueue.SCORE,
            ColsScoreQueue.PUZZLE_ID
    };

    private static final @Nonnull String[] FIELDS_P_PURCHASES =
            {
                    ColsPurchases.ID,
                    ColsPurchases.CLIENT_ID,
                    ColsPurchases.GOOGLE_ID,
                    ColsPurchases.GOOGLE_PURCHASE,
                    ColsPurchases.GOOGLE_RESET_PURCHASE,
                    ColsPurchases.SERVER_PURCHASE
            };

    public final @Nonnull SQLiteDatabase mDb;

    public DbReader(@Nonnull SQLiteHelper helper, boolean mustBeSQLiteDatabaseWriteable) throws DbException
    {
        mDb = mustBeSQLiteDatabaseWriteable
            ? helper.createWritableSQLiteDatabase()
            : helper.createReadableSQLiteDatabase();
        SQLiteHelper.configureSQLiteDatabase(mDb);
    }

    protected @Nonnull SQLiteDatabase getDb()
    {
        return mDb;
    }

    public void close()
    {
        mDb.close();
    }

//    @Nullable
//    @Override
//    public UserData getUserByEmail(@Nonnull String email)
//    {
//        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_USERS,
//                FIELDS_P_USER, ColsUsers.EMAIL, email);
//        UserData user = createObjectByCursor(cursor, mUserDataCreator);
//        return user;
//    }

    @Nullable
    @Override
    public UserData getUserById(long user_id)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_USERS,
                FIELDS_P_USER, ColsUsers.ID, user_id);
        UserData user = createObjectByCursor(cursor, mUserDataCreator);
        return user;
    }

    @Nullable
    @Override
    public UserImage getUserImage(long user_id)
    {
        UserData userData = getUserById(user_id);
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_IMAGES,
                FIELDS_P_IMAGES, ColsImages.KEY, userData.previewUrl);
        UserImage image = createObjectByCursor(cursor, mUserImageCreator);
        return image;
    }


    public @Nullable ArrayList<UserProvider> getUserProvidersByUserId(long userId)
    {
        final  Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PROVIDERS, FIELDS_P_USER_PROVIDERS,
                ColsProviders.USER_ID, userId);
        ArrayList<UserProvider> providerList = createTypedListByCursor(cursor, new ObjectCreatorByCursor<UserProvider>()
        {
            @Override
            public UserProvider createObject(Cursor c)
            {
                return new UserProvider(c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getLong(4));
            }
        });
        return providerList;
    }

    @Nullable
    @Override
    public PuzzleSet getPuzzleSetById(long id)
    {
        final  Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PUZZLE_SETS, FIELDS_P_PUZZLE_SETS,
                ColsPuzzleSets.ID, id);
        @Nullable PuzzleSet set = createObjectByCursor(cursor, mPuzzleSetCreator);
        return set;
    }

    @Nullable
    @Override
    public PuzzleSet getPuzzleSetByServerId(@Nonnull String serverId)
    {
        final  Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PUZZLE_SETS, FIELDS_P_PUZZLE_SETS,
                ColsPuzzleSets.SERVER_ID, serverId);
        @Nullable PuzzleSet set = createObjectByCursor(cursor, mPuzzleSetCreator);
        return set;
    }

    @Nullable
    @Override
    public List<PuzzleSet> getPuzzleSets()
    {
        final Cursor cursor = mDb.query(TNAME_PUZZLE_SETS, FIELDS_P_PUZZLE_SETS, null, null, null, null, null, null);
        @Nullable List<PuzzleSet> set = createTypedListByCursor(cursor, mPuzzleSetCreator);
        return set;
    }

    @Nullable
    @Override
    public List<Puzzle> getPuzzles(List<String> serverIds)
    {
        int size = serverIds.size();
        StringBuilder selection = new StringBuilder(size);
        for(int i=0; i<size; i++)
        {
            selection.append(ColsPuzzles.SERVER_ID +"=? "+((i == size-1) ? "" : "OR "));
        }
        String[] selectArgs = serverIds.toArray(new String[size]);
        final Cursor cursor = mDb.query(TNAME_PUZZLES, FIELDS_P_PUZZLES, selection.toString(), selectArgs, null, null, null, null);
        @Nullable List<Puzzle> set = createTypedListByCursor(cursor, mPuzzleCreator);
        return set;
    }

    @Nullable
    @Override
    public List<Puzzle> getPuzzlesBySetId(long setId)
    {
        final Cursor cursor = mDb.query(TNAME_PUZZLES, FIELDS_P_PUZZLES, ColsPuzzles.SET_ID+"="+String.valueOf(setId), null, null, null, ColsPuzzles.SERVER_ID, null);
        @Nullable List<Puzzle> set = createTypedListByCursor(cursor, mPuzzleCreator);
        for(@Nullable Puzzle puzzle : set)
        {
            if(puzzle!=null)
            {
                puzzle.questions = getQuestionsByPuzzleId(puzzle.id);
                puzzle.countSolvedPercent();
            }
        }
        return set;
    }

    @Nullable
    @Override
    public Puzzle getPuzzleById(long id)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PUZZLES, FIELDS_P_PUZZLES, ColsPuzzles.ID, id);
        Puzzle puzzle = createObjectByCursor(cursor, mPuzzleCreator);
        if (puzzle != null)
        {
            puzzle.questions = getQuestionsByPuzzleId(puzzle.id);
            puzzle.countSolvedPercent();
        }
        return puzzle;
    }

    @Nullable
    @Override
    public Puzzle getPuzzleByServerId(@Nonnull String serverId)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PUZZLES, FIELDS_P_PUZZLES, ColsPuzzles.SERVER_ID, serverId);
        Puzzle puzzle = createObjectByCursor(cursor, mPuzzleCreator);
        if (puzzle != null)
        {
            puzzle.questions = getQuestionsByPuzzleId(puzzle.id);
            puzzle.countSolvedPercent();
            Collections.sort(puzzle.questions, new Comparator<PuzzleQuestion>()
            {
                @Override
                public int compare(PuzzleQuestion lhs, PuzzleQuestion rhs)
                {
                    int col = lhs.column - rhs.column;
                    int row = lhs.row - rhs.row;
                    if(row == 0)
                        return col;
                    if(col == 0)
                        return row;
                    if(row < 0 && col < 0)
                        return -1;
                    else return 1;
                }
            });
        }
        return puzzle;
    }



    @Nullable
    @Override
    public List<Puzzle> getPuzzleListBySetId(long setId)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PUZZLES, FIELDS_P_PUZZLES, ColsPuzzles.SET_ID, setId);
        List<Puzzle> puzzles = createTypedListByCursor(cursor, mPuzzleCreator);
        for (Puzzle puzzle : puzzles)
        {
            if (puzzle != null)
            {
                puzzle.questions = getQuestionsByPuzzleId(puzzle.id);
                puzzle.countSolvedPercent();
            }
        }
        return puzzles;
    }

    @Nullable
    @Override
    public List<PuzzleQuestion> getQuestionsByPuzzleId(long puzzleId)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PUZZLE_QUESTIONS, FIELDS_P_PUZZLE_QUESTIONS, ColsPuzzleQuestions.PUZZLE_ID, puzzleId);
        List<PuzzleQuestion> questions = createTypedListByCursor(cursor, mPuzzleQuestionsCreator);
        Collections.sort(questions, new Comparator<PuzzleQuestion>()
        {
            @Override
            public int compare(PuzzleQuestion lhs, PuzzleQuestion rhs)
            {
                int col = lhs.column - rhs.column;
                int row = lhs.row - rhs.row;
                if(row == 0)
                    return col;
                if(col == 0)
                    return row;
                if(row < 0 && col < 0)
                    return -1;
                else return 1;
            }
        });
        return questions;
    }

    @Override
    public int getUserHintsCount()
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_USERS,
                new String[]{ColsUsers.HINTS}, ColsUsers.ID, SQLiteHelper.ID_USER);
        if (cursor == null)
        {
            return 0;
        }
        int count = 0;
        try
        {
            cursor.moveToFirst();

            if(!cursor.isAfterLast())
            {
                count = cursor.getInt(0);
            }
        }
        finally
        {
            cursor.close();
        }
        return count;
    }

    @Nullable
    @Override
    public Coefficients getCoefficients()
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_COEFFICIENTS, FIELDS_P_COEFFICIENTS, ColsCoefficients.ID, SQLiteHelper.ID_COEFFICIENTS);
        return createObjectByCursor(cursor, mCoefficientsCreator);
    }

    @Nullable
    @Override
    public ScoreQueue getScoreQueue()
    {
        final Cursor cursor = mDb.query(TNAME_POST_SCORE_QUEUE, FIELDS_P_SCORE_QUEUE, null, null, null, null, null);
        @Nullable List<ScoreQueue.Score> queue = createTypedListByCursor(cursor, mScoreQueueCreator);
        if (queue != null)
        {
            return new ScoreQueue(queue);
        }
        return null;
    }

    @Nullable
    @Override
    public ArrayList<Purchase> getPurchases()
    {
        final  Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PURCHASES, FIELDS_P_PURCHASES,
                null, null);
        @Nullable ArrayList<Purchase> purchases = createTypedListByCursor(cursor, mPurchaseCreator);
        return purchases;
    }

    @Nullable
    @Override
    public Purchase getPurchaseByGoogleId(@Nonnull String googleId)
    {
        final  Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PURCHASES, FIELDS_P_PURCHASES,
                ColsPurchases.GOOGLE_ID, googleId);
        @Nullable Purchase purchase = createObjectByCursor(cursor, mPurchaseCreator);
        return purchase;
    }

    // ==== object creators =====================

    private ObjectCreatorByCursor<PuzzleSet> mPuzzleSetCreator = new ObjectCreatorByCursor<PuzzleSet>()
    {
        @Override
        public PuzzleSet createObject(Cursor c)
        {
            long id = c.getLong(0);
            String serverId = c.getString(1);
            String name = c.getString(2);
            boolean bought = c.getInt(3) == 1;
            String type = c.getString(4);
            int month = c.getInt(5);
            int year = c.getInt(6);
            String created_at = c.getString(7);
            boolean published = c.getInt(8) == 1;
            List<String> puzzlesServerIds = parsePuzzleServerIds(c.getString(9));
            return new PuzzleSet(id, serverId, name, bought, type, month, year, created_at, published, puzzlesServerIds);
        }
    };

    private ObjectCreatorByCursor<UserData> mUserDataCreator = new ObjectCreatorByCursor<UserData>()
    {
        @Override
        public UserData createObject(Cursor c)
        {
            return new UserData(c.getLong(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getInt(6),
                    c.getInt(7),
                    c.getInt(8),
                    c.getInt(9),
                    c.getInt(10),
                    c.getInt(11),
                    c.getString(12),
                    null);
        }
    };

    private ObjectCreatorByCursor<UserImage> mUserImageCreator = new ObjectCreatorByCursor<UserImage>()
    {
        @Override
        public UserImage createObject(Cursor c)
        {
            return new UserImage(c.getLong(0),
                    c.getString(1),
                    c.getBlob(2));
        }
    };

    private ObjectCreatorByCursor<Puzzle> mPuzzleCreator = new ObjectCreatorByCursor<Puzzle>()
    {
        @Override
        public Puzzle createObject(Cursor c)
        {
            return new Puzzle(c.getLong(0),
                              c.getLong(1),
                              c.getString(2),
                              c.getString(3),
                              c.getString(4),
                              c.getInt(5),
                              c.getInt(6),
                              c.getInt(7),
                              c.getInt(8),
                              c.getInt(9) == 1,
                              null);
        }
    };

    private ObjectCreatorByCursor<PuzzleQuestion> mPuzzleQuestionsCreator = new ObjectCreatorByCursor<PuzzleQuestion>()
    {
        @Override
        public PuzzleQuestion createObject(Cursor c)
        {
            return new PuzzleQuestion(c.getLong(0),
                            c.getLong(1),
                            c.getInt(2),
                            c.getInt(3),
                            c.getString(4),
                            c.getString(5),
                            c.getString(6),
                            c.getInt(7) == 1);
        }
    };

    private ObjectCreatorByCursor<Coefficients> mCoefficientsCreator = new ObjectCreatorByCursor<Coefficients>()
    {
        @Override
        public Coefficients createObject(Cursor c)
        {
            return new Coefficients(c.getLong(0),
                    c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5), c.getInt(6), c.getInt(7));
        }
    };

    private ObjectCreatorByCursor<ScoreQueue.Score> mScoreQueueCreator = new ObjectCreatorByCursor<ScoreQueue.Score>()
    {
        @Override
        public ScoreQueue.Score createObject(Cursor c)
        {
            return new ScoreQueue.Score(c.getLong(0), c.getInt(1), c.getString(2));
        }
    };

    private ObjectCreatorByCursor<Purchase> mPurchaseCreator = new ObjectCreatorByCursor<Purchase>()
    {
        @Override
        public Purchase createObject(Cursor c)
        {
            long id = c.getLong(0);
            String clientId = c.getString(1);
            String googleId = c.getString(2);
            boolean googlePurchase = c.getInt(3) == 1;
            boolean googleResetPurchase = c.getInt(4) == 1;
            boolean serverPurchase = c.getInt(5) == 1;
            return new Purchase(id, clientId, googleId, googlePurchase, googleResetPurchase, serverPurchase);
        }
    };

    //===========================================

    private static @Nonnull List<String> parsePuzzleServerIds(@Nonnull String idsSeparated)
    {
        String[] ids = idsSeparated.split(REGEXP_SHIELD + SET_PUZZLE_IDS_SEPARATOR);
        List<String> list = Arrays.asList(ids);
        return list;
    }

    //=========================================================================

    @Nullable
    private static <T> ArrayList<T> createTypedListByCursor(@Nullable Cursor cursor, final @Nonnull ObjectCreatorByCursor<T> creator)
    {
        if (cursor == null)
        {
            return null;
        }

        final ArrayList<T> list = new ArrayList<T>(cursor.getCount());
        try
        {
            DbHelper.iterateCursor(cursor, new DbHelper.CursorIterator()
            {
                @Override
                public void handle(@Nonnull Cursor cursor)
                {
                    T object = creator.createObject(cursor);
                    list.add(object);
                }
            });
        }
        finally
        {
            cursor.close();
        }
        return list;
    }

    @Nullable
    private static <T> T createObjectByCursor(@Nullable Cursor cursor, @Nonnull ObjectCreatorByCursor<T> creator)
    {
        if (cursor == null)
        {
            return null;
        }

        T object = null;
        try
        {
            cursor.moveToFirst();

            if(!cursor.isAfterLast())
            {
                object = creator.createObject(cursor);
            }
        }
        finally
        {
            cursor.close();
        }
        return object;
    }
    public interface ObjectCreatorByCursor<T>
    {
        public T createObject(Cursor c);

    }
}
