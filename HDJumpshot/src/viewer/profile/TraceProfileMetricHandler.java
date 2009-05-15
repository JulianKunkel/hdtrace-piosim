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

/**
 * Is a filter class which returns the value of the profile we are interested in. 
 * For instance on might want to visualize inclusive time, another exclusive time,
 * number of calls etc...
 * 
 * @author Julian M. Kunkel
 *
 */
abstract public class TraceProfileMetricHandler {
	
	/**
	 * Return the value we are interested in
	 * @param profile
	 * @return
	 */
	abstract public double getInterestingValue(TraceCategoryStateProfile profile);
	
	static public class InclusiveTimeHandler extends TraceProfileMetricHandler{
		@Override
		public double getInterestingValue(TraceCategoryStateProfile profile) {		
			return profile.getInclusiveTimeDouble();
		}
	}
	
	static public class ExclusiveTimeHandler extends TraceProfileMetricHandler{
		@Override
		public double getInterestingValue(TraceCategoryStateProfile profile) {		
			return profile.getExclusiveTimeDouble();
		}
	}
	
	static public class NumberOfOccurrenceHandler extends TraceProfileMetricHandler{
		@Override
		public double getInterestingValue(TraceCategoryStateProfile profile) {		
			return profile.numberOfOccurences;
		}
	}
	
	static public class MaxInclusiveTimeHandler extends TraceProfileMetricHandler{
		@Override
		public double getInterestingValue(TraceCategoryStateProfile profile) {		
			return profile.maxDurationInclusive;
		}
	}
	
	static public class MaxExclusiveTimeHandler extends TraceProfileMetricHandler{
		@Override
		public double getInterestingValue(TraceCategoryStateProfile profile) {		
			return profile.maxDurationExclusive;
		}
	}
}
