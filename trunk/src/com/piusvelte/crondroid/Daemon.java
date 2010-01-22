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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

public class Daemon extends Service {
	public static String ACTION_CRON = "com.piusvelte.crondroid.intent.action.CRON";
	public static String ACTION_TRIGGER = "com.piusvelte.crondroid.intent.action.TRIGGER";
	public static String ACTION_CONFIGURE = "com.piusvelte.crondroid.intent.action.CONFIGURE";
	private DatabaseManager mDatabaseManager;
	private AlarmManager mAlarmManager;
	private PendingIntent mPendingIntent;
	private int mWakeTime = -1;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;}
	
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
				int n = c.getInt(c.getColumnIndex(DatabaseManager.ACTIVITY_INTERVAL));
				if ((mWakeTime == -1) || (mWakeTime > n)) {
					// next wake up
					mWakeTime = n;}
				if ((System.currentTimeMillis() % n) == 0) {
					// need to account for variance in the time
					String mComponent = c.getString(c.getColumnIndex(DatabaseManager.ACTIVITY_PACKAGE));
					String mComponentName = c.getString(c.getColumnIndex(DatabaseManager.ACTIVITY_TRIGGER));
					startActivity(new Intent(ACTION_TRIGGER).setComponent(new ComponentName(mComponent, mComponentName)));}
				c.moveToNext();}}}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if (mWakeTime != -1) {
    		mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mWakeTime, mPendingIntent);}
		WakeLockManager.release();}}
