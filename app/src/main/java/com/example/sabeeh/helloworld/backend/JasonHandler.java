package com.example.sabeeh.helloworld.backend;

/**
 * Created by mac on 05/01/15.
 */


import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//import com.example.mac.test2.entites.swim_record;

public class JasonHandler {
    private String country = "county";
    private String temperature = "temperature";
    private String humidity = "humidity";
    private String pressure = "pressure";
    private String urlString = null;
  // public swim_record swim_obj;
    public volatile boolean parsingComplete = true;


    private Integer JType;



    private String JUser;



    private Double Jnlap;

    private Double JFreq;



    private JSONArray JCoordina;



    private Double JTot_time;



    private Double JTot_distance;



    private JSONArray JStroke_each_pool;



    private JSONArray JTot_stroke;



    private JSONArray JSplit;



    private Double JTiming_turn;



    private JSONArray JCycle_rate_l;



    private JSONArray JCycle_rate_r;



    private JSONArray JMean_velocity;



    private JSONArray JStroke_length;


    private JSONArray JStroke_freq;



    private JSONArray JRoll_peaks;



    private JSONArray JMean_roll_dx;



    private JSONArray JMean_roll_sx;


    private JSONArray JStd_roll_dx;



    private JSONArray JStd_roll_sx;



    private JSONArray JMean_roll;



    private JSONArray JStd_roll;



    private JSONArray JMean_pitch;



    private JSONArray JStd_pitch;



    private JSONArray JClean_stroke_time;


    private JSONArray Jglobal_coordination;



    private JSONArray JErrore;



    private JSONObject fatalError;


    private String arraytype;


    private  String arraysize;

    private  String arraydata;



    @SuppressLint("NewApi")
    public void readAndParseJSON(String in) {
        try {
            //JSONArray responseArray = new JSONArray("results");
           // JSONObject sys = responseArray.toJSONObject(responseArray);
            //JSONObject jsonObject = (JSONObject) jsonParser.parse(strippedJSON)

            JSONObject sys  = new JSONObject(in);
            if(sys.optInt("type")!=0)
            JType=sys.optInt("type");

            if(sys.optString("user_log")!=null)
            JUser=sys.optString("user_log");

            if(sys.optDouble("freq")!=0)
            JFreq= sys.optDouble("freq");

            if(sys.optJSONArray("coordina")!=null)
            JCoordina=sys.optJSONArray("coordina");

            if(sys.optJSONArray("tot_stroke")!=null)
            JTot_time=sys.optDouble("tot_time");


            if(sys.optDouble("tot_distace")!=0)
            JTot_distance=sys.optDouble("tot_distace");



            if(sys.optDouble("nlap")!=0)
                Jnlap=sys.optDouble("nlap");

            if(sys.optJSONArray("stroke_each_pool")!=null)
            JStroke_each_pool=sys.optJSONArray("stroke_each_pool");

            if(sys.optJSONArray("tot_stroke")!=null)
                JTot_stroke=sys.optJSONArray("tot_stroke");

            if(sys.optJSONArray("split")!=null)
            JSplit=sys.optJSONArray("split");

            if(sys.optDouble("timing_turn")!=0)
            JTiming_turn=sys.optDouble("timing_turn");

            if(sys.optJSONArray("cycle_Rate_l")!=null)
            JCycle_rate_l=sys.optJSONArray("cycle_Rate_l");

            if(sys.optJSONArray("cycle_Rate_r")!=null)
            JCycle_rate_r= sys.optJSONArray("cycle_Rate_r");

            if(sys.optJSONArray("mean_velocity")!=null)
            JMean_velocity=sys.optJSONArray("mean_velocity");

            if(sys.optJSONArray("stroke_length")!=null)
            JStroke_length=sys.optJSONArray("stroke_length");

            if(sys.optJSONArray("stroke_freq")!=null)
            JStroke_freq=sys.optJSONArray("stroke_freq");

            if(sys.optJSONArray("roll_peaks")!=null)
            JRoll_peaks=sys.optJSONArray("roll_peaks");

            if(sys.optJSONArray("mean_roll_dx")!=null)
            JMean_roll_dx=sys.optJSONArray("mean_roll_dx");

            if(sys.optJSONArray("mean_roll_sx")!=null)
            JMean_roll_sx=sys.optJSONArray("mean_roll_sx");

            if(sys.optJSONArray("std_roll_dx")!=null)
            JStd_roll_dx=sys.optJSONArray("std_roll_dx");

            if(sys.optJSONArray("std_roll_sx")!=null)
            JStd_roll_sx=sys.optJSONArray("std_roll_sx");

            if(sys.optJSONArray("mean_roll")!=null)
            JMean_roll=sys.optJSONArray("mean_roll");

            if(sys.optJSONArray("std_roll")!=null)
            JStd_roll=sys.optJSONArray("std_roll");

            if(sys.optJSONArray("mean_pitch")!=null)
            JMean_pitch=sys.optJSONArray("mean_pitch");

            if(sys.optJSONArray("std_pitch")!=null)
            JStd_pitch=sys.optJSONArray("std_pitch");

            if(sys.optJSONArray("clean_stroke_time")!=null)
            JClean_stroke_time=sys.optJSONArray("clean_stroke_time");

            if(sys.optJSONArray("global_coordination")!=null)
                Jglobal_coordination=sys.optJSONArray("global_coordination");


            if(sys.optJSONArray("errore")!=null)
            JErrore=sys.optJSONArray("errore");

            if(sys.optJSONObject("fatal_error")!=null) {
                fatalError = sys.getJSONObject("fatal_error");

                arraytype = fatalError.getString("_ArrayType_");
                arraysize = fatalError.getString("_ArraySize_");
                arraydata = fatalError.getString("_ArrayData_");
            }








            /*JSONObject sys  = reader.getJSONObject("results");
            swim_obj.setmType(sys.getString("type"));
            swim_obj.setmUser(sys.getString("user_log"));
            swim_obj.setmFreq(sys.getDouble("freq"));
            swim_obj.setmCoordina(sys.getJSONArray("coordina"));
            swim_obj.setmTot_time(sys.getDouble("tot_time"));
            swim_obj.setmTot_distance(sys.getDouble("tot_distance"));
            swim_obj.setmStroke_each_pool(sys.getJSONArray("stroke_each_pool"));
            swim_obj.setmTot_stroke(sys.getJSONArray("tot_stroke"));
            swim_obj.setmSplit(sys.getJSONArray("split"));
            swim_obj.setmTiming_turn(sys.getDouble("timing_turn"));
            swim_obj.setmCycle_rate_l(sys.getJSONArray("cycle_Rate_l"));
            swim_obj.setmCycle_rate_r(sys.getJSONArray("cycle_Rate_r"));
            swim_obj.setmMean_velocity(sys.getJSONArray("mean_velocity"));
            swim_obj.setmStroke_length(sys.getJSONArray("stroke_length"));
            swim_obj.setmStroke_freq(sys.getJSONArray("stroke_freq"));
            swim_obj.setmRoll_peaks(sys.getJSONArray("roll_peaks"));
            swim_obj.setmMean_roll_dx(sys.getJSONArray("mean_roll_dx"));
            swim_obj.setmMean_roll_sx(sys.getJSONArray("mean_roll_sx"));
            swim_obj.setmStd_roll_dx(sys.getJSONArray("std_roll_dx"));
            swim_obj.setmStd_roll_sx(sys.getJSONArray("std_roll_sx"));
            swim_obj.setmMean_roll(sys.getJSONArray("mean_roll"));
            swim_obj.setmStd_roll(sys.getJSONArray("std_roll"));
            swim_obj.setmMean_pitch(sys.getJSONArray("mean_pitch"));
            swim_obj.setmStd_pitch(sys.getJSONArray("std_pitch"));
            swim_obj.setmClean_stroke_time(sys.getJSONArray("clean_stroke_time"));
            swim_obj.setmErrore(sys.getInt("errore"));
            JSONObject fatalError=sys.getJSONObject("fatal_error");
            String arraytype=fatalError.getString("_ArrayType_");
            String arraysize=fatalError.getString("_ArraySize_");
            String arraydata= fatalError.getString("_ArrayData_");*/


























            parsingComplete = true;



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public void fetchJSON(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    String data = convertStreamToString(stream);

                    readAndParseJSON(data);
                    stream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }


    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }



    //getter setter methods


    public Integer getJType() {
        return JType;
    }

    public void setJType(Integer JType) {
        this.JType = JType;
    }

    public String getJUser() {
        return JUser;
    }

    public void setJUser(String JUser) {
        this.JUser = JUser;
    }

    public Double getJFreq() {
        return JFreq;
    }

    public void setJFreq(Double JFreq) {
        this.JFreq = JFreq;
    }

    public JSONArray getJCoordina() {
        return JCoordina;
    }

    public void setJCoordina(JSONArray JCoordina) {
        this.JCoordina = JCoordina;
    }

    public Double getJTot_time() {
        return JTot_time;
    }

    public void setJTot_time(Double JTot_time) {
        this.JTot_time = JTot_time;
    }

    public Double getJTot_distance() {
        return JTot_distance;
    }

    public void setJTot_distance(Double JTot_distance) {
        this.JTot_distance = JTot_distance;
    }
    public JSONArray getJStroke_each_pool() {
        return JStroke_each_pool;
    }

    public void setJStroke_each_pool(JSONArray JStroke_each_pool) {
        this.JStroke_each_pool = JStroke_each_pool;
    }

    public JSONArray getJTot_stroke() {
        return JTot_stroke;
    }

    public void setJTot_stroke(JSONArray JTot_stroke) {
        this.JTot_stroke = JTot_stroke;
    }

    public JSONArray getJSplit() {
        return JSplit;
    }

    public void setJSplit(JSONArray JSplit) {
        this.JSplit = JSplit;
    }

    public Double getJTiming_turn() {
        return JTiming_turn;
    }

    public void setJTiming_turn(Double JTiming_turn) {
        this.JTiming_turn = JTiming_turn;
    }

    public JSONArray getJCycle_rate_l() {
        return JCycle_rate_l;
    }

    public void setJCycle_rate_l(JSONArray JCycle_rate_l) {
        this.JCycle_rate_l = JCycle_rate_l;
    }

    public JSONArray getJCycle_rate_r() {
        return JCycle_rate_r;
    }

    public void setJCycle_rate_r(JSONArray JCycle_rate_r) {
        this.JCycle_rate_r = JCycle_rate_r;
    }
    public JSONArray getJMean_velocity() {
        return JMean_velocity;
    }

    public void setJMean_velocity(JSONArray JMean_velocity) {
        this.JMean_velocity = JMean_velocity;
    }
    public JSONArray getJStroke_length() {
        return JStroke_length;
    }

    public void setJStroke_length(JSONArray JStroke_length) {
        this.JStroke_length = JStroke_length;
    }

    public JSONArray getJStroke_freq() {
        return JStroke_freq;
    }

    public void setJStroke_freq(JSONArray JStroke_freq) {
        this.JStroke_freq = JStroke_freq;
    }


    public JSONArray getJRoll_peaks() {
        return JRoll_peaks;
    }

    public void setJRoll_peaks(JSONArray JRoll_peaks) {
        this.JRoll_peaks = JRoll_peaks;
    }
    public JSONArray getJMean_roll_dx() {
        return JMean_roll_dx;
    }

    public void setJMean_roll_dx(JSONArray JMean_roll_dx) {
        this.JMean_roll_dx = JMean_roll_dx;
    }
    public JSONArray getJMean_roll_sx() {
        return JMean_roll_sx;
    }

    public void setJMean_roll_sx(JSONArray JMean_roll_sx) {
        this.JMean_roll_sx = JMean_roll_sx;
    }

    public JSONArray getJStd_roll_dx() {
        return JStd_roll_dx;
    }

    public void setJStd_roll_dx(JSONArray JStd_roll_dx) {
        this.JStd_roll_dx = JStd_roll_dx;
    }
    public JSONArray getJStd_roll_sx() {
        return JStd_roll_sx;
    }

    public void setJStd_roll_sx(JSONArray JStd_roll_sx) {
        this.JStd_roll_sx = JStd_roll_sx;
    }
    public JSONArray getJStd_roll() {
        return JStd_roll;
    }

    public void setJStd_roll(JSONArray JStd_roll) {
        this.JStd_roll = JStd_roll;
    }
    public JSONArray getJMean_roll() {
        return JMean_roll;
    }

    public void setJMean_roll(JSONArray JMean_roll) {
        this.JMean_roll = JMean_roll;
    }
    public JSONArray getJMean_pitch() {
        return JMean_pitch;
    }

    public void setJMean_pitch(JSONArray JMean_pitch) {
        this.JMean_pitch = JMean_pitch;
    }
    public JSONArray getJStd_pitch() {
        return JStd_pitch;
    }

    public void setJStd_pitch(JSONArray JStd_pitch) {
        this.JStd_pitch = JStd_pitch;
    }

    public JSONArray getJClean_stroke_time() {
        return JClean_stroke_time;
    }

    public void setJClean_stroke_time(JSONArray JClean_stroke_time) {
        this.JClean_stroke_time = JClean_stroke_time;
    }
    public JSONArray getJErrore() {
        return JErrore;
    }

    public void setJErrore(JSONArray JErrore) {
        this.JErrore = JErrore;
    }
    public JSONObject getFatalError() {
        return fatalError;
    }

    public void setFatalError(JSONObject fatalError) {
        this.fatalError = fatalError;
    }

    public String getArraydata() {
        return arraydata;
    }

    public void setArraydata(String arraydata) {
        this.arraydata = arraydata;
    }
    public String getArraysize() {
        return arraysize;
    }

    public void setArraysize(String arraysize) {
        this.arraysize = arraysize;
    }
    public String getArraytype() {
        return arraytype;
    }

    public void setArraytype(String arraytype) {
        this.arraytype = arraytype;
    }

    public Double getJNlap() {
        return Jnlap;
    }

    public void setJNlap(Double nlap) {
        this.Jnlap = nlap;
    }

    public JSONArray getJGlobal_coordination() {
        return Jglobal_coordination;
    }

    public void setJGlobal_coordination(JSONArray global_coordination) {
        this.Jglobal_coordination = global_coordination;
    }
}
