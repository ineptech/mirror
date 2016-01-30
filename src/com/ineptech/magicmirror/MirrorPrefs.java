package com.ineptech.magicmirror;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MirrorPrefs {
	
	/* 
	 * This is a utility class for saving and loading settings.  It uses SharedPreferences, which is a built-in 
	 * Android class for easily saving data across use sessions.  All modules use it to save their settings.
	 */
	
	private static SharedPreferences prefs = null;
	private Editor editor = null;
	private Context context;
	private String prefsName = "MagicMirrorSavedPreferences";
	
	public MirrorPrefs(Context context) {
		this.context = context;
	}

	public void set(String name, Boolean b) {
		if (prefs == null || editor == null) {
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
			editor = prefs.edit();
		}
		editor.putBoolean(name, b);
		editor.commit();
	}
	public void set(String name, String s) {
		if (prefs == null || editor == null) {
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
			editor = prefs.edit();
		}
		editor.putString(name, s);
		editor.commit();
	}
	public void set(String name, int i) {
		if (prefs == null || editor == null) {
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
			editor = prefs.edit();
		}
		editor.putInt(name, i);
		editor.commit();
	}
	public void set(String name, long i) {
		if (prefs == null || editor == null) {
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
			editor = prefs.edit();
		}
		editor.putLong(name, i);
		editor.commit();
	}
	public String get(String name, String defValue) {
		if (prefs == null) 
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
		return prefs.getString(name, defValue);
	}
	public Boolean get(String name, Boolean defValue) {
		if (prefs == null) 
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
		return prefs.getBoolean(name, defValue);
	}
	public int get(String name, int defValue) {
		if (prefs == null) 
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
		return prefs.getInt(name, defValue);
	}
	public long get(String name, long defValue) {
		if (prefs == null) 
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
		return prefs.getLong(name, defValue);
	}
	public void clear() {
		if (prefs == null || editor == null) {
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
			editor = prefs.edit();
		}
		editor.clear();
		editor.commit();
	}
	public void remove(String name) {
		if (prefs == null || editor == null) {
			prefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
			editor = prefs.edit();
		}
		editor.remove(name);
		editor.commit();
	}
}
