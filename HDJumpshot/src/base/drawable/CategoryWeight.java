/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package base.drawable;

import java.io.DataInput;

/*
   CategoryWeight extends CategorySummary which extends CategoryRatios
*/
public class CategoryWeight extends CategorySummary
{
    public  static final int         BYTESIZE        = CategorySummary.BYTESIZE
                                                     + 4;  // type_idx
    private static final int INVALID_INDEX = Integer.MIN_VALUE;

    private Category   type;

    private int        width;    // pixel width, for SLOG-2 Input & Jumpshot
    private int        height;   // pixel height, for SLOG-2 Input & Jumpshot

    public CategoryWeight()
    {
        super();
        type           = null;
        width          = 0;
        height         = 0;
    }

    public void setPixelWidth( int wdh )
    {
        width = wdh;
    }

    public int getPixelWidth()
    {
        return width;
    }

    public void setPixelHeight( int hgt )
    {
        height = hgt;
    }

    public int getPixelHeight()
    {
        return height;
    }

    public Category getCategory()
    {
        return type;
    }

    // For InfoPanelForDrawable
    public String toInfoBoxString( int print_status )
    {
        StringBuffer rep = new StringBuffer( "legend=" );
        if ( type != null )
            rep.append( type.getName() );

        rep.append( ", " );
        rep.append( super.toInfoBoxString( print_status ) );
        
        return rep.toString();
    }

}
