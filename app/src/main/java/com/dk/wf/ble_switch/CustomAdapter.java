package com.dk.wf.ble_switch;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.dk.wf.ble_switch.R;

public class CustomAdapter extends BaseAdapter{
    Context context;
    LayoutInflater inflater;
    String switchName[];

    public CustomAdapter(Context applicationContext, String[] switchName) {
        this.context = applicationContext;
        this.switchName = switchName;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return switchName.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.activity_listview, null);
        Button switchText = (Button) view.findViewById(R.id.textView);
        switchText.setText(switchName[i] + " " + Integer.toString(i + 1));

        return view;
    }
}
