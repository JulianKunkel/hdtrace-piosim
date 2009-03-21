package viewer;



public class Globals {
	static private TraceFormatBufferedFileReader  reader;

	
	public static TraceFormatBufferedFileReader getProjectReader() {
		return reader;
	}
	
	public static void setProjectReader(TraceFormatBufferedFileReader tReader){
		reader = tReader;
	}
}
