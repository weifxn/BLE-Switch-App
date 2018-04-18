package com.dk.wf.switchapp;

import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;

    Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        aSwitch = (Switch) findViewById(R.id.switch1);

        aSwitch .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked == true) {
                    Toast.makeText(getBaseContext(), "On", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getBaseContext(), "Off", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}
