# BLE-android

## 1.0 Setting up Bluetooth Adapter
### Allow permission for Bluetooth

1. Go AndroidManifest.xml
  
  ```html
  <uses-sdk
        android:minSdkVersion="18"
        android:targetSdkVersion="18" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
  ```


### Add `BluetoothAdapter` in MainActivity
1. Create bluetooth adapter object. 
  
  ```java
	private BluetoothAdapter mBtAdapter = null;
  ```
  
2. Check if Bluetooth is available in `onCreate` method.
  
  ```java
	mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	
	if(mBtAdapter == null) {
		Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG);
		finish();
		return;
	}
  ```

### Assign button to scan
1. Create button.
  
  ```java
	private Button btnConnectDisconnect;
  ```

2. Bind button to xml in `onCreate`.
  
  ```java
	btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
  ```

3. Add click event listener.
  
  ```java
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
  ```

## 2.0 Bluetooth device list

### Creating `DeviceListActivity.java`

1. Create a new layout XML file named `device_list.xml` and add these elements.
   
   - TextView: `title_devices`
      - text: Select a Device
   - ListView: `new_devices`
      - `stackFromBottom = "true"`
   - TextView: `empty`
      - text: Scanning for devices
   - Button: `btn_cancel`
      - text: Cancel

2. Create a new class file `DeviceListActivity`.
  
  ```java
  public class DeviceListActivity extends Activity {
      private BluetoothAdapter mBluetoothAdapter;
      List<BluetoothDevice> deviceList;
  }
  ```

3. Create `onCreate` method in the class.
  
  ```java
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
      setContentView(R.layout.device_list);
  }
  ```

4. To check if Bluetooth Low Energy is supported on the device, add this line below.
  
  ```java
  if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(this, "Bluetooth Low Energy not supported", Toast.LENGTH_SHORT).show();
  }
  ```


