package com.idi.reminders;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class PreferencesPanel extends Activity {

	private static final String RPREFS_MAIN = "Reminders Preferences";
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private Resources res;

	private Spinner myPrefDefaultEndDate;
	private CheckBox myPrefViewLast;
	private CheckBox myPrefShowPriority;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.preferences);

		settings = getSharedPreferences(RPREFS_MAIN, 0);
		editor = settings.edit();
		res = getResources();

		// Spinner with lists

		myPrefDefaultEndDate = (Spinner) findViewById(R.id.preferences_panel_defaultEndDate_sp);
		myPrefViewLast = (CheckBox) findViewById(R.id.preferences_panel_setLast_cb);
		myPrefShowPriority = (CheckBox) findViewById(R.id.preferences_panel_showPriority_cb);

		Button confirmButton = (Button) findViewById(R.id.reminder_save_button);

		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				saveState();
				finish();
			}
		});

		populateFields();
	}

	private void populateFields() {

		myPrefViewLast.setChecked(settings.getBoolean(
				res.getString(R.string.preferences_mainList_setLast), true));
		myPrefShowPriority.setChecked(settings.getBoolean(
				res.getString(R.string.preferences_panel_showPriority), true));
		myPrefDefaultEndDate.setSelection(settings.getInt(
				res.getString(R.string.preferences_info_defaultEndDate), 5));

	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	private void saveState() {

		editor.putBoolean(res.getString(R.string.preferences_mainList_setLast),
				myPrefViewLast.isChecked());
		editor.putBoolean(
				res.getString(R.string.preferences_panel_showPriority),
				myPrefShowPriority.isChecked());
		editor.putInt(res.getString(R.string.preferences_info_defaultEndDate),
				myPrefDefaultEndDate.getSelectedItemPosition());

		// Commit the edits!
		editor.commit();

	}
}