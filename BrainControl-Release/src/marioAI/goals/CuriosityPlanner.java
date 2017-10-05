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

import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
/* 
 * Planner to get Goals the Agents knows nothing about.
 */
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.TreeMap;

import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.Condition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.movement.heuristics.FineGrainPotentialField;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeCoarse;

public class CuriosityPlanner extends GoalPlanner
{

	//LinkedList<PlayerWorldObject> currentlyExploring = new LinkedList<PlayerWorldObject>();

	/*@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		if (arg instanceof TypeOfChange) {
			if (o == agent) {
				TypeOfChange currentType = (TypeOfChange) arg;
				switch (currentType) {
				case AGENT_REACHED_GOAL:
					if (!currentlyExploring.isEmpty())
						currentlyExploring.removeFirst();
					break;
				default:
					break;
				}
			}
		}
	}*/

	@Override
	public ArrayList<Goal> selectGoal() 
	{
		ArrayList<Goal> ret = new ArrayList<Goal>();

		ArrayList<Entry<Goal,Double>> possibleGoals = new ArrayList<Entry<Goal,Double>>(); 
				
		for(CollisionDirection cd : CollisionDirection.values())
			if(cd!=CollisionDirection.IRRELEVANT)
			{
				SimulatedLevelScene scene;
				try {
					scene = (SimulatedLevelScene) levelSceneAdapter.getSimulatedLevelScene().clone();
					scene.updateReachabilityMaps();
					possibleGoals.addAll(PlayerWorldObject.getReachableObjectGoals(scene.getPlanningPlayer(), cd, scene));
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

		Goal minKnowledgeGoal = null;

		int minimum_count=Integer.MAX_VALUE;
		
		// check for all targets Mario knows nothing about
		for (Entry<Goal,Double> goal : possibleGoals)
		{
			//TODO: this is rather dirty
			Condition condition = new Condition(agent.getPlayer().getType(), goal.getKey().getTarget(), goal.getKey().getHealthCondition());
			CollisionDirection direction = goal.getKey().getDirection();
			ConditionDirectionPair cdp = new ConditionDirectionPair(condition, direction);
			
			int count = -1; //completely new objects are prefered. others have 0 conditional interactions (of none observed)
			
			if(agent.getBrain().getKnowledge().containsKey(cdp))
			{
				//get how often the interaction was observed:
				count = agent.getBrain().getKnowledge().ceilingEntry(cdp).getKey().interactionCount;
				System.out.println("Curiosity planner: tried that interaction "+count+" times: " + cdp);				
			}
			else			
				System.out.println("Curiosity planner: this possible interaction is new to me: " + cdp);
			
			if(count<minimum_count)	//this takes the first possible interaction of this kind, should be the one with lowest distance!
			{
				minimum_count=count;
				minKnowledgeGoal=goal.getKey();
			}
		}
				
		System.out.println(minKnowledgeGoal);
				
		//TODO: call effect chaining for all goals, not just for separate planners
		//translate effects to sequences of goals:
		if(minKnowledgeGoal!=null)
		{
			//TODO: this does not call effect chaining right now. not neccessary (nor working!!) since only reachable object interactions are selected. if fixed, select from all goals!
			//return minKnowledgeGoal.translateEffectGoal(this.levelSceneAdapter.getSimulatedLevelScene());
			if(minimum_count < 5) {
				ret.add(minKnowledgeGoal);
			}
			
		}
			
		return ret;
	}

	

//	private LinkedList<PlayerWorldObject> getObjectKnowledge()
//	{
//		LinkedList<PlayerWorldObject> objectKnowledge = new LinkedList<PlayerWorldObject>();
//
//		NavigableSet<ConditionDirectionPair> conditionSet = agent.getBrain().getKnowledge().navigableKeySet();
//
//		for (ConditionDirectionPair cdp : conditionSet) {
//			objectKnowledge.add(cdp.condition.getTarget());
//		}
//		return objectKnowledge;
//	}

//	public CollisionDirection getRandomDirection() {
//		int randomValue = (int) (Math.random() * 4 + 1);
//
//		switch (randomValue) {
//		case 1:
//			return CollisionDirection.LEFT;
//		case 2:
//			return CollisionDirection.ABOVE;
//		case 3:
//			return CollisionDirection.RIGHT;
//		case 4:
//			return CollisionDirection.BELOW;
//		}
//		return null;
//	}

//	private ArrayList<Goal> getAllGoals(PlayerWorldObject[][] observation) 
//	{
//		ArrayList<Goal> possibleGoals = new ArrayList<Goal>();
//		GlobalContinuous marioPos = this.aStarSimulator.getCurrentSimulatedLevelScene().getPlayerPos();	
//
//		SimulatedLevelScene scene = levelSceneAdapter.getSimulatedLevelScene();
//		
///*		PlayerWorldObject[][] map = scene.getStaticLocalMap();
//		Goal newGoal = new Goal(scene.getPlayerPos().toGlobalCoarse(), false);				
//		FineGrainPotentialField heuristicAlgorithm = new FineGrainPotentialField(map, newGoal.createWorldGoalTester(scene), scene.getPlayerPos());
//		double[][] heuristicsMap = heuristicAlgorithm.getMap();*/			
//
//		for (int y = 1; y < observation.length - 1; y++) 
//		{
//			for (int x = 1; x < observation[0].length - 1; x++) 
//			{
//				PlayerWorldObject object = observation[y][x];
//				if (object != null /*&& !currentlyExploring.contains(object)*/) 
//				{
//					ObservationRelativeCoarse relPos = new ObservationRelativeCoarse(x, y);
//					GlobalCoarse globalPos = relPos.toGlobalCoarse(marioPos);
//					
//					for(CollisionDirection direction : CollisionDirection.values())
//					{
//						if(direction==CollisionDirection.IRRELEVANT)
//							continue;
//						
//						if (LearningHelper.isRelevantToLearning(object) && scene.isEasilyReachable(globalPos,direction/*,map,heuristicsMap*/)) 
//						{
//							ObservationRelativeCoarse p = new ObservationRelativeCoarse(x, y);
//							Goal newElement = new Goal(p.toGlobalCoarse(marioPos), object, direction, null, PlayerHealthCondition.fromInt(agent.getPlayer().getNonStaticHealth(),false,false));							
//							possibleGoals.add(newElement);
//						}
//					}
//				}
//
//			}
//		}
//		
//		return possibleGoals;
//	}

	@Override
	protected String getGoalSelectionFailureIdentifier() 
	{
		return "PLANNERTYPE(CURIOSITY)";
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
