package com.ltst.prizeword;

import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.lists.ISlowSource;
import org.omich.velo.log.Log;
import org.omich.velo.sherlock.ForResultSherlockActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LauncherActivity extends ForResultSherlockActivity
{
    private @Nonnull TextView mTextView;
    private @Nonnull IBcConnector mBcConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        mTextView = (TextView) this.findViewById(R.id.textView);
        mTextView.setText("sample");
        Log.i("SAMPLE TAG", "Empty:"+Strings.EMPTY);
    }

}
