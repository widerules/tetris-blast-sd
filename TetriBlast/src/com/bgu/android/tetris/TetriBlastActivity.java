package com.bgu.android.tetris;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TetriBlastActivity extends Activity {
	private static String ICICLE_KEY = "tetris-blast-view";
	public TetriBlastActivity me = this;
	public static final String TAG = "TetrisBlast";
	
	private MainMap mMainMapView;
	private TextView mComboView;
	private TextView mLinesToSendView;
	private TextView mLineSent;
	private TextView mScoreView;
	private RelativeLayout mNameLine;
	
    public static final int MSG_LINES_CLEARED = 1;
    public static final int MSG_END_GAME = 2;
        
    public static final int GAME_STATUS_PROGRESS = 0;
    public static final int GAME_STATUS_WIN = 1;
    public static final int GAME_STATUS_LOSS = 2;
    
    public static final int MAX_LINES_TO_SEND = 7;
    
    private SharedPreferences ref;
    private BluetoothConnectivity mBluetoothCon = BluetoothConnectivity.getInstance(this);
    private Profile profileDb = Profile.getInstance(this);
    
    private int mGameMode;		//The mode of the game (vs, coop, single @ MainMenu.MODE_*)
    private long mCurrentScore;	//Curent score of the player
    private int linesToSend;	//Num of lines to send via bluetooth
    private int mTotalLinesSent;//Total lines sent
    private int mCombo;			//Combo score multiplier
    private int mLinesToIncrease;//Num of score to increase
    
    // The Handler that gets information back from the BluetoothConnectivity
    private final Handler mBtHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothConnectivity.MESSAGE_WRITE:
            	//TODO update name of opponet here
            	break;
            }
        }
    };
    
    // The Handler that gets information back from the MainMap class
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_LINES_CLEARED:
                Log.i(TAG, "TetrisBlastActivity msg: " + msg.what + "Lines Cleared: " + msg.arg1);
                increaseScore(msg.arg1);
                if (mGameMode == MainMenu.MODE_VS || mGameMode == MainMenu.MODE_COOP)
                	increaseLinesToSend(msg.arg1);
                incraseLineToIncrease();
                break;
            case MSG_END_GAME:
            	Log.i(TAG, "GAME OVER!");
            	break;
           

            }
        }

		private void incraseLineToIncrease() {
			if (mLinesToIncrease!=0)
				mMainMapView.increaseLines(mLinesToIncrease);
			mLinesToIncrease=0;
			
		}

		
    };
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "Create main layout");
        //getWindow().setBackgroundDrawableResource(R.drawable.tetris_bg2);//Draw background
        mMainMapView = (MainMap) findViewById(R.id.tetris);
        mMainMapView.initNewGame();
        mMainMapView.setActivityHandler(mHandler);
        mComboView = (TextView)findViewById(R.id.main_combo);
        mScoreView = (TextView)findViewById(R.id.main_score);
        mLinesToSendView = (TextView)findViewById(R.id.main_lines_to_send);
        mLineSent = (TextView)findViewById(R.id.main_lines_sent);
        mNameLine = (RelativeLayout)findViewById(R.id.main_name_line);
        
        ref = getSharedPreferences(MainMenu.PREF_TAG, MODE_PRIVATE);
        mGameMode = ref.getInt(MainMenu.GAME_MODE, MainMenu.MODE_UNDEFINED);
        if (mGameMode == MainMenu.MODE_SINGLE) {
        	mNameLine.setVisibility(View.GONE);
        }
        else {
        	Cursor cursor = profileDb.queryById(ref.getString(MainMenu.PROFILE_ID, null));
    		cursor.moveToFirst();
    		String name = cursor.getString(cursor.getColumnIndex(Profile.NAME));
        	mBluetoothCon.write(BluetoothConnectivity.TYPE_NAME, name.getBytes());
        }
        mCurrentScore = 0;
        linesToSend = 0;
        mCombo = 1;
        mTotalLinesSent = 0;
        mLinesToIncrease = 2;
        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
        	mMainMapView.setMode(MainMap.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
            	mMainMapView.restoreState(map);
            } else {
            	mMainMapView.setMode(MainMap.PAUSE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        mMainMapView.setMode(MainMap.PAUSE);
    }
    
    @Override
    protected void onStop() {
        super.onPause();
        // Pause the game along with the activity
        mMainMapView.setMode(MainMap.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store the game state
        outState.putBundle(ICICLE_KEY, mMainMapView.saveState());
    }
    
    private void increaseScore(int linesCleared) {
    	if (linesCleared == 0) {
    		mCombo = 1;
    	}
    	else {
    		mCurrentScore += linesCleared*100*mCombo;
    		mCombo++;//TODO think about combo formula!!!!
    	}
    	
    	if(mCombo == 1) 
    		mComboView.setText("x1");
    	else
    		mComboView.setText("x" + Integer.toString(mCombo - 1));
    	mScoreView.setText(Long.toString(mCurrentScore));
    	if (mGameMode ==  MainMenu.MODE_COOP) {
    		//send(mScore)//TODO via Bluetooth
    	}
    }
    
    private void increaseLinesToSend(int linesCleared) {
    	if (linesCleared == 0) {
    		//send(linesToSend)//TODO send via Bluetooth
    		mTotalLinesSent += linesToSend;
    		linesToSend = 0;
    		
    	}
    	else {
    		int temp = (linesCleared-1) + linesToSend;
    		if (temp > MAX_LINES_TO_SEND) {
    			//send(MAX_LINES)//TODO send via Bluetooth
    			linesToSend = temp - MAX_LINES_TO_SEND;
    			mTotalLinesSent += MAX_LINES_TO_SEND;
    		}
    		else {
    			linesToSend += (linesCleared - 1);
    		}
    	}
		mLinesToSendView.setText(Integer.toString(linesToSend));	
		mLineSent.setText(Integer.toString(mTotalLinesSent));
    	
		
	}
}