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

import java.util.List;



/**
 * This class represents a mapping from the trace profiles to one timeline,
 * this includes the presentation order of the different categories.
 * 
 * @author Julian M. Kunkel
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
	public TraceObjectProfileMap(List<TraceCategoryStateProfile> listWithTheOrder, TraceProfileMetricHandler handler) {
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
		
		if(max == -1){
			// if no profile entry.
			return null;
		}
		
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
