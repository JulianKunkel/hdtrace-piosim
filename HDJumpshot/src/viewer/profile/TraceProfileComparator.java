package viewer.profile;

import java.util.Comparator;

/**
 * Provides comparators to sort the trace profile list
 * @author julian
 *
 */
abstract public class TraceProfileComparator implements Comparator<TraceCategoryStateProfile> {
	@Override
	abstract public int compare(TraceCategoryStateProfile o1, TraceCategoryStateProfile o2);
	
	static public class Normal extends TraceProfileComparator{
		final TraceProfileValueHandler handler;
		
		public Normal(TraceProfileValueHandler handler) {
			this.handler = handler;
		}
		
		@Override
		public int compare(TraceCategoryStateProfile arg0,
				TraceCategoryStateProfile arg1) {		
			return (int) (handler.getInterestingValue(arg0) - handler.getInterestingValue(arg1));
		}
	}
	
	static public class Reversed extends TraceProfileComparator{
		final TraceProfileValueHandler handler;
		
		public Reversed(TraceProfileValueHandler handler) {
			this.handler = handler;
		}
		
		@Override
		public int compare(TraceCategoryStateProfile arg0,
				TraceCategoryStateProfile arg1) {		
			return (int) (handler.getInterestingValue(arg1) - handler.getInterestingValue(arg0));
		}
	}
}
