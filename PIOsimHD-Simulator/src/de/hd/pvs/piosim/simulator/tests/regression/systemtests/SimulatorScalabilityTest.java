package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.util.Date;

import org.junit.Test;

import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.simulator.SimulationResultSerializer;
import de.hd.pvs.piosim.simulator.Simulator;

public class SimulatorScalabilityTest extends Validation{

	double setupTime;
	double simInitTime;

	/**
	 * @return model creation time
	 * @throws Exception
	 */
	public void bcastTestScalability(int processCount) throws Exception{

			long sTime, setupSystemTime;
			sTime = new Date().getTime();

			setupWrCluster(1, false, false, false, true, processCount,processCount,0,0, null, 10000);
			//model.getGlobalSettings().setTransferGranularity(100 * MiB);
			System.out.println("Model built! " + processCount);

			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");

			pb.addBroadcast(world, 0, 100 * MiB);
			// 	pb.addBroadcast(world,  (i - 2 >= 0 ? i -2 : 0), 100 * MiB);

			setupSystemTime = (new Date().getTime() - sTime);


			sTime = new Date().getTime();
			sim = new Simulator();
			sim.initModel(model, parameters);
			simRes = sim.simulate();

			simInitTime = (new Date().getTime() - sTime) / 1024.0;

			final SimulationResultSerializer serializer = new SimulationResultSerializer();
			System.out.println(serializer.serializeResults(simRes));

			if(simRes.isErrorDuringProcessing()){
				throw new IllegalArgumentException("Errors occured during processing");
			}


			setupTime = setupSystemTime / 1024.0;
	}


	@Test public void bcastTestScalability() throws Exception{
		final int max = 12;
		double [] times = new double[max];
		double [] simTimes = new double[max];
		double [] modelBuildTimes = new double[max];

		int count = 1;
		for(int i=1; i < max; i++){
			bcastTestScalability(count);

			times[i] = sim.getVirtualTime().getDouble();
			simTimes[i] = simRes.getWallClockTime();
			modelBuildTimes[i] = setupTime;

			count = count*2;
		}

		printTiming("Broadcast VirtualTime", times);
		printTiming("Broadcast ModelBuildTime", modelBuildTimes);
		printTiming("Broadcast SimTime", simTimes);
	}

	void printTiming(){
		System.out.println("VirtualTime: " + sim.getVirtualTime().getDouble() + " WallClockTime:" + simRes.getWallClockTime() + " ModelBuildTime:" + setupTime  + " ModelSimInitTime:" + simInitTime);
	}

	public static void main(String[] args) throws Exception{
		int processCount = Integer.parseInt(args[0]);

		SimulatorScalabilityTest t = new SimulatorScalabilityTest();

		System.out.print(processCount + " ");
		t.bcastTestScalability(processCount);
		t.printTiming();
	}

}
