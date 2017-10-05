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

import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import marioAI.brain.Effect;
import marioAI.brain.Generalizer;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.brain.simulation.sprites.SimulatedSpawn;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.Goal;
import marioAI.movement.PlayerActions;
import marioAI.movement.ReachabilityNode;
import marioAI.movement.WorldNode;
import marioAI.movement.heuristics.FineGrainPotentialField;
import marioAI.util.EqualsUtil;
import marioAI.util.GeneralUtil;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.ILevelScene;
import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.BlockingType;
import marioWorld.engine.PlayerWorldObject.ObjectType;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeCoarse;
import marioWorld.engine.level.SpriteTemplate;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;
import marioWorld.mario.environments.Environment;

/**
 * Clones the map, sprites & mario. The cloned mario is also simpler than the
 * mario used by the game engine. Uses a forward model to predict movements (and
 * movements only). This means collisions are included in the forward model only
 * as far as positions and velocities are regarded. The forward model does NOT
 * simulate any creations/destructions of objects or mario's health changes.
 * 
 * @author Stephan, based on: Ylva, Stefan M., modified by Jannine, Matthias,
 *         Leona, Daniel
 * 
 */
public class SimulatedLevelScene implements ILevelScene {
	/** position of the camera in global continuous coordinates */
	private float xCam, yCam;
	/** Global Course level size */
	protected final int mapHeight;
	protected final int mapWidth;
	// package visibility
	/** the global map of static objects (cloned in constructor) */
	private final PlayerWorldObject[][] staticGlobalMap; // ATTENTION: MATRIX
															// ENCODING IS
															// [y][x]
	private PlayerWorldObject[][] staticLocalMap; // ATTENTION: MATRIX ENCODING
													// IS [y][x]
		

	//public ReachabilityMap Rmap;

	/** the map of moving objects */
	final HashSet<SimulatedSprite> simulatedSprites;
	final HashSet<SimulatedSprite> simulatedSpritesToAdd;
	// final List<SimulatedSprite> simulatedSprites;

	/** a simplified version of mario ("cloned" in constructor) */
	ArrayList<SimulatedPlayer> simulatedPlayers = new ArrayList<SimulatedPlayer>();
	/** just in case simulated effects will be implemented later on */
	ArrayList<Integer> playerHealthsBeforeSimulation = new ArrayList<Integer>();

	final boolean paused;
	public int elapsedTime;
	/**
	 * All collisions which occured in the last time step. Generally, this
	 * should be zero or one. In some cases when mario moves diagonally,
	 * however, horizontal and vertical collisions can occur in one time step,
	 * or even collisions with multiple objects. This list covers all of them
	 */
	private List<CollisionAtPosition> collisionsInLastTick = null;
	/**
	 * Use this instead of the fullList collisionsInLastTick, if you're only
	 * interested in those characteristics, which are equal in all collisions.
	 * Unequal characteristics (e.g. direction or target) are set to UNDEFINED
	 * The idea is, that mario can not distinguish which object triggered an
	 * effect if multiple are involved in one time step
	 */
	private CollisionAtPosition singleObservedCollisionInLastTick = null;
	/** Currently only 0 or 1 */
	private double knowlegeIncreaseInLastTimeStep;
	/**
	 * Required only for debugging. Used to match engine sprites with simulated
	 * sprites - we assume mario can track them, so we don't want to code a
	 * tracking algorithm here and simply remember the matching.
	 */
	private final HashMap<SimulatedSprite, Sprite> enemyTracking;

	/** ensures, that the the constructor and clone() can't run in parallel */

	public final ReentrantLock lock;

	private final TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge;

	public static double anticipationThreshold = 0.1; // TEST
	// we use stochastic sampling now

	/** list of possible Spawns for the simulation */
	private HashSet<SimulatedSpawn> simulatedSpawns;
	private int planningPlayerIndex;

	public int getPlanningPlayerIndex() 
	{
		return planningPlayerIndex;
	}
	
	public ArrayList<ReachabilityNode> reachabilityNodes;

	/**
	 * This constructor creates a simplified backup of all sprites and mario.
	 * Later, the forwardModel() can be called to convert the backup into a
	 * simulated scene one time step along.
	 * 
	 * @param levelSceneAdapter
	 * @param lastSimulated
	 *            TODO: is this really necessary?
	 */
	public SimulatedLevelScene(ArrayList<SimulatedPlayer> set_simulatedPlayers,
			HashSet<SimulatedSprite> simulatedSprites,
			HashSet<SimulatedSprite> simulatedSpritesToAdd,
			HashMap<SimulatedSprite, Sprite> enemyTracking,
			PlayerWorldObject[][] staticGlobalMap,
			PlayerWorldObject[][] staticLocalMap, int mapHeight, int mapWidth,
			final boolean paused, final int elapsedTime, ReentrantLock lock,
			TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge,
			SpriteTemplate[][] spriteTemplate, int playerIndex) 
	{
		lock.lock();
		try {
			this.simulatedPlayers = set_simulatedPlayers;
			this.simulatedSprites = simulatedSprites;
			this.simulatedSpritesToAdd = simulatedSpritesToAdd;
			this.enemyTracking = enemyTracking;
			this.staticGlobalMap = staticGlobalMap;
			this.staticLocalMap = staticLocalMap;
			for (SimulatedPlayer simulatedPlayer : simulatedPlayers)
				this.playerHealthsBeforeSimulation.add(simulatedPlayer
						.getHealth());
			this.mapHeight = mapHeight;
			this.mapWidth = mapWidth;
			this.paused = paused;
			this.elapsedTime = elapsedTime;
			this.lock = lock;
			this.knowledge = knowledge;
			this.planningPlayerIndex = playerIndex;

			updateXCam();

		} finally {
			lock.unlock();
		}
	}

	
	protected SimulatedLevelScene
	(
			SimulatedLevelScene other
	) 
	{
		
		//TODO: is this lock still needed???
		this.lock = other.lock;
		lock.lock();
		try 
		{
			ArrayList<SimulatedPlayer> simulatedPlayersClone = new ArrayList<SimulatedPlayer>();
			
			//mapping from old to new simplayer
			HashMap <SimulatedPlayer,SimulatedPlayer> psmap = new HashMap <SimulatedPlayer,SimulatedPlayer>();
			
			int j=0;
			for (SimulatedPlayer othersimplayer : other.getSimulatedPlayers())
			{
				simulatedPlayersClone.add(othersimplayer.clone());
				psmap.put(othersimplayer, simulatedPlayersClone.get(j));
				j++;
			}
						
			//set the carry flags
			for(int i=0;i<simulatedPlayersClone.size();i++)
			{
				simulatedPlayersClone.get(i).setCarries(psmap.get(other.getSimulatedPlayers().get(i).getCarries()));
				simulatedPlayersClone.get(i).setCarriedBy(psmap.get(other.getSimulatedPlayers().get(i).getCarriedBy()));
			}			
			
			this.simulatedPlayers=simulatedPlayersClone;
			
//			for(int i=0;i<simulatedPlayers.size();i++)
//			{
//				System.out.println(simulatedPlayers.get(i).getCarries());
//				System.out.println(other.getSimulatedPlayers().get(i).getCarries());
//				System.out.println(simulatedPlayers.get(i).getCarriedBy());				
//				System.out.println(other.getSimulatedPlayers().get(i).getCarriedBy());
//			}
//			System.out.println("");

			
			HashSet<SimulatedSprite> simulatedSprites = new HashSet<SimulatedSprite>();
			HashSet<SimulatedSprite> simulatedSpritesToAdd = new HashSet<SimulatedSprite>();
			HashMap<SimulatedSprite, Sprite> enemyTracking = new HashMap<SimulatedSprite, Sprite>();			
			
			for (SimulatedSprite simSprite : other.simulatedSprites) 
			{
				SimulatedSprite clonedSprite = (SimulatedSprite) simSprite.clone();
				simulatedSprites.add(clonedSprite);
				enemyTracking.put(clonedSprite,other.enemyTracking.get(simSprite));
			}
			
			for (SimulatedSprite simSprite : other.simulatedSpritesToAdd) 
			{
				SimulatedSprite clonedSprite = (SimulatedSprite) simSprite.clone();
				simulatedSpritesToAdd.add(clonedSprite);
				enemyTracking.put(clonedSprite,other.enemyTracking.get(simSprite));
			}
			
			this.simulatedSprites=simulatedSprites;
			this.simulatedSpritesToAdd=simulatedSpritesToAdd;
			this.enemyTracking=enemyTracking;			
			
			PlayerWorldObject[][] staticGlobalMap = GeneralUtil.clone2DPlayerWorldObjectArray(other.getStaticGlobalMap());
			PlayerWorldObject[][] staticLocalMap = GeneralUtil.clone2DPlayerWorldObjectArray(other.staticLocalMap);
			this.staticGlobalMap=staticGlobalMap;
			this.staticLocalMap=staticLocalMap;
					
			this.mapHeight = other.mapHeight;
			this.mapWidth = other.mapWidth;
			this.paused = other.paused;
			this.elapsedTime = other.elapsedTime;			
			this.knowledge = other.knowledge;
			this.simulatedSpawns = other.simulatedSpawns;
			this.planningPlayerIndex = other.planningPlayerIndex;
			
			//don't clone or recreate! not necessary when none of the objects changed...
			//this.Rmap=other.Rmap;
			this.reachabilityNodes = other.reachabilityNodes;
			
			updateXCam();
		}
		finally
		{
			lock.unlock();
		}
	}
	
	// for ReachabilityMap only!
	public SimulatedLevelScene
	(
			SimulatedLevelScene other, Boolean Rmap
	) 
	{
		
		//TODO: is this lock still needed???
		this.lock = other.lock;
		lock.lock();
		try 
		{
			ArrayList<SimulatedPlayer> simulatedPlayersClone = new ArrayList<SimulatedPlayer>();
			
			//mapping from old to new simplayer
			HashMap <SimulatedPlayer,SimulatedPlayer> psmap = new HashMap <SimulatedPlayer,SimulatedPlayer>();
			
			int j=0;
			for (SimulatedPlayer othersimplayer : other.getSimulatedPlayers())
			{
				simulatedPlayersClone.add(new SimulatedPlayer(othersimplayer));
				psmap.put(othersimplayer, simulatedPlayersClone.get(j));
				j++;
			}
						
			//set the carry flags
			for(int i=0;i<simulatedPlayersClone.size();i++)
			{
				simulatedPlayersClone.get(i).setCarries(psmap.get(other.getSimulatedPlayers().get(i).getCarries()));
				simulatedPlayersClone.get(i).setCarriedBy(psmap.get(other.getSimulatedPlayers().get(i).getCarriedBy()));
			}			
			
			this.simulatedPlayers=simulatedPlayersClone;

			HashSet<SimulatedSprite> simulatedSprites = new HashSet<SimulatedSprite>();
			HashSet<SimulatedSprite> simulatedSpritesToAdd = new HashSet<SimulatedSprite>();
			HashMap<SimulatedSprite, Sprite> enemyTracking = new HashMap<SimulatedSprite, Sprite>();			
			
			for (SimulatedSprite simSprite : other.simulatedSprites) 
			{
				SimulatedSprite clonedSprite = new SimulatedSprite(simSprite.getOriginalSprite());
				simulatedSprites.add(clonedSprite);
				enemyTracking.put(clonedSprite,other.enemyTracking.get(simSprite));
			}
			
			for (SimulatedSprite simSprite : other.simulatedSpritesToAdd) 
			{
				SimulatedSprite clonedSprite =  new SimulatedSprite(simSprite.getOriginalSprite());
				simulatedSpritesToAdd.add(clonedSprite);
				enemyTracking.put(clonedSprite,other.enemyTracking.get(simSprite));
			}
			
			this.simulatedSprites=simulatedSprites;
			this.simulatedSpritesToAdd=simulatedSpritesToAdd;
			this.enemyTracking=enemyTracking;			
			
			PlayerWorldObject[][] staticGlobalMap = GeneralUtil.clone2DPlayerWorldObjectArray(other.getStaticGlobalMap());
			PlayerWorldObject[][] staticLocalMap = GeneralUtil.clone2DPlayerWorldObjectArray(other.staticLocalMap);
			this.staticGlobalMap=staticGlobalMap;
			this.staticLocalMap=staticLocalMap;
					
			this.mapHeight = other.mapHeight;
			this.mapWidth = other.mapWidth;
			this.paused = other.paused;
			this.elapsedTime = other.elapsedTime;			
			this.knowledge = other.knowledge;
			this.simulatedSpawns = other.simulatedSpawns;
			this.planningPlayerIndex = other.planningPlayerIndex;
			this.reachabilityNodes = other.reachabilityNodes;
			//don't clone or recreate! not necessary when none of the objects changed...
			//this.Rmap=other.Rmap;
			
			
			updateXCam();
		}
		finally
		{
			lock.unlock();
		}
	}

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
		boolean isInMap = !(globalDiscrete.x < 0
				|| globalDiscrete.x >= mapWidth || globalDiscrete.y < 0 || globalDiscrete.y >= mapHeight);
		return new SimpleEntry<Boolean, GlobalCoarse>(isInMap, globalDiscrete);
	}

	/**
	 * compare the simulated mario to the real mario
	 * 
	 * TODO: this function does not seem to make sense.
	 * 
	 * @return
	 */
	public boolean compareWithEngine(final ArrayList<Player> enginePlayers,
			final byte[][] engineStaticMap) {
		boolean ret = true;
		int elapsedTime = this.elapsedTime + 1;
		final String s = "\ntime " + elapsedTime + ": ";
		ArrayList<String> ignoredFields = new ArrayList<String>();
		ignoredFields.add("keys");
		ignoredFields.add("invulnerableTime");
		ignoredFields.add("carried");
		ignoredFields.add("carriedBy");

		/** compare mario */

		HashMap<Field, Object> simulated;
		HashMap<Field, Object> engine;

		String player = "Player";

		for (int i = 0; i < this.simulatedPlayers.size(); i++) {
			if (i == 1) {
				player = "Jay"; // TODO: WTH!??
			}

			simulated = this.simulatedPlayers.get(i).getAttributeMap();
			engine = enginePlayers.get(i).getAttributeMap();

			if (!EqualsUtil.equal(s, player, "Simulated", "engine", simulated,
					engine, "SimulationError", Level.SEVERE, ignoredFields)) {
				//System.out.println("Eieieieiei, simulation error!\n");
				ret = false;
			}

		}

		/** compare sprites */
		for (Entry<SimulatedSprite, Sprite> entry : this.enemyTracking
				.entrySet()) {
			ignoredFields.clear();
			ignoredFields.add("type");
			ignoredFields.add("winged");
			// if(entry.getKey().type == MarioWorldObject.GRUMPY_MOVING ||
			// entry.getKey().type == MarioWorldObject.GRUMPY_STILL)
			// ignoredFields.add("facing");
			Sprite engineSprite = entry.getValue();
			if (LevelSceneAdapter
					.isInMap(
							engineStaticMap.length,
							engineStaticMap[0].length,
							new GlobalContinuous(engineSprite.x, engineSprite.y))
					.getKey().booleanValue()) {
				//@c2-pavel: ex-Learninghelper
				if ((entry.getKey().type).isRelevantToLearning()) {
					/**
					 * compare a simulated sprite to its respective engine
					 * sprite
					 */
					simulated = entry.getKey().getAttributeMap();
					engine = entry.getValue().getAttributeMap();
					for (Field field : simulated.keySet()) {
						ignoreField(field.getName(), entry.getKey().type,
								ignoredFields);
					}
					if (!EqualsUtil.equal(s, entry.getKey().type.name(),
							"Simulated", "engine", simulated, engine,
							"SimulationError", Level.SEVERE, ignoredFields)) {
						ret = false;
					}
				}
			}
		}
		ignoredFields.clear();

		// Commented out because this detects all the object interactions and
		// floods the console.
		// Fixing this isn't easy, because a distinction between object
		// interactions and simulation errors would need to be done here.
		/** compare static maps */
		// MarioWorldObject[][] simulatedMap = this.staticGlobalMap;
		// byte[][] engineMap = engineStaticMap;
		// for(int i=0; i<simulatedMap.length; i++) {
		// for(int j=0; j<simulatedMap[i].length; j++) {
		// /**compare a cell in the simulated map to its respective cell in the
		// engine map*/
		// if(simulatedMap[i][j] != MarioWorldObject.getElement(engineMap[j][i],
		// ObjectType.STATIC, 0)) {
		// String difference =
		// "different static maps at ["+i+"]["+j+"]. Simulated: "+simulatedMap[i][j]+", engine: "+engineMap[i][j];
		// Logging.log("SimulationError", s+difference, Level.SEVERE);
		// ret = false;
		// }
		// }
		// }
		if (ret == false) {
			// Logging.logStackTrace("SimulationError");
		}
		return ret;
	}

	/*
	 * polymorphic function
	 * is used during planning
	 * */
//	public List<CollisionAtPosition> tick(boolean[] keys) 
//	{
//		ArrayList<boolean[]> allKeys = new ArrayList<boolean[]>();
//
//		//add empty keys for all players
//		for(int i=0;i<simulatedPlayers.size();i++)
//		{
//			allKeys.add(new boolean[Environment.numberOfButtons]);
//		}
//		
//		//set the keys of the acting player in simulation (not necessarily the planning player)
//		allKeys.set(actorIndex,keys);
//		
//		return tick(allKeys);
//	}
	
	/**
	 * forward model. simulates a tick instead of letting it compute through the
	 * game engine, without computing the interactions
	 */
	@Override
	public List<CollisionAtPosition> tick(PlayerActions action)
	{			
//		if(!action.isEmpty())
//		{
//			System.out.println(this.getPlanningPlayer().getType() + " got keys!");
//		}
				
		updateXCam();
		elapsedTime++;
		ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();
		PlayerWorldObject[][] map = this.getStaticGlobalMap();

		/** Spawn and remove sprites */
		if (simulatedSpawns != null) {
			for (SimulatedSpawn spawn : simulatedSpawns) 
			{
				if (paused) 
				{

				} 
				else 
				{
					spawn.spawnSprite(simulatedSprites, getPlanningPlayerPos(), xCam, yCam);
					spawn.removeSprite(simulatedSprites, xCam, yCam);
				}
			}
		}

		/** Move sprites (this excludes mario) */
		for (SimulatedSprite sprite : simulatedSprites) 
		{
			if (paused) 
			{
				sprite.tickNoMove();
			} 
			else 				
			{
				if(sprite.getType().isPlayer())
					System.out.println("WARNING: dont tick players without keys in SimulatedLevelScene::tick");
				
				sprite.tick(map, new boolean[8], this);	//no keys for sprites...
			}
		}

		
		/** move all players and remember collisions */
		int i=0;
		for (SimulatedPlayer player : simulatedPlayers) 
		{					
			player.keys=action.getKeyStates().get(i).getKeyState();	//TODO: dirty??
			ret.addAll(player.tick(map, action.getKeyStates().get(i).getKeyState(),this));
			i++;			
		}
		
		
		/**
		 * execute changes to movement through interactions from above &
		 * remember the collision
		 */
//		for (SimulatedSprite sprite : simulatedSprites) 
//		{
//			List<CollisionAtPosition> collisionWithMovingObjectsFromAbove = simulatedPlayers.get(planningPlayerIndex).collideCheck(sprite, map, action.getKeyStates().get(planningPlayerIndex).getKeyState());
//			
//			if (collisionWithMovingObjectsFromAbove != null)
//			{
//				ret.addAll(collisionWithMovingObjectsFromAbove);
//			}
//		}
		
		/**
		 * execute changes to movement through interactions from above &
		 * remember the collision
		 */
		for (SimulatedPlayer sprite1 : simulatedPlayers) 
		{
			for (SimulatedPlayer sprite2 : simulatedPlayers) 
			{
				if(sprite1.getType()==sprite2.getType())
					continue;
				
				List<CollisionAtPosition> collisionWithMovingObjectsFromAbove = sprite1.collideCheck
					(
							sprite2,
							map
					);
				
				if (collisionWithMovingObjectsFromAbove != null)
				{
					ret.addAll(collisionWithMovingObjectsFromAbove);
				}
			}
		}
		
//		/**
//		 * execute changes to movement from other directions and remember
//		 * collision between mario and sprites
//		 */
		ArrayList<CollisionAtPosition> collisionsWithMovingObjects = GeneralUtil
				.doCollisionWithMovingObjects(simulatedSprites,
						simulatedPlayers.get(planningPlayerIndex));
		ret.addAll(collisionsWithMovingObjects);
		


		/**
		 * include known effects in the simulation
		 */
		simulateCollisionList(ret,false);		
		
		
		/** remember Mario's surrounding */
		this.staticLocalMap = LevelSceneAdapter
				.createLocalObservationFromGlobalObservation(simulatedPlayers
						.get(planningPlayerIndex).getMapPosition(0),
						getStaticGlobalMap(), getStaticGlobalMap().length);

		this.simulatedSprites.addAll(simulatedSpritesToAdd);
		this.simulatedSpritesToAdd.clear();
		this.collisionsInLastTick = ret;
		return ret;
	}

	/**
	 * Anticipates and simulates the effects of the collisions based on the
	 * current knowledge.
	 * 
	 * @param collisions
	 */
	private TreeMap<Effect, Double> simulateCollisionList(ArrayList<CollisionAtPosition> collisions, boolean stochastic) 
	{
		TreeMap<Effect, Double> ret = new TreeMap<Effect, Double>();
		
		// anticipate the effects for each collision
		for (CollisionAtPosition collision : collisions)
		{
			//System.out.println("SIMULATING collision at position " + collision);
			
			TreeMap<Effect, Double> knownEffects = knowledge.get(collision.conditionDirectionPair);
			
			if (knownEffects != null) 
			{
				// destructions must be simulated first to make sure a new
				// object is not overridden by NONE
				for (Entry<Effect, Double> entry : knownEffects.entrySet()) 
				{
					// simulate effects that are likely to happen
					if ((!stochastic && entry.getValue() > anticipationThreshold) || (stochastic && Math.random() < entry.getValue()))
					//if(Math.random() <= entry.getValue()); 
					{
						if (entry.getKey().getType() == ActionEffect.OBJECT_DESTRUCTION)
						{							
							// include effect in simulation
							simulateEffect(collision, entry.getKey());
							ret.put(entry.getKey(), entry.getValue());
						}
					}
				}
				
				// now the other effects can be simulated
				for (Entry<Effect, Double> entry : knownEffects.entrySet()) 
				{
					// simulate effects that are likely to happen					
					if ((!stochastic && entry.getValue() > anticipationThreshold) || (stochastic && Math.random() < entry.getValue()))
					//if(Math.random() <= entry.getValue()); 
					{
						if (entry.getKey().getType() != ActionEffect.OBJECT_DESTRUCTION)
						{
							//System.out.println("SIM " + collision + entry.getKey().getType());
							
							// include effect in simulation
							simulateEffect(collision, entry.getKey());
							ret.put(entry.getKey(), entry.getValue());
						}
					}
				}
			}
			/*else	//if nothing is known, anticipate "NOTHING HAPPENS"
			{
				simulateEffect(collision, new Effect(ActionEffect.NOTHING_HAPPENS, collision.collision.condition.getTarget()));
			}*/			
		}
		
		return ret;
	}
	/*
	public ReachabilityMap getReachabilityMap()
	{
		return Rmap;
	}
	
	public void recomputeReachabilityMap()
	{
		this.Rmap=computeReachabilityMap(5);
	}
	
	private ReachabilityMap computeReachabilityMap(int recursions)
	{
		Rmap = new ReachabilityMap(this,getStaticGlobalMap(),recursions);
		return Rmap;
	}	*/
	
	
	public CollisionDirection getEasilyReachableDirection(GlobalCoarse position)
	{
		for(CollisionDirection direction : CollisionDirection.values())
		{
			if(isEasilyReachable(position, direction) && direction!=CollisionDirection.IRRELEVANT)
				return direction;
		}
		return CollisionDirection.IRRELEVANT;
	} 
	
	/**
	 * checks if three tiles next to the given position (depending on the direction we are approaching the object from)are blocked or not
	 * 
	 * @param Goal
	 *            position
	 * @param direction
	 * @return boolean
	 */	
	public boolean isEasilyReachable(GlobalCoarse position, CollisionDirection direction) 
	{	
		boolean result = false;
		for(CollisionAtPosition cap : reachabilityNodes.get(planningPlayerIndex).uniqueFoundCollisions) {
			if( cap.getDirection().equals(direction) && cap.getPosition().equals(position)) {
				//result =  true;
				return true;
			}
		}
		return result;

//		GlobalCoarse orposition = position;
//		switch(direction)
//		{
//		case LEFT:
//			return Rmap.isReachable(orposition.y,orposition.x-1);
//			
//		case RIGHT:
//			return Rmap.isReachable(orposition.y,orposition.x+1);
//		case ABOVE:
//			return Rmap.isReachable(orposition.y-1,orposition.x);
//		case BELOW:
//			return Rmap.isReachable(orposition.y+1,orposition.x);
//		case IRRELEVANT:
//			return (Rmap.isReachable(orposition.y,orposition.x-1) ||
//					Rmap.isReachable(orposition.y,orposition.x+1) || 
//					Rmap.isReachable(orposition.y-1,orposition.x) ||
//					Rmap.isReachable(orposition.y+1,orposition.x) ||
//					Rmap.isReachable(orposition.y,orposition.x));
//		default:
//			return Rmap.isReachable(orposition.y,orposition.x);
//		}	
		
//		PlayerWorldObject[][] observation = this.getGlobalStaticSimulation();
//		int x = position.getX();
//		int y = position.getY();
//		float yVelocity;
//				
//		switch (direction) 
//		{
//		case ABOVE:
//			yVelocity = 1;
//			if(
//					   safeIsBlocking(observation,y-1,x,yVelocity)		//observation[y - 1][x].isBlocking(yVelocity)
//					|| safeIsBlocking(observation,y-2,x,yVelocity)		//observation[y - 2][x].isBlocking(yVelocity)
//			  ) 
//			{
//				return false;
//			}
//			break;
//		case LEFT:
//			yVelocity = 0;
//			if(
//					  (safeIsBlocking(observation,y-1,x-1,yVelocity) && safeIsBlocking(observation,y+1,x-1,yVelocity))
//					|| safeIsBlocking(observation,y,x-1,yVelocity)		//observation[y][x - 1].isBlocking(yVelocity)
//			  ) 
//			{
//				return false;
//			}
//			break;
//		case RIGHT:
//			yVelocity = 0;
//			if(
//					  (safeIsBlocking(observation,y-1,x+1,yVelocity) && safeIsBlocking(observation,y+1,x+1,yVelocity))
//					|| safeIsBlocking(observation,y,x+1,yVelocity)		//observation[y][x + 1].isBlocking(yVelocity)
//			  ) 
//			{				
//				return false;
//			}
//			break;
//		case BELOW:
//			yVelocity = -1;
//			if(
//					   safeIsBlocking(observation,y+1,x,yVelocity)	//observation[y + 1][x].isBlocking(yVelocity)
//					|| safeIsBlocking(observation,y+2,x,yVelocity)	//observation[y + 2][x].isBlocking(yVelocity)
//			  ) 
//			{
//				return false;
//			}
//			break;
//		default:
//			return isEasilyReachable(position);
//		}
//
//		return true;
	}		

	public TreeMap<Effect, Double> simulateCollision(CollisionAtPosition collision, boolean stochastic) 
	{
		ArrayList<CollisionAtPosition> list = new ArrayList<CollisionAtPosition>();
		list.add(collision);
		return this.simulateCollisionList(list, stochastic);
	}

	/**
	 * Simulates a single effect.
	 * 
	 * @param collisionAtPosition
	 * @param effect
	 */
	private void simulateEffect(CollisionAtPosition collisionAtPosition, Effect effect) 
	{
		switch (effect.getType()) {
		case MOUNT:
			
			// get the player, on which can be mounted
			SimulatedSprite PlayerToMount = getSpriteBasedOnType(collisionAtPosition.getTargetSprite().type);
			// get the player, which will mount on another
			SimulatedSprite mountPlayer = getSpriteBasedOnType(collisionAtPosition.getActorSprite().type);
			if(checkInteractionPreconditions(PlayerToMount,mountPlayer,effect)){
			
				PlayerToMount.setCarries(mountPlayer);
				mountPlayer.setCarriedBy(PlayerToMount);
				this.reachabilityNodes = updateReachabilityMaps(this);
			}
			//recomputeReachabilityMap();
			break;
		case UNMOUNT:
			SimulatedSprite PlayerToUnmount = getSpriteBasedOnType(collisionAtPosition.getTargetSprite().type);
			SimulatedSprite unmountPlayer = getSpriteBasedOnType(collisionAtPosition.getActorSprite().type);
			if(checkInteractionPreconditions(PlayerToUnmount,unmountPlayer,effect)){
				PlayerToUnmount.setCarries(null);
				unmountPlayer.setCarriedBy(null);
				//expandReachabilityMap(this.Rmap);
				this.reachabilityNodes = updateReachabilityMaps(this);
			}
			break;
		case CARRY:
			// get the player, which can be carried
			
			SimulatedSprite PlayerToCarry= getSpriteBasedOnType(collisionAtPosition.getTargetSprite().type);
			// get the player, which will carry another
			SimulatedSprite carryPlayer = getSpriteBasedOnType(collisionAtPosition.getActorSprite().type);
			
			if(checkInteractionPreconditions(PlayerToCarry,carryPlayer,effect)){
				PlayerToCarry.setCarriedBy(carryPlayer);
				carryPlayer.setCarries(PlayerToCarry);
				//expandReachabilityMap(this.Rmap);
				this.reachabilityNodes = updateReachabilityMaps(this);
			}
			break;
		case DROP:
			SimulatedSprite PlayerToDrop = getSpriteBasedOnType(collisionAtPosition.getTargetSprite().type);
			SimulatedSprite dropPlayer = getSpriteBasedOnType(collisionAtPosition.getActorSprite().type);
			if(checkInteractionPreconditions(PlayerToDrop,dropPlayer,effect)){
				PlayerToDrop.setCarriedBy(null);
				dropPlayer.setCarries(null);
				//expandReachabilityMap(this.Rmap);
				this.reachabilityNodes = updateReachabilityMaps(this);
			}
			break;
		case ENERGY_INCREASE:
			// increase the number of gathered energys by 1
			simulatedPlayers.get(planningPlayerIndex).gatherEnergy(); //
			this.reachabilityNodes = updateReachabilityMaps(this);
			break;
		case ENERGY_DECREASE:
			// decrease the number of gathered energys by 1
			simulatedPlayers.get(planningPlayerIndex).loseEnergy();
			break;
		case OBJECT_DESTRUCTION:
			if (effect.getEffectTarget().isStatic) // is the EFFECT TARGET static?
												// (actually, this is
												// necessarily the thing we
												// collide with since we do not
												// encode effect positions)
			{				
				GlobalCoarse staticPos = collisionAtPosition
						.getPosition();

				// TODO: not quite sure why this happens...
				if (staticPos == null
						|| !staticPos.isWithinObservation(this.getPlanningPlayerPos()))
					break;

				// Special Case for Door: Simulate 2 destructions
				if(effect.getEffectTarget() == PlayerWorldObject.BOTTOM_LEFT_DOOR
						|| effect.getEffectTarget() == PlayerWorldObject.BOTTOM_RIGHT_DOOR)
				{
					getStaticGlobalMap()[staticPos.y][staticPos.x] = PlayerWorldObject.NONE;
					getStaticGlobalMap()[staticPos.y-1][staticPos.x] = PlayerWorldObject.NONE;
				} else if(effect.getEffectTarget() == PlayerWorldObject.QUESTIONMARK_BLOCK1) {
					getStaticGlobalMap()[staticPos.y][staticPos.x] = PlayerWorldObject.IRON_BLOCK1;
				}
				// remove the object from the map of static objects
				else 
				{
					getStaticGlobalMap()[staticPos.y][staticPos.x] = PlayerWorldObject.NONE;
				}
				
				//recomputeReachabilityMap();				
			} else {
				// System.out.println("SPRITE DESTRUCTION SIMULATED!!!");

				// a sprite is destroyed
				// remove the sprite from the map of moving objects
				SimulatedSprite sprite = collisionAtPosition.getTargetSprite();
				simulatedSprites.remove(sprite);
				
			}
			//recomputeReachabilityMap();
			//Rmap.updateReachabilityMap(this, this.getPlanningPlayerIndex(), 2);
			this.reachabilityNodes = updateReachabilityMaps(this);
			break;
		case OBJECT_HEALTH_DECREASE:
			// decrease the health of the simulated Mario by 1
			simulatedPlayers.get(planningPlayerIndex).takeDamage();
			break;
		case OBJECT_HEALTH_INCREASE:
			// increase the health of the simulated Mario by 1
			simulatedPlayers.get(planningPlayerIndex).heal(collisionAtPosition.getTargetSprite().getType().equals(PlayerWorldObject.WRENCH)? Player.healthLevelScaling : Player.healthLevelScaling * 2);
			break;
		case OBJECT_CREATION:
			if (effect.getEffectTarget().isStatic) // is the EFFECT TARGET static?
												// (basically NOT the thing we
												// collide with)
			{
				// a static object is created at the collision position
				GlobalCoarse pos = collisionAtPosition.getPosition();
				getStaticGlobalMap()[pos.y][pos.x] = effect.getEffectTarget();
			} else {
				// System.out.println("SPRITE CREATION SIMULATED!!!");

				// a sprite is created above the position
				GlobalCoarse pos = collisionAtPosition.getPosition()
						.clone();
				pos.y--;
				this.simulatedSprites.add(effect.getEffectTarget()
						.createSimulatedSprite(null, pos.toGlobalContinuous(),
								1, false));

				// staticGlobalMap[pos.y-1][pos.x] = effect.getTarget();
				// //NECESSARY!?
			}
			//expandReachabilityMap(this.Rmap);
			this.reachabilityNodes = updateReachabilityMaps(this);
			break;
		case NOTHING_HAPPENS:
		case UNDEFINED:
		case OBJECT_SPEED_CHANGE:
		case OBJECT_STABILITY:
			break;
		default:
			throw new IllegalArgumentException("Unknown enum "
					+ effect.getType());
		}
	}

	// public void setCollisionsInLastTick(List<CollisionAtPosition>
	// collisionsInLastTick) {
	// this.collisionsInLastTick = collisionsInLastTick;
	// }
	/**
	 * 
	 * Checks if the planned interaction is even possible based on the carry flags.
	 * e.g. cant mount someone that already carrys someone
	 * 
	 * @param targetSprite
	 * @param actorSprite
	 * @param effect
	 * @return
	 */
	private boolean checkInteractionPreconditions(SimulatedSprite targetSprite, SimulatedSprite actorSprite, Effect effect) {
		switch (effect.getType()) {
			case MOUNT:
				if(targetSprite.getCarries() != null) {
					return false;
				}
				break;
			case UNMOUNT:
				if(targetSprite.getCarries() == null) {
					return false;
				}
				break;
			case CARRY:
				if(actorSprite.getCarries() != null) {
					return false;
				}
				break;
			case DROP:
				if(actorSprite.getCarries() == null) {
					return false;
				}
				break;
			default:
				return true;
		
		}
		return true;
	}


	/**
	 * All collisions which occured in the last time step. Generally, this
	 * should be zero or one. In some cases when mario moves diagonally,
	 * however, horizontal and vertical collisions can occur in one time step,
	 * or even collisions with multiple objects. This list covers all of them
	 */
	public List<CollisionAtPosition> getCollisionsInLastTick() {
		return collisionsInLastTick;
	}

	public void setSingleObservedCollisionInLastTick(
			CollisionAtPosition singleObservedCollisionInLastTick) {
		this.singleObservedCollisionInLastTick = singleObservedCollisionInLastTick;
	}

	/**
	 * Use this instead of the fullList getCollisionsInLastTick(), if you're
	 * only interested in those characteristics, which are equal in all
	 * collisions. Unequal characteristics (e.g. direction or target) are set to
	 * UNDEFINED The idea is, that mario can not distinguish which object
	 * triggered an effect if multiple are involved in one time step
	 */
	public CollisionAtPosition getSingleObservedCollisionInLastTick() {
		return singleObservedCollisionInLastTick;
	}

	public void setKnowlegeIncreaseInLastTimeStep(
			double knowlegeIncreaseInLastTimeStep) {
		this.knowlegeIncreaseInLastTimeStep = knowlegeIncreaseInLastTimeStep;
	}

	/** Approximately between 0 and 4? */
	public double getKnowlegeIncreaseInLastTimeStep() {
		return knowlegeIncreaseInLastTimeStep;
	}

	
	public Object clone() throws CloneNotSupportedException
	{
//		lock.lock();
//		try 
//		{
//			ArrayList<SimulatedPlayer> simulatedPlayersClone = new ArrayList<SimulatedPlayer>();
//			
//			for (SimulatedPlayer simulatedPlayer : simulatedPlayers)
//				simulatedPlayersClone.add(simulatedPlayer.clone());
//
//			HashSet<SimulatedSprite> simulatedSprites = new HashSet<SimulatedSprite>();
//			HashSet<SimulatedSprite> simulatedSpritesToAdd = new HashSet<SimulatedSprite>();
//			HashMap<SimulatedSprite, Sprite> enemyTracking = new HashMap<SimulatedSprite, Sprite>();
//			
//			for (SimulatedSprite simSprite : this.simulatedSprites) 
//			{
//				SimulatedSprite clonedSprite = (SimulatedSprite) simSprite.clone();
//				simulatedSprites.add(clonedSprite);
//				enemyTracking.put(clonedSprite,this.enemyTracking.get(simSprite));
//			}
//			for (SimulatedSprite simSprite : this.simulatedSpritesToAdd) 
//			{
//				SimulatedSprite clonedSprite = (SimulatedSprite) simSprite.clone();
//				simulatedSpritesToAdd.add(clonedSprite);
//				enemyTracking.put(clonedSprite,this.enemyTracking.get(simSprite));
//			}
//			
//			PlayerWorldObject[][] staticGlobalMap = GeneralUtil.clone2DPlayerWorldObjectArray(this.staticGlobalMap);
//			PlayerWorldObject[][] staticLocalMap = GeneralUtil.clone2DPlayerWorldObjectArray(this.staticLocalMap);
//			
//			return new SimulatedLevelScene
//			(
//					simulatedPlayersClone,
//					simulatedSprites, 
//					simulatedSpritesToAdd, 
//					enemyTracking,
//					staticGlobalMap, 
//					staticLocalMap, 
//					this.mapHeight,
//					this.mapWidth, 
//					this.paused, 
//					this.elapsedTime, 
//					this.lock,
//					this.knowledge, 
//					this.simulatedSpawns, 
//					this.playerIndex
//			);
//		}
//		finally 
//		{
//			lock.unlock();
//		}
		
		return new SimulatedLevelScene(this);
	}

	@Override
	public GlobalContinuous getPlanningPlayerPos() 
	{
		return simulatedPlayers.get(planningPlayerIndex).getPosition(0);
	}
	
	public SimulatedPlayer getPlanningPlayer() 
	{
		return simulatedPlayers.get(planningPlayerIndex);
	}

	/**
	 * searches the closest hostile object in the scene
	 * 
	 * @param scene
	 * @return
	 */
	public SimulatedSprite getClosestEnemy() {
		SimulatedPlayer agent = this.getPlanningPlayer();
		SimulatedSprite closestEnemy = null;

		// iterate over all players and search for hostile ones which are closer
		// than the closest enemy so far

		// iterate over all sprites and search for hostile ones which are closer
		// than the closest enemy so far
		for (SimulatedSprite sprite : this.getSimulatedSprites()) {
			if (sprite.isAlive() && sprite.isHostile()) {
				if (agent.distance(sprite) < agent.distance(closestEnemy)) {
					closestEnemy = sprite;
				}
			}
		}
		return closestEnemy;
	}

	// /**
	// * return a global discrete map of either static or moving objects
	// *
	// * @param isStatic
	// * if static or moving objects are desired
	// * @param timeDelay
	// * only relevant for moving objects. For timeDelay=0 (1, 2), this
	// * method uses x, (xOld, xOldOld), analogous for y.
	// * @param levelSceneAdapter
	// * @return
	// */
	// public MarioWorldObject[][] getObservation(ObjectType isStatic, int
	// timeDelay,
	// LevelSceneAdapter levelSceneAdapter) {
	// switch (isStatic) {
	// case NON_STATIC:
	// return levelSceneAdapter.praktikumGlobalMovingObservation(timeDelay);
	// case STATIC:
	// if(timeDelay!=0) {
	// new
	// Throwable("timeDelay "+timeDelay+" not implemented.").printStackTrace();
	// }
	// return this.staticGlobalMap;
	// default:
	// return null;
	// }
	// }

	/**
	 * Returns a list of simulated sprites.
	 * 
	 * @return
	 */
	public HashSet<SimulatedSprite> getSimulatedSprites() {
		return this.simulatedSprites;
	}

	/**
	 * Returns a list of simulated Players
	 * 
	 * @return
	 */
	public ArrayList<SimulatedPlayer> getSimulatedPlayers() {
		return simulatedPlayers;
	}

	// public PlayerWorldObject[][] getObservation(boolean isGlobal, int
	// timeDelay)
	// {
	// if (isGlobal)
	// {
	// return cloneGlobalStaticSimulation();
	// }
	// return cloneLocalStaticSimulation();
	// }

	public PlayerWorldObject[][] getGlobalStaticSimulation() {
		// return LevelSceneAdapter.cloneMap(staticGlobalMap);

		// returns the map only for comparison, this is a reference, not a
		// clone!
		return getStaticGlobalMap();
	}

	public PlayerWorldObject[][] createLocalStaticSimulation() {
		GlobalCoarse playerPosition = simulatedPlayers.get(planningPlayerIndex)
				.getPosition(0).toGlobalCoarse();
		return LevelSceneAdapter.createLocalStaticObservation(playerPosition,
				getStaticGlobalMap(), mapHeight);
	}

	/*
	 * public PlayerWorldObject[][] getGlobalMovingSimulation(int timeDelay) {
	 * return
	 * LevelSceneAdapter.praktikumGlobalMovingObservation(simulatedSprites,
	 * timeDelay, mapHeight, mapWidth); }
	 */

	/*
	 * public PlayerWorldObject[][] getLocalMovingSimulation(final int
	 * timeDelay) { GlobalCoarse playerPos = simulatedPlayers.get(playerIndex)
	 * .getPosition(timeDelay).toGlobalCoarse(); //
	 * getMario().getPosition().toGlobalCoarse(); return
	 * LevelSceneAdapter.praktikumLocalMovingObservation( simulatedSprites,
	 * timeDelay, playerPos); }
	 */

	public PlayerWorldObject[][] getStaticLocalMap() {
		return staticLocalMap;
	}

	/**
	 * If there are differences between the engine and simulation which are
	 * proven to be correct and no mistake, they can be specified here.
	 * 
	 * @param fieldName
	 * @param type
	 * @param ignoredFields
	 *            the list which is modified
	 * @return
	 */
	private void ignoreField(String fieldName, PlayerWorldObject type,
			ArrayList<String> ignoredFields) {
		switch (fieldName) {
		case "width":
			switch (type) {
			case GRUMPY_MOVING:
			case GRUMPY_STILL:
				ignoredFields.add(fieldName);
				break;
			default:
				break;
			}
		default:
			break;
		}
	}

	public boolean isBlockingAgainstDirection(GlobalCoarse position, CollisionDirection direction) 
	{
		//wrong function for this! is checked for in worldgoaltester
//		//carried positions are safe... trust the carrier...
//		if(getPlanningPlayer().getCarriedBy()!=null)
//			return true;

		if (position.y < 0 || position.y >= getStaticGlobalMap().length || position.x < 0 || position.x >= getStaticGlobalMap()[0].length)
			return true;

		switch (getStaticGlobalMap()[position.y][position.x].blocksPath) 
		{
		case FULL:
			return true;
		case TOP:
			return direction == CollisionDirection.ABOVE;
		case BOTTOM:
			return direction == CollisionDirection.BELOW;
		case NONE:
		default:
			return false;
		}
	}

	public boolean isInMap(GlobalCoarse position) {
		return LevelSceneAdapter.isInMap(getStaticGlobalMap()[0].length,
				getStaticGlobalMap().length, position);
	}

	@Override
	public boolean setLevelScene(byte[][] data) {
		// TODO implement
		return false;
	}

	@Override
	public boolean setEnemies(float[] enemies) {
		// TODO implement
		return false;
	}

	@Override
	public void setPlayerPosition(float x, float y) {
		// TODO implement
	}

	@Override
	public void setPlayerVelocity(float xa, float ya) {
		// TODO implement
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	/**
	 * This method simulates the pending motor commands ...
	 * 
	 * @param lastNode
	 * @return WorldNode with static Mario is its worldstate
	 */
	public SimulatedLevelScene getNextStaticPosition(ArrayList<PlayerActions> actionSequence) 
	{
		// TODO: this function does NOT calculate the next static position!

		/*
		 * adding an empty action to the actionList until Mario is standing
		 * still
		 */

		int tol = 100;
		while (this.getPlanningPlayer().isMoving() && tol > 0) 
		{
			if (actionSequence.size() == 0) 
			{
				actionSequence.add(new PlayerActions());
				tol--;
				// System.out.println("INTOLERANT STUFF HERE!");
			}

			//NOTE: this clears the action sequence! Is that intended????
			//this.tick(actionSequence.remove(0));
			
			this.tick(new PlayerActions());
		}
		return this;
	}

	private void updateXCam() {
		// TODO!!
		this.xCam = simulatedPlayers.get(0).getPosition(0).x - 160;
		final int levelWidth = mapWidth * CoordinatesUtil.UNITS_PER_TILE;
		if (this.xCam < 0)
			this.xCam = 0;
		if (this.xCam > levelWidth - 320)
			this.xCam = levelWidth - 320;
	}

	/**
	 * Takes spawn points of enemies from spriteTemplate and adds them to
	 * simulatedSpawns
	 * 
	 * @param spriteTemplate
	 */
	public void initialiseSpawns(SpriteTemplate[][] spriteTemplate) {
		this.simulatedSpawns.clear();
		for (int i = 0; i < mapWidth; i++) {
			for (int j = 0; j < mapHeight; j++) {
				if (spriteTemplate[i][j] != null) {
					if (spriteTemplate[i][j].getType() != -1) {
						GlobalCoarse position = new GlobalCoarse(i, j);
						SimulatedSpawn spawn = new SimulatedSpawn(
								spriteTemplate[i][j], position);
						this.simulatedSpawns.add(spawn);
					}
				}
			}
		}
	}

	public TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> getKnowledge() {
		return this.knowledge;
	}

	public int getMapWidth() {
		return this.mapWidth;
	}

	public int getMapHeight() {
		return this.mapHeight;
	}

	public PlayerWorldObject getStaticGlobalMapObject(int x, int y) {
		return this.getStaticGlobalMap()[x][y];
	}

	public void setCollisionsInLastTick(List<CollisionAtPosition> collisions) {
		this.collisionsInLastTick = collisions;

	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;

		if (!o.getClass().equals(getClass()))
			return false;

		SimulatedLevelScene other = (SimulatedLevelScene) o;

		// CHECK: why is this always equal??
		// PlayerWorldObject[][] globalMovingMap =
		// this.getGlobalMovingSimulation(0);
		// PlayerWorldObject[][] otherGlobalMovingMap =
		// other.getGlobalMovingSimulation(0);

		// this works:
		PlayerWorldObject[][] globalMovingMap = this
				.getGlobalStaticSimulation();
		PlayerWorldObject[][] otherGlobalMovingMap = other
				.getGlobalStaticSimulation();

		for (int i = 0; i < globalMovingMap.length; i++) {
			for (int j = 0; j < globalMovingMap[i].length; j++) {
				if (globalMovingMap[i][j] != otherGlobalMovingMap[i][j]) {
					// System.out.println("SCENES DIFFER AT " + i + " " + j);
					return false;
				}
			}
		}

		// compare sprites!
		for (SimulatedSprite sprite : this.simulatedSprites) {
			if (!other.simulatedSprites.contains(sprite))
				return false;
		}

//		if (!this.simulatedPlayers.get(playerIndex).equals(
//				other.simulatedPlayers.get(playerIndex))) {
//			return false;
//		}
		
		// compare sprites!
		for (SimulatedPlayer player : this.simulatedPlayers) {
			if (!other.simulatedPlayers.contains(player))
				return false;
		}		

		return true;
	}

	public PlayerWorldObject[][] getStaticGlobalMap() {
		return staticGlobalMap;
	}

	public void expandReachabilityMap(ReachabilityMap map) {
		map.updateReachabilityMap(this);
	}
	/*
	public void initialReachabilityMap(int i) {
		computeReachabilityMap(i);
		
	}*/

	public List<CollisionAtPosition> simulateReachabilityMovement(boolean[] inputKeys, float xVelocity, float yVelocity,
			float[][] reachabilityCounter, int playerIndex) {
		
		
		updateXCam();
		elapsedTime++;
		ArrayList<CollisionAtPosition> ret = new ArrayList<CollisionAtPosition>();


		PlayerWorldObject[][] map = this.staticGlobalMap;
		
		
		/** Spawn and remove sprites */
		if (simulatedSpawns != null) {
			for (SimulatedSpawn spawn : simulatedSpawns) {
				if (paused) {

				} else {
					spawn.spawnSprite(simulatedSprites, getPlanningPlayerPos(), xCam, yCam);
					spawn.removeSprite(simulatedSprites, xCam, yCam);
				}
			}
		}


		
		/** move mario and remember collision between mario and static world */
		ret.addAll(simulatedPlayers.get(playerIndex).simulateReachabilityMovement(map, inputKeys, xVelocity, yVelocity,reachabilityCounter));
		/**
		 * checks if carry collision happens
		 */
		
		for (SimulatedPlayer otherPlayer : simulatedPlayers) {
			if(otherPlayer == simulatedPlayers.get(playerIndex)) {
				continue;
			}
			CollisionAtPosition mountCollision = simulatedPlayers.get(playerIndex).mountCheck(otherPlayer);
			List<CollisionAtPosition> dropCollision = simulatedPlayers.get(playerIndex).collideCheck(otherPlayer);
			if(mountCollision != null) {
				ret.add(mountCollision);
			}
			if(dropCollision.size() > 0){
				ret.addAll(dropCollision);
			}

		} 
		

		/**
		 * execute changes to movement from other directions and remember
		 * collision between mario and sprites
		 * TODO ï¿½berprï¿½fen ob das funktioniert
		 */
		ArrayList<CollisionAtPosition> collisionsWithMovingObjects = GeneralUtil
				.doCollisionWithMovingObjects(simulatedSprites,
						simulatedPlayers.get(playerIndex));
		ret.addAll(collisionsWithMovingObjects);
		
		/**
		 * include known effects in the simulation
		 */
		/*TreeMap<Effect, Double> test = simulateCollisionList(ret,false);
		
		//see if the collision destroyed an object.
		for (Entry<Effect, Double> entry : test.entrySet()) {
			if(entry.getKey().getType() == ActionEffect.OBJECT_DESTRUCTION){
				for (CollisionAtPosition current : ret) {
					if(current.condition.condition.getTarget() != PlayerWorldObject.COIN_FRONT){
						//System.out.println("destruction was found");
						this.destructionOfObjectOccured =true;
					}
				}
				
			}
		}*/
		//if(test.getKey().getType() == ActionEffect.OBJECT_DESTRUCTION)
		/** remember Mario's surrounding */
		/*this.staticLocalMap = LevelSceneAdapter
				.createLocalObservationFromGlobalObservation(
						simulatedPlayers.get(playerIndex).getMapPosition(0),
						staticGlobalMap, staticGlobalMap.length);*/

		this.simulatedSprites.addAll(simulatedSpritesToAdd);
		this.simulatedSpritesToAdd.clear();
		this.collisionsInLastTick = ret;
		return ret;
	
	}
	
	public SimulatedSprite getSpriteBasedOnType(PlayerWorldObject otherType){
		SimulatedSprite result = null;
		for(SimulatedPlayer player : getSimulatedPlayers())
		{
			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(player.type);

			if (otherType.equals(type))
			{
				result = player;
				if(/*sprite.getType().equals(PlayerWorldObject.MARIO) ||sprite.getType().equals(PlayerWorldObject.TOAD)*/
						PlayerWorldObject.PLAYER.equals(Generalizer.generalizePlayer(player.getType())) ){
					//System.out.println("PWO: sprite: " + sprite.toString());
					for (SimulatedPlayer simplayer : getSimulatedPlayers()) {
						if(simplayer.getType().equals(type)) {
							return simplayer;
						}
					}
				}
			}
		}
			
		for(SimulatedSprite sprite : getSimulatedSprites())
		{
			PlayerWorldObject spriteType = Generalizer.generalizePlayerWorldObject(sprite.type);

			if (otherType.equals(spriteType))
			{
				result = sprite;
				if(/*sprite.getType().equals(PlayerWorldObject.MARIO) ||sprite.getType().equals(PlayerWorldObject.TOAD)*/
						PlayerWorldObject.PLAYER.equals(Generalizer.generalizePlayer(sprite.getType())) ){
					//System.out.println("PWO: sprite: " + sprite.toString());
					for (SimulatedPlayer simplayer : getSimulatedPlayers()) {
						if(simplayer.getType().equals(spriteType)) {
							return simplayer;
						}
					}
				}
			}
			
			
		}
		return result;
	}
	
	public ArrayList<ReachabilityNode> updateReachabilityMaps(SimulatedLevelScene simulatedLevelScene) {
	
		ArrayList<ReachabilityNode> newRechabilityNodes = new ArrayList<ReachabilityNode>();
		//if one player carries another player, then both players have the same reachability,thus only needing to calculate it once
		for(SimulatedPlayer tmp : simulatedPlayers){
			if(tmp.getCarries() != null){
				for(int i = 0; i<simulatedPlayers.size();i++){
					newRechabilityNodes.add(reachabilityNodes.get(0).updateReachabilityMap(simulatedLevelScene,i));
				}
				return newRechabilityNodes;
			}
		}
		
		for(int i = 0; i<simulatedPlayers.size();i++){
			newRechabilityNodes.add(reachabilityNodes.get(0).updateReachabilityMap(simulatedLevelScene,i));
		}
		return newRechabilityNodes;
	}
	
	public void updateReachabilityMaps() {
		
		ArrayList<ReachabilityNode> newRechabilityNodes = new ArrayList<ReachabilityNode>();
		//if one player carries another player, then both players have the same reachability,thus only needing to calculate it once
		for(SimulatedPlayer tmp : simulatedPlayers){
			if(tmp.getCarries() != null){
				for(int i = 0; i<simulatedPlayers.size();i++){
					newRechabilityNodes.add(reachabilityNodes.get(0).updateReachabilityMap(this,i));
				}
				this.reachabilityNodes = newRechabilityNodes;;
				return;
			}
		}
		
		for(int i = 0; i<simulatedPlayers.size();i++){
			newRechabilityNodes.add(reachabilityNodes.get(0).updateReachabilityMap(this,i));
		}
		this.reachabilityNodes = newRechabilityNodes;
	}


	/**
	 * calculates the index of the actor of a CAP that should be moved based on who carries whom
	 * @return
	 */
	public int getActingPlayerIndex(SimulatedPlayer actor){

		for(int i = 0; i< this.getSimulatedPlayers().size();i++) {
			if(this.getSimulatedPlayers().get(i).getType() == actor.getType()) {
				return i;
			}
		}
 
		return this.getPlanningPlayerIndex();

		
	}
	
	/**
	 * calculates the index of the player that should be moved based on who carries whom
	 * 
	 * @return
	 */
	public int calculateMovingPlayer(){
		
		if(this.getPlanningPlayer().getCarriedBy() != null) {
			
			SimulatedPlayer carryingPlayer = (SimulatedPlayer)this.getPlanningPlayer().getCarriedBy();
			
			for(int i = 0; i< this.getSimulatedPlayers().size();i++) {
				if(this.getSimulatedPlayers().get(i).getType() == carryingPlayer.getType()) {
					return i;
				}
			}
		} 
		return this.getPlanningPlayerIndex();
		
		
		
	}
	/**
	 * calculates the index of the player that should be moved based on who carries whom
	 * If no carry interaction is found return the input default index.
	 * @return
	 */
	public int calculateMovingPlayer(int defaultPlayerIndex){
		
		if(this.getPlanningPlayer().getCarriedBy() != null) {
			
			SimulatedPlayer carryingPlayer = (SimulatedPlayer)this.getPlanningPlayer().getCarriedBy();
			
			for(int i = 0; i< this.getSimulatedPlayers().size();i++) {
				if(this.getSimulatedPlayers().get(i).getType() == carryingPlayer.getType()) {
					return i;
				}
			}
		} 
		return defaultPlayerIndex;
		
		
		
	}


	public SimulatedPlayer getMovingPlayer() {
		
		return simulatedPlayers.get(calculateMovingPlayer());
	}

}
