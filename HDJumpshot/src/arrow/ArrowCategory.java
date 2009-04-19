
/** Version Control Information $Id: CategoryEvent.java 205 2009-04-11 15:33:40Z kunkel $
 * @lastmodified    $Date: 2009-04-11 17:33:40 +0200 (Sa, 11 Apr 2009) $
 * @modifiedby      $LastChangedBy: kunkel $
 * @version         $Revision: 205 $ 
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


package arrow;

import drawable.Category;
import drawable.ColorAlpha;
import drawable.VisualizedObjectType;


public class ArrowCategory extends Category {
	
	ManagedArrowGroup managedGroup;

	public ArrowCategory(String in_name, ColorAlpha in_color ) {
		super(in_name, in_color);
	}
	
	void setManagedGroup(ManagedArrowGroup group) {
		this.managedGroup = group;
	}
	
	public ManagedArrowGroup getManagedGroup() {
		return managedGroup;
	}
	
	@Override
	public void setSearchable(boolean new_value) {
		
	}
	
	@Override
	public boolean isSearchable() {		
		return false;
	}

	@Override
	public VisualizedObjectType getTopologyType() {
		return VisualizedObjectType.ARROW;
	}
}
