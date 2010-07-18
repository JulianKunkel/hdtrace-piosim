
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

package de.hd.pvs.piosim.model.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.program.commands.Allgather;
import de.hd.pvs.piosim.model.program.commands.Allreduce;
import de.hd.pvs.piosim.model.program.commands.Barrier;
import de.hd.pvs.piosim.model.program.commands.Bcast;
import de.hd.pvs.piosim.model.program.commands.Compute;
import de.hd.pvs.piosim.model.program.commands.Fileclose;
import de.hd.pvs.piosim.model.program.commands.Fileopen;
import de.hd.pvs.piosim.model.program.commands.Fileread;
import de.hd.pvs.piosim.model.program.commands.Filereadall;
import de.hd.pvs.piosim.model.program.commands.Filewrite;
import de.hd.pvs.piosim.model.program.commands.Filewriteall;
import de.hd.pvs.piosim.model.program.commands.Gather;
import de.hd.pvs.piosim.model.program.commands.Recv;
import de.hd.pvs.piosim.model.program.commands.Reduce;
import de.hd.pvs.piosim.model.program.commands.Send;
import de.hd.pvs.piosim.model.program.commands.Sendrecv;
import de.hd.pvs.piosim.model.program.commands.Wait;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.program.fileView.FileView;

/**
 * This class allows a rapid creation of programs.
 * It uses an ApplicationBuilder to add collective or individual operations.
 *
 * @author Julian M. Kunkel
 *
 */
public class ProgramBuilder {
	private final ApplicationBuilder appBuilder;

	/**
	 * For each Client rank the last AIO ID is stored here to allow creation of unique AIO IDs.
	 */
	private HashMap<Integer, Integer> lastUnusedAsynchronousIDForClient = new HashMap<Integer, Integer>();

	/**
	 * For verification contains the number of pending AIO operations.
	 */
	private HashMap<Integer, ArrayList<Integer>> pendingAsynchronousIDsForClient = new HashMap<Integer, ArrayList<Integer>>();


	//////////// INDIVIDUAL COMMANDS //////////////////////////

	public void addComputate(int rank, long cycles){
		Compute com = new Compute();
		com.setCycles(cycles);
		appBuilder.addCommand(rank, com);
	}

	public FileDescriptor addFileOpen(FileMetadata file, Communicator communicator, boolean shallTruncate) {
		Fileopen com = new Fileopen();
		com.setFileDescriptor( new FileDescriptor(file, communicator));
		com.setTruncateOnOpen(shallTruncate);

		for (Integer rank : communicator.getParticipatingRanks()) {
			appBuilder.addCommand(rank, com);
		}

		return new FileDescriptor(file, communicator);
	}

	public void addFileClose(FileDescriptor fd) {
		Fileclose com = new Fileclose();
		com.setFileDescriptor(fd);

		for (Integer rank : fd.getCommunicator().getParticipatingRanks()) {
			appBuilder.addCommand(rank, com);
		}
	}

	public void addReadSequential(int rank, FileDescriptor fd, long offset, long seqSize) {
		addReadFileView(rank, fd, null, offset, seqSize);
	}

	public void addReadFileView(int rank, FileDescriptor fd, FileView view, long offset, long accessSize){
		Fileread com = new Fileread();
		ListIO lio = new ListIO();

		if(view != null){
			view.createIOOperation(lio, offset, accessSize);
		}else{
			lio.addIOOperation(offset, accessSize);
		}
		com.setListIO(lio);
		appBuilder.addCommand(rank, com, fd);
	}

	public void addWriteSequential(int rank, FileDescriptor fd, long offset, long seqSize) {
		Filewrite com = new Filewrite();
		ListIO lio = new ListIO();
		lio.addIOOperation(offset, seqSize);
		com.setListIO(lio);
		appBuilder.addCommand(rank, com, fd);
	}

	public void addReadCollective(FileDescriptor fd, LinkedList<ListIO> io) {
		assert(io.size() ==  fd.getCommunicator().getParticipiants().size());

		final Iterator<ListIO> iter =  io.iterator();
		for (Integer rank : fd.getCommunicator().getParticipatingRanks()) {
			Filereadall com = new Filereadall();
			com.setListIO(iter.next());
			appBuilder.addCommand(rank, com, fd);
		}
	}

	public void addWriteCollective(FileDescriptor fd, LinkedList<ListIO> io) {
		assert(io.size() ==  fd.getCommunicator().getParticipiants().size());

		final Iterator<ListIO> iter =  io.iterator();
		for (Integer rank : fd.getCommunicator().getParticipatingRanks()) {
			Filewriteall com = new Filewriteall();
			com.setListIO(iter.next());
			appBuilder.addCommand(rank, com, fd);
		}
	}

	public void addSend(Communicator communicator, int srcRank, int tgtRank, long size, int tag){
		Send send = new Send();

		send.setSize(size);
		send.setToTag(tag);
		send.setToRank(tgtRank);
		send.setCommunicator(communicator);

		appBuilder.addCommand(srcRank, send);
	}

	public void addRecv(Communicator communicator, int srcRank, int tgtRank, int tag){
		Recv recv = new Recv();

		recv.setFromTag(tag);
		recv.setFromRank(srcRank);
		recv.setCommunicator(communicator);

		appBuilder.addCommand(tgtRank, recv);
	}

	public void addSendRecv(Communicator communicator, int myRank, int fromRank, int toRank, long size,
			int fromTag, int toTag){
		Sendrecv sr = new Sendrecv();
		sr.setCommunicator(communicator);
		sr.setFromRank(fromRank);
		sr.setFromTag(fromTag);

		sr.setToTag(toTag);
		sr.setToRank(toRank);

		sr.setSize(size);

		appBuilder.addCommand(myRank, sr);
	}

	public void addSendAndRecv(Communicator communicator, int srcRank, int tgtRank, long size, int tag){
		addSend(communicator, srcRank, tgtRank, size, tag);
		addRecv(communicator, srcRank, tgtRank, tag);
	}

	public void addBarrier(Communicator comm){
		Barrier barrier = new Barrier();
		appBuilder.addCommand(comm, barrier);
	}


	public void addAllreduce(Communicator comm, long size){
		Allreduce reduce = new Allreduce();
		reduce.setSize(size);
		appBuilder.addCommand(comm, reduce);
	}

	public void addAllgather(Communicator comm, long size){
		Allgather gather = new Allgather();
		gather.setSize(size);
		appBuilder.addCommand(comm, gather);
	}

	public void addBroadcast(Communicator comm, int root, long size){
		Bcast bcast = new Bcast();
		bcast.setSize(size);
		bcast.setRootRank(root);
		appBuilder.addCommand(comm, bcast);
	}

	public void addReduce(Communicator comm, int root, long size){
		Reduce reduce = new Reduce();
		reduce.setSize(size);
		reduce.setRootRank(root);
		appBuilder.addCommand(comm, reduce);
	}

	public void addGather(Communicator comm, int root, long size){
		Gather gather = new Gather();
		gather.setSize(size);
		gather.setRootRank(root);
		appBuilder.addCommand(comm, gather);
	}


	//////////////// END COLLECTIVE COMMANDS //////////////////////

	public ProgramBuilder(ApplicationBuilder appBuilder) {
		this.appBuilder = appBuilder;
	}


	public int setLastCommandAsynchronous(int process){
		return setLastCommandAsynchronous(process, 0);
	}


	/**
	 * Set the last non AIO command as asynchronous == non-blocking.
	 *
	 * @param process
	 * @return the asynchronous ID
	 */
	public int setLastCommandAsynchronous(int process, int thread){
		ProgramInMemory program = (ProgramInMemory) appBuilder.getApplication().getClientProgram(process, thread);

		if(program.getSize() == 0){
			throw new IllegalArgumentException("The program is empty yet");
		}

		Command lastCmd = program.getCommands().get(program.getSize()-1);

		if (lastCmd.getClass() == Wait.class){
			throw new IllegalArgumentException("Wait cannot be set to be asynchronous.");
		}

		int asynchronousID = 1;
		// create a unique AIO ID for this command.

		if (lastUnusedAsynchronousIDForClient.containsKey(process)){
			asynchronousID = lastUnusedAsynchronousIDForClient.get(process);
		}

		lastCmd.setAsynchronousID(asynchronousID);

		lastUnusedAsynchronousIDForClient.put(process, asynchronousID + 1 );

		// add the asynchronous operation to the pending operations (for verification).

		ArrayList<Integer> pendingIds =  pendingAsynchronousIDsForClient.get(process);
		if (pendingIds == null){
			pendingIds = new ArrayList<Integer>();
			pendingAsynchronousIDsForClient.put(process, pendingIds);
		}

		pendingIds.add(asynchronousID);

		return asynchronousID;
	}


	/**
	 * Add a Wait call for one or multiple asynchronous IDs.
	 * @param rank
	 * @param aids
	 */
	public void addWait(int rank, int [] aids){
		ArrayList<Integer> pendingIds =  pendingAsynchronousIDsForClient.get(rank);
		if (pendingIds == null){
			throw new IllegalArgumentException("No pending asynchronous operations");
		}

		ArrayList<Integer> waitFor = new ArrayList<Integer>();

		for (int aid: aids){
			if (! pendingIds.contains(aid)){
				throw new IllegalArgumentException("Asynchronous operation with ID: " + aid + " is not pending => program will deadlock");
			}
			pendingIds.remove(aid);
			waitFor.add(aid);
		}

		Wait w = new Wait();
		w.setWaitFor(waitFor);
		appBuilder.addCommand(rank, w);
	}

	/**
	 * Add a WaitAll, which will wait for all pending asynchronous operations of this client.
	 * @param rank
	 */
	public void addWaitAll(int rank){
		ArrayList<Integer> pendingIds =  pendingAsynchronousIDsForClient.remove(rank);
		if (pendingIds == null){
			//throw new IllegalArgumentException("No pending asynchronous operations");
			pendingIds = new ArrayList<Integer>();
		}

		Wait w = new Wait();

		w.setWaitFor(pendingIds);
		appBuilder.addCommand(rank, w);
	}

}
