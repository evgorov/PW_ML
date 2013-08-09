package com.ltst.prizeword.login.view;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InviteFriendsFragment extends SherlockFragment implements View.OnClickListener
{
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.login.view.InviteFriendsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = InviteFriendsFragment.class.getName();

    private @Nonnull android.content.Context mContext;

    private @Nonnull Button mMenuBtn;
    private @Nonnull Button mInviteAllBtn;
    private @Nonnull ListView mFbListView;
    private @Nonnull ListView mVkListView;
    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity)
    {
        mContext = (Context) activity;

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.invate_friends_fragment_layout, container, false);
        mMenuBtn = (Button) v.findViewById(R.id.header_menu_btn);
        mInviteAllBtn = (Button) v.findViewById(R.id.header_invite_all_btn);
        mFbListView = (ListView)v.findViewById(R.id.fb_friends_listview);
        mVkListView = (ListView)v.findViewById(R.id.vk_friends_listview);
        return v;
    }

    @Override
    public void onResume()
    {

        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        mMenuBtn.setOnClickListener(this);
        mInviteAllBtn.setOnClickListener(this);
        /*mFbListView.setAdapter();
        mVkListView.setAdapter();*/
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
}
