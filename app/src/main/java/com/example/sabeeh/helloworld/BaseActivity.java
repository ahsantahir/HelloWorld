package com.example.sabeeh.helloworld;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by mac on 21/04/15.
 */

    public class BaseActivity extends Activity {

        protected AuthService mAuthService;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            SwimAppApplication myApp = (SwimAppApplication) getApplication();
            myApp.setCurrentActivity(this);
            mAuthService = myApp.getAuthService();
        }
    }


