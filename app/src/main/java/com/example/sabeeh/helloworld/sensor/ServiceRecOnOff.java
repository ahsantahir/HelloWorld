// 21/03/2014: NON SO SE IL registerReceiver sull' ACTION_BATTERY_CHANGED inizializza o meno un registerReceiver che deve poi essere unregistrato. Trovato nulla sul web! 

package com.example.sabeeh.helloworld.sensor;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sabeeh.helloworld.R;
import com.example.sabeeh.helloworld.SwimAppApplication;
import com.example.sabeeh.helloworld.entites.swim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

//import android.support.v4.content.LocalBroadcastManager;	/* TODO: broadcast

public class ServiceRecOnOff extends Service implements SensorEventListener{

   private 		String DoveSono 			= "(ServiceRecOnOff) ";
   static final String LOG_TAG 				= "NUOT_APP";
   final static String MY_ACTION 			= "MY_ACTION";
   private 		SensorManager mgr;
   private 		List<Sensor> sensorList;										// utilizzato per startare l'ascolto di tutti i sensori
   private		Sensor mAcc, mGyr, mMag;										// utilizzato per startare l'ascolto dei soli 3 sensori d'interesse
   Intent intent = new Intent("com.practice.SimpleService.MY_ACTION");
    public static final String SHAREDPREFFILE = "temp";
   //variabili generiche:
   long tempo_precedente_acc 		= System.currentTimeMillis();
   long tempo_precedente_gyr 		= System.currentTimeMillis();
   long tempo_ultima_identificaz 	= System.currentTimeMillis();
   long tempo_prima_identificaz_staz= System.currentTimeMillis();				// used to start the timer for the identification of the stationarity  
   
   //crezione vettori di identificazione sequenza movimento (ritrovo di un movimento di rotazione su se stessi (tempo stimato 1 secondo) in un buffef di 200 campioni consecutivi):
   List<Float> 	identificGyr_x 		= new ArrayList<Float> ();					// to use before cellular orientation is determined
   List<Float> 	identificAcc_x 		= new ArrayList<Float> ();					// ...
   List<Float> 	identificGyr_y 		= new ArrayList<Float> ();					// ...
   List<Float> 	identificAcc_y 		= new ArrayList<Float> ();					// ...
   List<Float> 	identificGyr_ok		= new ArrayList<Float> ();					// to use after cellular orientation is determined
   List<Float> 	identificAcc_ok		= new ArrayList<Float> ();					// to use after cellular orientation is determined
   int flag_orientation_determined  = 0;										// indicates when the system has determined the orientation of the device (0=none, 1=x , 2=y)
   int dt_rec   					= 200;										// 200ms, cio� 0.2 sec (check originario)
   double dt_rec_InSec				= (double)dt_rec/(double)1000;				// 0.2 sec, utilizzato in modo da non sovraccaricare successivamente il calcolo dell'integrale
   boolean can_update_array			= true;										// flag used to permit at the code to update (flag = true) or not (flag = false) the arrays used to perform the checks (rotation, verticality, etc) 
   int n_camp_MaxNum				= 16;										// create the vectors, for the user positions, with max dimension of 16 elements 	(3 sec = 0.2 sec * (15+1), considerando il dt tra due campioni, non il singolo)
   int n_camp_stationarity			= 6;										// number of elements used for the stationary position  							(1 sec = 0.2 sec * (5+1),  considerando il dt tra due campioni, non il singolo)	
   int n_camp_rotation				= 16;										// number of elements used for the rotation of the user 							(3 sec = 0.2 sec * (15+1), considerando il dt tra due campioni, non il singolo)
   int n_camp_vertical				= 11;										// number of elements used for the vertical position	 							(2 sec = 0.2 sec * (10+1), considerando il dt tra due campioni, non il singolo)
   int n_camp_horizontal			= 6;										// number of elements used for the horizontalposition	 							(1 sec = 0.2 sec * (5+1), considerando il dt tra due campioni, non il singolo)
   double pi						= 3.14;										// (used for check of 360�) 
   double almost_two_pi				= 4.72;										// (used for check of 360�) it is 270�
   double angolo 					= 0;										// (used for check of 360�) value of the estimated angle, obtaine d with the integration
   long tempo_ultimocheck_Rotation360    = System.currentTimeMillis();				// (used for check of 360�) utilizzato per non effettuare eccessivi controlli sul buffer del check della rotazione (dc ha un dt di aggiornamento << del dt dell'aggiornamento dei sensori del sistema
   long tempo_ultimocheck_stationarity   = System.currentTimeMillis();			// (used for check of stationarity)
   long tempo_ultimocheck_horizontality  = System.currentTimeMillis();			// (used for check of horizontality)
   long tempo_ultimocheck_verticality    = System.currentTimeMillis();			// (used for check of verticality)
   int FlagSensorIdentify_G_or_A	= 0;										// la setto a 1 se debuggo su dispositivo SENZA gyro, a 0 in dispositivo con TUTTI i sensori
   int flag_identific 				= 0; 										// flag usata per segnalare se ho trovato un valore buono o non
   int dim_vett_acc;
   int dim_vett_gyr;
   int FlagSensorRec 				= 0;										// lo setto ad 1 quando voglio registrare e a 0 quando ho finito
   long tempo_start 				= 0;										// millisecs all'inizio della registrazione, lo uso per salvarmi la durata del log
   long tempo_end 					= 0;										// millisecs alla fine della registrazione, lo uso per salvarmi la durata del log
   long tempo_sleep_post_identificaz= 2000;										// time used to sleep the system after a state change
   
   //variabili usate durante il salvataggio dai sensori:
   int sensorType;																				//tipo sensore analizzato
   String g_val_x, g_val_y, g_val_z, m_val_x, m_val_y, m_val_z, a_val_x, a_val_y, a_val_z;		//valori dai sensori, castati a stringhe
   int soglia_acc_vert = 7;																		//values used during the analyze of the state machine
   double soglia_acc_orizz = 6;																	//values used during the analyze of the state machine
   double soglia_stazionarieta_Gyro = 0.5;														//values used during the analyze of the state machine (on the values of Gyro)
   double soglia_stazionarieta_Acc  = 0.5;														//values used during the analyze of the state machine (on the values of Acc)
   
   //stringhe usate per salvare i dati dai sensori:
   List<String> listDatiSensGyr_x 	= new ArrayList<String>(); 
   List<String> listDatiSensGyr_y 	= new ArrayList<String>(); 
   List<String> listDatiSensGyr_z 	= new ArrayList<String>();
   List<String> listDatiSensAcc_x 	= new ArrayList<String>();
   List<String> listDatiSensAcc_y 	= new ArrayList<String>();
   List<String> listDatiSensAcc_z 	= new ArrayList<String>();
   List<String> listDatiSensMag_x 	= new ArrayList<String>();
   List<String> listDatiSensMag_y 	= new ArrayList<String>();
   List<String> listDatiSensMag_z 	= new ArrayList<String>();
   List<String> listTempoGyro 	  	= new ArrayList<String>();
   List<String> listTempoAcc 	  	= new ArrayList<String>();
   List<String> listTempoMag 	  	= new ArrayList<String>();
   
   // elements used in the phase of pre-recording, before the athlete put his body horizontali:
   int numMaxCampFasePreRec 		= 100;										// number of valures saved in the array before the athlete starts his run
   double dtCampMinFasePreRec 		= 0.02;										// minimum sampling time in the phase previous the horizontal detection
   long lastSamplePreRec_Acc 		= 0;										// to avoid the problem of different sampling time, use three different "timer"
   long lastSamplePreRec_Gyr 		= 0;
   long lastSamplePreRec_Mag 		= 0;
   long timeOfLastDW				= 5000;	//TODO: eliminare questo task
   
   //variabili utilizzate per il "debug" a posteriori dell'operato dell'applicazione
   Vector<String> vettDebug			= new Vector<String> ();		//vettore utilizzato per visionare le operazioni effettuate durante la misurazione
   Vector<String> vettTempoDebug	= new Vector<String> ();		//vettore temporale dei debug effetttuati
   String debug_DATI_SENS_SALVATI 	= "6";
   private WakeLock wl;
   private SimpleDateFormat ora = new SimpleDateFormat("HH:mm:ss");
   
   //private BroadcastReceiver BatteryChange;
   
   //avvisi sonori/vibrazionali:
   long timePreviousBeep	= 0;	//usata per il sound feedback durante la registrazione dei sensori
   final ToneGenerator tg 			= new ToneGenerator(AudioManager.STREAM_NOTIFICATION,200);
   int numAvvisiMaxLog 				= 1;	//conto il num di volte che avviso l'utente quando ho raggiunto il num max di registrazioni (10 da 500msec)
   int numAvvisiBatteryLow			= 1;	//conto il num di volte che avviso l'utente quando ho raggiunto il livello min di batteria  (10 da 500msec)
   int numAvvisiScreenOff			= 1;	//conto il num di volte che avviso l'utente quando lo schermo viene spento ed il cellulare non � in grado di registrare a schermo spento
   int flag_device_stopped			= 0;	//set to 1 when the device is stopped. When this flag is =1, a timer can starts to wait 1 minuto before go into SLEEP mode
   int time_before_sleep			= 60000;//time before the device goes sleep (if it is stopped)
   Vibrator vibrator;						//variabile utilizzata per il feedback vibrazionale durante il Listen\Record
   
   //parte per la notifica all'utente:
   int icona 						= R.drawable.ic_launcher;
   String testoNotifica 			= "SwimApp";
   long quando 						= System.currentTimeMillis();
   int numRecEffettuati 			= 0;
   
   //elementi per visione stato batteria:
   int flagBatt 					= 0;
   int level;
   int scale;
   double batteryVal;
   double valMinBatt 				= GlobalVariables.batteryValMin;	//10
   long tempo_lastCheck				= 0;
   long tempo_traCheck 				= 300000;							// (300000 ms) 5 minuti
   long tempo_attuale;
   long t_prec_G = System.currentTimeMillis();
   long t_prec_M = System.currentTimeMillis();
   long t_prec_A = System.currentTimeMillis();
   long t_prec_SensorBroadcast 		= 0;								// used to update the sensors' cells via Broadcast. TODO: da rimuovere per versione finale
   long tempo_SensorBroadcast		= 1000;								// update every one second. 						TODO: da rimuovere per versione finale
   int	dt_G, dt_M, dt_A;
   int flagSaveBattery				= 0;	//=0 quando durante la registrazione l'utente non vuole guardare all batteria; =1 quando sceglie che sotto il 5% si stoppi la registrazione
   
   //elementi per la BroadcastReceiver per lo spegnimento del monitor:
   String 		type;						//variabile usata per loggare il tipo di BroadcastReceiver arrivato
   BroadcastReceiver screenReceiver;
   int ScreenIsOn					= 1;	//=1 se lo schemro � acceso e =0 se lo schermo � spento
   long timeVibrazAttuale			= 0;	//usata per la vibrazione feedback quando spengo lo schermo
   long timeVibrazPrecedente		= 0;	//usata per la vibrazione feedback quando spengo lo schermo
   
   //stato in cui si trova il cell durante la sessione: 
   int StatoSistema					= 2;	// 	1: dormo 		(schermo pento, 				feedback ogni 5 sec -Vib/Beep-)
   											//	2: standby 		(aspetta una rotazione, 		feedback ogni 3 sec -Vib/Beep-)
   											//	3: attesa		(aspetta la posa orizzontale, 	feedback ogni 1 sec -Vib/Beep-)
											//	4: registro 	(aspetta la posa verticale, 	feedback ogni 1 sec -Beep-)
   long[] pattern_ScreenOn 			= {0,200,100,400,100,800,0};	// pattern alla riaccensione dello schermo
   long[] pattern_ScreenOff			= {0,800,100,400,100,200,0};	// pattern allo spegnimento dello schermo
   //gestione dello schermo On-Off, in base alla variabile salvata tra quelle globali, sulla base delle info derivanti dal file di configurazione:
   int RecWithScreenOnOff			= GlobalVariables.flagRecScreenOnOff;	// =1 se non pox rec a schermo spento e =2 se pox. Lo inserisco in fase di configurazione.
   	   
   // variables used to implement the Broadcast update of the text sensor cells in the Main view:
   public static final String		SENSORS_UPDATE_BROADCAST = ServiceRecOnOff.class.getName() + "SensorUpdateBroadcast",
		   							ACC_x = "acc_x",	ACC_y = "acc_y",	ACC_z = "acc_z",	ACC_N_stuck = "gyr_n_stuck",
		   							GYR_x = "gyr_x",	GYR_y = "gyr_y",	GYR_z = "gyr_z",	GYR_N_stuck = "acc_n_stuck",
		   							MAG_x = "mag_x",	MAG_y = "mag_y",	MAG_z = "mag_z",	MAG_N_stuck = "mag_n_stuck";
   float G_val_x, G_val_y, G_val_z, M_val_x, M_val_y, M_val_z, A_val_x, A_val_y, A_val_z;		//valori dai sensori, utilizzati (SOLO) per il Broadcast TODO: eliminare nella versione finale
   int G_num_stuck = 0;					// variable used to enumerate the gyroscope stucks
   int A_num_stuck = 0;					// variable used to enumerate the accelerometer stucks
   int M_num_stuck = 0;					// variable used to enumerate the magnetometer stucks
   boolean flag_sensor_stuck 	= true;	// flag that indicates a sensor stuck and a sequential updates of the value in the cell (set to true in order to send the first value to the cells).
   long tempo_max_sensor_stuck 	= 300;	// max msec before restart the Listener
   
   //******************************************** ON CREATE: **************************************
   
   @Override
   public void onCreate() {
      Log.d(LOG_TAG, DoveSono + "Entro in onCreate.");
      //acquisisco WakeLock:
      PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
      //wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"MyWakeLock");
      wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK ,"MyWakeLock"); //schermo sempre acceso
      wl.acquire();
	  Log.d(LOG_TAG, DoveSono + "acquisisco WakeLock (wl) XXX");
	  
	  //starto il listener dei sensori:
      mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
      mAcc = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);		//definisco a che tipo di sensore si matcha ACC
      mGyr = mgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);			//definisco a che tipo di sensore si matcha GYR
      mMag = mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);		//definisco a che tipo di sensore si matcha	MAG
      registraListenerSensori();									//definisco quali sensori analizzare e in quale SensorDelay
      Log.d(LOG_TAG, DoveSono + "Register listener Service (mgr) XXX" );
      
      //creo e lancio il Broadcast Receiver:
      IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
      filter.addAction(intent.ACTION_SCREEN_ON);
      screenReceiver = new ScreenReceiver();
      registerReceiver(screenReceiver,filter);
      Log.d(LOG_TAG, DoveSono + "Attivo il Receiver dello schermo (screenReceiver) XXX" );
  }
  
 //******************************************** ON DESTROY: ***************************************
@Override
public void onDestroy() {
	super.onDestroy();
	//nel caso la registrazione sia ancora attiva, setto la flag=0 e salvo i dati:
	if(FlagSensorRec==1){
		FlagSensorRec=0;
		stopRec_UpdateOrNotNumOfRecord (0);
	}
	Log.d(LOG_TAG, DoveSono + "Entro in onDestroy" );
	mgr.unregisterListener(this);
	Log.d(LOG_TAG, DoveSono + "unregister listener Service (mgr) XXX" );
	
	//disattivo il Receiver:
	unregisterReceiver(screenReceiver);
	Log.d(LOG_TAG, DoveSono + "Disattivo Receiver (screenReceiver) XXX" );
	
	wl.release();
	Log.d(LOG_TAG, DoveSono + "Rilascio WakeLock (wl) XXX");
	Log.d(LOG_TAG, DoveSono + "Esco da onDestroy" );
}

//******************************************** RECEIVER DELLO SCREEN SPENTO/ACCESO ****************
public class ScreenReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		type = intent.getAction();
		Log.v( LOG_TAG, DoveSono + "Broadcast arrivato, tipo: " + type);
		if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
			Log.w( LOG_TAG, DoveSono + "Schermo spento!" );
			//unregister and register listener:
			refreshListener();
			//setto a zero la flag dello schermo:
			ScreenIsOn = 0;		
		}
		else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
				Log.v( LOG_TAG, DoveSono + "Schermo acceso!" );
				
				tempo_ultima_identificaz = tempo_attuale;		
				//cancelo i buffer, per rivalutare da capo la posizione dell'atleta:
//clear_check_vectors(flag_orientation_determined);

				//setto a uno la flag dello schermo:
				ScreenIsOn = 1;			
		}
	}
}

public void refreshListener(){
	mgr.unregisterListener(this);	//unregistro tutti i sensori	
	registraListenerSensori();		//li registro nuovamente (definisco quali sensori analizzare e in quale SensorDelay)
	
	Log.v( LOG_TAG, DoveSono + "Register listener Sevice (mgr) XXX" );
}

@Override
public IBinder onBind(Intent arg0) {
    // TODO Auto-generated method stub
    return null;
}

//metodo collegato al BroadcastReceiver
private String IntentFilter(String actionBatteryChanged) {
	// TODO Auto-generated method stub
	return null;
}

private void registraListenerSensori(){
	mgr.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_GAME);
    mgr.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_GAME);
    mgr.registerListener(this, mMag, SensorManager.SENSOR_DELAY_GAME);
}

//******************************************** CheckBattery ***************************************
private void checkBattery(){
	//controllo livello batteria:
    Context context 			= getApplicationContext();
    final Intent batteryStatus 	= context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    level 						= batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
    scale 						= batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);

    //prevent error of a division by 0:
    if (scale!=0)
    {
    	batteryVal 	= (double)(level/(float)scale);
    }
    else			batteryVal 	= 0;
    
    //se batteria scarica:
    if( (double)batteryVal<=valMinBatt)
    {
    	GlobalVariables.batteryState = 1;
        if(FlagSensorRec==1){
        	//stoppo la registrazione dai sensori e salvo i dati
        	stopRec_UpdateOrNotNumOfRecord (0);
        	//print the right information
        	if (scale!=0)
        		DisplayToastLong("Batteria quasi scarica, stop REC!");
        	else
        		DisplayToastLong("Errore durante la lettura dati, stop REC!");
        }
        else{
      	  Log.v( LOG_TAG, DoveSono + "batteria quasi scarica, no REC. " + "GlobalVariables.batteryState: " + GlobalVariables.batteryState);
        }
    }else{
    	GlobalVariables.batteryState = 0;
    	DisplayToastLong("Batteria ancora carica: " + (int)(batteryVal*(double)scale) + "%!");
    	Log.w( LOG_TAG, DoveSono + "BATTERY. Sono dentro l' ELSE > SOGLIA, post-Display.");
    	//se la batteria � stata ricaricata, risetto a 1 il contatore degli avvisi per la batteri:
    	if(numAvvisiBatteryLow>=10) numAvvisiBatteryLow=1;
    }
}

//******************************************** OnSensorChanged ************************************
@Override
public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // TODO Auto-generated method stub      
}

@Override
public void onSensorChanged(SensorEvent event) {
	tempo_attuale 	= System.currentTimeMillis();		//aggiorno la variabile dedicata ai millisecondi del sistema:
	
	//ogni minuto controllo la batteria (se l'utente lo richiede):
	if(GlobalVariables.flagSaveBattery==1){
		if(tempo_lastCheck==0 || (tempo_attuale-tempo_lastCheck)>=tempo_traCheck){
			checkBattery();
			tempo_lastCheck = tempo_attuale;
		}
	}
	
	// Check the sensor change, update the arrays of values (acc e gyro) and save the values (if the system is in "record state"):
	updatesAfterSensorChange (event);

	// before do any new operations, after the identification of one state transition, the system has to wait 2 seconds
	if ((tempo_attuale - tempo_ultima_identificaz) >= tempo_sleep_post_identificaz)
	{
		// TODO: reinserire quando vorremo lanciare l'App o cmq se sar� necessario.	
		/*	
			if(GlobalVariables.LogEffettuati<100)
			{
		*/		
		// Check the airplane mode (it is necessary to the Listen\Record)
		if(Settings.System.getInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0)==1)
		{
			// Cerco sequenza solo se la batteria � UP:
			if(GlobalVariables.batteryState==0)
			{
				// Flag che mi identifica l'eventuale sequenza/comando data dall'atleta. A fine switch avr� valore 0 se nessuna sequenza � stata trovata, altrimenti avr� il valore della modalit� a cui si riferisce..
				flag_identific = 0;
				
				// In base alla modalit� in cui sono, differisco il comportamento:
				switch (StatoSistema) {
					
					case 0:
						// State: ALERT the user with vibration (for low battery or screen off). This state is for undesired events!
						{
							if (RecWithScreenOnOff==1 && ScreenIsOn==0)
							{
								alertsTheUser_ScreenOff();
							}
							else
							{
								// if the user has turned on the screen, change the system mode
								StatoSistema = 1;
								Log.w( LOG_TAG, DoveSono + "Transizione di Stato: 0 --> 1 (acceso lo schermo)");
							}
							break;
						}
					case 1:
						// State: SLEEP
						{
							// give the feedback (vibration and sound) to the user
							feedbackTotheUser();
							
							// check on the vectors dimensions:
							if( (tempo_attuale - tempo_ultimocheck_stationarity)>dt_rec &&
								(
									(flag_orientation_determined != 0 && identificGyr_ok.size() >= n_camp_stationarity && identificAcc_ok.size() >= n_camp_stationarity) ||
									(flag_orientation_determined == 0 && identificGyr_x.size()  >= n_camp_stationarity && identificAcc_x.size()  >= n_camp_stationarity)
								)
							)
							// analyze the stationarity of the device
							checkStationarityOfDevice();
							
							break;
						}
	
					case 2:
						// State: STANDBY
						{
							// if the device can't record with the screen turned off (RecWithScreenOnOff==1) and the screen is off (ScreenIsOn==0), switchs the system to SLEEP
							if (RecWithScreenOnOff==1 && ScreenIsOn==0)
							{
								StatoSistema = 1;
								Log.w( LOG_TAG, DoveSono + "Transizione di Stato: 2 --> 1 (spento lo schermo)" );
							}
							else
							{
								// give the feedback (vibration and sound) to the user
								feedbackTotheUser();
								
								// Search the rotation or the stationarity of the device:
								// if the device has already determined his orientation, check for only one axes
								if(flag_orientation_determined != 0)
								{
									//check if the user makes a rotation
									if(	(tempo_attuale - tempo_ultimocheck_Rotation360)>dt_rec &&
										identificAcc_ok.size() >= n_camp_rotation &&
										identificGyr_ok.size() >= n_camp_rotation
									)
										checkForTheRotationOf360Degree();
									
									// if no rotation is detected, analyze the stationarity of the device
									if(	flag_identific == 0 &&
										(tempo_attuale - tempo_ultimocheck_stationarity)>dt_rec &&
										(identificAcc_ok.size() >= n_camp_stationarity &&
										identificGyr_ok.size() >= n_camp_stationarity)
									)
										checkStationarityOfDevice();
								}
								else
								{
									//check if the user makes a rotation
									if(	(tempo_attuale - tempo_ultimocheck_Rotation360)>dt_rec &&
										identificAcc_x.size() >= n_camp_rotation &&
										identificGyr_x.size() >= n_camp_rotation &&
										identificAcc_y.size() >= n_camp_rotation && 
										identificGyr_y.size() >= n_camp_rotation
									)
										checkForTheRotationOf360Degree();
									
									// if no rotation is detected, analyze the stationarity of the device
									if(	flag_identific == 0 &&
										(tempo_attuale - tempo_ultimocheck_stationarity)>dt_rec &&
										identificAcc_x.size() >= n_camp_stationarity &&
										identificGyr_x.size() >= n_camp_stationarity &&
										identificAcc_y.size() >= n_camp_stationarity &&
										identificGyr_y.size() >= n_camp_stationarity
									)	
										checkStationarityOfDevice();
								}
							}
							break;
						}
					
					case 3:
						// State: LISTEN (attendo che l'utente si metta in orizzontale (per registrare) o faccia una rotazione (per mettere in standby))
						{
							// if the device can't record with the screen turned off (RecWithScreenOnOff==1) and the screen is off (ScreenIsOn==0), switchs the system to ALERT and then, automatically, to SLEEP
							if (RecWithScreenOnOff==1 && ScreenIsOn==0)
							{
								numAvvisiScreenOff = 1;		// reset the variable of the alerts
								StatoSistema = 0;
								Log.w( LOG_TAG, DoveSono + "Transizione di Stato: 3 --> 0 (spento lo schermo)");
							}
							else
							{
								// give the feedback (vibration and sound) to the user
								feedbackTotheUser();
								
								//check if the user makes a rotation
								if(	(tempo_attuale - tempo_ultimocheck_Rotation360)>dt_rec &&
									identificAcc_ok.size() >= n_camp_rotation &&
									identificGyr_ok.size()  >= n_camp_rotation
								)
									checkForTheRotationOf360Degree();
								
								// if no rotation is detected, analyze the possible horizontal position
								if(	flag_identific == 0 &&
									(tempo_attuale - tempo_ultimocheck_horizontality)>dt_rec &&
									identificAcc_ok.size() >= n_camp_horizontal &&
									identificGyr_ok.size() >= n_camp_horizontal
								)
									checkForHorizontalPosition();
							}
							break;
						}
					
					case 4:
						// State: RECORDING (il cellulare sta registrando i dati)
						{
							// if the device can't record with the screen turned off (RecWithScreenOnOff==1) and the screen is off (ScreenIsOn==0), switchs the system to ALERT and then, automatically, to SLEEP
							if (RecWithScreenOnOff==1 && ScreenIsOn==0)
							{
								stopRec_UpdateOrNotNumOfRecord (1);
								Log.w(LOG_TAG, "FERMATA REC!");
								
								numAvvisiScreenOff = 1;													// reset the variable of the alerts
								StatoSistema = 0;														// alert the user
								Log.w( LOG_TAG, DoveSono + "Transizione di Stato: 4 --> 0 (spento lo schermo)");
							}
							else
							{
								// give the feedback (only sound) to the user
								feedbackTotheUser();
								
								// check for the vertical position
								if(	(tempo_attuale - tempo_ultimocheck_verticality)>dt_rec &&
									identificAcc_ok.size() >= n_camp_vertical
								)
									checkForVerticalPosition();
							}
							break;
						}
						
					default:
						// State: UNDESIDERATED
						{
							break;
						}
					}
					
					// if a sequenze is found (FLAG != 0), the state of the system is changed:
					if(flag_identific!=0){
						sequenceFoundChangeState();
					}
				}
			else{
				//se batteria sotto soglia minima, avviso l'utente dello stop dei rec:
				if(GlobalVariables.batteryState==1){
					alertsTheUserWithTotVibr_Battery (5);
				}
			}
		}
		else
		{
			alertsTheUser_Airplane();
		}
	// TODO: reinserire quando vorremo lanciare l'App o cmq se sar� necessario.	
	/*			
		}
		//se num log effettuati � > della soglia di 100, avviso l'utente con una vibrazione particolare
		else{
			timeSystemBeepAttuale=tempo_attuale;
			if (numAvvisiMaxLog<=10 && (timeSystemBeepAttuale-timePreviousBeep)>=1000){
				vibrator.vibrate(500);
				numAvvisiMaxLog = numAvvisiMaxLog+1;
				tg.startTone(ToneGenerator.TONE_PROP_BEEP);
				timePreviousBeep=timeSystemBeepAttuale;
				//sull'ultimo avviso di vibrazione, mando a schermo la finestra di avviso:
				if(numAvvisiMaxLog==1) periodoProvaTerminato();
			}
		}
*/	
	}
}

//********************************************************************** Check the sensor change, update the arrays of values (acc e gyro) and save the values (if the system is in "record state")
private void updatesAfterSensorChange (SensorEvent event) {
	
	//analizzo i sensori:
	
	sensorType = event.sensor.getType();			//salvo il tipo di sensore di cui ho l'aggiornamento:
	
	if (sensorType == Sensor.TYPE_MAGNETIC_FIELD)
	{
		t_prec_M = tempo_attuale;
		//gestione del salvataggio dei valori:
		m_val_x = String.valueOf(event.values[0]);
		m_val_y = String.valueOf(event.values[1]);
		m_val_z = String.valueOf(event.values[2]);
		//save float values (for Broadcast update) TODO: rimuovere in versione finale
		M_val_x = event.values[0];
		M_val_y = event.values[1];
		M_val_z = event.values[2];

		// se sto aspettando una registrazione (StatoSistema=3), salvo solo 10 valori consecutivi:
		if(StatoSistema==3)
		{
			// check if the timer is elapsed:
			if ((tempo_attuale-lastSamplePreRec_Mag) >= dtCampMinFasePreRec)
			{
				lastSamplePreRec_Mag = tempo_attuale;
				listTempoMag.add(String.valueOf(tempo_attuale));
				listDatiSensMag_x.add(m_val_x);
				listDatiSensMag_y.add(m_val_y);
				listDatiSensMag_z.add(m_val_z);
				// remove the 11� element: only the last 10 were stored (10*0.1sec)
				if(listTempoMag.size()>numMaxCampFasePreRec) 		{listTempoMag.remove(0);}
				if(listDatiSensMag_x.size()>numMaxCampFasePreRec) 	{listDatiSensMag_x.remove(0);}
				if(listDatiSensMag_y.size()>numMaxCampFasePreRec) 	{listDatiSensMag_y.remove(0);}
				if(listDatiSensMag_z.size()>numMaxCampFasePreRec) 	{listDatiSensMag_z.remove(0);}
			}
		}
		else if(StatoSistema==4)
		// se sto registrando (StatoSistema=4), salvo i valori:
		{
			listTempoMag.add(String.valueOf(tempo_attuale));
			listDatiSensMag_x.add(m_val_x);
			listDatiSensMag_y.add(m_val_y);
			listDatiSensMag_z.add(m_val_z);
		}
	}
	
	else if (sensorType == Sensor.TYPE_GYROSCOPE)
	{
		t_prec_G = tempo_attuale;
		//gestione del salvataggio dei valori (Strings)
		g_val_x = String.valueOf(event.values[0]);
		g_val_y = String.valueOf(event.values[1]);
		g_val_z = String.valueOf(event.values[2]);
		//save float values (for Broadcast update) TODO: rimuovere in versione finale
		G_val_x = event.values[0];
		G_val_y = event.values[1];
		G_val_z = event.values[2];

		//riempio vettore per l'identificazione (riempio un vettore di dieci campioni: 0.2*10=2 secondo di analisi)
		if (can_update_array==true && (tempo_attuale-tempo_precedente_gyr)>dt_rec){
			// check if the system has already found the device orientation and save the correct values of gyro
			switch (flag_orientation_determined)
			{
				// save both axes (x and y)
				case 0:
				{
					identificGyr_x.add(event.values[0]);
					identificGyr_y.add(event.values[1]);
					if(identificGyr_x.size()>n_camp_MaxNum) {identificGyr_x.remove(0);}	// mantengo il vettore di dimensione sempre 10, equivalente ad 1 sec di analisi
					if(identificGyr_y.size()>n_camp_MaxNum) {identificGyr_y.remove(0);}
					break;
				}
				// only axis x
				case 1:
				{
					identificGyr_ok.add(event.values[0]);
					if(identificGyr_ok.size()>n_camp_MaxNum) {identificGyr_ok.remove(0);}
					break;
				}
				// only axis y
				case 2:
				{
					identificGyr_ok.add(event.values[1]);
					if(identificGyr_ok.size()>n_camp_MaxNum) {identificGyr_ok.remove(0);}
					break;
				}
				default:
				{
					DisplayToast("ERROR!");
					break;
				}
			}
			
			tempo_precedente_gyr = tempo_attuale;
		}
		// se sto aspettando una registrazione (StatoSistema=3), salvo solo 10 valori consecutivi:
		if(StatoSistema==3)
		{
			// check if the timer is elapsed:
			if ((tempo_attuale-lastSamplePreRec_Gyr) >= dtCampMinFasePreRec)
			{
				lastSamplePreRec_Gyr = tempo_attuale;
				listTempoGyro.add(String.valueOf(tempo_attuale));
				listDatiSensGyr_x.add(g_val_x);
				listDatiSensGyr_y.add(g_val_y);
				listDatiSensGyr_z.add(g_val_z);
				// remove the 11� element: only the last 10 were stored (10*0.1sec)
				if(listTempoGyro.size()>numMaxCampFasePreRec) 		{listTempoGyro.remove(0);}
				if(listDatiSensGyr_x.size()>numMaxCampFasePreRec) 	{listDatiSensGyr_x.remove(0);}
				if(listDatiSensGyr_y.size()>numMaxCampFasePreRec) 	{listDatiSensGyr_y.remove(0);}
				if(listDatiSensGyr_z.size()>numMaxCampFasePreRec) 	{listDatiSensGyr_z.remove(0);}
			}
		}
		else if(StatoSistema==4)
		// se sto registrando (StatoSistema=4), salvo i valori:
		{
			listTempoGyro.add(String.valueOf(tempo_attuale));
			listDatiSensGyr_x.add(g_val_x);
			listDatiSensGyr_y.add(g_val_y);
			listDatiSensGyr_z.add(g_val_z);
		}
	}

	else if (sensorType == Sensor.TYPE_ACCELEROMETER)
	{
		t_prec_A = tempo_attuale;
		//gestione del salvataggio dei valori:
		a_val_x = String.valueOf(event.values[0]);
		a_val_y = String.valueOf(event.values[1]);
		a_val_z = String.valueOf(event.values[2]);		
		//save float values (for Broadcast update) TODO: rimuovere in versione finale
		A_val_x = event.values[0];
		A_val_y = event.values[1];
		A_val_z = event.values[2];

		//riempio vettore per l'identificazione (riempio un vettore di sei campioni: 1 secondo di comportamento del sistema)
		if (can_update_array==true && (tempo_attuale-tempo_precedente_acc)>dt_rec){
			// check if the system has already found the device orientation and save the correct values of gyro
			switch (flag_orientation_determined)
			{
				// save both axes (x and y)
				case 0:
				{
					identificAcc_x.add(event.values[0]);
					identificAcc_y.add(event.values[1]);
					if(identificAcc_x.size()>n_camp_MaxNum) {identificAcc_x.remove(0);}	// mantengo il vettore di dimensione sempre 6, equivalente ad 1 sec di analisi
					if(identificAcc_y.size()>n_camp_MaxNum) {identificAcc_y.remove(0);}
					break;
				}
				// only axis x
				case 1:
				{
					identificAcc_ok.add(event.values[0]);
					if(identificAcc_ok.size()>n_camp_MaxNum) {identificAcc_ok.remove(0);}
					break;
				}
				// only axis y
				case 2:
				{
					identificAcc_ok.add(event.values[1]);
					if(identificAcc_ok.size()>n_camp_MaxNum) {identificAcc_ok.remove(0);}
					break;
				}
			}
			
			tempo_precedente_acc = tempo_attuale;
		}
		
		// se sto aspettando una registrazione (StatoSistema=3), salvo solo 10 valori consecutivi:
		if(StatoSistema==3)
		{
			// check if the timer is elapsed:
			if ((tempo_attuale-lastSamplePreRec_Acc) >= dtCampMinFasePreRec)
			{
				lastSamplePreRec_Acc = tempo_attuale;
				listTempoAcc.add(String.valueOf(tempo_attuale));
				listDatiSensAcc_x.add(a_val_x);
				listDatiSensAcc_y.add(a_val_y);
				listDatiSensAcc_z.add(a_val_z);
				// remove the 11� element: only the last 10 were stored (10*0.1sec)
				if(listTempoAcc.size()>numMaxCampFasePreRec) 		{listTempoAcc.remove(0);}
				if(listDatiSensAcc_x.size()>numMaxCampFasePreRec) 	{listDatiSensAcc_x.remove(0);}
				if(listDatiSensAcc_y.size()>numMaxCampFasePreRec) 	{listDatiSensAcc_y.remove(0);}
				if(listDatiSensAcc_z.size()>numMaxCampFasePreRec) 	{listDatiSensAcc_z.remove(0);}
			}
		}
		else if(StatoSistema==4)
		// se sto registrando (StatoSistema=4), salvo i valori:
		{
			listTempoAcc.add(String.valueOf(tempo_attuale));
			listDatiSensAcc_x.add(a_val_x);
			listDatiSensAcc_y.add(a_val_y);
			listDatiSensAcc_z.add(a_val_z);
		}
	}

	// refresh the listener only if the device can record with the screen OFF:
	if (ScreenIsOn==1 || RecWithScreenOnOff!=1)
	{
		//check the stuck of the sensors
		if		((tempo_attuale - t_prec_A) >= tempo_max_sensor_stuck)
			sensorStuckUpdateSensorsListener(1);
		else if ((tempo_attuale - t_prec_M) >= tempo_max_sensor_stuck)
			sensorStuckUpdateSensorsListener(2);
		else if	((tempo_attuale - t_prec_G) >= tempo_max_sensor_stuck)
			sensorStuckUpdateSensorsListener(3);
	}
	
	//check if send or not the Broadcast sensor update
	if((tempo_attuale - t_prec_SensorBroadcast) >= tempo_SensorBroadcast)
	{
		sendBroadcastMessage();
		t_prec_SensorBroadcast = tempo_attuale;
	}
}

//********************************************************************** Refresh the timers for the stuck sensors:
private void setTimerToNowForStuckSensor(){
	t_prec_A = System.currentTimeMillis();
	t_prec_G = t_prec_A;
	t_prec_M = t_prec_A;
}

//**********************************************************************  When a sensor is stuck, call this function to refresh the sensors and increase the variable (for the number of sensor stuck) that will be displayed in the cell
private void sensorStuckUpdateSensorsListener(int val) {
	// val = 1 ----> Acc is stuck
	// val = 2 ----> Mag is stuck
	// val = 3 ----> Gyr is stuck
	
	// update the counter of the sensor and refresh the listener:
	if (val == 1)
	{
		if (A_num_stuck <= 1000)
			A_num_stuck = A_num_stuck + 1;
		
		mgr.unregisterListener(this, mAcc);
		mgr.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_GAME);
	}
	else if (val == 2)
	{
		if (M_num_stuck <= 1000)
			M_num_stuck = M_num_stuck + 1;
		
		mgr.unregisterListener(this, mMag);
		mgr.registerListener(this, mMag, SensorManager.SENSOR_DELAY_GAME);
	}
	else if (val == 3)
	{
		if (G_num_stuck <= 1000)
			G_num_stuck = G_num_stuck + 1;
		
		mgr.unregisterListener(this, mGyr);
		mgr.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_GAME);
	}
	
//	flag_sensor_stuck = true;
}

//********************************************************************** Check if the device is is stationary in a position (this means that he has wants enter in 		'SLEEP' 		state)
private void checkStationarityOfDevice () {
	
	// set a default value for the variable (DeviceIsStationary = 0, this means that the device is not stationary)
	int DeviceIsStationary = 0;
	
	// research the stationarity of the system if it is into STANBY mode
	if (StatoSistema==2)
	{
		// check if the device has the possibility to rec with the screen turned off (RecWithScreenOnOff==2), if not, if also the screen is off, it means that the user wants to put his device into SLEEP mode
		if (ScreenIsOn==0 && RecWithScreenOnOff==1)
		{
			flag_identific = 1; 								// here I can modify the state of the system
		}
		// if the device has the screen on OR the device can play with screen off, the code to enter into this "if" statment
		else
		{
			DeviceIsStationary = checkValueOfStationarity();
			// if the device is not stationary
			if (DeviceIsStationary == 0)
			{
				flag_device_stopped	= 0;
			}
			else
			{
				// the device is stopped, from now starts the timer (time_before_sleep)
				if (flag_device_stopped == 0)
				{
					flag_device_stopped	= 1;					// from now I count until the timer expire the "time_before_sleep"
					tempo_prima_identificaz_staz = tempo_attuale;
				}
				else
				{
					// check if the timer is expired
					if ((tempo_attuale-tempo_prima_identificaz_staz) >= time_before_sleep)
					{
						flag_identific = 1; 					// here I can modify the state of the system
					}
				}
			}
		}
	}
	// if the system is into SLEEP mode, the code search the movements of the device
	else if (StatoSistema==1)
	{
		// set a default value for the variable (DeviceIsStationary = 1, this means that the device is stationary) 
		DeviceIsStationary = 1;
		// check if the device has the possibility to rec with the screen turned off (RecWithScreenOnOff==2), if not, the screen has to be on
		if (  RecWithScreenOnOff==2 ||
			 (RecWithScreenOnOff==1 && ScreenIsOn==1)
			)
		{
			DeviceIsStationary = checkValueOfStationarity();
			// if the device is not stationary
			if (DeviceIsStationary == 0)
			{
				flag_identific = 1;
				flag_device_stopped	= 0;    // in order to prevent an instantaneous come back to the DEEP_SLEEP (resetting the timer of 60 sec)
			}
		}
	}
	
	tempo_ultimocheck_stationarity = tempo_attuale;
}

//********************************************************************** THis function analyze the vector of the accelerometer and provide a the "stationarity" feedback
private int checkValueOfStationarity () {
	
	// theDeviceIsStationary = 0 (NO STATIONARITY,	IT'S MOVING)
	// theDeviceIsStationary = 1 (STATIONARITY,		IT'S STOP)
	// the value is determined checking: |vettGyro[1]|, |vettGyro[3]|, |vettGyro[5]|, and |diff(acc(i),acc(i+1))|
	
	// If 		the system in in STANDBY mode, it checks the values of the gyro and the acc for the 1 second (all array)
	// else if 	the system in in SLEEP   mode, it checks the values of the gyro and the acc for the 0.6 second (3 values of the array)
	
	int theDeviceIsStationary 	= 0;
	int dim_vett 				= 0;
	float fivethGyro, thirdGyro, secondGyro, firstGyro, sixthAcc, fivethAcc, fourthAcc, thirdAcc, secondAcc, firstAcc;		// will be the values taken from the check vectors

	if (flag_orientation_determined == 0)
	{
		dim_vett = Math.min((identificGyr_x.size() - 1), (identificAcc_x.size() - 1));
		
		// give to each element his vector value:
		fivethGyro 	= Math.abs(identificGyr_x.get(dim_vett - 4));
		thirdGyro 	= Math.abs(identificGyr_x.get(dim_vett - 2));		
		secondGyro 	= Math.abs(identificGyr_x.get(dim_vett - 1));		
		firstGyro 	= Math.abs(identificGyr_x.get(dim_vett));
		sixthAcc 	= Math.abs(identificAcc_x.get(dim_vett - 5));
		fivethAcc 	= Math.abs(identificAcc_x.get(dim_vett - 4));
		fourthAcc 	= Math.abs(identificAcc_x.get(dim_vett - 3));
		thirdAcc 	= Math.abs(identificAcc_x.get(dim_vett - 2));
		secondAcc 	= Math.abs(identificAcc_x.get(dim_vett - 1));
		firstAcc 	= Math.abs(identificAcc_x.get(dim_vett));
		
		Log.e(LOG_TAG, DoveSono + "Check_STAT:\t" + String.valueOf(identificGyr_x.size()) + " (dim),\t" + String.valueOf(dim_vett) + " (from),\t" + String.valueOf(dim_vett - 5) + " (to).");
	}	
	else
	{
		dim_vett = Math.min((identificGyr_ok.size() - 1), (identificAcc_ok.size() - 1));
		
		// give to each element his vector value:
		fivethGyro 	= Math.abs(identificGyr_ok.get(dim_vett - 4)); 
		thirdGyro 	= Math.abs(identificGyr_ok.get(dim_vett - 2));
		secondGyro 	= Math.abs(identificGyr_ok.get(dim_vett - 1));
		firstGyro 	= Math.abs(identificGyr_ok.get(dim_vett));
		sixthAcc 	= Math.abs(identificAcc_ok.get(dim_vett - 5));
		fivethAcc 	= Math.abs(identificAcc_ok.get(dim_vett - 4));
		fourthAcc 	= Math.abs(identificAcc_ok.get(dim_vett - 3));
		thirdAcc 	= Math.abs(identificAcc_ok.get(dim_vett - 2));
		secondAcc 	= Math.abs(identificAcc_ok.get(dim_vett - 1));
		firstAcc 	= Math.abs(identificAcc_ok.get(dim_vett));
		
		Log.e(LOG_TAG, DoveSono + "Check_STAT:\t" + String.valueOf(identificGyr_ok.size()) + " (dim),\t" + String.valueOf(dim_vett) + " (from),\t" + String.valueOf(dim_vett - 5) + " (to).");
	}
	
	if (StatoSistema==2)
	{	
		if( (	fivethGyro 				< (float)soglia_stazionarieta_Gyro 		&&
				thirdGyro 				< (float)soglia_stazionarieta_Gyro 		&&
				firstGyro 				< (float)soglia_stazionarieta_Gyro 	)	||
			(	(sixthAcc  - fivethAcc) < (float)soglia_stazionarieta_Acc 		&&
				(fourthAcc - thirdAcc) 	< (float)soglia_stazionarieta_Acc 		&&
				(secondAcc - firstAcc) 	< (float)soglia_stazionarieta_Acc 	)
			)
			theDeviceIsStationary = 1;
		else
			theDeviceIsStationary = 0;	
	}
	else if (StatoSistema==1)
	{				
		if( (	thirdGyro				> (float)soglia_stazionarieta_Gyro 		&&
				secondGyro				> (float)soglia_stazionarieta_Gyro 		&&
				firstGyro				> (float)soglia_stazionarieta_Gyro 	) 	||
			(	(fourthAcc - thirdAcc) 	> (float)soglia_stazionarieta_Acc 		&&
				(secondAcc - firstAcc) 	> (float)soglia_stazionarieta_Acc 	)
			)
			theDeviceIsStationary = 0;
		else
			theDeviceIsStationary = 1;
	}
	
	Log.w(LOG_TAG, DoveSono + "Check_STAT: OK!");
		
	return theDeviceIsStationary;
}

//********************************************************************** Check if the user has makes a rotation of 360� in at least 2 seconds (this means that he wants to enter (or exit) from the 	'LISTENING' 	state)
private void checkForTheRotationOf360Degree() {
	
	flag_identific 	= 0;
	angolo 			= 0;
	int i, j, from, to;
	
	// take the correct vector(X/Y or OK)
	if (flag_orientation_determined == 0)
	{
		from 	= Math.min((identificGyr_x.size() - 1), (identificAcc_x.size() - 1));
		to 		= from - n_camp_rotation + 1;
	}
	else
	{
		from 	= Math.min((identificGyr_ok.size() - 1), (identificAcc_ok.size() - 1));
		to 		= from - n_camp_rotation + 1;
	}
		
	// if the orientation is not already determined, check for oth the axes (x and y) values:
	if (flag_orientation_determined == 0)
	{
		// controllo se ho ruotato attorno alle x:
		for(i=from; i>=to; i--){
			angolo = angolo + identificGyr_x.get(i)*dt_rec_InSec;
			// 315� di rotazione effettuati:
				if(Math.abs(angolo) >= almost_two_pi){					
				flag_identific=2;
				break;
			}				
		}
		
		//se ho trovato tutti valori in soglia, effettuo l'altro controllo a cascata sugli acc
		if(flag_identific==2){
			// controllo se il salvataggio degli acc ha almeno 4 valori sopra la soglia (ricerco 0.8 sec di posizione verticale):
			for(j=i; j<=from; j++){
				if(Math.abs(identificAcc_x.get(i))<soglia_acc_vert)	{						
					flag_identific=0;
					break;
				}
			}
			
			// if flag_identific == 2, it means that the device has detected a good rotation: the selected axis is saved
			if (flag_identific == 2)
			{
				flag_orientation_determined = 1;	// x axis is saved
				DisplayToast("Angolo calc. (asse X): " + String.valueOf(Math.round(angolo*(double)180/pi)) + ", Time: " + String.valueOf((n_camp_rotation-i-1)*dt_rec) + " msec.");
				Log.e(LOG_TAG, DoveSono + "ASSE X INDIVIDUATO");
			}
		}
		
		// if a rotation around the x axis is not detected, the y axis is analyzed
		if (flag_identific == 0)
		{
			angolo 	= 0;
			
			// controllo se ho ruotato attorno alle x:
			for(i=from; i>=to; i--){
				angolo = angolo + identificGyr_y.get(i)*dt_rec_InSec;
				// 360� di rotazione effettuati:
					if(Math.abs(angolo) >= almost_two_pi){				
					flag_identific=2;
					break;
				}			
			}		
			
			//se ho trovato tutti valori in soglia, effettuo l'altro controllo a cascata sugli acc
			if(flag_identific==2){
				// controllo se il salvataggio degli acc ha almeno 4 valori sopra la soglia (ricerco 0.8 sec di posizione verticale):
				for(j=i; j<=from; j++){
					if(Math.abs(identificAcc_y.get(i))<soglia_acc_vert)	{
						flag_identific=0;
						break;
					}
				}
				
				// if flag_identific == 2, it means that the device has detected a good rotation: the selected axis is saved
				if (flag_identific == 2)
				{
					flag_orientation_determined = 2;	// y axis is saved
					DisplayToast("Angolo calc. (asse Y): " + String.valueOf(Math.round(angolo*(double)180/pi)) + ", Time: " + String.valueOf((n_camp_rotation-i-1)*dt_rec) + " msec.");
				}
			}
		}
	}
	else
	{
		// controllo se ho ruotato attorno all'asse preselezionato:
		for(i=from; i>=to; i--){
			angolo = angolo + identificGyr_ok.get(i)*dt_rec_InSec;
			// 360� di rotazione effettuati:
				if(Math.abs(angolo) >= almost_two_pi){					
				flag_identific=2;
				break;
			}			
		}
								
		//se ho trovato tutti valori in soglia, effettuo l'altro controllo a cascata sugli acc
		if(flag_identific==2){
			// controllo se il salvataggio degli acc ha almeno 4 valori sopra la soglia (ricerco 0.8 sec di posizione verticale):
			for(j=i; j<=from; j++){
				if(Math.abs(identificAcc_ok.get(i))<soglia_acc_vert)	{
					flag_identific=0;
					break;
				}
			}
			
			// if flag_identific == 2, it means that the device has detected a good rotation: the selected axis is saved
			if (flag_identific == 2)
			{
				DisplayToast("Angolo calc. (asse OK): " + String.valueOf(Math.round(angolo*(double)180/pi)) + ", Time: " + String.valueOf((n_camp_rotation-i-1)*dt_rec) + " msec.");
			}
		}
	}
	
	tempo_ultimocheck_Rotation360 = tempo_attuale;
}

//********************************************************************** Check if the user put his body in horizontal position (this means that he wants to start the 	'RECORD' 		state)
private void checkForHorizontalPosition () {
	
	int i, from, to;

	from 	= (identificAcc_ok.size() - 1);
	to 		= (identificAcc_ok.size() - n_camp_horizontal);
	
	// flag_identific=3, means that the user is in horizontal position
	flag_identific=3;

	// when the system is here, it has already determined the device orientatin
		for(i=from; i>=to; i--){
			if(Math.abs(identificAcc_ok.get(i))>soglia_acc_orizz){				
				flag_identific=0;
				break;
			}	
	}
	
	tempo_ultimocheck_horizontality = tempo_attuale;
}

//********************************************************************** Check if the user put his body in vertical position (this means that he wants to stop the 		'RECORD' 		state)
private void checkForVerticalPosition () {
	
	int i, from, to;
	
	from 	= (identificAcc_ok.size() - 1);
	to 		= (identificAcc_ok.size() - n_camp_vertical);
	
	// flag_identific=4, means that the user is in vertical position
	flag_identific=4;
		
	// when the system is here, it has already determined the device orientatin
	for(i=from; i>=to; i--){
		if(Math.abs(identificAcc_ok.get(i))<soglia_acc_vert)
		{
				flag_identific			= 0;
				break;				
			}
	}
	
	tempo_ultimocheck_verticality = tempo_attuale;
}

//********************************************************************** it's used to cancel the vectors used for the identification of the user movements 
void clear_check_vectors (int flag_orientation_determined){
	
	can_update_array = false;				// It's a kind of semaphore on the update of the "identific#_#" vectors: doesn't permit updates.
	
	if (flag_orientation_determined == 0)
	{
		identificGyr_x.clear();
		identificAcc_x.clear();
		identificGyr_y.clear();
		identificAcc_y.clear();					
	}
	else
	{					
		identificGyr_ok.clear();
		identificAcc_ok.clear();
	}
	
	can_update_array = true;				// It's a kind of semaphore on the update of the "identific#_#" vectors: permit new updates.
}

//********************************************************************** If user's movement is found in the saved arrays (acc and gyro), the state machine changes state
private void sequenceFoundChangeState (){
	
		// aggiorno il vett tempo post ultima identificazione
		tempo_ultima_identificaz = tempo_attuale;
		
		//cancelo i buffer, per evitare un erroneo secondo ritrovo della sequenza al passo successivo:
		clear_check_vectors(flag_orientation_determined);
		
		Log.w(LOG_TAG, DoveSono + "Flag identificata: " + String.valueOf(flag_identific));
		////Change Flag colours here
		switch(flag_identific){
			
			case 1:{
				// the device is stationary: move the system from STANDBY --> SLEEP. Or it is no longer stationary: move the system from SLEEP --> STANDBY.
				if(StatoSistema==2)
				{
					timePreviousBeep = tempo_attuale;			// to take trace of last vibrational feedback
					vibrator.vibrate(pattern_ScreenOff,-1);
					DisplayToast("STANDBY --> SLEEP");
					Log.w(LOG_TAG, DoveSono + "State Transition: " + String.valueOf(StatoSistema) + " --> 1");
					StatoSistema=1;	// the device is going to SLEEP
                    SwimAppApplication obj = (SwimAppApplication) getApplicationContext();

                    ImageView light1=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightRed);
                    light1.setImageResource(R.drawable.light_red);
                    ImageView light2=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightYellow);
                    light2.setImageResource(R.drawable.light_gray);
                    ImageView light3=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightGreen);
                    light3.setImageResource(R.drawable.light_gray);
				}
				else if(StatoSistema==1)
				{
					timePreviousBeep = tempo_attuale;			// to take trace of last vibrational feedback
					vibrator.vibrate(pattern_ScreenOn,-1);
					DisplayToast("SLEEP --> STANDBY");
					Log.w(LOG_TAG, DoveSono + "State Transition: " + String.valueOf(StatoSistema) + " --> 2");
					StatoSistema=2;	// the device goes into STANBY mode
                    SwimAppApplication obj = (SwimAppApplication) getApplicationContext();
                    ImageView light1=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightRed);
                    light1.setImageResource(R.drawable.light_red);
                    ImageView light2=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightYellow);
                    light2.setImageResource(R.drawable.light_gray);
                    ImageView light3=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightGreen);
                    light3.setImageResource(R.drawable.light_gray);
				}
				break;
			}
			case 2:{
				timePreviousBeep = tempo_attuale;			// to take trace of last vibrational feedback
				vibrator.vibrate(1000); 
				//se ho avuto la rotazione sul posto, in base alla modalit� in cui ero, passo ad un'altra:
				if(StatoSistema==2)
				{
					Log.w(LOG_TAG, DoveSono + "State Transition: " + String.valueOf(StatoSistema) + " --> 3");
					clearSensorArrays();																				//clear all the arrays with sensor values
					StatoSistema=3;
                    SwimAppApplication obj = (SwimAppApplication) getApplicationContext();
                    ImageView light1=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightRed);
                    light1.setImageResource(R.drawable.light_gray);
                    ImageView light2=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightYellow);
                    light2.setImageResource(R.drawable.light_yellow);
                    ImageView light3=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightGreen);
                    light3.setImageResource(R.drawable.light_gray);

																									//passo alla ricerca delle pose orizz./vertic.
					// TODO: eliminare questo if nella versione finale
					if (flag_orientation_determined == 1)
						DisplayToast("STANDBY --> LISTEN\n(horizontal)");
					else
						DisplayToast("STANDBY --> LISTEN\n(vertical)");
				}
				else
				{
					Log.w(LOG_TAG, DoveSono + "State Transition: " + String.valueOf(StatoSistema) + " --> 2");
					StatoSistema=2;	//torno alla fase di standby
                    SwimAppApplication obj = (SwimAppApplication) getApplicationContext();
                    ImageView light1=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightRed);
                    light1.setImageResource(R.drawable.light_red);
                    ImageView light2=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightYellow);
                    light2.setImageResource(R.drawable.light_gray);
                    ImageView light3=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightGreen);
                    light3.setImageResource(R.drawable.light_gray);
					// TODO: eliminare questo if nella versione finale
					if (flag_orientation_determined == 1)
						DisplayToast("LISTEN --> STANDBY\n(horizontal)");
					else
						DisplayToast("LISTEN --> STANDBY\n(vertical)");
				}
				break;
			}
			case 3:{
				//attivo la registrazione dai sensori
				sensorREC(1);
				Log.w(LOG_TAG, DoveSono + "State Transition: " + String.valueOf(StatoSistema) + " --> 4");
				StatoSistema = 4;	//passo alla modalit� registrazione
                SwimAppApplication obj = (SwimAppApplication) getApplicationContext();
                ImageView light1=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightRed);
                light1.setImageResource(R.drawable.light_gray);
                ImageView light2=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightYellow);
                light2.setImageResource(R.drawable.light_gray);
                ImageView light3=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightGreen);
                light3.setImageResource(R.drawable.light_green);

                // TODO: eliminare questo if nella versione finale
				if (flag_orientation_determined == 1)
					DisplayToast("LISTEN --> REC\n(horizontal)");
				else
					DisplayToast("LISTEN --> REC\n(vertical)");
				
				Log.v(LOG_TAG, "ATTIVATA REC!");
				break;
			}
			case 4:{
				//stoppo la registrazione dai sensori e salvo i dati
				stopRec_UpdateOrNotNumOfRecord(1);
				Log.w(LOG_TAG, DoveSono + "State Transition: " + String.valueOf(StatoSistema) + " --> 3");
				
				// TODO: eliminare questo if nella versione finale
				if (flag_orientation_determined == 0)
					DisplayToast("REC --> LISTEN\n(horizontal)");
				else
					DisplayToast("REC --> LISTEN\n(vertical)");
				
				Log.v(LOG_TAG, "FERMATA REC!");
				//il cambio dello stato del sistema lo faccio dentro la funzione di sensorRec(0), in modo che fino a quando non ho salvato tutti i dati, il sistema rimane in uno stato vacante (StatoSistema=0)
				//StatoSistema = 3;	//passo alla modalit� attesa registrazione

                SwimAppApplication obj = (SwimAppApplication) getApplicationContext();
                ImageView light1=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightRed);
                light1.setImageResource(R.drawable.light_gray);
                ImageView light2=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightYellow);
                light2.setImageResource(R.drawable.light_yellow);
                ImageView light3=(ImageView)obj.getCurrentActivity().findViewById(R.id.lightGreen);
                light3.setImageResource(R.drawable.light_gray);
                break;
			}
			default: {
				DisplayToast("ERROR!");
				break;
			}
	}
}

//********************************************************************** Alerts the user if the battery is low (if the user wants to check it)
private void alertsTheUserWithTotVibr_Battery (int vibrTotTimes) {
	
	if ((tempo_attuale-timePreviousBeep)>=3000){
		if (numAvvisiBatteryLow<=vibrTotTimes)
		{
			vibrator.vibrate(1500);
			numAvvisiBatteryLow = numAvvisiBatteryLow+1;
			tg.startTone(ToneGenerator.TONE_PROP_BEEP);
		}
		DisplayToast("Batteria quasi scarica (" + level + "%), impedisco altri REC!");
		timePreviousBeep = tempo_attuale;
	}
}

//********************************************************************** Alerts the user if the battery is low (if the user wants to check it)
private void alertsTheUser_Airplane () {
	
	if(FlagSensorRec==1){
    	stopRec_UpdateOrNotNumOfRecord (0);		// if the device is recording (FlagSensorRec==1), stop it and save data
    }
	
	StatoSistema = 2;							// set the state to Standby. Until the Airplane mode isn't inserted, the user can't do nothing
	
	if ((tempo_attuale-timePreviousBeep)>=3000){
		DisplayToast("AirPlane mode is necessary for 'Spin and Swim'..");
		vibrator.vibrate(300);
		timePreviousBeep = tempo_attuale;
	}
}

//********************************************************************** Alerts the user if the screen is turned off (and the device can't record in this mode) or the battery is low (if the user wants to check it)
private void alertsTheUser_ScreenOff () {
	
	if (numAvvisiScreenOff<=10 && (tempo_attuale-timePreviousBeep)>=1000){
		vibrator.vibrate(500);
		numAvvisiScreenOff = numAvvisiScreenOff+1;
		tg.startTone(ToneGenerator.TONE_PROP_BEEP);
		timePreviousBeep = tempo_attuale;
	}
}

//********************************************************************** Registrazione da Sensori
public void sensorREC(int REC) {
	vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	switch (REC) {
		case 1:
		{
			//mi salvo i millisecs all'inizio della registrazione, in modo da poter poi scrivere nel nome del file la sua durata:
			tempo_start	= tempo_attuale;
			//la flag sotto fa si che si attivi la registrazione ad ogni aggiornamento dei sensori
			FlagSensorRec=1;
			Log.v(LOG_TAG, "attivo REC sensori");
			break;
		}
		case 0:
		{
			//metto il sistema in uno stato "nullo", fino a quando la registrazione non � stata completata (alla fine la risetto su StatoSistema==3):
			StatoSistema = -1;
			//mi salvo i millisecs alla fine della registrazione, in modo da poter poi scrivere nel nome del file la sua durata:
			tempo_end 	= tempo_attuale;
			//stoppo la registrazione settando la flag a 0
			FlagSensorRec = 0;
			//give to the user a vibrational feedback
			timePreviousBeep = tempo_attuale;			// to take trace of last vibrational feedback
			vibrator.vibrate(2000);
			Log.v(LOG_TAG, "stop REC sensori, inizio salvataggio!");
			
			// save the data on a file (internal or external storage is a user decision):
			salvaDati(GlobalVariables.saveOnIntExtStorage);
			
			// to avoid the refresh of the sensors after this phase, refresh the timer of the last sensors updates:
			setTimerToNowForStuckSensor();
			
			//permetto al sistema di tornare ad avvertire l'utente, aspettando altra registrazione:
			StatoSistema=3;
			break;
		}
	}
}

//********************************************************************** Send the sensor update message via Broadcast	
private void sendBroadcastMessage() {
	Intent intent = new Intent(SENSORS_UPDATE_BROADCAST);
    
	intent.putExtra(ACC_x, A_val_x);	intent.putExtra(ACC_y, A_val_y);	intent.putExtra(ACC_z, A_val_z);
    intent.putExtra(GYR_x, G_val_x);	intent.putExtra(GYR_y, G_val_y);	intent.putExtra(GYR_z, G_val_z);
    intent.putExtra(MAG_x, M_val_x);	intent.putExtra(MAG_y, M_val_y);	intent.putExtra(MAG_z, M_val_z);
    
//    if (flag_sensor_stuck == true)
//    {
    	intent.putExtra(GYR_N_stuck, G_num_stuck);	intent.putExtra(ACC_N_stuck, A_num_stuck);	intent.putExtra(MAG_N_stuck, M_num_stuck);
    	
//    	flag_sensor_stuck = false;			// update the flag, to avoid furter not usefull updates
//    }
    
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
}

//********************************************************************** Feedback all'utente (sound and\or vibration)
private void feedbackTotheUser () {
	
	vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	
	// Each System State has his type of feedback:
	switch (StatoSistema) {
	
		case 1:
			// Sato: SLEEP
			{
				if ((tempo_attuale-timePreviousBeep)>=5000){
					vibrator.vibrate(100);
					timePreviousBeep=tempo_attuale;
				}
				break;
			}
			
		case 2:
			// State: STANDBY
			{
				if ((tempo_attuale-timePreviousBeep)>=3000){
					vibrator.vibrate(300);
					tg.startTone(ToneGenerator.TONE_PROP_BEEP);	//tg.startTone(ToneGenerator.TONE_PROP_NACK);
					timePreviousBeep=tempo_attuale;
					//Log.v(LOG_TAG, "Feedback (modalit� " + StatoSistema +"): " + ora.format(new Date().getTime()));
				}
				break;
			}
			
		case 3:
			// State: LISTEN
			{
				if ((tempo_attuale-timePreviousBeep)>=1000){
					vibrator.vibrate(300);
					tg.startTone(ToneGenerator.TONE_PROP_BEEP);	//tg.startTone(ToneGenerator.TONE_PROP_NACK);
					timePreviousBeep=tempo_attuale;
					Log.v(LOG_TAG, "Feedback (modalit� " + StatoSistema +"): " + ora.format(new Date().getTime()));
				}
				break;
			}
			
		case 4:
			// State: RECORDING
			{
				if ((tempo_attuale-timePreviousBeep)>=1000){
					tg.startTone(ToneGenerator.TONE_PROP_BEEP);	//tg.startTone(ToneGenerator.TONE_PROP_NACK);
					timePreviousBeep=tempo_attuale;
					Log.v(LOG_TAG, "Feedback (stato " + StatoSistema +"): " + ora.format(new Date().getTime()));
				}
				break;
			}
		
		default:
		{
			// State: UNDESIDERATED
			break;
		}
	}
}

//******************************************** Salvo identificativo del cell ********************
private String scriviIdCriptato(){
	//prelevo l'ID del cellulare:
  	String id_cell = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
  	//creo la stringa con i caratteri disponibili:
  	String caratteri = new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
  	int n = caratteri.length();
  	String Stringafinale = new String();
  	Random r = new Random();
  	int conta_16 		= 0; //utilizzato per contare 16 posizionamenti dell'ID del cell (16 caratteri alfanumerici)
  	String vettTempo = listTempoAcc.get(0) + listTempoAcc.get(0).substring(0, 3);
  	//String vettTempo 	= "1234567891234567";
  	int[] vettPosId 	= new int[vettTempo.length()];
  	int somma 			= 0;
  	//questo for sotto � quello che mi servir� anche su Matlab per ricercare il DOVE sono i valori salvati:
  	for(int i=0; i<vettTempo.length(); i++){
  		//vettPosId[i] = Character.getNumericValue(vettTempo.charAt(i));
  		if (Character.getNumericValue(vettTempo.charAt(i))==0)
  			somma += Character.getNumericValue(vettTempo.charAt(i)) + 1;
  		somma += Character.getNumericValue(vettTempo.charAt(i));
  		vettPosId[i] = somma;
  		//Log.v(LOG_TAG, DoveSono + "Valore " + (i+1) + "� della stringa: " + Character.getNumericValue(vettTempo.charAt(i)));
  		Log.v(LOG_TAG, DoveSono + "Somma valori inseriti, fino al " + (i+1) + "� : " + vettPosId[i] );
  	}
  	Log.v(LOG_TAG, DoveSono + "ID del cell: " + id_cell);
  	//creo una stringa di 20 caratteri. Inserisco quelli relativi all'ID del cellulare in tal modo:
  	for(int i=0;i<160;i++){
  		if(conta_16<=15){
			if(i==vettPosId[conta_16]){
				Stringafinale = Stringafinale + id_cell.charAt(conta_16);
				conta_16 = conta_16+1;
			}
			else
				Stringafinale = Stringafinale + caratteri.charAt(r.nextInt(n));
  		}
  		else
  			Stringafinale = Stringafinale + caratteri.charAt(r.nextInt(n));
  	}
  	Log.v(LOG_TAG, DoveSono + "Valori (160) random: " + Stringafinale);
	return Stringafinale;
}

//********************************************************************** Salvo i dati presi con i diversi Delay:
private void salvaDati (int InternalExternalMemory){
	
	// InternalExternalMemory == 0, save on internal memory;
	// InternalExternalMemory == 1, save on external memory;
	// default case, save on internal memory;
	
	// check for the preence of the external memory and the possibility to read and write on it:




  String FileForAnalysis;

	boolean ExternalStorageAvailable = false;
	boolean ExternalStorageWriteable = false;


    //****************** CODESPIKE ALTERATION **********************//
    // To force save on internal memory
       InternalExternalMemory = 1;
	String ext_mem_state = Environment.getExternalStorageState();

	if (Environment.MEDIA_MOUNTED.equals(ext_mem_state))
	{
		// We can read and write the media
		ExternalStorageAvailable = ExternalStorageWriteable = true;
	}
	else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(ext_mem_state))
	{
	    // We can only read the media
		ExternalStorageAvailable = true;
		ExternalStorageWriteable = false;
		
		// adjust the location where write the file:
		InternalExternalMemory = 0;
	}
	else
	{
	    // Something else is wrong. It may be one of many other states, but all we need
	    //  to know is we can neither read nor write
		ExternalStorageAvailable = ExternalStorageWriteable = false;
		
		// adjust the location where write the file:
		InternalExternalMemory = 0;
	}
	
	int dim_gyro = listTempoGyro.size();			//num saved values of gyros
	int dim_acc  = listTempoAcc.size();				//num saved values of acc
	int dim_mag  = listTempoMag.size();				//num saved values of magn
	
	//se la dimensione dei vettori dei sensori non � "buona", non salvo ed avviso l'utente:
	if (listDatiSensGyr_x.size()!=0 &&
		listDatiSensAcc_x.size()!=0 &&
		listDatiSensMag_x.size()!=0)
	{
		//creo il file sul quale salver� i dati da sensori (dentro la sottocartella appena creata), il nome � composta con "data_ora" corrente
		SimpleDateFormat sdf 		= new SimpleDateFormat("yyy-MM-dd__HH-mm-ss");		//cambio i formato della data
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString("uid", "undefined");

        String	currentDateandTime 	= sdf.format(new Date().getTime());
	String user_id				= userId;										//this will be used for save the user_id, to identificate how made the run
		long Duration =(tempo_end-tempo_start)/1000;
		// name of the saving file (date+duration of the record):
		String FileName = currentDateandTime + "__" + String.valueOf((tempo_end-tempo_start)/1000) + "sec__" + user_id + ".txt";
        FileForAnalysis=FileName;
		//setto a zero i valori di "tempo_start" e "tempo_end":

        // Add a temporary variable here to recvoed the tiom4

        tempo_start	= 0;
		tempo_end	= 0;
		
		// SAVE THE FILE:
		switch(InternalExternalMemory)
		{
			// SAVE FILE ON EXTERNAL SOTRAGE:
			case 1:
			{
				//Creo le cartelle e sottocartelle ad hoc:
				String newFolder = "/ SWIM APP";
				String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
				File myNewFolderApp = new File(extStorageDirectory + newFolder);
				if (!myNewFolderApp.exists())	myNewFolderApp.mkdir();							// creo la cartella, se non esiste
				
				SimpleDateFormat day = new SimpleDateFormat("yyy-MM-dd");						// prendo solo la data
				String cartellaGiorno = day.format(new Date().getTime());
				File myNewFolderDay = new File(extStorageDirectory + newFolder + "/" + cartellaGiorno);
				if (!myNewFolderDay.exists())	{
					myNewFolderDay.mkdir();														// creo la cartella del giorno, se non esiste
				}
				if(GlobalVariables.myNewFolderDay=="")
					GlobalVariables.myNewFolderDay = extStorageDirectory + newFolder + "/" + cartellaGiorno; //salvo il nome della cartella odierna tra i file comuniLog.v(LOG_TAG, DoveSono + "Cartella gi� esistente.");
				
				File file = new File(Environment.getExternalStorageDirectory() + newFolder + "/" + cartellaGiorno + "/" + FileName);	// text file
				
				Log.v(LOG_TAG, "creato file su cui scriverei dati");
				
				FileWriter f 	= null;
				PrintWriter p	= null;
				
				try{
					Log.v(LOG_TAG, "provo a salvare i dati dei sensori");
					f = new FileWriter(file, false);
					p = new PrintWriter(f);
					
					//Inserisco la parte inerente l'ID criptato (N.B. � relativo al 1� valore temporale del dato salvato dagli accelerometri):
					//p.println(scriviIdCriptato());
					//Log.v(LOG_TAG, "Scritto ID criptato."); 
					
					//Save the orientation of the device when It record
					if (flag_orientation_determined==1)
						p.println(String.valueOf(1) + '\t' + String.valueOf(0) + '\t' + String.valueOf(0) + '\t' + String.valueOf(0));		// horizontal
					else if (flag_orientation_determined==2)
						p.println(String.valueOf(2) + '\t' + String.valueOf(0) + '\t' + String.valueOf(0) + '\t' + String.valueOf(0));		// vertical
					
					//faccio la distinzione del salvataggio nel caso non abbia nel mio cellulare un dato tipo di sensore:
					//-------------------------------------------------------------------------------------------------
					Log.v(LOG_TAG, "Salvo " + listDatiSensGyr_x.size() + " Gyro, " + listDatiSensAcc_x.size() + " Acc, " + listDatiSensMag_x.size() + " Magn., ");
					//salvo Gyro:
					for(int i=0; i<dim_gyro;  i++){
						p.println(listTempoGyro.get(0) + '\t' + listDatiSensGyr_x.get(0) + '\t' + listDatiSensGyr_y.get(0) + '\t' + listDatiSensGyr_z.get(0));
						listTempoGyro.remove(0);
						listDatiSensGyr_x.remove(0);
						listDatiSensGyr_y.remove(0);
						listDatiSensGyr_z.remove(0);
					}
					
					//dividere acquisizioni di sensori diversi:
					p.println(String.valueOf(1) + '\t' + String.valueOf(1) + '\t' + String.valueOf(1) + '\t' + String.valueOf(1));
					
					//salvo Acc:
					for(int i=0; i<dim_acc;  i++){
						p.println(listTempoAcc.get(0) + '\t' + listDatiSensAcc_x.get(0) + '\t' + listDatiSensAcc_y.get(0) + '\t' + listDatiSensAcc_z.get(0));
						listTempoAcc.remove(0);
						listDatiSensAcc_x.remove(0);
						listDatiSensAcc_y.remove(0);
						listDatiSensAcc_z.remove(0);
					}
					
					//dividere acquisizioni di sensori diversi:
					p.println(String.valueOf(1) + '\t' + String.valueOf(1) + '\t' + String.valueOf(1) + '\t' + String.valueOf(1));
					
					//salvo Magn:
					for(int i=0; i<dim_mag;  i++){
						p.println(listTempoMag.get(0) + '\t' + listDatiSensMag_x.get(0) + '\t' + listDatiSensMag_y.get(0) + '\t' + listDatiSensMag_z.get(0));
						listTempoMag.remove(0);
						listDatiSensMag_x.remove(0);
						listDatiSensMag_y.remove(0);
						listDatiSensMag_z.remove(0);
					}
					Log.v(LOG_TAG, "Salvati i dati di Gyro, Accel e Magn.");
					
					//chiudo il documento di scrittura:
					p.close();
					f.close();
					vettTempoDebug.add(String.valueOf(System.currentTimeMillis()));
					vettDebug.add(debug_DATI_SENS_SALVATI);
					
					DisplayToast("File salvato nella cartella (memoria esterna)!");


                    String newFolder_d = "/ SWIM APP";
                    String extStorageDirectory_d = Environment.getExternalStorageDirectory().toString();
                    File myNewFolderApp_d = new File(extStorageDirectory_d + newFolder_d);
                    // creo la cartella, se non esiste
////////////*** STORING THE FILE IN DATABASE ****////////////////////////
                    SimpleDateFormat day_d = new SimpleDateFormat("yyy-MM-dd");						// prendo solo la data
                    String cartellaGiorno_d = day_d.format(new Date().getTime());
                //    SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
                 //   String userId = prefs.getString("uid", "undefined");

                    String FileName_d = extStorageDirectory_d + newFolder_d + "/" + cartellaGiorno_d + "/" + currentDateandTime + "__" + Duration + "sec__" + user_id + ".txt";
                    swim swim_obj=new swim();
                    swim_obj.analysed=false;
                    swim_obj.user=user_id;
                    swim_obj.AzureFlag=false;
                    swim_obj.duration=Duration;
                    swim_obj.Comment="";
                    swim_obj.Local_file=FileName_d;
                    swim_obj.pool_length=50;
                    swim_obj.rating=0;
                    swim_obj.time_Stamp=new Date();
                    swim_obj.jason_analysis=new String();
                    swim_obj.SwimDate=cartellaGiorno_d;
                    swim_obj.Liked=false;
                    swim_obj.save();

                    Log.v(LOG_TAG, DoveSono + "Data saved.");







					//file.setReadOnly(); //setto il file con la caratteristica di SolaLettura (in modo che non possonano essere modificate le info sul dispositivo)
				}
				catch (IOException e)
				{       
					e.printStackTrace();
				}
				break;
			}
				
			// SAVE FILE ON INTERNAL SOTRAGE:
			default:
			{
				try
				{
					Log.v(LOG_TAG, DoveSono + "Il file non esiste, lo creo!");
					FileOutputStream fos = openFileOutput(FileName, Context.MODE_PRIVATE);	// MODE_PRIVATE = privato all'applicazione, MODE_WORLD_READABLE = renderlo visibile anche alle altre App.
					OutputStreamWriter osw = new OutputStreamWriter(fos);

					//Save the orientation of the device when It record
					if (flag_orientation_determined==1)
						osw.write(String.valueOf(1) + '\t' + String.valueOf(0) + '\t' + String.valueOf(0) + '\t' + String.valueOf(0) + '\n');		// horizontal
					else if (flag_orientation_determined==2)
						osw.write(String.valueOf(2) + '\t' + String.valueOf(0) + '\t' + String.valueOf(0) + '\t' + String.valueOf(0) + '\n');		// vertical
					
					//faccio la distinzione del salvataggio nel caso non abbia nel mio cellulare un dato tipo di sensore:
					//-------------------------------------------------------------------------------------------------
					Log.v(LOG_TAG, "Salvo " + listDatiSensGyr_x.size() + " Gyro, " + listDatiSensAcc_x.size() + " Acc, " + listDatiSensMag_x.size() + " Magn., ");
					//salvo Gyro:
					for(int i=0; i<dim_gyro;  i++){
						osw.write(listTempoGyro.get(0) + '\t' + listDatiSensGyr_x.get(0) + '\t' + listDatiSensGyr_y.get(0) + '\t' + listDatiSensGyr_z.get(0) + '\n');
						listTempoGyro.remove(0);
						listDatiSensGyr_x.remove(0);
						listDatiSensGyr_y.remove(0);
						listDatiSensGyr_z.remove(0);
					}
					
					//dividere acquisizioni di sensori diversi:
					osw.write(String.valueOf(1) + '\t' + String.valueOf(1) + '\t' + String.valueOf(1) + '\t' + String.valueOf(1) + '\n');
					
					//salvo Acc:
					for(int i=0; i<dim_acc;  i++){
						osw.write(listTempoAcc.get(0) + '\t' + listDatiSensAcc_x.get(0) + '\t' + listDatiSensAcc_y.get(0) + '\t' + listDatiSensAcc_z.get(0) + '\n');
						listTempoAcc.remove(0);
						listDatiSensAcc_x.remove(0);
						listDatiSensAcc_y.remove(0);
						listDatiSensAcc_z.remove(0);
					}
					
					//dividere acquisizioni di sensori diversi:
					osw.write(String.valueOf(1) + '\t' + String.valueOf(1) + '\t' + String.valueOf(1) + '\t' + String.valueOf(1) + '\n');
					
					//salvo Magn:
					for(int i=0; i<dim_mag;  i++){
						osw.write(listTempoMag.get(0) + '\t' + listDatiSensMag_x.get(0) + '\t' + listDatiSensMag_y.get(0) + '\t' + listDatiSensMag_z.get(0) + '\n');
						listTempoMag.remove(0);
						listDatiSensMag_x.remove(0);
						listDatiSensMag_y.remove(0);
						listDatiSensMag_z.remove(0);
					}
					Log.v(LOG_TAG, "Salvati i dati di Gyro, Accel e Magn.");

					//*************** fine salvataggio dati;
					osw.flush();
					osw.close();

                    String newFolder_d = "/ SWIM APP";
                    String extStorageDirectory_d = Environment.getExternalStorageDirectory().toString();
                    File myNewFolderApp_d = new File(extStorageDirectory_d + newFolder_d);
                    // creo la cartella, se non esiste


                    ////////////*** STORING THE FILE IN DATABASE ****////////////////////////
                    SimpleDateFormat day_d = new SimpleDateFormat("yyy-MM-dd");						// prendo solo la data
                    String cartellaGiorno_d = day_d.format(new Date().getTime());

                    String FileName_d 			= extStorageDirectory_d + newFolder_d + "/" + cartellaGiorno_d + "/" + currentDateandTime + "__" + Duration + "sec__" + user_id + ".txt";
                    swim swim_obj=new swim();
                   // SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
                    //String userId = prefs.getString("uid", "undefined");

                    swim_obj.analysed=false;
                    swim_obj.AzureFlag=false;
                    swim_obj.user=user_id;
                    swim_obj.Comment="";
                    swim_obj.duration=(Duration);
                    swim_obj.Local_file=FileName_d;
                    swim_obj.pool_length=50;
                    swim_obj.rating=0;
                    swim_obj.time_Stamp=new Date();
                    swim_obj.jason_analysis=new String();
                    swim_obj.SwimDate=cartellaGiorno_d;
                    swim_obj.Liked=false;

                    swim_obj.save();




                    Log.v(LOG_TAG,  "Data saved.");
                    //**************************** Code Spike Code alteration **************//
					Log.v(LOG_TAG, DoveSono + "Saved the file on internal memory.");





					
				}catch (IOException e) {       
					e.printStackTrace();
					Log.v(LOG_TAG, DoveSono + "Error saving in internal memory.");
				}
				break;
			}
		}
			
		// create the notification of the saved file:
		Notification notifica 		= new Notification(icona,testoNotifica,quando);
		Context context 			= getApplicationContext();
		numRecEffettuati 			= numRecEffettuati+1;
		String contentTitle 		= "Swim App";
		String contentText 			= "Effettuate " + numRecEffettuati + " registrazioni!";
		Intent intent = new Intent(this, NotificationReceiverActivity.class);
		PendingIntent launchIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notifica.setLatestEventInfo(context, contentTitle, contentText, launchIntent);
		NotificationManager notificationManager 	= (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifica.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(1, notifica);
	}
	else
	{
		Log.e(LOG_TAG, "Don't save: number of logged values not correct: " + listDatiSensGyr_x.size() + " Gyro, " + listDatiSensAcc_x.size() + " Acc, " + listDatiSensMag_x.size() + " Magn., ");
	}
	
	// resetto tutti i vettori di acquisizione, per una nuova registrazione;
	clearSensorArrays();





}

//********************************************************************** Clear all the saved arrays for Acc, Gyr and Mag
private void clearSensorArrays()
{
	listTempoAcc.clear();
	listTempoGyro.clear();
	listTempoMag.clear();
	listDatiSensAcc_x.clear();
	listDatiSensAcc_y.clear();
	listDatiSensAcc_z.clear();
	listDatiSensGyr_x.clear();
	listDatiSensGyr_y.clear();
	listDatiSensGyr_z.clear();
	listDatiSensMag_x.clear();
	listDatiSensMag_y.clear();
	listDatiSensMag_z.clear();
}

//**********************************************************************  Info Fine periodo di prova (Dialog window): *******
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
	//elemento aggiuntivo da inserire se la finestra appare in un Servizio:
	alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	alertDialog.show();
}

//********************************************************************** stop the record, save it and update (or not) the total number of records
private void stopRec_UpdateOrNotNumOfRecord (int flag){
	// stop the record, save it and update the total number of record
	if (flag == 1)
	{
		sensorREC(0);
		GlobalVariables.LogEffettuati = GlobalVariables.LogEffettuati+1;		//aggiorno numero registrazioni effettuate:
		GlobalVariables.NewLogEffettuati = 1;									//aggiorno la flag per avvertire l'OnResume dell'Activity che deve aggiornare il File in Memoria interna
	}
	else
	// only stop the record and save it
	{
		sensorREC(0);
	}
}

//******************************************* Dislpay Toast *************************************
private void DisplayToast(String msg) {
	if(GlobalVariables.displayOnOff==1)
	Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
}
private void DisplayToastLong(String msg) {
	if(GlobalVariables.displayOnOff==1)
	Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
}
	
}