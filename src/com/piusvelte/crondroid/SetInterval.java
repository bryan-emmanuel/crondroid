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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SetInterval extends ListActivity {
	private Button mCancel;
	private int mInterval;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intervals);
        Intent i = getIntent();
        mInterval = i.getIntExtra(DatabaseManager.ACTIVITY_INTERVAL, 0);
        mCancel = (Button) findViewById(R.id.cancel);
        mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();}});
        setListAdapter(new ArrayAdapter<String>(this, R.layout.interval, getResources().getStringArray(R.array.interval_entries)));}

	@Override
	public void finish()
	{
		Intent i = new Intent();
        i.putExtra(DatabaseManager.ACTIVITY_INTERVAL, mInterval);
		setResult(RESULT_OK, i);}
	
    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
    	super.onListItemClick(list, view, position, id);
    	mInterval = Integer.parseInt(getResources().getStringArray(R.array.interval_values)[position]);
    	finish();}}
