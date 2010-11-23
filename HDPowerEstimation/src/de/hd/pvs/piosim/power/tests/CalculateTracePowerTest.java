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
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.calculation.BaseCalculation;
import de.hd.pvs.piosim.power.cluster.BuildException;
import de.hd.pvs.piosim.power.cluster.ExtendedNode;
import de.hd.pvs.piosim.power.cluster.Node;
import de.hd.pvs.piosim.power.cluster.NodeFactory;
import de.hd.pvs.piosim.power.cluster.PowerSupply;
import de.hd.pvs.piosim.power.data.DeviceData;
import de.hd.pvs.piosim.power.replay.Replay;
import de.hd.pvs.piosim.power.replay.ReplayDevice;
import de.hd.pvs.piosim.power.replay.ReplayException;
import de.hd.pvs.piosim.power.replay.ReplayItem;
import de.hd.pvs.piosim.power.replay.strategy.ApproachPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.OptimalPlayStrategy;
import de.hd.pvs.piosim.power.replay.strategy.SimplePlayStrategy;
import de.hd.pvs.piosim.power.trace.HDTraceImporter;
import de.hd.pvs.piosim.power.trace.HDTraceImporterException;

public class CalculateTracePowerTest extends AbstractTestCase {

	@Test
	public void testWithThesisFolder() {

		Logger.getRootLogger().setLevel(Level.OFF);
		
		
		Map<String,List<BigDecimal>> statistics = new HashMap<String,List<BigDecimal>>();

		boolean printOnlySum = true;

		FileWriter writer = null;

		try {
			writer = new FileWriter("/home/quark/studium/masterthesis/traces/tex.out");
			writer.write("\\begin{tabular}{lllllllllllll}\\toprule\n");
			writer.write("\\multicolumn{4}{l}{setup} & \\multicolumn{9}{l}{power consumption}\\\\\n");
			writer.write("\\midrule\\\\\n");
			writer.write(" & & & & measured & \\multicolumn{2}{l}{simple} & \\multicolumn{2}{l}{optimal} & \\multicolumn{2}{l}{approach} & \\multicolumn{2}{l}{efficient devices} \\\\\n");
			writer.write("\\midrule\\\\\n");
			writer.write("interlines & special & pvfs & mpi & total & total & deviance & total & saving & total & saving & total & saving \\\\\n");
			writer.write("\\midrule\\\\\n");
		} 
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Map<String, String> nameToACPIDeviceMapping = new HashMap<String, String>();

		nameToACPIDeviceMapping.put("CPU_TOTAL_0", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("CPU_TOTAL_1", "pvscluster.CPU");
		nameToACPIDeviceMapping.put("MEM_USED", "pvscluster.Memory");
		nameToACPIDeviceMapping.put("NET_OUT", "pvscluster.NIC");
		nameToACPIDeviceMapping.put("NET_IN", "pvscluster.NIC");
		nameToACPIDeviceMapping.put("HDD_WRITE", "pvscluster.Disk");

		String[] componentNames = new String[nameToACPIDeviceMapping.size()];
		componentNames = nameToACPIDeviceMapping.keySet().toArray(
				componentNames);

		Map<String,BigDecimal[]> powerConsumptions = new HashMap<String,BigDecimal[]>();

		List<String> excludeProjectNames = new ArrayList<String>();
		excludeProjectNames.add("partdiff-mpi-non-collective-4-0-800-nfs");

		// if empty or not initialized print all
		List<String> projectNames = new ArrayList<String>();

//		projectNames.add("partdiff-mpi-non-collective-1-0-800");
//		projectNames.add("partdiff-mpi-non-collective-1-1-800");
//		projectNames.add("partdiff-mpi-non-collective-1-1-950");
//		projectNames.add("partdiff-mpi-non-collective-3-1-1400");
//		projectNames.add("partdiff-mpi-non-collective-4-0-1600");
//		projectNames.add("partdiff-mpi-non-collective-4-0-1600-100it");
//		projectNames.add("partdiff-mpi-non-collective-4-0-1600-200it");
//		projectNames.add("partdiff-mpi-non-collective-4-0-1600-500it");
//		projectNames.add("partdiff-mpi-non-collective-4-0-800-nfs");
//		projectNames.add("partdiff-mpi-non-collective-4-1-1600");
//		projectNames.add("partdiff-mpi-non-collective-4-1-800");
//		projectNames.add("partdiff-mpi-non-collective-4-2-800");
//		projectNames.add("partdiff-mpi-non-collective-4-4-1600");
//		projectNames.add("partdiff-mpi-non-collective-4-4-1600-50it");
//		projectNames.add("partdiff-mpi-non-collective-4-4-1600-boundary");
//		projectNames.add("partdiff-mpi-non-collective-4-4-1800");
//		projectNames.add("partdiff-mpi-non-collective-4-4-800");
//		projectNames.add("partdiff-mpi-non-collective-8-0-1600");
//		projectNames.add("partdiff-mpi-non-collective-8-0-1600-100it");
//		projectNames.add("partdiff-mpi-non-collective-8-0-1600-200it");
//		projectNames.add("partdiff-mpi-non-collective-8-0-1600-500it");
//		projectNames.add("partdiff-mpi-non-collective-8-4-1600");
//		projectNames.add("partdiff-mpi-non-collective-8-4-1600-boundary");
//		projectNames.add("partdiff-mpi-non-collective-8-4-1800");
//		projectNames.add("stresstest-mpi-non-collective-2-0-0-stresstest");
		

		projectNames.add("partdiff-mpi-collective-1-1-800");
		projectNames.add("partdiff-mpi-collective-2-2-1100");
		projectNames.add("partdiff-mpi-collective-2-2-800");
		projectNames.add("partdiff-mpi-collective-3-1-1400");
		projectNames.add("partdiff-mpi-collective-4-1-1600");
		projectNames.add("partdiff-mpi-collective-4-2-800");
		projectNames.add("partdiff-mpi-collective-4-4-800");

		File folder = new File("/home/quark/studium/masterthesis/traces");

		File[] files = folder.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				File[] directoryFiles = file.listFiles();
				String projectFile = "";
				String projectName = "";

				List<String> energyHosts = new ArrayList<String>();
				List<String> performanceHosts = new ArrayList<String>();
				for (File directoryFile : directoryFiles) {
					if (directoryFile.getName().endsWith(".proj")) {
						projectFile = directoryFile.getAbsolutePath();
						projectName = file.getName();
					} else if (directoryFile.getName().endsWith("Energy.stat")) {
						energyHosts.add(directoryFile.getName().split("_")[1]);
					} else if (directoryFile.getName().endsWith(
					"Performance.stat")) {
						performanceHosts
						.add(directoryFile.getName().split("_")[1]);
					}
				}

				

				if (!excludeProjectNames.contains(projectName) && (projectNames.size() == 0 || projectNames.contains(projectName))) {

					try {

						String[] setup = projectName.split("-");
						
						int firstSetup = 3;

						int mpi = Integer.parseInt(setup[firstSetup]);
						int pvfs = Integer.parseInt(setup[firstSetup+1]);
						int interlines = Integer.parseInt(setup[firstSetup+2]);
						String special = "";

						if(setup.length == firstSetup+4)
							special = setup[firstSetup+3];
						
						HDTraceImporter reader = new HDTraceImporter();
						reader.setFilename(projectFile);

						BigDecimal originalSum = new BigDecimal("0");
						BigDecimal replaySum = new BigDecimal("0");

						System.out.println(projectName);
						
						if(projectName.equals("stresstest-mpi-non-collective-2-0-0-stresstest")) {
							performanceHosts.clear();
							performanceHosts.add("node07");
							mpi = 1;
						}

						Collections.sort(performanceHosts);

						PowerSupply powerSupply = new PowerSupply();

						powerSupply.setProcentualOverhead(new BigDecimal("0.35"));

						for (String hostname : performanceHosts) {
							try {
								ExtendedNode node = NodeFactory.createExtendedNode(nameToACPIDeviceMapping, hostname,new BigDecimal("6.305"));
								node.setPowerSupply(powerSupply);
								reader.addNode(node);
							} catch (BuildException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						Map<Node, BigDecimal> nodePowerConsumption = null;
						try {
							nodePowerConsumption = reader
							.getOriginalPowerConsumption();
						} catch (HDTraceImporterException e) {
							System.err.println("Not possible to get original consumption for project file: " + projectFile);
							//						e.printStackTrace();
							fail(e.getMessage());
							return;
						}

						try {
							reader.setUtilization(componentNames);
						} catch (HDTraceImporterException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						Map<ACPIDevice, DeviceData> data = reader.getDeviceData();

						List<ReplayItem> items = new ArrayList<ReplayItem>();

						int countValues = 0;

						for (ACPIDevice device : data.keySet()) {
							ReplayItem item = new ReplayItem();
							ReplayDevice replayDevice = new ReplayDevice();
							replayDevice.setACPIDevice(device);
							DeviceData deviceData = data.get(device);
							countValues = deviceData.getCountValues();
							replayDevice.setDeviceData(deviceData);
							item.setReplayDevice(replayDevice);
							item.setPlayStrategy(new SimplePlayStrategy());
							items.add(item);
						}

						Replay replay = new Replay();

						replay.setReplayItems(items);
						replay.setStepsize(1000);
						replay.setCountSteps(countValues);
						try {
							// play with simple strategy

							replay.play();

							int i=0;
							BigDecimal[] powerConsumption = new BigDecimal[replay.getNodes().size()];
							for(Node node : replay.getNodes())
								powerConsumption[i++] = node.getEnergyConsumption();

							powerConsumptions.put("EstimatedSimple", powerConsumption);

							// play with optimal strategy

							replay.reset();
							replay.setPlayStrategy(new OptimalPlayStrategy());
							replay.play();

							i=0;
							powerConsumption = new BigDecimal[replay.getNodes().size()];
							for(Node node : replay.getNodes())
								powerConsumption[i++] = node.getEnergyConsumption();

							powerConsumptions.put("EstimatedOptimal", powerConsumption);

							// play with approach strategy

							replay.reset();
							replay.setPlayStrategy(new ApproachPlayStrategy());
							replay.play();

							i=0;
							powerConsumption = new BigDecimal[replay.getNodes().size()];
							for(Node node : replay.getNodes())
								powerConsumption[i++] = node.getEnergyConsumption();

							powerConsumptions.put("EstimatedApproach", powerConsumption);
							
							// play with optimal devices

							replay.reset();
							replay.setPlayStrategy(new SimplePlayStrategy());
							
							replay.play();

							i=0;
							powerConsumption = new BigDecimal[replay.getNodes().size()];
							for(Node node : replay.getNodes())
								powerConsumption[i++] = node.getEnergyConsumption();

							powerConsumptions.put("EstimatedEfficientDevices", powerConsumption);

						} catch (ReplayException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						writer.write(interlines + " & " + special + " & " + pvfs + " & " + mpi + " & ");

						System.out.print("Strategy\t|");
						if(!printOnlySum)
							for (Node node : nodePowerConsumption.keySet()) 
								System.out.print(node.getName() + "\t\t\t|");
						System.out.println("sum\t\t\t| deviance total\t| deviance % ");

						System.out.print("original\t");
						for (Node node : replay.getNodes()) {
							if(nodePowerConsumption != null && nodePowerConsumption.get(node) != null) {
								originalSum = BaseCalculation.sum(originalSum, nodePowerConsumption.get(node));
								if(!printOnlySum)
									System.out.print(formattedString(nodePowerConsumption.get(node)) + "\t");
							}
						}

						System.out.println(originalSum);
						if(originalSum.compareTo(BigDecimal.ZERO) > 0) {
							writer.write(formattedString(originalSum));
						} else {
							writer.write("-");
						}
						writer.write(" & ");


						for(String strategy : new String[]{"Simple  ","Optimal  ","Approach", "EfficientDevices"}) {
							replaySum = new BigDecimal("0");
							System.out.print(strategy + "\t");
							for(BigDecimal powerConsumption : powerConsumptions.get("Estimated" + strategy.trim())) {
								if(!printOnlySum)
									System.out.print(powerConsumption + "\t");
								replaySum = BaseCalculation.sum(replaySum,powerConsumption);
							}

							System.out.print(formattedString(replaySum) + "\t");
							writer.write(formattedString(replaySum));
							if(originalSum.compareTo(BigDecimal.ZERO) > 0) {
								BigDecimal deviance = BaseCalculation.substract(replaySum, originalSum).abs();
								BigDecimal deviancePercent = BaseCalculation.multiply(BaseCalculation.divide(deviance, originalSum),BaseCalculation.HUNDRED);
								System.out.println(formattedString(deviance) + "\t" + formattedString(deviancePercent));
								writer.write(" & " + formattedString(deviancePercent));
								
								List<BigDecimal> statisticList = statistics.get(strategy);
								if(statisticList == null)
									statisticList = new ArrayList<BigDecimal>();
								
								statisticList.add(deviancePercent);
								statistics.put(strategy, statisticList);
								
							} else {
								writer.write(" & ");
								System.out.println();
							}
							
							if(!strategy.trim().equals("EfficientDevices"))
								writer.write(" & ");

							if(strategy.trim().equals("Simple"))
								originalSum = replaySum;
						}

						for (int i = 0; i < 80; ++i)
							System.out.print("=");

						System.out.println();

						writer.write("\\\\\n");


					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		try {
			writer.write("\\midrule\n");
			
			writer.write("& & & & & & Average: ");
			
			for(String strategy : new String[]{"Simple  ","Optimal  ","Approach", "EfficientDevices"} ) {
				List<BigDecimal> statisticList = statistics.get(strategy);
				if(statisticList != null) {
					BigDecimal sum = new BigDecimal("0");
					for(BigDecimal item : statisticList)
						sum = BaseCalculation.sum(sum,item);
					BigDecimal average = BaseCalculation.divide(sum, new BigDecimal(statisticList.size()));
					if(strategy.equals("EfficientDevices"))
						writer.write("& " + formattedString(average) + "\\\\\n");
					else
						writer.write("& " + formattedString(average) + "& ");
				}
			}
			
			
			writer.write("\\bottomrule\n\\end{tabular}\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String formattedString(BigDecimal bigDecimal) {
		String s = bigDecimal.toString();
		
		if(s.length() > 8) {
			s = s.substring(0,6);
		}
		
		return s;
	}
}
