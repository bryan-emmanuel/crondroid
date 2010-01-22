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

import android.graphics.drawable.Drawable;

public class ActivityListItem {
	public String mPkg;
	public String mTrigger;
	public String mConfigure;
	public String mLabel;
	public Drawable mIcon;
	
	public ActivityListItem(String pkg, String trigger, String configure, String label, Drawable icon) {
		mPkg = pkg;
		mTrigger = trigger;
		mConfigure = configure;
		mLabel = label;
		mIcon = icon;}
	
	public String getPkg() {
		return mPkg;}
	
	public String getTrigger() {
		return mTrigger;}

	public String getConfigure() {
		return mConfigure;}
	
	public String getLabel() {
		return mLabel;}
	
	public Drawable getIcon() {
		return mIcon;}}
