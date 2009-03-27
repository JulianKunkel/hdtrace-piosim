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

package drawable;

public interface CoordPixelXform
{
    public int     convertTimeToPixel( double time_coord );

    public double  convertPixelToTime( int hori_pixel );

    public int     convertTimelineToPixel( int timeline );
    
    public int     getTimelineHeight();

    public int     convertPixelToTimeline( int vert_pixel );

    
    public boolean contains( double time_coord );

    public boolean overlaps( final TimeBoundingBox  timebox );

    public int     getImageWidth();
}
