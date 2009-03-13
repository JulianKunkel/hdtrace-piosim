package de.hd.pvs.traceConverter;

public class FileNames {
	static public String getFilenameXML(String filePrefix, int rank, int thread){
		return filePrefix + "_" + rank +"_" + thread +".xml";
	}
	
	static public String getFilenameStatistics(String filePrefix, int rank, int thread, String statGroup){		
		return filePrefix + "_" + rank +"_" + thread + "_stat_" + statGroup + ".dat";
	}	
	
}
