package com.bgu.android.tetris;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ManageProfile extends ListActivity{
	protected final ListActivity me = this;
	private ListAdapter mAdapter = null;
	public static final int DIALOG_NEW_PROFILE = 0;
	public static final int DIALOG_DELETE_PROFILE = 1;
	Profile profileDb = Profile.getInstance(this);
	Button newProfile;
	Button selectProfile;
	Button deleteProfile;
	Cursor mCur;
	private long deleteProfileId = 0;//position of profile list to delete
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_profile);
        Button newProfile = (Button)findViewById(R.id.btn_manage_new);
        Button deleteProButton = (Button)findViewById(R.id.btn_manage_delete);
        newProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_NEW_PROFILE);	
			}    
		});
        
        deleteProButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				profileDb.clearDb();
				updateList();
				}    
		});
        
        ListView profileList = getListView();
        profileList.setOnItemLongClickListener(new OnItemLongClickListener() {

			

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(me,
						"Profile in position " + position + " long clicked",
						Toast.LENGTH_SHORT).show();
				deleteProfileId = id;
				showDialog(DIALOG_DELETE_PROFILE);
				return true;
			}
		});
    }
    
    @Override
    public void onStart() {
    	super.onStart();
        updateList();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Object o = this.getListAdapter().getItemId(position);
    	String keyword = o.toString();
    	Toast.makeText(this, "You selected: " + keyword, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	switch(id) {
    	case DIALOG_NEW_PROFILE:
    		dialog = createNewProfileDialog("Enter a new profile name");
    		break;
    	case DIALOG_DELETE_PROFILE:
    		dialog = deleteProfileDialog("Are you shure?");
    		break;
    	default:
    			dialog = null;
    	}
    	return dialog;
    }
    
    private Dialog createNewProfileDialog(String title) {
    	final Dialog newProfileDialog = new Dialog(me);
    	newProfileDialog.setContentView(R.layout.dialog_new_profile);
    	newProfileDialog.setTitle(title);
    	
    	Button createBtn = (Button) newProfileDialog.findViewById(R.id.btn_dialog_create);
    	createBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText newName = (EditText) newProfileDialog.findViewById(R.id.edittxt_dialog_new_name);
				if (newName.getText().length() > 0){
					profileDb.addProfile(newName.getText().toString(), "0", 
							Profile.DEFAULT_DIFFICULT, Profile.DEFAULT_SHADOW, Profile.DEFAULT_COOPERATIVE);
					newProfileDialog.dismiss();
					updateList();
				}	
			}
		});
    	return newProfileDialog;
		
	}
    
	private Dialog deleteProfileDialog(String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(title);
		builder.setCancelable(false);
		builder.setPositiveButton("Confirm Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				profileDb.deleteById(Long.toString(deleteProfileId));
				Toast.makeText(me, "Profile id: " + deleteProfileId + " was deleted", Toast.LENGTH_SHORT).show();
				updateList();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}

	private void updateList() {
		mCur = profileDb.query();
        mAdapter = new SimpleCursorAdapter(me,R.layout.profile_list_entry, mCur,
				new String[] {Profile.NAME, Profile.SCORE}, new int[] {R.id.entry_list_name, R.id.entry_list_score});
        setListAdapter(mAdapter);
	}
}

