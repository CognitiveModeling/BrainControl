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
package marioAI.movement.tgng;

import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.WriteAbortedException;

import marioAI.agents.CAEAgentHook;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.goals.CuriosityPlanner;
import marioWorld.engine.Logging;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;
import marioWorld.utils.ResourceStream;
import marioWorld.utils.UserDir;

/**
 * main class for the Tgng algorithm, builds and handles different types of
 * networks for different enemies and extracts the best action in a given
 * situation
 * 
 * @author benjamin
 *
 */
public class Tgng implements Serializable {
	/**
	 * Version number for serialization
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * threshold parameter, decides the minimum distance between nodes
	 */
	public static final double THETA = 1;

	/**
	 * weakening parameter for the reward propagation, can be used to implement
	 * some sort of way costs
	 */
	public static final double GAMMA = 1;

	/**
	 * the number of blocks above/right/under/left of the player to be tracked
	 * with the tgng
	 */
	public static final int[] TGNG_SIZE = { 2, 2, 2, 2 };

	/**
	 * use to weight the influence of the SensoryInformation Parameters on the
	 * distance of two nodes
	 */
	public static final double[] WEIGHTS = initializeWeights();

//	/**
//	 * network used to deal with fireballs
//	 */
//	private TgngNetwork fireballTgng;
//
//	/**
//	 * network used to deal with other players
//	 */
//	private TgngNetwork playerTgng;

	/**
	 * network used to deal with non-playeable characters
	 */
	private TgngNetwork npcTgng;

	/**
	 * last used network
	 */
	private TgngNetwork lastActiveTgng;

//	/**
//	 * the planner is used to propagate rewards according to the emotional
//	 * status
//	 */
//	private transient CuriosityPlanner planner;

	/**
	 * shows if the tgng is currently in use
	 */
	private boolean isActive = false;

	/**
	 * the last enemy tracked by the tgng
	 */
	private transient SimulatedSprite trackedEnemy;

	/**
	 * path for tgng save file
	 */
	private final String TGNG_FILE_PATH;
	
	/**
	 * Enum of possible tgng types
	 * @author benjamin
	 *
	 */
	public static enum TgngType {
		FIREBALL, INTELLIGENT, NON_INTELLIGENT
	};

	/**
	 * tgng will be loaded from disk if true
	 */
	private static final boolean AUTOMATICALLY_LOAD_TGNG = true;

	/**
	 * creates a tgng object, can load it from a file
	 * 
	 * @param name
	 */
	public Tgng(String name /*, CuriosityPlanner planner*/) {
		this.TGNG_FILE_PATH = getTgngFilePath(name);
		File file = UserDir.getUserFile(this.TGNG_FILE_PATH);
		if (file.exists() && AUTOMATICALLY_LOAD_TGNG) {
			System.out.println("Loading TGNG");
			loadFromDisk(file.getAbsolutePath() /*, planner*/);
		} else {
			System.err.println("Using new TGNG");
//			this.fireballTgng = new TgngNetwork(/*this.planner,*/ TgngType.FIREBALL);
//			this.playerTgng = new TgngNetwork(/*this.planner,*/ TgngType.INTELLIGENT);
			this.npcTgng = new TgngNetwork(/*this.planner,*/ TgngType.NON_INTELLIGENT);
		}
	}

	/**
	 * Method can be used to set parameters after initializing of CAEAgent has
	 * taken place
	 */
	public void init() {
		System.out.println(this.TGNG_FILE_PATH);
		setWindowListener(this);
	}

	/**
	 * creates a unique file path for each agent, used to save the tgng
	 * 
	 * @param name
	 * @return
	 */
	private String getTgngFilePath(String name) {
		StringBuilder path = new StringBuilder(ResourceStream.DIR_AI_RESOURCES+"/movement/tgng/");
		path.append(name);
		path.append("Tgng");
		path.append(".ser");
		return path.toString();
	}

	/**
	 * decides which tgng has to be used, deals with activation and deactivation
	 * of the networks and returns the best known action
	 * 
	 * @param scene
	 * @return
	 */
	public boolean[] getTgngMove(SimulatedLevelScene scene) {
		SensoryInformation input = new SensoryInformation(scene);

		if (isActive && trackedEnemy != null) {
			if (!trackedEnemy.isAlive()) {
				// enemy killed, tick and return null
				lastActiveTgng.tick(scene, trackedEnemy);
				// System.out.println("Enemy killed");
				this.lastActiveTgng.getAnalysator().recordGoalReached();
				//System.out.println(this.lastActiveTgng.entropyEvaluate());
				//this.lastActiveTgng.getAnalysator().printOut();
				deactivate();
				return null;
			} else if (scene.getPlanningPlayer().isOnGround()
					&& (input.getEnemy() == null || !SensoryInformation
							.isInObservationRange(input.getEnemy(),
									scene.getPlanningPlayer()))) {
				// enemy out of range which isn't caused by jumping higher than tgng range,
				// return null
				// System.out.println("Enemy out of range");
				this.lastActiveTgng.getAnalysator().recordGoalNotReached();
				//System.out.println(this.lastActiveTgng.entropyEvaluate());
				//this.lastActiveTgng.getAnalysator().printOut();
				deactivate();
				return null;
			} 
			/*else if (!trackedEnemy.stemsFromSameEngineSprite(input.getEnemy())) 
			{
				// switch network
				// System.out.println("Switched Tgng from "
				// + lastActiveTgng.activeNode.getInput().getEnemyType()
				// + " to " + input.getEnemyType());
				this.lastActiveTgng.getAnalysator().recordGoalNotReached();
				//System.out.println(this.lastActiveTgng.entropyEvaluate());
				//this.lastActiveTgng.getAnalysator().printOut();
				deactivate();
				activate(input);	
			}*///TEST: disabled
		} else {
			// System.out.println("Tgng activated");
			activate(input);
		}

		lastActiveTgng.tick(scene);
		boolean[] action = lastActiveTgng.getAction();
		this.lastActiveTgng.getAnalysator().move();

		return action;

	}

	/**
	 * deactivates the currently active tgng network
	 */
	private void deactivate() 
	{
		//System.out.println("TGNG deactivated (" + (fireballTgng.getNodes().size() + npcTgng.getNodes().size() + playerTgng.getNodes().size()) + " nodes)");
		System.out.println("TGNG deactivated (" + npcTgng.getNodes().size() + " nodes)");
		
		lastActiveTgng.deactivate();
		lastActiveTgng = null;
		isActive = false;
	}

	/**
	 * activates the needed tgng network
	 * 
	 * @param input
	 */
	private void activate(SensoryInformation input) 
	{
		//System.out.println("TGNG activated (" + (fireballTgng.getNodes().size() + npcTgng.getNodes().size() + playerTgng.getNodes().size()) + " nodes)");
		System.out.println("TGNG activated (" + npcTgng.getNodes().size() + " nodes)");
		
		isActive = true;
		
		lastActiveTgng = determineTgngType(input.getEnemyType());
		trackedEnemy = input.getEnemy();
	}

	/**
	 * decides which tgng network has to be used by the enemy type
	 * 
	 * @param type
	 * @return
	 */
	private TgngNetwork determineTgngType(TgngType type) 
	{
		//TEST: right now, we just use a single TGNG:
		return npcTgng;
		
//		if (type == null) 
//			return npcTgng;
//		
//		switch (type) 
//		{
//		case FIREBALL:
//			return fireballTgng;
//		case INTELLIGENT:
//			return playerTgng;
//		case NON_INTELLIGENT:
//			return npcTgng;
//		default:
//			System.out.println("Unknown Enemy Type");
//			return null;
//		}
	}

	/**
	 * saves the tgng object
	 */
	public void saveToDisk() {
		System.out.println("Saved tgng to: " + TGNG_FILE_PATH);

		// delete current tgng-file to ensure a proper creation of the new
		// file
		File file = UserDir.getUserFile(TGNG_FILE_PATH);

		try {
			if (file.exists()) {
				file.delete();
			}
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(file, true));
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			Logging.logSevere("Agent", "Error while saving the tgng: " + e);
		}

	}

	/**
	 * Loads tgng from disk.
	 * 
	 * @param The
	 *            path where to load.
	 */
	public void loadFromDisk(String path /*, CuriosityPlanner planner*/) {
		try {
			System.out.println("Path: "+path);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					path));
			Tgng network = (Tgng) in.readObject();
			in.close();
//			this.fireballTgng = network.fireballTgng;
//			this.playerTgng = network.playerTgng;
			this.npcTgng = network.npcTgng;
			//setPlannerRecursive(planner);
			System.out.println("Loaded tgng from " + path);
		} catch (WriteAbortedException e) {
			System.out.println(e.detail);
		} catch (IOException e) {
			Logging.logSevere("Agent",
					"Error while loading tgng " + e.getMessage());
		} catch (ClassNotFoundException e) {
			Logging.logSevere("Agent",
					"Class not found while loading tgng " + e.getMessage());
		}
	}
	
	/**
	 * returns true if the enemy is in the area tracked by the players tgng
	 * 
	 * @param player
	 * @param enemy
	 * @return
	 */
	public static boolean isInTgngThresholds(Player player, Sprite enemy) {
		return isInTgngThresholds(new SimulatedPlayer(player),
				new SimulatedSprite(enemy));
	}

	/**
	 * returns true if the enemy is in the area tracked by the players tgng
	 * 
	 * @param player
	 * @param enemy
	 * @return
	 */
	public static boolean isInTgngThresholds(SimulatedPlayer player,
			SimulatedSprite enemy) {
		return SensoryInformation.isInObservationRange(enemy, player);
	}

	/**
	 * sets the planner of this tgng recursive to all networks, edges and nodes
	 * 
	 * @param planner
	 */
//	private void setPlannerRecursive(CuriosityPlanner planner) {
//		this.planner = planner;
//		this.fireballTgng.setPlannerRecursive(planner);
//		this.npcTgng.setPlannerRecursive(planner);
//		this.playerTgng.setPlannerRecursive(planner);
//	}

	public boolean isActive() {
		return isActive;
	}

//	public TgngNetwork getFireballTgng() {
//		return fireballTgng;
//	}

	public TgngNetwork getNpcTgng() {
		return npcTgng;
	}

//	public TgngNetwork getPlayerTgng() {
//		return playerTgng;
//	}

	
	/**
	 * only used once to initialize the weights vector
	 * 
	 * @return
	 */
	private static double[] initializeWeights() {
		int index = 0;
		double[] weights = new double[8 + (TGNG_SIZE[0] + TGNG_SIZE[2] + 2)
				* (TGNG_SIZE[1] + TGNG_SIZE[3] + 1)];

		// health weight
		weights[index] = 1;
		index++;

		// xa weight
		weights[index] = 1d/16d;
		index++;

		// ya weight
		weights[index] = 1d/16d;
		index++;

		// enemyHealth weight
		weights[index] = 1;
		index++;

		// enemyX weight
		weights[index] = 1d/16d;
		index++;

		// enemyY weight
		weights[index] = 1d/16d;
		index++;

		// enemyXa weight
		weights[index] = 1d/16d;
		index++;

		// enemyYa weight
		weights[index] = 1d/16d;
		index++;

		// environment weights
		for (; index < weights.length; index++) {
			weights[index] = 1;
		}

		return weights;
	}

	private void setWindowListener(final Tgng tgng) {
		CAEAgentHook.view.addWindowListener(new WindowAdapter() {
			/*
			 * abstract class which gets called when play-window is going to be
			 * closed and game will be quit.
			 */

			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {

				/*
				 * each instance of the tgng gets saved in a file on window
				 * closing
				 */
				{
					System.out.println("Saving Tgng:");
					tgng.saveToDisk();
				}

			}
		});
	}
}
