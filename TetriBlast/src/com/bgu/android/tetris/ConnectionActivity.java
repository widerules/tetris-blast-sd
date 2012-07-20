package com.bgu.android.tetris;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ConnectionActivity extends Activity {
	final ConnectionActivity me = this;
	public static final String TAG = MainMenu.TAG;
    
	 // Intent request codes
    private static final int REQUEST_DISCOVER_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_JOIN_DEVICE = 3;
    private static final int REQUEST_JOIN_THE_GAME = 4;
    
    // Dialog IDs
    public static final int DIALOG_MAKING_CONNECT = 0;
	public static final int DIALOG_CONNECTED = 1;
    
    // Member object for the chat services
    private BluetoothConnectivity mBTconnection = null;
    
	// Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    
    //Status Dialogs Holder
    private Dialog mStatusDialog;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    
    private boolean mTetrisNotStarted;
    // The Handler that gets information back from the BluetoothConnectivity
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothConnectivity.MESSAGE_STATE_CHANGE:
                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothConnectivity.STATE_CONNECTED:
                	me.mStatusDialog.dismiss();
                	showDialog(DIALOG_CONNECTED);
                	break;
                case BluetoothConnectivity.STATE_CONNECTING:
                	Toast.makeText(me, "Connecting...", Toast.LENGTH_SHORT);
                    break;
                case BluetoothConnectivity.STATE_LISTEN:
                	break;
                case BluetoothConnectivity.STATE_NONE:
                	if(me.mStatusDialog.isShowing()){
                		me.mStatusDialog.dismiss();
                	}
                	Toast.makeText(me, "Not connected :(", Toast.LENGTH_SHORT);
                    break;
                }
                break;
            //Received message on Bluetooth
            case BluetoothConnectivity.MESSAGE_READ:
                int type = msg.arg2;
                if (type == BluetoothConnectivity.TYPE_START && mTetrisNotStarted) {
                	me.mStatusDialog.dismiss();
                	Log.i(MainMenu.TAG, "BT Received: Host started a game");
                	mTetrisNotStarted = false;
                	Intent intt = new Intent(me, TetriBlastActivity.class);
                	startActivityForResult(intt, REQUEST_JOIN_THE_GAME);
                }
                break;
            case BluetoothConnectivity.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(BluetoothConnectivity.DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case BluetoothConnectivity.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothConnectivity.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection);
        Log.d(TAG, "Connection Activity Created");
        Button hostBtn = (Button)findViewById(R.id.btn_host);
        Button joinBtn = (Button)findViewById(R.id.btn_join);
        mTetrisNotStarted = true;        
        hostBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "ensure discoverable");
				if (mBluetoothAdapter.getScanMode() !=
						BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
					Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
					startActivityForResult(discoverableIntent, REQUEST_DISCOVER_DEVICE);
				}
				else {
					SharedPreferences ref = getSharedPreferences(MainMenu.PREF_TAG, MODE_PRIVATE);
					SharedPreferences.Editor ed = ref.edit();
					ed.putInt(MainMenu.GAME_MODE, MainMenu.MODE_MULTY);
					ed.commit();
					Intent intt = new Intent(me, NewGameActivity.class);
					startActivity(intt);
				}
			    
			}
		});
        
        joinBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Launch the DeviceListActivity to see devices and do scan
	            Intent serverIntent = new Intent(me, DeviceListActivity.class);
	            startActivityForResult(serverIntent, REQUEST_JOIN_DEVICE);
			}
		});
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //Get Bluetooth Connectivity singleton 
        mBTconnection = BluetoothConnectivity.getInstance(me);
    }
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Request code: " + requestCode+ " received Activity Result: " + resultCode);
        switch (requestCode) {
        case REQUEST_DISCOVER_DEVICE:
          	// When the request to enable Bluetooth returns
            if (resultCode != 0) {
            	SharedPreferences ref = getSharedPreferences(MainMenu.PREF_TAG, MODE_PRIVATE);
				SharedPreferences.Editor ed = ref.edit();
				ed.putInt(MainMenu.GAME_MODE, MainMenu.MODE_MULTY);
				ed.commit();
            	Intent intt = new Intent(me, NewGameActivity.class);
				startActivity(intt);
            } 
            else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "Device not Discoverable");
                Toast.makeText(this, "Device not Discoverable" , Toast.LENGTH_SHORT).show();
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode != Activity.RESULT_OK) {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "BT not enabled" , Toast.LENGTH_SHORT).show();
            }
            break;
        case REQUEST_JOIN_DEVICE:
        	if(resultCode == Activity.RESULT_OK) {//TODO add checking BT enabled flag
        		// Get the device MAC address
                String address = data.getExtras()
                    .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
        		mBTconnection.connect(device);
        		showDialog(DIALOG_MAKING_CONNECT);
        		break;
        	}
        case REQUEST_JOIN_THE_GAME:
        	if(resultCode == Activity.RESULT_OK) {//Devices still connected
        		showDialog(DIALOG_CONNECTED);
        	}
        	else 
        		if(mStatusDialog.isShowing())
        			mStatusDialog.dismiss();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Connection Activity Started");
        mTetrisNotStarted = true;
        // If BT is disabled, start enable request.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        mBTconnection.setHandler(mHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	mStatusDialog = null;
    	switch(id) {
    	case DIALOG_MAKING_CONNECT:
    		mStatusDialog = createMakingConnectDialog();
    		break;
    	case DIALOG_CONNECTED:
    		mStatusDialog = createConnectedDialog();
    		break;
    	default:
    		mStatusDialog = null;
    	}
    	return mStatusDialog;
    }
    
    private Dialog createMakingConnectDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Bluetooth Connection...");
    	builder.setMessage("Making Bluetooth connection");
    	builder.setCancelable(false);
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mBTconnection.stop();
				mStatusDialog.dismiss();
				
			}
		});

    	AlertDialog dialog = builder.create();
    	mBTconnection.setHandler(mHandler);
    	return dialog;

    }
    
    private Dialog createConnectedDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Device Conected!");
    	builder.setMessage("Whaiting for Host start the game");
    	builder.setCancelable(false);
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mBTconnection.stop();
				mStatusDialog.dismiss();
				
			}
		});

    	AlertDialog dialog = builder.create();
    	return dialog;
    }
}