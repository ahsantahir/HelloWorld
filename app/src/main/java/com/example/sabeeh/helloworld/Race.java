package com.example.sabeeh.helloworld;

/**
 * Created by sabeeh on 1/16/2015.
 */
public class Race {
    boolean checked;
    boolean selected;
    float distance;
    String time;
    String dateTime;
    int type;
    String comment;


    boolean like;

    String local_file;

    public Race()
    {
        checked = false;
        distance = 0;
        time = "0";
        dateTime = "0";
        type = 1;
        local_file="";
        comment="";
        like=false;
    }

    public Race(boolean checked, boolean selected, float distance, String time, String dateTime, int type, String local_file,String comment,boolean like) {
        this.checked = checked;
        this.selected = selected;
        this.distance = distance;
        this.time = time;
        this.dateTime = dateTime;
        this.type = type;
        this.local_file = local_file;
        this.comment=comment;
        this.like=like;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public String getLocal_file() {
        return local_file;
    }

    public void setLocal_file(String local_file) {
        this.local_file = local_file;
    }
    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }


}
