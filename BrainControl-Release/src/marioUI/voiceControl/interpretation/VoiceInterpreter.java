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
package marioUI.voiceControl.interpretation;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.cmu.sphinx.frontend.filter.Preemphasizer;
import mario.main.StartJar.ExitCode;
import marioAI.agents.CAEAgent;
import marioAI.agents.CAEAgentHook;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.Generalizer;
import marioAI.brain.Brain.KnowledgeSource;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.Goal;
import marioAI.goals.MovementPlanner;
import marioAI.goals.VoicePlanner;
import marioAI.goals.VoicePlanner;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirAction;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirParameter;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioAI.run.PlayHook;
import marioAI.run.ReleaseGame;
import marioUI.speechSynthesis.KnowledgeResponse;
import marioUI.speechSynthesis.VoiceResponse;
import marioUI.speechSynthesis.VoiceResponseType;
import marioUI.voiceControl.VoiceControl;
import marioUI.voiceControl.VoicePerson;
import marioUI.voiceControl.input.VoiceRecognitionMessage;
import marioWorld.agents.Agent;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeCoarse;

/**
 * VoiceInterpreter converts voice input strings to voice command objects of a
 * specific type.
 * 
 * The class discriminates between: changing reservoirs, setting of goals,
 * giving feedback, querying knowledge.
 * 
 * The commands are forwarded to back to agent and executed by the corresponding
 * voice planner.
 * 
 * @author Chantal, Katrin, Mihael
 */

public class VoiceInterpreter extends Observable 
{
	public static final int LOW_PRIZE = 5;
	public static final int MIDDLE_PRIZE = 10;
	public static final int HIGH_PRIZE = 15;

	Agent lastAgent;
	
	VoiceRecognitionMessage lastMsg = null;
	
	/**
	 * determine whether to search locally or globally for the existence of a
	 * target (to check whether goal is reachable)
	 */
	public boolean useLocalObservation = true;

	/**
	 * (de)activates is it true that sentences
	 */
	public boolean feedbackSeeking = false;

	/**
	 * commands are sent to an agent, which then executes interpreted commands
	 * either on its own (direction commands) or sends them to voice planner
	 */
	private ArrayList<Agent> agents;

	/**
	 * Currently active agent (will be determined during interpretation process
	 * every single time)
	 */
	private CAEAgent activeAgent;

	/**
	 * Agents need a name to process voice command
	 */

	private VoiceControl voiceControl;

	/**
	 * true if last command was an exit command, false otherwise
	 */
	private boolean exitReceived;

	/**
	 * true if there was a knowledge query created by reservoir planner, false
	 * otherwise
	 */
	private boolean knowledgeQueryReceived;

	/**
	 * stores knowledge query created by reservoir planner
	 */
	Entry<ConditionDirectionPair, Entry<Effect, Double>> knowledgeQuery;
	/**
	 * LevelSceneAdapter to keep energy levels updated
	 */
	LevelSceneAdapter levelSceneAdapter;
	/**
	 * Default Constructor
	 * 
	 * @param voiceControl
	 */
	public VoiceInterpreter(ArrayList<Agent> agents, VoiceControl voiceControl) 
	{
		this.agents = agents;
		this.voiceControl = voiceControl;
		this.knowledgeQueryReceived = false;
		this.exitReceived = false;
		
		addObserver(PlayHook.voiceResponseEventBus);
	}

	public VoiceInterpreter() 
	{
		Logging.log("Interpreter", "Interpreter started through configuration manager. Make sure .init is called");
		
		addObserver(PlayHook.voiceResponseEventBus);		
	}

//	public void init(ArrayList<Agent> agents, VoiceControl voiceControl) 
//	{
//		this.agents = agents;
//		this.voiceControl = voiceControl;
//		this.knowledgeQueryReceived = false;
//		this.exitReceived = false;
//	}

	private boolean payEnergys(int amount)
	{
		boolean couldPay = activeAgent.payEnergys(amount);
		if(levelSceneAdapter != null)
			levelSceneAdapter.createAndSetNewSimulatedLevelScene(activeAgent.getBrain());
		
		return couldPay;
	}
	
	/**
	 * Coordinates exit commands and knowledge queries from reservoir planner
	 * TODO: rewrite, should go to voice control
	 * @param msg
	 */
	public void processRecognitionResult(VoiceRecognitionMessage msg) 
	{
		if (msg != null) 
		{
			//TODO: this is dirty and obsolete... why do voicerecognition messages do not have separate "tag-type" and "tag-parameter" variables?
			
			String tags = msg.getTag();
						
			if (exitReceived) 
			{
				// if there has been an exit command
				// and answer is yes then know exiting game
				// if the answer is sth else, pass it to interpreter
				if (tags.split(" ")[0].equals("CONTROL(YES)")) 
				{
					voiceControl.display("yes", -1);
					voiceControl.display("Goodbye", 0);
					ReleaseGame.stop(ExitCode.QUIT);
				} 
				else 
				{
					exitReceived = false;
					//voiceControl.display(msg.getCommand(), 1);
					//interpretCommandType(msg.getTag());
					processRecognitionResult(msg);
				}
			} 
			else if (tags.split(" ")[0].equals("CONTROL(EXIT)")) 
			{		 
				// if exit command received
				this.exitReceived = true;
				voiceControl.display("exit", -1);
				voiceControl.display("Do you really want to exit the game?", 0);

			} 
//	OBSOLETE
//			else if (knowledgeQueryReceived) {
//				// if there has been a knowledge query 
//				// and now the answer is "yes", knowledge is adapted
//				// if the answer is sth else, msg will be passed to interpreter
//				if (tags.split(" ")[0].equals("yes") && knowledgeQuery != null) 
//				{
//					//Generalizer.generalizeSingleEffect(knowledgeQuery, activeAgent.getBrain().getKnowledge(), false,true);
//					this.activeAgent.getBrain().updateKnowledge(knowledgeQuery, KnowledgeSource.FULLY_TRUSTED);
//					voiceControl.display("Ok, thank you!", 0);
//				} else {
//					knowledgeQuery = null;
//					knowledgeQueryReceived = false;
//					interpretTags(msg);					
//				}
//			} 
			else 
			{
				// only show command if we actually process it
				voiceControl.display(msg.getCommand(), -1);
				//interpretCommandType(msg.getTag());
				interpretTags(msg);
			}
		}
	}

	/**
	 * Called by planner directly, if there is a voice query (curiosity
	 * reservoir)
	 * 
	 * saves query, displays question
	 * 
	 * DEPRECATED
	 * 
	 * @param knowledgeQuery
	 */
	/*public void processKnowledgeQuery(Entry<ConditionDirectionPair, Entry<Effect, Double>> knowledgeQuery,
			CAEAgent agent) {

		if (knowledgeQuery != null && feedbackSeeking) {

			this.activeAgent = agent;

			VoiceControl.voiceKnowledgeManager.setAgent(agent);

			String answer = VoiceControl.voiceKnowledgeManager.createSentence(knowledgeQuery);

			if (answer != null
					&& answer != ""
					&& !answer.trim().toLowerCase()
							.equals(new KnowledgeResponse("DONTKNOW").toString().trim().toLowerCase())) {
				voiceControl.display("Is it true that " + answer, activeAgent);

				this.knowledgeQueryReceived = true;
				this.knowledgeQuery = knowledgeQuery;
			} else {
				Logging.logInfo("Interpreter", activeAgent.getName() + " didn't know what to say");
			}
		}

	}*/

	/**
	 * transfers input string to sortTags() and reads command type from created
	 * hash map. processes tags to specific command creation method.
	 * 
	 * @param tags
	 *            : sequence of tags (generated while passing the grammar after
	 *            giving a voice input)
	 */
//	private void interpretCommandType(String tags) {
//		// Split tag with information to process
//		if (tags != null && tags != "") {
//			if (tags.contains("ENERGY_TRANSFER")) {
//				if (activeAgent.payEnergys(HIGH_PRIZE)) {
//					// TODO: wie anderen Agent rausfinden?
//					CAEAgent otherAgent = (CAEAgent) VoicePerson.agentSelector(tags.split(" ")[1], agents);
//					otherAgent.increaseEnergys(HIGH_PRIZE);
//				} else {
//					this.randomNoEnergyResponse();
//				}
//			} else if (tags.contains("RES_START")) {
//				voiceControl.setReservoirActivation(true);
//			} else if (tags.contains("RES_STOP")) {
//				voiceControl.setReservoirActivation(false);
//			}
//		}
//	}

	/**
	 * Determine command type and proceed to specific command creation method
	 * 
	 * @param msg
	 *            VoiceRecogntionMessage
	 */
	private void interpretTags(VoiceRecognitionMessage msg) {
		

		if (msg == null)
			return;

		Logging.logInfo("Interpreter", "Processing \"" + msg.getTag() + "\"");
		Map<String, String> tags = sortTags(msg.getTag());

		if (tags == null)
			return;
		//TODO: player bfangen der nicht da ist
		//FIXME: move this to voicepersons again
		Agent agent = selectAgent(tags);
		


		//agent not in game
		if (agent == null)
			return;		
		
		activeAgent = (CAEAgent) agent;
		levelSceneAdapter = this.activeAgent.getPlanner().getLevelSceneAdapter();
		VoiceControl.voiceKnowledgeManager.setAgent(activeAgent);
		
		if (tags.containsKey("GOAL"))
			interpretGoal(tags);
		else if (tags.containsKey("RESERVOIR"))
			interpretReservoir(tags);
		else if (tags.containsKey("MOVEMENT"))
			interpretMovement(tags);
		else if (tags.containsKey("KNOWLEDGE"))
			interpretKnowledge(tags);
		else if (tags.containsKey("SMALLTALK"))
			interpretSmallTalk(tags);
		else if (tags.containsKey("CONTROL"))
		{
			//DIRTY PLACEMENT
			if(tags.get("CONTROL").equalsIgnoreCase("REPEAT"))
			{
				interpretTags(this.lastMsg);
				return;	//avoid saving the "repeat" message
			}
			else			
				gameControl(tags);
		}
		
		lastMsg=msg;
	}

	private Agent selectAgent(Map<String, String> tags) 
	{
		Agent agent = null;

		if (tags.containsKey("PLAYER")) 
		{
			String player = tags.get("PLAYER");
			
			for (int i = 0; i < agents.size(); i++) 
			{
				if (player.toUpperCase().contains(agents.get(i).getName().toUpperCase()))
				{
					agent = agents.get(i);
					CAEAgentHook.view.changeVisiblePlayer(i);
					Logging.logInfo("Interpreter", "passing commands to " + agent.getName());
				}
			}
		} 
		else 
		{
			if(lastAgent!=null)			
			{
				agent = agents.get(GlobalOptions.playerGameWorldToDisplay);
				Logging.logInfo("Interpreter", "No information about agent given. Last was " + lastAgent.getName() + "'.");
			}
			else
			{				
				agent = agents.get(GlobalOptions.playerGameWorldToDisplay);
				Logging.logInfo("Interpreter", "No information about agent given. Default is " + agent.getName() + "'.");
			}
		}
		if(agent != null) {
			lastAgent = agent;
		}
		

		// should not be really happening
		if (!(agent instanceof CAEAgent) || !(((CAEAgent) agent).getPlanner() instanceof VoicePlanner)) {
			Logging.logSevere("Interpreter", "This player is incapable of speaking ...");
			//System.exit(1);
		}
	
		return agent;
	}

	private Map<String, String> sortTags(String tags) {

		Map<String, String> sortedTags = new HashMap<String, String>();

		String[] bagOfTags = tags.split(" ");

		// checks if tags follow the TYPE(VALUE) scheme
		// TODO: check for tags without values
		if (tags.split(Pattern.quote("("), -1).length != tags.split(Pattern.quote(")"), -1).length) {
			Logging.logInfo("Interpreter", "Malformed tags");
			return null;
		}

		// loop from the end to avoid collisions if skipper is present
		for (int i = bagOfTags.length - 1; i >= 0; i--) {

			String tag = bagOfTags[i];
			String t_type, t_val;

			
//			if (!tag.toUpperCase().contains("SKIPPER")) 
//			{
				t_type = tag.substring(0, tag.indexOf("(")).toUpperCase().trim();
				t_val = tag.substring(tag.indexOf("(") + 1, tag.indexOf(")")).toUpperCase().trim();
//			}
//			else 
//			{ //hack for distinguishing if a MWO has a role of an object, subject or target
//				t_type = tag.substring(tag.indexOf("(") + 1, tag.indexOf(")")).toUpperCase().trim(); //value of skipper is tag type
//				t_val = bagOfTags[--i].substring(bagOfTags[i].indexOf("(") + 1, bagOfTags[i].indexOf(")"))
//						.toUpperCase().trim(); //value of previous tag in the bag
//			}

			sortedTags.put(t_type, t_val);
		}

		return sortedTags;
	}

	/*@Deprecated
	private void createResponse(String tag, String object) 
	{
		//TODO: use grammars!!
		
		if (object != null) {
			object = object.toLowerCase();
			object = object.replace("_front", " ");
			object = object.replace("_", " ");
			object = object.replace("1", "");
			object = object.replace("2", "");
			object = object.replace("3", "");
			object = object.replace("4", "");
		}

		switch (tag) {
		case "NOENERGY":
			this.voiceControl.display("I need more energys to do that.", activeAgent);
			break;
		case "TRY":
			if (object == null)
				voiceControl.display("Ok, I'll try to do it.", activeAgent);
			else
				voiceControl.display("Ok, I'll try to go to a " + object + "!", activeAgent);
			break;
		case "SORRY":
			voiceControl.display("I'm sorry, I couldn't find a " + object + ".", activeAgent);
			break;
		case "DONTKNOW":
			voiceControl.display("I'm sorry, I don't know how to do it.", activeAgent);
			break;
		case "MOTIVATIONCHANGE":
			voiceControl.display("i think " + object + ".", activeAgent);
			break;
		default:
			Logging.logWarning("Interpreter", "Unknown response tag (" + tag + ")");
			break;
		}
	}*/

	/*@Deprecated
	private void createResponse(String tag) {
		createResponse(tag, null);
	}*/

	private void interpretGoal(Map<String, String> tags) {

		String goalType = tags.get("GOAL");
				
		if (goalType.equals("MWO") || goalType.equals("PLAYER"))
		{			
			createGoalCommand(tags);
		}		
		else if (goalType.equals("INTRINSIC")) 
		{
			//currently, "destroy something" is the only effect not included in the motivations, thus it has to be triggered manually instead of by increasing the respective drive.
			createEffectCommand(tags);
		}
		//OBSOLETE: these commands should be replaced by activating specific reservoir types 
//		else if (goalType.equals("POSITIONAL")) 
//		{
//			goToPosition(tags.get("POS"));
//		}
	}

	private void createEffectCommand(Map<String, String> tags)
	{			
		Goal goal = null;
		
		if (tags.containsKey("EFFECT")) 
		{
			System.out.println("trying to reach effect: " + tags.get("EFFECT"));
			goal = new Goal(activeAgent.getPlayer().getType(),new Effect(ActionEffect.valueOf(tags.get("EFFECT")),PlayerWorldObject.valueOf(tags.get("TARGET"))));
		}

		if (goal != null) 
		{
			if (payEnergys(MIDDLE_PRIZE))
			{
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_WORLD_GOAL,activeAgent.getPlayerIndex()));
				
				/*
				ArrayList<Goal> goallist = goal.translateEffectGoal(activeAgent.getPlanner().getLevelSceneAdapter().getSimulatedLevelScene());
				
				if(goallist.size()==0)
				{
					this.setChanged();
					this.notifyObservers(new VoiceResponse(VoiceResponseType.SCHEMATIC_PLANNING_FAILED,"EFFECT("+tags.get("EFFECT")+")",activeAgent.getPlayerIndex()));				
				} */
				System.out.println("\n\n\n ________ "+tags.get("EFFECT"));
//				((VoicePlanner) activeAgent.getPlanner()).setSelfMotivated(false);

				((VoicePlanner) activeAgent.getPlanner()).setWaitingDuration(30000);
				((VoicePlanner) activeAgent.getPlanner()).approachNewGoal(goal,false);
			} 
			else
			{
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));
			}
		}
	}

	private void interpretReservoir(Map<String, String> tags) 
	{
		if (payEnergys(LOW_PRIZE)) 
		{
			this.setChanged();
			if(tags.containsKey("DIRECTION")){
				this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_RESERVOIR_GOAL,"ACTION(" + tags.get("ACTION") + ") DIRECTION(" + tags.get("DIRECTION") + ") RESERVOIR(" + tags.get("RESERVOIR")+")",activeAgent.getPlayerIndex()));
			}
			else{
				this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_RESERVOIR_GOAL,"ACTION(" + tags.get("ACTION") + ") RESERVOIR(" + tags.get("RESERVOIR")+")",activeAgent.getPlayerIndex()));
			}
			createReservoirCommand(tags);
		} 
		else 
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));					
			
			//createResponse("NOENERGY");
		}
	}

	private void interpretMovement(Map<String, String> tags) 
	{
		//"WAIT" didn't have any plausible effect. disabled.
		/*if (tags.get("MOVEMENT") == "WAIT")
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_MOVEMENT_GOAL,activeAgent.getPlayerIndex()));
			
			activeAgent.directionCommand("WAIT");
		}
		else */if (payEnergys(MIDDLE_PRIZE))
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_MOVEMENT_GOAL,activeAgent.getPlayerIndex()));
			((VoicePlanner) activeAgent.getPlanner()).setWaitingDuration(30000);
			activeAgent.directionCommand(tags.get("MOVEMENT"));
		}
		else
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));					
			
			//createResponse("NOENERGY");
		}

	}

	private void interpretKnowledge(Map<String, String> tags) 
	{
		if (payEnergys(LOW_PRIZE)) 
		{
			if(tags.get("KNOWLEDGE").equalsIgnoreCase("TRANSFER"))
			{
				transferKnowledgeCommand(tags);
			} 
			else if(tags.get("KNOWLEDGE").equalsIgnoreCase("FEEDBACK")) 
			{
				createFeedbackCommand(tags);
			} 
			else if(tags.get("KNOWLEDGE").equalsIgnoreCase("QUERY"))
			{
				createKnowledgeQuery(tags);
			}
			else
			{
				System.err.println("NOT IMPLEMENTED!");
			}
		} 
		else 
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));					
			
			//createResponse("NOENERGY");
		}
	}

	private void interpretSmallTalk(Map<String, String> tags) 
	{
		createSmalltalk(tags.get("SMALLTALK"));
	}

	/**
	 * Transferring energys, controlling reservoirs, etc.
	 * TODO: refactor, split into separate functions
	 */
	private void gameControl(Map<String, String> tags) 
	{
		String command = tags.get("CONTROL");
		
		if (command.equalsIgnoreCase("ENERGY_TRANSFER")) 
		{
			int num_energys=0;
			
			if(tags.containsKey("AMOUNT_POWER_ONE"))
				num_energys += Integer.parseInt(tags.get("AMOUNT_POWER_ONE"));
			
			if(tags.containsKey("AMOUNT_POWER_TWO"))
				num_energys += Integer.parseInt(tags.get("AMOUNT_POWER_TWO")) * 10;			
			
			if (tags.containsKey("NUMBER")) 
				num_energys = Integer.parseInt(tags.get("NUMBER"));
			
			
			if (payEnergys(num_energys))
			{
				CAEAgent otherAgent = (CAEAgent) VoicePerson.agentSelector(tags.get("TARGET"), agents);
				otherAgent.increaseEnergys(num_energys);
			} 
			else 
			{				
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));				
			}			
		} 
		else if (command.equalsIgnoreCase("RES_ON")) 
		{ 
			if (payEnergys(LOW_PRIZE))
			{
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_SELF_MOTIVATION_ON,activeAgent.getPlayerIndex()));			
				
				((VoicePlanner) activeAgent.getPlanner()).setSelfMotivated(true);
				((VoicePlanner) activeAgent.getPlanner()).enableAllReservoirs(true);
				((VoicePlanner) activeAgent.getPlanner()).selectAndApproachNewGoal(false);
			} 
			else 
			{				
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));				
			}
		} 
		else if (command.equalsIgnoreCase("RES_OFF"))
		{
			if (payEnergys(LOW_PRIZE))
			{
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_SELF_MOTIVATION_OFF,activeAgent.getPlayerIndex()));
				
//				((VoicePlanner) activeAgent.getPlanner()).enableAllReservoirs(false);
				((VoicePlanner) activeAgent.getPlanner()).setWaitingDuration(60000);
				this.activeAgent.getAStarSimulator().stopEverythingInAStarComputation();
				if(this.activeAgent.getEffectChainingSupervisor() != null) {
					this.activeAgent.getEffectChainingSupervisor().stopPlanning();
				}
				
			} 
			else 
			{				
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));				
			}
		}
	}

	/**
	 * Allows running to the end of the screen (only local observation is
	 * possible)
	 * 
	 * @param position
	 *            END or START of the screen
	 */
	private void goToPosition(String position) 
	{
		System.out.println(position);
		
		Goal goal = null;
		
		//TODO: THIS COMMAND DOES NOT COST ANYTHING SO FAR! Also: do goals cost energys when reservoirs are active? i don't think so
		
		this.setChanged();
		this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_WORLD_GOAL,activeAgent.getPlayerIndex()));			
		
		/*if(activeAgent.getPlanner() instanceof VoicePlannerImplementation && position != null)
		{				
			VoicePlannerImplementation voiceplanner = ((VoicePlannerImplementation)activeAgent.getPlanner());
			MovementPlanner movementPlanner = (MovementPlanner)voiceplanner.getReservoirPlanner(ReservoirType.PROGRESS);
			
			if(position.equals("END"))
			{
				movementPlanner.toTheRight=true;
				goal = voiceplanner.getReservoirPlanner(ReservoirType.PROGRESS).computeAndSetNewGoal();
			}
			else if(position.equals("START"))
			{
				movementPlanner.toTheRight=false;
				goal = voiceplanner.getReservoirPlanner(ReservoirType.PROGRESS).computeAndSetNewGoal();
				movementPlanner.toTheRight=true;
			}
			else
			{
				System.err.println("Postional instruction not found.");
				return;
			}
			//((VoicePlanner) activeAgent.getPlanner()).executeCommand(goal);
		}
		else
		{
			System.err.println("Postional instruction erroneous or wrong type of planner.");
			return;
		}*/
		
		
		/*if (activeAgent.payEnergys(HIGH_PRIZE)) 
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_WORLD_GOAL,activeAgent.getPlayerIndex()));				
			
			((VoicePlanner) activeAgent.getPlanner()).executeCommand(goal);
			//if (!goalTarget.equals(PlayerWorldObject.NONE))
				//createResponse("TRY", tags.get("MWO"));
		} 
		else 
		{
			//createResponse("NOENERGY");
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));
		}*/		
		

		/*
		 * ReservoirCommand reservoirCommand = new
		 * ReservoirCommand(ReservoirType.PROGRESS, ReservoirAction.INCREASE);
		 * ((VoicePlanner)
		 * activeAgent.getPlanner()).executeCommand(reservoirCommand);
		 * createResponse("TRY");
		 */
		

		/*Goal goal = null;
		GoalCommand goalCommand = null;

		LevelSceneAdapter levelSceneAdapter = this.activeAgent.getPlanner().getLevelSceneAdapter();

		PlayerWorldObject[][] map = levelSceneAdapter.getLocalStaticRealWorldObservation();

		boolean goalFound = false;

		//y-coordinate first 
		for (int i = 0; i < map.length && !goalFound; i++) { //x-coordinate but from the right corner 
			for (int j = map[i].length - 1; j > 0; j--) {
				if (map[i][j] == null) {
					break;
				}

				if (map[i][j].blocksPath == BlockingType.BOTTOM || map[i][j].blocksPath == BlockingType.FULL) {
					goal = new Goal(new ObservationRelativeCoarse(j, i).toGlobalCoarse(levelSceneAdapter
							.getSimulatedLevelScene().getPlayerPos()), false);
					System.out.println("Goal in (" + j + "," + i + ")");
					goalFound = true;
					break;
				}
			}
		}

		if (goal != null) {
			goalCommand = new GoalCommand(goal);
			if (goalCommand != null) {
				((VoicePlanner) activeAgent.getPlanner()).executeCommand(goalCommand);
				createResponse("TRY");
			}
		}*/

	}

	/**
	 * handles the processing of small talk input. It distinguishes between
	 * greeting and querying Mario's state of happiness. Mario's answer depends
	 * on the value of his happiness reservoir.
	 * 
	 * @param command
	 *            : string that tells what question has been asked
	 */
	private void createSmalltalk(String command) {
		VoicePlanner voicePlanner = (VoicePlanner) activeAgent.getPlanner();

		// checks whether to process a greeting or querying for state of
		// happiness
		if (command.equals("GREET")) 
		{
			//this.voiceControl.display("hello", activeAgent);
			//this.voiceControl.display(VoiceKnowledgeManager.sentenceGenerator.getSentence("HELLO"), activeAgent);
			
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.GREETINGS,activeAgent.getPlayerIndex()));
		} 
		else if (command.equals("FEELING")) 
		{
			double happiness = voicePlanner.getReservoir(ReservoirType.HAPPY).getValue();

			// TODO: overall true -> VoicePersons.fromAgent(agent)
			// answer depends on value of happiness reservoir
			if (happiness < 0.25) {
				this.voiceControl.display("its just not my day", activeAgent);
			} else if (happiness < 0.75) {
				this.voiceControl.display("I'm fine", activeAgent);
			} else {
				this.voiceControl.display("I feel very good", activeAgent);
			}
		} else {
			System.out.println("Error in smalltalk");
		}
	}

	//helper method
	//TODO: this does not implement the same visibility contraint than learning by observation!
	private boolean isVisibleToPlayer(Object mwo) {
		if (mwo instanceof SimulatedSprite)
			return Math.abs(((SimulatedSprite) mwo).getPosition(0).x - activeAgent.getPlayer().getPosition().x) < 150;
		else if (mwo instanceof Agent)
			return Math.abs(((Agent) mwo).getPlayer().getPosition(0).x - activeAgent.getPlayer().getPosition().x) < 150;
		return false;
	}

	/**
	 * creates a goal command by first creating a goal from a specific type and
	 * then instantiating a goal command object, which is then executed by the
	 * voice planner.
	 * 
	 * @param tags
	 *            hash map that contains all information transmitted by voice
	 *            recognition
	 */
	private void createGoalCommand(Map<String, String> tags)
	{
		Goal goal = null;
		//GoalCommand goalCommand = null;

		levelSceneAdapter = this.activeAgent.getPlanner().getLevelSceneAdapter();
		SimulatedLevelScene level = levelSceneAdapter.getSimulatedLevelScene();

		ArrayList<PlayerWorldObject> goalTypes;

		PlayerWorldObject goalTarget = null;

		//determine direction from voice command
		CollisionDirection collisionDirection = CollisionDirection.IRRELEVANT;
		if (tags.containsKey("COLLISIONDIRECTION"))
			collisionDirection = CollisionDirection.valueOf(tags.get("COLLISIONDIRECTION"));
						
//		// go to specific player
//		if (tags.get("GOAL").equalsIgnoreCase("PLAYER")) 
//		{
//			for (Agent agent : agents) 
//			{
//				if (tags.get("TARGET").equalsIgnoreCase(agent.getName()))
//					if (isVisibleToPlayer(agent)) 
//					{
////						//CHECK: player goals ignore collision direction
////						
////						goalTarget = PlayerWorldObject.PLAYER;
////						goal = new Goal(agent.getPlayer().getPosition().toGlobalCoarse(), goalTarget);
//						
//						goal = new Goal(agent.getPlayer().getPosition().toGlobalCoarse(), PlayerWorldObject.valueOf(PlayerWorldObject.class, tags.get("TARGET")), collisionDirection, PlayerHealthCondition.IRRELEVANT);
//						break;
//					}
//			}
//		} 
/*		else */if (tags.get("GOAL").equalsIgnoreCase("MWO") || tags.get("GOAL").equalsIgnoreCase("PLAYER")) 
		{
			goalTarget = PlayerWorldObject.getElement(tags.get("TARGET"));

			
			if(tags.get("TARGET").equalsIgnoreCase("SELECTED_TILE")){
				goal = PlayerWorldObject.lookupSelectedTile(activeAgent,collisionDirection,level);
				
				
			} else {
				
				goalTypes = Generalizer.degeneralizePlayerWorldObject(goalTarget);

				goal = PlayerWorldObject.getNearWorldObjectGoal(goalTypes,level.getPlanningPlayer(),collisionDirection,level);
			}
			
		}
		

		
		//if (goalCommand == null)
		if (goal == null)
		{
			if(tags.get("GOAL").equalsIgnoreCase("PLAYER"))
			{
				//createResponse("SORRY", tags.get("PLAYER_TARGET"));
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.PLAYER_LOOKUP_FAILED,activeAgent.getPlayerIndex()));
			}
			else
			{
				//createResponse("SORRY", tags.get("MWO"));
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.OBJECT_LOOKUP_FAILED,activeAgent.getPlayerIndex()));
			}
		}
		else 
		{			
			if (payEnergys(HIGH_PRIZE)) 
			{
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_WORLD_GOAL,activeAgent.getPlayerIndex()));				
				
				ArrayList<Goal> goallist = new ArrayList<Goal>();
				goallist.add(goal);
//				((VoicePlanner) activeAgent.getPlanner()).setSelfMotivated(false);
				((VoicePlanner) activeAgent.getPlanner()).setWaitingDuration(30000);
				((VoicePlanner) activeAgent.getPlanner()).approachNewGoal(goal,false);
				//if (!goalTarget.equals(PlayerWorldObject.NONE))
					//createResponse("TRY", tags.get("MWO"));
			} 
			else 
			{
				//createResponse("NOENERGY");
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.OUT_OF_ENERGY,activeAgent.getPlayerIndex()));
			}
		}
	}

	private void transferKnowledgeCommand(Map<String, String> infoType) 
	{
		int passivePlayer;

		passivePlayer = getPlayerIdByName(infoType.get("PUPIL"));
		
		if(passivePlayer<0)
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.PLAYER_LOOKUP_FAILED, activeAgent.getPlayerIndex()));							
			
			return;
		}

		//voice out and knowledge transfer and generalization
		((CAEAgent) PlayHook.agents.get(passivePlayer)).getBrain().updateKnowledge(createKnowledgeQuery(infoType),KnowledgeSource.FULLY_TRUSTED);
		
		this.setChanged();
		this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_KNOWLEDGE_INPUT, passivePlayer));
	}

	//helper method to get position in players array
	private int getPlayerIdByName(String name) {
		for (int i = 0; i < PlayHook.agents.size(); i++)
			if (PlayHook.agents.get(i).getName().equalsIgnoreCase(name))
				return i;
		return -1;
	}

	/**
	 * reads from tags what kind of reservoir needs to be changed and what
	 * action is going to be executed. Using this information this method
	 * creates a reservoir command, which is then executed by the voice planner.
	 * 
	 * @param tags
	 *            hash map that contains all information transmitted by voice
	 *            recognition
	 */
	private void createReservoirCommand(Map<String, String> tags) 
	{
		ReservoirCommand reservoirCommand = null;
		
		// checks what reservoir type needs to be changed
		switch (tags.get("RESERVOIR")) 
		{
		case "ENERGY":
			switch (tags.get("ACTION")) 
			{
			case "INCREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.ENERGY, ReservoirAction.INCREASE, ReservoirParameter.NONE,( tags.get("EXCLUSIVE")!= null));
				break;
			case "DECREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.ENERGY, ReservoirAction.DECREASE, ReservoirParameter.NONE);
				break;
			}
			break;
		case "PROGRESS":
			switch (tags.get("ACTION")) 
			{
			case "INCREASE":
				switch (tags.get("DIRECTION")) 
				{
				case "RIGHT":
					reservoirCommand = new ReservoirCommand(ReservoirType.PROGRESS, ReservoirAction.INCREASE, ReservoirParameter.RIGHT,( tags.get("EXCLUSIVE")!= null));
					break;
				case "LEFT":
					reservoirCommand = new ReservoirCommand(ReservoirType.PROGRESS, ReservoirAction.INCREASE, ReservoirParameter.LEFT,( tags.get("EXCLUSIVE")!= null));
					System.out.println("\n REEEEtreat \n");
					break;
				}
				break;
			case "DECREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.PROGRESS, ReservoirAction.DECREASE, ReservoirParameter.NONE);
				break;
			}
			break;
		case "CURIOSITY":
			switch (tags.get("ACTION")) 
			{
			case "INCREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.CURIOSITY, ReservoirAction.INCREASE, ReservoirParameter.NONE,( tags.get("EXCLUSIVE")!= null));
				break;
			case "DECREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.CURIOSITY, ReservoirAction.DECREASE, ReservoirParameter.NONE);
				break;
			}
			break;
		case "HEALTHINESS":
			switch (tags.get("ACTION")) 
			{
			case "INCREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.HEALTHINESS, ReservoirAction.INCREASE, ReservoirParameter.NONE,( tags.get("EXCLUSIVE")!= null));
				break;
			case "DECREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.HEALTHINESS, ReservoirAction.DECREASE, ReservoirParameter.NONE);
				break;
			}
			break;
		// Neuer Case fï¿½r Esteem Reservoir
		case "ESTEEM":
			switch (tags.get("ACTION")) 
			{
			case "INCREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.ESTEEM, ReservoirAction.INCREASE, ReservoirParameter.NONE,( tags.get("EXCLUSIVE")!= null));
				break;
			case "DECREASE":
				reservoirCommand = new ReservoirCommand(ReservoirType.ESTEEM, ReservoirAction.DECREASE, ReservoirParameter.NONE);
				break;
			}
			break;
		}
		

		if (reservoirCommand != null) 
		{
			((VoicePlanner) activeAgent.getPlanner()).setSelfMotivated(true);

			((VoicePlanner) activeAgent.getPlanner()).executeCommand(reservoirCommand);
		} 
		else 
		{
			System.out.println("Interpreter failed to compute reservoir command object!!");
		}

	}

	/**
	 * processes a knowledge command using the information in the sorted hash
	 * map by first generating all objects needed (MarioHealthCondition,
	 * CollisionDirection, Interactor) and then creating a knowledge command
	 * object.
	 * 
	 * @param tags
	 *            : hash map that contains all information transmitted by voice
	 *            recognition
	 */
	private Entry<ConditionDirectionPair, TreeMap<Effect, Double>> createKnowledgeQuery(Map<String, String> tags) 
	{
		CollisionDirection collisionDirection = CollisionDirection.IRRELEVANT;
		PlayerWorldObject object = PlayerWorldObject.UNDEFINED;
		PlayerHealthCondition healthCondition = PlayerHealthCondition.IRRELEVANT;
		Effect effect = null;

		if (tags.containsKey("COLLISIONDIRECTION"))
			collisionDirection = CollisionDirection.valueOf(tags.get("COLLISIONDIRECTION"));

		if (tags.containsKey("HEALTHCONDITION"))
			healthCondition = PlayerHealthCondition.valueOf(tags.get("HEALTHCONDITION"));
		
		//Remark:
		//SUBJECT: who is taking the action? -> should be the player ...
		//OBJECT: collision target
		//TARGET: to whom the effect should be applied
		//-> TODO: implement correct usage of object/target!
		
		if (tags.containsKey("OBJECT"))
			object = PlayerWorldObject.valueOf(tags.get("OBJECT"));		
		
		//set currentPlayer for knowledgeCommand
		PlayerWorldObject currentPlayer = activeAgent.getPlayer().getType();

		if (tags.containsKey("EFFECT") && tags.containsKey("OBJECT"))	//TODO: this should be "TARGET"
			effect = new Effect(ActionEffect.valueOf(tags.get("EFFECT")),PlayerWorldObject.valueOf(tags.get("OBJECT")));
		else if (tags.containsKey("EFFECT"))
			//change Player into the currentPlayer, because needed in knowledgeCommand
			effect = new Effect(ActionEffect.valueOf(tags.get("EFFECT")),currentPlayer);		//normally, property effects are applied to the player.
		
		// generate knowledge command
		KnowledgeCommand knowledgeCommand;
		if(effect==null)
			
			knowledgeCommand = new KnowledgeCommand(healthCondition, currentPlayer, object, collisionDirection);
		else
			knowledgeCommand = new KnowledgeCommand(effect);
		
		
		// send command to voiceKnowledgeManager
		return VoiceControl.voiceKnowledgeManager.queryKnowledge(knowledgeCommand);
	}

	/**
	 * processes a feedback command using the information in the sorted hash map
	 * by first generating all objects needed and then creating a knowledge
	 * command object.
	 * 
	 * @param tags
	 *            : hash map that contains all information transmitted by voice
	 *            recognition.
	 */
	private void createFeedbackCommand(Map<String, String> tags) 
	{
		CollisionDirection collisionDirection = CollisionDirection.IRRELEVANT;
		PlayerWorldObject subject = PlayerWorldObject.UNDEFINED; // TODO:
																	// subject should be named "ACTOR"
		PlayerWorldObject object = PlayerWorldObject.UNDEFINED;
		PlayerWorldObject target = PlayerWorldObject.UNDEFINED;
		PlayerHealthCondition health = PlayerHealthCondition.IRRELEVANT;
		ActionEffect actionEffect = ActionEffect.UNDEFINED;
		double probability=1.0;		

		if (tags.containsKey("COLLISIONDIRECTION"))
			collisionDirection = CollisionDirection.valueOf(tags.get("COLLISIONDIRECTION"));

		if (tags.containsKey("SUBJECT"))
		{
			if(PlayerWorldObject.valueOf(tags.get("SUBJECT")).toString().equals("PLAYER"))
			{
				subject = activeAgent.getPlayer().getType();
			}
			else subject = PlayerWorldObject.valueOf(tags.get("SUBJECT"));
		}
		
		System.out.println("The subject is " + subject);

		if (tags.containsKey("OBJECT"))
		{
			if((tags.get("OBJECT")).equalsIgnoreCase("PLAYER"))
			{
				object = activeAgent.getPlayer().getType();
			}
			else object = PlayerWorldObject.valueOf(tags.get("OBJECT"));
		}

		if (tags.containsKey("EFFECT"))
		{
			actionEffect = (ActionEffect.valueOf(tags.get("EFFECT")));
		}
		if (tags.containsKey("TARGET"))
		{
			System.out.println(tags.get("TARGET"));
			if(tags.get("TARGET").toString().equalsIgnoreCase("PLAYER")
					//special case "mount you
				|| ((tags.containsKey("NEW_TARGET"))? tags.get("NEW_TARGET").equals("PLAYER") 
												   : false))
			{
				target = activeAgent.getPlayer().getType();
				if(tags.get("EFFECT").equals("MOUNT")){
					target = object;
				}
				
				
			}
			else if(tags.get("TARGET").toString().equalsIgnoreCase("PRONOUN")
					   && (!(subject.equals(PlayerWorldObject.UNDEFINED)))
					   && (!(object.equals(PlayerWorldObject.UNDEFINED))))
			{
				// if the tag for the subject is "PLAYER", the subject was addressed by "you",
				// if the target is addressed by a pronoun (he/she), it must stand for the
				// object
				if(tags.get("SUBJECT").equalsIgnoreCase("PLAYER"))
				{
					target = object;
				}
				else
				{	target = subject;
				}
			}
			else target = PlayerWorldObject.valueOf(tags.get("TARGET"));	
		}

		if (tags.containsKey("HEALTHCONDITION"))
			health = PlayerHealthCondition.valueOf(tags.get("HEALTHCONDITION"));


		if (tags.containsKey("PROBABILITY"))
		{
			if(tags.get("PROBABILITY").equalsIgnoreCase("MAYBE"))
				probability=0.4;
			if(tags.get("PROBABILITY").equalsIgnoreCase("PROBABLY"))
				probability=0.7;
			if(tags.get("PROBABILITY").equalsIgnoreCase("CERTAINLY"))
				probability=1.0;
		}
		
		// fix sentences using "it"
		if (object.equals(PlayerWorldObject.UNDEFINED))
			object = target;
		else if (target.equals(PlayerWorldObject.UNDEFINED))
			target = object;		
		
		System.out.println("Interpreter knowledge input: " + "direction: " + collisionDirection + ", target: " + target + ", health: "
				+ health + ", effect: " + actionEffect + ", subject: " + subject + ", obj: " + object);

		FeedbackCommand feedbackCommand = new FeedbackCommand(collisionDirection, target, health, actionEffect, subject, object, probability);

		if (VoiceControl.voiceKnowledgeManager.giveFeedback(feedbackCommand)) 
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.CONFIRM_KNOWLEDGE_INPUT,activeAgent.getPlayerIndex()));							
			
			//this.voiceControl.display("Yes, I got that.", activeAgent);
		} 
		else 
		{
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.KNOWLEDGE_INPUT_FAILED,activeAgent.getPlayerIndex()));										
			
			//this.voiceControl.display(voiceControl.voicePersons.get(activeAgent.getPlayerIndex()).whatDoYouMeanSentence, activeAgent);
		}
	}

	/*private void randomNoEnergyResponse() {
		//TODO: add more options, use grammars
		this.voiceControl.display("I need more energys to do that.", this.activeAgent);
	}*/
}
