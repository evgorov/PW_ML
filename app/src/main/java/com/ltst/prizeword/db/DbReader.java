package com.ltst.prizeword.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserImage;
import com.ltst.prizeword.login.model.UserProvider;

import org.omich.velo.db.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
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
            ColsPuzzleQuestions.ANSWER_POSITION

    };

    public static final @Nonnull String[] FIELDS_P_IMAGES =
    {
            ColsImages.ID,
            ColsImages.KEY,
            ColsImages.IMAGE,
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
    public UserData getUserById(long id)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_USERS,
                FIELDS_P_USER, ColsUsers.ID, id);
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


    public @Nullable List<UserProvider> getUserProvidersByUserId(long userId)
    {
        final  Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PROVIDERS, FIELDS_P_USER_PROVIDERS,
                ColsProviders.USER_ID, userId);
        List<UserProvider> providerList = createTypedListByCursor(cursor, new ObjectCreatorByCursor<UserProvider>()
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
    public Puzzle getPuzzleById(long id)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PUZZLES, FIELDS_P_PUZZLES, ColsPuzzles.ID, id);
        Puzzle puzzle = createObjectByCursor(cursor, mPuzzleCreator);
        if (puzzle != null)
        {
            puzzle.questions = getQuestionsByPuzzleId(puzzle.id);
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
            }
        }
        return puzzles;
    }

    @Nullable
    @Override
    public List<PuzzleQuestion> getQuestionsByPuzzleId(long puzzleId)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PUZZLE_QUESTIONS, FIELDS_P_PUZZLE_QUESTIONS, ColsPuzzleQuestions.PUZZLE_ID, puzzleId);
        return createTypedListByCursor(cursor, mPuzzleQuestionsCreator);
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
                            c.getString(6));
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
    private static <T> List<T> createTypedListByCursor(@Nullable Cursor cursor, final @Nonnull ObjectCreatorByCursor<T> creator)
    {
        if (cursor == null)
        {
            return null;
        }

        final List<T> list = new ArrayList<T>(cursor.getCount());
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
