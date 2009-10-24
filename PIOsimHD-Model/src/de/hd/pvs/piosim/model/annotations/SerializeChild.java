
 /** Version Control Information $Id: ChildComponents.java 703 2009-10-04 08:23:49Z kunkel $
  * @lastmodified    $Date: 2009-10-04 10:23:49 +0200 (So, 04. Okt 2009) $
  * @modifiedby      $LastChangedBy: kunkel $
  * @version         $Revision: 703 $
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

package de.hd.pvs.piosim.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runtime parseable annotation for fields of components.
 * Allows to easily setup child components inside a component.
 * Signals that the field contains either a collection of BasicComponents or a single BasicComponent.
 *
 * @author Julian M. Kunkel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializeChild {
	/**
	 * Specify a default class to be loaded if the value is not specified
	 * @return
	 */
	// String defaultClass() default "";
	// is that really useful?
}
