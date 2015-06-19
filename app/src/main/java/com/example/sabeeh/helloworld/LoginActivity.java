package com.example.sabeeh.helloworld;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.example.sabeeh.helloworld.entites.swim;
import com.facebook.AppEventsLogger;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponseCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;

import java.text.Normalizer;

public class LoginActivity extends ActionBarActivity {
    private MobileServiceTable<swim> mswim;

    public static MobileServiceClient mClient;
    private ProgressBar mProgressBar;
    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";
    public boolean bAuthenticating = false;
    public final Object mAuthenticationLock = new Object();
    ProgressDialog progress;
   static int login_completion_flag=0;

    EditText mTxtPassword;
    EditText mTxtUsername;
AuthService mAuthService;

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActiveAndroid.initialize(this);
        try {

            mClient = new MobileServiceClient(
                    "https://swimapp.azure-mobile.net/",
                    "lZdVyrXNsLlYbzbNYmeMVKEuCzHSAs69",

                    this).withFilter(new ProgressFilter());
            GlobalSwimRecords obj=new GlobalSwimRecords();
            obj.MobileClient=mClient;



            // swim temp=new swim();



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if (loadUserTokenCache(mClient)) {

            //createTable();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else {

            setContentView(R.layout.activity_login);
            SwimAppApplication myApp = (SwimAppApplication) getApplicationContext();
            myApp.setCurrentActivity(this);
            mAuthService = myApp.getAuthService();

            mTxtPassword = (EditText) findViewById(R.id.PasswordTextField);
            mTxtUsername = (EditText) findViewById(R.id.UsernameTextField);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void Login_Facebook(View view)
    {

        authenticate_facebook();
    }
    public void Login_Google(View view)
    {

        authenticate_google();
    }

    public void Login_Twitter(View view)
    {

        authenticate_twitter();
    }


    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT)
                .show();

           if(login_completion_flag==1)
        {
            createAndShowDialog("Sorry We cant log you in","Oops!");
            login_completion_flag=0;
        }
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private boolean isNetworkConnected(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }



    private class ProgressFilter implements ServiceFilter {

        @Override
        public void handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback,
                                  final ServiceFilterResponseCallback responseCallback) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            nextServiceFilterCallback.onNext(request, new ServiceFilterResponseCallback() {

                @Override
                public void onResponse(ServiceFilterResponse response, Exception exception) {

                   // SwimAppApplication myApp = (SwimAppApplication) getApplicationContext();
                   // Activity currentActivity = myApp.getCurrentActivity();




                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    if (responseCallback != null) {
                        responseCallback.onResponse(response, exception);


                    }
                }
            });
        }
    }



// Service providers
private void authenticate_facebook() {
    // Login using the Google provider.

    // TODO Auto-generated method stub


    if (loadUserTokenCache(mClient)) {

        //createTable();

        // If we failed to load a token cache, login and create a token cache



    /*    tbl_swim azureobj = new tbl_swim();

        // azureobj.getAzureObj(temp);

        tbl_swim[] arr=new tbl_swim[2];
        arr[0]=azureobj;
        arr[1]=azureobj;


        tbl_swim_all temp_all=new tbl_swim_all();
        temp_all.all_swims=arr;

        mClient.getTable(tbl_swim_all.class).insert(temp_all, new TableOperationCallback<tbl_swim_all>() {
            public void onCompleted(tbl_swim_all entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.i("Debug", "status:"+entity.status);
                    Log.d("Azure", "Insertion complete");
                    //azure_check = true;

                    // Insert succeeded
                } else {
                    Log.w("Azure", exception.getMessage());
                }
            }
        });*/






        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    else {
        mClient.login(MobileServiceAuthenticationProvider.Facebook,new UserAuthenticationCallback() {



                    @Override
                    public void onCompleted(MobileServiceUser user, Exception exception,
                                            ServiceFilterResponse response) {


                      //  synchronized (mAuthenticationLock) {
                            if (exception == null) {

                                cacheUserToken(mClient.getCurrentUser());

                                Log.w("Azure Connectivity", "Success");
                                Log.w("Facebook ", "You are now logged in " + user.getUserId());
                                //Do further processing of the app from here

                               // createAndShowDialog(user,"Logged In");
                                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);


                            } else {
                                Log.w("Azure Connectivity", "Failure");
                                Log.w("Facebook Exception", exception.getMessage());
                                createAndShowDialog("Exception",exception.getMessage());
                                // createAndShowDialog("You must log in.  Login required", "Error");
                            }



                     //       bAuthenticating=false;
                     //       mAuthenticationLock.notifyAll();


                    //    }

                    }
                }



        );

    }
}
    private void authenticate_google() {
        // Login using the Google provider.

        // TODO Auto-generated method stub



        if (loadUserTokenCache(mClient)) {

            //createTable();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        // If we failed to load a token cache, login and create a token cache
        else {
            mClient.login(MobileServiceAuthenticationProvider.Google,new UserAuthenticationCallback() {



                        @Override
                        public void onCompleted(MobileServiceUser user, Exception exception,
                                                ServiceFilterResponse response) {


                            //  synchronized (mAuthenticationLock) {
                            if (exception == null) {

                                cacheUserToken(mClient.getCurrentUser());

                                Log.w("Azure Connectivity", "Success");
                                Log.w("Facebook ", "You are now logged in " + user.getUserId());
                                //Do further processing of the app from here

                                // createAndShowDialog(user,"Logged In");
                                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);


                            } else {
                                Log.w("Azure Connectivity", "Failure");
                                Log.w("Facebook Exception", exception.getMessage());
                                createAndShowDialog("Exception",exception.getMessage());
                                // createAndShowDialog("You must log in.  Login required", "Error");
                            }



                            //       bAuthenticating=false;
                            //       mAuthenticationLock.notifyAll();


                            //    }

                        }
                    }



            );

        }
    }
    private void authenticate_twitter() {
        // Login using the Google provider.

        // TODO Auto-generated method stub

        bAuthenticating = true;

        if (loadUserTokenCache(mClient)) {

            //createTable();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        // If we failed to load a token cache, login and create a token cache
        else {
            mClient.login(MobileServiceAuthenticationProvider.Twitter,new UserAuthenticationCallback() {



                        @Override
                        public void onCompleted(MobileServiceUser user, Exception exception,
                                                ServiceFilterResponse response) {


                            //  synchronized (mAuthenticationLock) {
                            if (exception == null) {

                                cacheUserToken(mClient.getCurrentUser());

                                Log.w("Azure Connectivity", "Success");
                                Log.w("Facebook ", "You are now logged in " + user.getUserId());
                                //Do further processing of the app from here

                                // createAndShowDialog(user,"Logged In");
                                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);


                            } else {
                                Log.w("Azure Connectivity", "Failure");
                                Log.w("Facebook Exception", exception.getMessage());
                                createAndShowDialog("Exception",exception.getMessage());
                                // createAndShowDialog("You must log in.  Login required", "Error");
                            }



                            //       bAuthenticating=false;
                            //       mAuthenticationLock.notifyAll();


                            //    }

                        }
                    }



            );

        }
    }
/*
   private void createTable() {

        // Get the Mobile Service Table instance to use
        mswim = mClient.getTable(swim.class);

        //mswim = (EditText) findViewById(R.id.textNewToDo);

        // Create an adapter to bind the items with the view
       // mAdapter = new ToDoItemAdapter(this, R.layout.row_list_to_do);
        ListView listViewToDo = (ListView) findViewById(R.id.listViewToDo);
        listViewToDo.setAdapter(mAdapter);

        // Load the items from the Mobile Service
        refreshItemsFromTable();
    }
*/

    private void ClearcacheUserToken(MobileServiceUser user)
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, "undefined");
        editor.putString(TOKENPREF, "undefined");
        editor.commit();
    }
    private void cacheUserToken(MobileServiceUser user)
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, user.getUserId());
        editor.putString(TOKENPREF, user.getAuthenticationToken());
        editor.commit();
    }
    private boolean loadUserTokenCache(MobileServiceClient client)
    {

        String userId="";
        String token="";
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        if(prefs.getString(USERIDPREF,"undefined")!=null) {
            userId = Normalizer.normalize(prefs.getString(USERIDPREF, "undefined"), Normalizer.Form.NFC);
        }
        /*String provider = userId.substring(0, userId.indexOf(":"));
        if (provider.equals("Custom")) {
            userId=userId.substring(userId.indexOf(":")+1,userId.length());
        }*/
        if (userId.equals("undefined"))
            return false;
        if(prefs.getString(TOKENPREF,"undefined")!=null) {
            token = Normalizer.normalize(prefs.getString(TOKENPREF, "undefined"), Normalizer.Form.NFC);
           // token=prefs.getString(TOKENPREF,"undefined");
        }
        if (token.equals("undefined"))
            return false;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        client.setCurrentUser(user);

        return true;
    }
    /**
     * Detects if authentication is in progress and waits for it to complete.
     * Returns true if authentication was detected as in progress. False otherwise.
     */
    public boolean detectAndWaitForAuthentication()
    {
        boolean detected = false;
        synchronized(mAuthenticationLock)
        {
            do
            {
                if (bAuthenticating == true)
                    detected = true;
                try
                {
                    mAuthenticationLock.wait(1000);
                }
                catch(InterruptedException e)
                {}
            }
            while(bAuthenticating == true);
        }
        if (bAuthenticating == true)
            return true;

        return detected;
    }

    /**
     * Waits for authentication to complete then adds or updates the token
     * in the X-ZUMO-AUTH request header.
     *
     * @param request
     *            The request that receives the updated token.
     */
    private void waitAndUpdateRequestToken(ServiceFilterRequest request)
    {
        MobileServiceUser user = null;
        if (detectAndWaitForAuthentication())
        {
            user = mClient.getCurrentUser();
            if (user != null)
            {
                request.removeHeader("X-ZUMO-AUTH");
                request.addHeader("X-ZUMO-AUTH", user.getAuthenticationToken());
            }
        }
    }







    /**
     * The RefreshTokenCacheFilter class filters responses for HTTP status code 401.
     * When 401 is encountered, the filter calls the authenticate method on the
     * UI thread. Out going requests and retries are blocked during authentication.
     * Once authentication is complete, the token cache is updated and
     * any blocked request will receive the X-ZUMO-AUTH header added or updated to
     * that request.
     */
    /*
    private class RefreshTokenCacheFilter implements ServiceFilter {

        AtomicBoolean mAtomicAuthenticatingFlag = new AtomicBoolean();

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(
                final ServiceFilterRequest request,
                final NextServiceFilterCallback nextServiceFilterCallback
        )
        {
            // In this example, if authentication is already in progress we block the request
            // until authentication is complete to avoid unnecessary authentications as
            // a result of HTTP status code 401.
            // If authentication was detected, add the token to the request.
            waitAndUpdateRequestToken(request);

            // Send the request down the filter chain
            // retrying up to 5 times on 401 response codes.
            ListenableFuture<ServiceFilterResponse> future = null;
            ServiceFilterResponse response = null;
            int responseCode = 401;
            for (int i = 0; (i < 5 ) && (responseCode == 401); i++)
            {
                future = nextServiceFilterCallback.onNext(request);


                try {

                    response = future.get();
                    responseCode = response.getStatus().getStatusCode();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    if (e.getCause().getClass() == MobileServiceException.class)
                    {
                        MobileServiceException mEx = (MobileServiceException) e.getCause();
                        responseCode = response.getStatus().getStatusCode();
                        if (responseCode == 401)
                        {
                            // Two simultaneous requests from independent threads could get HTTP status 401.
                            // Protecting against that right here so multiple authentication requests are
                            // not setup to run on the UI thread.
                            // We only want to authenticate once. Requests should just wait and retry
                            // with the new token.
                            if (mAtomicAuthenticatingFlag.compareAndSet(false, true))
                            {
                                // Authenticate on UI thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Force a token refresh during authentication.
                                        authenticate(true);
                                    }
                                });
                            }

                            // Wait for authentication to complete then update the token in the request.
                            waitAndUpdateRequestToken(request);
                            mAtomicAuthenticatingFlag.set(false);
                        }
                    }
                }
            }
            return future;
        }
    }












*/








    //Interface support
    private void createAndShowDialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.create().show();
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }






public void CustomLogin(View view)
{


    if(isNetworkConnected(this)) {


        if (mTxtPassword.getText().toString().equals("") ||
                mTxtUsername.getText().toString().equals("")) {
            //We're just logging this here, we should show something to the user
            Log.w("Azure", "Username or password not entered");
            return;
        }
       progress = new ProgressDialog(this);

      progress.show(this, "Please wait","Logging you in ");

        mAuthService.login(mTxtUsername.getText().toString(), mTxtPassword.getText().toString(), new TableJsonOperationCallback() {


            @Override
            public void onCompleted(JsonObject jsonObject, Exception exception,
                                    ServiceFilterResponse response) {
                if (exception == null) {
                   // login_completion_flag=1;
                    progress.dismiss();
                    //If they've registered successfully, we'll save and set the userdata and then
                    //show the logged in activity
                    mAuthService.setUserAndSaveData(jsonObject);
                    Intent loggedInIntent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(loggedInIntent);
                } else {
                   progress.dismiss();
                    Log.e("Azure", "Error loggin in: " + exception.getMessage());
                    login_completion_flag=1;
                    //createAndShowDialog(exception.getMessage(),"Oops");


                }
            }
        });



    }
    else
    {
        Toast.makeText(this, "Please connect internet to LogIn.", Toast.LENGTH_SHORT)
                .show();
    }

}


public void CustomRegister(View view)
{
    if(isNetworkConnected(this)) {
        Intent registerIntent = new Intent(getApplicationContext(), RegisterAccountActivity.class);
        startActivity(registerIntent);
    }
    else
    {
        Toast.makeText(this, "Please connect internet to sign up.", Toast.LENGTH_SHORT)
                .show();
    }
}




}
