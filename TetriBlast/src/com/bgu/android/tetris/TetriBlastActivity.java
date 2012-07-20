package com.bgu.android.tetris;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
	public static final int DIALOG_PAUSE = 0;
	public static final int DIALOG_GAME_OVER = 1;
	
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
    public static final int MSG_UPDATE = 4;
    public static final int MSG_PAUSE = 5;
    public static final int MSG_UNPAUSE = 6;
        
    public static final int GAME_STATUS_PROGRESS = 0;
    public static final int GAME_STATUS_WIN = 1;
    public static final int GAME_STATUS_LOSS = 2;
    
    public static final int MAX_LINES_TO_SEND = 7;
    
    private SharedPreferences ref;
    private BluetoothConnectivity mBluetoothCon;
    private Profile profileDb = Profile.getInstance(this);
    private Dialog mDialog;
   
    private int mGameMode;		//The mode of the game (vs, coop, single @ MainMenu.MODE_*)
    private long mCurrentScore;	//Curent score of the player
    private long mOppScore;		//Received Opponent Score via bluetooth
    private boolean mOppLost;	//Received opponent win or loos//TODO think if need this
    private int linesToSend;	//Num of lines to send via bluetooth
    private int mTotalLinesSent;//Total lines sent
    private int mCombo;			//Combo score multiplier
    private int mLinesToIncrease;//Num of score to increase
    private int mGameState = MainMap.PAUSE;//State of the game (PAUSE or READY final on MainMap)
    
    private int mDifficulty = 0;
    private boolean mGhostEn = false;
    private boolean mIamHost = false;
    
    // The Handler that gets information back from the BluetoothConnectivity
    private final Handler mBtHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothConnectivity.MESSAGE_READ:
            	switch (msg.arg2) {
            	case BluetoothConnectivity.TYPE_NAME:
            		String opName = new String((byte[])msg.obj);
            		mOpponentName.setText(opName);
            		break;
            	case BluetoothConnectivity.TYPE_DIFFICULTY:
            		String diffSt = new String((byte[])msg.obj);
            		int tempDiff = Integer.parseInt(diffSt);
            		//Log.i(MainMenu.TAG, "BT Received diff: " + tempDiff + " lenght: " + msg.arg1);
            		mDifficulty = tempDiff;
            		mMainMap.setDifficulty(mDifficulty);
            		break;
            	case BluetoothConnectivity.TYPE_SHADOW:
            		String shadowSt = new String((byte[])msg.obj);
            		boolean tempGh = Boolean.parseBoolean(shadowSt);
            		//Log.i(MainMenu.TAG, "BT Received ghost: " + tempGh + " lenght: " + msg.arg1);
            		mGhostEn = tempGh;
            		Tetrino.ghostEnabled = mGhostEn;
            		break;
            	case BluetoothConnectivity.TYPE_UNPAUSE:
            		if(mDialog != null && mDialog.isShowing())
            			mDialog.cancel();
            		me.mHandler.sendEmptyMessage(MSG_UNPAUSE);
            		//Log.i(MainMenu.TAG, "Sent unpause hendler message");
            		break;
            	case BluetoothConnectivity.TYPE_PAUSE:
            		me.mHandler.sendEmptyMessage(MSG_PAUSE);
            		showDialog(DIALOG_PAUSE);
            		//Log.i(MainMenu.TAG, "Sent pause hendler message");
            		break;
            	case BluetoothConnectivity.TYPE_LINES://received lines to increase
            		String stLines = new String((byte[])msg.obj);
            		int lines = Integer.parseInt(stLines);
            		Log.i(MainMenu.TAG, "Received lines to increase: " + lines);
            		mLinesToIncrease = lines;
            		break;
            	case BluetoothConnectivity.TYPE_SCORE:
            		String stScore = new String((byte[])msg.obj);
            		long score = Long.parseLong(stScore);
            		Log.i(MainMenu.TAG, "Received opponent Score: " + score);
            		mOppScore = score;
            		break;
            	case BluetoothConnectivity.TYPE_LOSS:
            		//String isWinSt = new String((byte[])msg.obj);
            		//boolean isWin = Boolean.parseBoolean(isWinSt);
            		Log.i(MainMenu.TAG, "BT Received opponent lost: ");
            		mOppLost = true;
            		showDialog(DIALOG_GAME_OVER);     		
            		break;
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
                if (mGameMode != MainMenu.MODE_SINGLE) {//(mGameMode == MainMenu.MODE_VS || mGameMode == MainMenu.MODE_COOP)
                	increaseLinesToSend(msg.arg1);
                	incraseLineToIncrease();
                }
                break;
            case MSG_END_GAME:
            	Log.i(TAG, "TetrisBlastActivity: GAME OVER received");
            	mGameState = MainMap.PAUSE;
            	mMainMap.setMode(mGameState);
            	if(mGameMode != MainMenu.MODE_SINGLE) {
            		String myScore = Long.toString(mCurrentScore);
            		mBluetoothCon.write(BluetoothConnectivity.TYPE_SCORE, myScore.getBytes());
            		mBluetoothCon.write(BluetoothConnectivity.TYPE_LOSS, null);
            	}
            	showDialog(DIALOG_GAME_OVER);
            	break;
            case MSG_NEXT_PIC:
            	setNextPic(msg.arg1);
            	break;
            case MSG_UPDATE://update name, difficult and shadow
            	String myName = mMyName.getText().toString();
            	String myDiff = Integer.toString(mDifficulty);
            	String myShadow = Boolean.toString(mGhostEn);
            	mBluetoothCon.write(BluetoothConnectivity.TYPE_NAME, myName.getBytes());
            	Log.i(MainMenu.TAG, "Sent via BT name: " + myName);
            	if(mIamHost) {
            		mBluetoothCon.write(BluetoothConnectivity.TYPE_DIFFICULTY, myDiff.getBytes());
                	Log.i(MainMenu.TAG, "Sent via BT difficult: " + myDiff);
                	mBluetoothCon.write(BluetoothConnectivity.TYPE_SHADOW, myShadow.getBytes());
                	Log.i(MainMenu.TAG, "Sent via BT shadow: " + myShadow);
            	}
            	mBluetoothCon.write(BluetoothConnectivity.TYPE_UNPAUSE, null);//unpause and start the game
            	Log.i(MainMenu.TAG, "Sent TYPE_UNPAUSE via BT name: ");
            	break;
            case MSG_PAUSE:
            	mGameState = MainMap.PAUSE;
            	mMainMap.setMode(mGameState);
            	Log.i(MainMenu.TAG,"MSG_PAUSE");
            	break;
            case MSG_UNPAUSE:
            	if(mGameState == MainMap.PAUSE){
            		mGameState = MainMap.READY;
            		mMainMap.setMode(mGameState);
            		Log.i(MainMenu.TAG,"MSG_UNPAUSE");
            	}
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
        mDifficulty = ref.getInt(NewGameActivity.DIFFICULTY, 0);
        mMainMap.setDifficulty(mDifficulty);
        mGhostEn = ref.getBoolean(NewGameActivity.SHADOW, false);
        Tetrino.ghostEnabled = mGhostEn;
        
        mIamHost = ref.getBoolean(NewGameActivity.HOST, false);
        SharedPreferences.Editor ed = ref.edit();
		ed.putBoolean(NewGameActivity.HOST, false);
		ed.commit();
        
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
    		mHandler.sendEmptyMessageDelayed(MSG_UPDATE, 500);
        	//mBluetoothCon.write(BluetoothConnectivity.TYPE_NAME, name.getBytes());
        }
        mCurrentScore = 0;
        mOppScore = 0;
        mOppLost = false;
        linesToSend = 0;
        mCombo = 1;
        mTotalLinesSent = 0;
        mLinesToIncrease = 0;
        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
        	if(mGameMode == MainMenu.MODE_SINGLE) {
        		mMainMap.setMode(MainMap.READY);
        	}else
        		mHandler.sendEmptyMessage(MSG_PAUSE);
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
        //mMainMap.setMode(MainMap.PAUSE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Pause the game along with the activity
        //mMainMap.setMode(mGameState);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Stop the game along with the activity
        mMainMap.setMode(MainMap.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store the game state
        outState.putBundle(ICICLE_KEY, mMainMap.saveState());
    }
    
    @Override
    public void onBackPressed() {
    	mHandler.sendEmptyMessage(MSG_PAUSE);
    	if(mGameMode != MainMenu.MODE_SINGLE) 
    		mBluetoothCon.write(BluetoothConnectivity.TYPE_PAUSE, null);
    	showDialog(DIALOG_PAUSE);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	mDialog = null;
    	switch(id) {
    	case DIALOG_PAUSE:
    		mDialog = createPauseDialog();
    		break;
    	case DIALOG_GAME_OVER:
    		mDialog = createGameOverDialog();
    		break;
    	default:
    		mDialog = null;
    	}
    	return mDialog;
    }
    
    private Dialog createPauseDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Game Paused");
    	builder.setMessage("You can exit or resume the game");
    	builder.setCancelable(false);
    	builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				me.finish();
			}
		})
		.setNegativeButton("Resume", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mGameMode != MainMenu.MODE_SINGLE)
					mBluetoothCon.write(BluetoothConnectivity.TYPE_UNPAUSE, null);
				me.mHandler.sendEmptyMessage(MSG_UNPAUSE);
				dialog.cancel();
			}
		});
    	AlertDialog dialog = builder.create();
    	return dialog;

    }
    
    private Dialog createGameOverDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Game Over");
    	if(mGameMode == MainMenu.MODE_SINGLE) {
    		builder.setMessage("Score: " + mCurrentScore);
    	}
    	else {//TODO separate between Coop/VS
    		String msg;
    		if(mOppLost) {
    			msg = "You WIN! Score: " + Long.toString(mCurrentScore);
    		} else {
    			msg = "You LOST! Score: " + Long.toString(mCurrentScore);
    		}
//    		if(mCurrentScore == mOppScore)
//    			msg = "Draw! Score: " + Long.toString(mCurrentScore);
//    		else if(mCurrentScore > mOppScore)
//    			msg = "You WIN! Score: " + Long.toString(mCurrentScore);
//    		else
//    			msg = "You LOST! Score: " + Long.toString(mCurrentScore);
    		builder.setMessage(msg);
    	}
    	builder.setCancelable(false);
    	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				//updateScore(mCurrentScore, mode);//TODO implement
				if(mGameMode == MainMenu.MODE_SINGLE) {
					me.finish();
				} else {
					//TODO implement this for multiplayer mode
					me.finish();
				}
				
			}
		});
		AlertDialog dialog = builder.create();
    	return dialog;
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
    		if(linesToSend != 0){
    			String stLines = Integer.toString(linesToSend);
    			Log.i(MainMenu.TAG, "Line sent via BT: " + stLines);
    			mBluetoothCon.write(BluetoothConnectivity.TYPE_LINES, stLines.getBytes());//send(linesToSend)//send via Bluetooth
    			mTotalLinesSent += linesToSend;
    			//TODO update screen lines sent
    			linesToSend = 0;	
    		}
    	}
    	else {
    		int temp = (linesCleared-1) + linesToSend;
    		if (temp > MAX_LINES_TO_SEND) {
    			String stLines = Integer.toString(MAX_LINES_TO_SEND);
        		mBluetoothCon.write(BluetoothConnectivity.TYPE_LINES, stLines.getBytes());//send(MAX_LINES)//send via Bluetooth
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