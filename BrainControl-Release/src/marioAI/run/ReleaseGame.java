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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mario.main.PlayRelease;
import mario.main.Settings;
import mario.main.Settings.LevelSettings;
import mario.main.Settings.PlayerSettings;
import mario.main.StartJar.ExitCode;
import marioAI.agents.BrainAgent;
import marioAI.agents.BrainAgentHook;
import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.CommonGround;
import marioAI.goals.GoalPlanner;
import marioAI.goals.VoicePlanner;
import marioAI.goals.VoicePlanner;
import marioAI.run.PlayHook;
import marioAI.run.options.CustomLevel;
import marioUI.gui.AgentConfiguration;
import marioUI.gui.GameEndHandler;
import marioUI.gui.StartGuiController;
import marioUI.gui.StartGuiModel;
import marioWorld.agents.Agent;
import marioWorld.engine.GameWorld;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.BlockingType;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Jay;
import marioWorld.engine.sprites.Clark;
import marioWorld.engine.sprites.Bruce;
import marioWorld.engine.sprites.Peter;
import marioWorld.tools.EvaluationOptions;

public class ReleaseGame extends PlayHook implements Runnable
{
	public static StartGuiController controller;
	private EvaluationOptions options;
	public GameEndHandler geh = new GameEndHandler();
	private String levelName;
	public static Thread t = new Thread();
	
	public ReleaseGame(StartGuiController controller, ArrayList<AgentConfiguration> agentCongigurations, String levelName) 
	{
		super(true);
		this.levelName = levelName;
		classesWithStaticStuff.add(this.getClass());
		
		ReleaseGame.controller = controller;
		
		int i=0;	//TODO: hack. the player index is not handled consequently. thus, we count the number of players here.
		
		for(AgentConfiguration ac : agentCongigurations)
		{
			if(ac.getPlannerType().equals(StartGuiModel.PlannerTypes.VOICE))
			{
				VoicePlanner planner = new VoicePlanner();
				switch(ac.getKnowledgeType())
				{
				case NONE:
					agents.add(new CAEAgent(ac.getPlayerName(), i, planner, null,voiceControl, planner));
					break;
				case LITTLE:
					PlayerWorldObject currentPlayer = PlayerWorldObject.getElement(ac.getPlayerName());
					agents.add(new CAEAgent(ac.getPlayerName(), i, planner, createLittleInitialKnowledge(currentPlayer),voiceControl, ac.getMicrophone(), planner));
					break;
				case MUCH:
					agents.add(new CAEAgent(ac.getPlayerName(), i, planner, createMuchInitialKnowledge(),voiceControl, ac.getMicrophone(), planner));
					break;
				}

				switch(ac.getPlayerType())
				{
				case CLARK:
					players.add(new Clark(i, 1));
					break;
				case BRUCE:
					players.add(new Bruce(i, 1));
					break;
				case PETER:
					players.add(new Peter(i, 1));
					break;
				case JAY:
					players.add(new Jay(i, 1));
					break;
				}
				
			}
			
			i++;
		}
		

		// if this contains call of powerSet, it leads to NullPointerException, but why?
//----- initialization of strategy discussion resources
		
		// build common grounds between all possible combinations of at least two players
		ArrayList<CAEAgent> caeAgents = new ArrayList<>();
		for (Agent agent : agents) {
			if(agent instanceof CAEAgent) caeAgents.add((CAEAgent) agent);
		}
		ArrayList<ArrayList<CAEAgent>> playerCombinations = powerSet(caeAgents);
		for (ArrayList<CAEAgent> combination : playerCombinations) {
			if (combination.size() <= 1) continue;
			System.out.print("Building Common Ground for ");
			CommonGround newCG = new CommonGround(combination);
			for (CAEAgent caeAgent : combination) {
				caeAgent.getPlanner().addCommonGround(newCG);
				System.out.print(caeAgent.getName() + " ");
			}
			System.out.println();
		}
		// make the voice planner observe each other
		for (Agent agent1 : agents) {
			for (Agent agent2 : agents){
				if (agent1.getPlayerIndex() != agent2.getPlayerIndex()){
					VoicePlanner planner1 = (VoicePlanner) ((CAEAgent) agent1).getPlanner();
					VoicePlanner planner2 = (VoicePlanner) ((CAEAgent) agent2).getPlanner();
					planner1.addObserver(planner2);
					System.out.println("SD: " + agent2.getName() + " is now observing " + agent1.getName());
				}
			}
		}
		
//-----------
				

		voiceControl.init();

		
		this.options = new CustomLevel(agents, players, levelName ).getOptions();
		
		//FABIAN: CHECK
		//this.options.setLevelType(Integer.parseInt(""+levelName.charAt(12)));
		this.options.setLevelRandSeed(1889574193);

		t = new Thread(this);
		t.start();
	}

	private Map<ConditionDirectionPair, TreeMap<Effect, Double>> createMuchInitialKnowledge() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Once the GameWorld is prepared, set initial Player settings
	 */
	public void OnGameWorldPrepared(GameWorld component) {
		LevelSettings ls = Settings.getLevelSettings(levelName);
		GlobalContinuous[] playerStartPositions = new GlobalContinuous[ls.players.size()];
		int[] playerStartEnergy = new int[ls.players.size()];
		int i = 0;
		for (PlayerSettings player : ls.players) {
			playerStartPositions[i] = new GlobalCoarse(player.startPosX, player.startPosY).toGlobalContinuous();
			playerStartEnergy[i] = player.startEnergy;
			i++;
		}
		
		PlayerHealthCondition[] playerHealthConditions = {
				PlayerHealthCondition.LARGE_NON_BULB,
				PlayerHealthCondition.LARGE_NON_BULB,
				PlayerHealthCondition.LARGE_NON_BULB,
				PlayerHealthCondition.LARGE_NON_BULB,
				PlayerHealthCondition.LARGE_NON_BULB };

		int[] playerFacing = new int[] { 1, -1, 1, -1, 1 };

		initPlayers(component, playerStartPositions, null, playerFacing, playerHealthConditions, playerStartEnergy);
		controller.addSprites(component);
//		addSprite(component, PlayerWorldObject.END_ITEM, new GlobalCoarse(5, 12).toGlobalContinuous());
		this.addEndItemtoEnd(component);
		
	}
	

	/**
	 * Adds end item to the end of a level. Assumes: item will always be placed at the bottom
	 * @param component
	 */
	private void addEndItemtoEnd(GameWorld component) 
	{
		byte[][] map = GlobalOptions.realLevelScene.level.map;
		int x = map.length-1;
		int y = map[0].length-4;
		
//		for(int i = 0; i < map.length; i++){
//			if(map[i][14].)
//		}
//		for (int i = x ; i >= 0;i--){
//			if (map[i][y] != 0) {
//				x = i;
//				break;
//			}
//		}

		GlobalCoarse endItemPosCoarse = new GlobalCoarse(222,14);
		GlobalContinuous endItemPos = endItemPosCoarse.toGlobalContinuous();
		addSprite(component, PlayerWorldObject.END_ITEM, endItemPos);
		
	}

	public static Map<ConditionDirectionPair, TreeMap<Effect, Double>> createLittleInitialKnowledge(PlayerWorldObject currentplayer) 
	{
		Map<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge = new TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>>();
		TreeMap<Effect, Double> effects = new TreeMap<Effect, Double>();
		effects.put(new Effect(ActionEffect.ENERGY_INCREASE,
				currentplayer), 1.0);
		knowledge.put(
				new ConditionDirectionPair(new Condition(
						currentplayer,
						PlayerWorldObject.ENERGY_FRONT,
						PlayerHealthCondition.LARGE_NON_BULB),
						CollisionDirection.BELOW), effects);

		TreeMap<Effect, Double> effects2 = new TreeMap<Effect, Double>();
		effects2.put(new Effect(ActionEffect.OBJECT_CREATION,
				PlayerWorldObject.BULB_FLOWER), 1.0);
		knowledge.put(
				new ConditionDirectionPair(new Condition(
						currentplayer,
						PlayerWorldObject.QUESTIONMARK_BLOCK1,
						PlayerHealthCondition.LARGE_NON_BULB),
						CollisionDirection.BELOW), effects2);

		return knowledge;
	}

	public static void deleteStaticAttributes() {
		PlayHook.deleteStaticAttributes();
	}
	
	public static void resetStaticAttributes(){
		PlayHook.resetStaticAttributes();
		controller = null;
	}
	
	public static void restartApplication(int i)
	{
		//TODO: this function is untested for using JAR files!!!
		
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		
		URI path = null;
		try
		{
			path = PlayRelease.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		} 
		catch (URISyntaxException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/* Build command: java -jar application.jar */
		final ArrayList<String> command = new ArrayList<String>();
				
		command.add(javaBin);
		command.add("-classpath");
		command.add(System.getProperty("java.class.path"));
		command.add("mario.main.PlayRelease");
		
		// Command Line Argument to start correct Level
		if(i == 1){command.add("1");}
		if(i == 2){command.add("2");}
		if(i == 3){command.add("3");}
		// Start Epilog
		// ONLY AVAILABLE VIA COMMANDLINE-ARGUMENT!
		if(i == 4){command.add("4");}

		
		final ProcessBuilder builder = new ProcessBuilder(command);
		
		//go to local directory:		
		builder.directory(new File(System.getProperty("user.dir")));
		
		System.out.println("WORKING DIRECTORY: "+builder.directory());
		System.out.println("EXECUTING: "+command);		
		
		Process proc=null;
		try
		{
			proc = builder.start();
		} 
		catch (IOException e)
		{
			  // TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		//use this for debugging:
//		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
//		StringBuilder sb = new StringBuilder();
//		String line = null;
//		try
//		{
//			while ( (line = reader.readLine()) != null) 
//			{
//				sb.append(line);
//				sb.append(System.getProperty("line.separator"));
//			}
//		} 
//		catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String result = sb.toString();		
//		System.out.println(result);
		  
		System.exit(0);
	}	
	
	
	public static void stop(ExitCode exitCode)
	{

		// -1: Back to Menu
		int levelNumber = -1; 
		
		// used to determine which level was played last
		// and to start the next level
		if(exitCode == ExitCode.LEVEL_SOLVED){
			if(marioUI.gui.View.levelBeaten()){
				restartApplication(levelNumber);
			}
			else{	
		if(PlayRelease.getCurrentLevel() == "releaseLevel0"){
			levelNumber = 1; 
		}
	
		if(PlayRelease.getCurrentLevel() == "releaseLevel1"){
			levelNumber = 2; 
		}
		
		if(PlayRelease.getCurrentLevel() == "releaseLevel2"){
			levelNumber = 3; 
		}
			}
		}
		
		// TO DO: Write Level 5 Epilog
		// Game-Solved. Start with Level 5 (Epilog)
		if(exitCode == ExitCode.GAME_SOLVED){			
				levelNumber = 4; 
			
		}
		
		if(exitCode == ExitCode.QUIT){
			
			// wantQuit will always ask the player if he wants to quit
			if(marioUI.gui.View.wantQuit()){
				//save all the knowledge to file
				for(Agent agent : PlayHook.agents){
					if(agent instanceof BrainAgent) {
						
						 ((BrainAgent)agent).getBrain().saveToDisk(null);
						
					}
					
					
				}
				// Quits the Game
				System.exit(1);
			}
			
			
			
			
		}
		
		else{
		// Restarts
		restartApplication(levelNumber);
		
		}
		
//		// all kinds of things can go wrong in the code below, due to lack of time,
//		// we simply kill the runtime, sorry, maybe some day we will provide a cleaner solution...


	
//		if(exitCode == ExitCode.GAME_SOLVED){
//			System.out.println("Game-Solved-Exit");
//			
//			// ICH BIN ANGELEGT IN main/StartJar
//			System.exit(exitCode.ordinal());
//		}
//		
//	
//		
//		// rest not used 
//
//		//GlobalOptions.getGameWorld().stop();
//		
//		//voiceControl.stop();
//		
//		//controller.gameFinished();
//		
//		//System.exit(1);
//		
//		
//		//TODO: this so so awful!
////		for(Class<Object> c : (ArrayList<Class<Object>>)ResetStaticInterface.classesWithStaticStuff.clone())
////		{
////			try 
////			{
////				Method m = c.getMethod("deleteStaticAttributes");
////				try 
////				{
////					m.invoke(null, (Object[])null);
////				} 
////				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
////				{
////					e.printStackTrace();
////				}
////			} 
////			catch (NoSuchMethodException | SecurityException e) 
////			{
////				e.printStackTrace();
////			}
////		}
//		

	}

	@Override
	public synchronized void run() {
		run(options);
	}

	/**
	 * 
	 * @param givenSet
	 * @return power set of givenSet
	 */
	public static <T> ArrayList<ArrayList<T>> powerSet (ArrayList<T> givenSet){
		ArrayList<ArrayList<T>> powerSet = new ArrayList<>();
		if(givenSet.isEmpty()) {
			powerSet.add(new ArrayList<T>());
		} else {
			T head = givenSet.get(0);
			ArrayList<T> rest = givenSet;
			rest.remove(head);
			for (ArrayList<T> subset : powerSet(rest)) {
				ArrayList<T> subsetPlusHead = new ArrayList<>();
				subsetPlusHead.add(head);
				subsetPlusHead.addAll(subset);
				powerSet.add(subsetPlusHead);
				powerSet.add(subset);
			}
		}
		return powerSet;
	}
}
