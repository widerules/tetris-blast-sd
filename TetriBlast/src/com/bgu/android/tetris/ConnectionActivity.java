package com.bgu.android.tetris;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
	public static final String TAG = "TetrisBlast";
    
	 // Intent request codes
    private static final int REQUEST_DISCOVER_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_JOIN_DEVICE = 3;
    
    
    // Member object for the chat services
    private BluetoothConnectivity mBTconnection = null;
    
	// Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    
    public ProgressDialog mProgresDialog;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    
    // The Handler that gets information back from the BluetoothConnectivity
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothConnectivity.MESSAGE_STATE_CHANGE:
                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothConnectivity.STATE_CONNECTED:
                	me.mProgresDialog.dismiss();
                	me.mProgresDialog = ProgressDialog.show(me, "Device Conected!", "Whaiting for Host start a game");
                	
//                	Intent intt = new Intent(me, NewGameActivity.class);
//					startActivity(intt);
                    break;
                case BluetoothConnectivity.STATE_CONNECTING:
                	Toast.makeText(me, "Connecting...", Toast.LENGTH_SHORT);
                    break;
                case BluetoothConnectivity.STATE_LISTEN:
                case BluetoothConnectivity.STATE_NONE:
                	Toast.makeText(me, "Not connected :(", Toast.LENGTH_SHORT);
                    break;
                }
                break;
//            case MESSAGE_WRITE:
//                byte[] writeBuf = (byte[]) msg.obj;
//                // construct a string from the buffer
//                String writeMessage = new String(writeBuf);
//                mConversationArrayAdapter.add("Me:  " + writeMessage);
//                break;
            case BluetoothConnectivity.MESSAGE_READ:
                int type = msg.arg2;
                // construct a string from the valid bytes in the buffer
                //String readMessage = new String(readBuf, 0, msg.arg1);
                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                if (type == BluetoothConnectivity.TYPE_START) {
                	me.mProgresDialog.dismiss();
                	Toast.makeText(me, "Host started a game", Toast.LENGTH_SHORT);
                	Intent intt = new Intent(me, TetriBlastActivity.class);
					startActivity(intt);
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
        //getWindow().setBackgroundDrawableResource(R.drawable.tetris_bg);//Draw background
        Button hostBtn = (Button)findViewById(R.id.btn_host);
        Button joinBtn = (Button)findViewById(R.id.btn_join);
                
        hostBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "ensure discoverable");
				//TODO pass parameter Game Mode to TetrisBalst Activity
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
        //Get Bluetooth Connectivity singleton 
        mBTconnection = BluetoothConnectivity.getInstance(me);
        mBTconnection.setHandler(mHandler);
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
    }
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
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
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "Device not Discoverable");
                Toast.makeText(this, "Device not Discoverable" , Toast.LENGTH_SHORT).show();
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
            	//TODO check BT enabled flag
                //me.mProgresDialog = ProgressDialog.show(me, "Bluetooth Connection ...", "Making Bluetooth connection");
            } else {
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
        		me.mProgresDialog = ProgressDialog.show(me, "Bluetooth Connection ...", "Making Bluetooth connection");
        	}
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
        	//setupChat();
            //TODO chesk thisif (mChatService == null) setupChat();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
//    private void setupChat() {
//        Log.d(TAG, "setupChat()");
//
//        //sendMessage(message);
//        
//        // Initialize the BluetoothChatService to perform bluetooth connections
//        //mChatService = new BluetoothMsgService(this, mHandler);
//    }

}