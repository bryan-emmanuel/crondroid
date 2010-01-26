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

public class Daemon extends Service {
	public static String ACTION_CRON = "com.piusvelte.crondroid.intent.action.CRON";
	public static String ACTION_TRIGGER = "com.piusvelte.crondroid.intent.action.TRIGGER";
	public static String ACTION_CONFIGURE = "com.piusvelte.crondroid.intent.action.CONFIGURE";
	public static String ACTION_INTERVAL = "com.piusvelte.crondroid.intent.action.INTERVAL";
	private DatabaseManager mDatabaseManager;
	AlarmManager mAlarmManager;
	PendingIntent mPendingIntent;
	
	
	private final IDaemon.Stub mIDaemon = new IDaemon.Stub() {};
	
	@Override
	public IBinder onBind(Intent intent) {
		// cancel the alarm
    	mAlarmManager.cancel(mPendingIntent);
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
         */
        long now = ((long) Math.floor((System.currentTimeMillis() / 1000))) * 1000;
		mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, DaemonManager.class);
		i.setAction(ACTION_CRON);
		mPendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
		mDatabaseManager = new DatabaseManager(this);
		mDatabaseManager.open();
		Cursor c = mDatabaseManager.getActivities();
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				long n = c.getLong((c.getColumnIndex(DatabaseManager.ACTIVITY_INTERVAL)));
				if (n > 0) {
					if ((now % n) == 0) {
						// need to account for variance in the time
						String pkg = c.getString(c.getColumnIndex(DatabaseManager.ACTIVITY_PACKAGE));
						String trigger = c.getString(c.getColumnIndex(DatabaseManager.ACTIVITY_TRIGGER));
						try {
							sendBroadcast(new Intent(ACTION_TRIGGER).setComponent(new ComponentName(pkg, trigger)));}
						catch (ActivityNotFoundException e) {
							// if the activity doesn't exist, stop managing it
			    			mDatabaseManager.deleteActivity(pkg);}}}
				c.moveToNext();}}
		c.close();
		stopSelf();}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if (mDatabaseManager != null) {
    		long interval = mDatabaseManager.getMinInterval();
    		if (interval != 0) {
    			// need to wake at now + interval where now % interval == 0
    			long now = System.currentTimeMillis();
    			mAlarmManager.set(AlarmManager.RTC_WAKEUP, (interval + (now - (now % interval))), mPendingIntent);}
    		mDatabaseManager.close();
    		mDatabaseManager = null;}
		WakeLockManager.release();}}
