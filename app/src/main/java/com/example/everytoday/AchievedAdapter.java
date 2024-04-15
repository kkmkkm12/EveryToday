package com.example.everytoday;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class AchievedAdapter extends BaseAdapter {
    ArrayList<ListItem> items = new ArrayList<ListItem>();
    Context context;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        ListItem listItem = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        Button achievedButton = convertView.findViewById(R.id.goalCheckButton);
        achievedButton.setText(listItem.getGoalStr());
        achievedButton.setBackgroundColor(Color.parseColor("#6699FF"));

        notifyDataSetChanged();
        return convertView;
    }

    public void clear(){
        items.clear();
        notifyDataSetChanged(); // 변경된 데이터를 알림
    }

    public void addItem(ListItem item) {
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
