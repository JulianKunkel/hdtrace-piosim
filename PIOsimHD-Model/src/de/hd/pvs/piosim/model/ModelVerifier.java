
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

package de.hd.pvs.piosim.model;


import java.lang.reflect.Field;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegative;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.Program;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;
import de.hd.pvs.piosim.model.util.Epoch;

/**
 * This class implements some simple methods to check the consistency of a model.
 * A model component must have valid values for each field.
 * 
 * Uses reflection to read fields of the objects and compares the annotations.
 * 
 * @author Julian M. Kunkel
 *
 */
public class ModelVerifier {
	
	/**
	 * Automatically verifies if this model is valid.
	 * In case one or multiple invalid states are present only one exception is thrown.
	 * 
	 */
	public void checkConsistency(Model model) throws Exception{
		boolean err = false;
		
		// for each BasicComponent check if it is valid:
		for (BasicComponent c: model.getCidCMap().values()){
			try { 
				checkConsistency(c, false);
			}catch(IllegalArgumentException e) {
				//e.printStackTrace();
				System.err.print(e.getMessage());
				err = true;
			}			
		}
		
		// TODO application checks on demand?
		for(Application app: model.getApplicationNameMap().values()) {
			try { 
				checkConsistency(app);  
			}catch(IllegalArgumentException e) {
				//e.printStackTrace();
				System.err.print(e.getMessage());
				err = true;
			}			
		}
		
		if (err){
			throw new IllegalArgumentException("Model contains errors!");
		}
	}
	
	/**
	 * Check the consistency of a component, are all attributes in valid ranges?
	 * 
	 * @param comp
	 * @throws Exception
	 */
	public void checkConsistency(BasicComponent comp, boolean isTemplate) throws Exception{
		checkAttributeConsistency(comp, isTemplate);		
	}
	
  /**
   * Check the correctness of an application
   * 
   * @param app
   * @throws Exception
   */
	public void checkConsistency(Application app) throws Exception{		
		for (int p= 0; p < app.getProcessCount();  p++){
			boolean err = false;
			
			Program program = app.getClientProgram(p);
			for(Command cmd: program.getCommands()){
				try{
					checkAttributeConsistency(cmd, false);
				}catch(Exception e){
					System.err.print(cmd + " " + e.getMessage());
					err = true;
				}
			}

			if (err){
				throw new IllegalArgumentException("Application: " + app.getName() +  " rank " + p +  "  contains errors!");
			}		
		}
	}
	
	/**
	 * Check the consistency of a component, are all attributes in valid ranges?
	 * 
	 * @param obj
	 * @throws Exception
	 */
	private void checkAttributeConsistency(Object obj, boolean isTemplate) throws Exception{		
		Class<?> classIterate = obj.getClass();	
		
		// all errors are written to this StringBuffer.
		StringBuffer errorMessage = new StringBuffer();
		
		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();		
			for (Field field : fields) {
				
				if (! isTemplate) {
					if (field.isAnnotationPresent(ChildComponents.class)) {
						// this field should contain a reference to the parent component.
						Object value = null;
						
						field.setAccessible(true);
						value = field.get(obj);				
						field.setAccessible(false);		
						
						if(value == null) {
							appendError("null", obj, field.getName(), errorMessage);
						}
						continue;
					}
				}
				
				if( ! field.isAnnotationPresent(Attribute.class))
					continue;
				
				//Attribute annotation = field.getAnnotation(Attribute.class);
				
				String name = field.getName();
				
				Class<?> type = field.getType();
				Object value = null;
				
				field.setAccessible(true);
				value = field.get(obj);				
				field.setAccessible(false);		
				
				if( field.isAnnotationPresent(NotNull.class) && value == null){
					appendError("null", obj, name, errorMessage);
				}
				
				
				if (type == int.class || (type == long.class)) {
					long val;
					if (type == long.class) {
						val = (Long) value;
					}else{
						val =  ((Integer) value);
					}
					
					if(field.isAnnotationPresent(NotNegative.class) && val < 0){
						appendError("negative", obj, name, errorMessage);
					}
					if(field.isAnnotationPresent(NotNegativeOrZero.class) && val <= 0){
						appendError("negative or zero", obj, name, errorMessage);
					}
				}else if (type == Epoch.class){
					Epoch val = (Epoch) value;
					
					
					if(field.isAnnotationPresent(NotNegativeOrZero.class) && val.getDouble() < 0){
						appendError("negative",obj, name, errorMessage);
					}
				}else if (type == String.class){
					// do nothing.
				}else if (type.isEnum()) {
					// check if not null.
					if( value == null ){
						appendError("null", obj, name, errorMessage);
					}
				}else if (type == Communicator.class){
					// TODO
				}else if (type == MPIFile.class){
					// TODO
				}else {
					throw new IllegalArgumentException("ModelVerifier does not know how to handle type (not configured): " + type.getCanonicalName());
				}
				
			}
			
			classIterate = classIterate.getSuperclass();
		}		
		
		if(errorMessage.length() > 0){
			String objname = "";
			if(BasicComponent.class.isAssignableFrom(obj.getClass()) ){
				objname = ((BasicComponent) obj).getIdentifier().toString() + " ";
			}
			objname += "of type " + obj.getClass().getSimpleName();
			
			throw new IllegalArgumentException("Object " + objname + " contains errors:\n" + errorMessage.toString());
		}
	}
	
	/**
	 * Append error message to the StringBuffer
	 * 
	 * @param error
	 * @param obj
	 * @param field
	 */
	private void appendError(String error, Object obj, String field, StringBuffer stringbuffer){
		stringbuffer.append(" value in field \"" + field + "\" is " +  error + "\n");
	}
	
	
}
