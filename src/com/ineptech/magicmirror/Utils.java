package com.ineptech.magicmirror;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class Utils {

	/*
	 * Class for a bunch of static utility functions that didn't make sense to put anywhere else. 
	 */
	
	public static Boolean debug = false;  // used to debug some modules
	
	public static boolean isWeekday() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    public static boolean afterFive(){
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hourOfDay >= 17;
    }
    
    public static boolean isTimeForWork() {  
        int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return (isWeekday() && hourOfDay > 7 && hourOfDay < 9 || true);
    }
    
    public static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
    
    public static String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
		InputStream in = entity.getContent();
		StringBuffer out = new StringBuffer();
		int n = 1;
		while (n>0) {
			byte[] b = new byte[4096];
			n =  in.read(b);
			if (n>0) 
				out.append(new String(b, 0, n));
		}
		return out.toString();
	}
    
    public static String parseTransitResult(String result, String regex) {
		String text = "";
		// Apply the user-supplied regex to the response from the Transit API and hope it produces a parsable time
    	try {
	        Pattern r = Pattern.compile(regex);
	        Matcher m = r.matcher(result);
	        if (m.find( )) { 
	           text = m.group(1);
	        }
	        if (text.length() > 0) 
	        	text = Utils.parseTransitTime(text);  
    	} catch (Exception e) {
    		text = "The regex threw an exception :(";
    	}
    	return text;
	}
    
    public static String parseTransitTime(String s) {
    	// take whatever the transit API returns and try to turn it into a human readable eta, e.g. "12m"
    	String response = "";
    	long now = Calendar.getInstance().getTimeInMillis();
    	// First try epoch time in ms
    	if (s.length() == 13) {
    		try {
    			Long l = Long.parseLong(s);
    			long diff = l-now;
    			if (diff > -60*1000*3 && diff < 1000*60*60*24) {
    				if (diff < 0) diff = 0;   
    				// this parsed to a date in the next 24 hours, this is probably right
   					long hours = diff/(1000*60*60);
   					long mins = (diff%(1000*60*60))/(1000*60);
   					if (hours > 0) response = hours+"h ";
   					response += mins+"m";
   					return response;
    			}
    		} catch (Exception e) {}
    	}
    	// next try epoch time in seconds
    	if (s.length() == 10) {
    		try {
    			long now_s = now/1000;
    			Long l = Long.parseLong(s);
    			long diff = l-now_s;
    			if (diff > -60*3 && diff < 60*60*24) {
    				if (diff < 0) diff = 0;
    				// this parsed to a date in the next 24 hours, this is probably right
   					long hours = diff/(60*60);
   					long mins = (diff%(60*60))/60;
   					if (hours > 0) response = hours+"h ";
   					response += mins+"m";
   					return response;
    			}
    		} catch (Exception e) {}
    	}

    	String[] dateFormats = {"yyyy-MM-dd'T'HH:mm:ss.SSSZ","yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss" };   
    	// TODO: add other date formats as necessary
    	for (String dateFormat : dateFormats) {
    		try { 
    			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    			Date d = sdf.parse(s);
    			if (d.getTime() > 0) { // evidently this was the right format, since it didn't crash or eval to 0...
    				long diff = d.getTime() - now;
    				diff /= 1000;
    				if (diff > 0 && diff < 60*60*24) {
        				// this parsed to a date in the next 24 hours, this is probably right
       					long hours = diff/(60*60);
       					long mins = (diff%(60*60))/60;
       					if (hours > 0) response = hours+"h ";
       					response += mins+"m";
       					return response;
        			}
    			}
    		} catch (Exception e) {} 
    	}
    	
    	
    	// Parsing failed, just return the string
    	return s;
    }   
}

class TimeSelectionWidget {
	// A pair of TimePicker views and some utility functions for interacting with them, for setting on/off times of modules
	private LinearLayout holder;
	private TimePicker start, end;
	public TimeSelectionWidget() {
		Context context = MainApplication.getContext();
		holder = new LinearLayout(context);
		start = new TimePicker(context);
		end = new TimePicker(context);
		float size = 0.6f;
		start.setScaleX(size); start.setScaleY(size); end.setScaleX(size); end.setScaleY(size);  
		TextView stv = new TextView(context);
		stv.setText("From");
		TextView etv = new TextView(context);
		etv.setText(" To ");
		LinearLayout sholder = new LinearLayout(context);
		LinearLayout eholder = new LinearLayout(context);
		sholder.addView(stv);
		sholder.addView(start);
		eholder.addView(etv);
		eholder.addView(end);
		holder.addView(sholder);
		holder.addView(eholder);
		holder.setOrientation(LinearLayout.VERTICAL);
		
	}
	public LinearLayout getLayout() {
		return holder;
	}
	public int getStartHour() {
		if (android.os.Build.VERSION.SDK_INT < 23) 
			return start.getCurrentHour();
		else
			return start.getHour();
	}
	public int getEndHour() {
		if (android.os.Build.VERSION.SDK_INT < 23) 
			return end.getCurrentHour();
		else
			return end.getHour();
	}
	public int getStartMinute() {
		if (android.os.Build.VERSION.SDK_INT < 23) 
			return start.getCurrentMinute();
		else
			return start.getMinute();
	}
	public int getEndMinute() {
		if (android.os.Build.VERSION.SDK_INT < 23) 
			return end.getCurrentMinute();
		else
			return end.getMinute();
	}
}