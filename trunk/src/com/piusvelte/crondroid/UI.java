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

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
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
	public static final int INTERVAL_RESULT = 1;
	public static final int CONFIGURE_RESULT = 2;
	private DatabaseManager mDatabaseManager;
	private String mPackage;
	private String mTrigger;
	private String mConfigure;
	private int mInterval;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	menu.add(0, REFRESH_ID, 0, R.string.refresh_list).setIcon(android.R.drawable.ic_menu_info_details);
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
    public void onPause() {
    	super.onPause();}
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (mDatabaseManager == null) {
    		mDatabaseManager = new DatabaseManager(this);
    		mDatabaseManager.open();}
    	listActivities();}

	@Override
	protected void onDestroy() {
		super.onDestroy();
    	if (mDatabaseManager != null) {
    		mDatabaseManager.close();
    		mDatabaseManager = null;}}

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
    	super.onListItemClick(list, view, position, id);
    	mPackage = ((ActivityListItem) getListView().getItemAtPosition(position)).getPkg();
		mTrigger = ((ActivityListItem) getListView().getItemAtPosition(position)).getTrigger();
		mConfigure = ((ActivityListItem) getListView().getItemAtPosition(position)).getConfigure();
		// if missing elements, toast an error
		Toast.makeText(UI.this, "error", Toast.LENGTH_LONG).show();
		// get interval from database
    	Intent i = new Intent(this, SetInterval.class);
    	i.putExtra(DatabaseManager.ACTIVITY_INTERVAL, mDatabaseManager.getInterval(mPackage));
    	startActivityForResult(i, INTERVAL_RESULT);}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    		case INTERVAL_RESULT:
    			if (resultCode == RESULT_OK) {
    				mInterval = data.getIntExtra(DatabaseManager.ACTIVITY_INTERVAL, 0);
    				// confirm this with the activity
    				startActivityForResult(new Intent(Daemon.ACTION_CONFIGURE).setPackage(mPackage), CONFIGURE_RESULT);}
    			else {
    				Toast.makeText(UI.this, "error", Toast.LENGTH_LONG).show();}
    		case CONFIGURE_RESULT:
    			if (resultCode == RESULT_OK) {
    				// handshake complete
    				mDatabaseManager.setActivity(mPackage, mTrigger, mConfigure, mInterval);}
    			else {
    				Toast.makeText(UI.this, "error", Toast.LENGTH_LONG).show();}}}

    public void listActivities() {
    	/*
    	 * list installed packages, matching our filter
    	 * match the list against the managed packages, and auto-delete
    	 * any that no longer appear in the installed list, alerting 
    	 * the user through Toast
    	 */
    	PackageManager mPackageManager = getPackageManager();
    	List<ResolveInfo> results = mPackageManager.queryBroadcastReceivers(new Intent(Daemon.ACTION_TRIGGER), 0);
    	List<ActivityListItem> activities = new ArrayList<ActivityListItem>(results.size());
    	for (ResolveInfo a : results) {
    		String pkg = a.activityInfo.packageName;
    		String configure = "";
    		// find the activity used to configure the receiver
    		List<ResolveInfo> l = mPackageManager.queryIntentActivities(new Intent(Daemon.ACTION_CONFIGURE).setPackage(pkg), 0);
    		if (l.size() > 0) {
    			configure = l.get(0).activityInfo.name;}
    		activities.add(new ActivityListItem(
    				pkg,
    				a.activityInfo.name,
    				configure,
    				(String) a.loadLabel(mPackageManager),
    				a.loadIcon(mPackageManager)));}
    	setListAdapter(new ActivityListAdapter(this,
    			activities));}}