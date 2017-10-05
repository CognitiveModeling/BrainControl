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

import java.awt.Point;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import mario.main.Settings;
import marioAI.aStar.AStarNodeVisualizer;
import marioAI.aStar.actionEncoded.Action;
import marioAI.aStar.actionEncoded.State;
import marioAI.aStar.stateEncoded.ASearchNode;
import marioAI.aStar.stateEncoded.AStarStateEncoded;
import marioAI.brain.Effect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.goals.Goal;
import marioAI.movement.heuristics.FineGrainPotentialField;
import marioAI.movement.heuristics.FineGrainPotentialField.HeuristicParameters;
import marioAI.movement.heuristics.WorldNodeHeuristic;
import marioAI.util.GeneralUtil;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Player;

/**
 * A WorldNode captures Mario's world state and can be used in the AStar
 * algorithm. New Nodes can be expanded from a WorldNode.
 * 
 * @author unknown, extended by Jonas E. & Marcel 
 */
public class WorldNode extends ASearchNode<WorldNode> implements State<PlayerActions>  {

	//private static final double ADDITIONAL_COST_FOR_INTERACTIONS_WITH_GOAL_TARGET_FROM_NON_DESIRED_DIRECTIONS = 0; //Double.POSITIVE_INFINITY;
	//public static final double ADDITIONAL_COST_FOR_NON_GOAL_TARGET_INTERACTIONS = 0;
	/**
	 * the expandedLength (cf. g in ASearchNode) from start to the first node
	 * within the same heuristic cell as this node + the heuristic
	 */
	private final double totalCost;

	//public static final double daytimeVelocity = 1;

	// Mario's position at the beginning of the search, used to compute Mario's
	// relative positions on the map.
	public final GlobalContinuous initialPlayerPosition;

	protected final SimulatedLevelScene worldState;
	// Goal tester containing the goal used to expand the heuristic from.
	private final WorldGoalTester goalTester;
	// Parent node used to reconstruct the path once a node reaches the goal.
	private WorldNode parent;
	// Heuristic used by AStar.
	private final WorldNodeHeuristic heuristic;
	// Mario's current position in continuous world coordinates.
	private final GlobalContinuous playerPosition;
	// Mario's current velocity in continuous world coordinates.
	private final GlobalContinuous playerVelocity;
	// Sequence of actions that has lead to this worldState in chronological
	// order.
	protected final ArrayList<PlayerActions> actionSequence;
	private final List<CollisionAtPosition> collisionsInBetweenParentAndThis;
	//public final TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge;
	private final double timeStepPenalty = 1.;
	//private final double velocityBoost = 1.;

	// How long is mario in the air
	private double currentJumpHeight;
	// public final HeuristicParameters heuristicParameters ;
	public final double heuristicCostToGoalNode;
	public final double[] expansionDirection;
	public final int cellSize = (int) (CoordinatesUtil.UNITS_PER_TILE / MovementTuning.currentGrainFactor);
	public final Point cellCoordinates;
	private final int numPreviousNodeForDirection = 4;

	private boolean goalInterActionWasReached = false;
	private boolean noInteractionSinceGoalInterActionWasReached = true;
	
	private final static double COST_FACTOR = 0.001;

	//TODO: speed button simulation does not work correctly... could be due to sliding variable in player, also, is this the same key for fire balls??
	//TODO: also the cower action is not simulated correctly!
	//TODO: disabled right now... could be used to simulate the astar for all players in parallel...
	static final PlayerAction[] PLAYER_ACTIONS = new PlayerAction[] 
	{
			/* do nothing */ new PlayerAction(false, false, false, false, false, false, 1.1*COST_FACTOR),
			/* Left */ 		 new PlayerAction( true, false, false, false, false, false, 1.0*COST_FACTOR),
			/* Right */ 	 new PlayerAction(false,  true, false, false, false, false, 1.0*COST_FACTOR),
			/* Jump */ 		 new PlayerAction(false, false, false,  true, false, false, 7.0*COST_FACTOR),
			/* JumpLeft */   new PlayerAction( true, false, false,  true, false, false, 3.0*COST_FACTOR),
			/* JumpRight */  new PlayerAction(false,  true, false,  true, false, false, 3.0*COST_FACTOR),
			/* Carry */      new PlayerAction(false, false, false, false, false, true, 7.0*COST_FACTOR)
	};

	public void setGoalInterActionWasReached()
	{
		this.goalInterActionWasReached=true;
	}

	public void setNoInteractionSinceGoalInterActionWasReached(boolean set)
	{
		this.noInteractionSinceGoalInterActionWasReached=set;
	}	
	
	public boolean getGoalInterActionWasReached()
	{
		return this.goalInterActionWasReached;
	}	

	public boolean getNoInteractionSinceGoalInterActionWasReached()
	{
		return this.noInteractionSinceGoalInterActionWasReached;
	}		
	
	public WorldNode(SimulatedLevelScene worldState,
			WorldGoalTester goalTester,
			/*TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge,*/
			ArrayList<PlayerActions> aactionSequence, WorldNodeHeuristic heuristic,
			GlobalContinuous initialPlayerPosition,
			List<CollisionAtPosition> collisionsInBetweenParentAndThis,
			WorldNode parent) {
		this.worldState = worldState;
		//this.knowledge = knowledge;
		this.playerPosition = worldState.getPlanningPlayerPos();
		this.playerVelocity = worldState.getPlanningPlayer().getVelocity(0);
		this.goalTester = goalTester;
		this.actionSequence = aactionSequence;
		this.heuristic = heuristic;
		this.initialPlayerPosition = initialPlayerPosition;
		this.collisionsInBetweenParentAndThis = collisionsInBetweenParentAndThis;
		this.parent = parent;
				
		setCostFromStartNode(parent == null ? 0 : parent.costFromStartNode() + parent.costToSuccessor(this));

		// calculate currentJumpHeight

		if (this.parent != null) 
		{
			//just copy if the past interactions were successful
			this.goalInterActionWasReached=parent.getGoalInterActionWasReached();
			this.noInteractionSinceGoalInterActionWasReached=parent.getNoInteractionSinceGoalInterActionWasReached();
			
			double jumpDifference = this.getParent().getPlayerPos().getY()
					- this.getPlayerPos().getY();
			if (jumpDifference < 0) {
				jumpDifference = 0;
			}

			this.currentJumpHeight = this.getParent().currentJumpHeight
					+ jumpDifference;

		} else
			this.currentJumpHeight = 0;
		if (this.worldState.getPlanningPlayer().isMayJump()) {
			this.currentJumpHeight = 0;
		}

		this.cellCoordinates = this.heuristic.cellCoordinates(this);
		WorldNode ancestorForDirection = this;
		for (int i = 0; i < numPreviousNodeForDirection; i++) {
			if (ancestorForDirection.parent == null) {
				break;
			}
			ancestorForDirection = ancestorForDirection.parent;
		}
		this.expansionDirection = HeuristicParameters.getDirection(
				ancestorForDirection, this);

		WorldNode ancestorInSameHeuristicCell = calculateAncestorInSameCell();
		if (ancestorInSameHeuristicCell == this) {
			this.heuristicCostToGoalNode = this
					.calculateHeuristicCostToGoalNode();
			this.totalCost = super.totalCost();
		} else {
			this.heuristicCostToGoalNode = ancestorInSameHeuristicCell.heuristicCostToGoalNode;
			this.totalCost = ((ASearchNode<WorldNode>) ancestorInSameHeuristicCell)
					.totalCost();
		}
	}

	private WorldNode calculateAncestorInSameCell() 
	{
		//TODO: this "cell" stuff does not seem to speed things up but causes a lot of trouble resulting in extraordinary spastic action plans
		return this;
		
//		WorldNode ancestor = this;
//		WorldNode previousAncestor = this;
//
//		while (ancestor != null && heuristic.cellCoordinates(ancestor).equals(this.cellCoordinates)) 
//		{
//			previousAncestor = ancestor;
//			ancestor = ancestor.parent;
//		}
//		
//		return previousAncestor;
	}

//	private String test(double[] vector) {
//		String ret = "";
//		for (int i = 0; i < vector.length; i++) {
//			ret += vector[i] + (i < vector.length - 1 ? ", " : "");
//		}
//		return ret;
//	}

	/**
	 * this method calculates the costs for one node. the gradient of the heuristic map, the velocity of the current movement and the daytime are taken into account.
	 * 
	 * CHECK: daytime?!?!?
	 */
	@Override
	public double totalCost() 
	{		
		//TODO: this is not at all what class hierarchies are for... 
		if(heuristic instanceof FineGrainPotentialField)
		{
			/*double deviation = 0.0;
			
			HeuristicParameters heuristicParameters = this.heuristic.getHeuristicMap()[this.cellCoordinates.y][this.cellCoordinates.x][0];
			
			double[] vector1 = heuristicParameters.getGradient();
			
			if (vector1 == null) 
			{
				deviation = heuristicParameters.getParent() == null ? 0
						: FineGrainPotentialField.MAX_VALUE;
			} 
			else 
			{
				double[] vector2 = this.expansionDirection;
				if (vector2 == null) 
				{
					deviation = 0;
				} 
				else 
				{
					double cos = HeuristicParameters.dotProduct(vector1, vector2);
					deviation = (1.0 - cos) * 2d * (double) cellSize;
				}
			}*/

//			return totalCost
//					+ deviation
//					+ timeStepPenalty
//					- this.velocityBoost*WorldNode.daytimeVelocity
//					* Math.sqrt(this.playerVelocity.x * this.playerVelocity.x
//							+ this.playerVelocity.y * this.playerVelocity.y);

			
			//TEST: punish keypresses! why doesn't this work!?
			double pun=0.0;
			/*if(parent!=null && parent.actionSequence.size()>0 && actionSequence.size()>0)
			{
				boolean eq=true;
				for(int i=0;i<5;i++)
				{
					if(!parent.actionSequence.get(actionSequence.size()-1)[i] && actionSequence.get(actionSequence.size()-1)[i])
					{
						eq=false;
						break;
					}
				}
				
				if(!eq)
				{		
					pun=1;
				}
			}*/			
						
			//CHECK: this works better. also, why should the actual costs depend on the heuristics?
			return totalCost + timeStepPenalty + pun;
		}
		
		return totalCost + timeStepPenalty;
	}

	@Override
	public double heuristicCostToGoalNode() {
		return heuristicCostToGoalNode;
	}

	// TODO: save
	/**
	 * Calculate the heuristic cost to the goal node, consists of the heuristic
	 * and and penalty for approaching the goal from the wrong direction. The
	 * Point on Mario from which we calculate the heuristic is no longer located
	 * at the center of his feet but the edge with highest heuristic cost. This
	 * is needed to properly detect areas with high heuristic penalty (as used
	 * in directional approaches) and not accidentally jumping though them.
	 */
	public double calculateHeuristicCostToGoalNode() {
		if (goalTester.goalReached(this))
			return 0;

		/*
		 * If the heuristic is a FineGrainPotentialField use its extra
		 * information about jump distances to exclude points, that won't lead
		 * to the goal.
		 */
		//TODO: this is extremely dirty and btw. does NOT work! jump value is not correct! does this even work in theory!?
		/*if (heuristic instanceof FineGrainPotentialField) 
		{
			if (((FineGrainPotentialField) heuristic).jumpValue(this) + currentJumpHeight > FineGrainPotentialField.MAX_JUMP_VALUE) 
			{
				goalTester.excludedNodes.add(this);
				return Double.POSITIVE_INFINITY;
			}
		}*/

		int width = worldState.getPlanningPlayer().type.getWidthForCollisionDetection();
		int height = worldState.getPlanningPlayer().getHeight();

		// List of points we take the maximum cost from
		List<GlobalContinuous> pointsToCheck = new ArrayList<>(13);
		// Mario's upper left corner
		pointsToCheck.add(new GlobalContinuous(playerPosition.x - width / 2,
				playerPosition.y - height));
		// Mario's upper right corner
		pointsToCheck.add(new GlobalContinuous(playerPosition.x + width / 2,
				playerPosition.y - height));
		// Mario'slower left corner
		pointsToCheck.add(new GlobalContinuous(playerPosition.x - width / 2,
				playerPosition.y));
		// Mario's lower right corner
		pointsToCheck.add(new GlobalContinuous(playerPosition.x + width / 2,
				playerPosition.y));
		// Mario's top center
		pointsToCheck.add(new GlobalContinuous(playerPosition.x, playerPosition.y
				- height));
		// Mario's bottom center
		pointsToCheck.add(playerPosition);
		// Mario's center left
		pointsToCheck.add(new GlobalContinuous(playerPosition.x - width / 2,
				playerPosition.y - height / 2));
		// Mario's center right
		pointsToCheck.add(new GlobalContinuous(playerPosition.x + width / 2,
				playerPosition.y - height / 2));
		// Mario's center
		pointsToCheck.add(new GlobalContinuous(playerPosition.x, playerPosition.y
				- height / 2));
		// Mario's upper left quarter center
		pointsToCheck.add(new GlobalContinuous(playerPosition.x - width / 4,
				playerPosition.y - (height / 4) * 3));
		// Mario's lower left quarter center
		pointsToCheck.add(new GlobalContinuous(playerPosition.x - width / 4,
				playerPosition.y - (height / 4)));
		// Mario's upper right quarter center
		pointsToCheck.add(new GlobalContinuous(playerPosition.x + width / 4,
				playerPosition.y - (height / 4) * 3));
		// Mario's lower right quarter center
		pointsToCheck.add(new GlobalContinuous(playerPosition.x + width / 4,
				playerPosition.y - (height / 4)));

		List<Double> costs = new ArrayList<>(13);

		for (GlobalContinuous p : pointsToCheck) 
		{
			/*ObservationRelativeFineGrain toCheck = p
					.toObservationRelativeFineGrain(initialPlayerPosition);
			double additionalCost = goalTester.additionalHeuristicCost(toCheck);*/
			
			//TEST: directional goals seem to make trouble... thus this "penalty" is disabled. it is not necessary to reach a goal EXACTLY from a specific direction, particularly if its non blocking. this is what star accounts for.
			double additionalCost=0;
			
			costs.add(this.weightFunction() * (this.heuristic.heuristic(this) + additionalCost));
		}
		return Collections.max(costs);
		
		//return weightFunction() * heuristic.heuristic(this);		
	}

	/* Calculates the weight for the heuristic cost to the goal, different function are possible. At the moment
	it is a constant factor */

	private double weightFunction() {
		// if (this.heuristic instanceof FineGrainPotentialField) {
		// return MovementTuning.currentHeuristicWeight/(1+
		// FineGrainPotentialField.MAX_JUMP_VALUE * Math.exp( -0.1 *
		// (((FineGrainPotentialField) heuristic).jumpValue(this)
		// + currentJumpHeight)));
		// } else
		return MovementTuning.currentHeuristicWeight;

	}

//	private int getNumberOfActionsBetweenParentAndThis() {
//		return actionSequence.size();
//	}

	/**
	 * Cost of getting from one node to another. Since successor nodes are
	 * determined by simulating a fixed amount of time this is a constant.
	 */
	@Override
	public double costToSuccessor(WorldNode successor) 
	{
		//return this.playerPosition.distance(successor.playerPosition)
		
		//TODO: dirty!
		//TODO: check differences to action encoded approach...
		if(actionSequence.size()>0)
			return actionSequence.get(actionSequence.size()-1).getActionCost();
		else
			return 0;
	}

//	/**
//	 * sum up over additional costs for all collisions
//	 * 
//	 * @param parent
//	 * @param successor
//	 * @return
//	 */
//	public static double additionalCostFromNodeToSuccessor(Goal goal,
//			List<CollisionAtPosition> collisionsBetweenNodes
//			/*TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge2*/) 
//	{
//		if (AStarSearchRunnable.USE_DIRECTIONAL_APPROACH)
//			return 0;
//		
//		double ret = 0;
//		// create less strict goal collision constraints: these constraints
//		// consider only the goal target, not the direction or mario health.
//		CollisionAtPosition generalGoalCollision = goal.collisionAtPosition
//				.clone(true, true, false);
//		// For all collisions
//		for (CollisionAtPosition collisionAtPosition : collisionsBetweenNodes) {
//			// if the collision matches the less strict goal constraints (only
//			// target match)
//			if (collisionAtPosition.equals(generalGoalCollision, false, true,
//					false, true)) {
//				// and matches the strict goal constraints (direction and mario
//				// health match)
//				if (collisionAtPosition.equals(goal.collisionAtPosition, false,
//						true, false, true)) {
//					// this collision is really the desired collision => no
//					// additional cost.
//					// do nothing unless you want to speed up AStar search here
//					// by adding a negative cost.
//				} else {
//					// worst case scenario: mario has interacted with the
//					// desired goal from the wrong direction or with wrong
//					// health => add huge cost
//					// but only if mario already knows that an effect will occur
//					double inhibitionFactor = GeneralUtil
//							.inhibitEffectsBasedOnKnowledge(knowledge2,
//									collisionAtPosition.condition);
//					ret += inhibitionFactor
//							* ADDITIONAL_COST_FOR_INTERACTIONS_WITH_GOAL_TARGET_FROM_NON_DESIRED_DIRECTIONS;
//				}
//			} else {
//				// moderately bad scenario: mario has interacted with a non-goal
//				// target on the way to the goal => add low cost
//				double inhibitionFactor = GeneralUtil
//						.inhibitEffectsBasedOnKnowledge(knowledge2,
//								collisionAtPosition.condition);
//				ret += inhibitionFactor
//						* ADDITIONAL_COST_FOR_NON_GOAL_TARGET_INTERACTIONS;
//			}
//		}
//		return ret;
//	}

	// /**
	// * define an additional cost for a single collision
	// * @param conditionDirectionPair the collision
	// * @return
	// */
	// private double getAdditionalCost(ConditionDirectionPair
	// conditionDirectionPair) {
	// //Here a custom differentiation can be made between different collisions
	// return additionalCostForInteractions;
	// }

	/*
	 * Forward simulates successor nodes resulting from all possible keys a single player (the actor) can press
	 * 
	 * Used for AStar planning
	 */
//	public ArrayList<WorldNode> getSuccessors(int actorIndex) 
//	{
//		ArrayList<WorldNode> successors = new ArrayList<WorldNode>();
//		
//		for (PlayerAction action : PLAYER_ACTIONS) 
//		{			
//			successors.add(this.actionToNode(action,actorIndex));
//		}
//		
//		return successors;
//	}
	
	/*
	 * Finds out the current actor and forward simulates all the possible successor nodes
	 * 
	 * Used for AStar planning
	 */	
	@Override
	public List<WorldNode> getSuccessors()
	{		
		List<WorldNode> ret = new ArrayList<WorldNode>();
		int ai = goalTester.getActorIndex();
		for(PlayerAction pa : PLAYER_ACTIONS)
		{
			PlayerActions pas = new PlayerActions(ai,pa);
			List<PlayerActions> lpas = new ArrayList<PlayerActions>();
			lpas.add(pas);
			
			ret.add((WorldNode) getSuccessor(lpas));
		}
		
		return ret;
	}	
	
	/*
	 * Forward simulates the successor node given a SEQUENCE of actions (of all players)
	 * */
	@Override
	public State<PlayerActions> getSuccessor(List<PlayerActions> actions)
	{
		// fast implementation
		return actionsToNode(actions);
	}	

	/**
	 * Simulate the future of this node executing the given action a fixed
	 * number of times.
	 * 
	 * @param action
	 * @return
	 */
//	public WorldNode actionToNode(PlayerAction action, int actorIndex) 
//	{
//		final int numberOfActionsPerNode = 1;		//TEST! was 2... can this cause errors!?
//		SimulatedLevelScene worldState = backupState();
//
//		ArrayList<PlayerAction> aactionSequence = new ArrayList<PlayerAction>();
//
//		ArrayList<CollisionAtPosition> collisionsInBetweenParentAndThis = new ArrayList<CollisionAtPosition>();
//
//		// in each iteration calculate new maxNumberOfSteps on worldState
//		// for (int i = 0; i <
//		// MovementTuning.getCurrentActionsPerNode(worldState,
//		// goalTester); i++) {
//		for (int i = 0; i < numberOfActionsPerNode; i++) 
//		{
//			// change worldstate
//			List<CollisionAtPosition> collisions = worldState.tick(action.getKeyState(),actorIndex);
//
//			collisionsInBetweenParentAndThis.addAll(collisions);
//
//			// worldState = advanceStep(worldState, action);
//			aactionSequence.add(action); // remember action done
//			
//			// If any collision occurs, do not simulate any more ticks with the
//			// same action, as the collision might have a strong impact on our
//			// plan. Instead, decide on a new action afterwards.
//			if (collisions.size() > 0)
//				break;
//		}
//
//		return new WorldNode(worldState, this.goalTester, /*this.knowledge,*/
//				aactionSequence, this.heuristic, this.initialPlayerPosition,
//				collisionsInBetweenParentAndThis, this);
//	}

	/**
	 * Simulate the future of this node executing the list of actions.
	 * 
	 * @param action
	 * @return
	 */
	public WorldNode actionsToNode(List<PlayerActions> actions) 
	{
//		//TEST: this is a workaround for some strange bug
		if(actions==null)
			return this;
		
		SimulatedLevelScene worldState = backupState();
		ArrayList<PlayerActions> actionList = new ArrayList<>();
		WorldNode node = this;
		for (PlayerActions action : actions) 
		{
			actionList.add(action);
			
			// change worldstate
			List<CollisionAtPosition> collisions = worldState.tick(action);
			ArrayList<PlayerActions> oneActionSequence = new ArrayList<>(1);
			oneActionSequence.add(action);
			node = new WorldNode(worldState, node.goalTester, /*node.knowledge,*/ oneActionSequence , node.heuristic, node.initialPlayerPosition, collisions, node);
		}
		return node;
	}

	/**
	 * Clone the current world state
	 */
	public SimulatedLevelScene backupState() {
		SimulatedLevelScene sceneCopy = null;
		try 
		{
			sceneCopy = (SimulatedLevelScene) this.worldState.clone();		
		} 
		catch (CloneNotSupportedException e) 
		{
			e.printStackTrace();
		}
		return sceneCopy;
	}

//	/**
//	 * Fill a size 5 boolean array representing key presses given an action that
//	 * is to be performed.
//	 */
//	public static PlayerAction createAction(boolean left, boolean right, boolean down, boolean jump, boolean speed, boolean carry, double cost) 
//	{
//		return new PlayerAction(left, right, down, jump, speed, cost, carry);
//	}

//	/**
//	 * Print total, cost from start, heuristic cost, MarioPosition and action
//	 * plan (if available).
//	 */
//	public String pathToString() 
//	{
//		String returnString = "";
//		for (PlayerAction actionSequence : AStarSimulator.extractActionPlan(AStarStateEncoded.<WorldNode> path(this))) 
//		{
//			returnString += actionToString(actionSequence.getKeyState());
//		}
//		return returnString;
//	}

//	public String toString() 
//	{
//		String returnString = "Total cost: "
//				+ Double.toString(this.totalCost()) + ", "
//				+ "cost from start: "
//				+ Double.toString(this.costFromStartNode()) + ", "
//				+ "heuristic: "
//				+ Double.toString(this.heuristicCostToGoalNode()) + ", "
//				+ "Player position: " + this.getPlayerPos().toString() + ", ";
//		// This happens for the initial node.
//		if (this.actionSequence.isEmpty()) {
//			return returnString;
//		}
//
//		String actionString = "";
//		for (PlayerAction actionSequence : AStarSimulator.extractActionPlan(AStarStateEncoded
//				.<WorldNode> path(this))) {
//			actionString += actionToString(actionSequence.getKeyState());
//		}
//		returnString += actionString;
//		return returnString;
//	}

	// /**
	// * Casts all ISearchNodes of the list, which are WorldNodes to its proper
	// type.
	// *
	// * @param list List of ISearchNodes, as returned by AStar
	// * @return List of all WorldNodes contained in the argument.
	// */
	// public static List<WorldNode> castToWorldNodeList(List<ISearchNode> list)
	// {
	// List<WorldNode> ret = new ArrayList<WorldNode>();
	// for (ISearchNode iNode : list) {
	// if (iNode instanceof WorldNode) {
	// ret.add((WorldNode) iNode);
	// }
	// }
	// return ret;
	// }

	public static String actionToString(boolean[] action) {
		return (action[Player.KEY_DOWN] ? "d" : "")
				+ (action[Player.KEY_RIGHT] ? "r" : "")
				+ (action[Player.KEY_LEFT] ? "l" : "")
				+ (action[Player.KEY_JUMP] ? "j" : "")
				+ (action[Player.KEY_SPEED] ? "s" : "")
				+ (action[Player.KEY_CARRYDROP] ? "c" : "");
	}

	// private WorldNode castToWorldNode(Object other) {
	// return (WorldNode) other;
	// }

	public List<PlayerActions> getActionSequence() {
		return this.actionSequence;
	}

	public WorldNode getParent() {
		return this.parent;
	}

	public void setParent(WorldNode parent) {
		this.parent = parent;
	}

	public GlobalContinuous getPlayerPos() {
		return this.playerPosition;
	}

	public SimulatedLevelScene getWorldState() {
		return this.worldState;
	}

	/**
	 * As multiple time steps may be simulated in between a parent node and a
	 * child node, this is the sum of the collisions obtained in each of the
	 * simulatedLevelScene.tick()
	 * 
	 * @return
	 */
	public List<CollisionAtPosition> getCollisionsInBetweenParentAndThis() {
		return collisionsInBetweenParentAndThis;
	}

	private static final double accuracyPosition = 0.6;
	private static final double accuracyVelocity = 0.7;
		
	public static final int observationWidth = 352; // Tile Width * Observation
	// With = 16 * 22
	private static int numberOfPositionBitsPerDimension;
	private static int numberOfVelocityBitsPerDimension;
	private static final float maxAbsVelocityInOneDimension = 50; // this is
	// definitely
	// enough.
	// mario's
	// velocity
	// nether
	// exceeds ~
	// 16
	static {
		// if values lie in [0, maxValue], then log_2(maxValue) is the maximum
		// number of bits required to represent any value.
		// the logarithm (of n) with respect to base 2 can be obtained by:
		// log_2(n) = log_x(n)/log_x(2), where x is any arbitrary base, like e
		
		numberOfPositionBitsPerDimension = (int) (Math.ceil(Math.log((observationWidth + 0.5) * accuracyPosition) / Math.log(2)));
		numberOfVelocityBitsPerDimension = (int) (Math.ceil(Math.log((2 * maxAbsVelocityInOneDimension + 0.5) * accuracyVelocity) / Math.log(2)));
		
//		if (numberOfPositionBitsPerDimension > 16/4 || numberOfVelocityBitsPerDimension > 16/4)	//for 4 players max 
//		{
//			throw new IllegalArgumentException("Please use lower accuracy, numberOfPositionBitsPerDimension="+numberOfPositionBitsPerDimension+", numberOfVelocityBitsPerDimension="+numberOfVelocityBitsPerDimension);
//		}
	}

	@Override
	public long keyCode() 
	{
		int shift = 0;		
		long hash = 0;
		
		SimulatedPlayer sp = getWorldState().getSimulatedPlayers().get(goalTester.getActorIndex());
		
		GlobalContinuous playerPosition = sp.getPosition(0);		
		
		//CHECK: why should the initial position be used!?
//			double relativePlayerPosX = playerPosition.x - initialPlayerPosition.x;
//			double relativePlayerPosY = playerPosition.y - initialPlayerPosition.y;
		double relativePlayerPosX = playerPosition.x - sp.getOriginalSpritePosition().x;
		double relativePlayerPosY = playerPosition.y - sp.getOriginalSpritePosition().y;

		//for BigInteger
//		BigInteger roundedX = BigInteger.valueOf((long) ((relativePlayerPosX + (float) observationWidth / 2.f + 0.5f) * accuracyPosition));
//		BigInteger roundedY = BigInteger.valueOf((long) ((relativePlayerPosY + (float) observationWidth / 2.f + 0.5f) * accuracyPosition));
//		BigInteger roundedXA = BigInteger.valueOf((long) ((sp.getVelocity(0).x + maxAbsVelocityInOneDimension + 0.5f) * accuracyVelocity));
//		BigInteger roundedYA = BigInteger.valueOf((long) ((sp.getVelocity(0).y + maxAbsVelocityInOneDimension + 0.5f) * accuracyVelocity));

		long roundedX = (long) ((relativePlayerPosX + (float) observationWidth / 2.f + 0.5f) * accuracyPosition);
		long roundedY = (long) ((relativePlayerPosY + (float) observationWidth / 2.f + 0.5f) * accuracyPosition);
		long roundedXA = (long) ((sp.getVelocity(0).x + maxAbsVelocityInOneDimension + 0.5f) * accuracyVelocity);
		long roundedYA = (long) ((sp.getVelocity(0).y + maxAbsVelocityInOneDimension + 0.5f) * accuracyVelocity);		
		//long roundedJumpHeight = (long) this.currentJumpHeight / 10;

		//use player id of carries -> 5 different values (can carry one of 4 players or noone) -> 3 bits		
		long carriesHash = 0;
		if(sp.getCarries()!=null)
		{			
			carriesHash+=((SimulatedPlayer)sp.getCarries()).getPlayerIndex()+1;
			//System.out.println(sp.getPlayerIndex()+" carries "+(carriesHash-1));
		}
		
		long carriedByHash = 0;
		if(sp.getCarriedBy()!=null)
		{
			carriedByHash+=((SimulatedPlayer)sp.getCarriedBy()).getPlayerIndex()+1;
			//System.out.println(sp.getPlayerIndex()+" carriedBy "+(carriedByHash-1));
		}
		
		//for BigInteger
//			hash = roundedY.shiftLeft(shift).or(hash);
//			shift += numberOfPositionBitsPerDimension;
//			hash = roundedX.shiftLeft(shift).or(hash);
//			shift += numberOfPositionBitsPerDimension;
//			hash = roundedYA.shiftLeft(shift).or(hash);
//			shift += numberOfVelocityBitsPerDimension;
//			hash = roundedXA.shiftLeft(shift).or(hash);
//			shift += numberOfVelocityBitsPerDimension;
		
		hash = roundedY << shift;		
		shift += numberOfPositionBitsPerDimension;
		hash = roundedX << shift | hash;
		shift += numberOfPositionBitsPerDimension;
		hash = roundedYA << shift | hash;
		shift += numberOfVelocityBitsPerDimension;
		hash = roundedXA << shift | hash;
		shift += numberOfVelocityBitsPerDimension;
		
		//TODO: this creates problems with unnecessary carries
//		hash = carriesHash << shift | hash;
//		shift += 3;
//		
//		hash = carriedByHash << shift | hash;
//		shift += 3;
		
		//hash += roundedJumpHeight << shift | hash;

		return hash;
	}

	// methods from IState interface:
	
	@Override
	public AStarNodeVisualizer getStateVisualizer(double costFromStart) {
		double cost = costFromStart;
		if (Settings.ASTAR_VISUALIZE_TOTAL_COST) {
			cost += heuristicCostToGoalNode;
		}
		return new AStarNodeVisualizer(getWorldState().getSimulatedPlayers().get(goalTester.getActorIndex()).getPosition(0), cost);
	}

	@Override
	public double getHeuristicCostToGoalNode() {
		return heuristicCostToGoalNode;
	}

	@Override
	public long getKeyCode() {
		return keyCode();
	}

	// @Override
	// public long keyCode() {
	// GlobalContinuous marioPosition = this.getMarioPos();
	// double relativeMarioPosX = marioPosition.x - initialMarioPosition.x;
	// double relativeMarioPosY = marioPosition.y - initialMarioPosition.y;
	//
	// // To save bits we do not use global continuous coordinates. More
	// importantly, we want to use a specific accuracy: values too close
	// together are mapped on the same value.
	// short x = (short) ((relativeMarioPosX + (float)observationWidth / 2. +
	// 0.5f) * accuracyPosition);
	// short y = (short) ((relativeMarioPosY + (float)observationWidth / 2. +
	// 0.5f) * accuracyPosition);
	//
	// // bits 32 to 24 are node's x coordinates, bits 23 to 15 y
	// int hash = (x << numberOfPositionBitsPerDimension) | (y << 0);
	// /**
	// * The code below also takes the rough direction of the parent into
	// consideration, making the search space a lot larger but solves the
	// 'platform error'.
	// */
	// // if (parent != null) {
	// // GlobalContinuous parentPosition = parent.getMarioPos();
	// // float dx = parentPosition.x - marioPosition.x;
	// // // if(dx == 0) hash += 0;
	// // if(dx < 0)
	// // hash += 1;
	// // if(dx > 0)
	// // hash += 2;
	// // }
	//
	// return hash;
	// }
}
