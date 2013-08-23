package com.ltst.prizeword.rating.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;
import com.ltst.prizeword.rating.model.UsersListModel;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RatingFragment extends SherlockFragment implements View.OnClickListener
{
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.rating.view.RatingFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RatingFragment.class.getName();

    private @Nullable Context mContext;
    private @Nullable IBcConnector mBcConnector;
    private @Nullable String mSessionKey;
    private @Nonnull INavigationDrawerHolder mINavigationDrawerHolder;

    private @Nonnull ListView mRatingListView;
    private @Nonnull Button mMenuBtn;
    private @Nonnull ImageView mHeaderImage;
    private @Nonnull ImageView mFooterImage;

    private @Nullable UsersListModel mModel;
    private @Nullable RatingAdapter mRatingAdapter;

    @Override
    public void onAttach(Activity activity)
    {
        Log.i("RatingFragment.onAttach()"); //$NON-NLS-1$
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mINavigationDrawerHolder = (INavigationDrawerHolder) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i("RatingFragment.onCreateView()"); //$NON-NLS-1$
        View v = inflater.inflate(R.layout.rating_fragment_layout, container, false);
        mMenuBtn = (Button) v.findViewById(R.id.header_menu_btn);

        mHeaderImage =  new ImageView(mContext);
        mFooterImage = new ImageView(mContext);
        mHeaderImage.setBackgroundResource(R.drawable.rating_header);
        mFooterImage.setBackgroundResource(R.drawable.rating_footer);

        mRatingListView = (ListView)v.findViewById(R.id.rating_listview);
        mRatingListView.setDivider(null);
        mRatingListView.addHeaderView(mHeaderImage);
        mRatingListView.addFooterView(mFooterImage);

        mMenuBtn.setOnClickListener(this);

        return v;
    }

    @Override
    public void onStart()
    {
        Log.i("RatingFragment.onStart()"); //$NON-NLS-1$
        mMenuBtn.setOnClickListener(this);
        initListView();
        super.onStart();
    }

    private void initListView()
    {
        Log.i("RatingFragment.initListView()"); //$NON-NLS-1$
        assert mContext != null && mBcConnector != null && mSessionKey != null
                : "Fragment must be attached to activity. Context, BcConnector, SessionKey must be initialized";

        UsersListModel model = mModel;
        if (model == null)
        {
            model = new UsersListModel(mSessionKey, mBcConnector);
            mModel = model;
        }

        RatingAdapter adapter = mRatingAdapter;
        if (adapter == null)
        {
            adapter = new RatingAdapter(mContext, mModel);
            mRatingAdapter = adapter;
            mRatingListView.setAdapter(mRatingAdapter);
        }
    }

    @Override
    public void onResume()
    {
        Log.i("RatingFragment.onResume()"); //$NON-NLS-1$
        super.onResume();
        UsersListModel m = mModel;
        if(m != null)
        {
            m.resumeLoading();
        }
        RatingAdapter adapter = mRatingAdapter;
        if(adapter != null)
        {
            adapter.update();
        }
    }

    @Override
    public void onPause()
    {
        Log.i("RatingFragment.onPause()"); //$NON-NLS-1$
        UsersListModel m = mModel;
        if(m != null)
        {
            m.pauseLoading();
        }
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        Log.i("RatingFragment.onDestroy()"); //$NON-NLS-1$
        UsersListModel model = mModel;
        if(model != null)
        {
            model.close();
            mModel = null;
            mRatingAdapter = null;
        }
        super.onDestroy();

    }

    @Override public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.header_menu_btn:
                mINavigationDrawerHolder.toogle();
                break;
        }
    }
}
