package com.ltst.przwrd.navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.crashlytics.android.Crashlytics;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.SharedPreferencesHelper;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.crossword.model.HintsModel;
import com.ltst.przwrd.crossword.model.IPuzzleSetModel;
import com.ltst.przwrd.crossword.model.PuzzleSetModel;
import com.ltst.przwrd.crossword.view.CrosswordsFragment;
import com.ltst.przwrd.crossword.view.ICrosswordsFragment;
import com.ltst.przwrd.invitefriends.view.InviteFriendsFragment;
import com.ltst.przwrd.login.model.IUserDataModel;
import com.ltst.przwrd.login.model.UserData;
import com.ltst.przwrd.login.model.UserDataModel;
import com.ltst.przwrd.login.model.UserProvider;
import com.ltst.przwrd.login.view.AuthorizationFragment;
import com.ltst.przwrd.login.view.ForgetPassFragment;
import com.ltst.przwrd.login.view.IAutorization;
import com.ltst.przwrd.login.view.LoginFragment;
import com.ltst.przwrd.login.view.RegisterFragment;
import com.ltst.przwrd.login.view.ResetPassFragment;
import com.ltst.przwrd.login.view.RulesActivity;
import com.ltst.przwrd.login.view.SocialLoginActivity;
import com.ltst.przwrd.manadges.BillingV3Activity;
import com.ltst.przwrd.manadges.PurchasePrizeWord;
import com.ltst.przwrd.push.GcmHelper;
import com.ltst.przwrd.rating.view.RatingFragment;
import com.ltst.przwrd.rest.RestParams;
import com.ltst.przwrd.score.UploadScoreQueueModel;
import com.ltst.przwrd.scoredetail.view.ScoreDetailFragment;
import com.ltst.przwrd.sounds.SoundsWork;
import com.ltst.przwrd.splashscreen.SplashScreenFragment;
import com.ltst.przwrd.tools.BitmapAsyncTask;
import com.ltst.przwrd.tools.ChoiceImageSourceHolder;
import com.ltst.przwrd.tools.DimenTools;
import com.ltst.przwrd.tools.ErrorAlertDialog;
import com.ltst.przwrd.tools.IBitmapAsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static com.ltst.przwrd.tools.RequestAnswerCodes.*;

public class NavigationActivity extends BillingV3Activity
        implements
        IFragmentsHolderActivity,
        INavigationDrawerHolder,
        IAutorization,
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        IReloadUserData,
        IBitmapAsyncTask,
        INavigationActivity
{
    public static final @Nonnull String LOG_TAG = "prizeword";

    private final static @Nonnull String CURENT_POSITION = "currentPosition";
    private final static @Nonnull String STATE_CHECK_VKONTAKE = "stateCheckVkontakte";
    private final static @Nonnull String STATE_CHECK_FACEBOOK = "stateCheckFacebook";
    private @Nonnull Context mContext;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull IPuzzleSetModel mPuzzleSetModel;

    private @Nonnull ChoiceImageSourceHolder mDrawerChoiceDialog;
    private @Nonnull SlidingMenu mSlidingMenu;
    private @Nonnull MainMenuHolder mDrawerMenu;
    private @Nullable ViewGroup mTabletMenuViewGroup;

    private @Nonnull List<NavigationDrawerItem> mDrawerItems;
    private @Nonnull FragmentManager mFragmentManager;
    private @Nonnull SparseArrayCompat<Fragment> mFragments;

    private int mCurrentSelectedFragmentPosition = 0;

    private @Nonnull IUserDataModel mUserDataModel;
    private @Nonnull BitmapAsyncTask mBitmapAsyncTask;
    private @Nonnull String mPositionText;
    private @Nonnull String mScoreText;
    private boolean mVkSwitch = false;
    private boolean mFbSwitch = false;
    private boolean mIsDestroyed = false;
    private boolean mIsTablet = false;
    private boolean mNotificationsEnabled = true;
    private boolean mPasswordReset = false;

    private @Nullable UploadScoreQueueModel mUploadScoreQueueModel;
    private @Nullable HintsModel mHintsModel;

    private @Nonnull GcmHelper mGcmHelper;
    private boolean mNeedMerge;
    private @Nonnull String mProvider;
    private @Nonnull String mSessionKey;

    private @Nonnull List<PurchasePrizeWord> mRestoreproducts;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Crashlytics.start(this);

        mBcConnector = getBcConnector();
        mIsTablet = DimenTools.isTablet(this);
        SoundsWork.ALL_SOUNDS_FLAG = SharedPreferencesValues.getSoundSwitch(this);
        // Устанавливаем соединение с Google Play для внутренних покупок;
        mContext = this.getBaseContext();

        mGcmHelper = new GcmHelper(this, mBcConnector);
//        mGcmHelper.onCreate(savedInstanceState);

        // Устанавливаем русскую локаль для всего приложения;
        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        mContext.getResources().updateConfiguration(config, mContext.getResources().getDisplayMetrics());

        mSlidingMenu = new SlidingMenu(this);
        if (!mIsTablet)
        {
            mSlidingMenu.setMode(SlidingMenu.LEFT);
            mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            mSlidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
            mSlidingMenu.setFadeDegree(0.35f);
            mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_width);
            mSlidingMenu.setOnCloseListener(new SlidingMenu.OnCloseListener()
            {
                @Override public void onClose()
                {
                    SoundsWork.sidebarMusic(mContext);
                }
            });
            mSlidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener()
            {
                @Override public void onOpen()
                {
                    SoundsWork.sidebarMusic(mContext);
                }
            });
        }
        else
        {
            mTabletMenuViewGroup = (ViewGroup) findViewById(R.id.navigation_slider);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View vfooter = inflater.inflate(R.layout.navigation_drawer_footer_layout, null);
        assert vfooter != null;

        mFragmentManager = getSupportFragmentManager();
        mFragments = new SparseArrayCompat<Fragment>();

        mDrawerChoiceDialog = new ChoiceImageSourceHolder(this);
        mDrawerChoiceDialog.mGalleryButton.setOnClickListener(this);
        mDrawerChoiceDialog.mCameraButton.setOnClickListener(this);

        if (!mIsTablet)
        {
            mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
            mSlidingMenu.setMenu(vfooter);
        } else if (mTabletMenuViewGroup != null)
        {
            mTabletMenuViewGroup.addView(vfooter);
        }

        mDrawerMenu = new MainMenuHolder(this, vfooter);
        mDrawerMenu.mImage.setOnClickListener(this);
        mDrawerMenu.mMyCrossword.setOnClickListener(this);
        mDrawerMenu.mShowRulesBtn.setOnClickListener(this);
        mDrawerMenu.mLogoutBtn.setOnClickListener(this);
        mDrawerMenu.mVkontakteSwitcher.setOnCheckedChangeListener(this);
        mDrawerMenu.mFacebookSwitcher.setOnCheckedChangeListener(this);
        mDrawerMenu.mInviteFriendsBtn.setOnClickListener(this);
        mDrawerMenu.mRatingBtn.setOnClickListener(this);
        mDrawerMenu.mScoreBtn.setOnClickListener(this);
        mDrawerMenu.mRestoreBtn.setOnClickListener(this);

        mNotificationsEnabled = SharedPreferencesValues.getNotificationsSwitch(this);
        mDrawerMenu.mNotificationSwitcher.setChecked(mNotificationsEnabled);
        SharedPreferencesValues.setNotifications(this, mNotificationsEnabled);

        initNavigationDrawerItems();
        checkLauchingAppByLink();

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if(!mPasswordReset)
            selectNavigationFragmentByClassname(SplashScreenFragment.FRAGMENT_CLASSNAME);
    }

    @Override protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(CURENT_POSITION, mCurrentSelectedFragmentPosition);
        outState.putBoolean(STATE_CHECK_VKONTAKE, mVkSwitch);
        outState.putBoolean(STATE_CHECK_FACEBOOK, mFbSwitch);
        super.onSaveInstanceState(outState);
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {
            mVkSwitch = savedInstanceState.getBoolean(STATE_CHECK_VKONTAKE);
            mFbSwitch = savedInstanceState.getBoolean(STATE_CHECK_FACEBOOK);
        }

        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            mCurrentSelectedFragmentPosition = savedInstanceState.getInt(CURENT_POSITION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(this.onActivityResultBillingV3(requestCode, resultCode, data))
            return;

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case REQUEST_NAVIGATION_LOAD_GALARY:
                {
                    // Получаем картинку из галереи;
                    Uri chosenImageUri = data.getData();
                    Bitmap photo = null;
                    try
                    {
                        photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageUri);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    // Меняем аватарку на панеле;
                    mDrawerMenu.setImage(photo);
                    // Отправляем новую аватарку насервер;
                    mBitmapAsyncTask = new BitmapAsyncTask(this);
                    mBitmapAsyncTask.execute(photo);
                }
                break;
                case REQUEST_NAVIGATION_LOAD_FOTO:
                {
                    // получаем фото с камеры;
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    // Меняем аватарку на панеле;
                    mDrawerMenu.setImage(photo);
                    // Отправляем новую аватарку насервер;
                    mBitmapAsyncTask = new BitmapAsyncTask(this);
                    mBitmapAsyncTask.execute(photo);
                }
                break;
                case REQUEST_NAVIGATION_VKONTAKTE:
                case REQUEST_NAVIGATION_FACEBOOK:
                {
                    if(data.hasExtra(SocialLoginActivity.BF_SESSION_KEY))
                    {
                        SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(this);
//                        String sessionKey1 = spref.getString(SharedPreferencesValues.SP_SESSION_KEY, Strings.EMPTY);
                        String sessionKey2 = data.getStringExtra(SocialLoginActivity.BF_SESSION_KEY);
                        spref.putString(SharedPreferencesValues.SP_SESSION_KEY_2, sessionKey2);
                        spref.commit();

                        mNeedMerge = true;
                        mProvider = (requestCode == REQUEST_NAVIGATION_VKONTAKTE) ? RestParams.VK_PROVIDER : RestParams.FB_PROVIDER;
                    }
                    else
                    {
                        if(requestCode == REQUEST_NAVIGATION_VKONTAKTE)
                        {
                            mVkSwitch = true;
                            mDrawerMenu.mVkontakteSwitcher.setChecked(false);
                            mDrawerMenu.mVkontakteSwitcher.setEnabled(true);
                        }
                        else if(requestCode == REQUEST_NAVIGATION_FACEBOOK)
                        {
                            mFbSwitch = true;
                            mDrawerMenu.mFacebookSwitcher.setChecked(false);
                            mDrawerMenu.mFacebookSwitcher.setEnabled(true);
                        }
                    }
                }
                break;

                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mIsDestroyed = true;
        mGcmHelper.onDestroy();
    }

    @Override
    protected void onStop()
    {
        mGcmHelper.onStop();
        SoundsWork.releaseMPALL();
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        mGcmHelper.onResume();

        mSessionKey = SharedPreferencesValues.getSessionKey(this);
        mPuzzleSetModel = new PuzzleSetModel(mContext, mBcConnector, mSessionKey);
        mUserDataModel = new UserDataModel(this, mBcConnector);
        if(mNeedMerge)
        {
            mUserDataModel.setProvider(mProvider);
            mUserDataModel.mergeAccounts(mTaskHandlerMergeAccounts);
            mNeedMerge = false;
        }

        reloadUserData();
        mDrawerMenu.mNotificationSwitcher.setOnCheckedChangeListener(this);
        super.onResume();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // Если запуск первый, то стартуем обучалку;
        boolean firstLaunch = SharedPreferencesValues.getFirstLaunchFlag(mContext);
        if (firstLaunch)
        {
            @Nonnull Intent intent = RulesActivity.createIntent(this);
            this.startActivity(intent);
            SharedPreferencesValues.setFirstLaunchFlag(mContext, false);
        }
    }

    @Override
    protected void onPause()
    {
        mGcmHelper.onPause();
        mUserDataModel.close();
        mPuzzleSetModel.close();
        if (mUploadScoreQueueModel != null)
        {
            mUploadScoreQueueModel.close();
        }
        if (mHintsModel != null)
        {
            mHintsModel.close();
        }
        super.onPause();
    }

    public void initNavigationDrawerItems()
    {
        if (mDrawerItems == null)
        {
            mDrawerItems = new ArrayList<NavigationDrawerItem>();
            // login, auth fragments
            initFragmentToList(SplashScreenFragment.FRAGMENT_ID, SplashScreenFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(LoginFragment.FRAGMENT_ID, LoginFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(RegisterFragment.FRAGMENT_ID, RegisterFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(ResetPassFragment.FRAGMENT_ID, ResetPassFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(AuthorizationFragment.FRAGMENT_ID, AuthorizationFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(ForgetPassFragment.FRAGMENT_ID, ForgetPassFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(InviteFriendsFragment.FRAGMENT_ID, InviteFriendsFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(RatingFragment.FRAGMENT_ID, RatingFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(ScoreDetailFragment.FRAGMENT_ID, ScoreDetailFragment.FRAGMENT_CLASSNAME);
            // crossword
            initFragmentToList(CrosswordsFragment.FRAGMENT_ID, CrosswordsFragment.FRAGMENT_CLASSNAME);
        }
    }

    // ==== IFragmentsHolderActivity =================================

    @Override
    public void selectNavigationFragmentByPosition(int position)
    {
        if (mIsDestroyed)
            return;
        unlockDrawer();
        if (!isFragmentInitialized(position))
        {
            String classname = mDrawerItems.get(position).getFragmentClassName();
            Fragment fr = Fragment.instantiate(this, classname);
            mFragments.append(position, fr);
        }

        mCurrentSelectedFragmentPosition = position;
        Fragment fr = mFragments.get(position);

        mFragmentManager.beginTransaction()
                .replace(R.id.navigation_content_frame, fr)
                .commit();

        mSlidingMenu.showContent();
        setTitle(mDrawerItems.get(position).getTitle());
    }

    @Override
    public void selectNavigationFragmentByClassname(@Nonnull String fragmentClassname)
    {
        if (mIsDestroyed)
            return;
        int size = mDrawerItems.size();
        for (int i = 0; i < size; i++)
        {
            NavigationDrawerItem item = mDrawerItems.get(i);
            if (fragmentClassname.equals(item.getFragmentClassName()))
            {
                mCurrentSelectedFragmentPosition = i;
                selectNavigationFragmentByPosition(i);
                break;
            }
        }
    }

    @Override public String getPositionText()
    {
        return mPositionText;
    }

    @Override public String getScoreText()
    {
        return mScoreText;
    }

    @Override public boolean getFbSwitch()
    {
        return mFbSwitch;
    }

    @Override public boolean getVkSwitch()
    {
        return mVkSwitch;
    }

    @Override public boolean getIsTablet()
    {
        return mIsTablet;
    }

    // ==================================================

    private void initFragmentToList(@Nonnull String id, @Nonnull String classname)
    {
        String title = Strings.EMPTY;
        Resources res = getResources();
        if (id.equals(LoginFragment.FRAGMENT_ID))
            title = res.getString(R.string.login_fragment_title);
        else if (id.equals(AuthorizationFragment.FRAGMENT_ID))
            title = res.getString(R.string.authorization_fragment_title);
        else if (id.equals(CrosswordsFragment.FRAGMENT_ID))
            title = res.getString(R.string.crosswords_fragment_title);
        else if (id.equals(RegisterFragment.FRAGMENT_ID))
            title = res.getString(R.string.registration_fragment_title);
        else if (id.equals(ResetPassFragment.FRAGMENT_ID))
            title = res.getString(R.string.resetpass_fragment_title);
        else if (id.equals(ForgetPassFragment.FRAGMENT_ID))
            title = res.getString(R.string.forgetpass_fragment_title);
        else if (id.equals(InviteFriendsFragment.FRAGMENT_ID))
            title = res.getString(R.string.invite_fragment_title);
        else if (id.equals(RatingFragment.FRAGMENT_ID))
            title = res.getString(R.string.rating_fragment_title);
        else if (id.equals(ScoreDetailFragment.FRAGMENT_ID))
            title = res.getString(R.string.rating_fragment_title);
        else if (id.equals(SplashScreenFragment.FRAGMENT_ID))
            title = res.getString(R.string.splashscreen_fragment_title);

        if (!title.equals(Strings.EMPTY))
        {
            NavigationDrawerItem item = new NavigationDrawerItem(title, classname);
            mDrawerItems.add(item);
        }
    }

    private boolean isFragmentInitialized(int position)
    {
        return mFragments.get(position) != null;
    }

    private void checkLauchingAppByLink()
    {
        @Nullable Intent intent = getIntent();
        if (intent == null)
            return;
        if (intent.getAction() != Intent.ACTION_VIEW)
            return;

        @Nullable String url = intent.getDataString();
        if (url == null)
            return;
        URI uri = URI.create(url);
        List<NameValuePair> values = URLEncodedUtils.parse(uri, "UTF-8");
        for (NameValuePair value : values)
        {
            if (value.getName().equals(RestParams.PARAM_PARSE_TOKEN))
            {
                String passwordToken = value.getValue();
                selectNavigationFragmentByClassname(ResetPassFragment.FRAGMENT_CLASSNAME);
                ResetPassFragment fr = (ResetPassFragment) mFragments.get(mCurrentSelectedFragmentPosition);
                fr.setPasswordToken(passwordToken);
                mPasswordReset = true;
                break;
            }
        }
    }

    // ==================== BACK_PRESS ==============================

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Fragment fr = mFragments.get(mCurrentSelectedFragmentPosition);
            if (fr instanceof INavigationBackPress)
            {
                ((INavigationBackPress) fr).onBackKeyPress();
            } else
            {
                return super.onKeyDown(keyCode, event);
            }
        }
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE))
        {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    //==== INavigationDrawerHolder ==============================================

    @Override
    public void lockDrawerClosed()
    {
        if (!mIsTablet)
        {
            mSlidingMenu.showContent();
            mSlidingMenu.setSlidingEnabled(false);
        } else if (mTabletMenuViewGroup != null)
        {
            mTabletMenuViewGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void lockDrawerOpened()
    {
        if (!mIsTablet)
        {
            mSlidingMenu.showMenu();
            mSlidingMenu.setSlidingEnabled(false);
        }
        if (mTabletMenuViewGroup != null)
        {
            mTabletMenuViewGroup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void unlockDrawer()
    {
        if (!mIsTablet)
            mSlidingMenu.setSlidingEnabled(true);
    }

    @Override
    public void toogle()
    {
        if (!mIsTablet)
            mSlidingMenu.toggle();
        else
            lockDrawerOpened();
    }

//==== IAutorization ==============================================

    @Override
    public void onAuthotized()
    {
        if(mNotificationsEnabled)
        {
            mSessionKey = SharedPreferencesValues.getSessionKey(this);
            mGcmHelper.onAuthorized(mSessionKey);
            mDrawerMenu.mNotificationSwitcher.setChecked(true);
        }
//        mGcmHelper.onAuthorized(null);

//        // Если запуск первый, то стартуем обучалку;
//        boolean firstLaunch = SharedPreferencesValues.getFirstLaunchFlag(mContext);
//        if (firstLaunch)
//        {
//            @Nonnull Intent intent = RulesActivity.createIntent(mContext);
//            this.startActivityForResult(intent, REQUEST_RULES);
//            SharedPreferencesValues.setFirstLaunchFlag(mContext,false);
//        }
//        else
//        {
            reloadUserData();
            selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
//        }

        if (mIsTablet)
            lockDrawerOpened();
    }

    //==== IOnClickListeber ========

    @Override
    public void onClick(View view)
    {
        SoundsWork.interfaceBtnMusic(this);
        switch (view.getId())
        {
            case R.id.menu_mypuzzle_btn:
                selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.menu_show_rules_btn:
                @Nonnull Intent intent = RulesActivity.createIntent(this);
                this.startActivity(intent);
                break;
            case R.id.header_listview_logout_btn:
                SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(NavigationActivity.this);
                spref.erase(SharedPreferencesValues.SP_SESSION_KEY);
                spref.commit();
                SharedPreferencesValues.setFacebookToken(NavigationActivity.this, Strings.EMPTY);
                selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
                mUserDataModel.clearDataBase(null);
                mVkSwitch = false;
                mFbSwitch = false;
                break;
            case R.id.header_listview_photo_img:
                // Вызываем окно выбора источника получения фото;
                mDrawerChoiceDialog.show();
                break;
            case R.id.choice_photo_dialog_camera_btn:
                mDrawerChoiceDialog.cancel();
                // Вызываем камеру;
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_NAVIGATION_LOAD_FOTO);
                break;
            case R.id.choice_photo_dialog_gallery_btn:
                mDrawerChoiceDialog.cancel();
                // Вызываем галерею;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_NAVIGATION_LOAD_GALARY);
                break;
            case R.id.menu_invite_friends_btn:
                selectNavigationFragmentByClassname(InviteFriendsFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.menu_pride_rating_btn:
                selectNavigationFragmentByClassname(RatingFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.menu_pride_score_btn:
                selectNavigationFragmentByClassname(ScoreDetailFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.restore_buy:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.question_restore_purchases_title);
                builder.setMessage(R.string.question_restore_purchases_msg);
                builder.setPositiveButton(R.string.ok_bnt_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Fragment fr = mFragments.get(mCurrentSelectedFragmentPosition);
                        if (fr instanceof ICrosswordsFragment)
                        {
                            ((ICrosswordsFragment) fr).waitLoader(true);
                        }
                        getManadgeHolder().restoreProducts(mTaskHandlerRestoreProducts);
                    }
                });
                builder.setNegativeButton(R.string.cancel_message_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(true);
                builder.create().show();
                break;
            default:
                break;
        }
    }

    //===== Task loading user datas ==============

    public void reloadUserData()
    {
        // загружаем данные о пользователе с сервера;
        mUserDataModel.loadUserDataFromInternet(mTaskHandlerLoadUserData);
    }

    private void reloadProviders(long user_id)
    {
        // загружаем провайдеры;
        mUserDataModel.loadProvidersFromDB(user_id, mTaskHandlerLoadProviders);
    }

    private void resetUserImage(byte[] userPic)
    {
        // изменить аватарку;
        mUserDataModel.resetUserImage(userPic, mTaskHandlerResetUserPic);
    }

    private void reloadUserImageFromServer(@Nonnull String url)
    {
        // загружаем аватарку с сервера;
        mUserDataModel.loadUserImageFromServer(url, mTaskHandlerLoadUserImageFromServer);
    }

    private void reloadUserImageFromDB(long user_id)
    {
        // загружаем аватарку из базы данных;
        mUserDataModel.loadUserImageFromDB(user_id, mTaskHandlerLoadUserImageFromServer);
    }

    private void checkSyncData()
    {
        @Nullable String sessionKey = SharedPreferencesValues.getSessionKey(this);
        if (sessionKey == null)
            return;

        mUploadScoreQueueModel = new UploadScoreQueueModel(mBcConnector, sessionKey);
        mUploadScoreQueueModel.upload();
        mHintsModel = new HintsModel(mBcConnector, sessionKey);
        final SharedPreferencesHelper mHelper = SharedPreferencesHelper.getInstance(mContext);
        int currentHintsChangeCount = mHelper.getInt(SharedPreferencesValues.SP_HINTS_TO_CHANGE, 0);
        if(currentHintsChangeCount != 0)
        {
            mHintsModel.changeHints(currentHintsChangeCount, new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    mHelper.erase(SharedPreferencesValues.SP_HINTS_TO_CHANGE);
                }
            });
        }
    }

    private IListenerVoid mTaskHandlerRestoreProducts = new IListenerVoid()
    {
        @Override
        public void handle() {
            mRestoreproducts = getManadgeHolder().getRestoreProducts();
            mTaskHandlerRestoreProducts2.handle();
        }
    };

    private IListenerVoid mTaskHandlerRestoreProducts2 = new IListenerVoid()
    {
        @Override
        public void handle() {
            if(mRestoreproducts.size()>0)
            {
                @Nonnull PurchasePrizeWord product = mRestoreproducts.get(0);
                mRestoreproducts.remove(0);
                mPuzzleSetModel.buyCrosswordSet(product.googleId, product.receipt_data, product.signature, mTaskHandlerRestoreProducts2);
                getManadgeHolder().productBuyOnServer(product.googleId);
            }
            else
            {
                Fragment fr = mFragments.get(mCurrentSelectedFragmentPosition);
                if (fr instanceof ICrosswordsFragment)
                {
                    ((ICrosswordsFragment) fr).updateAllSets();
                }
            }
        }
    };

    private IListenerVoid mTaskHandlerLoadUserData = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if (mIsDestroyed)
                return;
            UserData data = mUserDataModel.getUserData();
            if (data != null)
            {
                Fragment fr = mFragments.get(mCurrentSelectedFragmentPosition);
                if (fr instanceof ICrosswordsFragment)
                {
                    ((ICrosswordsFragment) fr).setHintCount(data.hints);
                }
                mDrawerMenu.mNickname.setText(data.name != Strings.EMPTY ? data.name + " " + data.surname : data.surname);
                mDrawerMenu.mHightRecord.setText(String.valueOf(data.highScore));
                mDrawerMenu.mScore.setText(String.valueOf(data.monthScore));
                mScoreText = String.valueOf(data.monthScore);
                mDrawerMenu.mPosition.setText(String.valueOf(data.position));
                mPositionText = String.valueOf(data.position);
                reloadProviders(data.id);
                reloadUserImageFromDB(data.id);
                reloadUserImageFromServer(data.previewUrl);
                if (mIsTablet)
                    lockDrawerOpened();
                if (mCurrentSelectedFragmentPosition != 0)
                    selectNavigationFragmentByPosition(mCurrentSelectedFragmentPosition);
                else
                {
                    checkSyncData();
                    selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                }
            }
            else
            {
                Fragment fr = mFragments.get(mCurrentSelectedFragmentPosition);
                if(!(fr instanceof ResetPassFragment)
                        && !(fr instanceof ForgetPassFragment)
                        && !(fr instanceof AuthorizationFragment)
                        && !(fr instanceof LoginFragment)
                        && !(fr instanceof RegisterFragment)
                        || !mPasswordReset)
                        {
                            mDrawerMenu.clean();
                            selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
                        }
            }
        }
    };

    private IListenerVoid mTaskHandlerLoadUserImageFromServer = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if (mIsDestroyed)
                return;

            byte[] buffer = mUserDataModel.getUserPic();
            Bitmap bitmap = null;
            if (!byte.class.isEnum() && buffer != null)
            {
                try
                {
                    bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                } catch (NullPointerException e)
                {
                    throw new RuntimeException(e);
                }
            }
            mDrawerMenu.setImage(bitmap);
        }
    };

    private IListenerVoid mTaskHandlerResetUserPic = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if (mIsDestroyed)
                return;
            UserData data = mUserDataModel.getUserData();
            if (data == null || data.previewUrl == null || data.previewUrl == Strings.EMPTY)
            {
                mDrawerMenu.setImage(null);
            } else
            {
                reloadUserImageFromServer(data.previewUrl);
            }
        }
    };

    private IListenerVoid mTaskHandlerLoadProviders = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if (mIsDestroyed)
                return;
            // Получили список провайдеров, выставляем значение свитчеров;
            @Nullable ArrayList<UserProvider> providers = mUserDataModel.getProviders();
            @Nonnull List<String> names = new ArrayList<String>();
            if (providers != null)
            {
                for (UserProvider provider : providers)
                {
                    names.add(provider.name);
                }
                if (names.contains(RestParams.VK_PROVIDER))
                {
                    mVkSwitch = true;
                    mDrawerMenu.mVkontakteSwitcher.setEnabled(false);
                    mDrawerMenu.mVkontakteSwitcher.setChecked(true);
                }
                else
                {
                    mDrawerMenu.mVkontakteSwitcher.setEnabled(true);
                    mDrawerMenu.mVkontakteSwitcher.setChecked(false);
                }
                if (names.contains(RestParams.FB_PROVIDER))
                {
                    mFbSwitch = true;
                    mDrawerMenu.mFacebookSwitcher.setEnabled(false);
                    mDrawerMenu.mFacebookSwitcher.setChecked(true);
                }
                else
                {
                    mDrawerMenu.mFacebookSwitcher.setEnabled(true);
                    mDrawerMenu.mFacebookSwitcher.setChecked(false);
                }
            }
        }
    };

    private IListenerVoid mTaskHandlerMergeAccounts = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if (mIsDestroyed)
                return;
            int statusCode = mUserDataModel.getStatusCodeAnswer();
            @Nonnull String msg = mUserDataModel.getStatusMessageAnswer();
            msg = getResources().getString(R.string.error_merge_accounts);
            @Nonnull String provider = mUserDataModel.getProvider();
            if (statusCode == RestParams.SC_SUCCESS)
            {
                if (provider == RestParams.VK_PROVIDER)
                {
                    mDrawerMenu.mVkontakteSwitcher.setEnabled(false);
                }
                if (provider == RestParams.FB_PROVIDER)
                {
                    mDrawerMenu.mFacebookSwitcher.setEnabled(false);
                }
            }
            else
            {
                if (provider == RestParams.VK_PROVIDER)
                {
                    mDrawerMenu.mVkontakteSwitcher.setChecked(false);
                }
                if (provider == RestParams.FB_PROVIDER)
                {
                    mDrawerMenu.mFacebookSwitcher.setChecked(false);
                }
                ErrorAlertDialog.showDialog(NavigationActivity.this, R.string.error_merge_accounts);
            }
        }
    };

    @Override
    public void bitmapConvertToByte(@Nullable byte[] buffer)
    {
        // Отправляем новую аватарку насервер;
        resetUserImage(buffer);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean state)
    {

        @Nonnull Intent intent;
            switch (compoundButton.getId())
            {
                case R.id.menu_vk_switcher:
                    NavigationActivity.debug("VK CHECK CHANGE, state = "+state);
                    if (state && !mVkSwitch)
                    {
                        NavigationActivity.debug("YES");
                        intent = new Intent(this, SocialLoginActivity.class);
                        intent.putExtra(SocialLoginActivity.BF_PROVEDER_ID, RestParams.VK_PROVIDER);
                        startActivityForResult(intent, REQUEST_NAVIGATION_VKONTAKTE);
                    }
                    break;
                case R.id.menu_fb_switcher:
                    NavigationActivity.debug("FB CHECK CHANGE, state = "+state);
                    if (state && !mFbSwitch)
                    {
                        NavigationActivity.debug("YES");
                        intent = new Intent(this, SocialLoginActivity.class);
                        intent.putExtra(SocialLoginActivity.BF_PROVEDER_ID, RestParams.FB_PROVIDER);
                        startActivityForResult(intent, REQUEST_NAVIGATION_FACEBOOK);
                    }
                    break;
                case R.id.menu_notification_switcher:
                    NavigationActivity.debug("PUSH CHECK CHANGE, state = "+state);
                        SharedPreferencesValues.setNotifications(this, !mNotificationsEnabled);
                        mNotificationsEnabled = !mNotificationsEnabled;
                        if(mNotificationsEnabled)
                        {
                            String sessionKey = SharedPreferencesValues.getSessionKey(this);
                            mGcmHelper.onAuthorized(sessionKey);
                        }
                        else
                        {
                            mGcmHelper.unregister();
                        }
                    break;
                default:
                    break;
            }
    }

    @Override
    public void sendMessage(@Nonnull String msg) {
        ErrorAlertDialog.showDialog(this, msg);
    }

    public static void debug(@Nonnull String msg)
    {
        Calendar cal = Calendar.getInstance();
        Log.d(LOG_TAG, String.format("%02d:%02d:%02d",cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND)) + ": " + msg);
    }

}
