package com.ineptech.magicmirror.modules;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import com.ineptech.magicmirror.Utils;

public class DayModule extends Module {
	
	public DayModule() {
		super("Day of week");
		desc = "Shows what day of the week and month it is.";
		defaultTextSize = 64;
		sampleString = "Wednesday the 27th";
	}
	
    public void update() {
        SimpleDateFormat formatDayOfMonth = new SimpleDateFormat("EEEE", Locale.US);
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        Spanned span = Html.fromHtml(formatDayOfMonth.format(now.getTime()) + " the " + dayOfMonth + "<sup><small>" 
        				+ Utils.getDayOfMonthSuffix(dayOfMonth) + "</small></sup>");
        tv.setText(span);
    }


}
