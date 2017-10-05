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
package marioAI.run.options;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.logging.Level;

import marioAI.agents.CAEAgent;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.Goal;
import marioAI.goals.GoalListPlanner;
//import marioAI.goals.IntrinsicMotivationPlanner;
//import marioAI.goals.IntrinsicMotivationUtilities.TypeOfGoals;
//import marioAI.goals.MouseControlledPlanner;
import marioAI.goals.VoicePlanner;
import marioWorld.agents.Agent;
import marioWorld.engine.Logging;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Enemy;
import marioWorld.engine.sprites.Clark;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;

public class CustomLevel extends CommonOptions {

	public CustomLevel(ArrayList <Agent> agents, ArrayList <Player> players, String levelName) {
		super();
		
		options.setAgents(agents);
		options.setPlayers(players);
		
		if (levelName != null && levelName.endsWith(".lvl")) {
			levelName = levelName.substring(0, levelName.length() - 4);
		}
		options.setOwnLevelName(levelName);

		/** settings which should rarely change: */
		options.setMaxFPS(false);
		options.setVisualization(true);
		options.setNumberOfTrials(1);
		options.setMatlabFileName("");
		options.setLevelDifficulty(1);
		options.setLevelRandSeed((int) (Math.random() * Integer.MAX_VALUE));
	}

	// /**
	// * factory for a default setup. Copy, then modify this for your own
	// * settings. Examples for settings might include: randomSeed, agent,
	// * levelName, initialSprites, initialMarioPosition, initialGoals, Loggers,
	// * Visualization
	// *
	// * @return
	// */
	// // DO NOT CHANGE
	// public static CustomLevel defaultSetup() {
	// // DO NOT CHANGE
	// Logging.addLogger("Agent");
	// // DO NOT CHANGE
	// Logging.addLogger("Brain");
	// // DO NOT CHANGE
	// Logging.addLogger("SimulationError");
	// // DO NOT CHANGE
	// Logging.addLogger("Planner");
	// Logging.addLogger("AStarSimulator");
	// Logging.addLogger("Controller");
	// // DO NOT CHANGE
	// Agent agent = new CAEAgent(new MouseControlledPlanner());
	// // DO NOT CHANGE
	// CustomLevel ret = new CustomLevel(agent, null);
	// // DO NOT CHANGE
	// ret.options.setLevelRandSeed((int) (Integer.MAX_VALUE * Math.random()));
	// // DO NOT CHANGE
	// ret.options.setInitialMarioPosition(new GlobalContinuous(32, 32));
	// // DO NOT CHANGE
	// return ret;
	// }

//	public static CustomLevel fixedMotivationSetup() {
//		Logging.addLogger("Agent");
//		Logging.addLogger("Brain");
//		Logging.addLogger("SimulationError");
//		Logging.addLogger("Planner");
//		Logging.addLogger("AStarSimulator");
//		Logging.addLogger("Controller");
//		ArrayList<PlayerWorldObject> mwo = new ArrayList<PlayerWorldObject>();
//		mwo.add(PlayerWorldObject.ENERGY_FRONT);
//		mwo.add(PlayerWorldObject.RED_KOOPA);
//		mwo.add(PlayerWorldObject.GREEN_KOOPA);
//		
//		ArrayList <Agent> agents = new ArrayList<Agent>();
//		ArrayList <Player> players = new ArrayList<Player>();
//		
//		agents.add(new CAEAgent(new WorldObjectPlanner<CAEAgent>(mwo), null));
//		players.add(new Mario(1));
//		
//		CustomLevel ret = new CustomLevel(agents, players, null);
//		
//		ret.options.setLevelRandSeed(1406430883);
//		ret.options.setInitialPlayerPosition(new GlobalContinuous(32, 32));
//		return ret;
//	}

	// public static CustomLevel extrinsicMotivationSetup() {
	// Logging.addLogger("Agent");
	// Logging.addLogger("Brain");
	// Logging.addLogger("SimulationError");
	// Logging.addLogger("Planner");
	// Logging.addLogger("AStarSimulator");
	// Logging.addLogger("Controller");
	// // combine an intrinsic planner with an extrinsic
	// DoublePlanner<CAEAgent, IntrinsicMotivationPlanner,
	// WorldObjectPlanner<CAEAgent>> planner = new DoublePlanner(
	// new IntrinsicMotivationPlanner(TypeOfGoals.STATIC_AND_SPRITES),
	// new WorldObjectPlanner<CAEAgent>());
	// Agent agent = new CAEAgent(planner);
	// CustomLevel ret = new CustomLevel(agent, null);
	// ret.options.setInitialMarioPosition(new GlobalContinuous(32, 32));
	// return ret;
	// }

	// public static CustomLevel motivationSetup() {
	// Logging.addLogger("Agent");
	// Logging.addLogger("Brain");
	// Logging.addLogger("SimulationError");
	// Logging.addLogger("Planner");
	// Logging.addLogger("AStarSimulator");
	// Logging.addLogger("Controller");
	//
	// // Agent agent = new CAEAgent(new NearestObjectPlanner());
	// Agent agent = new CAEAgent(new IntrinsicMotivationPlanner(
	// TypeOfGoals.STATIC_AND_SPRITES));
	//
	// // CustomLevel ret = new CustomLevel(agent, null);
	// // CustomLevel ret = new CustomLevel(agent, "presentation");
	// // CustomLevel ret = new CustomLevel(agent, "testDirection");
	// CustomLevel ret = new CustomLevel(agent, "video1");
	// ret.options.addInitialSprite(createSprite(new
	// GlobalContinuous(690,150),Enemy.ENEMY_LUKO,false,1));
	// ret.options.addInitialSprite(createSprite(new
	// GlobalContinuous(820,150),Enemy.ENEMY_GREEN_KOOPA,false,1));
	// ret.options.setLevelRandSeed((int) (Integer.MAX_VALUE * Math.random()));
	// // CustomLevel ret = new CustomLevel(agent, "presentation");
	// ret.options.setInitialMarioPosition(new GlobalContinuous(50, 0));
	//
	// // ret.options.setLevelRandSeed(2043956928);
	// // example levels: 1873210648 891499214 1260717012 864109516 1376412545
	// 1778962755
	// // ret.options.setInitialMarioPosition(new GlobalContinuous(32, 32));
	// return ret;
	// }

	/*public static CustomLevel setupToShowSpritePositionProblems() {
		Logging.addLogger("Agent");
		Logging.addLogger("Brain");
		Logging.addLogger("SimulationError");
		Logging.addLogger("Planner");
		Logging.addLogger("AStarSimulator");
		Logging.addLogger("Controller");
		
		ArrayList <Agent> agents = new ArrayList<Agent>();
		ArrayList <Player> players = new ArrayList<Player>();
		
		agents.add(new CAEAgent(new IntrinsicMotivationPlanner(TypeOfGoals.STATIC_AND_SPRITES), null));
		players.add(new Mario(1));		
		
		CustomLevel ret = new CustomLevel(agents, players, null);
		ret.options.setLevelRandSeed(1579987072);
		ret.options.setInitialPlayerPosition(new GlobalContinuous(150, 32));
		return ret;
	}*/

	/*public static CustomLevel goalSetup(Goal g) 
	{
		Logging.addLogger("Agent");
		Logging.addLogger("Brain");
		Logging.addLogger("SimulationError");
		Logging.addLogger("Planner");
		Logging.addLogger("AStarSimulator");
		Logging.addLogger("Controller");		
				
		ArrayList <Agent> agents = new ArrayList<Agent>();
		ArrayList <Player> players = new ArrayList<Player>();
		
		agents.add(new CAEAgent(new MouseControlledPlanner(), null));
		players.add(new Mario(1));		
		
		((CAEAgent) agents.get(0)).getAStarSimulator().setGoal(((CAEAgent) agents.get(0)).getPlanner().getLevelSceneAdapter(),g);
		
		CustomLevel ret = new CustomLevel(agents, players, null);
				
		// ret.options.setLevelRandSeed(1204336835); //2078927572 //210612760
		return ret;
	}*/

	/**
	 * Factory for the level testing_track.lvl
	 * 
	 * @author Niels
	 * @return
	 */
	/*public static CustomLevel testingTrack() {
		Logging.addLogger("Agent", Level.OFF);
		Logging.addLogger("Brain", Level.OFF);
		Logging.addLogger("AStarSimulator");
		Logging.addLogger("Controller");

		ArrayList <Agent> agents = new ArrayList<Agent>();
		agents.add(new CAEAgent(new MouseControlledPlanner(), null));
		
		ArrayList <Player> players = new ArrayList<Player>();
		players.add(new Mario(1));		
		
		CustomLevel ret = new CustomLevel(agents, players, "testing_track");
				
		ret.options.setInitialPlayerPosition(new GlobalContinuous(32, 223));
		ret.options.addInitialSprite(createSprite(new GlobalContinuous(119 * 16 + 8, 12 * 16), Enemy.ENEMY_GREEN_KOOPA, false, -1));
		ret.options.addInitialSprite(createSprite(new GlobalContinuous(122 * 16 + 8, 13 * 16), Enemy.ENEMY_SHALLY, false, -1));
		ret.options.addInitialSprite(createSprite(new GlobalContinuous(36 * 16, 11 * 16), Enemy.ENEMY_RED_KOOPA, false, 1));
		return ret;
	}*/

	/**
	 * Factory for the level testing_track.lvl
	 * 
	 * @author Niels
	 * @return
	 */
	/*public static CustomLevel testingTrack2() {
		Logging.addLogger("Agent", Level.OFF);
		Logging.addLogger("Brain", Level.OFF);
		Logging.addLogger("AStarSimulator");
		Logging.addLogger("Controller");

		ArrayList <Agent> agents = new ArrayList<Agent>();
		agents.add(new CAEAgent(new MouseControlledPlanner(), null));
		
		ArrayList <Player> players = new ArrayList<Player>();
		players.add(new Mario(1));		
		
		// CustomLevel ret = new CustomLevel(agent, "testing_track");
		// //"testLearningError"
		// ret.options.setInitialMarioPosition(new GlobalContinuous(1196, 223));
		// //1192, 111

		CustomLevel ret = new CustomLevel(agents, players, "testLearningError");
		ret.options.setInitialPlayerPosition(new GlobalContinuous(85, 223));

		// ret.options.addInitialSprite(createSprite(new GlobalContinuous(
		// 119 * 16 + 8, 12 * 16), Enemy.ENEMY_GREEN_KOOPA, false, -1));
		// ret.options.addInitialSprite(createSprite(new GlobalContinuous(
		// 122 * 16 + 8, 13 * 16), Enemy.ENEMY_SHALLY, false, -1));
		// ret.options.addInitialSprite(createSprite(new GlobalContinuous(36 *
		// 16,
		// 11 * 16), Enemy.ENEMY_RED_KOOPA, false, 1));
		return ret;
	}*/

	/**
	 * Factory for the presentation setup.
	 * 
	 * @return
	 */
//	public static CustomLevel movementPresentation() {
//		Logging.addLogger("Agent", Level.OFF);
//		Logging.addLogger("Brain", Level.OFF);
//		Logging.addLogger("AStarSimulator");
//		Logging.addLogger("Controller");
//
//		ArrayDeque<Goal> goalList = new ArrayDeque<Goal>();
//		goalList.add(new Goal(new GlobalCoarse(11, 5), false));
//		goalList.add(new Goal(new GlobalCoarse(100, 11), false));
//		GoalListPlanner list = new GoalListPlanner(goalList);
//
//		ArrayList <Agent> agents = new ArrayList<Agent>();
//		agents.add(new CAEAgent(list, null));
//		
//		ArrayList <Player> players = new ArrayList<Player>();
//		players.add(new Mario(1));		
//		
//		CustomLevel ret = new CustomLevel(agents, players, "movementPresentationTest");
//		ret.options.setInitialPlayerPosition(new GlobalContinuous(64, 223));
//		return ret;
//	}

	/*public static CustomLevel heuristicError() {
		Logging.addLogger("Agent");
		Logging.addLogger("Brain");
		Logging.addLogger("AStarSimulator");
		Logging.addLogger("Controller");
		
		ArrayList <Agent> agents = new ArrayList<Agent>();
		agents.add(new CAEAgent(new MouseControlledPlanner(), null));
		((CAEAgent) agents.get(0)).getAStarSimulator().setGoal(((CAEAgent) agents.get(0)).getPlanner().getLevelSceneAdapter(),new Goal(new GlobalCoarse(6, 1), false));

		ArrayList <Player> players = new ArrayList<Player>();
		players.add(new Mario(1));				
		
		CustomLevel ret = new CustomLevel(agents, players, "testing_track");
		
		ret.options.setInitialPlayerPosition(new GlobalContinuous(32, 223));
		return ret;
	}*/

//	public static CustomLevel learningError() {
//		Logging.addLogger("Agent");
//		Logging.addLogger("Brain");
//		Logging.addLogger("AStarSimulator");
//		Logging.addLogger("Controller");
//		ArrayDeque<Goal> goalList = new ArrayDeque<>();
//		// goalList.add(new Goal(new GlobalCoarse(73, 6)));
//		// goalList.add(new Goal(new GlobalCoarse(73, 5)));
//		GoalListPlanner list = new GoalListPlanner(goalList);
//
//		ArrayList <Agent> agents = new ArrayList<Agent>();
//		agents.add(new CAEAgent(list, null));
//		
//		ArrayList <Player> players = new ArrayList<Player>();
//		players.add(new Mario(1));		
//		
//		CustomLevel ret = new CustomLevel(agents, players, "testing_track");
//		
//		// ret.options.setInitialMarioPosition(new GlobalContinuous(1215, 223));
//		ret.options.setInitialPlayerPosition(new GlobalContinuous(1203, 111));
//		return ret;
//	}
//
//	public static CustomLevel learningError2() {
//		Logging.addLogger("Agent");
//		Logging.addLogger("Brain");
//		Logging.addLogger("AStarSimulator");
//		Logging.addLogger("Controller");
//		ArrayDeque<Goal> goalList = new ArrayDeque<>();
//		GoalListPlanner list = new GoalListPlanner(goalList);
//				
//		ArrayList <Agent> agents = new ArrayList<Agent>();
//		agents.add(new CAEAgent(list, null));
//		
//		ArrayList <Player> players = new ArrayList<Player>();
//		players.add(new Mario(1));		
//		
//		CustomLevel ret = new CustomLevel(agents, players, "testLearningError");
//		
//		ret.options.setInitialPlayerPosition(new GlobalContinuous(84, 223));
//		return ret;
//	}

	/**
	 * Factory for the level direction_testing.lvl
	 * 
	 * @author Niels, Eduard
	 * @return
	 */
//	public static CustomLevel directionTesting() {
//		Logging.addLogger("Agent", Level.OFF);
//		Logging.addLogger("Brain", Level.OFF);
//		Logging.addLogger("AStarSimulator");
//		Logging.addLogger("Controller");
//
//		Goal energyLeft = new Goal(new GlobalCoarse(8, 10), CollisionDirection.LEFT, false, false);
//		Goal energyBelow = new Goal(new GlobalCoarse(16, 10), CollisionDirection.BELOW, false, false);
//		Goal energyAbove = new Goal(new GlobalCoarse(24, 10), CollisionDirection.ABOVE, false, false);
//		Goal energyRight = new Goal(new GlobalCoarse(32, 10), CollisionDirection.RIGHT, false, false);
//
//		ArrayDeque<Goal> goalList = new ArrayDeque<>(4);
//		// goalList.add(new Goal(new GlobalContinuous(80, 223)));
//		// goalList.add(new Goal(new GlobalContinuous(8, 223)));
//		// goalList.add(new Goal(new GlobalContinuous(80, 223)));
//		goalList.add(energyLeft);
//		goalList.add(energyBelow);
//		goalList.add(energyAbove);
//		goalList.add(energyRight);
//
//		GoalListPlanner list = new GoalListPlanner(goalList);
//
//		ArrayList <Agent> agents = new ArrayList<Agent>();
//		agents.add(new CAEAgent(list, null));
//
//		ArrayList <Player> players = new ArrayList<Player>();
//		players.add(new Mario(1));		
//		
//		CustomLevel ret = new CustomLevel(agents, players, "direction_testing");
//		
//		ret.options.setInitialPlayerPosition(new GlobalContinuous(32, 223));
//		// ret.options.addInitialSprite(createSprite(48, 223,
//		// Enemy.ENEMY_RED_KOOPA, false, 1));
//		return ret;
//	}

	/**
	 * factory for a test case. By Stephan, date: 22.05.13
	 * 
	 * @return
	 */
	/*public static CustomLevel testLearningForwardModel_22_05_13() {
		Logging.addLogger("Agent");
		Logging.addLogger("Brain");
		Logging.addLogger("SimulationError");
		Logging.addLogger("AStarSimulator");
		Logging.addLogger("Controller");
		
		ArrayList <Agent> agents = new ArrayList<Agent>();
		agents.add(new CAEAgent(new MouseControlledPlanner(), null));
		
		ArrayList <Player> players = new ArrayList<Player>();
		players.add(new Mario(1));		
		
		CustomLevel ret = new CustomLevel(agents, players, "testInteraction"); // "testInteraction"
		
		//if mario's starting position is not set, mario starts at (32,0) and falls down. if set to an unavailable position, the game will not start. TODO:
		//catch this.
		ret.options.setInitialPlayerPosition(new GlobalContinuous(260, 150));
		ret.options.addInitialSprite(createSprite(new GlobalContinuous(100, 150), Enemy.ENEMY_LUKO, false, 1));
		return ret;
	}*/
	
	/**
	 * Factory to test voice control with map testInteraction.lvl
	 */
//	public static CustomLevel testVoiceControl(){
//		Logging.addLogger("Agent");
//		Logging.addLogger("Brain");
//		Logging.addLogger("SimulationError");
//		Logging.addLogger("AStarSimulator");
//		Logging.addLogger("Controller");
//		Logging.addLogger("Planner");
//		Logging.addLogger("Interpreter");
//		Logging.addLogger("Recognizer");
//		
//		ArrayList <Agent> agents = new ArrayList<Agent>();
//		VoicePlannerImplementation planner1 = new VoicePlannerImplementation();
//		agents.add(new CAEAgent("Mario", planner1, /*new VoiceControl(), */ planner1));
//		
//		ArrayList <Player> players = new ArrayList<Player>();
//		players.add(new Mario(1));		
//		
//		CustomLevel ret = new CustomLevel(agents, players, "level0"); // "testInteraction"
//		
//		ret.options.setInitialPlayerPosition(new GlobalContinuous(260, 150));
//		ret.options.addInitialSprite(createSprite(new GlobalContinuous(100, 150), Enemy.ENEMY_LUKO, false, 1));
//		return ret;
//	}

	/**
	 * This sprite is just used to carry information, another sprite will be created later on.
	 * 
	 * @param x
	 * @param y
	 * @param kind
	 * @param isWinged
	 * @return
	 */
	private static Sprite createSprite(GlobalContinuous position, int kind, boolean isWinged, int facing) {
		GlobalCoarse spriteMapPos = position.toGlobalCoarse();
		return new Enemy(null, position, spriteMapPos, facing, kind, isWinged);
	}
	
	/*public static CustomLevel reservoirMap() {
		
			Logging.addLogger("Agent");
			Logging.addLogger("Brain");
			Logging.addLogger("SimulationError");
			Logging.addLogger("Planner");
			Logging.addLogger("AStarSimulator");
			Logging.addLogger("Controller");
			ArrayList<MarioWorldObject> mwo = new ArrayList<MarioWorldObject>();
			mwo.add(MarioWorldObject.ENERGY_FRONT);
			mwo.add(MarioWorldObject.RED_KOOPA);
			mwo.add(MarioWorldObject.GREEN_KOOPA);
			Agent agent = new CAEAgent(new WorldObjectPlanner<CAEAgent>(mwo), null);
			CustomLevel ret = new CustomLevel(agent, "reservoirMap");
			ret.options.setLevelRandSeed(1406430883);
			ret.options.setInitialMarioPosition(new GlobalContinuous(32, 32));
			ret.options.addInitialSprite(createSprite(new GlobalContinuous(119 * 16 + 8, 12 * 16), Enemy.ENEMY_GREEN_KOOPA, false, -1));
			ret.options.addInitialSprite(createSprite(new GlobalContinuous(122 * 16 + 8, 13 * 16), Enemy.ENEMY_SHALLY, false, -1));
			ret.options.addInitialSprite(createSprite(new GlobalContinuous(36 * 16, 11 * 16), Enemy.ENEMY_RED_KOOPA, false, 1));
			
			return ret;
		
	}*/
}
