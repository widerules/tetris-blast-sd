package com.bgu.android.tetris;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity{
	public static final String OPT_MUSIC_ON = "music_on";
	public static final String OPT_SOUND_ON = "sound_on";
	public static final String OPT_SENS_LIST = "sens_list";
	public static final String OPT_PATTERN = "pattern";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	public static boolean getSoundEn(Context context) {
		Boolean res = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_SOUND_ON, true);
		return res;
	}
	
	public static boolean getMusicEn(Context context) {
		Boolean res = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_MUSIC_ON, true);
		return res;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		SoundManager.getInstance(this).updateMusicEn(getMusicEn(this));
		SoundManager.getInstance(this).updateSoundEn(getSoundEn(this));		
	}
	
	
}
