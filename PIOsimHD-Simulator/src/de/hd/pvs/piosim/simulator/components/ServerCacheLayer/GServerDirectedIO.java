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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.event.IOJob.IOOperation;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;

/**
 * Try to do clever I/O optimization.
 *
 * @author Michael Kuhn
 */
public class GServerDirectedIO extends GAggregationCache {
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

	/* Remember the last file and offset to perform contiguous operations,
	 * when possible. */
	MPIFile lastFile = null;
	long lastOffset = -1;

	/* Use per-file queues to make merging easier. */
	HashMap<MPIFile, LinkedList<IOJob>> queuedReadJobs = new HashMap<MPIFile, LinkedList<IOJob>>();
	HashMap<MPIFile, LinkedList<IOJob>> queuedWriteJobs = new HashMap<MPIFile, LinkedList<IOJob>>();

	/* Combine as many IOJobs as possible. */
	private LinkedList<IOJob> mergeReadJobs(LinkedList<IOJob> l) {
		long size = -1;
		long offset = -1;

		LinkedList<IOJob> nl = new LinkedList<IOJob>();
		Iterator<IOJob> it;
		IOJob io = null;
		List<PendingReadRequest> map = new ArrayList<PendingReadRequest>();
		MPIFile file = null;

		System.out.print("MERGE READ old: " + l.size());

		/* FIXME: Sort job queue. Real implementations should probably keep the list sorted on insert. */
		Collections.sort(l, new IOJobComparator());

		it = l.iterator();

		/* This loop merges IOJobs by iterating over the job queue and removing
		 * IOJobs that can be merged. When two IOJobs are too far apart or
		 * the merged IOJob would be larger than IOGranularity, the old (merged)
		 * IOJob is inserted into a new job queue (the old one can not be modified
		 * without invalidating the iterator) and a new merge is started.
		 *
		 * Special care must be taken with regards to pendingReadRequestMap. */
		while (it.hasNext()) {
			long holeSize;
			IOJob cur = it.next();

			/* FIXME: Determine file. */
			if (file == null) {
				file = cur.getFile();
			}

			if (io == null) {
				io = cur;
				size = io.getSize();
				offset = io.getOffset();

				map.addAll(pendingReadRequestMap.remove(cur));
				it.remove();
				continue;
			}

			holeSize = 0;
			holeSize = Math.min(size / 5, getSimulator().getModel().getGlobalSettings().getIOGranularity() / 10);

			if (offset + size + holeSize >= cur.getOffset()) {
				/* Merge two IOJobs. We allow holes, that is, perform data sieving. */
				long newOffset;
				long newSize;

				newOffset = Math.min(offset, cur.getOffset());
				newSize = Math.max(offset + size, cur.getOffset() + cur.getSize()) - newOffset;

				if (newSize > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
					/* New IOJob would get too large. Start a new one. */
					IOJob tmp;

					tmp = new IOJob(io.getFile(), size, offset, io.getType());
					nl.add(tmp);

					if (pendingReadRequestMap.get(tmp) == null) {
						pendingReadRequestMap.put(tmp, new ArrayList<PendingReadRequest>());
					}
					pendingReadRequestMap.get(tmp).addAll(map);
					map.clear();

					io = cur;
					size = io.getSize();
					offset = io.getOffset();

					map.addAll(pendingReadRequestMap.remove(cur));
					it.remove();
					continue;
				}

				offset = newOffset;
				size = newSize;

				map.addAll(pendingReadRequestMap.remove(cur));
				it.remove();
			} else {
				/* IOJobs can not be merged. Start a new one. */
				IOJob tmp;

				tmp = new IOJob(io.getFile(), size, offset, io.getType());
				nl.add(tmp);

				if (pendingReadRequestMap.get(tmp) == null) {
					pendingReadRequestMap.put(tmp, new ArrayList<PendingReadRequest>());
				}
				pendingReadRequestMap.get(tmp).addAll(map);
				map.clear();

				io = cur;
				size = io.getSize();
				offset = io.getOffset();

				map.addAll(pendingReadRequestMap.remove(cur));
				it.remove();
				continue;
			}
		}

		assert (l.size() == 0);

		if (io != null) {
			IOJob tmp;

			tmp = new IOJob(io.getFile(), size, offset, io.getType());
			nl.add(tmp);

			if (pendingReadRequestMap.get(tmp) == null) {
				pendingReadRequestMap.put(tmp, new ArrayList<PendingReadRequest>());
			}
			pendingReadRequestMap.get(tmp).addAll(map);
		}

		if (file != null) {
			queuedReadJobs.put(file, nl);
		}

		System.out.println(" new: " + nl.size());

		return nl;
	}

	private LinkedList<IOJob> mergeWriteJobs(LinkedList<IOJob> l) {
		long size = -1;
		long offset = -1;

		LinkedList<IOJob> nl = new LinkedList<IOJob>();
		Iterator<IOJob> it;
		IOJob io = null;
		MPIFile file = null;

		System.out.print("MERGE WRITE old: " + l.size());

		it = l.iterator();

		/* This loop merges IOJobs by iterating over the job queue and removing
		 * IOJobs that can be merged. When two IOJobs are too far apart or
		 * the merged IOJob would be larger than IOGranularity, the old (merged)
		 * IOJob is inserted into a new job queue (the old one can not be modified
		 * without invalidating the iterator) and a new merge is started. */
		while (it.hasNext()) {
			IOJob cur = it.next();

			/* FIXME: Determine file. */
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

			if (offset + size == cur.getOffset()) {
				/* Merge two IOJobs. */
				if (size + cur.getSize() > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
					/* New IOJob would get too large. Start a new one. */
					nl.add(new IOJob(io.getFile(), size, offset, io.getType()));

					io = cur;
					size = io.getSize();
					offset = io.getOffset();
					it.remove();
					continue;
				}

				size += cur.getSize();
				it.remove();
			} else {
				/* IOJobs can not be merged. Start a new one. */
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

	private IOJob getJob(IOOperation type) {
		long size = 0;

		HashMap<MPIFile, LinkedList<IOJob>> queue = null;

		LinkedList<IOJob> list = null;
		LinkedList<IOJob> lastFileList = null;

		IOJob io = null;
		IOJob ioClose = null;
		IOJob ioLarge = null;

		Iterator<IOJob> it = null;

		if (type == IOOperation.WRITE) {
			queue = queuedWriteJobs;
		} else if (type == IOOperation.READ) {
			queue = queuedReadJobs;
		}

		if (lastFile != null) {
			lastFileList = queue.get(lastFile);
		}

		/* Take a look at all job queues. */
		for (LinkedList<IOJob> l : queue.values()) {
			long tmp = 0;

			it = l.iterator();

			/* Determine the size of the job queue. */
			while (it.hasNext()) {
				tmp += it.next().getSize();
			}

			/* Prefer operating on the last file. */
			if (l == lastFileList) {
				tmp *= 10;
			}

			/* Use the largest job queue. */
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

		if (type == IOOperation.READ) {
			list = mergeReadJobs(list);
		} else if (type == IOOperation.WRITE) {
			list = mergeWriteJobs(list);
		}

		it = list.iterator();

		/* Take a look at all IOJobs. */
		while (it.hasNext()) {
			IOJob cur = it.next();

			if (lastOffset >= 0 && cur.getOffset() == lastOffset) {
				/* We can resume writing at the last position, perfect match. */
				io = cur;
				break;
			} else if (ioClose == null || Math.abs(cur.getOffset() - lastOffset) < Math.abs(ioClose.getOffset() - lastOffset)) {
				/* Determine the IOJob closest to the last position. */
				ioClose = cur;
			} else if (ioLarge == null || cur.getSize() > ioLarge.getSize()) {
				/* Determine the largest IOJob. */
				ioLarge = cur;
			}
		}

		if (io == null) {
			if (ioClose != null && ioLarge == null) {
				io = ioClose;
			} else if (ioClose == null && ioLarge != null) {
				io = ioLarge;
			} else if (ioClose != null && ioLarge != null) {
				/* Use the largest IOJob if it is at least ten times bigger
				 * than the closest one. */
				if (ioLarge.getSize() > ioClose.getSize() * 10) {
					io = ioLarge;
				} else {
					io = ioClose;
				}
			} else {
				/* Just use the first IOJob. */
				io = list.peek();
			}
		}

		if (io == null) {
			return null;
		}

		if (type == IOOperation.READ) {
			if (!parentNode.isEnoughFreeMemory(io.getSize())) {
				return null;
			}

			parentNode.reserveMemory(io.getSize());
		}

		list.remove(io);

		lastFile = io.getFile();
		lastOffset = io.getOffset() + io.getSize();

		return io;
	}

	@Override
	protected IOJob getNextSchedulableJob() {
		IOJob io;

		/* We prefer read jobs. */
		io = getJob(IOOperation.READ);

		if (io == null) {
			io = getJob(IOOperation.WRITE);
		}

		return io;
	}

	@Override
	protected int getNumberOfQueuedOperations() {
		int size = 0;

		for (LinkedList<IOJob> l : queuedReadJobs.values()) {
			size += l.size();
		}

		for (LinkedList<IOJob> l : queuedWriteJobs.values()) {
			size += l.size();
		}

		return size;
	}

	@Override
	protected void addReadIOJob(long size, long offset, RequestRead req) {
		if (queuedReadJobs.get(req.getFile()) == null) {
			queuedReadJobs.put(req.getFile(), new LinkedList<IOJob>());
		}

		IOJob io = new IOJob(req.getFile(), size, offset, IOOperation.READ);
		queuedReadJobs.get(req.getFile()).add(io);

		if (pendingReadRequestMap.get(io) == null) {
			pendingReadRequestMap.put(io, new ArrayList<PendingReadRequest>());
		}

		pendingReadRequestMap.get(io).add(new PendingReadRequest(io, req));

		scheduleNextIOJobIfPossible();
	}

	@Override
	protected void addWriteIOJob(long size, long offset, RequestIO req) {
		if (queuedWriteJobs.get(req.getFile()) == null) {
			queuedWriteJobs.put(req.getFile(), new LinkedList<IOJob>());
		}

		IOJob io = new IOJob(req.getFile(), size, offset, IOOperation.WRITE);
		List<IOJob> jobList = queuedWriteJobs.get(req.getFile());
		int i = jobList.size();

		/* This loop is used to determine the position to insert the new IOJob at.
		 * Real implementations should use a more efficient approach (like a binary
		 * search), but for now we just search the position from the end of the
		 * job queue and modify existing IOJobs along the way.
		 *
		 *  We must make sure that IOJobs do not overlap and are sorted by offset. */
		while (true) {
			if (i == 0) {
				jobList.add(0, io);
				break;
			}

			IOJob cmp = jobList.get(i - 1);

			if (cmp.getOffset() >= io.getOffset()) {
				if (io.getOffset() + io.getSize() >= cmp.getOffset() + cmp.getSize()) {
					/* The new operation completely overwrites the old one, so we
					 * can delete it. */
					jobList.remove(i - 1);
				} else if (io.getOffset() + io.getSize() > cmp.getOffset()) {
					/* The new operation overwrites part of the old one, so we must
					 * adjust the offset and size. */
					long tmpOffset = io.getOffset() + io.getSize();
					long tmpSize = cmp.getSize() - (tmpOffset - cmp.getOffset());
					IOJob tmp = new IOJob(cmp.getFile(), tmpSize, tmpOffset, cmp.getType());

					jobList.set(i - 1, tmp);
				}

				i--;
			} else {
				if (io.getOffset() < cmp.getOffset() + cmp.getSize()) {
					if (io.getOffset() + io.getSize() < io.getOffset() + io.getSize()) {
						/* The new operation splits the old one into two. */
						long upSize = (cmp.getOffset() + cmp.getSize()) - (io.getOffset() + io.getSize());
						long upOffset = io.getOffset() + io.getSize();
						IOJob up = new IOJob(cmp.getFile(), upSize, upOffset, cmp.getType());
						long downSize = io.getOffset() - cmp.getOffset();
						IOJob down = new IOJob(cmp.getFile(), downSize, cmp.getOffset(), cmp.getType());

						jobList.set(i - 1, down);
						jobList.add(i, up);
					} else {
						/* The new operation overwrites part of the old one, so we must
						 * adjust the offset and size. */
						long tmpSize = io.getOffset() - cmp.getOffset();
						IOJob tmp = new IOJob(cmp.getFile(), tmpSize, cmp.getOffset(), cmp.getType());

						jobList.set(i - 1, tmp);
					}
				}

				jobList.add(i, io);
				break;
			}
		}

		scheduleNextIOJobIfPossible();
	}
}
