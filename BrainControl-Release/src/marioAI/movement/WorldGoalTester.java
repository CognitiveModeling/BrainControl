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
import java.util.List;
import java.util.Observable;

import marioAI.aStar.GoalTester;
import marioAI.aStar.actionEncoded.State;
import marioAI.aStar.stateEncoded.IGoal;
import marioAI.brain.Generalizer;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.Goal;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.BlockingType;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeFineGrain;
import marioWorld.mario.environments.Environment;

/**
 * Provide the functionality for A* to check whether a current ISearchNode is
 * considered to be in the goal.
 * 
 * @author Niels, Eduard. Modified by Stephan, Jonas E.
 * 
 */

public class WorldGoalTester extends Observable implements IGoal<WorldNode>, GoalTester 
{
	public ArrayList<WorldNode> excludedNodes = new ArrayList<>();
	
	protected /*final*/ Goal goal;
	
	protected SimulatedLevelScene scene;
	
	protected final float goalThreshold;
	//protected final float goalThresholdY;
	protected final float goalVelocityThresholdX = MovementTuning.INITIAL_GOAL_VELOCITY_ERROR_TOLERANCE;
	//protected final float goalVelocityThresholdY = MovementTuning.INITIAL_GOAL_VELOCITY_ERROR_TOLERANCE;
	
	/** Constructor */
	public WorldGoalTester(Goal goal, SimulatedLevelScene simulatedLevelScene)
	{
		this.goal = goal;
		this.scene = simulatedLevelScene;
		
//		if(isGoalBlocking())
//		{			
//			goalThresholdX=MovementTuning.INITIAL_FINE_GOAL_POSITION_TOLERANCE;
//			goalThresholdY=MovementTuning.INITIAL_FINE_GOAL_POSITION_TOLERANCE;
//		}
//		else
//		{
//			goalThresholdX=MovementTuning.INITIAL_COARSE_GOAL_POSITION_TOLERANCE;
//			goalThresholdY=MovementTuning.INITIAL_COARSE_GOAL_POSITION_TOLERANCE;			
//		}
		
		goalThreshold=MovementTuning.INITIAL_GOAL_POSITION_TOLERANCE;
		//goalThresholdY=MovementTuning.INITIAL_GOAL_POSITION_TOLERANCE;
	}
	
	public void setScene(SimulatedLevelScene newScene) 
	{
		this.scene = newScene;
	}
	
	public SimulatedLevelScene getScene()
	{
		return scene;
	}

	/*protected boolean velocityConstraintImportant()
	{
		//if this is a solid object and the collisiondirection is "from above", make sure not to move when this action ends...
		//return this.getGoal().hasTargetObject() && this.getGoal().getDirection().equals(CollisionDirection.ABOVE);
		return true;
	}	
	
	protected boolean velocityConstraintMet(SimulatedLevelScene testScene)
	{
		return 	Math.abs(testScene.getPlayer().getVelocity(0).x)<goalVelocityThresholdX && 
				Math.abs(testScene.getPlayer().getVelocity(0).y)<goalVelocityThresholdY;
	}*/

	private boolean positionIsSafe(SimulatedLevelScene scene)
	{
		SimulatedPlayer actor = scene.getSimulatedPlayers().get(getActorIndex());
		
		return  Math.abs(scene.getPlanningPlayer().getVelocity(0).x) < goalVelocityThresholdX && 
				(scene.isBlockingAgainstDirection(new GlobalCoarse(actor.getPosition(0).toGlobalCoarse().x, actor.getPosition(0).toGlobalCoarse().y+1), CollisionDirection.ABOVE) || scene.getSimulatedPlayers().get(getActorIndex()).getCarriedBy() != null);
	}
	
	private boolean isGoalBlocking()
	{		
		return (goal.getTarget().blocksPath != BlockingType.NONE); 
	}
	
	public int getActorIndex()
	{
		//find out which player is currently the actor in the mental simulation of the planning agent
		int actorIndex=1000;	//this should crash if there is no actor index...
		for(SimulatedPlayer player : scene.getSimulatedPlayers())
		{
			if(player.getType()==goal.getActor())
				actorIndex=player.getPlayerIndex();
		}
		return actorIndex;
	}
	
	/*
	 * return code:
	 * 0: no, the simulated collisions did not contain the goal interaction
	 * 1: yes, desired interaction was reached
	 * 2: there was no collision 
	 * */
	private int goalInteractionReached(SimulatedLevelScene scene)
	{
		//this function returns true if a collision with the desired object happened in the last tick OR if the position equals the desired position if the object is non blocking
		
		//check for collision with blocking object
		if(isGoalBlocking() || this.goal.getTarget().equals(PlayerWorldObject.ENERGY_FRONT)/* && !goal.getTarget().isPlayer()*/ && goal.collisionAtPosition.getDirection() != CollisionDirection.IRRELEVANT)
		{		
			
			if(!(Generalizer.generalizePlayerWorldObject(goal.getTarget()).isRelevantToLearning()) && Generalizer.generalizePlayer(goal.getTarget()) != PlayerWorldObject.PLAYER ){
				//scene.getSimulatedPlayers().get(scene.calculateMovingPlayer()).getPosition(0);
				if(isAboveGoalPosition(scene.getMovingPlayer().getPosition(0))){
					return 1;
				}
			}
			List<CollisionAtPosition> collisionList = scene.getCollisionsInLastTick();		
			
			if(collisionList!=null)
			{		
				for(CollisionAtPosition cap : collisionList)
				{
//					System.out.println("Cap " + cap);
//					System.out.println("Goal cap " + goal.collisionAtPosition);
//					System.out.println("");
					
					if (cap.equals(goal.collisionAtPosition, true, true, false, true))
//							&& goal.getActor() == scene.getPlayer().type) 
					{						
						return 1;
					}
				}
			}
			else
			{
				return 2;
			}
				
			return 0;
		}
		else
		{
			//System.out.println(goal.getActorSprite().getPosition(0));
			
			if(isAtGoalPosition(scene.getSimulatedPlayers().get(getActorIndex()).getPosition(0)))
			{
				return 1;
			}
			else
			{
				if(scene.getCollisionsInLastTick()==null)
				{
					return 2;
				}
				else
				{
					return 0;
				}
			}
		}
//		//check if target is within object
//		else if(goal.hasSprite() && goal.getActor().equals(scene.getPlayer().getType()))
//		{
//			return isAtGoalPosition(goal.getSpriteTarget().getPosition(0));
//		}
//		else
//		{
//			if(goal.getActor().equals(scene.getPlayer().getType()))
//			{
//				return isAtGoalPosition(scene.getPlayerPos());
//			}
//			else 
//			{
//				return false;
//			}
//		}
	}	
	
	//needed by AStar
	@Override
	public boolean goalReached(WorldNode node)
	{		
		int exitCode=goalInteractionReached(node.getWorldState());
		
		if(exitCode==1)	//the desired interaction was reached
		{
			node.setGoalInterActionWasReached();
		}
		
//		//there were observed interactions but they did not contain the goal interaction
//		if(exitCode==0 && node.getGoalInterActionWasReached())
//		{
//			node.setNoInteractionSinceGoalInterActionWasReached(false);
//		}
		
		return node.getGoalInterActionWasReached() && positionIsSafe(node.getWorldState()) /*&& node.getNoInteractionSinceGoalInterActionWasReached()*/; 
	}

	/**
	 * checks, if all conditions for the goal have been met (this includes that
	 * mario is at the goal position).
	 * 
	 * @param scene
	 * @param collisionsInBetweenParentAndThis
	 * @return
	 */
	/*public boolean goalReached(SimulatedLevelScene scene,
			List<CollisionAtPosition> collisionsInBetweenParentAndThis,
			TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge)
	{
		//this is called for sprites?
		
		if(velocityConstraintImportant() && !velocityConstraintMet(scene))
			return false;		
				
		return goalPositionReached(scene.getPlayerPos());
	}*/
	

	/**
	 * checks merely, if mario is at the goal position, but not if additional
	 * conditions have been met
	 * 
	 * @param playerPosition
	 * @return
	 */
	/*private boolean goalPositionReached(GlobalContinuous position)
	{
		return isWithinGoalThresholds(playerPosition);		
	}*/

	private boolean isAtGoalPosition(GlobalContinuous actualPlayerPosition)
	{	
		GlobalCoarse goalPosition = goal.getPosition().clone();
		/*switch(goal.collisionAtPosition.conditionDirectionPair.collisionDirection)
		{ 
			case LEFT: 
		 			goalPosition.x--; 
					break; 
				case RIGHT: 
					goalPosition.x++; 
					break; 
				case ABOVE: 
					goalPosition.y--; 
					break; 
				case BELOW: 
				goalPosition.y++; 
					break; 
				default: 
				break; 
		} */

		return isWithinGoalThresholds(goalPosition.toGlobalContinuous().toObservationRelativeFineGrain(actualPlayerPosition), actualPlayerPosition.toObservationRelativeFineGrain(actualPlayerPosition), goalThreshold);
	}
	
	private boolean isAboveGoalPosition(GlobalContinuous actualPlayerPosition){
		GlobalCoarse goalPosition =new GlobalCoarse(goal.getPosition().x, goal.getPosition().y -1);
		return isWithinGoalThresholds(goalPosition.toObservationRelativeFineGrain(actualPlayerPosition), actualPlayerPosition.toObservationRelativeFineGrain(actualPlayerPosition), goalThreshold);
	}
	
	public boolean isWithinGoalThresholds(ObservationRelativeFineGrain goal, ObservationRelativeFineGrain pos, double tolerance) 
	{
		float dx = Math.abs(goal.x - pos.x);
		float dy = Math.abs(goal.y - pos.y);
		int grainSize = CoordinatesUtil.UNITS_PER_TILE / CoordinatesUtil.getGrainFactor();
		dx *= grainSize;
		dy *= grainSize;
		return dx < tolerance && dy < tolerance;
	}

	/**
	 * Dijkstra's algorithm to find a path that matches the conditions.
	 * 
	 * @param source
	 *            source node containing the relevant infos at the start of
	 *            search.
	 * @return node where conditions are fulfilled.
	 */
	//protected abstract WorldGoalTester getIntermediateGoal(GlobalContinuous playerPosition, int halfObservationWidth, int halfObservationHeight);

	//CHECK: what is an intermediate goal!??!
//	public boolean isIntermediate() {
//		return goal.isIntermediate();
//	}

	/*public double additionalHeuristicCost(ObservationRelativeFineGrain p) {
		// Override where needed
		return 0;
	}*/

	public GlobalCoarse getGoalPos() 
	{		
		if(goal.hasTargetSprite())
		{
			return this.goal.getPosition();
		}
		else
			return this.goal.getPosition();
	}

	public Goal getGoal() {
		return this.goal;
	}

	public CollisionDirection getDirection() {
		return goal.getDirection();
	}

	@Override
	public boolean isGoalReached(State<?> state) {
		return goalReached((WorldNode)state);
	}

	/*public DirectionalHeuristic getDirectionalHeuristic() {
		// Override where needed
		return null;
	}*/

	/*public void refreshDirectionalHeuristic(GlobalContinuous playerPosition) {
		// do nothing.
		// Override where needed.
	}*/

	/**
	 * If a goal node is outside Mario's observation create an intermediate
	 * goal.
	 * 
	 * @author Niels
	 * 
	 * @param playerPosition
	 *            Mario's real position
	 * @param halfObservationWidth
	 *            half the width of an observation
	 * @param halfObservationHeight
	 *            half the height of an observation
	 * @return GoalNode which does not exceed observation bounds
	 */
//	public IntermediateGoalReturn helpIntermediateWorldGoalTester(GlobalContinuous playerPosition,
//			int halfObservationWidth, int halfObservationHeight) {
//		// if the goal lies within the observation
//		if (this.getGoalPos().isWithinObservation(playerPosition))
//			return null;
//
//		/*
//		 * TODO These calculations could maybe lead to errors if Mario is to the
//		 * far right of his tile, and the goal is to the far left of its tile,
//		 * but just outside the right of the observation.
//		 */
//		GlobalContinuous goalPos = this.getGoalPos().toGlobalContinuous();
//		float goalDistanceX = goalPos.x - playerPosition.x;
//		float goalDistanceY = goalPos.y - playerPosition.y;
//
//		int halfOfObservationWidthContinuous = halfObservationWidth * CoordinatesUtil.UNITS_PER_TILE;
//		int halfOfObservationHeightContinuous = halfObservationHeight * CoordinatesUtil.UNITS_PER_TILE;
//
//		float inIntermediateGoalThresholdX;
//		float inIntermediateGoalThresholdY;
//
//		GlobalContinuous intermediateGoalPosition;
//
//		int MARGIN = 32;
//
//		// TODO MARGIN might not be optimal
//		// shift X just inside of the observation
//		float shiftedX = playerPosition.x + (halfOfObservationWidthContinuous - MARGIN) * (goalDistanceX > 0 ? 1 : -1);
//		// shift Y just inside of the observation
//		float shiftedY = playerPosition.y + (halfOfObservationHeightContinuous - MARGIN) * (goalDistanceY > 0 ? 1 : -1);
//
//		if (goalDistanceX == 0) {
//			inIntermediateGoalThresholdX = Float.POSITIVE_INFINITY;
//			inIntermediateGoalThresholdY = MovementTuning.INITIAL_FINE_GOAL_POSITION_TOLERANCE;
//			intermediateGoalPosition = new GlobalContinuous(playerPosition.x, shiftedY);
//		} else if (goalDistanceY == 0) {
//			inIntermediateGoalThresholdX = MovementTuning.INITIAL_FINE_GOAL_POSITION_TOLERANCE;
//			inIntermediateGoalThresholdY = Float.POSITIVE_INFINITY;
//			intermediateGoalPosition = new GlobalContinuous(shiftedX, playerPosition.y);
//		} else {
//			float slopeOfPlayerToGoalConnection = goalDistanceY / goalDistanceX;
//			/**
//			 * slope is greater than the ratio of the observation rectangle. It
//			 * follows that the intersection of the connection with the
//			 * observation border lies in the top or bottom borders.
//			 */
//			if (Math.abs(slopeOfPlayerToGoalConnection) > (float) halfObservationHeight / (float) halfObservationWidth) {
//				float newX = -1 / slopeOfPlayerToGoalConnection * (halfOfObservationHeightContinuous - MARGIN)
//						+ playerPosition.x;
//				intermediateGoalPosition = new GlobalContinuous(newX, shiftedY);
//				// use the y-distance of the intermediate goal to the current
//				// position as x threshold
//				inIntermediateGoalThresholdX = Math.abs(shiftedY - playerPosition.y);
//				// use the x-distance of the intermediate goal to the current
//				// position as y threshold
//				inIntermediateGoalThresholdY = Math.abs(newX - playerPosition.x);
//			} else {
//				float newY = slopeOfPlayerToGoalConnection * (halfOfObservationWidthContinuous - MARGIN)
//						+ playerPosition.y;
//				intermediateGoalPosition = new GlobalContinuous(shiftedX, newY);
//				// use the y-distance of the intermediate goal to the current
//				// position as x threshold
//				inIntermediateGoalThresholdX = Math.abs(newY - playerPosition.y);
//				// use the x-distance of the intermediate goal to the current
//				// position as y threshold
//				inIntermediateGoalThresholdY = Math.abs(shiftedX - playerPosition.x);
//			}
//			// make both thresholds at least as large as the goal distance error
//			float error = MovementTuning.INITIAL_FINE_GOAL_POSITION_TOLERANCE;
//			inIntermediateGoalThresholdX = inIntermediateGoalThresholdX < error ? error : inIntermediateGoalThresholdX;
//			inIntermediateGoalThresholdY = inIntermediateGoalThresholdY < error ? error : inIntermediateGoalThresholdY;
//		}
//		Goal intermediateGoal = new Goal(intermediateGoalPosition.toGlobalCoarse(), true);
//		return new IntermediateGoalReturn(intermediateGoal, inIntermediateGoalThresholdX, inIntermediateGoalThresholdY);
//	}

	/**
	 * Helper class to wrap return values
	 */
//	protected static class IntermediateGoalReturn {
//		/** all final values */
//		protected final Goal intermediateGoal;
//		protected float inIntermediateGoalThresholdX;
//		protected float inIntermediateGoalThresholdY;
//
//		/** constructor to set the encapsulated values */
//		protected IntermediateGoalReturn(Goal intermediateGoal, float inIntermediateGoalThresholdX,
//				float inIntermediateGoalThresholdY) {
//			this.intermediateGoal = intermediateGoal;
//			this.inIntermediateGoalThresholdX = inIntermediateGoalThresholdX;
//			this.inIntermediateGoalThresholdY = inIntermediateGoalThresholdY;
//		}
//	}

}
