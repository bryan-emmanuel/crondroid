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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityListAdapter extends BaseAdapter {
	private List<ActivityListItem> mActivityList;
	private LayoutInflater mInflater;
	
	public ActivityListAdapter(Context context, List<ActivityListItem> activityList) {
		mActivityList = activityList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);}

	@Override
	public int getCount() {
		return mActivityList.size();}

	@Override
	public Object getItem(int position) {
		return mActivityList.get(position);}

	@Override
	public long getItemId(int position) {
		return position;}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = mInflater.inflate(R.layout.activity, parent, false);}
		else {
			v = convertView;}
		ActivityListItem activity = mActivityList.get(position);
		TextView label = (TextView)	v.findViewById(R.id.activity_label);
		label.setText(activity.getLabel());
		ImageView icon = (ImageView) v.findViewById(R.id.activity_icon);
		icon.setImageDrawable(activity.getIcon());
		return v;}}
