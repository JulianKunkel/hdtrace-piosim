/*
 * ModelInfo
 */

/*
 *  @author  Frank Panse
 *  @organization PVS University Heidelberg
 */

package viewer.zoomable;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.Color;

import viewer.common.Const;
import base.drawable.*;


public class ModelInfo 
{
   
    private static final String         FORMAT = Const.INFOBOX_TIME_FORMAT;
    private static       DecimalFormat  fmt    = null;
    private static       TimeFormat     tfmt   = null;
    private ModelInfoPanel     params_display = null;

    public ModelInfo() {
       if ( fmt == null ) {
           fmt = (DecimalFormat) NumberFormat.getInstance();
           fmt.applyPattern( FORMAT );
       }
       if ( tfmt == null )
           tfmt = new TimeFormat();
    }  
    
    
    public void showInfo( DrawObjects dobj ) {
       double duration;
       
       if ( (dobj != null) || (dobj instanceof Primitive) ) {
           this.setStartTime( fmt.format( ( (Primitive) dobj ).getStartVertex().time ) );
               this.setEndTime( fmt.format( ( (Primitive) dobj ).getFinalVertex().time ) );
               duration = ( (Primitive) dobj ).getFinalVertex().time   
                         -( (Primitive) dobj ).getStartVertex().time;
               this.setDuration( fmt.format( duration ) );             
           this.setCategoryName(dobj.getCategory().getName());    
           this.setCategoryColor( (Color) dobj.getCategory().getColor() );
           this.setInfoString( ( (Primitive) dobj ).toInfoBoxString().trim() );
       } else {
           this.reset();
       }
    }
    
    
    public void reset() {
        params_display.reset();    
    }
    
    
    public void setTime(String start, String end) {
      params_display.setStartTime(start);
      params_display.setEndTime(end);
    }

    public void setStartTime(String time) {
      params_display.setStartTime(time);
    }
    
    public void setEndTime(String time) {
      params_display.setEndTime(time);
    }      

    public void setInfoString(String info) {
      params_display.setInfoString(info);
    }      

    public void setCategoryName(String type) {
      params_display.setCategoryName(type);
    }                  

    public void setCategoryColor(Color color) {
        params_display.setCategoryColor(color);
      }        
    
    public void setDuration(String duration) {
      params_display.setDuration(duration);

    }

    public void setParamDisplay( ModelInfoPanel tl )
    {
        params_display = tl;
    }

    public void removeParamDisplay()
    {
        params_display = null;
    }

    public String toString()
    {
        String str_rep =  "" ;
        return getClass().getName() + "{" + str_rep + "}";
    }
}
