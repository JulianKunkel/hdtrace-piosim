package de.hd.pvs.traceConverter;

public class SimpleConsoleLogger {

	/**
	 * If true then all debug messages are printed.
	 */
	static boolean debugEverything = false;
	
	static public void setDebugEverything(boolean debugEverythin) {
		debugEverything = debugEverythin;
	}

	static public boolean isDebugEverything() {
		return debugEverything;
	}
	
	/**
	 * Write the debugging string if necessary.
	 * @param what
	 */
	static public void Debug(String what){
		if(debugEverything)
			System.err.println(what);
	}

}
