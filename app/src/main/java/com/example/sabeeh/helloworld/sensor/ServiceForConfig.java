package com.example.sabeeh.helloworld.sensor;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.sabeeh.helloworld.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ServiceForConfig extends Service implements SensorEventListener{
	//Variabili:
	private String DoveSono = "(ServiceCongif)   ";
	static final String LOG_TAG = "NUOT_APP";
	private WakeLock wlServConfig;
	//Elementi utilizzati per i sensori ed il loro Rec.:
	private SensorManager sensorServConfig;
	private List<Sensor> sensorServConfigList;					//utilizzato per startare l'ascolto di tutti i sensori
	private Sensor mAcc, mGyr, mMag;							// utilizzato per startare l'ascolto dei soli 3 sensori d'interesse
	long tempo_attuale;
	long tempo_precedente;
	int Flag_t						= 0;						//flag usata per plottare solo una volta la finestra di informazione a schermo, prima di ogni azione
	long t_0 						= 3000;						// msec, per info della modalit� Aereo
	long t_1 						= 2500;						// msec, per info sull'analisi della presenza o meno dei sensori
	long t_2 						= 5000;						// msec, per info sull'analisi del sensore lungo i diversi assi
	int n_Toast						= 0;						//numero le Toast che mando a schermo: 5 secondi, 5 Toast
	int sensor_M					= 0;						//presenza/assenza di magnetometri
	int sensor_G					= 0;						//presenza/assenza di giroscopi
	int sensor_A					= 0;						//presenza/assenza di accelerometri
	float acc_0						= 0;						//valore dell'accelerometro sul primo   canale
	float acc_1						= 0;						//valore dell'accelerometro sul secondo canale
	float acc_2						= 0;						//valore dell'accelerometro sul terzo   canale
	int n_acc						= 0;						//numero i valori arrivati
	int N_acc						= 0;						//numero i valori arrivati
	int N_gyro						= 0;						//numero i valori arrivati
	int N_mag						= 0;						//numero i valori arrivati
	int Flag_screen					= 1;						//flag usata per sapere quando lo schermo � spento (1=acceso, 0=spento)
	int feedbackNumAcc				= 0;						//variabile per Feedback: numero assi degli accelerometri;
	int feedbackNumGyr				= 0;						//variabile per Feedback: numero assi degli giroscopis;
	int feedbackNumMag				= 0;						//variabile per Feedback: numero assi degli magnetometri;
	int feedbackScreenOff			= 0;						//variabile per feedback: registrazione a schermo spento;
	List<String> feedbackAcc_OE		= new ArrayList<String>();	//variabile per feedback: valore degli accelerometri presi in tali configurazione
	List<String> feedbackAcc_NS		= new ArrayList<String>();	//variabile per feedback: valore degli accelerometri presi in tali configurazione
	List<String> feedbackAcc_UD		= new ArrayList<String>();	//variabile per feedback: valore degli accelerometri presi in tali configurazione
	int sceltaFinestraDialogo		= 0;						//valore utilizzata come return dalla selezione della finestra di dialogo
	int sceltaFinestraDialogoPos	= 1;						//valore utilizzata come return dalla selezione della finestra di dialogo (risp positiva)
	int sceltaFinestraDialogoNeg	= 2;						//valore utilizzata come return dalla selezione della finestra di dialogo (risp negativa)
	List<String> listDatiSensGyr_x	= new ArrayList<String>();	//variabili usate per salvare il valore dei sensori
	List<String> listDatiSensGyr_y 	= new ArrayList<String>(); 
	List<String> listDatiSensGyr_z 	= new ArrayList<String>();
	List<String> listDatiSensAcc_x 	= new ArrayList<String>();
	List<String> listDatiSensAcc_y 	= new ArrayList<String>();
	List<String> listDatiSensAcc_z 	= new ArrayList<String>();
	List<String> listDatiSensMag_x 	= new ArrayList<String>();
	List<String> listDatiSensMag_y 	= new ArrayList<String>();
	List<String> listDatiSensMag_z 	= new ArrayList<String>();
	List<String> listTempoGyro  	= new ArrayList<String>();
	List<String> listTempoAcc  		= new ArrayList<String>();
	List<String> listTempoMag  		= new ArrayList<String>();
	List<String> listDatiSensGyr_x_1= new ArrayList<String>();	//Sensori per 1� modalit�: SENSOR_DELAY_UI
	List<String> listDatiSensGyr_y_1= new ArrayList<String>(); 
	List<String> listDatiSensGyr_z_1= new ArrayList<String>();
	List<String> listDatiSensAcc_x_1= new ArrayList<String>();
	List<String> listDatiSensAcc_y_1= new ArrayList<String>();
	List<String> listDatiSensAcc_z_1= new ArrayList<String>();
	List<String> listDatiSensMag_x_1= new ArrayList<String>();
	List<String> listDatiSensMag_y_1= new ArrayList<String>();
	List<String> listDatiSensMag_z_1= new ArrayList<String>();
	List<String> listTempoGyro_1 	= new ArrayList<String>();
	List<String> listTempoAcc_1 	= new ArrayList<String>();
	List<String> listTempoMag_1 	= new ArrayList<String>();
	List<String> listDatiSensGyr_x_2= new ArrayList<String>();	//Sensori per 1� modalit�: SENSOR_DELAY_NORMAL
	List<String> listDatiSensGyr_y_2= new ArrayList<String>(); 
	List<String> listDatiSensGyr_z_2= new ArrayList<String>();
	List<String> listDatiSensAcc_x_2= new ArrayList<String>();
	List<String> listDatiSensAcc_y_2= new ArrayList<String>();
	List<String> listDatiSensAcc_z_2= new ArrayList<String>();
	List<String> listDatiSensMag_x_2= new ArrayList<String>();
	List<String> listDatiSensMag_y_2= new ArrayList<String>();
	List<String> listDatiSensMag_z_2= new ArrayList<String>();
	List<String> listTempoGyro_2 	= new ArrayList<String>();
	List<String> listTempoAcc_2 	= new ArrayList<String>();
	List<String> listTempoMag_2 	= new ArrayList<String>();
	List<String> listDatiSensGyr_x_3= new ArrayList<String>();	//Sensori per 1� modalit�: SENSOR_DELAY_GAME
	List<String> listDatiSensGyr_y_3= new ArrayList<String>(); 
	List<String> listDatiSensGyr_z_3= new ArrayList<String>();
	List<String> listDatiSensAcc_x_3= new ArrayList<String>();
	List<String> listDatiSensAcc_y_3= new ArrayList<String>();
	List<String> listDatiSensAcc_z_3= new ArrayList<String>();
	List<String> listDatiSensMag_x_3= new ArrayList<String>();
	List<String> listDatiSensMag_y_3= new ArrayList<String>();
	List<String> listDatiSensMag_z_3= new ArrayList<String>();
	List<String> listTempoGyro_3 	= new ArrayList<String>();
	List<String> listTempoAcc_3 	= new ArrayList<String>();
	List<String> listTempoMag_3 	= new ArrayList<String>();
	List<String> listDatiSensGyr_x_4= new ArrayList<String>();	//Sensori per 1� modalit�: SENSOR_DELAY_FASTEST
	List<String> listDatiSensGyr_y_4= new ArrayList<String>(); 
	List<String> listDatiSensGyr_z_4= new ArrayList<String>();
	List<String> listDatiSensAcc_x_4= new ArrayList<String>();
	List<String> listDatiSensAcc_y_4= new ArrayList<String>();
	List<String> listDatiSensAcc_z_4= new ArrayList<String>();
	List<String> listDatiSensMag_x_4= new ArrayList<String>();
	List<String> listDatiSensMag_y_4= new ArrayList<String>();
	List<String> listDatiSensMag_z_4= new ArrayList<String>();
	List<String> listTempoGyro_4 	= new ArrayList<String>();
	List<String> listTempoAcc_4 	= new ArrayList<String>();
	List<String> listTempoMag_4 	= new ArrayList<String>();
	int FlagSensorRec 				= 0;						//lo setto ad 1 quando voglio registrare e a 0 quando nooppure ho finito
	int FlagCheck 					= 0;						//tiene informato sul punto di analisi all'interno dell'onsensorChanged
	int FlagVibPerScreen			= 0;						//flag per il segnale di vibrazione che do all'utente (1=prima vibrazione, 2=seconda vibrazione)
	int FlagDelaySensor				= 0;						//flag per il cambio del Delay Time dei sensori
	//attivo la BroadcastReceiver per lo spegnimento del monitor:
	BroadcastReceiver screenReceiver;
	Intent intent = new Intent("com.practice.SimpleService.MY_ACTION");
	/* ******************************************************************************************** */
	//variabili per il salvataggio del file di configurazione in memoria interna:
	private String datiConfigurazione 	= "config.txt";
	long t_begin_acc;			/* primo  salvataggio dati acc (per un generico delay) */	
	long t_end_acc;				/* ultimo salvataggio dati acc (per un generico delay) */
	long t_begin_gyr;			/* primo  salvataggio dati gyr (per un generico delay) */
	long t_end_gyr;				/* ultimo salvataggio dati gyr (per un generico delay) */
	long t_begin_mag;			/* primo  salvataggio dati mag (per un generico delay) */
	long t_end_mag;				/* ultimo salvataggio dati mag (per un generico delay) */
	int  flag_procedura_config = 3;	/* inserito per scegliere tra la vecchia procedura flag_procedura=1 (salvo tutti i recording dei sensori), oppure flag_procedura=2 (dove salvo solo il worst-case tra le freq di campionamento dei diversi delay) */
	int t_med_1;				/* variabili utilizzate per salvare i tempi medi per ogni delay */
	int t_med_2;
	int t_med_3;
	int t_med_4;

	
	
	//********************************************************************** onCreate:
	public void onCreate() {
		Log.v(LOG_TAG, DoveSono + "Entro nel Service di Configuazione");
		
		WakeLock_e_SensorChange();
		
	}
	//********************************************************************** onSensorChanged:
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		tempo_attuale = System.currentTimeMillis();
		
		switch (FlagCheck) {
			//richiesta modalit� aereo;
			case 0:	{
				if((tempo_attuale-tempo_precedente)>t_0){
					tempo_precedente = tempo_attuale;
					isAirplaneModeOn();
				}
				break;
			}
			//analizzo presenza dei sensori e valori Acc in posa verticale:
			case 1: {
				if(Flag_t==0){
					Log.v(LOG_TAG, DoveSono + "Check NordSud");
					sensor_M			= 0;	sensor_A			= 0;	sensor_G			= 0;
					feedbackNumAcc		= 0;	feedbackNumGyr		= 0;	feedbackNumMag		= 0;
					Flag_t 				= 1;
					FlagCheck 			= -1;
					feedbackAcc_NS.clear();
					checkSensoreNordSud();
				}
				else{
					//controllo per 5 secondi:
					if(Flag_t==1){
						//controllo presenza sensori, nel caso siano presenti setto le variabili (sensor_M, sensor_A, sensor_G) a "1":
						//controllo anche il numero dei loro assi, indicandolo in "feedbackNumMag", "feedbackNumGyr", "feedbackNumAcc":
						if (sensor_M==0 && event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
							sensor_M = 1;
							if(event.values[2]!=0)		feedbackNumMag = 3;
							else if(event.values[1]!=0)	feedbackNumMag = 2;
							else 						feedbackNumMag = 1;
						}
						if (sensor_G==0 && event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
							sensor_G = 1;
							if(event.values[2]!=0)		feedbackNumGyr = 3;
							else if(event.values[1]!=0)	feedbackNumGyr = 2;
							else 						feedbackNumGyr = 1;
						}
						if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
							if(sensor_A==0){
								sensor_A = 1;
								if(event.values[2]!=0)		feedbackNumAcc = 3;
								else if(event.values[1]!=0)	feedbackNumAcc = 2;
								else 						feedbackNumAcc = 1;
							}
							//salvo il valore degli accelerometri:
							acc_0 += event.values[0];
							acc_1 += event.values[1];
							acc_2 += event.values[2];
							n_acc += 1;
						}
						
						//dopo 5 secondi di analisi, setto la Flag ad un val tale da stoppare il calcolo della media
						if((tempo_attuale-tempo_precedente)>t_2){
							Flag_t = 2;
						}
					}
					//dopo i 5 secondi di ricerca:
					else{
						if(sensor_M==0 || sensor_G==0 || sensor_A==0){
							Flag_t 	  = 0;
							FlagCheck 	= -1;
							//chiedo all'utente se vuole ripetere l'operazione:
								//String msg = "Cellulare non adatto: mancano i sensori necessari!"+'\n'+'\n'+"Analizzare nuovamente presenza sensori?";
							String msg = "Oops! You will have full results access functionality, but some device sensors are missing to enable the recording of trainings. More info available in the FAQ section."+'\n'+'\n'+"Do you want to repeat the previous step?";
							int rispPos = 1;	//se risp pos, vado al passo successivo
							int rispNeg = 4;	//se risp neg, ripeto il passo attuale
							interazioneUtente(msg, rispPos, rispNeg);
						}
						else{
							Log.v(LOG_TAG, DoveSono + "Val. Med. Nord-Sud acc_x: " + acc_0/(float)n_acc);
							Log.v(LOG_TAG, DoveSono + "Val. Med. Nord-Sud acc_y: " + acc_1/(float)n_acc);
							Log.v(LOG_TAG, DoveSono + "Val. Med. Nord-Sud acc_z: " + acc_2/(float)n_acc);
							acc_0 		= Math.round((acc_0/(float)n_acc)*1000);	// vecchio metodo (no round): acc_0 = acc_0/(float)n_acc;
							acc_1 		= Math.round((acc_1/(float)n_acc)*1000);
							acc_2 		= Math.round((acc_2/(float)n_acc)*1000);
							FlagCheck 	= 2;
							Flag_t  	= 0;
							Log.v(LOG_TAG, DoveSono + "Val. Med. Nord-Sud acc_x (rounded,*1000,cast ad int): " + (int)acc_0);
							Log.v(LOG_TAG, DoveSono + "Val. Med. Nord-Sud acc_y (rounded,*1000,cast ad int): " + (int)acc_1);
							Log.v(LOG_TAG, DoveSono + "Val. Med. Nord-Sud acc_z (rounded,*1000,cast ad int): " + (int)acc_2);
							//salvo i valori trovati, che poi metter� sul file di configurazione;
							feedbackAcc_NS.add(String.valueOf((int)acc_0));		//li salvo come interi, almeno son di pi� comoda lettura
							feedbackAcc_NS.add(String.valueOf((int)acc_1));
							feedbackAcc_NS.add(String.valueOf((int)acc_2));
							acc_0=0;acc_1=0;acc_2=0;n_acc=0;					//resetto i valori per il conteggio della media, che uso per le altre due direioni
						}
					}
				}
				break;
			}
			//analisi a schermo spento:
			case 2: {
				if(Flag_t==0){
					Log.v(LOG_TAG, DoveSono + "Check schermo spento.");
					Flag_t 					= 1;
					FlagCheck 				= -1;
					//setto a zero le variabili:
					N_acc = 0;	N_gyro = 0;	N_mag = 0;
					FlagVibPerScreen = 0;
					//avviso l'utente della procedura:
					checkSchermoSpento();
				}
				else{
					//l'utente ha letto il msg a video, lo schermo � ancora acceso, aspetto un secondo, vibro e ascolto per lo spegnimento dello schermo:
					if(Flag_screen==1){
						//se non ho gi� vibrato, vibro
						if(FlagVibPerScreen==0){
							if((tempo_attuale-tempo_precedente)>1000){
								FlagVibPerScreen = 1;	//setto una flag per dire che ho dato il segnale di vibrazione
								String vibratorService = Context.VIBRATOR_SERVICE;
								Vibrator vibrator = (Vibrator)getSystemService(vibratorService);
								vibrator.vibrate(500);
								DisplayToast("..switch off the screen!");
								Log.v(LOG_TAG, DoveSono + "Vibro 1� volta.");
							}
						}
					}
					//se l'utente ha spento lo schermo, dopo che il segnale � stato dato, allora inizio ad analizzare i sensori:
					else{
						//se, post vibrazione di avviso, l'utente spegne lo schermo, allora guardo ai sensori
						if(FlagVibPerScreen==1 && Flag_screen==0){
							//per 5 secondi, controllo sensori:
							if((tempo_attuale-tempo_precedente)<t_2){
								if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	N_acc  += 1;
								if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		N_gyro += 1;
								if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)	N_mag  += 1;
							}
							else{
								if(N_acc>0 && N_gyro>0 && N_mag>0)	feedbackScreenOff = 2;
								else								feedbackScreenOff = 1;
								Log.v(LOG_TAG, DoveSono + "N� acc (schermo OFF): " + N_acc );
								Log.v(LOG_TAG, DoveSono + "N� giroscopi:         " + N_gyro);
								Log.v(LOG_TAG, DoveSono + "N� magnetometri:      " + N_mag );
								String vibratorService = Context.VIBRATOR_SERVICE;
								Vibrator vibrator = (Vibrator)getSystemService(vibratorService);
								vibrator.vibrate(500);
								FlagVibPerScreen = 2;
								Log.v(LOG_TAG, DoveSono + "Vibro 2� volta.");
								FlagCheck = 3;
								Flag_t 	  =  0;
								Log.v(LOG_TAG, DoveSono + "Rilascio il Receiver dello schermo.");
							}
						}
					}
				}
				break;
			}
			//analizzo per 5 secondi ogni modalit� (SENSOR_DELAY_UI, SENSOR_DELAY_NORMAL, SENSOR_DELAY_GAME) + i valori degli Acc nella posa  UpDown:
			case 3: {
				if (flag_procedura_config==2) {
					//caso di salvataggio su memoria interna:
						if(Flag_t==0){
							Log.v(LOG_TAG, DoveSono + "Check UpDown.");
							Flag_t 					= 1;
							FlagCheck 				= -1;
							FlagDelaySensor			= 0;
							checkSensoreUpDown();
							feedbackAcc_UD.clear();
							Log.v(LOG_TAG, DoveSono + "Analisi posa UpDown + Analisi dei 4 tipi di Delay.");
						}
						else{
							tempo_attuale = System.currentTimeMillis();
							//analizzo i primi 5 secondi (SENSOR_DELAY_UI):
							if((tempo_attuale-tempo_precedente)<=t_1){
								//cambio il DelayTime per i sensori:
								if(FlagDelaySensor==0){
									scelgoDelayPerSensori(1);
									FlagDelaySensor = 1;
									DisplayToast("10 seconds remaining...");
									Log.v(LOG_TAG, DoveSono + "Analisi con SENSOR_DELAY_UI. ");
									//setto a zero i valori delle variabili:
									N_acc		= 0;	N_gyro		= 0;	N_mag		= 0;
									t_begin_acc = 0;	t_begin_gyr = 0;	t_begin_mag = 0;
									t_end_acc 	= 0;	t_end_gyr 	= 0;	t_end_mag 	= 0;
								}
								//prelevo il tempo di registrazione:
								if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 	{
									if (N_mag  == 0)	t_begin_mag = tempo_attuale;			//mi salvo il tempo iniziale
									N_mag  += 1;												//incremento il contatore per gli accelerometri:
									t_end_mag = tempo_attuale;									//aggiorno quello che sar� il tempo finale
								}
								if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		{
									if (N_gyro == 0)	t_begin_gyr = tempo_attuale;
									N_gyro += 1;
									t_end_gyr = tempo_attuale;
								}
								if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	{
									if (N_acc == 0)	t_begin_acc = tempo_attuale;
									N_acc  += 1;
									t_end_acc = tempo_attuale;
								}
							}
							//analizzo i secondi 5 secondi (SENSOR_DELAY_NORMAL):
							else if((tempo_attuale-tempo_precedente)<=2*t_1){
								//cambio il DelayTime per i sensori:
								if(FlagDelaySensor==1){
									//salvo il valore del tempo medio per il Delay precedente:								
									t_med_1 = (int)Math.max((t_end_mag-t_begin_mag)/N_mag,Math.max((t_end_gyr-t_begin_gyr)/N_gyro,(t_end_acc-t_begin_acc)/N_acc));
									Log.v(LOG_TAG, DoveSono + "Media dt_rec 1� delay: " + t_med_1);
									//cambio sensor Delay:
									scelgoDelayPerSensori(2);
									FlagDelaySensor = 2;
									DisplayToast("7,5 seconds remaining...");
									Log.v(LOG_TAG, DoveSono + "Analisi con SENSOR_DELAY_NORMAL. ");
									//setto a zero i valori delle variabili:
									N_acc		= 0;	N_gyro		= 0;	N_mag		= 0;
									t_begin_acc = 0;	t_begin_gyr = 0;	t_begin_mag = 0;
									t_end_acc 	= 0;	t_end_gyr 	= 0;	t_end_mag 	= 0;
								}
								//prelevo il tempo di registrazione:
								if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 	{
									if (N_mag  == 0)	t_begin_mag = tempo_attuale;			//mi salvo il tempo iniziale
									N_mag  += 1;												//incremento il contatore per gli accelerometri:
									t_end_mag = tempo_attuale;									//aggiorno quello che sar� il tempo finale
								}
								if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		{
									if (N_gyro == 0)	t_begin_gyr = tempo_attuale;
									N_gyro += 1;
									t_end_gyr = tempo_attuale;
								}
								if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	{
									if (N_acc == 0)	t_begin_acc = tempo_attuale;
									N_acc  += 1;
									t_end_acc = tempo_attuale;
								}
							}
							else
							//analizzo i terzi 5 secondi (SENSOR_DELAY_GAME), in pi� analizzo anche il valor medio degli Acc per tale posa:
							if((tempo_attuale-tempo_precedente)<=3*t_1){
								//cambio il DelayTime per i sensori:
								if(FlagDelaySensor==2){
									//salvo il valore del tempo medio per il Delay precedente:
									t_med_2 = (int)Math.max((t_end_mag-t_begin_mag)/N_mag,Math.max((t_end_gyr-t_begin_gyr)/N_gyro,(t_end_acc-t_begin_acc)/N_acc));
									Log.v(LOG_TAG, DoveSono + "Media dt_rec 2� delay: " + t_med_2);
									//cambio sensor Delay:
									scelgoDelayPerSensori(3);
									FlagDelaySensor = 3;
									DisplayToast("5 seconds remaining...");
									Log.v(LOG_TAG, DoveSono + "Analisi con SENSOR_DELAY_GAME. ");
									//setto a zero i valori delle variabili:
									N_acc		= 0;	N_gyro		= 0;	N_mag		= 0;
									t_begin_acc = 0;	t_begin_gyr = 0;	t_begin_mag = 0;
									t_end_acc 	= 0;	t_end_gyr 	= 0;	t_end_mag 	= 0;
								}
								//prelevo il tempo di registrazione:
								if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 	{
									//mi salvo il tempo iniziale
									if (N_mag  == 0)	t_begin_mag = tempo_attuale;
									N_mag += 1;
									t_end_mag = tempo_attuale;
								}
								if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		{
									if (N_gyro == 0)	t_begin_gyr = tempo_attuale;
									N_gyro += 1;
									t_end_gyr = tempo_attuale;
								}
								if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	{
									if (N_acc == 0)	t_begin_acc = tempo_attuale;
									N_acc  += 1;
									t_end_acc = tempo_attuale;
									acc_0 += event.values[0];						// salvo valori per il feedback.UD, cio� lungo asse z. 
									acc_1 += event.values[1];
									acc_2 += event.values[2];
									n_acc += 1;
								}
							}
							//analizzo i secondi 5 secondi (SENSOR_DELAY_FASTEST):
							else if((tempo_attuale-tempo_precedente)<=4*t_1){
								//cambio il DelayTime per i sensori:
								if(FlagDelaySensor==3){
									//salvo il valore del tempo medio per il Delay precedente:
									t_med_3 = (int)Math.max((t_end_mag-t_begin_mag)/N_mag,Math.max((t_end_gyr-t_begin_gyr)/N_gyro,(t_end_acc-t_begin_acc)/N_acc));
									Log.v(LOG_TAG, DoveSono + "Media dt_rec 3� delay: " + t_med_3);
									//cambio sensor Delay:
									scelgoDelayPerSensori(4);
									FlagDelaySensor = 4;
									DisplayToast("2,5 seconds remaining...");
									Log.v(LOG_TAG, DoveSono + "Analisi con SENSOR_DELAY_FASTEST. ");
									//setto a zero i valori delle variabili:
									N_acc		= 0;	N_gyro		= 0;	N_mag		= 0;
									t_begin_acc = 0;	t_begin_gyr = 0;	t_begin_mag = 0;
									t_end_acc 	= 0;	t_end_gyr 	= 0;	t_end_mag 	= 0;
								}
								//prelevo il tempo di registrazione:
								if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 	{
									if (N_mag  == 0)	t_begin_mag = tempo_attuale;			//mi salvo il tempo iniziale
									N_mag  += 1;												//incremento il contatore per gli accelerometri:
									t_end_mag = tempo_attuale;									//aggiorno quello che sar� il tempo finale
								}
								if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		{
									if (N_gyro == 0)	t_begin_gyr = tempo_attuale;
									N_gyro += 1;
									t_end_gyr = tempo_attuale;
								}
								if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	{
									if (N_acc == 0)	t_begin_acc = tempo_attuale;
									N_acc  += 1;
									t_end_acc = tempo_attuale;
								}
							}
							//dopo i 20 secondi di ricerca:
							else{
								//salvo il valore del tempo medio per il Delay precedente:
								t_med_4 = (int)Math.max((t_end_mag-t_begin_mag)/N_mag,Math.max((t_end_gyr-t_begin_gyr)/N_gyro,(t_end_acc-t_begin_acc)/N_acc));
								Log.v(LOG_TAG, DoveSono + "Media dt_rec 4� delay: " + t_med_4);
								acc_0 		= Math.round((acc_0/(float)n_acc)*1000);	// vecchio metodo (no round): acc_0 = acc_0/(float)n_acc;
								acc_1 		= Math.round((acc_1/(float)n_acc)*1000);
								acc_2 		= Math.round((acc_2/(float)n_acc)*1000);
								FlagCheck 	= -1;
								Flag_t  	= 0;
								//salvo i valori trovati, che poi metter� sul file di configurazione;
								feedbackAcc_UD.add(String.valueOf((int)acc_0));			// li salvo come interi, almeno sono di pi� facile lettura
								feedbackAcc_UD.add(String.valueOf((int)acc_1));
								feedbackAcc_UD.add(String.valueOf((int)acc_2));
								Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_x: " + acc_0/(float)n_acc);
								Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_y: " + acc_1/(float)n_acc);
								Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_z: " + acc_2/(float)n_acc);
								Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_x (rounded,*1000,cast ad int): " + feedbackAcc_UD.get(0));
								Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_y (rounded,*1000,cast ad int): " + feedbackAcc_UD.get(1));
								Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_z (rounded,*1000,cast ad int): " + feedbackAcc_UD.get(2));
								acc_0=0;acc_1=0;acc_2=0;n_acc=0;	//resetto i valori per il conteggio della media, che uso per le altre due direzioni
								
								//salvo i dati, se presenti:
								if(t_med_1!=0 && t_med_2!=0 && t_med_3!=0 && t_med_4!=0){
									salvaDati();
								}
							}
						}
				}
				else
					//salvo i valori sensibili sia in memoria esterna che in quella interna:
					if (flag_procedura_config==3) {
						//caso di salvataggio su memoria interna:
							if(Flag_t==0){
								Log.v(LOG_TAG, DoveSono + "Check UpDown.");
								Flag_t 					= 1;
								FlagCheck 				= -1;
								FlagDelaySensor			= 0;
								checkSensoreUpDown();
								feedbackAcc_UD.clear();
								Log.v(LOG_TAG, DoveSono + "Analisi posa UpDown + Analisi dei 4 tipi di Delay.");
								//cancello i vettori, nel caso si voglia ripetere l'ultima analisi:
								listTempoMag_1.clear();		listDatiSensMag_x_1.clear();	listDatiSensMag_y_1.clear();	listDatiSensMag_z_1.clear();
								listTempoGyro_1.clear();	listDatiSensGyr_x_1.clear();	listDatiSensGyr_y_1.clear();	listDatiSensGyr_z_1.clear();
								listTempoAcc_1.clear();		listDatiSensAcc_x_1.clear();	listDatiSensAcc_y_1.clear();	listDatiSensAcc_z_1.clear();
								listTempoMag_2.clear();		listDatiSensMag_x_2.clear();	listDatiSensMag_y_2.clear();	listDatiSensMag_z_2.clear();
								listTempoGyro_2.clear();	listDatiSensGyr_x_2.clear();	listDatiSensGyr_y_2.clear();	listDatiSensGyr_z_2.clear();
								listTempoAcc_2.clear();		listDatiSensAcc_x_2.clear();	listDatiSensAcc_y_2.clear();	listDatiSensAcc_z_2.clear();
								listTempoMag_3.clear();		listDatiSensMag_x_3.clear();	listDatiSensMag_y_3.clear();	listDatiSensMag_z_3.clear();
								listTempoGyro_3.clear();	listDatiSensGyr_x_3.clear();	listDatiSensGyr_y_3.clear();	listDatiSensGyr_z_3.clear();
								listTempoAcc_3.clear();		listDatiSensAcc_x_3.clear();	listDatiSensAcc_y_3.clear();	listDatiSensAcc_z_3.clear();
								listTempoMag_4.clear();		listDatiSensMag_x_4.clear();	listDatiSensMag_y_4.clear();	listDatiSensMag_z_4.clear();
								listTempoGyro_4.clear();	listDatiSensGyr_x_4.clear();	listDatiSensGyr_y_4.clear();	listDatiSensGyr_z_4.clear();
								listTempoAcc_4.clear();		listDatiSensAcc_x_4.clear();	listDatiSensAcc_y_4.clear();	listDatiSensAcc_z_4.clear();
							}
							else{
								tempo_attuale = System.currentTimeMillis();
								//analizzo i primi 5 secondi (SENSOR_DELAY_UI):
								if((tempo_attuale-tempo_precedente)<=t_1){
									//cambio il DelayTime per i sensori:
									if(FlagDelaySensor==0){
										scelgoDelayPerSensori(1);
										FlagDelaySensor = 1;
										DisplayToast("10 seconds remaining...");
										Log.v(LOG_TAG, DoveSono + "Analisi con SENSOR_DELAY_UI. ");
										//setto a zero i valori delle variabili:
										N_acc		= 0;	N_gyro		= 0;	N_mag		= 0;
										t_begin_acc = 0;	t_begin_gyr = 0;	t_begin_mag = 0;
										t_end_acc 	= 0;	t_end_gyr 	= 0;	t_end_mag 	= 0;
									}
									//prelevo il tempo di registrazione:
									if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 	{
										if (N_mag  == 0)	t_begin_mag = tempo_attuale;			//mi salvo il tempo iniziale
										N_mag  += 1;												//incremento il contatore per gli accelerometri:
										t_end_mag = tempo_attuale;									//aggiorno quello che sar� il tempo finale
										//salvo tutti i valori:
										listTempoMag_1.add(String.valueOf(tempo_attuale));		listDatiSensMag_x_1.add(String.valueOf(event.values[0]));	listDatiSensMag_y_1.add(String.valueOf(event.values[1]));	listDatiSensMag_z_1.add(String.valueOf(event.values[2]));
									}
									if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		{
										if (N_gyro == 0)	t_begin_gyr = tempo_attuale;
										N_gyro += 1;
										t_end_gyr = tempo_attuale;
										listTempoGyro_1.add(String.valueOf(tempo_attuale));	listDatiSensGyr_x_1.add(String.valueOf(event.values[0]));	listDatiSensGyr_y_1.add(String.valueOf(event.values[1]));	listDatiSensGyr_z_1.add(String.valueOf(event.values[2]));
									}
									if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	{
										if (N_acc == 0)	t_begin_acc = tempo_attuale;
										N_acc  += 1;
										t_end_acc = tempo_attuale;
										listTempoAcc_1.add(String.valueOf(tempo_attuale));		listDatiSensAcc_x_1.add(String.valueOf(event.values[0]));	listDatiSensAcc_y_1.add(String.valueOf(event.values[1]));	listDatiSensAcc_z_1.add(String.valueOf(event.values[2]));
									}
								}
								//analizzo i secondi 5 secondi (SENSOR_DELAY_NORMAL):
								else if((tempo_attuale-tempo_precedente)<=2*t_1){
									//cambio il DelayTime per i sensori:
									if(FlagDelaySensor==1){
										//salvo il valore del tempo medio per il Delay precedente:								
										t_med_1 = (int)Math.max((t_end_mag-t_begin_mag)/N_mag,Math.max((t_end_gyr-t_begin_gyr)/N_gyro,(t_end_acc-t_begin_acc)/N_acc));
										Log.v(LOG_TAG, DoveSono + "Media dt_rec 1� delay: " + t_med_1);
										//cambio sensor Delay:
										scelgoDelayPerSensori(2);
										FlagDelaySensor = 2;
										DisplayToast("7,5 seconds remaining...");
										Log.v(LOG_TAG, DoveSono + "Analisi con SENSOR_DELAY_NORMAL. ");
										//setto a zero i valori delle variabili:
										N_acc		= 0;	N_gyro		= 0;	N_mag		= 0;
										t_begin_acc = 0;	t_begin_gyr = 0;	t_begin_mag = 0;
										t_end_acc 	= 0;	t_end_gyr 	= 0;	t_end_mag 	= 0;
									}
									//prelevo il tempo di registrazione:
									if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 	{
										if (N_mag  == 0)	t_begin_mag = tempo_attuale;			//mi salvo il tempo iniziale
										N_mag  += 1;												//incremento il contatore per gli accelerometri:
										t_end_mag = tempo_attuale;									//aggiorno quello che sar� il tempo finale
										listTempoMag_2.add(String.valueOf(tempo_attuale));		listDatiSensMag_x_2.add(String.valueOf(event.values[0]));	listDatiSensMag_y_2.add(String.valueOf(event.values[2]));	listDatiSensMag_z_2.add(String.valueOf(event.values[2]));
									}
									if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		{
										if (N_gyro == 0)	t_begin_gyr = tempo_attuale;
										N_gyro += 1;
										t_end_gyr = tempo_attuale;
										listTempoGyro_2.add(String.valueOf(tempo_attuale));	listDatiSensGyr_x_2.add(String.valueOf(event.values[0]));	listDatiSensGyr_y_2.add(String.valueOf(event.values[1]));	listDatiSensGyr_z_2.add(String.valueOf(event.values[2]));
									}
									if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	{
										if (N_acc == 0)	t_begin_acc = tempo_attuale;
										N_acc  += 1;
										t_end_acc = tempo_attuale;
										listTempoAcc_2.add(String.valueOf(tempo_attuale));		listDatiSensAcc_x_2.add(String.valueOf(event.values[0]));	listDatiSensAcc_y_2.add(String.valueOf(event.values[1]));	listDatiSensAcc_z_2.add(String.valueOf(event.values[2]));
									}
								}
								else
								//analizzo i terzi 5 secondi (SENSOR_DELAY_GAME), in pi� analizzo anche il valor medio degli Acc per tale posa:
								if((tempo_attuale-tempo_precedente)<=3*t_1){
									//cambio il DelayTime per i sensori:
									if(FlagDelaySensor==2){
										//salvo il valore del tempo medio per il Delay precedente:
										t_med_2 = (int)Math.max((t_end_mag-t_begin_mag)/N_mag,Math.max((t_end_gyr-t_begin_gyr)/N_gyro,(t_end_acc-t_begin_acc)/N_acc));
										Log.v(LOG_TAG, DoveSono + "Media dt_rec 2� delay: " + t_med_2);
										//cambio sensor Delay:
										scelgoDelayPerSensori(3);
										FlagDelaySensor = 3;
										DisplayToast("5 seconds remaining...");
										Log.v(LOG_TAG, DoveSono + "Analisi con SENSOR_DELAY_GAME. ");
										//setto a zero i valori delle variabili:
										N_acc		= 0;	N_gyro		= 0;	N_mag		= 0;
										t_begin_acc = 0;	t_begin_gyr = 0;	t_begin_mag = 0;
										t_end_acc 	= 0;	t_end_gyr 	= 0;	t_end_mag 	= 0;
									}
									//prelevo il tempo di registrazione:
									if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 	{
										//mi salvo il tempo iniziale
										if (N_mag  == 0)	t_begin_mag = tempo_attuale;
										N_mag += 1;
										t_end_mag = tempo_attuale;
										listTempoMag_3.add(String.valueOf(tempo_attuale));		listDatiSensMag_x_3.add(String.valueOf(event.values[0]));	listDatiSensMag_y_3.add(String.valueOf(event.values[1]));	listDatiSensMag_z_3.add(String.valueOf(event.values[2]));
									}
									if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		{
										if (N_gyro == 0)	t_begin_gyr = tempo_attuale;
										N_gyro += 1;
										t_end_gyr = tempo_attuale;
										listTempoGyro_3.add(String.valueOf(tempo_attuale));	listDatiSensGyr_x_3.add(String.valueOf(event.values[0]));	listDatiSensGyr_y_3.add(String.valueOf(event.values[1]));	listDatiSensGyr_z_3.add(String.valueOf(event.values[2]));
									}
									if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	{
										if (N_acc == 0)	t_begin_acc = tempo_attuale;
										N_acc  += 1;
										t_end_acc = tempo_attuale;
										listTempoAcc_3.add(String.valueOf(tempo_attuale));		listDatiSensAcc_x_3.add(String.valueOf(event.values[0]));	listDatiSensAcc_y_3.add(String.valueOf(event.values[1]));	listDatiSensAcc_z_3.add(String.valueOf(event.values[2]));
										acc_0 += event.values[0];						// salvo valori per il feedback.UD, cio� lungo asse z. 
										acc_1 += event.values[1];
										acc_2 += event.values[2];
										n_acc += 1;
									}
								}
								//analizzo i secondi 5 secondi (SENSOR_DELAY_FASTEST):
								else if((tempo_attuale-tempo_precedente)<=4*t_1){
									//cambio il DelayTime per i sensori:
									if(FlagDelaySensor==3){
										//salvo il valore del tempo medio per il Delay precedente:
										t_med_3 = (int)Math.max((t_end_mag-t_begin_mag)/N_mag,Math.max((t_end_gyr-t_begin_gyr)/N_gyro,(t_end_acc-t_begin_acc)/N_acc));
										Log.v(LOG_TAG, DoveSono + "Media dt_rec 3� delay: " + t_med_3);
										//cambio sensor Delay:
										scelgoDelayPerSensori(4);
										FlagDelaySensor = 4;
										DisplayToast("2,5 seconds remaining...");
										Log.v(LOG_TAG, DoveSono + "Analisi con SENSOR_DELAY_FASTEST. ");
										//setto a zero i valori delle variabili:
										N_acc		= 0;	N_gyro		= 0;	N_mag		= 0;
										t_begin_acc = 0;	t_begin_gyr = 0;	t_begin_mag = 0;
										t_end_acc 	= 0;	t_end_gyr 	= 0;	t_end_mag 	= 0;
									}
									//prelevo il tempo di registrazione:
									if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 	{
										if (N_mag  == 0)	t_begin_mag = tempo_attuale;			//mi salvo il tempo iniziale
										N_mag  += 1;												//incremento il contatore per gli accelerometri:
										t_end_mag = tempo_attuale;									//aggiorno quello che sar� il tempo finale
										listTempoMag_4.add(String.valueOf(tempo_attuale));		listDatiSensMag_x_4.add(String.valueOf(event.values[0]));	listDatiSensMag_y_4.add(String.valueOf(event.values[1]));	listDatiSensMag_z_4.add(String.valueOf(event.values[2]));
									}
									if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)		{
										if (N_gyro == 0)	t_begin_gyr = tempo_attuale;
										N_gyro += 1;
										t_end_gyr = tempo_attuale;
										listTempoGyro_4.add(String.valueOf(tempo_attuale));	listDatiSensGyr_x_4.add(String.valueOf(event.values[0]));	listDatiSensGyr_y_4.add(String.valueOf(event.values[1]));	listDatiSensGyr_z_4.add(String.valueOf(event.values[2]));
									}
									if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)	{
										if (N_acc == 0)	t_begin_acc = tempo_attuale;
										N_acc  += 1;
										t_end_acc = tempo_attuale;
										listTempoAcc_4.add(String.valueOf(tempo_attuale));		listDatiSensAcc_x_4.add(String.valueOf(event.values[0]));	listDatiSensAcc_y_4.add(String.valueOf(event.values[1]));	listDatiSensAcc_z_4.add(String.valueOf(event.values[2]));
									}
								}
								//dopo i 20 secondi di ricerca:
								else{
									//salvo il valore del tempo medio per il Delay precedente:
									t_med_4 = (int)Math.max((t_end_mag-t_begin_mag)/N_mag,Math.max((t_end_gyr-t_begin_gyr)/N_gyro,(t_end_acc-t_begin_acc)/N_acc));
									Log.v(LOG_TAG, DoveSono + "Media dt_rec 4� delay: " + t_med_4);
									acc_0 		= Math.round((acc_0/(float)n_acc)*1000);	// vecchio metodo (no round): acc_0 = acc_0/(float)n_acc;
									acc_1 		= Math.round((acc_1/(float)n_acc)*1000);
									acc_2 		= Math.round((acc_2/(float)n_acc)*1000);
									FlagCheck 	= -1;
									Flag_t  	= 0;
									//salvo i valori trovati, che poi metter� sul file di configurazione;
									feedbackAcc_UD.add(String.valueOf((int)acc_0));			// li salvo come interi, almeno sono di pi� facile lettura
									feedbackAcc_UD.add(String.valueOf((int)acc_1));
									feedbackAcc_UD.add(String.valueOf((int)acc_2));
									Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_x: " + acc_0/(float)n_acc);
									Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_y: " + acc_1/(float)n_acc);
									Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_z: " + acc_2/(float)n_acc);
									Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_x (rounded,*1000,cast ad int): " + feedbackAcc_UD.get(0));
									Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_y (rounded,*1000,cast ad int): " + feedbackAcc_UD.get(1));
									Log.v(LOG_TAG, DoveSono + "Val. Med. Up-Down acc_z (rounded,*1000,cast ad int): " + feedbackAcc_UD.get(2));
									acc_0=0;acc_1=0;acc_2=0;n_acc=0;	//resetto i valori per il conteggio della media, che uso per le altre due direzioni
									
									//salvo i dati, se presenti:
									if(t_med_1!=0 && t_med_2!=0 && t_med_3!=0 && t_med_4!=0){
										salvaDati();
									}
								}
							}
						}				
				break;
			}
			case 4:{
				//caso di assenza di sensori appropriati, salvo ed esco:
				Flag_t	  = 1;
				FlagCheck = -1;
				salvaDati();
				break;
			}
				
		}
	}
	
	//********************************************************************** Dislpay Toast:
	private void DisplayToast(String msg) {
		if(GlobalVariables.displayOnOff==1)
		Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
	}
	private void DisplayToastLong(String msg) {
		if(GlobalVariables.displayOnOff==1)
		Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
	}
	//********************************************************************** Valuto la modalit� Aereo on, senn� avviso:
	private void isAirplaneModeOn(){
		if(Settings.System.getInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0)==0)
			DisplayToast("Activate airplane mode..");
		else
			FlagCheck = 1;
	}
	//********************************************************************** Receiver dello schermo Spento\Acceso:
	public class ScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String type = intent.getAction();
			Log.v(LOG_TAG, DoveSono + "Broadcast arrivato, tipo: " + type);
			if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				Log.v(LOG_TAG, DoveSono + "Schermo spento!" );
				tempo_precedente=tempo_attuale;
				Flag_screen = 0;
			}
			else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
					Log.v(LOG_TAG, DoveSono + "Schermo acceso!" );
					Flag_screen = 1;
			}
		}
	}

	//********************************************************************** Avverto della ricerca di sensori (D.W.):
	public void interazioneUtente(String msg, int rispPos, int rispNeg){
		sceltaFinestraDialogoPos = rispPos;
		sceltaFinestraDialogoNeg = rispNeg;
	 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Swim App:");
		builder.setCancelable(false);
		builder.setMessage(msg);
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				FlagCheck = sceltaFinestraDialogoPos;
			}
		});
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				FlagCheck = sceltaFinestraDialogoNeg;
			}
		});
		builder.setIcon(R.drawable.ic_launcher);
		AlertDialog alertDialog = builder.create();
		//elemento aggiuntivo da inserire se la finestra appare in un Servizio:
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.show();
	}
	//********************************************************************** Analisi sensori Nord/Sud cell -asse Y- (D.W.):
	public void checkSensoreNordSud(){
	 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Swim App:");
		builder.setCancelable(false);
		builder.setMessage("1 of 3:"+'\n'+'\n'+"Please, put the device in vertical position and hold it with the speaker pointing upwards. Then, press OK when ready.");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						DisplayToastLong("I'm analyzing..");
						tempo_precedente 	= tempo_attuale;
						FlagCheck 			= 1;
					}
				});
		builder.setIcon(R.drawable.ic_launcher);
		AlertDialog alertDialog = builder.create();
		//elemento aggiuntivo da inserire se la finestra appare in un Servizio:
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.show();
	}
	//********************************************************************** Analisi sensori Up/Down cell -asse Z- (D.W.):
	public void checkSensoreUpDown(){
	 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Swim App:");
		builder.setCancelable(false);
		builder.setMessage("3 of 3:"+'\n'+'\n'+"Please, put the device in horizontal position with the screen pointing up. Then, press OK when ready."+'\n'+'\n'+"Press 'Back' to return to the previous configuration step.");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						unregisterReceiver(screenReceiver);
						Log.d(LOG_TAG, DoveSono + "Rilascio il Receiver dello schermo (screenReceiver) XXX");
						tempo_precedente 	= tempo_attuale;
						FlagCheck 			= 3;
					}
				});
		builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				unregisterReceiver(screenReceiver);
				Log.d(LOG_TAG, DoveSono + "Rilascio il Receiver dello schermo (screenReceiver) XXX");
				Flag_t				= 0;
				FlagCheck 			= 2;
			}
		});
		builder.setIcon(R.drawable.ic_launcher);
		AlertDialog alertDialog = builder.create();
		//elemento aggiuntivo da inserire se la finestra appare in un Servizio:
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.show();
	}
	//********************************************************************** Analisi sensori Up/Down cell -asse Z- (D.W.):
	public void checkSchermoSpento(){
	 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Swim App:");
		builder.setCancelable(false);
		builder.setMessage("2 of 3:"+'\n'+'\n'+"Once 'OK' is pressed, the device will vibrate twice:"+'\n'+"- At the first vibration, turn off the screen."+'\n'+ "- At the second vibration, turn on the screen and disable the screen lock (if present)."+'\n'+'\n'+"Press 'Back' to return to the previous configuration step.");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						//creo e lancio il Broadcast Receiver:
						IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
						filter.addAction(intent.ACTION_SCREEN_ON);
						screenReceiver = new ScreenReceiver();
						registerReceiver(screenReceiver,filter);
						Log.d(LOG_TAG, DoveSono + "Attivo il Receiver dello schermo (screenReceiver) XXX");
						DisplayToastLong("I'm going to vibrate..");
						tempo_precedente 	= tempo_attuale;
						FlagCheck 			= 2;
					}
				});
		builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Flag_t				= 0;
				FlagCheck 			= 1;
			}
		});
		builder.setIcon(R.drawable.ic_launcher);
		AlertDialog alertDialog = builder.create();
		//elemento aggiuntivo da inserire se la finestra appare in un Servizio:
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.show();
	}
	//********************************************************************** Chiedo se vuole rifare tutta la procedura (D.W.):
	public void rifareTuttaProcedura(){
	 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Swim App:");
		builder.setCancelable(false);
		builder.setMessage("Configuration completed."+'\n'+'\n'+"Do you want to repeat the configuration?");
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {				
				//cancello il file, lo ricreo e riparto con tutta la procedura:
				String newFolder = "/ SWIM APP";	String nomaCartella = "DatiConfigurazione";	String NomeFile="FileConfigurazione";
				File file = new File(Environment.getExternalStorageDirectory() + newFolder + "/" + nomaCartella + "/" + NomeFile + ".txt");
				file.delete();
				Log.v(LOG_TAG, DoveSono + "Cancellato il file.");
				try {
					file.createNewFile();
					Log.v(LOG_TAG, DoveSono + "RI-creato il file.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				FlagCheck = 0;
				Flag_t	  = 0;
			}
		});
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				FlagCheck 			= -1;
				ultimoAvvisoPoiChiudo();
			}
		});
		builder.setIcon(R.drawable.ic_launcher);
		AlertDialog alertDialog = builder.create();
		//elemento aggiuntivo da inserire se la finestra appare in un Servizio:
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.show();
	}
	//********************************************************************** Avviso di fine sessione, fine Service (D.W.):
	public void ultimoAvvisoPoiChiudo(){
	 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Swim App:");
		builder.setCancelable(false);
		if(feedbackNumGyr==0 || feedbackNumAcc==0 || feedbackNumMag==0)
			builder.setMessage("Thank you for the collaboration!");
		else
			builder.setMessage("Thank you for the collaboration." +'\n'+'\n' + "Now we can Spin And Swim!");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						DestroyService();
					}
				});
		builder.setIcon(R.drawable.ic_launcher);
		AlertDialog alertDialog = builder.create();
		//elemento aggiuntivo da inserire se la finestra appare in un Servizio:
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.show();
	}
	//********************************************************************** Salvo i dati presi con i diversi Delay:
	private void salvaDati(){
		// *************** START GESTIONE-SALVATAGGIO FILE DI CONFIGURAZIONE IN MEMORIA INTERNA (28/07/2014):
		// per valutare la presenza del file o meno:
		File fileMemInt = getBaseContext().getFileStreamPath(datiConfigurazione);
		if (!fileMemInt.exists()){
			try {
				Log.v(LOG_TAG, DoveSono + "Il file non esiste, lo creo!");
				FileOutputStream fos = openFileOutput(datiConfigurazione, Context.MODE_PRIVATE);	// MODE_PRIVATE = privato all'applicazione, MODE_WORLD_READABLE = renderlo visibile anche alle altre App.
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				// *************** inizio a salvare i dati (num. acc, num. gyro, num. mag, possibilit� di rec a screen on):
				osw.write(String.valueOf(feedbackNumAcc) + String.valueOf(feedbackNumGyr) + String.valueOf(feedbackNumMag) + String.valueOf(feedbackScreenOff) + '\n');	// TODO: non � String.valueOf(2), ma String.valueOf(feedbackScreenOff). Usato per provare l'analisi a schermo spento.
				//salvo la frequenza di campionamento inerente il tipo di Delay (worst case tra i tre sensori):
				osw.write(String.valueOf(t_med_1) + '\n' + String.valueOf(t_med_2) + '\n' + String.valueOf(t_med_3) + '\n' + String.valueOf(t_med_4) + '\n');
				//Salvo i valori medi degli Accelerometri nelle due configurazioni:
				switch(feedbackAcc_NS.size()){
					case 3:
					{
						osw.write(	feedbackAcc_NS.get(0) + '\n' + feedbackAcc_NS.get(1) + '\n' + feedbackAcc_NS.get(2) + '\n' + 
									feedbackAcc_UD.get(0) + '\n' + feedbackAcc_UD.get(1) + '\n' + feedbackAcc_UD.get(2) + '\n');
						Log.v("NUOT_APP", "Salvato valori: " + 1 + '\t' + feedbackAcc_NS.get(0) + '\t' + feedbackAcc_NS.get(1) + '\t' + feedbackAcc_NS.get(2) + '\n');
						Log.v("NUOT_APP", "Salvato valori: " + 2 + '\t' + feedbackAcc_UD.get(0) + '\t' + feedbackAcc_UD.get(1) + '\t' + feedbackAcc_UD.get(2) + '\n');
//						feedbackAcc_NS.clear();
//						feedbackAcc_UD.clear();
						break;
					}
					case 2:
					{
						osw.write(	feedbackAcc_NS.get(0) + '\n' + feedbackAcc_NS.get(1) + '\n' + String.valueOf(0) + '\n' +
									feedbackAcc_UD.get(0) + '\n' + feedbackAcc_UD.get(1) + '\n' + String.valueOf(0) + '\n');
						break;
					}
					case 1:
					{
						osw.write(	feedbackAcc_NS.get(0) + '\n' + String.valueOf(0) + '\n' + String.valueOf(0) + '\n' + 
									feedbackAcc_UD.get(0) + '\n' + String.valueOf(0) + '\n' + String.valueOf(0) + '\n');
						break;
					}
					case 0:
					{
						osw.write(	String.valueOf(0) + '\n' + String.valueOf(0) + '\n' + String.valueOf(0) + '\n' + 
									String.valueOf(0) + '\n' + String.valueOf(0) + '\n' + String.valueOf(0) + '\n');
						break;
					}
				}
				Log.v("NUOT_APP", "Salvati i dati dei Dt per i quattro Delay! (Mem. Interna)");
				//*************** fine salvataggio dati;
				osw.flush();
				osw.close();
				Log.v(LOG_TAG, DoveSono + "Salvato file di configurazione in Memoria Interna.");
			}catch (IOException e) {       
				e.printStackTrace();
				Log.v(LOG_TAG, DoveSono + "Errore nella creazione del file in mem interna.");
			}
			DisplayToastLong("Sensor_Delay: " + t_med_1 + ", " + t_med_2 + ", " + t_med_3 + ", " + t_med_4);
			//riporto i valori dello "Screen_on_off", dei "sensor_delay" e del numero di "sensori attivi" anche tra le variabili globali
			GlobalVariables.flagAxesOfAcc 		= feedbackNumAcc;								//salvo il numero di assi a disposizione per gli Accelerometri
			GlobalVariables.flagAxesOfGyr 		= feedbackNumGyr;								//salvo il numero di assi a disposizione per i Giroscopi
			GlobalVariables.flagAxesOfMag 		= feedbackNumMag;								//salvo il numero di assi a disposizione per i Magnetometri
			GlobalVariables.flagRecScreenOnOff 	= feedbackScreenOff;							//salvo la possibilit� del cell di REC a schermo spento
			GlobalVariables.dt_delay_rec_1 		= t_med_1;										//salvo il primo valore: SENSOR_DELAY_UI
			GlobalVariables.dt_delay_rec_2 		= t_med_2;										//salvo il primo valore: SENSOR_DELAY_NORMAL
			GlobalVariables.dt_delay_rec_3 		= t_med_3;										//salvo il primo valore: SENSOR_DELAY_GAME
			GlobalVariables.dt_delay_rec_4 		= t_med_4;										//salvo il primo valore: SENSOR_DELAY_FASTEST
		}
		// *************** END GESTIONE-SALVATAGGIO FILE DI CONFIGURAZIONE IN MEMORIA INTERNA (28/07/2014):
		//chiedo se vuole essere ripetuta l'intera procedura:
		rifareTuttaProcedura();
	}
	//********************************************************************** Binder:
	public IBinder onBind(Intent arg0) {
		return null;
	}
	//********************************************************************** Acquisisco W.L. e onSensorChanged:
	private void WakeLock_e_SensorChange(){
		//acquisisco WakeLock:
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wlServConfig = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"MyWakeLock");
		wlServConfig.acquire();
		Log.v(LOG_TAG, DoveSono + "Acquisisco WakeLock (wlServConfig) XXX");
		
		//starto il listener dei sensori:
		sensorServConfig = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAcc = sensorServConfig.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);		//definisco a che tipo di sensore si matcha ACC
		mGyr = sensorServConfig.getDefaultSensor(Sensor.TYPE_GYROSCOPE);			//definisco a che tipo di sensore si matcha GYR
		mMag = sensorServConfig.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);		//definisco a che tipo di sensore si matcha	MAG
		scelgoDelayPerSensori(3);													//definisco il SensorDelay (GAME) per i 3 sensori scelti
	
		Log.v(LOG_TAG, DoveSono + "Start Listener Sensors (sensorServConfig) XXX");
	}
	
	//********************************************************************** Cambio modalit� dell'OnSensorChanged:
	private void scelgoDelayPerSensori(int tipo){
		Log.v(LOG_TAG, DoveSono + DoveSono + "Setto il tipo di SensorDelay per sensori: ");
		//stoppo il listener dei sensori:
		sensorServConfig.unregisterListener(this);
		//starto il listener dei sensori:
		//****************************************************************** 1� MODO (solo 3 sensori):
		switch(tipo){
			case 1:{
				sensorServConfig.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
				sensorServConfig.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_UI);
				sensorServConfig.registerListener(this, mMag, SensorManager.SENSOR_DELAY_UI);
				break;
			}
			case 2:{
				sensorServConfig.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
				sensorServConfig.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_NORMAL);
				sensorServConfig.registerListener(this, mMag, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			}
			case 3:{
				sensorServConfig.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_GAME);
				sensorServConfig.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_GAME);
				sensorServConfig.registerListener(this, mMag, SensorManager.SENSOR_DELAY_GAME);
				break;
			}
			case 4:{
				sensorServConfig.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
				sensorServConfig.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_FASTEST);
				sensorServConfig.registerListener(this, mMag, SensorManager.SENSOR_DELAY_FASTEST);
				break;
			}
		}
		Log.v(LOG_TAG, DoveSono + "Cambiato Delay del SensorsListener (solo i 3 sensori), (" + tipo +"�).");
	}
	//********************************************************************** StopTheService:
	private void DestroyService(){
		stopSelf();
		Log.v(LOG_TAG, DoveSono + "Chiamata funzione 'DestroyService'.");
	}
	//********************************************************************** OnDestroy:
	public void onDestroy() {
		super.onDestroy();
		Log.v(LOG_TAG, DoveSono + "Entro in onDestroy");
		sensorServConfig.unregisterListener(this);
		Log.v(LOG_TAG, DoveSono + "Unregister listener onSensorChenged (sensorServConfig) XXX");
		wlServConfig.release();
		Log.v(LOG_TAG, DoveSono + "Rilascio WakeLock (wlServConfig) XXX");
		Log.v(LOG_TAG, DoveSono + "Esco da onDestroy");
	}
}
