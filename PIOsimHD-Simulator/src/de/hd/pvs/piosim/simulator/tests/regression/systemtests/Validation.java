package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.IOSubsystem.IOSubsystem;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.NetworkEdge.NetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkEdge.SimpleNetworkEdge;
import de.hd.pvs.piosim.model.components.NetworkNode.NetworkNode;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationReorderCache;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.ServerCacheLayer;
import de.hd.pvs.piosim.model.dynamicMapper.CommandType;
import de.hd.pvs.piosim.model.inputOutput.FileDescriptor;
import de.hd.pvs.piosim.model.inputOutput.FileMetadata;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketFirstRoute;
import de.hd.pvs.piosim.model.networkTopology.RoutingAlgorithm.PaketRoutingAlgorithm;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.ApplicationBuilder;
import de.hd.pvs.piosim.model.program.ApplicationXMLReader;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.ProgramBuilder;
import de.hd.pvs.piosim.model.program.ProgramInMemory;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.simulator.RunParameters;
import de.hd.pvs.piosim.simulator.SimulationResultSerializer;
import de.hd.pvs.piosim.simulator.Simulator;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.IOC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NICC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkEdgesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NetworkNodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.hardwareConfigurations.NodesC;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.ClusterT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.HardwareConfiguration;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.IOServerCreator;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.NodeT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTNodeT;
import de.hd.pvs.piosim.simulator.tests.regression.systemtests.topologies.SMTSocketNodeT;

public class Validation  extends ModelTest {

	String [] configs = new String[]{"1-1","1-2","1-3","1-4","1-5","1-6","1-7","1-8","1-9","1-10","1-11","1-12",
			"2-2","2-3","2-4","2-5","2-7","2-9", "2-11",
			"3-3","3-6","4-4","4-8","5-5","5-10","6-6","6-12","7-7","7-14","8-8","8-16","9-9","9-18","10-10","10-20"};

	int [] sizes = {10240, 1048576, 10485760, 104857600};
	int [] sizes100KiB = {10240, 1048576};
	int [] sizes10KiB10MiB = {10240, 10485760};

	enum MPIImplementations{
		MPICH2,
		MPICH2_FLUSH,
		SYNC_VIRTUALLY,
		NO_IO,
		NO_OP
	};

	MPIImplementations useMPIImplementations = MPIImplementations.MPICH2;

	IOSubsystem usedDisk = IOC.PVSDisk();



	long procSpeed =  2660l*1000000;

	@Override
	protected void postSetup() {
	}

	protected void setup(int nodeCount, int smtPerNode) throws Exception {
		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.LocalNodeQPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}

	protected void setupAnalytical(int nodeCount, int smtPerNode) throws Exception {
		SMTNodeT smtNodeT = new SMTNodeT(smtPerNode,
				NICC.NICAnalytical(),
				NodesC.PVSSMPNode(smtPerNode),
				NetworkNodesC.LocalNodeQPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}

	protected void setupAnalyticalNonSMP(int nodeCount) throws Exception {
		NodeT smtNodeT = new NodeT(	NICC.NICAnalytical(),
				NodesC.PVSSMPNode(1));
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}



	protected void setupSMP(int smtPerSocket) throws Exception {
		setupSMP(smtPerSocket, 1);
	}


	protected void setupSMP(int smtPerSocket, int sockets) throws Exception {
		SMTSocketNodeT smtNodeT = new SMTSocketNodeT(smtPerSocket,
				sockets,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerSocket*sockets),
				NetworkNodesC.SocketLocalNode(),
				NetworkEdgesC.SocketLocalEdge(),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( smtNodeT );
	}

	protected void setupSocketCluster(int nodeCount, int smtPerSocket, int sockets) throws Exception {
		SMTSocketNodeT smtNodeT = new SMTSocketNodeT(smtPerSocket,
				sockets,
				NICC.PVSNIC(),
				NodesC.PVSSMPNode(smtPerSocket*sockets),
				NetworkNodesC.SocketLocalNode(),
				NetworkEdgesC.SocketLocalEdge(),
				NetworkNodesC.QPI(),
				NetworkEdgesC.QPI()
				);
		super.setup( new ClusterT(nodeCount,
				NetworkEdgesC.GIGE(),
				NetworkNodesC.GIGSwitch(),
				smtNodeT) );
	}


	protected void setupWrCluster(int processNodes, int processes, int overlappingServerCount, int additionalServerNodes, ServerCacheLayer cacheLayer, long RAM) throws Exception {
		setupWrCluster(2, false, false, false, true, processNodes, processes, overlappingServerCount, additionalServerNodes, cacheLayer, RAM);
	}

	/**
	 * Setup a configuration equal to the WR cluster configuration.
	 * @param processNodes
	 * @param processes
	 * @throws Exception
	 */
	protected void setupWrCluster(int socketCount, boolean analyticalNIC, boolean fasterNIC, boolean latencyBoundNetwork, boolean nodeSocketPetPlacement, int processNodes, int processes, int overlappingServerCount, int additionalServerNodes, ServerCacheLayer cacheLayer, long RAM) throws Exception {
		final int procsPerSocket ;

		procsPerSocket = processes / (processNodes * socketCount) + ( processes % (processNodes * socketCount) == 0 ? 0 : 1 );

		final int procsPerNode = procsPerSocket * socketCount;

		SMTSocketNodeT smtNodeT;

		final NIC nic;
		final NetworkEdge socketLocalEdge;
		final NetworkEdge interSocketEdge;
		final NetworkEdge interNodeEdge;
		final NetworkNode nodeLocal;
		final NetworkNode socketNode;


		if (analyticalNIC){ // real vs. analytical NIC?
			nic = NICC.NICAnalytical();
		}else{
			nic = NICC.PVSNIC();
		}

		if(false){
			socketLocalEdge = NetworkEdgesC.infiniteFast();
			interSocketEdge = NetworkEdgesC.infiniteFast();
			interNodeEdge = NetworkEdgesC.infiniteFast();
			nodeLocal = NetworkNodesC.infiniteFast();
			socketNode = NetworkNodesC.infiniteFast();

		}else{
			if(latencyBoundNetwork){ // real vs. latency bound.
				socketLocalEdge = NetworkEdgesC.SocketLocalNoLatencyEdge();
				interSocketEdge = NetworkEdgesC.QPINoLatency();
				interNodeEdge = NetworkEdgesC.GIGEPVSNoLatency();
			}else{
				socketLocalEdge = NetworkEdgesC.SocketLocalEdge();
				interSocketEdge = NetworkEdgesC.QPI();

				if(fasterNIC){ // real cluster value vs. GiGE throughput
					interNodeEdge = NetworkEdgesC.GIGE();
				}else{
					interNodeEdge = NetworkEdgesC.GIGEPVS();
				}
			}
			nodeLocal = NetworkNodesC.QPI();
			socketNode = NetworkNodesC.SocketLocalNode();
		}

		smtNodeT = new SMTSocketNodeT(procsPerSocket,
				socketCount,
				nic,
				NodesC.PVSSMPNode(procsPerSocket * socketCount, RAM),
				socketNode,
				socketLocalEdge,
				nodeLocal,
				interSocketEdge );

		final HardwareConfiguration config = new ClusterT(processNodes, interNodeEdge ,NetworkNodesC.GIGSwitch(), smtNodeT);

		PaketRoutingAlgorithm routingAlgorithm = new PaketFirstRoute();
		mb = new ModelBuilder();
		topology = mb.createTopology("LAN");
		topology.setRoutingAlgorithm(routingAlgorithm);

		// create servers & overhaul existing configuration if necessary
		if(additionalServerNodes > 0 || overlappingServerCount > 0){
			final NetworkEdge nodeEdge = NetworkEdgesC.GIGEPVS();
			final NetworkNode Switch = NetworkNodesC.GIGSwitch();
			mb.addTemplateIf(nodeEdge);
			mb.addTemplateIf(Switch);

			NetworkNode testSW = mb.cloneFromTemplate(Switch);

			testSW.setName("Switch" + testSW.getName());

			mb.addNetworkNode(testSW);

			// the cluster nodes
			ArrayList<NetworkNode> nodes = new ArrayList<NetworkNode>();

			final IOServerCreator ios = new IOServerCreator(IOC.PVSServer(), usedDisk, cacheLayer);

			// create client only nodes:
			for(int i = 0; i < processNodes - overlappingServerCount ; i++){
				nodes.add(smtNodeT.createModel("" + nodes.size(), mb, topology));
			}


			// overlapping of clients and servers
			SMTSocketNodeT myIOServerGen = new SMTSocketNodeT(procsPerSocket,
					socketCount,
					nic,
					NodesC.PVSSMPNode(procsPerNode, RAM),
					NetworkNodesC.SocketLocalNode(),
					socketLocalEdge,
					NetworkNodesC.QPI(),
					interSocketEdge,
					ios );
			if(overlappingServerCount > 0){
				for (int i=0; i < overlappingServerCount; i++){
					nodes.add(myIOServerGen.createModel("" + nodes.size(), mb, topology));
				}
			}

			// servers only
			myIOServerGen = new SMTSocketNodeT(0,
					1,
					nic,
					NodesC.PVSSMPNode(1, RAM),
					NetworkNodesC.SocketLocalNode(),
					socketLocalEdge,
					NetworkNodesC.QPI(),
					interSocketEdge,
					ios );
			for (int i=0; i < additionalServerNodes; i++){
				nodes.add(myIOServerGen.createModel("" + nodes.size(), mb, topology));
			}


			for (int i = 0; i < nodes.size(); i++) {
				NetworkNode n = nodes.get(i);

				NetworkEdge edge1 = mb.cloneFromTemplate(nodeEdge);
				NetworkEdge edge2 = mb.cloneFromTemplate(nodeEdge);
				edge1.setName(i + "_TX " + edge1.getName());
				edge2.setName(i + "_RX "+ edge2.getName());
				mb.connect(topology, n, edge1 , testSW);
				mb.connect(topology, testSW, edge2 , n);
			}
		}else{
			config.createModel("", mb, topology);
		}

		parameters.setLoggerDefinitionFile("loggerDefinitionFiles/example");
		parameters.setTraceEnabled(false);
		parameters.setTraceInternals(false);
		parameters.setTraceClientSteps(false);
		parameters.setTraceServers(true);


		aB = new ApplicationBuilder("Validate", "Validation runs", processes, 1);
		app = aB.getApplication();

		// build a dummy app for all nodes
		ApplicationBuilder dummy = new ApplicationBuilder("Test", "Test", procsPerSocket * socketCount * processNodes, 1);
		int cur = 0;
		for(ClientProcess c : mb.getModel().getClientProcesses()){
			c.setRank(cur++);
			c.setApplication("Test");
		}
		mb.setApplication("Test", dummy.getApplication());

		pb = new ProgramBuilder(aB);

		// number => clients
		final ClientProcess [] clients = mb.getModel().getClientProcesses().toArray(new ClientProcess[0]);

		int curNode = 0;
		int curSocket = 0;
		int curProc = 0;

		// placement of the processes
		if(nodeSocketPetPlacement){ //  => nodes, sockets, PEs

			for(int rank = 0; rank < processes; rank++){

				int physicalCPU = curNode * procsPerNode + curSocket * procsPerSocket + curProc;
				//System.out.println("rank: " + rank + " node: " + curNode + " socket: " + curSocket + " proc: " + curProc + " physicalCPU: " + physicalCPU);

				curNode++;
				if (curNode >= processNodes){
					curNode = 0;
					curSocket++;
					if(curSocket == socketCount){
						curSocket = 0;
						curProc++;
					}
				}

				ClientProcess c = clients[physicalCPU]; // 6 * 2 * nodes
				c.setApplication("Validate");
				c.setRank(rank);
				c.setName("" + rank);
			}
		}else{ // => PEs on one node, one socket, i.e. 2 nodes 5 procs => procs 0,1 and 2,3,4 are together
			int numberOfProcsPerNode = processes / processNodes;
			int restProcs = processes % processNodes;
			int rank = 0;
			for(int node = 0; node < processNodes; node++){
				for(int pCPU = 0; pCPU < numberOfProcsPerNode ; pCPU++){
					ClientProcess c = clients[procsPerNode * node + pCPU]; // 6 * 2 * nodes
					c.setApplication("Validate");
					c.setRank(rank);
					c.setName("" + rank);

					rank++;
				}
			}
			// last node gets the remainder
			for(int rest = 0; rest < restProcs; rest++){
				ClientProcess c = clients[procsPerNode * (processNodes-1) + numberOfProcsPerNode + rest];
				c.setApplication("Validate");
				c.setRank(rank);
				c.setName("" + rank);

				rank++;
			}
		}

		mb.setApplication("Validate", app);


		world = aB.getWorldCommunicator();
		model = mb.getModel();

		final String nop = "de.hd.pvs.piosim.simulator.program.Global.NoOperation";

		// set useful defaults:
		switch (useMPIImplementations){

		case MPICH2:
			// default values
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.ReduceBroadcast");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allgather"), "de.hd.pvs.piosim.simulator.program.Allgather.AllgatherMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BarrierMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Reduce"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");

			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("ReduceScatter"), "de.hd.pvs.piosim.simulator.program.ReduceScatter.ReduceScatterPowerOfTwo");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterGatherall");


			// close without doing anything
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileclose"), "de.hd.pvs.piosim.simulator.program.Global.NoOperation");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileopen"),  "de.hd.pvs.piosim.simulator.program.FileOpen.BroadcastOpen");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filewriteall"),  "de.hd.pvs.piosim.simulator.program.Filewriteall.TwoPhase");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filereadall"),  "de.hd.pvs.piosim.simulator.program.Filereadall.TwoPhase");
			break;

		case MPICH2_FLUSH:
			// default values, but flush data upon close
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.ReduceBroadcast");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allgather"), "de.hd.pvs.piosim.simulator.program.Allgather.AllgatherMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BarrierMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Reduce"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");

			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("ReduceScatter"), "de.hd.pvs.piosim.simulator.program.ReduceScatter.ReduceScatterPowerOfTwo");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterGatherall");


			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileclose"), "de.hd.pvs.piosim.simulator.program.FileClose.FlushClose");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileopen"),  "de.hd.pvs.piosim.simulator.program.FileOpen.BroadcastOpen");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filewriteall"),  "de.hd.pvs.piosim.simulator.program.Filewriteall.TwoPhase");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filereadall"),  "de.hd.pvs.piosim.simulator.program.Filereadall.TwoPhase");
			break;

		case SYNC_VIRTUALLY:
			// for the critical path
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Global.VirtualSync");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allgather"), "de.hd.pvs.piosim.simulator.program.Global.VirtualSync");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Global.VirtualSync");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Reduce"), "de.hd.pvs.piosim.simulator.program.Global.VirtualSync");

			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Global.VirtualSync");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Global.VirtualSync");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("ReduceScatter"), "de.hd.pvs.piosim.simulator.program.Global.VirtualSync");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Global.VirtualSync");


			// close without doing anything
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileopen"),  "de.hd.pvs.piosim.simulator.program.Global.VirtualSyncIORequest");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filewriteall"),  "de.hd.pvs.piosim.simulator.program.Global.VirtualSyncIORequest");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filereadall"),  "de.hd.pvs.piosim.simulator.program.Global.VirtualSyncIORequest");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileclose"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filewrite"),  nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileread"),  nop);

			// de.hd.pvs.piosim.model.program.commands.Send,de.hd.pvs.piosim.model.program.commands.Recv,de.hd.pvs.piosim.model.program.commands.Sendrecv
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Send"),  "de.hd.pvs.piosim.simulator.program.SendReceive.Virtual.VirtualSend");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Recv"),  "de.hd.pvs.piosim.simulator.program.SendReceive.Virtual.VirtualRcv");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Sendrecv"),  "de.hd.pvs.piosim.simulator.program.SendReceive.Rendezvous.RendezvousSendrecv");

			break;

		case NO_IO:
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.ReduceBroadcast");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allgather"), "de.hd.pvs.piosim.simulator.program.Allgather.AllgatherMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BarrierMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Reduce"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");

			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("ReduceScatter"), "de.hd.pvs.piosim.simulator.program.ReduceScatter.ReduceScatterPowerOfTwo");
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterGatherall");


			// close without doing anything
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileclose"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filewrite"),  nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileread"),  nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileopen"),  nop);

			break;

		case NO_OP:
			// all MPI operations do nothing
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allgather"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Reduce"), nop);

			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("ReduceScatter"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), nop);


			// close without doing anything
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileclose"), nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileopen"),  nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filewriteall"),  nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filereadall"),  nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Filewrite"),  nop);


			// de.hd.pvs.piosim.model.program.commands.Send,de.hd.pvs.piosim.model.program.commands.Recv,de.hd.pvs.piosim.model.program.commands.Sendrecv
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Send"),nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Recv"),  nop);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Sendrecv"), nop);
		}

		// do not flush on close => default behavior...
		//mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Fileclose"), "de.hd.pvs.piosim.simulator.program.FileClose.SimpleClose");


		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		model.getGlobalSettings().setTransferGranularity(100 * KiB);
	}


	/**
	 * Setup a configuration equal to the WR cluster configuration.
	 * @param nodeCount
	 * @param processes
	 * @throws Exception
	 */
	protected void setupWrCluster(int nodeCount, int processes) throws Exception {
		setupWrCluster(nodeCount, processes, 0, 0, null, 12000);
	}

	@Test public void sendAndRecvTest() throws Exception{
		setupWrCluster(2, 6);

		parameters.setTraceFile("/tmp/sendRecv");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		//mb.getGlobalSettings().setMaxEagerSendSize(10 * MiB);


		pb.addSendAndRecv(world, 0, 1, 10 * MiB, 1);
		pb.setLastCommandAsynchronous(1, 0);
		pb.addSendAndRecv(world, 2, 1, 10 * MiB, 1);
		pb.setLastCommandAsynchronous(1, 0);
		pb.addSendAndRecv(world, 4, 1, 10 * MiB, 1);
		pb.setLastCommandAsynchronous(1, 0);
		pb.addWaitAll(1);

		runSimulationAllExpectedToFinish();
	}


	@Test public void sendAndRecvTestSimple() throws Exception{
		setupWrCluster(1, 2);

		parameters.setTraceFile("/tmp/sendRecv");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);


		pb.addSendAndRecv(world, 0, 1, 200 *KiB, 1);
		pb.addWaitAll(1);

		runSimulationAllExpectedToFinish();
	}



 // Evaluation of the simulation speed
	@Test public void sendAndRecvPerformanceTest() throws Exception{
		setupWrCluster(1, 2);

		pb.addSendAndRecv(world, 0, 1, 100 *GiB, 1);

		runSimulationAllExpectedToFinish();
	}

	@Test public void sendRecvTest() throws Exception{
		// test cases run on the WR cluster
		long size = 10000l*1024*1024;
		final int nodes = 1;
		final int processes = 2;

		setupWrCluster(nodes, processes);
		pb.addSendRecv(world, 0, 1, 1, size, 4711, 4711);
		pb.addSendRecv(world, 1, 0, 0, size, 4711, 4711);
		runSimulationWithoutOutput();
		System.out.println(simRes.getVirtualTime().getDouble() + " ");

		System.out.println("Same socket");

		setupWrCluster(nodes, 12);
		pb.addSendRecv(world, 0, 2, 2, size, 4711, 4711);
		pb.addSendRecv(world, 2, 0, 0, size, 4711, 4711);
		runSimulationWithoutOutput();
		System.out.println(simRes.getVirtualTime().getDouble() + " ");
	}

	@Test public void sendRecvIntersocketValidate() throws Exception{
		System.out.println("Single socket");

		setupSMP(2, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		pb.addSendRecv(world, 0, 1, 1, 0, 100, 101);
		pb.addSendRecv(world, 1, 0, 0, 0, 101, 100);
		runSimulationAllExpectedToFinish();


		System.out.println("Across two sockets");
		setupSMP(1, 2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		pb.addSendRecv(world, 0, 1, 1, 0, 100, 101);
		pb.addSendRecv(world, 1, 0, 0, 0, 101, 100);
		runSimulationAllExpectedToFinish();


		System.out.println("Inter-node");
		setupSocketCluster(2, 1, 1);
		pb.addSendRecv(world, 0, 1, 1, 0, 100, 101);
		pb.addSendRecv(world, 1, 0, 0, 0, 101, 100);
		runSimulationAllExpectedToFinish();
	}

	@Test public void sendDataRoot() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/validationRuns-modelTime-sendRoot.txt"));
		modelTime.write("# Experiment configuration & times \n");
		// test cases run on the WR cluster

		for(int size: sizes){
			modelTime.write("SendRoot" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				for(int rank=1; rank < processes ; rank++){
					pb.addSendAndRecv(world, rank, 0, size, 4711);
				}

				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}


	@Test public void sendRecvWithRoot() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/validationRuns-modelTime-sendRecvRoot.txt"));
		modelTime.write("# Experiment configuration & times \n");
		// test cases run on the WR cluster

		for(int size: sizes){
			modelTime.write("SendRecvRoot" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				for(int rank=1; rank < processes ; rank++){
					pb.addSendRecv(world, rank, 0, 0, size, 4711, 4711);
					pb.addSendRecv(world, 0, rank, rank, size, 4711, 4711);
				}

				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}

	@Test public void sendRecvRightNeighbour() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/validationRuns-modelTime-sendRecvRN.txt"));
		modelTime.write("# Experiment configuration & times \n");
		// test cases run on the WR cluster

		for(int size: sizes){
			modelTime.write("SendRecvRightNeighbor" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				for(int rank=0; rank < processes ; rank++){
					int dest = (rank == processes - 1) ? 0 : rank + 1;
					int src = (rank == 0) ? processes - 1 : rank - 1;

					pb.addSendRecv(world, rank, src, dest, size, 4711, 4711);
				}

				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}






	@Test public void allreduceRootComputes() throws Exception{
		setup(3, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allreduce"), "de.hd.pvs.piosim.simulator.program.Allreduce.RootComputes");
		parameters.setTraceFile("/tmp/allreduce");

		parameters.setTraceEnabled(true);

		pb.addAllreduce(world, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void barrierTree() throws Exception{
		setup(8, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Barrier"), "de.hd.pvs.piosim.simulator.program.Barrier.BinaryTree");
		parameters.setTraceFile("/tmp/barrier");

		parameters.setTraceEnabled(true);

		pb.addBarrier(world);

		runSimulationAllExpectedToFinish();
	}


	@Test public void bcastTest() throws Exception{
		setupWrCluster(8, 8);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setTransferGranularity(512);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed"); // BinaryTreeSimpleBlockwise BinaryTreeNotMultiplexed
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);
		parameters.setTraceClientNestingOperations(true);
		parameters.setTraceClientSteps(true);


		pb.addBroadcast(world,0, 10*MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void broadcastMultiplex() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeMultiplex");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastSimple() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimple");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void runAsingleCollective() throws Exception{
		RunParameters p = new RunParameters();
		p.setTraceEnabled(true);
		p.setTraceFile("/tmp/barrier");
		p.setTraceInternals(true);

		setupWrCluster(4,4);
		model.getGlobalSettings().setTransferGranularity(512);

		//runCollectiveTest(4,4, "Barrier", "", null, null, true, false, p, 99);
		runCollectiveTest(4,4, "Bcast", "10240", null, null, true, true, p, 99);
	}



	/**
	 * Benchmark the I/O subsystem by using a extreme-fast interconnect between client and server
	 * @throws Exception
	 */
	@Test public void validateDisk() throws Exception{

		ServerCacheLayer cacheLayers [] = new ServerCacheLayer[]{IOC.SimpleNoCache(), IOC.SimpleWriteBehindCache(), IOC.AggregationReorderCache()}; //AggregationCache

		//ServerCacheLayer cacheLayers [] = new ServerCacheLayer[]{IOC.SimpleNoCache(), IOC.AggregationReorderCache()};
		// final long RAM = 200* MiB;
		//final long RAM = 500* MiB;

		final long RAM = 12000* MiB;

		BufferedWriter output = new BufferedWriter(new FileWriter("/tmp/io.txt"));

		for(ServerCacheLayer cacheLayer: cacheLayers){
			for(int r= 0; r <= 1 ; r++){
				for(int w= 0; w <= 1 ; w++){

					final boolean random = r == 0 ? true : false ;
					final boolean write = w == 0 ? true : false ;

					long currentBlockSize = 16*KiB;

					output.append(cacheLayer.getNiceName() + " " + RAM + "RAM random:" + random + " write:" + write + " ");
					for(int a = 0 ; a < 10; a++){
						PaketRoutingAlgorithm routingAlgorithm = new PaketFirstRoute();
						mb = new ModelBuilder();

						topology = mb.createTopology("LAN");
						topology.setRoutingAlgorithm(routingAlgorithm);

						NIC nicT = NICC.PVSNIC();
						mb.addTemplate(nicT);

						Node n = new Node();
						n.setName("IOTest");
						n.setMemorySize(RAM);
						n.setCPUs(12);
						n.setInstructionsPerSecond(100000000l);

						Server s = IOC.PVSServer();
						s.setParentComponent(n);
						RefinedDiskModel disk = (RefinedDiskModel) IOC.PVSDisk();

						disk.setMaxConcurrentRequests(1); // NCQ is not effective in this toy example, since just one stream accesses the disk.

						s.setIOsubsystem(disk);
						s.setNetworkInterface(mb.cloneFromTemplate(nicT));

						cacheLayer.setParentComponent(s);
						s.setCacheImplementation(cacheLayer);

						ClientProcess c = new ClientProcess();
						c.setName("Client");
						NIC cnic = mb.cloneFromTemplate(nicT);
						cnic.setName("ClientNIC");
						c.setNetworkInterface(cnic);

						mb.addServer(n, s);
						mb.addClient(n, c);
						mb.addNode(n);

						SimpleNetworkEdge conn = new SimpleNetworkEdge();
						conn.setName("UF");
						conn.setLatency(Epoch.ZERO);
						conn.setBandwidth(40000 * MiB);
						mb.addTemplate(conn);


						SimpleNetworkEdge connt = mb.cloneFromTemplate(conn);
						SimpleNetworkEdge connr = mb.cloneFromTemplate(conn);

						connt.setName("tx");
						connr.setName("rx");

						mb.connect(topology, c.getNetworkInterface(), connt, s.getNetworkInterface());
						mb.connect(topology, s.getNetworkInterface(), connr, c.getNetworkInterface());

						parameters.setLoggerDefinitionFile("loggerDefinitionFiles/example");
						parameters.setTraceEnabled(false);
						parameters.setTraceInternals(false);
						parameters.setTraceClientSteps(false);
						parameters.setTraceServers(true);


						aB = new ApplicationBuilder("Validate", "Validation runs", 1, 1);
						world = aB.getWorldCommunicator();

						app = aB.getApplication();

						pb = new ProgramBuilder(aB);
						c.setRank(0);
						c.setApplication("Validate");

						SimpleStripe stripe = new SimpleStripe();
						stripe.setChunkSize(GiB * 100);
						mb.setApplication("Validate", app);

						FileMetadata file = aB.createFile("testFile", 10 * GiB, stripe);

						FileDescriptor fd = pb.addFileOpen(file, world, false);

						long size = 1280 * MiB ;
						long count = size / currentBlockSize;

						Random rand = new Random(1);

						for(long i=0; i < count; i++){
							long offset = -1;
							if(random){
								// random case:
								while(offset < 0){
									offset = (rand.nextLong() % size)  / currentBlockSize;
								}
							}else{
								offset = i;
							}
							if(write){
								pb.addWriteSequential(0, fd, offset  * currentBlockSize , currentBlockSize);
							}else{
								pb.addReadSequential(0, fd, offset  * currentBlockSize , currentBlockSize);
							}
						}

						pb.addFileClose(fd);

						sim = new Simulator();
						model = mb.getModel();
						model.getGlobalSettings().setIOGranularity(10 * MiB);
						model.getGlobalSettings().setTransferGranularity(100 * KiB);

						sim.initModel(model, parameters);

						simRes = sim.simulate();

						if (a == 9){
							final SimulationResultSerializer serializer = new SimulationResultSerializer();
							System.out.println(serializer.serializeResults(simRes));
						}

						output.append( (size / sim.getVirtualTime().getDouble() / 1024.0 / 1024.0) + " ");

						currentBlockSize = currentBlockSize * 2;
					}
					output.append("\n");
					output.flush();
				}
			}
		}

		output.close();
	}


	@Test public void MPIIORunTest() throws Exception{
		setupWrCluster(1, 1, 1, 1, IOC.AggregationCache(), 1000);

		parameters.setTraceFile("/tmp/ios");
		parameters.setTraceEnabled(false);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(64 * KiB);
		FileMetadata file =  aB.createFile("test", GiB, dist );

		FileDescriptor fd = pb.addFileOpen(file, world, false);
		pb.addWriteSequential(0, fd, 0, 100 * MiB);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();


		setupWrCluster(1, 1, 1, 0, IOC.AggregationCache(), 1000);

		parameters.setTraceFile("/tmp/ios");
		parameters.setTraceEnabled(true);
		file =  aB.createFile("test", GiB, dist );
		fd = pb.addFileOpen(file, world, false);
		pb.addWriteSequential(0, fd, 0, 100 * MiB);
		pb.addFileClose(fd);

		runSimulationAllExpectedToFinish();
	}


	FilenameFilter projFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".proj");
		}
	};

	String getProjectFile(String folder){
		File f = new File(folder);
		String files[] = f.list(projFilter);
		if (files == null || files.length != 1){
			System.err.println("Invalid configuration: " + folder);
			return "";
		}
		return folder + "/" + files[0];
	}

	final String extractedCommunicationPatternPath = "/home/julian/Dokumente/Dissertation/Latex/results/mpi-bench-current/extracted-communication-patterns/";

	void addBarrier(Application app, String config) throws Exception{
		if(false){
		  pb.addBarrier(world);
		}else{
			final ApplicationXMLReader axml = new ApplicationXMLReader();

			String barrierProj= getProjectFile(extractedCommunicationPatternPath + config + "/Barrier");
			final Application barrierApp;
			barrierApp = axml.parseApplication(barrierProj, true);

			for(int r = 0 ; r < barrierApp.getProcessCount(); r++){
				ProgramInMemory bp = (ProgramInMemory) barrierApp.getClientProgram(r, 0);
				ProgramInMemory p = (ProgramInMemory) app.getClientProgram(r, 0);

				ArrayList<Command> prevCommands = bp.getCommands();
				int commandCount = prevCommands.size();

				for(int i=0; i < commandCount; i++){
					p.addCommand(prevCommands.get(i));
				}
			}
		}
	}

	public void runCollectiveTest(int nodes, int processes, String experiment, String strSize, BufferedWriter outputFile, BufferedWriter modelTime, boolean doCompute, boolean addBarrier, RunParameters parameters, int repeatReading) throws Exception{

		if(outputFile == null){
			outputFile = new BufferedWriter(new FileWriter("/tmp/collectives-runTime.txt"));
			outputFile.write("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n");
		}
		if(modelTime == null){
			modelTime = new BufferedWriter(new FileWriter("/tmp/collectives-modelTime.txt"));
		}

		if(addBarrier){
			System.out.println("Added barrier");
		}

		final ApplicationXMLReader axml = new ApplicationXMLReader();

		long sTime, setupSystemTime, setupProgramTime;
		// dual socket configuration.
		sTime = new Date().getTime();

		final String config = nodes + "-" + processes;

		if(parameters != null)
			this.parameters = parameters;

		setupSystemTime = (new Date().getTime() - sTime);

		sTime = new Date().getTime();

		// load traces
		final String folder = extractedCommunicationPatternPath + config + "/" + experiment + "/" + strSize;
		String proj= getProjectFile(folder);
		if(proj == ""){
			outputFile.write("Invalid configuration: " + folder);
			// we know the time is 0.0 for the configuration 1-1
			modelTime.write("0.0 ");
			return;
		}

		System.out.println(proj);
		outputFile.write(proj);
		outputFile.flush();

		// don't do any computation ...
		// TODO run both tests at the same time
		if (! doCompute){
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Compute"), "de.hd.pvs.piosim.simulator.program.Global.NoOperation");
		}


		// load program:
		final Application app = axml.parseApplication(proj, true);
		mb.setApplication("Validate", app);

		if(repeatReading > 0){
			for(int r = 0 ; r < app.getProcessCount(); r++){
				ProgramInMemory p = (ProgramInMemory) app.getClientProgram(r, 0);
				ArrayList<Command> prevCommands = p.getCommands();
				int commandCount = prevCommands.size();

				for(int c=0; c < repeatReading; c++){
					for(int i=0; i < commandCount; i++){
						p.addCommand(prevCommands.get(i));
					}
				}
			}
		}

		if(addBarrier){
			addBarrier(app, config);
		}


		setupProgramTime = (new Date().getTime() - sTime);
		runSimulationWithoutOutput();

		outputFile.write("\t" + config + "\t" + simRes.getEventCount() + "\t" + simRes.getWallClockTime() + "\t" + setupSystemTime  / 1000.0 + "\t" + setupProgramTime  / 1000.0 + "\n");
		outputFile.flush();

		modelTime.write(simRes.getVirtualTime().getDouble() + " ");
		System.out.println("Modeltime: " + simRes.getVirtualTime().getDouble());
		modelTime.flush();
	}


	@Test public void validationRunCollectiveWithSendRecv() throws Exception{
		BufferedWriter outputFile = new BufferedWriter(new FileWriter("/tmp/collectives-runTime.txt"));
		outputFile.write("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n");

		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/collectives-modelTime.txt"));

		modelTime.write("# Experiment ");
		for(String config: configs){
			modelTime.write(config + " ");
		}
		modelTime.write("\n");

		validationRunCollectiveWithSendRecv(modelTime);

		sendRecvRingRightLeftNeighbor(modelTime);
		sendRecvPaired(modelTime);
		sendRootWhichReceives(modelTime);
		sendRecvRoot(modelTime);

		System.out.println("All Completed!");
		modelTime.write("\nCompleted!\n");
		modelTime.close();
	}

	/**
	 * Verification with the mpi-network-behavior results.
	 * @throws Exception
	 */
	@Test
	public void PingPongKernelValidation() throws Exception{
		BufferedWriter modelTime = new BufferedWriter(new FileWriter("/tmp/pingPong.txt"));

		modelTime.write("# Sizes:");
		for(int size = 0 ; size <= 128 * MiB ; size *=2){
			modelTime.write(" " + size);

			if(size == 0){
				size = 64;
			}
		}
		modelTime.write("\n");


		for(int c = 0; c <= 1; c++) {
			for(long transferGranularity: new long [] {512, 5120, 100*KiB, 10*MiB} ){
				modelTime.write("TransferGranularity " + transferGranularity + " inter-node:" + c + " PingPong Kernel " );

				for(int size = 0 ; size <= 128 * MiB ; size *=2){

					setupWrCluster( c == 1 ? 2 : 1, 2);

					getGlobalSettings().setTransferGranularity(transferGranularity);

					// PingPong Kernel
					if(true){
						pb.addSendAndRecv(world, 1, 0, size, 4711);
						pb.addSendAndRecv(world, 0, 1, size, 4711);
					}else{ //Sendrecv kernel
						pb.addSendRecv(world, 0, 1, 1, size, 4711, 4711);
						pb.addSendRecv(world, 1, 0, 0, size, 4711, 4711);
					}

					runSimulationWithoutOutput();

					//modelTime.write(simRes.getVirtualTime().getDouble() + " " + " throughput: " +  (2*(size + 40) / simRes.getVirtualTime().getDouble() / 1024 / 1024) + " MiB/s" );
					modelTime.write((2*(size + 40) / simRes.getVirtualTime().getDouble() / 1024 / 1024) + " " );
					modelTime.flush();
					if(size == 0){
						size = 64;
					}
				}
				modelTime.write("\n");
			}
		}
		System.out.println("Completed!");
	}


	public void sendRecvPaired(BufferedWriter modelTime ) throws Exception{
		// test cases run on the WR cluster
		for(int size: sizes){
			modelTime.write("SendRecvPaired" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);


				if (processes % 2 != 0){ // invalid test
					modelTime.write("0.0 ");
					continue;
				}

				setupWrCluster(nodes, processes);
				int repeats = 1;

				if(nodes == 1 && size <= 100*1024){
					repeats = 100;
				}

				for(int i=0; i < repeats; i++){
					for(int rank=0; rank < processes ; rank++){
						int dest = rank % 2 == 0 ? rank + 1 : rank -1;

						pb.addSendRecv(world, rank, dest, dest, size, 4711, 4711);
					}
				}
				addBarrier(app, config);
				if(size <= 1*MiB){
					model.getGlobalSettings().setTransferGranularity(512);
				}else{
					model.getGlobalSettings().setTransferGranularity(100 * KiB);
				}
				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}



	public void sendRootWhichReceives(BufferedWriter modelTime) throws Exception{
		// test cases run on the WR cluster

		for(int size: sizes){
			modelTime.write("SendRoot" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				int repeats = 1;

				if(nodes == 1 && size <= 100*1024){
					repeats = 100;
				}

				for(int i=0; i < repeats; i++){
					for(int rank=1; rank < processes ; rank++){
						pb.addSendAndRecv(world, rank, 0, size, 4711);
					}
				}
				addBarrier(app, config);
				if(size <= 1*MiB){
					model.getGlobalSettings().setTransferGranularity(512);
				}else{
					model.getGlobalSettings().setTransferGranularity(100 * KiB);
				}

				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}


	public void sendRecvRoot(BufferedWriter modelTime) throws Exception{
		// test cases run on the WR cluster

		for(int size: sizes){
			modelTime.write("SendRecvRoot" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				int repeats = 1;

				if(nodes == 1 && size <= 100*1024){
					repeats = 100;
				}

				for(int i=0; i < repeats; i++){
					for(int rank=1; rank < processes ; rank++){
						pb.addSendRecv(world, rank, 0, 0, size, 4711, 4711);
						pb.addSendRecv(world, 0, rank, rank, size, 4711, 4711);
					}

				}
				addBarrier(app, config);
				if(size <= 1*MiB){
					model.getGlobalSettings().setTransferGranularity(512);
				}else{
					model.getGlobalSettings().setTransferGranularity(100 * KiB);
				}
				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}

	public void sendRecvRingRightLeftNeighbor(BufferedWriter modelTime) throws Exception{
		// test cases run on the WR cluster

		for(int size: sizes){
			modelTime.write("SendRecvRightNeighbor" + size + " ");
			for(String config : configs){
				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				setupWrCluster(nodes, processes);

				if(processes == 1){
					modelTime.write("0.0 ");
					continue;
				}

				int repeats = 1;

				if(nodes == 1 && size <= 100*1024){
					repeats = 100;
				}

				for(int i=0; i < repeats; i++){
					for(int rank=0; rank < processes ; rank++){
						int dest = (rank == processes - 1) ? 0 : rank + 1;
						int src = (rank == 0) ? processes - 1 : rank - 1;

						pb.addSendRecv(world, rank, src, dest, size, 4711, 4711);
					}
				}
				addBarrier(app, config);

				if(size <= 1*MiB){
					model.getGlobalSettings().setTransferGranularity(512);
				}else{
					model.getGlobalSettings().setTransferGranularity(100 * KiB);
				}

				runSimulationWithoutOutput();

				modelTime.write(simRes.getVirtualTime().getDouble() + " ");
				modelTime.flush();
			}
			modelTime.write("\n");
		}
		System.out.println("Completed!");
	}


	public void validationRunCollectiveWithSendRecv(BufferedWriter modelTime) throws Exception{
		BufferedWriter outputFile = new BufferedWriter(new FileWriter("/tmp/collectives-runTime.txt"));
		outputFile.write("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n");

		// test cases run on the WR cluster


		String [] experiments = new String[]{"Reduce", "Allreduce", "Bcast", "Barrier",  "Allgather", "Gather", "Scatter"};


		for(String experiment: experiments){
			for(int size: sizes){
				String strSize = "" + size;

				if(experiment.equals("Barrier")){
					if(size != sizes[0]){
						continue;
					}
					strSize = "";
				}

				modelTime.write(experiment + strSize + " ");

				for(String config: configs){

					final int nodes = Integer.parseInt(config.split("-")[0]);
					final int processes = Integer.parseInt(config.split("-")[1]);

					// those values are set by the real-world test...
					int repeats = 0;

					if(! experiment.equals("Barrier")){
						if(nodes == 1 && size <= 100*1024){
							repeats = 99;
						}
					}else if (experiment.equals("Barrier") && nodes == 1){
						repeats = 99;
					}

					setupWrCluster(nodes, processes);
					if(size <= 1*MiB){
						model.getGlobalSettings().setTransferGranularity(512);
					}else{
						model.getGlobalSettings().setTransferGranularity(100 * KiB);
					}
					runCollectiveTest(nodes, processes, experiment, strSize, outputFile, modelTime,  true,  ! experiment.equals("Barrier"), null, repeats);
				}
				modelTime.write("\n");

				if(experiments.equals("Reduce") || experiments.equals("Allreduce") || experiments.equals("Broadcast")){
					// add a dummy
					modelTime.write(experiment + "1000M\n");
				}
			}
		}
		outputFile.close();
	}

	@Test public void benchmarkingLargeTransmissions() throws Exception{
		BufferedOutputStream outputFile = new BufferedOutputStream(new FileOutputStream(new File("/tmp/timing")));

		boolean asserts = false;

		// set the value:
		//		assert( asserts = true );

		outputFile.write(("Assertions enabled: " + asserts).getBytes());

		for(int i=0; i < 3 ; i++){
			setupSMP(2, 1);
			mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
			pb.addSendAndRecv(world, 0, 1, 100000* MiB, 4711);
			parameters.setTraceEnabled(false);

			runSimulationAllExpectedToFinish();

			final SimulationResultSerializer serializer = new SimulationResultSerializer();
			outputFile.write(serializer.serializeResults(simRes).toString().getBytes());
			outputFile.flush();
		}

		outputFile.close();
	}

	@Test public void broadcastTreeAnalytical() throws Exception{
		setupAnalytical(8, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed");

		parameters.setTraceEnabled(false);
		parameters.setTraceFile("/tmp/bcast");


		pb.addBroadcast(world, 3, 100 * MiB);

		runSimulationWithoutOutput();
	}


	@Test public void broadcastTreeAnalyticalIterative() throws Exception{
		int count = 1;

		BufferedOutputStream outputFile = new BufferedOutputStream(new FileOutputStream(new File("/tmp/treeAnalyticalIterative.txt")));
		outputFile.write(("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n").getBytes());

		for(int i=0; i < 11;i++){
			count = count*2;
			long sTime, setupSystemTime, setupProgramTime;

			sTime = new Date().getTime();
			setupAnalytical(count, 1);
			setupSystemTime = (new Date().getTime() - sTime);


			mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
			mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed");

			parameters.setTraceEnabled(false);
			parameters.setTraceFile("/tmp/bcast");


			sTime = new Date().getTime();
			pb.addBroadcast(world, 0, 100 * MiB);
			setupProgramTime = (new Date().getTime() - sTime);


			runSimulationWithoutOutput();

			outputFile.write((count + "\t" + simRes.getEventCount() + "\t" + simRes.getWallClockTime() + "\t" + setupSystemTime  / 1000.0 + "\t" + setupProgramTime  / 1000.0 + "\n").getBytes());
			outputFile.flush();
		}
		outputFile.close();
	}


	@Test public void broadcastSimpleBlockwise() throws Exception{
		setup(5, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimpleBlockwise");
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 3, 100 * MiB);

		runSimulationAllExpectedToFinish();
	}




	@Test public void test() throws Exception{
		setupWrCluster(5, 5);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		pb.addAllgather(world, 10*MiB);

		parameters.setTraceFile("/tmp/out");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Allgather"), "de.hd.pvs.piosim.simulator.program.Allgather.AllgatherMPICH2");

		parameters.setTraceEnabled(true);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastBroadcastScatterGatherall() throws Exception{
		setup(8, 1);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterGatherall");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 0,100 * MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void broadcastBroadcastPipedBlockwise() throws Exception{
		//setup(3, 1);
		setupSMP(3);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwise");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		pb.addBroadcast(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastBroadcastScatterBarrierGatherall() throws Exception{
		setupWrCluster(2, 7);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterBarrierGatherall");

		parameters.setTraceFile("/tmp/bcast");
		parameters.setTraceEnabled(true);
		parameters.setTraceInternals(true);

		pb.addBroadcast(world, 0,100 * MiB);

		runSimulationAllExpectedToFinish();
	}

	protected void printTiming(String header, double[] times) throws IOException{
		final FileWriter fo = new FileWriter("/tmp/timing-" + this.getClass().getSimpleName() + ".txt", true);

		fo.write(header + " timing:\n");
		System.out.println(header + " timing");

		for(int i=1; i < times.length; i++){
			if(times[i] != 0){
				fo.write(i + " " + times[i] + "\n");
				System.out.println(i + " " + times[i]);
			}
		}
		fo.write("\n");
		fo.close();
	}

	@Test public void sendAndRecvEagerTestSMP() throws Exception{
		setupSMP(2);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/sendRecvTest");

		parameters.setTraceEnabled(true);

		pb.addSendAndRecv(world, 0, 1, 10 * MiB, 1);

		runSimulationAllExpectedToFinish();
	}



	@Test public void MPICH2Reduce() throws Exception{
		setup(5, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/mpich2reduce");

		parameters.setTraceEnabled(true);

		pb.addReduce(world, 0, 10 * MiB);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterHierachicalTwoLevels() throws Exception{
		setup(5, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterHierachicalTwoLevels");  //andere Implementation
		//CommandToSimulationMapper Eintrag, Standard letzter

		parameters.setTraceFile("/tmp/scatter");

		parameters.setTraceEnabled(true);

		pb.addScatter(world, 0, 10 * KiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void TestReduceScatterGatherMPICH2() throws Exception{
		setup(2, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Reduce"), "de.hd.pvs.piosim.simulator.program.Reduce.ReduceScatterGatherMPICH2");

		parameters.setTraceFile("/home/scan/scatter");

		//	parameters.setTraceInternals(true);
		parameters.setTraceEnabled(true);

		pb.addReduce(world, 0, 100* MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void TestGatherDirect() throws Exception{
		setupSMP(5);

		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.Direct");

		parameters.setTraceFile("/tmp/gather");
		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 100* MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void TestGatherWorld() throws Exception{
		setupSMP(15);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.GatherBinaryTreeMPICH2");

		parameters.setTraceFile("/tmp/gather");
		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 100* MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void TestScatter3World() throws Exception{
		setupSMP(5);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		pb.addScatter(world, 4, 100* MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatter2() throws Exception{
		setupSMP(2);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		Communicator comm = new Communicator("world");
		comm.addRank(0, 1, 0);
		comm.addRank(1, 0, 0);

		pb.addScatter(comm, 1, 100* MiB);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterMPICH5ProcessesOnThreeNodes() throws Exception{
		setup(2, 3);


		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.ScatterMPICH2");

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		Communicator comm = new Communicator("world");
		comm.addRank(0, 0, 0);
		comm.addRank(1, 2, 0);
		comm.addRank(2, 4, 0);
		comm.addRank(3, 1, 0);
		comm.addRank(4, 3, 0);

		pb.addScatter(comm, 0, 100* MiB);

		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterFCFS() throws Exception{
		setup(3, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");
		parameters.setTraceFile("/tmp/scatterFCFS");
		parameters.setTraceEnabled(true);
		pb.addScatter(world, 0, 10 * MiB);
		runSimulationAllExpectedToFinish();
	}

	@Test public void TestGatherFCFS() throws Exception{
		setup(3, 1);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Gather.FCFS");
		parameters.setTraceFile("/tmp/gatherFCFS");
		parameters.setTraceEnabled(true);
		pb.addGather(world, 0, 10 * MiB);
		runSimulationAllExpectedToFinish();
	}



	@Test public void TestScatterFCFSOrdered123() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addCompute(1, 10000000);
		pb.addCompute(2, 20000000);
		pb.addCompute(3, 30000000);

		pb.addScatter(world, 0, 10 * MiB);
		runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatterFCFSLate() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addCompute(0, 10000000);

		pb.addScatter(world, 0, 10 * MiB);
		runSimulationAllExpectedToFinish();
	}

	@Test public void TestScatterFCFSEqual() throws Exception{
		setup(4,1);

		parameters.setTraceFile("/tmp/scatter");
		parameters.setTraceEnabled(true);

		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		mb.getGlobalSettings().setClientFunctionImplementation(
				new CommandType("Scatter"), "de.hd.pvs.piosim.simulator.program.Scatter.FCFS");

		pb.addScatter(world, 0, 10 * MiB);
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

	@Test
	public void runJacobi_x() throws Exception{
		String which = "/7000-NS-NC-NProc-Var-Unlimited/N5-P1-C5-P5-S0-RAM17800/23220.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";
		which = "/1000-2S-NC-NProc-1000M-shm/N5-P1-C3-P3-S2-RAM10000/23094.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";
		which = "/1000-2S-1C-NProc-150M/N3-P1-C1-P4-S2-RAM150/22939.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";
		which = "/100-0S-NC-NProc-Unlimited/N5-P1-C5-P5-S0-RAM17800/25839.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";
		which = "/7000-NS-NC-NProc-Var-Unlimited/N5-P1-C5-P5-S0-RAM17800/23220.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";
		which = "/1000-2S-NC-NProc-1000M-shm/N6-P1-C4-P4-S2-RAM10000/23096.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";

		which = "/7000-NS-NC-NProc-Var-Unlimited/N5-P1-C5-P5-S5-RAM17800/22904.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";
		//which = "/1000-2S-NC-NProc-150M/N4-P1-C2-P2-S2-RAM150/23139.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";
		which = "/1000-2S-1C-NProc-150M/N3-P1-C1-P1-S2-RAM150/22947.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj";

		//useMPIImplementations = useMPIImplementations.MPICH2;
		useMPIImplementations = useMPIImplementations.MPICH2;

		BufferedWriter output = new BufferedWriter(new FileWriter("/tmp/x.txt"));

		runPartdiffParExperiment(which, output, true);
	}






	// Test case showing that the implemented broadcast is not 100% MPICH2 broadcast, because it does sendrecv, but MPI does just ones send/recv.
	@Test public void broadcast100MiB() throws Exception{
		setupWrCluster(1, 2, 0,0, null, 1000);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		parameters.setTraceFile("/tmp/bcast");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 0, 100 * MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void gatherBug8_8() throws Exception{
		setupWrCluster(8, 8, 0, 0, null, 1000);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		parameters.setTraceFile("/tmp/gather");

		parameters.setTraceEnabled(true);

		pb.addGather(world, 0, 100*MiB);
		//pb.addBarrier(world);

		runSimulationAllExpectedToFinish();
	}

	@Test public void gatherDirectPerformance() throws Exception{
		benchmarkCollective( new ValidationExperiment() {

			@Override
			String getName() {
				return "GatherDirect";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addGather(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Gather"), "de.hd.pvs.piosim.simulator.program.Gather.Direct");
			}
		}, null);
	}

	@Test public void bcastPipedBlockwisePerformance() throws Exception{
		benchmarkCollective( new ValidationExperiment() {

			@Override
			String getName() {
				return "BcastPipedBlockwise";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwise");
			}
		}, null);
	}

	@Test public void bcastPipedBlockwise48() throws Exception{
		setupWrCluster(4, 8, 0, 0, null, 1000);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		parameters.setTraceFile("/tmp/bcastPiped");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwise");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 0, 100*MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void bcastPipedBlockwiseSMPAware48() throws Exception{
		setupWrCluster(4, 8, 0, 0, null, 1000);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		parameters.setTraceFile("/tmp/bcastPiped");
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwiseSMPAware");

		parameters.setTraceEnabled(true);

		pb.addBroadcast(world, 0, 100*MiB);

		runSimulationAllExpectedToFinish();
	}

	@Test public void bcastPipedBlockwise48ChangedMapping() throws Exception{
		setupWrCluster(2, false, false, false, false, 4, 8, 0, 0, null, 1000);
		mb.getGlobalSettings().setMaxEagerSendSize(100 * KiB);
		parameters.setTraceFile("/tmp/bcastPiped");

		parameters.setTraceEnabled(true);
		mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwise");

		pb.addBroadcast(world, 0, 100*MiB);

		runSimulationAllExpectedToFinish();
	}


	@Test public void bcastBinaryTreePerformance() throws Exception{
		benchmarkCollective( new ValidationExperiment() {

			@Override
			String getName() {
				return "BcastBinaryTree";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");
			}
		}, null);
	}

	abstract class ValidationExperiment{
		abstract String getName();
		abstract void addOperation(ProgramBuilder p, long size);
		boolean createTrace(){
			return true;
		}

		void setup(ModelBuilder mb){

		}
	}

	ValidationExperiment [] collectiveExperiments = new ValidationExperiment[]{
			new ValidationExperiment() {
				@Override
				String getName() {	return "Reduce";	}
				@Override
				void addOperation(ProgramBuilder p, long size) {
					pb.addReduce(world, 0, size);
				}
			},
			new ValidationExperiment() {
				@Override
				String getName() {	return "Allreduce";	}
				@Override
				void addOperation(ProgramBuilder p, long size) {
					pb.addAllreduce(world, size);
				}
			},
			new ValidationExperiment() {
				@Override
				String getName() {	return "Broadcast";	}
				@Override
				void addOperation(ProgramBuilder p, long size) {
					pb.addBroadcast(world, 0, size);
				}
			},
			new ValidationExperiment() {
				@Override
				String getName() {	return "Barrier";	}
				@Override
				void addOperation(ProgramBuilder p, long size) {
					pb.addBarrier(world);
				}
			},
			new ValidationExperiment() {
				@Override
				String getName() {	return "Allgather";	}
				@Override
				void addOperation(ProgramBuilder p, long size) {
					pb.addAllgather(world, size);
				}
			}
			,
			new ValidationExperiment() {
				@Override
				String getName() {	return "Gather";	}
				@Override
				void addOperation(ProgramBuilder p, long size) {
					pb.addGather(world, 0, size);
				}
			}
			,
			new ValidationExperiment() {
				@Override
				String getName() {	return "Scatter";	}
				@Override
				void addOperation(ProgramBuilder p, long size) {
					pb.addScatter(world, 0, size);
				}
			}

	};

	public void benchmarkCollective(ValidationExperiment experiment, BufferedWriter outputFile) throws Exception{
		if(outputFile == null){
			outputFile = new BufferedWriter(new FileWriter("/tmp/benchmarkCollective.txt"));
		}

		for(int size: sizes){
			String strSize = "" + size;

			if(experiment.getName().equals("Barrier")){
				if(size != sizes[0]){
					continue;
				}
				strSize = "";
			}

			outputFile.write(experiment.getName() + strSize + " ");
			outputFile.flush();

			for(String config: configs){

				final int nodes = Integer.parseInt(config.split("-")[0]);
				final int processes = Integer.parseInt(config.split("-")[1]);

				// those values are set by the real-world test...
				int repeats = 0;

				if(! experiment.equals("Barrier")){
					if(nodes == 1 && size <= 100*1024){
						repeats = 99;
					}
				}

				setupWrCluster(nodes, processes, 0,0, null, 1000);

				for(int i=0; i <= repeats; i++){
					experiment.addOperation(pb, size);
				}

				if(!experiment.getName().equals("Barrier")){
					pb.addBarrier(world);
				}

				experiment.setup(mb);


				sim = new Simulator();
				model = mb.getModel();

				if(size <= 1*MiB){
					model.getGlobalSettings().setTransferGranularity(512);
				}else{
					model.getGlobalSettings().setTransferGranularity(100 * KiB);
				}


				try{

					sim.initModel(model, parameters);
					simRes = sim.simulate();

					outputFile.append( " " + sim.getVirtualTime().getDouble()); // + " simTime: " + simRes.getWallClockTime() + " events: " + simRes.getEventCount()  + "\n");
				}catch(Throwable e){
					outputFile.write(config + " error " + e.getMessage());
				}
				outputFile.flush();

			}
			outputFile.write("\n");
		}
	}



	final String projectsPath = "/mnt/diss/Jacobi-MPI";

	// test cases run on the WR cluster
	@Test
	public void verifyValidateSimulationCollectives() throws Exception{
		BufferedWriter outputFile = new BufferedWriter(new FileWriter("/tmp/sim-collectives.txt"));

		outputFile.write("#Proc\tEvents\tRuntime\tSysModelT\tProgramMT\n");

		for(ValidationExperiment experiment: collectiveExperiments) {
			benchmarkCollective(experiment, outputFile);
		}
		outputFile.close();
	}


	@Test
	public void computePipelineBcast() throws Exception{
		BufferedWriter outputFile = new BufferedWriter(new FileWriter("/tmp/pipelineBcast.txt"));


		benchmarkCollective( new ValidationExperiment() {
			@Override
			String getName() {
				return "PipelineBcastSMPAware";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwiseSMPAware");
			}
		}, outputFile);

		benchmarkCollective( new ValidationExperiment() {
			@Override
			String getName() {
				return "PipelineBcast";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.PipedBlockwise");
			}
		}, outputFile);


		benchmarkCollective( new ValidationExperiment() {
			@Override
			String getName() {
				return "BroadcastScatterGatherall";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BroadcastScatterGatherall");
			}
		}, outputFile);



		benchmarkCollective( new ValidationExperiment() {
			@Override
			String getName() {
				return "BinaryTreeSimpleBlockwise";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeSimpleBlockwise");
			}
		}, outputFile);



		benchmarkCollective( new ValidationExperiment() {
			@Override
			String getName() {
				return "BinaryTree";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTree");
			}
		}, outputFile);





		benchmarkCollective( new ValidationExperiment() {
			@Override
			String getName() {
				return "BinaryTreeNotMultiplexed";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.BinaryTreeNotMultiplexed");
			}
		}, outputFile);


		benchmarkCollective( new ValidationExperiment() {
			@Override
			String getName() {
				return "Direct";
			}

			@Override
			void addOperation(ProgramBuilder p, long size) {
				p.addBroadcast(world, 0, size);
			}

			@Override
			void setup(ModelBuilder mb) {
				mb.getGlobalSettings().setClientFunctionImplementation(	new CommandType("Bcast"), "de.hd.pvs.piosim.simulator.program.Bcast.Direct");
			}
		}, outputFile);

	}

	@Test
	public void runPartdiff() throws Exception{
		//runPartdiffParExperiment("/7000-NS-NC-NProc-Var-Unlimited/N10-P1-C10-P10-S10-RAM20390/23109.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj", null, true);
		//runPartdiffParExperiment("/2000-NS-NC-NProc-Overlapped-Unlimited/N10-P1-C10-P10-S10-RAM20390/23163.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj", null, true);
		runPartdiffParExperiment("/100-0S-NC-NProc-Unlimited/N7-P1-C7-P7-S0-RAM15507/25849.cluster.wr.informatik.uni-hamburg.de/partdiff-par.proj", null, true);
		 //runPartdiffParExperiment("/1000-2S-NC-NProc-1000M-shm/N5-P1-C3-P3-S2-RAM10000/23094.cluster.wr.inform
		//runPartdiffParExperiment("/7000-NS-NC-NProc-Var-Unlimited/N5-P1-C5-P5-S0-RAM17800/23220.cluster.wr.info


	}



	public void runPartdiffParExperiment(String projectLocal, BufferedWriter output, boolean trace) throws Exception{
		String parts [] = projectLocal.split("/");
		String config = parts[2];

		if(output == null){
			output = new BufferedWriter(new FileWriter("/tmp/partdiff.txt"));
		}


		// N6-P1-C6-P6-S6-RAM16500
		if ( projectLocal.contains("-shm/") ){
			System.out.println("WARNING using TMPFS!!!");
			usedDisk = IOC.ShmTmpfs();
		}else{
			usedDisk = IOC.PVSDisk();
		}


		Pattern p2 = Pattern.compile("([0-9]*)-.*");
		Matcher m2 = p2.matcher(parts[1]);

		if (! m2.matches()) {
			throw new IllegalArgumentException("Matrix size could not be determined " + parts[1]);
		}

		int matrixSize = Integer.parseInt(m2.group(1));

		Pattern p = Pattern.compile("N([0-9]*)-P([0-9]+)-C([0-9]+)-P([0-9]+)-S([0-9]+)-RAM([0-9]+)");
		Matcher m = p.matcher(config);
		if(! m.matches()){
			System.out.println("Config does not match: " + config);
			output.write("Config does not match: " + config + "\n");
			return;
		}
		final int nodes = Integer.parseInt(m.group(1));
		final int clientNodes = Integer.parseInt(m.group(3));
		final int processes = Integer.parseInt(m.group(4));
		final int servers = Integer.parseInt(m.group(5));
		int ram = Integer.parseInt(m.group(6)) - 100; // 100 MiB for the Linux system are subtracted

		if (ram > 11500) ram = 11500;

		AggregationReorderCache cache = new AggregationReorderCache();
		cache.setName("PVS-CACHE");
		cache.setMaxNumberOfConcurrentIOOps(1);

		final int overlapping;

		if(servers + clientNodes > nodes){
			overlapping = clientNodes + servers - nodes;

			if (overlapping != clientNodes){
				System.out.println("Error experiment does not make sense: " + overlapping + " " + clientNodes);
				output.write("Error experiment does not make sense: " + overlapping + " " + clientNodes + "\n");
				return;
			}

			// change amount of memory to account for the matrix size
			int matrixMemory = (int) ((((long) matrixSize) * matrixSize) * 8*8*8*2  / 1024 / 1024 / processes);
			ram = ram - matrixMemory;

			System.out.println("Amount of memory needed for the matrix " + matrixMemory + " memFree: " + ram );
		}else{
			overlapping = 0;
		}

		setupWrCluster(clientNodes, processes, overlapping,  servers - overlapping , cache, ram);

		parameters.setTraceFile("/tmp/test");
		parameters.setTraceEnabled(trace);
		//parameters.setTraceClientSteps(true);
		//parameters.setTraceInternals(true);
		//parameters.setTraceClientNestingOperations(true);
		parameters.setTraceServers(true);

		final String project = projectsPath + "/" + projectLocal;


		final ApplicationXMLReader axml = new ApplicationXMLReader();
		final Application app = axml.parseApplication(project, true);
		mb.setApplication("Validate", app);


		sim = new Simulator();
		model = mb.getModel();
		model.getGlobalSettings().setIOGranularity(10 * MiB);
		model.getGlobalSettings().setTransferGranularity(100 * KiB);

		sim.initModel(model, parameters);
		simRes = sim.simulate();

		output.append( " modeltime: " + sim.getVirtualTime().getDouble() + " simTime: " + simRes.getWallClockTime() + " events: " + simRes.getEventCount()  + "\n");

		//final SimulationResultSerializer serializer = new SimulationResultSerializer();
		//output.append(serializer.serializeResults(simRes)+ "\n");
		output.flush();
	}

	// parse inputs from configuration files...
	@Test
	public void runPartdiffParExperiments() throws Exception{
		final BufferedReader projectsToRun = new BufferedReader(new FileReader(projectsPath + "/projects.txt"));

		useMPIImplementations = MPIImplementations.MPICH2;

		BufferedWriter output = new BufferedWriter(new FileWriter("/tmp/partdiff.txt"));

		int i=0;

		while(projectsToRun.ready()){
			final String projectLocal = projectsToRun.readLine();
			if(projectLocal.length() < 3){
				//output.write(" ");
				continue;
			}

			final String project = projectsPath + "/" + projectLocal;

			i++;

			if(i < 398){
				continue;
			}


			System.out.println(i + " " + projectLocal);
			output.write(i + " " + project + "\n");
			output.flush();

			// parse required configuration, nodes etc...

			runPartdiffParExperiment(projectLocal, output, false);

		}

		output.close();
	}

}
