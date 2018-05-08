# BLE for Android

## 1.0 Bluetooth Adapter
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

## 2.0 Bluetooth Device List

### Add new user interface layout
1. Create a new layout XML file named `device_list.xml` and add these elements.
   
   - TextView: `title_devices`
      - text: Select a Device
   - ListView: `new_devices`
      - `stackFromBottom = "true"`
   - TextView: `empty`
      - text: Scanning for devices
   - Button: `btn_cancel`
      - `text="@android:string/cancel"`

2. Add these String values in `res/values/strings.xml`.
  
  ```html
  <resources>
      <string name="app_name">BLE-Switch-App</string>
      <string name="scan">Scan</string>
      <string name="cancel">Cancel</string>
      <string name="select_device">Select a device</string>
      <string name="scanning">Scanning for devices</string>
  </resources>
  ```

### Creating `DeviceListActivity.java`


1. Create a new class file `DeviceListActivity`.
  
  ```java
  public class DeviceListActivity extends Activity {
      private BluetoothAdapter mBluetoothAdapter;
      List<BluetoothDevice> deviceList;
      private boolean mScanning;
      private Handler mHandler;
      private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds
      private TextView mEmptyList;
  }
  ```

2. Create `onCreate` method in the class.
  
  ```java
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
      setContentView(R.layout.device_list);
  }
  ```

### Initialize Bluetooth Adapter

1. Create a `BluetoothManager` object and assign to `mBluetoothAdapter` in `onCreate`.
  
  ```java
  final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
  mBluetoothAdapter = bluetoothManager.getAdapter();
  ```

2. To check if both **Bluetooth** and **Bluetooth Low Energy** are supported on the device, add this line below.
  - Bluetooth:
  
  ```java
  if (mBluetoothAdapter == null) {
      Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
      finish();   
      return;
   }
  ```
  
  - Bluetooth Low Energy:
  
  ```java
  if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Toast.makeText(this, "Bluetooth Low Energy not supported", Toast.LENGTH_SHORT).show();
  }
  ```

### Add Bluetooth scanner

1. Create `addDevice` method.
  
  ```java
  private void addDevice(BluetoothDevice device, int rssi) {
      boolean deviceFound = false;

      for (BluetoothDevice listDev : deviceList) {
          if (listDev.getAddress().equals(device.getAddress())) {
              deviceFound = true
              break;
          }
      }

      devRssiValues.put(device.getAddress(), rssi);
      if (!deviceFound) {
          mdeviceList.add(device);
          mEmptyList.setVisibility(View.GONE);
          deviceAdapter.notifyDataSetChanged();
      }
  }
  ```

2. Add a callback `LeScanCallback` to deliver scan results.
  
  ```java
  private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
          runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  addDevice(device,rssi);
              }
          });
      }
  };

  ```

3. Add a scanner method `scanLeDevice` to scan for devices. 
  
  ```java
  private void scanLeDevice(final boolean enable) {
      final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
      if (enable) {
          mHandler.postDelayed(new Runnable() {
              @Override
              public void run() {
                  mScanning = false;
                  mBluetoothAdapter.stopLeScan(mLeScanCallback);
                  cancelButton.setText(R.string.scan);
              }
          }, SCAN_PERIOD); // Stops scanning after 10 seconds.

          mScanning = true;
          mBluetoothAdapter.startLeScan(mLeScanCallback);
          cancelButton.setText(R.string.cancel);
      } else {
          mScanning = false;
          mBluetoothAdapter.stopLeScan(mLeScanCallback);
          cancelButton.setText(R.string.cancel);
      }
  }
  ```

### Display device list

1. First, populate the list with `populateList`.
  
  ```java
  private void populateList() {
      deviceList = new ArrayList<BluetoothDevice>();
      deviceAdapter = new DeviceAdapter(this,deviceList);
      devRssiValues = new HashMap<String, Integer>();

      ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
      newDevicesListView.setAdapter(deviceAdapter);
      newDevicesListView.setOnItemClickListener(mDeviceClickListener);

      scanLeDevice(true);

  }
  ```
2. Create a object from `onItemClickListener` 
  
  ```java
  private onItemClickListener mDeviceClickListener = new OnItemClickListener() {
      @Override
      public void onitemClick(AdapterView<?> parent, View view, int position, long id) {
          BluetoothDevice device = deviceList.get(position);
          mBluetoothAdapter.stopLeScan(mLeScanCallback);

          Bundle b = new Bundle();
          b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAdress());

          Intent result = new Intent();
          result.putExtras(b);
          setResult(Activity.RESULT_OK, result);
          finish();
      }
  }
  ```








