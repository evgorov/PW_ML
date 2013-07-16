package com.ltst.prizeword.vk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltst.prizeword.R;
import com.actionbarsherlock.app.SherlockFragment;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 16.07.13.
 */
public class VkLoginFragment extends SherlockFragment {

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.vk.VkLoginFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = VkLoginFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_webview, container, false);
    }
}
