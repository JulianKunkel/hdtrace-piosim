
//	Copyright (C) 2011 Julian M. Kunkel
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

package de.hd.pvs.piosim.model.program.commands;

import java.util.HashMap;
import java.util.LinkedList;

import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.program.commands.superclasses.CommunicatorCommand;

/**
 * http://www.mpi-forum.org/docs/mpi-11-html/node83.html
MPI_REDUCE_SCATTER( sendbuf, recvbuf, recvcounts, datatype, op, comm)
[ IN sendbuf] starting address of send buffer (choice)
[ OUT recvbuf] starting address of receive buffer (choice)
[ IN recvcounts] integer array specifying the number of elements in result distributed to each process. Array must be identical on all calling processes.
[ IN datatype] data type of elements of input buffer (handle)
[ IN op] operation (handle)
[ IN comm] communicator (handle)

int MPI_Reduce_scatter(void* sendbuf, void* recvbuf, int *recvcounts, MPI_Datatype datatype, MPI_Op op, MPI_Comm comm)

MPI_REDUCE_SCATTER(SENDBUF, RECVBUF, RECVCOUNTS, DATATYPE, OP, COMM, IERROR)
<type> SENDBUF(*), RECVBUF(*)
INTEGER RECVCOUNTS(*), DATATYPE, OP, COMM, IERROR

MPI_REDUCE_SCATTER first does an element-wise reduction on vector of elements in the send buffer defined by sendbuf, count and datatype.
Next, the resulting vector of results is split into n disjoint segments, where n is the number of members in the group.
Segment i contains recvcounts[i] elements. The ith segment is sent to process i and stored in the receive buffer defined by recvbuf, recvcounts[i] and datatype.
 */

public class ReduceScatter extends CommunicatorCommand{

	// Amount of data to recv from each rank of the communicator:
	@NotNull
	protected HashMap<Integer, Long> recvcounts = null;

	private long totalSize;

	public void readCommandXML(XMLTag xml) throws Exception {
		final LinkedList<XMLTag> elems = xml.getNestedXMLTagsWithName("count");
		recvcounts = new HashMap<Integer, Long>();

		totalSize = 0;

		for (XMLTag e : elems) {
			final long value = Long.parseLong(  e.getAttribute("size"));
			final int key = Integer.parseInt(  e.getAttribute("rank"));
			recvcounts.put(key, value);
			totalSize += value;
		}
	}

	@Override
	public String toString() {
		return "ReduceScatter";
	}

	public void setRecvcounts( HashMap<Integer, Long> commSize) {
		totalSize = 0;
		for (long val : commSize.values()){
			totalSize += val;
		}
		this.recvcounts = commSize;
	}

	public HashMap<Integer, Long> getRecvcounts() {
		return recvcounts;
	}

	public long getTotalSize(){
		return totalSize;
	}

}
