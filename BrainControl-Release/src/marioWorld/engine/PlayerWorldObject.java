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
package marioWorld.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;


/*
 * Edit 06.06.2016
 * 
 * Names changed! 
 * 
 * 
 * VIRUS = KOOPA
 * ENERGY = COIN
 * FIREFLOWER = BULBFLOWER (might be changed to just bulb)
 * MUSHROOM = WRENCH
 * GOALSTAR = CURSOR
 * GOOMBA = LUKO
 * BULLETBILL = BULLETWILLY
 * SPIKY = SHALLY
 */



import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.Generalizer;
//import marioAI.brain.LearningHelper;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.Goal;
import marioAI.movement.GoalDeque;
import marioAI.movement.ReachabilityNode;
import marioAI.run.PlayHook;
import marioWorld.agents.Agent;
//import marioWorld.agents.human.HumanInfoKeyboardAgent;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeCoarse;
import marioWorld.engine.level.Level;
import marioWorld.engine.level.SpriteTemplate;
import marioWorld.engine.sprites.EnergyAnim;
import marioWorld.engine.sprites.Enemy;
import marioWorld.engine.sprites.BulbFlower;
import marioWorld.engine.sprites.Fireball;
import marioWorld.engine.sprites.FlowerEnemy;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Wrench;
import marioWorld.engine.sprites.EndItem;
import marioWorld.engine.sprites.Grumpy;
import marioWorld.engine.sprites.ISprite;
import marioWorld.engine.sprites.Sprite;

/**
 * Objects in the world which Mario can interact with
 * 
 * @author Ylva
 * 
 */
public enum PlayerWorldObject {
	// sprites, !isStatic, zLevel 0
	PLAYER(-31, false, BlockingType.TOP), //
	//LUKO(2, false, BlockingType.NONE), //
	//LUKO_WINGED(3, false, BlockingType.NONE), //
	GREEN_VIRUS(2, false, BlockingType.NONE), //
	GREEN_VIRUS_WINGED(3, false, BlockingType.NONE), //
	RED_VIRUS(4, false, BlockingType.NONE), //
	RED_VIRUS_WINGED(5, false, BlockingType.NONE), //
	//GREEN_VIRUS(6, false, BlockingType.NONE), //
	//GREEN_VIRUS_WINGED(7, false, BlockingType.NONE), //
	GRUMPY(6, false, BlockingType.NONE), //
	GRUMPY_WINGED(7, false, BlockingType.NONE), //
	BULLET_WILLY(8, false, BlockingType.NONE), //
	SHALLY(9, false, BlockingType.NONE), //
	SHALLY_WINGED(10, false, BlockingType.NONE), //
	PIRHANA_PLANT(12, false, BlockingType.NONE), //
	GRUMPY_STILL(13, false, BlockingType.NONE), //
	GRUMPY_MOVING(13, false, BlockingType.NONE), //
	WRENCH(14, false, BlockingType.NONE), //
	FLOWER_ENEMY(12, false, BlockingType.NONE), //
	BULB_FLOWER(15, false, BlockingType.NONE), //
	PARTICLE(21, false, BlockingType.NONE), //
	SPARCLE(22, false, BlockingType.NONE), //
	ENERGY_ANIM(20, false, BlockingType.NONE), //
	FIREBALL(25, false, BlockingType.NONE), //
	UNDEFINED(-42, false, BlockingType.NONE), //
	END_ITEM( 1, false, BlockingType.NONE),
	
	/*
	 * Player to Player interaction
	 */	
	BRUCE(-1, false, BlockingType.TOP),
	CLARK(-2, false, BlockingType.TOP),
	PETER(-3, false, BlockingType.TOP),
	JAY(-4, false, BlockingType.TOP),
	// isStatic
	// zLevel 1

	// Define properties of different tiles with tile ID being their order in
	// mapeditor (top half 0 to 127, bottom half -128 to -1)
	// GROUND tiles are not passable though looking like non-ground tiles
	// SECOND tiles are identical in appearance and blocking behavior to their
	// original tiles (reason for occurrence unknown)

	TOP_LEFT_DIRT_BORDER_GROUND(-128, true, BlockingType.FULL), //
	TOP_DIRT_BORDER_GROUND(-127, true, BlockingType.FULL), //
	TOP_RIGHT_DIRT_BORDER_GROUND(-126, true, BlockingType.FULL), //
	INNER_BOTTOM_RIGHT_DIRT_CORNER_GROUND(-125, true, BlockingType.FULL), //

	TOP_LEFT_DIRT_BORDER(-124, true, BlockingType.TOP), //
	TOP_DIRT_BORDER(-123, true, BlockingType.TOP), //
	TOP_RIGHT_DIRT_BORDER(-122, true, BlockingType.TOP), //
	INNER_BOTTOM_RIGHT_DIRT_CORNER(-121, true, BlockingType.FULL), //

	TOP_LEFT_BRICK_BORDER(-120, true, BlockingType.FULL), //
	TOP_BRICK_BORDER(-119, true, BlockingType.FULL), //
	TOP_RIGHT_BRICK_BORDER(-118, true, BlockingType.FULL), //
	INNER_BOTTOM_RIGHT_BRICK_CORNER(-117, true, BlockingType.NONE), //

	TOP_LEFT_ROCK_BORDER(-116, true, BlockingType.FULL), //
	TOP_ROCK_BORDER(-115, true, BlockingType.FULL), //
	TOP_RIGHT_ROCK_BORDER(-114, true, BlockingType.FULL), //
	INNER_BOTTOM_RIGHT_ROCK_CORNER(-113, true, BlockingType.NONE), //

	LEFT_DIRT_BORDER_GROUND(-112, true, BlockingType.FULL), //
	CENTER_DIRT_GROUND(-111, true, BlockingType.FULL), //
	RIGHT_DIRT_BORDER_GROUND(-110, true, BlockingType.FULL), //
	INNER_BOTTOM_LEFT_DIRT_CORNER_GROUND(-109, true, BlockingType.FULL), //

	LEFT_DIRT_BORDER(-108, true, BlockingType.NONE), //
	CENTER_DIRT(-107, true, BlockingType.NONE), //
	RIGHT_DIRT_BORDER(-106, true, BlockingType.NONE), //
	INNER_BOTTOM_LEFT_DIRT_CORNER(-105, true, BlockingType.FULL), //

	LEFT_BRICK_BORDER(-104, true, BlockingType.FULL), //
	CENTER_BRICK(-103, true, BlockingType.FULL), //
	RIGHT_BRICK_BORDER(-102, true, BlockingType.FULL), //
	INNER_BOTTOM_LEFT_BRICK_CORNER(-101, true, BlockingType.NONE), //

	LEFT_ROCK_BORDER(-100, true, BlockingType.FULL), //
	CENTER_ROCK(-99, true, BlockingType.FULL), //
	RIGHT_ROCK_BORDER(-98, true, BlockingType.FULL), //
	INNER_BOTTOM_LEFT_ROCK_CORNER(-97, true, BlockingType.NONE), //

	BOTTOM_LEFT_DIRT_BORDER_GROUND(-96, true, BlockingType.FULL), //
	BOTTOM_DIRT_BORDER_GROUND(-95, true, BlockingType.FULL), //
	BOTTOM_RIGHT_DIRT_BORDER_GROUND(-94, true, BlockingType.FULL), //
	INNER_TOP_LEFT_DIRT_CORNER_GROUND(-93, true, BlockingType.FULL), //

	BOTTOM_LEFT_DIRT_BORDER(-92, true, BlockingType.NONE), //
	BOTTOM_DIRT_BORDER(-91, true, BlockingType.NONE), //
	BOTTOM_RIGHT_DIRT_BORDER(-90, true, BlockingType.NONE), //
	INNER_TOP_LEFT_DIRT_CORNER(-89, true, BlockingType.FULL), //

	BOTTOM_LEFT_BRICK_BORDER(-88, true, BlockingType.FULL), //
	BOTTOM_BRICK_BORDER(-87, true, BlockingType.FULL), //
	BOTTOM_RIGHT_BRICK_BORDER(-86, true, BlockingType.FULL), //
	INNER_TOP_LEFT_BRICK_CORNER(-85, true, BlockingType.NONE), //

	BOTTOM_LEFT_ROCK_BORDER(-84, true, BlockingType.FULL), //
	BOTTOM_ROCK_BORDER(-83, true, BlockingType.FULL), //
	BOTTOM_RIGHT_ROCK_BORDER(-82, true, BlockingType.FULL), //
	INNER_TOP_LEFT_ROCK_CORNER(-81, true, BlockingType.NONE), //

	SECOND_TOP_LEFT_DIRT_BORDER_GROUND(-80, true, BlockingType.FULL), //
	SECOND_TOP_DIRT_BORDER_GROUND(-79, true, BlockingType.FULL), //
	SECOND_TOP_RIGHT_DIRT_BORDER_GROUND(-78, true, BlockingType.FULL), //
	SECOND_INNER_BOTTOM_RIGHT_DIRT_CORNER_GROUND(-77, true, BlockingType.FULL), //

	SECOND_TOP_LEFT_DIRT_BORDER(-76, true, BlockingType.TOP), //
	SECOND_TOP_DIRT_BORDER(-75, true, BlockingType.TOP), //
	SECOND_TOP_RIGHT_DIRT_BORDER(-74, true, BlockingType.TOP), //
	SECOND_INNER_BOTTOM_RIGHT_DIRT_CORNER(-73, true, BlockingType.FULL), //

	// IDs -72 to -70 are blank
	SECOND_INNER_BOTTOM_RIGHT_BRICK_CORNER(-69, true, BlockingType.NONE), //
	// IDs -68 to -666 are blank
	SECOND_INNER_BOTTOM_RIGHT_ROCK_CORNER(-65, true, BlockingType.NONE), //
	
	// IDs -64 to -62 are blank
	TOP_LEFT_DOOR(-61, true, BlockingType.FULL), //
	TOP_RIGHT_DOOR(-60, true, BlockingType.FULL), //
	TOP_LEFT_DOOR_OPEN(-59, true, BlockingType.NONE), //
	TOP_RIGHT_DOOR_OPEN(-58, true, BlockingType.NONE), //
	// IDs -57 to -46 are blank
	
	BOTTOM_LEFT_DOOR(-45, true, BlockingType.FULL), //
	BOTTOM_RIGHT_DOOR(-44, true, BlockingType.FULL), //
	BOTTOM_LEFT_DOOR_OPEN(-43, true, BlockingType.NONE), //
	BOTTOM_RIGHT_DOOR_OPEN(-42, true, BlockingType.NONE), //
	// IDs -41 to -33 are blank
	TOP_LEFT_WRENCH_HEAD(-32, true, BlockingType.TOP), //
	TOP_CENTER_WRENCH_HEAD(-31, true, BlockingType.TOP), //
	TOP_RIGHT_WRENCH_HEAD(-30, true, BlockingType.TOP), //
	// IDs -29 to -17 are blank
	LEFT_WRENCH_STEM(-16, true, BlockingType.NONE), //
	CENTER_WRENCH_STEM(-16, true, BlockingType.NONE), //
	RIGHT_WRENCH_STEM(-16, true, BlockingType.NONE), //

	// IDs -13 to -1 are blank
	// relics, function unknown
	// SOFT_OBSTACLE(-11, true, false, BlockingType.NONE), //
	// HARD_OBSTACLE(-10,true,false,BlockingType.FULL), //

	NONE(0, true, BlockingType.NONE), //
	// IDs 1 to 3 are sprites
	IRON_BLOCK1(4, true, BlockingType.FULL), //
	IRON_BLOCK2(5, true, BlockingType.FULL), //
	IRON_BLOCK3(6, true, BlockingType.FULL), //
	IRON_BLOCK4(7, true, BlockingType.FULL), //
	// blank ID
	STONE_BLOCK(9, true, BlockingType.FULL), //
	BIG_TUBE_TOP_LEFT(10, true, BlockingType.FULL), //
	BIG_TUBE_TOP_RIGHT(11, true, BlockingType.FULL), //
	BROWN_STONE_BLOCK(12, true, BlockingType.FULL), //
	// blank ID
	CANNON_HEAD(14, true, BlockingType.FULL), //
	// blank ID
	SIMPLE_BLOCK1(16, true, BlockingType.FULL), //
	SIMPLE_BLOCK2(17, true, BlockingType.FULL), //
	SIMPLE_BLOCK3(18, true, BlockingType.FULL), //
	SIMPLE_BLOCK4(19, true, BlockingType.FULL), //
	QUESTIONMARK_BLOCK1(20, true, BlockingType.FULL), //
	QUESTIONMARK_ENERGY_BLOCK(21, true, BlockingType.FULL), //
	QUESTIONMARK_FLOWER_BLOCK(22, true, BlockingType.FULL), //
	QUESTIONMARK_BLOCK2(23, true, BlockingType.FULL), //
	SMALL_TUBE_TOP(24, true, BlockingType.FULL), //
	UPPER_TREE_LOG(25, true, BlockingType.NONE), //
	BIG_TUBE_LEFT_PIPE(26, true, BlockingType.FULL), //
	BIG_TUBE_RIGHT_PIPE(27, true, BlockingType.FULL), //
	BLUE_BLOCK(28, true, BlockingType.FULL), //
	// blank ID
	CANNON_SKULL_BLOCK(30, true, BlockingType.FULL), //
	// blank ID
	ENERGY_FRONT(32, true, BlockingType.NONE), //
	ENERGY_SIDE1(33, true, BlockingType.NONE), //
	ENERGY_SIDE2(34, true, BlockingType.NONE), //
	ENERGY_SIDE3(35, true, BlockingType.NONE), //
	PETERS_BLOCK1(36, true, BlockingType.FULL), //
	PETERS_BLOCK2(37, true, BlockingType.FULL), //
	PETERS_BLOCK3(38, true, BlockingType.FULL), //
	PETERS_BLOCK4(39, true, BlockingType.FULL), //
	SMALL_TUBE_PIPE(40, true, BlockingType.FULL), //
	TREE_LOG_CENTER_WITH_LOG_BELOW(41, true, BlockingType.FULL), //
	// IDs 42 to 45 blank
	CANNON_BASE(46, true, BlockingType.FULL), //
	// IDs 47 to 55 blank
	SMALL_TUBE_BOTTOM(56, true, BlockingType.FULL), //
	TREE_LOG_CENTER_WITHOUT_LOG_BELOW(57, true, BlockingType.FULL), //
	// IDs 58 to 59 blank
	WHITE_PLATFORM_LEFT(60, true, BlockingType.NONE), //
	WHITE_PLATFORM_CENTER(61, true, BlockingType.NONE), //
	// IDs 62 to 66 blank
	ARROW_SIGN_TOP_LEFT(67, true, BlockingType.NONE), //
	ARROW_SIGN_TOP_RIGHT(68, true, BlockingType.NONE), //
	// ID 69 blank
	BIG_BUSH_TOP_LEFT(70, true, BlockingType.NONE), //
	BIG_BUSH_TOP_RIGHT(71, true, BlockingType.NONE), //
	// ID 72 blank
	BIG_BUSH_CLUSTER_TOP_LEFT(73, true, BlockingType.NONE), //
	BIG_BUSH_CLUSTER_TOP_CENTER(74, true, BlockingType.NONE), //
	BIG_BUSH_CLUSTER_TOP_RIGHT(75, true, BlockingType.NONE), //
	WHITE_POLE_TOP(76, true, BlockingType.NONE), //
	BLUE_POLE_TOP(77, true, BlockingType.NONE), //
	// IDs 78 to 79 blank
	WILD_GRASS_LEFT(80, true, BlockingType.NONE), //
	WILD_GRASS_CENTER(81, true, BlockingType.NONE), //
	WILD_GRASS_RIGHT(82, true, BlockingType.NONE), //
	ARROW_SIGN_BOTTOM_LEFT(83, true, BlockingType.NONE), //
	ARROW_SIGN_BOTTOM_RIGHT(84, true, BlockingType.NONE), //
	// ID 85 blank
	BIG_BUSH_MIDDLE_LEFT(86, true, BlockingType.NONE), //
	BIG_BUSH_MIDDLE_RIGHT(87, true, BlockingType.NONE), //
	// ID 88 blank
	BIG_BUSH_CLUSTER_BOTTOM_LEFT(89, true, BlockingType.NONE), //
	BIG_BUSH_CLUSTER_BOTTOM_CENTER(90, true, BlockingType.NONE), //
	BIG_BUSH_CLUSTER_BOTTOM_RIGHT(91, true, BlockingType.NONE), //
	WHITE_POLE_BLOCK(92, true, BlockingType.NONE), //
	BLUE_POLE_BLOCK(93, true, BlockingType.NONE), //
	// IDS 94 to 95 blank
	GRASS_LEFT(96, true, BlockingType.NONE), //
	GRASS_CENTER(97, true, BlockingType.NONE), //
	GRASS_RIGHT(98, true, BlockingType.NONE), //
	// IDS 99 to 101 blank
	BIG_BUSH_BOTTOM_LEFT(102, true, BlockingType.NONE), //
	BIG_BUSH_BOTTOM_RIGHT(103, true, BlockingType.NONE), //
	BUSH_COMPONENT1(104, true, BlockingType.NONE), //
	BUSH_COMPONENT2(105, true, BlockingType.NONE), //
	// IDS 106 to 117 blank
	SMALL_BUSH_LEFT(118, true, BlockingType.NONE), //
	SMALL_BUSH_RIGHT(119, true, BlockingType.NONE), //
	BUSH_COMPONENT3(120, true, BlockingType.NONE), //
	BUSH_COMPONENT4(121, true, BlockingType.NONE),//
	// IDs 122 to 127 blank
	SELECTED_TILE(122, true, BlockingType.NONE);//

	/** ID of the Object. This is used only by the engine. */
	private final int objectID;
	/** If the object can not move. */
	public final boolean isStatic;
	/** blocks path: if true, mario can not pass this object */
	public final BlockingType blocksPath;

	//its more effective to save a static value list:
	static private PlayerWorldObject[] objects = PlayerWorldObject.values();
	
	private PlayerWorldObject(int objectID, boolean isStatic, BlockingType blocksPath) {
		this.objectID = objectID;
		this.isStatic = isStatic;
		this.blocksPath = blocksPath;
	}

	public int getWidthForCollisionDetection() {
		switch (this) {
		case BULLET_WILLY:
			return 6;
		case FLOWER_ENEMY:
			return 2;
		case BULB_FLOWER:
			return 6;
		case GRUMPY_MOVING:
		case GRUMPY_STILL:
			return 6;
			case CLARK:
			case BRUCE:
			case PETER:
			case JAY:
				return 4;
			// /**
			// * This is double the width provided by the engine. This should be
			// * o.k., since we think the "width" within the engine is only half
			// the
			// * actual width anyways.
			// */
			// return 8;
		default:
			return 4;
		}
	}

	public int getHeight() {
		switch (this) {
		case FIREBALL:
			return 8;
		case GRUMPY_STILL:
		case GRUMPY_MOVING:
		case BULLET_WILLY:
		case BULB_FLOWER:
		case FLOWER_ENEMY:
		case WRENCH:
		//case LUKO:
		//case LUKO_WINGED:
		case GREEN_VIRUS:
		case GREEN_VIRUS_WINGED:
			return 12;
		/* case RED_KOOPA:
		case RED_KOOPA_WINGED:
		case GREEN_KOOPA:
		case GREEN_KOOPA_WINGED:
		*/
		case RED_VIRUS:
		case RED_VIRUS_WINGED:
		//case GREEN_VIRUS:
		//case GREEN_VIRUS_WINGED:
		case GRUMPY:
		case GRUMPY_WINGED:
			return 24;
		case CLARK:
		case PETER:
		case BRUCE:
		case JAY:
		case PLAYER:
			new Throwable("Please use Player.getHeight() instead of PlayerWorldObject.Player.getHeight()").printStackTrace();
			return 24;
		default:
			new Throwable("Height not implemented for " + name()).printStackTrace();
			return 0;
		}
	}

	/**
	 * No idea why the engine handles this differently from getWidthForCollision for Grumpys. Probably a bug. This method needs to be used when sprites are reset
	 * to a new position after collision.
	 */
	public int getWidthForBumping() {
		switch (this) {
		case GRUMPY_MOVING:
		case GRUMPY_STILL:
			return 4;
		default:
			return getWidthForCollisionDetection();
		}
	}

	public boolean isWinged() {
		switch (this) {
		//case LUKO_WINGED:
		case GREEN_VIRUS_WINGED:
		//case GREEN_KOOPA_WINGED:
		//case RED_KOOPA_WINGED:
		//case GREEN_VIRUS_WINGED:
		case GRUMPY_WINGED:
		case RED_VIRUS_WINGED:
		case SHALLY_WINGED:
			return true;
		default:
			return false;
		}
	}

	/** final set of enemies */
	public static final EnumSet<PlayerWorldObject> ENEMIES;
	static 
	{		
		ArrayList<PlayerWorldObject> helpList = new ArrayList<PlayerWorldObject>();
		/*
		helpList.add(PlayerWorldObject.RED_KOOPA);
		helpList.add(PlayerWorldObject.RED_KOOPA_WINGED);
		helpList.add(PlayerWorldObject.GREEN_KOOPA);
		helpList.add(PlayerWorldObject.GREEN_KOOPA_WINGED);
		*/
		helpList.add(PlayerWorldObject.RED_VIRUS);
		helpList.add(PlayerWorldObject.RED_VIRUS_WINGED);
		//helpList.add(PlayerWorldObject.GREEN_VIRUS);
		//helpList.add(PlayerWorldObject.GREEN_VIRUS_WINGED);
		helpList.add(PlayerWorldObject.GRUMPY);
		helpList.add(PlayerWorldObject.GRUMPY_WINGED);
		//helpList.add(PlayerWorldObject.LUKO);
		//helpList.add(PlayerWorldObject.LUKO_WINGED);
		helpList.add(PlayerWorldObject.GREEN_VIRUS);
		helpList.add(PlayerWorldObject.GREEN_VIRUS_WINGED);
		helpList.add(PlayerWorldObject.SHALLY);
		helpList.add(PlayerWorldObject.SHALLY_WINGED);
		helpList.add(PlayerWorldObject.BULB_FLOWER);
		helpList.add(PlayerWorldObject.BULLET_WILLY);
		ENEMIES = EnumSet.copyOf(helpList);
	}
	
	public static final EnumSet<PlayerWorldObject> PLAYERS;
	static 
	{		
		ArrayList<PlayerWorldObject> helpList = new ArrayList<PlayerWorldObject>();
		helpList.add(PlayerWorldObject.CLARK);
		helpList.add(PlayerWorldObject.BRUCE);
		helpList.add(PlayerWorldObject.PETER);
		helpList.add(PlayerWorldObject.JAY);
		PLAYERS = EnumSet.copyOf(helpList);
	}
	
	
	//suggested new function for determining whether an object is player 
	public boolean isPlayer(){
		switch(this){
		case CLARK:
		case BRUCE:
		case PETER:
		case   JAY:
		//safe solution... still includes PlayerWorldObject.PLAYER
		case PLAYER:
			return true;
		default:
			return false;
		}
	}
	
	//function for determining whether the other Player is not the same Player like this Player
	public boolean isAnotherPlayer(PlayerWorldObject otherplayer)
	{
		if(otherplayer.isPlayer()){
			if(this != otherplayer){
				return true;
			}
		}
		return false;
	}
	
	public boolean isRelevantToLearning() 
	{
		//Fabian: removed non-interactable stuff from the list to speed up schematic planning when there are lots of them around. 
		
		switch (this) {
		//case LUKO:
		case GREEN_VIRUS:
		case BULLET_WILLY:
		case WRENCH:
		case BULB_FLOWER:		
		//case STONE_BLOCK:
		case SIMPLE_BLOCK1:
		case SIMPLE_BLOCK2:
		case SIMPLE_BLOCK3:
		case SIMPLE_BLOCK4:
		case QUESTIONMARK_BLOCK1:
		case QUESTIONMARK_ENERGY_BLOCK:
		case QUESTIONMARK_FLOWER_BLOCK:
		case QUESTIONMARK_BLOCK2:
		case ENERGY_FRONT:
		case ENERGY_SIDE1:
		case ENERGY_SIDE2:
		case ENERGY_SIDE3:
		//case IRON_BLOCK1:
		//case IRON_BLOCK2:
		case BOTTOM_LEFT_DOOR: 
		case BOTTOM_RIGHT_DOOR:
		case PETERS_BLOCK1:
		case PETERS_BLOCK2:
		case PETERS_BLOCK3:
		case PETERS_BLOCK4:
		//TODO: learning collisions with players does not work when the following is enabled. probably because they are created manually by the collideCheck function during simulation. 
//		case CLARK:
//		case PETER:
//		case JAY:
//		case BRUCE:
		case PLAYER:
			return true;
		default:
//			System.out.println("LearningHelper: Check if " + obj.name() + " is relevant to learning.");
//			new Throwable("Unknown if " + obj.name() + " is relevant to learning. Please add this in LearningHelper if you use a level containing "
//					+ obj.name() + ".").printStackTrace();
			return false;
		}
	}
	

	/**
	 * Simplifies an element's ID to its superclass' ID
	 * 
	 * @param objectID
	 * @return int
	 */
	@Deprecated
	public static int simplifyElementID(int objectID) {
		switch (objectID) {

		case 16:
			return 16; // brick, simple, without any surprise.
		case 17:
			return 17; // brick with a hidden energy
		case 18:
			return 18; // brick with a hidden flower
			// return 16; // prevents cheating
		case 21:
			return 21; // question brick, contains energy
		case 22:
			return 22; // question brick, contains flower/wrench
			// return 21; // question brick, contains something
		case (-108):
		case (-107):
		case (-106):
		case (15): // Sparcle, irrelevant
			// case(34): // Energy, irrelevant for the current contest
			// return 0;
		case (-128):
		case (-127):
		case (-126):
		case (-125):
		case (-120):
		case (-119):
		case (-118):
		case (-117):
		case (-116):
		case (-115):
		case (-114):
		case (-113):
		case (-112):
		case (-111):
		case (-110):
		case (-109):
		case (-104):
		case (-103):
		case (-102):
		case (-101):
		case (-100):
		case (-99):
		case (-98):
		case (-97):
		case (-69):
		case (-65):
		case (-88):
		case (-87):
		case (-86):
		case (-85):
		case (-84):
		case (-83):
		case (-82):
		case (-81):
		case (4): // kicked hidden brick
		case (9):
			return -10;

		case (-124):
		case (-123):
		case (-122):
		case (-76):
		case (-74):
			return -11; // half-border, can jump through from bottom and can
		// stand on
		case (10):
		case (11):
		case (26):
		case (27): // flower pot
		case (14):
		case (30):
		case (46): // canon
			return 20; // angry flower pot or cannon
		}
		return objectID;

	}

	/**
	 * Returns the PlayerWorldObject; where necessary simplifies an element's ID to its superclass' ID before returning the (simplified) Object TODO Where is the
	 * simplification? - Simplification? What do you mean? This method transforms an id into an enum constant.
	 * 
	 * TODO: doing this conversion all the time is very ineffective! Memory killer...
	 * 
	 * @param objectID
	 * @param isStatic
	 * @param xa
	 *            necessary to distinguish between still and moving grumpys.
	 * @return PlayerWorldObject
	 */
	public static PlayerWorldObject getElement(byte objectID, ObjectType isStatic, int facing) 
	{				
		for (PlayerWorldObject object : objects) 	//brute force search -.- ...
		{
			if (object.objectID == objectID) 
			{
				if (objectID == GRUMPY_MOVING.objectID && !isStatic.key) 
				{
					return (facing == 0 ? GRUMPY_STILL : GRUMPY_MOVING);
				}
				// NONE is both static and non-static
				if (object == NONE || object.isStatic == isStatic.key) 
				{
					return object;
				}
				// distinguish between still (xa==0) and moving (xa!=0) grumpys.
				// Still grumpys are also non-static!
			}
		}
		
		//new Throwable("objectID " + objectID + " unknown in set of " + (isStatic.key ? "static" : "moving") + " objects!").printStackTrace();
		//return UNDEFINED;
		
		//if this object ID is unknown, it is probably one of the buggy "background" objects: the editor "features" like 50 different background blocks with different IDs...
		return PlayerWorldObject.NONE;		
	}
	
	public static PlayerWorldObject getElement(String element) 
	{		
		for (PlayerWorldObject object : objects) 
		{
			if (object.toString().equalsIgnoreCase(element)) 
			{
				return object;
			}
		}
		
		return PlayerWorldObject.NONE;		
	}	

	public static PlayerWorldObject getElement(Sprite sprite) {
		return getElement(sprite.kind, ObjectType.NON_STATIC, sprite.getFacing());
	}

	public Sprite createSprite(LevelScene levelScene, GlobalContinuous position, int facing, boolean isRedGrumpy) {
		GlobalCoarse spriteMapPos = position.toGlobalCoarse();
		switch (this) {
		/*
		case RED_KOOPA:
		case RED_KOOPA_WINGED:
		case GREEN_KOOPA:
		case GREEN_KOOPA_WINGED:
		*/
		case RED_VIRUS:
		case RED_VIRUS_WINGED:
		//case GREEN_VIRUS:
		//case GREEN_VIRUS_WINGED:
		case GRUMPY:
		case GRUMPY_WINGED:
		//case LUKO:
		//case LUKO_WINGED:
		case GREEN_VIRUS:
		case GREEN_VIRUS_WINGED:
		case SHALLY:
		case SHALLY_WINGED:
		case PIRHANA_PLANT:
		case BULLET_WILLY:
			/** Enemy uses a different number encoding than Sprite */
			//boolean isWinged = (this == RED_KOOPA_WINGED || this == GREEN_KOOPA_WINGED || this == LUKO_WINGED || this == SHALLY_WINGED);
			boolean isWinged = (this == RED_VIRUS_WINGED || this == GRUMPY_WINGED || this == GREEN_VIRUS_WINGED || this == SHALLY_WINGED);
			int enemyType = SpriteTemplate.translatePlayerWorldObjectToTemplateType(this);
			return new Enemy(levelScene, position, spriteMapPos, facing, enemyType, isWinged);
		case GRUMPY_STILL:
		case GRUMPY_MOVING:
			return new Grumpy(levelScene, position.x, position.y, isRedGrumpy ? 0 : 1);
		case WRENCH:
			return new Wrench(levelScene, (int) position.x, (int) position.y);
		case FLOWER_ENEMY:
			return new FlowerEnemy(levelScene, position, spriteMapPos);
		case BULB_FLOWER:
			return new BulbFlower(levelScene, (int) position.x, (int) position.y);
		case ENERGY_ANIM:
			return new EnergyAnim(spriteMapPos.x, spriteMapPos.y);
		case FIREBALL:
			return new Fireball(levelScene, position.x, position.y, facing);
		case END_ITEM:
			return new EndItem(levelScene,  position.x, position.y);
		case PARTICLE:
		case SPARCLE:
		case UNDEFINED:
			new Throwable("Can't create a sprite out of: " + this.name() + " This is only GUI-relevant.").printStackTrace();
			return null;
		default:
			if (this.isStatic) {
				new Throwable("Can't create a sprite out of a static object (" + this.name() + ")").printStackTrace();
			} else {
				new Throwable("Creating a sprite from " + this.name() + " not implemented.").printStackTrace();
			}
			return null;
		}
	}
	
	public SimulatedSprite createSimulatedSprite(LevelScene levelScene, GlobalContinuous position, int facing, boolean isRedGrumpy) 
	{
		if(!this.isStatic)
			return new SimulatedSprite(createSprite(levelScene, position, facing, isRedGrumpy));
		else
			return null;
	}	

	public void addStaticToMap(byte[][] map, GlobalCoarse position) {
		if (!isStatic) {
			new Throwable("Plase call with static object. " + this.name() + " is non-static.").printStackTrace();
			return;
		}
		map[position.x][position.y] = (byte) this.objectID;
	}

	public enum BlockingType {
		NONE, FULL, TOP, BOTTOM;
	}

	/**
	 * WARNING: if mario's GlobalCoarse x AND (!) y position are within an object, the object is not blocking. Normally, he is moving first in x-, then in
	 * y-direction. Thus after each partial movement, only one coordinate is inside, and the block occurs, resetting mario's position.
	 * 
	 * @param ya
	 *            the velocity in y-Direction. ya>0 is downwards
	 * @return
	 */
	public boolean isBlocking(float ya) {
		return Level.isBlocking((byte) this.objectID, ya);
	}

	/*public static void printArray(PlayerWorldObject[][] array, int minX, int maxX, int minY, int maxY) {
		String s = "";
		for (int y = minY; y < maxY; y++) {
			s = "";
			for (int x = minX; x < maxX; x++) {
				int code;
				int numCharacters;
				code = array[y][x].objectID;
				numCharacters = HumanInfoKeyboardAgent.numCharacters(code);
				s += code;
				for (int z = 0; z < 4 - numCharacters; z++)
					s += " ";
			}
			System.out.println(s);
		}
	}*/
	
	/**
	 * returns the name of the PWO that can be used to utter a command to the agent
	 * @return
	 */
	public static String getObjectVocalName(PlayerWorldObject item){

		switch (item) {
		case SIMPLE_BLOCK1:
		case SIMPLE_BLOCK2:
		case SIMPLE_BLOCK3:
		case SIMPLE_BLOCK4:
			return "simple block";
		case WRENCH:
			return "wrench";
		case BULB_FLOWER:
			return "bulb flower";
		case QUESTIONMARK_BLOCK1:
		case QUESTIONMARK_BLOCK2:
		case QUESTIONMARK_ENERGY_BLOCK:
		case QUESTIONMARK_FLOWER_BLOCK:
			return "question mark";
		case ENERGY_FRONT:
		case ENERGY_SIDE1:
		case ENERGY_SIDE2:
		case ENERGY_SIDE3:
			return "energy";
		case IRON_BLOCK1:
			return "iron block";
		case BOTTOM_LEFT_DOOR:
			return "door with a left arrow";
		case BOTTOM_RIGHT_DOOR:
			return "door with a right arrow";
		case PETERS_BLOCK1:
		case PETERS_BLOCK2:
		case PETERS_BLOCK3:
		case PETERS_BLOCK4:
			return "peters block";
		case STONE_BLOCK:
			return "stone";
		default:
			break;
		}
		return "";
	}
	
	public PlayerWorldObject loseWings() {
		switch (this) {
		//case LUKO_WINGED:
			//return LUKO;
		case GREEN_VIRUS_WINGED:
			return GREEN_VIRUS;
			/*
		case GREEN_KOOPA_WINGED:
			return GREEN_KOOPA;
		case RED_KOOPA_WINGED:
			return RED_KOOPA;
			*/
		//case GREEN_VIRUS_WINGED:
			//return GREEN_VIRUS;
		case GRUMPY_WINGED:
			return GRUMPY;
		case RED_VIRUS_WINGED:
			return RED_VIRUS;
		case SHALLY_WINGED:
			return SHALLY;
		default:
			if (this.isWinged()) {
				new Throwable(this.name() + " is winged but not included in switch case. Please update code.").printStackTrace();
			} else {
				new Throwable(this.name() + " is not winged.").printStackTrace();
			}
			return this;
		}
	}

	public enum ObjectType {
		STATIC(true), NON_STATIC(false); //C1R1 SAB
		private final boolean key;

		private ObjectType(boolean key) {
			this.key = key;
		}

		public static ObjectType getObjectType(boolean isStatic) {
			return isStatic ? STATIC : NON_STATIC;
		}
	}

	public boolean isHostile(){
		if(ENEMIES.contains(this)){
			return true;
		}

		return false;
	}
	/**
	 * 
	 * @param staticLocalMap
	 * @return Returns if this PlayerWorldObject is within observation
	 */
	public boolean isInRange(PlayerWorldObject[][] staticLocalMap) {
			
		for (int i = 0; i < staticLocalMap.length; i++) {
			for (int j = 0; j < staticLocalMap[i].length; j++) {
				if (staticLocalMap[i][j] == this) {
					return true;
				}
			}
		}
		return false;
	}

	
	/**
	 * 
	 * @param scene
	 * @return the position of this object in the current scene.
	 */
	public GlobalCoarse closestToPlayerPos(PlayerWorldObject[][] scene, GlobalCoarse playerPos) {
		double minDist = Double.POSITIVE_INFINITY;
		GlobalCoarse objPos = null;
		PlayerWorldObject[][] staticLocalMap = scene;
		for (int i = 0; i < staticLocalMap.length; i++) {
			for (int j = 0; j < staticLocalMap[i].length; j++) {
				if (this == staticLocalMap[i][j]) {
					GlobalCoarse pointToCheck = new ObservationRelativeCoarse(j, i).toGlobalCoarse(playerPos.toGlobalContinuous());
					if (playerPos.distance(pointToCheck) < minDist) 
					{
						minDist = playerPos.distance(pointToCheck);
						objPos = pointToCheck;
					}
				}
			}
		}
		return objPos;
	}
	
	public GlobalCoarse closestToPlayerPos(PlayerWorldObject[][] scene, PlayerWorldObject[][] scene2 , GlobalCoarse playerPos) {
		double minDist = Double.POSITIVE_INFINITY;
		GlobalCoarse objPos = null;
		PlayerWorldObject[][] staticLocalMap = scene;
		for (int i = 0; i < staticLocalMap.length; i++) {
			for (int j = 0; j < staticLocalMap[i].length; j++) {
				if (this == staticLocalMap[i][j] || this == scene2[i][j]) {
					GlobalCoarse pointToCheck = new ObservationRelativeCoarse(j, i).toGlobalCoarse(playerPos.toGlobalContinuous());
					if (playerPos.distance(pointToCheck) < minDist) 
					{
						minDist = playerPos.distance(pointToCheck);
						objPos = pointToCheck;
					}
				}
			}
		}
		return objPos;
	}

	/**
	 * searches through the scene to find MarioWordObject of interest and
	 * returns a GlobalCoarse with coordinates of that object. If no such object
	 * can be found it returns null.
	 * 
	 * @param goalType
	 *            type of goal to search for
	 * @return
	 */
	public static Goal getNearWorldObjectGoal(ArrayList<PlayerWorldObject> goalTypes, SimulatedPlayer actor, CollisionDirection direction, SimulatedLevelScene scene) 
	{	
		ArrayList<Entry<Goal,Double>> list = getReachableObjectGoals(goalTypes,actor,direction,scene); 
		if(list.size()>0)
			return list.get(0).getKey();	//returns nearest goal
		else return null;
	}	
	
//	public static Goal getNearWorldObjectGoal
//	(
//			ArrayList<PlayerWorldObject> goalTypes, 
//			CollisionDirection direction,
//			PlayerHealthCondition condition,
//			SimulatedLevelScene scene
//	) 
//	{	
//		GlobalCoarse pos = new GlobalCoarse();
//
//		PlayerWorldObject actor = scene.getPlayer().type;
//
//		// store observation of static real world
//		PlayerWorldObject[][] map = scene.createLocalStaticSimulation();
//
//		int mapHeight = map.length;
//		int mapWidth = map[0].length;
//		double minDistance = 1000;
//
//		GlobalContinuous playerPos = scene.getPlayerPos();
//
//		Goal goal = null;
//		
//		//GoalDistance goalDistance;
//
//		// CHECK: there is a function: PlayerWorldObject.closestToPlayerPos(...)
//		// ... doesn't implement "easily reachable" stuff
//		for (int y = 0; y < mapHeight; y++) {
//			for (int x = 0; x < mapWidth; x++) {
//				PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(map[y][x]);
//
//				if (goalTypes.contains(type)) {
//					pos = new ObservationRelativeCoarse(x, y).toGlobalCoarse(playerPos);
//
//					double distance = pos.toGlobalContinuous().distance(playerPos);
//
//					// punish near objects a bit
//					if (distance < 50)
//						distance += (50 - distance) * 6;
//
//					if (distance < minDistance && scene.isEasilyReachable(pos, direction)) 
//					{
//						minDistance = distance;
//						goal = new Goal(pos, actor, type, scene.getPlayer(), direction, condition);
//					}
//				}
//			}
//		}
//
//		// also look at sprite goals!
//		for (SimulatedSprite sprite : scene.getSimulatedSprites()) {
//			// System.out.println("for sprites");
//			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(sprite.type);
//
//			if (goalTypes.contains(type)) {
//				pos = sprite.getMapPosition(0);
//
//				double distance = pos.toGlobalContinuous().distance(playerPos);
//
//				// punish near objects a bit
//				if (distance < 32)
//					distance += (32 - distance) * 6;
//
//				if (distance < minDistance && scene.isEasilyReachable(pos, direction)) {
//					minDistance = distance;
//					//goal = new Goal(pos, actor, type, direction, sprite, PlayerHealthCondition.IRRELEVANT);
//					goal = new Goal(pos, actor, type, scene.getPlayer(), direction, condition);
//				}
//			}
//		}
//
//		// also look at player goals!
//		// TODO: remove redundant code!
//		for (SimulatedPlayer player : scene.getSimulatedPlayers()) {
//			// System.out.println("player for");
//			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(player.type);
//			 
//
//			if (goalTypes.contains(type)) {
//				pos = player.getMapPosition(0);
//
//				double distance = pos.toGlobalContinuous().distance(playerPos);
//
//				// punish near objects a bit
//				if (distance < 32)
//					distance += (32 - distance) * 6;
//
//				if (distance < minDistance && scene.isEasilyReachable(pos, direction)) {
//					minDistance = distance;
////					goal.setSprite(player);
//					goal = new Goal(pos, actor, type, scene.getPlayer(), direction, condition);
//					//goal = new Goal(pos, actor, type, direction, player, PlayerHealthCondition.IRRELEVANT);
//				}
//			}
//		}
//
//		return goal;
//	}
	
		// also look at sprite goals!
		/*for(SimulatedSprite sprite : scene.getSimulatedSprites()) {
			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(sprite.type);
			
			goalDistance = lookAtGoal(type,sprite,goalTypes,pos,playerPos,direction,scene,minDistance,actor);
			minDistance = goalDistance.minDistance;
			goal = goalDistance.goal; 
		}
	  
		// also look at player goals!
		for (SimulatedPlayer player : scene.getSimulatedPlayers()) {
			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(player.type);
			
			goalDistance = lookAtGoal(type,player,goalTypes,pos,playerPos,direction,scene,minDistance,actor);
			minDistance = goalDistance.minDistance;
			goal = goalDistance.goal;
		}
		
		return goal;
	}

	

	 private static GoalDistance lookAtGoal(PlayerWorldObject type, SimulatedSprite sprite, ArrayList<PlayerWorldObject> goalTypes,
			 GlobalCoarse pos, GlobalContinuous playerPos, CollisionDirection direction, SimulatedLevelScene scene, double minDistance,
			 PlayerWorldObject actor)
	 {
		 Goal goal = null;
	
		 if (goalTypes.contains(type)) {
			 pos = sprite.getMapPosition(0);
	
			 double distance = pos.toGlobalContinuous().distance(playerPos);
	
			 //punish near objects a bit
			 if(distance < 32)
				 distance += (32-distance)*6;
	
			 if(distance<minDistance && scene.isEasilyReachable(pos,direction)) {
				 minDistance = distance;
//				 goal = new Goal(pos, type, direction, PlayerHealthCondition.IRRELEVANT);
				 goal = new Goal(pos, actor, type, direction, sprite, PlayerHealthCondition.IRRELEVANT);
	 
				 if(sprite instanceof SimulatedPlayer){
					 goal.setSprite(sprite);
				 }
			 }
		 }
	
		 return new GoalDistance(goal, minDistance);
	
	 }*/

	// needed to return a pair of goal and double in lookAtGoal
	private static class GoalDistance {
		public Goal goal;
		public double minDistance;

		public GoalDistance(Goal goal, double minDistance) {
			this.goal = goal;
			this.minDistance = minDistance;
		}
	}
	
	public CAEAgent findAgent () {
		ArrayList<Player> players = PlayHook.players;
		ArrayList<Agent> agents = PlayHook.agents;
		for (int i = 0; i < players.size(); i++) {
			if ((players.get(i).getType() == this) && (agents.get(i) instanceof CAEAgent)) return (CAEAgent) agents.get(i); 
		}
		return null;
	}
	
	public static PlayerWorldObject findPlayer (int index){
		ArrayList<Player> players = PlayHook.players;
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getPlayerIndex() == index) return players.get(i).getType();
		}
		return null;
	}
	
	
	public static ArrayList<Entry<Goal,Double>> getReachableObjectGoals2(ArrayList<PlayerWorldObject> goalTypes, SimulatedPlayer actor, CollisionDirection direction, SimulatedLevelScene scene) 
	{
		// store observation of static real world
		PlayerWorldObject[][] map = scene.createLocalStaticSimulation();

		int mapHeight = map.length;
		int mapWidth = map[0].length;

		GlobalContinuous playerPos = scene.getPlanningPlayerPos();
		
		//TreeMap<Double,Goal> goalDistances = new TreeMap<Double,Goal>();
		ArrayList<Entry<Goal,Double>> goalDistances = new ArrayList<Entry<Goal,Double>>();
		
		//CHECK: there is a function: PlayerWorldObject.closestToPlayerPos(...) ... doesn't implement "easily reachable" stuff
		for (int y = 0; y < mapHeight; y++) 
		{
			for (int x = 0; x < mapWidth; x++) 
			{
				PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(map[y][x]);
				GlobalCoarse pos = new ObservationRelativeCoarse(x, y).toGlobalCoarse(playerPos);
				
				CollisionDirection test_direction = direction;				
//				if(type.blocksPath==BlockingType.NONE)
//					test_direction=CollisionDirection.IRRELEVANT;
				
				if(direction==CollisionDirection.IRRELEVANT && type.blocksPath!=BlockingType.NONE)
					direction=scene.getEasilyReachableDirection(pos);		
				
				if((goalTypes==null || goalTypes.contains(type)) && type.isRelevantToLearning() && scene.isEasilyReachable(pos,test_direction)) 
				{						
					double distance = pos.toGlobalContinuous().distance(playerPos);	

					goalDistances.add(new SimpleEntry<Goal,Double>
					(
							new Goal
							(
									pos, 
									actor.type, 
									actor, 
									type,
									null,
									test_direction, 
									PlayerHealthCondition.fromInt(scene.getPlanningPlayer().getHealth(), false, false)
							),
							distance
					));
				}
			}
		}
		
		//also look at sprite goals!
		for(SimulatedSprite sprite : scene.getSimulatedSprites())
		{
			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(sprite.type);
			GlobalCoarse pos = sprite.getMapPosition(0);

			CollisionDirection test_direction = direction;
						
//			if(type.blocksPath==BlockingType.NONE)
//				test_direction=CollisionDirection.IRRELEVANT;			

			if(direction==CollisionDirection.IRRELEVANT && type.blocksPath!=BlockingType.NONE)
				direction=scene.getEasilyReachableDirection(pos);		
			
			if ((goalTypes==null || goalTypes.contains(type)) && type!=actor.type && /*type.isRelevantToLearning() &&*/ scene.isEasilyReachable(pos,test_direction))
			{				
				double distance = pos.toGlobalContinuous().distance(playerPos);
				
				goalDistances.add(new SimpleEntry<Goal,Double>
				(
						new Goal
						(
								pos, 
								actor.type,
								actor,
								type, 
								sprite, 
								test_direction, 
								PlayerHealthCondition.fromInt(scene.getPlanningPlayer().getHealth(), false, false)
						),
						distance
				));
			}
		}				

		//also look at player goals!
		for(SimulatedPlayer player : scene.getSimulatedPlayers())
		{
			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(player.type);
			GlobalCoarse pos = player.getMapPosition(0);

			CollisionDirection test_direction = direction;				
//			if(type.blocksPath==BlockingType.NONE)
//				test_direction=CollisionDirection.IRRELEVANT;			

			if(direction==CollisionDirection.IRRELEVANT && type.blocksPath!=BlockingType.NONE)
				direction=scene.getEasilyReachableDirection(pos);					
			
			if ((goalTypes==null || goalTypes.contains(type)) && type!=actor.type && /*type.isRelevantToLearning() &&*/ scene.isEasilyReachable(pos,test_direction))
			{				
				double distance = pos.toGlobalContinuous().distance(playerPos);
				
				goalDistances.add(new SimpleEntry<Goal,Double>
				(
						new Goal
						(
								pos, 
								actor.type,
								actor,
								type, 
								player, 
								test_direction, 
								PlayerHealthCondition.fromInt(scene.getPlanningPlayer().getHealth(), false, false)
						),
						distance
				));
			}
		}		

		Collections.sort(goalDistances, new Comparator<Entry<Goal, Double>>() 
		{
		    @Override
		    public int compare(Entry<Goal, Double> x, Entry<Goal, Double> y) 
		    {
		        return (int)(x.getValue() - y.getValue());
		    }
		});
				
		//return sorted list of goals
		return goalDistances;
	}
	/**
	 * @author Yves
	 * @param goalType
	 * @param actorSprite
	 * @param collisionDirection
	 * @param scene
	 * @return
	 */
	public static ArrayList<Entry<Goal, Double>> getReachableObjectGoals(ArrayList<PlayerWorldObject> goalType,
			SimulatedPlayer actorSprite, CollisionDirection collisionDirection, SimulatedLevelScene scene) {
		
		ArrayList<ReachabilityNode> reachabilities = scene.reachabilityNodes;
		//get rechabality node of the actor of the cdp
		ArrayList<Entry<Goal,Double>> ret = new ArrayList<Entry<Goal,Double>>();
	
		if(reachabilities.size() != scene.getSimulatedPlayers().size()) {
			return ret;
		}

		int actorPlayerIndex  = scene.getActingPlayerIndex(actorSprite);
		ReachabilityNode reachability = reachabilities.get(actorPlayerIndex);

		GlobalContinuous actorPos = scene.getSimulatedPlayers().get(actorPlayerIndex).getPosition(0);
		double minDistance=100000;
		Goal goal = null;
		PlayerWorldObject targetType = null;
		PlayerWorldObject actorType = actorSprite.type;
		Boolean actingPlayerisCarrier =  actorSprite.getCarries() != null;
		SimulatedSprite targetSprite = null;
		SimulatedSprite movingSprite = scene.getSimulatedPlayers().get(scene.calculateMovingPlayer());
		
		
			for(CollisionAtPosition collision : reachability.getEffectTargetRelevantCollisions()) {
				targetType = Generalizer.generalizePlayerWorldObject(collision.conditionDirectionPair.condition.getTarget());
				if(collision.getDirection().equals(collisionDirection) || collisionDirection.equals(CollisionDirection.IRRELEVANT)) {
					if(goalType.contains(targetType)){
						SimulatedSprite collisionActor = scene.getSpriteBasedOnType(collision.conditionDirectionPair.condition.getActor());
						targetSprite = scene.getSpriteBasedOnType(targetType);
						//if target is equal to the moving player (means, the player that is a carrier or if no such relation exist, the currently calculating player)
						//is important for dismount
						if(targetType.equals(actorType)){
							
							//goal = new Goal(collision.getPosition(), scene.getPlayer().type, type, collisionDirection, targetSprite, false, false, health);
							goal = new Goal
										(
												collision.getPosition(), 
												actorSprite.type,
												scene.getPlanningPlayer(),
												targetType,
												targetSprite,
												collisionDirection,
												PlayerHealthCondition.fromInt(scene.getPlanningPlayer().getHealth(), false, false)
										);
							} else {
								//goal = new Goal(collision.getPosition(), cdp.condition.getActor(), target, collisionDirection, targetSprite, false, false, health);
								goal = new Goal
										(
												collision.getPosition(), 
												collisionActor.type,
												collisionActor,
												targetType,
												targetSprite,
												collisionDirection,
												PlayerHealthCondition.fromInt(scene.getPlanningPlayer().getHealth(), false, false)
										);	
						}
						
						if(!movingSprite.type.equals(actorType) && actingPlayerisCarrier){
							goal = new Goal
									(
											collision.getPosition(), 
											movingSprite.type,
											movingSprite,
											targetType,
											targetSprite,
											collisionDirection,
											PlayerHealthCondition.fromInt(scene.getPlanningPlayer().getHealth(), false, false)
									);
						} 
						
						minDistance = actorPos.distance(collision.getPosition().toGlobalContinuous());
						ret.add(new SimpleEntry<Goal, Double>(goal,minDistance));
					}
				}
			}
		
		
		
		//new SimpleEntry<Goal, Double>(goal,minDistance)
		return ret;
	}
	
	/**
	 * Gets all objects goals for a given simulated levelscene
	 * Is used in the curiosity planner
	 * @author Yves
	 * @param actorSprite
	 * @param collisionDirection
	 * @param scene
	 * @return
	 */
	public static ArrayList<Entry<Goal, Double>> getReachableObjectGoals(SimulatedPlayer actorSprite, CollisionDirection collisionDirection, SimulatedLevelScene scene) {
		
		ArrayList<ReachabilityNode> reachabilities = scene.reachabilityNodes;
		//get rechabality node of the actor of the cdp
		ArrayList<Entry<Goal,Double>> ret = new ArrayList<Entry<Goal,Double>>();
	
		if(reachabilities.size() != scene.getSimulatedPlayers().size()) {
			return ret;
		}

		int actorPlayerIndex  = scene.getActingPlayerIndex(actorSprite);
		ReachabilityNode reachability = reachabilities.get(actorPlayerIndex);
		
		

		GlobalContinuous actorPos = scene.getSimulatedPlayers().get(actorPlayerIndex).getPosition(0);
		double minDistance=100000;
		Goal goal = null;
		PlayerWorldObject targetType = null;
		PlayerWorldObject actorType = actorSprite.type;
		Boolean actingPlayerisCarrier =  actorSprite.getCarries() != null;
		SimulatedSprite targetSprite = null;
		SimulatedSprite movingSprite = scene.getSimulatedPlayers().get(scene.calculateMovingPlayer());
		
		
			for(CollisionAtPosition collision : reachability.getEffectTargetRelevantCollisions()) {
				goal = null;
				targetType = Generalizer.generalizePlayerWorldObject(collision.conditionDirectionPair.condition.getTarget());
				
				if(collision.getDirection().equals(collisionDirection) && isVisibleToPlayer(actorPos.toGlobalCoarse(), collision.getPosition())) {
					
						SimulatedSprite collisionActor = scene.getSpriteBasedOnType(collision.conditionDirectionPair.condition.getActor());
						targetSprite = scene.getSpriteBasedOnType(targetType);
						//if target is equal to the moving player (means, the player that is a carrier or if no such relation exist, the currently calculating player)
						//is important for dismount
						if(targetType.equals(actorType)){
							continue;
						} else if(collisionActor.type.equals(actorType)) {
								//goal = new Goal(collision.getPosition(), cdp.condition.getActor(), target, collisionDirection, targetSprite, false, false, health);
								goal = new Goal
										(
												collision.getPosition(), 
												collisionActor.type,
												collisionActor,
												targetType,
												targetSprite,
												collisionDirection,
												PlayerHealthCondition.fromInt(scene.getPlanningPlayer().getHealth(), false, false)
										);	
						}
						
						if(!movingSprite.type.equals(actorType) && actingPlayerisCarrier){
							goal = new Goal
									(
											collision.getPosition(), 
											movingSprite.type,
											movingSprite,
											targetType,
											targetSprite,
											collisionDirection,
											PlayerHealthCondition.fromInt(scene.getPlanningPlayer().getHealth(), false, false)
									);
						} 
						
						if(goal != null) {
							minDistance = actorPos.distance(collision.getPosition().toGlobalContinuous());
							ret.add(new SimpleEntry<Goal, Double>(goal,minDistance));	
						}
						
					
				}
			}
		
		
		
		//new SimpleEntry<Goal, Double>(goal,minDistance)
		return ret;
	}
	/**
	 * Checks if the objects position is within the visible limits (i.e. 10tiles in x direction)
	 * 
	 * @param playerPos
	 * @param objectPos
	 * @return
	 */
	private static boolean isVisibleToPlayer(GlobalCoarse playerPos, GlobalCoarse objectPos) {
		int xDif = playerPos.x - objectPos.x;
		if(xDif > 10 || xDif < -10) {
			return false;
		} else {
			return true;
		}
		
	}
	
	public static ArrayList<Entry<Goal, Double>> getReachablePositionGoals(CollisionDirection collisionDirection, SimulatedLevelScene scene, Goal finalGoal) {
		
		

		SimulatedPlayer finalGoalActor = (SimulatedPlayer)scene.getSpriteBasedOnType(finalGoal.getActor());
		//Actor is the one carrying the other one, elsewise its the actor specified in the final goal.
		SimulatedPlayer movingActor =  scene.getSimulatedPlayers().get(scene.calculateMovingPlayer(finalGoalActor.getPlayerIndex()));
		GlobalContinuous playerPos = movingActor.getPosition(0);
		ArrayList<ReachabilityNode> reachabilities = scene.reachabilityNodes;
		//get rechabality node of the actor of the cdp
		ReachabilityNode reachability = reachabilities.get(movingActor.getPlayerIndex());
		ArrayList<Entry<Goal,Double>> ret = new ArrayList<Entry<Goal,Double>>();
		GlobalCoarse goalPosition = new GlobalCoarse(finalGoal.getPosition().x,finalGoal.getPosition().y);
		
		
		double minDistance=100000;
		Goal goal = null;
		PlayerWorldObject targetType = null;
		SimulatedSprite targetSprite = null;
		
		
		int counter = 0;
		
		if(finalGoal.hasEffect() && !finalGoal.getEffect().getType().equals(ActionEffect.PROGRESS)){
			for(CollisionAtPosition collision : reachability.getPositionTargetRelevantCollisions()) {
				targetType = Generalizer.generalizePlayerWorldObject(collision.conditionDirectionPair.condition.getTarget());
				if(collision.getDirection().equals(collisionDirection)) {
					if(counter <= 2 && collision.getPosition().getY() < movingActor.getMapPosition(0).y){
						targetSprite = scene.getSpriteBasedOnType(targetType);
						GlobalCoarse position = collision.getPosition();
						//if(!isPositionBlocking(position,scene,actor)) {
							//position.y--;
							goal = new Goal(
									position, 
									movingActor.type,
									movingActor, 
									targetType, 
									targetSprite, 
									collisionDirection, 
									collision.getHealth());								
							minDistance = playerPos.distance(collision.getPosition().toGlobalContinuous());
							ret.add(new SimpleEntry<Goal, Double>(goal,minDistance));
						//}
						
						counter++;
					}
				}
			}
		} else { //if progress goal e.g. "go there"
			collisionDirection = finalGoal.getDirection();
//			movingActor = (SimulatedPlayer)finalGoal.getActorSprite();
			reachability = reachabilities.get(scene.calculateMovingPlayer(scene.getActingPlayerIndex(movingActor)));
			
			//go to empty position
//			if(finalGoal.getTarget().equals(PlayerWorldObject.NONE)) {
			if(finalGoal.getTarget().blocksPath.equals(BlockingType.NONE)) {
				if(reachability.reachabilityCounter[goalPosition.y][goalPosition.x] != 0 ) {
					goal = new Goal(
							goalPosition, 
							movingActor.type,
							movingActor, 
							targetType, 
							targetSprite, 
							collisionDirection, 
							PlayerHealthCondition.IRRELEVANT);
					goal.setEffect(new Effect(ActionEffect.PROGRESS, movingActor.type));
					minDistance = playerPos.distance(goalPosition.toGlobalContinuous());
					ret.add(new SimpleEntry<Goal, Double>(goal,minDistance));
				}
			}  else { //collide with object
				
				for(CollisionAtPosition collision : reachability.uniqueFoundCollisions) {
					targetType = Generalizer.generalizePlayerWorldObject(collision.conditionDirectionPair.condition.getTarget());
					if(collision.getDirection().equals(collisionDirection, false, true)) {
						if(collision.getPosition().equals(goalPosition)){
							targetSprite = scene.getSpriteBasedOnType(targetType);
							GlobalCoarse position = collision.getPosition();
							//if(!isPositionBlocking(position,scene,actor)) {
								//position.y--;
								goal = new Goal(
										position, 
										movingActor.type,
										movingActor, 
										targetType, 
										targetSprite, 
										collision.getDirection(), 
										collision.getHealth());
								goal.setEffect(new Effect(ActionEffect.PROGRESS, movingActor.type));
								minDistance = playerPos.distance(collision.getPosition().toGlobalContinuous());
								ret.add(new SimpleEntry<Goal, Double>(goal,minDistance));
							//}
							
							
						}
					}
				}
			}
			
			
			
			
		}
		
		
		//new SimpleEntry<Goal, Double>(goal,minDistance)
		return ret;
	}
	
	public static boolean isPositionBlocking(GlobalCoarse position,SimulatedLevelScene scene, SimulatedPlayer actor) {
		int numberOfTiles = actor.getCarries()== null ? 3 : 5;
		for(int i = 1; i<=numberOfTiles && (position.y - i) >= 0; i++){
			if(scene.getStaticGlobalMap()[position.y - i][position.x].blocksPath != BlockingType.NONE){
				return true;
			}
		}
		
		return false;
	}

	public static Goal lookupSelectedTile(CAEAgent activeAgent, CollisionDirection collisionDirection, SimulatedLevelScene level) {
		Goal goal = null;
		if(activeAgent.getMouseInput().isOverlayActive()) {
			GlobalCoarse bestGoalPos = activeAgent.getMouseInput().getSelectedTilePosition(activeAgent.getBrain().levelSceneAdapter.getClonedLevelScene(), activeAgent.getPlayerIndex()).toGlobalCoarse();
//			activeAgent.getMouseInput().setOverlayActive(false);
			PlayerWorldObject target = level.getStaticGlobalMapObject(bestGoalPos.y, bestGoalPos.x);
			SimulatedPlayer targetSprite = null;
			for(SimulatedPlayer player : level.getSimulatedPlayers()) {
				if( player.getMapPosition(0).equals(bestGoalPos)) {
					target = player.getType();
					targetSprite = player;
				}
			}
			
			goal = new Goal(bestGoalPos, level.getPlanningPlayer().getType(), level.getPlanningPlayer(), target, targetSprite, collisionDirection, PlayerHealthCondition.IRRELEVANT);
			goal.setEffect(new Effect(ActionEffect.PROGRESS, level.getPlanningPlayer().getType()));
		} 
		
		
		
		return goal;
	}
}


// -20 BREAKABLE_BRICK Simple brick or with hidden energy or with hidden flower
// -22 UNBREAKABLE_BRICK Brick with a question sign with power up
// 2 ENERGY_ANIM Energy
// -60 BORDER_CANNOT_PASS_THROUGH
// -82 CANNON_MUZZLE
// -80 CANNON_TRUNK
// -90 FLOWER_POT
// -62 BORDER_HILL Soft obstacle, can overjump (half-border, can jump through
// from bottom and can stand on)

