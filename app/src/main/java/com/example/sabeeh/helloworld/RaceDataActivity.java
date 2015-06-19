package com.example.sabeeh.helloworld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.example.sabeeh.helloworld.backend.AzureSyncService;
import com.example.sabeeh.helloworld.backend.FileAnalysis;
import com.example.sabeeh.helloworld.entites.swim;
import com.example.sabeeh.helloworld.entites.tbl_swim;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;



public class RaceDataActivity extends ActionBarActivity {

    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";
    StickyListHeadersAdapter adapter;
    public   Race[] dateTimes;
    //private CustomAdapter mAdapter;
    private List<Race> myRaces = new ArrayList<Race>();
    Intent settingsIntent;
    Intent feedback;

    Button analyzeButton;

    public int selectedRows = 0;
    GlobalSwimRecords global_obj=new GlobalSwimRecords();


    //----------Caldroid-------------
    private boolean undo = false;
    private CaldroidFragment caldroidFragment;
    private CaldroidFragment dialogCaldroidFragment;
    BroadcastReceiver recv;
    BroadcastReceiver recv2;
   // public List<Race> swim_array=new ArrayList<>();

    public void analyzePerformance(View view)
    {
        Intent intent = new Intent(this, AnalysisActivity.class);


        if(global_obj.selected_races.size()<2)
        {


            createAndShowDialog("Please select atleast one more race to compare with.", "Wait");
            global_obj.multiple_swim_flag = 0;
            analyzeButton.setVisibility(View.INVISIBLE);
            return;

        }
        else {


            global_obj.multiple_swim_flag = 1;
            for (int i = 0; i < global_obj.selected_rows_for_analysis; i++) {
                if (!global_obj.selected_races.get(i).analysed) {
                    createAndShowDialog("Please analyse all races first.", "Wait");
                    global_obj.multiple_swim_flag = 0;
                    analyzeButton.setVisibility(View.INVISIBLE);
                    return;
                }


            }


            startActivity(intent);
        }


        //First get races that are checked
        //Then set global varialble for number of races selected , and the flag that multiple races are selected
        // in on create method for analysis activity check the falf and call downloads for all of the races that are selected



    }

    private void setCustomResourceForDates() {
        Calendar cal = Calendar.getInstance();

        // Min date is last 7 days
        cal.add(Calendar.DATE, 2);
        Date blueDate = cal.getTime();

        // Max date is next 7 days
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 16);
        Date greenDate = cal.getTime();


        if (caldroidFragment != null) {
            caldroidFragment.setBackgroundResourceForDate(R.color.blue,
                    blueDate);
            caldroidFragment.setBackgroundResourceForDate(R.color.green,
                    greenDate);
            caldroidFragment.setTextColorForDate(R.color.white, blueDate);
            caldroidFragment.setTextColorForDate(R.color.white, greenDate);
        }
    }
    //---------------CALDROID-------------



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // swim_array=getAll();
        dateTimes=new Race[getAll().size()];

        int i = 0;
        for (swim element : getAll()) {

                DateFormat dateFormat = new SimpleDateFormat("dd/M/yy HH:mm:ss");

                String temp_date=dateFormat.format(element.time_Stamp);
                String[] date_time_string= temp_date.split(" ");
                String time=date_time_string[1];
                String date=date_time_string[0];


           dateTimes[i]=new Race(element.analysed,false,element.pool_length,Float.toString(element.duration),temp_date,element.rating,element.Local_file,element.Comment,element.Liked);


                i++;
            }
if(dateTimes.length==0)
{
    SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
    String userId = prefs.getString("uid", "undefined");


    new GlobalSwimRecords().MobileClient.getTable(tbl_swim.class).where().field("user").eq(userId).execute(new TableQueryCallback<tbl_swim>() {
        @Override
        public void onCompleted(List<tbl_swim> result, int count, Exception exception, ServiceFilterResponse response) {

            if(exception==null)
            {
                for(tbl_swim element: result)
                {
                    swim temp_obj=new swim();
                    if(element.azureflag==false) {
                        temp_obj = element.getswimObj();
                        temp_obj.save();
                    }
                    else
                    {
                        if(dateTimes.length==0)
                        {
                            temp_obj = element.getswimObj();
                            temp_obj.save();
                        }

                    }

                }
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(RaceDataActivity.this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Great")
                                .setContentText("We just finsihed downloading your previous swims from the cloud.");

                Intent resultIntent = new Intent(RaceDataActivity.this,RaceDataActivity.class);


                TaskStackBuilder stackBuilder = TaskStackBuilder.create(RaceDataActivity.this);

                stackBuilder.addParentStack(RaceDataActivity.class);

                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build());
                updateraces();
            }
            else
            {

                Log.w("Azure", exception.getMessage());

            }
        }
    });

}
   //     if(dateTimes.length!=0) {
   //         startService(new Intent(RaceDataActivity.this, AzureSyncService.class));
    //    }

        recv=new BroadcastReceiver() {
            IntentFilter intentFilter = new IntentFilter("com.example.sabeeh.helloworld.SYNC_DATA");

            @Override
            public void onReceive(Context context, Intent intent) {
                stopService(new Intent(RaceDataActivity.this, AzureSyncService.class));
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(RaceDataActivity.this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Great")
                                .setContentText("Your swims have been synced with cloud.");

                Intent resultIntent = new Intent(RaceDataActivity.this,RaceDataActivity.class);


                TaskStackBuilder stackBuilder = TaskStackBuilder.create(RaceDataActivity.this);

                stackBuilder.addParentStack(RaceDataActivity.class);

                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build());
                updateraces();
                          }
        };
        recv2=new BroadcastReceiver() {
            IntentFilter intentFilter = new IntentFilter("com.example.sabeeh.helloworld.Analyse_DATA");

            @Override
            public void onReceive(Context context, Intent intent) {
                updateraces();
                    startService(new Intent(RaceDataActivity.this, AzureSyncService.class));
                 NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(RaceDataActivity.this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Great")
                                .setContentText("Your swims have been analysed,results are ready.");

                Intent resultIntent = new Intent(RaceDataActivity.this,RaceDataActivity.class);


                TaskStackBuilder stackBuilder = TaskStackBuilder.create(RaceDataActivity.this);

                stackBuilder.addParentStack(RaceDataActivity.class);

                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(2, mBuilder.build());
                updateraces();
            }
        };

        new GlobalSwimRecords().selected_rows_for_analysis=0;
        new GlobalSwimRecords().multiple_swim_flag=0;




        for(swim element : getAll())
        {
            if(element.analysed==false) {
                FileAnalysis BaseObject = new FileAnalysis(this);
                BaseObject.UploadFileToVM(element.Local_file);
            }
        }
        setContentView(R.layout.activity_race_data);

        analyzeButton = (Button) findViewById(R.id.analyzeButton);
        feedback = new Intent(this, FeedbackActivity.class);

        adapter = new MyListAdapter(this);
        setHeaderButtonListeners();
        registerCallback();
        populateRaceData();
        populateRaceDataListView();

        settingsIntent = new Intent(this, SettingsActivity2.class);

        final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

        // Setup caldroid fragment
        // **** If you want normal CaldroidFragment, use below line ****
        caldroidFragment = new CaldroidFragment();

        // //////////////////////////////////////////////////////////////////////
        // **** This is to show customized fragment. If you want customized
        // version, uncomment below line ****
//		 caldroidFragment = new CaldroidSampleCustomFragment();

        // Setup arguments

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

            // Uncomment this to customize startDayOfWeek
            // args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
            // CaldroidFragment.TUESDAY); // Tuesday

            // Uncomment this line to use Caldroid in compact mode
            args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

            caldroidFragment.setArguments(args);
        }

        setCustomResourceForDates();

        // Attach to the activity
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, caldroidFragment);
        t.commit();

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {

                DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
                String temp_date=dateFormat.format(date).toString();
              //  String[] date_time_string= temp_date.split(" ");

                Toast.makeText(getApplicationContext(), dateFormat.format(date).toString(),

                        Toast.LENGTH_SHORT).show();
                int i =0;

               dateTimes=new Race[getAllByDate(temp_date).size()];

                    for (swim element : getAllByDate(temp_date)) {

                    dateTimes[i]=new Race(element.analysed,false,element.pool_length,Float.toString(element.duration),temp_date,element.rating,element.Local_file,element.Comment,element.Liked);

                        i++;


                }

                populateRaceData();
                populateRaceDataListView();
                adapter = new MyListAdapter(RaceDataActivity.this);

                synchronized (adapter)
                {
                    adapter.notify();
                    adapter.notifyAll();
                }
                //AHSAN
                //Parse Date and update Dataset
                //adapter.notifyDataSetChanged();

            }

            @Override
            public void onChangeMonth(int month, int year) {
                String text = "month: " + month + " year: " + year;
                /*Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();*/


            }

            @Override
            public void onLongClickDate(Date date, View view) {
               /* Toast.makeText(getApplicationContext(),
                        "Long click " + formatter.format(date),
                        Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onCaldroidViewCreated() {
                if (caldroidFragment.getLeftArrowButton() != null) {
                   /* Toast.makeText(getApplicationContext(),
                            "Caldroid view is created", Toast.LENGTH_SHORT)
                            .show();*/
                }
            }

        };

        // Setup Caldroid
        caldroidFragment.setCaldroidListener(listener);



    }


    public void registerCallback()
    {
        StickyListHeadersListView raceDataList = (StickyListHeadersListView ) findViewById(R.id.raceDataListView);
        raceDataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(RaceDataActivity.this);
                final Intent intent = new Intent(getApplicationContext(), AnalysisActivity.class);
           final   GlobalSwimRecords obj=new GlobalSwimRecords();
                obj.objectForAnalysis=getAll().get(position);
                //intent.putExtra("racedata",getAll().get(position));


//                if (view.equals(findViewById(R.id.raceLayoutShared))) {
//                    Toast.makeText(getApplicationContext(), "Share to Facebook", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Don't Share to Facebook", Toast.LENGTH_SHORT).show();
//                }

                // Include dialog.xml file
                dialog.setContentView(R.layout.race_dialog);
                // Set dialog title
                dialog.setTitle("Race Details");


                final Spinner type=(Spinner) dialog.findViewById(R.id.dialogRaceTypeValue);
                type.setSelection(obj.objectForAnalysis.rating-1);
                //type.getSelectedItem().toString();
                //Toast.makeText(RaceDataActivity.this,type.getSelectedItem().toString(),Toast.LENGTH_LONG).show();

                type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), type.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

                        obj.objectForAnalysis.rating=position+1;
                        obj.objectForAnalysis.save();
                        updateraces();

                    }


                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });



                TextView racetimevalue=(TextView) dialog.findViewById(R.id.dialogRaceTimeValue);
                racetimevalue.setText(obj.objectForAnalysis.duration+"");




                TextView racedistance=(TextView) dialog.findViewById(R.id.dialogRaceDistanceValue);
                racedistance.setText(obj.objectForAnalysis.pool_length+"");


                TextView racelaps=(TextView) dialog.findViewById(R.id.dialogRaceLapsValue);
                racelaps.setText(obj.objectForAnalysis.Laps+"");



                final TextView comments= (TextView)dialog.findViewById(R.id.commentsField);
                comments.setText(obj.objectForAnalysis.Comment);

                // set values for custom dialog components - text, image and button

                dialog.show();

                Button postButton = (Button) dialog.findViewById(R.id.postCommentButton);
                postButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //STUPID AHSAN

                        //user this as post comment

                        obj.objectForAnalysis.Comment=comments.getText().toString();

                        Toast.makeText(RaceDataActivity.this,comments.getText(),Toast.LENGTH_LONG).show();

                        obj.objectForAnalysis.save();
                        updateraces();


                    }
                });


                Button LikeButton = (Button) dialog.findViewById(R.id.likeButton);
                LikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //STUPID AHSAN

                        //user this as post comment

                        Toast.makeText(RaceDataActivity.this,"Liked",Toast.LENGTH_LONG).show();
                        obj.objectForAnalysis.Liked=true;
                           obj.objectForAnalysis.save();
                        updateraces();




                    }
                });

                Button viewGraphButton = (Button) dialog.findViewById(R.id.viewGraphButton);
                viewGraphButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                       if( new GlobalSwimRecords().objectForAnalysis.analysed==true) {
                           startActivity(intent);
                           dialog.dismiss();
                       }
                        else
                       {
                           createAndShowDialog("Please wait while we analyse your swim.","Wait");
                       }
                    }
                });

            }
        });
    }

    public void castVote(View view, Dialog dialog)
    {

    }

    public void populateRaceData()
    {

        for (int i =0;i<dateTimes.length;i++) {
            DateFormat dateFormat = new SimpleDateFormat("dd/M/yy HH:mm:ss");

            //String temp_date=dateFormat.format(element.time_Stamp);
            // 1 - can call methods of element
            myRaces.add(dateTimes[i]);
           // new Race(false,false,)
            // ...
        }
        /*myRaces.add(new Race(true,40,"00:00:00", "1/2/11 10:00",true,1));
        myRaces.add(new Race(true,30,"00:02:11", "1/2/11 10:01",true,2));
        myRaces.add(new Race(false,14,"00:30:03", "1/2/11 10:02",false,4));
        myRaces.add(new Race(true,345,"00:02:10", "2/2/11 10:03",true,1));
        myRaces.add(new Race(true,63,"00:00:00", "2/2/11 10:04",false,2));
        myRaces.add(new Race(true,92,"00:02:11", "3/2/11 10:05",true,4));
        myRaces.add(new Race(false,12,"00:30:03", "3/2/11 10:06",false,3));
        myRaces.add(new Race(true,4,"00:02:10", "3/2/11 10:07",true,3));*/
    }
    public static List<swim> getAll() {
        //return

                List<swim> temp =new Select()
                .from(swim.class)
                .execute();

        return temp;
    }

    public static List<swim> getAllByDate(String Date) {
        //return

        List<swim> temp =new Select()
                .from(swim.class).where("SwimDate=?", Date)
                .execute();



       // .where("Category = ?", category.getId())

        return temp;
    }
    private void populateRaceDataListView() {
        StickyListHeadersListView list = (StickyListHeadersListView) findViewById(R.id.raceDataListView);


        list.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_race_data, menu);
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

    private class MyListAdapter extends BaseAdapter implements StickyListHeadersAdapter
    {
        private LayoutInflater inflater;


//initialize date time here again
        public MyListAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            //dateTimes[0]= new Race(true,40,"00:00:00", "1/2/11 10:00",false,1);

             int i =0;


        }

        @Override
        public int getCount() {
            return dateTimes.length;
        }

        @Override
        public Object getItem(int position) {
            return dateTimes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override


        //This function is actually cheecking race
        public View getView(final int position, View convertView, ViewGroup parent) {
            View raceView = convertView;

            if(raceView == null)
            {
                raceView = getLayoutInflater().inflate(R.layout.race_data_layout, parent, false);
            }

            final int position_swim=position;

            //find activity
            final Race currentRace = dateTimes[position];

            //fill the view
          ImageView raceChecked = (ImageView) raceView.findViewById(R.id.raceChecked);
            TextView raceDistance = (TextView) raceView.findViewById(R.id.raceLayoutDistanceValue);
            TextView raceTime = (TextView) raceView.findViewById(R.id.raceLayoutTimeValue);
            TextView raceDate = (TextView) raceView.findViewById(R.id.raceLayoutDateTimeValue);
            CheckBox raceSelected = (CheckBox) raceView.findViewById(R.id.raceLayoutSelected);
            ImageView raceType = (ImageView) raceView.findViewById(R.id.raceLayoutType);
            ImageView raceVoteIcon = (ImageView) raceView.findViewById(R.id.raceLayoutVoteIcon);
            TextView raceVoteValue = (TextView) raceView.findViewById(R.id.raceLayoutVoteValue);
            ImageView raceDistanceIcon = (ImageView) raceView.findViewById(R.id.raceLayoutDistanceIcon);
            ImageView raceTimeIcon = (ImageView) raceView.findViewById(R.id.raceLayoutTimeIcon);
            View raceDivider = (View) raceView.findViewById(R.id.raceLayoutRowDivider);

            ImageView like = (ImageView) raceView.findViewById(R.id.raceLayoutLike);
            ImageView comment = (ImageView) raceView.findViewById(R.id.raceLayoutComment);

            if(currentRace.comment=="")
            {
                comment.setImageResource(R.drawable.icon_comment_normal);
            }
            else
            {
                comment.setImageResource(R.drawable.icon_comment_colored);
            }
            if(currentRace.like==true)
            {
                like.setImageResource(R.drawable.icon_heart_colored);
            }
            else
            {
                like.setImageResource(R.drawable.icon_heart_normal);
            }







            //swim_type_1 for type values
            //icon_heart_colored
            //icon_comment
            if(currentRace.isChecked())

                    raceChecked.setImageResource(R.drawable.icon_checked);

            else
                raceChecked.setImageResource(R.drawable.icon_unchecked);

            raceDistance.setText(currentRace.getDistance()+" m");
            raceTime.setText(currentRace.getTime());
           // raceDate.setText(currentRace.getDateTime());
            raceSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CheckBox)v).setChecked(!currentRace.isSelected());
                    currentRace.setSelected(!currentRace.isSelected());
                   // Toast.makeText(getApplicationContext(), "Swim at " + position_swim + "selected ", Toast.LENGTH_SHORT).show();


                    if(currentRace.isSelected()) {


                        selectedRows++;
                        global_obj.selected_rows_for_analysis++;

                        global_obj.selected_races.add(getAll().get(position));



                    }

                    else {
                        selectedRows--;
                        global_obj.selected_rows_for_analysis--;
                        if(position==0) {
                            global_obj.selected_races.remove(position);
                        }
                        else
                        {
                            global_obj.selected_races.remove(position % global_obj.selected_races.size());
                        }

                    }

                    if(selectedRows>0)
                        analyzeButton.setVisibility(View.VISIBLE);

                    else
                        analyzeButton.setVisibility(View.INVISIBLE);

                    synchronized(adapter)
                    {
                        adapter.notify();
                    }
                }
            });


            if(currentRace.getType()==1)
                raceType.setBackgroundResource(R.drawable.swim_type_1);
            else if(currentRace.getType()==2)
                raceType.setBackgroundResource(R.drawable.swim_type_2);
            else if(currentRace.getType()==3)
                raceType.setBackgroundResource(R.drawable.swim_type_3);
            else if(currentRace.getType()==4)
                raceType.setBackgroundResource(R.drawable.swim_type_4);

            if(currentRace.isSelected())
                raceSelected.setChecked(true);

            else
                raceSelected.setChecked(false);

            return raceView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.listview_header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.listHeaderText);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            /*
            set header text as first char in name
            String headerText = "" + countries[position].subSequence(0, 1).charAt(0);
            */

            String headerText="";
            Date temp=new Date();

            SimpleDateFormat formatter = new SimpleDateFormat("dd/M/yy", Locale.ENGLISH);
            try {
                temp = formatter.parse(dateTimes[position].getDateTime());




                String day = (String) android.text.format.DateFormat.format("dd", temp);
                String month = (String) android.text.format.DateFormat.format("MMM", temp);

                headerText = "  "+month+" "+day+" ";


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }








            holder.text.setBackgroundColor(Color.GRAY);
            holder.text.setTextColor(Color.WHITE);
            holder.text.setText(headerText);
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            //return the first character of the country as ID because this is what headers are based upon
            return dateTimes[position].getDateTime().subSequence(0, 1).charAt(0);
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            TextView text;
        }
    }

    public void search(View v)
    {
        Toast.makeText(getApplicationContext(), "Search", Toast.LENGTH_SHORT).show();

        final Dialog dialog = new Dialog(RaceDataActivity.this);

        // Include dialog.xml file
        dialog.setContentView(R.layout.search_dialog);
        // Set dialog title
        dialog.setTitle("Search");

        // set values for custom dialog components - text, image and button

        dialog.show();

        Button searchButton = (Button) dialog.findViewById(R.id.searchDialogSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //STUPID AHSAN
                Toast.makeText(getApplicationContext(), "Searching", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    public void setHeaderButtonListeners()
    {
        final Intent swimActivity = new Intent(this,HomeActivity.class);
        final Intent analysisActivity = new Intent(this, AnalysisActivity.class);

        ImageView analysisButton = (ImageView) findViewById(R.id.headerAnalysisButton);
        analysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(analysisActivity);
            }
        });

        ImageView swimButton = (ImageView) findViewById(R.id.headerSwimButton);
        swimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(swimActivity);
            }
        });

        ImageView menuButton = (ImageView) findViewById(R.id.headerMenuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_logout:
                        ClearcacheUserToken();
                        new Delete().from(swim.class).execute();
                        Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_SHORT).show();
                        SwimAppApplication myApp = (SwimAppApplication) getApplication();
                        AuthService authService = myApp.getAuthService();

                        authService.logout(true);
                        return true;

                    case R.id.action_feedback:
                        startActivity(feedback);
                        return true;

                    case R.id.action_faq:
                        Toast.makeText(getApplicationContext(), "FAQ", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.action_settings:
                        startActivity(settingsIntent);
                        return true;

                    default:
                        return false;
                }
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_actions, popup.getMenu());
        popup.show();
    }
   /* public void broadcastIntent()
    {
        Intent intent = new Intent();
        intent.setAction("com.example.sabeeh.helloworld.DOWNLOAD_DATA");
        sendBroadcast(intent);
    }*/




    protected void onResume() {
        super.onResume();


        new GlobalSwimRecords().multiple_swim_flag=0;
     //  new GlobalSwimRecords().selected_rows_for_analysis=0;
      //  new GlobalSwimRecords().selected_races=new ArrayList<swim>();
        populateRaceData();
        registerReceiver(recv,
                new IntentFilter("com.example.sabeeh.helloworld.SYNC_DATA"));
        registerReceiver(recv2,
                new IntentFilter("com.example.sabeeh.helloworld.Analyse_DATA"));
    }

    protected  void onPause()
    {
        super.onPause();
        unregisterReceiver(recv);
        unregisterReceiver(recv2);

    }

    private void createAndShowDialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RaceDataActivity.this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.create().show();
    }

public void updateraces()
{
    dateTimes=new Race[getAll().size()];

    int i = 0;
    for (swim element : getAll()) {

        DateFormat dateFormat = new SimpleDateFormat("dd/M/yy HH:mm:ss");

        String temp_date=dateFormat.format(element.time_Stamp);
        String[] date_time_string= temp_date.split(" ");
        String time=date_time_string[1];
        String date=date_time_string[0];


        dateTimes[i]=new Race(element.analysed,false,element.pool_length,Float.toString(element.duration),temp_date,element.rating,element.Local_file,element.Comment,element.Liked);


        i++;
    }

    populateRaceData();
    populateRaceDataListView();

}
    private void ClearcacheUserToken()
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, "undefined");
        editor.putString(TOKENPREF, "undefined");
        editor.commit();
    }
}



