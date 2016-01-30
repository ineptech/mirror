package com.ineptech.magicmirror;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/* 
 *  This is a simple app that displays some useful information on a plain black screen.  It is intended 
 *  to be run on a tablet that has been fitted in to a 2-way mirror, so that the white text appears to float 
 *  on the mirror.  It was written by Nick Hall in late 2015.  It's free to use by anyone for any purpose.  
 *  
 *  It was inspired by Hannah Mitt's (https://github.com/HannahMitt/HomeMirror) app.  In addition, there 
 *  are many others, Android and otherwise, that can be found by searching for "smart mirror" or "magic 
 *  mirror".  
 *  
 *  Questions/comments/etc - mirror (at) ineptech (dot) com.  
 */

public class MainApplication extends Application {

    private static final long MINUTES_10 = 10 * 60 * 1000;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Cribbed directly from https://github.com/HannahMitt/HomeMirror/
        // This should keep the tablet from going to sleep.

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + MINUTES_10, MINUTES_10, alarmIntent);
        
        context = getApplicationContext();
    }
    
    public static Context getContext() {
    	return context;
    }
}
