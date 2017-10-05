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
package marioAI.movement.heuristics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.Goal;
import marioAI.movement.MovementTuning;
import marioAI.movement.WorldGoalTester;
import marioAI.movement.WorldNode;
import marioWorld.engine.ILevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.BlockingType;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeCoarse;
import marioWorld.engine.coordinates.ObservationRelativeFineGrain;
import marioWorld.mario.environments.Environment;

/**
 * Implementation of a heuristic using a potential field. That is, spreading
 * 'potential' from the goal node to other nodes.
 * 
 * @author Niels, Eduard, Marcel, Jonas E.
 * 
 * 
 * 
 */
public class FineGrainPotentialField extends WorldNodeHeuristic {

//	// A list that saves all points where jumping is possible. Used to show
//	// those points in the overlay.
//	public ArrayList<GlobalContinuous> jumpPoints = new ArrayList<GlobalContinuous>();
	
	// Mario's jump speed as factor of horizontal speed (is about 2-3)
	// Since Mario moves in y direction faster than in x the potential field
	// employs this
	private static final double JUMP_SPEED_RATIO = 1;

	// Stores if a point was already visited during expansion.
	final boolean[][] visited;

	// Stores the points in order of their expansion.
	final PriorityQueue<ObservationRelativeFineGrain> openSet;

	// Stores if a point is path blocking.
	final BlockingType[][] pathBlocking;
	
	boolean anticipateObjectDestruction;
	boolean expandsFromGoalToPlayer;

	// Map that saves all values needed for the heuristic's calculation. First 2
	// dimension are the coordinates. Third dimension stores alternative values.
	private HeuristicParameters[][][] heuristicMap;

	private static final int numPreviousCellsForGradient = 4;

	public static final double MAX_JUMP_VALUE = 4 * CoordinatesUtil.UNITS_PER_TILE / MovementTuning.currentGrainFactor;

	/**
	 * Constructs a new potential field given an array of IDs of the game world
	 * and a goal position within that part of the level.
	 * 
	 * @param staticObservation
	 *            array of IDs
	 * @param expandFromGoals
	 *            goalTester including the goal we want to reach
	 * @param goalPosition
	 *            index-based position of the goal in the level array
	 */
	public FineGrainPotentialField
	(
			PlayerWorldObject[][] staticObservation,
			WorldGoalTester expandFromGoals, 
			GlobalContinuous expandToPosition,			
			boolean anticipateObjectDestruction,
			boolean expandsFromGoalToPlayer
	) 
	{	
		super(expandFromGoals, expandToPosition);
		
		this.expandsFromGoalToPlayer=expandsFromGoalToPlayer;
		this.anticipateObjectDestruction=anticipateObjectDestruction;
		
		this.observation = staticObservation;
		this.lengthX = observation[0].length * MovementTuning.currentGrainFactor;
		this.lengthY = observation.length * MovementTuning.currentGrainFactor;
		visited = new boolean[lengthY][lengthX];
		pathBlocking = new BlockingType[lengthY][lengthX];
		
		List<ObservationRelativeFineGrain> goalRelFine = new LinkedList<>();

		Goal goal = expandFromGoals.getGoal();
		//for (Goal goal : expandFromGoals.getGoalAlternatives().getGoalsAsList()) 
		{
			GlobalCoarse globalCoarse = goal.getPosition();
			
			ObservationRelativeFineGrain fine = globalCoarse.toObservationRelativeFineGrain(expandToPosition);
			ObservationRelativeCoarse coarse = globalCoarse.toObservationRelativeCoarse(expandToPosition);
			
			PlayerWorldObject objectAtGoal = null;
			
			if ((coarse.y >= 0) && (coarse.y < observation.length) && (coarse.x >= 0) && (coarse.x < observation[coarse.y].length)) 
			{
				objectAtGoal = observation[coarse.y][coarse.x];
			}
			
			if (goal.getDirection() != CollisionDirection.IRRELEVANT && objectAtGoal != null && objectAtGoal.blocksPath == BlockingType.FULL)
			{
				fine = shiftOppositeTo(coarse, goal.getDirection()).toObservationRelativeFineGrain();
			}
			
			goalRelFine.add(fine);
		}

//		//TODO: this is a workaround:
//		if(pCoarse.y<0)
//			pCoarse.y=0;
//		if(pCoarse.x<0)
//			pCoarse.x=0;
//		if(pCoarse.x>=observation[0].length)
//			pCoarse.x=observation[0].length-1;
//		if(pCoarse.y>=observation.length)
//			pCoarse.y=observation.length-1;

		this.heuristicMap = new HeuristicParameters[lengthY][lengthX][2];

		this.openSet = new PriorityQueue<ObservationRelativeFineGrain>(lengthX * lengthY, new DijkstraComparator(this.heuristicMap));

		this.distanceMap = this.generateMap(goalRelFine);
	}

	private ObservationRelativeCoarse shiftOppositeTo(ObservationRelativeCoarse p, CollisionDirection dir) 
	{
		if (dir == null)
			return p.add(new ObservationRelativeCoarse(0, 0));
		switch (dir) 
		{
		case ABOVE:
			return p.subtract(new ObservationRelativeCoarse(0, 1));
		case LEFT:
			return p.subtract(new ObservationRelativeCoarse(1, 0));
		case BELOW:
			return p.add(new ObservationRelativeCoarse(0, 1));
		case RIGHT:
			return p.add(new ObservationRelativeCoarse(1, 0));
		default:
			return p;
//		default:
//			throw new IllegalArgumentException(
//					"In FineGrainPotentialField.shiftOppositeTo. Direction was: "
//							+ dir.toString());
		}
	}

	/**
	 * Returns the value of the potential field at the given WorldNode.
	 * 
	 * @param node
	 *            The WorldNode to get the distance-heuristic to.
	 * @result the heuristic value of node.
	 */

	@Override
	public double heuristic(WorldNode node) {
		ILevelScene state = node.getWorldState();
		GlobalContinuous currentPlayerPoint = state.getPlanningPlayerPos().clone();
		ObservationRelativeFineGrain relPlayerPos = currentPlayerPoint
				.toObservationRelativeFineGrain(node.initialPlayerPosition);

		if (relPlayerPos.x < 0 || relPlayerPos.x >= this.lengthX
				|| relPlayerPos.y < 0 || relPlayerPos.y >= this.lengthY) {
			return FineGrainPotentialField.MAX_VALUE;
		} else {
			return this.heuristicMap[relPlayerPos.y][relPlayerPos.x][0]
					.distance;
		}
	}

	/**
	 * Returns an array of doubles representing the potential field.
	 * 
	 * @result Potential field.
	 */
	@Override
	public double[][] getMap() {
		return distanceMap;
	}

	@Override
	public HeuristicParameters[][][] getHeuristicMap() {
		return heuristicMap;
	}

	/**
	 * Returns in which directions - if any - the specified tile is blocking
	 * 
	 * @param p
	 *            the point from which we want to check.
	 * @return the BlockingType of p.
	 */
//	private BlockingType isPathBlocking(ObservationRelativeCoarse c) 
//	{
//		if (observation[c.y][c.x] == null)
//			return BlockingType.FULL;
//		return observation[c.y][c.x].blocksPath;
//	}

//	private BlockingType isPathBlocking(ObservationRelativeFineGrain p) 
//	{
//		ObservationRelativeCoarse pCoarse = p.toObservationRelativeCoarse();
//		
//		//TEST: assume that the map is blocking at the top:
//		if(pCoarse.y < 0)
//		{
//			return BlockingType.FULL;
//		}
////		//TEST: assume that the observation is blocking left:
////		if(pCoarse.x < 0)
////			return BlockingType.FULL;
////		//TEST: assume that the observation is blocking right:
////		if(pCoarse.x >= observation[0].length)
////			return BlockingType.FULL;
//		
//		//objects outside the observation are considered non blocking (to avoid planning walljumps at the border of the observation)
//		if (!pCoarse.isWithinObservation() || observation[pCoarse.y][pCoarse.x]==null)
//			return BlockingType.NONE;
//		
//		BlockingType baseCellBlocking = observation[pCoarse.y][pCoarse.x].blocksPath;
//				
//		switch (baseCellBlocking) 
//		{
//		default:
//		case NONE:
//			return baseCellBlocking;
//		case FULL:
//			//get knowledge and check if player knows that he can destroy that thing... actually DIRTY HACK
//			SimulatedPlayer player = goalTester.getScene().getPlayer();
//			PlayerHealthCondition health = PlayerHealthCondition.fromInt(player.getHealth(), player.wasInvulnerable(), false);
//			Condition condition = new Condition(PlayerWorldObject.PLAYER,observation[pCoarse.y][pCoarse.x],health);		
//			ConditionDirectionPair cdp = new ConditionDirectionPair(condition, CollisionDirection.BELOW); 			
//			if(goalTester.getScene().getKnowledge().get(cdp) != null)	//agent knows something
//			{
//				Double destructionknowledge = goalTester.getScene().getKnowledge().get(cdp).get(new Effect(ActionEffect.OBJECT_DESTRUCTION,observation[pCoarse.y][pCoarse.x]));
//				
//				//avoid setting goals when knowledge about object destruction is below threshold
//				if(destructionknowledge == null || destructionknowledge<SimulatedLevelScene.anticipationThreshold)
//					return baseCellBlocking;
//				
//				//else this is considered to block only from the top.... CHECK: not at all? 
//				
//				if (p.y % MovementTuning.currentGrainFactor == 0)
//					//return BlockingType.NONE;	//TEST
//					return BlockingType.TOP;
//				else
//					return BlockingType.NONE;	//???
//			}
//			else
//				return baseCellBlocking;
//		case TOP:
//			if (p.y % MovementTuning.currentGrainFactor == 0)
//				return BlockingType.TOP;
//			else
//				return BlockingType.NONE;
//		case BOTTOM:			
//			if (p.y % MovementTuning.currentGrainFactor == MovementTuning.currentGrainFactor - 1)
//				return BlockingType.BOTTOM;
//			else
//				return BlockingType.NONE;
//		}
//	}

	/**
	 * Returns whether the way between two diagonally adjacent tiles is blocked
	 * by the other two adjacent tiles. Since Mario cannot move diagonally if
	 * one or both of the neighbor tiles are blocking, the potential field does
	 * not spread in this direction
	 * 
	 * @param start
	 * @param goal
	 * @return
	 */
//	private boolean diagonallyBlocking(ObservationRelativeFineGrain start,
//			ObservationRelativeFineGrain goal) {
//		ObservationRelativeFineGrain upperNeighbor = new ObservationRelativeFineGrain();
//		ObservationRelativeFineGrain lowerNeighbor = new ObservationRelativeFineGrain();
//		boolean comingFromAbove = start.y < goal.y;
//
//		if (!diagonallyAdjacent(start, goal)) // if tiles are not diagonally
//			// adjacent this problem does
//			// not occur
//			return false;
//
//		// determine the two neighbors positions
//		upperNeighbor.x = comingFromAbove ? goal.x : start.x;
//		upperNeighbor.y = comingFromAbove ? start.y : goal.y;
//		lowerNeighbor.x = comingFromAbove ? start.x : goal.x;
//		lowerNeighbor.y = comingFromAbove ? goal.y : start.y;
//
//		return isPathBlocking(lowerNeighbor) == BlockingType.FULL
//				|| isPathBlocking(upperNeighbor) == BlockingType.FULL
//				|| isPathBlocking(lowerNeighbor) == BlockingType.BOTTOM
//				|| isPathBlocking(upperNeighbor) == BlockingType.TOP;
//	}

	/**
	 * Determines if two tiles are diagonally adjacent
	 * 
	 * @param a
	 *            first point
	 * @param b
	 *            second point
	 * @return
	 */
//	private boolean diagonallyAdjacent(ObservationRelativeFineGrain a,
//			ObservationRelativeFineGrain b) {
//		int dx = Math.abs(a.x - b.x);
//		int dy = Math.abs(a.y - b.y);
//		return dx == 1 && dy == 1;
//	}

	/**
	 * Returns whether a coordinate is deemed valid, that is, not exceeding the
	 * level array's indices.
	 * 
	 * @param p
	 * @return
	 */
	private boolean isValidArrayCoordinate(ObservationRelativeFineGrain p) 	
	{
		if(p.toGlobalCoarse(goalTester.getScene().getPlanningPlayer().getPosition(0)).y<0)
			return false;
		
		int x = p.x;
		int y = p.y;
		return x >= 0 && x < lengthX && y >= 0 && y < lengthY;
	}

	/**
	 * Check if we may expand the heuristic from a given start point to the
	 * specified neighbor
	 */
	private boolean isPathBlocking(ObservationRelativeFineGrain from, ObservationRelativeFineGrain to) 
	{
//		boolean expandingFromBelow = from.y > to.y;
//		boolean expandingFromAbove = from.y < to.y;
//
//		BlockingType toBlocking = pathBlocking[to.y][to.x];
//		BlockingType fromBlocking = pathBlocking[from.y][from.x];
//
//		return (toBlocking == BlockingType.FULL) || 
//			   (fromBlocking == BlockingType.FULL) ||
//			   (expandingFromBelow && toBlocking == BlockingType.BOTTOM) ||
//			   (expandingFromAbove && toBlocking == BlockingType.TOP);

		
		int dx = to.x-from.x;
		int dy = to.y-from.y;
		
		if(from.y+dy >= pathBlocking.length || from.y+dy < 0 || from.x+dx >= pathBlocking[0].length || from.x+dx < 0)
			return true;

		boolean blockingXFirst = false;
		boolean blockingYFirst = false;
		
		BlockingType bottom=BlockingType.BOTTOM;
		BlockingType top=BlockingType.TOP;
		
		//special case: opposite propagation direction, swap this
		if(expandsFromGoalToPlayer)
		{
			bottom=BlockingType.TOP;
			top=BlockingType.BOTTOM;			
		}
				
		ObservationRelativeFineGrain p = from.clone();		
		if(pathBlocking[p.y][p.x]==BlockingType.FULL)
			blockingXFirst=true;
		//move x first:
		p.x+=dx;
		if(pathBlocking[p.y][p.x]==BlockingType.FULL)
			blockingXFirst=true;
		//move y:
		p.y+=dy;
		if(pathBlocking[p.y][p.x]==BlockingType.FULL || (dy < 0 && pathBlocking[p.y][p.x]==bottom) || (dy > 0 && pathBlocking[p.y][p.x]==top))
			blockingXFirst=true;

		p = from.clone();		
		if(pathBlocking[p.y][p.x]==BlockingType.FULL)
			blockingYFirst=true;
		//move y first:
		p.y+=dy;
		if(pathBlocking[p.y][p.x]==BlockingType.FULL || (dy < 0 && pathBlocking[p.y][p.x]==bottom) || (dy > 0 && pathBlocking[p.y][p.x]==top))
			blockingYFirst=true;		
		//move x:
		p.x+=dx;
		if(pathBlocking[p.y][p.x]==BlockingType.FULL)
			blockingYFirst=true;
		
		return blockingXFirst && blockingYFirst;
		
//		int dx = to.x-from.x;
//		int dy = to.y-from.y;
//		
//		BlockingType[][] blocking = new BlockingType[3][3];
//		
//		for(int y=-1;y<=1;y++)
//			for(int x=-1;x<=1;x++)
//			{
//				if(from.y+y >= pathBlocking.length || from.y+y < 0 || from.x+x >= pathBlocking[0].length || from.x+x < 0)
//					blocking[y+1][x+1]=BlockingType.FULL;
//				else
//					blocking[y+1][x+1]=pathBlocking[from.y+y][from.x+x];
//			}
//		
//		if(blocking[1][1]==BlockingType.FULL || blocking[1+dy][1+dx]==BlockingType.FULL)	//from or to positions are blocking
//			return true;
//		
//		if((dx > 0) && dy == 0)	//to the right
//			return blocking[0][1] == BlockingType.FULL ||
//				   blocking[0][2] == BlockingType.FULL ||
//				   blocking[1][1] == BlockingType.FULL ||
//				   blocking[1][2] == BlockingType.FULL;
//		if((dx < 0) && dy == 0)	//left
//			return blocking[0][0] == BlockingType.FULL ||
//				   blocking[0][1] == BlockingType.FULL ||
//				   blocking[1][0] == BlockingType.FULL ||
//				   blocking[1][1] == BlockingType.FULL;
//		if((dx == 0) && dy < 0)	//top
//			return blocking[1][1] == BlockingType.FULL ||
//				   blocking[0][1] == BlockingType.FULL ||
//				   blocking[0][1] == BlockingType.BOTTOM;
//		if((dx == 0) && dy > 0)	//bottom
//			return blocking[1][1] == BlockingType.FULL ||
//				   blocking[2][1] == BlockingType.FULL ||
//				   blocking[2][1] == BlockingType.TOP;
//		
//		if((dx > 0) && (dy < 0))	//to the top right
//			return blocking[0][1] == BlockingType.FULL ||
//				   blocking[0][2] == BlockingType.FULL ||
//				   blocking[1][1] == BlockingType.FULL ||
//				   blocking[1][2] == BlockingType.FULL ||
//				   blocking[0][1] == BlockingType.BOTTOM ||
//				   blocking[0][2] == BlockingType.BOTTOM;
//		if((dx < 0) && (dy < 0))	//top left
//			return blocking[0][0] == BlockingType.FULL ||
//				   blocking[0][1] == BlockingType.FULL ||
//				   blocking[1][0] == BlockingType.FULL ||
//				   blocking[1][1] == BlockingType.FULL ||
//				   blocking[0][0] == BlockingType.BOTTOM ||
//				   blocking[0][1] == BlockingType.BOTTOM;
//		if((dx > 0) && (dy > 0))	//bottom right
//			return (blocking[1][1] == BlockingType.FULL ||
//				   blocking[1][2] == BlockingType.FULL ||
//			       blocking[2][1] == BlockingType.FULL ||
//			       blocking[2][2] == BlockingType.FULL ||
//	    		   blocking[2][1] == BlockingType.TOP ||
//			       blocking[2][2] == BlockingType.TOP) ;
//		if((dx < 0) && (dy > 0))	//bottom left
//			return blocking[1][1] == BlockingType.FULL ||
//			       blocking[1][0] == BlockingType.FULL ||
//			       blocking[2][1] == BlockingType.FULL ||
//			       blocking[2][0] == BlockingType.FULL ||
//			       blocking[2][0] == BlockingType.TOP ||
//			       blocking[2][1] == BlockingType.TOP;
//		
//		return false;				   
	}

	/**
	 * Generates the heuristic Map with all its values. Returns only a map with
	 * the potential field's distances.
	 * 
	 * @param goalPositions
	 *            Position of the goal in the level.
	 * @result the potential field as an array of doubles.
	 */
	@Override
	public double[][] generateMap(List<ObservationRelativeFineGrain> goalPositions) 
	{
		initializeField(goalPositions);		
		
		//TODO / TEST: lets try this very simple approach for jump values... blocks are reachable by walking or jumping if there is at least one blocking block below it in a specific area:
		//this is to test if one will be able to jump UPWARDS from this position, not if falling down is possible...
//		for (int y = 0; y < lengthY; y++) 
//		{
//			for (int x = 0; x < lengthX; x++) 
//			{				
//				this.heuristicMap[y][x][0].jumpValue=MAX_VALUE;
//				this.heuristicMap[y][x][1].jumpValue=MAX_VALUE;
//			}
//		}
//		
//		for (int y = 0; y < lengthY; y++) 
//		{
//			for (int x = 0; x < lengthX; x++) 
//			{				
//				ObservationRelativeFineGrain pointRelative = new ObservationRelativeFineGrain(x, y);
//				BlockingType currentBT = this.pathBlocking[pointRelative.y][pointRelative.x];				
//				
//				if(currentBT!=BlockingType.NONE)
//				{
//					this.heuristicMap[y][x][0].jumpValue=0;
//					this.heuristicMap[y][x][1].jumpValue=0;
//					
//					for (int dy = 1; dy >= -5; dy--)
//					{
//						for (int dx = -4; dx <= 4; dx++)
//						{
//							if(Math.sqrt(dx*dx+dy*dy)>6)
//								continue;
//							
//							if(x+dx<0 || y+dy<0 || y+dy >= this.heuristicMap.length || x+dx >= this.heuristicMap[0].length)
//								continue;
//							
//							this.heuristicMap[y+dy][x+dx][0].jumpValue = Math.min(this.heuristicMap[y+dy][x+dx][0].jumpValue, Math.abs(dy)+Math.abs(dx));
//							this.heuristicMap[y+dy][x+dx][1].jumpValue = Math.min(this.heuristicMap[y+dy][x+dx][1].jumpValue, Math.abs(dy)+Math.abs(dx));
//						}
//					}		
//				}				
//			}
//		}	
				
//		//TEST: set unreachable space to "full blocking" to block the potential field:
//		for (int y = 0; y < lengthY; y++) 
//		{
//			for (int x = 0; x < lengthX; x++) 
//			{				
//				ObservationRelativeFineGrain pointRelative = new ObservationRelativeFineGrain(x, y);
//				if(heuristicMap[y][x][0].jumpValue==MAX_VALUE)
//					this.pathBlocking[pointRelative.y][pointRelative.x]=BlockingType.FULL;				
//			}
//		}
		
		
		//TEST: set left and right border of the screen to unreachable!
		/*for (int y = 0; y < lengthY; y++) 
		{
			for (int x = 0; x < lengthX; x++) 
			{						
				if(x==0 || x>lengthX-2)
				{
					pathBlocking[y][x]=BlockingType.FULL;				
				}
			}
		}*/		
		
		while (!openSet.isEmpty()) 
		{
			ObservationRelativeFineGrain current = openSet.poll();
			expandNode(current);
		}
		
		return extractDistanceMap();
	}

	/**
	 * Expands a node.
	 * 
	 * @param current
	 */
	private void expandNode(ObservationRelativeFineGrain current) {

		// if we only moved up till now, visit all neighbors, else only the one below

		int direction = (this.heuristicMap[current.y][current.x][0].hasOnlyMovedUp ? -1
				: 0);

		for (int dx = -1; dx <= 1; dx++) 
		{
			for (int dy = direction; dy <= 1; dy++) 
			{
				boolean movingVertically = dy != 0;
				final int newX = current.x + dx;
				final int newY = current.y + dy;
				ObservationRelativeFineGrain toVisit = new ObservationRelativeFineGrain(newX, newY);

				// If neighbor is within observation, not yet visited and
				// the path not blocked set the new values

				if (
						isValidArrayCoordinate(toVisit)
						&& !visited[newY][newX]
								/**
								 * TODO: isPathBlocking lets blocking goals not find a sequence to it (e.g. hit quesiton mark block - direciton irrelevant)
								 */
//						&& !isPathBlocking(current,toVisit)
						//&& !diagonallyBlocking(current, toVisit)
						//&& this.heuristicMap[current.y][current.x][0].jumpValue <= FineGrainPotentialField.MAX_JUMP_VALUE
					)
				{
					HeuristicParameters newHeuristicParameters = new HeuristicParameters(
							newY,
							newX,
							this.heuristicMap[current.y][current.x][0].distance
									+ (movingVertically ? 1 / JUMP_SPEED_RATIO
											: 1)
									* (CoordinatesUtil.UNITS_PER_TILE / MovementTuning.currentGrainFactor),
							this.heuristicMap[current.y][current.x][0].jumpValue,
							true);

					HeuristicParameters newAltHeuristicParameters = new HeuristicParameters(
							newY,
							newX,
							this.heuristicMap[current.y][current.x][1].distance
									+ (movingVertically ? 1 / JUMP_SPEED_RATIO
											: 1)
									* (CoordinatesUtil.UNITS_PER_TILE / MovementTuning.currentGrainFactor),
							this.heuristicMap[current.y][current.x][1].jumpValue,
							true);

					boolean movingDown = (dy > 0);

					// if moving down, update the values
					if (movingDown) 
					{
						//disabled:
						/*newHeuristicParameters.jumpValue += (CoordinatesUtil.UNITS_PER_TILE / MovementTuning.currentGrainFactor);
						newAltHeuristicParameters.jumpValue += (CoordinatesUtil.UNITS_PER_TILE / MovementTuning.currentGrainFactor);
						if (newHeuristicParameters.jumpValue > MAX_JUMP_VALUE) {
							newHeuristicParameters.distance = FineGrainPotentialField.MAX_VALUE;
						}
						if (newAltHeuristicParameters.jumpValue > MAX_JUMP_VALUE) {
							newAltHeuristicParameters.distance = FineGrainPotentialField.MAX_VALUE;
						}*/
						newHeuristicParameters.hasOnlyMovedUp = false;
						newAltHeuristicParameters.hasOnlyMovedUp = false;
					}

//					// if jumping is possible, update the values
//					if (isJumpPossible(toVisit)) 
//					{
//						//disabled:
//						/*newHeuristicParameters.jumpValue = 0;
//						newAltHeuristicParameters.jumpValue = 0;*/
//						newHeuristicParameters.hasOnlyMovedUp = true;
//						newAltHeuristicParameters.hasOnlyMovedUp = true;
//					}					

					// check if new path is shorter and update the old values if
					// needed.
					if (newHeuristicParameters
							.compareTo(this.heuristicMap[newY][newX][0]) < 0) {
						if (this.heuristicMap[newY][newX][0].gradientChangeable) {
							newHeuristicParameters
									.setParent(this.heuristicMap[current.y][current.x][0]);
						}
						updateParameters(dy, toVisit, newHeuristicParameters, 0);
					}
					
					//TEST: disabled
					/*if (newAltHeuristicParameters.jumpValue < this.heuristicMap[newY][newX][1].jumpValue) {
						if (this.heuristicMap[newY][newX][0].gradientChangeable) {
							newAltHeuristicParameters
									.setParent(this.heuristicMap[current.y][current.x][0]);
						}

						updateParameters(dy, toVisit,
								newAltHeuristicParameters, 1);
					}

					if (this.heuristicMap[newY][newX][0].jumpValue >= MAX_JUMP_VALUE
							&& this.heuristicMap[newY][newX][1].jumpValue < MAX_JUMP_VALUE) {
						updateParameters(dy, toVisit,
								this.heuristicMap[newY][newX][1], 0);
					}*/

				}
			}
		}

		// mark node as visited
		visited[current.y][current.x] = true;
	}

	/**
	 * Sets new heuristic values for a node.
	 * 
	 * @param dy
	 *            : y-direction of the expansion
	 * @param toVisit
	 *            : position of the point, that is updated
	 * @param newHeuristicParameters
	 *            : new Parameters
	 * @param dim
	 *            : which dimension of the map will be updated
	 */
	private void updateParameters(int dy, ObservationRelativeFineGrain toVisit,
			HeuristicParameters newHeuristicParameters, int dim) 
	{
		openSet.remove(toVisit);
		this.heuristicMap[toVisit.y][toVisit.x][dim] = newHeuristicParameters;
		openSet.add(toVisit);
		setNeighborsNonVisited(toVisit.y, toVisit.x);

	}

	/**
	 * Extracts the map with only the distance values as it is used by A*
	 * 
	 * @return Map with distances for heuristic points.
	 */
	public double[][] extractDistanceMap() 
	{
		double[][] distanceOnlyMap = new double[lengthY][lengthX];

		for (int y = 0; y < this.heuristicMap.length; y++) 
		{
			for (int x = 0; x < this.heuristicMap[0].length; x++) 
			{				
				distanceOnlyMap[y][x] = this.heuristicMap[y][x][0].distance;
				
//				//TEST: punish definitely unreachable regions
//				if(this.heuristicMap[y][x][0].jumpValue==MAX_VALUE)
//					distanceOnlyMap[y][x]+=1000;
			}
		}		
		
		return distanceOnlyMap;
	}

	private boolean isWithinGoal(ObservationRelativeFineGrain goal, ObservationRelativeFineGrain pos) 
	{
//		GlobalContinuous pointGlobal = pos.toGlobalContinuous(initialMarioPosition);
//		return goalTester.isWithinGoalThresholds(pointGlobal);
		return goalTester.isWithinGoalThresholds(goal, pos, 16.0);
	}
	
	/**
	 * Iterate over the field. If a point is in the goal and its path is not
	 * block, set the distance to 0 and add it to the open set. Else set its
	 * distance to infinite.
	 */

	private void initializeField(List<ObservationRelativeFineGrain> goalPositions) 
	{		
		for (int y = 0; y < lengthY; y++) 
		{
			for (int x = 0; x < lengthX; x++) 
			{
				ObservationRelativeFineGrain pointRelative = new ObservationRelativeFineGrain(x, y);
				
				//BlockingType currentBT = isPathBlocking(pointRelative);
				BlockingType currentBT = observation[pointRelative.y][pointRelative.x].blocksPath;
				pathBlocking[y][x] = currentBT;

				//checking for object destructions is done by the schematic search!
				if(currentBT==BlockingType.FULL && anticipateObjectDestruction)
				{
					//get knowledge and check if player knows that he can destroy that thing... actually DIRTY HACK
					SimulatedPlayer player = goalTester.getScene().getPlanningPlayer();
					PlayerHealthCondition health = PlayerHealthCondition.fromInt(player.getHealth(), player.wasInvulnerable(), false);
					Condition condition = new Condition(player.getType(),observation[pointRelative.y][pointRelative.x],health);		
					ConditionDirectionPair cdp = new ConditionDirectionPair(condition, CollisionDirection.BELOW); 			
					if(goalTester.getScene().getKnowledge().get(cdp) != null)	//agent knows something
					{
						Double destructionknowledge = goalTester.getScene().getKnowledge().get(cdp).get(new Effect(ActionEffect.OBJECT_DESTRUCTION,observation[pointRelative.y][pointRelative.x]));
						
						//avoid setting goals when knowledge about object destruction is below threshold
						if(destructionknowledge != null && destructionknowledge>=SimulatedLevelScene.anticipationThreshold)
							pathBlocking[y][x] = BlockingType.NONE;	//TEST.... TOP didn't work!?
					}
				}
				
//				GlobalContinuous pointGlobal = pointRelative
//						.toGlobalContinuous(initialMarioPosition);
//				if(goalTester.isWithinGoalThresholds(pointGlobal)) {
//					if(currentBT == BlockingType.FULL && )
//				}
				
				//if (currentBT != BlockingType.FULL && goalTester.isWithinGoalThresholds(pointGlobal)) 				
				boolean found = false;
				for (ObservationRelativeFineGrain goalPosition : goalPositions) {
				if(isWithinGoal(goalPosition, pointRelative))
				{
					this.heuristicMap[y][x][0] = new HeuristicParameters(y, x, 0, 0, true, false);
					this.heuristicMap[y][x][1] = new HeuristicParameters(y, x, 0, 0, true, false);

					openSet.add(new ObservationRelativeFineGrain(x, y));
						found = true;
				} 
				}
				if (!found) {
					this.heuristicMap[y][x][0] = new HeuristicParameters(y, x,
							FineGrainPotentialField.MAX_VALUE,
							FineGrainPotentialField.MAX_VALUE, true);
					this.heuristicMap[y][x][1] = new HeuristicParameters(y, x,
							FineGrainPotentialField.MAX_VALUE,
							FineGrainPotentialField.MAX_VALUE, true);
				}				
			}
		}
	}

	/**
	 * Mark neighbors of a node as not visited again.
	 * 
	 * @param y
	 *            y-coordinate
	 * @param x
	 *            x-coordinate
	 */
	private void setNeighborsNonVisited(int y, int x) 
	{
		for (int dx = -1; dx <= 1; dx++) 
		{
			for (int dy = -1; dy <= 1; dy++) 
			{
				int neighborY = y + dy;
				int neighborX = x + dx;

				if (new ObservationRelativeFineGrain(neighborX, neighborY).isWithinObservation(initialPlayerPosition))
				{
					visited[neighborY][neighborX] = false;
					
					if (heuristicMap[neighborY][neighborX][0].distance != MAX_VALUE) 
					{
						heuristicMap[neighborY][neighborX][0].gradientChangeable = false;
					}
				}
			}
		}
	}

	/**
	 * 
	 * @return the map containing the jump values.
	 */
	public double[][] getJumpValueMap()
	{
		double[][] jumpValueMap = new double[lengthY][lengthX];

		for (int y = 0; y < heuristicMap.length; y++) 
		{
			for (int x = 0; x < heuristicMap[0].length; x++) 
			{
				jumpValueMap[y][x] = heuristicMap[y][x][0].jumpValue;
			}
		}
		return jumpValueMap;
	}

	/**
	 * 
	 * @param node
	 * @return jump values for node.
	 */
	public double jumpValue(WorldNode node) 
	{
		ILevelScene state = node.getWorldState();
		GlobalContinuous currentPlayerPoint = state.getPlanningPlayerPos().clone();
		ObservationRelativeFineGrain relPlayerPos = currentPlayerPoint
				.toObservationRelativeFineGrain(node.initialPlayerPosition);

		if (relPlayerPos.x < 0 || relPlayerPos.x >= this.lengthX
				|| relPlayerPos.y < 0 || relPlayerPos.y >= this.lengthY) {
			return FineGrainPotentialField.MAX_VALUE;
		} else {
			return this.heuristicMap[relPlayerPos.y][relPlayerPos.x][0].jumpValue;
		}
	}

//	/**
//	 * 
//	 * @param p
//	 *            point from which we want to check
//	 * @return true if a jump is possible at p
//	 */
//	private boolean isJumpPossible(ObservationRelativeFineGrain p) {
//		boolean jumpPossible = false;
//
//		ArrayList<ObservationRelativeFineGrain> pointsToCheck = new ArrayList<>();
//		ObservationRelativeFineGrain toCheckBottom = new ObservationRelativeFineGrain(p.x, p.y + 1);
//
//		// Checks if a jump below is possible.
//
//		if (toCheckBottom.isWithinObservation(initialPlayerPosition)
//				&& this.pathBlocking[toCheckBottom.y][toCheckBottom.x] != BlockingType.NONE) 
//		{
//			jumpPoints.add(toCheckBottom
//					.toGlobalContinuous(initialPlayerPosition));
//			return true;
//		}
//
//		// Checks if a jump on the side is possible.
//
//		pointsToCheck.add(new ObservationRelativeFineGrain(p.x + 1, p.y));
//		pointsToCheck.add(new ObservationRelativeFineGrain(p.x - 1, p.y));
//
//		for (ObservationRelativeFineGrain toCheck : pointsToCheck) 
//		{
//			jumpPossible = isJumpPossibleSidewards(toCheck);
//		}
//
//		return jumpPossible;
//
//	}

	/**
	 * returns the coordinates of a potential-field-cell in which the WorldNode
	 * ( parameter ) is located
	 */
	@Override
	public Point cellCoordinates(WorldNode worldNode) 
	{
		ObservationRelativeFineGrain pos = worldNode.getPlayerPos()
				.toObservationRelativeFineGrain(this.initialPlayerPosition);
		if (pos.x > 22) {
			pos.x = 22;
		} else if (pos.x < 0) {
			pos.x = 0;
		} else if (pos.y > 22) {
			pos.y = 22;
		} else if (pos.y < 0) {
			pos.y = 0;
		}
		return new Point(pos.x, pos.y);
	}

//	/**
//	 * checks if a jump sidewards (walljump) is possible. This needs at least 2
//	 * tiles with jumping possibilities.
//	 * 
//	 * @param toCheck
//	 *            : point to check
//	 * @return true if a walljump is possible.
//	 */
//
//	private boolean isJumpPossibleSidewards(ObservationRelativeFineGrain toCheck) 
//	{
//		boolean jumpPossible = false;
//		if (toCheck.isWithinObservation(initialPlayerPosition)) 
//		{
//			BlockingType pathBlockingToCheck = this.pathBlocking[toCheck.y][toCheck.x];
//
//			if (pathBlockingToCheck != BlockingType.NONE) 
//			{
//				ObservationRelativeCoarse toCheckCoarse = toCheck
//						.toObservationRelativeCoarse();
//				ObservationRelativeCoarse top = new ObservationRelativeCoarse(
//						toCheckCoarse.x, toCheckCoarse.y + 1);
//				ObservationRelativeCoarse bottom = new ObservationRelativeCoarse(
//						toCheckCoarse.x, toCheckCoarse.y - 1);
//				
//				if (bottom.isWithinObservation() && top.isWithinObservation()) 
//				{
//					BlockingType baseCellBlockingBottom = this.pathBlocking[bottom.y][bottom.x];
//					BlockingType baseCellBlockingTop = this.pathBlocking[top.y][top.x];
//
//					if (baseCellBlockingBottom == pathBlockingToCheck
//							|| baseCellBlockingTop == pathBlockingToCheck) 
//					{
//						jumpPossible = true;
//						jumpPoints.add(toCheck.toGlobalContinuous(initialPlayerPosition));
//					}
//
//				}
//
//			}
//		}
//		return jumpPossible;
//	}

	/**
	 * Comparator for Integer-coordinates which queries the distance field.
	 * 
	 * @author sebastian
	 * 
	 */
	class DijkstraComparator implements
			Comparator<ObservationRelativeFineGrain> {
		private HeuristicParameters[][][] distanceField;

		public DijkstraComparator(HeuristicParameters[][][] distanceField) {
			this.distanceField = distanceField;
		}

		@Override
		public int compare(ObservationRelativeFineGrain coord1,
				ObservationRelativeFineGrain coord2) {
			HeuristicParameters first = distanceField[coord1.y][coord1.x][0];
			HeuristicParameters second = distanceField[coord2.y][coord2.x][0];
			return first.compareTo(second);
		}

	}

	/**
	 * 
	 * Helper class to encapsulate all the values needed for the heuristic
	 * calculation. distance - distance between the goal and a point of the
	 * heuristic jumpValue - the distance which mario has been in the air
	 * hasOnlyMovedUp - true if the heuristic only expanded upwards
	 * 
	 * @author Marcel
	 * 
	 */

	public static final double relativeJumpWeight = 0.2;

	public static class HeuristicParameters implements
			Comparable<HeuristicParameters> {
		double distance;
		double jumpValue;			//TODO!!! the implementation of this is incorrect, thus disabled
		boolean hasOnlyMovedUp;
		boolean gradientChangeable;
		private final int row, col;
		private HeuristicParameters parent;
		private double[] gradient;

		public HeuristicParameters(int row, int col) {
			this.row = row;
			this.col = col;
			gradientChangeable = true;
		}

		public HeuristicParameters(int row, int col, double distance,
				double jumpValue, boolean hasOnlyMovedUp) {
			this(row, col);
			this.distance = distance;
			this.jumpValue = jumpValue;
			this.hasOnlyMovedUp = hasOnlyMovedUp;
			}

		public HeuristicParameters(int row, int col, double distance,
				double jumpValue, boolean hasOnlyMovedUp,
				boolean gradientChangeable) {
			this(row, col, distance, jumpValue, hasOnlyMovedUp);
			this.gradientChangeable = false;
		}

		public HeuristicParameters getParent() {
			return parent;
		}

		public double getWeightedSum() {
			return this.distance * (1. - relativeJumpWeight) + this.jumpValue
					* relativeJumpWeight;
		}

		@Override
		public int compareTo(HeuristicParameters o) {
			return Double.compare(this.getWeightedSum(), o.getWeightedSum());
		}

		public double[] getGradient() {
			return gradient;
		}

		@Override
		public String toString() {
			//abbreviate Infinity with Inf
			if(Double.isInfinite(distance))
				return (distance<0 ? "-" : "")+"Inf";
			return String.valueOf(distance);
		}
		
		public void setParent(HeuristicParameters parent) {
			this.parent = parent;
			if (parent == null) {
				this.gradient = null;
			} else {
				HeuristicParameters parentForGradient = this;
				for (int i = 0; i < numPreviousCellsForGradient; i++) {
					if (parentForGradient.parent == null) {
						break;
					}
					parentForGradient = parentForGradient.parent;
				}
				this.gradient = parent == null ? null : new double[] {
						parentForGradient.col - this.col,
						parentForGradient.row - this.row };
				double gradientLength = Math
						.sqrt(dotProduct(gradient, gradient));
				gradient[0] = gradient[0] / gradientLength;
				gradient[1] = gradient[1] / gradientLength;
			}
		}

		public static double dotProduct(double[] vector1, double[] vector2) {
			double dotProduct = 0;
			for (int i = 0; i < vector1.length; i++) {
				dotProduct += vector1[i] * vector2[i];
			}
			return dotProduct;
		}

		/**
		 * unit vector
		 * 
		 * @param start
		 * @param end
		 * @return
		 */
		public static double[] getDirection(WorldNode start, WorldNode end) {

			if (start == end) {
				return null;
			}
			GlobalContinuous startPos = start.getPlayerPos();
			GlobalContinuous endPos = end.getPlayerPos();

			double[] dir = new double[] { endPos.x - startPos.x,
					endPos.y - startPos.y };
			double vectorLength = Math.sqrt(dotProduct(dir, dir));

			return new double[] { dir[0] / vectorLength, dir[1] / vectorLength };
		}
	}
}
