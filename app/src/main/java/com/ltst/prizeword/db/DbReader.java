package com.ltst.prizeword.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.login.model.UserData;
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
    private static final String SET_PUZZLE_IDS_SEPARATOR = "|";

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

    public final @Nonnull SQLiteDatabase mDb;

    public DbReader(@Nonnull SQLiteHelper helper) throws DbException
    {
        mDb = helper.createReadableSQLiteDatabase();
        SQLiteHelper.configureSQLiteDatabase(mDb);
    }

    protected @Nonnull IDbReader getReader()
    {
        return this;
    }

    @Nullable
    @Override
    public UserData getUserByEmail(@Nonnull String email)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_USERS,
                FIELDS_P_USER, ColsUsers.EMAIL, email);
        UserData user = createObjectByCursor(cursor, new ObjectCreatorByCursor<UserData>()
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
        });
        return user;
    }

    @Nullable
    @Override
    public UserData getUserById(long id)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_USERS,
                FIELDS_P_USER, ColsUsers.ID, id);
        UserData user = createObjectByCursor(cursor, new ObjectCreatorByCursor<UserData>()
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
        });
        return user;
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

    private static @Nonnull List<String> parsePuzzleServerIds(@Nonnull String idsSeparated)
    {
        String[] ids = idsSeparated.split(SET_PUZZLE_IDS_SEPARATOR);
        List<String> list = Arrays.asList(ids);
        return list;
    }

    //=========================================================================

    @Nullable
    private static <T> List<T> createTypedListByCursor(@Nonnull Cursor cursor, @Nonnull ObjectCreatorByCursor<T> creator)
    {
        cursor.moveToFirst();
        List<T> list = null;
        while (!cursor.isAfterLast())
        {
            if (list != null)
            {
                list = new ArrayList<T>(cursor.getCount());
            }

            T object = creator.createObject(cursor);
            list.add(object);

            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    @Nullable
    private static <T> T createObjectByCursor(@Nonnull Cursor cursor, @Nonnull ObjectCreatorByCursor<T> creator)
    {
        cursor.moveToFirst();
        T object = null;
        if(!cursor.isAfterLast())
        {
            object = creator.createObject(cursor);
        }
        cursor.close();
        return object;
    }

    public abstract static class ObjectCreatorByCursor<T>
    {
        public abstract T createObject(Cursor c);
    }
}
