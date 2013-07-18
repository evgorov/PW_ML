package com.ltst.prizeword.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserProvider;

import org.omich.velo.db.DbHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.ltst.prizeword.db.SQLiteHelper.*;

public class DbReader implements IDbReader
{
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
        cursor.moveToFirst();
        UserData user = null;
        if(!cursor.isAfterLast())
        {
            user = new UserData(cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getInt(7),
                    cursor.getInt(8),
                    cursor.getInt(9),
                    cursor.getInt(10),
                    cursor.getInt(11),
                    cursor.getString(12),
                    null);
        }
        cursor.close();
        return user;
    }

    @Nullable
    @Override
    public UserData getUserById(long id)
    {
        final Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_USERS,
                FIELDS_P_USER, ColsUsers.ID, id);
        cursor.moveToFirst();
        UserData user = null;
        if(!cursor.isAfterLast())
        {
            user = new UserData(cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getInt(7),
                    cursor.getInt(8),
                    cursor.getInt(9),
                    cursor.getInt(10),
                    cursor.getInt(11),
                    cursor.getString(12),
                    null);
        }
        cursor.close();
        return user;
    }

    public @Nullable List<UserProvider> getUserProvidersByUserId(long userId)
    {
        final  Cursor cursor = DbHelper.queryBySingleColumn(mDb, TNAME_PROVIDERS, FIELDS_P_USER_PROVIDERS,
                ColsProviders.USER_ID, userId);
        cursor.moveToFirst();
        List<UserProvider> providerList = null;
        while (!cursor.isAfterLast())
        {
            if (providerList != null)
            {
                providerList = new ArrayList<UserProvider>(cursor.getCount());
            }

            UserProvider provider = new UserProvider(cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getLong(4));
            providerList.add(provider);

            cursor.moveToNext();
        }
        cursor.close();
        return providerList;
    }
}
