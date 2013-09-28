package com.ltst.przwrd.rating.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.IBcConnectorOwner;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.navigation.IFragmentsHolderActivity;
import com.ltst.przwrd.navigation.INavigationDrawerHolder;
import com.ltst.przwrd.rating.model.UsersListModel;
import com.ltst.przwrd.sounds.SoundsWork;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.handlers.IListenerVoid;
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
    private @Nonnull IFragmentsHolderActivity mIFragmentActivity;

    private @Nonnull ListView mRatingListView;
    private @Nonnull LinearLayout mRatingContainer;
    private @Nonnull Button mMenuBtn;
    private @Nonnull LinearLayout mHeaderImage;
    private @Nonnull LinearLayout mFooterImage;

    private @Nullable UsersListModel mModel;
    private @Nullable RatingAdapter mRatingAdapter;
    private @Nullable ProgressBar mProgressBar;
    private @Nonnull TextView mPositionTV;

    @Override
    public void onAttach(Activity activity)
    {
        Log.i("RatingFragment.onAttach()"); //$NON-NLS-1$
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mINavigationDrawerHolder = (INavigationDrawerHolder) activity;
        mIFragmentActivity = (IFragmentsHolderActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i("RatingFragment.onCreateView()"); //$NON-NLS-1$
        View v = inflater.inflate(R.layout.rating_fragment_layout, container, false);
        mMenuBtn = (Button) v.findViewById(R.id.header_menu_btn);

        mHeaderImage = (LinearLayout) inflater.inflate(R.layout.rating_header, null, false);
        mFooterImage = (LinearLayout) inflater.inflate(R.layout.rating_footer, null, false);
        mPositionTV = (TextView) v.findViewById(R.id.position_in_rating);
        StringBuffer sb = new StringBuffer();
        sb.append(mIFragmentActivity.getPositionText());
        sb.append(mContext.getResources().getString(R.string.ending));
        sb.append(" ");
        sb.append(mContext.getResources().getString(R.string.in_rating));
        mPositionTV.setText(sb.toString());
        mRatingListView = (ListView) v.findViewById(R.id.rating_listview);
        mRatingListView.setDivider(null);
        mRatingListView.addHeaderView(mHeaderImage);
        mRatingListView.addFooterView(mFooterImage);

        mRatingContainer = (LinearLayout) v.findViewById(R.id.raiting_fragment_container);

        mProgressBar = (ProgressBar) v.findViewById(R.id.list_progressBar);

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
            adapter.setRefreshHandler(mRefreshHandler);
            mRatingListView.setAdapter(mRatingAdapter);
        }
    }

    @Override
    public void onResume()
    {
        Log.i("RatingFragment.onResume()"); //$NON-NLS-1$
        super.onResume();
        UsersListModel m = mModel;
        if (m != null)
        {
            m.resumeLoading();
        }
        RatingAdapter adapter = mRatingAdapter;
        if (adapter != null)
        {
            adapter.update();
        }

    }

    @Override
    public void onPause()
    {
        Log.i("RatingFragment.onPause()"); //$NON-NLS-1$
        UsersListModel m = mModel;
        if (m != null)
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
        if (model != null)
        {
            model.close();
            mModel = null;
            mRatingAdapter = null;
        }
        super.onDestroy();

    }

    @Override public void onClick(View view)
    {
        SoundsWork.interfaceBtnMusic(mContext);
        switch (view.getId())
        {
            case R.id.header_menu_btn:
                mINavigationDrawerHolder.toogle();
                break;
        }
    }

    private final @Nonnull IListenerVoid mRefreshHandler = new IListenerVoid()
    {
        @Override public void handle()
        {
            ProgressBar bar = mProgressBar;
            assert bar != null;
            bar.setVisibility(View.GONE);
            if(!BcTaskHelper.isNetworkAvailable(mContext))
            {
                Toast.makeText(mContext, NonnullableCasts.getStringOrEmpty(
                        mContext.getString(R.string.msg_no_internet)), Toast.LENGTH_LONG).show();
            }
            else
            {
                mRatingContainer.setVisibility(View.VISIBLE);
            }
        }
    };
}
