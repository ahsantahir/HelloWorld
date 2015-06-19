package com.example.sabeeh.helloworld.sensor;

public class GlobalVariables {
	public static int LogEffettuati			= 0;	// LogEffettuati>=100, log terminati
	public static int ggRimanenti			= 0;	// 31 � il massimo
	public static int NewLogEffettuati		= 0;	// setto a 1 se ho effettuato nuovi log (lo uso per salvare o meno il file in mem interna nell'OnResume)
    
	public static int saveOnIntExtStorage   = 0;    // 0 = internal memory (default); 1 = external memory (SD_card o shared_internal_memory);
	
	public static int flagService 			= 0;
	public static int displayOnOff 			= 1;
	public static int airplaneMode 			= 0; 	// 0=OFF, 1=ON;
	public static int batteryState 			= 0; 	// 0=buono, 1=bassa;
	public static int flagSaveBattery 		= 0;	// 0=non controllo la batteria, 1=controllo sempre la batteria ogni tot
	public static double batteryValLow		= 0.25;	// valore basso della batteria, sul quale fa riferimento la richiesta del "controllo della batteria"
	public static double batteryValMin		= 0.10;	// valore minimo della batteria, che viene pescato sia dall'Activity che dal Service
	
	public static String myNewFolderDay 	= "";
	
	public static int flagAxesOfAcc			= 0;	// usato per valutare se il cellulare ha i sensori necessari o meno per effettuare una registrazione 
	public static int flagAxesOfGyr			= 0;	// " "
	public static int flagAxesOfMag			= 0;	// " "
	public static int flagRecScreenOnOff	= 0;	// =1 se non pox rec a schermo spento e =2 se pox. Lo inserisco in fase di configurazione.
	
	public static int dt_delay_rec_1		= 0;	// valori post configurazione (o dal file salvato), utilizzate per la registrazione secondo un adeguato Delay
	public static int dt_delay_rec_2		= 0;
	public static int dt_delay_rec_3		= 0;
	public static int dt_delay_rec_4		= 0;
	public static int dt_minimo_per_rec		= 30;	// dt per il campionamento dai sensori deve essere <=30 msec, calcolato come 1/(2*16Hz).
	
	public static double gyro_threshold		= 0;	// 0 by default. This value changes if the user chooses a different value from the related button (in the Main activity). TODO: remove for the final version (this is TEST VARIABLE)
}