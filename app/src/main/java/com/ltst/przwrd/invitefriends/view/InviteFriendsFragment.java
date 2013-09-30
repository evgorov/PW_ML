package com.ltst.przwrd.invitefriends.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.WebDialog;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.IBcConnectorOwner;
import com.ltst.przwrd.app.SharedPreferencesHelper;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.crossword.view.OneCrosswordActivity;
import com.ltst.przwrd.invitefriends.model.InviteFriendsData;
import com.ltst.przwrd.invitefriends.model.InviteFriendsDataModel;
import com.ltst.przwrd.login.model.LoadSessionKeyTask;
import com.ltst.przwrd.login.model.SocialParser;
import com.ltst.przwrd.login.view.IInviteFriendsFragment;
import com.ltst.przwrd.navigation.IFragmentsHolderActivity;
import com.ltst.przwrd.navigation.INavigationDrawerHolder;
import com.ltst.przwrd.rest.RestParams;
import com.ltst.przwrd.sounds.SoundsWork;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ISlowSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InviteFriendsFragment extends SherlockFragment implements View.OnClickListener, IInviteFriendsFragment {
    private @Nonnull String LOG_TAG = "InviteFriends";
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.InviteFiends.view.InviteFriendsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = InviteFriendsFragment.class.getName();

    private @Nonnull android.content.Context mContext;
    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull Button mMenuBtn;
    private @Nonnull Button mInviteAllBtn;
    private @Nonnull ListView mFriendsListView;
    private @Nonnull LinearLayout mFriendsContainer;
    private @Nonnull InviteFragmentAdapter mAdapter;
    private @Nonnull INavigationDrawerHolder mINavigationDrawerHolder;
    private @Nonnull com.ltst.przwrd.navigation.IFragmentsHolderActivity mIFragmentActivity;

    private @Nonnull ImageView mHeaderImage;
    private @Nonnull ImageView mFooterImage;
    private @Nullable InviteFriendsDataModel mModel;
    private @Nullable ProgressBar mProgressBar;
    private @Nullable ViewGroup mMessageView;

    private boolean mDataRequested = false;

    private final int REQUEST_GET_FACEBOOK_TOKEN = 1;

    private @Nonnull Session mFbSession;
    private @Nonnull String mIds;
    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity) {
        Log.i(LOG_TAG, "InviteFriendsFragment.onAttach()"); //$NON-NLS-1$

        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mINavigationDrawerHolder = (INavigationDrawerHolder) activity;
        mIFragmentActivity = (IFragmentsHolderActivity) activity;
        mFbSession = new Session(activity);
        super.onAttach(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOG_TAG, "InviteFriendsFragment.onCreateView()"); //$NON-NLS-1$
        View v = inflater.inflate(R.layout.invite_friends_fragment_layout, container, false);
        mMenuBtn = (Button) v.findViewById(R.id.header_menu_btn);
        mInviteAllBtn = (Button) v.findViewById(R.id.header_invite_all_btn);
        mFriendsListView = (ListView) v.findViewById(R.id.vk_friends_listview);
        mFriendsContainer = (LinearLayout) v.findViewById(R.id.vk_friends_container);
        mHeaderImage = new ImageView(mContext);
        mFooterImage = new ImageView(mContext);
        mHeaderImage.setBackgroundResource(R.drawable.invite_vk_header);
        mFooterImage.setBackgroundResource(R.drawable.invite_footer);
        mProgressBar = (ProgressBar) v.findViewById(R.id.list_progressBar);
        mMessageView = (ViewGroup) v.findViewById(R.id.invite_message_not_social);
        mMenuBtn.setOnClickListener(this);
        mInviteAllBtn.setOnClickListener(this);

        return v;

    }

    @Override
    public void getToken() {
        @Nonnull Intent intent = new Intent(mContext, LoginFacebook.class);
        startActivityForResult(intent, REQUEST_GET_FACEBOOK_TOKEN);
    }

    @Override
    public void invite() {
        initInvite(mIds);
    }

    private void initInvite(@Nonnull String str){
        @Nonnull String token = SharedPreferencesValues.getFacebookToken(mContext);
        if (token == null) {
            getToken();
            return;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH) + 2));
            Date date = cal.getTime();
            try {
                date = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(date.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            AccessToken access_token = AccessToken.createFromExistingAccessToken(token, date, null, AccessTokenSource.TEST_USER, null);
            if (!mFbSession.isOpened())
                mFbSession.open(access_token, callback);
            Bundle params = new Bundle();
            params.putString("message", mContext.getResources().getString(R.string.invite_message_fb_text));
            params.putString("title", "PrizeWord");
            params.putString("to", str);
            showDialogWithoutNotificationBar(params, mFbSession);
        }
    }

    @Override
    public void setIdsFriends(@Nonnull String id) {
        mIds = id;
    }


    private void showDialogWithoutNotificationBar(Bundle params, Session session) {

        WebDialog requestDialog = (new WebDialog.RequestsDialogBuilder(mContext, session, params)).setOnCompleteListener(new WebDialog.OnCompleteListener() {
            @Override
            public void onComplete(Bundle values, FacebookException error) {
                if (error != null) {
                    if (error instanceof FacebookOperationCanceledException) {
                        Toast.makeText(mContext,
                                "Request cancelled",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext,
                                "Network Error",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    final String requestId = values.getString("request");
                    if (requestId != null) {
                        Toast.makeText(mContext,
                                "Request sent",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext,
                                "Request cancelled",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).build();
        requestDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_FACEBOOK_TOKEN: {
                    if (data.hasExtra(LoginFacebook.BF_FACEBOOK_TOKEN)) {
                        invite();
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG, "InviteFriendsFragment.onStart()"); //$NON-NLS-1$

        InviteFriendsDataModel model = mModel;
        if (model == null) {
            model = new InviteFriendsDataModel(mContext, mBcConnector);
            mModel = model;

            Log.i(LOG_TAG, "Create VkModel"); //$NON-NLS-1$
        }

        InviteFragmentAdapter adapter = mAdapter;
        if (adapter == null) {
            if (!BcTaskHelper.isNetworkAvailable(mContext)) {
                Toast.makeText(mContext, NonnullableCasts.getStringOrEmpty(
                        mContext.getString(R.string.msg_no_internet)), Toast.LENGTH_LONG).show();
                adapter = new InviteFragmentAdapter(mContext, this, model, true, true);
            } else {
                adapter = new InviteFragmentAdapter(mContext, this, model, mIFragmentActivity.getFbSwitch(), mIFragmentActivity.getVkSwitch());
            }
            Log.i(LOG_TAG, "create adapterVk"); //$NON-NLS-1$
            mAdapter = adapter;
            adapter.setRefreshHandler(mRefreshHandler);
            mDataRequested = true;
        }
        mFriendsListView.setAdapter(adapter);
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "InviteFriendsFragment.onResume()"); //$NON-NLS-1$
        InviteFriendsDataModel m = mModel;
        if (m != null) {
            m.resumeLoading();
            Log.i(LOG_TAG, "resume Vkloading"); //$NON-NLS-1$
        }

        InviteFragmentAdapter adapter = mAdapter;
        if (adapter != null && !mDataRequested) {
            adapter.updateByInternet();
            mDataRequested = true;
            Log.i(LOG_TAG, "update by internet"); //$NON-NLS-1$
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(LOG_TAG, "InviteFriendsFragment.onStop()"); //$NON-NLS-1$
        InviteFriendsDataModel m = mModel;
        if (m != null) {
            m.pauseLoading();
            Log.i(LOG_TAG, "Pause Loading"); //$NON-NLS-1$
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "InviteFriendsFragment.onDestroy()"); //$NON-NLS-1$
        InviteFriendsDataModel model = mModel;
        if (model != null) {
            model.close();
            Log.i(LOG_TAG, "Close model"); //$NON-NLS-1$

        }
        mModel = null;
        mAdapter = null;
        mDataRequested = false;
        super.onDestroy();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "InviteFriendsFragment.onActivityCreated()"); //$NON-NLS-1$
        mMenuBtn.setOnClickListener(this);
        mInviteAllBtn.setOnClickListener(this);
        mFriendsListView.setDivider(null);
        if (mIFragmentActivity.getVkSwitch()) {
            mFriendsListView.addHeaderView(mHeaderImage);
            mFriendsListView.addFooterView(mFooterImage);
        } else if (mIFragmentActivity.getFbSwitch())
            mFriendsListView.addFooterView(mFooterImage);
        super.onActivityCreated(savedInstanceState);
    }

    // ==== Events =================================

    @Override
    public void onClick(View view) {
        SoundsWork.interfaceBtnMusic(mContext);
        switch (view.getId()) {
            case R.id.header_menu_btn:
                mINavigationDrawerHolder.toogle();
                break;
            case R.id.header_invite_all_btn:
                inviteAll();
                break;
        }
    }

    private void inviteAll() {

        InviteFragmentAdapter adapter = mAdapter;
        ISlowSource.Item<InviteFriendsData, Bitmap> data;
        StringBuffer ids_vk = new StringBuffer();
        StringBuffer ids_fb = new StringBuffer();
        int countFbFriends = 0;
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {

                data = (ISlowSource.Item<InviteFriendsData, Bitmap>) adapter.getItem(i);
                if (!data.quick.id.equals(Strings.EMPTY) && data.quick.providerName.equals(RestParams.VK_PROVIDER)
                        && (!data.quick.status.equals("already_registered") || !data.quick.status.equals("invite_sent"))) {
                    ids_vk.append(data.quick.id);
                    ids_vk.append(',');
                } else if (!data.quick.id.equals(Strings.EMPTY) && data.quick.providerName.equals(RestParams.FB_PROVIDER)
                        && (!data.quick.status.equals("already_registered") || !data.quick.status.equals("invite_sent"))) {
                    if (countFbFriends < 50) {
                        ids_fb.append(data.quick.id);
                        ids_fb.append(',');
                        countFbFriends++;
                    }
                }
            }
            //adapter.invite(ids_vk.toString(), RestParams.VK_PROVIDER, null);
            //adapter.invite(ids_fb.toString(), RestParams.FB_PROVIDER, null);
            initInvite(ids_fb.toString());
        }
    }

    private final @Nonnull IListenerVoid mRefreshHandler = new IListenerVoid() {
        @Override
        public void handle() {
            ProgressBar bar = mProgressBar;
            assert bar != null;
            bar.setVisibility(View.GONE);
//            mFriendsListView.setVisibility(View.VISIBLE);
            if (mIFragmentActivity.getVkSwitch() || mIFragmentActivity.getFbSwitch()) {
                mFriendsContainer.setVisibility(View.VISIBLE);
                mInviteAllBtn.setEnabled(true);
            } else {
                mMessageView.setVisibility(View.VISIBLE);
                mInviteAllBtn.setEnabled(false);
            }
            mDataRequested = false;
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception excepton) {
        if (state.isOpened())
            org.omich.velo.log.Log.i("aut", "logged in");
        if (state.isClosed())
            org.omich.velo.log.Log.i("aut", "logged out");
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

}
