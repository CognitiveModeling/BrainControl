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
package marioWorld.engine;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Provides READING access to all fields in the implementing class. Do not abuse - use for debugging purposes only.
 * 
 * @author Stephan
 * 
 */
public interface GenericGetterForAllAttributes {

	/**
	 * This method provides READING access to all fields in this class. Do not abuse - use for debugging purposes only. Please implement this method in the
	 * class which defines the desired attributes - inheriting won't help to get access to private attributes. Exemplary implementation (you can just copy paste
	 * this):
	 * 
	 * Field[] fields = getClass().getDeclaredFields(); //uses java.lang.reflect.Field HashMap<Field, Object> map = new HashMap<Field, Object>(); try{ //loop
	 * over all fields for(Field field : fields) { map.put(field, field.get(this)); //read the value inside the field for the instance given by "this" and put
	 * the value in the map } }catch(IllegalAccessException e) { // Do something } return map;
	 * 
	 * @return
	 */
	public HashMap<Field, Object> getAttributeMap();

}
