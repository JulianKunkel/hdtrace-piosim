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
import java.util.List;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.event.IOJob;
import de.hd.pvs.piosim.simulator.event.IOJob.IOOperation;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestIO;
import de.hd.pvs.piosim.simulator.network.jobs.requests.RequestRead;

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

	HashMap<MPIFile, LinkedList<IOJob>> queuedReadJobs = new HashMap<MPIFile, LinkedList<IOJob>>();
	HashMap<MPIFile, LinkedList<IOJob>> queuedWriteJobs = new HashMap<MPIFile, LinkedList<IOJob>>();

	private void mergeHashMaps(HashMap<IOJob, RequestRead> dst, HashMap<IOJob, RequestRead> src) {
		for (IOJob io : src.keySet()) {
			dst.put(io, src.get(io));
		}
	}

	/*
	 * Combine as many IOJobs as possible.
	 */
	private LinkedList<IOJob> mergeReadJobs(LinkedList<IOJob> l) {
		long size = -1;
		long offset = -1;

		LinkedList<IOJob> nl = new LinkedList<IOJob>();
		Iterator<IOJob> it;
		IOJob io = null;
		HashMap<IOJob, RequestRead> map = new HashMap<IOJob, RequestRead>();
		MPIFile file = null;

		System.out.print("MERGE READ old: " + l.size());

		// FIXME
		Collections.sort(l, new IOJobComparator());

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

				mergeHashMaps(map, pendingReadRequestMap.remove(cur));
				it.remove();
				continue;
			}

			if ((offset >= cur.getOffset() && offset <= cur.getOffset() + cur.getSize())
					|| (offset + size >= cur.getOffset() && offset + size <= cur.getOffset() + cur.getSize())) {
				long newOffset;
				long newSize;

				newOffset = Math.min(offset, cur.getOffset());
				newSize = Math.max(offset + size, cur.getOffset() + cur.getSize()) - newOffset;

				if (newSize > getSimulator().getModel().getGlobalSettings().getIOGranularity()) {
					IOJob tmp;

					tmp = new IOJob(io.getFile(), size, offset, io.getType());
					nl.add(tmp);

					if (pendingReadRequestMap.get(tmp) == null) {
						pendingReadRequestMap.put(tmp, new HashMap<IOJob, RequestRead>());
					}
					mergeHashMaps(pendingReadRequestMap.get(tmp), map);
					map.clear();

					io = cur;
					size = io.getSize();
					offset = io.getOffset();

					mergeHashMaps(map, pendingReadRequestMap.remove(cur));
					it.remove();
					continue;
				}

				offset = newOffset;
				size = newSize;

				mergeHashMaps(map, pendingReadRequestMap.remove(cur));
				it.remove();
			} else {
				IOJob tmp;

				tmp = new IOJob(io.getFile(), size, offset, io.getType());
				nl.add(tmp);

				if (pendingReadRequestMap.get(tmp) == null) {
					pendingReadRequestMap.put(tmp, new HashMap<IOJob, RequestRead>());
				}
				mergeHashMaps(pendingReadRequestMap.get(tmp), map);
				map.clear();

				io = cur;
				size = io.getSize();
				offset = io.getOffset();

				mergeHashMaps(map, pendingReadRequestMap.remove(cur));
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
				pendingReadRequestMap.put(tmp, new HashMap<IOJob, RequestRead>());
			}
			mergeHashMaps(pendingReadRequestMap.get(tmp), map);
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

		for (LinkedList<IOJob> l : queue.values()) {
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

		if (type == IOOperation.READ) {
			list = mergeReadJobs(list);
		} else if (type == IOOperation.WRITE) {
			list = mergeWriteJobs(list);
		}

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
			pendingReadRequestMap.put(io, new HashMap<IOJob, RequestRead>());
		}

		pendingReadRequestMap.get(io).put(io, req);

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

		while (true) {
			if (i == 0) {
				jobList.add(0, io);
				break;
			}

			IOJob cmp = jobList.get(i - 1);

			if (cmp.getOffset() >= io.getOffset()) {
				if (io.getOffset() + io.getSize() >= cmp.getOffset() + cmp.getSize()) {
					// new operation overwrites the old one
					jobList.remove(i - 1);
				} else if (io.getOffset() + io.getSize() > cmp.getOffset()) {
					// new operation overwrites part of the old one
					long tmpOffset = io.getOffset() + io.getSize();
					long tmpSize = cmp.getSize() - (tmpOffset - cmp.getOffset());
					IOJob tmp = new IOJob(cmp.getFile(), tmpSize, tmpOffset, cmp.getType());

					jobList.set(i - 1, tmp);
				}

				i--;
			} else {
				if (io.getOffset() < cmp.getOffset() + cmp.getSize()) {
					if (io.getOffset() + io.getSize() < io.getOffset() + io.getSize()) {
						// new operation splits up the old one
						long upSize = (cmp.getOffset() + cmp.getSize()) - (io.getOffset() + io.getSize());
						long upOffset = io.getOffset() + io.getSize();
						IOJob up = new IOJob(cmp.getFile(), upSize, upOffset, cmp.getType());
						long downSize = io.getOffset() - cmp.getOffset();
						IOJob down = new IOJob(cmp.getFile(), downSize, cmp.getOffset(), cmp.getType());

						jobList.set(i - 1, down);
						jobList.add(i, up);
					} else {
						// new operation overwrites part of the old one
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
