package de.hd.pvs.piosim.simulator.program.Filereadall.Splitter;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.ClientPhaseOperations;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.IOData;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.MultiPhaseClientOP;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.MultiPhaseContainer;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.MultiPhaseRun;

public class TwoPhaseIOSplitter extends IOSplitter {
	/**
	 * This function is called once, when it became clear we will use a multi-phase.
	 * It must set the number of i/o-aggregators used and maxIter
	 * @return the number of phases
	 */
	public MultiPhaseRun initMultiphasesOnce(final long totalsize, MultiPhaseContainer mp, LinkedList<IOData> iops){
		final MultiPhaseRun mpr = new MultiPhaseRun(totalsize);
		mpr.ioAggregators = this.ioaggregators < 1 ? mp.clients.size() :  ( this.ioaggregators >  mp.clients.size() ?  mp.clients.size() : this.ioaggregators) ;
		mpr.fullPhases = (int) (mpr.totalAccessSize / mpr.ioAggregators / twoPhaseBufferSize);

		final long lastPhaseBytes = mpr.totalAccessSize - mpr.fullPhases *   mpr.ioAggregators * twoPhaseBufferSize;
		// assign the jobs to the clients AND to the phases

		mpr.lastAndPartialPhaseAggregators = (int) ((lastPhaseBytes + twoPhaseBufferSize -1) / twoPhaseBufferSize);

		final long remainderForLastAggregator =  lastPhaseBytes - (mpr.lastAndPartialPhaseAggregators -1) *  twoPhaseBufferSize;

		final int totalPhaseCount = mpr.getPhaseCount();

		//long bytesToPerform = totalsize;
		// this iterator points to the next item to perform.
		//final Iterator<IOData> it = iops.iterator();
		//IOData curIO = it.next();

		final long startOffset = iops.get(0).offset;
		final long endOffset = iops.get(iops.size()-1).size + iops.get(iops.size()-1).offset;

		final long bytesPerClient = mpr.fullPhases * twoPhaseBufferSize;

		final int lastAggregator =  mpr.lastAndPartialPhaseAggregators - 1;

		// assign the I/O jobs to the clients and phases


		// client number
		int c = 0;

		// the amount of data for the last client:
		final long clientsTillPartial = remainderForLastAggregator / twoPhaseBufferSize;

		// we know each client performs the full I/O
		for(MultiPhaseClientOP client: mp.clients){
			// here we must add also normal clients!
			final ClientPhaseOperations phases = mpr.addClientOrAggregator(client.client, totalPhaseCount);

			if ( c > mpr.ioAggregators ){
				break;
			}

			long remainderLastPhase  = (c - lastAggregator) * twoPhaseBufferSize;
			remainderLastPhase =  (remainderLastPhase > 0 ? remainderLastPhase : 0);

			for(int i=0; i < mpr.fullPhases; i++){
				ListIO io = new ListIO();

				// remainder from lastPhase (some clients might do).
				final long offset = startOffset + i * twoPhaseBufferSize + c * bytesPerClient;

				io.addIOOperation(offset + remainderLastPhase , twoPhaseBufferSize);

				phases.setPhase(io, i);
			}


			// perform last phase:

			if(c <= lastAggregator){
				// last aggregator might access partial data.
				final ListIO io = new ListIO();

				// remainder from lastPhase (some clients might do).
				final long offset = startOffset + mpr.fullPhases * twoPhaseBufferSize +	c * bytesPerClient;
				final long size = (c < clientsTillPartial ? twoPhaseBufferSize :  remainderForLastAggregator) ;

				io.addIOOperation(offset + remainderLastPhase, size);

				phases.setPhase(io, mpr.fullPhases);
			}

			c++;
		}
		return mpr;
	}

	/**
	 * This method is invoked, once all clients entered two-phase mode and synchronized virtually.
	 * @param container
	 * @return
	 */
	public boolean checkMultiPhase(MultiPhaseContainer container){
		// sort the list of the I/O operations by the offset.
		Collections.sort(container.clients, new
			Comparator<MultiPhaseClientOP>() {
				@Override
				public int compare(MultiPhaseClientOP arg0, MultiPhaseClientOP arg1) {
					return arg0.firstByteAccessed < arg1.firstByteAccessed ? -1 :
						arg0.lastByteAccessed < arg1.lastByteAccessed ? -1 : +1; // maybe both starts are identical...
				}
			});

		boolean twoPhase = true;

		// check if the operations overlaps with the current op:
		long lastByteAccessed = container.clients.getFirst().firstByteAccessed;

		for(MultiPhaseClientOP client: container.clients){
			// the real two phase would have checked lastByteAccessed < client.firstByteAccessed ...
			if(lastByteAccessed > client.lastByteAccessed ){
				twoPhase = false;
				break;
			}
			lastByteAccessed = client.lastByteAccessed;
		}

		//System.out.println(this.getClass() + " MultiPhase: " + twoPhase);

		return twoPhase;
	}
}
