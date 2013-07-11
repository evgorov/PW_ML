package org.omich.velo.net;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import android.content.res.Resources;

public class Network
{
	public static class FilePart
	{
		public final @Nonnull String fileName;
		public final @Nonnull String partName;
		public final @Nonnull byte[] bytes;
		
		public FilePart(@Nonnull String fileName, @Nonnull String partName, @Nonnull byte[] bytes)
		{
			this.fileName = fileName;
			this.partName = partName;
			this.bytes = bytes;
		}
	}
	
	public static class NetInputStream extends FilterInputStream
	{
		private NetInputStream(@Nonnull InputStream is)
		{
			super(is);
		}
		
		private static @Nonnull NetInputStream create(@Nonnull InputStream is)
		{
			return new NetInputStream(is);
		}
	}




	private static final String PROTOCOL_HTTPS = "https"; //$NON-NLS-1$
	private static final String PROTOCOL_HTTP  = "http"; //$NON-NLS-1$
	private static final @Nonnull HttpParams HTTP_PARAMS = createHttpConnectionParams();





	public static @Nullable String readStringAndCloseStream(@Nullable NetInputStream is)
	{
		byte[] bytes = readBytesAndCloseStream(is);
		return bytes == null ? null : new String(bytes);
	}

	public static @Nullable byte[] readBytesAndCloseStream(@Nullable NetInputStream is)
	{
		if(is == null)
			return null;

		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[100000];
			int len;
			
			try
			{
				while(0 < (len = is.read(buffer)))
				{
					bos.write(buffer, 0, len);
				}
			}
			finally
			{
				is.close();
			}
			
			return NonnullableCasts.byteArrayOutputStreamToByteArray(bos);
		}
		catch(IOException e)
		{
			Log.w("Cant read bytes from NetInputStream that was returned by download method", e); //$NON-NLS-1$
			return null;
		}
	}

	
	
	
	public static @Nullable NetInputStream download(@Nonnull Resources res, int storeId,
			@Nonnull String url)
	{
		return downloadSequre(res, storeId, url, null, null, false);
	}

	public static @Nullable NetInputStream download(@Nonnull Resources res, int storeId,
			@Nonnull String url, @Nonnull Map<String, String> params)
	{
		return downloadSequre(res, storeId, url, params, null, true);
	}


	public static @Nullable NetInputStream download(@Nonnull Resources res, int storeId,
			@Nonnull String url, @Nullable Map<String, String> params, @Nonnull FilePart[] files)
	{
		return downloadSequre(res, storeId, url, params, files, true);
	}

	
	
	
	public static @Nullable NetInputStream download(@Nonnull String url)
	{
		return downloadDefault(url, null, null, false);
	}

	public static @Nullable NetInputStream download(@Nonnull String url, @Nonnull Map<String, String> params)
	{
		return downloadDefault(url, params, null, true);
	}

	public static @Nullable NetInputStream download(@Nonnull String url, @Nonnull FilePart[] files)
	{
		return downloadDefault(url, null, files, true);
	}

	public static @Nullable NetInputStream download(@Nonnull String url, @Nonnull Map<String, String> params, @Nonnull FilePart[] files)
	{
		return downloadDefault(url, params, files, true);
	}

	
	
	




	//=========================================================================
	public static @Nullable NetInputStream downloadSequre(@Nonnull Resources res, int storeId,
			@Nonnull String url, @Nullable Map<String, String> params, @Nullable FilePart[] files, boolean post)
	{
		DefaultHttpClient client = (url.indexOf(PROTOCOL_HTTPS) == 0)
				? new HttpsClient(res, storeId)
				: new DefaultHttpClient();

		return download(client, url, params, files, post);
	}

	private static @Nullable NetInputStream downloadDefault(@Nonnull String url, @Nullable Map<String, String> params, @Nullable FilePart[] files, boolean post)
	{
		return download(new DefaultHttpClient(), url, params, files, post);
	}

	@SuppressWarnings({ "resource"})
	private static @Nullable NetInputStream executeRequest(@Nonnull DefaultHttpClient client, HttpUriRequest request)
	{
		InputStream is = null;
		int count = 3;
		while(is == null && count > 0)
		{
			try
			{
				--count;
				is = client.execute(request).getEntity().getContent();
			}
			catch(Throwable e)
			{
				Log.i("Can't download data from " + request.getURI() + (count == 0 ? "" : ". Try again")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
		return is == null ? null : NetInputStream.create(is);
	}

	public static @Nullable NetInputStream download(@Nonnull DefaultHttpClient client,
			@Nonnull String url, @Nullable Map<String, String> params, @Nullable FilePart[] files,
			boolean post)
	{
		client.setParams(HTTP_PARAMS);

		try
		{
			HttpUriRequest request;
			
			if(post)
			{
				HttpPost r = new HttpPost(url);
				if(params != null)
				{
					fillHttpUriRequest(r, params);
				}
				
				if(files != null)
				{
					fillHttpUriRequestByFiles(r, files);
				}
				request = r;
			}
			else
			{
				request = new HttpGet(url);
			}
			
			return executeRequest(client, request);
		}
		catch(WtfException e)
		{
			Log.w("Something wrong with creating HttpUriRequest", e); //$NON-NLS-1$
			return null;
		}
	}

	private static void fillHttpUriRequest(@Nonnull HttpPost request,
			@Nonnull Map<String, String> params) throws WtfException
	{
		try
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			Set<Map.Entry<String, String>> entries = params.entrySet();
			
			for(Map.Entry<String, String> entry : entries)
			{
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			
		    request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		}
		catch (UnsupportedEncodingException e)
		{
			Log.w("Cant fill PostRequest by params from params-map", e); //$NON-NLS-1$
			throw new WtfException("Cant fill PostRequest by params from params-map", e); //$NON-NLS-1$
		}
	}
	
	private static void fillHttpUriRequestByFiles(@Nonnull HttpPost request,
	@Nonnull FilePart[] files)
	{
		MultipartEntity mpe = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		for(FilePart file : files)
		{
			mpe.addPart(file.partName, new ByteArrayBody(file.bytes, file.fileName));
		}
		request.setEntity(mpe);
	}

	private static @Nonnull HttpParams createHttpConnectionParams()
	{
		int socketOperationTimeout = 5000;
		BasicHttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, socketOperationTimeout);
		HttpConnectionParams.setSoTimeout(params, socketOperationTimeout);
		return params;
	}
	

	private static class HttpsClient extends DefaultHttpClient
	{
		private final @Nonnull Resources res;
		private final int storeId;

		public HttpsClient(@Nonnull Resources res, int storeId) 
		{
			this.res = res;
			this.storeId = storeId;
		}

		//==== DefaultHttpClient ==================================================
		@Override
		protected ClientConnectionManager createClientConnectionManager()
		{
			try
			{
				SchemeRegistry registry = new SchemeRegistry();
				
				InputStream is = res.openRawResource(storeId);
				if(is == null)
					throw new NullPointerException("InputStream is = res.openRawResource(storeId); was null"); //$NON-NLS-1$

				try
				{
					registry.register(new Scheme(PROTOCOL_HTTP, PlainSocketFactory.getSocketFactory(), 80));
					registry.register(new Scheme(PROTOCOL_HTTPS, createSslSocketFactory(is), 443));
				}
				finally
				{
					is.close();
				}
	
				return new SingleClientConnManager(getParams(), registry);
			}
			catch (Exception e)
			{
				throw new AssertionError(e);
			}
		}

		private static @Nonnull SSLSocketFactory createSslSocketFactory(@Nonnull InputStream is)
				throws KeyStoreException, CertificateException, NoSuchAlgorithmException, 
					IOException, UnrecoverableKeyException, KeyManagementException
		{
			KeyStore trusted = KeyStore.getInstance("BKS"); //$NON-NLS-1$
			trusted.load(is, "ez24get".toCharArray()); //$NON-NLS-1$
			return new SSLSocketFactory(trusted);
		}
	}

	private static class WtfException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public WtfException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}
