package com.bgu.android.tetris;

import java.util.ArrayList;
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
	    
	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 */
	private static final int BLOCK_RED = 1;
	private static final int BLOCK_BLUE = 2;
	private static final int BLOCK_GREEN = 3;
	private static final int BLOCK_YELLOW = 4;
	private static final int BLOCK_PINK = 5;
	private static final int BLOCK_LIGHBLUE = 6;
	private static final int BLOCK_ORANGE = 7;
	private static final int BLOCK_GREY = 8;
	private static final int BLOCK_GHOST = 9;
	private static final int BLOCK_BLOCK = 10;
	private static final int BLOCK_BG1 = 11;
	private static final int BLOCK_BG2 = 12;
	private static final int NUM_OF_TILES = 12;

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
	
	private int[][] tetrino = new int[3][3];
	    
//	    /**
//	     * 
//	     * @author Denis
//	     * Coordinates For the touch recognition
//	     */
//	    private float initialX = 0;
//		private float initialY = 0;
//		private float deltaX = 0;
//		private float deltaY = 0;
	    
	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			MainMap.this.update();
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
		resetTiles(NUM_OF_TILES+10);
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
		mMoveDelay = 500;//delay [ms]
		tetrino[0][0] = BLOCK_PINK;
		tetrino[0][1] = BLOCK_PINK;
		tetrino[0][2] = 0;
		tetrino[1][0] = 0;
		tetrino[1][1] = BLOCK_PINK;
		tetrino[1][2] = BLOCK_PINK;
		tetrino[2][0] = 0;
		tetrino[2][1] = 0;
		tetrino[2][2] = 0;
		update();
	}
	
	private void putTetrinoOnMap(int x, int y) {
		for(int col = 0; col < 3; col++){
			for(int row = 0; row < 3; row++) {
				setTile(tetrino[col][row], x+row, y+col);
			}
		}
		
	}
	
	private void rotateTetrino() {
		int[][] temp = new int[3][3];
		for(int col = 0; col < 3; col++){
			for(int row = 0; row < 3; row++) {
				temp[col][row] = tetrino[row][2-col];
			}
		}
		tetrino = temp;
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
		//			//This prevents touchscreen events from flooding the main thread
		//			synchronized (event)
		//			{
		//				try
		//				{
		//					//Waits 16ms.
		//					event.wait(16);
		//
		//					//when user touches the screen
		//					if(event.getAction() == MotionEvent.ACTION_DOWN)
		//					{
		//						//reset deltaX and deltaY
		//						deltaX = deltaY = 0;
		//
		//						//get initial positions
		//						initialX = event.getRawX();
		//						initialY = event.getRawY();
		//					}
		//
		//					//when screen is released
		//					if(event.getAction() == MotionEvent.ACTION_UP)
		//					{
		//						deltaX = event.getRawX() - initialX;
		//						deltaY = event.getRawY() - initialY;
		//
		//						if(Math.abs(deltaX) >= Math.abs(deltaY))
		//						{
		//							if(deltaX < 0)
		//							{
		//								//make your object/character move left
		//								myShape.moveLeft();
		//								if (mDirection != EAST) {
		//					                mNextDirection = WEST;
		//					            }
		//					            return (true);
		//							}
		//							else
		//							{
		//								//make your object/character move right
		//								myShape.moveRight();
		//								if (mDirection != WEST) {
		//					                mNextDirection = EAST;
		//					            }
		//					            return (true);
		//							}
		//						}
		//						else
		//						{
		//							if(deltaY < 0)
		//							{
		//								//make your object/character move up
		//								if (mMode == READY | mMode == LOSE) {
		//					                /*
		//					                 * At the beginning of the game, or the end of a previous one,
		//					                 * we should start a new game.
		//					                 */
		//					                initNewGame();
		//					                setMode(RUNNING);
		//					                update();
		//					                return (true);
		//					            }
		//
		//					            if (mMode == PAUSE) {
		//					                /*
		//					                 * If the game is merely paused, we should just continue where
		//					                 * we left off.
		//					                 */
		//					                setMode(RUNNING);
		//					                update();
		//					                return (true);
		//					            }
		//					            if (mMode == RUNNING) {
		//					                if(myShape instanceof ShapeT)
		//					                	((ShapeT)myShape).rotate();
		//					            }
		//					            return (true);
		//							}
		//							else
		//							{
		//								
		//								if (mDirection != NORTH) {
		//					                mNextDirection = SOUTH;
		//					            }
		//					            return (true);
		//					            //make your object/character move down							
		//							}
		//						}
		//					
		//					}
		//				}
		//
		//				catch (InterruptedException e)
		//				{
		//					return true;
		//				}
		//			}
		return true;
	}
	  
	private void addShape() {
		//myShape = new ShapeT();
		//TODO add some shape
		putTetrinoOnMap(5, 2);
		//Log.d(TAG, "shape created");
		//setTile(BLOCK_GREY, 2,2);
	}

//	    private void updateShape()
//	    {
//	    	if (myShape != null) {
//	    		Vector<Coordinate> toRemove = new Vector<Coordinate>();
//	    		for (Coordinate aCoord: mAppleList) {
//	    			for(Coordinate c : myShape.getCoordinate()) {
//	    				if (aCoord.equals(c)) {
//	    					//mAppleList.remove(c);
//	    					toRemove.add(c);
//	    				}
//	    			}
//	    		}
//	    		mAppleList.removeAll(toRemove);
//
//	    		myShape.dropDown();
//
//	    		for(Coordinate c : myShape.getCoordinate()) {
//	    			mAppleList.add(c);
//	    		}
//	    		Log.d(TAG, "List Size" + mAppleList.size());
//	    		for (Coordinate c : mAppleList) {
//	    			setTile(YELLOW_STAR, c.getX(), c.getY());
//	    		}
//	    	}
//	    }

	/**
	 * Handles the basic update loop, checking to see if we are in the running
	 * state, determining if a move should be made, updating the snake's location.
	 */
	public void update() {
		if(isReady) {
			clearTiles();
			updateWalls();
			addShape();
			rotateTetrino();
		}
		mRedrawHandler.sleep(mMoveDelay);	
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
