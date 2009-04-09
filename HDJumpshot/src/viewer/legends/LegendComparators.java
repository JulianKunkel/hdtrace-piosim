
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

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


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */
package viewer.legends;

import java.util.Comparator;

import drawable.Category;

public class LegendComparators
{
	private static final Comparator<Category> TOPOLOGY_ORDER	= new TopologyOrder();
	public static final Comparator<Category> TOPOLOGY_NAME_ORDER = new TopologyNameOrder();

	public  static final Comparator<Category> CASE_SENSITIVE_ORDER	= new CaseSensitiveOrder();
	public  static final Comparator<Category> CASE_INSENSITIVE_ORDER	= new CaseInsensitiveOrder();


	public static class TopologyNameOrder implements Comparator<Category>
	{
		public int compare( Category type1, Category type2 )
		{
			int      diff       = 0;
			diff = TOPOLOGY_ORDER.compare( type1, type2 );
			if ( diff != 0 )
				return diff;
			return type1.getName().compareToIgnoreCase( type2.getName() );
		}
	}


	public static class TopologyOrder implements Comparator<Category>
	{
		public int compare( Category o1, Category o2 )
		{
			return o1.getTopologyType().ordinal() - o2.getTopologyType().ordinal(); 
			// intentionally reversed, so arrow < state < event
		}
	}

	public static class CaseSensitiveOrder implements Comparator<Category>
	{
		public int compare( Category o1, Category o2 )
		{
			return o1.getName().compareTo( o2.getName() );
		}
	}

	public static class CaseInsensitiveOrder implements Comparator<Category>
	{
		public int compare( Category o1, Category o2 )
		{
			return o1.getName().compareToIgnoreCase( o2.getName() );
		}
	}
}
