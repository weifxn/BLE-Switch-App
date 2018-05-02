# BLE-android


## 1.0 Allow permission for bluetooth

1. Go AndroidManifest.xml

```html
  <uses-sdk
        android:minSdkVersion="18"
        android:targetSdkVersion="18" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```


## 2.0 Check bluetooth using Bluetooth Adapter
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

## 3.0 Assign button to scan
1. Declare button.

```java
	private Button btnConnectDisconnect;
```

2. Bind button to xml.
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
 
