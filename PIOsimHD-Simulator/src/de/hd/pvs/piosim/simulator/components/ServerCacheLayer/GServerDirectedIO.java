/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$ 
 */

//	Copyright (C) 2009 Michael Kuhn
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
/**
 * 
 */
package de.hd.pvs.piosim.simulator.components.ServerCacheLayer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.event.IOJob.IOOperation;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;

final class IOJobComparator implements Comparator<IOJob> {
	public int compare(IOJob a, IOJob b) {
		if (a.getOffset() < b.getOffset()) {
			return -1;
		} else if (a.getOffset() > b.getOffset()) {
			return 1;
		}

		return 0;
	}
}

/**
 * Try to do clever I/O optimization.
 * 
 * @author Michael Kuhn
 */
public class GServerDirectedIO extends GAggregationCache {
	MPIFile lastFile = null;
	long lastOffset = -1;

	HashMap<MPIFile, LinkedList<IOJob>> queuedWriteJobs = new HashMap<MPIFile, LinkedList<IOJob>>();

	/*
	 * Combine as many IOJobs as possible.
	 */
	private LinkedList<IOJob> mergeIOJobs(LinkedList<IOJob> l) {
		long size = -1;
		long offset = -1;

		LinkedList<IOJob> nl = new LinkedList<IOJob>();
		Iterator<IOJob> it;
		IOJob io = null;
		MPIFile file = null;

		Collections.sort(l, new IOJobComparator());

		System.out.print("MERGE old: " + l.size());

		it = l.iterator();

		while (it.hasNext()) {
			IOJob cur = it.next();

			if (file == null) {
				file = cur.getFile();
			}

			if (io == null) {
				io = cur;
				size = io.getSize();
				offset = io.getOffset();
				it.remove();
				continue;
			}

			if (offset >= cur.getOffset() && offset + size <= cur.getOffset() + cur.getSize()) {
				// forget operation
				io = cur;
				size = io.getSize();
				offset = io.getOffset();
				it.remove();
				continue;
			} else if (size + offset == cur.getOffset()) {
				// forward combination
				if (size + cur.getSize() > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
					nl.add(new IOJob(io.getFile(), size, offset, io.getType()));

					io = cur;
					size = io.getSize();
					offset = io.getOffset();
					it.remove();
					continue;
				}

				size += cur.getSize();
				it.remove();
			} else if (cur.getOffset() + cur.getSize() == offset) {
				// backwards combination
				if (size + cur.getSize() > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
					nl.add(new IOJob(io.getFile(), size, offset, io.getType()));

					io = cur;
					size = io.getSize();
					offset = io.getOffset();
					it.remove();
					continue;
				}

				size += cur.getSize();
				offset = cur.getOffset();
				it.remove();
			} else {
				nl.add(new IOJob(io.getFile(), size, offset, io.getType()));

				io = cur;
				size = io.getSize();
				offset = io.getOffset();
				it.remove();
				continue;
			}
		}

		assert (l.size() == 0);

		if (io != null) {
			nl.add(new IOJob(io.getFile(), size, offset, io.getType()));
		}

		if (file != null) {
			queuedWriteJobs.put(file, nl);
		}

		System.out.println(" new: " + nl.size());

		return nl;
	}

	private IOJob getJob() {
		long size = 0;

		LinkedList<IOJob> list = null;
		LinkedList<IOJob> lastFileList = null;

		IOJob io = null;
		IOJob ioClose = null;
		IOJob ioLarge = null;

		Iterator<IOJob> it = null;

		if (lastFile != null) {
			lastFileList = queuedWriteJobs.get(lastFile);
		}

		for (LinkedList<IOJob> l : queuedWriteJobs.values()) {
			long tmp = 0;

			it = l.iterator();

			while (it.hasNext()) {
				tmp += it.next().getSize();
			}

			if (l == lastFileList) {
				tmp *= 10;
			}

			if (tmp > size) {
				size = tmp;
				list = l;
			}
		}

		if (list == null) {
			return null;
		}

		if (list != lastFileList) {
			lastFile = null;
			lastOffset = -1;
		}

		list = mergeIOJobs(list);

		it = list.iterator();

		while (it.hasNext()) {
			IOJob cur = it.next();

			if (lastOffset >= 0 && cur.getOffset() == lastOffset) {
				io = cur;
				break;
			} else if (ioClose == null || Math.abs(cur.getOffset() - lastOffset) < Math.abs(ioClose.getOffset() - lastOffset)) {
				ioClose = cur;
			} else if (ioLarge == null || cur.getSize() > ioLarge.getSize()) {
				ioLarge = cur;
			}
		}

		if (io == null) {
			if (ioClose != null && ioLarge == null) {
				io = ioClose;
			} else if (ioClose == null && ioLarge != null) {
				io = ioLarge;
			} else if (ioClose != null && ioLarge != null) {
				if (ioLarge.getSize() > ioClose.getSize() * 10) {
					io = ioLarge;
				} else {
					io = ioClose;
				}
			} else {
				io = list.peek();
			}
		}

		if (io == null) {
			return null;
		}

		list.remove(io);

		lastFile = io.getFile();
		lastOffset = io.getOffset() + io.getSize();

		return io;
	}

	@Override
	protected IOJob getNextSchedulableJob() {
		// prefer read requests for write requests
		IOJob io = null;

		if (!queuedReadJobs.isEmpty() && parentNode.isEnoughFreeMemory(queuedReadJobs.peek().getSize())) {
			// reserve memory for READ requests
			// io = combineOperation(queuedReadJobs);
			io = queuedReadJobs.poll();

			parentNode.reserveMemory(io.getSize());
		} else {
			io = getJob();
		}

		return io;
	}

	@Override
	protected int getNumberOfQueuedOperations() {
		int size = queuedReadJobs.size();

		for (LinkedList<IOJob> l : queuedWriteJobs.values()) {
			size += l.size();
		}

		return size;
	}

	@Override
	protected void addWriteIOJob(long size, long offset, RequestIO req) {
		if (queuedWriteJobs.get(req.getFile()) == null) {
			queuedWriteJobs.put(req.getFile(), new LinkedList<IOJob>());
		}

		queuedWriteJobs.get(req.getFile()).add(new IOJob(req.getFile(), size, offset, IOOperation.WRITE));

		scheduleNextIOJobIfPossible();
	}
}
