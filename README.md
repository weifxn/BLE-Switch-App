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


 
