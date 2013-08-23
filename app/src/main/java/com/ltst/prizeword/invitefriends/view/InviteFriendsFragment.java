package com.ltst.prizeword.invitefriends.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.invitefriends.model.InviteFriendsDataModel;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InviteFriendsFragment extends SherlockFragment implements View.OnClickListener
{
    private @Nonnull String LOG_TAG = "InviteFriends";
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.InviteFiends.view.InviteFriendsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = InviteFriendsFragment.class.getName();

    private @Nonnull android.content.Context mContext;
    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull Button mMenuBtn;
    private @Nonnull Button mInviteAllBtn;
    private @Nonnull ListView mFriendsListView;
    private @Nonnull InviteFragmentAdapter mAdapter;
    private @Nonnull INavigationDrawerHolder mINavigationDrawerHolder;

    private @Nonnull ImageView mHeaderImage;
    private @Nonnull ImageView mFooterImage;
    private @Nullable InviteFriendsDataModel mModel;
    private boolean mDataRequested = false;
    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity)
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onAttach()"); //$NON-NLS-1$

        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mINavigationDrawerHolder = (INavigationDrawerHolder) activity;
        super.onAttach(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onCreateView()"); //$NON-NLS-1$
        View v = inflater.inflate(R.layout.invite_friends_fragment_layout, container, false);
        mMenuBtn = (Button) v.findViewById(R.id.header_menu_btn);
        mInviteAllBtn = (Button) v.findViewById(R.id.header_invite_all_btn);
        mFriendsListView = (ListView)v.findViewById(R.id.vk_friends_listview);
        mHeaderImage = new ImageView(mContext);
        mFooterImage = new ImageView(mContext);
        mHeaderImage.setBackgroundResource(R.drawable.invite_vk_header);
        mFooterImage.setBackgroundResource(R.drawable.invite_footer);
        mMenuBtn.setOnClickListener(this);
        return v;

    }
    @Override
    public void onStart(){
        Log.i(LOG_TAG, "InviteFriendsFragment.onStart()"); //$NON-NLS-1$

        InviteFriendsDataModel model = mModel;
        if(model == null)
        {
            model = new InviteFriendsDataModel(mContext,mBcConnector);
            mModel = model;

            Log.i(LOG_TAG, "Create VkModel"); //$NON-NLS-1$
        }

        InviteFragmentAdapter adapter = mAdapter;
        if(adapter == null)
        {
            adapter = new InviteFragmentAdapter(mContext, model);
            Log.i(LOG_TAG, "create adapterVk"); //$NON-NLS-1$
            mAdapter = adapter;
            adapter.setRefreshHandler(mRefreshHandler);
            mDataRequested = true;
        }
        mFriendsListView.setAdapter(adapter);
        super.onStart();
    }

    @Override
    public void onResume()
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onResume()"); //$NON-NLS-1$
        InviteFriendsDataModel m = mModel;
        if(m != null)
        {
            m.resumeLoading();
            Log.i(LOG_TAG, "resume Vkloading"); //$NON-NLS-1$
        }

        InviteFragmentAdapter adapter = mAdapter;
        if(adapter != null && !mDataRequested)
        {
            adapter.updateByInternet();
            mDataRequested = true;
            Log.i(LOG_TAG, "update by internet"); //$NON-NLS-1$
        }
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
    @Override
    public void onStop()
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onStop()"); //$NON-NLS-1$
        InviteFriendsDataModel m = mModel;
        if(m != null)
        {
            m.pauseLoading();
            Log.i(LOG_TAG, "Pause Loading"); //$NON-NLS-1$
        }
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onDestroy()"); //$NON-NLS-1$
        InviteFriendsDataModel model = mModel;
        if(model != null)
        {
            model.close();
            Log.i(LOG_TAG, "Close model"); //$NON-NLS-1$

        }
        mModel = null;
        mAdapter = null;
        mDataRequested =false;
        super.onDestroy();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onActivityCreated()"); //$NON-NLS-1$
        mMenuBtn.setOnClickListener(this);
        mInviteAllBtn.setOnClickListener(this);
        mFriendsListView.setDivider(null);
        mFriendsListView.addHeaderView(mHeaderImage);
        mFriendsListView.addFooterView(mFooterImage);
        super.onActivityCreated(savedInstanceState);
    }

    // ==== Events =================================

    @Override public void onClick(View view)
    {
        switch (view.getId()){
            case R.id.header_menu_btn:
                mINavigationDrawerHolder.toogle();
                break;
            case R.id.header_invite_all_btn:
                break;
        }
    }

    private final @Nonnull IListenerVoid mRefreshHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mDataRequested = false;
        }
    };
}
