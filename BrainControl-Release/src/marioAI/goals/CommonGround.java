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

import java.util.ArrayList;

import marioAI.agents.CAEAgent;
import marioWorld.engine.PlayerWorldObject;

/**
 * The Common Ground between agents contains strategies, which they have already successfully executed together
 * @author Johannes, Kathrin
 *
 */
public class CommonGround {

	protected ArrayList<CAEAgent> agents;
	protected ArrayList<Strategy> workingStrategies = new ArrayList<Strategy>();
	
	/**
	 * Constructor for an empty common ground
	 * @param agents, which share the common
	 */
	public CommonGround(ArrayList<CAEAgent> agents) {
		this.agents = agents;
	}
	
	// returns a strategy from the common ground with goal as final goal - not very useful right now
//	public Strategy getCommonStrategy(GeneralizedGoal goal){
//		if(!workingStrategies.isEmpty()){
//			for (Strategy strategy : workingStrategies) {
//				if(goal.equals(strategy.getFinalGoal())){
//					return strategy;
//				}
//			
//			}
//		}
//		return null;
//	}
	
	/**
	 * @param askingAgent
	 * @return all agents askingAgents shares the common ground with (excluding himself)
	 */
	public ArrayList<CAEAgent> getPartners (CAEAgent askingAgent){
		ArrayList<CAEAgent> otherAgents = new ArrayList<>();
		for (CAEAgent caeAgent : agents) {
			if (! caeAgent.equals(askingAgent)) otherAgents.add(caeAgent);
		}
		return otherAgents;
	}
	
	/**
	 * add workingStrategy to this common ground
	 * @param workingStrategy
	 */
	public void addStrategy(Strategy workingStrategy){
		this.workingStrategies.add(workingStrategy);
	}

	/**
	 * @param strategy
	 * @return true, if this common ground contains strategy
	 */
	public boolean isStored(Strategy strategy) {
		return workingStrategies.contains(strategy); //TODO: Does this work? Is equals implemented equally?
	}
	
	public String toString(){
		String cgString = "Common Ground of ";
		for (CAEAgent caeAgent : agents) {
			cgString += caeAgent.getName() + " ";
		}
		return cgString;
	}
}
