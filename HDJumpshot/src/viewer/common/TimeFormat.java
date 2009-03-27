//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package viewer.common;

import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TimeFormat
{
	private static final double[] LIMITS  = {Double.NEGATIVE_INFINITY, 0.0d,
		0.1E-9, 0.1E-6, 0.1E-3, 0.1d};
	private static final String[] UNITS   = {"-ve", "ps", "ns",
		"us", "ms", "s" };
	private static final String   PATTERN = "#,##0.00###";

	private              DecimalFormat decfmt   = null;
	private              ChoiceFormat  unitfmt  = null;

	public TimeFormat()
	{
		decfmt = (DecimalFormat) NumberFormat.getInstance();
		decfmt.applyPattern( PATTERN );
		unitfmt = new ChoiceFormat( LIMITS, UNITS );
	}

	public String format( double time )
	{
		String unit = unitfmt.format( Math.abs( time ) );
		if ( unit.equals( "s" ) )
			return decfmt.format(time) + " sec";
		else if ( unit.equals( "ms" ) )
			return decfmt.format(time * 1.0E3) + " msec";
		else if ( unit.equals( "us" ) )
			return decfmt.format(time * 1.0E6) + " usec";
		else if ( unit.equals( "ns" ) )
			return decfmt.format(time * 1.0E9) + " nsec";
		else if ( unit.equals( "ps" ) )
			return decfmt.format(time * 1.0E12) + " psec";
		else
			return decfmt.format(time) + " sec";
	}
}
