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
package marioAI.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.Generalizer;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * Collection of static utility-methods
 * 
 * @author Stephan, Sebastian
 * 
 */
public abstract class GeneralUtil {

	/**
	 * Collapses a string array to a single string, using the specified
	 * separator between the single entries of the array.
	 */
	public static String collapse(String[] array, String separator) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(separator);
			}
			sb.append(array[i]);
		}

		return sb.toString();
	}

	/**
	 * Adds a marker to an array of strings. Given the following string array
	 * and x = 4, y = 1, line 0: ggggggggggg line 1: ggggggggggg
	 * 
	 * it will return line 0: ggggggggggg line 1:>ggggggggggg line 2: ^
	 * 
	 * @param array
	 * @param x
	 * @param y
	 * @return
	 */
	public static String[] addMarker(String[] array, int x, int y) {
		String[] marked = new String[array.length + 1];
		for (int i = 0; i < array.length; i++) {
			if (i == y)
				marked[i] = '>' + array[i];
			else
				marked[i] = ' ' + array[i];
		}
		int len = array[0].length();
		StringBuffer sb = new StringBuffer(len + 1);
		sb.append(' ');
		for (int i = 0; i < len; i++) {
			if (i == x)
				sb.append('^');
			else
				sb.append(' ');
		}
		marked[marked.length - 1] = sb.toString();
		return marked;
	}

	/**
	 * deep cloning of a 2d byte array
	 * 
	 * @param array
	 * @return
	 */
	public static byte[][] clone2DByteArray(byte[][] array) {
		byte[][] ret = new byte[array.length][array[0].length];
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret[0].length; j++) {
				ret[i][j] = array[i][j];
			}
		}
		return ret;
	}

	/**
	 * deep cloning of MarioWorldObjects
	 * 
	 * @param array
	 * @return
	 */
	public static PlayerWorldObject[][] clone2DPlayerWorldObjectArray(PlayerWorldObject[][] array) 
	{
		PlayerWorldObject[][] ret = new PlayerWorldObject[array.length][array[0].length];
		for (int i = 0; i < ret.length; i++) 
		{
			for (int j = 0; j < ret[0].length; j++) 
			{
				ret[i][j] = array[i][j]; // enum constants. no further clone required.
			}
		}
		return ret;
	}

	/**
	 * wrapper class. The information contained within is required as return
	 * value for a method.
	 * 
	 * @author Stephan
	 */
	public static class NearestObject implements Comparable<NearestObject> {
		/**
		 * In order to correctly infer the direction of an object relative to
		 * mario, it is more robust to use a positions before the interaction
		 * occured. This constant defines the time delay of observations, on
		 * which the direction is inferred.
		 */
		public static final int TIME_DELAY_FOR_SPRITES = 1;
		private final boolean isStatic;
		/** the position where this object was timeDelaySteps back in time */
		private final GlobalCoarse position;
		private final PlayerWorldObject playerWorldObject;
		private final double squaredDistanceToReference;

		/**
		 * Constructor. This only sets attributes (which are all final).
		 * 
		 * @param isStatic
		 * @param position
		 * @param playerWorldObject
		 */
		public NearestObject(boolean isStatic, GlobalCoarse point,
				PlayerWorldObject playerWorldObject,
				double squaredDistanceToReference) {
			this.isStatic = isStatic;
			this.position = point;
			this.playerWorldObject = playerWorldObject;
			this.squaredDistanceToReference = squaredDistanceToReference;
		}

		@Override
		public int compareTo(NearestObject other) {
			return (int) Math.signum(this.getSquaredDistanceToReference()
					- other.getSquaredDistanceToReference());
		}

		/**
		 * @return the position
		 */
		public GlobalCoarse getPosition() {
			return position;
		}

		/**
		 * @return the squaredDistanceToReference
		 */
		public double getSquaredDistanceToReference() {
			return squaredDistanceToReference;
		}

		/**
		 * @return the isStatic
		 */
		public boolean isStatic() {
			return isStatic;
		}

		/**
		 * @return the marioWorldObject
		 */
		public PlayerWorldObject getPlayerWorldObject() {
			return playerWorldObject;
		}

	}

	/**
	 * Computes a measure for inhibition based on how much is known about a
	 * ConditionDirectionPair.
	 * 
	 * @param knowledge
	 * @param collision
	 * @return inhibitionFactor
	 */

	public static double inhibitEffectsBasedOnKnowledge(
			TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge,
			ConditionDirectionPair collision) {
		final double inhibitAlsoUnknownEffects = 0.5;

		if (knowledge == null || !knowledge.containsKey(collision)) // nothing
																	// is known
																	// at all
			return inhibitAlsoUnknownEffects; // the collision may be undesired,
												// so we might want to inhibit
												// it.
		TreeMap<Effect, Double> effects = knowledge.get(collision);
		double inhibitionFactor = 0.0;
		for (Entry<Effect, Double> e : effects.entrySet()) {
			if (e.getKey().getType() != ActionEffect.NOTHING_HAPPENS)
				inhibitionFactor += e.getValue().doubleValue(); // always
																// inhibit
																// non-zero-effects
																// (the more is
																// known
																// about them
																// the more
																// inhibit,
																// therefore sum
																// up all
																// probabilities
																// in knowledge)
		}
		return inhibitionFactor; // never inhibit zero-effects (then 0.0 is
									// returned)
	}
	/**
	 * Returns the distance between two points as double
	 * 
	 * @param referencePoint
	 * @param otherPoint
	 * @return
	 */
	public static double getDistance(GlobalCoarse referencePoint, GlobalCoarse otherPoint) {
		
		
		double squaredDistance = (otherPoint.x - referencePoint.x)
				* (otherPoint.x - referencePoint.x)
				+ (otherPoint.y - referencePoint.y)
				* (otherPoint.y - referencePoint.y);
		return Math.sqrt(squaredDistance);
	}

	/**
	 * Searches Mario's surroundings for a relevant object and returns the
	 * coordinates of the nearest one. Returns null if there is none. This
	 * method only checks either static OR moving objects.
	 * 
	 * @param map
	 *            map of objects. [row][column]. If this is in global (mario
	 *            relative) coordinates, position should be global
	 *            (mario-relative), too.
	 * @param isStatic
	 *            whether stateBasedOnLastObservation covers static or moving
	 *            objects.
	 * @param referencePoint
	 *            the position (in global discrete coordinates), from which the
	 *            nearest object is calculated.
	 * @return Point the nearest relevant object from position
	 */
	public static NearestObject getNearestObject(PlayerWorldObject[][] map,
			GlobalCoarse referencePoint, boolean isStatic) {
		double minSquaredDistance = Double.POSITIVE_INFINITY;
		int retWidth = 0;
		int retHeight = 0;
		PlayerWorldObject nearestObject = null;
		for (int rows = 0; rows < map.length; rows++) {
			for (int cols = 0; cols < map[rows].length; cols++) {
				PlayerWorldObject object = map[rows][cols];
				if (object != null
						&& (object).isRelevantToLearning()) {
					double squaredDistance = (cols - referencePoint.x)
							* (cols - referencePoint.x)
							+ (rows - referencePoint.y)
							* (rows - referencePoint.y);
					if (squaredDistance < minSquaredDistance) {
						nearestObject = object;
						minSquaredDistance = squaredDistance;
						retHeight = rows;
						retWidth = cols;
					}
				}
			}
		}
		if (nearestObject == null) {
			return null;
		}
		return new NearestObject(isStatic,
				new GlobalCoarse(retWidth, retHeight), nearestObject,
				minSquaredDistance);
	}

	/**
	 * Searches Mario's surroundings for a relevant object and returns the
	 * coordinates of the nearest one. Returns null if there is none. This
	 * method checks static and moving objects at the same time.
	 * 
	 * @param globalStaticMap
	 *            the static map which is searched
	 * @param globalMovingMap
	 *            the map of sprites which is searched
	 * @param referencePoint
	 *            the position (in global discrete coordinates), from which the
	 *            nearest object is calculated.
	 * @return (isStatic, Position)
	 */
	public static NearestObject getNearestObject(
			PlayerWorldObject[][] globalStaticMap,
			PlayerWorldObject[][] globalMovingMap, GlobalCoarse referencePoint) {
		NearestObject nearestStaticObject = getNearestObject(globalStaticMap,
				referencePoint, true);
		NearestObject nearestMovingObject = getNearestObject(globalMovingMap,
				referencePoint, false);

		if (nearestStaticObject == null && nearestMovingObject == null) {
			// new Throwable("No nearest object found.").printStackTrace();
			return null;
		}
		if (nearestMovingObject == null
				|| (nearestStaticObject != null && nearestStaticObject
						.compareTo(nearestMovingObject) < 0)) {
			return nearestStaticObject;
		}
		return nearestMovingObject;
	}

	/**
	 * Represents an observation as a String
	 * 
	 * @param observation
	 * @return String
	 */
	public static String observationOut(byte[][] observation) {
		String separator = "\t";
		String ret = " " + separator;
		// write column indices
		for (int j = 0; j < observation[0].length; j++) {
			ret += (j + separator);
		}
		ret += "\n";
		for (int i = 0; i < observation.length; i++) {
			// write row indices
			ret += (i + separator);
			// write entries
			for (int j = 0; j < observation[i].length; j++) {
				ret += (observation[i][j] + separator);
			}
			ret += "\n";
		}
		return ret;
	}

	public static String observationOut(String caption,
			PlayerWorldObject[][] observation) {
		final String separator1 = " ", separator2 = "\n";
		String s = caption;
		final int rows = observation.length;
		final int cols = observation[0].length;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				s += observation[i][j] + (j < cols - 1 ? separator1 : "");
			}
			s += (i < rows - 1 ? separator2 : "");
		}
		return s;
	}

	/**
	 * Checks, if a collision with a static object would occur, if the sprite
	 * executed the desired movement. This method does not check for collisions
	 * with moving objects.
	 * 
	 * @param map
	 *            this contains the static objects, with
	 * @param avoidCliffs
	 *            whether the moving sprite turns around at the end of a
	 *            platform
	 * @param onGround
	 *            whether the moving sprite (e.g. mario) stands on the ground
	 * @param x
	 *            position of the moving sprite before movement
	 * @param y
	 *            position of the moving sprite before movement
	 * @param xa
	 *            desired movement. This is executed fully if no collision
	 *            occurs. Else, it is executed partly (as far as possible).
	 * @param ya
	 *            desired movement. This is executed fully if no collision
	 *            occurs. Else, it is executed partly (as far as possible).
	 * @param width
	 *            width of the moving sprite.
	 * @param height
	 *            height of the moving sprite.
	 * @return Integer: number of collisions with blocking static objects (this
	 *         includes the ground, but not energys). CollisionAtPosition: the
	 *         "main" or "first" collision with a static object, but only if it
	 *         is relevant for learning. If the main collision is not relevant,
	 *         null is returned.
	 */
	public static Entry<Integer, CollisionAtPosition> detectCollisionWithStaticObjects(
			PlayerWorldObject[][] map, final int playerHealth, final float x,
			final float y, final float xa, final float ya, final float width,
			final float height, final PlayerWorldObject actor,
			final boolean wasInvulnerable, final float addXForIsBlockingToRight) {
		/**
		 * determine whether the point (x+xa, y+ya) collides with anything. Six
		 * testing points around this are relevant: NE, NW, SE, SW, E, W, where
		 * N=North, ... Asymmetric, because mario is taller than he is wide.
		 * depending on the movement (left, right, up, down), only two or three
		 * of those four testing points need to be tested. if moving up: test NW
		 * and NE. down: SW and SE. left: NW and SW. right: NE and SE. For some
		 * sprites, turning around at the end of a cliff is handled, too.
		 */
		float bottomAdd = ya > 0 ? 1 : 0;
		GlobalContinuous[] testingPoints = new GlobalContinuous[5];
		float xTest = x + xa
				+ (xa > 0 ? width + addXForIsBlockingToRight : -width);
		float yTest = y + ya + (ya > 0 ? bottomAdd : -height);
		/**
		 * if moving horizontally, check thrice left (W, NW, SW) or right (E,
		 * NE, SE)
		 */
		if (ya == 0) {
			/** TODO: not sure about the right order here. */
			testingPoints[0] = new GlobalContinuous(xTest, y + ya - height / 2);
			testingPoints[1] = new GlobalContinuous(xTest, y + ya - height);
			testingPoints[2] = new GlobalContinuous(xTest, y + ya + bottomAdd);
			testingPoints[3] = new GlobalContinuous(xTest, y  - (height / 4)*3);
			testingPoints[4] = new GlobalContinuous(xTest, y  - height / 4);
		}
		/**
		 * if moving vertically, check thrice top (N, NW, NE) or bottom (S, SW,
		 * SE)
		 */
		if (xa == 0) {
			/**
			 * order is important here. Directly upwards (North) has to be
			 * tested first.
			 */
			testingPoints[0] = new GlobalContinuous(x + xa, yTest);
			testingPoints[1] = new GlobalContinuous(x + xa - width, yTest);
			testingPoints[2] = new GlobalContinuous(x + xa + width
					+ addXForIsBlockingToRight, yTest);
			testingPoints[3] = new GlobalContinuous(-1, -1);
			testingPoints[4] = new GlobalContinuous(-1, -1);
		}

		int numberOfCollisionsWithBlockingObjects = 0;
		// ConditionDirectionPair collisionWithLearningRelevantObject = null;
		CollisionAtPosition firstCollision = null;
		boolean firstCollisionFound = false;
		GlobalCoarse playerPosition = new GlobalContinuous(x, y)
				.toGlobalCoarse();
		/** now iterate over all 2 or 3 points */
		for (GlobalContinuous testingPoint : testingPoints) {
			if (LevelSceneAdapter.isInMap(map[0].length, map.length,
					testingPoint).getKey()) {

				boolean[] occupation = LevelSceneAdapter.occupation(map,
						testingPoint, playerPosition, ya);
				if (occupation[0]) { // there is an object at the testingPoint
					if (occupation[2]) { // the object is blocking: just count
											// the number of blocking testing
											// points
						numberOfCollisionsWithBlockingObjects++;
					}
					if (!firstCollisionFound
							&& (occupation[1] || occupation[2])) { // this
																	// is
																	// the
																	// first
																	// testing
																	// point
																	// with
																	// a
																	// non-empty
																	// cell.
																	// Only
																	// this
																	// collision
																	// is
																	// executed.
						firstCollisionFound = true;
						if (occupation[1]) 
						{ 
												// the first collision is also
												// learning relevant: remember
												// it.
							CollisionDirection direction = GeneralUtil
									.getDirection(xa, ya);
							if (direction == null) {
								// fallback method which might not be correct.
								direction = getDirection(x, y, testingPoint.x,
										testingPoint.y);
							}
							PlayerWorldObject collisionObject = LevelSceneAdapter
									.getObject(map, testingPoint);							
							boolean undefinedHealth = !(actor.isPlayer()); // if the actor is a player his health is defined
																						
							PlayerHealthCondition playerHealthCondition = PlayerHealthCondition
									.fromInt(playerHealth, wasInvulnerable,
											undefinedHealth);
							
							firstCollision = new CollisionAtPosition
									(
											new ConditionDirectionPair
											(
													new Condition
													(
															actor, 
															collisionObject,
															playerHealthCondition
													), 
													direction
											),
											testingPoint.toGlobalCoarse(), 
											null,
											null
									);
						}
					}

				}

				// if (LevelSceneAdapter.isBlocking(map, testingPoint, ya)) {
				// numberOfCollisionsWithBlockingObjects++;
				// }
				// if(!firstCollisionFound) { //this is the first (i.e. "main")
				// collision
				// firstCollisionFound = true;
				// if
				// (LevelSceneAdapter.isCellOccupiedWithLearningRelevantStaticObject(map,
				// testingPoint)) { //and the first collision is learning
				// relevant
				// CollisionDirection direction = GeneralUtil.getDirection(xa,
				// ya);
				// if (direction == null) {
				// // fallback method which might not be correct.
				// direction = getDirection(x, y, testingPoint.x,
				// testingPoint.y);
				// }
				// MarioWorldObject collisionObject =
				// LevelSceneAdapter.getObject(map, testingPoint);
				// boolean undefinedHealth = actor!=MarioWorldObject.MARIO; //if
				// this is mario, his health is defined.
				// MarioHealthCondition marioHealthCondition =
				// MarioHealthCondition.fromInt(marioHealth, wasInvulnerable,
				// undefinedHealth);
				// firstCollision = new CollisionAtPosition(new
				// ConditionDirectionPair(new Condition(actor, collisionObject,
				// marioHealthCondition), direction),
				// testingPoint.toGlobalCoarse(), null);
				// }//else (the first collision is not learning relevant):
				// firstCollision remains null
				// }
			}
		}
		return new AbstractMap.SimpleEntry<Integer, CollisionAtPosition>(
				numberOfCollisionsWithBlockingObjects, firstCollision);
	}

	public static ArrayList<CollisionAtPosition> doCollisionWithMovingObjects(
			HashSet<SimulatedSprite> sprites, SimulatedSprite actor) {
		GlobalContinuous playerPosition = actor.getPosition(0);
		GlobalContinuous playerVelocity = actor.getVelocity(0);
		GlobalContinuous oldPlayerPosition = actor.getPosition(1);
		GlobalContinuous oldPlayerVelocity = actor.getVelocity(1);
		ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();
		for (SimulatedSprite sprite : sprites) {
			//@c2-pavel: ex-Learninghelper
			if ((sprite.type).isRelevantToLearning()) {
				GlobalContinuous objectPosition = sprite.getPosition(0);
				GlobalContinuous objectVelocity = sprite.getVelocity(0);
				GlobalContinuous oldObjectPosition = sprite.getPosition(1);
				// GlobalContinuous oldObjectVelocity = sprite.getVelocity(1);
				/** base collision on position after movement */
				boolean collision = checkIfCollisionOccursWithMovingObjects(
						playerPosition.x, playerPosition.y, playerVelocity.x,
						playerVelocity.y,
						actor.type.getWidthForCollisionDetection(),
						actor.getHeight(), objectPosition.x, objectPosition.y,
						objectVelocity.x, objectVelocity.y,
						sprite.type.getWidthForCollisionDetection(),
						sprite.getHeight());
				/** base direction on position before movement */
				if (collision) {
					CollisionDirection direction = getDirectionFromSprite(
							oldPlayerPosition.x, oldPlayerPosition.y,
							oldPlayerVelocity.y, oldObjectPosition.x,
							oldObjectPosition.y);
					boolean undefinedHealth = false; // this method should only
														// ever be called by
														// mario, and his health
														// is defined.
					PlayerHealthCondition playerHealthCondition = PlayerHealthCondition
							.fromInt(actor.getHealth(),
									actor.wasInvulnerable(), undefinedHealth);
					ret.add(new CollisionAtPosition
							(
									new ConditionDirectionPair
									(
											new Condition
											(
													actor.type, 
													sprite.type,
													playerHealthCondition
											), 
											direction
									), 
									sprite.getMapPosition(0),
									actor,
									sprite
							));
				}
			}
		}
		return ret;
	}

	/**
	 * Centered on the object: Returns in which direction Mario is.
	 * 
	 * @param yObject
	 * @param xObject
	 * @return Direction
	 */
	private static CollisionDirection getDirection(float playerX, float playerY,
			float xObject, float yObject) {
		boolean leftORRight = Math.abs((yObject - playerY) / (xObject - playerX)) <= 1;
		if (leftORRight) {
			if (playerX < xObject)
				return CollisionDirection.LEFT;
			return CollisionDirection.RIGHT;
		}
		if (playerY < yObject)
			return CollisionDirection.ABOVE;
		return CollisionDirection.BELOW;
	}

	/**
	 * Centered on the object: Returns in which direction Mario is.
	 * 
	 * @param xa
	 *            mario's movement BEFORE interacting with the object
	 * @param ya
	 *            mario's movement BEFORE interacting with the object
	 * @return Direction
	 */
	private static CollisionDirection getDirection(float xa, float ya) {
		if ((xa != 0 && ya != 0) || (xa == 0 && ya == 0)) {
			// new
			// Throwable("Movement is: ("+xa+", "+ya+"). Please call with either a horizontal or a vertical movement.").printStackTrace();
			return null;
		}
		/**
		 * if mario walks to the right and then interacts with an object, he is
		 * left of the object.
		 */
		if (xa > 0) {
			return CollisionDirection.LEFT;
		}
		if (xa < 0) {
			return CollisionDirection.RIGHT;
		}
		if (ya > 0) {
			return CollisionDirection.ABOVE;
		}
		if (ya < 0) {
			return CollisionDirection.BELOW;
		}
		return null;
	}

	/**
	 * 
	 * @param deltaX
	 *            marioX - objectX
	 * @param deltaY
	 *            marioY - objectY
	 * @param marioXA
	 * @param playerYA
	 * @param marioWidth
	 * @param marioHeight
	 * @param objectWidth
	 * @param objectHeight
	 * @return
	 */
	private static CollisionDirection getDirectionFromSprite(float playerX,
			float playerY, float playerYA, float objectX, float objectY) {
		// above: cf. Mario.stomp()
		if (playerYA > 0 && playerY - objectY < 0)
			return CollisionDirection.ABOVE;
		// left right: cf. Enemy.bumpCheck()
		CollisionDirection direction = getDirection(playerX, playerY, objectX,
				objectY);
		if (direction == CollisionDirection.ABOVE) {
//			new Throwable(
//					"Found above where the engine thinks differently. Check what to do.")
//					.printStackTrace();
			return CollisionDirection.BELOW;
		}
		return direction;
	}

	private static boolean checkIfCollisionOccursWithMovingObjects(
			float playerX, float playerY, float playerXA, float playerYA,
			int playerWidth, int playerHeight, float objectX, float objectY,
			float objectXA, float objectYA, int objectWidth, int objectHeight) {
		// float xMarioD = marioX + marioXA - objectX - objectXA;
		// float yMarioD = marioY + marioYA - objectY - objectYA;
		float xPlayerD = playerX - objectX;
		float yPlayerD = playerY - objectY;

		final int sumWidth = 2 * objectWidth + 2*playerWidth;
		if (xPlayerD > -sumWidth && xPlayerD < sumWidth) {
			if (yPlayerD > -objectHeight && yPlayerD < playerHeight) {
				return true;
				// if (marioYA > 0 && yMarioD <= 0 && (!world.mario.onGround ||
				// !world.mario.wasOnGround)) {
				// stomp
				// }else {
				// noStomp
				// }
			}
		}
		return false;
	}

	/**
	 * returns a new array where array1 and array2 are concatenated
	 * 
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static int[] concatenate(int[] array1, int[] array2) {
		int[] ret = new int[array1.length + array2.length];
		for (int i = 0; i < array1.length; i++) {
			ret[i] = array1[i];
		}
		for (int i = 0; i < array2.length; i++) {
			ret[array1.length + i] = array2[i];
		}
		return ret;
	}

	public static double getInBounds(double value, double min, double max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	/**
	 * Checks, if a collision with a static object would occur, if the sprite
	 * executed the desired movement. This method does not check for collisions
	 * with moving objects.
	 * MODIFIED VERSION FOR THE REACHABILITY
	 */
	public static Entry<Integer, CollisionAtPosition> detectCollisionWithStaticObjects(
			PlayerWorldObject[][] map, final int playerHealth, final float x,
			final float y, final float xa, final float ya, final float width,
			final float height, final PlayerWorldObject actor,
			final boolean wasInvulnerable, final float addXForIsBlockingToRight, boolean modified) {
		/**
		 * determine whether the point (x+xa, y+ya) collides with anything. Six
		 * testing points around this are relevant: NE, NW, SE, SW, E, W, where
		 * N=North, ... Asymmetric, because mario is taller than he is wide.
		 * depending on the movement (left, right, up, down), only two or three
		 * of those four testing points need to be tested. if moving up: test NW
		 * and NE. down: SW and SE. left: NW and SW. right: NE and SE. For some
		 * sprites, turning around at the end of a cliff is handled, too.
		 */
		float bottomAdd = ya > 0 ? 1 : 0;
		GlobalContinuous[] testingPoints = new GlobalContinuous[5];
		float xTest = x + xa
				+ (xa > 0 ? width + addXForIsBlockingToRight : -width);
		float yTest = y + ya + (ya > 0 ? bottomAdd : -height);
		/**
		 * if moving horizontally, check thrice left (W, NW, SW) or right (E,
		 * NE, SE)
		 */
		if (ya == 0) {
			/** TODO: not sure about the right order here. */
			testingPoints[0] = new GlobalContinuous(xTest, y  - height / 2);
			testingPoints[1] = new GlobalContinuous(xTest, y  - height / 4);
			testingPoints[2] = new GlobalContinuous(xTest, y  - (height / 4)*3);
			testingPoints[3] = new GlobalContinuous(xTest, y  - height);
			testingPoints[4] = new GlobalContinuous(xTest, y  + bottomAdd);
		}
		/**
		 * if moving vertically, check thrice top (N, NW, NE) or bottom (S, SW,
		 * SE)
		 */
		if (xa == 0) {
			/**
			 * order is important here. Directly upwards (North) has to be
			 * tested first.
			 */
			testingPoints[0] = new GlobalContinuous(x + xa, yTest);
			testingPoints[1] = new GlobalContinuous(x + xa - width, yTest);
			testingPoints[2] = new GlobalContinuous(x + xa + width
					+ addXForIsBlockingToRight, yTest);
			testingPoints[3] = testingPoints[2];
			testingPoints[4] = testingPoints[2];
		}

		int numberOfCollisionsWithBlockingObjects = 0;
		// ConditionDirectionPair collisionWithLearningRelevantObject = null;
		CollisionAtPosition firstCollision = null;
		boolean firstCollisionFound = false;
		GlobalCoarse playerPosition = new GlobalContinuous(x, y)
				.toGlobalCoarse();
		/** now iterate over all 2 or 3 points */
		for (GlobalContinuous testingPoint : testingPoints) {
			if (LevelSceneAdapter.isInMap(map[0].length, map.length,
					testingPoint).getKey()) {

				boolean[] occupation = LevelSceneAdapter.occupation(map,
						testingPoint, playerPosition, ya);
				if (occupation[0]) { // there is an object at the testingPoint
					if (occupation[2]) { // the object is blocking: just count
											// the number of blocking testing
											// points
						numberOfCollisionsWithBlockingObjects++;
					}
					if (!firstCollisionFound
							&& (occupation[1] || occupation[2])) { // this
																	// is
																	// the
																	// first
																	// testing
																	// point
																	// with
																	// a
																	// non-empty
																	// cell.
																	// Only
																	// this
																	// collision
																	// is
																	// executed.
						firstCollisionFound = true;
						if (true) { // the first collision is also
												// learning relevant: remember
												// it.
							CollisionDirection direction = GeneralUtil
									.getDirection(xa, ya);
							if (direction == null) {
								// fallback method which might not be correct.
								direction = getDirection(x, y, testingPoint.x,
										testingPoint.y);
							}
							PlayerWorldObject collisionObject = LevelSceneAdapter
									.getObject(map, testingPoint);
							boolean undefinedHealth = (PlayerWorldObject.PLAYER != Generalizer.generalizePlayer(actor)); // if
																						// this
																						// is
																						// mario,
																						// his
																						// health
																						// is
																						// defined.
							PlayerHealthCondition playerHealthCondition = PlayerHealthCondition
									.fromInt(playerHealth, wasInvulnerable,
											undefinedHealth);

							firstCollision = new CollisionAtPosition
									(
											new ConditionDirectionPair
											(
													new Condition
													(
															actor, 
															collisionObject,
															playerHealthCondition
													), 
													direction
											),
											testingPoint.toGlobalCoarse(), 
											null,
											null
									);
						}
					}

				}

				// if (LevelSceneAdapter.isBlocking(map, testingPoint, ya)) {
				// numberOfCollisionsWithBlockingObjects++;
				// }
				// if(!firstCollisionFound) { //this is the first (i.e. "main")
				// collision
				// firstCollisionFound = true;
				// if
				// (LevelSceneAdapter.isCellOccupiedWithLearningRelevantStaticObject(map,
				// testingPoint)) { //and the first collision is learning
				// relevant
				// CollisionDirection direction = GeneralUtil.getDirection(xa,
				// ya);
				// if (direction == null) {
				// // fallback method which might not be correct.
				// direction = getDirection(x, y, testingPoint.x,
				// testingPoint.y);
				// }
				// MarioWorldObject collisionObject =
				// LevelSceneAdapter.getObject(map, testingPoint);
				// boolean undefinedHealth = actor!=MarioWorldObject.MARIO; //if
				// this is mario, his health is defined.
				// MarioHealthCondition marioHealthCondition =
				// MarioHealthCondition.fromInt(marioHealth, wasInvulnerable,
				// undefinedHealth);
				// firstCollision = new CollisionAtPosition(new
				// ConditionDirectionPair(new Condition(actor, collisionObject,
				// marioHealthCondition), direction),
				// testingPoint.toGlobalCoarse(), null);
				// }//else (the first collision is not learning relevant):
				// firstCollision remains null
				// }
			}
		}
		return new AbstractMap.SimpleEntry<Integer, CollisionAtPosition>(
				numberOfCollisionsWithBlockingObjects, firstCollision);
	}
}
