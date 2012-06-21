package com.bgu.android.tetris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

public class MapView extends SurfaceView implements Runnable {

	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 */
	public static final int BLOCK_EMPTY = 0;
	public static final int BLOCK_RED = 1;
	public static final int BLOCK_BLUE = 2;
	public static final int BLOCK_GREEN = 3;
	public static final int BLOCK_YELLOW = 4;
	public static final int BLOCK_PINK = 5;
	public static final int BLOCK_LIGHBLUE = 6;
	public static final int BLOCK_ORANGE = 7;
	public static final int BLOCK_GREY = 8;
	public static final int BLOCK_GHOST = 9;
	public static final int BLOCK_BLOCK = 10;
	public static final int BLOCK_BG1 = 11;
	public static final int BLOCK_BG2 = 12;
		
	public static final int NUM_OF_TILES_IN_PATERN = 9;
	public static final int MAP_COLS = 10;
	public static final int MAP_ROWS = 20;
	
	public Thread mViewThread = null;
	public SurfaceHolder mHolder;
	boolean isOk = false;
	boolean needToUpdate = true;
	
	Bitmap[] tiles;
	int[][] tileMap;
	int paternHeight;
	int paternWidth;
	
	int curTile = 0;
	
	
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHolder = getHolder();
		tiles = new Bitmap[NUM_OF_TILES_IN_PATERN+1];//+1 for empty tile on index 0
		tileMap = new int[MAP_COLS][MAP_ROWS];
		initTilePatern(R.drawable.blocks_patern);//default block pattern
		//this.setLayoutParams(new LinearLayout.LayoutParams(400, 200));
	}

	public void initTilePatern(int drawbleId) {
		Bitmap tilePatern = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), drawbleId));
		paternWidth = tilePatern.getWidth();
		paternHeight = tilePatern.getHeight();
		for(int i = 0; i < NUM_OF_TILES_IN_PATERN; i++) {
			tiles[i+1] = Bitmap.createBitmap(tilePatern, i*paternHeight,0,paternWidth/NUM_OF_TILES_IN_PATERN,paternHeight);
		}
	}
	
	
	@Override
	public void run() {
		while (isOk) {
			if(!mHolder.getSurface().isValid()) {
				continue;//go back to while and check again
			}
			if (needToUpdate) {
				Canvas c = mHolder.lockCanvas();
				drawOnCanvas(c);
				mHolder.unlockCanvasAndPost(c);
				needToUpdate = false;
			}
		}
	}
	
	private void drawOnCanvas(Canvas c) {
		int tileSize = c.getWidth()/MAP_COLS;
		c.drawARGB(255, 0, 0, 0);//Black background
		Rect src = new Rect(0,0,paternHeight,paternHeight);
		Rect dst = new Rect(0 , 0, tileSize, tileSize);
		for(int col = 0; col < MAP_COLS; col++) {
			for (int row = 0; row < MAP_ROWS; row++) {
				int tileToDraw = tileMap[col][row];
				if (tileToDraw > 0) {
					dst.set(col*tileSize, row*tileSize, (col+1)*tileSize, (row+1)*tileSize);
					c.drawBitmap(tiles[tileToDraw], src, dst, null);
				}
			}
		}
	}
	
	public void update(int[][] map) {		
		for(int col = 0; col < MAP_COLS; col++) {
			for (int row = 0; row < MAP_ROWS; row++) {
				tileMap[col][row] = map[col][row];
			}
		}
		needToUpdate = true;
	}
	
	public void pause() {
		isOk = false;
		while(mViewThread.isAlive() == true) {
			try {
				mViewThread.join();
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
		mViewThread = null;
	}
	
	public void resume() {
		isOk = true;
		needToUpdate = true;
		mViewThread = new Thread(this);
		mViewThread.start();
	}
	
}