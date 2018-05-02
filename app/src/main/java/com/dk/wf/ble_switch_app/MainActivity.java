package com.dk.wf.ble_switch_app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends Activity {

    RadioGroup mRg;

    // UI section
    private Button btnConnectDisconnect, btnOn, btnOff;


    // Bluetooth section
    private BluetoothAdapter mBtAdapter = null;
    private ArrayAdapter<String> listAdapter;

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();


        // Check if bluetooth is available
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ArrayAdapter is to provide views for AdapterView, Adapter provides access to the data items
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        btnOn = (Button) findViewById(R.id.onButton);
        btnOff = (Button) findViewById(R.id.offButton);
        // service_init();

        // Set connect button function
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if bluetooth is activated
                if (!mBtAdapter.isEnabled()) {
                    // An intent is an abstract description of an operation to be performed
                    // used in start activity
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

                }
                else {
                    // If connected the button text will show "Disconnect"
                    // If wanna auto connect, should test here when click connect it connects to SM_BT
                    if (btnConnectDisconnect.getText().equals("Connect")){
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    }
                }
            }
        });


    }
}
