package viewer.profile;

import java.util.List;



/**
 * This class represents a mapping from the trace profiles to one timeline,
 * this includes the presentation order of the different categories.
 * 
 * @author julian
 */
public class TraceObjectProfileMap {
	// increasing time of the interesting criterion beginning by 0
	final double [] values;
	
	// the profiles matching the time
	final TraceCategoryStateProfile [] profiles;
	
	/**
	 * Create a increasing array of values of the sorted list with the value handler.
	 * 
	 * @param listWithTheOrder
	 * @param handler
	 */
	public TraceObjectProfileMap(List<TraceCategoryStateProfile> listWithTheOrder, TraceProfileValueHandler handler) {
		values = new double[listWithTheOrder.size()];
		profiles = new TraceCategoryStateProfile[listWithTheOrder.size()];
		
		int pos = 0;
		double value = 0;
		for(TraceCategoryStateProfile cur: listWithTheOrder){
			profiles[pos] = cur;
			
			value += handler.getInterestingValue(cur);
			values[pos] = value;
			
			pos++;
		}
	}

	/**
	 * Do a binary search to find the profile for a given time.
	 * @param modelTime
	 * @return
	 */
	public TraceCategoryStateProfile getProfileWithTime(double modelTime){
		// check for bounds:
		if(modelTime < 0 || modelTime > getMaxValue())
			return null;

		int min = 0; 
		int max = values.length - 1;
		
		while(true){						
			final int cur = (min + max) / 2;
			final double entry = values[cur];
			
			if(min == max){ // found entry or stopped.				
				return profiles[cur];
			} 
			// not found => continue bin search:

			if ( entry > modelTime ){
				max = cur;
			}else{
				min = cur + 1;
			}
		}
	}
	
	public TraceCategoryStateProfile[] getProfiles() {
		return profiles;
	}
	
	/**
	 * Maximum value/time obtained during creation.
	 * @return
	 */
	public double getMaxValue(){
		if(values.length == 0) 
			return 0;
		return values[values.length-1];
	}
	
	public double[] getValues() {
		return values;
	}	
}
