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
package marioAI.brain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import marioAI.agents.BrainAgent;
import marioAI.agents.CAEAgent;
import marioAI.brain.simulation.CAEExtractor;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.ReservoirEvent;
import marioAI.goals.ReservoirEventType;
import marioAI.movement.ReachabilityNode;
import marioAI.run.PlayHook;
import marioUI.speechSynthesis.KnowledgeResponse;
import marioWorld.agents.Agent;
import marioWorld.agents.Agent.AGENT_TYPE;
import marioWorld.engine.LevelScene;
import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.BlockingType;
import marioWorld.engine.Tickable;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeCoarse;
import marioWorld.engine.sprites.Player;
import marioWorld.utils.ResourceStream;
import marioWorld.utils.UserDir;

/**
 * Represents Mario's knowledge by holding a mapping from ConditionActionPairs
 * to Effects. Extending the AbstractTableModel a view is able to display the
 * knowledge and its changes accordingly. Brain itself listens on the LevelScene
 * to react to world state changes by comparing real and simulated
 * RelevantObservation. It also instantiates a view upon creation.
 * 
 * @author Ylva, Stefan, Volker, Jonas, Jonas E, Daniel modified by Matthias, Leona, Chantal
 */
public class Brain extends AbstractTableModel implements Serializable, Tickable 
{
	static public enum KnowledgeSource
	{
		FULLY_TRUSTED(0.6,0.005),
		SUSPICIOUS(0.3,0.001);
		
		double observation_update_rate;
		double generalization_update_rate;
		
		private KnowledgeSource(double observation_update_rate, double generalization_update_rate)
		{
			this.observation_update_rate=observation_update_rate;
			this.generalization_update_rate=generalization_update_rate;
		}
	}
	
	public static final boolean AUTOMATICALLY_LOAD_BRAIN = true;
	
	/**
	 * Wrapper around a simulated world which is created newly from the engine
	 * world in each frame.
	 */
	public LevelSceneAdapter levelSceneAdapter;

	// private final LevelSceneAdapter levelSceneAdapter;
	private static final long serialVersionUID = 1136596946471343420L;

	/** the knowledge that the brain is maintaining */
	private TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge = new TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>>();

	private SimulatedLevelScene observationFromLastTimeStep = null;

	private ConditionDirectionPair lastObservedCondition = null;

	final static int NUM_CONDITION_COLUMNS = 3;

	final static int HEALTH_COLUMN = 0;
	public final static int CONDITION_OBJECT_COLUMN = 1;
	public final static int ACTOR_COLUMN = 2;
	
	private final String[] columnNames;
	public String KNOWLEDGE_FILE_PATH;

	/** workaround as multiple inheritance is not supported in java */
	public static final class HelpObservable extends Observable {
		/** making protected method public */
		@Override
		public synchronized void setChanged() {
			super.setChanged();
		}

	};

	public HelpObservable helpObservable = new HelpObservable();
	
	private Agent agent;
	
	private String playerName;

	public ReachabilityNode reachabilityNode;
	public int[][] exploredMap;
	private int tickCounter = 0;
	/**
	 * Instantiates the view and sets the first mission in its agent.
	 * 
	 * @param the
	 *            agent who owns the brain
	 */
	public Brain(final BrainAgent agent, Map<ConditionDirectionPair, TreeMap<Effect, Double>> initialKnowledge) 
	{
		super();
		
		this.agent = agent;
		this.playerName = agent.getName();
		
		KNOWLEDGE_FILE_PATH = this.getKnowledgeFilePath();
		
		//TEST
		//eval_wealth_curiosity
		//KNOWLEDGE_FILE_PATH = "marioAI/brain/eval_0.0_0.0_2.brain";
		//KNOWLEDGE_FILE_PATH = "marioAI/brain/eval_0.0_0.7_1.brain";
		//KNOWLEDGE_FILE_PATH = "marioAI/brain/eval_0.1_0.5_2.brain";
		//KNOWLEDGE_FILE_PATH = "marioAI/brain/eval_0.5_0.1_1.brain";		
		
		columnNames = new String[NUM_CONDITION_COLUMNS + CollisionDirection.values().length];
		columnNames[HEALTH_COLUMN] = "Health";
		columnNames[CONDITION_OBJECT_COLUMN] = "Object";
		columnNames[ACTOR_COLUMN] = "Actor";

		for (int i = NUM_CONDITION_COLUMNS; i < columnNames.length; i++) {
			columnNames[i] = CollisionDirection.values()[i - NUM_CONDITION_COLUMNS].name();
		}
		
		// run and show Brain GUI
		
		// load knowledge from disk, if there is a brain file
		File file = UserDir.getUserFile(this.KNOWLEDGE_FILE_PATH);
		if ((file != null) && (file.exists()) && AUTOMATICALLY_LOAD_BRAIN) 
		{
			// System.out.println("tries to load from disk");
			System.out.println("Loading Brain");
			loadFromDisk(file);
		}
		
		if (initialKnowledge != null) {
			knowledge.putAll(initialKnowledge);
		}
	
		fireTableDataChanged();
	}

	//set knowledgeFilePath for according to player index
	private String getKnowledgeFilePath() 
	{
		StringBuilder path = new StringBuilder(ResourceStream.DIR_AI_RESOURCES+"/brain/brain");
		path.append(playerName);
		path.append(".ser");
		return path.toString();
	}

	/**
	 * this method is called after a time step in LevelScene. What it does: 1)
	 * It uses an own forward model to transition the memorized state from the
	 * last time step 2) compares the simulated new state with a measurement of
	 * the real state 3) infers (Condition,Action)->Effect mappings and stores
	 * them in the brain knowledge 4) memorizes the measurement of the real
	 * state for usage in the next time step
	 */
	@SuppressWarnings("unused")
	@Override
	public void onTick(LevelScene levelScene)
	{
		
		tickCounter++;
		// change to false if you always want to learn
		// note that a conditional action may have multiple effects!
		final boolean learnOnlyNewConditions = false;

		/** create new observation */
		this.levelSceneAdapter.createAndSetNewSimulatedLevelScene(this);
	
			if(tickCounter%50  == 20) {
				reachabilityNode = new ReachabilityNode(this);
			}
	
		if (observationFromLastTimeStep != null) {
			// store last observation condition that was not null in field
			// "lastObservedCondition, so VoiceKnowledgeManager can
			// use it to map feedback to last observation

			/**
			 * 1) use forward model to simulate transition of old observation to
			 * simulated new state
			 */
			if (levelScene.startTime == PlayHook.DEBUG_BREAK_AT_TIME_STEP) {
				System.out.println("debug");
			}
			List<CollisionAtPosition> collisions = observationFromLastTimeStep.tick(levelSceneAdapter.getAllKeys());
			
			if(collisions.size()>0)
			{
//				System.out.println("Brain of player "+this.agent.getName()+" ticked "+collisions.size()+" collisions:");
//				for(CollisionAtPosition cap : collisions)
//					System.out.println("\t"+cap);
			}
			
			
			// TODO: is this really what we should do!?
			CollisionAtPosition collisionAtPosition = null;
			
			/*
			 *  throws an Exception if more than one collisionAtPosition occurs in one time step
			 *  -> if this error occurs, please talk to C1
			 */
			try 
			{
				collisionAtPosition = handleMultipleCollisionsInOneTimeStep(collisions);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				//System.exit(1);
			}
			
			ConditionDirectionPair conditionDirectionPair = collisionAtPosition == null ? null : collisionAtPosition.conditionDirectionPair;
			
			// avoid repeated learning when standing on a relevant object...
			// should only learn a single time
			if (lastObservedCondition != null && lastObservedCondition.equals(conditionDirectionPair)) 
			{
				observationFromLastTimeStep = levelSceneAdapter.getSimulatedLevelScene();
				return;
			}

			// also just ignore observations with entries that couldn't be
			// determined (UNDEFINED entries)
			// the latter might happen either if there are multiple collisions
			// at a single timestep (thus this "handleMultiple" function is
			// pretty useless ...)
			// or if the forward model did expect the collision with something
			// that was expected to be created, but wasn't .
			if (conditionDirectionPair != null && (conditionDirectionPair.condition.getActor() == PlayerWorldObject.UNDEFINED || conditionDirectionPair.condition.getTarget() == PlayerWorldObject.UNDEFINED)) 
			{
				observationFromLastTimeStep = levelSceneAdapter.getSimulatedLevelScene();
				return;
			}

			if (conditionDirectionPair != null) 
			{
				lastObservedCondition = conditionDirectionPair;
			}

			// debug test if the forward model was done correctly. This is not
			// necessary for theCAEExtractor program to work.
			// levelSceneAdapter.compareSimulationWithEngine(observationFromLastTimeStep);

			/**
			 * if mario does not already know what happens if he interacts with
			 * the collision object in the collision direction with his current
			 * health:
			 */
			Entry<ConditionDirectionPair, TreeMap<Effect, Double>> observed_effects = null;
			// get new effects if knowledge is empty or if it contains
			// generalized values (and no probability 1)
			if (conditionDirectionPair != null) 
			{
				/** 2+3) observe actual effects */
				// unexpected_effects =
				// CAEExtractor.getDifferences(observationFromLastTimeStep,levelSceneAdapter.getSimulatedLevelScene(),levelSceneAdapter,
				// conditionDirectionPair, playerIndex);
				
				//TODO: shouldnt this compare the forward prediction to the current scene?
				//this currently compares the last scene with the current to determine the effects!
				observed_effects = CAEExtractor.getDifferences
						(
								levelSceneAdapter.getLastSimulatedLevelScene(),
								levelSceneAdapter.getSimulatedLevelScene(), 
								levelSceneAdapter, 
								conditionDirectionPair,
								agent.getPlayerIndex()
						);
//				System.out.println("The observed effects are: " + observed_effects);
			}

			// double knowledgeIncrease = observed_effects == null ? 0 :
			// calculateKnowlegeIncrease(knowledge.get(observed_effects.getKey()),observed_effects.getValue());
			double knowledgeIncrease = 0.0;

			/**
			 * if an effect occurred, store it in the brain, generalize and do
			 * some GUI changes
			 */
			if (observed_effects != null && observed_effects.getValue().size() > 0) 
			{
				// 3) add new knowledge to brain knowledge (update probabilities
				// for observed effects)
				knowledgeIncrease = updateKnowledge(observed_effects, KnowledgeSource.FULLY_TRUSTED);
				int size = PlayHook.agents.size();

				// 4)go through all other players and see if they are watching
				// if yes then they also learn the knowledge entry but with less
				// probability
				Agent currentAgent = null;
				Player player = agent.getPlayer();
				Player otherPlayer = null;
				for (int i = 0; i < size; i++) {
					otherPlayer = levelScene.getPlayers().get(i);
					if (i != agent.getPlayerIndex() && inSight(player, otherPlayer, levelScene)) {
						currentAgent = PlayHook.agents.get(i);
						if (currentAgent instanceof CAEAgent) {
							knowledgeIncrease += ((CAEAgent) currentAgent).getBrain().updateKnowledge(observed_effects,
									KnowledgeSource.SUSPICIOUS);
						}
					}
				}
			}

			/* store collisions in new observation */
			levelSceneAdapter.getSimulatedLevelScene().setKnowlegeIncreaseInLastTimeStep(knowledgeIncrease);
			levelSceneAdapter.getSimulatedLevelScene().setCollisionsInLastTick(collisions);
			levelSceneAdapter.getSimulatedLevelScene().setSingleObservedCollisionInLastTick(collisionAtPosition);

			if (observed_effects != null) {
				notifyMotivationsAndEmotions(observed_effects.getValue().keySet());
			}

			// if (knowledgeIncrease > 0.0)
			if (observed_effects != null) {
				// System.out.println("knowledgeIncrease " + knowledgeIncrease);
				helpObservable.setChanged();
				helpObservable.notifyObservers(
						new ReservoirEvent(ReservoirEventType.KNOWLEDGE_INCREASE, knowledgeIncrease + 2.2));
			}
		}

		/** 4) store new observation for next time step */
		observationFromLastTimeStep = levelSceneAdapter.getSimulatedLevelScene();
	}

	/**
	 * checks if the <code>otherPlayer</code> can observe the active <code>player</code>
	 * @param player
	 * @param otherPlayer
	 */
	public boolean inSight(Player player, Player otherPlayer, LevelScene scene) {
		boolean ret = false;
		double playerX = player.getPosition().x;
		double otherPlayerX = otherPlayer.getPosition().x;

		//Fabian: line of sight disabled right now.
		return Math.abs(player.getPosition().distance(otherPlayer.getPosition())) <= 200;
		
//		//check if the passive player is facing the active player
//		ret |= (otherPlayer.getFacing() < 0 && otherPlayerX >= playerX);
//		ret |= (otherPlayer.getFacing() >= 0 && otherPlayerX <= playerX);		
//		
//		//check if the distance between the players is below a threshold of 200 GlobalContinous
//		ret &= player.getPosition().distance(otherPlayer.getPosition()) <= 200;
//
//		//if the requirements above are not satisfied passive player may not observe the action
//		if(!ret)return ret;	
//		
//		//also check if there are any obstacles blocking the view of the passive ayer onto the action
//		ret = lineOfSight( player,  otherPlayer, scene);
////		System.out.println("result   " + ret);
//		return ret;		
	}
	
	/**
	 * computes if there are any obstacles in the line between the two players where one can't look through
	 * @param player
	 * @param observingPlayer
	 */
	private boolean lineOfSight(Player player, Player observingPlayer, LevelScene scene){
		boolean result = true;
		
		//get player coordinates (always head coordinates)
		float playerX = player.getPosition().x;
		float playerY = player.getPosition().y + (float) player.getHeight();
		
		GlobalContinuous activePlayerPos = player.getPosition();
		
		GlobalContinuous observingPlayerPosition = observingPlayer.getPosition();
		float observingPlayerX = observingPlayerPosition.x;
		float observingPlayerY = observingPlayerPosition.y + (float)observingPlayer.getHeight();

		//we get the slope of the line between the two players
		float slope;
		if((playerX - observingPlayerX) == 0){
			slope = Float.MAX_VALUE;
		}else{
			slope = (playerY - observingPlayerY)/Math.abs(playerX - observingPlayerX);
		}
				
		
		
		//work with GlobalContinuous here!! to be more accurate
		GlobalContinuous startPos = new GlobalContinuous(observingPlayerX, observingPlayerY);
		GlobalContinuous workingPos = startPos;
		ObservationRelativeCoarse currentObsPos;
		
		PlayerWorldObject[][] map = observationFromLastTimeStep.createLocalStaticSimulation();
		
		float stepVariable;
		float direction;
		if(slope < 1 && slope > (-1)){
			stepVariable = Math.abs(workingPos.x - playerX);
			direction = ((playerX - observingPlayerX) > 0)? 1.f : (-1.f);
		}else{
			stepVariable = Math.abs(workingPos.y - playerY);
			direction = ((playerY - observingPlayerY) > 0)? 1.f : (-1.f);
		}

		BlockingType blocking;
		
		while(stepVariable > 0){
			currentObsPos = workingPos.toObservationRelativeCoarse(activePlayerPos);
	
			blocking = isPathBlocking(currentObsPos, map);
			switch(blocking){
			case NONE:
				break;
			case FULL:
				return false;
			case TOP:
				//passive player is above active player --> can't look through this Block
				if(observingPlayerY - playerY < 0)return false;
			case BOTTOM:
				//passive player is below active player --> can't look through this Block
				if(observingPlayerY - playerY > 0)return false;
			default:
				//should not happen
				System.err.println("Brain.lineOfSight  " + blocking  + " Something strange happend! Please call support!! 0800-101010 99ct/min");
				break;
			}

			if(slope == Float.MAX_VALUE){
				if(playerY > observingPlayerY){
					workingPos = new GlobalContinuous(workingPos.x, workingPos.y + 16  );
				}else{
					workingPos = new GlobalContinuous(workingPos.x, workingPos.y - 16  );
				}
				stepVariable -= 16;
				continue;
			}
	
			if(slope < 1 && slope > (-1)){
				//with the calculation '16f*direction' we are always walking towards the active player
				workingPos = new GlobalContinuous(workingPos.x + 16f*direction, workingPos.y + 16f*slope);
				stepVariable -= 16;
			}else{
				workingPos = new GlobalContinuous(workingPos.x + 16f*(-1/slope), workingPos.y + 16f*direction);
				stepVariable -= 16;
			}
		}
	
		return result;
	}
	
	/**
	 * checks if the block which is defined by the point is Blocking in the Gameworld
	 * @param point <code> ObservationRelativeCoarse </code>
	 * @param map  <code> MarioWorldObject[][] </code>
	 */
	private BlockingType isPathBlocking(ObservationRelativeCoarse point, PlayerWorldObject[][] map) 
	{	
		if(point.x > map[0].length-1 || point.y > map.length-1 || point.x<0 || point.y<0)
			System.err.println("CHECK: Brain::isPathBlocking: Point exceeds map dimensions");
		
		//WORKAROUND:
		int sx = Math.max(Math.min(point.x, map[0].length-1),0);
		int sy = Math.max(Math.min(point.y, map.length-1),0);

		if (map[sy][sx] == null)
			return BlockingType.FULL;
		return map[sy][sx].blocksPath;		
		
		/*if (map[point.y][point.x] == null)
			return BlockingType.FULL;
		return map[point.y][point.x].blocksPath;*/
	}
	
	/**
	 * learns from multiple observed effects, given a precondition
	 * generalizes over preconditions
	 */
	public double updateKnowledge(Entry<ConditionDirectionPair, TreeMap<Effect, Double>> observed_effects, KnowledgeSource source)
	{
		if(observed_effects==null)
			return 0;
			
		// create map with observed effects and probability 1 for each effect
		// if the entries are contained in the knowledge  get the highest probability to save the entry
		// if there are already values: remove them and put the new values in
				
		double probabilityAdaptation = 0.0;
		
		// get target and actor of entry
		PlayerWorldObject target = observed_effects.getKey().condition.getTarget();
		PlayerWorldObject actor = observed_effects.getKey().condition.getActor();		
		
		if(target==PlayerWorldObject.UNDEFINED)
		{			
			System.out.println("WARNING: target object to learn about was 'UNDEFINED'");
			return 0;
		}
				
		//generalize over health conditions
		for(PlayerHealthCondition healthCondition: PlayerHealthCondition.values())
		{				
			if(healthCondition==PlayerHealthCondition.IRRELEVANT)
				continue;
			
			Condition condition = new Condition(actor, target, healthCondition);
			
			//generalize over collision directions
			for(CollisionDirection direction: CollisionDirection.values())
			{
				if(direction==CollisionDirection.IRRELEVANT)
					continue;
				
				ConditionDirectionPair pairToBeGeneralized = new ConditionDirectionPair(condition, direction);
				
				double update_rate;
				
				//this is the actually observed condition
				//IRRELEVANT IS EXPANDED TO "ALL CONDITIONS" HERE!!
				//e.g. "undefined", "undefined" is expanded to 4*4 knowledge updates.				
				if
					(
							(condition.getPlayerHealthCondition()==observed_effects.getKey().condition.getPlayerHealthCondition() || observed_effects.getKey().condition.getPlayerHealthCondition() == PlayerHealthCondition.IRRELEVANT) &&
							(direction==observed_effects.getKey().collisionDirection || observed_effects.getKey().collisionDirection == CollisionDirection.IRRELEVANT)
					)
				{
					update_rate=source.observation_update_rate;
				}
				else
				{
					update_rate=source.generalization_update_rate;
//					
//					//TODO TEST: disable direction generalization!
//					continue;
				}
				
				TreeMap <Effect, Double> expected_effects = knowledge.get(pairToBeGeneralized);				
				TreeMap <Effect, Double> new_effects = new TreeMap <Effect, Double>();
				
				//update stuff...
				if(expected_effects!=null)	//knowledge about interaction exists
				{					
					//append entries that are observed now but have not been observed before
					for (Entry<Effect,Double> observed : observed_effects.getValue().entrySet())
					{			
						Double prob = observed.getValue();
						
						if(!expected_effects.keySet().contains(observed.getKey()))
						{
							probabilityAdaptation+=prob * update_rate;
							new_effects.put(observed.getKey(), prob * update_rate);	//assume 0.0 als old probability...
						}
						else	//average effects both observed and expected ... implements short-term bayesian learning
						{
							probabilityAdaptation+=update_rate*Math.abs(((double)expected_effects.get(observed.getKey())) - prob);
							new_effects.put(observed.getKey(), ((double)expected_effects.get(observed.getKey())) * (1.0-update_rate) + prob * update_rate);
						}
					}
					
					//adapt entries that have been observed before but aren't observed now
					for (Entry<Effect,Double> expected : expected_effects.entrySet())
					{
						Double prob = expected.getValue();
						
						if(!observed_effects.getValue().keySet().contains(expected.getKey()))	//weaken effects that are expected but not observed
						{
							double old_prob = 0.0;
							
							if(expected_effects.get(expected.getKey()) != null)
									old_prob = ((double) expected_effects.get(expected.getKey()));
							
							probabilityAdaptation+=update_rate*Math.abs(old_prob - (1.0-prob));
							new_effects.put(expected.getKey(), old_prob * (1.0-update_rate) + 0.0 * update_rate);	//assume 0.0 as new probability...
							
							//System.out.println("old prob: " + old_prob + ", new prob: " + (old_prob * (1.0-update_rate) + (1.0-prob) * update_rate));
						}
					}					
				}
				else	//no knowledge about this interaction...
				{
					for (Entry<Effect,Double> observed : observed_effects.getValue().entrySet())
					{
						Double prob = observed.getValue();
						
						probabilityAdaptation+=prob * update_rate;
						new_effects.put(observed.getKey(), prob * update_rate);		//new entry
					}								
				}
				
				//TODO: the interaction count is set extremely dirty in the following...
				
				//if observed effects are already in knowlege base, update the interactionCount!
				int dirtySave=0;
				if(knowledge.containsKey(pairToBeGeneralized))
				{
					dirtySave=knowledge.ceilingKey(pairToBeGeneralized).interactionCount;
				}

				//put knowledge				
				knowledge.put(pairToBeGeneralized, new_effects);
				
				if(knowledge.containsKey(pairToBeGeneralized))
				{
					knowledge.ceilingKey(pairToBeGeneralized).interactionCount=dirtySave;
				}				
			}
		}
						
		//INCREMENT ACTUAL INTERACTION COUNT!!
		//generalize over health conditions
		for(PlayerHealthCondition healthCondition: PlayerHealthCondition.values())
		{				
			if(healthCondition==PlayerHealthCondition.IRRELEVANT)
				continue;
			
			Condition condition = new Condition(actor, target, healthCondition);
			
			//generalize over collision directions
			for(CollisionDirection direction: CollisionDirection.values())
			{
				if(direction==CollisionDirection.IRRELEVANT)
					continue;
				
				ConditionDirectionPair pairToBeGeneralized = new ConditionDirectionPair(condition, direction);
				
				if
					(
							(condition.getPlayerHealthCondition()==observed_effects.getKey().condition.getPlayerHealthCondition() || observed_effects.getKey().condition.getPlayerHealthCondition() == PlayerHealthCondition.IRRELEVANT) &&
							(direction==observed_effects.getKey().collisionDirection || observed_effects.getKey().collisionDirection == CollisionDirection.IRRELEVANT)
					)
				{
					knowledge.ceilingKey(pairToBeGeneralized).interactionCount++;
				}
			}
		}
		
		if(probabilityAdaptation>0)
			fireTableDataChanged();
		
		return probabilityAdaptation;
	}
	
/**
 * transfers knowledge from one player to an other player who observed the first.
 * 
 * !!!with probability = 1.0!!!
 * 
 * @param entry
 */
	/*private void updateKnowledge(Entry<ConditionDirectionPair, TreeSet<Effect>> entry) 
	{
		//very intelligent function polymorphism...
		updateKnowledge(entry, new Double(1.0));
	}*/

	
	//TODO: find a more elegant solution
	public String dummyTranslator(String... t) {
		String allTags = "";
		for (int i = 0; i < t.length; i++) {
			allTags += " "+t[i];
		}
		
		return (new KnowledgeResponse(allTags.trim())).toString();
	}
	
	/*
	 * not tested and not needed
	  public void askForEffect(int activeIndex,ConditionDirectionPair condition ){
	 

		//if player asks himself do nothing!
		if(this.playerIndex == activeIndex)return;
	
		//player asks the question
		
		PlayHook.voiceControl.display(dummyTranslator("OBJECTDIRECTION", "KNOWLEDGE_Q"), playerIndex);

		TreeMap<Effect, Double> entry = null;
		
		if(PlayHook.agents.get(activeIndex) instanceof CAEAgent){
			entry = ((CAEAgent)PlayHook.agents.get(activeIndex)).getBrain().knowledge.get(condition);
		}
		
		if(entry != null){
			double prob = entry.get(entry.firstKey());
			//find highest probability for the effect
			//not sure if this should be done is every effect saved with this prob?
			prob = max(entry.values());
			this.updateKnowledge(new AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>(condition, getKeySetAsTreeSet(entry)), prob);
		}
		
		//depending on what the other player knows he answers
		if(entry != null){
			PlayHook.voiceControl.display(dummyTranslator("YES"), activeIndex);
		}else{
			PlayHook.voiceControl.display(dummyTranslator("IGNORANCE"), activeIndex);
		}
		
		this.fireTableDataChanged();
		//friendly player says his thanks
		PlayHook.voiceControl.display(dummyTranslator("THANKS"), playerIndex);
	}
	*/
	
//	/**
//	 * player gets all knowledge about the <code> MarioWorldObject mObj </code> from the player with the given index 
//	 * @param activePlayer
//	 * @param mObj
//	 */
//	public void askForObject(int activePlayer, PlayerWorldObject mObj) {
//
//		//if player asks himself do nothing!
//		if(this.playerIndex == activePlayer)return;
//
//		//player asks the question
//		//PlayHook.voiceControl.display(dummyTranslator(mObj.toString(),"KNOWLEDGE_Q"), playerIndex);
//
//		//ArrayList to store all knowledge entries of the other player
//		ArrayList<AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double>> knowledgeEntrys = new ArrayList<AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair,TreeSet<Effect>>,Double>>();
//		
//		
//		if(PlayHook.agents.get(activePlayer) instanceof CAEAgent){ //if other agent has no brain the following code would be a problem
//			//iterate over the complete knowledge of the other player
//			for(Entry<ConditionDirectionPair, TreeMap<Effect, Double>> item: ((CAEAgent)PlayHook.agents.get(activePlayer)).getBrain().knowledge.entrySet()){
//				//if one of the entries matches the condition that the target-object is the same as the object which was asked for, store entry with the highest probability of the several effects
//				if(item.getKey().condition.getTarget() == mObj ){
//					knowledgeEntrys.add(new AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double>(new AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>(item.getKey(),getKeySetAsTreeSet(item.getValue())), max(item.getValue().values())));
//				}
//			}
//		}
//		
//		//depending on what the other player knows he answers
//		/*if(!knowledgeEntrys.isEmpty()){
//			PlayHook.voiceControl.display(dummyTranslator("YES"), activePlayer);
//		}else{
//			PlayHook.voiceControl.display(dummyTranslator("IGNORANCE"), activePlayer);
//		}*/
//		
//		//bring all found entries into the brain of this player.
//		double knowledgeIncrease = 0.0;
//		for(AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double> e: knowledgeEntrys )
//		{
//			knowledgeIncrease += this.updateKnowledge(e.getKey(),KnowledgeSource.SUSPICIOUS);
//		}
//		
//		//if (knowledgeIncrease > 0.0)
//		{
//			//System.out.println("knowledgeIncrease " + knowledgeIncrease);
//			helpObservable.setChanged();
//			helpObservable.notifyObservers(new ReservoirEvent(ReservoirEventType.KNOWLEDGE_INCREASE,knowledgeIncrease+0.2));
//		}		
//		
//		//friendly player says his thanks
//		//PlayHook.voiceControl.display(dummyTranslator("THANKS"), playerIndex);
//		
//		fireTableDataChanged();
//	}
	
	/**
	 * transfers all knowledge from agent with index <code> other </code> to current agent
	 * @param other
	 */
	/*public void askForAllKnowledge(int other) {
		//i player wants his own knowledge --> does not make sense to do that!
		if(playerIndex == other)return;
		// return if other agent has no brain
		
		//player asks the question
		PlayHook.voiceControl.display(dummyTranslator("EXCHANGE_K"), playerIndex);
				
		if(!(PlayHook.agents.get(other) instanceof CAEAgent))return;
		
		for(Entry<ConditionDirectionPair, TreeMap<Effect, Double>> item : ((CAEAgent)PlayHook.agents.get(other)).getBrain().getKnowledge().entrySet()){
			if(!this.knowledge.containsKey(item.getKey())){ 
				// only exchange knowledge if this.agent does not know about the entry -> prevents overwrite of probabilities
				this.knowledge.put(item.getKey(), item.getValue());
			}
		}
		
		//the other player answers
		PlayHook.voiceControl.display(dummyTranslator("SURE"), other);
		
		this.fireTableDataChanged();
		
		//friendly player says his thanks
		PlayHook.voiceControl.display(dummyTranslator("THANKS"), playerIndex);
	}*/
	
	
//	public void askForItemKnowledge(int activePlayer, Effect effect){
//		//if player asks himself do nothing!
//		if(this.playerIndex == activePlayer)return;
//
//		//player asks the question about the target item
//		//PlayHook.voiceControl.display(dummyTranslator(effect.getTarget().toString(), "KNOWLEDGE_Q"), playerIndex);
//		
//		ArrayList<AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double>> knowledgeEntrys = new ArrayList<AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair,TreeSet<Effect>>,Double>>();
//		
//		if(PlayHook.agents.get(activePlayer) instanceof CAEAgent){
//			for(Entry<ConditionDirectionPair, TreeMap<Effect, Double>> item: ((CAEAgent)PlayHook.agents.get(activePlayer)).getBrain().knowledge.entrySet()){
//				for(Entry<Effect, Double> obj: item.getValue().entrySet()){
//					if(obj.getKey().getType() == effect.getType() && obj.getKey().getTarget() == effect.getTarget()){
//						knowledgeEntrys.add(new AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double>(new AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>(item.getKey(),getKeySetAsTreeSet(item.getValue())), max(item.getValue().values())));
//					}
//				}
//			}
//		}
//		
//		//depending on what the other player knows he answers
//		/*if(!knowledgeEntrys.isEmpty()){
//			PlayHook.voiceControl.display(dummyTranslator("YES"), activePlayer);
//		}else{
//			PlayHook.voiceControl.display(dummyTranslator("IGNORANCE"), activePlayer);
//		}*/
//
//		double knowledgeIncrease = 0.0;
//		for(AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double> e: knowledgeEntrys )
//		{
//			knowledgeIncrease += this.updateKnowledge(e.getKey(),KnowledgeSource.SUSPICIOUS);
//		}
//		
//		//if (knowledgeIncrease > 0.0)
//		{
//			//System.out.println("knowledgeIncrease " + knowledgeIncrease);
//			helpObservable.setChanged();
//			helpObservable.notifyObservers(new ReservoirEvent(ReservoirEventType.KNOWLEDGE_INCREASE,knowledgeIncrease+0.2));
//		}		
//		
//		//friendly player says his thanks
//		//PlayHook.voiceControl.display(dummyTranslator("THANKS"), playerIndex);
//		
//		fireTableDataChanged();
//	}
	
	
//	public void askForEffectKnowledge(int activePlayer, ActionEffect effect){
//
//		//if player asks himself do nothing!
//		if(this.playerIndex == activePlayer)return;
//		
//		//player asks the question
//		//PlayHook.voiceControl.display(dummyTranslator("ACTIONEFFECT", "KNOWLEDGE_Q") , playerIndex);
//
//		
//		ArrayList<AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double>> knowledgeEntrys = new ArrayList<AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair,TreeSet<Effect>>,Double>>();
//		
//		if(PlayHook.agents.get(activePlayer) instanceof CAEAgent){
//			for(Entry<ConditionDirectionPair, TreeMap<Effect, Double>> item: ((CAEAgent)PlayHook.agents.get(activePlayer)).getBrain().knowledge.entrySet()){
//				for(Entry<Effect, Double> obj: item.getValue().entrySet()){
//					//if effect is for individual player and not for an object
//					if(obj.getKey().getType() == effect && obj.getKey().getTarget() == PlayerWorldObject.PLAYER ){
//						knowledgeEntrys.add(new AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double>(new AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>(item.getKey(),getKeySetAsTreeSet(item.getValue())), max(item.getValue().values())));
//					}
//				}
//			}
//		}
//		
//		//depending on what the other player knows he answers
//		/*if(!knowledgeEntrys.isEmpty()){
//			PlayHook.voiceControl.display(dummyTranslator("YES"), activePlayer);
//		}else{
//			PlayHook.voiceControl.display(dummyTranslator("IGNORANCE"), activePlayer);
//		}*/
//
//		double knowledgeIncrease = 0.0;
//		for(AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<ConditionDirectionPair, TreeSet<Effect>>, Double> e: knowledgeEntrys )
//		{
//			knowledgeIncrease += this.updateKnowledge(e.getKey(),KnowledgeSource.SUSPICIOUS);
//		}
//		
//		//if (knowledgeIncrease > 0.0)
//		{
//			//System.out.println("knowledgeIncrease " + knowledgeIncrease);
//			helpObservable.setChanged();
//			helpObservable.notifyObservers(new ReservoirEvent(ReservoirEventType.KNOWLEDGE_INCREASE,knowledgeIncrease+0.2));
//		}		
//		
//		//friendly player says his thanks
//		//PlayHook.voiceControl.display(dummyTranslator("THANKS"), playerIndex);
//		
//		fireTableDataChanged();
//	}
//	
//	
//	/**
//	 * returns the max of a collection of numbers 
//	 * @param values
//	 * @return null if argument is empty else returns the max value of the collection
//	 */
//	private <T extends Comparable<T>> T max(Collection<T> values) {
//		if(values.size() < 1) return null;
//		Boolean first = true;
//		T maximum = null; 
//		for(T d: values)
//			if(first){// get first element in the list to have a reference value
//				maximum = d;
//				first = false;
//			}else{
//				if(d.compareTo( maximum ) == 1) // if d > max  -- > save d as new max
//					maximum = d;
//			}
//		return maximum;
//	}
//	
//	/**
//	 * returns the keyset of a Treemap as a Treeset
//	 * @param map
//	 * @return
//	 */
//	private <T, E> TreeSet<T> getKeySetAsTreeSet(TreeMap<T, E> map) {
//		TreeSet<T> ret = new TreeSet<T>();
//		ret.addAll(map.keySet());
//		return ret;
//	}
	
	// MOUNT, UNMOUNT, CARRY, DROP are not needed here, since ESTEEM_CHANGE
	// occurs only during cooperative planning and not during the actual
	// mount,unmount...-effects take place
	private void notifyMotivationsAndEmotions(Set<Effect> effects) {
		for (Effect effect : effects) {
			//check if target is a player
			//@c2-pavel: ex-pwo.PLAYER
			if (effect.getEffectTarget().isPlayer()) {
				switch (effect.getType()) {
				case OBJECT_HEALTH_DECREASE:
					helpObservable.setChanged();
					helpObservable.notifyObservers(new ReservoirEvent(ReservoirEventType.HEALTH_CHANGE, -1.0));
					break;
				case OBJECT_HEALTH_INCREASE:
					helpObservable.setChanged();
					helpObservable.notifyObservers(new ReservoirEvent(ReservoirEventType.HEALTH_CHANGE, 1.0));
					break;
				case ENERGY_INCREASE:
					helpObservable.setChanged();
					helpObservable.notifyObservers(new ReservoirEvent(ReservoirEventType.ENERGY_CHANGE, 1.0));
					break;
				case ENERGY_DECREASE:
					helpObservable.setChanged();
					helpObservable.notifyObservers(new ReservoirEvent(ReservoirEventType.ENERGY_CHANGE, -1.0));
					break;
				default:
					break;
				}
			}
			// if the target is not a player
			else {
				switch (effect.getType()) {
				case OBJECT_CREATION:
				case OBJECT_DESTRUCTION:
				case NOTHING_HAPPENS:
					helpObservable.setChanged();
					helpObservable.notifyObservers(new ReservoirEvent(ReservoirEventType.OBJECT_INTERACTION, 1.0));
					break;
				default:
					break;
				}
			}
		}
	}
	

	/**
	 * method is called, when there is an effect to be written into the brainGUI
	 * (all cells except for the first two), so that it can be painted in the
	 * correspondent grey-tone (depending on its probability)
	 */
	public TreeMap<Effect, Double> getValueMapAt(int rowIndex, int columnIndex) {
		TreeMap<Condition, TreeMap<CollisionDirection, TreeMap<Effect, Double>>> condMap = getMapForGUI();

		// convert (ordered) set to (ordered) array
		Condition[] rows = condMap.keySet().toArray(new Condition[condMap.keySet().size()]);
		if (rowIndex >= rows.length)
			return null;

		// get condition at specified row index
		Condition cond = rows[rowIndex];

		// if first two columns are used, return captions
		switch (columnIndex) {
		case HEALTH_COLUMN:
			return null;
		case CONDITION_OBJECT_COLUMN:
			return null;
		case ACTOR_COLUMN:
			return null;
		default:
			// get action at specified column index
			CollisionDirection action = CollisionDirection.values()[columnIndex - NUM_CONDITION_COLUMNS];
			// and search for action in map which equals this new action
			TreeMap<CollisionDirection, TreeMap<Effect, Double>> actionMap = condMap.get(cond);
			// get list of effects for the specified cell;
			return actionMap.get(action);
		}
	}

	/**
	 * method is called for the first 2 cells in the brainGUI (showing health
	 * condition and object of interaction)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		TreeMap<Condition, TreeMap<CollisionDirection, TreeMap<Effect, Double>>> condMap = getMapForGUI();
		// convert (ordered) set to (ordered) array
		Condition[] rows = condMap.keySet().toArray(new Condition[condMap.keySet().size()]);
		if (rowIndex >= rows.length)
			return "N\\A";
		// get condition at specified row index
		Condition cond = rows[rowIndex];
		// if first two columns are used, return captions
		switch (columnIndex) {
		case HEALTH_COLUMN:
			return cond.getPlayerHealthCondition().guiString;
		case CONDITION_OBJECT_COLUMN:
			return cond.getTarget();
		case ACTOR_COLUMN:
			return cond.getActor();
		default:
			// get action at specified column index
			CollisionDirection action = CollisionDirection.values()[columnIndex - NUM_CONDITION_COLUMNS];
			// and search for action in map which equals this new action
			TreeMap<CollisionDirection, TreeMap<Effect, Double>> actionMap = condMap.get(cond);
			// get list of effects for the specified cell
			TreeMap<Effect, Double> effects = actionMap.get(action);
			if (effects == null)
				return "N\\A";

			// convert list of effects into String
			return actionsToString(effects.keySet());
		}
	}
	
	
	/*
	 * throws an exception if more than one CollisionAttPosition occurs in one time step
	 * -> needed for debugging 
	 */
	public CollisionAtPosition handleMultipleCollisionsInOneTimeStep(List<CollisionAtPosition> collisions) throws Exception 
	{
		final int numCollisions = collisions.size();
		
		if (numCollisions == 0)
			return null;
		
		//look for interactions with other players as a priority to learn.
		for(CollisionAtPosition cap : collisions){
			if
			(
					cap.conditionDirectionPair.condition.getActor()==((CAEAgent)agent).getPlayer().getType() && 
					cap.conditionDirectionPair.condition.getPlayerHealthCondition() != PlayerHealthCondition.IRRELEVANT &&
					cap.conditionDirectionPair.collisionDirection != CollisionDirection.IRRELEVANT &&
					PlayerWorldObject.PLAYER == Generalizer.generalizePlayer(cap.conditionDirectionPair.condition.getTarget())
			)
			{
//				System.out.println("CAP used for learning: "+cap);
				return cap;
			}
		}
		
		
		//just use the first sensible collision that refers to the observing player
		for (int i = 0; i < numCollisions; i++) 
		{			
			CollisionAtPosition cap = collisions.get(i);

			if
			(
					cap.conditionDirectionPair.condition.getActor()==((CAEAgent)agent).getPlayer().getType() && 
					cap.conditionDirectionPair.condition.getPlayerHealthCondition() != PlayerHealthCondition.IRRELEVANT &&
					cap.conditionDirectionPair.collisionDirection != CollisionDirection.IRRELEVANT
			)
			{
//				System.out.println("CAP used for learning: "+cap);
				return cap;
			}
			else
			{
//				System.out.println("CAP ignored: "+cap);
			}
			
//			PlayerWorldObject actor = first.conditionDirectionPair.condition.getActor();
//			PlayerWorldObject target = first.conditionDirectionPair.condition.getTarget();
//			PlayerHealthCondition health = first.conditionDirectionPair.condition.getPlayerHealthCondition();
//			CollisionDirection direction = first.conditionDirectionPair.collisionDirection;
//			
//			SimulatedSprite targetSprite = first.getTargetSprite();
//			SimulatedSprite actorSprite = first.getActorSprite();
//			GlobalCoarse staticPosition = first.getPosition();			
//			
//			//CollisionAtPosition other = collisions.get(i);
//			
//			System.out.println(other);
//			
//			if (!other.conditionDirectionPair.condition.getActor().equals(actor))
//			{
////				System.out.println("WARNING: actor 1 ("+ actor +") is not actor " + i + " ("+other.conditionDirectionPair.condition.getActor()+")");
////				actor = PlayerWorldObject.UNDEFINED;
//			
//				throw new Exception("collisions with different actors");
//			}
//			
//			if (!other.conditionDirectionPair.condition.getTarget().equals(target))
//			{
////				System.out.println("WARNING: target 1 ("+ target +") is not target " + i + " ("+other.conditionDirectionPair.condition.getTarget()+")");
////				
////				target = PlayerWorldObject.UNDEFINED;
//				
//				throw new Exception("collisions with different targets");
//			}
//			
//			if (!other.conditionDirectionPair.condition.getPlayerHealthCondition().equals(health)) {
////				health = PlayerHealthCondition.IRRELEVANT;
//			
//				throw new Exception("collisions with different playerHealthConditions");
//			}
//			
//			if (!other.conditionDirectionPair.collisionDirection.equals(direction)) {
////				direction = CollisionDirection.IRRELEVANT;
//				
//				throw new Exception("collisions with different directions");
//			}
//			
//			
//			if (other.getTargetSprite() == null || targetSprite == null || !other.getTargetSprite().stemsFromSameEngineSprite(targetSprite))
//				targetSprite = null;
			
			//TODO: why was this here??
//			if (other.getPosition() == null || staticPosition == null
//					|| other.getPosition().x != staticPosition.x
//					|| other.getPosition().y != staticPosition.y)
//				staticPosition = null;
		}
		
//		System.out.println("first: "+cap);
//		
//		return new CollisionAtPosition
//				(
//						new ConditionDirectionPair
//						(
//								new Condition(actor, target, health), 
//								direction
//						),
//						staticPosition,
//						actorSprite,
//						targetSprite
//				);
		
//		System.out.println("WARNING: no meaningful collision detected!");
		
		return null;
	}

	/**
	 * @param pair
	 *            ConditionActionPair we want the Effects of
	 * @return Returns effects or empty list if CAP is not known
	 * 
	 * TODO: pretty much a nonsense function
	 */
	public TreeMap<Effect, Double> getEffects(ConditionDirectionPair pair) {
		TreeMap<Effect, Double> listEffects = knowledge.get(pair);
		if (listEffects == null)
			return new TreeMap<Effect, Double>();
		else
			return listEffects;
	}	
	
	@Override
	public int getColumnCount() {
		return NUM_CONDITION_COLUMNS + CollisionDirection.values().length - 1;	//ignore "UNKNOWN" column
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case (HEALTH_COLUMN):
			return int.class;
		case (CONDITION_OBJECT_COLUMN):
			return PlayerWorldObject.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getRowCount() {
		TreeSet<Condition> set = new TreeSet<Condition>();
		// add all conditions to a tree set to have each condition only once
		for (ConditionDirectionPair pair : this.knowledge.keySet()) {
			Condition cond = pair.condition;
			set.add(cond);
		}
		// the size of the tree set equals the number of lines
		// show minimum 1 row with N/A entries
		return Math.max(1, set.size());
	}

	// only effects should be editable as of now
	@Override
	public boolean isCellEditable(int row, int col) {
		return (row > 0) && (col > NUM_CONDITION_COLUMNS);
	}

	/**
	 * Changes to the effects in the GUI are transfered to the model by mapping
	 * the corresponding ConditionActionPair on the effects in the table cell
	 * after executing the method.
	 */
	// @Override
	/*
	 * TODO: Needs change because of change in structure of knowledge. (Isn't
	 * used at the moment.)
	 */
	public void setValueAt(Object value, int row, int col) {
		/*
		 * Object healthObject = this.getValueAt(row, HEALTH_COLUMN); Object
		 * worldObject = this.getValueAt(row, CONDITION_OBJECT_COLUMN); // if
		 * (healthObject instanceof Integer && objectObject instanceof //
		 * Integer) { // int health = (Integer) healthObject; Condition
		 * condition = new Condition(MarioWorldObject.MARIO, (MarioWorldObject)
		 * worldObject, (MarioHealthCondition) healthObject); CollisionDirection
		 * action = CollisionDirection.values()[col - NUM_CONDITION_COLUMNS]; //
		 * generate key to insert effects into the map ConditionDirectionPair
		 * conditionActionPair = new ConditionDirectionPair(condition, action);
		 * String effectIDCompleteString = (String) value; // read ','-separated
		 * effectIDs from string try { TreeSet<Effect> effects =
		 * actionsFromString(effectIDCompleteString); // add new effects to the
		 * key knowledge.put(conditionActionPair, effects); } catch (Exception
		 * e) { // an invalid objectID was entered, the model will not be //
		 * changed return; } // } // notify table that the model has changed
		 * fireTableCellUpdated(row, col);
		 */
	}

	/** @return a map of CAP that is specially suited for GUIs */
	private TreeMap<Condition, TreeMap<CollisionDirection, TreeMap<Effect, Double>>> getMapForGUI() {
		TreeMap<Condition, TreeMap<CollisionDirection, TreeMap<Effect, Double>>> ret = new TreeMap<Condition, TreeMap<CollisionDirection, TreeMap<Effect, Double>>>();

		for (Entry<ConditionDirectionPair, TreeMap<Effect, Double>> entry : knowledge
				.entrySet()) {
			if (!ret.containsKey(entry.getKey().condition)) {
				ret.put(entry.getKey().condition,
						new TreeMap<CollisionDirection, TreeMap<Effect, Double>>());

			}
			ret.get(entry.getKey().condition).put(
					entry.getKey().collisionDirection,
					new TreeMap<Effect, Double>(entry.getValue()));
		}
		return ret;
	}

	/**
	 * Build a string representation of a tree set of actions.
	 * 
	 * @param actions
	 * @return
	 */

	private static String actionsToString(Set<Effect> actions) {
		StringBuilder b = new StringBuilder();

		Iterator<Effect> it = actions.iterator();
		while (it.hasNext()) {
			Effect next = it.next();
			String effectType = next.getType().name();
			String target = next.getEffectTarget().name();

			b.append(effectType);
			if (!target.equals("NONE")) {
				b.append('(');
				b.append(target);
				b.append(')');
			}
			if (it.hasNext()) {
				b.append("; ");
			}
		}
		return b.toString();
	}

	/**
	 * Build a tree set of actions from a string representation.
	 * 
	 * @param s
	 * @return
	 */
	// TODO: not suited for the use with the TreeMap<Effect, Double> in
	// knowledge yet.
	public static TreeSet<Effect> actionsFromString(String s) {
		String[] effectsStr = s.split(";");
		TreeSet<Effect> effects = new TreeSet<Effect>();
		for (int i = 0; i < effectsStr.length; i++) {
			String toParse = effectsStr[i];
			if (toParse.length() == 0) {
				continue;
			}

			int sep = toParse.indexOf(",");
			String targetStr = toParse.substring(sep + 1);
			String typeStr = toParse.substring(0, sep);
			Effect.ActionEffect type = Effect.ActionEffect.valueOf(typeStr);
			PlayerWorldObject target = PlayerWorldObject.valueOf(targetStr);
			effects.add(new Effect(type, target));
		}
		return effects;
	}

	/**
	 * Saves brain to the disk
	 * 
	 * @param path
	 *            the path where to save.
	 */
	public void saveToDisk(File file) {
		
		if(file == null) {
			file = UserDir.getUserFile(KNOWLEDGE_FILE_PATH);
		}
		System.out.println("Saved knowledge to: " + file.getAbsolutePath());

		// delete current brain-file to ensure a proper creation of the new
		// file
		if (file.exists()) {
			file.delete();
		}

		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(file, false));
			out.writeObject(this.getKnowledge());
			out.close();
		} catch (IOException e) {
			Logging.logSevere("Agent", "Error while saving the brain: " + e);
		}
	}
	
	public static void deleteKnowledgeFile(File file) {
		if (file.exists()) {
			file.delete();
		}

	}

	/**
	 * Loads brain from disk.
	 * 
	 * @param The
	 *            path where to load.
	 */
	public void loadFromDisk(File file) 
	{
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			@SuppressWarnings("unchecked")
			TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> loadedKnowledge = (TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>>) in
					.readObject();
			in.close();
			this.setKnowledge(loadedKnowledge);
			System.out.println("Loaded knowledge from " + file.getPath());
		} catch (IOException e) {
			Logging.logSevere("Agent",
					"Error while loading brain " + e.getMessage());
			// System.err.println(e);
		} catch (ClassNotFoundException e) {
			Logging.logSevere("Agent",
					"Error while loading brain " + e.getMessage());
			// System.err.println(e);
		}
	}

	/** @return The knowledge of the brain. */
	public TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> getKnowledge() {
		return knowledge;
	}

	public void setKnowledge(
			TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge) {
		this.knowledge = knowledge;
		this.fireTableDataChanged();
	}

	/**
	 * returns subset of BaseEffects (and their probabilities) in knowledge
	 * referring to the same type of marioWorldObjects
	 * 
	 * @param playerWorldObject
	 * @return
	 */
	public TreeMap<Effect, Double> getPlayerWorldObjectSubset(
			PlayerWorldObject playerWorldObject) {
		// iterate over all entries in knowledge
		TreeMap<Effect, Double> effectMap = new TreeMap<Effect, Double>();
		for (Entry<ConditionDirectionPair, TreeMap<Effect, Double>> entry : knowledge
				.entrySet()) {
			Condition currCond = entry.getKey().condition;
			PlayerWorldObject currObj = currCond.getTarget();
			// if the entry concerns the right MarioWorldObject, add all known
			// effect and probabilities to the map
			if (currObj == Generalizer.generalizePlayerWorldObject(playerWorldObject)) {
				effectMap.putAll(entry.getValue());
			}
		}
		return effectMap;
	}

	/**
	 * Creates a map with the knowledge Mario has about the interactions with an
	 * object for each direction. But only considers those interactions, which
	 * have the same context, as would be now, i.e. same mario health and same
	 * object. (If nothing is known for a direction the knowledge entry is 0)
	 * 
	 * @param playerWorldObject
	 * @return
	 */
	public Map<CollisionDirection, Double> getContextDependentDirectionKnowledge(
			int playerHealth, PlayerWorldObject playerWorldObject,
			boolean invulnerable) {
		Map<CollisionDirection, Double> helpReturn = new TreeMap<CollisionDirection, Double>();
		// check all entries in knowledge
		for (Entry<ConditionDirectionPair, TreeMap<Effect, Double>> entry : knowledge
				.entrySet()) {
			Condition currCond = entry.getKey().condition;
			PlayerWorldObject currObj = currCond.getTarget();
			// check if its the right context
			if (currObj == Generalizer.generalizePlayerWorldObject(playerWorldObject)
					&& playerHealth == currCond.getPlayerHealth()) {
				if (entry.getValue().size() > 0) {// Mario has knowledge for
													// this context
					Double probability = 0.0;

					// sum of all probabilities of the effects for this
					// conditionDirectionPair
					for (Double prob : entry.getValue().values()) {
						probability += prob;
					}
					helpReturn.put(entry.getKey().collisionDirection,
							probability);
				}
			}
		}
		// if there is no knowledge for this context, insert all directions with
		// the value 0
		if (helpReturn.size() == 0) {
			for (CollisionDirection dir : CollisionDirection.values()) {
				helpReturn.put(dir, 0.0);
			}
		}
		return helpReturn;

	}

	/**
	 * Returns a map from CollisionDirections to maps of effects and
	 * probabilities for a specific MarioWorldObject.
	 * 
	 * @param playerWorldObject
	 * @return
	 */
	public Map<CollisionDirection, TreeMap<Effect, Double>> getPlayerWorldObjectDirectedSubset(
			PlayerWorldObject playerWorldObject) {

		HashMap<CollisionDirection, TreeMap<Effect, Double>> effects = new HashMap<CollisionDirection, TreeMap<Effect, Double>>();
		for (Entry<ConditionDirectionPair, TreeMap<Effect, Double>> entry : knowledge
				.entrySet()) {
			Condition currCond = entry.getKey().condition;
			PlayerWorldObject currObj = currCond.getTarget();

			// check for the right object
			if (currObj == Generalizer.generalizePlayerWorldObject(playerWorldObject)) {
				CollisionDirection dir = entry.getKey().collisionDirection;
				// if there is already an entry for the current direction, add
				// the effect and probabilities to the entry's
				// Effect-Probability-Map
				if (effects.containsKey(dir))
					effects.get(dir).putAll(entry.getValue());
				else { // else create a new entry with the current direction and
						// the effects and probabilities
					effects.put(dir,
							new TreeMap<Effect, Double>(entry.getValue()));
				}

			}
		}
		return effects;
	}

	public LevelSceneAdapter getLevelSceneAdapter() {
		return levelSceneAdapter;
	}

	/**
	 * Should be called only once
	 * 
	 * @param levelSceneAdapter
	 */
	public void initLevelSceneAdapter(LevelSceneAdapter levelSceneAdapter) {
		this.levelSceneAdapter = levelSceneAdapter;
	}

	/**
	 * simple get method for observationFromLastTimeStep
	 * 
	 * @return
	 */
	public SimulatedLevelScene getObservationFromLastTimeStep() {
		return this.observationFromLastTimeStep;
	}

	/**
	 * simple get method for lastObservedEvent
	 */
	public ConditionDirectionPair getLastObservedCondition() {
		return this.lastObservedCondition;
	}

	public int getPlayerIndex()
	{
		return agent.getPlayerIndex();
	}
	
	/**
	 * clears knowledge
	 */
	public void clearKnowledge() {
		knowledge.clear();
		fireTableDataChanged();
		saveToDisk(UserDir.getUserFile(KNOWLEDGE_FILE_PATH));
	}
}
