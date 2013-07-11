package org.omich.velo.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.cast.NonnullableCasts;

public class StorageLog implements ILog
{
	public static interface IStorage
	{
		void log (@Nonnull String msg, @Nonnull Level level);
	}

	public static final String LEVEL_E     = "ERROR__"; //$NON-NLS-1$
	public static final String LEVEL_W     = "WARNING"; //$NON-NLS-1$
	public static final String LEVEL_WTF   = "WTF____"; //$NON-NLS-1$
	public static final String LEVEL_D     = "DEBUG__"; //$NON-NLS-1$
	public static final String LEVEL_I     = "INFO___"; //$NON-NLS-1$
	public static final String LEVEL_WTF_NOLEVEL = "WTF_NOL"; //$NON-NLS-1$
	
	private final @Nonnull IStorage mStorage;
	
	private final @Nonnull Calendar mCal;
	private final @Nonnull SimpleDateFormat mDf;
	private final @Nonnull String mAppVersion;
	private final @Nonnull String mDeviceAndOs;
	
	public StorageLog (@Nonnull IStorage storage,
			@Nullable String appVersion,
			@Nullable String deviceAndOs)
	{
		mStorage = storage;
		mAppVersion = appVersion == null ? "null" : appVersion; //$NON-NLS-1$
		mDeviceAndOs = deviceAndOs == null ? "null" : deviceAndOs; //$NON-NLS-1$
		
		mCal = new GregorianCalendar();
		mDf = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS Z", Locale.US); //$NON-NLS-1$);
		mDf.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
	}
	
	public void log (@Nonnull String tag, @Nonnull String msg, @Nonnull Level level)
	{
		mStorage.log(getLogMessage(tag, getStringByLevel(level), msg), level);
	}
	
	public void log (@Nonnull String msg, @Nonnull Level level)
	{
		mStorage.log(getLogMessage(getStringByLevel(level), msg), level);
	}
	
	private static @Nonnull String getStringByLevel(@Nonnull Level level)
	{
		switch(level)
		{
		case WTF:
			return LEVEL_WTF;
		case E:
			return LEVEL_E;
		case W:
			return LEVEL_W;
		case I:
			return LEVEL_I;
		case D:
			return LEVEL_D;
		default:
			return LEVEL_WTF_NOLEVEL;
		}		
	}
	
	private @Nonnull String getLogMessage (@Nonnull String level, @Nonnull String msg)
	{
		return NonnullableCasts.stringFormat("v.%s (%s). %s;\t%s;\t%s\n", //$NON-NLS-1$
				mAppVersion, mDeviceAndOs, 
				createDateTimeString(),
				level, msg);
	}

	private @Nonnull String getLogMessage (@Nonnull String tag, @Nonnull String level, @Nonnull String msg)
	{
		return NonnullableCasts.stringFormat("v.%s (%s). %s;\t%s;\ttag(%s)\t%s\n", //$NON-NLS-1$ 
				mAppVersion, mDeviceAndOs,
				createDateTimeString(),
				level, tag, msg);
	}
	
	private @Nullable String createDateTimeString()
	{
		long curTime = System.currentTimeMillis();
		mCal.setTimeInMillis(curTime);
		return mDf.format(mCal.getTime());		
	}
}
