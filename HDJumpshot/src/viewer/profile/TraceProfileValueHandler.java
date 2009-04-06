package viewer.profile;

/**
 * Is a filter class which returns the value of the profile we are interested in. 
 * For instance on might want to visualize inclusive time, another exclusive time,
 * number of calls etc...
 * 
 * @author julian
 *
 */
abstract public class TraceProfileValueHandler {
	
	/**
	 * Return the value we are interested in
	 * @param profile
	 * @return
	 */
	abstract public double getInterestingValue(TraceCategoryStateProfile profile);
	
	static public class InclusiveTimeHandler extends TraceProfileValueHandler{
		@Override
		public double getInterestingValue(TraceCategoryStateProfile profile) {		
			return profile.inclusiveTime;
		}
	}
	
	static public class ExclusiveTimeHandler extends TraceProfileValueHandler{
		@Override
		public double getInterestingValue(TraceCategoryStateProfile profile) {		
			return profile.exclusiveTime;
		}
	}
	
	static public class NumberOfCallsHandler extends TraceProfileValueHandler{
		@Override
		public double getInterestingValue(TraceCategoryStateProfile profile) {		
			return profile.numberOfCalls;
		}
	}
}
