package com.example.sabeeh.helloworld;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.example.sabeeh.helloworld.backend.FileAnalysis;
import com.example.sabeeh.helloworld.backend.JasonHandler;
import com.example.sabeeh.helloworld.entites.swim;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class AnalysisActivity extends ActionBarActivity {

    Intent feedback;

    GraphView graph;
    LinearLayout checkBoxHolder;
    GraphLine[] data;
    Spinner axisSelector;
    RelativeLayout graphHolder;
    boolean graphVisible = false;

    TableLayout table;
    ScrollView graphDataTableScroll;
    ScrollView graphViewScroll;
    JasonHandler JasonForSwimAnalysed;
    Spinner dataTableSpinner;



    private MobileServiceClient mClient;
    private boolean azure_check=false;

    public swim objectForAnalysis=new GlobalSwimRecords().objectForAnalysis;

    Intent settings;
    int raceCounter = 0;

    ImageButton nextButton ;
    ImageButton prevButton;

    // Graph curves Data
    DataPoint[] mean_roll;
    DataPoint[] mean_roll_dx;
    DataPoint[] mean_roll_sx;
    DataPoint[] global_coordination;
    DataPoint[] strokes_each_pool;
    DataPoint[] rollpeaks;
    DataPoint[] coordina;
    Double[] Split_times;
    Double[] Mean_Velocity;
    Double[] Stroke_Frequency;
    Double[] Stroke_Length;
    Double Timing_turn;
    Double[] Clean_Stroke_Time;

//Multiple Races Data

    Double Maxroll=0.0;
    Double Minroll=0.0;
    Double MaxStroke=0.0;


    //Table data
    List<Double[]> swim_splits=new ArrayList<Double[]>();
    List<Double[]> swim_mean_velocity=new ArrayList<Double[]>();
    List<Double[]> swim_stroke_frequency=new ArrayList<Double[]>();
    List<Double[]> swim_stroke_length=new ArrayList<Double[]>();
    List<Double> swim_timing_turns=new ArrayList<Double>();
    List<Double[]> swim_clean_stroke=new ArrayList<Double[]>();

    //Graph data
    List<DataPoint[]> multiple_mean_roll=new ArrayList<DataPoint[]>();
    List<DataPoint[]> multiple_mean_roll_dx=new ArrayList<DataPoint[]>();
    List<DataPoint[]> multiple_mean_roll_sx=new ArrayList<DataPoint[]>();
    List<DataPoint[]> multiple_global_coordination=new ArrayList<DataPoint[]>();
    List<DataPoint[]> multiple_strokes_each_lap=new ArrayList<DataPoint[]>();

    ArrayList<String> Spinner_Options=new ArrayList<String>();




    BroadcastReceiver recv;
    protected void onResume() {
        super.onResume();
        // visual_initializations();


        registerReceiver(recv,
                new IntentFilter("com.example.sabeeh.helloworld.DOWNLOAD_DATA"));





    }
    protected  void onDestroy()
    {
        super.onDestroy();
    }



    protected  void onPause()
    {
        super.onPause();
        unregisterReceiver(recv);

    }
    @TargetApi(19)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Spinner_Options.add("Mean roll");
        Spinner_Options.add("Mean roll dx");
        Spinner_Options.add("Mean roll sx");
        Spinner_Options.add("Global_coordination");
        Spinner_Options.add("Strokes_each_lap");




        ////// Ahsan Code//////////////////////


        recv=new BroadcastReceiver() {
            IntentFilter intentFilter = new IntentFilter("com.example.sabeeh.helloworld.DOWNLOAD_DATA");
            @Override
            public void onReceive(Context context, Intent intent) {
                if(new GlobalSwimRecords().multiple_swim_flag==0) {



                    downloadData();
                }

                else
                {
                    if(new GlobalSwimRecords().analysis_flag_count==new GlobalSwimRecords().selected_races.size())
                    {
                        Toast.makeText(context, "All analysis have been done.", Toast.LENGTH_LONG).show();
                    }

                }
                // axis_selector_init();

                Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
            }

        };
        visual_initializations();

        if (new GlobalSwimRecords().multiple_swim_flag==0) {

            swim temp_obj=getSwim(objectForAnalysis.Local_file);
            if(!temp_obj.jason_analysis.isEmpty())
            {


                new GlobalSwimRecords().jasonForAnalysis.readAndParseJSON(temp_obj.jason_analysis);
                downloadData();
                //axis_selector_init();
            }
            else {

                if(objectForAnalysis.analysed==false) {

                    FileAnalysis BaseObject = new FileAnalysis(this);
                    BaseObject.UploadFileToVM(objectForAnalysis.Local_file);
                }
            }
        }
        else
        {

            GlobalSwimRecords temp_global_obj=new GlobalSwimRecords();
            temp_global_obj.ListjasonForAnalysis=new ArrayList<JasonHandler>();
            for(int i=0;i < temp_global_obj.selected_rows_for_analysis;i++)
            {

                JasonHandler object=new JasonHandler();
                object.readAndParseJSON(temp_global_obj.selected_races.get(i).jason_analysis);
                temp_global_obj.ListjasonForAnalysis.add(object);



            }
            downloadDataForMultipleSwim();
            populateTableLayourForMultipleSwim(swim_splits);
            //axis_selector_init();



        }
        nextButton = (ImageButton) findViewById(R.id.nextGraphButton);
        prevButton = (ImageButton) findViewById(R.id.prevGraphButton);
        if(new GlobalSwimRecords().selected_rows_for_analysis > 3)
        {
            nextButton.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.VISIBLE);
        }

        else
        {
            nextButton.setVisibility(View.GONE);
            prevButton.setVisibility(View.GONE);
        }

    }



    public void axis_selector_init()
    {
        if(new GlobalSwimRecords().multiple_swim_flag==0) {
            axisSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    initializeData(position);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
        }
        else
        {
            axisSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // initializeData_Multiple(position);
                    initializeData_Multiple(position);
                    // Toast.makeText(getApplicationContext(),"Spinner working",Toast.LENGTH_LONG).show();


                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

        }
    }


    public void visual_initializations()
    {


        setContentView(R.layout.activity_analysis);

        feedback = new Intent(this, FeedbackActivity.class);
        feedback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        table = (TableLayout)findViewById(R.id.graphDataTable);
        graphViewScroll = (ScrollView) findViewById(R.id.graphViewScroll);
        graphDataTableScroll = (ScrollView) findViewById(R.id.graphDataTableScroll);
        graphDataTableScroll.setVisibility(View.INVISIBLE);

        setHeaderButtonListeners();
        graphHolder = (RelativeLayout) findViewById(R.id.graphViewHolder);


       /* axisSelector = (Spinner) findViewById(R.id.axisSelector);
        if(new GlobalSwimRecords().multiple_swim_flag!=0) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Spinner_Options);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            axisSelector.setAdapter(spinnerAdapter);

           spinnerAdapter.notifyDataSetChanged();

        }*/



        graph = (GraphView) findViewById(R.id.graph);
        checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);

        settings = new Intent(this, SettingsActivity2.class);
        settings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        if(!graphVisible)
        {
            graphVisible = false;

            graphViewScroll.setVisibility(View.INVISIBLE);
            graphDataTableScroll.setVisibility(View.VISIBLE);
        }

        else if(graphVisible)
        {
            graphVisible = true;

            graphViewScroll.setVisibility(View.VISIBLE);
            graphDataTableScroll.setVisibility(View.INVISIBLE);
        }


        //    updateAxisLabels("x","y");
        //     PlotLine("a",new DataPoint[]{new DataPoint(2,2)},Color.RED);
        if(new GlobalSwimRecords().multiple_swim_flag==0) {
            ApplicationData.raceCount = 3;
            data = new GraphLine[ApplicationData.raceCount];
        }
        else
        {
            ApplicationData.raceCount = new GlobalSwimRecords().selected_rows_for_analysis  ;
            data = new GraphLine[ApplicationData.raceCount];
        }
    }
    public void showTableOptions(View v)
    {
        v.setBackgroundResource(R.drawable.table_button_selected);
        ImageButton graphButton = (ImageButton) findViewById(R.id.showGraphButton);
        graphButton.setBackgroundResource(R.drawable.graph_button_normal);

        if(!graphVisible)
            showTableValueOptions(v);

        graphVisible = false;
        if(!graphVisible)
        {
            graphVisible = false;

            graphViewScroll.setVisibility(View.INVISIBLE);
            graphDataTableScroll.setVisibility(View.VISIBLE);
        }

        else if(graphVisible)
        {
            graphVisible = true;

            graphViewScroll.setVisibility(View.VISIBLE);
            graphDataTableScroll.setVisibility(View.INVISIBLE);
        }
    }

    public void showGraphOptions(View view)
    {
        view.setBackgroundResource(R.drawable.graph_button_selected);
        ImageButton tableButton = (ImageButton) findViewById(R.id.showTableButton);
        tableButton.setBackgroundResource(R.drawable.table_button_normal);

        if(graphVisible)
            showGraphValueOptions(view);

        graphVisible = true;
        if(!graphVisible)
        {
            graphVisible = false;

            graphViewScroll.setVisibility(View.INVISIBLE);
            graphDataTableScroll.setVisibility(View.VISIBLE);
        }

        else if(graphVisible)
        {
            graphVisible = true;

            graphViewScroll.setVisibility(View.VISIBLE);
            graphDataTableScroll.setVisibility(View.INVISIBLE);
        }
    }

    public void initializeData( int position)
    {

        String  xAxis ="Laps";
        String yAxis = "Degrees";





        switch(position)
        {
            case 0:


                // graph.getSeries().clear();
                graph.getSecondScale().setMinY(0);
                graph.getSecondScale().setMaxY(0);
                ApplicationData.raceCount = 3;
                data = new GraphLine[ApplicationData.raceCount];
                raceCounter=0;
                graph.removeAllSeries();

                graph.refreshDrawableState();

                if(graph.getSecondScale().getSeries().size() > 0)
                    graph.getSecondScale().getSeries().remove(0);

                checkBoxHolder.removeAllViews();
                checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);

                xAxis ="Laps";
                yAxis = "Degrees";
                PlotLine("mean roll", mean_roll, Color.parseColor("#80ff0000")); //Color.RED
                PlotLine("mean roll dx",mean_roll_dx,Color.parseColor("#80ffffff")); //Color.WHITE
                PlotLine("mean roll sx",mean_roll_sx,Color.parseColor("#8000ff00"));  //Color.GREEN



                updateAxisLabels(xAxis,yAxis);



                break;

            case 1:
                graph.getSecondScale().setMinY(0);
                graph.getSecondScale().setMaxY(0);
                ApplicationData.raceCount = 1;
                raceCounter=0;
                data = new GraphLine[ApplicationData.raceCount];
                xAxis ="Laps";
                yAxis="Global Coordination";
                graph.removeAllSeries();
                if(graph.getSecondScale().getSeries().size() > 0)
                    graph.getSecondScale().getSeries().remove(0);
                checkBoxHolder.removeAllViews();
                checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);
                PlotLine("Global Coordination",global_coordination,Color.parseColor("#800000ff")); //Color.BLUE


                LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(strokes_each_pool);
                graph.getSecondScale().addSeries(series2);
                // the y bounds are always manual for second scale
                graph.getSecondScale().setMinY(0);
                graph.getSecondScale().setMaxY(MaxStroke+MaxStroke/2);
                series2.setColor(Color.RED);
                series2.setTitle("Strokes each pool");
                //graph.getGridLabelRenderer()
                graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.RED);








                break;

            case 2:
                graph.getSecondScale().setMinY(0);
                graph.getSecondScale().setMaxY(0);
                xAxis ="Strokes";
                ApplicationData.raceCount = 1;
                raceCounter=0;
                data = new GraphLine[ApplicationData.raceCount];

                yAxis="Coordina";
                graph.removeAllSeries();
                checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);
                if(graph.getSecondScale().getSeries().size() > 0)
                    graph.getSecondScale().getSeries().remove(0);
                checkBoxHolder.removeAllViews();
                PlotLine("Coordina",coordina,Color.parseColor("#800000ff"));


                LineGraphSeries<DataPoint> series3 = new LineGraphSeries<DataPoint>(rollpeaks);
                graph.getSecondScale().addSeries(series3);
                // the y bounds are always manual for second scale
                graph.getSecondScale().setMinY(Minroll+Minroll);
                graph.getSecondScale().setMaxY(Maxroll+Maxroll);
                series3.setColor(Color.RED);
                series3.setTitle("Roll peaks");
                //graph.getGridLabelRenderer()
                graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.RED);
                break;
        }

        updateAxisLabels(xAxis,yAxis);


    }

    public void initializeData_Multiple( int position)
    {

        String  xAxis ="Laps";
        String yAxis = "Degrees";





        switch(position)
        {
            case 0:


                // graph.getSeries().clear();

                ApplicationData.raceCount = new GlobalSwimRecords().selected_rows_for_analysis;
                data = new GraphLine[ApplicationData.raceCount];
                raceCounter=0;
                graph.removeAllSeries();

                graph.refreshDrawableState();


                checkBoxHolder.removeAllViews();
                checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);
                xAxis="Laps";
                yAxis="Degrees";
                // updateAxisLabels("Laps","Degrees");
                for(int i=0;i<ApplicationData.raceCount;i++) {
                    Random rnd = new Random();
                    int color = Color.argb(80, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    PlotLine_Multiple("Swim "+i, multiple_mean_roll.get(i), color);

                }







                break;

            case 1:

                ApplicationData.raceCount = new GlobalSwimRecords().selected_rows_for_analysis;
                data = new GraphLine[ApplicationData.raceCount];
                raceCounter=0;
                graph.removeAllSeries();

                graph.refreshDrawableState();


                checkBoxHolder.removeAllViews();
                checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);

                xAxis="Laps";
                yAxis="Degrees";
                for(int i=0;i<ApplicationData.raceCount;i++) {
                    Random rnd = new Random();
                    int color = Color.argb(80, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    PlotLine_Multiple("Swim "+i, multiple_mean_roll_dx.get(i), color);

                }




                break;

            case 2:
                ApplicationData.raceCount = new GlobalSwimRecords().selected_rows_for_analysis;
                data = new GraphLine[ApplicationData.raceCount];
                raceCounter=0;
                graph.removeAllSeries();

                graph.refreshDrawableState();


                checkBoxHolder.removeAllViews();
                checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);

                xAxis="Laps";
                yAxis="Degrees";
                //updateAxisLabels("Laps","Degrees");
                for(int i=0;i<ApplicationData.raceCount;i++) {
                    Random rnd = new Random();
                    int color = Color.argb(80, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    PlotLine_Multiple("Swim "+i, multiple_mean_roll_sx.get(i), color);

                }


                break;
            case 3:
                ApplicationData.raceCount = new GlobalSwimRecords().selected_rows_for_analysis;
                data = new GraphLine[ApplicationData.raceCount];
                raceCounter=0;
                graph.removeAllSeries();

                graph.refreshDrawableState();


                checkBoxHolder.removeAllViews();
                checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);

                xAxis="Laps";
                yAxis="Global Coordination";
                //updateAxisLabels("Laps","Degrees");
                for(int i=0;i<ApplicationData.raceCount;i++) {
                    Random rnd = new Random();
                    int color = Color.argb(80, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    PlotLine_Multiple("Swim "+i, multiple_global_coordination.get(i), color);

                }
                break;
            case 4:
                ApplicationData.raceCount = new GlobalSwimRecords().selected_rows_for_analysis;
                data = new GraphLine[ApplicationData.raceCount];
                raceCounter=0;
                graph.removeAllSeries();

                graph.refreshDrawableState();


                checkBoxHolder.removeAllViews();
                checkBoxHolder = (LinearLayout) findViewById(R.id.checkBoxHolder);

                xAxis="Laps";
                yAxis="Strokes";
                //updateAxisLabels("Laps","Degrees");
                for(int i=0;i<ApplicationData.raceCount;i++) {
                    Random rnd = new Random();
                    int color = Color.argb(80, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    PlotLine_Multiple("Swim "+i, multiple_strokes_each_lap.get(i), color);

                }



                break;
        }

        updateAxisLabels(xAxis,yAxis);


    }

    public Double[] returnfloatarray(JSONArray temp)
    {

        if(temp.length()!=0) {
            Double result[] = new Double[temp.length()];
            for (int i = 0; i < temp.length(); i++) {
                JSONArray arr=temp.optJSONArray(i);


                result[i]= Math.floor(arr.optDouble(0) * 100) / 100;

                // result[i] = (float)temp.opt(i);

            }
            return result;
        }
        else
        {
            return  new Double[0];
        }
    }


    public void downloadDataForMultipleSwim()
    {
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
        List<JasonHandler> temp_list=new GlobalSwimRecords().ListjasonForAnalysis;

        for(int i=0;i<temp_list.size();i++) {
            swim_splits.add(returnfloatarray(temp_list.get(i).getJSplit()));
            swim_clean_stroke.add(returnfloatarray(temp_list.get(i).getJClean_stroke_time()));
            swim_stroke_frequency.add(returnfloatarray(temp_list.get(i).getJStroke_freq()));
            swim_stroke_length.add(returnfloatarray(temp_list.get(i).getJStroke_length()));
            swim_timing_turns.add(Math.floor(temp_list.get(i).getJTiming_turn() * 100) / 100);
            swim_mean_velocity.add(returnfloatarray(temp_list.get(i).getJMean_velocity()));

            Double[] mean_roll=new Double[temp_list.get(i).getJMean_roll().length()];
            Double[] mean_roll_dx=new Double[temp_list.get(i).getJMean_roll_dx().length()];
            Double[] mean_roll_sx=new Double[temp_list.get(i).getJMean_roll_sx().length()];
            Double[] global_coordination=new Double[temp_list.get(i).getJGlobal_coordination().length()];
            Double[] strokes_each_lap=new Double[temp_list.get(i).getJStroke_each_pool().length()];

            mean_roll=returnfloatarray(temp_list.get(i).getJMean_roll());
            mean_roll_dx=returnfloatarray(temp_list.get(i).getJMean_roll_dx());
            mean_roll_sx=returnfloatarray(temp_list.get(i).getJMean_roll_sx());
            global_coordination=returnfloatarray(temp_list.get(i).getJGlobal_coordination());
            strokes_each_lap=returnfloatarray(temp_list.get(i).getJStroke_each_pool());



            multiple_mean_roll.add(returnDataPointArray(mean_roll));
            multiple_mean_roll_dx.add(returnDataPointArray(mean_roll_dx));
            multiple_mean_roll_sx.add(returnDataPointArray(mean_roll_sx));
            multiple_global_coordination.add(returnDataPointArray(global_coordination));
            multiple_strokes_each_lap.add(returnDataPointArray(strokes_each_lap));


        }


        updateAxisLabels("Laps","Degrees");
        for(int i=0;i<temp_list.size();i++) {
            Random rnd = new Random();
            int color = Color.argb(80, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            PlotLine_Multiple("Swim "+i, multiple_mean_roll.get(i), color);

        }



    }
    public DataPoint[] returnDataPointArray(Double[] temp_list)
    {
        DataPoint[] temp=new DataPoint[temp_list.length];
        for(int j=0;j<temp_list.length;j++)
        {

            temp[j]=new DataPoint(j+1,temp_list[j]);
        }
        return  temp;
    }
    public void populateTableLayourForMultipleSwimArray(List<Double> value_arr)
    {
        table.removeAllViews();

        TableRow headerRow = new TableRow(getApplicationContext());
        headerRow.setBackgroundColor(Color.parseColor("#0073a8"));
        TextView Laps = new TextView(getApplicationContext());
        Laps.setText("Laps");
        Laps.setTextColor(Color.WHITE);
        Laps.setTextSize(12);
        Laps.setPadding(14,0,0,0);

        headerRow.addView(Laps);
        for(int i=0;i<new GlobalSwimRecords().selected_rows_for_analysis;i++)
        {


            TextView Header_label = new TextView(getApplicationContext());
            Header_label.setText("Swim " + i);
            Header_label.setTextColor(Color.WHITE);
            Header_label.setTextSize(12);
            Header_label.setPadding(14,0,0,0);

            headerRow.addView(Header_label);
        }
        table.addView(headerRow);


        Double Max_laps=0.0;
        List<JasonHandler> temp_list=new GlobalSwimRecords().ListjasonForAnalysis;
        Max_laps=temp_list.get(0).getJNlap();






        for(int i=0;i<temp_list.size();i++)
        {
            Double temp=temp_list.get(i).getJNlap();
            if(Max_laps<temp)
            {
                Max_laps=temp;
            }
        }







        //for(int i = 0;i <value_arr.size(); i++) {

        TableRow coordRow = new TableRow(getApplicationContext());
        coordRow.setId(0);

        coordRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setBackgroundColor(Color.GREEN);
                TableRow row = (TableRow) v;
                TextView first = (TextView) row.getChildAt(0);
                TextView second = (TextView) row.getChildAt(1);
                TextView third = (TextView) row.getChildAt(2);
                TextView fourth = (TextView) row.getChildAt(3);

                Toast.makeText(getApplicationContext(), "ID "+first.getText()+" Strokes "+second.getText()+" Coord "+third.getText()+" S.Freq "+fourth.getText(), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        coordRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(v.getId()%2 == 0)
                v.setBackgroundColor(Color.WHITE);

            }
        });

//            if(0%2==0)
//                coordRow.setBackgroundColor(Color.parseColor("#dadada"));
        //else
        coordRow.setBackgroundColor(Color.WHITE);







        TextView lap = new TextView(getApplicationContext());
        lap.setText("-");
        lap.setTextColor(Color.WHITE);
        lap.setTextSize(12);
        lap.setPadding(14,0,0,0);
        coordRow.addView(lap);
        for(int j=0;j<value_arr.size();j++) {
            TextView arr = new TextView(getApplicationContext());
            arr.setText(""+value_arr.get(j));
            arr.setTextColor(Color.WHITE);
            arr.setTextSize(12);
            arr.setPadding(14, 0, 0, 0);
            coordRow.addView(arr);
        }






        table.addView(coordRow);
        //}


    }
    public void populateTableLayourForMultipleSwim(List<Double[]> value_arr)
    {
        table.removeAllViews();


        TableRow headerRow = new TableRow(getApplicationContext());
        headerRow.setBackgroundColor(Color.parseColor("#0073a8"));
        headerRow.setBackground(getResources().getDrawable(R.drawable.table_header_row_background));
        TextView Laps = new TextView(getApplicationContext());
        Laps.setText("Laps");
        Laps.setTextColor(Color.BLACK);
        Laps.setTextSize(12);
        Laps.setPadding(16,0,0,0);

        headerRow.addView(Laps);
        for(int i=0;i<new GlobalSwimRecords().selected_rows_for_analysis;i++)
        {


            TextView Header_label = new TextView(getApplicationContext());
            Header_label.setText("Swim " + i);
            Header_label.setTextColor(Color.BLACK);
            Header_label.setTextSize(12);
            Header_label.setPadding(14,0,0,0);

            headerRow.addView(Header_label);
        }
        table.addView(headerRow);


        Double Max_laps=0.0;
        List<JasonHandler> temp_list=new GlobalSwimRecords().ListjasonForAnalysis;
        Max_laps=temp_list.get(0).getJNlap();






        for(int i=0;i<temp_list.size();i++)
        {
            Double temp=temp_list.get(i).getJNlap();
            if(Max_laps<temp)
            {
                Max_laps=temp;
            }
        }







        for(int i = 0;i <Max_laps; i++) {

            TableRow coordRow = new TableRow(getApplicationContext());
            coordRow.setId(i);

            coordRow.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.setBackgroundColor(Color.GREEN);
                    TableRow row = (TableRow) v;
                    TextView first = (TextView) row.getChildAt(0);
                    TextView second = (TextView) row.getChildAt(1);
                    TextView third = (TextView) row.getChildAt(2);
                    TextView fourth = (TextView) row.getChildAt(3);

                    Toast.makeText(getApplicationContext(), "ID "+first.getText()+" Strokes "+second.getText()+" Coord "+third.getText()+" S.Freq "+fourth.getText(), Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            coordRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if(v.getId()%2 == 0)
//                        v.setBackgroundColor(Color.parseColor("#dadada"));
//                    else
                    v.setBackgroundColor(Color.WHITE);
                }
            });

            //if(i%2==0)
            //  coordRow.setBackgroundColor(Color.parseColor("#dadada"));
            //else
            coordRow.setBackgroundColor(Color.WHITE);



            TextView lap = new TextView(getApplicationContext());
            lap.setText(""+i);
            lap.setTextColor(Color.BLACK);
            lap.setTextSize(12);
            lap.setPadding(16,0,0,0);
            coordRow.addView(lap);
            for(int j=0;j<value_arr.size();j++) {

                TextView arr = new TextView(getApplicationContext());
                arr.setText(""+value_arr.get(j)[i]);
                arr.setTextColor(Color.parseColor("#979797"));
                arr.setTextSize(12);
                arr.setPadding(28, 0, 0, 0);
                coordRow.addView(arr);
            }

            table.addView(coordRow);
        }
    }

    public void populateTableLayout(DataPoint[] points, String headingX, String headingY)
    {
        table.removeAllViews();

        TextView laps = new TextView(getApplicationContext());
        laps.setText("Laps");
        laps.setTextColor(Color.BLACK);
        laps.setTextSize(12);
        laps.setPadding(16,0,0,0);

        TextView idHeading = new TextView(getApplicationContext());
        idHeading.setText("Split"); //Split Times
        idHeading.setTextColor(Color.BLACK);
        idHeading.setTextSize(12);
        idHeading.setPadding(28,0,0,0); //14,0,0,0

        TextView xHeading = new TextView(getApplicationContext());
        xHeading.setText("Velocity"); //Mean Velocity
        xHeading.setTextColor(Color.BLACK);
        xHeading.setTextSize(12);
        xHeading.setPadding(28,0,0,0);

        TextView yHeading = new TextView(getApplicationContext());
        yHeading.setText("Str. Freq."); //Stroke Frequency
        yHeading.setTextColor(Color.BLACK);
        yHeading.setGravity(Gravity.RIGHT);
        yHeading.setTextSize(12);
        yHeading.setPadding(28,0,0,0);

        TextView heading3 = new TextView(getApplicationContext());
        heading3.setText("Str. Length");
        heading3.setTextColor(Color.BLACK);
        heading3.setGravity(Gravity.RIGHT);
        heading3.setTextSize(12);
        heading3.setPadding(28,0,0,0);

        TextView heading4 = new TextView(getApplicationContext());
        heading4.setText("Turn");
        heading4.setTextColor(Color.BLACK);
        heading4.setGravity(Gravity.RIGHT);
        heading4.setTextSize(12);
        heading4.setPadding(28,0,0,0);

        TextView heading5 = new TextView(getApplicationContext());
        heading5.setText("Clean Str.");
        heading5.setTextColor(Color.BLACK);
        heading5.setGravity(Gravity.RIGHT);
        heading5.setTextSize(12);
        heading5.setPadding(28,0,0,0);


        TableRow headerRow = new TableRow(getApplicationContext());
        headerRow.setBackgroundColor(Color.parseColor("#0073a8"));
        headerRow.setBackground(getResources().getDrawable(R.drawable.table_header_row_background));

        headerRow.addView(laps);
        headerRow.addView(idHeading);
        headerRow.addView(xHeading);
        headerRow.addView(yHeading);
        headerRow.addView(heading3);
        headerRow.addView(heading4);
        headerRow.addView(heading5);


        table.addView(headerRow);


        GlobalSwimRecords obj=new GlobalSwimRecords();
        Double nlaps=obj.jasonForAnalysis.getJNlap();



        for(int i = 0;i <nlaps; i++) {

            TableRow coordRow = new TableRow(getApplicationContext());
            coordRow.setId(i);

            coordRow.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.setBackgroundColor(Color.GREEN);
                    TableRow row = (TableRow) v;
                    TextView first = (TextView) row.getChildAt(0);
                    TextView second = (TextView) row.getChildAt(1);
                    TextView third = (TextView) row.getChildAt(2);
                    TextView fourth = (TextView) row.getChildAt(3);

                    Toast.makeText(getApplicationContext(), "ID "+first.getText()+" Strokes "+second.getText()+" Coord "+third.getText()+" S.Freq "+fourth.getText(), Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            coordRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if(v.getId()%2 == 0)
//                        v.setBackgroundColor(Color.parseColor("#dadada"));
//                    else
                    v.setBackgroundColor(Color.WHITE);
                }
            });

            //if(i%2==0)
            coordRow.setBackgroundColor(Color.WHITE);
            //else
            //  coordRow.setBackgroundColor(Color.parseColor("#0096db"));








            TextView lap = new TextView(getApplicationContext());
            lap.setText((i+1)+"");
            lap.setTextColor(Color.BLACK);
            lap.setTextSize(12);
            lap.setPadding(28,0,0,0);


            TextView id = new TextView(getApplicationContext());
            id.setText( Split_times[i]+"");
            id.setTextColor(Color.parseColor("#979797"));
            id.setTextSize(12);
            id.setPadding(28,0,0,0);


            TextView xCoord = new TextView(getApplicationContext());
            xCoord.setText( Mean_Velocity[i]+" m/s");
            xCoord.setTextColor(Color.parseColor("#979797"));
            xCoord.setTextSize(12);
            xCoord.setPadding(28,0,0,0);

            TextView yCoord = new TextView(getApplicationContext());
            yCoord.setText(Stroke_Frequency[i]+" Hz");
            yCoord.setGravity(Gravity.CENTER);
            yCoord.setTextColor(Color.parseColor("#979797"));
            yCoord.setTextSize(12);
            yCoord.setPadding(28,0,0,0);

            TextView value3 = new TextView(getApplicationContext());
            value3.setText(Stroke_Length[i]+" m");
            value3.setGravity(Gravity.CENTER);
            value3.setTextColor(Color.parseColor("#979797"));
            value3.setTextSize(12);
            value3.setPadding(28,0,0,0);

            TextView value4 = new TextView(getApplicationContext());
            value4.setText(Timing_turn+" s");
            value4.setGravity(Gravity.CENTER);
            value4.setTextColor(Color.parseColor("#979797"));
            value4.setTextSize(12);
            value4.setPadding(28,0,0,0);

            TextView value5 = new TextView(getApplicationContext());
            value5.setText(Clean_Stroke_Time[i]+" s");
            value5.setGravity(Gravity.CENTER);
            value5.setTextColor(Color.parseColor("#979797"));
            value5.setTextSize(12);
            value5.setPadding(28,0,0,0);

           /* TextView value6 = new TextView(getApplicationContext());
            value6.setText("0");
            value6.setGravity(Gravity.RIGHT);
            value6.setTextColor(Color.WHITE);
            value6.setTextSize(20);
            value6.setPadding(0,0,50,0);
*/


//            View v2 = new View(this);
//            v2.setLayoutParams(new TableRow.LayoutParams(1,TableRow.LayoutParams.MATCH_PARENT));
//            v2.setBackgroundColor(Color.parseColor("#777777"));

            coordRow.addView(lap);
            coordRow.addView(id);
            coordRow.addView(xCoord);
//            coordRow.addView(v2);
            coordRow.addView(yCoord);
            coordRow.addView(value3);
            coordRow.addView(value4);
            coordRow.addView(value5);
            // coordRow.addView(value6);

            table.addView(coordRow);
        }

    }

    public void updateAxisLabels(String xAxis, String yAxis) {
        graph.getGridLabelRenderer().setHorizontalAxisTitle(xAxis);
        graph.getGridLabelRenderer().setVerticalAxisTitle(yAxis);
        graph.getGridLabelRenderer().setGridColor(Color.parseColor("#0073a8"));
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.parseColor("#0073a8"));
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.parseColor("#0073a8"));
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.parseColor("#0073a8"));
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.parseColor("#0073a8"));

    }


    public void downloadData()
    {

        //  graph.removeAllSeries();


        GlobalSwimRecords obj=new GlobalSwimRecords();


        if((obj.jasonForAnalysis.getJMean_roll()==null)||

                (obj.jasonForAnalysis.getJMean_roll_dx()==null)||
                (obj.jasonForAnalysis.getJMean_roll_sx()==null)||
                (obj.jasonForAnalysis.getJStroke_each_pool()==null)||
                (obj.jasonForAnalysis.getJGlobal_coordination()==null)||
                (obj.jasonForAnalysis.getJCoordina()==null)||
                (obj.jasonForAnalysis.getJRoll_peaks()==null)||
                (obj.jasonForAnalysis.getJStroke_each_pool()==null))
        {
            createAndShowDialog("Sorry your swim is not good enough.","Ooops");
            return;

        }

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);



        final Double Meanrolls[]=returnfloatarray(obj.jasonForAnalysis.getJMean_roll());
        final Double Meanrolls_dx[]=returnfloatarray(obj.jasonForAnalysis.getJMean_roll_dx());
        final Double Meanrolls_sx[]=returnfloatarray(obj.jasonForAnalysis.getJMean_roll_sx());
        final Double nlap=obj.jasonForAnalysis.getJNlap();

        //mean roll array plot
        mean_roll=new DataPoint[Meanrolls.length];
        mean_roll_dx=new DataPoint[Meanrolls_dx.length];
        mean_roll_sx=new DataPoint[Meanrolls_sx.length];

        for(int i=0;i<nlap;i++)
        {
            mean_roll[i]=new DataPoint(i+1,Meanrolls[i]);
            mean_roll_dx[i]=new DataPoint(i+1,Meanrolls_dx[i]);
            mean_roll_sx[i]=new DataPoint(i+1,Meanrolls_sx[i]);

        }



        //Second graph data population

        Double Strokes_each_pool[]=returnfloatarray(obj.jasonForAnalysis.getJStroke_each_pool());
        Double Global_coordination[]=returnfloatarray(obj.jasonForAnalysis.getJGlobal_coordination());

        MaxStroke=getmax(Strokes_each_pool);


        strokes_each_pool=new DataPoint[Strokes_each_pool.length];
        global_coordination=new DataPoint[Global_coordination.length];
        for(int i=0;i<nlap;i++)
        {
            global_coordination[i]=new DataPoint(i+1,Global_coordination[i]);
            strokes_each_pool[i]=new DataPoint(i+1,Strokes_each_pool[i]);
            //  mean_roll_dx[i]=new DataPoint(i+1,Meanrolls_dx[i]);


        }

        //Third graph data population

        Double Coordina[]=returnfloatarray(obj.jasonForAnalysis.getJCoordina());
        Double Roll_Peaks[]=returnfloatarray(obj.jasonForAnalysis.getJRoll_peaks());

        Maxroll=getmax(Roll_Peaks);
        Minroll=getmin(Roll_Peaks);

        Double Tot_stroke[]=returnfloatarray(obj.jasonForAnalysis.getJStroke_each_pool());





        //Double Max_stroke= Array.g


        coordina=new DataPoint[Coordina.length];
        rollpeaks=new DataPoint[Roll_Peaks.length];
        Double total_stroke=0.0;
        for (int i=0;i<Tot_stroke.length;i++)
        {
            total_stroke+=Tot_stroke[i];
        }
        for (int i=0;i<total_stroke;i++)
        {
            coordina[i]=new DataPoint(i,Coordina[i]);
            rollpeaks[i]=new DataPoint(i,Roll_Peaks[i]);
        }


//Populate Table data for one swim selected;

        Split_times=new Double[obj.jasonForAnalysis.getJSplit().length()];
        Mean_Velocity=new Double[obj.jasonForAnalysis.getJMean_velocity().length()];
        Stroke_Frequency=new Double[obj.jasonForAnalysis.getJStroke_freq().length()];
        Stroke_Length=new Double[obj.jasonForAnalysis.getJStroke_length().length()];
        Timing_turn=Math.floor(obj.jasonForAnalysis.getJTiming_turn() * 100) / 100;
        Clean_Stroke_Time=new Double[obj.jasonForAnalysis.getJClean_stroke_time().length()];


        Split_times=returnfloatarray(obj.jasonForAnalysis.getJSplit());
        Mean_Velocity=returnfloatarray(obj.jasonForAnalysis.getJMean_velocity());
        Stroke_Frequency=returnfloatarray(obj.jasonForAnalysis.getJStroke_freq());
        Stroke_Length=returnfloatarray(obj.jasonForAnalysis.getJStroke_length());
        Clean_Stroke_Time=returnfloatarray(obj.jasonForAnalysis.getJClean_stroke_time());






        //PlotLine("LOL GREEN", new DataPoint[]{new DataPoint(-2,2), new DataPoint(2,3)}, Color.GREEN);
        // PlotLine("a",new DataPoint[]{new DataPoint(2,2)},Color.RED);
        PlotLine("mean roll", mean_roll, Color.RED);
        PlotLine("mean roll dx",mean_roll_dx,Color.parseColor("#80ffffff")); //Color.WHITE
        PlotLine("mean roll sx",mean_roll_sx,Color.parseColor("#8000ff00")); //Color.GREEN

        //  PlotLine("LOL", new DataPoint[]{new DataPoint(-1,2), new DataPoint(2,2)}, Color.BLACK);

    }

    public DataPoint[] sortPoints(DataPoint[] points)
    {
        DataPoint temp;

        for(int i = 0; i < points.length; i++)
        {
            for(int j = 1; j < (points.length -i); j++)
            {
                //if numbers[j-1] > numbers[j], swap the elements
                if(points[j-1].getX() > points[j].getX())
                {
                    temp = points[j-1];
                    points[j-1]=points[j];
                    points[j]=temp;
                }
            }
        }

        return points;
    }

    public void PlotLine_Multiple(String title, DataPoint[] points, int color)
    {
        data[raceCounter] = new GraphLine(AnalysisActivity.this);
        data[raceCounter].setColor(color);
        data[raceCounter].setTitle(title);
        data[raceCounter].setCheckBox(title);
        data[raceCounter].getCheckBox().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(int i = 0; i < ApplicationData.raceCount; i++)
                {
                    if(!((CheckBox)v).isChecked() && data[i].getCheckBox().getText().equals(((CheckBox)v).getText()))
                    {
                        data[i].getPoints().resetData(new DataPoint[] {new DataPoint(data[i].getPoints().getLowestValueX(),data[i].getPoints().getLowestValueY())});
                    }

                    else if(((CheckBox)v).isChecked() && data[i].getCheckBox().getText().equals(((CheckBox)v).getText()))
                    {
                        data[i].getPoints().resetData(sortPoints(data[i].getBackupPoints()));
                    }
                }
            }
        });

        checkBoxHolder.addView(data[raceCounter].getCheckBox());

        points = sortPoints(points);
        LineGraphSeries<DataPoint> line = new LineGraphSeries<>(points);
        // line.resetData(points);
        line.setColor(color);
        line.setTitle(title);
        line.setDrawDataPoints(true);
        line.setDataPointsRadius(2);

        line.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(AnalysisActivity.this, " Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        //STYLING
//        line.setDrawBackground(true);
//        line.setBackgroundColor(color);
//        line.setDrawDataPoints(true);
//        line.setDataPointsRadius(10);
//        line.setThickness(8);
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(10);
//        paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
//        line.setCustomPaint(paint);

        line.setBackgroundColor(color);

        data[raceCounter].setPoints(line);
        data[raceCounter].setBackupPoints(points);
        graph.addSeries(line);

        raceCounter++;

    }
    public void PlotLine(String title, DataPoint[] points, int color)
    {
        data[raceCounter] = new GraphLine(AnalysisActivity.this);
        data[raceCounter].setColor(color);
        data[raceCounter].setTitle(title);
        data[raceCounter].setCheckBox(title);
        data[raceCounter].getCheckBox().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(int i = 0; i < ApplicationData.raceCount; i++)
                {
                    if(!((CheckBox)v).isChecked() && data[i].getCheckBox().getText().equals(((CheckBox)v).getText()))
                    {
                        data[i].getPoints().resetData(new DataPoint[] {new DataPoint(data[i].getPoints().getLowestValueX(),data[i].getPoints().getLowestValueY())});
                    }

                    else if(((CheckBox)v).isChecked() && data[i].getCheckBox().getText().equals(((CheckBox)v).getText()))
                    {
                        data[i].getPoints().resetData(sortPoints(data[i].getBackupPoints()));
                    }
                }
            }
        });

        checkBoxHolder.addView(data[raceCounter].getCheckBox());

        points = sortPoints(points);
        LineGraphSeries<DataPoint> line = new LineGraphSeries<>(points);
        // line.resetData(points);
        line.setColor(color);
        line.setTitle(title);
        line.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(AnalysisActivity.this, "Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        line.setDrawDataPoints(true);
        line.setDataPointsRadius(8);
        line.setBackgroundColor(color);

        //STYLING
//        line.setDrawBackground(true);
//        line.setBackgroundColor(color);
//        line.setDrawDataPoints(true);
//        line.setDataPointsRadius(10);
//        line.setThickness(8);
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(10);
//        paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
//        line.setCustomPaint(paint);

        data[raceCounter].setPoints(line);
        data[raceCounter].setBackupPoints(points);
        graph.addSeries(line);

        populateTableLayout(points, "Strokes", "Coordina");

        raceCounter++;
    }

    public void toggleViews(View view)
    {
        if(graphVisible)
        {
            view.setBackgroundResource(R.drawable.button_toggle_data);
            graphVisible = false;

            graphViewScroll.setVisibility(View.INVISIBLE);
            graphDataTableScroll.setVisibility(View.VISIBLE);
        }

        else if(!graphVisible)
        {
            view.setBackgroundResource(R.drawable.button_toggle_graph);
            graphVisible = true;

            graphViewScroll.setVisibility(View.VISIBLE);
            graphDataTableScroll.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        else if(id == R.id.action_analysis)
        {
            Toast.makeText(getApplicationContext(), "Analysis", Toast.LENGTH_SHORT).show();
        }

        else if(id == R.id.action_swim)
        {
            Toast.makeText(getApplicationContext(), "Swim", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void setHeaderButtonListeners()
    {
        final Intent swimActivity = new Intent(this,HomeActivity.class);
        swimActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ImageView analysisButton = (ImageView) findViewById(R.id.headerAnalysisButton);
        analysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    public void showGraphValueOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        if(new GlobalSwimRecords().multiple_swim_flag==0) {
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.action_roll_all:
                            Toast.makeText(getApplicationContext(), "Roll All Laps", Toast.LENGTH_SHORT).show();
                            initializeData(0);
                            return true;

                        case R.id.action_coordination_strokes:
                            Toast.makeText(getApplicationContext(), "Coordination + Strokes All Laps", Toast.LENGTH_SHORT).show();
                            initializeData(1);
                            return true;

                        case R.id.action_roll_coordination:
                            Toast.makeText(getApplicationContext(), "Roll + Coordination Index one Lap", Toast.LENGTH_SHORT).show();
                            initializeData(2);
                            return true;

                        default:
                            return false;
                    }
                }
            });
        }
        else
        {
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.action_roll_all:
                            Toast.makeText(getApplicationContext(), "Roll All Laps", Toast.LENGTH_SHORT).show();
                            initializeData_Multiple(0);
                            return true;

                        case R.id.action_coordination_strokes:
                            Toast.makeText(getApplicationContext(), "Coordination + Strokes All Laps", Toast.LENGTH_SHORT).show();
                            initializeData_Multiple(1);
                            return true;

                        case R.id.action_roll_coordination:
                            Toast.makeText(getApplicationContext(), "Roll + Coordination Index one Lap", Toast.LENGTH_SHORT).show();
                            initializeData_Multiple(2);
                            return true;

                        default:
                            return false;
                    }
                }
            });
        }
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_graph_options, popup.getMenu());

        popup.show();
    }

    public void showTableValueOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_split:
                        populateTableLayourForMultipleSwim(swim_splits);
                        Toast.makeText(getApplicationContext(), "Split Times", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.action_velocity:
                        populateTableLayourForMultipleSwim(swim_mean_velocity);
                        Toast.makeText(getApplicationContext(), "Velocity", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.action_stroke_length:
                        populateTableLayourForMultipleSwim(swim_stroke_length);
                        Toast.makeText(getApplicationContext(), "Stroke Length", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.action_stroke_frequency:
                        populateTableLayourForMultipleSwim(swim_stroke_frequency);
                        Toast.makeText(getApplicationContext(), "Stroke Frequency", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.action_clean_stroke:
                        populateTableLayourForMultipleSwim(swim_clean_stroke);
                        Toast.makeText(getApplicationContext(), "Stroke Clean Stroke", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.action_timing_turns:
                        populateTableLayourForMultipleSwimArray(swim_timing_turns);
                        Toast.makeText(getApplicationContext(), "Timing Turns", Toast.LENGTH_SHORT).show();
                        return true;

                    default:
                        return false;
                }
            }
        });

        if(new GlobalSwimRecords().multiple_swim_flag==1) {
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_table_options, popup.getMenu());
        }
        else
        {
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.single_race_menu_table_options, popup.getMenu());
        }

        popup.show();
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_logout:
                        Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.action_feedback:
                        startActivity(feedback);
                        return true;

                    case R.id.action_faq:
                        Toast.makeText(getApplicationContext(), "FAQ", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.action_settings:
                        startActivity(settings);
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


    Double getmin(Double arr[])
    {

        Double result=arr[0];
        for(int i = 0;i < arr.length;i++)
        {
            if(arr[i]<result)
            {
                result=arr[i];

            }
        }

        return result;

    }

    Double getmax(Double arr[])
    {

        Double result=arr[0];
        for(int i = 0;i < arr.length;i++)
        {
            if(arr[i]>result)
            {
                result=arr[i];

            }
        }

        return result;

    }
    private void createAndShowDialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.create().show();
    }

    public static swim getSwim(String local_file) {
        return new Select()
                .from(swim.class)
                .where("Local_file = ?",local_file)
                .executeSingle();
    }


    public void moveGraphs(View view)
    {
        ImageView button = (ImageView) view;

        if(button.getId() == R.id.nextGraphButton)
        {
            //Show Next Graph
            Toast.makeText(getApplicationContext(), "Next Graph", Toast.LENGTH_SHORT).show();
        }

        else if(button.getId() == R.id.prevGraphButton)
        {
            //Show Previous Graph
            Toast.makeText(getApplicationContext(), "Previous Graph", Toast.LENGTH_SHORT).show();
        }
    }
}
