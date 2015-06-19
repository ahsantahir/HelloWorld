package com.example.sabeeh.helloworld.entites;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by mac on 26/03/15.
 */
@Table(name = "swim")
public class swim extends Model {


    @Column(name = "user")
    @com.google.gson.annotations.SerializedName("user")
    public String user;



    @Column(name = "pool_length")
    @com.google.gson.annotations.SerializedName("pool_length")
    public int pool_length;

    @Column(name = "duration")
    @com.google.gson.annotations.SerializedName("duration")
    public float duration;

    @Column(name = "rating")
    @com.google.gson.annotations.SerializedName("rating")
    public int rating;

    @Column(name = "analysed")
    @com.google.gson.annotations.SerializedName("analysed")
    public Boolean analysed;

    @Column(name = "time_Stamp")
    @com.google.gson.annotations.SerializedName("time_stamp")
    public Date time_Stamp;


    @Column(name = "SwimDate")
    @com.google.gson.annotations.SerializedName("swimdate")
    public String SwimDate;

    @Column(name = "Local_file")
    @com.google.gson.annotations.SerializedName("local_file")
     public String Local_file;

    @Column(name = "Jason_Analysis")
    @com.google.gson.annotations.SerializedName("jason_analysis")
    public String jason_analysis;



    @Column(name = "Comment")
    @com.google.gson.annotations.SerializedName("Comment")
    public String Comment;

    @Column(name="AzureFlag")
    public boolean AzureFlag;


    @Column(name="Liked")
    public boolean Liked;

    @Column(name="Laps")
    public int Laps;


    public swim()
    {
        super();
    }


    public swim(String User,int pool_length, float duration, int rating, Boolean analysed, Date time_Stamp, String local_file,String temp_jason,boolean azureflag,String swimdate,String comment,boolean like,int laps) {

        this.user=User;
        this.pool_length = pool_length;
        this.duration = duration;
        this.rating = rating;
        this.analysed = analysed;
        this.time_Stamp = time_Stamp;
        this.Local_file = local_file;
        this.jason_analysis=temp_jason;
        this.AzureFlag=azureflag;
        this.SwimDate=swimdate;
        this.Comment=comment;
        this.Liked=like;
        this.Laps=laps;
    }
}
