package com.example.sabeeh.helloworld;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.provider.ContactsContract;
import android.widget.CheckBox;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by sabeeh on 1/8/2015.
 */
public class GraphLine {

    LineGraphSeries<DataPoint> points;
    DataPoint[] backupPoints;
    int color;
    String title;
    CheckBox checkBox;

    GraphLine(Context context)
    {
        points = null;
        backupPoints = null;
        color = Color.BLACK;
        title = "Race 1";
        checkBox = new CheckBox(context);
    }

    GraphLine(LineGraphSeries<DataPoint> linePoints, int color, String title, Context context)
    {
        points = linePoints;
        this.color = color;
        this.title = title;
        checkBox = new CheckBox(context);
        backupPoints = null;
    }

    public void setPoints(LineGraphSeries<DataPoint> points)
    {
        this.points = points;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public void setTitle(String title) { this.title = title; }

    public void setCheckBox(String text) {
        checkBox.setChecked(true);
        checkBox.setText(text);
        checkBox.setTextColor(Color.BLACK);
    }

    public void setBackupPoints (DataPoint[] bPoints)
    {
        backupPoints = bPoints;
    }

    public CheckBox getCheckBox() { return checkBox; }

    public int getColor()
    {
        return color;
    }

    public LineGraphSeries<DataPoint> getPoints()
    {
        return points;
    }

    public String getTitle() { return title; }

    public DataPoint[] getBackupPoints () { return backupPoints; }
}
