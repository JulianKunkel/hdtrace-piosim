package viewer.profile;

import drawable.CategoryState;

/**
 * Contains the profile for one state category and for one timeline.
 * 
 * @author julian
 *
 */
public class TraceCategoryStateProfile {
	final CategoryState category;
	
	//of all calls:
	int numberOfCalls = 0;
	
	// exclusive, means nested states are not counted. 
	double exclusiveTime = 0; 
	// inclusive, means nested states are counted.
	double inclusiveTime = 0;
	
	//of one call
	double minDurationExclusive = Double.MAX_VALUE; 
	double minDurationInclusive = Double.MAX_VALUE;
	double maxDurationExclusive = Double.MIN_VALUE; 
	double maxDurationInclusive = Double.MIN_VALUE;	
	
	public TraceCategoryStateProfile(CategoryState category) 
	{
		this.category = category;
	}
	
	public void addCall(double exclusiveTime, double inclusiveTime){

		this.numberOfCalls++;
		this.exclusiveTime += exclusiveTime;
		this.inclusiveTime += inclusiveTime;
		
		// adapt min/max values:
		minDurationExclusive = (exclusiveTime < minDurationExclusive) ?  exclusiveTime : minDurationExclusive;
		minDurationInclusive = (inclusiveTime < minDurationInclusive) ?  inclusiveTime : minDurationInclusive;
		
		maxDurationExclusive = (exclusiveTime > maxDurationExclusive) ?  exclusiveTime : maxDurationExclusive;
		maxDurationInclusive = (inclusiveTime > maxDurationInclusive) ?  inclusiveTime : maxDurationInclusive;
	}
	
	public int getNumberOfCalls() {
		return numberOfCalls;
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
}
