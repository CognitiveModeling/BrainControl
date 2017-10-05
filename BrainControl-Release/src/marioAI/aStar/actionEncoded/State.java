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
package marioAI.aStar.actionEncoded;

import java.math.BigInteger;
import java.util.List;

import marioAI.aStar.AStarNodeVisualizer;

/**
 * Interface for the state of the system after a sequence of actions, provides necessary values to control and continue search expansion
 * @author Jan
 *
 */
public interface State<ACTION extends Action> {
	
	/**
	 * heuristic cost to the goal states (some function > 0, should tend to become smaller when approaching the goal set)
	 * 
	 * @return
	 */
	public double getHeuristicCostToGoalNode();
	
	/**
	 * encodes the relevant details of the state.
	 * when two slightly different nodes shall be treated as equal in AStar, they should return same keycode
	 *
	 * @return a long value suitable for hashing
	 */
	public long getKeyCode();
	
	
	/**
	 * If the states are supposed to be shown visually, provide a suitable visualizer object.
	 * 
	 * @return an object used for visualization purposes
	 */
	public AStarNodeVisualizer getStateVisualizer(double costFromStart);

	
	/**
	 * Creates the next state, assuming a sequence of actions is taken.
	 * 
	 * @param state state in which the action is taken
	 * @return new state after the action has produced its effect
	 */
	public State<ACTION> getSuccessor(List<ACTION> actions);
	
}
