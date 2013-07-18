package com.ltst.prizeword.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

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
        @Nullable UserData exitingUser = findExistingUser(user.email);
        if(exitingUser == null)
        {
            putNewUser(user, providers);
        }
        else
            updateExistingUser(user, providers);
    }

    private void putNewUser(@Nonnull UserData user, @Nullable List<UserProvider> providers)
    {
        mDb.beginTransaction();
        ContentValues cvUser = new ContentValues();
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
        cvUser.put(ColsUsers.PREVIEW_KEY, user.previewUrl);

        @Nullable List<ContentValues> cvProviders = null;
        if (providers != null)
        {
            cvProviders = new ArrayList<ContentValues>(providers.size());
            for (Iterator<UserProvider> iterator = providers.iterator(); iterator.hasNext(); )
            {
                UserProvider prov =  iterator.next();
                ContentValues cv = new ContentValues();
                cv.put(ColsProviders.NAME, prov.name);
                cv.put(ColsProviders.PROVIDER_ID, prov.providerId);
                cv.put(ColsProviders.TOKEN, prov.providerToken);
                cvProviders.add(cv);
            }
        }

        try
        {
            mDb.insert(TNAME_USERS, null, cvUser);

            UserData createdUser = findExistingUser(user.email);
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
            Log.e(e);
        }
        finally
        {
            mDb.endTransaction();
        }
    }

    private void updateExistingUser(UserData user, List<UserProvider> providers)
    {
    }

    private @Nullable UserData findExistingUser(@Nonnull String email)
    {
        IDbReader reader = getReader();
        return reader.getUserByEmail(email);
    }

    private @Nullable List<UserProvider> findUserProviders(@Nonnull String email)
    {
        return null;
    }
}
