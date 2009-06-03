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

/**
 * 
 */
package viewer.datatype;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import viewer.dialog.traceEntries.ResizeListener;
import de.hd.pvs.TraceFormat.project.datatypes.Datatype;

/**
 * Layout the visible datatypes in a dependency graph.
 * @author Julian M. Kunkel
 *
 */
class DatatypeLayoutManager implements LayoutManager2{
	private final DatatypeView datatypeView;

	DatatypeLayoutManager(DatatypeView datatypeView) {
		this.datatypeView = datatypeView;
	}

	private ResizeListener datatypeViewChangeListener = null; 
	
	final Dimension currentSize = new Dimension(0, 0);
	
	/**
	 * Is the layout valid, i.e. must not be recomputed?
	 */
	boolean isValid = false;

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		isValid = false;		
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		isValid = false;
	}
		
	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0.5f;
	}
	
	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}
	
	@Override
	public void invalidateLayout(Container target) {
		//isValid = false;
	}		
	
	@Override
	public void layoutContainer(Container parent) {
		if(isValid){
			return;
		}
		
		// datatype and referring datatypes
		final HashMap<Datatype, HashSet<Datatype>> dependency = new HashMap<Datatype, HashSet<Datatype>>();
		final HashMap<Datatype, JDatatype> createdTypes = datatypeView.getCreatedDatatypes();

		// create a dependency graph
		// no dependencies for root datatype: 
		dependency.put(datatypeView.getRoot(), new HashSet<Datatype>());

		for(int i = 0; i < parent.getComponentCount(); i++){
			//  update dependency graph
			final Datatype cur = ((JDatatype) parent.getComponent(i)).getDatatype();

			//System.out.println("ADDING " + cur);

			for(Datatype usedType: cur.getChildDataTypes()){
				HashSet<Datatype> isUsedByList = dependency.get(usedType);
				if(isUsedByList == null){
					isUsedByList = new HashSet<Datatype>();
					dependency.put(usedType, isUsedByList);
				}
				isUsedByList.add(cur);

				//System.out.println("DEPENDS on " + usedType);
			}
		}

		// update positions of all components according to dependency:

		// right now use a N� algorithm, could use a priority queue / heap, though
		final HashSet<Datatype> curDrawn = new HashSet<Datatype>();

		// first add the ones which do not have any dependency. Then the ones which refer only
		// to drawn and so forth.
		int row = 0;
		int maxWidth = 0;
		int curY = 2;

		while(true){
			// Datatypes which shall be drawn in this row:
			final LinkedList<Datatype> toDraw = new LinkedList<Datatype>();

			// determine the components which can be drawn:
			for(Datatype typ: dependency.keySet()){
				if(curDrawn.contains(typ)){
					continue;
				}

				HashSet<Datatype> isUsedByList = dependency.get(typ);
				//System.out.println("CHECKING typ: " + typ);
				
				boolean hasDependencies = false;
				for(Datatype usedBy: isUsedByList){
					if(! createdTypes.containsKey(usedBy)){
						continue;
					}
					if( ! curDrawn.contains(usedBy)){
						hasDependencies = true;
						break;
					}
				}
				if(! hasDependencies){
					toDraw.add(typ);
				}
			}

			int rowX = 2;
			int maxRowHeight = 0; 

			// exit criterion
			if(toDraw.size() == 0){
				break;
			}

			// align and draw components:
			for(Datatype typ: toDraw){
				curDrawn.add(typ);
				final JDatatype jType = createdTypes.get(typ);
				if(jType != null){
					//System.out.println("DRAWING " + typ);

					final Dimension prefSize = jType.getPreferredSize();

					jType.setBounds(rowX, curY, prefSize.width, prefSize.height);
					if (prefSize.height > maxRowHeight){
						maxRowHeight = prefSize.height;
					}

					rowX += prefSize.width + 20;
				}
			}


			if(rowX > maxWidth){
				maxWidth = rowX;
			}

			curY += maxRowHeight + 20;

			row++;
		}

		currentSize.height = curY - 40 + 2;
		currentSize.width = maxWidth - 20 + 2;			
		
		isValid = true;
		
		if(datatypeViewChangeListener != null){
			// notify it:
			datatypeViewChangeListener.layoutRefreshed();
		}
	}


	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent) {		
		if(! isValid){
			layoutContainer(parent);
		}
		return currentSize;
	}
	
	@Override
	public void removeLayoutComponent(Component comp) {
		throw new IllegalStateException("it is not allowed to remove datatypes right now");
	}
	
	public ResizeListener getDatatypeViewChangeListener() {
		return datatypeViewChangeListener;
	}
	
	public void setDatatypeViewChangeListener(
			ResizeListener datatypeViewChangeListener) {
		this.datatypeViewChangeListener = datatypeViewChangeListener;
	}
}