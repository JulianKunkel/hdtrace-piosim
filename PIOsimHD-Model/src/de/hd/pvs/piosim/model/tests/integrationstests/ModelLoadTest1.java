
/** Version Control Information $Id$
 * @lastmodified    $Date$
 * @modifiedby      $LastChangedBy$
 * @version         $Revision$
 */


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

package de.hd.pvs.piosim.model.tests.integrationstests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestSuite;

import org.junit.Before;
import org.junit.Test;

import de.hd.pvs.TraceFormat.xml.XMLReaderToRAM;
import de.hd.pvs.piosim.model.Model;
import de.hd.pvs.piosim.model.ModelVerifier;
import de.hd.pvs.piosim.model.ModelXMLReader;
import de.hd.pvs.piosim.model.ModelXMLWriter;

/**
 * This test parses/read a XML model and creates
 * an XML file out of it, reloads the new XML file and compares it to the original.
 *
 * @author Julian M. Kunkel
 */
public class ModelLoadTest1  extends TestSuite {
	public String fancyPrint(Model model) throws Exception{
		/* generate XML output */
		StringBuffer newModelsb = new StringBuffer();
		ModelXMLWriter xmlWriter = new ModelXMLWriter();

		xmlWriter.createXMLFromModel(model, newModelsb);

		/* Print XML */
		String [] lines = newModelsb.toString().split("\n");
		int i = 1;
		for (String line: lines){
			System.out.println( i + " " + line);
			i++;
		}

		return newModelsb.toString();
	}

	@Before public void setUp() {
		/* setup log4j */
		System.out.println();
	}


	@Test public void test() throws Exception{

		ModelXMLReader xmlReader = new ModelXMLReader();

		File file = new File("src/"	+ ModelLoadTest1.class.getCanonicalName().replace(".", "/")
				+ "-model.xml");

		Model model = xmlReader.parseProjectXML(file.getAbsolutePath());

		System.out.println("My Model: \n" + model);
		System.out.println("My XML (rewritten): \n");

		String newModelstr = fancyPrint(model);

		/* generate XML input out of output */
		final String dirname = file.getParent();

		final XMLReaderToRAM reader = new XMLReaderToRAM();

		System.out.println("\n\nRereading model");

		Model newModel = xmlReader.readProjectXML(reader.convertXMLToXMLTag(newModelstr),
				dirname, null);


		StringBuffer newModelsb = new StringBuffer();
		ModelXMLWriter xmlWriter = new ModelXMLWriter();
		xmlWriter.createXMLFromModel(model, newModelsb);

		String newModel2Str = newModelsb.toString();

		System.out.println("Writing model to /tmp/out.xml");

		FileWriter w = new FileWriter(new File("/tmp/out.xml"));
		w.write(newModel2Str.toString());
		w.close();

		System.out.println("Rereading model and compare results");
		/* generate XML input out of output */
		Model newModel2 = xmlReader.readProjectXML(reader.convertXMLToXMLTag(newModel2Str), dirname, null);

		assertTrue(newModel2Str.compareTo(newModelstr) == 0);

		// test if the model is valid with ModelVerifier.
		ModelVerifier verifier = new ModelVerifier();
		verifier.checkConsistency(newModel2);
	}

	public static void main(String[] args) throws Exception{
		ModelLoadTest1 t = new ModelLoadTest1();
		t.test();
	}

}
