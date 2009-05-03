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

		System.out.print("old: " + l.size());

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

			if (size + offset == cur.getOffset()) {
				// forward combination
				if (size + cur.getSize() > getSimulator().getModel()
						.getGlobalSettings().getIOGranularity()) {
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
				if (size + cur.getSize() > getSimulator().getModel()
						.getGlobalSettings().getIOGranularity()) {
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
		boolean foundShort = false;

		LinkedList<IOJob> list = null;
		LinkedList<IOJob> lastFileList = null;

		IOJob io = null;
		Iterator<IOJob> it = null;

		for (LinkedList<IOJob> l : queuedWriteJobs.values()) {
			if (list == null || list.size() < l.size()) {
				list = l;
			}
		}

		if (list == null) {
			return null;
		}

		if (lastFile != null) {
			lastFileList = queuedWriteJobs.get(lastFile);

			if (lastFileList != null) {
				System.out.println("cur: " + list.size() + " last: "
						+ lastFileList.size());

				if (lastFileList.size() > (list.size() / 10)) {
					list = lastFileList;
				} else {
					lastFile = null;
					lastOffset = -1;
				}
			} else {
				lastFile = null;
				lastOffset = -1;
			}
		}

		list = mergeIOJobs(list);

		it = list.iterator();

		// IDEA: use largest IOJob
		while (it.hasNext()) {
			IOJob cur = it.next();

			if (lastOffset >= 0 && cur.getOffset() == lastOffset) {
				io = cur;
				break;
			} else if (io == null || cur.getOffset() < io.getOffset()) {
				if (lastOffset >= 0
						&& Math.abs(cur.getOffset() - lastOffset) <= 1024 * 1024) {
					foundShort = true;
					io = cur;
				} else if (!foundShort) {
					io = cur;
				}
			}
		}

		if (io == null) {
			io = list.peek();
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

		if (!queuedReadJobs.isEmpty()
				&& parentNode.isEnoughFreeMemory(queuedReadJobs.peek()
						.getSize())) {
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

		queuedWriteJobs.get(req.getFile()).add(
				new IOJob(req.getFile(), size, offset, IOOperation.WRITE));

		scheduleNextIOJobIfPossible();
	}
}
