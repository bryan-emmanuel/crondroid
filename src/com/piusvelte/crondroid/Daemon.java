/*
 * Crondroid - Android process scheduler
 * Copyright (C) 2009 Bryan Emmanuel
 * 
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Bryan Emmanuel piusvelte@gmail.com
 */

package com.piusvelte.crondroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

public class Daemon extends Service {
	public static String ACTION_CRON = "com.piusvelte.crondroid.intent.action.CRON";
	public static String ACTION_TRIGGER = "com.piusvelte.crondroid.intent.action.TRIGGER";
	public static String ACTION_CONFIGURE = "com.piusvelte.crondroid.intent.action.CONFIGURE";
	public static String ACTION_INTERVAL = "com.piusvelte.crondroid.intent.action.INTERVAL";
	private DatabaseManager mDatabaseManager;
	private int mWakeTime = 0;
	
	private final IDaemon.Stub mIDaemon = new IDaemon.Stub() {};
	
	@Override
	public IBinder onBind(Intent intent) {
		return mIDaemon;}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStart(intent, startId);
		return 0;}
	
    @Override
    public void onCreate() {
        super.onCreate();
        /*
         * the daemon should check that a managed activity is still
         * installed before attempting to trigger it this will also
         * provide an opportunity to cleanup activities that we're
         * uninstalled but left configured in crondroid
         * 
         * 
         * NEED TO START OUT ON A COMMON DENOMINATOR TIME
         * 
         * 
         */
        int now = (int) System.currentTimeMillis();
        now = (int) Math.floor(now / 1000);
        now *= 1000;
		mDatabaseManager = new DatabaseManager(this);
		mDatabaseManager.open();
		Cursor c = mDatabaseManager.getActivities();
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				int n = c.getInt(c.getColumnIndex(DatabaseManager.ACTIVITY_INTERVAL));
				Log.v("Crondroid.Daemon", "n " + n);
				if ((mWakeTime == 0) || (mWakeTime > n)) {
					// set the next wake time for the alarm
					mWakeTime = n;
					Log.v("Crondroid.Daemon", "set wake " + mWakeTime);}
				Log.v("Crondroid.Daemon", "now % n " + now % n);
				if ((now % n) == 0) {
					// need to account for variance in the time
					String cm = c.getString(c.getColumnIndex(DatabaseManager.ACTIVITY_PACKAGE));
					String cn = c.getString(c.getColumnIndex(DatabaseManager.ACTIVITY_TRIGGER));
					Log.v("Crondroid.Daemon", "trigger " + cn);
					try {
						startActivity(new Intent(ACTION_TRIGGER).setComponent(new ComponentName(cm, cn)));}
					catch (ActivityNotFoundException e) {
						// where's the activity?
					}}
				c.moveToNext();}
			mWakeTime += now;}
		c.close();}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.v("Crondroid.Daemon", "destroy " + mWakeTime);
    	if (mWakeTime != 0) {
    		Log.v("Crondroid.Daemon", "set the alarm");
    		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    		Intent i = new Intent(this, DaemonManager.class);
    		i.setAction(ACTION_CRON);
    		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
    		am.set(AlarmManager.RTC_WAKEUP, mWakeTime, pi);}
		WakeLockManager.release();}}
