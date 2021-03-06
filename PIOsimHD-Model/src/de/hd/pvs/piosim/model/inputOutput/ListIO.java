
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

		assert(ioOperations.size() == 0 ||  offset >=
			ioOperations.get(ioOperations.size()-1).accessSize +
			ioOperations.get(ioOperations.size()-1).offset  );

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

	public long getFirstAccessedByte(){
		return ioOperations.get(0).getOffset();
	}

	public long getLastAccessedByte(){
		final SingleIOOperation last = ioOperations.get(ioOperations.size()-1);
		return last.getAccessSize() + last.getOffset();
	}

	/**
	 * Return the number of ListIO elements
	 * @return
	 */
	public int getCount(){
		return ioOperations.size();
	}

	public void printAll(){
		for(SingleIOOperation op: ioOperations){
			System.out.println(op);
		}
	}
}
