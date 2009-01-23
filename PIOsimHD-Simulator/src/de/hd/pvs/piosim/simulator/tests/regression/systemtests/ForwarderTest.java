package de.hd.pvs.piosim.simulator.tests.regression.systemtests;

import java.util.ArrayList;

import org.junit.Test;

import de.hd.pvs.piosim.model.ModelBuilder;
import de.hd.pvs.piosim.model.components.ClientProcess.ClientProcess;
import de.hd.pvs.piosim.model.components.Connection.Connection;
import de.hd.pvs.piosim.model.components.IOSubsystem.RefinedDiskModel;
import de.hd.pvs.piosim.model.components.NIC.NIC;
import de.hd.pvs.piosim.model.components.Node.Node;
import de.hd.pvs.piosim.model.components.Port.Port;
import de.hd.pvs.piosim.model.components.Server.Server;
import de.hd.pvs.piosim.model.components.ServerCacheLayer.AggregationCache;
import de.hd.pvs.piosim.model.components.Switch.SimpleSwitch;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.inputOutput.distribution.SimpleStripe;
import de.hd.pvs.piosim.model.program.ApplicationBuilder;
import de.hd.pvs.piosim.model.program.ProgramBuilder;
import de.hd.pvs.piosim.model.util.Epoch;

/**
 * Test the router
 * 
 * @author julian
 */
public class ForwarderTest extends ClusterTest{
	protected ModelBuilder createDisjointGridModel(int clientsHalve, int serversHalve) throws Exception {

		int nodeCountOneHalf = clientsHalve + serversHalve;

		ModelBuilder mb = new ModelBuilder();

		Connection conn = new Connection();
		conn.setName("1GBit Ethernet");
		conn.setLatency(new Epoch(0.0002));
		conn.setBandwidth(100 * MBYTE);

		mb.getModel().getGlobalSettings().setTransferGranularity(100 * KBYTE);

		mb.addTemplate(conn);

		Node node = new Node();
		node.setName("PVS-Node");
		//maschine.setCacheSize(1024*1024*1024);
		node.setCPUs(1);
		node.setInstructionsPerSecond(1000000);
		node.setInternalDataTransferSpeed(1000 * MBYTE);
		node.setMemorySize(1000*1024*1024);

		//SimpleDisk iosub = new SimpleDisk();

		RefinedDiskModel iosub = new RefinedDiskModel();
		iosub.setAverageSeekTime(new Epoch(0.005));
		iosub.setTrackToTrackSeekTime(new Epoch(0.001));
		iosub.setRPM(7200);
		iosub.setPositionDifferenceConsideredToBeClose(MBYTE * 5);
		iosub.setSequentialTransferRate(((int) 50 * MBYTE));

		iosub.setName("IBM");
		//iosub.setAvgAccessTime(new Epoch(0.002));
		//iosub.setMaxThroughput(50 * MBYTE);

		mb.addTemplate(iosub);

		NIC nic = new NIC();
		nic.setConnection(conn);

		mb.addNIC(node, nic);

		mb.addTemplate(node);

		SimpleSwitch sw = new SimpleSwitch();
		sw.setName("PVS-Switch");
		sw.setTotalBandwidth(380 * MBYTE);

		Port  port = new Port();
		//port.setParentSwitch(sw);
		port.setConnection(conn);
		for(int i=0; i <= nodeCountOneHalf; i++)
			mb.addPort(sw, port);

		mb.addTemplate(sw);

		///// NOW BUILD OBJECTS BASED ON PREVIOUS SETUP...


		ArrayList<Node> nodes = new ArrayList<Node>();

		// two switches, one for each halve
		SimpleSwitch testSW [] = { mb.cloneFromTemplate(sw), mb.cloneFromTemplate(sw)};
		mb.addSwitch(testSW[0]);
		mb.addSwitch(testSW[1]);

		for( int swi = 0; swi < 2; swi++){
			for (int i=0; i < nodeCountOneHalf; i++){
				Node node2 = mb.cloneFromTemplate(node);
				nodes.add(node2);
				mb.addNode(node2);

				mb.setConnection(node2.getNICs().get(0), testSW[swi].getPorts().get(i));
			}
		}

		SimpleSwitch backBoneSwitch = mb.cloneFromTemplate(sw);
		backBoneSwitch.setName("Backbone");
		mb.addSwitch(backBoneSwitch);
		// link backbone with other two switches.
		mb.setConnection(testSW[0].getPorts().get(nodeCountOneHalf), backBoneSwitch.getPorts().get(0));
		mb.setConnection(testSW[1].getPorts().get(nodeCountOneHalf), backBoneSwitch.getPorts().get(1));

		for(int i=0; i < clientsHalve * 2; i++ ){
			ClientProcess c = new ClientProcess();
			c.setName("Client" + i);
			mb.addClient(nodes.get(i), c);
			c.setRank(i);
			c.setApplication("Jacobi");
		}

		Server serverTemplate = new Server();
		serverTemplate.setName("Server");
		serverTemplate.setIOsubsystem(iosub);

		AggregationCache cacheImpl = new AggregationCache(); //NoCache()
		cacheImpl.setReadDataSievingMaxHoleSizeToCombine(10 * (int) MBYTE);
		cacheImpl.setMaxNumberOfConcurrentIOOps(1);		
		serverTemplate.setCacheImplementation(cacheImpl);


		mb.addTemplate(serverTemplate);


		for( int swi = 0; swi < 2; swi++){
			for(int i=0; i < serversHalve; i++ ){
				Server s = mb.cloneFromTemplate(serverTemplate);
				// disjoint client and server processes
				mb.addServer(nodes.get(i + (swi +1) * clientsHalve + serversHalve * swi), s);
			}
		}

		return mb;
	}
	
	protected void setup(int clientHalve, int serverHalve) throws Exception{
		mb = createDisjointGridModel(clientHalve, serverHalve);		
		aB = new ApplicationBuilder("Jacobi", "Example Jacobi", clientHalve*2);
		app = aB.getApplication();

		pb = new ProgramBuilder(aB);
		
		world = aB.getWorldCommunicator();
		
		model = mb.getModel();		
	}

	@Test public void writeTest() throws Exception{
		testMsg();		
		setup(1, 1);

		SimpleStripe dist = new SimpleStripe();
		dist.setChunkSize(MBYTE);
		
		MPIFile file =  aB.createFile("testfile", 0, dist);
		
		pb.addFileOpen(file, world, true);
		
		pb.addWriteSequential(0, file, 4, 2 * MBYTE);
		runSimulationAllExpectedToFinish();
	}

	@Test public void broadcastTest() throws Exception{
		testMsg();		
		setup(1, 0);
		
		pb.addBroadcast(world,  0, 10 * MBYTE);
		//pb.addSendAndRecv(world, 0, 1, 100, 1);
		
		//parameters.setDebugEverything(true);
		
		runSimulationAllExpectedToFinish();
	}
	
		
	public static void main(String[] args) throws Exception{
		ForwarderTest t = new ForwarderTest();
		
		//t.writeTest();
		t.broadcastTest();
		
		//System.out.println(t.mb.getModel());
	}
}
