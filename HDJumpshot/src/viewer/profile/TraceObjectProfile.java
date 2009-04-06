package viewer.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * 
 * Contains the profile for all (visible) categories and all timelines.
 * Provides iterators to go through different sortings of the states.
 * 
 * @author julian
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
