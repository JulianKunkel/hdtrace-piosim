package viewer.profile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

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
	final HashMap<Integer, LinkedList<TraceCategoryStateProfile>> categoryProfiles = new HashMap<Integer, LinkedList<TraceCategoryStateProfile>>(); 
	
	private LinkedList<TraceCategoryStateProfile> getProfileForTimeline(int timeline){
		LinkedList<TraceCategoryStateProfile> profileInfo = categoryProfiles.get(timeline);
		if(profileInfo == null){
			profileInfo = new LinkedList<TraceCategoryStateProfile>();
			categoryProfiles.put(timeline, profileInfo);
		}
		return profileInfo;
	}
	
	public void addProfileInformation(int timeline, LinkedList<TraceCategoryStateProfile> profile){
		categoryProfiles.put(timeline, profile);
	}
	
	public Iterator<TraceCategoryStateProfile> getProfileIteratorUnsorted(int timeline){
		final LinkedList<TraceCategoryStateProfile> profileInfo = getProfileForTimeline(timeline);
		return profileInfo.iterator();
	}

	public Iterator<TraceCategoryStateProfile> getProfileIteratorSortByName(int timeline){
		final LinkedList<TraceCategoryStateProfile> profileInfo = getProfileForTimeline(timeline);
		return profileInfo.iterator();
	}
	
	public Iterator<TraceCategoryStateProfile> getProfileIteratorSortByInclusiveTime(int timeline){
		final LinkedList<TraceCategoryStateProfile> profileInfo = getProfileForTimeline(timeline);
		return profileInfo.iterator();
	}
	
	public Iterator<TraceCategoryStateProfile> getProfileIteratorSortByExclusiveTime(int timeline){
		final LinkedList<TraceCategoryStateProfile> profileInfo = getProfileForTimeline(timeline);
		return profileInfo.iterator();
	}
}
