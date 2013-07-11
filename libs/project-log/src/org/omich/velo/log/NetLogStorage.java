package org.omich.velo.log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import org.omich.velo.cast.NonnullableCasts;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

/**
 * Сохраняет всё в файл, а извлекает оттуда только ту часть (10 или сколько-то записей),
 * которая предшествовала ошибке (w, e, wtf levels)
 */
public class NetLogStorage implements StorageLog.IStorage, INetLogStorage
{
	private static final String INTERNAL_LOG_TAG = NetLogStorage.class.getSimpleName();
	private static final String BIG_MARKER    = "\n===BIG===\n"; //$NON-NLS-1$
	private static final int BIG_MARKER_SUM   = sumBytes(BIG_MARKER);
	private static final String SMALL_MARKER  = "\n--SMALL--\n"; //$NON-NLS-1$
	private static final int SMALL_MARKER_SUM = sumBytes(SMALL_MARKER);
	private static final int NO_MARKER_SUM = 0;
	private static final @Nonnull Object msMutex = new Object();

	private final @Nonnull String fileName;
	private final int numLogItemsBeforeBigMarker;
	private final @Nonnull Context context;
	private final @Nonnull StringBuilder stringBuilder = new StringBuilder();

	public NetLogStorage(@Nonnull Context context)
	{
		this(context, "log.txt", 10); //$NON-NLS-1$
	}

	public NetLogStorage(@Nonnull Context context, @Nonnull String fileName,
			int numLogItemsBeforeBigMarker)
	{
		this.fileName = fileName;
		this.context = context;
		this.numLogItemsBeforeBigMarker = numLogItemsBeforeBigMarker;
	}
	
	public void commit()
	{
		synchronized (msMutex)
		{
			commitStringBuilder(context, stringBuilder, fileName);			
		}
	}
	
	//==== INetLogStorage =====================================================
	public @Nonnull byte[] getLogs()
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writeLogsToOutputStream(os);
		return NonnullableCasts.byteArrayOutputStreamToByteArray(os);
	}
	
	public void writeLogsToOutputStream(@Nonnull OutputStream os)
	{
		synchronized (msMutex)
		{
			if(isMainThread())
			{
				commitStringBuilder(context, stringBuilder, fileName);
			}

			try
			{
				FileInputStream fis = context.openFileInput(fileName);
				try
				{
					InputStream bis = new BufferedInputStream(fis);
					Queue queue = new Queue(numLogItemsBeforeBigMarker);
					
					while(storeToQueueUntilBigMarker(queue, bis))
					{
						while(!queue.isEmpty())
						{
							os.write(queue.pop());
						}
					}
				}
				finally
				{
					fis.close();
				}
			}
			catch(IOException e)
			{
				LoggableOfThrowable lot = new LoggableOfThrowable(e);
				Log.e(INTERNAL_LOG_TAG,
						"Can't read or write logs to external output stream: " + lot.getFullLogMessage()); //$NON-NLS-1$
			}
		}
	}
	
	public void clearLogs()
	{
		synchronized (msMutex)
		{
			context.deleteFile(fileName);
		}		
	}
	
	//==== IStorage ==========================================================
	@Override
	public void log(@Nonnull String msg, @Nonnull Level level)
	{
		synchronized (msMutex)
		{
			String marker = level == Level.WTF || level == Level.E || level == Level.W
					? BIG_MARKER
					: SMALL_MARKER;
			if(isMainThread())
			{
				stringBuilder.append(msg);
				stringBuilder.append(marker);
			}
			else
			{
				try
				{
					FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
					try
					{
						Writer w = new BufferedWriter(new OutputStreamWriter(fos));
						int sbLength = stringBuilder.length();
						if(sbLength > 0)
						{
							w.write(stringBuilder.toString());
							stringBuilder.delete(0, sbLength);
						}
						w.write(msg);
						w.write(marker);
						w.close();
					}
					finally
					{
						fos.close();
					}
				}
				catch(IOException e)
				{
					LoggableOfThrowable lot = new LoggableOfThrowable(e);
					Log.e(INTERNAL_LOG_TAG,
							"Can't log to file \"" + fileName + "\": " + lot.getFullLogMessage()); //$NON-NLS-1$  //$NON-NLS-2$
				}
			}
		}
	}

	//========================================================================
	private static boolean storeToQueueUntilBigMarker(@Nonnull Queue queue, @Nonnull InputStream is)
	throws IOException
	{
		int sum;
		while(NO_MARKER_SUM != (sum = readUntilMarker(queue, is)))
		{
			if(sum == BIG_MARKER_SUM)
				return true;
		}
		return false;
	}
	
	private static int readUntilMarker(@Nonnull Queue queue, @Nonnull InputStream is)
	throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int len = BIG_MARKER.length();
		assert len == SMALL_MARKER.length();
		
		int b = 0;
		ByteQueue bq = new ByteQueue(len);

		for(int i = 0; i < len && b >= 0; ++i)
		{
			b = is.read();
			bq.push((byte)b);
		}

		while(b >= 0 && bq.getSum() != BIG_MARKER_SUM && bq.getSum() != SMALL_MARKER_SUM)
		{
			b = is.read();
			bos.write(bq.pop());
			bq.push((byte)b);
		}
		bos.write((byte)'\n');
		
		if(b < 0)
			return NO_MARKER_SUM;
		
		queue.push(NonnullableCasts.byteArrayOutputStreamToByteArray(bos));
		return bq.getSum();
	}

	private static void commitStringBuilder(@Nonnull Context context,
			@Nonnull StringBuilder stringBuilder,
			@Nonnull String fileName)
	{
		int sbLength = stringBuilder.length();
		if(sbLength <= 0)
			return;

		try
		{
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
			try
			{
				Writer w = new BufferedWriter(new OutputStreamWriter(fos));
				w.write(stringBuilder.toString());
				stringBuilder.delete(0, sbLength);
				w.close();
			}
			finally
			{
				fos.close();
			}
		}
		catch(IOException e)
		{
			LoggableOfThrowable lot = new LoggableOfThrowable(e);
			Log.e(INTERNAL_LOG_TAG,
					"Can't commit log buffer to file \"" + fileName + "\": " + lot.getFullLogMessage()); //$NON-NLS-1$  //$NON-NLS-2$
		}		
	}
	
	private static boolean isMainThread()
	{
		return Looper.getMainLooper().getThread() == Thread.currentThread();
	}
	
	private static int sumBytes(String str)
	{
		if(str == null)
			return 0;
		byte[] bytes = str.getBytes();
		int size = bytes.length;
		int sum = 0;
		for(int i = 0; i < size; ++i)
		{
			sum += bytes[i];
		}
		return sum;
	}
	
	private static class ByteQueue
	{
		private final @Nonnull byte[] bytes;
		private int sum = 0;
		private int start = 0;
		private int end = 0;
		
		public ByteQueue(int maxLength)
		{
			bytes = new byte[2 * maxLength];
		}
		
		public int push(byte b)
		{
			bytes[end] = b;
			++end;
			sum += b;
			return sum;
		}
		
		public byte pop()
		{
			byte b = bytes[start];
			++start;
			sum -= b;
			if(slideIfReady(bytes, start, end))
			{
				end -= start;
				start = 0;
			}
			return b;
		}
		
		public int getSum()
		{
			return sum;
		}
		
		private static boolean slideIfReady(byte[] bytes, int start, int end)
		{
			if(start <= bytes.length / 2)
				return false;
			
			System.arraycopy(bytes, start, bytes, 0, end - start);
			return true;
		}
	}
	
	private static class Queue
	{
		private final @Nonnull List<byte[]> list = new LinkedList<byte[]>();
		private final int limit;
		
		public Queue(int limit)
		{
			this.limit = limit;
		}
		
		public void push(@Nonnull byte[] arr)
		{
			list.add(arr);
			if(list.size() > limit)
			{
				list.remove(0);
			}
		}
		
		@SuppressWarnings("null")// Мы не допускаем попадания сюда Null объектов.
		public @Nonnull byte[] pop()
		{
			return list.remove(0);
		}
		
		public boolean isEmpty()
		{
			return list.size() <= 0;
		}
	}
}
