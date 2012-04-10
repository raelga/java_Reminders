package com.idi.reminders;

import com.idi.reminders.data.ReminderDbAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;

public class ReminderCursorAdapter extends CursorAdapter {
	//private final Cursor c;
	private final Activity context;
    private LayoutInflater mLayoutInflater;
    
	private static final String RPREFS_MAIN = "Reminders Preferences";
	private SharedPreferences settings;
	private Resources res;

	private ReminderDbAdapter dbHelper;
	
	public ReminderCursorAdapter(Activity context, Cursor items) {
		super(context,items);
		
		this.context = context;
    	settings = context.getSharedPreferences(RPREFS_MAIN, 0);
		res = context.getResources();
		//this.c = items;
        mLayoutInflater = LayoutInflater.from(context); 

	}

	static class ViewHolder {
		protected long _id;
		protected long reminderId;
		protected CheckBox stateCheckBox;
		protected TextView labelView;
		protected ImageView priorityView;
	}
	
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = mLayoutInflater.inflate(R.layout.reminder_row, parent, false);
        return v;
    }
    
    @Override
    public void bindView(View v, Context context, final Cursor c) {

		
		View rowView = null;

		if (v == null) {

			rowView = mLayoutInflater.inflate(R.layout.reminder_row, null);

			final ViewHolder myViewHolder = new ViewHolder();

			myViewHolder._id = c.getLong(0);
			myViewHolder.reminderId = c.getLong(0);
			
			myViewHolder.stateCheckBox = (CheckBox) rowView
					.findViewById(R.id.row_state);
			myViewHolder.labelView = (TextView) rowView
					.findViewById(R.id.row_label);
			myViewHolder.priorityView = (ImageView) rowView
					.findViewById(R.id.row_priority);
			
			final CheckBox stateCheckBox = (CheckBox) rowView.findViewById(R.id.row_state);
			stateCheckBox
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							updateReminderState(c.getLong(1), stateCheckBox
									.isChecked());
						}
					});

			stateCheckBox.setClickable(false);
			stateCheckBox
					.setChecked(Boolean.parseBoolean(c.getString(c
							.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RSTATE))));
			
			rowView.setTag(myViewHolder);

		} 

		rowView = v;
 

		final CheckBox stateCheckBox = (CheckBox) rowView.findViewById(R.id.row_state);
		stateCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						updateReminderState(c.getLong(1), stateCheckBox
								.isChecked());
					}
				});


		stateCheckBox
				.setChecked(Boolean.parseBoolean(c.getString(c
						.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RSTATE))));
				
		
			stateCheckBox.setClickable(false);
			
		final TextView labelView = (TextView) rowView.findViewById(R.id.row_label);
		labelView.setText(c.getString(c.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RNAME)));
		

		/*
		 * Priority
		 */

		final ImageView priorityView = (ImageView) rowView.findViewById(R.id.row_priority);
		int priority = Integer.parseInt(c.getString(c.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RPRIORITY)));
		
		if (!(settings.getBoolean(res.getString(R.string.preferences_panel_showPriority), false))) {
			priority = 0;
		}
		
		switch (priority) {
			case 1:
				priorityView.setImageResource(R.drawable.priority_low);
				break;

			case 2:
				priorityView.setImageResource(R.drawable.priority_medium);
				break;

			case 3:
				priorityView.setImageResource(R.drawable.priority_high);
				break;

			case 4:
				priorityView.setImageResource(R.drawable.priority_urgent);
				break;
				
			default:
				priorityView.setImageResource(R.drawable.transparent);
				break;
		}

		/*
		 * CheckBox State
		 */

		Boolean newState = Boolean.parseBoolean(c.getString(c.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RSTATE)));
		final CheckBox cbState = (CheckBox) rowView.findViewById(R.id.row_state);
		cbState.setChecked(newState);

		cbState.setChecked(c.getString(c.getColumnIndexOrThrow(ReminderDbAdapter.KEY_RSTATE)).equalsIgnoreCase("1"));
		cbState.setClickable(false);
		
		if(cbState.isChecked()){
			labelView.setPaintFlags(labelView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			labelView.setPaintFlags(labelView.getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
		}
		
	}

	public void updateReminderState(long rId, Boolean newState) {

		dbHelper = new ReminderDbAdapter(context);
		dbHelper.open();
		dbHelper.setReminderState(rId, newState);
		dbHelper.close();
		
		this.notifyDataSetChanged();
		
	}

}