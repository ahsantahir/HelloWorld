package com.example.sabeeh.helloworld.backend;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.example.sabeeh.helloworld.GlobalSwimRecords;
import com.example.sabeeh.helloworld.entites.swim;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by mac on 22/03/15.
 */
public class FileAnalysis {




    TextView messageText;
    Button uploadButton;
    String message;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String upLoadServerUri = null;
    private  String phpresponse=null;
    private MobileServiceClient mClient;
    /**********  File Path *************/
    final String uploadFilePath = "/storage/emulated/0/Android/data/";
    final String uploadFileName = "2014-10-11__15-54-52__50sec.txt";
   // private MobileServiceTable<swim_record> mswim_record;

    public JasonHandler getJasonHandler() {
        return jasonHandler;
    }

    private JasonHandler jasonHandler;

    //private MobileServiceClient mClient;
    private JasonHandler obj;
    private ProgressDialog progress;
    private ProgressBar mProgressBar;
    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";
    public static  boolean azure_check=false;
    public boolean bAuthenticating = false;
    public final Object mAuthenticationLock = new Object();
    public int swim_count=0;
    private List<String> item = null;

    private List<String> path = null;

    private String root="/";

    private TextView myPath;
    private Context mContext;

    public void broadcastIntent()
    {
        Intent intent = new Intent();
        intent.setAction("com.example.sabeeh.helloworld.Analyse_DATA");
        mContext.sendBroadcast(intent);
    }


    public FileAnalysis(Context context) {
        mContext = context;

      // MyApplication myApplication = (MyApplication) this.getApplicationContext();
       // myApplication.mainActivity = this;






    }

    public void UploadFileToVM(String Filename)
    {
        azure_check=false;

        if(!isNetworkConnected(mContext))
        {
            Toast.makeText(mContext, "Connect internet and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        File temp=new File(Filename);
        if(!temp.isFile())
        {
            Toast.makeText(mContext, "Local file not found..", Toast.LENGTH_LONG).show();
            return;
        }
        // progress.setMessage("Uploading Log file ");
        // progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // progress.setIndeterminate(true);
        // progress.show();

       // final int totalProgressTime = 100;



        new UploadFilesTask().execute(Filename);
    }
    private class UploadFilesTask extends AsyncTask<String, String, String> {




        protected String doInBackground(String... files) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://swimapp.cloudapp.net/upload.php");

            File file = new File(files[0]);
            if(!file.isFile())
            {
                return null;
            }
            //   InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
            //  reqEntity.setContentType("binary/octet-stream");
            // reqEntity.setChunked(true);

            try {
                // Add your data
                // List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                //nameValuePairs.add(new BasicNameValuePair("Username", "ahsan.tahir.92"));
                //nameValuePairs.add(new BasicNameValuePair("fileToUpload",reqEntity));
                //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();

/* example for setting a HttpMultipartMode */
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);



                //builder.addPart("Username","ahsan.tahir.92");

                builder.addTextBody("Username","ahsan.tahir.92");
                builder.addPart("fileToUpload", new FileBody(file));
                HttpEntity entity=builder.build();
                httppost.setEntity(entity);
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity hentity = response.getEntity();
                InputStream is = hentity.getContent();
                phpresponse=inputStreamToString(is).toString();
                phpresponse=phpresponse.replace("results = ", "");
                //jasonHandler=new JasonHandler();
               // jasonHandler.readAndParseJSON(phpresponse);





                GlobalSwimRecords obj=new GlobalSwimRecords();
                if(obj.multiple_swim_flag==0) {
                    obj.jasonForAnalysis.readAndParseJSON(phpresponse);
                    swim temp_obj=getSwim(files[0]);
                    if(temp_obj.jason_analysis.isEmpty()) {
                        temp_obj.jason_analysis = phpresponse;
                        temp_obj.analysed = true;
                        temp_obj.Laps=(int)Math.floor(obj.jasonForAnalysis.getJNlap());
                      //  temp_obj.duration=obj.jasonForAnalysis.getJTot_time().floatValue();

                        temp_obj.save();

                    }

                    SharedPreferences prefs = mContext.getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("ahsan.tahir.92", phpresponse);

                    editor.commit();
                    Log.w("response", phpresponse);
                    broadcastIntent();

                }
                else
                {
                    obj.ListjasonForAnalysis.get(swim_count).readAndParseJSON(phpresponse);
                    obj.analysis_flag_count++;

                    if(obj.analysis_flag_count==obj.ListjasonForAnalysis.size())
                        broadcastIntent();
                }

              //  createAndShowDialog(phpresponse,"response");
                //messageText.setText();



            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                createAndShowDialog(e,"Ooops");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                createAndShowDialog(e,"Ooops");
            }





            return "";
        }

        @Override
        protected void onPreExecute(){


          //  dialog.setMessage("Uploading the File to server");
           // dialog.show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //setProgressPercent(progress[0]);
            // dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file..."+ progress[0], true);
            //       dialog.show();
            //mProgressBar.setProgress( Integer.parseInt(progress[0]);

            // messageText.setText(progress[0]);

            // Log.w("Progress",progress[0]);
            // messageText.setText("Working" +  progress[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            // showDialog("Downloaded " + result + " bytes");\
         //   dialog.dismiss();
          //  message=phpresponse;
           // messageText.setText(message);
            Log.w("Post Execute",phpresponse);
        }
    }


    private StringBuilder inputStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        }
        catch(Exception e)
        {

        }

        // Return full string
        return total;
    }

    //Interface support
    private void createAndShowDialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

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

    private boolean isNetworkConnected(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public static swim getSwim(String local_file) {
        return new Select()
                .from(swim.class)
                .where("Local_file = ?",local_file)
                .executeSingle();
    }


}
