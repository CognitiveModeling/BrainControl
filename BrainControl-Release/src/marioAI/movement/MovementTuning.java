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

import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.util.GeneralUtil;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * This class contains tuning values for the A* search as well as the definition of their change over time as the goal is approached.
 * 
 * @author Niels, Eduard
 * 
 */

public abstract class MovementTuning {

	/**
	 * The initial tuning values to which we reset the dynamically changed 'current-' values once we reach the goal
	 */

	// How many steps Mario takes in the Simulation. A higher value improves
	// performance a lot at cost of accuracy.
	private static final int INITIAL_ACTIONS_PER_NODE = 1;// 5;

	// How fine grained the potential field should be (Shift by 1: one 16x16
	// tile has one heuristic value, ... , up to shift by 4 where every pixel
	// has its own heuristic value). The shift must be in {1,2,3,4}. A finer
	// grained field makes A* find better paths (due to less overestimating) but
	// is expensive (but only really in the overlay).
	private static final int INITIAL_GRAIN_FACTOR = CoordinatesUtil.getGrainFactor();

	// Factor by which the heuristic is multiplied
	// - to adjust for different scales heuristic <-> costFromStart
	// - allow the heuristic to be differently weighted

	private static final double INITIAL_HEURISTIC_WEIGHT = 1.7;//3.5;


	// Radius of a circle around the target in pixel in which Mario is
	// PS: doesn't seem like a circle to me
	// considered to be in the goal. This ONLY applies to intermediate goals.
//	public static final float INITIAL_FINE_GOAL_POSITION_TOLERANCE = 20.0f;		//was 1
//	public static final float INITIAL_COARSE_GOAL_POSITION_TOLERANCE = 20.0f;	//was 12
	public static final float INITIAL_GOAL_POSITION_TOLERANCE = 4.0f;
	
	//TEST: avoid to overshoot goals:
	public static final float INITIAL_GOAL_VELOCITY_ERROR_TOLERANCE = 0.2f;

	// Ratio of already traveled distance to distance to goal above which a new
	// path search is started.
	private static final double INITIAL_RECALCULATION_CRITERION = Double.POSITIVE_INFINITY; // default:
																							// 0.6
																							// or
																							// >1.
																							// 0.6:
																							// recalculate
																							// after
																							// 60%
																							// of
																							// the
																							// distance(start,
																							// goal)
																							// has
																							// been
																							// traversed.
																							// >=1:
																							// no
																							// recalculation

	// Lower distance bound for recalculation to prevent from becoming
	// infinitely accurate.
	private static final double INITIAL_LOWER_RECALCULATION_BOUND = 5.0;

	// Resolution of the keys within the observation. A value of one is
	// pixel-acurate while a value of 0.5 considers a 2x2 area to be the same.
	// Low values reduce the search space which increases performance but may
	// lead to accuracy errors. Higher values than 1 have no effect.
	private static final double INITIAL_KEYCODE_ACCURACY = 0.5;

	/** Public, dynamically changing values the search operates with */
	//TODO: are those "variables" adapted a single time? i don't see that... btw this wouldn't work for parallel planning since these variables are static...
	public static int currentActionsPerNode = INITIAL_ACTIONS_PER_NODE;
	public static int currentGrainFactor = INITIAL_GRAIN_FACTOR;
	public static double currentHeuristicWeight = INITIAL_HEURISTIC_WEIGHT;
	//public static float currentGoalDistanceError = INITIAL_GOAL_DISTANCE_ERROR;
	public static double currentRecalculationCriterion = INITIAL_RECALCULATION_CRITERION;
	public static double currentLowerRecalculationBound = INITIAL_LOWER_RECALCULATION_BOUND;
	public static double currentKeyCodeAccuracy = INITIAL_KEYCODE_ACCURACY;
	
	
	/**
	 * How to adjust the tuning values when we get closer and closer to the goal. This gets called once per recalculation of the search.
	 */
	public static void updateTuningValues() {
	}

	/**
	 * After the search for the goal is finally finished (and not just a recalculation mid-way) the values are reset to their initial values.
	 */
	public static void resetTuningValues() {
		
	}

	public static int getCurrentActionsPerNode(SimulatedLevelScene simulatedLevelScene, WorldGoalTester worldGoalTester) {
		/**
		 * Info about mario capabilities: Highest acceleration is about 1 in x-direction and 3 in y-direction. Fastest speed in x-direction is about 10 due to
		 * friction, fastest during jumping about 13. Most importantly, a jump takes about 14 time steps
		 */
		// Constants used:
		final double minVelocity = 2; // mario could accelerate any moment
		final double maxVelocity = 13;
		final int maxNumberOfSuccessiveSteps = 5; // upper bound mainly because
													// of jumps
		final double approximateNumberOfCommandsUntilGoalIsReached = 8; // how  often  the  A*  has  to  decide on new  actions until mario  is at the goal.

		/*
		 * Find out, how long mario would take to reach the goal at his current speed
		 */
		/* first get the distance between mario's boundary and the goal */
		GlobalContinuous goalPos = worldGoalTester.getGoalPos().toGlobalContinuous();
		GlobalContinuous playerPos = simulatedLevelScene.getPlanningPlayer().getPositionOfCenter();
		
		//c2 removed PLAYER 
		double dx = Math.abs(goalPos.x - playerPos.x) - 0.5 * simulatedLevelScene.getPlanningPlayer().type.getWidthForCollisionDetection();
//		double dx = Math.abs(goalPos.x - playerPos.x) - 0.5 * PlayerWorldObject.PLAYER.getWidthForCollisionDetection();
		double dy = Math.abs(goalPos.y - playerPos.y) - 0.5 * simulatedLevelScene.getPlanningPlayer().getHeight();
		double requiredDistance = Math.sqrt(dx * dx + dy * dy);
		// without boundary, this would simplify to:
		// double requiredDistance =
		// worldGoalTester.getGoalPos().toGlobalContinuous().distance(worldNode.getMarioPos());

		GlobalContinuous currentPlayerVelocity = simulatedLevelScene.getPlanningPlayer().getVelocity(0);
		/*
		 * use absolute velocity. This could be differentiated by using different weights for x and y direction.
		 */
		double absoluteVelocity = Math.sqrt(currentPlayerVelocity.x * currentPlayerVelocity.x + currentPlayerVelocity.y * currentPlayerVelocity.y);
		absoluteVelocity = GeneralUtil.getInBounds(absoluteVelocity, minVelocity, maxVelocity);

		double timeStepsNeeded = requiredDistance / absoluteVelocity;
		int numberOfSuccessiveSteps = (int) (timeStepsNeeded / approximateNumberOfCommandsUntilGoalIsReached);
		return Math.max(1, Math.min(numberOfSuccessiveSteps, maxNumberOfSuccessiveSteps));
	}
	
}
