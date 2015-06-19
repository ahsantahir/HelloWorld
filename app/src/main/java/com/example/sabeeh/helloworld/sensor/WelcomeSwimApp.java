package com.example.sabeeh.helloworld.sensor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.sabeeh.helloworld.R;

public class WelcomeSwimApp extends Activity {
	//VARIABILI UTILIZZATE NELL'ACTIVITY:
	static final String LOG_TAG = "NUOT_APP";
	private String DoveSono = "(WelcomeActivity) ";
	//bottoni per informazioni:
	private Button MailButton;
	private Button InfoAscolto;
	private Button InfoMail;
	private Button VaiAiSensori;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_swim_app);
		
		//DEFINIZIONE BOTTONI:
		MailButton 	= (Button) findViewById(R.id.MailButton);
		InfoAscolto = (Button) findViewById(R.id.InfoAscolto);
		InfoMail 	= (Button) findViewById(R.id.InfoMail);
		VaiAiSensori= (Button) findViewById(R.id.RuotaNuota);
		//Bottone per invio mail:
		MailButton.setOnClickListener(new View.OnClickListener() {
			String oggetto;
			String msg;
			String[] indirizzi = new String[]{"swimapp.ruotaenuota@gmail.com"};
			@Override
			public void onClick(View arg0) {
				//se il service di ascolto � spento, allora invio:
				if(Settings.System.getInt(getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0)==0){
				// TODO Auto-generated method stub
				oggetto = "Swim App";
				msg 	= "1) COMPLETA I CAMPI SOTTO: "
						+ '\n'+'\n' + "- Atleta: "
						+ '\n'+'\n' + "- Numero registrazioni da analizzare: " 
						+ '\n'+'\n' + "- Cosa sono (Es. '50 m SL: 25 progressione, 25 veloce'): "
						+ '\n'+'\n' + "- Problemi e/o suggerimenti per il miglioramento dell'App.: "
						+ '\n'+'\n' + "2) ALLEGA I FILE CORRETTI."
						+ '\n'+'\n' + "3) INVIA EMAIL."
						+'\n'+'\n'+'\n' + "----------"
						+ '\n' +"Riceverai presto una mail dei tuoi dati analizzati." +
						'\n'+'\n' +"Grazie della collaborazione!" + '\n' + "Lo Staff di SwimApp";
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.putExtra(Intent.EXTRA_EMAIL, indirizzi);
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, oggetto);
				emailIntent.putExtra(Intent.EXTRA_TEXT, msg);
				emailIntent.setType("message/rfc822");
				//emailIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(getExternalFilesDir(Environment.getExternalStorageDirectory() + nomeCartella + "/" + cartellaGiorno + "/" + nomeFile + ".txt")));
				startActivity(emailIntent);
				}
				else
					InvioSSSServiceOff();
			}
		});
		//Bottone Info Ascolto/Registrazione:
		InfoAscolto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				InfoAscoltoDialogWindow();
			}
		});
		//Bottone Info Invio Mail:
		InfoMail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				InfoInvioMailDialogWindow();
			}
		});
		//Bottone per lanciare il Servizio:
		VaiAiSensori.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {


                //Launch the activity class for the record purpose
				//Intent myIntent = new Intent(WelcomeSwimApp.this, SwimAppMainActivity.class);
			//	WelcomeSwimApp.this.startActivity(myIntent);
				Log.v(LOG_TAG, DoveSono + "Startata la nuova Activity");
			}
		});
	}
	
	//********************************************************************** OnPause:
	public void onPause(){
		super.onPause();
		Log.v(LOG_TAG, DoveSono + "OnPause");
	}
	//********************************************************************** OnResume:
	public void onResume()
	{
	   	super.onResume();
	   	Log.v(LOG_TAG, DoveSono + "OnResume");
	}
	//********************************************************************** OnDestroy:
	public void onDestroy(){
	   	super.onDestroy();
	   	Log.v(LOG_TAG, DoveSono + "OnDestroy");
	}
	//********************************************************************** Invio posta SOLO SE il Service � off (Dialog window):
	public void InvioSSSServiceOff(){
		 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Per inviare i dati devi:");
			builder.setCancelable(false);
			builder.setMessage("- settare la sessione in modalit� off;" + '\n' + "- uscire dalla modalit� offline;");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							
						}
					});
			builder.setIcon(R.drawable.ic_launcher);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
	 }
	//********************************************************************** Invio sulla modalit� di registrazione:
	public void InfoAscoltoDialogWindow(){
		 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Info Registrazione:");
			builder.setCancelable(false);
			/*
			builder.setMessage("Le operazioni per effettuare una o pi� registrazioni durante una sessione, sono:"
					 + '\n' + "1) Inserire la modalit� aereo/offline."
					 + '\n' + "2) Accedere all'area 'Ruota.. e nuota!', quindi cliccare il pulsante per lo start/stop della sessione (il cell inizia a vibrare e suonare ogni 3 sec). Nota che NON sta registrando, ma aspettando il tuo segnale di inizio registrazione."
					 + '\n' + "3) Da ora il cell rimane, se non si preme il pulsante apposito, a schermo acceso, ed acceso deve rimanere, quindi: copri lo schermo con del materiale (gomma) che prevenga la digitazione involontaria, in pi� fai in modo che il pulsante di spegnimento schermo non possa essere facilmente premuto."
					 + '\n' + "4) Se durante la sessione verr� premuto erroneamente tale pulsante, il cellulare inizier� a vibrare mezzo secondo, ogni secondo. Smetter� solo quando, premendo ancora tale pulsante, lo schermo si riaccender�."
					 + '\n' + "4) Prepara il cell per la nuotata (ricorda che al cell non piace l'acqua: FAI ATTENZIONE!)."
					 + '\n' + "5) Fissa il cellulare ad una cintura (con fascette od altro)."
					 + '\n' + "6) Metti la cintura BEN STRETTA (il cell non deve muoversi!)."
					 + '\n' + "7) Una volta fissata, il cell deve trovarsi sulla zona lombare, il pi� possibile centrale alla schiena."
					 + '\n' + "8) Stando in piedi, effettuare una rotazione su se stessi ad alta velocit�, per circa 1 secondo."
					 + '\n' + "9) Il cell far� una vibrazione continuata di 1 secondo ed inizia a registrare! Nota che durante la registrazione non c'� la vibrazione ogni 3 secondi (punto 2)."
					 + '\n' + "10) Nuota!"
					 + '\n' + "11) Quando finisci la percorrenza, mettiti in piedi ed effettua una rotazione su te stesso, per circa 1 secondo."
					 + '\n' + "12) Il cell far� una vibrazione continuata di 2 secondi, la registrazione si ferma ed il file viene salvato."
					 + '\n' + "13) Il cell rinizia a vibrare e suonare ogni 3 secondi (aspetta per altre registrazioni)." 
					 + '\n' + "14) Puoi ora effettuare un'altra analisi, ripartendo dal punto 8."
					 + '\n' + '\n' + "NOTE:"
					 + '\n' + "- Quando ruoti su te stesso, metti una mano sul cell, per sentire meglio le lunghe vibrazioni di start/stop."
					 + '\n' + "- Nel nome del file salvato � riportata anche la durata dell'analisi: tieni il tempo tra le due vibrazioni lunghe di start/stop registrazione, in modo da identificare le analisi svolte e scartare quelle non desiderate.");
			 */
			builder.setMessage("Le operazioni per effettuare una o pi� registrazioni durante una sessione, sono:"
					 + '\n' + "1) Inserire la modalit� aereo/offline."
					 + '\n' + "2) Accedere all'area 'Ruota.. e nuota!', quindi cliccare il pulsante per lo start/stop della sessione (il cell inizia a vibrare e suonare ogni 3 sec). Questa modalit� � denominata 'Standby': il cellulare sta aspettando un segnale dall'atleta, per l'inizio della sessione di registrazione."
					 + '\n' + "3) E' importante che da questo momento, fino alla conclusione delle registrazioni, il cellulare rimanga sulla finestra corrente (anche se lo schermo si spegne)."
					 + '\n' + "4) Da ora il cell rimane a schermo acceso, se non si preme il pulsante apposito, quindi: copri lo schermo con del materiale (gomma) che prevenga la digitazione involontaria, in pi� fai in modo che il pulsante di spegnimento schermo non possa essere facilmente premuto."
					 + '\n' + "5) Durante la modalit� di 'Standby', quella in cui ci troviamo ora, si pu� spegnere lo schermo per risparmiare batteria. Quando per� si vuole iniziare una sessione di registrazione, si dovr� riaccedere lo schermo, premendo semplicemente il tasto (non importa sbloccare lo schermo)."
					 + '\n' + "6) Prepara il cell per la nuotata (ricorda che al cell non piace l'acqua: FAI ATTENZIONE!)."
					 + '\n' + "7) Fissa il cellulare ad una cintura, meglio se elastica (con fascette od altro)."
					 + '\n' + "8) Metti la cintura BEN STRETTA (il cell non deve muoversi!)."
					 + '\n' + "9) Una volta fissata, il cell deve trovarsi sulla zona lombare, il pi� possibile centrale alla schiena."
					 + '\n' + "10) (Se lo schermo era spento, ora devi accenderlo) Stando in piedi, effettuare una rotazione su se stessi ad alta velocit�, per circa 1 secondo."
					 + '\n' + "11) Il cell far� una vibrazione continuata di 1 secondo ed inizia a vibrare e suonare ogni 1 secondo. Ora il cellulare � pronto per registrare!"
					 + '\n' + "12) Se l'atleta, per almeno 2 secondi, manterr� una posizione orizzontale, il cellulare vibrer� a lungo (1.5 secondi) ed inizier� a registrare."
					 + '\n' + "13) Nuota!"
					 + '\n' + "14) Non appena l'atleta si metter� in piedi (corpo in verticale), per almeno 2 secondi, il cellulare vibrer� a lungo (2.5 secondi), terminando la registrazione e salvandola in memoria!"
					 + '\n' + "15) Non appena il cellulare torna a vibrare ogni secondo (quando ha finito di salvare il file), si pu� riprendere l'iter '10->11->12' ad oltranza..."
					 + '\n' + "16) Se durante la registrazione verr� premuto erroneamente il pulsante dello spegnimento schermo, il cellulare inizier� a vibrare ogni mezzo secondo. Smetter� solo quando, premendo ancora tale pulsante, lo schermo si riaccender�. Quindi il cellulare sar� di nuovo pronto a registrare, operando come dal punto 10."
					 + '\n' + "17) Quando finisci la sessione di registrazioni, mettiti in piedi ed effettua una rotazione su te stesso, per circa 1 secondo."
					 + '\n' + "18) Il cell far� una vibrazione di feedback e torner� a vibrare e suonare ogni 3 secondi, entrando in modalit� 'Standby' (come al punto 2)."
					 + '\n' + '\n' + "NOTE:"
					 + '\n' + "- Quando effettui le varie operazioni, metti una mano sul cell, per sentire meglio le vibrazioni di feedback che il cell ti sta dando!"
					 + '\n' + "- Nel nome del file salvato � riportata anche la durata dell'analisi: tieni il tempo tra le due vibrazioni lunghe di start/stop registrazione, in modo da identificare le analisi svolte e scartare quelle non desiderate.");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							
						}
					});
			builder.setIcon(R.drawable.ic_launcher);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
	 }
	//********************************************************************** Invio sulla modalit� di registrazione:
	public void InfoInvioMailDialogWindow(){
		 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 	builder.setTitle("Info Invio Dati:");
			builder.setCancelable(false);
			builder.setMessage("Invio dei file:"
					 + '\n' + "1) Occorre che il dispositivo sia online, quindi disattivare la modalit� aereo."
					 + '\n' + "2) Cliccare sul pulsante 'Invia Dati'."
					 + '\n' + "3) Completare brevemente i campi preimpostati della mail (non obbligatorio), descrivendo la tipologia di analisi svolta."
					 + '\n' + "4) Inserisci qui anche eventuali problemi e/o suggerimenti riscontrati durante l'uso dell'Applicazione."
		 			 + '\n' + "5) ALLEGARE IL/I FILE CORRETTI (i file sono salvati in una cartella chiamata 'SwimApp', con sottocartelle divise per giorno)."
					 + '\n' + "6) Inviare!");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							
						}
					});
			builder.setIcon(R.drawable.ic_launcher);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
	 }
}
