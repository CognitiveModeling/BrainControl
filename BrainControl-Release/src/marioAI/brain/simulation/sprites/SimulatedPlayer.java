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
package marioAI.brain.simulation.sprites;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.sound.midi.Synthesizer;

import marioAI.brain.simulation.ReachabilityMap;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.util.GeneralUtil;
import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Fireball;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sparkle;
import marioWorld.engine.sprites.Sprite;

/**
 * This is a clone of mario which only encompasses attributes which are relevant for moving. Those parts of object-interactions which do not affect moving as
 * well as GUI-related stuff is dropped. the tick() method is part of the brain's forward model and includes movement-related changes done by object
 * interactions.
 * 
 * @author Stephan
 * 
 */
public class SimulatedPlayer extends SimulatedSprite {
	private int oldInvulnerableTime = 0;
	/**
	 * currently write only. Will be required as soon as invulnerability is included in CAE-learning
	 */
	private int invulnerableTime = 0;
	/**
	 * whether player was standing on ground and not sliding in the last time step.
	 */
	private boolean wasOnGround;
	/**
	 * the velocity while in the air. Changes are only possible in a small time interval around beginning to jump or collision-interactions (including landing).
	 */
	public float xJumpSpeed, yJumpSpeed, individualYJumpSpeedMultiplier;
	// /** player position. Old refers to the last time step. */
	// private float xOld, xOldOld, yOld, yOldOld, x, y;
	// /** velocity component (in x or y direction separately) */
	// private float xa, ya;
	/**
	 * required because a ducking player can not change facing which in turn affects sliding which affects movement.
	 */
	private boolean ducking;
	/**
	 * 2:bulb. 1: large. 0: small. (-1:dead). Size(i.e. large vs small) is also relevant for collisions and for movement because a small mario can not duck
	 * which in turn affects sliding.
	 */
	private int health;
	/** the number of energys which mario has gathered. */
	private int numEnergys;

	int birthTime;

	// /** for debugging purposes only */
	// private final Mario mario;
	/** for debugging purposes only */
	@SuppressWarnings("unused")
	public boolean[] keys; // this is read via reflection
	
	public int width = 4;
	public int height = 24;
	
	private boolean canShoot = false; 

	@Override
	public SimulatedPlayer clone() 
	{
		try 
		{
			return (SimulatedPlayer) super.clone();
		} 
		catch (Exception e) 
		{
			// Should never happen.
			throw new RuntimeException(e);
		}
	}	
	
	/**
	 * creates a simplified mario by cloning the real mario. The simplified mario can simulate all the movement-related functionality, including object
	 * collisions. It can NOT simulate any changes on its environment or its own not-movement-related-state.
	 * 
	 * @param tmp
	 */
	public SimulatedPlayer(Player tmp) 
	{
		super(tmp);
		this.invulnerableTime = tmp.getInvulnerableTime();
		this.jumpTime = tmp.getJumpTime();
		this.onGround = tmp.isOnGround();
		this.wasOnGround = tmp.wasOnGround();
		this.sliding = tmp.isSliding();
		this.xJumpSpeed = tmp.getxJumpSpeed();
		this.yJumpSpeed = tmp.getyJumpSpeed();
		this.individualYJumpSpeedMultiplier = tmp.getIndividualYJumpSpeedMultiplier();
		this.mayJump = tmp.isMayJump();
		this.ducking = tmp.isDucking();
		this.facing = tmp.facing;
		this.numEnergys = tmp.getEnergys();
		this.birthTime = tmp.birthTime;
		if (tmp.bulb) 
		{
			this.health = 2;
		} 
		else if (tmp.large) 
		{
			this.health = 1;
		} 
		else 
		{
			this.health = 0;
		}
		this.health = tmp.getHealthLevel();
		this.keys = tmp.keys.clone();
	}

	public SimulatedPlayer(SimulatedPlayer tmp) 
	{
		super(tmp);
		this.invulnerableTime = tmp.invulnerableTime;
		this.jumpTime = tmp.getJumpTime();
		this.onGround = tmp.isOnGround();
		this.wasOnGround = tmp.wasOnGround;
		this.sliding = tmp.isSliding();
		this.xJumpSpeed = tmp.xJumpSpeed;
		this.yJumpSpeed = tmp.yJumpSpeed;
		this.individualYJumpSpeedMultiplier = tmp.getIndividualYJumpSpeedMultiplier();
		this.mayJump = tmp.isMayJump();
		this.ducking = tmp.ducking;
		this.facing = tmp.facing;
		this.numEnergys = tmp.numEnergys;
		this.birthTime = tmp.birthTime;
		this.health = tmp.health;
		this.keys = tmp.keys.clone();
	}

	public int getPlayerIndex()
	{
		return ((Player)originalSprite).getPlayerIndex();
	}
	
	/**
	 * increases the number of gathered energys by 1
	 */
	public void gatherEnergy() 
	{
		numEnergys += LevelScene.ENERGY_INCREASE;
	}

	/**
	 * decreases the number of gathered energys by 1
	 */
	public void loseEnergy() 
	{
		numEnergys--;
	}	

	/**
	 * decreases health by 1
	 */
	public void takeDamage() 
	{
		health--;

		if(health<0)
		{
			health=1;
			birthTime=0;
		}
	}

	/**
	 * increases health by 1
	 */
	public void heal() 
	{
		if (health <= 2) health++;
	}
	public void heal(int amount) 
	{
		health = amount;
	}

	@Override
	public int getHealth() 
	{
		return health;
	}

	public int getBirthTime() 
	{
		return birthTime;
	}	

	public int getNumEnergys() 
	{
		return numEnergys;
	}
	
	public int getJumpTime() {
		return jumpTime;
	}
	
	public boolean getOnGround() {
		return onGround;
	}
	
	public boolean getSliding() {
		return sliding;
	}
	
	public boolean getMayJump() {
		return mayJump;
	}
	
	public float getXJumpSpeed() {
		return xJumpSpeed;
	}
	
	public float getYJumpSpeed() {
		return yJumpSpeed;
	}
	
	public float getIndividualYJumpSpeedMultiplier(){
		return individualYJumpSpeedMultiplier;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getXa() {
		return xa;
	}
	public void setXa(float xa) {
		this.xa = xa;
	}
	
	public float getYa() {
		return ya;
	}
	
	/**
	 * whether mario is currently moving (else standing still).
	 * 
	 * @return
	 */
	public boolean isMoving() 
	{
		//that is when mario falls down he is not moving? thats not what we need for planning ...
		//return (xa != 0 || (ya != 0 && !onGround));
		
		//CHECK: ya is never 0!!
		return xa != 0.0 || Math.abs(ya) > 0.2 || x != xOld || y != yOld || xOld != xOldOld || yOld != yOldOld;	//lets include some time here...
	}

	
	private void initfacing(boolean[] keys) {
		/** fast x-velocity changes mario's facing direction */
		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}
	}
	
	/*
	 * sets ducking
	 */
	private void initducking(boolean[] keys) {
		/**
		 * if mario is on the ground, large, and the ducking key is pressed, he will actually duck
		 */
		this.wasOnGround = onGround;
		if (onGround) {
			if (keys[Player.KEY_DOWN] && isLarge()) {
				ducking = true;
			} else {
				ducking = false;
			}
		}
	}
	
	
	/*
	 * moves the carried player with the carrier. only when carry was invoked in previous time steps.
	 * */
	private void moveCarry(PlayerWorldObject[][] map) 
	{
		// if the player is carrying some other players, then the positions etc of these players are adapted
		if (this.getCarries() != null && !keys[Player.KEY_CARRYDROP]) 
		{
			SimulatedPlayer carriedPlayer = this;
			// counts how many player this is carrying to set the y-coordinate
			int numberOfCarriedPlayers = 0;
			
			// every player that stands on this player should have the adapted properties
			do
			{
				carriedPlayer = (SimulatedPlayer) carriedPlayer.getCarries();
				numberOfCarriedPlayers++;
				carriedPlayer.onGround = true;
				carriedPlayer.mayJump = true;
		
				
				// but if the player jump, then the properties are allowed to change
				if(!carriedPlayer.keys[Player.KEY_JUMP])
				{				
					// test if the carried player collides with an static object
					SimulatedPlayer cloneOfCarriedPlayer = new SimulatedPlayer(carriedPlayer);
					
					final float addXForIsBlockingToRightClone = Player.USE_SYMMETRIC_COLLISIONS && cloneOfCarriedPlayer.type.isPlayer() ? (float) (-1E-3) : 0;
					final int collisionWidthClone = cloneOfCarriedPlayer.type.getWidthForCollisionDetection();
					
					CollisionAtPosition horCollision = cloneOfCarriedPlayer.carryCollision(cloneOfCarriedPlayer.xa, 0, cloneOfCarriedPlayer.x, cloneOfCarriedPlayer.y, addXForIsBlockingToRightClone, collisionWidthClone, cloneOfCarriedPlayer.type, map).getValue();
					CollisionAtPosition vertCollision = cloneOfCarriedPlayer.carryCollision(0, cloneOfCarriedPlayer.ya, cloneOfCarriedPlayer.x, cloneOfCarriedPlayer.y, addXForIsBlockingToRightClone, collisionWidthClone, cloneOfCarriedPlayer.type, map).getValue();

					// if there is an collision in horizontal or vertical direction
					/*if (horCollision != null || vertCollision != null)
					{
						// then the player will be set down
						carriedPlayer.y = y;
						carriedPlayer.xa = 0;
						carriedPlayer.ya = 0;
					}
					
					// else the carried player should have adapted properties
					else*/
//					{
						carriedPlayer.x = x;
						carriedPlayer.y = y - (numberOfCarriedPlayers * getHeight());
						
						carriedPlayer.xa = 0;
						carriedPlayer.ya = 0;
						
						carriedPlayer.facing = getFacing();
//					}
				}		
				
				// and all players that stands on the jumping player should not have adapted properties to this player but
				// to the jumping player (will be adapted in the move of the jumping player)
				else break;
			}
			while(carriedPlayer.getCarries() != null);
		}
	}	
	
	/*private void moveCarry() 
	{
		// if the player is carrying some other players, then the positions etc of these players are adapted
		if (this.getCarries() != null && !keys[Player.KEY_CARRYDROP]) 
		{
			SimulatedPlayer carriedPlayer = this;
			// counts how many player this is carrying to set the y-coordinate
			int numberOfCarriedPlayers = 0;
			
			// every player that stands on this player should have the adapted properties
			do
			{
				carriedPlayer = (SimulatedPlayer) carriedPlayer.getCarries();
				numberOfCarriedPlayers++;
				carriedPlayer.onGround = true;
				carriedPlayer.mayJump = true;
				
				// but if the player jump, then the properties are allowed to change
				if(!carriedPlayer.keys[Player.KEY_JUMP])
				{
					carriedPlayer.x = x;
					carriedPlayer.y = y - (numberOfCarriedPlayers * getHeight());
					
					carriedPlayer.xa = 0;
					carriedPlayer.ya = 0;
					
					carriedPlayer.facing = getFacing();
				}		
				
				// and all players that stands on the jumping player should not have adapted properties to this player but
				// to the jumping player (will be adapted in the move of the jumping player)
				else break;
			}
			while(carriedPlayer.getCarries() != null);
		}
	}*/
	
	// like move-function in SimulatedSprite but just collision checking, no really moving
	private Entry<Integer, CollisionAtPosition> carryCollision(float xa, float ya, float x, float y, float addXForIsBlockingToRight, int collisionWidth, PlayerWorldObject type,  PlayerWorldObject[][] map) {
		if (xa != 0 && ya != 0) {
			throw new RuntimeException("Please call with one velocity component at a time.");
		}
		final float HALF_CELL_SIZE_X = CoordinatesUtil.UNITS_PER_TILE / 2;
		final float HALF_CELL_SIZE_Y = CoordinatesUtil.UNITS_PER_TILE / 2;
		

		/**
		 * move component-wise (first x-movement, than y-movement), maximally as far as half a cell (16/2=8). Check for collision. If no collision, repeat.
		 * Else, break.
		 */
		Entry<Integer, CollisionAtPosition> collision = null;
		while (xa > HALF_CELL_SIZE_X) {
			if ((collision = carryCollision(HALF_CELL_SIZE_X, 0f, x, y, addXForIsBlockingToRight, collisionWidth, type, map)).getKey() > 0) {
				return collision;
			}
			xa -= HALF_CELL_SIZE_X;
		}
		while (xa < -HALF_CELL_SIZE_X) {
			if ((collision = carryCollision(-HALF_CELL_SIZE_X, 0f, x, y, addXForIsBlockingToRight, collisionWidth, type, map)).getKey() > 0) {
				return collision;
			}
			xa += HALF_CELL_SIZE_X;
		}
		while (ya > HALF_CELL_SIZE_Y) {
			if ((collision = carryCollision(0f, HALF_CELL_SIZE_Y, x, y, addXForIsBlockingToRight, collisionWidth, type, map)).getKey() > 0) {
				return collision;
			}
			ya -= HALF_CELL_SIZE_Y;
		}
		while (ya < -HALF_CELL_SIZE_Y) {
			if ((collision = carryCollision(0f, -HALF_CELL_SIZE_Y, x, y, addXForIsBlockingToRight, collisionWidth, type, map)).getKey() > 0) {
				return collision;
			}
			ya += HALF_CELL_SIZE_Y;
		}
		
		/** actual collision check is done here */
		if (xa != 0 || ya != 0) {
			collision = GeneralUtil.detectCollisionWithStaticObjects(map, getHealth(), x, y, xa, ya, collisionWidth, getHeight(), type, wasInvulnerable(),
					addXForIsBlockingToRight);
		} else {
			collision = new AbstractMap.SimpleEntry<Integer, CollisionAtPosition>(0, null);
		}
		return collision;
	}

	
	/**
	 * This method moves the simulated mario given the keys. All this method does is changing mario's MOVEMENT_RELATED attributes, like position, velocity, etc.
	 * It does not influence the world around mario or mario's health. The movement-related functionality also handles collision with objects (as stated above,
	 * only the movement related changes). Furthermore, sliding, jumping to different heights and external forces like inertia & gravitation are covered.
	 */
	@Override
	public List<CollisionAtPosition> move(PlayerWorldObject[][] map, boolean[] keys, SimulatedLevelScene scene) 
	{
		ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();
		
		birthTime++;
		
		oldInvulnerableTime = invulnerableTime;
		if (invulnerableTime > 0) {
			invulnerableTime--;		
		}


		float sideWaysSpeed = calculateSpeed();

		//preAccelerationUpdateOfFlags(keys);

		
		initducking(keys);
		initfacing(keys);
		
		// VELOCITY RELATED CHANGES
		/**
		 * jumps are done here, i.e. mario's velocity (xa, ya) are changed. Apparently, jumps straight upwards can differ in height (the higher the longer the
		 * jump key is pressed), while diagonal jumps always use the maximum height. For some reason negative jumpTimes encode sliding-jumps, while positive
		 * jumpTimes encode jumps straight upwards. Straight upwards jumps are a little higher and take longer than a sliding jumps.
		 */
		// if the jump key is pressed or if this is a diagonal jump (negative
		// jumpTime)
		// if the jump is an unmount action, the collision is returned so that the player
		jumping(keys);
		leftorright(keys, sideWaysSpeed);
		
		
		if ((!keys[Player.KEY_LEFT] && !keys[Player.KEY_RIGHT]) || ducking || ya < 0 || onGround) {
			sliding = false;
		}

		//TODO: enable to simulate fireballs
//		if (keys[Player.KEY_SPEED] && canShoot && this.bulb && isReal && scene.fireballsOnScreen < 2) {
//			scene.addSprite(new Fireball(scene, x + facing * 6, y - 20, facing));
//		}
		
		if (keys[Player.KEY_CARRYDROP] && xa > -2 && xa < 2 && this.getCarries() == null) 
		{
			carryOtherPlayer(scene);
		}
		else if (keys[Player.KEY_CARRYDROP] && this.getCarries() != null) 
		{
			dropOtherPlayer(scene);
		}	
		
		
		//TODO: enable to simulate fireballs
		canShoot = !keys[Player.KEY_SPEED];
		
		if ((!keys[Player.KEY_LEFT] && !keys[Player.KEY_RIGHT]) || ducking || ya < 0 || onGround) {
			sliding = false;
		}
		
		mayJump = (onGround || sliding) && !keys[Player.KEY_JUMP];

		
		/**
		 * inertia as defined below results in asymptotic behavior, so use a threshold to stop here.
		 */
		if (Math.abs(xa) < 0.2f) { //if (Math.abs(xa) < 0.5f) {           SAP Important
			xa = 0;
		}
		
		if (sliding) {
			ya *= 0.5f; // sliding down on wall
		}
		
		onGround = false;
		//onGround is initialized here, it will be updated in move(int, int)
		
		/**
		 * This is the main movement call: move mario and do collision checks. This requires two calls: one for horizontal, one for vertical movement.
		 * Optionally, the object with which mario collided can be returned.
		 */
		CollisionAtPosition xLearningRelevantCollision = move(xa, 0, map).getValue();
		CollisionAtPosition yLearningRelevantCollision = move(0, ya, map).getValue();
		// if(xLearningRelevantCollision!=null && yLearningRelevantCollision !=
		// null) {
		// new
		// Throwable("Found two objects with which mario will collide: "+xLearningRelevantCollision.toString()+" and "+yLearningRelevantCollision.toString()).printStackTrace();
		// }

		// falling into a gap
		// if (y > world.level.height * 16 + 16) {
		// }

		/** left border reached */
		if (x < 0) {
			x = 0;
			xa = 0;
		}

		// /**level exit reached*/
		// if (x > world.level.xExit * 16) {
		// x = world.level.xExit * 16;
		// xa = 0;
		// }

		// /**right border reached*/
		// if (x > world.level.width * 16) {
		// x = world.level.width * 16;
		// xa = 0;
		// }

		physics();

		moveCarry(map);

		// if (carried != null) {
		// carried.x = x + facing * 8;
		// carried.y = y - 2;
		// if (!keys[Mario.KEY_SPEED]) {
		// carried.release(this);
		// carried = null;
		// }
		// }
		// mario.compareWith(new DebugWrapper(this).createAttributeMap());
		
		if (xLearningRelevantCollision != null) {
			//System.out.println("add");
			ret.add(xLearningRelevantCollision);
		}
		if (yLearningRelevantCollision != null) {
			ret.add(yLearningRelevantCollision);
		}
		//System.out.println("ret" + ret.isEmpty());
		return ret;
	}
	
	private float calculateSpeed() {
		int normalizedEnergy = getNumEnergys() > Player.maxEnergySpeed? Player.maxEnergySpeed : getNumEnergys();
		float sideWaysSpeed = keys[Player.KEY_SPEED] ? 1.2f : 0.6f;
		sideWaysSpeed = (sideWaysSpeed / 2.0f) + ((sideWaysSpeed / 2.0f) * (normalizedEnergy /100.0f));
		
		
		return sideWaysSpeed;
	}
	// private CollisionAtPosition
	// handleMultipleCollisionsInOneTimeStep(CollisionAtPosition
	// xLearningRelevantCollisionAtPosition, CollisionAtPosition
	// yLearningRelevantCollisionAtPosition) {
	// ConditionDirectionPair xCollision =
	// xLearningRelevantCollisionAtPosition.collision;
	// ConditionDirectionPair yCollision =
	// yLearningRelevantCollisionAtPosition.collision;
	// GlobalContinuous pos =
	// GlobalContinuous.mean(xLearningRelevantCollisionAtPosition.position,
	// yLearningRelevantCollisionAtPosition.position);
	// if(xCollision!=null && yCollision!=null) {
	// //here mario has interacted once from left or right with xObject and once
	// from above or below with yObject.
	// //now test if xObject and yObject are the same and mario had the same
	// condition
	// if(xCollision.equalConditionAs(yCollision)) {
	// //in that case we don't know which direction induced the effect. => 1x
	// UNDEFINED
	// return new CollisionAtPosition(new
	// ConditionDirectionPair(xCollision.getCondition(),
	// CollisionDirection.UNDEFINED), pos);
	// }
	// Condition helpX = xCollision.getCondition();
	// Condition helpY = xCollision.getCondition();
	// if(helpX.equalAgentStateAs(helpY)) {
	// //mario condition is equal but both objects are different and we still
	// don't know which direction induced the effect. => 2x UNDEFINED
	// Condition newCondition = new Condition(MarioWorldObject.UNDEF,
	// helpX.getMarioHealth(), helpX.getWasInvulnerable(), helpX.isMario());
	// return new CollisionAtPosition(new ConditionDirectionPair(newCondition,
	// CollisionDirection.UNDEFINED), pos);
	// }
	// throw new
	// IllegalArgumentException("In SimulatedMario.handleMultipleCollisionsInOneTimeStep: Mario Conditions should not be different here.");
	// }else {
	// if(yCollision!=null) {
	// return yLearningRelevantCollisionAtPosition;
	// }
	// return xLearningRelevantCollisionAtPosition;
	// }
	// }

	// /**
	// * move with the specified velocity, EITHER in x-direction, OR in
	// * y-direction. If a diagonal move is desired, call this method first with
	// a
	// * move in x-direction, then in y-direction. This method also implements
	// * collision-dependent movement changes. The method modifies the
	// attributes
	// * x, y, xa, ya and sliding.
	// *
	// * @param xa
	// * x-component of the velocity
	// * @param ya
	// * y-component of the velocity
	// * @return if the movement was unhindered (i.e. if no collisions happened)
	// */
	// public boolean move(float xa, float ya, LevelSceneAdapter
	// levelSceneAdapter) {
	// if (xa != 0 && ya != 0) {
	// throw new RuntimeException(
	// "Please call with one velocity component at a time.");
	// }
	// final float HALF_CELL_SIZE_X = CoordinatesUtil.UNITS_PER_TILE_X / 2;
	// final float HALF_CELL_SIZE_Y = CoordinatesUtil.UNITS_PER_TILE_Y / 2;
	// /**
	// * move component-wise (first x-movement, than y-movement), maximally as
	// * far as half a cell (16/2=8). Check for collision. If no collision,
	// * repeat. Else, break.
	// */
	// while (xa > HALF_CELL_SIZE_X) {
	// if (!move(HALF_CELL_SIZE_X, 0f, levelSceneAdapter))
	// return false;
	// xa -= HALF_CELL_SIZE_X;
	// }
	// while (xa < -HALF_CELL_SIZE_X) {
	// if (!move(-HALF_CELL_SIZE_X, 0f, levelSceneAdapter))
	// return false;
	// xa += HALF_CELL_SIZE_X;
	// }
	// while (ya > HALF_CELL_SIZE_Y) {
	// if (!move(0f, HALF_CELL_SIZE_Y, levelSceneAdapter))
	// return false;
	// ya -= HALF_CELL_SIZE_Y;
	// }
	// while (ya < -HALF_CELL_SIZE_Y) {
	// if (!move(0f, -HALF_CELL_SIZE_Y, levelSceneAdapter))
	// return false;
	// ya += HALF_CELL_SIZE_Y;
	// }
	//
	// /** actual collision check is done here */
	// boolean collide = false;
	// /**
	// * determine whether the point (x+xa, y+ya) collides with anything. Six
	// * testing points around this are relevant: NE, NW, SE, SW, E, W, where
	// * N=North, ... Asymmetric, because mario is taller than he is wide.
	// * depending on the movement (left, right, up, down), only two or three
	// * of those four testing points need to be tested. if moving up: test NW
	// * and NE. down: SW and SE. left: NW and SW. right: NE and SE.
	// */
	// float[] testingPointsX = new float[xa != 0 ? 3 : 2];
	// float[] testingPointsY = new float[xa != 0 ? 3 : 2];
	// float bottomAdd = ya > 0 ? 1 : 0;
	// testingPointsX[0] = x + xa + (xa > 0 ? Mario.width : -Mario.width);
	// // test first mario's left side, unless he's moving to the right. In
	// // that case, test twice right
	// testingPointsX[1] = x + xa + (xa < 0 ? -Mario.width : Mario.width);
	// // then test mario's right side, unless he's moving to the left...
	// testingPointsY[0] = y + ya + (ya > 0 ? bottomAdd : -getHeight());
	// // test mario's head unless he's moving down.
	// // In that case, test twice bottom.
	// testingPointsY[1] = y + ya + (ya < 0 ? -getHeight() : bottomAdd);
	// if (xa != 0) {
	// testingPointsX[2] = testingPointsX[1];
	// // or x2=x0, doesn't matter. x1 and x0 are the same if
	// // x2 is required
	// testingPointsY[2] = y + ya - getHeight() / 2;
	// }
	// /** now iterate over all 2 or 3 points */
	// for (int i = 0; i < testingPointsY.length; i++) {
	// Point2D.Float testingPoint = new Point2D.Float(testingPointsX[i],
	// testingPointsY[i]);
	// if (levelSceneAdapter.isInMap(testingPoint).getKey()
	// && levelSceneAdapter.isBlocking(testingPoint, xa, ya)) {
	// collide = true;
	// break;
	// }
	// }
	// /** handle sliding flag */
	// if (xa != 0) {
	// sliding = collide;
	// // TODO: Does this make sense? Mario slides if
	// // and only if he collides? Code in
	// // Sprites.Mario seems like that...
	// }
	//
	// /** handle collision by replacing mario at a not occupied position. */
	// if (collide) {
	// if (xa < 0) {
	// x = (int) ((x - Mario.width) / 16) * 16 + Mario.width;
	// this.xa = 0;
	// }
	// if (xa > 0) {
	// x = (int) ((x + Mario.width) / 16 + 1) * 16 - Mario.width - 1;
	// this.xa = 0;
	// }
	// if (ya < 0) {
	// y = (int) ((y - getHeight()) / 16) * 16 + getHeight();
	// jumpTime = 0;
	// this.ya = 0;
	// }
	// if (ya > 0) {
	// y = (int) ((y - 1) / 16 + 1) * 16 - 1;
	// onGround = true;
	// }
	// return false;
	// /** if no collision happened, simply execute the movement */
	// } else {
	// x += xa;
	// y += ya;
	// return true;
	// }
	// }

	private void jumping(boolean[] keys) {
		if (keys[Player.KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding)) {
			// This is the 2nd, third, ... frame of a sliding jump. Here,
			// mario's y-speed is slowed down, but not as much as gravity would
			// slow him down.
			
			// diagonal jump
			if (jumpTime < 0) {
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				// this results in negative y-speed (upwards). The magnitude is
				// reduced in each frame, though.
				jumpTime++;
				// 1st frame of a jump from the ground.
				
			} else if (onGround && mayJump) {
				/** starting to jump */
				
				xJumpSpeed = 0;
				yJumpSpeed = -1.9f*individualYJumpSpeedMultiplier; // individual jump height for each player
				jumpTime = 7;
				ya = jumpTime * yJumpSpeed; // this is the initial jumping speed
				// in y-direction, it is negative
				// (upwards)
				onGround = false;
				sliding = false;
				// last frame of a wall jump
				
			} else if (sliding && mayJump) {
				xJumpSpeed = -facing * 6.0f;
				yJumpSpeed = -2.0f*individualYJumpSpeedMultiplier; // constant for wall jumps
				jumpTime = -6;
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed; // this is the initial jumping speed in y-direction
				// it is negative (upwards)
				onGround = false;
				sliding = false;
				facing = -facing;
				/*
				 * this is the 2nd, third,...frame of a straight jump. Here, mario's
				 * y-speed is slowed down, but not as much as gravity would slow him down.
				 * Here, the distinction between low jumps (shortly pressed jump key) and
				 * high jumps (longer pressed jump key) is made.
				 */
				
			} else if (jumpTime > 0) {
				xa += xJumpSpeed;
				ya = jumpTime * yJumpSpeed; // this results in negative y-speed
				--jumpTime;
				// (upwards). The magnitude is
				// reduced in each frame, though.
			}
		} else {
			jumpTime = 0;
			// from here on, gravity will do its job pulling mario down.
		}
	
	}
	
	private void leftorright(boolean[] keys, float sideWaysSpeed) {
		/** left key */
		if (keys[Player.KEY_LEFT] && !ducking) {
			if (facing == 1)
				sliding = false;
			xa -= sideWaysSpeed;
			if (jumpTime >= 0)
				facing = -1;
		}

		/** right key */
		if (keys[Player.KEY_RIGHT] && !ducking) {
			if (facing == -1)
				sliding = false;
			xa += sideWaysSpeed;
			if (jumpTime >= 0)
				facing = 1;
		}
	}
	
	
	private void physics() {
		/** EXTERNAL FORCES: inertia and gravitation */
		/** inertia */
		ya *= 0.85f;
		if (onGround) {
			xa *= Player.GROUND_INERTIA;
			ya *= 0.85f;
		} else {
			xa *= Player.AIR_INERTIA;
		}
		/** gravitation */
		if (!onGround) {
			ya += 3;
		}
	}
	

	public void startDiagonalJump(float factorxSpeed){
		xJumpSpeed = -facing * factorxSpeed;
		yJumpSpeed = -2.0f*individualYJumpSpeedMultiplier; // constant for wall jumps
		jumpTime = -6;
		xa = xJumpSpeed;
		ya = -jumpTime * yJumpSpeed; // this is the initial jumping
		// speed in y-direction. It is
		// negative (upwards).
		onGround = false;
		sliding = false;
		facing = -facing;
		// This is the 2nd, third, ... frame of a straight jump. Here,
		// mario's y-speed is slowed down, but not as much as gravity
		// would slow him down. Here the distinction between low jumps
		// (shortly pressed jump key) and high jumps (longer pressed
		// jump key) is made.
	}
	
	private void startStraightUpwardJump(){
		xJumpSpeed = 0;
		yJumpSpeed = -1.9f*individualYJumpSpeedMultiplier; // constant for ground jumps
		jumpTime = 7;
		ya = jumpTime * yJumpSpeed; // this is the initial jumping speed
		// in y-direction. It is negative
		// (upwards).
		onGround = false;
		sliding = false;
		// 1st frame of a wall jump.
	}

	public void straightUpwardJump(){
		xa += xJumpSpeed;
		ya = jumpTime * yJumpSpeed; // this results in negative y-speed
		// (upwards). The magnitude is
		// reduced in each frame, though.
		jumpTime--;
	}

//	private void carry() {
//		if(this.getCarriedBy() != null) 
//		{			
//			// the player first makes the carryMove; after that (carryTime == 0) he has always the same
//			// position as the player carriedBy
//			if(jumpTime == 0)
//			{
//				x = this.getCarriedBy().x;
//				y = this.getCarriedBy().y - this.getCarriedBy().getHeight();
//				
//				xa = 0;
//				ya = 0;
//				
//				facing = this.getCarriedBy().getFacing();
//				onGround = true;
//				mayJump = true;
//			}			
//		}
//	}
	
//	private CollisionAtPosition mountOtherPlayer(ArrayList<SimulatedPlayer> simulatedPlayersToCarry) 
//	{
//		for(SimulatedPlayer player : simulatedPlayersToCarry)
//		{
//			if(player == this) {
//				continue;
//			}
//
//			player.ya = 0;
//			player.xa = 0;
//			
//			int carryTime = 100;
//			float carryXDistance = (player.x - this.x) / carryTime;
//			float carryYDistance = (player.y - (this.y - this.height)) / carryTime;
//			for(int i = 0; i <= carryTime; i++)
//			{
//				if(player.y > player.y - this.getHeight()) player.y -= carryYDistance;
//				if(player.x > this.x) player.x -= carryXDistance;	
//			}
//			player.facing = this.facing;
//			
//			Condition condition = new Condition(this.type, player.type, getHealth(), wasInvulnerable(), true);
//			CollisionDirection dir;
//			if(player.x - this.x < 0)
//				dir = CollisionDirection.RIGHT;
//			else
//				dir = CollisionDirection.LEFT;
//				
//			this.setCarries(player);
//			player.setCarriedBy(this);
//			
//			return new CollisionAtPosition
//					(
//							new ConditionDirectionPair(condition, dir), 
//							player.getMapPosition(0),
//							this,
//							player
//					);
////			
////			return null;
//		}
//		
//		return null;
//	}
//	
	
//	//TODO: somewhat dirty function
//	private boolean isBlocking(SimulatedLevelScene scene, float _x, float _y, float xa, float ya) 
//	{ 
//		Level level();
//		
//		int x = (int) (_x / 16);
//		int y = (int) (_y / 16);
//		if (x == (int) (this.x / 16) && y == (int) (this.y / 16))
//			return false;
//		
//		boolean blocking = scene.level.isBlocking(x, y, xa, ya);
//		
//		byte block = scene.level.getBlock(x, y);
//
//		if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0 &&
//				!((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_LOWER) > 0))	//CHECK: ALL pickupables result in energy increase!? thats not good.
//		{
//			this.increaseEnergy(LevelScene.ENERGY_INCREASE);
//
//			//addEnergy();
//			
//			scene.level.setBlock(x, y, (byte) 0);
//			for (int xx = 0; xx < 2; xx++)
//				for (int yy = 0; yy < 2; yy++)
//					scene.addSprite(new Sparkle(x * 16 + xx * 8 + (int) (Math.random() * 8), y * 16 + yy * 8 + (int) (Math.random() * 8), 0, 0, 0, 2, 5));
//		}
//		
//		
//		// GATE-MECHANISM
//		// Only Tiles on Mapsheet with blocklower + special Combination
//		else if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_LOWER) > 0 && 
//				(Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_SPECIAL) > 0 &&
//				!((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_UPPER) > 0))
//		{
//			
//			// von right to left
//			if(xa < -1){
//			scene.level.setBlock(x, y, (byte) 213);
//			scene.level.setBlock(x, y- 1, (byte) 197);
//		
//			}
//			
//			// Door left to right marked as pickupable
//			if((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_PICKUPABLE) > 0){
//			// left to right
//			if(xa > 1) {
//				scene.level.setBlock(x, y, (byte) 214);
//				scene.level.setBlock(x, y- 1, (byte) 198);
//				}
//			}
//		}
//		
//		
//		// Block from above destruction
//		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_LOWER) > 0 && 
//				(Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_SPECIAL) > 0 &&
//				(Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_UPPER) > 0)
//		{
//			
//			// !!! HIER BEI IF ABFRAGE CHARAKTER EINFUEGEN !!!
//			if(ya > 0 // && character == jay oder so
//					){
//			scene.level.setBlock(x, y, (byte) 0);
//			}
//		}
//		
//		
//
//		if (blocking && ya < 0) 
//		{
//				scene.bump(x, y, large, this);
//		}
//
//		return blocking;
//	}	
	
	/*
	 * start carrying another player that is in x range of this player.
	 */
	private void carryOtherPlayer(SimulatedLevelScene scene) 
	{	
		// all Players in the scene could be carried
		for(SimulatedPlayer player : scene.getSimulatedPlayers())
		{
			// but the player can't carry himself
			if(player.getType() == getType()) 
			{
				continue;
			}
			
			float xPlayerD = player.x - this.x;
			float yPlayerD = player.y - this.y;
			
			if (!scene.isBlockingAgainstDirection(new GlobalContinuous(x, y-height).toGlobalCoarse(), CollisionDirection.IRRELEVANT))
			{
				// the first player in the list which has got adequate properties wil be carried
				if (xPlayerD > -this.width * 2 - 12 && xPlayerD < this.width * 2 + 12 && yPlayerD == 0)
				{
					player.ya = 0;
					player.xa = 0;
					player.facing = this.facing;
					player.x = x;
					player.y = y - height;
					
					break;
				}
			}			
		}
	}

	/*
	 * Sets the position of the player that is dropped
	 */
	private void dropOtherPlayer(SimulatedLevelScene scene)
	{
		if (!scene.isBlockingAgainstDirection(new GlobalContinuous(x + (facing * width), y).toGlobalCoarse(), CollisionDirection.IRRELEVANT))
		{
			this.getCarries().x = x + (facing * width);
			this.getCarries().y = y;
		}
	}	

	/**
	 * Sets the carry flags for players according to their positions
	 */
	public List<CollisionAtPosition> collideCheck(SimulatedSprite targetSprite, PlayerWorldObject[][] map)
	{
		float xTolerance=width*2+2;
		float yTolerance=height/2;
		
		// if sprite is a player: mount, drop, unmount, mount could be learned.. 
		if (targetSprite.getType().isPlayer()) 			
		{							
			ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();
			CollisionDirection dir;	
			
			//I MOUNTED SOMEONE
			float xTriggerPositionOfTarget=x;
			float yTriggerPositionOfTarget=y+height;
			if
			(
					getCarriedBy() == null &&
					Math.abs(targetSprite.x - xTriggerPositionOfTarget) <= xTolerance &&
					Math.abs(targetSprite.y - yTriggerPositionOfTarget) <= yTolerance &&
					// depends on which player is ticked first:
					// if first this is ticked, then the carries flag of the target is null
					// if first the target is ticked, then the carriesFlag must be set on this					
					(targetSprite.getCarries() == null || targetSprite.getCarries().getType() == this.getType()) &&
					// this player only mounts the target if this stands on the target and not just jump above the target
					this.jumpTime == 0
			) 
			{
				
//				System.out.println("-------------MOUNT-------------");
//				System.out.println("unten: " + targetSprite.toString() + " oben " + this.toString());
				this.setCarriedBy(targetSprite);

				dir = CollisionDirection.ABOVE;
				
				Condition condition = new Condition
						(
								this.type, 
								targetSprite.type, 
								getHealth(), 
								wasInvulnerable(), 
								true
						);
				
				ret.add(new CollisionAtPosition
							(
									new ConditionDirectionPair(condition, dir), 
									targetSprite.getMapPosition(0),
									this,
									targetSprite
							));
				
				return ret;
			}
			
			//I UNMOUNTED SOMEONE
			xTriggerPositionOfTarget=x;
			yTriggerPositionOfTarget=y+height;			
			if
			(
					this.getCarriedBy() != null && 
					this.getCarriedBy().getType() == targetSprite.getType() &&
					!(Math.abs(targetSprite.x - xTriggerPositionOfTarget) <= xTolerance &&
					Math.abs(targetSprite.y - yTriggerPositionOfTarget) <= yTolerance) &&
					// depends on which player is ticked first:
					// if first the target is ticked, the carries flag is null
					// if first this is ticked, then the carries flag must be the target
					(targetSprite.getCarries() == null || targetSprite.getCarries().getType() == this.getType())
			)
			{	
//				System.out.println("-----------UNMOUNT-----------");
//				System.out.println("unten: " + targetSprite.toString() + " oben " + this.toString());
				
				this.setCarriedBy(null);		

				dir = CollisionDirection.RIGHT;
				
				Condition condition = new Condition
						(
								this.type, 
								targetSprite.type, 
								getHealth(), 
								wasInvulnerable(), 
								true
						);
				
				ret.add(new CollisionAtPosition
							(
									new ConditionDirectionPair(condition, dir), 
									targetSprite.getMapPosition(0),
									this,
									targetSprite
							));
				
				return ret;
			}
			
			//I BEGAN TO CARRY SOMEONE
			xTriggerPositionOfTarget=x;
			yTriggerPositionOfTarget=y-height;						
			if
			( 
					getCarries() == null &&
					Math.abs(targetSprite.x - xTriggerPositionOfTarget) <= xTolerance &&
					Math.abs(targetSprite.y - yTriggerPositionOfTarget) <= yTolerance &&
					// depends on which player is ticked first
					// if first this is ticked, then the carriedBy flag of the target is null
					// if first the target is ticked, then the carriedBy flag must be set on this
					(targetSprite.getCarriedBy() == null || targetSprite.getCarriedBy().getType() == this.getType()) &&
					// this player only carries the target if the target stands on this player and not just jump above this player
					targetSprite.jumpTime == 0
			)
			{
//				System.out.println("------------CARRY------------");
//				System.out.println("unten: " + this.toString() + " oben " + targetSprite.toString());
				
				this.setCarries(targetSprite);
				
				dir = CollisionDirection.LEFT;
				
				Condition condition = new Condition
						(
								this.type, 
								targetSprite.type, 
								getHealth(), 
								wasInvulnerable(), 
								true
						);
				
				ret.add(new CollisionAtPosition
							(
									new ConditionDirectionPair(condition, dir), 
									targetSprite.getMapPosition(0),
									this,
									targetSprite
							));
				
				return ret;
			}
			
			//I DROPPED SOMEONE
			xTriggerPositionOfTarget=x;
			yTriggerPositionOfTarget=y-height;	
			
//			if(getCarries() != null){
//				System.out.println("-------------DROP-------------");
//				System.out.println("erste if richtig: " + getCarries().toString());
//				System.out.println("flag typ: " + getCarries().getType() + " target typ " + targetSprite.getType().toString());
//				System.out.println("Position: " + !(Math.abs(targetSprite.x - xTriggerPositionOfTarget) <= xTolerance &&
//					Math.abs(targetSprite.y - yTriggerPositionOfTarget) <= yTolerance));
//				System.out.println("last but not least " + (targetSprite.getCarriedBy() == null || targetSprite.getCarriedBy().getType() == this.getType()));
//			}
				
		
			if
			(
					getCarries() != null && 
					getCarries().getType() == targetSprite.getType() && 
					!(Math.abs(targetSprite.x - xTriggerPositionOfTarget) <= xTolerance &&
					Math.abs(targetSprite.y - yTriggerPositionOfTarget) <= yTolerance) &&
					// depends on which player is ticked first
					// if first the targetSprite is ticked, the the carriedBy flag is null
					// if first this is ticked, then the carriedBy flag must be the player that will be dropped
					(targetSprite.getCarriedBy() == null || targetSprite.getCarriedBy().getType() == this.getType())
			)
			{
//				System.out.println("-------------DROP-------------");
//				System.out.println("unten: " + this.toString() + " oben " + targetSprite.toString());
				
				setCarries(null);
				
				dir = CollisionDirection.BELOW;
				
				Condition condition = new Condition
						(
								this.type, 
								targetSprite.type, 
								getHealth(), 
								wasInvulnerable(), 
								true
						);
				
				ret.add(new CollisionAtPosition
							(
									new ConditionDirectionPair(condition, dir), 
									targetSprite.getMapPosition(0),
									this,
									targetSprite
							));
				
				return ret;
			}
		}
//		else if (PlayerWorldObject.ENEMIES.contains(targetSprite.getType())) 
//		{
//			ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();
//			float xPlayerD = this.x - targetSprite.x;
//			float yPlayerD = this.y - targetSprite.y;
//			int enemyWidth = targetSprite.getType().getWidthForCollisionDetection();
//			if (xPlayerD > -enemyWidth * 2 - 4 && xPlayerD < enemyWidth * 2 + 4) {
//				if (yPlayerD > -targetSprite.getHeight() && yPlayerD < this.getHeight()) {
//					if (targetSprite.getType() != PlayerWorldObject.SHALLY && targetSprite.type != PlayerWorldObject.SHALLY_WINGED && this.ya > 0 && yPlayerD <= 0
//							&& (!this.onGround || !this.isWasOnGround())) {
//						if (targetSprite.type.isWinged()) {
//							targetSprite.ya = 0;
//						}
//						// add the collision with the enemy
//						//@c2-pavel
//						//search in condition
//						Condition condition = new Condition(this.type, targetSprite.type, getHealth(), wasInvulnerable(), true);
//						ret.add(new CollisionAtPosition
//								(
//										new ConditionDirectionPair(condition, CollisionDirection.ABOVE), 
//										targetSprite.getOriginalSpritePosition().toGlobalCoarse(),
//										this,
//										targetSprite
//								));
//						// do the actual velocity changes
//						CollisionAtPosition add = velocityChangesOnStomp(targetSprite, map);
//						// if additional collisions occured during the velocity
//						// changes, add them as well
//						if (add != null)
//							ret.add(add);
//					}
//				}
//			}
//			return ret;
//		}
		
		return null;
	}	
	
//	/**
//	 * checks if mario is stomping on another player or an enemy. If he does, his movement-related state is modified accordingly. WARNING: may modify enemy
//	 * 
//	 * @param enemy
//	 * @param levelSceneAdapter
//	 */
//	public List<CollisionAtPosition> collideCheck(SimulatedSprite targetSprite, PlayerWorldObject[][] map) 
//	{
//		// if sprite is a player: mount, drop, unmount, mount could be learned.. 
//		if (targetSprite.type.isPlayer()) 
//		{	
//			ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();
//			CollisionDirection dir;			
//			
//			// this check whether this player "collides" with another from above and sets carriedBy flag
//			float xPlayerD = targetSprite.x - x;
//			float yPlayerD = targetSprite.y - y;
//			
//			//check that target is below me
//			if
//			(
//					(xPlayerD > -width * 2 - 4 && xPlayerD < width * 2 + 4) && 
//					(yPlayerD > 0 && Math.abs(yPlayerD) < targetSprite.getHeight() + 0.5 && Math.abs(yPlayerD) > targetSprite.getHeight() - 0.5) 
//			) 
//			{
//				//I MOUNT SOMEONE
//				if(this.getCarriedBy() == null/* && targetSprite.getCarries()==null*/)
//				{	
//					//System.out.println(this.getType()+" MOUNTS "+targetSprite);
//					
//					this.setCarriedBy(targetSprite);
//					
//					dir = CollisionDirection.ABOVE;
//					
//					Condition condition = new Condition
//							(
//									this.type, 
//									targetSprite.type, 
//									getHealth(), 
//									wasInvulnerable(), 
//									true
//							);
//					
//					ret.add(new CollisionAtPosition
//								(
//										new ConditionDirectionPair(condition, dir), 
//										targetSprite.getMapPosition(0),
//										this,
//										targetSprite
//								));
//					
//					return ret;
//				}				
//			}
//			
//			//I ACTIVELY UNMOUNT SOMEONE
//			if(this.getCarriedBy()!=null && this.getCarriedBy().getType() == targetSprite.getType() && ((y-targetSprite.getPosition(0).y)>height/2 || keys[Player.KEY_JUMP]))
//			{	
//				//System.out.println(this.getType()+" UNMOUNTS "+targetSprite);				
//				
//				//System.out.println("UNMOUNT - actor: " + this.toString() + " target: " + sprite.toString());
//
//				this.setCarriedBy(null);
//				
//				dir = CollisionDirection.RIGHT;
//				
//				Condition condition = new Condition
//						(
//								this.type, 
//								targetSprite.type, 
//								getHealth(), 
//								wasInvulnerable(), 
//								true
//						);
//				
//				ret.add(new CollisionAtPosition
//						(
//								new ConditionDirectionPair(condition, dir), 
//								targetSprite.getMapPosition(0),
//								this,
//								targetSprite
//						));
//				
//				return ret;
//			}
//			
//			// this check whether another player "collides" with this player from above and sets carries flag
//			if
//			( 
//					(xPlayerD > -width * 2 - 4 && xPlayerD < width * 2 + 4) &&
//					(yPlayerD < 0 && Math.abs(yPlayerD) < targetSprite.getHeight() + 0.5 && Math.abs(yPlayerD) > targetSprite.getHeight() - 0.5)
//			)
//			{
//				//I START TO CARRY SOMEONE
//				if(this.getCarries() == null)
//				{
//					//System.out.println(this.getType()+" CARRIES "+targetSprite);
//										
//					//System.out.println("CARRY - actor: " + this.toString() + " target: " + sprite.toString());
//					
//					this.setCarries(targetSprite);
//					
//					dir = CollisionDirection.LEFT;
//					
//					Condition condition = new Condition
//							(
//									this.type, 
//									targetSprite.type, 
//									getHealth(), 
//									wasInvulnerable(), 
//									true
//							);
//					
//					ret.add(new CollisionAtPosition
//							(
//									new ConditionDirectionPair(condition, dir), 
//									targetSprite.getMapPosition(0),
//									this,
//									targetSprite
//							));
//					
//					return ret;
//				}
//			}
//			
//			//I DROP SOMEONE (OR HE ACTIVELY UNMOUNTS)
//			if(getCarries() != null && getCarries().getType() == targetSprite.getType() && (targetSprite.ya<-0.1 || keys[Player.KEY_CARRYDROP]))	//TODO: dirty
//			{
//				//System.out.println(this.getType()+" DROPS "+targetSprite);
//				
////				if(this.facing >= 0)
////					dir = CollisionDirection.RIGHT;
////				else
////					dir = CollisionDirection.LEFT;
//				
//				setCarries(null);
//				
//				dir = CollisionDirection.BELOW;
//								
//				Condition condition = new Condition
//						(
//								this.type, 
//								targetSprite.type, 
//								getHealth(), 
//								wasInvulnerable(), 
//								true
//						);
//				
//				ret.add(new CollisionAtPosition
//						(
//								new ConditionDirectionPair(condition, dir), 
//								targetSprite.getOriginalSpritePosition().toGlobalCoarse(),
//								this,
//								targetSprite
//						));		
//				
//				return ret;
//			}
//			
//			return ret;
//		}
////		else if (PlayerWorldObject.ENEMIES.contains(targetSprite.type)) 
////		{
////			ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();
////			float xPlayerD = this.x - targetSprite.x;
////			float yPlayerD = this.y - targetSprite.y;
////			int enemyWidth = targetSprite.type.getWidthForCollisionDetection();
////			if (xPlayerD > -enemyWidth * 2 - 4 && xPlayerD < enemyWidth * 2 + 4) {
////				if (yPlayerD > -targetSprite.getHeight() && yPlayerD < this.getHeight()) {
////					if (targetSprite.type != PlayerWorldObject.SHALLY && targetSprite.type != PlayerWorldObject.SHALLY_WINGED && this.ya > 0 && yPlayerD <= 0
////							&& (!this.onGround || !this.isWasOnGround())) {
////						if (targetSprite.type.isWinged()) {
////							targetSprite.ya = 0;
////						}
////						// add the collision with the enemy
////						//@c2-pavel
////						//search in condition
////						Condition condition = new Condition(this.type, targetSprite.type, getHealth(), wasInvulnerable(), true);
////						ret.add(new CollisionAtPosition
////								(
////										new ConditionDirectionPair(condition, CollisionDirection.ABOVE), 
////										targetSprite.getOriginalSpritePosition().toGlobalCoarse(),
////										this,
////										targetSprite
////								));
////						// do the actual velocity changes
////						CollisionAtPosition add = velocityChangesOnStomp(targetSprite, map);
////						// if additional collisions occured during the velocity
////						// changes, add them as well
////						if (add != null)
////							ret.add(add);
////					}
////				}
////			}
////			return ret;
////		}
//		return null;
//	}
	
	/**
	 * does changes in mario's velocity when mario jumps on top of a sprite
	 * 
	 * @param sprite
	 *            the sprite on which mario jumps
	 * @param levelSceneAdapter
	 */
	private CollisionAtPosition velocityChangesOnStomp(SimulatedSprite sprite, PlayerWorldObject[][] map) {
		float targetY = sprite.y - sprite.getHeight() / 2;
		CollisionAtPosition ret = move(0, targetY - y, map).getValue();

		xJumpSpeed = 0;
		yJumpSpeed = -1.9f*individualYJumpSpeedMultiplier;
		jumpTime = 8;
		ya = jumpTime * yJumpSpeed;
		onGround = false;
		sliding = false;
		invulnerableTime = 1;
		return ret;
	}

	/**
	 * the height in pixels: typically 24(large) or 12(small)
	 * 
	 * @return
	 */
	@Override
	public int getHeight() {
		return STANDARD_HEIGHT;
//		return isLarge() ? STANDARD_HEIGHT : SMALL_HEIGHT;
	}

	/**
	 * Whether mario is large or small. This is also relevant for collisions and for movement because a small mario can not duck which in turn affects sliding.
	 * 
	 * @return
	 */
	private boolean isLarge() {
		return health > 0;
	}

	@Override
	public HashMap<Field, Object> getAttributeMap() {
		Field[] fields = getClass().getDeclaredFields();
		HashMap<Field, Object> map = new HashMap<Field, Object>();
		try { // loop over all fields
			for (Field field : fields) {
				map.put(field, field.get(this)); // read the value inside the
				// field for the instance
				// given by "this" and put
				// the value in the map
			}
		} catch (IllegalAccessException e) {
			new Throwable(e).printStackTrace();
		}
		map.putAll(super.getAttributeMap());
		return map;
	}

	@Override
	public boolean wasInvulnerable() {
		return oldInvulnerableTime > 0;
	}

	/**
	 * While getPosition() returns mario's feet, this method returns mario's center.
	 * 
	 * @param timeDelay
	 * @return
	 */
	public GlobalContinuous getPositionOfCenter() {
		GlobalContinuous feet = getPosition(0);
		return new GlobalContinuous(feet.x, feet.y - 0.5f * getHeight());
	}
	/*
	@Override
	public boolean equals(Object o){
		if (o == null) return false;
		if (o == this) return true;
		if (! o.getClass().equals(getClass())) return false;
		SimulatedPlayer other = (SimulatedPlayer) o;

		if
		(
				this.health != other.health || 
				this.numEnergys != other.numEnergys || 
				this.getCarries().getType() != other.getCarries().getType() || 
				this.getCarriedBy().getType() != other.getCarriedBy().getType()
		) 
		{
			return false;
		}
		
		return true;
	}*/
	@Override
	public boolean equals(Object o){
		if (o == null) return false;
		if (o == this) return true;
		if (! o.getClass().equals(getClass())) return false;
		SimulatedPlayer other = (SimulatedPlayer) o;
		if (this.health != other.health || this.numEnergys != other.numEnergys ){
			return false;
		}
		
		if((this.getCarriedBy() == null && other.getCarriedBy() != null) ||(this.getCarriedBy() != null && other.getCarriedBy() == null)) {
			return false;
		}
		
		if((this.getCarries() == null && other.getCarries() != null) ||(this.getCarries() != null && other.getCarries() == null)) {
			return false;
		}
		
		if(this.getCarriedBy() != null &&!this.getCarriedBy().getType().equals(other.getCarriedBy().getType())) {
			return false;
		}
		
		if(this.getCarries() != null && !this.getCarries().getType().equals(other.getCarries().getType())) {
			return false;
		}
		return true;
	}

	public boolean isWasOnGround() {
		return wasOnGround;
	}


	public float getWidth() {
		return width;
	}
	
	/**
	 * simulates all movement-related changes given the keys. Part of the brain's forward model. This includes movement-related changes done by object
	 * interactions. 
	 * ONLY USED TO SIMULATE REACHABILITY
	 * 
	 * @param keys
	 *            only relevant if this is mario
	 * @param collisionDirections 
	 * @param visited 
	 */
	public final List<CollisionAtPosition> simulateReachabilityMovement(PlayerWorldObject[][] map,
			boolean[] inputKeys, float xVelocity, float yVelocity, float[][] reachabilityCounter) {
		
		
			xOldOld = xOld;
			yOldOld = yOld;
			xOld = x;
			yOld = y;
			xaOld = xa;
			yaOld = ya;
			return move(map, inputKeys,xVelocity,yVelocity, reachabilityCounter);
		
	}
	
	/**
	 * This method moves the simulated mario given the keys. All this method does is changing mario's MOVEMENT_RELATED attributes, like position, velocity, etc.
	 * It does not influence the world around mario or mario's health. The movement-related functionality also handles collision with objects (as stated above,
	 * only the movement related changes). Furthermore, sliding, jumping to different heights and external forces like inertia & gravitation are covered.
	 * 
	 * 
	 * Used for the reachability calculation with variable x/y velocities
	 */
	
	protected List<CollisionAtPosition> move(PlayerWorldObject[][] map,boolean[] keys, float xVelocity, float yVelocity, 
			float[][]reachabilityCounter) 
	{
		oldInvulnerableTime = invulnerableTime;
		if (invulnerableTime > 0) {
			invulnerableTime--;		
		}
		
		birthTime++;

		float sideWaysSpeed = xVelocity;

		preAccelerationUpdateOfFlags(keys);

		// VELOCITY RELATED CHANGES
		/**
		 * jumps are done here, i.e. mario's velocity (xa, ya) are changed. Apparently, jumps straight upwards can differ in height (the higher the longer the
		 * jump key is pressed), while diagonal jumps always use the maximum height. For some reason negative jumpTimes encode sliding-jumps, while positive
		 * jumpTimes encode jumps straight upwards. Straight upwards jumps are a little higher and take longer than a sliding jumps.
		 */
		// if the jump key is pressed or if this is a diagonal jump (negative
		// jumpTime)
		if (keys[Player.KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding)) {
			// This is the 2nd, third, ... frame of a sliding jump. Here,
			// mario's y-speed is slowed down, but not as much as gravity would
			// slow him down.
			if (jumpTime < 0) {
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				// this results in negative y-speed (upwards). The magnitude is
				// reduced in each frame, though.
				jumpTime++;
				// 1st frame of a jump from the ground.
			} else if (onGround && mayJump) {
				/*release the carrie on jump*/
				
				/** starting to jump */
				xJumpSpeed = 0;
				yJumpSpeed = -yVelocity; // variable for ground jumps
				jumpTime = 7;
				ya = jumpTime * yJumpSpeed; // this is the initial jumping speed
				// in y-direction. It is negative
				// (upwards).
				onGround = false;
				sliding = false;
				// 1st frame of a wall jump.
			} else if (sliding && mayJump) {
				xJumpSpeed = -facing * 6.0f;
				yJumpSpeed = -yVelocity+0.1f; // constant for wall jumps
				jumpTime = -6;
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed; // this is the initial jumping
				// speed in y-direction. It is
				// negative (upwards).
				onGround = false;
				sliding = false;
				facing = -facing;
				// This is the 2nd, third, ... frame of a straight jump. Here,
				// mario's y-speed is slowed down, but not as much as gravity
				// would slow him down. Here the distinction between low jumps
				// (shortly pressed jump key) and high jumps (longer pressed
				// jump key) is made.
			} else if (jumpTime > 0) {
				xa += xJumpSpeed;
				ya = jumpTime * yJumpSpeed; // this results in negative y-speed
				// (upwards). The magnitude is
				// reduced in each frame, though.
				jumpTime--;
			}
		} else {
			jumpTime = 0;
			// from here on, gravity will do its job pulling mario down.
		}

		/** left key */
		if (keys[Player.KEY_LEFT] && !ducking) {
			if (facing == 1)
				sliding = false;
			xa -= sideWaysSpeed;
			if (jumpTime >= 0)
				facing = -1;
		}

		/** right key */
		if (keys[Player.KEY_RIGHT] && !ducking) {
			if (facing == -1)
				sliding = false;
			xa += sideWaysSpeed;
			if (jumpTime >= 0)
				facing = 1;
		}

		postAccelerationUpdateOfFlags(keys);

		/**
		 * inertia as defined below results in asymptotic behavior, so use a threshold to stop here.
		 */
		// runTime += (Math.abs(xa)) + 5;
		if (Math.abs(xa) < 0.5f) {
			// runTime = 0;
			xa = 0;
		}

		onGround = false;
		if (sliding) {
			ya *= 0.5f; // sliding down on wall
		}
		
		ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();
		
		
		
		/**
		 * This is the main movement call: move mario and do collision checks. This requires two calls: one for horizontal, one for vertical movement.
		 * Optionally, the object with which mario collided can be returned.
		 */
		/*
		 * TODO: Workaround,fix this move() to not have duplicate code wih only one difference in always beeing learnignrelevant
		 * 
		 */
		CollisionAtPosition xLearningRelevantCollision = move(xa, 0, map,true).getValue();
		CollisionAtPosition yLearningRelevantCollision = move(0, ya, map,true).getValue();

		
		

		/** left border reached */
		if (x < 0) {
			x = 0;
			xa = 0;
		}


		/** EXTERNAL FORCES: inertia and gravitation */
		/** inertia */
		ya *= 0.85f;
		if (onGround) {
			xa *= Player.GROUND_INERTIA;
		} else {
			xa *= Player.AIR_INERTIA;
		}
		/** gravitation */
		if (!onGround) {
			ya += 3;
		}


		
		if (xLearningRelevantCollision != null) {
			ret.add(xLearningRelevantCollision);
		}
		if (yLearningRelevantCollision != null) {
			ret.add(yLearningRelevantCollision);
		}
		/*
		if(ret.isEmpty() && carries != null){
			carries.x = this.x;
			carries.y = this.y-carries.getHeight();
		} */
		//used to draw continous positions

		GlobalCoarse gc = this.getMapPosition(0);
		/*
		 * checks if you´d go out of bounds and sets a collision with nothing
		 */
		if(y > 15 * 16  || x > 256*16){
			Condition condition = new Condition(this.type, PlayerWorldObject.NONE, getHealth(), wasInvulnerable(), true);
			ret.add( new CollisionAtPosition
						(
								new ConditionDirectionPair
									(
											condition, 
											CollisionDirection.ABOVE
									), 
								new GlobalCoarse(gc.x,15),
								this,
								null)
						);
		}

		if(gc.y > -1 && gc.y < reachabilityCounter.length && gc.x > -1 && gc.x < reachabilityCounter[0].length) {			
			reachabilityCounter[gc.y][gc.x] ++;
			if(gc.y > 0 && gc.y < reachabilityCounter.length-1 && gc.x > 0 && gc.x < reachabilityCounter[0].length-1){
				reachabilityCounter[gc.y-1][gc.x] ++;
			}
		}
		
		
		return ret;
	}
	/**
	 * sets wasOnGround, ducking and facing
	 * 
	 * @param keys
	 */
	private void preAccelerationUpdateOfFlags(boolean[] keys) {
		// wasOnGround = onGround;
		/**
		 * if mario is on the ground, large, and the ducking key is pressed, he will actually duck
		 */
		wasOnGround = onGround;
		if (onGround) {
			if (keys[Player.KEY_DOWN] && isLarge()) {
				ducking = true;
			} else {
				ducking = false;
			}
		}
		/** fast x-velocity changes mario's facing direction */
		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}
	}

	/**
	 * sets sliding & mayJump
	 * 
	 * @param keys
	 */
	private void postAccelerationUpdateOfFlags(boolean[] keys) {
		if ((!keys[Player.KEY_LEFT] && !keys[Player.KEY_RIGHT]) || ducking || ya < 0 || onGround) {
			sliding = false;
		}
		//mayJump = (onGround || sliding) && !keys[Player.KEY_JUMP];
		mayJump = (onGround || sliding);
	}

	/**
	 * checks if mario is mounting another player. If he does, his movement-related state is modified accordingly.
	 * 
	 * @param otherPlayer
	 * @param levelSceneAdapter
	 */
	public CollisionAtPosition mountCheck(SimulatedPlayer otherPlayer) {
		
		CollisionAtPosition ret = null;
		float xPlayerD = this.x - otherPlayer.x;
		float yPlayerD = this.y - otherPlayer.y;
		int otherPlayerWidth = otherPlayer.type.getWidthForCollisionDetection();
		if (xPlayerD > -otherPlayerWidth * 3 && xPlayerD < otherPlayerWidth * 3) {
			if (yPlayerD > -otherPlayer.getHeight() && yPlayerD < this.getHeight()) {
				if (this.ya > 0 && yPlayerD <= 0 && (!this.onGround || !this.wasOnGround) && otherPlayer.getCarries() == null) {
					
					// add the collision with the player
					Condition condition = new Condition(this.type, otherPlayer.type, getHealth(), wasInvulnerable(), true);
					ret = new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.ABOVE), otherPlayer.getMapPosition(0), this, otherPlayer);
					// 
			
					//mount(otherPlayer);
				}
			}
		}
		return ret;
	}
	/**
	 * checks if mario collides with otherPlayer from the side
	 * 
	 * @param otherPlayer
	 * @param levelSceneAdapter
	 */
	public List<CollisionAtPosition> collideCheck(SimulatedPlayer otherPlayer) {
		
		ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();

		float xPlayerD = this.x - otherPlayer.x;
		int playerWidth = otherPlayer.type.getWidthForCollisionDetection();
		if(this.y == otherPlayer.y){
			if (xPlayerD > -playerWidth  - 12 && xPlayerD < playerWidth  + 12) {
				if(xPlayerD <= 0) {
					Condition condition = new Condition(this.type, otherPlayer.type, getHealth(), wasInvulnerable(), true);
					ret.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.LEFT), this.getMapPosition(0), this,otherPlayer));
				} 
				if(xPlayerD >= 0) {
					Condition condition = new Condition(this.type, otherPlayer.type, getHealth(), wasInvulnerable(), true);
					ret.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.RIGHT), this.getMapPosition(0), this,otherPlayer));
				}
				//if(yPlayerD > -1 && yPlayerD < 1) {
				// add the collision with the player
				
			
				
			}
		}
		return ret;
	}
	
	
	
	

}
