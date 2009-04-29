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

import drawable.CategoryState;

/**
 * Contains the profile for one state category and for one timeline.
 * 
 * @author Julian M. Kunkel
 *
 */
public class TraceCategoryStateProfile {
	final CategoryState category;
	
	//of all calls:
	int numberOfOccurences = 0;
	
	// exclusive, means nested states are not counted. 
	double exclusiveTime = 0; 
	// inclusive, means nested states are counted.
	double inclusiveTime = 0;
	
	//of one call
	double minDurationExclusive = Double.MAX_VALUE; 
	double minDurationInclusive = Double.MAX_VALUE;
	double maxDurationExclusive = Double.MIN_VALUE; 
	double maxDurationInclusive = Double.MIN_VALUE;	
	
	final TraceProfileFrame profileFrame;
	
	public TraceCategoryStateProfile(CategoryState category, TraceProfileFrame profileFrame) 
	{
		this.category = category;
		this.profileFrame = profileFrame;
	}
	
	public void addCall(double exclusiveTime, double inclusiveTime){

		this.numberOfOccurences++;
		this.exclusiveTime += exclusiveTime;
		this.inclusiveTime += inclusiveTime;
		
		// adapt min/max values:
		minDurationExclusive = (exclusiveTime < minDurationExclusive) ?  exclusiveTime : minDurationExclusive;
		minDurationInclusive = (inclusiveTime < minDurationInclusive) ?  inclusiveTime : minDurationInclusive;
		
		maxDurationExclusive = (exclusiveTime > maxDurationExclusive) ?  exclusiveTime : maxDurationExclusive;
		maxDurationInclusive = (inclusiveTime > maxDurationInclusive) ?  inclusiveTime : maxDurationInclusive;
	}
	
	public int getNumberOfOccurrences() {
		return numberOfOccurences;
	}
	
	public double getExclusiveTime() {
		return exclusiveTime;
	}
	
	public double getInclusiveTime() {
		return inclusiveTime;
	}
	
	public CategoryState getCategory() {
		return category;
	}

	public double getMaxDurationExclusive() {
		return maxDurationExclusive;
	}
	
	public double getMaxDurationInclusive() {
		return maxDurationInclusive;
	}
	
	public double getMinDurationExclusive() {
		return minDurationExclusive;
	}
	
	public double getMinDurationInclusive() {
		return minDurationInclusive;
	}
	
	public TraceProfileFrame getProfileFrame() {
		return profileFrame;
	}
}
