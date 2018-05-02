package com.dk.wf.ble_switch_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ServiceConnection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dk.wf.ble_switch_app.R;

import java.util.List;
import java.util.Map;

public class DeviceListActivity extends Activity {

    // Bluetooth section
    private BluetoothAdapter mBluetoothAdapter;
    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    private ServiceConnection onService = null;

    // Rssi Value
    Map<String, Integer> devRssiValues;
}

class DeviceAdapter extends BaseAdapter {
    Context context;
    List<BluetoothDevice> devices;
    LayoutInflater inflater;

    // Constructor for device adapter class object
    public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.devices = devices;
    }

    // Getter
    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    // Bluetooth device details
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup vg;

        if(convertView != null) {
            vg = (ViewGroup) convertView;
        }
        else {
            // show on device_element, list out in the select device page
            vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
        }

        // collect the bluetooth device details
        BluetoothDevice device = devices.get(position);
        final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
        final TextView tvname = ((TextView) vg.findViewById(R.id.name));
        final TextView tvpaired = ((TextView) vg.findViewById(R.id.paired));
        final TextView tvrssi = ((TextView) vg.findViewById(R.id.rssi));

        // RSSI section
        tvrssi.setVisibility(View.VISIBLE);
        byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
        if (rssival != 0) {
            tvrssi.setText("Rssi = " + String.valueOf(rssival));
        }


    }
}
