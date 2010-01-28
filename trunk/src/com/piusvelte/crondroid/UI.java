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

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class UI extends ListActivity {
	public static final int REFRESH_ID = Menu.FIRST;
	public static final int ABOUT_ID = Menu.FIRST + 1;
	public static final int CONFIGURE_RESULT = 1;
	private DatabaseManager mDatabaseManager;
	private String mPackage, mTrigger, mConfigure, mLabel;
	private int mInterval;
	private AlertDialog mAlertDialog;
	private DaemonConnection mDaemonConnection;
	private IDaemon mIDaemon;
	private boolean mConfiguring = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	menu.add(0, REFRESH_ID, 0, R.string.refresh_list).setIcon(android.R.drawable.ic_menu_info_details);
    	menu.add(0, ABOUT_ID, 0, R.string.label_about).setIcon(android.R.drawable.ic_menu_more);
    	return result;}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case REFRESH_ID:
    			listActivities();
    			return true;
        	case ABOUT_ID:
        		Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.about);
                dialog.setTitle(R.string.label_about);
                Button donate = (Button) dialog.findViewById(R.id.button_donate);
                donate.setOnClickListener(new OnClickListener() {
    				public void onClick(View v) {
    					startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.bryanemmanuel.com?wapdroid")));}});
                dialog.show();
        		return true;}
        return super.onOptionsItemSelected(item);}
        
    @Override
    public void onResume() {
    	super.onResume();
    	if (mDatabaseManager == null) {
    		mDatabaseManager = new DatabaseManager(this);
    		mDatabaseManager.open();}
    	listActivities();
    	if (mDaemonConnection == null) {
    		mDaemonConnection = new DaemonConnection();
    		bindService(new Intent(this, Daemon.class), mDaemonConnection, Context.BIND_AUTO_CREATE);}}

	@Override
    public void onPause() {
    	super.onPause();
    	if (!mConfiguring) {
    		if (mDatabaseManager != null) {
    			mDatabaseManager.close();
    			mDatabaseManager = null;}
    		if (mDaemonConnection != null) {
    			if (mIDaemon != null) {
    				mIDaemon = null;}
    			unbindService(mDaemonConnection);
    			mDaemonConnection = null;}
    		stopService(new Intent(this, Daemon.class));}}

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
    	super.onListItemClick(list, view, position, id);
    	mPackage = ((ActivityListItem) list.getItemAtPosition(position)).getPkg();
		mTrigger = ((ActivityListItem) list.getItemAtPosition(position)).getTrigger();
		mConfigure = ((ActivityListItem) list.getItemAtPosition(position)).getConfigure();
		mLabel = ((ActivityListItem) list.getItemAtPosition(position)).getLabel();
		mInterval = mDatabaseManager.getInterval(mPackage);
		if ((mPackage != "") && (mTrigger != "") && (mConfigure != "")) {
			String[] intervals = getResources().getStringArray(R.array.interval_values);
			int which = 0;
			for (int i = 0; i < intervals.length; i++) {
				if (mInterval == Integer.parseInt(intervals[i])) {
					which = i;
					break;}}
			Builder b = new AlertDialog.Builder(this);
			b.setSingleChoiceItems(
					R.array.interval_entries,
					which,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mAlertDialog.dismiss();
							mInterval = Integer.parseInt(getResources().getStringArray(R.array.interval_values)[which]);
							configure();}});
			mAlertDialog = b.create();
			mAlertDialog.show();}
		else {
			Toast.makeText(UI.this, getString(R.string.error_communication) + mLabel, Toast.LENGTH_LONG).show();}}
    
    private void configure() {
		// confirm this with the activity
		Intent i = new Intent(Daemon.ACTION_CONFIGURE);
		i.setComponent(new ComponentName(mPackage, mConfigure));
		i.putExtra(Daemon.ACTION_INTERVAL, mInterval);
		// this will cause onPause, set mConfiguring to prevent unbinding the service
		try {
			mConfiguring = true;
			startActivityForResult(i, CONFIGURE_RESULT);}
		catch (ActivityNotFoundException e) {
			mConfiguring = false;
			Toast.makeText(UI.this, getString(R.string.error_communication) + mLabel, Toast.LENGTH_LONG).show();}}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case CONFIGURE_RESULT:
    		mConfiguring = false;
    		switch (resultCode) {
    		case RESULT_OK:
    			// handshake complete
    			mDatabaseManager.setActivity(mPackage, mTrigger, mConfigure, mInterval);
    			return;
    		case RESULT_CANCELED:
    			// handshake failed, stop managing by default
    			mDatabaseManager.deleteActivity(mPackage);
        		return;
    		case 1:
    			Toast.makeText(UI.this, getString(R.string.error_communication) + mLabel, Toast.LENGTH_LONG).show();
    			return;}}}

    public void listActivities() {
    	/*
    	 * list installed packages, matching our filter
    	 * match the list against the managed packages, and auto-delete
    	 * any that no longer appear in the installed list, alerting 
    	 * the user through Toast
    	 */
    	PackageManager mPackageManager = getPackageManager();
    	List<ResolveInfo> triggers = mPackageManager.queryBroadcastReceivers(new Intent(Daemon.ACTION_TRIGGER), 0);
    	List<ActivityListItem> activities = new ArrayList<ActivityListItem>(triggers.size());
    	for (ResolveInfo t : triggers) {
    		String pkg = t.activityInfo.packageName;
    		Log.v("Crondroid.UI", "pkg " + pkg);
    		String configure = "";
    		// find the activity used to configure the receiver
    		// setPackage not supported in 1.5
    		//List<ResolveInfo> configures = mPackageManager.queryIntentActivities(new Intent(Daemon.ACTION_CONFIGURE).setPackage(pkg), 0);
    		List<ResolveInfo> configures = mPackageManager.queryIntentActivities(new Intent(Daemon.ACTION_CONFIGURE), 0);
    		for (ResolveInfo c : configures) {
        		Log.v("Crondroid.UI", "configures " + c.activityInfo.packageName);
    			if (pkg.equals(c.activityInfo.packageName)) {
    				configure = c.activityInfo.name;
            		Log.v("Crondroid.UI", "configure " + configure);
    				break;}}
    		if (!configure.equals("")) {
    			activities.add(new ActivityListItem(
    				pkg,
    				t.activityInfo.name,
    				configure,
    				(String) t.loadLabel(mPackageManager),
    				t.loadIcon(mPackageManager)));}}
    	setListAdapter(new ActivityListAdapter(this,
    			activities));}
    
	public class DaemonConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder boundService) {
			mIDaemon = IDaemon.Stub.asInterface((IBinder) boundService);}		
		public void onServiceDisconnected(ComponentName className) {
			mIDaemon = null;}}}