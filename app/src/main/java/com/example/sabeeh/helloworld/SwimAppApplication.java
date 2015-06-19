package com.example.sabeeh.helloworld;

import android.app.Activity;

/**
 * Created by mac on 21/04/15.
 */
public class SwimAppApplication extends com.activeandroid.app.Application {
    private AuthService mAuthService;
    private Activity mCurrentActivity;

    public SwimAppApplication() {}

    public AuthService getAuthService() {
        if (mAuthService == null) {
            mAuthService = new AuthService(this);
        }
        return mAuthService;
    }

    public void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }
}


