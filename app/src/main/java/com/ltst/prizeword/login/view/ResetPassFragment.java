package com.ltst.prizeword.login.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.rest.RestParams;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResetPassFragment extends SherlockFragment
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.ResetPassFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ResetPassFragment.class.getName();

    private static @Nullable String mPassedUrl;
    private static @Nullable String mPasswordToken;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.resetpass_fragment_layout, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    public void setUrl(@Nonnull String url)
    {
        mPassedUrl = url;
        parseToken();
    }

    private void parseToken()
    {
        if (mPassedUrl == null)
        {
            return;
        }

        URI uri = URI.create(mPassedUrl);
        List<NameValuePair> values = URLEncodedUtils.parse(uri, "UTF-8");
        for (NameValuePair value : values)
        {
            if(value.getName().equals(RestParams.PARAM_PARSE_TOKEN))
            {
                mPasswordToken = value.getValue();
                break;
            }
        }
    }
}
