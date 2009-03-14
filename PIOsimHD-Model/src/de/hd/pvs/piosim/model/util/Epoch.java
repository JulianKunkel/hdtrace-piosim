
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.model.util;

import java.util.Formatter;

/**
 * Epoch is a well defined "time" which is immutable.
 * It can be either a time difference of something or a actual time point.
 * Internally it uses a data type which guarantees precision even for "large" Epochs. 
 * 
 * @author Julian M. Kunkel
 *
 */
public class Epoch implements Comparable<Epoch> {
	/** 10^-9 seems sufficient right now */
	private final int seconds;
	private final int nanoSeconds;

	/**
	 * Used to convert the internal values to double and vice versa. 
	 */
	final static private int MULTIPLIER = 1000000000;

	/**
	 * Null time, sometimes used.
	 */
	final static public Epoch ZERO = new Epoch(0.0);

	/**
	 * Create a valid epoch time based on the argument, note that val can be rounded.
	 * @param val
	 */
	public Epoch(double val) {
		this.seconds = (int) val;
		this.nanoSeconds = (int) ((val - this.seconds) * MULTIPLIER);
	}

	/**
	 * Create a valid epoch by converting the nanoseconds to seconds.
	 * This constructor might round the actual values. 
	 * 
	 * @param nanoseconds
	 */
	public Epoch(long nanoseconds){
		this((int) (nanoseconds / MULTIPLIER), (int) (nanoseconds % MULTIPLIER));
	}

	/**
	 * Create a valid epoch. 
	 * 
	 * @param seconds
	 * @param nanoSeconds
	 */
	public Epoch(int seconds, int nanoSeconds) {
		this.seconds = seconds;
		this.nanoSeconds = nanoSeconds;
	}

	/**
	 * Add the time and return a new Time
	 */
	public Epoch add(Epoch t) {
		if (t.nanoSeconds + this.nanoSeconds >= MULTIPLIER) {
			return new Epoch( t.seconds + this.seconds + 1,  this.nanoSeconds + t.nanoSeconds - MULTIPLIER);
		}else if(t.nanoSeconds + this.nanoSeconds < 0){
			return new Epoch( t.seconds + this.seconds - 1,  this.nanoSeconds + t.nanoSeconds + MULTIPLIER);
		}else{
			return new Epoch( t.seconds + this.seconds, t.nanoSeconds + this.nanoSeconds);
		}
	}

	/**
	 * Add the time as specified with the unprecise double value.
	 */
	public Epoch add(double offset) {
		int nanoSecs = (int)((offset - (int) offset) * MULTIPLIER);
		int secs = (int) offset;

		if (this.nanoSeconds + nanoSecs >= MULTIPLIER) {
			return new Epoch(this.seconds + secs + 1, this.nanoSeconds + nanoSecs - MULTIPLIER);
		}else if(nanoSecs + this.nanoSeconds < 0){
			return new Epoch( secs + this.seconds - 1,  this.nanoSeconds + nanoSecs + MULTIPLIER);
		}else{
			return new Epoch(this.seconds + secs, this.nanoSeconds + nanoSecs);
		}
	}

	/**
	 * Subtract from this Epoch another Epoch, does not allow to subtract two negative values!
	 * @param sub
	 * @return
	 */
	public Epoch subtract(Epoch sub){
		int seconds;
		int nanosec;
		if (sub.nanoSeconds > this.nanoSeconds) {
			seconds = this.seconds - sub.seconds - 1;
			nanosec = this.nanoSeconds - sub.nanoSeconds + MULTIPLIER;
		}else{
			nanosec = this.nanoSeconds - sub.nanoSeconds;
			seconds = this.seconds - sub.seconds;
		}

		return new Epoch(seconds, nanosec);
	}

	/**
	 * Read Epoch from String
	 * @param time
	 * @return
	 */
	static public Epoch parseTime(String time) {
		int multiplier = 1;

		// split extension
		if(time.endsWith("ms")){
			time = time.substring(0, time.length()-2);
			multiplier = 1000;
		}
		if(time.endsWith("s")){
			time = time.substring(0, time.length()-1);
		}		

		if(time.contains(".")){
			final int pos = time.indexOf('.');
			final String secondsS = time.substring(0, pos);
			final String subSecondsS = time.substring(pos + 1);

			final int seconds = Integer.parseInt(secondsS);
			int subSeconds = Integer.parseInt(subSecondsS);

			for (int i= (9 - subSecondsS.length()); i > 0; i-- ){
				subSeconds *=10;
			}
			
			if(time.startsWith("-")){			
				return new Epoch(seconds / multiplier, (seconds % multiplier) * (MULTIPLIER / multiplier) - subSeconds / multiplier);
			}else{
				return new Epoch(seconds / multiplier, (seconds % multiplier) * (MULTIPLIER / multiplier)  + subSeconds / multiplier);
			}
		}else{
			final int seconds = Integer.parseInt(time);

			return new Epoch(seconds / multiplier, (seconds % multiplier) * (MULTIPLIER / multiplier));						
		}
	}

	/**
	 * Return a string with all digits.
	 * @return
	 */
	public String getFullDigitString(){
		Formatter timeFormatter = new Formatter();		
		
		if(seconds == 0 && nanoSeconds < 0){
			return seconds + "." + timeFormatter.format("%09d", -nanoSeconds).toString() + "s";
		}else{
			return seconds + "." + timeFormatter.format("%09d", nanoSeconds).toString() + "s";
		}		
	}

	/**
	 * Convert Epoch to String but remove trailing zeros.
	 */
	@Override
	public String toString() {
		return toNormalizedString() + "s";
	}

	/**
	 * Normalize to second and do not print unit.
	 * @return
	 */
	public String toNormalizedString(){
		String prefix = "";
		
		Formatter timeFormatter = new Formatter();
		/* This function is rather inefficient but rarely called ... */
		String out;
		
		if(seconds == 0 && nanoSeconds < 0){
			prefix = "-";
			out = timeFormatter.format("%09d", -nanoSeconds).toString();
		}else{
			out = timeFormatter.format("%09d", nanoSeconds).toString();
		}
		
		/* strip the last zeros */
		int lastZeros;
		for(lastZeros=out.length()-1; lastZeros >= 0 ; lastZeros--){
			if( out.charAt(lastZeros) != '0')
				break;
		}	
		
		if (lastZeros >= 0){
			return prefix + seconds
			+ "." //%+10.4f %9d
			+ out.substring(0, lastZeros + 1);
		}else{
			return "" + seconds;
		}		
	}
	
	/**
	 * Compare two Epochs, the earlier Epoch is "smaller".
	 */
	public int compareTo(Epoch t) {
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
		return compareTo((Epoch) obj) == 0; 
	}

	/**
	 * Return only the seconds of this Epoch. 
	 * @return
	 */
	public int getSeconds() {
		return seconds;
	}

	/**
	 * return only the nanoseconds of this Epoch
	 * @return
	 */
	public int getNanoSeconds() {
		return nanoSeconds;
	}

	/**
	 * Return the Epoch time in Nanoseconds.
	 * @return
	 */
	public long getLongTimeAsNS(){
		return seconds * MULTIPLIER + nanoSeconds; 
	}

	/**
	 * Return the Epoch time as double. 
	 * @return
	 */
	public double getDouble(){
		return this.seconds + ((double) this.nanoSeconds) / MULTIPLIER;
	}	

	/**
	 * This method returns the smallest possible time which is really bigger than this time.
	 * @return
	 */
	public Epoch getNextLaterTime() {
		return new Epoch(this.getSeconds(), this.getNanoSeconds() + 10);
	}	

	/**
	 * The time resolution possible with this class. 
	 * @return
	 */
	static public double getTimeResolution() {
		return 1.0 / MULTIPLIER;
	}
}
