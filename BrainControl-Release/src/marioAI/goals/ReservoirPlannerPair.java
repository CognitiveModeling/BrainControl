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

import marioAI.agents.CAEAgent;
import marioAI.reservoirMotivation.AbstractReservoir;

/** Class for a pair of a planner and a reservoir
 * 
 * @author marcel
 *
 */

public class ReservoirPlannerPair 
{
	private GoalPlanner planner;
	private AbstractReservoir reservoir;

	public ReservoirPlannerPair(GoalPlanner planner, AbstractReservoir reservoir) 
	{
		this.planner = planner;
		this.reservoir = reservoir;
	}

	public GoalPlanner getPlanner() 
	{
		return planner;
	}

	public AbstractReservoir getReservoir() 
	{
		return reservoir;
	}
	
	
	public ReservoirPlannerPair copy(){
		System.out.println("Copying in progress ");
		System.out.println(planner.agent.getName());
		return new ReservoirPlannerPair(this.planner, this.reservoir.copy());
		
	}
	
	
}
