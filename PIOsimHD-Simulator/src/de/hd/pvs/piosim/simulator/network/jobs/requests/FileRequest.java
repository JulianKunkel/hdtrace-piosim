package de.hd.pvs.piosim.simulator.network.jobs.requests;

import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.simulator.network.jobs.NetworkSimpleData;

public class FileRequest extends NetworkSimpleData{

	/**
	 * The file on which the I/O should be performed.
	 */
	private final FileMetadata file;

	public FileRequest(FileMetadata file, long size) {
		super(size);
		this.file = file;
	}

	final public FileMetadata getFile() {
		return file;
	}
}
