package com.ltst.prizeword.login;

import com.ltst.prizeword.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.authorization.AuthoFragment;
import com.ltst.prizeword.navigation.NavigationActivity;

import javax.annotation.Nonnull;

import static android.view.View.OnClickListener;

/**
 * Created by naghtarr on 7/11/13.
 */
public class LoginFragment extends SherlockFragment implements OnClickListener {
    public static final
    @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.login.LoginFragment";
    public static final
    @Nonnull
    String FRAGMENT_CLASSNAME = LoginFragment.class.getName();

    private
    @Nonnull
    FragmentManager mFragmentManager;

    private
    @Nonnull
    AuthoFragment fr;
    private
    @Nonnull
    Button mAuthorization;

    public interface onSomeEventListener {
        public void someEvent(AuthoFragment frag);
    }
    onSomeEventListener someEventListener;

    public void OnAttach(Activity activity){
        super.onAttach(activity);
        try{someEventListener=(onSomeEventListener)activity;
        }catch(ClassCastException e){
            throw new ClassCastException((activity).toString()+"must ipmplement onsomeeventlistener");

        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_fragment_layout, container, false);
        mAuthorization = (Button) v.findViewById(R.id.enter);
        fr  = new AuthoFragment();

        return v;
    }

    public void onActivityCreated(Bundle saveInstanceState) {
        mAuthorization.setOnClickListener(this);
        super.onActivityCreated(saveInstanceState);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enter:
                someEventListener.someEvent(fr);
                break;
            default:
                break;
        }
    }
}
