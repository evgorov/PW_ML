package com.ltst.prizeword.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserProvider;

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
        super(helper);
        mDb = helper.createWritableSQLiteDatabase();
        SQLiteHelper.configureSQLiteDatabase(mDb);
    }

    @Override
    public void putUser(@Nonnull UserData user, @Nullable List<UserProvider> providers)
    {
        @Nullable UserData exitingUser = getReader().getUserByEmail(user.email);
        if(exitingUser == null)
        {
            putNewUser(user, providers);
        }
        else
            updateExistingUser(exitingUser.id, user, providers);
    }

    private void putNewUser(@Nonnull UserData user, @Nullable List<UserProvider> providers)
    {
        mDb.beginTransaction();
        ContentValues cvUser = mUserDataContentValuesCreator.createObjectContentValues(user);

        @Nullable List<ContentValues> cvProviders = createContentValuesList(providers, mUserProviderContentValuesCreator);

        try
        {
            mDb.insert(TNAME_USERS, null, cvUser);

            UserData createdUser = getReader().getUserByEmail(user.email);
            if (cvProviders != null)
            {
                for (Iterator<ContentValues> iterator = cvProviders.iterator(); iterator.hasNext(); )
                {
                    ContentValues cvProv =  iterator.next();
                    cvProv.put(ColsProviders.USER_ID, createdUser.id);
                    mDb.insert(TNAME_PROVIDERS, null, cvProv);
                }
            }
        }
        catch(Throwable e)
        {
            Log.e(e.getMessage());
        }
        finally
        {
            mDb.endTransaction();
        }
    }

    private void updateExistingUser(long id, @Nonnull UserData user, @Nullable List<UserProvider> providers)
    {
        @Nullable UserData existingUser = getReader().getUserById(id);
        @Nullable List<UserProvider> existingProviders = getReader().getUserProvidersByUserId(id);
        if (existingUser == null)
        {
            return;
        }
        ContentValues cvUser = mUserDataContentValuesCreator.createObjectContentValues(user);

//        @TODO логику обновления провайдеров пользователя
//        @Nullable List<ContentValues> cvProviders = null;
//        boolean addProvider = false;
//        boolean updateProviders = false;
//        if(providers.size() > existingProviders.size())
//        {
//            for (UserProvider existingProvider : existingProviders)
//            {
//                providers.remove(existingProvider);
//            }
//            cvProviders = createContentValuesList(providers, mUserProviderContentValuesCreator);
//        }

        mDb.beginTransaction();
        try
        {
            mDb.update(TNAME_USERS, cvUser, ColsUsers.ID + "=" + id, null);
//            if (cvProviders != null)
//            {
//                for (Iterator<ContentValues> iterator = cvProviders.iterator(); iterator.hasNext(); )
//                {
//                    ContentValues cvProv = iterator.next();
//                    mDb.update(TNAME_PROVIDERS, cvProv, ColsProviders.USER_ID + "=" + id, null);
//                }
//            }

        }
        catch (Throwable e)
        {
            Log.e(e.getMessage());
        }
        finally
        {
            mDb.endTransaction();
        }
    }

    @Override
    public void putPuzzleSet(@Nonnull PuzzleSet set)
    {
        mDb.beginTransaction();
        ContentValues values = mPuzzleSetContentValuesCreator.createObjectContentValues(set);

        try
        {
            mDb.insert(TNAME_PUZZLE_SETS, null, values);
        }
        catch (Throwable e)
        {
            Log.e(e.getMessage());
        }
        finally
        {
            mDb.endTransaction();
        }
    }

    @Override
    public void putPuzzle(@Nonnull Puzzle puzzle)
    {
        mDb.beginTransaction();
        ContentValues values = mPuzzleContentValuesCreator.createObjectContentValues(puzzle);
        List<ContentValues> questionCv = createContentValuesList(puzzle.questions, mPuzzleQuestionContentValuesCreator);
        try
        {
            mDb.insert(TNAME_PUZZLES, null, values);
            for (ContentValues contentValues : questionCv)
            {
                mDb.insert(TNAME_PUZZLE_QUESTIONS, null, contentValues);
            }
        }
        catch (Throwable e)
        {
            Log.e(e.getMessage());
        }
        finally
        {
            mDb.endTransaction();
        }

    }

    @Override
    public void putPuzzleSetList(@Nonnull List<PuzzleSet> list)
    {
        for (PuzzleSet puzzleSet : list)
        {
            putPuzzleSet(puzzleSet);
        }
    }

    @Override
    public void putPuzzleList(@Nonnull List<Puzzle> list)
    {
        for (Puzzle puzzle : list)
        {
            putPuzzle(puzzle);
        }
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
            cv.put(ColsPuzzleQuestions.PUZZLE_ID, object.puzzleId);
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
