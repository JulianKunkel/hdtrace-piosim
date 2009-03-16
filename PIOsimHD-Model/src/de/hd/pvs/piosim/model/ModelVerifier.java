
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

import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.Application;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.program.Program;
import de.hd.pvs.piosim.model.program.ProgramInMemory;
import de.hd.pvs.piosim.model.program.commands.superclasses.Command;

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

	AttributeAnnotationHandler commonAttributeHandler = new AttributeAnnotationHandler(){
		// extend the attribute handler for common types

		@Override
		public void verifyConsistency(Field field, Object value,
				StringBuffer errorMessageBuffer) throws IllegalArgumentException 
				{

			if(field.getType() == Communicator.class){
				return;
			}

			if(field.getType() == MPIFile.class){
				return;
			}

			super.verifyConsistency(field, value, errorMessageBuffer);
				}
	};


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
		commonAttributeHandler.checkAttributeConsistency(comp, isTemplate);		
	}

	/**
	 * Check the correctness of an application, the commands are only checked if the
	 * program is already loaded into memory.
	 * 
	 * @param app
	 * @throws Exception
	 */
	public void checkConsistency(Application app) throws Exception{		
		for (int p= 0; p < app.getProcessCount();  p++){
			boolean err = false;
			final int threadCnt =  app.getProcessThreadCount(p);

			for(int thread = 0 ; thread < threadCnt; thread++){
				Program program = app.getClientProgram(p, thread);
				assert(program != null);			
				if(ProgramInMemory.class.isAssignableFrom(program.getClass())){
					for(Command cmd: ((ProgramInMemory) program).getCommands()){
						try{
							checkConsistency(cmd);
						}catch(Exception e){
							System.err.print(cmd + " " + e.getMessage());
							err = true;
						}
					}
				}
			}

			if (err){
				throw new IllegalArgumentException("Application: " + app.getName() +  " rank " + p +  "  contains errors!");
			}		
		}
	}

	/**
	 * Check the consistency of a single command
	 * @param cmd
	 * @throws Exception
	 */
	public void checkConsistency(Command cmd) throws Exception{
		commonAttributeHandler.checkAttributeConsistency(cmd, false);
	}


}
