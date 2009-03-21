/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author  Anthony Chan
 */

package base.drawable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Category
{
    private String          name;
    private Topology        topo;         // private Shape    shape;
    private ColorAlpha      color;
    private int             width;
    private CategorySummary summary;

    private boolean         isVisible;    
    private boolean         isSearchable;

    public Category( String in_name, Topology in_topo,
                     ColorAlpha in_color, int in_width )
    {
        name         = in_name;
        topo         = in_topo;
        color        = in_color;
        width        = in_width;
        summary      = new CategorySummary();
        isVisible    = true;
        isSearchable = true;
    }

    public void setVisible( boolean new_value )
    {
        isVisible = new_value;
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    public void setSearchable( boolean new_value )
    {
        isSearchable = new_value;
    }

    public boolean isSearchable()
    {
        return isSearchable;
    }

    public boolean isVisiblySearchable()
    {
        return isVisible && isSearchable;
    }

    public void setName( String in_name )
    {
        name       = in_name;
    }

    public String getName()
    {
        return name;
    }

    public void setTopology( Topology in_topo )
    {
        topo       = in_topo;
    }

    public Topology getTopology()
    {
        return topo;
    }

    /*
     * load default colors from current_dir ! Kinda hack !
     */
    static private HashMap<String, ColorAlpha> colorMapping = new HashMap<String,ColorAlpha>();

    static {
    	Properties prop = new Properties();
    	try{
    		prop.load(new FileInputStream("jumpshot-color.property"));

    		Pattern pattern =  Pattern.compile("RGB[(]([0-9]*),([0-9]*),([0-9]*)[)]", Pattern.DOTALL);
    		for(Object c: prop.keySet()){
    			String cat = (String) c;
    			String val = prop.getProperty(cat);
    			int r,g,b;

        		Matcher m = pattern.matcher( val );

        		if(! m.find()){
        			System.err.println("Warning could not parse color " + cat + " = " + val + " in jumpshot-color.property" );
        		}else{
        			colorMapping.put(cat, new ColorAlpha(
        					Integer.parseInt(m.group(1)),
        					Integer.parseInt(m.group(2)),
        					Integer.parseInt(m.group(3))
        					));
        		}
    		}
    	}catch(IOException e){
    		System.err.println("Warning accessing jumpshot-color.property not possible: " + e.getMessage());
    	}
    }

    private void saveColors(){
    	Properties prop = new Properties();
    	try{
    		for(String cat: colorMapping.keySet()){
    			 ColorAlpha c = colorMapping.get(cat);
    			 prop.put(cat, "RGB(" + c.getRed() + ","  + c.getGreen() +  ","  + c.getBlue()  + ")");
    		}
    		prop.save(new FileOutputStream("jumpshot-color.property"), "");
    	}catch(IOException e){
    		System.err.println("Warning accessing jumpshot-color.property not possible: " + e.getMessage());
    	}
    }

    public void setColor( ColorAlpha in_color )
    {
        colorMapping.put(getName(), in_color);
        saveColors();

        color      = in_color;
    }

    public ColorAlpha getColor()
    {
        if( colorMapping.containsKey(getName()) ){
        	return colorMapping.get(getName());
        }

        return color;
    }

    // For logformat.slog2.update.UpdatedInputLog
    public void setWidth( int in_width )
    {
        width      = in_width;
    }

    // For logformat.slog2.update.UpdatedInputLog
    public int getWidth()
    {
        return width;
    }


    
    public CategorySummary getSummary()
    {
        return this.summary;
    }
    
}
