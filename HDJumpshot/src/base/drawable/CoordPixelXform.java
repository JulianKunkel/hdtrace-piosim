/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package base.drawable;

public interface CoordPixelXform
{
    public int     convertTimeToPixel( double time_coord );

    public double  convertPixelToTime( int hori_pixel );

    public int     convertTimelineToPixel( int timeline );
    
    public int     getTimelineHeight(int timeline);

    public int     convertPixelToTimeline( int vert_pixel );

    
    public boolean contains( double time_coord );

    public boolean overlaps( final TimeBoundingBox  timebox );

    public int     getImageWidth();
}
