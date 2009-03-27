
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

package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.ModelSortIDbySubcomponents;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.SimulationResults;
import de.hd.pvs.piosim.simulator.Simulator;
public class JacobiTestIO extends ClusterTest{
	final long KBYTE = 1024;
	final long MBYTE = 1024*KBYTE;
	
	public boolean shouldSortModel = false; 
	
	@Before public void setUp() { 
	}
	
	@After public void tearDown(){
		System.out.println();
		Assert.assertTrue(true); /* to ensure assert stays */
	}
		
	public double runJacobiLow(String which) throws Exception{
		ModelBuilder mb = createDisjointClusterModel(10, 5);	
		
		ApplicationXMLReader axml = new ApplicationXMLReader();
		Application app = axml.parseApplication(which, false);
		mb.setApplication("Jacobi", app);
		
		RunParameters params = new RunParameters();
		
		//mb.getModel().getGlobalSettings().setClientFunctionImplementation("Reduce", "de.hd.pvs.piosim.simulator.program.Global.Dummy");
		
		//params.setDebugEverything(true);
		
		// test the sorter..
		if (shouldSortModel){
			ModelSortIDbySubcomponents sorter = new ModelSortIDbySubcomponents();
			sorter.sort(mb.getModel());
		}
		
		
		Simulator sim =  new Simulator();
		SimulationResults results = sim.simulate(mb.getModel(), params);
		
		if(results.getEventCount() == 0) {
			System.err.println("Nothing happened! Model:");
			System.err.println(sim.getModel());
		}
		
		return sim.getVirtualTime().getDouble();
	}
	

	public static void main(String[] args) throws Exception{
		JacobiTestIO t = new JacobiTestIO();

		t.shouldSortModel = true;
		//t.runJacobiLow("Examples/PDE-IO/large-tracer/trace-partdiff-par.xml");
		t.runJacobiLow("Examples/PDE-IO/small-tracer/trace-partdiff-par.xml");		
		System.exit(1);
		
		int cnt = 10;
		
		double [] runTimes = new double[cnt];
		double [] realRunTimes = new double[]{47.302645, 24.787603, 17.296363, 13.535730, 11.563651,
				10.094314, 9.160997, 8.388671,7.999543};
		
		for(int i=1; i < 10; i++){
			//runTimes[i-1] = t.runJacobi("" + i);
		}
		
		System.out.println("Runtimes: ");
		
		for(int i=1; i < 10; i++){
			System.out.println(i + " sim: " + runTimes[i-1] + " real: " + realRunTimes[i-1] +  " % " + 
					runTimes[i-1] / realRunTimes[i-1]);
		}
	}
}
