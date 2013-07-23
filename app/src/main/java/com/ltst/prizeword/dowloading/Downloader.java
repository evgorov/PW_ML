package com.ltst.prizeword.dowloading;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Created by cosic on 23.07.13.
 */
public class Downloader {

    static private final @Nonnull String LOG_TAG = "downloader";

    private static final @Nonnull String URL_PARAMS_DIVIDER = "&"; //$NON-NLS-1$
    private static final @Nonnull String URL_PARAMS_EQUAL 	= "="; //$NON-NLS-1$
    private static final @Nonnull String URL_PARAMS_STARTER = "?"; //$NON-NLS-1$
    private static final @Nonnull String CONTENT_ENCODING_UTF8 	= "UTF-8"; //$NON-NLS-1$

    public static byte[] download(@Nonnull String url) throws DownloaderException
    {
        return download(new DefaultHttpClient(), url);
    }

    public static byte[] download(@Nonnull DefaultHttpClient client,
                                  @Nonnull String url) throws DownloaderException
    {
        HttpRequestBase rq = new HttpGet(url);
        return executeRequest(client, rq);
    }

    public static byte[] executeRequest(@Nonnull DefaultHttpClient client,
                                        @Nonnull HttpUriRequest request) throws DownloaderException
    {
        byte [] result = new byte[0];
        HttpResponse execute;

        TryOrThrow tot = new TryOrThrow();

        while(tot.tryAgain && tot.tryCount < 3)
        {
            tot.tryAgain = false;
            try
            {
                final int SOCKET_OPERATION_TIMEOUT = 5000;
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, SOCKET_OPERATION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(httpParams, SOCKET_OPERATION_TIMEOUT);
                client.setParams(httpParams);

                execute = client.execute(request);
                Log.i(LOG_TAG, execute.getStatusLine().toString());

                InputStream content = execute.getEntity().getContent();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte [] buffer = new byte[1000];

                int count = 0;
                while ((count = content.read(buffer)) >= 0)
                {
                    bos.write(buffer, 0, count);
                }

                content.close();
                result = bos.toByteArray();
                bos.close();
            }
            //Ниже некоторые сообщения в лог идут со статусом i. Поскольку иногда возникают из-за проблем соединения.
            //А некоторые идут со статусом w, потому что с большей вероятностью являются следствием проблем
            //в программе и на сервере. Но не е, поскольку ещё не до конца уверен,
            //отчего в точности может возникать и что с этим можно сделать.
            catch (SSLPeerUnverifiedException e)
            {
                Log.w(LOG_TAG, "SSLPeerUnverifiedException", e); //$NON-NLS-1$
                tot.tryOrThrow(new DownloaderException("Unverified cert.", ErrorType.SERVER_TEMPORARY, e)); //$NON-NLS-1$
            }
            catch (SSLException e)
            {
                Log.i(LOG_TAG, "SSLException", e); //$NON-NLS-1$
                tot.tryOrThrow(new DownloaderException("SSL error, might be due to connection error", //$NON-NLS-1$
                        ErrorType.CONNECTION_ERROR, e));
            }
            catch (ConnectTimeoutException e)
            {
                Log.i(LOG_TAG, "ConnectTimeoutException", e); //$NON-NLS-1$
                tot.tryOrThrow(new DownloaderException("Timeout error", ErrorType.CONNECTION_ERROR, e)); //$NON-NLS-1$
            }
            catch (ClientProtocolException e)
            {
                Log.w(LOG_TAG, "Client protocol exception", e); //$NON-NLS-1$
                tot.tryOrThrow(new DownloaderException("ClientProtocol error", ErrorType.SERVER_TEMPORARY, e)); //$NON-NLS-1$
            }
            catch (SocketException e)
            {
                Log.i(LOG_TAG, "SocketException", e); //$NON-NLS-1$
                tot.tryOrThrow(new DownloaderException("SocketException", ErrorType.CONNECTION_ERROR, e)); //$NON-NLS-1$
            }
            catch (UnknownHostException e)
            {
                Log.i(LOG_TAG, "UnknownHostException", e); //$NON-NLS-1$
                tot.tryOrThrow(new DownloaderException("UnknownHostException", ErrorType.CONNECTION_ERROR, e)); //$NON-NLS-1$
            }
            catch (IOException e)
            {
                Log.w(LOG_TAG, "IOException on executing", e); //$NON-NLS-1$
                throw new DownloaderException("IO error", ErrorType.INTERNAL, e); //$NON-NLS-1$
            }
        }

        if(tot.tryCount > 0)
        {
            Log.i(LOG_TAG, "Try Count: " + tot.tryCount); //$NON-NLS-1$
        }

        return result;
    }

    private static class TryOrThrow
    {
        public boolean tryAgain = true;
        public int tryCount = 0;

        void tryOrThrow (DownloaderException e) throws DownloaderException
        {
            tryAgain = true;
            tryCount++;
            if(tryCount >= 3)
            {
                throw e;
            }
        }
    }

    public static enum ErrorType
    {
        SSL, //Проблема с SSL сертификатами.
        NO_INTERNET, //Устройство не подключено к интернету.
        CONNECTION_ERROR, //Не удаётся установить соединение.
        SERVER_TEMPORARY, //Сервер не даёт ответ
        INTERNAL, //Что-то непонятное.
        MEMORY; //На клиенте не хватает памяти, чтобы получить весь ответ.
    }

    public static class DownloaderException extends Exception
    {
        private static final long serialVersionUID = 1L;

        private ErrorType mType;

        public DownloaderException (String message, ErrorType type)
        {
            super(message);
            mType = type;
        }

        public DownloaderException (String message, ErrorType type,
                                    Throwable cause)
        {
            super(message, cause);
            mType = type;
        }

        public ErrorType getType ()
        {
            return mType;
        }
    }
}
