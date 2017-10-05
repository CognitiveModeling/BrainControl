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

import java.util.ArrayList;

import marioAI.movement.PlayerAction;

/**
 * Implements trivial functions for a search node.
 */
public class SearchNode {

	private final double estimatedCostToGoal;
	private double costFromStart;
	private SearchNode parent;
	//private int actionId; // the index of the action that was taken from the specified parent to arrive here
	private Action action;
	private ArrayList<SearchNode> children; // nodes generated from this by expansion

	public SearchNode(SearchNode parent, Action action, double actionCost, double estimatedCostToGoal, int numChildren) {
		this.parent = parent;
		//this.actionId = actionId;
		this.action = action;
		costFromStart = parent == null ? 0.0 : parent.costFromStart + actionCost;
		this.estimatedCostToGoal = estimatedCostToGoal;
		//this.children = new SearchNode[numChildren];
		children = new ArrayList<SearchNode>();
		if (parent != null)
		{
			parent.children.add(this);
		}
	}

	// cost from the start node (might decrease if better path to this node is found)
	public double costFromStartNode() {
		return costFromStart;
	}

	// estimated cost to goal  (using heuristic)
	public double estimatedTotalCost() {
		return costFromStart + estimatedCostToGoal;
	}

	public SearchNode getParent() {
		return parent;
	}

//	public int getActionId() {
//		return actionId;
//	}

	public Action getAction() {
		return action;
	}	
	
	public boolean isRoot() {
		return parent == null;
	}

	public void updateRecursively(double otherCostFromStart, SearchNode otherParent, Action otherAction, ArrayList<PlayerAction> possibleActions) {	
		if (otherCostFromStart < costFromStart) {
			costFromStart = otherCostFromStart;
			parent = otherParent;
			//actionId = otherActionId;
			action = otherAction;
			for (int i = 0; i < children.size(); i++) {
				SearchNode child = children.get(i);
				if (child != null) {
					updateRecursively(costFromStart+possibleActions.get(i).getActionCost(), this, action, possibleActions);
				}
			}
		}
	}

}
