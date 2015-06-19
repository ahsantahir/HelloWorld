package com.example.sabeeh.helloworld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.activeandroid.query.Delete;
import com.example.sabeeh.helloworld.entites.swim;
import com.example.sabeeh.helloworld.sensor.GlobalVariables;
import com.example.sabeeh.helloworld.sensor.ServiceForConfig;
import com.example.sabeeh.helloworld.sensor.ServiceRecOnOff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;


public class HomeActivity extends ActionBarActivity implements SensorEventListener {

    Intent analysisActivity, settingsIntent;
    Intent feedback;

    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";
    //Massimo Varialbes//
    private String DoveSono = "(SensorActivity) ";	// "DoveSono" = "where_I_am"
    static final String LOG_TAG = "NUOT_APP";

    // listener for the sensors:
    private SensorManager mgr_activity;
    private List<Sensor> sensorList;

    // variable used of the screen is off, to not accumulate a lot of Toast then later, when the screen will turned on, the user will have to see
    private int displayOnOff = 0;

    // this flag is used in order to not have the possibility to start two times the service of the recording phase (is put =1 when the user starts listen phase and =0 when it is stopped)
    public int FlagServiceSS = 0;

    // variables used for the start/stop of the recording and to change color to the button of used for the recording
    private static char g_r;					//	g = green, r= red
    private static int tipo;					//	type of button
    private ToggleButton StartStopService;

    // variables for the sensors:
    private int flag_disp_sensor = 1;		// flag used to activate the visualization of the 9 sensor's cells (in the main activity). These 9 cells not will be displayed in the finally App view.
    /* private TextView Accel_x;
     private TextView Accel_y;
     private TextView Accel_z;
     private TextView Gyro_x;
     private TextView Gyro_y;
     private TextView Gyro_z;
     private TextView Magnetic_x;
     private TextView Magnetic_y;
     private TextView Magnetic_z;
 */
    // variable used to limit the number of update of sensors on the 9 cells.
    private int contaAggiornamentiSensGyro 	= 0;
    private int contaAggiornamentiSensAccel = 0;
    private int contaAggiornamentiSensMagn 	= 0;
    private int modificaOgniTotMisure 		= 50;

    //variables used for save file in the internal memory (num max log e data installazione)
    private String dataInstallazione 	= "test.txt";
    private String datiConfigurazione 	= "config.txt";		// nome usato anche in "ServiceForConfig": essere sicuri che siano coerenti!

    //values for battery minimum level (is asked to the user if he wants to continue listen/record if the battery goes under this minimum level):
    double sogliaBatteria = GlobalVariables.batteryValMin;

    //number of maximum days and records that the user can does (initial idea for the distribution of the App, behavior not present in the final App version):
    private int n_max_log = 100;
    private int n_max_day = 31;

    //varaibles for the recostruction of the Date from the saved file (the one that contains the Date of installation):
    private double m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13;	// varaibles used to create the Date from the file that contains this information. Used also for the configuration file.
    long Data_tot;															// result of the Date recostruction (in the file of Data Installation)

    //general variables:
    private int Val_app;
    private int Val_app_1;
    private int contatore;
    private int segno;														//utilizzato per riportare il segno delle variabili dal file di configurazione
    int Valore_tot;
    private Button recording_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalVariables.displayOnOff=1;
        SwimAppApplication myApp = (SwimAppApplication) getApplicationContext();
        myApp.setCurrentActivity(this);
        setContentView(R.layout.activity_home);
        recording_button=(Button)findViewById(R.id.buttonStartSwim);

        feedback = new Intent(this, FeedbackActivity.class);
        feedback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setHeaderButtonListeners();
        analysisActivity = new Intent(this, RaceDataActivity.class);
        analysisActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        settingsIntent = new Intent(this, SettingsActivity2.class);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        checkNumLogData();

        // if the configuration file is not present, the service to do this file is lauched:
        checkFileConfigurazione();

        if (flag_disp_sensor != 0){
            mgr_activity = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorList = mgr_activity.getSensorList(Sensor.TYPE_ALL);
            for (Sensor sensor : sensorList) {
                mgr_activity.registerListener(this, sensor,SensorManager.SENSOR_DELAY_GAME);
            }
            Log.v(LOG_TAG, DoveSono + "Lanciato il listener dell'Activity (mgr_activity) XXX");	// 'XXX' is used during the debug mode, to analyze some particular items
        }

    }

    public void setHeaderButtonListeners()
    {
        ImageView analysisButton = (ImageView) findViewById(R.id.headerAnalysisButton);
        analysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent analysisActivity = new Intent(v.getContext(),AnalysisActivity.class);
                startActivity(analysisActivity);
            }
        });

        ImageView swimButton = (ImageView) findViewById(R.id.headerSwimButton);
        swimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
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
            Intent settingsIntent = new Intent(this, SettingsActivity2.class);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void ClearcacheUserToken()
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, "undefined");
        editor.putString(TOKENPREF, "undefined");
        editor.commit();
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

    public void startSwimming(View view)
    {
      //  Toast.makeText(getApplicationContext(), "Please fill all fields or go to FAQ for a tutorial on How To Record", Toast.LENGTH_SHORT).show();


        if(Settings.System.getInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0)==0) {
            DisplayToastLong("Please, active the airplane mode.");
            return ;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configurations");
        builder.setCancelable(false);


        final File fileMemInt = getBaseContext().getFileStreamPath(datiConfigurazione);




      //  if(recording_button.isPressed()==true)
       // {
           // StopMyService();
       // }
        if (fileMemInt.exists()){
            builder.setMessage("Start or Stop Swim.");
            builder.setNegativeButton("stop", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {




                    RelativeLayout dataHolder = (RelativeLayout) findViewById(R.id.swimScreenLayout);
                    RelativeLayout lightsHolder = (RelativeLayout) findViewById(R.id.swimScreenLightsHolder);
                    dataHolder.setVisibility(View.VISIBLE);
                    lightsHolder.setVisibility(View.INVISIBLE);
                    FlagServiceSS 	= 0;
                    tipo			= 3;
                    g_r				= 'r';
                    //changeButtonColor(g_r,tipo);
                    GlobalVariables.flagService = 0;
                    Log.v(LOG_TAG, DoveSono +"Setto a zero la Flag per il Service");
                    //stop the service:
                    StopMyService();
                }
            });
            builder.setNeutralButton("Swim", new DialogInterface.OnClickListener() {

                String vibratorService = Context.VIBRATOR_SERVICE;
                Vibrator vibrator = (Vibrator)getSystemService(vibratorService);
                @Override
                public void onClick(DialogInterface arg0, int arg1) {



                    RelativeLayout dataHolder = (RelativeLayout) findViewById(R.id.swimScreenLayout);
                    RelativeLayout lightsHolder = (RelativeLayout) findViewById(R.id.swimScreenLightsHolder);
                    dataHolder.setVisibility(View.INVISIBLE);
                    lightsHolder.setVisibility(View.VISIBLE);



                      recording_button.setPressed(true);

                    vibrator.vibrate(30);
                    //start the service of listen/record iff the number of days and records are not expired (this will not present in the final App):
                    if(GlobalVariables.ggRimanenti>0 && GlobalVariables.LogEffettuati<n_max_log){
                        if (GlobalVariables.flagAxesOfAcc == 3 &&
                                GlobalVariables.flagAxesOfGyr == 3 &&
                                GlobalVariables.flagAxesOfMag == 3
                                )
                        {
                            if (FlagServiceSS==0)
                            {
                                if(Settings.System.getInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0)==1)
                                {
                                    FlagServiceSS 	= 1;
                                    tipo			= 3;
                                    g_r				= 'g';
                                    //changeButtonColor(g_r,tipo);
                                    //the user chooses if he wants to continue record if the battery falls down under a fixed value:
                                    RichiestaPerBatteria();
                                    //launch the service iff this one is never been started (GlobalVariables.flagService==0). In this way only one service is active!
                                    if(GlobalVariables.flagService==0){
                                        GlobalVariables.flagService = 1;
                                        //the service will be started when the user click on OK button in this Dialog Window:
                                        rimaniSullaFinestra();	//the user is warned that, during the listen/record, the scrren has to remain on the present Activity.
                                    }
                                    else GlobalVariables.flagService = 1;
                                }
                                else{
                                    StartStopService.setText("OFF");
                                    //to avoid problem during the listen/record phase, the user is obliged to insert the 'Airplane' mode
                                    ModeOfflineNecessary();
                                }
                            }
                            else if(FlagServiceSS==1)
                            {
                                FlagServiceSS 	= 0;
                                tipo			= 3;
                                g_r				= 'r';
                                //changeButtonColor(g_r,tipo);
                                GlobalVariables.flagService = 0;
                                Log.v(LOG_TAG, DoveSono +"Setto a zero la Flag per il Service");
                                //stop the service:
                                StopMyService();
                            }
                        }
                        // the cellular hasn't the appropriate sensors:
                        else{
                            tipo = 3;
                            g_r	 = 'r';
                           // changeButtonColor(g_r,tipo);
                            avvisoSensoriMancanti();
                            recording_button.setEnabled(false);
                            //DisplayToastLong("Sorry you don't have required sensor on your device.");
                        }
                    }
                    else{
                        //the user is awared that the 31 days of try is expired
                        periodoProvaTerminato();
                    }










                }
            });


        }
        else {
            builder.setMessage("Please configure your Device first.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Log.v(LOG_TAG, DoveSono + "Il file di config. esiste, lo cancello."); //erase the existing config.file
                    fileMemInt.delete();
                    //DisplayToast("Cancellato FileConfigurazione dalla Mem. Interna.");
                    DisplayToast("Deleted the configuration file from the Internal Momory.");
                    //starts the sensors check for make the configuration file:
                    AvvisoCheckSensor();
                    Log.v(LOG_TAG, DoveSono + "Avvio nuovo Check per la configurazione!");

                }
            });
        }



        builder.show();







    }




    //Massimo code///
    //********************************************************************** Request for the battery (Dialog window):
    public void RichiestaPerBatteria(){
        GlobalVariables.batteryState=0;			//global variable is set to 0;
        GlobalVariables.flagSaveBattery = 0;
        Log.v( LOG_TAG, DoveSono + "Richiesta per batteria. " + "GlobalVariables.batteryState: " + GlobalVariables.batteryState);
        Log.v( LOG_TAG, DoveSono + "Richiesta per batteria. " + "GlobalVariables.flagSaveBattery: " + GlobalVariables.flagSaveBattery);
        //check battery level:
        Context context 			= getApplicationContext();
        final Intent batteryStatus 	= context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level 					= batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale 					= batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        float batteryVal 			= level/(float)scale;
        if(batteryVal<=sogliaBatteria){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Swim App:");
            builder.setCancelable(false);
            builder.setMessage("Batteria al " + batteryVal*100 + "%, sotto soglia minima (" + sogliaBatteria*100 + "%)." + '\n' + "Fermare registrazione sotto il 10%?");
            builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    GlobalVariables.flagSaveBattery = 1;
                    DisplayToastLong("Spengo la registrazione se batteria minore del 10%.");
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    GlobalVariables.flagSaveBattery = 0;
                    DisplayToastLong("Vado ad oltranza, senza curarmi della batteria!");
                }
            });
            builder.setIcon(R.drawable.ic_launcher);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
    //********************************************************************** Airplane mode is necessary (Dialog window):
    public void ModeOfflineNecessary(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Swim App:");
        builder.setCancelable(false);
        builder.setMessage("Modalit� Aereo necessaria." + '\n' + "Tieni premuto il tasto " + '\n' +"di spegnimento," + '\n' + "quindi scegli 'Modalit� Offline'.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //********************************************************************** End of the trial period (Dialog window):
    public void periodoProvaTerminato(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Swim App:");
        builder.setCancelable(false);
        builder.setMessage("Versione prova dell'applicazione terminata." + '\n' + '\n' + "Grazie della collaborazione.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //********************************************************************** Notice of absence of sensors (Dialog window):
    public void avvisoSensoriMancanti(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Swim App:");
        builder.setCancelable(false);
        builder.setMessage("This device is not able to record: absence of the necessary sensors!");
        //builder.setMessage("Cellulare non abilitato alla registrazione: mancanza dei sensori appropriati.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //********************************************************************** OnResume
    public void onResume()
    {
        super.onResume();
        Log.v(LOG_TAG, DoveSono + "Entro in onResume()");

        if (flag_disp_sensor != 0){
            //riattivo il sensor listener per vedere i valori a video:
            for (Sensor sensor : sensorList) {
                mgr_activity.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            }
            Log.v(LOG_TAG, DoveSono +"Riattivo il listener dei sensori a schermo (mgr_activity) XXX");
        }

        //screen is ON:
        GlobalVariables.displayOnOff=1;
        //notice if, when the screen was OFF, the number of registrations is expired:
        if(FlagServiceSS==1 && (GlobalVariables.LogEffettuati>=100 || GlobalVariables.ggRimanenti<=0)){
            periodoProvaTerminato();
            //stop the listen/record service:
            FlagServiceSS = 0;
            tipo=3;
            g_r='r';
            changeButtonColor(g_r,tipo);
            GlobalVariables.flagService = 0;	//setto a 0 la variabile globale a cui guarda il Service
            Log.v(LOG_TAG, DoveSono +"Setto a zero la Flag per il Service");
            // stoppo il servizio:
            StopMyService();
        }

        Log.v(LOG_TAG, DoveSono + "Esco da onResume()");
    }
    //********************************************************************** OnDestroy
    public void onDestroy()
    {
        super.onDestroy();
        Log.v(LOG_TAG, DoveSono + "Entro in onDestroy()");
        //if services are active, stop them:
        if(FlagServiceSS==1){
            //stoppo il servizio:
            FlagServiceSS = 0;
            tipo=3;
            g_r='r';
            //changeButtonColor(g_r,tipo);
            GlobalVariables.flagService = 0;	//setto a 0 la variabile globale a cui guarda il Service
            StopMyService();
            Log.v(LOG_TAG, DoveSono + "Stoppato Service ascolto sensori XXX");
            Log.v(LOG_TAG, DoveSono +"Setto a zero la Flag per il Service");
        }
        StopConfigService();
        Log.v(LOG_TAG, DoveSono + "Stoppato Service configurazione XXX");

        if (flag_disp_sensor != 0){
            mgr_activity.unregisterListener(this);
            Log.v(LOG_TAG, DoveSono + "unregister listener Activity (mgr_activity) XXX");
        }
        //update the number of record made from the user:
        if(GlobalVariables.NewLogEffettuati==1){
            try{
                FileInputStream fis = openFileInput(dataInstallazione);
                InputStreamReader isr = new InputStreamReader(fis);
                String str_refresh_date = "";
                StringBuffer buf = new StringBuffer();
                BufferedReader reader = new BufferedReader(isr);
                Log.v(LOG_TAG, DoveSono + "Stato buffer: " + buf);
                while((str_refresh_date=reader.readLine())!=null){
                    buf.append(str_refresh_date);
                    Log.v(LOG_TAG, DoveSono + "Stato buffer: " + buf);
                }
                String TempoMills = buf.substring(3, 16);
                //update the number of remaining record. This part will not be present in the final App (creo la nuova stringa (con l'inserimento ad hoc degli zeri)):
                String data_new = buf.toString();
                if(GlobalVariables.LogEffettuati<10)		data_new = "00" + String.valueOf(GlobalVariables.LogEffettuati) + TempoMills;
                else{
                    if(GlobalVariables.LogEffettuati<100) 	data_new = "0" + String.valueOf(GlobalVariables.LogEffettuati) + TempoMills;
                    else 									data_new = String.valueOf(GlobalVariables.LogEffettuati) + TempoMills;
                }

                //confronto nuovo e vecchio valore
                Log.v(LOG_TAG," Vecchia stringa salvata: " + buf);
                Log.v(LOG_TAG," Nuova stringa da salvare: " + data_new);
                //scrivo sul file il nuovo valore:
                FileOutputStream fos = openFileOutput(dataInstallazione,MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                osw.write(data_new);
                osw.flush();
                osw.close();
                Log.v(LOG_TAG, DoveSono + "Aggiornato num di registrazioni effettuate!");
            }catch(IOException e) {
                e.printStackTrace();
                Log.v(LOG_TAG, DoveSono + "Errore nell'aggiornamento del file in memoria interna!");
            }
            DisplayToast("File in Memoria Interna aggiornato!");
        }

        Log.v(LOG_TAG, DoveSono + "Esco da onDestroy()");
    }
    //********************************************************************** OnPause
    public void onPause(){
        super.onPause();
        Log.v(LOG_TAG, DoveSono + "Entro in onPause()");

        if (flag_disp_sensor != 0){
            //disactive the Listener:
            mgr_activity.unregisterListener(this);
            Log.v(LOG_TAG, DoveSono + "Rilascio il listener dell'Activity (mgr_activity) XXX");
        }
        //set the flag for the screen OFF:
        GlobalVariables.displayOnOff=0;
        Log.v(LOG_TAG, DoveSono + "Esco da onPause()");
    }
    //********************************************************************** Auto set/ask to set the airplane mode:
    // Quando svilupper� per dispositivi con sistema operativo >, allora inserir� anche questa linea di codice.
	/*
	public void isAirplaneModeOn(Context context) throws IOException{
		if(Settings.System.getInt(context.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0)==0)
			DisplayToast("Modalit� Aereo necessaria!");

		if(Settings.Globals.getInt(context.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON, 0)==0)
		DisplayToast("Modalit� Aereo necessaria!");
	}
	*/
    public void isAirplaneModeOn(){
        if(Settings.System.getInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0)==0)
            DisplayToastLong("Please, active the airplane mode.");
        //DisplayToastLong("Attivare modalit� aereo!");
    }
    //********************************************************************** The user has to remain on this activity during the listen/record (D.W.):
    public void rimaniSullaFinestra(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Swim App:");
        builder.setCancelable(false);
        builder.setMessage("During the 'listen-record' phase, the App must remain on this activity. If not, the listen\record will stopped.");
        //builder.setMessage("Quando l'ascolto � attivo, occorre rimanere su questa finestra dell'Applicazione, altrimenti verr� interrotta..");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                StartMyService();	//attivo il Service
                Log.v(LOG_TAG, DoveSono +"Lancio il service (ServiceRecOnOff)");
            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        AlertDialog alertDialog = builder.create();
        //elemento aggiuntivo da inserire se la finestra appare in un Servizio:
        //alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
    //********************************************************************** Check the file that contains the NUMBERL_OF_LOG and the DATE_OF_INSTALLATION of the App
    public void checkNumLogData(){

        //part of code used to cancel the old data\num_log (zzz=1 to cancel). In this way is possible to avoid the limit of 31 deys on one device.
        int zzz = 0;
        if(zzz==1){
            File dir 		= getFilesDir();
            File a 			= new File(dir,dataInstallazione);
            a.delete();
            Log.v(LOG_TAG, DoveSono + "Cancello il file!");
        }

        //create\check the file of date\num_log ("DataInizioUsoApp"):
        File file = getBaseContext().getFileStreamPath(dataInstallazione);
        if (!file.exists()){
            //the file not exist, I create it:
            try {
                Log.v(LOG_TAG, DoveSono + "Il file non esiste, lo creo!");
                String data 			= "000" + String.valueOf(System.currentTimeMillis()); //num log (000-100), millisecs attuali;
                FileOutputStream fos 	= openFileOutput(dataInstallazione,MODE_PRIVATE);
                OutputStreamWriter osw 	= new OutputStreamWriter(fos);
                osw.write(data);
                osw.flush();
                osw.close();
                DisplayToastLong("From today you can use this App for 31 trial days!");
                //DisplayToastLong("Da oggi hai a disposizione 31 giorni di prova!");
                GlobalVariables.ggRimanenti = 31;
                Log.v(LOG_TAG, DoveSono + "Creato file, con valore: " + data);
            }catch (IOException e) {
                e.printStackTrace();
                Log.v(LOG_TAG, DoveSono + "Errore nella creazione del file in mem interna.");
            }
        }
        else{
            //analyze the file:
            Log.v(LOG_TAG, DoveSono + "Il file esiste gi�, lo analizzo!");
            try {
                FileInputStream fis = openFileInput(dataInstallazione);
                InputStreamReader isr = new InputStreamReader(fis);
                String str_date = "";
                StringBuffer buf = new StringBuffer();
                BufferedReader reader = new BufferedReader(isr);
                Log.v(LOG_TAG, DoveSono + "Stato buffer: " + buf);
                while((str_date=reader.readLine())!=null){
                    buf.append(str_date);
                    Log.v(LOG_TAG, DoveSono + "Stato buffer: " + buf);
                }
                Log.v(LOG_TAG, DoveSono + "Stato buffer (finale): " + buf);
                //check the first three position of the file (num_log):
                char val_1 	= buf.charAt(0);
                char val_2 	= buf.charAt(1);
                char val_3 	= buf.charAt(2);
                //check the millisecs, to know the 31 days of trial:
                //char m_1 	= buf.charAt(3); ne prendo il valore come carattere
                m1 	= Character.getNumericValue(buf.charAt(3));
                m2 	= Character.getNumericValue(buf.charAt(4));
                m3 	= Character.getNumericValue(buf.charAt(5));
                m4 	= Character.getNumericValue(buf.charAt(6));
                m5 	= Character.getNumericValue(buf.charAt(7));
                m6 	= Character.getNumericValue(buf.charAt(8));
                m7 	= Character.getNumericValue(buf.charAt(9));
                m8 	= Character.getNumericValue(buf.charAt(10));
                m9 	= Character.getNumericValue(buf.charAt(11));
                m10 	= Character.getNumericValue(buf.charAt(12));
                m11 	= Character.getNumericValue(buf.charAt(13));
                m12 	= Character.getNumericValue(buf.charAt(14));
                m13 	= Character.getNumericValue(buf.charAt(15));
                //Ricreo il num. Log.:
                int Val_1 	= Character.getNumericValue(val_1);	//centinaia
                int Val_2 	= Character.getNumericValue(val_2);	//decine
                int Val_3 	= Character.getNumericValue(val_3);	//unit�
                //Num totale di registrazioni:
                int Val_tot = Val_1*100+Val_2*10+Val_3;
                //Num giorni utilizzo:
                Data_tot = (long) (m1*Math.pow(10,12)+m2*Math.pow(10,11)+m3*Math.pow(10,10)+m4*Math.pow(10,9)+m5*Math.pow(10,8)+m6*Math.pow(10,7)+m7*Math.pow(10,6)
                        +m8*Math.pow(10,5)+m9*Math.pow(10,4)+m10*Math.pow(10,3)+m11*Math.pow(10,2)+m12*Math.pow(10,1)+m13);
                long gg_rimanenti = (n_max_day-(System.currentTimeMillis()-Data_tot)/(long)(86400000));
                GlobalVariables.ggRimanenti = (int)(gg_rimanenti);
                Log.v(LOG_TAG, DoveSono + "Msec calcolati:   " + Data_tot);
                Log.v(LOG_TAG, DoveSono + "Giorni calcolati: " + Data_tot/(long)86400000);
                Log.v(LOG_TAG, DoveSono + "Giorni passati:   " + ((System.currentTimeMillis()-Data_tot)/(long)(86400000)) );
                Log.v(LOG_TAG, DoveSono + "Giorni rimanenti: " + gg_rimanenti );
                if(gg_rimanenti<=0){
                    //DisplayToastLong("Periodo di prova esaurito!");
                    DisplayToastLong("Trial period expired!");
                    GlobalVariables.LogEffettuati = 100; //in this way I block the new registrations
                }else{
                    //check the remaining number of logs the user can does:
                    if(Val_tot<100){
                        GlobalVariables.LogEffettuati = Val_tot;
                        //DisplayToastLong("Giorni rimanenti:   " + gg_rimanenti + '\n' + '\n' + "Analisi rimanenti:  " + (100-Val_tot));
                        DisplayToastLong("Remaining days:   " + gg_rimanenti + '\n' + '\n' + "Remaining analysis:  " + (100-Val_tot));
                        Log.v(LOG_TAG, DoveSono + "Num tot di Log effettuati: " + Val_tot + ". Rimanenti: " + (100-Val_tot));
                    }
                    else{
                        DisplayToastLong("Effettuate num max di registrazioni (100)!");
                        GlobalVariables.LogEffettuati = n_max_log; //in modo che blocco nuove registrazioni;
                    }
                }
                isr.close();
            }catch (IOException e) {
                e.printStackTrace();
                Log.v(LOG_TAG, DoveSono + "ANDATA MALE!");
            }
        }
    }
    //********************************************************************** check the presence of the CONFIGURATION_FILE:
    public void checkFileConfigurazione(){

        //MANAGE-CHECK THE PRESENCE OF THE CONFIGURATION FILE ON THE INTERNAL MEMORY:

        //part of code used to cancel the configuration file (zzz=1 cancel the old file):
        int zzz = 0;

        if(zzz==1){
            File dir 		= getFilesDir();
            File a 			= new File(dir, datiConfigurazione);
            a.delete();
            Log.v(LOG_TAG, DoveSono + "Cancello il file!");
            DisplayToast("Erased the configuration file from the internal memory.");
            //DisplayToast("Cancellato FileConfigurazione dalla Mem. Interna.");
        }
        File fileMemInt = getBaseContext().getFileStreamPath(datiConfigurazione);
        if (!fileMemInt.exists()){
            Log.v(LOG_TAG, DoveSono +"Il file non esiste, avvio CheckSensori.");
            //avvio il Check sui sensori, se ancora non effettuato:
            AvvisoCheckSensor();
            Log.v(LOG_TAG, DoveSono + "Avvio Check per la configurazione");
        }
        else
        {
            //insert in the global variables the information of the ability of the phone to record with the screen turned off (the info is taken from the 4� values of the config.file):
            Log.v(LOG_TAG, DoveSono + "Il file esiste gi�, lo analizzo!");
            try {
                FileInputStream fis = openFileInput(datiConfigurazione);
                InputStreamReader isr = new InputStreamReader(fis);
                String str_config = "";
                StringBuffer buf = new StringBuffer();
                BufferedReader reader = new BufferedReader(isr);
                //analizzo riga per riga il file di configurazione:
                contatore = 0;
                while((str_config=reader.readLine())!=null){
                    buf.delete(0, buf.length());
                    buf.insert(0, str_config);
                    Log.v(LOG_TAG, DoveSono + "Stato buffer (lunghezza " + buf.length() + "): " + buf);
                    //the first line contains: number of accelerometers in the phone, number of gyro in the phone, number of magnetometers in the phone, possibility of the phone to record with the screen turned off:
                    if (contatore==0)
                    {
                        if(buf.length()>=3)
                        {
                            Val_app = Character.getNumericValue(buf.charAt(0));			// 0� carattere in quanto ho: N_acc
                            GlobalVariables.flagAxesOfAcc = Val_app;					// salvo tale info nella variabile globale,
                            Val_app = 0;												// setto la var a 0 prima di assegnargli un nuovo valore (prog.dif.)
                            Val_app = Character.getNumericValue(buf.charAt(1));			// 1� carattere in quanto ho: N_gyr
                            GlobalVariables.flagAxesOfGyr = Val_app;					// salvo tale info nella variabile globale,
                            Val_app = 0;
                            Val_app = Character.getNumericValue(buf.charAt(2));			// 2� carattere in quanto ho: N_mag
                            GlobalVariables.flagAxesOfMag = Val_app;					// salvo tale info nella variabile globale,
                            Val_app = 0;
                            Val_app = Character.getNumericValue(buf.charAt(3));			// 3� carattere in quanto ho: ScreenOnOff
                            GlobalVariables.flagRecScreenOnOff = Val_app;				// salvo tale info nella variabile globale,
                        }
                        else
                        {
                            Val_app	= 0;
                            DisplayToastLong("Errore nella lettura da Memoria Interna.");
                        }
                    }
                    //the other lines regard: dt_delay_1, dt_delay_2, dt_delay_3, dt_delay_4, (N.S.)_x, (N.S.)_y, (N.S.)_z, (U.D.)_x, (U.D.)_y, (U.D.)_z:
                    else
                    {
                        Valore_tot = 0;
                        //guardo ad un eventuale segno del numero riportato:
                        if (buf.charAt(0)=='-')
                        {
                            segno = -1;
                            for (Val_app = buf.length(); Val_app>1; Val_app--)
                            {
                                Val_app_1	= Character.getNumericValue(buf.charAt(Val_app-1));
                                Valore_tot 	= Valore_tot + (int)(Val_app_1*Math.pow(10,(buf.length()-Val_app)));	// (buf.length()-Val_app) = elevamento a potenza ad hoc per il valore in esame
                            }
                            Valore_tot = Valore_tot * segno;
                        }
                        //caso invece di valore positivo del numero letto:
                        else
                        {
                            segno = 1;
                            for (Val_app = buf.length(); Val_app>0; Val_app--)
                            {
                                Val_app_1 	= Character.getNumericValue(buf.charAt(Val_app-1));
                                Valore_tot 	= Valore_tot + (int)(Val_app_1*Math.pow(10,(buf.length()-Val_app)));	// (buf.length()-Val_app) = elevamento a potenza ad hoc per il valore in esame
                            }
                            Valore_tot = Valore_tot * segno;
                        }
                        Log.v(LOG_TAG, DoveSono + "Valore ricostruito: " + Valore_tot);
                        //define soma global variables useful for other part of the code:
                        switch (contatore)
                        {
                            case 1:	{	GlobalVariables.dt_delay_rec_1 = Valore_tot;	break;	}	//salvo il primo valore: SENSOR_DELAY_UI
                            case 2:	{	GlobalVariables.dt_delay_rec_2 = Valore_tot;	break;	}	//salvo il primo valore: SENSOR_DELAY_NORMAL
                            case 3:	{	GlobalVariables.dt_delay_rec_3 = Valore_tot;	break;	}	//salvo il primo valore: SENSOR_DELAY_GAME
                            case 4:	{	GlobalVariables.dt_delay_rec_4 = Valore_tot;	break;	}	//salvo il primo valore: SENSOR_DELAY_FASTEST
                        }
                    }
                    contatore ++;
                }
                isr.close();
            }catch (IOException e) {
                e.printStackTrace();
                Log.v(LOG_TAG, DoveSono + "ANDATA MALE!");
            }

            //print on screen the information of the smartphone:
            if (GlobalVariables.flagRecScreenOnOff ==1)			InfoValoriDaFileConfigurazione(2);
            else if (GlobalVariables.flagRecScreenOnOff==2)		InfoValoriDaFileConfigurazione(1);
        }
    }
    //********************************************************************** Notice the user that is needed a configuration of the device: (Dialog window):
    public void AvvisoCheckSensor(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Swim App:");
        builder.setCancelable(false);
        builder.setMessage("A configuration of the device is needed before begin to swim!"+'\n'+"Follow the instructions..");
        //builder.setMessage("E� necessario configurare il dispositivo prima di iniziare a nuotare!"+'\n'+"Seguire le istruzioni..");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //lancio il Service di configurazione
                StartConfigService();
            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //********************************************************************** OnSensorChange
    public void onSensorChanged(SensorEvent event) {
        //long tempo_attuale = System.currentTimeMillis();
      /*  if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            if((contaAggiornamentiSensGyro % modificaOgniTotMisure) == 0)
            {
                Gyro_x.setText(String.format("%.2f", event.values[0]));
                Gyro_y.setText(String.format("%.2f", event.values[1]));
                Gyro_z.setText(String.format("%.2f", event.values[2]));
                contaAggiornamentiSensGyro = 0;
            }
            contaAggiornamentiSensGyro++;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            if((contaAggiornamentiSensAccel % modificaOgniTotMisure) == 0)
            {
                Accel_x.setText(String.format("%.2f", event.values[0]));
                Accel_y.setText(String.format("%.2f", event.values[1]));
                Accel_z.setText(String.format("%.2f", event.values[2]));
                contaAggiornamentiSensAccel = 0;
            }
            contaAggiornamentiSensAccel++;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            if((contaAggiornamentiSensMagn % modificaOgniTotMisure) == 0)
            {
                Magnetic_x.setText(String.format("%.2f", event.values[0]));
                Magnetic_y.setText(String.format("%.2f", event.values[1]));
                Magnetic_z.setText(String.format("%.2f", event.values[2]));
                contaAggiornamentiSensMagn = 0;
            }
            contaAggiornamentiSensMagn++;
        }*/

    }
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }
    //********************************************************************** Display Toast
    private void DisplayToast(String msg) {
        if(GlobalVariables.displayOnOff==1)
            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private void DisplayToastLong(String msg) {
        if(GlobalVariables.displayOnOff==1)
            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
    }
    //********************************************************************** changeButtonColor
    public void changeButtonColor(char g_r, int tipo) {
        String vibratorService = Context.VIBRATOR_SERVICE;
        Vibrator vibrator = (Vibrator)getSystemService(vibratorService);
        if(g_r=='g'){
            switch (tipo) {
                case 3:		{vibrator.vibrate(100);
                    StartStopService.setBackgroundColor(Color.GREEN);
                    StartStopService.setText("ON");
                    break;}
            }
        }
        else
        {
            switch (tipo) {
                case 3:		{vibrator.vibrate(100);
                    StartStopService.setBackgroundColor(Color.RED);
                    StartStopService.setText("OFF");
                    break;}
            }
        }

    }
    //********************************************************************** Start and Stop Services in Background
    private void StartMyService(){
        startService(new Intent(this, ServiceRecOnOff.class));
        //cambio luminosit� schermo:
        Log.v(LOG_TAG, DoveSono + "Service startato. Cambio luminosit� schermo.");
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 0.01F;
        getWindow().setAttributes(layout);
    }
    private void StopMyService() 		{stopService(new Intent(this, ServiceRecOnOff.class));}
    private void StartConfigService() 	{startService(new Intent(this, ServiceForConfig.class));}
    private void StopConfigService() 	{stopService(new Intent(this, ServiceForConfig.class));}



    //********************************************************************** Information on the record mode:
    public void InfoValoriDaFileConfigurazione(int scegli_msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Info Config.txt:");
        builder.setCancelable(false);
        switch (scegli_msg)
        {
            case 1:
            {
                builder.setMessage(
                        "INFORMATION FROM CONFIGURATION FILE: " + '\n' +'\n' +
                                "Record with screen turned off: YES;" 									 + '\n' +
                                "DT for the SENSOR_DELAY_UI:      " 		+ GlobalVariables.dt_delay_rec_1 + '\n' +
                                "DT for the SENSOR_DELAY_NORMAL:  " 		+ GlobalVariables.dt_delay_rec_2 + '\n' +
                                "DT for the SENSOR_DELAY_GAME:    " 		+ GlobalVariables.dt_delay_rec_3 + '\n' +
                                "DT for the SENSOR_DELAY_FASTEST: " 		+ GlobalVariables.dt_delay_rec_4);
                break;
            }

            case 2:
            {
                builder.setMessage(
                        "INFORMATION FROM CONFIGURATION FILE: " + '\n' +'\n' +
                                "Record with screen turned off: NO;" 									 + '\n' +
                                "DT for the SENSOR_DELAY_UI:      " 		+ GlobalVariables.dt_delay_rec_1 + '\n' +
                                "DT for the SENSOR_DELAY_NORMAL:  " 		+ GlobalVariables.dt_delay_rec_2 + '\n' +
                                "DT for the SENSOR_DELAY_GAME:    " 		+ GlobalVariables.dt_delay_rec_3 + '\n' +
                                "DT for the SENSOR_DELAY_FASTEST: " 		+ GlobalVariables.dt_delay_rec_4);
                break;
            }

            default:
            {
                break;
            }
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onBackPressed(){


        moveTaskToBack(true);

    }

}
