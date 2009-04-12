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

package drawable;



public class TimeBoundingBox
{
    private              double           earliest_time;
    private              double           latest_time;

    public void reinitialize(){
        earliest_time = Double.POSITIVE_INFINITY;
        latest_time   = Double.NEGATIVE_INFINITY;
    }
    
    public TimeBoundingBox() {
    	reinitialize();
	}

    public TimeBoundingBox( final TimeBoundingBox timebox )
    {
        earliest_time = timebox.earliest_time;
        latest_time   = timebox.latest_time;
    }

    public void affectTimeBounds( final TimeBoundingBox endtimes )
    {
        this.affectEarliestTime( endtimes.getEarliestTime() );
        this.affectLatestTime( endtimes.getLatestTime() );
    }

    public void affectEarliestTime( double in_time )
    {
        if ( in_time < earliest_time )
            earliest_time = in_time;
    }

    public void setEarliestTime( double in_time )
    {
        earliest_time = in_time;
    }

    public double getEarliestTime()
    {
        return earliest_time;
    }


    public void affectLatestTime( double in_time )
    {
        if ( in_time > latest_time )
            latest_time = in_time;
    }

    public void setLatestTime( double in_time )
    {
        latest_time = in_time;
    }

    public double getLatestTime()
    {
        return latest_time;
    }


    public double getBorderTime( boolean isStartTime )
    {
        if ( isStartTime )
            return earliest_time;
        else
            return latest_time;
    }


    // Functions useful in ScrollableObject
    // This is used after this.setEarliestTime() is invoked.
    // time_extent is positive definite
    public void setLatestFromEarliest( double time_extent )
    {
        latest_time = earliest_time + time_extent;
    }

    // This is used after this.setLatestTime() is invoked.
    // time_extent is positive definite
    public void setEarliestFromLatest( double time_extent )
    {
        earliest_time = latest_time - time_extent;
    }

    public boolean isTimeOrdered()
    {
        return earliest_time <= latest_time;
    }

    // Return true when endtimes covers the one end of this.TimeBoundingBox
    public boolean remove( final TimeBoundingBox  endtimes )
    {
        if ( this.earliest_time == endtimes.earliest_time ) {
            this.earliest_time = endtimes.latest_time; 
            return true;
        }
        if ( this.latest_time == endtimes.latest_time ) {
            this.latest_time = endtimes.earliest_time; 
            return true;
        }
        return false;
    }

    //  TimeBoundingBox Checking Routines for SLOG-2 Input API
    /*
       Logic concerning overlaps(), covers() and disjoints()
       1) covers()      implies  overlaps().
       2) !overlaps()   implies  disjoints().
       3) !disjoints()  implies  overlaps().
    */
    public boolean covers( final TimeBoundingBox endtimes )
    {
        return    ( this.earliest_time <= endtimes.earliest_time )
               && ( endtimes.latest_time <= this.latest_time );
    }

    public boolean overlaps( final TimeBoundingBox endtimes )
    {
        return    ( this.earliest_time <= endtimes.latest_time )
               && ( endtimes.earliest_time <= this.latest_time );
    }

    // For consistence: Avoid using disjoints(), use !overlaps() instead
    public boolean disjoints( final TimeBoundingBox endtimes )
    {
        return    ( this.latest_time < endtimes.earliest_time )
               || ( endtimes.latest_time < this.earliest_time );
    }

    public boolean contains( double timestamp )
    {
        return    ( this.earliest_time <= timestamp )
               && ( timestamp <= this.latest_time );
    }

    public boolean equals( final TimeBoundingBox endtimes )
    {
        return    ( this.earliest_time == endtimes.earliest_time )
               && ( this.latest_time == endtimes.latest_time );
    }

    /*
       containsWithinLeft()/containsWithinRight() are for logformat.slog2.Print
       Or they are for logformat.slog2.input.InputLog.iterator()
    */
    public boolean containsWithinLeft( double timestamp )
    {
        return    ( this.earliest_time <= timestamp )
               && ( timestamp < this.latest_time );
    }

    public boolean containsWithinRight( double timestamp )
    {
        return    ( this.earliest_time < timestamp )
               && ( timestamp <= this.latest_time );
    }

    public TimeBoundingBox getIntersection( final TimeBoundingBox endtimes )
    {
        TimeBoundingBox  intersect_endtimes;
        double           intersect_earliest_time, intersect_latest_time;

        if ( this.overlaps( endtimes ) ) {
            if ( this.earliest_time < endtimes.earliest_time )
                intersect_earliest_time = endtimes.earliest_time;
            else
                intersect_earliest_time = this.earliest_time;
            if ( this.latest_time < endtimes.latest_time )
                intersect_latest_time   = this.latest_time;
            else
                intersect_latest_time   = endtimes.latest_time;
            intersect_endtimes = new TimeBoundingBox();
            intersect_endtimes.earliest_time  = intersect_earliest_time;
            intersect_endtimes.latest_time    = intersect_latest_time;
            return intersect_endtimes;
        }
        else
            return null;
    }

    public double getIntersectionDuration( final TimeBoundingBox endtimes )
    {
        double           intersect_earliest_time, intersect_latest_time;
        double           intersect_duration;

        if ( this.overlaps( endtimes ) ) {
            if ( this.earliest_time < endtimes.earliest_time )
                intersect_earliest_time = endtimes.earliest_time;
            else
                intersect_earliest_time = this.earliest_time;
            if ( this.latest_time < endtimes.latest_time )
                intersect_latest_time   = this.latest_time;
            else
                intersect_latest_time   = endtimes.latest_time;
            intersect_duration  = intersect_latest_time
                                - intersect_earliest_time;
            if ( intersect_duration > 0.0d )
                return intersect_duration;
            else
                return 0.0d;
        }
        else
            return 0.0d;
    }

    /* For SLOG-2 Input API & viewer */
    public double getDuration()
    {
        return latest_time - earliest_time;
    }

    public void setZeroDuration( double time )
    {
        earliest_time = time;
        latest_time   = time;
    }
}
