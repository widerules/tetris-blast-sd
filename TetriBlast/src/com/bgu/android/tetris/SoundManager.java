package com.bgu.android.tetris;

import java.util.HashMap;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
	public static final int MAX_STREAMS = 5;
	
	public static final int SOUND_TURN = 1;
	public static final int SOUND_LINE_BREAK = 2;
	public static final int SOUND_FAST_DROP = 3;
	public static final int SOUND_BG_MUSIC = 4;
	//public static final int SOUND_DROP = 5;
	
	private boolean mSoundEffectsEn;
	private boolean mMusicEn;
	private static SoundManager instance = null;
	private static SoundPool mSoundPool; 
	private static HashMap<Integer, Integer> mSoundPoolMap; 
	private static AudioManager  mAudioManager;
	private static Context mContext;
	
	/**
	 * Private constructor of singleton class
	 * @param context
	 */
	private SoundManager(Context context){
		mContext = context;
		mSoundEffectsEn = true;//default value
		mMusicEn = true;
		mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		mSoundPoolMap = new HashMap<Integer, Integer>(); 
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		loadSounds();
	}

	public static SoundManager getInstance(Context context) {
		if (instance == null)
			instance = new SoundManager(context);
		return instance;
	}
	
//	/**
//	 * Add a new Sound to the SoundPool
//	 * 
//	 * @param Index - The Sound Index for Retrieval
//	 * @param SoundID - The Android ID for the Sound asset.
//	 */
//	public static void addSound(int Index,int SoundID)
//	{
//		mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, 1));
//	}
	
	public void updateSoundEn (boolean isEnabled) {
		mSoundEffectsEn = isEnabled;
	}
	
	public void updateMusicEn (boolean isEnabled) {
		mMusicEn = isEnabled;
	}
	
	/**
	 * Loads the various sound assets
	 * Currently hardcoded but could easily be changed to be flexible.
	 */
	private static void loadSounds()
	{
		mSoundPoolMap.put(SOUND_TURN, mSoundPool.load(mContext, R.raw.turn, 1));
		mSoundPoolMap.put(SOUND_LINE_BREAK, mSoundPool.load(mContext, R.raw.linebreak, 1));
		mSoundPoolMap.put(SOUND_FAST_DROP, mSoundPool.load(mContext, R.raw.fastdrop, 1));
		mSoundPoolMap.put(SOUND_BG_MUSIC, mSoundPool.load(mContext, R.raw.bgmusic, 1));
	}
	
	/**
	 * Plays a Sound
	 * 
	 * @param index - The Index of the Sound to be played
	 * @param speed - The Speed to play not, not currently used but included for compatibility
	 */
	public void playSound(int index) 
	{ 		
		if(mSoundEffectsEn)  {    
			float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
			streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, 0, 1);
		}
	}
	
	public void playBgMusic() {
		if(mMusicEn)  {    
			float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
			streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			mSoundPool.play(mSoundPoolMap.get(SOUND_BG_MUSIC), streamVolume, streamVolume, 1, 0, 1);
		}
	}
	/**
	 * Stop a Sound
	 * @param index - index of the sound to be stopped
	 */
	public void stopSound(int index)
	{
		mSoundPool.stop(mSoundPoolMap.get(index));
	}
	
	public static void cleanup()
	{
		mSoundPool.release();
		mSoundPool = null;
	    mSoundPoolMap.clear();
	    mAudioManager.unloadSoundEffects();
	    instance = null; 
	}
	
}
