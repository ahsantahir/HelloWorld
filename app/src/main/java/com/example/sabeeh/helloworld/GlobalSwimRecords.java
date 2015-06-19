package com.example.sabeeh.helloworld;

import com.example.sabeeh.helloworld.backend.JasonHandler;
import com.example.sabeeh.helloworld.entites.swim;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 26/03/15.
 */

public class GlobalSwimRecords {

    public static swim objectForAnalysis=new swim();
    public static JasonHandler jasonForAnalysis=new JasonHandler();
   public static  MobileServiceClient MobileClient;




    // Multiple Swim For analysis global variables
    public static int selected_rows_for_analysis=0;
    public static List<swim> selected_races=new ArrayList<swim>();
    public static  int multiple_swim_flag=0;
    public static  int analysis_flag_count=0;
    public static  List<JasonHandler> ListjasonForAnalysis=new ArrayList<JasonHandler>() ;




}
