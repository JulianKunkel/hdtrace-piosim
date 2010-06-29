//	Copyright (C) 2010 Timo Minartz
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
package de.hd.pvs.piosim.power.tests;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests {
	
	private static String[] excludeTestFilenames = {"JFreeChartVisualizerTest","ThesisPicturesTest","CalculateTracePowerTest"};

	@SuppressWarnings("unchecked")
	public static Test suite() {
	
		String packageName = "de.hd.pvs.piosim.power.tests";
		
		TestSuite suite = new TestSuite("Tests for " + packageName);
		
		AbstractTestCase.setGlobalVisualizer("de.hd.pvs.piosim.power.data.visualizer.CommandlineVisualizer");

		File folder = new File("src/" + packageName.replace('.', '/'));

		String[] filenames = folder.list();

		for (String filename : filenames) {
			if (filename.endsWith("Test.java")) {
				filename = filename.substring(0, filename.lastIndexOf(".java"));
				try {
					boolean include = true;
					for(int i=0; i<excludeTestFilenames.length; ++i) {
						if(filename.equals(excludeTestFilenames[i]))
							include = false;
					}
					
					if(include)
						suite.addTestSuite((Class<? extends TestCase>) Class.forName(packageName + "." + filename));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		
		return suite;
	}

}
