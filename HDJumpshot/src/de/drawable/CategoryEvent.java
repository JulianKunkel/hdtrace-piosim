
/** Version Control Information $Id: CategoryEvent.java 220 2009-04-18 19:12:42Z kunkel $
 * @lastmodified    $Date: 2009-04-18 21:12:42 +0200 (Sa, 18. Apr 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 220 $ 
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


package de.drawable;


public class CategoryEvent extends Category {

	private boolean         isSearchable = true;

	public CategoryEvent(String in_name, ColorAlpha in_color ) {
		super(in_name, in_color);
	}

	public void setSearchable( boolean new_value )
	{
		isSearchable = new_value;
	}

	public boolean isSearchable()
	{
		return isSearchable;
	}

	@Override
	public VisualizedObjectType getTopologyType() {
		return VisualizedObjectType.EVENT;
	}
}
