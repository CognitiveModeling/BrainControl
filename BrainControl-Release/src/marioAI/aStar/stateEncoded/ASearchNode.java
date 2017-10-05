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

/**
 * Implements trivial functions for a search node.
 */
public abstract class ASearchNode<N extends ISearchNode<N>> implements ISearchNode<N> {
	private double g = 0.0;

	// total estimated cost of the node
	@Override
	public double totalCost() {
		return this.costFromStartNode() + this.heuristicCostToGoalNode();
	}

	

	// "tentative" g, cost from the start node
	@Override
	public double costFromStartNode() {
		return this.g;
	}

	// set "tentative" g
	@Override
	public void setCostFromStartNode(double g) {
		this.g = g;
	}

	@Override
	public long keyCode() {
		return this.hashCode();
	}

}
