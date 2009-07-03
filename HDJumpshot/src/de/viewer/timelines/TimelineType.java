
 /** Version Control Information $Id: TimelineType.java 404 2009-06-16 12:08:03Z kunkel $
  * @lastmodified    $Date: 2009-06-16 14:08:03 +0200 (Di, 16. Jun 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 404 $ 
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


package de.viewer.timelines;

public enum TimelineType {
	TRACE,
	STATISTIC,
	RELATION,
	RELATION_EXPANDED,
	INNER_NODE,
	INVALID_TIMELINE
}
