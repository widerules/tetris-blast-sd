package com.bgu.android.tetris;

import java.util.EmptyStackException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainMenu extends Activity{
	private final Activity me = this;
	public static final String TAG = "TetrisBlast";
	public static final String PREF_TAG = "com.bgu.android.tetris";
	public static final String HAVE_ACTIVE_PROFILE = "have_profile";
	public static final String PROFILE_ID = "profile_id";
	public static final String GAME_MODE = "game_mode";//Mode type
	public static final int MODE_UNDEFINED = 0;
	public static final int MODE_SINGLE = 1;
	public static final int MODE_MULTY = 2;
	public static final int MODE_VS = 3;
	public static final int MODE_COOP = 4;
	
	TextView profileName; 
	
	Profile profileDb = Profile.getInstance(this);
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        profileName = (TextView)findViewById(R.id.txt_main_menu_profile);
        Button singleBtn = (Button)findViewById(R.id.btn_main_menu_single);
        Button multiBtn = (Button)findViewById(R.id.btn_main_menu_multi);
        Button manageBtn = (Button)findViewById(R.id.btn_main_menu_manage);
        Button settingBtn = (Button)findViewById(R.id.btn_main_menu_settings);
        Button leaderBtn = (Button)findViewById(R.id.btn_main_menu_leader);
        Button helpBtn = (Button)findViewById(R.id.btn_main_menu_help);
        TextView verTxt = (TextView)findViewById(R.id.txt_main_menu_ver);
        verTxt.setText(verTxt.getText().toString() + getString(R.string.ver_num));
        //Button Listenerses
        singleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences ref = getSharedPreferences(PREF_TAG, MODE_PRIVATE);
				SharedPreferences.Editor ed = ref.edit();
				ed.putInt(GAME_MODE, MODE_SINGLE);
				ed.commit();
				Intent intt = new Intent(me, NewGameActivity.class);
				startActivity(intt);
			}
		});
        
        multiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences ref = getSharedPreferences(PREF_TAG, MODE_PRIVATE);
				SharedPreferences.Editor ed = ref.edit();
				ed.putInt(GAME_MODE, MODE_MULTY);
				ed.commit();
				Intent intt = new Intent(me, ConnectionActivity.class);
				startActivity(intt);
			}
		});
        
        manageBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intt = new Intent(me, ManageProfile.class);
				startActivity(intt);
			}
		});
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	Cursor cur = profileDb.query();
    	SharedPreferences ref = getSharedPreferences(PREF_TAG, MODE_PRIVATE);
		if(cur.getCount() == 0){
			SharedPreferences.Editor ed = ref.edit();
			ed.putString(MainMenu.PROFILE_ID, null);
			ed.putBoolean(MainMenu.HAVE_ACTIVE_PROFILE, false);
			ed.commit();
		}
    	
    	if(ref.getBoolean(HAVE_ACTIVE_PROFILE, false)==false) {
    	   	Intent intt = new Intent(me, ManageProfile.class);
    	   	startActivity(intt);
    	}
    	else {
    		Cursor cursor = profileDb.queryById(ref.getString(PROFILE_ID, null));
    		cursor.moveToFirst();
    		profileName.setText("Hello, " + cursor.getString(cursor.getColumnIndex(Profile.NAME)));
    	}
    }
}
