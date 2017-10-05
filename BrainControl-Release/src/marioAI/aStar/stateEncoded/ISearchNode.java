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
package marioAI.aStar.stateEncoded;

import java.util.List;

/**
 * Interface of a search node.
 */
public interface ISearchNode<N> {
	// total estimated cost of the node
	public double totalCost();
	

	// "tentative" g, cost from the start node
	public double costFromStartNode();

	// set "tentative" g
	public void setCostFromStartNode(double g);

	// heuristic cost to the goal node
	public double heuristicCostToGoalNode();

	// costs to a successor
	public double costToSuccessor(N successor);

	// a node possesses or computes his successors
	public List<N> getSuccessors();

	// get parent of node in a path
	public N getParent();

	// returns a key code for the ISearchNode object.
	// which has the relationship
	// (this == other) => (this.keyCode() == other.keyCode())
	// on the other hand, the key code can be the same if the search
	// nodes are not equal, namely when we want to treat
	// different nodes as equal in the AStar (e.g. don't explore nearby regions)
	public long keyCode();

	// set parent
	public void setParent(N parent);
	
	
}
