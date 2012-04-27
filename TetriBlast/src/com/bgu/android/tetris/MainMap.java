package com.bgu.android.tetris;

import java.util.ArrayList;

import android.R.bool;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MainMap extends TileView{
	public static final int	L_TYPE = 0;
	public static final int J_TYPE = 1;
	public static final int	T_TYPE = 2;
	public static final int	Z_TYPE = 3;
	public static final int	S_TYPE = 4;
	public static final int	O_TYPE = 5;
	public static final int	I_TYPE = 6;
	
	public int tempCount;
	/**
	 * mSnakeTrail: a list of Coordinates that make up the snake's body
	 * mAppleList: the secret location of the juicy apples the snake craves.
	 */
	private ArrayList<Point> mTileList = new ArrayList<Point>();
	    
	//private TetrisShape myShape;
	
	/**
	 * Create a simple handler that we can use to cause animation to happen.  We
	 * set ourselves as a target and we can use the sleep()
	 * function to cause an update/invalidate to occur at a later date.
	 */
	private RefreshHandler mRedrawHandler = new RefreshHandler();
	private long mMoveDelay;
	private boolean isReady = false;
	
	private Tetrino tetr;
	private boolean noShape;
	
	//two dimensional array hold the main tetris map 
	private TetrinoMap mapCur = new TetrinoMap();
	private TetrinoMap mapOld = new TetrinoMap();
	private TetrinoMap mapLast = new TetrinoMap();
	   
	private boolean wasMoved;
	private int xInitRaw;
	private int yInitRaw;
	private static final int xMoveSens = 30;
	private static final int rotateSens = 10; 
	private static final int dropSensativity = 100;//~30*3.5
	
	
	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if(isReady) {
				clearTiles();
				updateWalls();
				updateMap();
				mapCur.resetMap();
				mapCur.copyFrom(mapOld);
				moveShape();//TODO insert this function to the Tetrino
				
			}
			mRedrawHandler.sleep(mMoveDelay);
			//MainMap.this.update();
			MainMap.this.invalidate();
			isReady = true;
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};


	/**
	 * Constructs a MainMap View based on inflation from XML
	 * 
	 * @param context
	 * @param attrs
	 */
	public MainMap(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "MainMap constructor");
		initMainMap();
	}

	public MainMap(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d(TAG, "MainMap constructor defStyle");
		initMainMap();
	}

	/**
	 * Initialize MainMap Tail icons from drawable 
	 *
	 */
	private void initMainMap() {
		setFocusable(true);
		Resources r = this.getContext().getResources();
		resetTiles(NUM_OF_TILES+10);//TODO fix this
		loadTile(BLOCK_RED, r.getDrawable(R.drawable.block_red));
		loadTile(BLOCK_BLUE, r.getDrawable(R.drawable.block_blue));
		loadTile(BLOCK_GREEN, r.getDrawable(R.drawable.block_green));
		loadTile(BLOCK_YELLOW, r.getDrawable(R.drawable.block_yelow));
		loadTile(BLOCK_PINK, r.getDrawable(R.drawable.block_pink));
		loadTile(BLOCK_LIGHBLUE, r.getDrawable(R.drawable.block_lightblue));
		loadTile(BLOCK_ORANGE, r.getDrawable(R.drawable.block_orange));
		loadTile(BLOCK_GREY, r.getDrawable(R.drawable.block_grey));
		loadTile(BLOCK_GHOST, r.getDrawable(R.drawable.block_ghost2));
		loadTile(BLOCK_BLOCK, r.getDrawable(R.drawable.block_block));
		loadTile(BLOCK_BG1, r.getDrawable(R.drawable.block_bg1));
		loadTile(BLOCK_BG2, r.getDrawable(R.drawable.block_bg2));

	}
	    

	public void initNewGame() {
		mTileList.clear();
		Log.d(TAG, "game init");
		mMoveDelay = 400;//delay [ms]
		mapCur.resetMap();
		mapOld.resetMap();
		mapLast.resetMap();
		noShape = true;
		tempCount = 0;
		mRedrawHandler.sleep(mMoveDelay);
	}
	
	private Tetrino newTetrino(int type, int x, int y) {
		switch(type){
		case L_TYPE:
			return new LTetrino(x, y);
		case J_TYPE:
			return new JTetrino(x, y);
		case T_TYPE:
			return new TTetrino(x, y);
		case Z_TYPE:
			return new ZTetrino(x, y);
		case S_TYPE:
			return new STetrino(x, y);
		case O_TYPE:
			return new OTetrino(x, y);
		case I_TYPE:
			return new ITetrino(x, y);
		default:
			return new LTetrino(x, y);
				
		}
	}
	
	/**
	 * Given a ArrayList of coordinates, we need to flatten them into an array of
	 * ints before we can stuff them into a map for flattening and storage.
	 * 
	 * @param pointsList : a ArrayList of Coordinate objects
	 * @return : a simple array containing the x/y values of the coordinates
	 * as [x1,y1,x2,y2,x3,y3...]
	 */
	private int[] coordArrayListToArray(ArrayList<Point> pointsList) {
		int count = pointsList.size();
		int[] rawArray = new int[count * 2];
		for (int index = 0; index < count; index++) {
			Point c = pointsList.get(index);
			rawArray[2 * index] = c.x;
			rawArray[2 * index + 1] = c.y;
		}
		return rawArray;
	}

	/**
	 * Save game state so that the user does not lose anything
	 * if the game process is killed while we are in the 
	 * background.
	 * 
	 * @return a Bundle with this view's state
	 */
	public Bundle saveState() {
		Bundle map = new Bundle();

		map.putIntArray("mTileList", coordArrayListToArray(mTileList));
		map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
		return map;
	}

	/**
	 * Given a flattened array of ordinate pairs, we reconstitute them into a
	 * ArrayList of Coordinate objects
	 * 
	 * @param rawArray : [x1,y1,x2,y2,...]
	 * @return a ArrayList of Coordinates
	 */
	private ArrayList<Point> coordArrayToArrayList(int[] rawArray) {
		ArrayList<Point> coordArrayList = new ArrayList<Point>();
		int coordCount = rawArray.length;
		for (int index = 0; index < coordCount; index += 2) {
			Point c = new Point(rawArray[index], rawArray[index + 1]);
			coordArrayList.add(c);
		}
		return coordArrayList;
	}

	/**
	 * Restore game state if our process is being relaunched
	 * 
	 * @param icicle a Bundle containing the game state
	 */
	public void restoreState(Bundle icicle) {
		//setMode(PAUSE);
		mTileList = coordArrayToArrayList(icicle.getIntArray("mTileList"));
		mMoveDelay = icicle.getLong("mMoveDelay");
	}
	    
	/*
	 * touch recognition
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//This prevents touchscreen events from flooding the main thread
		synchronized (event)
		{
			try
			{
				//Waits 16ms.
				event.wait(16);

				//when user touches the screen
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					xInitRaw = (int) Math.floor(event.getRawX());
					yInitRaw = (int) Math.floor(event.getRawY());
					wasMoved = false;
				}

				if(event.getAction() == MotionEvent.ACTION_MOVE) {
					int xCurRaw = (int) Math.floor(event.getRawX());
					if ((xInitRaw - xCurRaw) > xMoveSens) {
						wasMoved = true;
						xInitRaw = xCurRaw;
						mapCur.resetMap();
						mapCur.copyFrom(mapOld);
						if(tetr.moveLeft(mapCur))
							mapCur.putTetrinoOnMap(tetr);
						else
							mapCur.copyFrom(mapLast);
						update();
					}
					else if((xCurRaw - xInitRaw) > xMoveSens) {
						wasMoved = true;
						xInitRaw = xCurRaw;
						mapCur.resetMap();
						mapCur.copyFrom(mapOld);
						if(tetr.moveRight(mapCur))
							mapCur.putTetrinoOnMap(tetr);
						else
							mapCur.copyFrom(mapLast);
						update();
					}
				}
					
				//when screen is released
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					int yCurRaw = (int) Math.floor(event.getRawY());
					if(yCurRaw - yInitRaw > dropSensativity ) {
						mapCur.resetMap();
						mapCur.copyFrom(mapOld);
						if(tetr.drop(mapCur))
							mapCur.putTetrinoOnMap(tetr);
						else
							mapCur.copyFrom(mapLast);
						update();
					}
					//Rotate tetrino (release on same x pos) 
					else if (!wasMoved && (int)Math.abs(yCurRaw - yInitRaw) < rotateSens ) {
						mapCur.resetMap();
						mapCur.copyFrom(mapOld);
						if(tetr.rotateTetrino(mapCur))
							mapCur.putTetrinoOnMap(tetr);
						else
							mapCur.copyFrom(mapLast);
						update();
					}
				}
			}
			catch (InterruptedException e)
			{
				return true;
			}
		}
		return true;
	}
	  
	private void moveShape() {
		if (noShape) {
			noShape = false;
			tetr = newTetrino(tempCount%7, 5, 0);//TODO check this
			tempCount++;
			mapCur.putTetrinoOnMap(tetr);
		}
		else
		{
			if(tetr.moveDown(mapCur)){
				mapCur.putTetrinoOnMap(tetr);
			}
			else {
				
				noShape = true;
				mapCur.copyFrom(mapLast);
				int i = mapCur.lineCheckAndClear();
				Log.d(TAG, "Cleared " + Integer.toString(i) + " lines!");
				mapOld.copyFrom(mapCur);
			}
		}
	}

	/**
	 * Handles the basic update loop, checking to see if we are in the running
	 * state, determining if a move should be made, updating the snake's location.
	 */
	public void update() {
		if(isReady) {
			//clearTiles();
			//updateWalls();
			updateMap();
			//mapCur.resetMap();
			//mapCur.copyFrom(mapOld);
			MainMap.this.invalidate();
			//moveShape();//TODO insert this function to the Tetrino
			
		}
	}
		
	private void updateMap() {
		mapLast.copyFrom(mapCur);
		for(int col = 0; col < TetrinoMap.MAP_X_SIZE; col++){
			for(int row = 0; row < TetrinoMap.MAP_Y_SIZE; row++) {
				setTile(mapCur.getMapValue(col, row), 1+col, 1+row);
			}
		}
		
	}

	/**
	 * Draws some walls.
	 * 
	 */
	private void updateWalls() {
		for (int x = 0; x < mXTileCount; x++) {
			setTile(BLOCK_GREY, x, 0);
			setTile(BLOCK_GREY, x, mYTileCount - 1);
		}
		for (int y = 1; y < mYTileCount - 1; y++) {
			setTile(BLOCK_GREY, 0, y);
			setTile(BLOCK_GREY, mXTileCount - 1, y);
		}
	}
	    
}
