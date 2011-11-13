package de.hd.pvs.piosim.simulator.program.Filereadall.Splitter;

import java.util.LinkedList;

import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.IOData;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.MultiPhaseContainer;
import de.hd.pvs.piosim.simulator.program.Global.MultiPhase.MultiPhaseRun;

abstract public class IOSplitter {
	static final protected long twoPhaseBufferSize = 16777216; // 16 MiB 16777216
	final protected int  ioaggregators = 0; // <= 0 means all !

	/**
	 * This function is called once, when it became clear we will use a multi-phase.
	 * It must set the number of ioaggregators used and maxIter
	 * @return the number of phases
	 */
	abstract public MultiPhaseRun initMultiphasesOnce(final long totalsize, MultiPhaseContainer mp,  LinkedList<IOData> iops);

	/**
	 * This method is invoked, once all clients entered two-phase mode and synchronized virtually.
	 * @param container
	 * @return
	 */
	abstract public boolean checkMultiPhase(MultiPhaseContainer container);
}
