package com.example.sabeeh.helloworld.entites;

import java.util.Date;

/**
 * Created by mac on 16/04/15.
 */
public class tbl_swim {


        @com.google.gson.annotations.SerializedName("id")
        public String id;



        @com.google.gson.annotations.SerializedName("user")
        public String user;

        @com.google.gson.annotations.SerializedName("pool_length")
        public int pool_length;


        @com.google.gson.annotations.SerializedName("duration")
        public float duration;


        @com.google.gson.annotations.SerializedName("rating")
        public int rating;


        @com.google.gson.annotations.SerializedName("analysed")
        public Boolean analysed;


        @com.google.gson.annotations.SerializedName("time_stamp")
        public Date time_Stamp;


        @com.google.gson.annotations.SerializedName("local_file")
        public String Local_file;


        @com.google.gson.annotations.SerializedName("jason_analysis")
        public String jason_analysis;


        @com.google.gson.annotations.SerializedName("comment")
        public String Comment;

    @com.google.gson.annotations.SerializedName("liked")
    public Boolean liked;

    @com.google.gson.annotations.SerializedName("azureflag")
    public Boolean azureflag;

        public tbl_swim()
        {
            super();
        }




    public void   getAzureObj(swim temp)
    {
        this.id=temp.getId().toString();
        this.user=temp.user;
        this.pool_length =temp.pool_length;
        this.duration = temp.duration;
        this.rating = temp.rating;
        this.analysed = temp.analysed;
        this.time_Stamp = temp.time_Stamp;
        this.Local_file = temp.Local_file;
        this.jason_analysis=temp.jason_analysis;
        this.Comment=temp.Comment;
        this.liked=temp.Liked;
        this.azureflag=temp.AzureFlag;
    }

    public swim  getswimObj()
    {
        swim obj=new swim();


       obj.user= this.user;
       obj.pool_length= this.pool_length ;
       obj.duration= this.duration ;
        obj.rating=this.rating ;
        obj.analysed=this.analysed ;
        obj.time_Stamp=this.time_Stamp ;
        obj.jason_analysis=this.jason_analysis;
       obj.Comment= this.Comment;
        obj.Liked= this.liked;
        obj.Local_file=this.Local_file;
        obj.AzureFlag=this.azureflag;
        return  obj;
    }

        public tbl_swim(String ID,String User, int pool_length, float duration, int rating, Boolean analysed, Date time_Stamp, String local_file, String temp_jason,String Comment,Boolean liked,Boolean azureflag) {
            this.id=ID;
            this.user=User;
            this.pool_length = pool_length;
            this.duration = duration;
            this.rating = rating;
            this.analysed = analysed;
            this.time_Stamp = time_Stamp;
            this.Local_file = local_file;
            this.jason_analysis=temp_jason;
            this.Comment=Comment;
            this.liked=liked;
            this.azureflag=azureflag;
        }
    }


