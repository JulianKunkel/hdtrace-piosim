
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

/**
 *
 */
package de.hd.pvs.piosim.model.dynamicMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import de.hd.pvs.piosim.model.interfaces.IDynamicImplementationObject;


/**
 * The ClassMapper loads the available implementations and object types for the model and allows
 * the GUI and the
 * simulator to find the appropriate classes.
 *
 * @author Julian M. Kunkel
 *
 */
public class DynamicModelClassMapper  extends DynamicMapper{

	public static class ModelObjectMap{
		final String modelClass;
		final String simulationClass;

		public ModelObjectMap(String mClass, String sClass) {
			this.modelClass = mClass;
			this.simulationClass = sClass;
		}

		/**
		 * @return the modelClass
		 */
		public String getModelClass() {
			return modelClass;
		}

		/**
		 * @return the simulationClass
		 */
		public String getSimulationClass() {
			return simulationClass;
		}
	}

	static private DynamicModelClassMapper instance = null;

	static public void loadConfiguration(boolean loadClasses){
		instance = new DynamicModelClassMapper();
		instance.load(loadClasses);
	}

	/**
	 * For one model object different implementations could exist.
	 */
	HashMap<String, ArrayList<ModelObjectMap>> mapModelType = new HashMap<String, ArrayList<ModelObjectMap>>();

	static public Collection<String> getAvailableModelTypes(){
		return instance.mapModelType.keySet();
	}

	/**
	 * Load the mapping.
	 */
	private void load(boolean loadClasses){
		String componentType = null;

		for(String line: readLines("ModelToSimulationMapper.txt")) {

			if ( line.charAt(0) == '+'){
				componentType = line.substring(1);

				continue;
			}


			String [] splitWords = line.split("=");

			// number of splits needed:
			if(splitWords.length != 2)
				continue;


			if(loadClasses){
				tryToLoadClass(splitWords[0]);
				tryToLoadClass(splitWords[1]);
			}

			addComponentImplementation(componentType, splitWords[0], splitWords[1]);
		}
	}

	/**
	 * Return the available implementations for a given object type.
	 * @param objectType
	 * @return
	 */
	static public Collection<ModelObjectMap> getAvailableComponentImplementations(String objectType) {
		ArrayList<ModelObjectMap> mop = instance.getModelObjectMapFor(objectType);
		return mop;
	}

	/**
	 * Return a specific implementation for a given object type.
	 *
	 * @param objectType
	 * @param modelClassName
	 * @return
	 */
	static public ModelObjectMap getComponentImplementation(String objectType, String modelClassName) {
		if(objectType.contains(".")){
			throw new IllegalArgumentException("Object type should contain only the simple name: " + objectType);
		}

		ArrayList<ModelObjectMap> list = instance.getModelObjectMapFor(objectType);

		for(ModelObjectMap mop:list ) {
			if (mop.getModelClass().equals(modelClassName)) {
				return mop;
			}
		}

		throw new IllegalArgumentException("Did not find the object for the model class with name: " + modelClassName);
	}

	/**
	 * Return the implementation for the specified object type.
	 *
	 * @param component
	 * @return
	 */
	static public ModelObjectMap getComponentImplementation(IDynamicImplementationObject component) {
		return getComponentImplementation(component.getObjectType(), component.getClass().getCanonicalName());
	}


	private ArrayList<ModelObjectMap> getModelObjectMapFor(String objectType) {
		ArrayList<ModelObjectMap> mop = mapModelType.get(objectType);
		if (mop == null) {
			throw new IllegalArgumentException("Model object type: " + objectType + " not mapped.");
		}
		return mop;
	}

	/**
	 * Allows to extend the model to simulation mapping on the fly. Especially for testing or
	 * online extending.
	 *
	 * @param componentType
	 * @param modelClass
	 * @param simulationClass
	 */
	static public void addComponentImplementation(String componentType, String modelClass, String simulationClass){
		ModelObjectMap mop = new ModelObjectMap(modelClass, simulationClass);

		ArrayList<ModelObjectMap> availableImplementationsForType = null;
		availableImplementationsForType = instance.mapModelType.get(componentType);

		if (availableImplementationsForType == null) {
			availableImplementationsForType = new ArrayList<ModelObjectMap>();
			instance.mapModelType.put(componentType, availableImplementationsForType);
		}

		availableImplementationsForType.add(mop);
	}
}
