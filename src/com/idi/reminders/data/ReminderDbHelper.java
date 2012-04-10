package com.idi.reminders.data;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ReminderDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "reminders_data";
	private static final int DATABASE_VERSION = 21;

	public ReminderDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase db) {

		// Enable foreign key constraints
		db.execSQL("PRAGMA foreign_keys=ON;");

		final String CREATE_TABLE_LISTS = "CREATE TABLE "
				+ ReminderDbAdapter.DB_LISTS_TABLE + " ("
				+ ReminderDbAdapter.KEY_LID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ReminderDbAdapter.KEY_LNAME + " TEXT UNIQUE NOT NULL);";
		final String CREATE_TABLE_REMINDERS = "CREATE TABLE "
				+ ReminderDbAdapter.DB_REMINDERS_TABLE + " ("
				+ ReminderDbAdapter.KEY_RID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ReminderDbAdapter.KEY_RNAME + " TEXT NOT NULL,"
				+ ReminderDbAdapter.KEY_RDESCRIPTION + " TEXT DEFAULT '',"
				+ ReminderDbAdapter.KEY_RPRIORITY + " INT DEFAULT 0,"
				+ ReminderDbAdapter.KEY_RADDEDDATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ ReminderDbAdapter.KEY_RSTARTDATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ ReminderDbAdapter.KEY_RENDDATE + " TIMESTAMP NULL,"
				+ ReminderDbAdapter.KEY_RSTATE + " INT DEFAULT 0,"
				+ ReminderDbAdapter.KEY_RLISTID + " INTEGER NOT NULL, "
				+ "FOREIGN KEY(" + ReminderDbAdapter.KEY_RLISTID + ") REFERENCES tbl_lists(" + ReminderDbAdapter.KEY_LID + ") "
				+ "ON DELETE CASCADE);";
		db.execSQL(CREATE_TABLE_LISTS);
		db.execSQL(CREATE_TABLE_REMINDERS);

		populateDB(db);

	}

	private void populateDB(SQLiteDatabase db) {

		Cursor cur = db.query("tbl_lists", null, null, null, null, null, null, null);
		if (cur.getCount() > 0)
			return;
		cur.close();

		ContentValues values = new ContentValues();
		values.put(ReminderDbAdapter.KEY_LNAME, "Main List");
		long listid = db.insert("tbl_lists", null, values);

		Date now = new Date();

		ContentValues reminderValues = new ContentValues();
		reminderValues.put(ReminderDbAdapter.KEY_RNAME, "Your first Reminder!");
		reminderValues.put(ReminderDbAdapter.KEY_RDESCRIPTION, "Welcome to Reminders!!");
		reminderValues.put(ReminderDbAdapter.KEY_RPRIORITY, "0");
		reminderValues.put(ReminderDbAdapter.KEY_RADDEDDATE, Long.toString(now.getTime()));
		reminderValues.put(ReminderDbAdapter.KEY_RSTARTDATE, Long.toString(now.getTime()));
		reminderValues.put(ReminderDbAdapter.KEY_RENDDATE, Long.toString(now.getTime()));
		reminderValues.put(ReminderDbAdapter.KEY_RSTATE, "0");
		reminderValues
				.put(ReminderDbAdapter.KEY_RLISTID, Long.toString(listid));

		try {
			db.insertOrThrow("tbl_reminders", null, reminderValues);
		} catch (Exception e) {
			// catch code
			Log.w("Populate Fail!", e.getMessage());
		}
		
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.w(ReminderDbHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS "
				+ ReminderDbAdapter.DB_REMINDERS_TABLE + " ");
		db.execSQL("DROP TABLE IF EXISTS " + ReminderDbAdapter.DB_LISTS_TABLE
				+ " ");
		onCreate(db);
	}
}
