package de.hd.pvs.traceConverter;

import java.util.Formatter;

public class SimpleEpoch implements Comparable<SimpleEpoch> {
	/** 10^-9 seems sufficient right now */
	private final int seconds;
	private final int nanoSeconds;

	/**
	 * Used to convert the internal values to double and vice versa. 
	 */
	final static private int MULTIPLIER = 1000000000;
	
	/**
	 * Create a valid epoch. 
	 * 
	 * @param seconds
	 * @param nanoSeconds
	 */
	public SimpleEpoch(int seconds, int nanoSeconds) {
		this.seconds = seconds;
		this.nanoSeconds = nanoSeconds;
	}

	/**
	 * Read Epoch from String
	 * @param time
	 * @return
	 */
	static public SimpleEpoch parseTime(String time) {
		/* sec.subsec */
		double d = Double.parseDouble(time);
		int seconds = (int) d;
		int nanoSeconds = (int) ((d - seconds) * MULTIPLIER);

		return new SimpleEpoch(seconds, nanoSeconds);
	}

	/**
	 * Convert Epoch to String but remove trailing zeros.
	 */
	@Override
	public String toString() {
		Formatter timeFormatter = new Formatter();
		/* This function is rather inefficient but rarely called ... */
		String out = timeFormatter.format("%09d", nanoSeconds).toString();

		/* strip the last zeros */
		int lastZeros;
		for(lastZeros=out.length()-1; lastZeros >= 0 ; lastZeros--){
			if( out.charAt(lastZeros) != '0')
				break;
		}	

		if (lastZeros > 0){
			return seconds
			+ "." //%+10.4f %9d
			+ out.substring(0, lastZeros + 1) + "s";
		}else{
			return seconds + "s";
		}
	}

	/**
	 * Compare two Epochs, the earlier Epoch is "smaller".
	 */
	public int compareTo(SimpleEpoch t) {
		if (seconds < t.seconds) {
			return -1;
		} else if (seconds > t.seconds) {
			return 1;
		}

		if (nanoSeconds < t.nanoSeconds) {
			return -1;
		} else if (nanoSeconds > t.nanoSeconds) {
			return 1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		// unlikely to produce much collisions:
		return seconds + nanoSeconds;
	}

	/**
	 * two Epochs are really Equal if the time spot is equal.
	 */
	@Override
	public boolean equals(Object obj) {
		return compareTo((SimpleEpoch) obj) == 0; 
	}
}
