
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

package de.hd.pvs.piosim.model.tests.regression.unittests;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.inputOutput.ListIO;
import de.hd.pvs.piosim.model.inputOutput.ListIO.SingleIOOperation;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;

/**
 * Test if the simple round-robin-striping is computed correctly.
 * @author Julian M. Kunkel
 */
public class distSimpleStripeTest {

	@Before public void setUp() {
		/* setup log4j */
	}

	@After public void tearDown(){
		System.out.println();
	}


	@Test public void test1(){
		test(2,
				new int [][]{{0, 10}},
				10,
				new int [][][]{{{0,10}}});
	}

	@Test public void test2(){
		test(2,
				new int [][]{{0, 20}},
				10,
				new int [][][]{ {{0,10}}, {{0,10}}  });
	}

	@Test public void test3(){
		test(2,
				new int [][]{{0, 103}},
				10,
				new int [][][]{ {{0,53}}, {{0,50}}  });
	}

	@Test public void test4(){
		test(2,
				new int [][]{{4, 103}},
				10,
				new int [][][]{ {{4,53}}, {{0,50}}  });
	}

	@Test public void test5(){
		test(2,
				new int [][]{{4, 3}},
				10,
				new int [][][]{ {{4,3}}});
	}

	@Test public void test6(){
		test(2,
				new int [][]{{10, 99}}, /* BA BA BA BA BA */
				10,
				new int [][][]{ {{10,49}}, {{0,50}}  });
	}

	@Test public void test7(){
		test(2,
				new int [][]{{20, 9}},
				10,
				new int [][][]{ {{10,9}}});
	}

	@Test public void test8(){
		test(2,
				new int [][]{{24, 7}}, /* AB */
				10,
				new int [][][]{ {{14,6}}, {{10,1}}  });
	}

	@Test public void test9(){
		test(2,
				new int [][]{{11, 95}}, /* BA BA BA BA BA */
				10,
				new int [][][]{ {{10,46}}, {{1,49}}  });
	}

	@Test public void test10(){
		test(3,
				new int [][]{{11, 17}}, /* BA BA BA BA BA */
				10,
				new int [][][]{ {}, {{1,9}}, {{0, 8}}  });
	}

	@Test public void test11(){
		test(3,
				new int [][]{{11, 17}, {40, 20}, {70,7}}, /* BA BA BA BA BA */
				10,
				new int [][][]{
				{},
				{{1, 26}}, //{{1,9},{10,10}, {20,7}},
				{{0, 8},{10,10}}
				});
	}

	static private int curID = 0;

	private class ServerEmu extends Server{

		public ServerEmu(int val) {
			this.getIdentifier().setName("S" + val);
			this.getIdentifier().setID(curID++);
		}
	}

	public distSimpleStripeTest(){

	}

	public void test(int serverCount, int [][] offsets, int chunkSize, int [][][] resultsPerServer ){
		StackTraceElement[] elements = new Exception().getStackTrace();
		System.err.println(elements[1] +"\n");

		SimpleStripe ss = new SimpleStripe();
		ss.setChunkSize(chunkSize);

		ArrayList<Server> servers = new ArrayList<Server>();
		for(int i=0; i < serverCount; i++)
			servers.add(new ServerEmu(i));

		ListIO listIO = new ListIO();
		for (int [] offset: offsets){
			listIO.addIOOperation(offset[0], offset[1]);
		}

		HashMap<Server, ListIO>  out = ss.distributeIOOperation(listIO, servers);
		/* check output */
		long outSum = 0;
		for(Server s: out.keySet()){
			String num = s.getName().substring(1);
			int srvNo = Integer.parseInt(num);

			ListIO lio = out.get(s);

			outSum+= lio.getTotalSize();

			int pos = 0;
			for(SingleIOOperation op: lio.getIOOperations()){
				System.out.println("srv,pos:" + srvNo + "," + pos + " " + op.getOffset() + " " + op.getAccessSize());

				pos++;
			}
		}


		System.out.println("Assert:");
		for(Server s: out.keySet()){
			String num = s.getName().substring(1);
			int srvNo = Integer.parseInt(num);

			ListIO lio = out.get(s);

			int pos = 0;
			for(SingleIOOperation op: lio.getIOOperations()){
				System.out.println("srv,pos:" + srvNo + "," + pos + " " + op.getOffset() + " " + op.getAccessSize());
				Assert.assertTrue( ((int) op.getOffset()) == resultsPerServer[srvNo][pos][0] );
				Assert.assertTrue( ((int) op.getAccessSize()) == resultsPerServer[srvNo][pos][1] );

				pos++;
			}
		}

		Assert.assertTrue(outSum == listIO.getTotalSize());
	}

	protected distSimpleStripeTest(int serverCount, int [][] offsets, int chunkSize, int [][][] resultsPerServer ){
		test(serverCount, offsets, chunkSize, resultsPerServer);
	}

	public static void main(String[] args) {
		distSimpleStripeTest test = new distSimpleStripeTest();
		test.test3();
	}
}
