package com.example.sabeeh.helloworld.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.example.sabeeh.helloworld.RaceDataActivity;
import com.example.sabeeh.helloworld.entites.swim;
import com.example.sabeeh.helloworld.entites.tbl_swim;
import com.example.sabeeh.helloworld.entites.tbl_swim_all;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 18/04/15.
 */
public class AzureDownloadService extends Service {
    public static final String SHAREDPREFFILE = "temp";
    private final IBinder mBinder = new LocalBinder();

    private NotificationManager mNM;



    private List<swim> UserSwims= new ArrayList<swim>();
    private MobileServiceClient mClient;
    public static int SyncFlag=0;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public class LocalBinder extends Binder {
        AzureDownloadService getService() {
            return AzureDownloadService.this;
        }
    }

    public void onCreate()
    {



        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        //   showNotification();




    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        if (isNetworkConnected(getApplicationContext()))
        {
            GetLocalRecordsForSync();
            SyncRecords();





        }



        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(0);

        // Tell the user we stopped.
        if(SyncFlag==1)
            Toast.makeText(this, "Sync Completed.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Sync Aborted.", Toast.LENGTH_SHORT).show();
    }

    public void SyncRecords()
    {

        if(UserSwims.size()!=0) {



            tbl_swim_all temp_all = new tbl_swim_all();
            temp_all.all_swims=new tbl_swim[UserSwims.size()];
            for(int i=0;i<UserSwims.size();i++) {

                temp_all.all_swims[i]=new tbl_swim();
                if(UserSwims.get(i).AzureFlag==false&&UserSwims.get(i).analysed==true) {
                    temp_all.all_swims[i].getAzureObj(UserSwims.get(i));
                    swim temp_obj = getSwim(temp_all.all_swims[i].Local_file);
                    temp_obj.AzureFlag = true;

                    temp_obj.save();
                }
            }







        }
        else
        {
            SyncFlag = 1;
            broadcastIntent();
        }


    }
    public void broadcastIntent()
    {
        Intent intent = new Intent();
        intent.setAction("com.example.sabeeh.helloworld.SYNC_DATA");
        getApplicationContext().sendBroadcast(intent);
    }
    public void GetLocalRecordsForSync()
    {
        for (swim element : getAll()) {
            if(element.AzureFlag==false&&element.analysed==true)
                this.UserSwims.add(element);
        }
    }
    private boolean isNetworkConnected(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        //  CharSequence text = getText(R.string.local_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(0, "Sync completed",
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RaceDataActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "SwimApp",
                "Sync Completed", contentIntent);

        // Send the notification.
        mNM.notify(0, notification);

    }
    public static List<swim> getAll() {
        //return

        List<swim> temp =new Select()
                .from(swim.class)
                .execute();

        return temp;
    }
    public static swim getSwim(String local_file) {
        return new Select()
                .from(swim.class)
                .where("Local_file = ?",local_file)
                .executeSingle();
    }
}




