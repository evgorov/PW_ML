package com.ltst.prizeword.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserProvider;

import org.omich.velo.db.DbHelper;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.ltst.prizeword.db.SQLiteHelper.*;

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
        @Nullable UserData exitingUser = getUserByEmail(user.email);
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

    private void updateExistingUser(final long id, @Nonnull UserData user, @Nullable List<UserProvider> providers)
    {
        @Nullable UserData existingUser = getUserById(id);
        @Nullable List<UserProvider> existingProviders = getUserProvidersByUserId(id);
        if (existingUser == null)
        {
            return;
        }
        final ContentValues cvUser = mUserDataContentValuesCreator.createObjectContentValues(user);

//        @TODO логику обновления провайдеров пользователя
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.update(TNAME_USERS, cvUser, ColsUsers.ID + "=" + id, null);
            }
        });
    }

    @Override
    public void putPuzzleSet(@Nonnull PuzzleSet set)
    {
        mDb.beginTransaction();
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
        final @Nullable Puzzle existingPuzzle = getPuzzleByServerId(puzzle.serverId);
        final ContentValues values = mPuzzleContentValuesCreator.createObjectContentValues(puzzle);
        final List<ContentValues> questionCv = createContentValuesList(puzzle.questions, mPuzzleQuestionContentValuesCreator);

        if (existingPuzzle == null) // новый кроссворд
        {
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    long id = mDb.insert(TNAME_PUZZLES, null, values);
                    for (ContentValues questionValues : questionCv)
                    {
                        questionValues.put(ColsPuzzleQuestions.PUZZLE_ID, id);
                        mDb.insert(TNAME_PUZZLE_QUESTIONS, null, questionValues);
                    }
                }
            });
        }
        else // нужно обновить старые кроссворды, вопросы обновлять не надо.
        {
            DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    mDb.update(TNAME_PUZZLES, values, ColsPuzzles.ID + "=" + existingPuzzle.id, null);
                }
            });
        }

    }

    @Override
    public void putPuzzleSetList(final @Nonnull List<PuzzleSet> list)
    {
        DbHelper.openTransactionAndFinish(mDb, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                mDb.delete(TNAME_PUZZLE_SETS, null, null); // clean puzzle sets
                for (PuzzleSet puzzleSet : list)
                {
                    ContentValues values = mPuzzleSetContentValuesCreator.createObjectContentValues(puzzleSet);
                    mDb.insert(TNAME_PUZZLE_SETS, null, values);
                }
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

    private ContentValuesCreator<UserProvider> mUserProviderContentValuesCreator = new ContentValuesCreator<UserProvider>()
    {
        @Override
        public ContentValues createObjectContentValues(@Nullable UserProvider prov)
        {
            ContentValues cv = new ContentValues();
            cv.put(ColsProviders.NAME, prov.name);
            cv.put(ColsProviders.PROVIDER_ID, prov.providerId);
            cv.put(ColsProviders.TOKEN, prov.providerToken);
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
            cv.put(ColsPuzzleQuestions.QUESTION_TEXT, object.quesitonText);
            cv.put(ColsPuzzleQuestions.ANSWER, object.answer);
            cv.put(ColsPuzzleQuestions.ANSWER_POSITION, object.answerPosition);
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
