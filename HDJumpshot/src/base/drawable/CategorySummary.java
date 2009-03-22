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

    public long getDrawableCount()
    {
        return num_real_objs;
    }

    public void addDrawableCount( long new_num_real_objs )
    {
        this.num_real_objs  += new_num_real_objs;
    }

    public String toString()
    {
        return super.toString() + ", count=" + num_real_objs;
    }
}
