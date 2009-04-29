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

/**
 * A group serves a purpose for instance MPI individual communication or 
 * client/server I/O communication. 
 * 
 * @author Julian M. Kunkel
 */
public class ManagedArrowGroup{
	private ArrowsOrdered arrowsOrdered = null;
	private final ArrowCategory category;		
	
	private final ArrowComputer computer;
	private boolean wasComputed = false;
	
	public ManagedArrowGroup(ArrowComputer computer) {
		this.computer = computer;
		this.category = computer.getResponsibleCategory();
	}

	public boolean isComputed() {
		return wasComputed;
	}
	
	public ArrowCategory getCategory() {
		return category;
	}
	
	public ArrowsOrdered getArrowsOrdered() {
		return arrowsOrdered;
	}
	
	ArrowComputer getComputer() {
		return computer;
	}
	
	void setComputeResults(ArrowsOrdered arrowsOrdered){
		wasComputed = true;
		this.arrowsOrdered = arrowsOrdered;
	}
	
	void clearComputedState(){
		this.arrowsOrdered = null;
		wasComputed = false;
	}
}