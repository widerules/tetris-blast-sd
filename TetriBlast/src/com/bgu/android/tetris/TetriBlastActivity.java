package com.bgu.android.tetris;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TetriBlastActivity extends Activity {
    
	private MainMap mMainMapView;
	public static final String TAG = "TetrisBlast";
    
    private static String ICICLE_KEY = "tetris-blast-view";
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "Create main layout");
        getWindow().setBackgroundDrawableResource(R.drawable.bg);//Draw background
        mMainMapView = (MainMap) findViewById(R.id.tetris);
        mMainMapView.initNewGame();
        //TextView myText = (TextView) findViewById(R.id.txt);
        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
            //mSnakeView.setMode(TetrisView.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                //mSnakeView.restoreState(map);
            } else {
                //mSnakeView.setMode(TetrisView.PAUSE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        //mSnakeView.setMode(TetrisView.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store the game state
        //outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }
}