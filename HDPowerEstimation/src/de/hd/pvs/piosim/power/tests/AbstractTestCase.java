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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import de.hd.pvs.piosim.power.Time;
import de.hd.pvs.piosim.power.acpi.ACPIDevice;
import de.hd.pvs.piosim.power.acpi.history.ACPIStateChangesHistory;
import de.hd.pvs.piosim.power.data.visualizer.Visualizer;

public abstract class AbstractTestCase extends TestCase {

	private static AbstractTestCase abstractTestCase;
	protected Visualizer testVisualizer;
	protected Logger logger;
	private static String globalVisualizer = "de.hd.pvs.piosim.power.data.visualizer.StepChartVisualizer";
	protected File outputFolder;
	protected File inputFolder;
	private static boolean loggerConfigured = false;
	private Level level;
	
	
	public static void setGlobalVisualizer(String visualizer) {
		globalVisualizer = visualizer;
	}
	
//	@SuppressWarnings("deprecation")
//	private void configureLogger() {
//		String testFolderName = "test-out/" + this.getClass().getSimpleName();
//		String testOutput = testFolderName + "/output";
//		testFolder = new File(testFolderName);
//		testFolder.delete();
//		testFolder.mkdirs();
//		
//		RollingFileAppender fileAppender = null;
//		ConsoleAppender consoleAppender = null;
//		
//		String pattern = "[%t] %-5p %l - %m%n";
//		PatternLayout layout = new PatternLayout(pattern);
//
//		try {
//			fileAppender = new RollingFileAppender(layout,testOutput, false);
//			consoleAppender = new ConsoleAppender(layout);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		logger = Logger.getLogger(this.getClass());
//		logger.addAppender(fileAppender);
//		logger.addAppender(consoleAppender);
//		
//		Logger.getRoot().setLevel(Level.DEBUG);
//	}

	protected AbstractTestCase() {
		abstractTestCase = this;

		//configureLogger();
		
		String testFolderName = "test-out/" + this.getClass().getSimpleName();
		outputFolder = new File(testFolderName);
		outputFolder.delete();
		outputFolder.mkdirs();
		
		inputFolder = new File("test-data");
		
		if(!loggerConfigured) {
			BasicConfigurator.configure();
			loggerConfigured = true;
		}
		
		try {
			testVisualizer = (Visualizer) Class.forName(globalVisualizer).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		try {
			org.junit.runner.JUnitCore.main(((Class<? extends TestCase>) Class
					.forName(abstractTestCase.getClass()
							.getCanonicalName())).getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	@Before
	public void setUp() {
		Time.getInstance().reset();
		ACPIStateChangesHistory.getInstance().reset();
		level = Logger.getRootLogger().getLevel();
	}
	
	@After
	public void tearDown() { 
		ACPIDevice.deregisterAllDevices();
		testVisualizer.reset();
		Logger.getRootLogger().setLevel(level);
	}
	
	/**
	 * fails if left >= right
	 * @param left
	 * @param right
	 */
	public void assertSmaller(BigDecimal left, BigDecimal right) {
		if(!(left.compareTo(right) < 0)) {
			fail(left + " < " + right + " failed.");
		}
			
	}
	
	/**
	 * fails if left > right
	 * @param left
	 * @param right
	 */
	public void assertSmallerOrEqual(BigDecimal left, BigDecimal right) {
		if(!(left.compareTo(right) <= 0)) {
			fail(left + " <= " + right + " failed.");
		}
			
	}
	
	/**
	 * fails if left <= right
	 * @param left
	 * @param right
	 */
	public void assertGreater(BigDecimal left, BigDecimal right) {
		if(!(left.compareTo(right) > 0)) {
			fail(left + " > " + right + " failed.");
		}
			
	}
	
	/**
	 * fails if left < right
	 * @param left
	 * @param right
	 */
	public void assertGreaterOrEqual(BigDecimal left, BigDecimal right) {
		if(!(left.compareTo(right) >= 0)) {
			fail(left + " >= " + right + " failed.");
		}
			
	}
	
	public void assertAllEquals(Object object1, Object object2) {
		List<Method> getters = getGetterMethods(object1.getClass().getMethods());
		
		for(Method method : getters) {
			assertEquals(((BigDecimal)invoke(object1,method)).doubleValue(), ((BigDecimal)invoke(object2,method)).doubleValue());
		}
	}
	
	private String getSimpleName(Method method) {
		if(method.getName().contains("."))
			return method.getName().substring(method.getName().lastIndexOf('.'));
		else
			return method.getName();
	}
	
	private List<Method> getGetterMethods(Method[] methods) {
		List<Method> getters = new ArrayList<Method>();
		
		for(Method method : methods) {
			if(getSimpleName(method).startsWith("get") && !getSimpleName(method).equals("getClass"))
				getters.add(method);
		}
		
		return getters;
	}
	
	private Object invoke(Object clazz, Method method) {
		try {
			return method.invoke(clazz, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

}
