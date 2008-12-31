
//	Copyright (C) 2008, 2009 Julian M. Kunkel
//	
//	This file is part of PIOsimHD.
//	
//	PIOsimHD is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	PIOsimHD is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PIOsimHD.  If not, see <http://www.gnu.org/licenses/>.

package de.hd.pvs.piosim.model.program;

import java.io.FileWriter;

/**
 * This class creates valid XML from an Application object.
 * TODO: this class is not implemented right now!
 * 
 * @author Julian M. Kunkel
 */
public class ApplicationXMLWriter {
	
	/**
	 * Create the XML for an application and write it to the file.
	 * @param app
	 * @param file
	 * @throws Exception
	 */
	public void writeXMLFromApplication(Application app, String file) throws Exception{
		StringBuffer buff = new StringBuffer();
		createXMLFromApplication(app, buff);
		
		FileWriter f = new FileWriter(file);
		f.write(buff.toString());
		f.close();	
	}
	
	/**
	 * Create the XML content for an application in a StringBuffer.
	 * 
	 * @param app
	 * @param sb
	 */
	public void createXMLFromApplication(Application app, StringBuffer sb){

	}
	///////////////////////////////////////////////////////////////////////////////

}
