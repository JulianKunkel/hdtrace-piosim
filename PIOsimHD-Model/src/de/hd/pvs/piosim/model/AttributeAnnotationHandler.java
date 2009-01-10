
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
package de.hd.pvs.piosim.model;

import java.lang.reflect.Field;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.annotations.ChildComponents;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegative;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.inputOutput.MPIFile;
import de.hd.pvs.piosim.model.program.Communicator;
import de.hd.pvs.piosim.model.util.Epoch;
import de.hd.pvs.piosim.model.util.Numbers;
import de.hd.pvs.piosim.model.util.XMLutil;

/**
 * This class provides methods to read/write and verify common attributes.
 * 
 * @author Julian M. Kunkel
 *
 */
public class AttributeAnnotationHandler {

	/**
	 * This method reads the attributes from the XML based on the <code>Attribute</code> annotation,
	 * it can be used to read primitive data types and a few derived data types.
	 * Therefore it walks through the object inheritance hierarchy.
	 * 
	 * @param xml
	 * @param component
	 */
	public static void readSimpleAttributes(Element xml, Object object) throws Exception{
		Class<?> classIterate = object.getClass();	
		
		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();
			
			for (Field field : fields) {
				if( ! field.isAnnotationPresent(Attribute.class))
					continue;
				
				Attribute annotation = field.getAnnotation(Attribute.class);
				
				// the name of the attribute or tag can be set explicitly in Attribute.
				String name = annotation.xmlName().length() > 0 ? annotation.xmlName() : field.getName();
				
				Node node;
				String stringAttribute = null;
				
				if (annotation.type() == AttributeXMLType.ATTRIBUTE){
					node = xml.getAttributes().getNamedItem(name);
					if (node == null)
						continue;
					
					stringAttribute = node.getNodeValue();
				}else{ // TAG
					node = XMLutil.getFirstElementByTag(xml, name);
					if (node == null)
						continue;
					
					stringAttribute = node.getTextContent();					
				}
				
				Class<?> type = field.getType();
				Object value = null;
				
				if (type == int.class || type == Integer.class) {
					value= (int) Numbers.getLongValue(stringAttribute);
				}else if (type == long.class || type == Long.class) {
					value =  Numbers.getLongValue(stringAttribute) ;
				}else if (type == Epoch.class){
					value = new Epoch(Numbers.getDoubleValue(stringAttribute));
				}else if (type == String.class){
					// do nothing
					value = stringAttribute;
				}else if (type.isEnum()) {
					Class<? extends Enum> eType = (Class<? extends Enum>) type;
					value = Enum.valueOf(eType, stringAttribute);					
				}else {
					System.err.println("ModelXMLReader not configured: " + type.getCanonicalName() + " for field " + field.getName() + " type " + object.getClass());
					System.exit(1);
				}
				field.setAccessible(true);
				field.set(object, value);				
				field.setAccessible(false);
			}
			
			classIterate = classIterate.getSuperclass();
		}
	}
	

	/**
	 * Create attributes and tags for the object based on the <code>Attribute</code> annotations.
	 * 
	 * @param obj The object which should be serialized to XML.
	 * @param tags StringBuffer for the Tags
	 * @param attributes StringBuffer for the attributes. Null if no attributes should be read.
	 * @throws Exception
	 */
	public static void writeSimpleAttributeXML(Object obj, StringBuffer tags, StringBuffer attributes) throws Exception{
		Class<?> classIterate = obj.getClass();	
		// Walk through the whole inheritance tree.
		
		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();		
			for (Field field : fields) {
				if( ! field.isAnnotationPresent(Attribute.class))
					continue;

				Attribute annotation = field.getAnnotation(Attribute.class);

				String name = annotation.xmlName().length() > 0 ? annotation.xmlName() : field.getName();

				Class<?> type = field.getType();
				Object value = null;

				field.setAccessible(true);
				value = field.get(obj);				
				field.setAccessible(false);		

				String stringValue = "";

				if (type == int.class) {
					stringValue = Numbers.getNiceString(((Integer) value).longValue());
				}else if (type == long.class) {
					stringValue = Numbers.getNiceString((Long) value);
				}else if (type == Epoch.class){
					stringValue = value.toString();
				}else if (type == String.class){
					// do nothing
					stringValue = (String) value;
				}else if (type.isEnum()) {
					if(value == null) {
						throw new IllegalArgumentException("Enum " + type + " is null, invalid! ");
					}
					stringValue = value.toString();					
				}else {
					System.out.println("ModelXMLWriter not configured: " + type.getCanonicalName());
					System.exit(1);
				}


				if (annotation.type() == AttributeXMLType.ATTRIBUTE){
					if (stringValue != null && attributes != null)
						attributes.append(" " + name + "=\"" + stringValue + "\"");
				}else{ // TAG
					name = name.substring(0, 1).toUpperCase() + name.substring(1);

					tags.append("<" + name + ">" + stringValue);					
					tags.append("</" + name + ">\n");
				}
			}

			classIterate = classIterate.getSuperclass();
		}
	}
	
	

	/**
	 * Check the consistency of a component, are all attributes in valid ranges?
	 * 
	 * @param obj
	 * @throws Exception
	 */
	static public void checkAttributeConsistency(Object obj, boolean isTemplate) throws Exception{		
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
							appendVerificationError("null", obj, field.getName(), errorMessage);
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
					appendVerificationError("null", obj, name, errorMessage);
				}
				
				
				if (type == int.class || (type == long.class)) {
					long val;
					if (type == long.class) {
						val = (Long) value;
					}else{
						val =  ((Integer) value);
					}
					
					if(field.isAnnotationPresent(NotNegative.class) && val < 0){
						appendVerificationError("negative", obj, name, errorMessage);
					}
					if(field.isAnnotationPresent(NotNegativeOrZero.class) && val <= 0){
						appendVerificationError("negative or zero", obj, name, errorMessage);
					}
				}else if (type == Epoch.class){
					Epoch val = (Epoch) value;
					
					
					if(field.isAnnotationPresent(NotNegativeOrZero.class) && val.getDouble() < 0){
						appendVerificationError("negative",obj, name, errorMessage);
					}
				}else if (type == String.class){
					// do nothing.
				}else if (type.isEnum()) {
					// check if not null.
					if( value == null ){
						appendVerificationError("null", obj, name, errorMessage);
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
	static private void appendVerificationError(String error, Object obj, String field, StringBuffer stringbuffer){
		stringbuffer.append(" value in field \"" + field + "\" is " +  error + "\n");
	}
	
	
}
