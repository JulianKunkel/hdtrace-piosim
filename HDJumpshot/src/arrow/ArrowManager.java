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

import hdTraceInput.TraceFormatBufferedFileReader;

import java.util.Collection;
import java.util.HashMap;

import viewer.legends.CategoryUpdatedListener;
import de.hd.pvs.TraceFormat.util.Epoch;
import drawable.Category;

/**
 * Manages arrows: allows to remove and add groups of arrows. Arrows are typically computed
 * on the fly. 
 * Computes arrows lazily once the category gets visible the first time.
 * Examples for groups are all individual MPI communication related arrows.
 * 
 * @author Julian M. Kunkel
 */
public class ArrowManager extends CategoryUpdatedListener{
	
	final HashMap<ArrowCategory, ManagedArrowGroup> groups = new HashMap<ArrowCategory, ManagedArrowGroup>();
	
	final static Class<?> [] existingComputers = {ClientMPICommunicationArrowComputer.class, RelationArrowComputer.class};
	
	final TraceFormatBufferedFileReader reader;
		
	public ArrowManager(TraceFormatBufferedFileReader reader) {
		this.reader = reader;
		for(Class<?> cls: existingComputers){
			try{
				final ArrowComputer computer = (ArrowComputer) cls.newInstance();
				groups.put(computer.getResponsibleCategory(), new ManagedArrowGroup(computer));
				
				computer.getResponsibleCategory().setVisible(false);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void categoryVisibilityModified(Category category, boolean value) {
		if(! ArrowCategory.class.isInstance(category)){
			return;
		}
		if(value == true){
			computeGroupIfNotComputed((ArrowCategory) category);
		}
	}
	
	public Collection<ManagedArrowGroup> getManagedGroups(){
		return groups.values();
	}
	
	/**
	 * Remove all computed arrows.
	 */
	public void clear(){
		for(ManagedArrowGroup group: groups.values()){
			group.clearComputedState();
		}
	}
	
	public ArrowsOrdered getArrows(ArrowCategory category) {
		return groups.get(category).getArrowsOrdered();
	}
		
	public ManagedArrowGroup getGroup(ArrowCategory category) {
		return groups.get(category);
	}
	
	public void recomputeGroup(ArrowCategory category){
		final ManagedArrowGroup group = groups.get(category);
		group.setComputeResults(group.getComputer().computeArrows(reader));
		category.setManagedGroup(group);
	}
	
	private void computeGroupIfNotComputed(ArrowCategory category){
		final ManagedArrowGroup group = groups.get(category);
		if(group.isComputed()){
			return;
		}
		recomputeGroup(category);
	}
	
	/**
	 * Enumerates only visible arrows!
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public VisibleArrowEnumerator getArrowEnumeratorVisible(Epoch startTime, Epoch endTime){
		return new VisibleArrowEnumerator(groups.values().iterator(), startTime, endTime);
	}	
}
