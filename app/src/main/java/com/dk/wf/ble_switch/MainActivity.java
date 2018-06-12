/*

  To-do:

  - Notification
    > Find a way to remove notification channel because it is not compatible with 4.1

  - Connect multiple device
    > Detect total number of connected devices

  - On App launch
    > Auto connect to known device
    > Receive lamp on/off status

  - Timer
    > Background service
    > Appear and collapse when tap
    > Set timer page
    > Clock icon change color when timer added
    > send time data in byte
    > send 3 time:  current time, start time, end time.
    > need backend because need to store alarm. Firebase?


 - Disable switch when disconnected



  Changelog:

    1.0 Able to turn on off all LED.
        Would not disconnect when screen is locked.
        Changed from Nordic to Android source code.
        Faster scan of BLE devices and higher range of scanning.
        Auto reconnect after disconnected from device.
        Changed from buttons to switches.

    1.1 Removed switch label and replaced with button.
        All buttons have transparent background, and gravity to left.
        Allow user to modify switch name.
        Pop out dialogue when user long hold to edit switch name.
        Tap name to appear timer will can be easily implemented
        because it is a button and only long hold is used.





 */

package com.dk.wf.ble_switch;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;


// Time set import
 import java.util.Calendar;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class MainActivity extends Activity implements TimePickerDialog.OnTimeSetListener {


//    // Notification
//    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.notification_icon)
//            .setContentTitle("Test Notification")
//            .setContentText("This is the content text of the notification")
//            .setStyle(new NotificationCompat.BigTextStyle()
//                .bigText("This is the content text of the notification"))
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    //
    private NotificationHelper mNotificationHelper;

    // for edit lamp name dialog
    private Button btnEdit1, btnEdit2, btnEdit3;
    private EditText edit;

    private final static String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private Switch switch1,switch2,switch3;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private int EndOrStart = 0;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder rawBinder) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) rawBinder).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;

        }
    };
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }
    public void sendOnChannel1(String title, String message) {
        NotificationCompat.Builder nb = mNotificationHelper.getChannel1Notification(title, message);
        mNotificationHelper.getManager().notify(1, nb.build());
    }

    public void sendOnChannel2(String title, String message) {
        NotificationCompat.Builder nb = mNotificationHelper.getChannel2Notification(title, message);
        mNotificationHelper.getManager().notify(2, nb.build());
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        // noti
        mNotificationHelper = new NotificationHelper(this);

        // Time picker section

        // For user edit lamp name dialog
        btnEdit1 = (Button) findViewById(R.id.btnEdit1);
        btnEdit2 = (Button) findViewById(R.id.btnEdit2);
        btnEdit3 = (Button) findViewById(R.id.btnEdit3);
        edit = (EditText) findViewById(R.id.edit_text);

        // alarm
        Button buttonCancelAlarm = findViewById(R.id.button_cancel);
        buttonCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                cancelAlarm();
                addNotification();
            }
        });

        // time picker
//        Button timebutton = (Button) findViewById(R.id.btn);
        switch1=(Switch) findViewById(R.id.switch1);
        switch2=(Switch) findViewById(R.id.switch2);
        switch3=(Switch) findViewById(R.id.switch3);
        Button sTime = (Button) findViewById(R.id.startTime);
        Button eTime = (Button) findViewById(R.id.endTime);
        sTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getFragmentManager(), "time picker");
                EndOrStart = 1; //start
                switch1.setChecked(true);
            }
        });
        eTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getFragmentManager(), "time picker");
                EndOrStart = 0; //end
            }
        });
//        timebutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DialogFragment timePicker = new TimePickerFragment();
//                timePicker.show(getFragmentManager(), "time picker");
//            }
//        });

        btnEdit1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog1(edit.getText().toString());

                return true;
            }
        });
        btnEdit2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog2(edit.getText().toString());

                return true;
            }
        });
        btnEdit3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog3(edit.getText().toString());

                return true;
            }
        });

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);

        mGattServicesList.setOnChildClickListener(servicesListClickListener);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    String message = "01";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mBluetoothLeService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    String message = "02";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mBluetoothLeService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    String message = "11";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mBluetoothLeService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    String message = "12";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mBluetoothLeService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    String message = "21";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mBluetoothLeService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    String message = "22";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mBluetoothLeService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });


    }
//    private TimePickerDialog.OnTimeSetListener showTimePicker = new TimePickerDialog.OnTimeSetListener() {
//        @Override
//        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//            if(view.getId() == R.id.startTime) {
//
//            }
//            else if(view.getId() == R.id.endTime) {
//
//            }
//        }
//    };
private void addNotification() {
    NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
                    .setContentTitle("Notifications Example")
                    .setContentText("This is a test notification");

    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    builder.setContentIntent(contentIntent);

    // Add as notification
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(0, builder.build());
}

    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.cancel(pendingIntent);
    }

    // thanks https://www.youtube.com/watch?v=QMwaNN_aM3U
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView endTimeShow = (TextView)findViewById(R.id.endText);
        TextView startTimeShow = (TextView)findViewById(R.id.startText);

        String nowTime, endTime;
        final Calendar c = Calendar.getInstance();
        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND,0);
        startAlarm(c);



        if(hourOfDay < 10) {
            nowTime = Integer.toString(nowHour) + Integer.toString(nowMinute);
            endTime = "0" + Integer.toString(hourOfDay) + Integer.toString(minute);
        } else if (nowHour < 10) {
            nowTime = "0" + Integer.toString(nowHour) + Integer.toString(nowMinute);
            endTime = Integer.toString(hourOfDay) + Integer.toString(minute);
        } else if (minute < 10) {
            nowTime = Integer.toString(nowHour) + Integer.toString(nowMinute);
            endTime = Integer.toString(hourOfDay) + "0" + Integer.toString(minute);
        } else if (nowMinute < 10) {
            nowTime = Integer.toString(nowHour) + "0" + Integer.toString(nowMinute);
            endTime = Integer.toString(hourOfDay) + Integer.toString(minute);
        } else {
            nowTime = Integer.toString(nowHour) + Integer.toString(nowMinute);
            endTime = Integer.toString(hourOfDay) + Integer.toString(minute);
        }
        if(EndOrStart == 1){ // start
            startTimeShow.setText(endTime);
        } else if (EndOrStart == 0) {
            endTimeShow.setText(endTime);

        }


        byte[] setTimeHex, nowTimeHex;
        try {
            //send data to service
            setTimeHex = endTime.getBytes("UTF-8");
            nowTimeHex = nowTime.getBytes("UTF-8");
//            Log.d(TAG, "Connect request result=" + endTime);
            mBluetoothLeService.writeRXCharacteristic(setTimeHex);
            mBluetoothLeService.writeRXCharacteristic(nowTimeHex);
            //Update the log with time stamp
            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
            //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
    private void showDialog1(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set name");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view, null);
        final EditText edit_dialog = (EditText) view.findViewById(R.id.edit_dialog);
        edit_dialog.setText(str);
        builder.setView(view);
        builder.setNegativeButton("cancel", null);
        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnEdit1.setText(edit_dialog.getText().toString());
            }

        });
        builder.show();

    }
    private void showDialog2(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set name");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view, null);
        final EditText edit_dialog = (EditText) view.findViewById(R.id.edit_dialog);
        edit_dialog.setText(str);
        builder.setView(view);
        builder.setNegativeButton("cancel", null);
        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnEdit2.setText(edit_dialog.getText().toString());
            }

        });
        builder.show();

    }
    private void showDialog3(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set name");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view, null);
        final EditText edit_dialog = (EditText) view.findViewById(R.id.edit_dialog);
        edit_dialog.setText(str);
        builder.setView(view);
        builder.setNegativeButton("cancel", null);
        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnEdit3.setText(edit_dialog.getText().toString());
            }

        });
        builder.show();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }
    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


}