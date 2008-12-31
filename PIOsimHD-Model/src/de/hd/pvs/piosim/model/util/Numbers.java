
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

/**
 * Helper class to convert Units in Strings to the Numbers and vice versa.
 * For instance "10M" == 10 * 1 MByte
 * Only one Unit at the end of the String is valid. 
 * 
 * @author Julian M. Kunkel
 */

public class Numbers {
	/**
	 * Define the multipliers:
	 */
	static public final long KByte = 1024;
	static public final long MByte = KByte * 1024;
	static public final long GByte = MByte * 1024;
	static public final long PByte = GByte * 1024;

	static public final double MILI = 1e-3;
	static public final double MIKRO = 1e-6;
	static public final double NANO = 1e-9;
	static public final double PIKO = 1e-12;

	/**
	 * Define the String abbreviation for a particular Multiplier. 
	 * Units must be sorted from smallest to highest.
	 * Long values: 
	 */
	static private String[] longFormats = { "ms", "K", "M", "G", "P" };
	static private long[] longMultiplier = { 1,   KByte, MByte, GByte, PByte };

	/**
	 * Define the String abbreviation for a particular Multiplier.
	 * Units must be sorted from highest to smallest.
	 * Double values (typically used for durations/times).
	 */
	static private String[] doubleFormats = { "s", "m", "u", "n", "p" };
	static private double[] doubleMultiplier = { 1.0, MILI, MIKRO, NANO, PIKO };

	/**
	 * Convert a string which might contain a Unit at the end to a long value.
	 * @param value
	 * @return
	 * @throws NumberFormatException
	 */
	static public long getLongValue(String value) throws NumberFormatException {
		long multiplier = 1l;
		int last_int_pos = value.length();
		value = value.trim();
		for (int i = 0; i < longFormats.length; i++) {
			if (value.contains(longFormats[i])) {
				multiplier = Numbers.longMultiplier[i];
				last_int_pos = value.indexOf(longFormats[i]);
				break;
			}
		}

		return Long.parseLong(value.substring(0, last_int_pos)) * multiplier;
	}

	/**
	 * Convert a string which might contain a Unit at the end to a double value.
	 * @param value
	 * @return
	 * @throws NumberFormatException
	 */
	static public double getDoubleValue(String value)
	throws NumberFormatException {
		double multiplier = 1.0;
		int last_int_pos = value.length();
		value = value.trim();
		for (int i = 0; i < doubleFormats.length; i++) {
			if (value.contains(doubleFormats[i])) {
				multiplier = Numbers.doubleMultiplier[i];
				last_int_pos = value.indexOf(doubleFormats[i]);
			}
		}

		return Double.parseDouble(value.substring(0, last_int_pos)) * multiplier;
	}

	/**
	 * Convert the long value back to a nice string with the best unit if possible.  
	 * @param l
	 * @return
	 */
	static public String getNiceString(long l){
		if( l != 0){
			for (int i = longFormats.length-1 ; i > 0; i--) {
				long rest = l % longMultiplier[i];
				if (rest == 0){
					return "" + l / longMultiplier[i] + longFormats[i];
				}
			}
		}

		return "" + l;
	}

	/**
	 * Convert the double value back to a nice string with the best unit if possible.
	 * @param d
	 * @return
	 */
	static public String getNiceString(double d){
		if( d != 0.0){
			for (int i = doubleFormats.length-1 ; i > 0; i--) {
				double rest = d % doubleMultiplier[i];
				if (rest == 0.0){
					return "" + d / doubleMultiplier[i] + doubleFormats[i];
				}
			}
		}

		return "" + d;  }
}
