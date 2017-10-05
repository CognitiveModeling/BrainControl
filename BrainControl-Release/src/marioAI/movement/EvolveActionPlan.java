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
package marioAI.movement;

import java.util.ArrayList;
import java.util.List;

public class EvolveActionPlan {


	boolean[][] actions = { { false, true, false, false, false }, // right
			{ true, false, false, false, false }, // left
			{ false, false, false, true, false }, // jump
			{ false, true, false, true, false }, // jump right
			{ true, false, false, true, false }, // jump left
			{ false, false, false, false, false }, // stand still
			{ false, false, true, false, false }, // cower to floor
			{ false, true, false, false, true }, // right & fire
			{ true, false, false, false, true }, // left & fire
			{ false, false, false, true, true }, // jump & fire
			{ false, true, false, true, true }, // jump right & fire
			{ true, false, false, true, true }, // jump left & fire
			{ false, false, false, false, true }, // stand still & fire
			{ false, false, true, false, true } // cower to floor & fire
	};


	/**
	 * Constructor attributes are probably later needed TODO: Brainstorm
	 * necessary attributs
	 */
	public EvolveActionPlan() {
	}

	/**
	 * Generates a random actionplan for ticks timesteps
	 * 
	 * @param ticks
	 *            number of timesteps
	 * @return
	 */
	public List<boolean[]> getRandomActionPlan(int ticks) {
		List<boolean[]> randomActionPlan = new ArrayList<boolean[]>();
		for (int i = 0; i < ticks; i++) {
			randomActionPlan.add(this.getRandomMove());
		}
		return randomActionPlan;
	}

	/**
	 * Generates a random Move in form of a boolean Array with length 5
	 * 
	 * @return
	 */
	public boolean[] getRandomMove() {
		int key = (int) (Math.random() * this.actions.length);

		return actions[key];
	}



}
