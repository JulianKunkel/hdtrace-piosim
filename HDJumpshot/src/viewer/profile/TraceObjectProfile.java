//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.

package viewer.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * 
 * Contains the profile for all (visible) categories and all timelines.
 * Provides iterators to go through different sortings of the states.
 * 
 * @author Julian M. Kunkel
 *
 */
public class TraceObjectProfile {
	//final HashMap<CategoryState, TraceCategoryStateProfile> categoryProfiles = new HashMap<CategoryState, TraceCategoryStateProfile>();
	final HashMap<Integer, ArrayList<TraceCategoryStateProfile>> categoryProfiles = new HashMap<Integer, ArrayList<TraceCategoryStateProfile>>(); 
	
	private ArrayList<TraceCategoryStateProfile> getProfileForTimeline(int timeline){
		ArrayList<TraceCategoryStateProfile> profileInfo = categoryProfiles.get(timeline);
		if(profileInfo == null){
			profileInfo = new ArrayList<TraceCategoryStateProfile>();
			categoryProfiles.put(timeline, profileInfo);
		}
		return profileInfo;
	}
	
	public void addProfileInformation(int timeline, ArrayList<TraceCategoryStateProfile> profile){
		categoryProfiles.put(timeline, profile);
	}
	
	/**
	 * Return the current sorting order of the list.
	 * @param timeline
	 * @return
	 */
	public ArrayList<TraceCategoryStateProfile> getProfileCurrentSortOrder(int timeline){
		return getProfileForTimeline(timeline);
	}

	public ArrayList<TraceCategoryStateProfile> getProfileSortedBy(int timeline, TraceProfileComparator comparator){
		ArrayList<TraceCategoryStateProfile> list = getProfileForTimeline(timeline); 
		Collections.sort(list, comparator);
		return list;
	}
}
