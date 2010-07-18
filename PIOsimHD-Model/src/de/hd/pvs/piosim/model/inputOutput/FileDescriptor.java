package de.hd.pvs.piosim.model.inputOutput;

import de.hd.pvs.piosim.model.program.Communicator;

/**
 * Contains information about the opened file.
 *
 * @author julian
 *
 */
public class FileDescriptor {
	final FileMetadata file;
	final Communicator communicator;

	public FileDescriptor(FileMetadata file, Communicator comm) {
		this.file = file;
		this.communicator = comm;
	}

	public FileMetadata getFile() {
		return file;
	}

	public Communicator getCommunicator() {
		return communicator;
	}
}
