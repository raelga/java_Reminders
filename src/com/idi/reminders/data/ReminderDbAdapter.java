package com.idi.reminders.data;

import java.util.Date;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ReminderDbAdapter {

	SQLiteDatabase db;
	// Database fields

	public static final String DB_LISTS_TABLE = "tbl_lists";
	public static final String KEY_LID = "l_id";
	public static final String KEY_LNAME = "l_name";

	public static final String DB_REMINDERS_TABLE = "tbl_reminders";
	public static final String KEY_RID = "r_id";
	public static final String KEY_RNAME = "r_name";
	public static final String KEY_RDESCRIPTION = "r_description";
	public static final String KEY_RPRIORITY = "r_priority";
	public static final String KEY_RADDEDDATE = "r_addedDate";
	public static final String KEY_RSTARTDATE = "r_startDate";
	public static final String KEY_RENDDATE = "r_endDate";
	public static final String KEY_RSTATE = "r_state";
	public static final String KEY_RLISTID = "r_listid";

	private static final String BASE_RSELECT = "SELECT " + KEY_RID
			+ " as _id, " + KEY_RID + " , " + KEY_RNAME + ", "
			+ KEY_RDESCRIPTION + ", " + KEY_RPRIORITY + ", " + KEY_RADDEDDATE
			+ ", " + KEY_RSTARTDATE + ", " + KEY_RSTATE + ", " + KEY_RENDDATE
			+ ", " + KEY_LID + ", " + KEY_RLISTID + ", " + KEY_LNAME + " " + "FROM "
			+ DB_REMINDERS_TABLE + ", " + DB_LISTS_TABLE + " " + "WHERE "
			+ KEY_RLISTID + "=" + KEY_LID + " ";

	private static final String BASE_LSELECT = "SELECT " + KEY_LID
			+ " as _id, " + KEY_LID + " , " + KEY_LNAME + " " + "FROM "
			+ DB_LISTS_TABLE + " ";

	private Context context;
	private SQLiteDatabase database;
	private ReminderDbHelper dbHelper;

	public ReminderDbAdapter(Context context) {
		this.context = context;
	}

	public ReminderDbAdapter open() throws SQLException {
		dbHelper = new ReminderDbHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public long createList(String name, String icon) {
		ContentValues initialValues = createListValues(name, icon);

		return database.insert(DB_LISTS_TABLE, null, initialValues);
	}

	/**
	 * Update the reminder
	 */
	public boolean updateList(long rowId, String name, String icon) {

		ContentValues updateValues = createListValues(name, icon);

		return database.update(DB_LISTS_TABLE, updateValues, KEY_LID + "="
				+ rowId, null) > 0;
	}

	/**
	 * Deletes reminder
	 */
	public boolean deleteList(long rowId) {

		clearList(rowId);

		if (rowId == 0) {
			return false;
		} else {
			return database.delete(DB_LISTS_TABLE, KEY_LID + "=" + rowId, null) > 0;
		}
	}

	public boolean clearList(long rowId) {

		return database.delete(DB_REMINDERS_TABLE, KEY_RLISTID + "=" + rowId,
				null) > 0;

	}

	public Cursor fetchLists() {

		final String query = BASE_LSELECT + ";";

		return database.rawQuery(query, null);

	}

	private ContentValues createListValues(String name, String icon) {
		ContentValues values = new ContentValues();
		values.put(KEY_LNAME, name);
		return values;
	}

	/**
	 * Create a new reminder If the reminder is successfully created return the
	 * new rowId for that note, otherwise return a -1 to indicate failure.
	 */

	public long createReminder(String name, String description, int priority,
			Date startDate, Date endDate, Boolean state, long listId) {

		if(name.trim().length()<1) return 0;
		
		ContentValues initialValues = createContentValues(name, description,
				priority, startDate, endDate, state, listId);
		long d = 0;
		try {
			d = database.insert(DB_REMINDERS_TABLE, null, initialValues);
		} catch (Exception e) {
			Log.e("Save Reminder info State", e.getMessage());
		}
		return d;
	}

	/**
	 * Update the reminder
	 */
	public boolean updateReminder(long rowId, String name, String description,
			int priority, Date startDate, Date endDate, boolean state,
			long listId) {

		ContentValues updateValues = createContentValues(name, description,
				priority, startDate, endDate, state, listId);

		return database.update(DB_REMINDERS_TABLE, updateValues, KEY_RID + "="
				+ rowId, null) > 0;
	}

	public boolean setReminderState(long rowId, boolean state) {
		try {

			int iState = 0;
			if (state) iState = 1;

			database.execSQL("UPDATE " + DB_REMINDERS_TABLE + " " + "SET "
					+ KEY_RSTATE + "=" + iState + " " + "WHERE " + KEY_RID
					+ "=" + rowId + ";");
		} catch (Exception e) {
			// catch code
			Log.e("Set State of " + rowId + " FAIL!", e.getMessage());
			return false;
		}
		return true;

	}

	public boolean getReminderState(long rowId) {
		try {

			Cursor mCursor = database.rawQuery(BASE_RSELECT + " AND " + KEY_RID
					+ "=" + rowId + ";", null);

			if (mCursor != null) {
				mCursor.moveToFirst();
				return mCursor.getShort(mCursor.getColumnIndex(KEY_RSTATE)) > 0;
				
			}
		} catch (Exception e) {
			// catch code
			Log.e("Get State of " + rowId + " FAIL!", e.getMessage());
			return false;
		}
		
		return false;
	
	}
	
	/**
	 * Deletes reminder
	 */
	public boolean deleteReminder(long rowId) {

		return database.delete(DB_REMINDERS_TABLE, KEY_RID + "=" + rowId, null) > 0;
	}

	public Cursor fetchAllReminders() {

		return database.rawQuery(BASE_RSELECT + ";", null);

	}

	/**
	 * Return a Cursor over the list of all reminder in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllReminders(long listId, String sortField,
			String sortOrder) {

		String[] validFields = { KEY_RID, KEY_RNAME, KEY_RPRIORITY, KEY_RSTATE,
				KEY_RADDEDDATE, KEY_RSTARTDATE, KEY_RENDDATE };

		if (sortField == null) {
			sortField = KEY_RID;
		} else {
			int i;
			for (i = 0; i < validFields.length; i++)
				if (validFields[i].equalsIgnoreCase(sortField))
					break;
			if (i == validFields.length)
				sortField = KEY_RID;
		}

		if (sortOrder == null) {
			sortOrder = "ASC";
		} else if (!(sortOrder.equalsIgnoreCase("ASC"))
				&& !(sortOrder.equalsIgnoreCase("DESC"))) {
			sortOrder = "ASC";
		}

		String mOrderBy = "ORDER BY " + sortField + " " + sortOrder + " "
				+ ", " + KEY_RID + " ASC ";

		return database.rawQuery(BASE_RSELECT + " AND " + KEY_LID + "="
				+ listId + " " + mOrderBy + ";", null);

	}

	/**
	 * Return a Cursor positioned at the defined reminder
	 */
	public Cursor fetchReminder(long rowId) throws SQLException {

		Cursor mCursor = database.rawQuery(BASE_RSELECT + " AND " + KEY_RID
				+ "=" + rowId + ";", null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createContentValues(String name, String description,
			int priority, Date startDate, Date endDate, Boolean state,
			long listId) {

		String sState = "0";
		if (state)
			sState = "1";

		ContentValues values = new ContentValues();

		values.put(KEY_RNAME, name);
		values.put(KEY_RDESCRIPTION, description);
		values.put(KEY_RPRIORITY, priority);
		values.put(KEY_RSTARTDATE, startDate.getTime());
		values.put(KEY_RENDDATE, endDate.getTime());
		values.put(KEY_RSTATE, sState);
		values.put(KEY_RLISTID, listId);

		return values;
	}

}
