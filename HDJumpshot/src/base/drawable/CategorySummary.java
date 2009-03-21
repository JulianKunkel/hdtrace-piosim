/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package base.drawable;

import java.io.DataInput;

public class CategorySummary extends CategoryRatios
{
    private long         num_real_objs;

    public CategorySummary()
    {
        super();
        num_real_objs  = 0;
    }

    // For SLOG-2 Output
    public CategorySummary( float new_incl_r, float new_excl_r,
                            long new_num_real_objs )
    {
        super( new_incl_r, new_excl_r );
        num_real_objs  = new_num_real_objs;
    }

    // For SLOG-2 Output
    public CategorySummary( final CategorySummary type_smy )
    {
        super( type_smy );
        this.num_real_objs  = type_smy.num_real_objs;
    }

    public long getDrawableCount()
    {
        return num_real_objs;
    }

    public void addDrawableCount( long new_num_real_objs )
    {
        this.num_real_objs  += new_num_real_objs;
    }

    public CategorySummary( DataInput ins )
    throws java.io.IOException
    {
        super();
        this.readObject( ins );
    }

    public String toString()
    {
        return super.toString() + ", count=" + num_real_objs;
    }
}
