package com.ltst.prizeword.tools;

import android.os.Bundle;

public interface IActivityLifeCycle
{
    public void onCreate(Bundle savedInstanceState);
    public void onResume();
    public void onPause();
    public void onStop();
    public void onDestroy();
}
