package com.bgu.android.tetris;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ManageProfile extends ListActivity{
	protected final ListActivity me = this;
	private ListAdapter mAdapter = null;
	Profile profileDb = Profile.getInstance(this);
	Button newProfile;
	Button selectProfile;
	Button deleteProfile;
	Cursor mCur;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_profile);
        mCur = profileDb.query();
        mAdapter = new SimpleCursorAdapter(this,R.layout.profile_list_entry, mCur,
				new String[] {Profile.NAME, Profile.SCORE}, new int[] {R.id.entry_list_name, R.id.entry_list_score});
        setListAdapter(mAdapter);
        Button newProfile = (Button)findViewById(R.id.btn_manage_new);
        Button deleteProButton = (Button)findViewById(R.id.btn_manage_delete);
        newProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				profileDb.addProfile("Denis", "200", "5", 1, 0);
				profileDb.addProfile("Ran", "400", "5", 1, 0);
				mCur = profileDb.query();
		        mAdapter = new SimpleCursorAdapter(me,R.layout.profile_list_entry, mCur,
						new String[] {Profile.NAME, Profile.SCORE}, new int[] {R.id.entry_list_name, R.id.entry_list_score});
		        setListAdapter(mAdapter);
			}    
		});
        
        deleteProButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				profileDb.clearDb();
				mCur = profileDb.query();
		        mAdapter = new SimpleCursorAdapter(me,R.layout.profile_list_entry, mCur,
						new String[] {Profile.NAME, Profile.SCORE}, new int[] {R.id.entry_list_name, R.id.entry_list_score});
		        setListAdapter(mAdapter);
				}    
		});
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	Cursor cur = profileDb.query();
        mAdapter = new SimpleCursorAdapter(this,R.layout.profile_list_entry, cur,
				new String[] {Profile.NAME, Profile.SCORE}, new int[] {R.id.entry_list_name, R.id.entry_list_score});
        setListAdapter(mAdapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Object o = this.getListAdapter().getItemId(position);
    	String keyword = o.toString();
    	Toast.makeText(this, "You selected: " + keyword, Toast.LENGTH_SHORT).show();
    }
}
