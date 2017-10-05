/*******************************************************************************
 * Copyright (C) 2017 Cognitive Modeling Group, University of Tuebingen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * You can contact us at:
 * University of Tuebingen
 * Department of Computer Science
 * Cognitive Modeling
 * Sand 14
 * 72076 Tübingen 
 * cm-sekretariat -at- inf.uni-tuebingen.de
 ******************************************************************************/
package marioAI.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;

/**
 * provides functionality to compare attributes in two objects. If an attribute is an array, a deep comparison is done (i.e. the elements most deep down in the
 * array are compared to the elements in the second array).
 * 
 * @author Stephan
 * 
 */
public abstract class EqualsUtil {

	/**
	 * get the Field with the name fieldName in classBeingSearched. If no such field exists, optionally the superclass is searched, then it's superclass...
	 * 
	 * @param classBeingSearched
	 *            this class' fields are searched for a field with the specified name.
	 * @param fieldName
	 *            this defines for which field we search.
	 * @param remainingSearchDepth
	 *            if 0, no superclasses are tested for the field. If one, only the first superclass, etc.
	 * @return the field
	 * @throws NoSuchFieldException
	 *             if no field matching the fieldName was found in remainingSearchDepth steps.
	 */
	static Field recursiveFieldSearch(Class<?> classBeingSearched, String fieldName, int remainingSearchDepth) throws NoSuchFieldException {
		try {
			return classBeingSearched.getDeclaredField(fieldName); // if field
																	// found:
																	// return.
																	// else: cf.
																	// catch
																	// block
		} catch (NoSuchFieldException e) {
			if (remainingSearchDepth == 0) {
				// we have exceeded the allowed search depth. We don't want to
				// consider any more superclasses.
				throw e;
			} else {
				// maybe the field is declared in a superclass. Check via
				// recursive call
				return recursiveFieldSearch(classBeingSearched.getSuperclass(), fieldName, remainingSearchDepth - 1);
			}
		}
	}

	/**
	 * compares two objects. If they are arrays (of arbitrary dimension), they are compared at the deepest level
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean deepEqual(Object o1, Object o2) 
	{
		if(o1 == o2)
		{
			return true;
		}
				
		if (o1 instanceof Array && o2 instanceof Array) 
		{
			final int length1 = Array.getLength(o1);
			final int length2 = Array.getLength(o2);
			if (length1 != length2) 
			{
				return false; // different length: break;
			}
			for (int i = 0; i < length1; i++) 
			{
				if (!deepEqual(Array.get(o1, i), Array.get(o2, i))) 
				{ 														// recursive
																		// call:
																		// handles
																		// arbitrary
																		// depth
					return false; // at least one element in the array is
									// unequal: break
				}
			}
			return true; // all elements in the array are equal: finished
							// comparing
		} 
		else if(o1 != null && o2 != null)
		{			
			return o1.equals(o2);
			// deepest depth reached: compare elements
		}		

		
		return false;
	}

	public static String deepToString(Object object) {
		if (object.getClass().isArray()) {
			final String open = "{", close = "}", separator = ",";
			String ret = open;
			final int length = Array.getLength(object);
			for (int i = 0; i < length; i++) {
				ret += deepToString(Array.get(object, i)) + separator;
			}
			return ret + close;
		} else {
			return object.toString();
		}
	}

	/**
	 * Compares two objects. All attributes in the first object are compared to attributes with the same name in the second object. This includes optionally
	 * searching in superclasses. If an attribute is an array, a deep comparison is done (i.e. the elements most deep down in the array are compared to the
	 * elements in the second array).
	 * 
	 * @param first
	 * @param second
	 * @param searchSuperClassDepth
	 *            if 0, no superclasses of second are tested for matching fields. If one, only the first superclass of second, etc.
	 * @param loggerName
	 *            defines to which logger differences should be logged. Use null if you do not wish to log.
	 * @param loggingLevel
	 *            defines at which level of severity differences should be logged.
	 * @return
	 */
	public static boolean equal(Object first, Object second, int searchSuperClassDepth, String loggerName, Level loggingLevel) {
		// use Java reflection to loop over all attributes in first
		Field[] firstFields = first.getClass().getDeclaredFields();
		try {
			for (Field firstField : firstFields) {
				String firstFieldName = firstField.getName();
				/**
				 * get the field of the second object which has the same name as the field in the first object
				 */
				Field secondField = recursiveFieldSearch(second.getClass(), firstFieldName, searchSuperClassDepth);
				/** read the values stored in the fields */
				Object firstFieldValue = null, secondFieldValue = null;
				try {
					firstFieldValue = firstField.get(first);
					secondFieldValue = secondField.get(second);
				} catch (IllegalAccessException e) {
					new Throwable("Trying to read field " + firstFieldName + ". " + e.getMessage()).printStackTrace();
					continue;
				}
				/** compare the values: if they are unequal, alert. */
				if (!deepEqual(firstFieldValue, secondFieldValue)) {
					if (loggerName != null) {
						String difference = "different " + firstFieldName + ". First: " + deepToString(firstFieldValue) + ", second: "
								+ deepToString(secondFieldValue);
						Logging.log(loggerName, difference, loggingLevel);
					}
					return false;
				}
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * This method performs worse than the other equal method, but can compare private attributes if they were read out and passed prior to calling this method.
	 * 
	 * @param typeName
	 *            used for logging purposes only. The class which has all the attributes (e.g. Mario, Luko, ...)
	 * @param firstName
	 *            used for logging purposes only. The first instance
	 * @param secondName
	 *            used for logging purposes only. The second instance
	 * @param firstFieldValueMap
	 * @param secondFieldValueMap
	 * @param loggerName
	 * @param loggingLevel
	 * @param ignoreFields
	 *            Optional list of fields. The fields in firstFieldValueMap and secondFieldValueMap whose fieldName is contained here, are ignored.
	 * @return
	 */
	public static boolean equal(String time, String typeName, String firstName, String secondName, HashMap<Field, Object> firstFieldValueMap,
			HashMap<Field, Object> secondFieldValueMap, String loggerName, Level loggingLevel, List<String> ignoreFields) {
		boolean ret = true;
		for (Entry<Field, Object> firstEntry : firstFieldValueMap.entrySet()) {
			String firstFieldName = firstEntry.getKey().getName();
			for (Entry<Field, Object> secondEntry : secondFieldValueMap.entrySet()) {
				/**
				 * get the field of the second object which has the same name as the field in the first object
				 */
				if (firstFieldName.equals(secondEntry.getKey().getName())) {
					if (!deepEqual(firstEntry.getValue(), secondEntry.getValue())) {
						if (!(ignoreFields != null && ignoreFields.contains(firstFieldName))) {
							if (loggerName != null) {
//								String difference = time + ": Different " + typeName + "." + firstFieldName + ". " + firstName + ": "
//										+ deepToString(firstEntry.getValue()) + ", " + secondName + ": " + deepToString(secondEntry.getValue());
//								Logging.log(loggerName, difference + "\n", loggingLevel);
							}
							ret = false;
						}
					}
				}
			}
		}
		return ret;
	}

	public static boolean equal(PlayerWorldObject[][] map1, PlayerWorldObject[][] map2, String loggerName, Level loggingLevel) {
		boolean equal = deepEqual(map1, map2);
		if (!equal && loggerName != null) {
			String difference = "different static maps";
			Logging.log(loggerName, difference, loggingLevel);
			Logging.logStackTrace(loggerName);
		}
		return equal;
	}
}
