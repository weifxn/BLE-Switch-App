package com.dk.wf.ble_switch;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter{
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    // for bluetooth
    private BluetoothLeService mBluetoothLeService;

    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>>  listDataChild, BluetoothLeService mBluetoothLeService) {
        this.context = context;
        this.listDataChild = listDataChild;
        this.listDataHeader = listDataHeader;
        this.mBluetoothLeService = mBluetoothLeService;
    }
    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if(convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//        ChildHolder childHolder;
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
//            childHolder = new ChildHolder();
//            childHolder.textChild = (TextView) convertView.findViewById(R.id.lblListItem);
//            childHolder.switchChild = (Switch) convertView.findViewById(R.id.switches);
//            convertView.setTag(childHolder);
//
//        } else {
//            childHolder = (ChildHolder) convertView.getTag();
//        }
//        childHolder.textChild.setText(childText);
//        childHolder.switchChild.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked) {
//                    String message = "01";
//
//                    byte[] value;
//                    try {
//                        //send data to service
//                        value = message.getBytes("UTF-8");
//                        mBluetoothLeService.writeRXCharacteristic(value);
//                        //Update the log with time stamp
//                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    } catch (UnsupportedEncodingException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                } else {
//                    String message = "02";
//                    byte[] value;
//                    try {
//                        //send data to service
//                        value = message.getBytes("UTF-8");
//                        mBluetoothLeService.writeRXCharacteristic(value);
//                        //Update the log with time stamp
//                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    } catch (UnsupportedEncodingException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });

        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;

    }
    private static class ChildHolder {
        TextView textChild;
        Switch switchChild;
    }




}


