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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager {
	private static final String DATABASE_NAME = "crondroid.db";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_ACTIVITY = "activity";
	public static final String ACTIVITY_ID = "_id";
	public static final String ACTIVITY_PACKAGE = "package";
	public static final String ACTIVITY_TRIGGER = "trigger";
	public static final String ACTIVITY_CONFIGURE = "configure";
	public static final String ACTIVITY_INTERVAL = "interval";
	
	private static final String CREATE_ACTIVITY = "create table "
		+ TABLE_ACTIVITY + " ("
		+ ACTIVITY_ID + " integer primary key autoincrement, "
		+ ACTIVITY_PACKAGE + " text not null, "
		+ ACTIVITY_TRIGGER + " text not null, "
		+ ACTIVITY_CONFIGURE + " text not null, "
		+ ACTIVITY_INTERVAL + " integer);";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

    public final Context mContext;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);}
        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(CREATE_ACTIVITY);}
        
        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {}}
    
    public DatabaseManager(Context context) {
        this.mContext = context;}
    
    public DatabaseManager open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;}

    public void close() {
        mDbHelper.close();}
        
    public void createTables() {
        mDb.execSQL(CREATE_ACTIVITY);}
    
    public int setActivity(String pkg, String trigger, String configure, int interval) {
    	int i = -1;
		ContentValues values = new ContentValues();
    	Cursor c = mDb.rawQuery("SELECT "
    			+ ACTIVITY_ID + ", "
    			+ ACTIVITY_INTERVAL
    			+ " FROM " + TABLE_ACTIVITY
    			+ " WHERE " + ACTIVITY_PACKAGE + "=\"" + pkg + "\"", null);
    	if (c.getCount() > 0) {
    		c.moveToFirst();
    		i = c.getInt(c.getColumnIndex(ACTIVITY_ID));
    		if (c.getInt(c.getColumnIndex(ACTIVITY_INTERVAL)) != interval) {
    			Log.v("Crondroid.DatabaseManager", "set interval " + interval);
    			values.put(ACTIVITY_INTERVAL, interval);
        		mDb.update(TABLE_ACTIVITY, values, ACTIVITY_ID + "=" + i, null);}}
    	else {
    		values.put(ACTIVITY_PACKAGE, pkg);
    		values.put(ACTIVITY_TRIGGER, trigger);
    		values.put(ACTIVITY_CONFIGURE, configure);
    		values.put(ACTIVITY_INTERVAL, interval);
    		i = (int) mDb.insert(TABLE_ACTIVITY, null, values);}
    	c.close();
    	return i;}
    
    public Cursor getActivities() {
    	return mDb.rawQuery("SELECT "
    			+ ACTIVITY_ID + ", "
    			+ ACTIVITY_PACKAGE + ", "
    			+ ACTIVITY_TRIGGER + ", "
    			+ ACTIVITY_INTERVAL
    			+ " FROM " + TABLE_ACTIVITY, null);}
    
    public int getInterval(String pkg) {
    	int interval = 0;
    	Cursor c = mDb.rawQuery("SELECT "
    			+ ACTIVITY_ID + ", "
    			+ ACTIVITY_INTERVAL
    			+ " FROM " + TABLE_ACTIVITY
    			+ " WHERE " + ACTIVITY_PACKAGE + "=\"" + pkg + "\"", null);
    	if (c.getCount() > 0) {
    		c.moveToFirst();
    		interval = c.getInt(c.getColumnIndex(ACTIVITY_INTERVAL));}
    	c.close();
    	return interval;}

	public void deleteActivity(String pkg) {
		mDb.delete(TABLE_ACTIVITY, ACTIVITY_PACKAGE + "=\"" + pkg + "\"", null);}}