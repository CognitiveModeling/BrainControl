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
package marioAI.movement.tgng;

import java.io.Serializable;
import java.util.ArrayList;

public class TgngAnalysator implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// number of movement for evaluation
	private ArrayList<Integer> steps;
	// save the number of steps done
	private int save;
	// what goal reached 1 = goal reached 0 = goal not reached
	private ArrayList<Integer> result;
	// Constructor
	public TgngAnalysator() {
		steps = new ArrayList<Integer>();
		save = 0;
		result = new ArrayList<Integer>();
	}
	
	// printer function
	public void printOut() {
		for (int i = 0; i < steps.size(); i++) {
			System.out.println(this.steps.get(i) + "   " + this.result.get(i));
		}
	}
	
	// following functions are used in getTgngMove to get necessary data
	
	// increase the amount of movements saved
	public void move() {
		save++;
	}
	// record the data received when TgngNetwork is deactived different functions for
	// recordGoalReached = enemy defeated
	// recordGoalNotReached = enemy escapes

	public void recordGoalReached() {
		this.steps.add(save);
		this.save = 0;
		this.result.add(1);
	}
	
	public void recordGoalNotReached() {
		this.steps.add(save);
		this.save = 0;
		this.result.add(0);
	}
}
