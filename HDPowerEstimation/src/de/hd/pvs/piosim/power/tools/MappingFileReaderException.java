package de.hd.pvs.piosim.power.tools;

public class MappingFileReaderException extends Exception  {
	
	private static final long serialVersionUID = 4563874128632699123L;

	public  MappingFileReaderException() {
		super();
	}
	
	public  MappingFileReaderException(String message) {
		super(message);
	}
	
	public  MappingFileReaderException(Exception ex) {
		super(ex.getMessage());
		super.setStackTrace(ex.getStackTrace());
	}
	
	public  MappingFileReaderException(String message, Exception ex) {
		super(message);
		super.setStackTrace(ex.getStackTrace());
	}
}
