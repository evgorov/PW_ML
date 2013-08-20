package com.ltst.prizeword.InviteFiends.view;

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
import com.ltst.prizeword.InviteFiends.model.InviteFriendsDataModel;
import com.ltst.prizeword.rest.RestParams;

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
    private @Nonnull ListView mFbListView;
    private @Nonnull ListView mVkListView;
    private @Nonnull InviteFragmentAdapter mFbAdapter;
    private @Nonnull InviteFragmentAdapter mVkAdapter;

    private @Nonnull ImageView mFbHeaderImage;
    private @Nonnull ImageView mVkHeaderImage;
    private @Nonnull ImageView mFooterImage;
    private @Nullable InviteFriendsDataModel mVkModel;
    private @Nullable InviteFriendsDataModel mFbModel;
    private boolean mDataRequested = false;
    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity)
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onAttach()"); //$NON-NLS-1$

        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        super.onAttach(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onCreateView()"); //$NON-NLS-1$
        View v = inflater.inflate(R.layout.invite_friends_fragment_layout, container, false);
        mMenuBtn = (Button) v.findViewById(R.id.header_menu_btn);
        mInviteAllBtn = (Button) v.findViewById(R.id.header_invite_all_btn);
        mFbListView = (ListView)v.findViewById(R.id.fb_friends_listview);
        mVkListView = (ListView)v.findViewById(R.id.vk_friends_listview);
        mFbHeaderImage = new ImageView(mContext);
        mVkHeaderImage = new ImageView(mContext);
        mFooterImage = new ImageView(mContext);
        mFbHeaderImage.setBackgroundResource(R.drawable.invite_fb_header);
        mVkHeaderImage.setBackgroundResource(R.drawable.invite_vk_header);
        mFooterImage.setBackgroundResource(R.drawable.invite_footer);
        return v;

    }
    @Override
    public void onStart(){
        Log.i(LOG_TAG, "InviteFriendsFragment.onStart()"); //$NON-NLS-1$

        InviteFriendsDataModel model = mVkModel;
        if(model == null)
        {
            model = new InviteFriendsDataModel(mContext,mBcConnector,RestParams.VK_PROVIDER);
            mVkModel = model;

            Log.i(LOG_TAG, "Create VkModel"); //$NON-NLS-1$
        }
        InviteFriendsDataModel model1 = mFbModel;
        if(model1 == null)
        {
            model1 = new InviteFriendsDataModel(mContext,mBcConnector,RestParams.FB_PROVIDER);
            mFbModel = model1;

            Log.i(LOG_TAG, "Create FbModel"); //$NON-NLS-1$
        }

        InviteFragmentAdapter adapterVk = mVkAdapter;
        InviteFragmentAdapter adapterFb = mFbAdapter;
        if(adapterVk == null)
        {
            adapterVk = new InviteFragmentAdapter(mContext, model);
            adapterFb = new InviteFragmentAdapter(mContext, model1);
            Log.i(LOG_TAG, "create adapterVk"); //$NON-NLS-1$
            mVkAdapter = adapterVk;
            mFbAdapter = adapterFb;
            adapterVk.setRefreshHandler(mRefreshHandler);
            adapterFb.setRefreshHandler(mRefreshHandler);
            mDataRequested = true;
        }
        mVkListView.setAdapter(adapterVk);
        mFbListView.setAdapter(adapterFb);
        super.onStart();
    }

    @Override
    public void onResume()
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onResume()"); //$NON-NLS-1$
        InviteFriendsDataModel m = mVkModel;
        if(m != null)
        {
            m.resumeLoading();
            Log.i(LOG_TAG, "resume Vkloading"); //$NON-NLS-1$
        }
        InviteFriendsDataModel m1 = mFbModel;
        if(m1 != null)
        {
            m1.resumeLoading();
            Log.i(LOG_TAG, "resume Fbloading"); //$NON-NLS-1$
        }

        InviteFragmentAdapter adapterVk = mVkAdapter;
        InviteFragmentAdapter adapterFb = mFbAdapter;
        if(adapterVk != null && !mDataRequested)
        {
            adapterVk.updateByInternet();
            adapterFb.updateByInternet();
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
        InviteFriendsDataModel m = mVkModel;
        if(m != null)
        {
            m.pauseLoading();
            Log.i(LOG_TAG, "Pause Loading"); //$NON-NLS-1$
        }
        InviteFriendsDataModel m1 = mFbModel;
        if(m1 != null)
        {
            m1.pauseLoading();
            Log.i(LOG_TAG, "Pause Loading"); //$NON-NLS-1$
        }
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onDestroy()"); //$NON-NLS-1$
        InviteFriendsDataModel model = mVkModel;
        if(model != null)
        {
            model.close();
            Log.i(LOG_TAG, "Close model"); //$NON-NLS-1$

        }
        mVkModel = null;
        InviteFriendsDataModel model1 = mFbModel;
        if(model1 != null)
        {
            model1.close();
            Log.i(LOG_TAG, "Close model"); //$NON-NLS-1$

        }
        mVkModel = null;
        mDataRequested =false;
        super.onDestroy();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.i(LOG_TAG, "InviteFriendsFragment.onActivityCreated()"); //$NON-NLS-1$
        mMenuBtn.setOnClickListener(this);
        mInviteAllBtn.setOnClickListener(this);
        mFbListView.setDivider(null);
        mVkListView.setDivider(null);
        mFbListView.addHeaderView(mFbHeaderImage);
        mVkListView.addHeaderView(mVkHeaderImage);
        mFbListView.addFooterView(mFooterImage);
        mVkListView.addFooterView(mFooterImage);

        super.onActivityCreated(savedInstanceState);
    }

    // ==== Events =================================

    @Override public void onClick(View view)
    {
        switch (view.getId()){
            case R.id.header_menu_btn:
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
