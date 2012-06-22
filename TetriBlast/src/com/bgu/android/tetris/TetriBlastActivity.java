package com.bgu.android.tetris;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TetriBlastActivity extends Activity {
	private static String ICICLE_KEY = "tetris-blast-view";
	public TetriBlastActivity me = this;
	public static final String TAG = "TetrisBlast";
	
	private MapView mMapView;
	private MainMap mMainMap;
	private TextView mComboView;
	private TextView mLinesToSendView;
	private TextView mLineSent;
	private TextView mScoreView;
	private TextView mMyName;
	private TextView mOpponentName;
	private RelativeLayout mNameLine;
	private ImageView mNextPic;
	
    public static final int MSG_LINES_CLEARED = 1;
    public static final int MSG_END_GAME = 2;
    public static final int MSG_NEXT_PIC = 3;
        
    public static final int GAME_STATUS_PROGRESS = 0;
    public static final int GAME_STATUS_WIN = 1;
    public static final int GAME_STATUS_LOSS = 2;
    
    public static final int MAX_LINES_TO_SEND = 7;
    
    private SharedPreferences ref;
    private BluetoothConnectivity mBluetoothCon;
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
            	switch (msg.arg2) {
            	case BluetoothConnectivity.TYPE_NAME:
            		String opName = new String((byte[])msg.obj);
            		mOpponentName.setText(opName);
            	}
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
            case MSG_NEXT_PIC:
            	setNextPic(msg.arg1);
            	break;
            }
        }

		private void incraseLineToIncrease() {
			if (mLinesToIncrease!=0)
				mMainMap.increaseLines(mLinesToIncrease);
			mLinesToIncrease=0;	
		}		
    };
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "Create main layout");
        mBluetoothCon = BluetoothConnectivity.getInstance(this);
        mBluetoothCon.setHandler(mBtHandler);
        mMapView = (MapView)findViewById(R.id.tetris);
        mMapView.initTilePatern(R.drawable.blocks_patern2);//TODO use from shared settings
        mMainMap = new MainMap(this, mMapView);
        mMainMap.initNewGame();
        mMainMap.setActivityHandler(mHandler);
        mComboView = (TextView)findViewById(R.id.main_combo);
        mScoreView = (TextView)findViewById(R.id.main_score);
        mMyName = (TextView)findViewById(R.id.main_my_name);
        mOpponentName = (TextView)findViewById(R.id.main_opponent_name);
        mLinesToSendView = (TextView)findViewById(R.id.main_lines_to_send);
        mLineSent = (TextView)findViewById(R.id.main_lines_sent);
        mNameLine = (RelativeLayout)findViewById(R.id.main_name_line);
        mNextPic = (ImageView)findViewById(R.id.next_pic);
        
        ref = getSharedPreferences(MainMenu.PREF_TAG, MODE_PRIVATE);
        mMainMap.setDifficulty(ref.getInt(NewGameActivity.DIFFICULTY, 0));
        Tetrino.ghostEnabled = ref.getBoolean(NewGameActivity.SHADOW, false);
        
        setNextPic(0);
        
        mGameMode = ref.getInt(MainMenu.GAME_MODE, MainMenu.MODE_UNDEFINED);
        if (mGameMode == MainMenu.MODE_SINGLE) {
        	mNameLine.setVisibility(View.GONE);
        }
        else {
        	Cursor cursor = profileDb.queryById(ref.getString(MainMenu.PROFILE_ID, null));
    		cursor.moveToFirst();
    		String name = cursor.getString(cursor.getColumnIndex(Profile.NAME));
    		mMyName.setText(name);
        	mBluetoothCon.write(BluetoothConnectivity.TYPE_NAME, name.getBytes());
        }
        mCurrentScore = 0;
        linesToSend = 0;
        mCombo = 1;
        mTotalLinesSent = 0;
        mLinesToIncrease = 2;
        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
        	mMainMap.setMode(MainMap.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
            	mMainMap.restoreState(map);
            } else {
            	mMainMap.setMode(MainMap.PAUSE);
            }
        }
        
        mMapView.setOnTouchListener(mMainMap.mTouchListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        mMainMap.setMode(MainMap.PAUSE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Pause the game along with the activity
        mMapView.resume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Stop the game along with the activity
        mMainMap.setMode(MainMap.PAUSE);
        mMapView.pause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store the game state
        outState.putBundle(ICICLE_KEY, mMainMap.saveState());
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
    
    private void setNextPic (int pic) {
    	switch (pic){
    	case MainMap.I_TYPE:
    		mNextPic.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.next_i)));
    		break;
    	case MainMap.J_TYPE:
    		mNextPic.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.next_j)));
    		break;
    	case MainMap.O_TYPE:
    		mNextPic.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.next_o)));
    		break;
    	case MainMap.L_TYPE:
    		mNextPic.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.next_l)));
    		break;
    	case MainMap.S_TYPE:
    		mNextPic.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.next_s)));
    		break;
    	case MainMap.T_TYPE:
    		mNextPic.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.next_t)));
    		break;
    	case MainMap.Z_TYPE:
    		mNextPic.setImageBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.next_z)));
    		break;
    	}
    	
    }
}