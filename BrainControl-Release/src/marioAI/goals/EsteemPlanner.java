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
/**
 * Implementation of a planner to generate goals to increase esteem
 * i.e. planning for other players to help them by proposing a goal Plan
 * @author Johannes, Katrin
 *
 */
import java.util.Observable;
import java.util.Random;

import marioAI.agents.CAEAgent;
import marioAI.run.PlayHook;
import marioWorld.agents.Agent;


public class EsteemPlanner extends GoalPlanner {

	private ReservoirMultiplePlanner copy;
	public CAEAgent otherAgent;

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	protected String getGoalSelectionFailureIdentifier() {
		return "PLANNERTYPE(ESTEEM)";
	}

	/**
	 * The planningPlayer plans goals for anotherPlayer according to their
	 * motivations
	 */
	@Override
	protected ArrayList<Goal> selectGoal() {
		ArrayList<Goal> ret = new ArrayList<Goal>();
		
		if(PlayHook.agents.size()<=1)
			return ret;
		
		int currentPlayerIndex = this.agent.getPlayerIndex();
		//List of Agents that are active
		ArrayList<Agent> agents = PlayHook.agents;
		

		//Sampling a random Player except our planning agent
		Random random = new Random();
		int index = random.nextInt((PlayHook.agents.size()));
		while(agent.getPlayerIndex() == index){
			index = random.nextInt((agents.size()));
		}
		otherAgent = (CAEAgent) agents.get(index);
		
		
		// if otherPlayer is in close proximity start goalSelection process
		if (agent.getPlayerPosition().getX()
				- otherAgent.getPlayerPosition().getX() <= 150){
			
			System.out.println(agent.getName() + " now Helping " + otherAgent.getName());
			
			//"Copying" ReservoirMultiplePlanner of other Player with planningAgents 
			//LevelSceneAdapter(i.e. Knowledge) 
			//and other Player as Agent for correct Effects in the ActionEffectPlanners 
			ReservoirMultiplePlanner planner = (ReservoirMultiplePlanner) otherAgent.getPlanner();
			copy = planner.softCopy(this.levelSceneAdapter);
			
			//Selecting goals according to otherAgents Motivations
			//if he currently has no motivations a random Reservoir (except Esteem or Curiosity) is sampled 
			ArrayList<Goal> goals = copy.selectGoalRegardlessOfActivePlanning();
			
			
			if(!goals.isEmpty()) {
				ret.addAll(goals);
			}	
			else{
				System.out.println("No goal could be selected");
			}
		} 				
		else{
			System.out.println("Other Player not reachable");
		}
	

	return ret;
	}

}
