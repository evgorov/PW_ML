package com.ltst.prizeword.app;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper
{
	private static final @Nonnull
	String SP_FILE_NAME = "PRIZEWORD_SETTINGS"; //$NON-NLS-1$

	private static Map<Context, SharedPreferencesHelper> instances = new HashMap<Context, SharedPreferencesHelper>();

	private @Nonnull SharedPreferences settings;
	private @Nonnull SharedPreferences.Editor editor;

	private SharedPreferencesHelper(@Nonnull Context context)
	{
		settings = context.getSharedPreferences(SP_FILE_NAME,
				Context.MODE_PRIVATE);
		editor = settings.edit();
	}

	public static @Nonnull SharedPreferencesHelper getInstance(@Nonnull Context context)
	{
		if (!instances.containsKey(context))
			instances.put(context, new SharedPreferencesHelper(context));
		SharedPreferencesHelper helper = instances.get(context);
		assert helper != null;
		return helper;
	}

	// никогда не вернет null т.к. defValue не null.
	@SuppressWarnings("null")
	public @Nonnull String getString(@Nonnull String key, @Nonnull String defValue)
	{
		return settings.getString(key, defValue);
	}

	public @Nonnull SharedPreferencesHelper putString(@Nonnull String key, @Nonnull String value)
	{
		editor.putString(key, value);
		return this;
	}

	public int getInt(@Nonnull String key, int defValue)
	{
		return settings.getInt(key, defValue);
	}

	public @Nonnull SharedPreferencesHelper putInt(@Nonnull String key, int value)
	{
		editor.putInt(key, value);
		return this;
	}

	public boolean getBoolean(@Nonnull String key, boolean defValue)
	{
		return settings.getBoolean(key, defValue);
	}

	public @Nonnull SharedPreferencesHelper putBoolean(@Nonnull String key, boolean value)
	{
		editor.putBoolean(key, value);
		return this;
	}
	
	public float getFloat(@Nonnull String key, float defValue)
	{
		return settings.getFloat(key, defValue);
	}
	
	public @Nonnull SharedPreferencesHelper putFloat(@Nonnull String key, float value)
	{
		editor.putFloat(key, value);
		return this;
	}
	
	public long getLong(@Nonnull String key, long defValue)
	{
		return settings.getLong(key, defValue);
	}
	
	public @Nonnull SharedPreferencesHelper putLong(@Nonnull String key, long value)
	{
		editor.putLong(key, value);
		return this;
	}
	
	public @Nonnull SharedPreferencesHelper commit()
	{
		editor.commit();
		return this;
	}

    public @Nonnull SharedPreferencesHelper erase(@Nonnull String key)
    {
        editor.remove(key);
        return this;
    }

}