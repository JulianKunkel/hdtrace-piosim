package de.hd.pvs.piosim.simulator.program.Filereadall.Splitter;

import java.util.LinkedList;

import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.ClientPhaseOperations;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.IOData;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.MultiPhaseClientOP;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.MultiPhaseContainer;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.MultiPhaseRun;

/**
 * Range divided by 3 clients: 012 012 012 0
 * Operations per phase:       000 111 222 3 (the 3 phase is partial, the last client might read partial twoPhaseBufferSize)
 **/
public class ContiguousIOSplitter extends TwoPhaseIOSplitter {
	public MultiPhaseRun initMultiphasesOnce(final long totalsize, MultiPhaseContainer mp, LinkedList<IOData> iops){
		assert(totalsize >= 0);

		final MultiPhaseRun mpr = new MultiPhaseRun(totalsize);
		mpr.ioAggregators = this.ioaggregators < 1 ? mp.clients.size() :  ( this.ioaggregators >  mp.clients.size() ?  mp.clients.size() : this.ioaggregators) ;

		final long bytesPerFullPhase  =  mpr.ioAggregators * twoPhaseBufferSize;
		mpr.fullPhases = (int) (mpr.totalAccessSize / bytesPerFullPhase);

		final long lastPhaseBytes = mpr.totalAccessSize - mpr.fullPhases *  bytesPerFullPhase;
		// assign the jobs to the clients AND to the phases

		mpr.lastAndPartialPhaseAggregators = (int) ((lastPhaseBytes + twoPhaseBufferSize -1) / twoPhaseBufferSize);

		final long remainderForLastAggregator =  lastPhaseBytes - (mpr.lastAndPartialPhaseAggregators -1) *  twoPhaseBufferSize;

		final int totalPhaseCount = mpr.getPhaseCount();
		assert(totalPhaseCount >= 0);

		final long startOffset = iops.get(0).offset;

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
				final long offset = startOffset + i * bytesPerFullPhase + c * twoPhaseBufferSize;

				io.addIOOperation(offset + remainderLastPhase , twoPhaseBufferSize);

				phases.setPhase(io, i);
			}


			// perform last phase:

			if(c <= lastAggregator){
				// last aggregator might access partial data.
				final ListIO io = new ListIO();

				// remainder from lastPhase (some clients might do).
				final long offset = startOffset + mpr.fullPhases * bytesPerFullPhase + c * twoPhaseBufferSize;
				final long size = (c < clientsTillPartial ? twoPhaseBufferSize :  remainderForLastAggregator) ;

				io.addIOOperation(offset + remainderLastPhase, size);

				phases.setPhase(io, mpr.fullPhases);
			}

			c++;
		}
		return mpr;
	}

}
