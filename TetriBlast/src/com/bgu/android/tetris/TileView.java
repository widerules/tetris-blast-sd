package com.bgu.android.tetris;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TileView extends View {
	public static final String TAG = "TetrisBlast";
	
	/**
	 * Labels for the drawables that will be loaded into the TileView class
	 */
	protected static final int BLOCK_EMPTY = 0;
	protected static final int BLOCK_RED = 1;
	protected static final int BLOCK_BLUE = 2;
	protected static final int BLOCK_GREEN = 3;
	protected static final int BLOCK_YELLOW = 4;
	protected static final int BLOCK_PINK = 5;
	protected static final int BLOCK_LIGHBLUE = 6;
	protected static final int BLOCK_ORANGE = 7;
	protected static final int BLOCK_GREY = 8;
	protected static final int BLOCK_GHOST = 9;
	protected static final int BLOCK_BLOCK = 10;
	protected static final int BLOCK_BG1 = 11;
	protected static final int BLOCK_BG2 = 12;
	protected static final int NUM_OF_TILES = 12;
    /**
     * Parameters controlling the size of the tiles and their range within view.
     * Width/Height are in pixels, and Drawables will be scaled to fit to these
     * dimensions. X/Y Tile Counts are the number of tiles that will be drawn.
     */

    protected static int mTileSize;

    protected static final int mXTileCount = 12;
    protected static final int mYTileCount = 22;

    private static int mXOffset;
    private static int mYOffset;


    /**
     * A hash that maps integer handles specified by the subclasser to the
     * drawable that will be used for that reference
     */
    private Bitmap[] mTileArray; 

    /**
     * A two-dimensional array of integers in which the number represents the
     * index of the tile that should be drawn at that locations
     */
    private int[][] mTileGrid;

    private final Paint mPaint = new Paint();

    public TextView myText;
    //Constructors
    public TileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d(TAG, "TileView constructor 1");
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileView);
        myText = (TextView) findViewById(R.id.txt);
        mTileSize = a.getInt(R.styleable.TileView_tileSize, 30);
        Log.d(TAG, "In constructor1: The tile size = " + Integer.toString(mTileSize));
        //myText.setText("In constructor1: The tile size = " + Integer.toString(mTileSize));
        a.recycle();
    }

    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "TileView constructor 2");
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileView);
        myText = (TextView) findViewById(R.id.txt);
        mTileSize = a.getInt(R.styleable.TileView_tileSize, 30);
        Log.d(TAG, "In constructor2: The tile size = " + Integer.toString(mTileSize));
        //myText.setText("In constructor2: The tile size = " + Integer.toString(mTileSize));
        a.recycle();
    }

     /**
     * Rests the internal array of Bitmaps used for drawing tiles, and
     * sets the maximum index of tiles to be inserted
     * 
     * @param tilecount
     */
    
    public void resetTiles(int tilecount) {
    	mTileArray = new Bitmap[tilecount];
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //mXTileCount = (int) Math.floor(w / mTileSize);
        //mYTileCount = (int) Math.floor(h / mTileSize);
    	Log.d(TAG, "OnSize changed, w = " + Integer.toString(w)+"h = " + Integer.toString(h));
    	if(w == 800) {
    		mTileSize = 18;
    	}
    	Log.d(TAG, "The tile size changed to = " + Integer.toString(mTileSize));
    	//resetTiles(20);//TODO check this
        mXOffset = ((w - (mTileSize * mXTileCount)) / 2);
        mYOffset = ((h - (mTileSize * mYTileCount)) / 2);

        mTileGrid = new int[mXTileCount][mYTileCount];
        clearTiles();
    }

    /**
     * Function to set the specified Drawable as the tile for a particular
     * integer key.
     * 
     * @param key
     * @param tile
     */
    public void loadTile(int key, Drawable tile) {
        Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tile.setBounds(0, 0, mTileSize, mTileSize);
        tile.draw(canvas);
        mTileArray[key] = bitmap;
    }

    /**
     * Resets all tiles to 0 (empty)
     * 
     */
    public void clearTiles() {
        for (int x = 0; x < mXTileCount; x++) {
            for (int y = 0; y < mYTileCount; y++) {
            	setTile(0, x, y);
            	//setTile(4+x%2, x, y);
                //setTile(5-x%2, x,y+1);
            }
        }
    }

    /**
     * Used to indicate that a particular tile (set with loadTile and referenced
     * by an integer) should be drawn at the given x/y coordinates during the
     * next invalidate/draw cycle.
     * 
     * @param tileindex
     * @param x
     * @param y
     */
    public void setTile(int tileindex, int x, int y) {
        mTileGrid[x][y] = tileindex;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int x = 0; x < mXTileCount; x += 1) {
            for (int y = 0; y < mYTileCount; y += 1) {
                if (mTileGrid[x][y] > 0) {
                    canvas.drawBitmap(mTileArray[mTileGrid[x][y]], 
                    		mXOffset + x * mTileSize,
                    		mYOffset + y * mTileSize,
                    		mPaint);//TODO play with mPaint
                }
            }
        }

    }

}
