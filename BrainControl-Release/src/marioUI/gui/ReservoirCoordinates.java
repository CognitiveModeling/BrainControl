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
 * 72076 T�bingen 
 * cm-sekretariat -at- inf.uni-tuebingen.de
 ******************************************************************************/
package marioUI.gui;

import java.util.LinkedList;

/**
 * LinkedList where the coordinates of the graph (=ReservoirGuiCoordinates) of a reservoir are saved
 * Please use only addLast() to add new points
 * @author 
 *
 */
public class ReservoirCoordinates extends LinkedList<ReservoirGuiCoordinate>{
	
	private static final long serialVersionUID = 1L;
	/*interval determines how many coordinates are visible*/
	public final int displayedTimeInterval = 200;
	
	@Override
	public void addLast(ReservoirGuiCoordinate point) {
		if(this.size() >= displayedTimeInterval) {
			super.removeFirst();
		}
		super.addLast(point);
	}
}
