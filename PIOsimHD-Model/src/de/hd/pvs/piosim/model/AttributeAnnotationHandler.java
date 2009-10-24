
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
package de.hd.pvs.piosim.model;

import java.lang.reflect.Field;

import de.hd.pvs.TraceFormat.util.Epoch;
import de.hd.pvs.TraceFormat.util.NumberPrefixes;
import de.hd.pvs.TraceFormat.xml.XMLTag;
import de.hd.pvs.piosim.model.annotations.Attribute;
import de.hd.pvs.piosim.model.annotations.AttributeXMLType;
import de.hd.pvs.piosim.model.annotations.SerializeChild;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegative;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNegativeOrZero;
import de.hd.pvs.piosim.model.annotations.restrictions.NotNull;
import de.hd.pvs.piosim.model.components.superclasses.BasicComponent;
import de.hd.pvs.piosim.model.interfaces.IExtendedXMLHandling;
import de.hd.pvs.piosim.model.interfaces.ISerializableObject;

/**
 * This class provides methods to read/write and verify common attributes.
 * This class can be extended to allow to specify methods for verification,
 * XML read/write routines which are
 * not covered by the <code>AttributeAnnotationHandler</code>
 *
 * @author Julian M. Kunkel
 *
 */
public class AttributeAnnotationHandler {

	/**
	 * The default XML Type
	 */
	protected AttributeXMLType defaultXMLType = AttributeXMLType.TAG;

	/**
	 * Set the XML Type which is normally used for the attributes.
	 * @param defaultXMLType
	 */
	public void setDefaultXMLType(AttributeXMLType defaultXMLType) {
		this.defaultXMLType = defaultXMLType;
	}

	/**
	 * Generate a valid object from the string.
	 * @param type
	 * @param what
	 * @return
	 */
	public Object parseXMLString(Class<?> type, String what) throws IllegalArgumentException{
		System.out.println("AttributeAnnotationHandler not configured for type: " + type.getCanonicalName() + "\n");
		System.exit(1);
		return null;
	}

	/**
	 * Write a object to a string.
	 * @param type
	 * @param obj
	 * @return
	 */
	public String toXMLString(Class<?> type, Object value) throws IllegalArgumentException{
		System.out.println("AttributeAnnotationHandler not configured for type: " + type.getCanonicalName() + "\n");
		System.exit(1);
		return null;
	}

	/**
	 * Verify the consistency of a given field with a given value.
	 * @param field
	 * @param value
	 * @param errorMessageBuffer
	 */
	public void verifyConsistency(Field field, Object value, StringBuffer errorMessageBuffer) throws IllegalArgumentException{
		System.out.println("AttributeAnnotationHandler not configured for type: " + field.getType().getCanonicalName()  + "\n");
		System.exit(1);
	}

	/**
	 * This method reads the attributes from the XML based on the <code>Attribute</code> annotation,
	 * it can be used to read primitive data types and a few derived data types.
	 * Therefore it walks through the object inheritance hierarchy.
	 *
	 * @param xml
	 * @param component
	 */
	final public void readSimpleAttributes(XMLTag xml, ISerializableObject object) throws Exception{
		Class<?> classIterate = object.getClass();

		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();

			for (Field field : fields) {
				if( ! field.isAnnotationPresent(Attribute.class))
					continue;

				Attribute annotation = field.getAnnotation(Attribute.class);

				// the name of the attribute or tag can be set explicitly in Attribute.
				final String name = annotation.xmlName().length() > 0 ? annotation.xmlName() : field.getName();
				String stringAttribute = null;

				final AttributeXMLType xmlTyp = annotation.type() == AttributeXMLType.DEFAULT ? defaultXMLType : annotation.type();

				if (xmlTyp == AttributeXMLType.ATTRIBUTE){
					stringAttribute = xml.getAttribute(name);
					if (stringAttribute == null)
						continue;
				}else if (xmlTyp == AttributeXMLType.TAG){ // TAG
					XMLTag node = xml.getFirstNestedXMLTagWithName(name);
					if (node == null)
						continue;

					stringAttribute = node.getContainedText();
				}

				Class<?> type = field.getType();
				Object value = null;

				if (type == int.class || type == Integer.class) {
					value= (int) NumberPrefixes.getLongValue(stringAttribute);
				}else if (type == boolean.class ) {
					value = Boolean.getBoolean(stringAttribute);
				}else if (type == long.class || type == Long.class) {
					value =  NumberPrefixes.getLongValue(stringAttribute) ;
				}else if (type == Epoch.class){
					value = new Epoch(NumberPrefixes.getDoubleValue(stringAttribute));
				}else if (type == String.class){
					// do nothing
					value = stringAttribute;
				}else if (type.isEnum()) {
					Class<? extends Enum> eType = (Class<? extends Enum>) type;
					value = Enum.valueOf(eType, stringAttribute);
				}else {
					value = parseXMLString(type, stringAttribute);
				}
				field.setAccessible(true);
				field.set(object, value);
				field.setAccessible(false);
			}

			classIterate = classIterate.getSuperclass();
		}


		// next, if we implement the IExtendedXMLHandling interface, then use it.
		if(IExtendedXMLHandling.class.isInstance(object)){
			final IExtendedXMLHandling extXMLHandling = (IExtendedXMLHandling) object;
			extXMLHandling.readXML(xml);
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
	final public void writeSimpleAttributeXML(ISerializableObject obj, StringBuffer tags, StringBuffer attributes) throws Exception{
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
					stringValue = NumberPrefixes.getNiceString(((Integer) value).longValue());
				}else if (type == long.class) {
					stringValue = NumberPrefixes.getNiceString((Long) value);
				}else if (type == boolean.class ) {
					stringValue = value.toString();
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
					stringValue = toXMLString(type, value);
				}

				AttributeXMLType xmlTyp = annotation.type() == AttributeXMLType.DEFAULT ? defaultXMLType : annotation.type();

				if (xmlTyp == AttributeXMLType.ATTRIBUTE){
					if (stringValue != null && attributes != null)
						attributes.append(" " + name + "=\"" + stringValue + "\"");
				}else if(xmlTyp == AttributeXMLType.TAG){
					name = name.substring(0, 1).toUpperCase() + name.substring(1);

					tags.append("<" + name + ">" + stringValue);
					tags.append("</" + name + ">\n");
				}
			}

			classIterate = classIterate.getSuperclass();
		}


		// next, if we implement the IExtendedXMLHandling interface, then use it to read specific attributes.
		if(IExtendedXMLHandling.class.isInstance(obj)){
			final IExtendedXMLHandling extXMLHandling = (IExtendedXMLHandling) obj;
			extXMLHandling.writeXML(tags);
		}
	}



	/**
	 * Check the consistency of a component, are all attributes in valid ranges?
	 *
	 * @param obj
	 * @throws Exception
	 */
	final public void checkAttributeConsistency(Object obj, boolean isTemplate) throws Exception{
		Class<?> classIterate = obj.getClass();

		// all errors are written to this StringBuffer.
		StringBuffer errorMessage = new StringBuffer();

		while(classIterate != Object.class) {
			Field [] fields = classIterate.getDeclaredFields();
			for (Field field : fields) {

				if (! isTemplate) {
					if (field.isAnnotationPresent(SerializeChild.class)) {
						// this field should contain a reference to the parent component.
						Object value = null;

						field.setAccessible(true);
						value = field.get(obj);
						field.setAccessible(false);

						// a child must be set, always
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
				}else if (type == boolean.class){
					// TODO
				}else {
					verifyConsistency(field, value, errorMessage);
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
	protected void appendVerificationError(String error, Object obj, String field, StringBuffer stringbuffer){
		stringbuffer.append(" value in field \"" + field + "\" is " +  error + "\n");
	}


}
