package com.idi.reminders;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.idi.reminders.data.ReminderDbAdapter;

public class ReminderInfo extends Activity {

	private static final String RPREFS_MAIN = "Reminders Preferences";
	public static final String FLAG_NEW = "newReminderFlag";
	public static final String FLAG_EDIT = "editReminderFlag";
	private SharedPreferences settings;
	private Resources res;

	private EditText mNameText;
	private EditText mDescriptionText;
	private Spinner mSpList;
	private Spinner mSpPriority;
	private CheckBox mStateCheckBox;

	private ImageButton mPickStartDate;
	private TextView mTextStartDate;
	private Date mStartDate;
	private ImageButton mPickEndDate;
	private TextView mTextEndDate;
	private Date mEndDate;
	private int mDateFlag;

	private Calendar mDialogCalendar;
	private DatePickerDialog.OnDateSetListener mDateSetListener =

	new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			mDialogCalendar.set(Calendar.YEAR, year);
			mDialogCalendar.set(Calendar.MONTH, monthOfYear);
			mDialogCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

			switch (mDateFlag) {

			case DATE_START_FLAG:
				mStartDate = mDialogCalendar.getTime();
				break;

			case DATE_END_FLAG:
				mEndDate = mDialogCalendar.getTime();
				break;

			}

			updateDisplay();
		}
	};

	private Long currentListId;
	private Long currentReminderId;
	private ReminderDbAdapter dbHelper;
	private Cursor mLists;

	Boolean flagEditable = false;
	Boolean flagNew = false;

	/*
	 * Dialogs
	 */
	static final int DATE_DIALOG_ID = 0;
	static final int DATE_START_FLAG = 1;
	static final int DATE_END_FLAG = 2;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		dbHelper = new ReminderDbAdapter(this);
		dbHelper.open();
		setContentView(R.layout.reminder_info);

		settings = getSharedPreferences(RPREFS_MAIN, 0);
		res = getResources();

		currentListId = settings.getLong(
				res.getString(R.string.preferences_mainList_id), 0);
		currentReminderId = null;

		Bundle inParameters = getIntent().getExtras();

		if (inParameters != null) {

			if (inParameters.containsKey(ReminderDbAdapter.KEY_RLISTID))
				currentListId = inParameters
						.getLong(ReminderDbAdapter.KEY_RLISTID);
			if (inParameters.containsKey(ReminderDbAdapter.KEY_RID))
				currentReminderId = inParameters
						.getLong(ReminderDbAdapter.KEY_RID);
			if (inParameters.containsKey(FLAG_EDIT)) {
				flagEditable = inParameters.getBoolean(FLAG_EDIT, false);
			}
			if (inParameters.containsKey(FLAG_NEW)) {
				flagNew = inParameters.getBoolean(FLAG_NEW, false);
			}

		}

		// Spinner with lists

		mSpList = (Spinner) findViewById(R.id.reminder_list);
		mLists = dbHelper.fetchLists();

		SimpleCursorAdapter mSca = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item, mLists,
				new String[] { ReminderDbAdapter.KEY_LNAME },
				new int[] { android.R.id.text1 }); // The "text1" view defined
													// in
													// the XML template

		mSca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpList.setAdapter(mSca);

		mLists.moveToFirst();
		for (int i = 0; i < mLists.getCount(); i++) {
			if (mLists.getLong(mLists
					.getColumnIndexOrThrow(ReminderDbAdapter.KEY_LID)) == currentListId) {
				if (i < mSpList.getCount())
					mSpList.setSelection(i, true);
				break;
			}
			mLists.moveToNext();
		}
		mLists.moveToFirst();

		mSpPriority = (Spinner) findViewById(R.id.reminder_priority);

		mNameText = (EditText) findViewById(R.id.reminder_name);
		mDescriptionText = (EditText) findViewById(R.id.reminder_description);

		mTextStartDate = (TextView) findViewById(R.id.reminder_info_viewStartDate);
		mTextEndDate = (TextView) findViewById(R.id.reminder_info_viewEndDate);

		mPickStartDate = (ImageButton) findViewById(R.id.reminder_info_setStartDate);
		mPickEndDate = (ImageButton) findViewById(R.id.reminder_info_setEndDate);

		Calendar cal = Calendar.getInstance();

		mStartDate = cal.getTime();

		cal.add(Calendar.DAY_OF_YEAR, settings.getInt(
				res.getString(R.string.preferences_info_defaultEndDate), 5));
		mEndDate = cal.getTime();

		mStateCheckBox = (CheckBox) findViewById(R.id.reminder_state);

		mDialogCalendar = Calendar.getInstance();

		populateFields();

		mTextStartDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDateFlag = DATE_START_FLAG;
				showDialog(DATE_DIALOG_ID);
			}
		});

		mPickStartDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDateFlag = DATE_START_FLAG;
				showDialog(DATE_DIALOG_ID);
			}
		});

		mTextEndDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDateFlag = DATE_END_FLAG;
				showDialog(DATE_DIALOG_ID);

			}
		});

		mPickEndDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDateFlag = DATE_END_FLAG;
				showDialog(DATE_DIALOG_ID);

			}
		});

		setEditableMode(flagEditable);

	}

	public void setEditableMode(boolean editableMode) {

		flagEditable = editableMode;
		Button confirmButton = (Button) findViewById(R.id.reminder_edit_button);

		mSpList.setEnabled(flagEditable || flagNew);
		mSpPriority.setEnabled(flagEditable || flagNew);

		mNameText = (EditText) findViewById(R.id.reminder_name);
		mDescriptionText = (EditText) findViewById(R.id.reminder_description);

		mNameText.setEnabled(flagEditable || flagNew);
		mDescriptionText.setEnabled(flagEditable || flagNew);

		 mNameText.setFocusable(flagEditable || flagNew);
		 mDescriptionText.setFocusable(flagEditable || flagNew);

		 mNameText.setFocusableInTouchMode(flagEditable || flagNew);
		 mDescriptionText.setFocusableInTouchMode(flagEditable || flagNew);
		 

		mPickStartDate.setEnabled(flagEditable || flagNew);
		mPickEndDate.setEnabled(flagEditable || flagNew);
		mTextStartDate.setEnabled(flagEditable || flagNew);
		mTextEndDate.setEnabled(flagEditable || flagNew);
		mStateCheckBox.setEnabled(flagEditable || flagNew);

		if (flagNew) {

			confirmButton.setBackgroundResource(R.drawable.ic_menu_save);
			confirmButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {

					if (!saveState()) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								view.getContext());
						builder.setMessage(
								"You haven't set any name, want to forget the reminder?")
								.setCancelable(false)
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dbHelper.close();
												finish();
											}
										})
								.setNegativeButton("No",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						builder.create().show();

					} else {

						saveState();

						Toast.makeText(getApplicationContext(),
								"New reminder added!", Toast.LENGTH_SHORT)
								.show();

						mLists.moveToPosition(mSpList.getSelectedItemPosition());
						Long listId = mLists.getLong(mLists
								.getColumnIndexOrThrow(ReminderDbAdapter.KEY_LID));
						Intent resultIntent = new Intent();

						resultIntent.putExtra("resultReminderInfo", listId);
						setResult(Activity.RESULT_OK, resultIntent);

						dbHelper.close();
						finish();
					}

				}

			});

		} else {
			
			if (flagEditable) {
				
				confirmButton.setBackgroundResource(R.drawable.lock);
				 
			} else {
				
				confirmButton.setBackgroundResource(R.drawable.lock_open);

				saveState();
				populateFields();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				
			}

			confirmButton.setOnClickListener(new View.OnClickListener() {

				public void onClick(View view) {

					setEditableMode(!flagEditable);

				}

			});
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:

			switch (mDateFlag) {

			case DATE_START_FLAG:
				mDialogCalendar.setTime(mStartDate);
				break;
			case DATE_END_FLAG:
				mDialogCalendar.setTime(mEndDate);
				break;
			}

			return new DatePickerDialog(this, mDateSetListener,
					mDialogCalendar.get(Calendar.YEAR),
					mDialogCalendar.get(Calendar.MONTH),
					mDialogCalendar.get(Calendar.DAY_OF_MONTH));

		}
		return null;
	}

	private void updateDisplay() {

		String myDate;
		Calendar cal = Calendar.getInstance();
		cal.setTime(mStartDate);

		int m = cal.get(Calendar.MONTH) + 1;
		myDate = cal.get(Calendar.DAY_OF_MONTH) + "/" + m + "/"
				+ cal.get(Calendar.YEAR) + "";

		mTextStartDate.setText("Starts on " + myDate);

		if (mStartDate.after(mEndDate)) {
			mEndDate = cal.getTime();
		} else {
			cal.setTime(mEndDate);
		}

		m = cal.get(Calendar.MONTH) + 1;
		myDate = cal.get(Calendar.DAY_OF_MONTH) + "/" + m + "/"
				+ cal.get(Calendar.YEAR) + "";

		if (this.mStateCheckBox.isChecked()) {
			mTextEndDate.setText("Ended on " + myDate + "");
		} else {
			mTextEndDate.setText("Ends on " + myDate + "");
		}

		mTextStartDate.refreshDrawableState();
		mTextEndDate.refreshDrawableState();

	}

	private void populateFields() {

		if (currentReminderId != null) {

			Cursor r = dbHelper.fetchReminder(currentReminderId);

			startManagingCursor(r);
			int myPriority = r.getInt(r
					.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RPRIORITY));

			Long myListId = r.getLong(r
					.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RLISTID));

			mSpPriority.setSelection(myPriority, true);

			mStartDate.setTime(r.getLong((r
					.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RSTARTDATE))));
			mEndDate.setTime(r.getLong((r
					.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RENDDATE))));

			int KeyColumnIdex = mLists
					.getColumnIndexOrThrow(ReminderDbAdapter.KEY_LID);
			for (int i = 0; i < mLists.getCount(); i++) {
				if (mLists.getLong(KeyColumnIdex) == myListId) {
					mSpList.setSelection(i);
				}
				mLists.moveToNext();
			}
			mLists.moveToFirst();

			mNameText.setText(r.getString(r
					.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RNAME)));
			mDescriptionText
					.setText(r.getString(r
							.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RDESCRIPTION)));

			mStateCheckBox.setChecked(dbHelper
					.getReminderState(currentReminderId));

		}

		updateDisplay();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(ReminderDbAdapter.KEY_RID, currentReminderId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	private boolean saveState() {

		String name = mNameText.getText().toString();
		String description = mDescriptionText.getText().toString();

		if (name.trim().length() < 1) {
			return false;
		}

		int priority = mSpPriority.getSelectedItemPosition();
		Boolean state = mStateCheckBox.isChecked();

		mLists.moveToPosition(mSpList.getSelectedItemPosition());
		Long listId = mLists.getLong(mLists
				.getColumnIndexOrThrow(ReminderDbAdapter.KEY_LID));

		if (currentReminderId == null) {
			long id = dbHelper.createReminder(name, description, priority,
					mStartDate, mEndDate, state, listId);
			if (id > 0) {
				currentReminderId = id;
			}
		} else {
			dbHelper.updateReminder(currentReminderId, name, description,
					priority, mStartDate, mEndDate, state, listId);
		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		MenuInflater inflater = getMenuInflater();

		if (flagNew) {
			inflater.inflate(R.menu.menu_remindersinfo_new, menu);
		} else if (flagEditable) {
			inflater.inflate(R.menu.menu_remindersinfo_edit, menu);
		} else {
			inflater.inflate(R.menu.menu_remindersinfo_view, menu);
		}
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.m_reminderinfo_edit:
			this.setEditableMode(true);
			return true;

		case R.id.m_reminderinfo_save:
			if (!saveState()) {

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(
						"You haven't set any name, want to forget the reminder?")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dbHelper.close();
										finish();
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				builder.create().show();

			} else {

				saveState();

				Toast.makeText(getApplicationContext(), "Reminder saved!",
						Toast.LENGTH_SHORT).show();

				mLists.moveToPosition(mSpList.getSelectedItemPosition());
				Long listId = mLists.getLong(mLists
						.getColumnIndexOrThrow(ReminderDbAdapter.KEY_LID));
				Intent resultIntent = new Intent();

				resultIntent.putExtra("resultReminderInfo", listId);
				setResult(Activity.RESULT_OK, resultIntent);

				dbHelper.close();
				finish();
			}

			return true;

		case R.id.m_remindersinfo_delete:
			dbHelper.deleteReminder(this.currentReminderId);
			Toast.makeText(getApplicationContext(), "Reminder deleted!",
					Toast.LENGTH_SHORT).show();
			dbHelper.close();
			finish();
			return true;

		case R.id.m_remindersinfo_list:
			dbHelper.close();
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}