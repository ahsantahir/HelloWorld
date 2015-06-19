package com.example.sabeeh.helloworld;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by sabeeh on 2/13/2015.
 */
public class CustomAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private ArrayList<Race> mData = new ArrayList<Race>();
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private LayoutInflater mInflater;

    public CustomAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(Race item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(Race item) {
        mData.add(item);
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Race getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.snippet_item1, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.raceLayoutDistanceValue);
                    TextView duration = (TextView) convertView.findViewById(R.id.raceLayoutTimeValue);
                    TextView dateTime = (TextView) convertView.findViewById(R.id.raceLayoutDateTimeValue);
                    TextView distance = (TextView) convertView.findViewById(R.id.raceLayoutDistanceValue);

                    CheckBox raceSelected = (CheckBox) convertView.findViewById(R.id.raceLayoutSelected);
                    raceSelected.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CheckBox current = (CheckBox)v;
                            ((CheckBox) v).setChecked(!current.isChecked());
                            getItem(position).setSelected(!current.isChecked());
                            notifyDataSetChanged();
                        }
                    });

                    ImageView raceType = (ImageView) convertView.findViewById(R.id.raceLayoutType);
                    if(getItem(position).getType()==1)
                        raceType.setBackgroundResource(R.drawable.swim_type_1);
                    else if(getItem(position).getType()==2)
                        raceType.setBackgroundResource(R.drawable.swim_type_2);
                    else if(getItem(position).getType()==3)
                        raceType.setBackgroundResource(R.drawable.swim_type_3);
                    else if(getItem(position).getType()==4)
                        raceType.setBackgroundResource(R.drawable.swim_type_4);

                    if(getItem(position).isSelected())
                        raceSelected.setChecked(true);

                    else
                        raceSelected.setChecked(false);

                    duration.setText(getItem(position).getTime());
                    dateTime.setText(getItem(position).getDateTime());
                    distance.setText(getItem(position).getDistance() + "LOL");

                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.snippet_item2, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.textSeparator);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(mData.get(position).getTime());

        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
