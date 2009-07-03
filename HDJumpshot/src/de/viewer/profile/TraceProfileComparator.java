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

package de.viewer.profile;

import java.util.Comparator;

/**
 * Provides comparators to sort the trace profile list
 * @author Julian M. Kunkel
 *
 */
abstract public class TraceProfileComparator implements Comparator<TraceCategoryStateProfile> {
	@Override
	abstract public int compare(TraceCategoryStateProfile o1, TraceCategoryStateProfile o2);
	
	static public class Normal extends TraceProfileComparator{
		final TraceProfileMetricHandler handler;
		
		public Normal(TraceProfileMetricHandler handler) {
			this.handler = handler;
		}
		
		@Override
		public int compare(TraceCategoryStateProfile arg0,
				TraceCategoryStateProfile arg1) {		
			return handler.getInterestingValue(arg1) > handler.getInterestingValue(arg0) ? + 1 : -1 ;
		}
	}
	
	static public class Reversed extends TraceProfileComparator{
		final TraceProfileMetricHandler handler;
		
		public Reversed(TraceProfileMetricHandler handler) {
			this.handler = handler;
		}
		
		@Override
		public int compare(TraceCategoryStateProfile arg0,
				TraceCategoryStateProfile arg1) {		
			return handler.getInterestingValue(arg1) > handler.getInterestingValue(arg0) ? - 1 : +1 ;
		}
	}
}
