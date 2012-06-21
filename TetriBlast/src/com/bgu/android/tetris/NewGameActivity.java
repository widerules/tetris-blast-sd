package com.bgu.android.tetris;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class NewGameActivity extends Activity implements SeekBar.OnSeekBarChangeListener{
	protected static final String TAG = MainMenu.TAG;
	public final NewGameActivity me = this;
	public static final String DIFFICULTY = "difficulty";
	public static final String SHADOW = "shadow";
	
	protected Profile profileDb = Profile.getInstance(this);
	private TextView profileName;
	private SeekBar diffLevel;
	private TextView diffTxt;
	private CheckBox shadowChk;
	private CheckBox coopChk;//TODO denis need to change it to radio buttons!!!
	private Button startBtn;
	private TextView statusTxt;
	private RelativeLayout multiLayout;
	private SharedPreferences ref;
	protected BluetoothConnectivity mBluetoothCon = BluetoothConnectivity.getInstance(this);
	
	// The Handler that gets information back from the BluetoothConnectivity
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothConnectivity.MESSAGE_STATE_CHANGE:
                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothConnectivity.STATE_CONNECTED:
                	showStatus();
                	startBtn.setEnabled(true);
                	Toast.makeText(me, "Remote Device Connected!", Toast.LENGTH_SHORT);
                	break;
                case BluetoothConnectivity.STATE_CONNECTING:
                	Toast.makeText(me, "Connecting...", Toast.LENGTH_SHORT);
                    break;
                case BluetoothConnectivity.STATE_LISTEN:
                	showStatus();
                	Toast.makeText(me, "Listening to remote device connection...", Toast.LENGTH_SHORT);
                    break;
                case BluetoothConnectivity.STATE_NONE:
                	showStatus();
                	Toast.makeText(me, "Not connected :(", Toast.LENGTH_SHORT);
                    break;
                }
                break;
            }
        }
    };
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
        mBluetoothCon.setHandler(mHandler);
        startBtn = (Button)findViewById(R.id.btn_newgame_start);
        diffLevel = (SeekBar)findViewById(R.id.seekb_newgame_difficulty);
        diffLevel.setOnSeekBarChangeListener(this);
        profileName = (TextView)findViewById(R.id.txt_newgame_profile);
        diffTxt = (TextView)findViewById(R.id.txt_newgame_difficulty);
        shadowChk = (CheckBox)findViewById(R.id.checkbox_newgame_shadow);
    	coopChk = (CheckBox)findViewById(R.id.checkbox_newgame_cooperative);
    	statusTxt = (TextView)findViewById(R.id.txt_start_status);
    	multiLayout = (RelativeLayout)findViewById(R.id.new_game_multi_mode);
        
    	startBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveActivitySettings();
				mBluetoothCon.write(BluetoothConnectivity.TYPE_START, null);
				Intent intt = new Intent(me, TetriBlastActivity.class);
				startActivity(intt);
				}    
		});
    }
    @Override
    public void onStart()
    {
    	super.onStart();
    	ref = getSharedPreferences(MainMenu.PREF_TAG, MODE_PRIVATE);
        profileDb.loadProfileById(ref.getString(MainMenu.PROFILE_ID, null));
        diffLevel.setOnSeekBarChangeListener(this);
        profileName.setText("Profile: " +profileDb.getName());
        diffTxt.setText(getString(R.string.difficult) + "  " +Integer.toString(profileDb.getDifficulty()));
        diffLevel.setProgress(profileDb.getDifficulty());
        shadowChk.setChecked(profileDb.isShadow());
        switch (ref.getInt(MainMenu.GAME_MODE, MainMenu.MODE_UNDEFINED))
        {
        case MainMenu.MODE_UNDEFINED:
        	break;
        case MainMenu.MODE_SINGLE:
        	break;
        case MainMenu.MODE_COOP:
        case MainMenu.MODE_VS:
        case MainMenu.MODE_MULTY:
        	multiLayout.setVisibility(View.VISIBLE);
        	statusTxt.setVisibility(View.VISIBLE);
        	startBtn.setEnabled(false);
        	showStatus();
        	boolean temp;
        	if (profileDb.getGameMode() == MainMenu.MODE_COOP)
        		temp =true;
        	else
        		temp = false;
        	coopChk.setChecked(temp);
        	break;
        default:
        	break;
        }
        //Start Listening on Bluetooth connection
        mBluetoothCon.startListen();
    }
	private void showStatus() {
		if (mBluetoothCon.getState() == BluetoothConnectivity.STATE_CONNECTED)
    	{
    		statusTxt.setText(R.string.status_connected);
    		statusTxt.setTextColor(getResources().getColor(R.color.green_ok));
    	}
    	else
    	{
    		statusTxt.setText(R.string.status_disconnected);
    		statusTxt.setTextColor(getResources().getColor(R.color.red_no_connect));
    	}
		
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		diffTxt.setText(getString(R.string.difficult) +"  " + Integer.toString(progress));
		//TODO add changes to the profile
		
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	private void saveActivitySettings()
	{
		profileDb.setDifficulty(diffLevel.getProgress());
		profileDb.setShadow(shadowChk.isChecked());
		int temp;
		if (coopChk.isChecked() == true)
			temp = MainMenu.MODE_COOP;//TODO denis need to change it to radio buttons!!!
		else
		{
			temp = MainMenu.MODE_VS;
		}
		if (ref.getInt(MainMenu.GAME_MODE, MainMenu.MODE_UNDEFINED)==MainMenu.MODE_MULTY
				|| ref.getInt(MainMenu.GAME_MODE, MainMenu.MODE_UNDEFINED)==MainMenu.MODE_COOP
				|| ref.getInt(MainMenu.GAME_MODE, MainMenu.MODE_UNDEFINED)==MainMenu.MODE_VS)
			profileDb.setGameMode(temp);
		profileDb.saveProfileById(ref.getString(MainMenu.PROFILE_ID, null));
		
		SharedPreferences.Editor ed = ref.edit();
		ed.putInt(DIFFICULTY, diffLevel.getProgress());
		ed.putBoolean(SHADOW, shadowChk.isChecked());
		ed.commit();
	}
}
