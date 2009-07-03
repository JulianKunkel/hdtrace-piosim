
 /** Version Control Information $Id: CategoryUpdatedListener.java 469 2009-07-01 13:27:24Z kunkel $
  * @lastmodified    $Date: 2009-07-01 15:27:24 +0200 (Mi, 01. Jul 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 469 $ 
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


package de.viewer.legends;

import java.util.EventListener;

import de.drawable.Category;

public class CategoryUpdatedListener implements EventListener{
	/**
	 * Called for each category which is modified
	 */
	public void categoryVisibilityModified(Category category, boolean value){
		
	}
	
	/**
	 * Called once after one or multiple visibilities were modified
	 */
	public void categoryVisibilityWasModified(){
		
	}
	
	/**
	 * Called for each category which is modified
	 */
	public void categoryAttributeModified(Category category, Object newValue){
		
	}

	/**
	 * Called once after one or multiple attributes were modified
	 */
	public void categoryAttributesWereModified(){
		
	}
}
