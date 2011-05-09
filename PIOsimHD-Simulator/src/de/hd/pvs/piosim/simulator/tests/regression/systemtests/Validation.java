package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import org.junit.Test;

import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;

public class Validation  extends ModelTest {

	@Override
	protected void postSetup() {
	}

	protected void setup(int nodeCount, int smtPerNode) throws Exception {
		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}


	protected void setupSMP(int smtPerNode) throws Exception {
		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( smtNodeT );
	}

	@Test public void recv1MB() throws Exception{
		setupSMP(2); 		//2 = Anzahl Prozessoren
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);

		parameters.setTraceFile("/tmp/recv1MB");   //Ausgabedatei

		parameters.setTraceEnabled(true); 	//Trace An

		pb.addReduce(world, 0, 1* MBYTE);  	// (-,-,Größe)

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendAndRecvEagerTestSMP() throws Exception{
		setupSMP(2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/sendRecvTest");

		parameters.setTraceEnabled(true);

		pb.addSendAndRecv(world, 0, 1, 10 * MBYTE, 1);

		runSimulationAllExpectedToFinish();
	}



	@Test public void MPICH2Reduce() throws Exception{
		setup(2, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/mpich2reduce");

		parameters.setTraceEnabled(true);

		pb.addReduce(world, 0, 10 * KBYTE);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterHierachicalTwoLevels() throws Exception{
		setup(5, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterHierachicalTwoLevels");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/scatter");

		parameters.setTraceEnabled(true);

		pb.addScatter(world, 0, 10 * KBYTE);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterFCFS() throws Exception{
		setup(3, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");
		parameters.setTraceFile("/tmp/scatterFCFS");
		parameters.setTraceEnabled(true);
		pb.addScatter(world, 0, 10 * MBYTE);
		runSimulationAllExpectedToFinish();
	}

	@Test public void TestGatherFCFS() throws Exception{
		setup(3, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Gather.FCFS");
		parameters.setTraceFile("/tmp/gatherFCFS");
		parameters.setTraceEnabled(true);
		pb.addGather(world, 0, 10 * MBYTE);
		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterFCFSOrdered123() throws Exception{
			setup(4,1);

			parameters.setTraceFile("/tmp/scatter");
			parameters.setTraceEnabled(true);

			mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
			mb.getGlobalSettings().setClientFunctionImplementation(
					new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

			pb.addCompute(1, 10000000);
			pb.addCompute(2, 20000000);
			pb.addCompute(3, 30000000);

			pb.addScatter(world, 0, 10 * MBYTE);
			runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatterFCFSLate() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addCompute(0, 10000000);

		pb.addScatter(world, 0, 10 * MBYTE);
		runSimulationAllExpectedToFinish();
}

	@Test public void TestScatterFCFSEqual() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KBYTE);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addScatter(world, 0, 10 * MBYTE);
		runSimulationAllExpectedToFinish();
}




	public void runJacobi_1C1S() throws Exception{
		final String which =
			"/home/julian/Dokumente/Geschäft/Dissertation/Simulation-Results/paper/trace/partdiff-par.proj";

		AggregationCache cache = new AggregationCache();
		cache.setName("PVS-CACHE");
		cache.setMaxNumberOfConcurrentIOOps(1);

		//setupDisjointIO(1, 1, 1, 12000, cache);

		parameters.setTraceFile("/tmp/jacobi1C1S");
		parameters.setTraceEnabled(true);

		final ApplicationXMLReader axml = new ApplicationXMLReader();
		final Application app = axml.parseApplication(which, true);

		mb.setApplication("Jacobi", app);

		final ClientProcess p = mb.getModel().getClientProcesses().get(0);
		p.setApplication("Jacobi");
		p.setRank(0);

		runSimulationAllExpectedToFinish();
	}
}
