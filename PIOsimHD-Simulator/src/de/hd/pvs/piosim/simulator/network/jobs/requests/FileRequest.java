package de.hd.pvs.piosim.simulator.network.jobs.requests;

import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;

public class FileRequest extends NetworkSimpleData{

	/**
	 * The file on which the I/O should be performed.
	 */
	private final MPIFile file;

	public FileRequest(MPIFile file, long size) {
		super(size);
		this.file = file;
	}

	final public MPIFile getFile() {
		return file;
	}
}
