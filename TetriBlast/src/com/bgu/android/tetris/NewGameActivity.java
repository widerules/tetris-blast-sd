package com.bgu.android.tetris;

import com.bgu.android.tetris.R.color;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class NewGameActivity extends Activity implements SeekBar.OnSeekBarChangeListener{
	final Activity me = this;
	protected Profile profileDb = Profile.getInstance(this);
	TextView profileName;
	SeekBar diffLevel;
	TextView diffTxt;
	CheckBox shadowChk;
	CheckBox coopChk;//TODO denis need to change it to radio buttons!!!
	Button startBtn;
	TextView statusTxt;
	RelativeLayout multiLayout;
	SharedPreferences ref;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
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
        	if (isConnected())
        	{
        		statusTxt.setText(R.string.status_connected);
        		statusTxt.setTextColor(getResources().getColor(R.color.green_ok));
        	}
        	else
        	{
        		statusTxt.setText(R.string.status_disconnected);
        		statusTxt.setTextColor(getResources().getColor(R.color.red_no_connect));
        	}
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
	}
	/*
	 * TODO denis plz do it
	 * NEED TO CHECK IF I AM CONNECTED TO BLUETOOTH
	 */
	private boolean isConnected()
	{
		return true;
	}
}
