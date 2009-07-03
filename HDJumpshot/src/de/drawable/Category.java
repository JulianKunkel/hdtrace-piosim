
 /** Version Control Information $Id: Category.java 220 2009-04-18 19:12:42Z kunkel $
  * @lastmodified    $Date: 2009-04-18 21:12:42 +0200 (Sa, 18. Apr 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 220 $ 
  */

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

package de.drawable;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public abstract class Category
{
    private String          name;
    private Color      color;

    private boolean         isVisible;   
    
    /**
     * Default color to category mapping.
     */
    static private HashMap<String, Color> colorMapping = new HashMap<String,Color>();

    static public void loadColors(String filename){
    	Properties prop = new Properties();
    	try{
    		prop.load(new FileInputStream(filename));

    		Pattern pattern =  Pattern.compile("RGB[(]([0-9]*),([0-9]*),([0-9]*)[)]", Pattern.DOTALL);
    		for(Object c: prop.keySet()){
    			String cat = (String) c;
    			String val = prop.getProperty(cat);

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
    

    static private void saveColors(){
    	Properties prop = new Properties();
    	try{
    		for(String cat: colorMapping.keySet()){
    			 Color c = colorMapping.get(cat);
    			 prop.put(cat, "RGB(" + c.getRed() + ","  + c.getGreen() +  ","  + c.getBlue()  + ")");
    		}
    		prop.store(new FileOutputStream("jumpshot-color.property"), "");
    	}catch(IOException e){
    		System.err.println("Warning accessing jumpshot-color.property not possible: " + e.getMessage());
    	}
    }
    
    /**
     * Create a new category.
     * @param in_name
     * @param in_color if null, then use default color, if unset then create a random color
     */
    public Category( String in_name,  ColorAlpha in_color )
    {
        name         = in_name;
        if(in_color == null){
        	color        = getColor();
        	if (color == null){
        		// create a random color
            final Random r = new Random();
        		setColor(new ColorAlpha(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
        	}
        }
        isVisible    = true;
    }

    abstract public void setSearchable( boolean new_value );
    abstract public boolean isSearchable();
    abstract public VisualizedObjectType getTopologyType();
    

    public final boolean isVisiblySearchable(){
    	return isSearchable() & isVisible();
    }

    public void setVisible( boolean new_value )
    {
        isVisible = new_value;
    }

    public boolean isVisible()
    {
        return isVisible;
    }
    
    public void setName( String in_name )
    {
        name       = in_name;
    }

    public String getName()
    {
        return name;
    }

    public void setColor( Color in_color )
    {
        colorMapping.put(getName(), in_color);
        saveColors();

        color      = in_color;
    }

    public Color getColor()
    {
        if( colorMapping.containsKey(getName()) ){
        	return colorMapping.get(getName());
        }

        return color;
    }
    
}
