# Bluetooth Low Energy

### 1.0 Permissions
1. In `AndroidManifest.xml`, include
    ```java
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
    ```

### 2.0 Setup
1. In `MainActivity` class, create a `BluetoothAdapter` object.
    ```java
    private BluetoothAdapter mBluetoothAdapter;
    // for Step 3: pass to startActivityForResult()
    private static final int REQUEST_ENABLE_BT = 2;
    ```

2. Then, initialize `BluetoothAdapter` in `onCreate()` by adding
    ```java
    final BluetoothManager bluetoothManager = 
        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // getSystemService() return to bluetoothManager, which is used to get the Adapter  
        mBluetoothAdapter = bluetoothManager.getAdapter();
    ```
3.  Next, use `isEnabled()` to ensure that Bluetooth is enabled.
    ```java
    // if null or return false for isEnabled()
    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
        // it will prompt user to enable Bluetooth in Settings.
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
    }
    ```
### 3.0 Scan for device


1. Create a new class `DeviceScanAcitivity` and add the following
    ```java
    public class DeviceScanActivity extends ListActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD); // 10 seconds

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        
    }

    ```
2. 

3. Implement `LeScanCallback` to deliver scan results.
    ```java
    private LeDeviceListAdapter mLeDeviceListAdapter;

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    ```
