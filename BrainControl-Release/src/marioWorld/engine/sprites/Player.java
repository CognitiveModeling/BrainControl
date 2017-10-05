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
package marioWorld.engine.sprites;
import marioWorld.engine.SpriteAnimation;
import marioWorld.engine.SpriteAnimationType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mario.main.PlayRelease;
import mario.main.Settings;
import mario.main.StartJar.ExitCode;
import marioAI.agents.CAEAgent;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.ReservoirEvent;
import marioAI.goals.ReservoirEventType;
import marioUI.gui.GameEndHandler;
import marioUI.gui.StartGuiModel;
import marioAI.run.PlayHook;
import marioAI.run.ReleaseGame;
import marioWorld.engine.Art;
import marioWorld.engine.GenericGetterForAllAttributes;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerVisualizationContainer;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.Scene;
import marioWorld.engine.SpriteRenderer;
import marioWorld.engine.PlayerWorldObject.ObjectType;
import marioWorld.engine.level.Level;
import marioWorld.utils.Sounds;
import marioWorld.utils.Sounds.Type;
import marioWorld.utils.UserDir;

/** gets Observable via Sprite */
public abstract class Player extends Sprite implements GenericGetterForAllAttributes {

	// originally, if mario collided with an object that was to his left, he
	// touched the object after the collision.
	// if, however, he collided with an object to his right, a gap of one pixel
	// occured.
	// That resulted in asymmetric behavior (e.g. wall-jumps could only be done
	// at walls to his left).
	// We introduced this custom change to fix that.
	public static final boolean USE_SYMMETRIC_COLLISIONS = true;
	public static GameEndHandler geh = new GameEndHandler();
	private boolean evaluatedYet = false;
	// constants
	private static final int FractionalPowerUpTime = 0;
	public static final int KEY_LEFT = 0;
	public static final int KEY_RIGHT = 1;
	public static final int KEY_DOWN = 2;
	public static final int KEY_JUMP = 3;
	public static final int KEY_SPEED = 4;
	public static final int KEY_CARRYDROP = 5;	
	public static final int KEY_UP = 6;
	public static final int KEY_PAUSE = 7;
	public static final int KEY_DUMP_CURRENT_WORLD = 8;
	public static final int KEY_LIFE_UP = 9;
	public static final int KEY_WIN = 10;
	public static final int STATUS_RUNNING = 2;
	public static final int STATUS_WIN = 1;
	public static final int STATUS_DEAD = 0;
	public static final float GROUND_INERTIA = 0.89f;
	public static final float AIR_INERTIA = 0.89f;
	public static final int width = 4;
	public static final int NUMBER_OF_NON_CHEATING_KEYS = 5;

	/*** NON_STATIC life for brain */
	private int mode = 2;
	private int energys = 100;		//Is changed through the Settings class upon player creation
	public static final int maxEnergySpeed = 150;
	private int healthLevel;
	public static final int healthLevelScaling = 3;
	public static int lives = 1024;
	private int status = STATUS_RUNNING;	

	public static int gainedWrenchs;
	public static int gainedFlowers;
	public static boolean isPlayerInvulnerable;
	// this determines whether the current object is the "real" mario or a clone
	private boolean isReal = true;

	public boolean[] keys;
	public boolean[] cheatKeys;
	private boolean mayJump = false;
	private int jumpTime = 0;
	private float xJumpSpeed;
	private float yJumpSpeed;
	protected float individualYJumpSpeedMultiplier = 1;
	protected boolean canDestroyFromAbove = false;
	private boolean canShoot = false;
	boolean wasOnGround = false;		
	private boolean sliding = false;	
	public boolean bulb = false;	

	/*
	 * The following properties are somewhat important for (also rendering) the movement of a player
	 * */
	protected float runTime;
	public boolean large = false;	
	boolean onGround = false;
	public int facing;
	protected boolean ducking = false;
	int height = 24;
	//also xa in sprite
	//possibly also bulb and others

	public LevelScene scene;	//this is imply a reference to the level scene (not really needed)
	private int powerUpTime = 0; // exclude pause for rendering changes

	public int xDeathPos, yDeathPos;

	public int birthTime = 0;		//time of last death
	public int winTime = 0;
	public int invulnerableTime = 0;	

	protected boolean lastLarge;
	protected boolean lastBulb;
	protected boolean newLarge;

	protected boolean newBulb;
	private boolean reachedEndItem = false;
	private boolean reachedLevelEnd = false;
	
	private int[][] exploredMap;
	
	
	final int playerIndex;

	public int getPlayerIndex()
	{
		return playerIndex;
	}

	public void setLevelScene(LevelScene world)
	{
		this.scene = world;
	}

	public Player(int playerIndex, int marioMode) 
	{
		this.playerIndex=playerIndex;

		//resetStatic(BasicSimulator.simulationOptions.getMarioMode());
		classesWithStaticStuff.add(this.getClass());

		//default renderer
		renderer = new SpriteRenderer(this, Art.clark, 0, 0);

		resetStaticAttributes();
		resetStatic(marioMode);

		kind = KIND_PLAYER;

		//this.scene = world;

		keys = Scene.keys; // SK: in fact, this is already redundant due to
		// using Agent
		cheatKeys = Scene.keys; // SK: in fact, this is already redundant due to
		// using Agent

		facing = 1;
		setLarge(true, false);
		healthLevel = healthLevelScaling;
	}

	public void resetStatic(int marioMode) 
	{
		large = marioMode > 0;
		bulb = marioMode == 2;

		//resetEnergys();

		gainedWrenchs = 0;
		gainedFlowers = 0;		

		//TODO: there should be some default position per implementation of player, maybe depending on the map
		//x = 32;
		x = 2 * 16;
		y = 0;
		xa = 0;
		ya = 0;				

		// lives = 65536;
		// levelString = "none";
		// numberOfAttempts = 0;

		birthTime=0;
	}

	public int getHeight() {
		return height;
	}

	public int getNonStaticHealth() {
		return mode;
	}

	@Override
	public Player cloneWithNewLevelScene(LevelScene ls) {
		Player ret = (Player) super.clone();
		ret.mode = mode;
		ret.status = status;
		ret.keys = keys.clone();
		ret.cheatKeys = cheatKeys.clone();
		ret.runTime = runTime;
		ret.wasOnGround = wasOnGround;
		ret.onGround = onGround;
		ret.mayJump = mayJump;
		ret.ducking = ducking;
		ret.sliding = sliding;
		ret.jumpTime = jumpTime;
		ret.xJumpSpeed = xJumpSpeed;
		ret.yJumpSpeed = yJumpSpeed;
		ret.individualYJumpSpeedMultiplier = individualYJumpSpeedMultiplier;
		ret.canShoot = canShoot;
		ret.height = height;
		ret.facing = facing;
		ret.powerUpTime = powerUpTime;
		ret.xDeathPos = xDeathPos;
		ret.yDeathPos = yDeathPos;
		ret.birthTime = birthTime;
		ret.winTime = winTime;
		ret.invulnerableTime = invulnerableTime;
		ret.setCarries(this.getCarries());
		ret.setCarriedBy(this.getCarriedBy());
		ret.lastLarge = lastLarge;
		ret.lastBulb = lastBulb;
		ret.newLarge = newLarge;
		ret.newBulb = newBulb;
		ret.x = x;
		ret.y = y;
		ret.xa = xa;
		ret.ya = ya;
		ret.xOld = xOld;
		ret.yOld = yOld;
		ret.xaOld = xaOld;
		ret.yaOld = yaOld;
		ret.mapX = mapX;
		ret.mapY = mapY;
		// this is set to false, so the clone wont evaluate if moved
		ret.isReal = false;
		ret.energys = energys;
		ret.scene = ls;
		ret.exploredMap = exploredMap;
		return ret;
	}

	// public void setMode(MODE mode)
	// {
	// large = (mode == MODE.MODE_LARGE);
	// bulb = (mode == MODE.MODE_BULB);
	// }

	public int getMode() {
		return ((large) ? 1 : 0) + ((bulb) ? 1 : 0);
	}

	public static enum MODE {
		MODE_SMALL, MODE_LARGE, MODE_BULB
	}

	public void resetEnergys() 
	{
		//		// update view
		//		this.setChanged();
		//		this.notifyObservers(new PlanningEvent(PlanningEventType.PLAYER_STATE_CHANGED));		

		// update reservoirs
		setChanged();
		notifyObservers(new ReservoirEvent(ReservoirEventType.ENERGY_CHANGE,100-energys));

		energys = 100;
	}

	public boolean decreaseEnergys(int decrease)
	{
		if (energys >= decrease)
		{
			energys-=decrease;

			//			// update view
			//			this.setChanged();
			//			this.notifyObservers(new PlanningEvent(PlanningEventType.PLAYER_STATE_CHANGED));

			// update reservoirs
			setChanged();
			notifyObservers(new ReservoirEvent(ReservoirEventType.ENERGY_CHANGE,-decrease));			

			return true;
		} 
		else 
		{
			energys = 0;
			return false;
		}
	}

	public void increaseEnergy(int increase)
	{
		Sounds.play(Type.ENERGY);
		energys += increase;

		// update reservoirs
		setChanged();
		notifyObservers(new ReservoirEvent(ReservoirEventType.ENERGY_CHANGE,increase));					
	}

	public void setLarge(boolean large, boolean bulb) {
		

		if (bulb) {
			large = true;	
		}
			
		if (!large)
			bulb = false;

		lastLarge = this.large;
		lastBulb = this.bulb;

		this.large = large;
		this.bulb = bulb;

		newLarge = this.large;
		newBulb = this.bulb;

		blink(true);
	}

	protected void setNonStaticHealth() {
		if (bulb)
			mode = 2;
		else if (large)
			mode = 1;
		else
			mode = 0;
	}

	protected abstract void blink(boolean on);

	/**
	 * isReal (i.e. no simulation) is currently only used to ensure, that the world is changed only for real movements. It would be best if this flag also
	 * ensured other things.
	 */
	@Override
	public void move() {

		if (checkTime()){
			return;
		}

//		float sideWaysSpeed = keys[KEY_SPEED] ? 1.2f : 0.6f;
		float sideWaysSpeed = calculateSpeed();
		// float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		initducking();
		initfacing();

		// VELOCITY RELATED CHANGES
		/**
		 * jumps are done here, i.e. mario's velocity (xa, ya) are changed. Apparently, jumps straight upwards can differ in height (the higher the longer the
		 * jump key is pressed), while diagonal jumps always use the maximum height. For some reason negative jumpTimes encode sliding-jumps, while positive
		 * jumpTimes encode jumps straight upwards. Straight upwards jumps are a little higher and take longer than a sliding jumps.
		 */
		// if the jump key is pressed or if this is a diagonal jump (negative
		// jumpTime)
		jumping();
		leftorright(sideWaysSpeed);


		if ((!keys[KEY_LEFT] && !keys[KEY_RIGHT]) || ducking || ya < 0 || onGround) {
			sliding = false;
		}

		if (keys[KEY_SPEED] && canShoot && this.bulb && isReal && scene.fireballsOnScreen < 2) {
			scene.addSprite(new Fireball(scene, x + facing * 6, y - 20, facing));
		}

		if (keys[KEY_CARRYDROP] && xa > -2 && xa < 2 && this.getCarries() == null) 
		{
			carryOtherPlayer();
		}
		else if (keys[KEY_CARRYDROP] && this.getCarries() != null) 
		{
			dropOtherPlayer();
		}

		// Cheats:
		/*if (GlobalOptions.PowerRestoration && keys[KEY_SPEED] && (!this.large || !this.bulb))
			setLarge(true, true);
		if (cheatKeys[KEY_LIFE_UP])
			Player.lives++;
		if (isReal)
			scene.paused = GlobalOptions.pauseWorld;
		if (cheatKeys[KEY_WIN])
			win();*/
		// if (keys[KEY_DUMP_CURRENT_WORLD])
		// try {
		// System.out.println("DUMP:");
		// // world.LevelSceneAroundMarioASCII(System.out);
		// //world.level.save(System.out);
		// System.out.println("DUMPED:");
		// } catch (IOException e) {
		// e.printStackTrace(); //To change body of catch statement use File |
		// Settings | File Templates.
		// }
		canShoot = !keys[KEY_SPEED];

		if ((!keys[KEY_LEFT] && !keys[KEY_RIGHT]) || ducking || ya < 0 || onGround) {
			sliding = false;
		}

		mayJump = (onGround || sliding) && !keys[KEY_JUMP];

		renderer.properties().flipFrameX = facing == -1;

		runTime += (Math.abs(xa)) + 5;
		if (Math.abs(xa) < 0.2f) { //if (Math.abs(xa) < 0.5f) {           SAB Important
			runTime = 0;
			xa = 0;
		}

		if (isReal)
			calcPic();

		if (sliding) {
			if (isReal) {
				for (int i = 0; i < 1; i++) {
					scene.addSprite(new Sparkle((int) (x + Math.random() * 4 - 2) + facing * 8, (int) (y + Math.random() * 4) - 24,
							(float) (Math.random() * 2 - 1), (float) Math.random() * 1, 0, 1, 5));
				}
			}
			ya *= 0.5f;
		}

		onGround = false;
		//onGround is initialized here, it will be updated in move(int, int)

		move(xa, 0);
		move(0, ya);

		// calculates the levelboundaries
		if (isReal && y > scene.level.height * 16 + 16) {
			die();
		}

		if (x < 0) {
			x = 0;
			xa = 0;
		}

		if (isReal && x > scene.level.xExit * 16) {
			x = scene.level.xExit * 16;
			reachLevelEnd();
		}

		if (isReal && x > scene.level.width * 16) {
			x = scene.level.width * 16;
			xa = 0;
		}

		physics();

		moveCarry();
		updateExploredMap(mapY,mapX);
		
	}

	private void updateExploredMap(int mapY, int mapX) {
		if(exploredMap == null) {
			exploredMap = new int[scene.level.height][scene.level.width];
		}
		if(mapY >= 0 && mapY < scene.level.height && mapX >= 0 && mapX < scene.level.width) {
			exploredMap[mapY][mapX] =1;
			}
	}
	
	private float calculateSpeed() {
		int normalizedEnergy = energys > maxEnergySpeed? maxEnergySpeed : energys;
		float sideWaysSpeed = keys[KEY_SPEED] ? 1.2f : 0.6f;
		sideWaysSpeed = (sideWaysSpeed / 2.0f) + ((sideWaysSpeed / 2.0f) * (normalizedEnergy /100.0f));
		
		
		return sideWaysSpeed;
	}
	
	private void physics() {
		ya *= 0.85f;
		if (onGround) {
			xa *= GROUND_INERTIA;
			ya *= 0.85f;
		} else {
			xa *= AIR_INERTIA;
		}

		if (!onGround) {			
			ya += 3;
		}
	}


	/*
	 * moves the carried player with the carrier. only when carry was invoked in previous time steps.
	 * */
	private void moveCarry() 
	{
		// if the player is carrying some other players, then the positions etc of these players are adapted
		if (this.getCarries() != null && !keys[KEY_CARRYDROP]) 
		{
			
			Player carriedPlayer = this;
			// counts how many player this is carrying to set the y-coordinate
			int numberOfCarriedPlayers = 0;
			
			// every player that stands on this player should have the adapted properties
			do
			{
				carriedPlayer = (Player) carriedPlayer.getCarries();
				numberOfCarriedPlayers++;
				carriedPlayer.onGround = true;
				carriedPlayer.mayJump = true;
				
				// but if the player jump, then the properties are allowed to change
				if(!carriedPlayer.keys[Player.KEY_JUMP])
				{			
					// test if the carried player collides with an static object
					// if there is an collision in horizontal or vertical direction
//					if(!carryCollision(xa, 0, carriedPlayer.x, carriedPlayer.y, carriedPlayer.height) || !carryCollision(0, ya, carriedPlayer.x, carriedPlayer.y, carriedPlayer.height))
//					{
//						// then the player will be set down
//						//carriedPlayer.y = y;
////						carriedPlayer.xa = 0;
////						carriedPlayer.ya = 0;
////						this.xa = 0;
////						this.ya = 0;
////						this.x = carriedPlayer.x;
//
//						
//					}
//					
//					// else the carried player should have adapted properties
//					else
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
		if (this.getCarries() != null && !keys[KEY_CARRYDROP]) 
		{
			
			Player carriedPlayer = this;
			// counts how many player this is carrying to set the y-coordinate
			int numberOfCarriedPlayers = 0;
			
			// every player that stands on this player should have the adapted properties
			do
			{
				carriedPlayer = (Player) carriedPlayer.getCarries();
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
	
	private boolean carryCollision(float xa, float ya, float x, float y, float height) {
		
		
		final float addXForIsBlockingToRight = USE_SYMMETRIC_COLLISIONS ? (float) (-1E-3) : 0;

		while (xa > 8) 
		{
			if (!carryCollision(8, 0, x, y, height))
				return false;
			xa -= 8;
		}

		while (xa < -8) 
		{
			if (!carryCollision(-8, 0, x, y, height))
				return false;
			xa += 8;
		}

		while (ya > 8) 
		{
			if (!carryCollision(0, 8, x, y, height))
				return false;
			ya -= 8;
		}

		while (ya < -8) 
		{
			if (!carryCollision(0, -8, x, y, height))
				return false;
			ya += 8;
		}

		boolean collide = false;
		if (isReal) {
			if (ya > 0) {
				if (isBlocking(x + xa - width, y + ya, xa, 0))
					collide = true;
				// else if (isBlocking(x + xa + width +
				// (USE_SYMMETRIC_COLLISIONS ? -1 : 0), y + ya, xa, 0))
				else if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya, xa, 0))
					collide = true;
				else if (isBlocking(x + xa - width, y + ya + 1, xa, ya))
					collide = true;
				// else if (isBlocking(x + xa + width +
				// (USE_SYMMETRIC_COLLISIONS ? -1 : 0), y + ya + 1, xa, ya))
				else if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya + 1, xa, ya))
					collide = true;
			}
			if (ya < 0) {
				if (isBlocking(x + xa, y + ya - height, xa, ya))
					collide = true;
				else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya))
					collide = true;
				else if (collide
						// || isBlocking(x + xa + width + (USE_SYMMETRIC_COLLISIONS ? -1
						// : 0), y + ya - height, xa, ya))
						|| isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya - height, xa, ya))
					collide = true;
			}
			if (xa > 0) {
				
				// SOLLTE DAS HIER WIRKLICH GETAN WERDEN??? du schaust dir ja die Werte vom carriedPlayer an aber würdest von
				// this das sliding-Verhalten ändern..
				//sliding = true;
				//!!!!!!!!!!!!!!!!!!
				
				
				// if (isBlocking(x + xa + width + (USE_SYMMETRIC_COLLISIONS ?
				// -1 : 0), y + ya - height, xa, ya))
				if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya - height, xa, ya))
					collide = true;
				//else
				//	sliding = false;
				// if (isBlocking(x + xa + width + (USE_SYMMETRIC_COLLISIONS ?
				// -1 : 0), y + ya - height / 2, xa, ya))
				if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya - height / 2, xa, ya))
					collide = true;
				//else
				//	sliding = false;
				// if (isBlocking(x + xa + width + (USE_SYMMETRIC_COLLISIONS ?
				// -1 : 0), y + ya, xa, ya))
				if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya, xa, ya))
					collide = true;
				//else
				//	sliding = false;
			}
			if (xa < 0) {
				//sliding = true;
				if (isBlocking(x + xa - width, y + ya - height, xa, ya))
					collide = true;
				//else
				//	sliding = false;
				if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya))
					collide = true;
				//else
				//	sliding = false;
				if (isBlocking(x + xa - width, y + ya, xa, ya))
					collide = true;
				//else
				//	sliding = false;
			}
		}
		
		return !collide;

	}


	/*
	// private void testSimulation() {
	// 
	// * low value (zero): test all attributes, high value(2): test only some
	// * attributes
	// 
	// int ignoreLevel = 2; // in {0, 1, 2}
	// switch (ignoreLevel) {
	// case 0: {
	// if (jumpTime != debugMarioClone.jumpTime)
	// new Throwable("different jumpTime").printStackTrace();
	// if (invulnerableTime != debugMarioClone.invulnerableTime)
	// new Throwable("different invulnerableTime").printStackTrace();
	// if (wasOnGround != debugMarioClone.wasOnGround)
	// new Throwable("different was on ground").printStackTrace();
	// if (mayJump != debugMarioClone.mayJump)
	// new Throwable("different mayJump").printStackTrace();
	// if (onGround != debugMarioClone.onGround)
	// new Throwable("different on ground").printStackTrace();
	// if (canShoot = debugMarioClone.canShoot)
	// new Throwable("different canShoot").printStackTrace();
	// // no break!!
	// }
	// case 1: {
	// if (ducking != debugMarioClone.ducking)
	// new Throwable("different ducking").printStackTrace();
	// if (sliding != debugMarioClone.sliding)
	// new Throwable("different sliding").printStackTrace();
	// if (facing != debugMarioClone.facing)
	// new Throwable("different facing").printStackTrace();
	// if (yJumpSpeed != debugMarioClone.yJumpSpeed)
	// new Throwable("different yJumpSpeed").printStackTrace();
	// if (carried != debugMarioClone.carried)
	// new Throwable("different carried").printStackTrace();
	// // no break!!
	// }
	// case 2: {
	// if (energys != debugMarioClone.energys)
	// new Throwable("different amount of energys").printStackTrace();
	// if (mode != debugMarioClone.mode)
	// new Throwable("different modes").printStackTrace();
	// if (large != debugMarioClone.large)
	// new Throwable("different size").printStackTrace();
	// if (bulb != debugMarioClone.bulb)
	// new Throwable("different bulb ability").printStackTrace();
	// if (status != debugMarioClone.status)
	// new Throwable("different status").printStackTrace();
	// if (xJumpSpeed != debugMarioClone.xJumpSpeed)
	// new Throwable("different xJumpSpeed").printStackTrace();
	// if (powerUpTime != debugMarioClone.powerUpTime)
	// new Throwable("different powerUpTime").printStackTrace();
	//
	// }
	// }
	// } */


	private void leftorright(float sideWaysSpeed) {
		if (keys[KEY_LEFT] && !ducking) {
			if (facing == 1)
				sliding = false;
			xa -= sideWaysSpeed;
			if (jumpTime >= 0)
				facing = -1;
		}

		if (keys[KEY_RIGHT] && !ducking) {
			if (facing == -1)
				sliding = false;
			xa += sideWaysSpeed;
			if (jumpTime >= 0)
				facing = 1;
		}
	}

	private void jumping() {
		if (keys[KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding)) {
			// This is the 2nd, third, ... frame of a sliding jump. Here,
			// mario's y-speed is slowed down, but not as much as gravity would
			// slow him down.
			if (jumpTime < 0) {
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				jumpTime++;

			} else if (onGround && mayJump) {
				/** starting to jump */
				//onGroundJump

				Sounds.play(Type.JUMP);

				xJumpSpeed = 0;
				yJumpSpeed = -1.9f*individualYJumpSpeedMultiplier; // constant for ground jumps

				jumpTime = 7;
				ya = jumpTime * yJumpSpeed; // this is the initial jumping speed
				// in y-direction. It is negative
				// (upwards).
				onGround = false;
				sliding = false;
				// 1st frame of a wall jump.

			} else if (sliding && mayJump) {
				//slidingJump
				Sounds.play(Type.JUMP);
				xJumpSpeed = -facing * 6.0f;
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

			} else if (jumpTime > 0) {
				xa += xJumpSpeed;
				ya = jumpTime * yJumpSpeed;
				jumpTime--;
			}
		} else {
			jumpTime = 0;
			// from here on, gravity will do its job pulling mario down.
		}

	}


	public void straightUpwardJump(){
		xa += xJumpSpeed;
		ya = jumpTime * yJumpSpeed; // this results in negative y-speed
		// (upwards). The magnitude is
		// reduced in each frame, though.
		jumpTime--;
	}

	private void initfacing() {
		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}
	}

	private void initducking() {
		wasOnGround = onGround;

		if (onGround) {
			if (keys[KEY_DOWN] && large) {
				ducking = true;
			} else {
				ducking = false;
			}
		}	
	}

	private Boolean checkTime() {
		// debugMarioClone = internalFakeClone();
		if (winTime > 0) {
			winTime++;

			xa = 0;
			ya = 0;
			return true;
		}

		birthTime++;		

		//				if (deathTime > 0) {
		//					deathTime++;
		//					if (deathTime < 11) {
		//						xa = 0;
		//						ya = 0;
		//					} else if (deathTime == 11) {
		//						ya = -15;
		//					} else {
		//						ya += 2;
		//					}
		//					x += xa;
		//					y += ya;
		//					return;
		//				}

		if (powerUpTime != 0) {
			if (powerUpTime > 0) {
				powerUpTime--;
				blink(((powerUpTime / 3) & 1) == 0);
			} else {
				powerUpTime++;
				blink(((-powerUpTime / 3) & 1) == 0);
			}

			if (isReal && powerUpTime == 0)
				scene.paused = false;

			calcPic();
			return true;
		}

		if (invulnerableTime > 0)
			invulnerableTime--;
		renderer.properties().isVisible = ((invulnerableTime / 2) & 1) == 0;
		return false;

	}

	protected void calcPic() 
	{
		// code for new sprite sheets

		//things that should not be in this function:
		height = ducking ? 12 : 24;

		//create animations with respect to the player state:
		SpriteAnimation animation = null;

		if(isOnGround())
		{
			if(Math.abs(xa+ya)<1)
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Stand, true, 30);
			else if(Math.abs(xa) < 8)
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Walk, true, 8);
			else
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Run, true, 4);
		}
		else
		{
			//			if(ya <= 0)
			animation = new SpriteAnimation(renderer, SpriteAnimationType.Jump, false, 2);
			//									else if(Math.abs(ya) < 1.5)
			//										animation = new SpriteAnimation(renderer, SpriteAnimationType.Fly, true, 1);
			//			else// if(ya > 1.5)
			//				animation = new SpriteAnimation(renderer, SpriteAnimationType.Fall, false, 1);
		}

		if(ducking)
			animation = new SpriteAnimation(renderer, SpriteAnimationType.Duck, true, 1);

		renderer.setAnimation(animation);

		// previous code with old spritesheets

		/*
		int runFrame = 0;

		if (large) {
			runFrame = ((int) (runTime / 20)) % 4;
			if (runFrame == 3)
				runFrame = 1;
			if (carries == null && Math.abs(xa) > 10)
				runFrame += 3;
			if (carries != null)
				runFrame += 10;
			if (!onGround) {
				if (carries != null)
					runFrame = 12;
				else if (Math.abs(xa) > 10)
					runFrame = 7;
				else
					runFrame = 6;
			}
		} else {
			runFrame = ((int) (runTime / 20)) % 2;
			if (carries == null && Math.abs(xa) > 10)
				runFrame += 2;
			if (carries != null)
				runFrame += 8;
			if (!onGround) {
				if (carries != null)
					runFrame = 9;
				else if (Math.abs(xa) > 10)
					runFrame = 5;
				else
					runFrame = 4;
			}
		}

		if (onGround && ((facing == -1 && xa > 0) || (facing == 1 && xa < 0))) {
			if (xa > 1 || xa < -1)
				runFrame = large ? 9 : 7;

			if (xa > 3 || xa < -3) {
				for (int i = 0; i < 3; i++) {
					scene.addSprite(new Sparkle((int) (x + Math.random() * 8 - 4), (int) (y + Math.random() * 4), (float) (Math.random() * 2 - 1), (float) Math
							.random() * -1, 0, 1, 5));
				}
			}
		}

		if (large) {
			if (ducking)
				runFrame = 14;
			height = ducking ? 12 : 24;
		} else {
			height = 12;
		}

		renderer.properties().selectedFrameX = runFrame;
		 */
	}

	private boolean move(float xa, float ya) 
	{
		int height = this.height;
		if(this.getCarries() != null){
			height = this.height+this.getCarries().getHeight();
		}
		final float addXForIsBlockingToRight = USE_SYMMETRIC_COLLISIONS ? (float) (-1E-3) : 0;

		while (xa > 8) 
		{
			if (!move(8, 0))
				return false;
			xa -= 8;
		}

		while (xa < -8) 
		{
			if (!move(-8, 0))
				return false;
			xa += 8;
		}

		while (ya > 8) 
		{
			if (!move(0, 8))
				return false;
			ya -= 8;
		}

		while (ya < -8) 
		{
			if (!move(0, -8))
				return false;
			ya += 8;
		}

		boolean collide = false;
		boolean carryCollide = false;
		if (isReal) {
			if (ya > 0) {
				
				if(this.getCarries()!= null) {
					carryCollide = !carryCollision(xa, ya, this.getCarries().x, this.getCarries().y, this.getCarries().getHeight());
				}
				
				if (isBlocking(x + xa - width, y + ya + 1, xa, ya))
					collide = true;
				// else if (isBlocking(x + xa + width +
				// (USE_SYMMETRIC_COLLISIONS ? -1 : 0), y + ya + 1, xa, ya))
				else if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya + 1, xa, ya))
					collide = true;
				else if (isBlocking(x + xa - width, y + ya, xa, 0))
					collide = true;
				// else if (isBlocking(x + xa + width +
				// (USE_SYMMETRIC_COLLISIONS ? -1 : 0), y + ya, xa, 0))
				else if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya, xa, 0))
					collide = true;
				
			}
			if (ya < 0) {
				if(this.getCarries()!= null) {
					carryCollide = !carryCollision(xa, ya, this.getCarries().x, this.getCarries().y, this.getCarries().getHeight());
				}
				if (isBlocking(x + xa, y + ya - height, xa, ya))
					collide = true;
				else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya))
					collide = true;
				else if (collide
						// || isBlocking(x + xa + width + (USE_SYMMETRIC_COLLISIONS ? -1
						// : 0), y + ya - height, xa, ya))
						|| isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya - height, xa, ya))
					collide = true;
			}
			if (xa > 0) {
				if(this.getCarries()!= null) {
					carryCollide = !carryCollision(xa, ya, this.getCarries().x, this.getCarries().y, this.getCarries().getHeight());
				}
				sliding = true;
				// if (isBlocking(x + xa + width + (USE_SYMMETRIC_COLLISIONS ?
				// -1 : 0), y + ya - height, xa, ya))
				if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya - height, xa, ya))
					collide = true;
				else
					sliding = false;
				// if (isBlocking(x + xa + width + (USE_SYMMETRIC_COLLISIONS ?
				// -1 : 0), y + ya - height / 2, xa, ya))
				if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya - height / 2, xa, ya))
					collide = true;
				else
					sliding = false;
				// if (isBlocking(x + xa + width + (USE_SYMMETRIC_COLLISIONS ?
				// -1 : 0), y + ya, xa, ya))
				if (isBlocking(x + xa + width + addXForIsBlockingToRight, y + ya, xa, ya))
					collide = true;
				else
					sliding = false;
			}
			if (xa < 0) {
				if(this.getCarries()!= null) {
					carryCollide = !carryCollision(xa, ya, this.getCarries().x, this.getCarries().y, this.getCarries().getHeight());
				}
				sliding = true;
				if (isBlocking(x + xa - width, y + ya - height, xa, ya))
					collide = true;
				else
					sliding = false;
				if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya))
					collide = true;
				else
					sliding = false;
				if (isBlocking(x + xa - width, y + ya, xa, ya))
					collide = true;
				else
					sliding = false;
			}
		}

		if (collide || carryCollide) {
			if (xa < 0) {
				x = (int) ((x - width) / 16) * 16 + width;
				this.xa = 0;
			}
			if (xa > 0) {
				x = (int) ((x + width + addXForIsBlockingToRight) / 16 + 1) * 16 - width;
				this.xa = 0;
			}
			if (ya < 0) {
				y = (int) ((y - height) / 16) * 16 + height;
				jumpTime = 0;
				this.ya = 0;
			}
			if (ya > 0) {
				y = (int) ((y - 1) / 16 + 1) * 16 - 1;
				onGround = true;
			}
			return false;
		} else 
		{
			x += xa;
			y += ya;
			return true;
		}
	}

	private boolean isBlocking(float _x, float _y, float xa, float ya) { 
		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16))
			return false;

		boolean blocking = scene.level.isBlocking(x, y, xa, ya);

		byte block = scene.level.getBlock(x, y);

		if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0 &&
				!((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_LOWER) > 0))	//CHECK: ALL pickupables result in energy increase!? thats not good.
		{
			this.increaseEnergy(LevelScene.ENERGY_INCREASE);

			//addEnergy();

			scene.level.setBlock(x, y, (byte) 0);
			for (int xx = 0; xx < 2; xx++)
				for (int yy = 0; yy < 2; yy++)
					scene.addSprite(new Sparkle(x * 16 + xx * 8 + (int) (Math.random() * 8), y * 16 + yy * 8 + (int) (Math.random() * 8), 0, 0, 0, 2, 5));
		}


		// GATE-MECHANISM
		// Only Tiles on Mapsheet with blocklower + special Combination
		else if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_LOWER) > 0 && 
				(Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_SPECIAL) > 0 &&
				!((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_UPPER) > 0))
		{

			// Door left to right marked as pickupable
						if((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_PICKUPABLE) > 0){
							// left to right
							if(xa > 1) {
								scene.level.setBlock(x, y, (byte) 214);
								scene.level.setBlock(x, y- 1, (byte) 198);
							}
						}
			
			// von right to left
				else if(xa < -1){
				scene.level.setBlock(x, y, (byte) 213);
				scene.level.setBlock(x, y- 1, (byte) 197);

			}

			
		}


		// Block from above destruction
		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_LOWER) > 0 && 
				(Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_ANIMATED) > 0 &&
				(Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BLOCK_UPPER) > 0)
		{
			if(ya > 0 && canDestroyFromAbove){
				scene.level.setBlock(x, y, (byte) 0);
				for (int xx = 0; xx < 2; xx++)
					for (int yy = 0; yy < 2; yy++)
						scene.addSprite(new Particle(x * LevelScene.DISCRETE_CELL_SIZE + xx * 8
								+ 4, y * LevelScene.DISCRETE_CELL_SIZE + yy * 8 + 4,
								(xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));

				Sounds.play(Type.BLOCK_DESTRUCTION);
			}
		}



		if (blocking && ya < 0) 
		{
			scene.bump(x, y, large, this);
		}

		return blocking;
	}


	/**
	 * Sets the carry flags for players according to their positions
	 */
	@Override
	public void collideCheck(Sprite targetSprite) 
	{
		float xTolerance=width*2+2;
		float yTolerance=height/2;

		// if sprite is a player: mount, drop, unmount, mount could be learned.. 
		if (targetSprite.getType().isPlayer()) 
		{							
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
				this.setCarriedBy(targetSprite);

				return;
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
				this.setCarriedBy(null);		
				return;
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
					((Player)targetSprite).jumpTime == 0
					)
			{
				this.setCarries(targetSprite);

				return;
			}

			//I DROPPED SOMEONE
			xTriggerPositionOfTarget=x;
			yTriggerPositionOfTarget=y-height;			
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
				setCarries(null);
				return;
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
		//		return null;
	}

	//	public void collideCheck() {
	//
	//		for(Player player : scene.getPlayers())
	//		{
	//			if(player == this) {
	//				continue;
	//			}
	//			
	//			// this check whether this player "collides" with another from above and sets carriedBy flag
	//			float xPlayerD = player.x - x;
	//			float yPlayerD = player.y - y;
	//			if
	//			(
	//					(xPlayerD > -width * 2 - 4 && xPlayerD < width * 2 + 4) && 
	//					(yPlayerD > 0 && Math.abs(yPlayerD) < player.height + 0.5 && Math.abs(yPlayerD) > player.height - 0.5) 
	//			) 
	//			{
	//				// this can be carried by another player, only if he is not carried by a player already
	//				if(this.getCarriedBy() == null)
	//				{
	//					
	//					// mount
	//					this.setCarriedBy(player);
	//				}
	//			}
	//			else if(this.getCarriedBy() == player)
	//			{
	//				
	//				// unmmount
	//				this.setCarriedBy(null);
	//			}
	//				
	//			// this check whether another player "collides" with this player from above and sets carries flag
	//			if
	//			( 
	//					(xPlayerD > -width * 2 - 4 && xPlayerD < width * 2 + 4) &&
	//					(yPlayerD < 0 && Math.abs(yPlayerD) < player.height + 0.5 && Math.abs(yPlayerD) > player.height - 0.5)
	//			)
	//			{
	//				// this check whether another player "collides" with this player from above and sets carries flag
	//				if(this.getCarries() == null)
	//				{
	//					
	//					// carry
	//					this.setCarries(player);
	//				}
	//			}
	//			else if(getCarries() == player)
	//			{
	//				
	//				// drop
	//				setCarries(null);
	//			}
	//		}
	//	}

	/*
	 * sets the position of another player that will be carried
	 */
	private void carryOtherPlayer() 
	{
		// all Players in the scene could be carried
		for(Player player : scene.getPlayers())
		{
			// but the player can't carry himself
			if(player.getType() == getType()) 
			{
				continue;
			}

			float xPlayerD = player.x - this.x;
			float yPlayerD = player.y - this.y;

			if (!isBlocking(x, y-(2*height), 0, 0))
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
	public void dropOtherPlayer()
	{
		if (!isBlocking(x + (facing * width), y, 0, 0))
		{
			this.getCarries().x = x + (facing * width);
			this.getCarries().y = y;
		}
	}


	public void stomp(Enemy enemy) {
		if (/*deathTime > 0 || */scene.paused)
			return;

		float targetY = enemy.y - enemy.height / 2;
		move(0, targetY - y);

		xJumpSpeed = 0;
		yJumpSpeed = -1.9f*individualYJumpSpeedMultiplier;
		jumpTime = 8;
		ya = jumpTime * yJumpSpeed;
		onGround = false;
		sliding = false;
		invulnerableTime = 1;
		// TODO: welcher Effect??? Enemy undifferenziert
	}



	public void stomp(Grumpy grumpy) {
		if (/*deathTime > 0 || */scene.paused)
			return;

		if (keys[KEY_SPEED] && grumpy.facing == 0) {
			this.setCarries(grumpy);
			grumpy.setCarriedBy(this);
			/**
			 * TODO Turtle Grumpy aufgenommen = Destruction?
			 */
			//this.notifyObservers("ObjectDestruction");	//!??!
		} else {
			float targetY = grumpy.y - Grumpy.height / 2;
			move(0, targetY - y);

			xJumpSpeed = 0;
			yJumpSpeed = -1.9f*individualYJumpSpeedMultiplier;
			jumpTime = 8;
			ya = jumpTime * yJumpSpeed;
			onGround = false;
			sliding = false;
			invulnerableTime = 1;
		}
	}

	public void getHurt() {
		if (/*deathTime > 0 || */scene.paused || isPlayerInvulnerable)
			return;

		if (invulnerableTime > 0)
			return;
		
		
		
		if (healthLevel > 0) 
		{
			healthLevel--;
			scene.paused = false;
			powerUpTime = -3 * FractionalPowerUpTime;
			if (healthLevel > 3) 
			{
				this.setLarge(true, true);
				//this.notifyObservers("MarioHealthChange");	//!??!
			} else if(healthLevel > 0) {
				this.setLarge(true, false);
			}
			else 
			{
				this.setLarge(false, false);
				//this.notifyObservers("MarioHealthChange");	//!??!
			}
			invulnerableTime = 0;
		} 
		else 
		{
			die();
			// setLarge(true, true);
		}
	}

	private void win() {
		//TODO implement start gui restart
		ReleaseGame.controller.gameFinished();
		System.out.println("won game");
		/*
		 * Knowledge is saved in file only when level succeeded
		 */
		for (PlayerVisualizationContainer play : PlayHook.gameWorld.getPlayerContainers())
		{
			System.out.println("Saving Brain");
			((CAEAgent)play.agent).getBrain().saveToDisk(UserDir.getUserFile(((CAEAgent)play.agent).getBrain().KNOWLEDGE_FILE_PATH));	
		}

		StartGuiModel.storeLevelSolved(PlayRelease.getCurrentLevel());

		// new: Level finished if level 4 is beaten, as level 5 is epilog
		GameEndHandler.stopWithoutEvaluation(PlayRelease.getCurrentLevel().equals(Settings.getLevelBeforeEpilog()) ? ExitCode.GAME_SOLVED : ExitCode.LEVEL_SOLVED);
		// old: Game finished if last Level beaten
		//GameEndHandler.stopWithoutEvaluation(PlayRelease.getCurrentLevel().equals(Settings.getLastLevelName()) ? ExitCode.GAME_SOLVED : ExitCode.LEVEL_SOLVED);
		//		resetStatic(1);	//workaround...
	}

	public void die() 
	{
		resetStatic(1);		
	}

	public void getFlower() 
	{
		if (/*deathTime > 0 || */scene.paused)
			return;

		if (!bulb) {
			scene.paused = false;
			powerUpTime = 3 * FractionalPowerUpTime;
			this.setLarge(true, true);
			this.setHealthLevel(2*healthLevelScaling);
			//this.notifyObservers("MarioHealthChange");	//!??!
		} else {
//			addEnergy();
			//this.notifyObservers("EnergyIncrease");		//!??!
		}
		++gainedFlowers;
	}

	public void getWrench() {
		if (/*deathTime > 0 || */scene.paused)
			return;

		if (!large) {
			scene.paused = false;
			powerUpTime = 3 * FractionalPowerUpTime;
			this.setLarge(true, false);
			this.setHealthLevel(healthLevelScaling);
			//this.notifyObservers("MarioHealthChange");	//!??!
		} else {
//			addEnergy();
			//this.notifyObservers("EnergyIncrease");		//!!??!
		}
		++gainedWrenchs;
	}

	public void kick(Grumpy grumpy) {
		// if (deathTime > 0 || world.paused) return;

		if (keys[KEY_SPEED]) {
			//this.setCarries(grumpy);
			//grumpy.setCarriedBy(this);
		} else {
			invulnerableTime = 1;
		}
	}

	public void stomp(BulletWilly willy) {
		if (/*deathTime > 0 || */scene.paused)
			return;

		float targetY = willy.y - willy.height / 2;
		move(0, targetY - y);

		xJumpSpeed = 0;
		yJumpSpeed = -1.9f*individualYJumpSpeedMultiplier;
		jumpTime = 8;
		ya = jumpTime * yJumpSpeed;
		onGround = false;
		sliding = false;
		invulnerableTime = 1;

		//this.notifyObservers("ObjectDestruction");	//!?!?
	}

	public byte getKeyMask() {
		int mask = 0;
		for (int i = 0; i < 7; i++) {
			if (keys[i])
				mask |= (1 << i);
		}
		return (byte) mask;
	}

	public void setKeys(boolean[] mask) {
		for (int i = 0; i < keys.length && i < mask.length; i++) {
			keys[i] = mask[i];
		}
	}

	public static void get1Up() {
		lives++;
	}

	public void addEnergy() {
		energys++;
	}

	public void setStatus(int setStatus)
	{
		status  = setStatus;
	}

	public int getStatus() 
	{
		return status;
	}

	public boolean wasOnGround() {
		return wasOnGround;
	}

	public boolean isMayJump() {
		return mayJump;
	}

	public int getEnergys() {
		return energys;
	}

	public boolean isSliding() {
		return sliding;
	}

	//	public boolean isMayJump() {
	//		return mayJump;
	//	}
	//
	public boolean isDucking() {
		return ducking;
	}

	public int getJumpTime() {
		return jumpTime;
	}

	public int getInvulnerableTime() {
		return invulnerableTime;
	}

	public float getxJumpSpeed() {
		return xJumpSpeed;
	}

	public float getyJumpSpeed() {
		return yJumpSpeed;
	}

	public float getIndividualYJumpSpeedMultiplier() {
		return individualYJumpSpeedMultiplier;
	}

	@Override
	public HashMap<Field, Object> getAttributeMap() {
		HashMap<Field, Object> map = super.getAttributeMap();
		Field[] fields = getClass().getDeclaredFields();
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
		return map;
	}

	@Override
	public boolean isFacingDefined() {
		return true;
	}

	@Override
	public int getFacing() {
		return facing;
	}

	@Override
	public boolean isOnGroundDefined() {
		return true;
	}

	@Override
	public boolean isOnGround() {
		return onGround;
	}

	public void reachEndItem() {
		if(!evaluatedYet){
			this.reachedEndItem  = true;
			geh.evaluate(false,scene.getOwnLevelName());
			evaluatedYet = true;
		}
	}

	public void reachLevelEnd() {
		if(!reachedLevelEnd){
			scene.increasePlayersAtEnd();
			this.reachedLevelEnd = !reachedLevelEnd;
			boolean allPlayersAtLevelEnd = true;
			for(Player player : scene.getPlayers()){
				allPlayersAtLevelEnd &= player.reachedLevelEnd;			
			}
			if(allPlayersAtLevelEnd){
				win();
			}
			else{
				geh.evalLevelFinished();
			}
		}
	}

	public boolean reachedEndItem(){
		return reachedEndItem;
	}

	public boolean reachedLevelEnd(){
		return reachedLevelEnd;
	}

	public static void deleteStaticAttributes() {
		Sprite.deleteStaticAttributes();
		spriteContext = null;
		lives = 0;
		gainedWrenchs = 0;
		gainedFlowers = 0;
		isPlayerInvulnerable = false;
	}

	public static void resetStaticAttributes(){
		Sprite.resetStaticAttributes();
		spriteContext = null;
		lives = 1024;
		gainedWrenchs = 0;
		gainedFlowers = 0;
		isPlayerInvulnerable = false;
	}

	public GameEndHandler getGEH() {
		return geh;		
	}

	public int[][] getExploredMap() {
		return exploredMap;
	}

	public void setExploredMap(int[][] exploredMap) {
		this.exploredMap = exploredMap;
	}

	public void setEnergys(int energys) {
		this.energys = energys;
	}

	public int getHealthLevel() {
		return healthLevel;
	}

	public void setHealthLevel(int healthLevel) {
		this.healthLevel = healthLevel;
	}

}
