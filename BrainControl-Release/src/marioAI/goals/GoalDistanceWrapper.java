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
package marioAI.goals;

import marioAI.goals.Goal;

/**
 * 
 * Simple Wrapper for Goal and effect probability
 * @author Yves
 *
 */
public class GoalDistanceWrapper {
	
	private Goal previousInteraction;
	private double distance;

	public GoalDistanceWrapper(Goal previousInteraction, double distance) {
		this.previousInteraction = previousInteraction;
		this.distance = distance;
	}

	public Goal getPreviousInteraction() {
		return previousInteraction;
	}

	public double getDistance() {
		return distance;
	}
	
}
