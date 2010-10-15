package de.hd.pvs.piosim.power.tests;

import java.util.Map;

import org.junit.Test;

import de.hd.pvs.piosim.power.tools.MappingFileReader;
import de.hd.pvs.piosim.power.tools.MappingFileReaderException;

public class MappingFileReaderTest extends AbstractTestCase {

	@Test
	public void testReading() {
		try {
			Map<String,String> nameToACPIDeviceMapping = MappingFileReader.readNameToACPIDeviceMapping(this.inputFolder + "/eeclust.mapping");
			
			assertEquals(9, nameToACPIDeviceMapping.size());
			
			assertEquals("pvscluster.CPU", nameToACPIDeviceMapping.get("CPU_TOTAL"));
			
			for(int i=0; i<4; ++i)
				assertEquals("pvscluster.CPU", nameToACPIDeviceMapping.get("CPU_TOTAL_" + i));
			
			assertEquals("eeclust.Memory", nameToACPIDeviceMapping.get("MEM_USED"));
			assertEquals("pvscluster.NIC", nameToACPIDeviceMapping.get("NET_OUT_eth0"));
			assertEquals("pvscluster.NIC", nameToACPIDeviceMapping.get("NET_IN_eth0"));
			assertEquals("eeclust.Disk", nameToACPIDeviceMapping.get("HDD_WRITE"));
		
		} catch (MappingFileReaderException e) {
			e.printStackTrace();
			fail(e.getMessage());		
		}
	}
}
