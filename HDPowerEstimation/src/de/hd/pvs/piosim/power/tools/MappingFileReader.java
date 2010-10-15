package de.hd.pvs.piosim.power.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class MappingFileReader {
	
	private static Logger logger = Logger.getLogger(MappingFileReader.class);

	@SuppressWarnings("unused")
	public static Map<String, String> readNameToACPIDeviceMapping(String filename) throws MappingFileReaderException {

		Map<String, String> nameToACPIDeviceMapping = new HashMap<String, String>();

		try {
			Scanner sc = new Scanner(new File(filename));
			
			String name = null;
			String acpiDevice = null;

			while (sc.hasNext()) {
				if(name == null) {
					name = sc.next();
				} else {
					acpiDevice = sc.next();
					nameToACPIDeviceMapping.put(name,acpiDevice);
					logger.debug("Added new mapping: (" + name + "," + acpiDevice + ")");
					name = null;
					acpiDevice = null;
				}
			}
			
			if(name != null) 
				throw new MappingFileReaderException("Unprocessed argument: " + name);
			
			if(acpiDevice != null)
				throw new MappingFileReaderException("Unprocessed argument: " + acpiDevice);
			
			logger.info("Added " + nameToACPIDeviceMapping.size() + " mapping items");
			
			return nameToACPIDeviceMapping;

		} catch (FileNotFoundException ex) {
			throw new MappingFileReaderException(ex);
		}

	}
}
