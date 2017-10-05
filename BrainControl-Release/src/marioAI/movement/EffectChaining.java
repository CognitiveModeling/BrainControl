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

import java.util.ArrayList;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.goals.GeneralizedGoal;
import marioAI.goals.Goal;
import marioAI.goals.GoalDistanceWrapper;
import marioAI.goals.PlanningEvent;
import marioAI.goals.PlanningEventType;
import marioWorld.engine.coordinates.GlobalCoarse;

/**
 * Class for Effect-Chaining. Provides functionality for a the search and the
 * creation of intermediate goals if Effect-Chaining is needed.
 * 
 * @author marcel
 *
 */
public class EffectChaining extends Observable
{
	public static final int    OPERATIONCOUNT_THRESHOLD = 500;
	public static final double 	DEPTH_COST_BIAS = Math.log(0.9);
	
	SimulatedLevelScene scene;
	Goal goal;
	ConditionVertex source;
	ArrayList<GeneralizedGoal> generalizedGoalList;
	private AtomicBoolean abort = new AtomicBoolean(false);
	private boolean goalDegeneralizationMode;

	public EffectChaining(SimulatedLevelScene scene, Goal goal) {
		goalDegeneralizationMode = false;
		try{
			this.scene = (SimulatedLevelScene) scene.clone();
		} catch (CloneNotSupportedException e){
			e.printStackTrace();
		}
		this.scene.reachabilityNodes = this.scene.updateReachabilityMaps(this.scene);
		this.goal = goal;
		PlayerHealthCondition playerHealthCondition = PlayerHealthCondition.fromInt(this.scene.getPlanningPlayer().getHealth(), false, false);			
		this.source = new ConditionVertex(playerHealthCondition, this.scene, this.scene.getKnowledge());
	}
	
	public EffectChaining(SimulatedLevelScene scene,ArrayList<GeneralizedGoal> generalizedGoalList,Goal goal){
		this(scene,goal);
		goalDegeneralizationMode = true;
		this.generalizedGoalList = generalizedGoalList;
	}

	private ConditionVertex computePathsDGM(){
		PriorityQueue<ConditionVertex> openSet = new PriorityQueue<ConditionVertex>();
		this.source.distance = 0;
		openSet.add(this.source);
		int operations = 0;
		System.out.println("openset at start: " + openSet);
		while(!openSet.isEmpty() && operations < OPERATIONCOUNT_THRESHOLD){
			System.out.println("operationsloop");
			ConditionVertex u = openSet.poll();
			if(u.depth == generalizedGoalList.size()){
				return u;
			}
			int numberOfAdjacenciesBefore = u.adjacencies.size();
			u.expandNodeDGM(generalizedGoalList.get(u.depth));
			int numberOfAdjacenciesAfter = u.adjacencies.size();
			//if goal is not expandable for the next generalizedGoal in the list
			//the loop has to continue with the next
			if(numberOfAdjacenciesBefore == numberOfAdjacenciesAfter){
				continue;
			} else {
				for (ProbabilityEdge e : u.adjacencies){
					operations++;
					ConditionVertex v = e.target;
					v.prev = u;
					double eventDistance = e.weight;
					double newDistanceToV = u.distance + eventDistance;								
					// check if new likelihood is greater
					openSet.add(v);
				}
			}
			
		}
		return null;
	}
	
	private ConditionVertex computePaths()
	{
		System.out.println("  -----------------------");
		System.out.println("  COMPUTEPATHS IS INVOKED");
		
		//the nodes are stored in a Priorityqueue, so that the node with the least
		//distance is extracted everytime the poll function is called
		PriorityQueue<ConditionVertex> openSet = new PriorityQueue<ConditionVertex>();

		//adding the source node to the PriorityQueue
		this.source.distance = 0;
		openSet.add(this.source);
		System.out.println("  openset initialized with: " + openSet);
		System.out.println("  now entering operationsloop");

		int operations=0;
		while (!abort.get() && !openSet.isEmpty() && operations < OPERATIONCOUNT_THRESHOLD) 	//TODO: as long as this is not threaded... we will have a maximum search complexity.
		{
			System.out.println("    iteration " + operations);
			ConditionVertex u = openSet.poll();
			System.out.println("    poll returned: " + u);
			// if conditions are fulfilled return the respective node
			if (checkConditions(u.scene.getPlanningPlayer().getMapPosition(0), u.effects) && Math.exp(-u.distance) > 0.01)
			{
				System.out.println("    iterationloop terminates");
				System.out.println("  EXPANDNODE RETURNS: " + u);
				System.out.println("  --------------------");
				return u;
			}
			// if conditions are not fulfilled, the node is expanded
			System.out.println("    expanding u with: " + goal);
			System.out.println("    adjacencies before: " +u.adjacencies.size());
			u.expandNode(goal);
			System.out.println("    adjacenceis after: " + u.adjacencies.size());
			System.out.println("    now entering adjacencies loop");
			for (ProbabilityEdge e : u.adjacencies){
				operations++;
				
				ConditionVertex v = e.target;
				double eventDistance = e.weight;
				System.out.print("WEIGHT =" + e.weight);
				System.out.print(" V DISTANCE =" + v.distance);
				System.out.print(" U DISTANCE =" + u.distance);
				System.out.println(" ");
				double newDistanceToV = u.distance + eventDistance - DEPTH_COST_BIAS;		
				
				// check if new likelihood is greater
				if (newDistanceToV < v.distance)
				{						
					openSet.remove(v);
					v.distance = newDistanceToV;
					v.prev = u;
					openSet.add(v);
				}
			}			
		}
		
		System.out.println(operations + " EFFECT CHAINING OPERATIONS PERFORMED (NOT FOUND)!");

		// return null if no possible goal node is reachable
		return null;
	}

	private boolean checkConditions(GlobalCoarse playerPos, TreeMap<Effect, Double> effects){
		System.out.println("      --------------------------------");
		System.out.println("      CHECKCONDITIONS IS INVOKED WITH:");
		System.out.println("      (" + playerPos + "," + effects);
		if (goal.hasEffect() && !goal.getEffect().getType().equals(ActionEffect.PROGRESS)) 
		{	
			//System.out.println("HO "+goal.getEffect());
			if(!effects.isEmpty())
				System.out.println("      " + effects.firstEntry().getKey() + " =? " + goal.getEffect());
			if (effects.containsKey(goal.getEffect())){
				System.out.println("      CHECKCONDITIONS RETURNS: " + true);
				return true;
			} 
			else{
				System.out.println("      CHECKCONDITIONS RETURNS: " + false);
				System.out.println("      ---------------------------------");
				return false;
			}
		}
		else{
		
			System.out.println("");
			System.out.println("DISTANCE: " + playerPos.distance(goal.getPosition()));
			if(playerPos.distance(goal.getPosition())<=1){	//TODO: the position of the target sprite should be checked!
				System.out.println("      CHECKCONDITIONS RETURNS: " + true);
				System.out.println("      ---------------------------------");
				return true;
			}
			else{
				System.out.println("      CHECKCONDITIONS RETURNS: " + false);
				System.out.println("      ---------------------------------");
				return false;
			}
		}
	}
	
	public ArrayList<GoalDistanceWrapper> getGoals() 
	{
		System.out.println("-------------------");
		System.out.println("GETGOALS IS INVOKED");
		ConditionVertex end = this.computePaths();
		System.out.println("computePaths returns: " + end);
		ArrayList<GoalDistanceWrapper> goalDeque = new ArrayList<GoalDistanceWrapper>();
		System.out.println("vertex backtracing invoked: ");
		for (ConditionVertex v = end; v != source && v != null; v = v.prev){
			// create a goal for each node
			//Goal g = new Goal(v.state.getStaticPosition(), v.state.getDirection());
			Goal goalStep = v.previousInteraction;
			
			
			System.out.println("  - added a new goal: " + goalStep + " with effect " + v.importantEffect);
			goalStep.setEffect(v.importantEffect);
			//TODO: THIS DOES NOT!!! WORK SINCE goalDEQUE is actually a LIST(?) of Goals
			//hence the result of the vertex backtracing is inverted
			//possible solution: goalDeque.add(0,goalStep);
			GoalDistanceWrapper goalWrapped = new GoalDistanceWrapper(goalStep, v.distance);
			goalDeque.add(0,goalWrapped);
		}
		System.out.println("GETGOALS RETURNS: " + goalDeque);
		System.out.println("------------------------------\n");
		return goalDeque;
	}
	
	public ArrayList<Goal> getGoalsDGM() 
	{
		System.out.println("------------------------");
		System.out.println("GETGOALSDGM IS INVOKED!!");
		ConditionVertex end = this.computePathsDGM();
		System.out.println("computePathsDGM has returned " + ((end == null)? "nothing" : "something"));
		ArrayList<Goal> goalDeque = new ArrayList<Goal>();
		double strategyProbability = 1.0;
		for (ConditionVertex v = end; v != source && v != null; v = v.prev){
			System.out.println("vertex backtracing invoked: ");
			// create a goal for each node
			//Goal g = new Goal(v.state.getStaticPosition(), v.state.getDirection());
			Goal goalStep = v.previousInteraction;
			System.out.println("adding goalStep: " + goalStep);
			goalStep.setEffect(v.importantEffect);
			goalDeque.add(0,goalStep);
			strategyProbability *= (v.importantEffect != null) ? v.getTopProbability() : 1.0; // no-effect-goals do not depend on effect probabilities
//			System.out.println("SD: Considered vertex probability: " + v.getTopProbability() + ", with effect: " + v.importantEffect);
			// TODO: right  now, the probability of that effect is used, which is considered to be the most important. 
			// Better use the probability of that effect, which is featured in the current strategy!!
		}

		// Probability massaging for strategy evaluation
//		System.out.println("SD: Strategy probability: " + strategyProbability);
		this.setChanged();
		this.notifyObservers(new PlanningEvent(PlanningEventType.GOALPLAN_PROBABILITY, strategyProbability));
		
		System.out.println("GETGOALSDGM RETURNS: ");
		System.out.println(goalDeque);
		System.out.println("---------------------");
		return goalDeque;
	}
	
	public void stop() {
		this.abort.set(true);
	}
}
