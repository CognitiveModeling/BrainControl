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
/**
 * Container-Deque for GoalSearchWrapper which provides additional functions.
 * @author Jonas E.
 */


import java.util.LinkedList;

import marioAI.goals.Goal;

public class GoalDeque extends LinkedList<GoalSearchWrapper> {
	
	private static final long serialVersionUID = -8287758156798901906L;
	
	public GoalDeque(){
		super();
	}
	
	
	/**
	 * checks if a path to the first goal is searched
	 * @return
	 */
	public boolean isSearching(){
		if(this.peekFirst() == null) 
			return false;
		else 
			return this.peekFirst().isSearching();
	}
	
	/**
	 * 
	 * @return returns the first goal that is not intermediate
	 */
	public Goal getFirstGoal(){
		return this.peekFirst().worldGoalTester.goal;
	}
	
	/**
	 * Returns the AStarSearchRunnable of the first element. If this Container is empty the method returns null
	 * @return returns the AStarSearchRunnable of the first element 
	 */
	public AStarSearchRunnable getFirstAStarSearchRunnable(){
		GoalSearchWrapper first = this.peekFirst();
		return first == null ? null : first.getAStarSearchRunnable();
	}

}
