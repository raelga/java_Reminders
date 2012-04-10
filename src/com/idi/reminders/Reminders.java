package com.idi.reminders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.idi.reminders.data.ReminderDbAdapter;

;

public class Reminders extends ListActivity {

	private ReminderDbAdapter dbHelper;

	private static final String RPREFS_MAIN = "Reminders Preferences";

	private static int KEY_COLUMN = 0;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private Resources res;

	private TextView mListName;

	private long currentListId = 0;
	private long currentReminderId = 0;

	private String currentSortCriteria = ReminderDbAdapter.KEY_RID;
	private Boolean currentSortOrderFlag = true;
	private String currentSortOrder = "ASC";
	private Cursor currentLists;
	private Cursor currentList;

	AlertDialog currentAlert;

	private float initialX = 0;  
	private float initialY = 0;  
	private float deltaX = 0;  
	private float deltaY = 0;  


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.reminder_list);

		dbHelper = new ReminderDbAdapter(this);
		dbHelper.open();

		res = getResources();
		settings = getSharedPreferences(RPREFS_MAIN, 0);
		editor = settings.edit();

		long startingList = settings.getLong(res.getString(R.string.preferences_mainList_id), 0);

		registerForContextMenu(getListView());
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);    
		lv.setDividerHeight(2);
		lv.clearChoices();

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				currentList.moveToPosition(position);
				currentReminderId = currentList.getLong(KEY_COLUMN);

				openReminderInfo(false);
				
				return true;

			}
		});


		// TODO: Simple click brings to read-only view.

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


				currentList.moveToPosition(position);
				currentReminderId = currentList.getLong(KEY_COLUMN);
				dbHelper.setReminderState(currentReminderId, !dbHelper.getReminderState(currentReminderId));
				buildCurrentListView();
//				showPopUpMenuReminder();
			}
		});

		setCurrentList(startingList);

	}

	@Override
	protected void onResume() {
		super.onResume();
		setCurrentList(currentListId);
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (settings.getBoolean(res.getString(R.string.preferences_mainList_setLast), true)) {
			setMainList();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbHelper != null) {
			dbHelper.close();
		}
	}

	/*
	 * List Management
	 */

	private void buildCurrentListView() {

		currentList = dbHelper.fetchAllReminders(currentListId, currentSortCriteria, currentSortOrder);

		ListView lv = getListView();
		//lv.destroyDrawingCache();
		//lv.requestLayout();

		lv.setAdapter(new ReminderCursorAdapter(Reminders.this,currentList));

		mListName = (TextView) findViewById(R.id.list_name);
		mListName.setText(currentLists.getString(currentLists.getColumnIndexOrThrow(ReminderDbAdapter.KEY_LNAME)));

	}


	private void setCurrentList(long newList) {

		int i = 0;
		Cursor aux = dbHelper.fetchLists();
		startManagingCursor(aux);
		aux.moveToFirst();

		// If not 0, get position
		Boolean bListFound = false;
		if (newList > 0) {

			for (i = 0; i < aux.getCount(); i++) {
				if (Long.parseLong(aux.getString(KEY_COLUMN)) == newList){
					bListFound = true;
					break;	
				}
				aux.moveToNext();
			}
		}

		// If not found, set first list

		if (bListFound) {
			if((Long.parseLong(aux.getString(KEY_COLUMN)) != newList)) {

				aux.moveToFirst();
				newList = Long.parseLong(aux.getString(KEY_COLUMN));
				i = 0;
			}
		} else {
			aux.moveToFirst();
			newList = Long.parseLong(aux.getString(KEY_COLUMN));
		}

		if (currentLists != null)
			currentLists.close();

		currentLists = dbHelper.fetchLists();
		startManagingCursor(currentLists);

		currentLists.moveToPosition(i);
		currentListId = newList;

		if (settings.getBoolean(res.getString(R.string.preferences_mainList_setLast), true)) {
			setMainList();
		}

		buildCurrentListView();

	}

	private void setCurrentListNext() {

		if (currentLists.isLast()) {
			currentLists.moveToFirst();
		} else {
			currentLists.moveToNext();
		}

		setCurrentList(Long.parseLong(currentLists.getString(currentLists
				.getColumnIndexOrThrow(ReminderDbAdapter.KEY_LID))));

	}


	private void setCurrentListPrev() {

		if (currentLists.isFirst()) {
			currentLists.moveToLast();
		} else {
			currentLists.moveToPrevious();
		}

		setCurrentList(Long.parseLong(currentLists.getString(currentLists
				.getColumnIndexOrThrow(ReminderDbAdapter.KEY_LID))));

	}


	private void applySortCriteria() {

		if (!currentSortOrderFlag) {
			currentSortOrder = "ASC";
		} else {
			currentSortOrder = "DESC";
		}

		// TODO: Check currentSortCriteria

		buildCurrentListView();

	}

	/*
	 * List Items
	 */

	// Reaction to the menu selection
	// ListView and view (row) on which was clicked, position and

	/*
	 * @Override protected void onListItemClick(ListView l, View v, int
	 * position, long id) { super.onListItemClick(l, v, position, id);
	 * 
	 * Toast.makeText(this, "ListItem" + position + ":" + id,
	 * Toast.LENGTH_SHORT);
	 * 
	 * }
	 */
	protected void openReminderInfo(boolean bEditable) {

		Intent i = new Intent(this, ReminderInfo.class);

		Bundle b = new Bundle();
		b.putLong(ReminderDbAdapter.KEY_RLISTID, currentListId);
		b.putLong(ReminderDbAdapter.KEY_RID, currentReminderId);
		b.putBoolean(ReminderInfo.FLAG_EDIT, bEditable);
		b.putBoolean(ReminderInfo.FLAG_NEW, false);
		i.putExtras(b);

		startActivityForResult(i, 110);

	}

	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
		case (101) : { 
			if (resultCode == Activity.RESULT_OK) { 
				this.setCurrentList(data.getLongExtra("resultReminderInfo",0));
			} 
			break; 
		} 
		} 
	}

	protected void markReminder() {

		dbHelper.setReminderState(currentReminderId, !dbHelper.getReminderState(currentReminderId));

		buildCurrentListView();

	}

	protected void deleteReminder() {

		dbHelper.deleteReminder(currentReminderId);
		currentReminderId = 0;
		buildCurrentListView();

	}


	/*
	 * Menu Stuff
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_reminders, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.m_add:
			createReminder();
			return true;

		case R.id.m_add_list:
			updateList(false);
			return true;

		case R.id.m_sort:
			selectSortCriteria();
			return true;

		case R.id.m_popup_list:
			showPopUpMenuList();
			return true;

		case R.id.m_switch_list:
			listSwitcher();
			return true;

		case R.id.m_preferences:
			openPreferences();
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private void createReminder() {

		Intent i = new Intent(this, ReminderInfo.class);

		Bundle b = new Bundle();
		b.putLong(ReminderDbAdapter.KEY_RLISTID, currentListId);
		b.putBoolean(ReminderInfo.FLAG_EDIT, true);
		b.putBoolean(ReminderInfo.FLAG_NEW, true);
		i.putExtras(b);

		startActivity(i);
		setCurrentList(currentListId);

	}

	private void updateList(Boolean exists) {

		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

		String title;
		final String myNewStatus;
		final Boolean myExistFlag = exists;

		if (!myExistFlag) {
			title = "Set a name for the new list!";
			myNewStatus = "added";
		} else {
			title = "Set a new name!";
			myNewStatus = "updated";
		}

		alertBuilder.setTitle(title);

		final EditText myInput = new EditText(this);

		alertBuilder.setView(myInput);

		alertBuilder.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				String name = myInput.getText().toString();

				if (myExistFlag) {

					dbHelper.updateList(currentListId, name, null);

				} else {
					currentListId = dbHelper.createList(name, null);
				}

				setCurrentList(currentListId);

				Toast.makeText(getApplicationContext(),
						"List " + name + " " + myNewStatus + "!",
						Toast.LENGTH_SHORT).show();
			}

		});

		alertBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		if (currentAlert != null && currentAlert.isShowing()) {
			currentAlert.dismiss();
		}

		final AlertDialog currentAlert = alertBuilder.create();

		myInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					currentAlert
					.getWindow()
					.setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});

		currentAlert.show();

	}

	private void selectSortCriteria() {

		final CharSequence[] fields = { "Name", "Priority", "State",
				"Added date", "End date" };
		final String[] fields_id = { ReminderDbAdapter.KEY_RNAME,
				ReminderDbAdapter.KEY_RPRIORITY, ReminderDbAdapter.KEY_RSTATE,
				ReminderDbAdapter.KEY_RADDEDDATE,
				ReminderDbAdapter.KEY_RSTARTDATE,
				ReminderDbAdapter.KEY_RENDDATE };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_sortCriteria_title);
		builder.setItems(fields, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int i) {

				currentAlert = new AlertDialog.Builder(Reminders.this)
				.setTitle(R.string.dialog_sortOrder_title)
				.setItems(R.array.sort_order,
						new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
							int which) {

						currentSortOrderFlag = (which == 1);

						applySortCriteria();

					}

				}).create();
				currentAlert.show();
				currentSortCriteria = (fields_id[i]);

			}

		});

		currentAlert = builder.create();
		currentAlert.show();
	}

	/*
	private void showPopUpMenuReminder() {

		final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

		alertBuilder.setItems(R.array.menu_popup_reminder,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {

				switch (i) {
				case 0:
					markReminder();
					break;
				case 1:
					openReminderInfo(true);
					break;
				case 2:
					deleteReminder();
					break;
				case 3:
				default:
					break;
				}

			}
		});

		currentAlert = alertBuilder.create();

		currentAlert.show();
	}
*/

	private void showPopUpMenuList() {

		final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

		// builder.setTitle("List actions");
		alertBuilder.setItems(R.array.menu_popup_list,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {

				switch (i) {
				case 0:
					setMainList();
					editor.putBoolean(res.getString(R.string.preferences_mainList_setLast),false);
					break;
				case 1:
					updateList(true);
					break;
				case 2:
					clearList();
					break;
				case 3:
					deleteList();
					break;
				default:
					break;
				}

			}
		});

		currentAlert = alertBuilder.create();

		currentAlert.show();

	}

	private void setMainList() {

		// TODO: Check setMain behavior

		editor.putLong(res.getString(R.string.preferences_mainList_id),
				currentListId);

		// Commit the edits!
		editor.commit();
	}

	private void deleteList() {


		if(true) {

			AlertDialog.Builder builder = new AlertDialog.Builder(Reminders.this);
			builder.setMessage("All the elements of " + Reminders.this.mListName.getText().toString() + " will be erased. Are you sure?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {

					long oldId = currentListId;
					String oldName = Reminders.this.mListName.getText().toString(); 
					dbHelper.deleteList(oldId);
					setCurrentListNext();

					Toast.makeText(getApplicationContext(),  oldName + " deleted successfully.",
							Toast.LENGTH_SHORT).show();

				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			builder.create().show();



		}

	}

	private void clearList() {

		if(true) {

			AlertDialog.Builder builder = new AlertDialog.Builder(Reminders.this);
			builder.setMessage("All the elements of " + Reminders.this.mListName.getText().toString() + " will be erased. Are you sure?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {


					dbHelper.clearList(currentListId);
					setCurrentList(currentListId);

					Toast.makeText(getApplicationContext(),  
							Reminders.this.mListName.getText().toString() + " cleared successfully.",
							Toast.LENGTH_SHORT).show();

				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			builder.create().show();



		}

	}

	private void listSwitcher() {

		final Cursor allLists = dbHelper.fetchLists();

		currentAlert = new AlertDialog.Builder(Reminders.this)
		.setTitle(R.string.dialog_listSwitcher_title)
		.setCursor(allLists, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				allLists.moveToPosition(which);
				currentListId = Long.parseLong(allLists
						.getString(KEY_COLUMN));

				setCurrentList(currentListId);
			}

		}, ReminderDbAdapter.KEY_LNAME).create();

		currentAlert.show();
	}

	private void openPreferences() {

		openPreferencesPanel();
		setCurrentList(currentListId);
	}

	/*
	 * Preferences
	 */

	private void openPreferencesPanel() {

		Intent i = new Intent(this, PreferencesPanel.class);
		startActivity(i);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//This avoids touchscreen events flooding the main thread
		synchronized (event)
		{
			try
			{
				//Waits 16ms.
				event.wait(16);

				//when user touches the screen
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					//reset deltaX and deltaY
					deltaX = deltaY = 0;

					//get initial positions
					initialX = event.getRawX();
					initialY = event.getRawY();
				}

				//when screen is released
				if(event.getAction() == MotionEvent.ACTION_UP)
				{
					deltaX = event.getRawX() - initialX;
					deltaY = event.getRawY() - initialY;

					//swipped up
					if(deltaY<0)
					{
						//make your object/character move up
					}
					else //swipped down
					{
						//make your object/character move down
					}

					//swipped right
					if(deltaX>0)
					{
						//make your object/character move right
						this.setCurrentListPrev();
						Toast.makeText(getApplicationContext(),
								"List " + this.mListName.getText().toString(),
								Toast.LENGTH_SHORT).show();
					}
					else
					{
						//make your object/character move left
						this.setCurrentListNext();
						Toast.makeText(getApplicationContext(),
								"List " + this.mListName.getText().toString(),
								Toast.LENGTH_SHORT).show();
					}

					return true;
				}
			}

			catch (InterruptedException e)
			{
				return true;
			}
		}
		return true;
	}
}
