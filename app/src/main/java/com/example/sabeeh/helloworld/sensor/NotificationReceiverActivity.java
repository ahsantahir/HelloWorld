package com.example.sabeeh.helloworld.sensor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.sabeeh.helloworld.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationReceiverActivity extends Activity {
	

	static final String LOG_TAG = "NUOT_APP";
	//elementi che uso a cavallo tra la lista degli elementi e la scelta tramite Dialog window:
	//elements that I use at the turn of the list of items and choice through Dialog window
	private ArrayList<String> list;
	private StableArrayAdapter adapter;
	private String item;
	
	protected void onCreate(Bundle savedInstancedState){
		super.onCreate(savedInstancedState);
		
		if(GlobalVariables.myNewFolderDay!=""){
			setContentView(R.layout.result);
			//informo l'utente sul come cancellare i file:
			//inform the user on how to delete files
			infoListaLog();
			
			final ListView listview = (ListView) findViewById(R.id.listview);
			File f = new File(GlobalVariables.myNewFolderDay);
			File file[] = f.listFiles();
			Log.e(LOG_TAG, GlobalVariables.myNewFolderDay);
			Log.e(LOG_TAG, "Numero file: " + String.valueOf(file.length));
		    list = new ArrayList<String>();
		    for (int i = 0; i < file.length; ++i) {
		      list.add(file[i].getName());
		      Log.e(LOG_TAG, "Nome file: " + file[i].getName());
		    }
		    adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);
		    listview.setAdapter(adapter);
		    
		    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    	@Override
		        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
		          item = (String) parent.getItemAtPosition(position);
		          richiestaCancellazione();
		        }
		      });
		}
	}

private class StableArrayAdapter extends ArrayAdapter<String> {

    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public StableArrayAdapter(Context context, int textViewResourceId,List<String> objects) {
      super(context, textViewResourceId, objects);
      for (int i = 0; i < objects.size(); ++i) {
    	  mIdMap.put(objects.get(i), i);
      }
    }
    @Override
    public long getItemId(int position) {
    	String item = getItem(position);
    	return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
    	return true;
    }

  }

//************************************ Cancellazione o meno di un elemento selezionato (Dialog window): ***
//Cancellation or less of an element selected


 public void richiestaCancellazione(){
	 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Swim App:");
		builder.setCancelable(false);
		builder.setMessage("Cancellare l'elemento selezionato?");
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//cancello il file dalla lista e dalla memoria:
				//I delete files from the list and from memory
				list.remove(item);
				adapter.notifyDataSetChanged();
				File fileToCancel = new File(GlobalVariables.myNewFolderDay + "/" + item);
				if(fileToCancel.exists())	fileToCancel.delete();
			}
		});
		builder.setIcon(R.drawable.ic_launcher);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
 }
//************************************ Info sul perch� di tale lista: per cancellare file erronei (Dialog window): ***
 //Info about why that list : to delete files erroneous
 
 public void infoListaLog(){
	 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Swim App:");
		builder.setCancelable(false);
		builder.setMessage("Visiona le registrazioni." + '\n' + "Se presenti registrazioni erronee, selezionale e cancellale." + '\n'+'\n' + "NOTA: attenzione ai file che cancelli, la cancellazione sar� definitiva.");
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