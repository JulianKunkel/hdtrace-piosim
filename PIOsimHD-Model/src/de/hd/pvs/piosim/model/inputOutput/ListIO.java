
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$
  */


//	Copyright (C) 2008, 2009 Julian M. Kunkel
//
//	This file is part of PIOsimHD.
//
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.model.inputOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Simple List of <Offset, Access-Size> tuples to allow non-contiguous I/O.
 *
 * @author Julian M. Kunkel
 */
public class ListIO{

	final class SingleIOOperationComparator implements Comparator<SingleIOOperation> {
		public int compare(SingleIOOperation a, SingleIOOperation b) {
			if (a.getOffset() < b.getOffset()) {
				return -1;
			} else if (a.getOffset() > b.getOffset()) {
				return 1;
			}

			return 0;
		}
	}

	/**
	 * Defines a single Tuple of <Offset, Size>
	 * @author Julian M. Kunkel
	 */
	static public class SingleIOOperation implements Cloneable{
		private long  accessSize = 0l;
		private long  offset = 0l;

		public SingleIOOperation(long offset, long accessSize) {
			this.offset = offset;
			this.accessSize = accessSize;

			assert(offset >= 0);
			assert(accessSize > 0);
		}

		public long getAccessSize(){
			return accessSize;
		}

		public long getOffset(){
			return offset;
		}

		public void setAccessSize(long accessSize) {
			this.accessSize = accessSize;
		}

		public void setOffset(long offset) {
			this.offset = offset;
		}

		public String toString() {
			return "SingleIOOperation <offset, size> = " + getOffset() + ", " +  getAccessSize() + ">";
		}
	}

	/**
	 * Internal ArrayList to store the <Offset, Size> tuples.
	 */
	private ArrayList<SingleIOOperation> ioOperations = new ArrayList<SingleIOOperation>();

	public ListIO(){}

	/**
	 * Return the ArrayList.
	 * @return
	 */
	public ArrayList<SingleIOOperation> getIOOperations() {
		return ioOperations;
	}

	/**
	 * Add a new IOOperation to the end of the list.
	 * A simple optimization is added which does back merging of requests if it is possible.
	 * @param offset
	 * @param accessSize
	 */
	public void addIOOperation(long offset, long accessSize){
		if (ioOperations.size() != 0){
			// check if last operation is mergable with the new operation.
			SingleIOOperation op = ioOperations.get(ioOperations.size()-1);
			if(op.offset + op.accessSize == offset){
				op.accessSize += accessSize;
				return;
			}
		}

		ioOperations.add(new SingleIOOperation(offset, accessSize));
	}

	/**
	 * Compute the total access size of this request.
	 * @return
	 */
	public long getTotalSize() {
		long totalSize = 0;
		for(SingleIOOperation op: ioOperations){
			totalSize += op.getAccessSize();
		}
		return totalSize;
	}

	public ListIO getPartition (long offset, long size) {
		ListIO list = new ListIO();

		if (size == 0) {
			return list;
		}

		for (SingleIOOperation op : ioOperations) {
			if (op.getOffset() >= offset) {
				if (op.getOffset() + op.getAccessSize() <= offset + size) {
					list.addIOOperation(op.getOffset(), op.getAccessSize());
				} else {
					long newSize = (offset + size) - op.getOffset();

					if (newSize > 0) {
						list.addIOOperation(op.getOffset(), newSize);
					}
				}
			} else {
				if (op.getOffset() + op.getAccessSize() <= offset + size) {
					long newSize = (op.getOffset() + op.getAccessSize()) - offset;

					if (newSize > 0) {
						list.addIOOperation(offset, newSize);
					}
				} else {
					list.addIOOperation(offset, size);
				}
			}
		}

		return list;
	}

	public ListIO getSorted () {
		ListIO list = new ListIO();

		for (SingleIOOperation op : ioOperations) {
			list.addIOOperation(op.getOffset(), op.getAccessSize());
		}

		Collections.sort(list.ioOperations, new SingleIOOperationComparator());

		return list;
	}
}
