package com.ltst.prizeword.navigation;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesHelper;
import com.ltst.prizeword.app.SharedPreferencesValues;
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
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.tools.BitmapTools;
import com.ltst.prizeword.tools.Files;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NavigationActivity extends SherlockFragmentActivity
        implements INavigationDrawerActivity<NavigationDrawerItem>,
        IFragmentsHolderActivity,
        IBcConnectorOwner,
        INavigationDrawerHolder,
        IAutorization,
        View.OnClickListener
{

    public static final @Nonnull String LOG_TAG = "prizeword";

    private int RESULT_LOAD_IMAGE = 1;
    private int REQUEST_MAKE_PHOTO = 2;

    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull Dialog mDrawerChoiceDialog;
    private @Nonnull DrawerLayout mDrawerLayout;
    private @Nonnull ListView mDrawerList;
    private @Nonnull HeaderHolder mDrawerHeader;
    private @Nonnull NavigationDrawerListAdapter mDrawerAdapter;

    private @Nonnull List<NavigationDrawerItem> mDrawerItems;

    private @Nonnull FragmentManager mFragmentManager;
    private @Nonnull SparseArrayCompat<Fragment> mFragments;

    private int mCurrentSelectedFragmentPosition = 0;

    private @Nonnull BitmapTools mBitMapTools;

    private @Nonnull
    UserDataModel mUserDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mBcConnector = new BcConnector(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.nagivation_drawer_list);
        View v = getLayoutInflater().inflate(R.layout.header_listview, null);
        mDrawerHeader = new HeaderHolder(v);
        mDrawerHeader.imgPhoto.setOnClickListener(this);
        mDrawerList.addHeaderView(v);
        mDrawerAdapter = new NavigationDrawerListAdapter(this);
        mDrawerList.setAdapter(mDrawerAdapter);
        mFragmentManager = getSupportFragmentManager();
        mFragments = new SparseArrayCompat<Fragment>();
        mUserDataModel = new UserDataModel(this,mBcConnector);
        mBitMapTools = new BitmapTools();

        Resources res = getResources();
        mDrawerChoiceDialog = new Dialog(this);
        mDrawerChoiceDialog.setContentView(R.layout.choice_photo_dialog_layout);
        mDrawerChoiceDialog.setTitle(res.getString(R.string.choice_source));
        ((Button) mDrawerChoiceDialog.findViewById(R.id.choice_photo_dialog_camera_btn)).setOnClickListener(this);
        ((Button) mDrawerChoiceDialog.findViewById(R.id.choice_photo_dialog_gallery_btn)).setOnClickListener(this);
        mDrawerChoiceDialog.getWindow().setLayout((int) res.getDimension(R.dimen.choise_photo_dialog_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);

        checkLauchingAppByLink();
//        selectNavigationFragmentByPosition(mCurrentSelectedFragmentPosition);
        selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
//            // Получаем картинку из галереи;
            Uri chosenImageUri = data.getData();
            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Меняем аватарку на панеле;
             mDrawerHeader.setImage(photo);
            // Отправляем новую аватарку насервер;
            mBitMapTools.convertBitmapToBytearray(photo, mTaskConvertBitmap);
//            mDrawerHeader.pbLoading.setVisibility(ProgressBar.VISIBLE);
        }
        if(requestCode == REQUEST_MAKE_PHOTO && resultCode == RESULT_OK){
            // получаем фото с камеры;
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // Меняем аватарку на панеле;
            mDrawerHeader.setImage(photo);
            // Отправляем новую аватарку насервер;
            mBitMapTools.convertBitmapToBytearray(photo, mTaskConvertBitmap);
//            mDrawerHeader.pbLoading.setVisibility(ProgressBar.VISIBLE);
        }
    }

    @Override
    protected void onDestroy()
    {
        SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(this);
        spref.putString(SharedPreferencesValues.SP_SESSION_KEY, Strings.EMPTY);
        spref.commit();
        super.onDestroy();
    }

    // ==== INavigationDrawerActivity =================================

    @Nonnull
    @Override
    public Context getContext()
    {
        return this;
    }

    @Override
    public int getDrawerItemResourceId()
    {
        return R.layout.navigation_drawer_item;
    }

    @Override
    public int getDrawerItemTextViewResourceId()
    {
        return R.id.navigation_drawer_textview;
    }

    @Nonnull
    @Override
    public IListenerInt getDrawerItemClickHandler()
    {
        return new IListenerInt()
        {
            @Override
            public void handle(int i)
            {
                selectNavigationFragmentByPosition(i);
            }
        };
    }

    @Nonnull
    @Override
    public List<NavigationDrawerItem> getNavigationDrawerItems()
    {
        if(mDrawerItems == null)
        {
            mDrawerItems = new ArrayList<NavigationDrawerItem>();
            // login, auth fragments
            initFragmentToList(LoginFragment.FRAGMENT_ID,  LoginFragment.FRAGMENT_CLASSNAME, false);
            initFragmentToList(RegisterFragment.FRAGMENT_ID, RegisterFragment.FRAGMENT_CLASSNAME, true);
            initFragmentToList(ResetPassFragment.FRAGMENT_ID, ResetPassFragment.FRAGMENT_CLASSNAME, true);
            initFragmentToList(AuthorizationFragment.FRAGMENT_ID, AuthorizationFragment.FRAGMENT_CLASSNAME, true);
            initFragmentToList(ForgetPassFragment.FRAGMENT_ID, ForgetPassFragment.FRAGMENT_CLASSNAME, true);

            // crossword
            initFragmentToList(CrosswordsFragment.FRAGMENT_ID, CrosswordsFragment.FRAGMENT_CLASSNAME, false);
        }
        return mDrawerItems;
    }

    // ==== IFragmentsHolderActivity =================================

    @Override
    public void selectNavigationFragmentByPosition(int position)
    {
        unlockDrawer();
        if(!isFragmentInitialized(position))
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

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mDrawerItems.get(position).getTitle());
    }

    @Override
    public void selectNavigationFragmentByClassname(@Nonnull String fragmentClassname)
    {
        int size = mDrawerItems.size();
        for (int i = 0; i < size; i++)
        {
            NavigationDrawerItem item = mDrawerItems.get(i);
            if(fragmentClassname.equals(item.getFragmentClassName()))
            {
                mCurrentSelectedFragmentPosition = i;
                selectNavigationFragmentByPosition(i);
                break;
            }
        }
    }

    // ==================================================

    private void initFragmentToList(@Nonnull String id, @Nonnull String classname, boolean hidden)
    {
        String title = Strings.EMPTY;
        Resources res = getResources();
        if(id.equals(LoginFragment.FRAGMENT_ID))
            title = res.getString(R.string.login_fragment_title);
        else if(id.equals(CrosswordsFragment.FRAGMENT_ID))
            title = res.getString(R.string.crosswords_fragment_title);
        else if(id.equals(AuthorizationFragment.FRAGMENT_ID))
            title = res.getString(R.string.authorization_fragment_title);
        else if(id.equals(RegisterFragment.FRAGMENT_ID))
            title = res.getString(R.string.registration_fragment_title);
        else if(id.equals(ResetPassFragment.FRAGMENT_ID))
            title = res.getString(R.string.resetpass_fragment_title);
        else if(id.equals(ForgetPassFragment.FRAGMENT_ID))
            title = res.getString(R.string.forgetpass_fragment_title);

        if(!title.equals(Strings.EMPTY))
        {
            NavigationDrawerItem item = new NavigationDrawerItem(title, classname, hidden);
            mDrawerItems.add(item);
        }
    }

    private boolean isFragmentInitialized(int position)
    {
        return mFragments.get(position) != null;
    }

    private void checkLauchingAppByLink()
    {
        @Nullable Intent intent  = getIntent();
        if (intent == null)
            return;
        if (intent.getAction() != Intent.ACTION_VIEW)
            return;

        @Nullable String url = intent.getDataString();
        if(url == null)
            return;
        URI uri = URI.create(url);
        List<NameValuePair> values = URLEncodedUtils.parse(uri, "UTF-8");
        for (NameValuePair value : values)
        {
            if(value.getName().equals(RestParams.PARAM_PARSE_TOKEN))
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Fragment fr = mFragments.get(mCurrentSelectedFragmentPosition);
            if(fr instanceof INavigationBackPress){
                ((INavigationBackPress)fr).onBackKeyPress();
            }
            else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }

    //==== INavigationDrawerHolder ==============================================

    @Override
    public void lockDrawerClosed()
    {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void lockDrawerOpened()
    {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
    }

    @Override
    public void unlockDrawer()
    {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    //==== IAutorization ==============================================

    @Override
    public void onAutotized() {
        loadUserData();
    }

    //==== IOnClickListeber ========

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.header_listview_photo_img:
                // Вызываем окно выбора источника получения фото;
                mDrawerChoiceDialog.show();
                break;
            case R.id.choice_photo_dialog_camera_btn:
                mDrawerChoiceDialog.cancel();
                // Вызываем камеру;
                Intent cameraIntent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_MAKE_PHOTO);
                break;
            case R.id.choice_photo_dialog_gallery_btn:
                // Вызываем галерею;
                mDrawerChoiceDialog.cancel();
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
            default:
                break;
        }
    }

    //===== Task loading user datas ==============

    private void loadUserData(){
        // загружаем данные о пользователе с сервера;
        mUserDataModel.loadUserData(mTaskHandlerLoadUserData);
    }

    private void resetUserData(byte[] userPic){
        // изменить аватарку;
        mUserDataModel.resetUserPic(userPic, mTaskHandlerResetUserPic);

    }

    private void resetUserData(@Nonnull String userName){
        // изменить имя пользователя;
        mUserDataModel.resetUserName(userName, mTaskHandlerResetUserName);
    }


    private void loadAvatar(@Nonnull String url){
        // загружаем аватарку с сервера;
        mUserDataModel.loadUserPic(url, mTaskHandlerLoadUserPic);
    }

    private IListenerVoid mTaskHandlerLoadUserData = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            UserData data = mUserDataModel.getUserData();
            if( data != null ){
                mDrawerHeader.tvNickname.setText(data.name != Strings.EMPTY ? data.name +" "+data.surname : data.surname);
                mDrawerHeader.tvPoints.setText(String.valueOf(data.highScore));
                loadAvatar(data.previewUrl);
            } else {
                mDrawerHeader.clean();
            }
        }
    };

    private IListenerVoid mTaskHandlerLoadUserPic = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            byte[] buffer = mUserDataModel.getUserPic();
            Bitmap bitmap = null;
            if(!byte.class.isEnum() && buffer != null){
                try
                {
                    bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                }
                catch (NullPointerException e){
                    throw new RuntimeException(e);
                }
            }
            mDrawerHeader.setImage(bitmap);
        }
    };

    private IListenerVoid mTaskHandlerResetUserPic = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            UserData data = mUserDataModel.getUserData();
            if( data != null ){
                loadAvatar(data.previewUrl);
            } else {
                mDrawerHeader.setImage(null);
            }
        }
    };

    private IListenerVoid mTaskHandlerResetUserName = new IListenerVoid()
    {
        @Override
        public void handle()
        {
        }
    };

    private IListenerVoid mTaskConvertBitmap = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    // Отправляем новую аватарку насервер;
                    byte[] userPic = mBitMapTools.getBuffer();
                    resetUserData(userPic);
//                    mDrawerHeader.pbLoading.setVisibility(ProgressBar.GONE);
                }
            });
        }
    };
}
