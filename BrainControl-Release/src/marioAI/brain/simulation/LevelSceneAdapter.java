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
package marioAI.brain.simulation;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import marioAI.brain.Brain;
import marioAI.brain.Effect;
import marioAI.brain.Generalizer;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.movement.PlayerAction;
import marioAI.movement.PlayerActions;
import marioAI.movement.ReachabilityNode;
import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.ObjectType;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.ISprite;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;
import marioWorld.mario.environments.Environment;
import marioWorld.utils.ResetStaticInterface;

/**
 * This class provides all information that the brain (and the praktikum
 * package) should need (ideally).
 * 
 * @author Natalie, Stephan
 * 
 */
public class LevelSceneAdapter implements ResetStaticInterface {

	/**
	 * size of the whole level in continuous coordinates
	 */
	private static int totalLevelWidth;
	private static int totalLevelHeight;

	/**
	 * ensures, that the simulatedLevelScene does not change while it is cloned
	 * during Start of AStar
	 */
	public final ReentrantLock simulatedLevelSceneLock = new ReentrantLock();
	public final ReentrantLock actionPlanLock = new ReentrantLock();

	/**
	 * An implementation of all the world's states and functionality which are
	 * known to mario. Includes simulatedMario, simulatedSprites and
	 * simulatedGlobalStaticMap
	 */
	private SimulatedLevelScene simulatedLevelScene;
	private SimulatedLevelScene lastSimulatedLevelScene;

	/**
	 * this is the engine world. Used to create a simulation copy from whenever
	 * readNewSimulatedLevelScene() is called.
	 */
	private final LevelScene levelScene;

	private final Brain brain;
	private int planningPlayerIndex;

	/**
	 * Constructor.
	 * 
	 * @param createClones
	 *            if true, clones of map, mario and sprites are created, which
	 *            are all contained in simulatedLevelScene.
	 * @param lastTimeStepInstance
	 */
	public LevelSceneAdapter(LevelScene levelScene, Brain brain) {
		classesWithStaticStuff.add(this.getClass());

		this.levelScene = levelScene; // ToolsConfigurator.gameWorldComponent.getLevelScene()
		createAndSetNewSimulatedLevelScene(brain);
		
		this.brain = brain;
		LevelSceneAdapter.totalLevelWidth = levelScene.level.width
				* CoordinatesUtil.UNITS_PER_TILE;
		LevelSceneAdapter.totalLevelHeight = levelScene.level.height
				* CoordinatesUtil.UNITS_PER_TILE;
		LevelSceneAdapter.totalLevelWidth = levelScene.level.width
				* CoordinatesUtil.UNITS_PER_TILE;
		LevelSceneAdapter.totalLevelHeight = levelScene.level.height
				* CoordinatesUtil.UNITS_PER_TILE;
		this.planningPlayerIndex = brain.getPlayerIndex();
	}

	/**
	 * Creates a new simulatedLevelScene by reading the actual engine world.
	 */
	public SimulatedLevelScene createNewSimulatedLevelScene(Brain brain) 
	{			
		/** create simulated clones of mario, sprites & map */
		ArrayList<SimulatedPlayer> simulatedPlayers = new ArrayList<SimulatedPlayer>();
				
		HashMap <Player,SimulatedPlayer> psmap = new HashMap <Player,SimulatedPlayer>();
		for (Player player : levelScene.getPlayers())
		{
			simulatedPlayers.add(new SimulatedPlayer(player));
			psmap.put(player, simulatedPlayers.get(simulatedPlayers.size()-1));
		}
		
		for(int i=0;i<simulatedPlayers.size();i++)
		{
			simulatedPlayers.get(i).setCarries(psmap.get(levelScene.getPlayers().get(i).getCarries()));
			simulatedPlayers.get(i).setCarriedBy(psmap.get(levelScene.getPlayers().get(i).getCarriedBy()));
		}

		HashSet<SimulatedSprite> simulatedSprites = new HashSet<SimulatedSprite>();
		HashSet<SimulatedSprite> simulatedSpritesToAdd = new HashSet<SimulatedSprite>();
		HashMap<SimulatedSprite, Sprite> enemyTracking = new HashMap<SimulatedSprite, Sprite>();
		for (Sprite sprite : levelScene.getSprites()) {
			if (sprite.kind != Sprite.KIND_PLAYER) {
				SimulatedSprite simulatedSprite = SimulatedSprite
						.createSimulatedSprite(sprite);
				if (simulatedSprite != null) {
					simulatedSprites.add(simulatedSprite);
					if (simulatedSprite.needsTracking) {
						enemyTracking.put(simulatedSprite, sprite);
					}
				}
			}
		}
		for (Sprite sprite : levelScene.getSpritesToAdd()) {
			SimulatedSprite simulatedSprite = SimulatedSprite
					.createSimulatedSprite(sprite);
			if (simulatedSprite != null) {
				simulatedSpritesToAdd.add(simulatedSprite);
				if (simulatedSprite.needsTracking) {
					enemyTracking.put(simulatedSprite, sprite);
				}
			}
		}

		// ATTENTION: Java is considered "row major", meaning that it does rows
		// first. This is because a 2D array is an "array of arrays".
		// so this is not the standard matrix encoding. also, the map is encoded
		// in [x][y] in the levelscene, but as [y][x] in simulatedlevelscene!!!
		// TODO!
		int mapHeight = getMap()[0].length;
		int mapWidth = getMap().length;
		PlayerWorldObject[][] staticGlobalMap = byte2DArrayToPlayerWorldObjectArray(levelScene.level.map);
		PlayerWorldObject[][] staticLocalMap = createLocalObservationFromGlobalObservation(
				simulatedPlayers.get(planningPlayerIndex).getMapPosition(0),
				staticGlobalMap, mapHeight);
		
		
		ArrayList<ReachabilityNode> reachabilityNodes = new ArrayList<ReachabilityNode>();
		if(brain != null) {
			reachabilityNodes.add(brain.reachabilityNode);
		}

		/** put simulated clones in wrapper */
		if (this.brain == null) {
			return new SimulatedLevelScene(simulatedPlayers, simulatedSprites,
					simulatedSpritesToAdd, enemyTracking, staticGlobalMap,
					staticLocalMap, mapHeight, mapWidth, levelScene.paused,
					getElapsedTimeInFrames(), this.simulatedLevelSceneLock,
					brain.getKnowledge(), levelScene.level.spriteTemplates,
					planningPlayerIndex);
		} else {
			//ReachabilityMap tempMap = simulatedLevelScene.Rmap;
			SimulatedLevelScene temp = new SimulatedLevelScene(simulatedPlayers, simulatedSprites,
					simulatedSpritesToAdd, enemyTracking, staticGlobalMap,
					staticLocalMap, mapHeight, mapWidth, levelScene.paused,
					getElapsedTimeInFrames(), this.simulatedLevelSceneLock,
					this.brain.getKnowledge(),
					levelScene.level.spriteTemplates, planningPlayerIndex);
			//temp.Rmap = tempMap;
			temp.reachabilityNodes = reachabilityNodes;
			
//			if(temp.elapsedTime == 15) {
//				temp.recomputeReachabilityMap();
//			}
//			// TODO: funktioniert nicht!!!!
//			if ((lastSimulatedLevelScene.getPlanningPlayer().getCarriedBy()!=null && simulatedLevelScene.getPlanningPlayer().getCarriedBy()==null)){
//				temp.recomputeReachabilityMap();
//			}
//			if(temp.elapsedTime%30==0){
//				temp.Rmap.resetfirsthalfofSceneList(simulatedLevelScene);
//			}
//			if(temp.elapsedTime > 15 && (temp.elapsedTime+15)%30==0){
//				temp.Rmap.resetsecondhalfofSceneList(simulatedLevelScene);
//			}
//			if (temp.elapsedTime > 15) {
//				temp.expandReachabilityMap(temp.Rmap);
//			}

			/**
			 * TODO: Auskommentiert
			 */
//			if(temp.elapsedTime == 15) 
//				temp.recomputeReachabilityMap();
//			else if(temp.elapsedTime > 15)
//			{
//				temp.expandReachabilityMap(temp.Rmap);
//			}
			
			return temp;
		}
	}

	/**
	 * Discards the simulatedLevelScene and creates a new one by reading the
	 * actual engine world. Should be called once per frame (and player).
	 */
	public void createAndSetNewSimulatedLevelScene(Brain brain) 
	{
		/** put simulated clones in wrapper */

		if (this.simulatedLevelScene != null) 
		{
			try 
			{
				this.lastSimulatedLevelScene = (SimulatedLevelScene) simulatedLevelScene.clone();
				//lastSimulatedLevelScene.Rmap = simulatedLevelScene.Rmap;
			} 
			catch (CloneNotSupportedException e) 
			{
				e.printStackTrace();
			}
		}

		this.simulatedLevelScene = createNewSimulatedLevelScene(brain);
		
		/*if (lastSimulatedLevelScene != null){
			simulatedLevelScene.Rmap = lastSimulatedLevelScene.Rmap;
			}
		else {simulatedLevelScene.Rmap = new ReachabilityMap(simulatedLevelScene, simulatedLevelScene.staticGlobalMap);
		}*/
		//reachabilityMapIndex = (reachabilityMapIndex +1)%20;
		//if ( reachabilityMapIndex == 19){			
			//SimulatedPlayer player = simulatedLevelScene.simulatedPlayers.get(simulatedLevelScene.getPlayerIndex());
			//player.iscloneForReachabilityMap = true;
			//player.scene = simulatedLevelScene;
			//player.scene.Kmap = simulatedLevelScene.Kmap;
			//simulatedLevelScene.Kmap.updateReachabilityMap(player, 3);
			//simulatedLevelScene.Kmap.showMap("LevelSceneAdapter: ");		
		//}
		/*for (int i = 0; i<simulatedLevelScene.Kmap.particles.size(); i++){
			simulatedLevelScene.Kmap.particles.get(i).scene = simulatedLevelScene;
		}*/
		
//		// test if everything was cloned correctly
//		//TODO: for debugging forward simulation
//		if(lastSimulatedLevelScene!=null)
//		{
//			SimulatedLevelScene test = new SimulatedLevelScene(lastSimulatedLevelScene);
//			test.tick(getAllKeys());
//			compareSimulationWithEngine(test);
//		}
	}

	public void compareSimulationWithEngine(SimulatedLevelScene simulation) {
		simulation.compareWithEngine(levelScene.getPlayers(),
				levelScene.level.map);
	}

	public SimulatedLevelScene getLastSimulatedLevelScene() {
		return lastSimulatedLevelScene;
	}

	public SimulatedLevelScene getSimulatedLevelScene() {
		return simulatedLevelScene;
	}

	public boolean[] getKeys(int playerindex) {
		return levelScene.getPlayers().get(playerindex).keys;
	}

	public PlayerActions getAllKeys() 
	{
		ArrayList<PlayerAction> list = new ArrayList<PlayerAction>();
		for(Player player : levelScene.getPlayers())
		{
			PlayerAction tmp = new PlayerAction();
			tmp.setKeyState(player.keys);
			list.add(tmp);
		}		
		PlayerActions ret = new PlayerActions();
		ret.setKeyStates(list);
		return ret;
	}
	
	// public PlayerWorldObject[][] getGlobalStaticRealWorldObservation()
	// {
	// return
	// praktikumGlobalStaticObservation(simulatedLevelScene.staticGlobalMap);
	// }

	/**
	 * The returned map is independent on marios position and covers the whole
	 * level. This method is similar to getMap(), but it's transposed and also
	 * the array contains MarioWorldObjects instead of bytes.
	 * 
	 * @return all static objects in the whole level
	 */
	// protected static PlayerWorldObject[][] cloneMap(PlayerWorldObject[][]
	// map)
	// {
	// PlayerWorldObject[][] ret = new
	// PlayerWorldObject[map.length][map[0].length];
	//
	// for (int i = 0; i < ret.length; i++)
	// {
	// for (int j = 0; j < ret[i].length; j++)
	// {
	// //ret[i][j] = praktikumStaticGeneralization(map[i][j]);
	// ret[i][j] = map[i][j]; //CHECK: generalization is done somewhere else
	// }
	// }
	// return ret;
	// }

	/**
	 * rotates the map in levelScene which is [col][row]
	 * 
	 * @param map
	 * @param generalize
	 * @return [row][col]
	 */
	private static PlayerWorldObject[][] byte2DArrayToPlayerWorldObjectArray(
			byte[][] map) {
		// ATTENTION: this converts from [x][y] encoding to [y][x] encoding

		PlayerWorldObject[][] ret = new PlayerWorldObject[map[0].length][map.length];

		for (int y = 0; y < ret.length; y++) {
			for (int x = 0; x < ret[0].length; x++) {
				ret[y][x] = PlayerWorldObject.getElement(map[x][y],
						ObjectType.STATIC, 0);
			}
		}
		return ret;
	}

	public static PlayerWorldObject[][] createLocalObservationFromGlobalObservation(
			GlobalCoarse playerPosition, PlayerWorldObject[][] staticGlobalMap,
			int mapHeight) {
		return createLocalStaticObservation(playerPosition, staticGlobalMap,
				mapHeight);
	}

	// public PlayerWorldObject[][] getLocalStaticRealWorldObservation()
	// {
	// GlobalCoarse playerPosition =
	// simulatedLevelScene.simulatedPlayers.get(playerIndex).getPosition(0).toGlobalCoarse();
	// return createLocalObservationFromGlobalObservation(playerPosition,
	// simulatedLevelScene.cloneGlobalStaticSimulation(),
	// simulatedLevelScene.mapHeight);
	// }

	/**
	 * returns an observation around mario of static objects similar to
	 * levelSceneObservation, only that this method returns an array over
	 * MarioWorldObjects instead of over bytes
	 * 
	 * @return all static objects around mario
	 */
	protected static PlayerWorldObject[][] createLocalStaticObservation(
			GlobalCoarse playerPosition, PlayerWorldObject[][] map,
			final int mapHeight) {
		PlayerWorldObject[][] ret = new PlayerWorldObject[Environment.HalfObsHeight * 2 + 1][Environment.HalfObsWidth * 2 + 1];

		for (int y = playerPosition.y - Environment.HalfObsHeight, yIndex = 0; y <= playerPosition.y
				+ Environment.HalfObsHeight; y++, yIndex++) {
			for (int x = playerPosition.x - Environment.HalfObsWidth, xIndex = 0; x <= playerPosition.x
					+ Environment.HalfObsWidth; x++, xIndex++) {
				if (x >= 0 && y >= 0 && y < mapHeight && x < map[y].length) { //Abbruch fï¿½r Reachability eingefï¿½hrt
					ret[yIndex][xIndex] = map[y][x];
				} else
					ret[yIndex][xIndex] = PlayerWorldObject.NONE;
			}
		}
		return ret;
	}

	// /**
	// * returns an observation around mario of static objects similar to
	// * levelSceneObservation, only that this method returns an array over
	// * MarioWorldObjects instead of over bytes
	// *
	// * @return all static objects around mario
	// */
	// protected MarioWorldObject[][] praktikumLocalStaticObservation(final int
	// mapHeight) {
	// MarioWorldObject[][] ret = new MarioWorldObject[Environment.HalfObsWidth
	// * 2][Environment.HalfObsHeight * 2];
	// GlobalCoarse marioPosition = getMario().getPosition().toGlobalCoarse();
	//
	// for (int y = marioPosition.y - Environment.HalfObsHeight, yIndex = 0; y <
	// marioPosition.y
	// + Environment.HalfObsHeight; y++, yIndex++) {
	// for (int x = marioPosition.x - Environment.HalfObsWidth, xIndex = 0; x <
	// marioPosition.x
	// + Environment.HalfObsWidth; x++, xIndex++) {
	// if (x >= 0 && y >= 0 && y < mapHeight) {
	// ret[yIndex][xIndex] = praktikumStaticGeneralization(getMap()[x][y]);
	// }
	// }
	// }
	// return ret;
	// }

	// /**
	// * returns an observation of objects which are able to move (but might
	// stand
	// * still at the moment) in the whole level.
	// *
	// * @param timeDelay
	// * For timeDelay=0 (1, 2), this method uses x, (xOld, xOldOld),
	// * analogous for y.
	// * @return all potentially moving objects in the whole level
	// */
	// public MarioWorldObject[][] praktikumGlobalMovingObservation(int
	// timeDelay) {
	// final int numRows = this.mapHeight;
	// final int numCols = this.mapWidth;
	// MarioWorldObject[][] ret = new MarioWorldObject[numRows][numCols];
	// for (int i = 0; i < numRows; i++) {
	// for (int j = 0; j < numCols; j++) {
	// ret[i][j] = MarioWorldObject.NONE;
	// }
	// }
	//
	// final boolean useSimulatedSprites = true;
	// if (useSimulatedSprites) {
	// for (SimulatedSprite simulatedSprite :
	// simulatedLevelScene.simulatedSprites) {
	// SimpleEntry<Boolean, GlobalCoarse> pointInMap = isInMap(
	// this.mapWidth, this.mapHeight,
	// simulatedSprite.getPosition(timeDelay));
	// if (pointInMap.getKey()) {
	// MarioWorldObject spriteObject =
	// praktikumMovingGeneralization(simulatedSprite);
	// // don't include mario
	// if (spriteObject != MarioWorldObject.NONE
	// && spriteObject != MarioWorldObject.MARIO) {
	// ret[pointInMap.getValue().y][pointInMap.getValue().x] = spriteObject;
	// }
	// }
	// }
	// } else { // currently not used
	// for (Sprite sprite : getSprites()) {
	// SimpleEntry<Boolean, GlobalCoarse> pointInMap = isInMap(
	// this.mapWidth, this.mapHeight,
	// sprite.getPosition(timeDelay));
	// if (pointInMap.getKey()) {
	// MarioWorldObject spriteObject = praktikumMovingGeneralization(sprite);
	// // don't include mario
	// if (spriteObject != MarioWorldObject.NONE
	// && spriteObject != MarioWorldObject.MARIO) {
	// ret[pointInMap.getValue().y][pointInMap.getValue().x] = spriteObject;
	// }
	// }
	// }
	// }
	// return ret;
	// }

	/*
	 * public PlayerWorldObject[][] getGlobalMovingRealWorldObservation(int
	 * timeDelay) { return
	 * LevelSceneAdapter.praktikumGlobalMovingObservation(simulatedLevelScene
	 * .simulatedSprites, timeDelay, simulatedLevelScene.mapHeight,
	 * simulatedLevelScene.mapWidth); }
	 */

	/**
	 * returns an observation of objects which are able to move (but might stand
	 * still at the moment) in the whole level.
	 * 
	 * @param timeDelay
	 *            For timeDelay=0 (1, 2), this method uses x, (xOld, xOldOld),
	 *            analogous for y.
	 * @return all potentially moving objects in the whole level
	 */
	/*
	 * protected static PlayerWorldObject[][]
	 * praktikumGlobalMovingObservation(final HashSet<SimulatedSprite> sprites,
	 * final int timeDelay, final int numRows, final int numCols) {
	 * PlayerWorldObject[][] ret = new PlayerWorldObject[numRows][numCols]; for
	 * (int i = 0; i < numRows; i++) { for (int j = 0; j < numCols; j++) {
	 * ret[i][j] = PlayerWorldObject.NONE; } } for (Object spriteObject :
	 * sprites) { ISprite sprite = (ISprite) spriteObject; SimpleEntry<Boolean,
	 * GlobalCoarse> pointInMap = isInMap(numCols, numRows,
	 * sprite.getPosition(timeDelay)); if (pointInMap.getKey()) {
	 * PlayerWorldObject generalizedSprite =
	 * praktikumMovingGeneralization(sprite); // if (spriteObject !=
	 * MarioWorldObject.NONE) {
	 * ret[pointInMap.getValue().y][pointInMap.getValue().x] =
	 * generalizedSprite; // } } } return ret; }
	 */

	/*
	 * public PlayerWorldObject[][] getLocalMovingRealWorldObservation(final int
	 * timeDelay) { //TODO: implement for all players GlobalCoarse playerPos =
	 * simulatedLevelScene
	 * .simulatedPlayers.get(playerIndex).getPosition(timeDelay
	 * ).toGlobalCoarse(); // getMario().getPosition().toGlobalCoarse(); return
	 * LevelSceneAdapter
	 * .praktikumLocalMovingObservation(simulatedLevelScene.simulatedSprites,
	 * timeDelay, playerPos); }
	 */

	/**
	 * returns an observation around mario of objects which are able to move
	 * (but might stand still at the moment) similar to enemiesObservation, only
	 * that this method returns an array over MarioWorldObjects instead of over
	 * bytes
	 * 
	 * @return all potentially moving objects around mario
	 */
	/*
	 * protected static PlayerWorldObject[][]
	 * praktikumLocalMovingObservation(final HashSet<SimulatedSprite> sprites,
	 * final int timeDelay, GlobalCoarse playerPosition) { final int numRows =
	 * Environment.HalfObsWidth * 2; final int numCols =
	 * Environment.HalfObsHeight * 2; PlayerWorldObject[][] ret = new
	 * PlayerWorldObject[numRows][numCols]; for (int i = 0; i < numRows; i++) {
	 * for (int j = 0; j < numCols; j++) { ret[i][j] = PlayerWorldObject.NONE; }
	 * } for (Object spriteObject : sprites) { ISprite sprite = (ISprite)
	 * spriteObject; GlobalCoarse spritePos = sprite.getMapPosition(timeDelay);
	 * if (spritePos.x >= 0 && spritePos.x > playerPosition.x -
	 * Environment.HalfObsWidth && spritePos.x < playerPosition.x +
	 * Environment.HalfObsWidth && spritePos.y >= 0 && spritePos.y >
	 * playerPosition.y - Environment.HalfObsHeight && spritePos.y <
	 * playerPosition.y + Environment.HalfObsHeight) { int obsX = spritePos.y -
	 * playerPosition.y + Environment.HalfObsHeight; int obsY = spritePos.x -
	 * playerPosition.x + Environment.HalfObsWidth; ret[obsX][obsY] =
	 * praktikumMovingGeneralization(sprite); } } return ret; }
	 */

	/**
	 * transforms a continuous point into a global discrete point and checks
	 * whether it is in the whole level.
	 * 
	 * @param position
	 *            the point which is used
	 * @return (isInMap, globalDiscretePoint)
	 */
	public static SimpleEntry<Boolean, GlobalCoarse> isInMap(int mapWidth,
			int mapHeight, GlobalContinuous position) {
		GlobalCoarse globalDiscrete = position.toGlobalCoarse();
		return new SimpleEntry<Boolean, GlobalCoarse>(isInMap(mapWidth,
				mapHeight, globalDiscrete), globalDiscrete);
	}

	public boolean isInMap(GlobalCoarse position) {
		return isInMap(getMapWidth(), getMapHeight(), position);
	}

	/**
	 * transforms a continuous point into a global discrete point and checks
	 * whether it is in the whole level.
	 * 
	 * @param position
	 *            the point which is used
	 * @return (isInMap, globalDiscretePoint)
	 */
	protected static boolean isInMap(int mapWidth, int mapHeight,
			GlobalCoarse position) {
		return !(position.x < 0 || position.x >= mapWidth || position.y < 0 || position.y >= mapHeight);
	}

	public static PlayerWorldObject getObject(PlayerWorldObject[][] map,
			GlobalContinuous continuousPosition) {
		GlobalCoarse globalDiscretePos = continuousPosition.toGlobalCoarse();
		return map[globalDiscretePos.y][globalDiscretePos.x];
	}

	public static boolean isBlocking(PlayerWorldObject[][] map,
			GlobalContinuous continuousPosition, float ya) {
		return getObject(map, continuousPosition).isBlocking(ya);
	}

	/**
	 * 
	 * @param map
	 * @param continuousPosition
	 * @param playerPosition
	 *            OPTIONAL, may be used to remove blocking checks if mario is
	 *            inside an element. This should happen only temporarily during
	 *            an interaction. Use null, if you don't want to catch this
	 *            case.
	 * @param ya
	 *            if positive, mario is moving down, in that case this method
	 *            checks, if he can stand on something.
	 * @return [0]: if the cell is occupied at all by any object. [1]: if the
	 *         object is relevant to learning. [2]: if the object is blocking
	 */
	public static boolean[] occupation(PlayerWorldObject[][] map,
			GlobalContinuous objectPosition, GlobalCoarse playerPosition,
			float ya) {
		PlayerWorldObject object = getObject(map, objectPosition);

		if (object == null) {
			return new boolean[] { false, false, false };
		}

		if (object == PlayerWorldObject.UNDEFINED)

			throw new IllegalArgumentException(
					"Undefined PlayerWorldObject at "
							+ objectPosition.toGlobalCoarse().toString());
		if (object == PlayerWorldObject.NONE)
			return new boolean[] { false, false, false };
		boolean blocking;
		if (playerPosition != null
				&& playerPosition.equals(objectPosition.toGlobalCoarse())) {
			blocking = false;
		} else {
			blocking = object.isBlocking(ya);
		}
		//@c2-pavel: ex-Learninghelper
		return new boolean[] { true, object.isRelevantToLearning(), blocking };
	}

	/**
	 * generalizes static objects (i.e. this method hides information which is
	 * either unnecessary or to which mario should have no access)
	 * 
	 * @param objectID
	 * @return
	 */
	// private static PlayerWorldObject
	// praktikumStaticGeneralization(PlayerWorldObject type)
	// {
	// /**
	// * TODO What this method could/should do: generalize over question brick
	// with energy, question brick with wrench ... (This means mario would
	// consider
	// * them to be the same). As is currently implemented, mario CAN
	// distinguish between them, making the concept of question bricks rather
	// useless. However,
	// * this should not be addressed, before learning can handle the
	// uncertainty.
	// */
	// return Generalizer.generalizePlayerWorldObject(type);
	// // return type;
	// }

	/**
	 * generalizes static objects (i.e. this method hides information which is
	 * either unnecessary or to which mario should have no access)
	 * 
	 * @param objectID
	 * @return
	 */
	// private static PlayerWorldObject praktikumStaticGeneralization(byte
	// objectID) {
	// /**
	// * TODO What this method could/should do: generalize over question brick
	// with energy, question brick with wrench ... (This means mario would
	// consider
	// * them to be the same). As is currently implemented, mario CAN
	// distinguish between them, making the concept of question bricks rather
	// useless. However,
	// * this should not be addressed, before learning can handle the
	// uncertainty.
	// */
	// //REMARK: This is done somewhere else. however, this function uses a LOT
	// of memory and provides no functionality.
	// return PlayerWorldObject.getElement(objectID, ObjectType.STATIC, 0);
	// }

	/**
	 * generalizes moving objects (i.e. this method hides information which is
	 * either unnecessary or to which mario should have no access)
	 * 
	 * @param sprite
	 *            the sprite which is converted
	 * @return
	 */
	/*
	 * private static PlayerWorldObject praktikumMovingGeneralization(ISprite
	 * sprite) { PlayerWorldObject type = sprite.getType(); switch (type) { case
	 * ENERGY_ANIM: case PARTICLE: case SPARCLE: case PLAYER: // disregard
	 * animations like sparcles return PlayerWorldObject.NONE; default: return
	 * type; } }
	 */

	// /**
	// * generalizes moving objects (i.e. this method hides information which is
	// * either unnecessary or to which mario should have no access)
	// *
	// * @param sprite
	// * the sprite which is converted
	// * @return
	// */
	// private static MarioWorldObject praktikumMovingGeneralization(Sprite
	// sprite) {
	// byte objectID = sprite.kind;
	// switch (sprite.kind) {
	// case (Sprite.KIND_GRUMPY):
	// // distinguish between moving and still grumpys. The engine uses the
	// // same id for both. We want to use different MarioWorldObjects,
	// // however
	// return sprite.xa != 0 ? MarioWorldObject.GRUMPY_MOVING
	// : MarioWorldObject.GRUMPY_STILL;
	// case (Sprite.KIND_ENERGY_ANIM):
	// case (Sprite.KIND_PARTICLE):
	// case (Sprite.KIND_SPARCLE):
	// case (Sprite.KIND_MARIO):
	// // disregard animations like sparcles
	// objectID = Sprite.KIND_NONE;
	// }
	// return MarioWorldObject.getElement(objectID, ObjectType.NON_STATIC,
	// sprite.xa);
	// }

	// /**
	// * generalizes moving objects (i.e. this method hides information which is
	// * either unnecessary or to which mario should have no access)
	// *
	// * @param sprite
	// * the sprite which is converted
	// * @return
	// */
	// private static MarioWorldObject praktikumMovingGeneralization(
	// SimulatedSprite sprite) {
	// switch (sprite.type) {
	// case ENERGY_ANIM:
	// case PARTICLE:
	// case SPARCLE:
	// case MARIO:
	// // disregard animations like sparcles
	// return MarioWorldObject.NONE;
	// default:
	// return sprite.type;
	// }
	// }

	/**
	 * the map used in LevelScene. This map containts int IDs to specify
	 * objects. Usage is discouraged. Also, this map uses byte[column][row]
	 * instead of standard byte[row][column]. WARNING: all other methods in this
	 * adapter which provide observations return [row][column]
	 * 
	 * @return
	 */
	private byte[][] getMap() {
		return levelScene.level.map;
	}

	public ArrayList<Player> getPlayers() {
		return levelScene.getPlayers();
	}

	/**
	 * GlobalCoarse level size
	 */
	public int getMapWidth() {
		return simulatedLevelScene.mapWidth;
	}

	/**
	 * GlobalCoarse level size
	 */
	public int getMapHeight() {
		return simulatedLevelScene.mapHeight;
	}

	public int getElapsedTimeInFrames() {
		return levelScene.startTime;
	}

	// /**
	// * This function is only used once within updateActionPlan (see below). It
	// is used to determine whether the goal position blocks in the approaching
	// * direction.
	// *
	// * @param goalPosition
	// * @param direction
	// * @param lsa
	// * @return
	// */
	// public boolean isBlockingAgainstDirection(GlobalCoarse position,
	// CollisionDirection direction)
	// {
	// PlayerWorldObject[][] pwo = getGlobalStaticRealWorldObservation();
	//
	// if(position.y > pwo.length - 1 || position.x > pwo[0].length - 1 ||
	// position.x < 0 || position.y < 0)
	// {
	// System.err.println("LevelSceneAdapter::isBlockingAgainstDirection: Array out of bounds!");
	// return true;
	// }
	//
	// /*position.y=Math.max(Math.min(position.y, pwo.length - 1),0);
	// position.x=Math.max(Math.min(position.x, pwo[0].length - 1),0);*/
	//
	// switch
	// (getGlobalStaticRealWorldObservation()[position.y][position.x].blocksPath)
	// {
	// case FULL:
	// return true;
	// case TOP:
	// return direction == CollisionDirection.ABOVE;
	// case BOTTOM:
	// return direction == CollisionDirection.BELOW;
	// case NONE:
	// default:
	// return false;
	//
	// }
	// }

	public LevelScene getClonedLevelScene() {
		return levelScene.cloneForSimulation();
	}

	public static int getTotalLevelWidth() {
		return totalLevelWidth;
	}

	public static int getTotalLevelHeight() {
		return totalLevelHeight;
	}

	public static void deleteStaticAttributes() {
		totalLevelWidth = -1;
		totalLevelHeight = -1;

	}

	public static void resetStaticAttributes() {
		totalLevelWidth = -1;
		totalLevelHeight = -1;
	}

}
