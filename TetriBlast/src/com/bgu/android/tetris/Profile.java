package com.bgu.android.tetris;

import java.util.HashMap;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

/**
 * This singleton class hold profile preferences database
 * @author Denis
 *
 */
public class Profile {
	//Constants
	public static final int FALSE = 0;
	public static final int TRUE = 1;
	public static final int DEFAULT_DIFFICULT = 4;//Default difficult of the game
	public static final int DEFAULT_SHADOW = TRUE;
	public static final int DEFAULT_COOPERATIVE = FALSE;
	public static final String MY_TAG = "TetrisBlast";
	public static final String DB_NAME = "db_profiles.db";
	public static final int DB_VERSION = 1;
	public static final String DB_TABLE_NAME = "profiles";
	public static final String NAME ="name";
	public static final String DIFFICULTY = "difficulty";
	public static final String SCORE = "score";
	public static final String SHADOW = "shadow";
	public static final String COOPERATIVE = "cooperative";
	
	
	private static HashMap<String, String> projectionMap; 
	static 
	{ 
		projectionMap = new HashMap<String, String>();
		projectionMap.put("_id","_id");
		projectionMap.put(NAME, NAME); 
		projectionMap.put(SCORE, SCORE); 
		projectionMap.put(DIFFICULTY, DIFFICULTY);
		projectionMap.put(SHADOW, SHADOW);
		projectionMap.put(COOPERATIVE, COOPERATIVE);
	} 
	//Inner members
	private static Profile instance = null;
	private DBholder mDataBase = null;
	
	
	private class DBholder extends SQLiteOpenHelper{

		public DBholder(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			Log.d(MY_TAG,"DBHolder constructor");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(MY_TAG,"DataBase onCreate begin");
			String sqlOpp = "CREATE TABLE " + DB_TABLE_NAME + " ( " 
					+ "_id" + " INTEGER PRIMARY KEY, "
					+ NAME + " TEXT, " 
					+ SCORE + " TEXT, " 
					+ DIFFICULTY + " INTEGER,"
					+ SHADOW + " INTEGER,"
					+ COOPERATIVE + " INTEGER "
					+ ");";
			db.execSQL(sqlOpp);
			Log.d(MY_TAG,"DataBase created!");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("content", "Upgrading database from version " + oldVersion + " to " 
					+ newVersion + ", which will destroy all old data"); 
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);
			onCreate(db);
			
		}
		
	}
	
	// Constructor
	private Profile(Context context) {
		mDataBase = new DBholder(context);
		Log.d(MY_TAG,"DataBase constructor!");
	}
	
	
	public static Profile getInstance(Context context) {
		if(instance == null) {
			instance = new Profile(context);
		}
		return instance;
	}
	
	public void clearDb(){
		SQLiteDatabase db = mDataBase.getWritableDatabase();
		db.execSQL("delete from " + DB_TABLE_NAME + ";");
		Log.d(MY_TAG, "Database deleted!");		
	}
	
	public Cursor query(){
		Cursor c = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DB_TABLE_NAME); 
		qb.setProjectionMap(projectionMap); 
		SQLiteDatabase db = mDataBase.getReadableDatabase();
		c = qb.query(db, null, null, null, null, null, null);
		return c;
	}
	
	public Cursor queryById(String id) {
		SQLiteDatabase db = mDataBase.getReadableDatabase(); 
		String[] coloms = {"_id", NAME, SCORE, DIFFICULTY, SHADOW, COOPERATIVE};
		String selection = "_id" + "=?";
		String[] args = {id};
		Cursor cc = null;
		cc = db.query(DB_TABLE_NAME, coloms, selection, args, null, null, null);
		return cc;
	}
	
	public void addProfile(String name, String score, int difficulty, int shadow, int cooperative) {
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(SCORE, score);
		values.put(DIFFICULTY, difficulty);
		values.put(SHADOW, shadow);
		values.put(COOPERATIVE, cooperative);
		SQLiteDatabase db = mDataBase.getWritableDatabase(); 
		long rowId = db.insert(DB_TABLE_NAME, NAME, values); 
		if (rowId <= 0) throw new SQLException("Failed to insert row into table " );
		Log.d(MY_TAG,"addProfile() new profile added");
	}
	
	public void deleteById(String id) {
		SQLiteDatabase db = mDataBase.getWritableDatabase();
		db.delete(DB_TABLE_NAME, "_id" + "=?", new String[] {id});
		Log.d(MY_TAG, "Profile _id " + id + " deleted from " + DB_TABLE_NAME);
	}
}
