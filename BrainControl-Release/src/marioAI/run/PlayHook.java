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
package marioAI.run;

import java.util.ArrayList;
import java.util.logging.Level;

import marioAI.agents.CAEAgent;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioUI.speechSynthesis.VoiceResponseEventBus;
import marioUI.voiceControl.VoiceControl;
import marioUI.voiceControl.interpretation.VoiceInterpreter;
import marioWorld.agents.Agent;
import marioWorld.engine.EngineHook;
import marioWorld.engine.GameWorld;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.level.SpriteTemplate;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;
import marioWorld.mario.simulation.BasicSimulator;
import marioWorld.mario.simulation.Simulation;
import marioWorld.mario.simulation.SimulationOptions;
import marioWorld.tools.EvaluationOptions;
import marioWorld.utils.UserDir;

public abstract class PlayHook extends EngineHook 
{
	public static GameWorld gameWorld = null;

	public static final int DEBUG_BREAK_AT_TIME_STEP = -1;

	public static ArrayList<Agent> agents = new ArrayList<Agent>();
	public static ArrayList<Player> players = new ArrayList<Player>();

	public static VoiceControl voiceControl = new VoiceControl();
	
	public static VoiceResponseEventBus voiceResponseEventBus = new VoiceResponseEventBus(voiceControl);

	/** Convenience methods */

	protected static void initPlayers(GameWorld gameWorldComponent, GlobalContinuous[] initialPosition) 
	{
		initPlayers(gameWorldComponent, initialPosition, null, new int[] { 1, 0 }, null,null);
	}

	/**
	 * All parameters are optional. Use null if you don't want to specify them.
	 * 
	 * @param gameWorldComponent
	 * @param initialPosition
	 *            GlobalContinous[ ] start Positions of the Players
	 * @param initialVelocity
	 * @param initialHealth
	 */
	protected static void initPlayers(GameWorld gameWorldComponent, GlobalContinuous[] initialPosition,
			GlobalContinuous initialVelocity, int[] facing, PlayerHealthCondition[] initialHealth, int[] initialEnergy) 
	{
		Player player;
		for (int i = 0; i < GlobalOptions.realLevelScene.getPlayers().size(); i++) {
			player = GlobalOptions.realLevelScene.getPlayers().get(i);
			
			if (initialPosition[i] != null) {
				player.x = initialPosition[i].x;
				player.y = initialPosition[i].y;
			}
			if (initialVelocity != null) {
				player.xa = initialVelocity.x;
				player.ya = initialVelocity.y;
			}
			if (initialHealth != null) {
				switch (initialHealth[i]) {
				case SMALL:
					player.setLarge(false, false);
					break;
				case LARGE_NON_BULB:
					player.setLarge(true, false);
					break;
				case BULB:
					player.setLarge(true, true);
					break;
				case INVULNERABLE:
					player.invulnerableTime = 32;
					break;
				default:
					break;
				}
				player.facing = facing[i];

			}
			if(initialEnergy != null) {
				player.setEnergys(initialEnergy[i]);
			}
		}
		
		//TODO: Just a test.
		//addRespawningSprite(gameWorldComponent, PlayerWorldObject.LUKO, new GlobalCoarse(28, 0));
		
		//addRespawningSprite(gameWorldComponent, PlayerWorldObject.LUKO, new GlobalCoarse(38, 0));
	}

	/**
	 * Method for creating and adding a sprite. The few options should generally
	 * be enough.
	 * 
	 * This method adds a sprite only once. It is opposed to SpriteTemplate,
	 * which creates sprites whenever mario gets close (even if the sprite was
	 * destroyed earlier, mario moves away and gets back).
	 * 
	 * @param gameWorldComponent
	 * @param spriteType
	 *            which type of sprite is created
	 * @param position
	 *            initial position
	 * @param velocity
	 *            Optional initial velocity
	 * @param facing
	 *            -1: to-the-left, 0: for non-facing sprites, +1: to-the-right
	 * @param isRedGrumpy
	 *            should be only GUI relevant. In case this is a grumpy, you can
	 *            paint it red or green here.
	 */
	public static Sprite addSprite(GameWorld gameWorld, PlayerWorldObject spriteType, GlobalContinuous position) 
	{
		return addSpriteWithMoreOptions(gameWorld, spriteType, position, null, -1, false);		
	}

	public static void addRespawningSprite(GameWorld gameWorld, PlayerWorldObject spriteType,
			GlobalCoarse spritePosition) 
	{
		LevelScene levelScene = GlobalOptions.realLevelScene;

		//create sprite, only required to read kind and winged. Direction does not matter
		Sprite sprite = spriteType.createSprite(levelScene, spritePosition.toGlobalContinuous(), -1, false);
		SpriteTemplate spriteTemplate = SpriteTemplate.createSpriteTemplate(sprite);
		levelScene.level.setSpriteTemplate(spritePosition, spriteTemplate);
	}

	/**
	 * Method for creating and adding a sprite with more options.
	 * 
	 * This method adds a sprite only once. It is opposed to SpriteTemplate,
	 * which creates sprites whenever mario gets close (even if the sprite was
	 * destroyed earlier, mario moves away and gets back).
	 * 
	 * @param gameWorldComponent
	 * @param spriteType
	 *            which type of sprite is created
	 * @param position
	 *            initial position
	 * @param velocity
	 *            Optional initial velocity
	 * @param facing
	 *            -1: to-the-left, 0: for non-facing sprites, +1: to-the-right
	 * @param isRedGrumpy
	 *            should be only GUI relevant. In case this is a grumpy, you can
	 *            paint it red or green here.
	 */
	protected static Sprite addSpriteWithMoreOptions(GameWorld gameWorld, PlayerWorldObject spriteType,
			GlobalContinuous position, GlobalContinuous velocity, int facing, boolean isRedGrumpy) 
	{		
		LevelScene levelScene = GlobalOptions.realLevelScene;
		Sprite sprite = spriteType.createSprite(levelScene, position, facing, isRedGrumpy);
		if (velocity != null) {
			sprite.xa = velocity.x;
			sprite.ya = velocity.y;
		}
		levelScene.addSprite(sprite);
		return sprite;
	}

	protected static void addStaticObject(GameWorld gameWorld, PlayerWorldObject objectType, GlobalCoarse position) 
	{
		objectType.addStaticToMap(GlobalOptions.realLevelScene.level.map, position);
	}

	protected static synchronized void run(EvaluationOptions options)
	{
//		Simulation simulator = new BasicSimulator(options.getSimulationOptionsCopy());		
//		simulator.simulateOneLevel();		

		SimulationOptions simulationOptions = options.getSimulationOptionsCopy();
		
		gameWorld.setZLevelScene(simulationOptions.getZLevelMap());
		gameWorld.setZLevelEnemies(simulationOptions.getZLevelEnemies());
		gameWorld.startLevel(simulationOptions);	//this creates the levelscene etc
		
		for (EngineHook h : EngineHook.getHooks())
		{			
			h.OnGameWorldPrepared(gameWorld);
		}

		LevelScene levelScene = GlobalOptions.realLevelScene;
		for (Sprite sprite : simulationOptions.getInitialSprites()) 
		{
			GlobalCoarse spriteMapPos = sprite.getPosition().toGlobalCoarse();
			SpriteTemplate spriteTemplate = SpriteTemplate.createSpriteTemplate(sprite);
			levelScene.level.setSpriteTemplate(spriteMapPos, spriteTemplate);
		}

		gameWorld.setPaused(simulationOptions.isPauseWorld());
		gameWorld.setZLevelEnemies(simulationOptions.getZLevelEnemies());
		gameWorld.setZLevelScene(simulationOptions.getZLevelMap());
		gameWorld.setPlayersInvulnerable(simulationOptions.isPlayerInvulnerable());
		
		//GAMEWORLDCOMPONENT IS THE MAIN RUN FUNCTION! All other components are just updated from this. 
		gameWorld.run1(SimulationOptions.currentTrial++, simulationOptions.getNumberOfTrials());
	}

	private void addDefaultLoggers() {
		Logging.addLogger("Agent", Level.CONFIG);
		Logging.addLogger("Brain", Level.WARNING);
		Logging.addLogger("SimulationError", Level.WARNING);
		Logging.addLogger("Planner", Level.CONFIG);
		Logging.addLogger("AStarSimulator", Level.FINEST);
		Logging.addLogger("Controller", Level.WARNING);
		Logging.addLogger("ListenerReservoir", Level.INFO);
	}

	/**
	 * registers the hook. As a consequence, onGameWorldPrepared() is called
	 * later.
	 */
	public PlayHook(boolean addDefaultLoggers) 
	{		
		classesWithStaticStuff.add(this.getClass());

		//game world is initialized before everything else! (needed for graphics etc)
		//ToolsConfigurator.createGameWorld(options);
		gameWorld = new GameWorld();
		
		EngineHook.installHook(this);
		if (addDefaultLoggers) {
			addDefaultLoggers();
		}

	}

	public static void resetStaticAttributes() {
		System.out.println("PlayHook: resetStaticAttributes()");
		EngineHook.resetStaticAttributes();
		agents = new ArrayList<Agent>();
		players = new ArrayList<Player>();
		voiceControl = new VoiceControl();
	}

	public static void deleteStaticAttributes() {
		EngineHook.deleteStaticAttributes();
		agents = new ArrayList<Agent>();
		players = new ArrayList<Player>();
		voiceControl = null;
	}

	public static boolean outOfEnergys() 
	{
		for (Player p : players) 
		{
			if(p.getEnergys() > VoiceInterpreter.LOW_PRIZE) 
			{
				return false;
			}
		}
		return true;
	}

}
