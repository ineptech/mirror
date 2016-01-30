package com.ineptech.magicmirror.modules;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.ineptech.magicmirror.MainApplication;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarModule extends Module{
	
	public int maxItems = 5;
	String sampleString = "\u2022 9-10:30: Water the pets"; 
	
	public CalendarModule() {
		super("Google Calendar");
		desc = "Shows the next 24 hours worth of Google Calendar items.  Note that this "
				+ "is specific to the user logged in and the Calendar settings on this tablet - if you "
				+ "open the Google Calendar app and hide some of your appointments or reminders, those items "
				+ "won't show up in the mirror display, even if they are still enabled on your other Android devices."
				+ "Use the buttons below to specify the maximum number of appointments to show.";
		defaultTextSize = 44;
		loadConfig();
	}

	public interface CalendarListener {
        void onCalendarUpdate(List<String> eventDetails);
    }
	
	private void loadConfig() {
    	maxItems = prefs.get(name+"_maxItems", maxItems);
    }
	
	@Override
    public void saveConfig() {
    	super.saveConfig();
    	prefs.set(name+"_maxItems", maxItems);
    }
	
	private Button maxItemsPlus;
	private Button maxItemsMinus;
	private TextView maxItemsTv;
	@Override
    public void makeConfigLayout() {
    	super.makeConfigLayout();
    	
    	Context context = MainApplication.getContext();
		LinearLayout ll = new LinearLayout(context);
		maxItemsTv = new TextView(context);
		maxItemsTv.setText("Max calendar items to display: " + maxItems);
		maxItemsPlus = new Button(context);
		maxItemsMinus = new Button(context);
		maxItemsPlus.setText("+");
		maxItemsMinus.setText("-");
		maxItemsMinus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				maxItems--;
				maxItemsTv.setText("Max calendar items to display: " + maxItems);
			}
		});
		maxItemsPlus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				maxItems++;
				maxItemsTv.setText("Max calendar items to display: " + maxItems);
			}
		});
		ll.addView(maxItemsMinus);
		ll.addView(maxItemsTv);
		ll.addView(maxItemsPlus);
    	configLayout.addView(ll);
    }
	
	public static String displayTime(Calendar c) {
		String s = "";
		if (c.get(Calendar.MINUTE) == 0)
        	s = ""+c.get(Calendar.HOUR);
        else 
        	s = new SimpleDateFormat("h:mm", Locale.US).format(c.getTime());
        return s;
	}

    public static void getCalendarEvents(final Context context, final CalendarListener calendarListener) {
        new AsyncTask<Void, Void, Void>() {
            List<String> eventDetails = new ArrayList<String>();
            @Override
            protected void onPostExecute(Void aVoid) {
                calendarListener.onCalendarUpdate(eventDetails);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Cursor cursor;
                ContentResolver contentResolver = context.getContentResolver();
                final String[] colsToQuery = new String[] 
                		{CalendarContract.Instances.CALENDAR_ID, CalendarContract.Instances.TITLE,
                        CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.BEGIN,
                        CalendarContract.Instances.END, CalendarContract.Instances.EVENT_LOCATION,
                        CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.EVENT_TIMEZONE};                
                
                long now = System.currentTimeMillis();
                long tomorrow = now + 2*(86400l * 1000l);
                now -= (86400l * 1000l);  // to deal with timezone issues, get several days' worth and then remove unneeded events 
                
                String selection = CalendarContract.Instances.BEGIN + " >= " + now + " and " + CalendarContract.Instances.BEGIN 
                        + " <= " + tomorrow + " and " + CalendarContract.Instances.VISIBLE + " = 1"; 
                String sort = CalendarContract.Instances.BEGIN + " ASC";
                
                Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE);
                ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE);
                Uri eventsUri = eventsUriBuilder.build();
                cursor = context.getContentResolver().query(eventsUri, colsToQuery, selection, null, sort);
                List<EventDetail> deets = new ArrayList<EventDetail>();
                
                // pull all the events and put them in a list of eventdetails
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                    	while (cursor.moveToNext()) {
	                        String title = cursor.getString(1);
	                        if (title.length() > 49)
	                        	title = title.substring(0,44)+"...";
	                        Calendar startTime = Calendar.getInstance();
	                        Calendar endTime = Calendar.getInstance();
	                        
	                        // adjust for timezone because, for some weird reason, all-day events are in GMT regardless of your tz
	                        TimeZone eventTz = TimeZone.getTimeZone(cursor.getString(7));
	                        TimeZone localTz = TimeZone.getDefault();
	                        int diffTz = localTz.getOffset(new Date().getTime()) - eventTz.getOffset(new Date().getTime()); 
	                        startTime.setTimeInMillis(cursor.getLong(3) - diffTz);
	                        endTime.setTimeInMillis(cursor.getLong(4) - diffTz);
	                        deets.add(new EventDetail(startTime, endTime, title));
                       	}
                    }
                cursor.close();
                }
	            // Now that the timezones are adjusted, re-order and prepare display strings for the events
                for (int i = 1; i < deets.size(); i++) {
                	if (deets.get(i).start.before(deets.get(i-1).start)) {
                		EventDetail e = deets.get(i);
                		deets.remove(i);
                		deets.add(i-1, e);
                		i = 1;
                	}
                }
                
                // Build a list of strings for the mainactivity routine to display
                now = System.currentTimeMillis();
                tomorrow = now + (86400l * 1000l);
                
                for (EventDetail d : deets) {
                	if (d.end.getTimeInMillis() > now && d.start.getTimeInMillis() < tomorrow) {
                		String timeformat = "h:mm";
                    	String time = displayTime(d.start);
                        if (d.end.getTimeInMillis() != d.start.getTimeInMillis()) 
                        	time += "-" + displayTime(d.end);
                        String displaytext = "\u2022 "+ time + ": " + d.name;
                        
                        // Assume any 24-hour appointment is an all-day reminder and remove the time.
                        if (d.end.getTimeInMillis() >= d.start.getTimeInMillis() + 86400l*1000l) {
                        	displaytext = "\u2022 "+ d.name;
                        }
                        eventDetails.add(displaytext);
                	}
                }
                return null;
            }
        }.execute();
    }
}

class EventDetail { // utility class to hold details of an event
	Calendar start, end;
	String name;
	public EventDetail(Calendar s, Calendar e, String n) {
		start = s; end = e; name = n;
	}
	public String toString() {
		return name + CalendarModule.displayTime(start) + "-" + CalendarModule.displayTime(end);
	}
}