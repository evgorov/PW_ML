package com.ltst.prizeword.navigation;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.SparseArrayCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.CompoundButton;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.crashlytics.android.Crashlytics;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesHelper;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.login.model.UserProvider;
import com.ltst.prizeword.invitefriends.view.InviteFriendsFragment;
import com.ltst.prizeword.login.view.RulesFragment;
import com.ltst.prizeword.login.view.IAutorization;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.view.AuthorizationFragment;
import com.ltst.prizeword.crossword.view.CrosswordsFragment;
import com.ltst.prizeword.login.view.ForgetPassFragment;
import com.ltst.prizeword.login.view.LoginFragment;
import com.ltst.prizeword.login.view.RegisterFragment;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.login.view.ResetPassFragment;
import com.ltst.prizeword.login.model.UserDataModel;
import com.ltst.prizeword.login.view.SocialLoginActivity;
import com.ltst.prizeword.rating.view.RatingFragment;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.sounds.SoundsWork;
import com.ltst.prizeword.tools.BitmapAsyncTask;
import com.ltst.prizeword.tools.ChoiceImageSourceHolder;
import com.ltst.prizeword.tools.ErrorAlertDialog;
import com.ltst.prizeword.tools.IBitmapAsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NavigationActivity extends SherlockFragmentActivity
        implements
        IFragmentsHolderActivity,
        IBcConnectorOwner,
        INavigationDrawerHolder,
        IAutorization,
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        IReloadUserData,
        IBitmapAsyncTask
{
    public static final @Nonnull String LOG_TAG = "prizeword";

    private final int RESULT_LOAD_IMAGE = 1;
    private final int REQUEST_MAKE_PHOTO = 2;
    public final static int REQUEST_LOGIN_VK = 3;
    public final static int REQUEST_LOGIN_FB = 4;

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull Context mContext;

    private @Nonnull ChoiceImageSourceHolder mDrawerChoiceDialog;
    private @Nonnull SlidingMenu mSlidingMenu;
    private @Nonnull MainMenuHolder mDrawerMenu;

    private @Nonnull List<NavigationDrawerItem> mDrawerItems;
    private @Nonnull FragmentManager mFragmentManager;
    private @Nonnull SparseArrayCompat<Fragment> mFragments;

    private int mCurrentSelectedFragmentPosition = 0;

    private @Nonnull UserDataModel mUserDataModel;
    private @Nonnull BitmapAsyncTask mBitmapAsyncTask;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Crashlytics.start(this);

        // Устанавливаем русскую локаль для всего приложения;
        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        mContext = (Context) getBaseContext();

        mBcConnector = new BcConnector(this);
        mSlidingMenu = new SlidingMenu(this);
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
        LayoutInflater inflater = LayoutInflater.from(this);
        View vfooter = inflater.inflate(R.layout.navigation_drawer_footer_layout, null);

        mFragmentManager = getSupportFragmentManager();
        mFragments = new SparseArrayCompat<Fragment>();
        mUserDataModel = new UserDataModel(this, mBcConnector);

        mDrawerChoiceDialog = new ChoiceImageSourceHolder(this);
        mDrawerChoiceDialog.mGalleryButton.setOnClickListener(this);
        mDrawerChoiceDialog.mCameraButton.setOnClickListener(this);

        checkLauchingAppByLink();

        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setMenu(vfooter);

        mDrawerMenu = new MainMenuHolder(this, vfooter);
        mDrawerMenu.mImage.setOnClickListener(this);
        mDrawerMenu.mMyCrossword.setOnClickListener(this);
        mDrawerMenu.mShowRulesBtn.setOnClickListener(this);
        mDrawerMenu.mLogoutBtn.setOnClickListener(this);
        mDrawerMenu.mVkontakteSwitcher.setOnCheckedChangeListener(this);
        mDrawerMenu.mFacebookSwitcher.setOnCheckedChangeListener(this);
        mDrawerMenu.mNotificationSwitcher.setOnCheckedChangeListener(this);
        mDrawerMenu.mInviteFriendsBtn.setOnClickListener(this);
        mDrawerMenu.mRatingBtn.setOnClickListener(this);

        initNavigationDrawerItems();
        reloadUserData();
        SoundsWork.startBackgroundMusic(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case RESULT_LOAD_IMAGE:
                {
                    // Получаем картинку из галереи;
                    Uri chosenImageUri = data.getData();
                    Bitmap photo = null;
                    try
                    {
                        photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageUri);
                    } catch (IOException e)
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
                case REQUEST_MAKE_PHOTO:
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
                case REQUEST_LOGIN_VK:
                case REQUEST_LOGIN_FB:
                    SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(this);
                    String sessionKey1 = spref.getString(SharedPreferencesValues.SP_SESSION_KEY, Strings.EMPTY);
                    String sessionKey2 = data.getStringExtra(SocialLoginActivity.BF_SESSION_KEY);
                    mUserDataModel.setProvider(requestCode == REQUEST_LOGIN_VK ? RestParams.VK_PROVIDER : RestParams.FB_PROVIDER);
                    mUserDataModel.mergeAccounts(sessionKey1, sessionKey2, mTaskHandlerMergeAccounts);
                    break;
            }
        } else
        {
            switch (requestCode)
            {
                case REQUEST_LOGIN_VK:
                {
                    mDrawerMenu.mVkontakteSwitcher.setChecked(false);
                    mDrawerMenu.mVkontakteSwitcher.setEnabled(true);
                }
                case REQUEST_LOGIN_FB:
                {
                    mDrawerMenu.mFacebookSwitcher.setChecked(false);
                    mDrawerMenu.mFacebookSwitcher.setEnabled(true);
                }
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        SoundsWork.pauseBackgroundMusic();
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        SoundsWork.pauseBackgroundMusic();
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        SoundsWork.startBackgroundMusic(this);
        super.onResume();
    }


    public void initNavigationDrawerItems()
    {
        if (mDrawerItems == null)
        {
            mDrawerItems = new ArrayList<NavigationDrawerItem>();
            // login, auth fragments
            initFragmentToList(LoginFragment.FRAGMENT_ID, LoginFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(RegisterFragment.FRAGMENT_ID, RegisterFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(ResetPassFragment.FRAGMENT_ID, ResetPassFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(AuthorizationFragment.FRAGMENT_ID, AuthorizationFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(ForgetPassFragment.FRAGMENT_ID, ForgetPassFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(InviteFriendsFragment.FRAGMENT_ID, InviteFriendsFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(RatingFragment.FRAGMENT_ID, RatingFragment.FRAGMENT_CLASSNAME);
            // crossword
            initFragmentToList(CrosswordsFragment.FRAGMENT_ID, CrosswordsFragment.FRAGMENT_CLASSNAME);
        }
    }

    // ==== IFragmentsHolderActivity =================================

    @Override
    public void selectNavigationFragmentByPosition(int position)
    {
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
                break;
            }
        }
    }

    //==== IBcConnectorOwner ==============================================

    @Nonnull
    @Override
    public IBcConnector getBcConnector()
    {
        return mBcConnector;
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
        return true;
    }

    //==== INavigationDrawerHolder ==============================================

    @Override
    public void lockDrawerClosed()
    {
        mSlidingMenu.showContent();
        mSlidingMenu.setSlidingEnabled(false);
    }

    @Override
    public void lockDrawerOpened()
    {
        mSlidingMenu.showMenu();
        mSlidingMenu.setSlidingEnabled(false);
    }

    @Override
    public void unlockDrawer()
    {
        mSlidingMenu.setSlidingEnabled(true);
    }

    @Override
    public void toogle()
    {
        mSlidingMenu.toggle();
    }

//==== IAutorization ==============================================

    @Override
    public void onAuthotized()
    {
        reloadUserData();
    }

    //==== IOnClickListeber ========

    @Override
    public void onClick(View view)
    {
        SoundsWork.menuBtnMusic(this);
        switch (view.getId())
        {
            case R.id.menu_mypuzzle_btn:
                selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.menu_show_rules_btn:
                @Nonnull Intent intent = RulesFragment.createIntent(this);
                this.startActivity(intent);
                break;
            case R.id.header_listview_logout_btn:
                mUserDataModel.clearDataBase(mTaskHandlerClearDataBase);
                break;
            case R.id.header_listview_photo_img:
                // Вызываем окно выбора источника получения фото;
                mDrawerChoiceDialog.show();
                break;
            case R.id.choice_photo_dialog_camera_btn:
                mDrawerChoiceDialog.cancel();
                // Вызываем камеру;
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_MAKE_PHOTO);
                break;
            case R.id.choice_photo_dialog_gallery_btn:
                mDrawerChoiceDialog.cancel();
                // Вызываем галерею;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
            case R.id.menu_invite_friends_btn:
                selectNavigationFragmentByClassname(InviteFriendsFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.menu_pride_rating_btn:
                selectNavigationFragmentByClassname(RatingFragment.FRAGMENT_CLASSNAME);
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

    private IListenerVoid mTaskHandlerLoadUserData = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            UserData data = mUserDataModel.getUserData();
            if (data != null)
            {
                mDrawerMenu.mNickname.setText(data.name != Strings.EMPTY ? data.name + " " + data.surname : data.surname);
                mDrawerMenu.mHightRecord.setText(String.valueOf(data.highScore));
                mDrawerMenu.mScore.setText(String.valueOf(data.monthScore));
                mDrawerMenu.mPosition.setText(String.valueOf(data.position));
                reloadProviders(data.id);
                reloadUserImageFromDB(data.id);
                reloadUserImageFromServer(data.previewUrl);
                selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
            } else
            {
                mDrawerMenu.clean();
                selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
            }
        }
    };

    private IListenerVoid mTaskHandlerLoadUserImageFromServer = new IListenerVoid()
    {
        @Override
        public void handle()
        {
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
                    mDrawerMenu.mVkontakteSwitcher.setEnabled(false);
                    mDrawerMenu.mVkontakteSwitcher.setChecked(true);
                } else
                {
                    mDrawerMenu.mVkontakteSwitcher.setEnabled(true);
                    mDrawerMenu.mVkontakteSwitcher.setChecked(false);
                }
                if (names.contains(RestParams.FB_PROVIDER))
                {
                    mDrawerMenu.mFacebookSwitcher.setEnabled(false);
                    mDrawerMenu.mFacebookSwitcher.setChecked(true);
                } else
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
            } else
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

    private IListenerVoid mTaskHandlerClearDataBase = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(NavigationActivity.this);
            spref.putString(SharedPreferencesValues.SP_SESSION_KEY, Strings.EMPTY);
            spref.commit();
            selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
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
        if (state)
        {
            switch (compoundButton.getId())
            {
                case R.id.menu_vk_switcher:
                    if (mDrawerMenu.mVkontakteSwitcher.isEnabled())
                    {
                        intent = new Intent(this, SocialLoginActivity.class);
                        intent.putExtra(SocialLoginActivity.BF_PROVEDER_ID, RestParams.VK_PROVIDER);
                        startActivityForResult(intent, REQUEST_LOGIN_VK);
                    }
                    break;
                case R.id.menu_fb_switcher:
                    if (mDrawerMenu.mFacebookSwitcher.isEnabled())
                    {
                        intent = new Intent(this, SocialLoginActivity.class);
                        intent.putExtra(SocialLoginActivity.BF_PROVEDER_ID, RestParams.FB_PROVIDER);
                        startActivityForResult(intent, REQUEST_LOGIN_FB);
                    }
                    break;
                case R.id.menu_notification_switcher:
                    break;
                default:
                    break;
            }
        } else
        {
            switch (compoundButton.getId())
            {
                case R.id.menu_vk_switcher:
                    break;
                case R.id.menu_fb_switcher:
                    break;
                case R.id.menu_notification_switcher:
                    break;
                default:
                    break;
            }
        }

    }
}
