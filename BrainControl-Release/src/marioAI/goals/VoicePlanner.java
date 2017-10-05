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
package marioAI.goals;

import java.util.ArrayList;
import java.util.Observable;

import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.reservoirMotivation.AbstractReservoir;
import marioAI.reservoirMotivation.ReservoirUtilities;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioAI.run.PlayHook;
import marioUI.speechSynthesis.GoalPlanResponse;
import marioUI.speechSynthesis.VoiceResponse;
import marioUI.speechSynthesis.VoiceResponseEventBus;
import marioUI.speechSynthesis.VoiceResponseType;
import marioUI.voiceControl.interpretation.ReservoirCommand;
import marioUI.voiceControl.interpretation.VoiceCommand;
import marioUI.voiceControl.interpretation.VoiceKnowledgeManager;
import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.BlockingType;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.sprites.Sprite;
import marioWorld.engine.Tickable;

/**
 * Class that executes Voice Commands given by the Interpreter, computes Goals
 * according to reservoirs if there are no goals set by voice commands
 * coordinates setting reservoirs
 * 
 * @author Katrin, modified by smihael
 * 
 * Also contains strategy discussion functions
 * modified by Johannes
 */

public  class VoicePlanner extends ReservoirMultiplePlanner implements
Tickable {

	/**
	* (De)Activates setting goals computed by reservoir multiple planner
	*/
	
	// more attributes for strategy handling
	private boolean appendCurrentStrategy = false;
	/**
	 * true as long as this player has sent a (common) strategy suggestion planning event
	 * and has not yet received a planning event as answer
	 */
	private boolean waitingForAnswers = false;
	
	/**
	 * GOALPLAN_COSTS and ASTAR_REACHED_GOAL are both relevant for 
	 */
	private boolean readyToEvaluate = false;
	
	/**
	* If the player is trying to convince others of an interaction plan (not if others try to convince him!), 
	* discussion will contain all information about the current state of discussion
	*/
	private Discussion discussion = null;
	
	private class Discussion{
		
		/**
		 * each time an other player disagrees, tolerance will decrease
		 * if it is low enough, this player will accept plans he otherwise would reject 
		 */
		private double tolerance;
		
		/**
		 * players, that are relevant for the plan and not yet convinced
		 */
		private ArrayList<PlayerWorldObject> playersToConvince;
		
		/**
		 * players, that have already been convinced by this player
		 */
		private ArrayList<PlayerWorldObject> convincedPlayers;
		
		private Discussion (ArrayList<CAEAgent> involvedAgents){
			tolerance = 1.0;
			initPlayers(involvedAgents);
		}
		
		/**
		 * when the original strategy has been rejected and a new strategy is discussed, the list of convinced and toConvince players have to be updated
		 * 
		 * @param involvedAgents
		 * @param suggestorIndex index of the player who suggested the new strategy
		 */
		protected void updatePlayers (ArrayList<CAEAgent> involvedAgents, int suggestorIndex){
			playersToConvince = new ArrayList<>(); convincedPlayers = new ArrayList<>();
			for (CAEAgent caeAgent : involvedAgents) {
				if (caeAgent.getPlayerIndex() == agent.getPlayerIndex()) continue; 
				if (caeAgent.getPlayerIndex() == suggestorIndex) convincedPlayers.add(caeAgent.getPlayer().getType());
				else playersToConvince.add(caeAgent.getPlayer().getType());
			}
		}
		
		/**
		 * init player lists when all involved players (excluded this one) still have to be convinced
		 * @param involvedAgents
		 */
		protected void initPlayers(ArrayList<CAEAgent> involvedAgents){
			updatePlayers(involvedAgents, agent.getPlayerIndex()); // no other player is already convinced
		}
		
		/**
		 * update player lists, given that convincedAgent has been convinced
		 * @param convincedAgent
		 */
		protected void convincePlayer(CAEAgent convincedAgent){
			playersToConvince.remove(convincedAgent.getPlayer().getType());
			convincedPlayers.add(convincedAgent.getPlayer().getType());
		}
		
		/**
		 * decrease tolerance
		 */
		public void decayTolerance() {
			this.tolerance /= 2; // formula is rather arbitrary ans simple
		}
	}
	
	public VoicePlanner() 
	{
		super();
		//this.reservoirsActive = false; // default is FALSE, according to GUI!
		}
		
		public void init(CAEAgent agent, LevelSceneAdapter levelSceneAdapter) 
		{
	
		super.init(agent, levelSceneAdapter);
		
		//CHECK: initially, all motivation is deactivated:
		enableAllReservoirs(false);
		
		addObserver(PlayHook.voiceResponseEventBus);
		}	
		
		
		public void onTick(LevelScene levelScene) 
		{
			if (isReservoirPlanningActive())
				super.onTick(levelScene);
			
			CAEAgent agentcopy = (CAEAgent) agent;
			
			//TODO: thats still dirty... movement primitives should be provided directly to the astarsimulator in the form of an action plan snippet
			if (agentcopy.actionCountdown == 0) 
			{
				agentcopy.toggle("RESET", false);
				agentcopy.actionCountdown = -1;
			} 
			else 
			{
				agentcopy.actionCountdown--;
			}	
		}
		
		@Override
		public void update(Observable o, Object arg)
		{
		if(arg instanceof PlanningEvent)
		{
			switch(((PlanningEvent)arg).type)
			{
			
			case AGENT_REACHED_GOAL:
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.WORLD_GOAL_REACHED,agent.getPlayerIndex()));	
				break;
			case ASTAR_COULD_NOT_REACH_GOAL:
				if (strategyStorage.getCurrentStrategy() != null) evaluateCurrentStrategy(false, Double.POSITIVE_INFINITY);
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.SENSORIMOTOR_PLANNING_FAILED,agent.getPlayerIndex()));				
				break;
			case GOAL_SELECTION_FAILED:
				
				setTimeStampOfLastAction(System.currentTimeMillis());
				setReadyToPlan(true);
				setWaitingDuration(60000);
				

				this.setChanged();
				this.notifyObservers(new PlanningEvent(PlanningEventType.RESERVOIR_INTERRUPT));
				
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.GOAL_SELECTION_FAILED,(String)(((PlanningEvent)arg).object),agent.getPlayerIndex()));
				break;
			case GOALPLAN_CONTAINS_INTERACTION: // if a plan contains interaction, look how you can get the other involved players to cooperate
				if ((arg instanceof StrategyPlanningEvent) && (((StrategyPlanningEvent) arg).recipientIndex == agent.getPlayerIndex()) && (((PlanningEvent) arg).object instanceof Strategy))
				{
					this.appendCurrentStrategy = ((StrategyPlanningEvent)arg).append;
					
					lookForHelp((Strategy)((PlanningEvent)arg).object); // new Strategy Discussion
				}
				break;
			case COMMON_STRATEGY_SUGGESTION: // if a strategy from common ground was suggested, this player will automatically accept it
				if ((arg instanceof StrategyPlanningEvent) && (((StrategyPlanningEvent) arg).recipientIndex == agent.getPlayerIndex())){
					this.setChanged();
					this.notifyObservers(new VoiceResponse(VoiceResponseType.ACCEPT_PLAN,agent.getPlayerIndex()));
					this.setChanged();
					this.notifyObservers(new StrategyPlanningEvent(PlanningEventType.COMMON_STRATEGY_ADOPTION, ((StrategyPlanningEvent) arg).object, agent.getPlayerIndex() ,((StrategyPlanningEvent) arg).senderIndex));
				}
				break;
			case COMMON_STRATEGY_ADOPTION: // if the other players have accepted the strategy from common ground, prepare its execution
				if ((this.waitingForAnswers) && (arg instanceof StrategyPlanningEvent) && (((StrategyPlanningEvent) arg).recipientIndex == agent.getPlayerIndex())){
					this.waitingForAnswers = false;
					prepareExecution((Strategy) ((StrategyPlanningEvent) arg).object);
				}
				break;
			case GOALPLAN_SUGGESTION: // if a strategy was suggested, decide over an answer
				if ((arg instanceof StrategyPlanningEvent) && (((StrategyPlanningEvent) arg).recipientIndex == agent.getPlayerIndex())){
					System.out.println("SD: " + agent.getName() + " got a suggestion");
					Object strategy = ((StrategyPlanningEvent) arg).object;
					if (strategy instanceof Strategy) answerStrategy(((Strategy) strategy), ((StrategyPlanningEvent) arg).senderIndex);
				}
				break;
			case GOALPLAN_ACCEPTION: // if this players suggestion was accepted, look if there are more players to convince
				if ((arg instanceof StrategyPlanningEvent) && (((StrategyPlanningEvent) arg).recipientIndex == agent.getPlayerIndex()) && this.isChairingDiscussion() && this.waitingForAnswers){
					System.out.println("SD: " + agent.getName() + " has convinced a player");
					this.waitingForAnswers = false;
					Object strategy = ((StrategyPlanningEvent) arg).object;
					discussion.convincePlayer(PlayerWorldObject.findPlayer(((StrategyPlanningEvent) arg).senderIndex).findAgent());
					if (strategy instanceof Strategy) continueDiscussion((Strategy) strategy);
				}
				break;
			case GOALPLAN_REJECTION: // if this players suggestion was rejected, decide over an answer to the alternative plan
				if ((arg instanceof StrategyPlanningEvent) && (((StrategyPlanningEvent) arg).recipientIndex == agent.getPlayerIndex())){
					Object strategy = ((StrategyPlanningEvent) arg).object;
					if (strategy instanceof Strategy) answerStrategy(((Strategy) strategy), ((StrategyPlanningEvent) arg).senderIndex);
				}
				break;
			case GOALPLAN_COSTS : // if the costs of the current strategy were computed, consequently update its cost
				System.out.println("SD: Set goalplan costs: " + ((PlanningEvent) arg).object);
				if((strategyStorage.currentStrategy != null) && (((double) ((PlanningEvent) arg).object) != Double.POSITIVE_INFINITY)) evaluateCurrentStrategy(true,((double) ((PlanningEvent) arg).object));
				break;
			case GOALPLAN_PROBABILITY : // if the effect probability of the current strategy were computed, consequently update its probability
				System.out.println("SD: Set goalplan probability: " + ((PlanningEvent) arg).object);
				if(strategyStorage.currentStrategy != null) strategyStorage.currentStrategy.setProbability((double) ((PlanningEvent) arg).object);
				break;
			default:
				break;			
			}
		}
		
		super.update(o, arg);
	}
	
	public void executeCommand(VoiceCommand cmd){
		switch(cmd.getType())
		{
		case RESERVOIR:
			ReservoirCommand reservoirCmd = (ReservoirCommand) cmd;
			updateReservoir(reservoirCmd);
			selectAndApproachNewGoal(false);
			break;
		default:
			System.out.println("VOICECOMMAND NOT HANDLED BY THIS FUNCTION");
		}
	}		
	
	/**
	* Updates reservoirs according to parameters
	* 
	* @param ReservoirType
	*            type
	* @param ReservoirAction
	*            action increase or decrease
	*/
	private void updateReservoir(ReservoirCommand cmd) 
	{	
		switch (cmd.getReservoirAction()) 
		{
		case INCREASE:
			if(cmd.isExlusiveReservoir()){
				enableDominantReservoir(cmd.getReservoirType());	
			} else {
				increaseDominantReservoir(cmd.getReservoirType());
			}
			
			break;
		case DECREASE:
			enableSingleReservoir(cmd.getReservoirType(),false);
			break;
		}	
		
		//TODO: this is dirty:		
		if(cmd.getReservoirType()==ReservoirUtilities.ReservoirType.PROGRESS && cmd.getReservoirParameter()!=ReservoirUtilities.ReservoirParameter.NONE)
		{
			if(cmd.getReservoirParameter()==ReservoirUtilities.ReservoirParameter.RIGHT)
				((MovementPlanner)(this.getReservoirPlanner(cmd.getReservoirType()))).toTheRight=true;
			if(cmd.getReservoirParameter()==ReservoirUtilities.ReservoirParameter.LEFT)
				((MovementPlanner)(this.getReservoirPlanner(cmd.getReservoirType()))).toTheRight=false;
		}			
	}
	
	/**
	* enable a certain reservoir
	*
	* @param type
	* @param set_enabled
	*/
	public void enableSingleReservoir(ReservoirType type, boolean set_active) 
	{
		for (ReservoirPlannerPair rpp : reservoirList) 
		{
			if (rpp.getReservoir().getReservoirType() == type) 
			{
				rpp.getReservoir().enable(set_active);
			}
		}
	}	
		
	public void enableAllReservoirs(boolean set_active) 
		{
		for (ReservoirPlannerPair rpp : reservoirList) 
		{
			rpp.getReservoir().enable(set_active);
		}
	}		
	
	/**
	* enable a certain reservoir, disable others
	*
	* @param type
	* @param set_enabled
	*/
	public void enableDominantReservoir(ReservoirType type) 
	{
		for (ReservoirPlannerPair rpp : reservoirList)
		{
			if (rpp.getReservoir().getReservoirType() == type) 
			{
				rpp.getReservoir().enable(true);
			}
			else			
			{
				rpp.getReservoir().enable(false);
			}
		}
	}
	
	public void increaseDominantReservoir(ReservoirType type){
		for (ReservoirPlannerPair rpp : reservoirList)
		{
			if (rpp.getReservoir().getReservoirType() == type) 
			{
				rpp.getReservoir().enable(true);
				rpp.getReservoir().increaseUrge();
			}
			else			
			{
				rpp.getReservoir().enable(true);
				rpp.getReservoir().decreaseUrge();
			}
			
			
		}
	}
	
	////STRATEGY-DISCUSSION-FUNCTIONS---------------------------------------------------------------
	
	
	/**
	 * Look for a way to assure, that the other players will help this player with his interaction plan	 
	 * @param strategy
	 */
	private void lookForHelp(Strategy strategy){
		System.out.println(strategy);
		
		// check if the involved players already have the strategy in their common ground
		if (hasInCommonGround(strategy)) suggestCommonStrategy(strategy, getPartners(strategy));
		else startDiscussion(strategy);
	}
	
	/**
	 * ask partners to perform a strategy from common ground
	 * @param commonStrategy
	 * @param partners
	 */
	private void suggestCommonStrategy(Strategy commonStrategy, ArrayList<CAEAgent> partners) {
		
		// express the suggestion via VoiceResponse
		ArrayList<GeneralizedGoal> finalGoalAsList = new ArrayList<>();
		finalGoalAsList.add(commonStrategy.getFinalGoal());
		String finalGoalTag = new GoalPlanResponse(GeneralizedGoal.toGoalObjectList(finalGoalAsList), agent.getPlayer().getType()).getTags().get(0);
		this.setChanged();
		this.notifyObservers(new VoiceResponse(VoiceResponseType.SUGGEST_GOAL, finalGoalTag, agent.getPlayerIndex()));
		// possible enhancement: add interlocutor; Problem: Must be set for each partner -> discussion required?
		
		// notify all partners about the new suggestion and wait for their answer
		this.waitingForAnswers = true;
		for (CAEAgent partner : partners) {
			this.setChanged();
			this.notifyObservers(new StrategyPlanningEvent(PlanningEventType.COMMON_STRATEGY_SUGGESTION, commonStrategy, agent.getPlayerIndex(), partner.getPlayerIndex()));
		}
	}	
	
	/**
	 * Start trying to convince other players to help and to find a common strategy
	 * @param strategy
	 * @param append 
	 */
	private void startDiscussion(Strategy strategy) {
		discussion = new Discussion(strategy.getInvolvedAgents());
		suggestNewStrategy(strategy);
	}
	
	/**
	 * make a suggestion in strategy discussion
	 * @param strategy
	 */
	private void suggestNewStrategy(Strategy strategy) {
		
		// if its a esteem-motivated strategy, offer your help before suggesting a plan
		if(activePlanner instanceof EsteemPlanner){
			this.setChanged();
			this.notifyObservers(new VoiceResponse(VoiceResponseType.OFFER_HELP, agent.getPlayerIndex()));
		}
		
		// present your strategy to the other player
		String counterpartTags = "PLAYER(" + getCurrentCounterpart().getName().toUpperCase() + ")";
		this.setChanged();
		this.notifyObservers(new VoiceResponse(VoiceResponseType.SUGGEST_PLAN, counterpartTags, agent.getPlayerIndex()));
		expressPlan(strategy.getPathToSuccess(), getCurrentCounterpart().getPlayerIndex()); 
		
		// send your suggestion and wait for his answer
		this.waitingForAnswers = true;
		System.out.println("SD: " + agent.getName() + " suggested a strategy to " + getCurrentCounterpart().getName());
		this.setChanged();
		this.notifyObservers(new StrategyPlanningEvent(
				PlanningEventType.GOALPLAN_SUGGESTION, strategy, agent.getPlayerIndex(), getCurrentCounterpart().getPlayerIndex()));
	}
	
	
	/**
	 * Decide, if this player accepts the suggested strategy rejects it suggesting a better plan
	 * @param strategy: suggestion by an other player
	 * @param counterpartIndex of the player who suggested the strategy
	 */
	private void answerStrategy(Strategy strategy, int counterpartIndex) {

		// if its a esteem-motivated strategy (i.e. EsteemPlanner of suggesting agent
		// is active), accept the help offer
		CAEAgent suggestingAgent = PlayerWorldObject.findPlayer(counterpartIndex).findAgent();
		ReservoirMultiplePlanner planner = (ReservoirMultiplePlanner) suggestingAgent.getPlanner();
		if(planner.activePlanner instanceof EsteemPlanner){
			this.setChanged(); 
			this.notifyObservers(new VoiceResponse(VoiceResponseType.ACCEPT_PLAN, agent.getPlayerIndex()));
			this.setChanged(); // asked player will send approval to the asking player
			this.notifyObservers(new StrategyPlanningEvent(PlanningEventType.GOALPLAN_ACCEPTION, strategy, agent.getPlayerIndex(), counterpartIndex));
		}
		else{
			// look, if this player knows a better strategy
			Strategy myStrategy = lookForBetterStrategy(strategy);
			final double TOLERANCE_THRESHOLD = 0.1; // this is rather simple and arbitrary
			
			// if this player couldn't find a better strategy (or if he is tired of discussing), work with the suggested strategy
			if (myStrategy.equals(strategy) || (isChairingDiscussion() && (discussion.tolerance < TOLERANCE_THRESHOLD))){ // "What are we waiting for"
				this.setChanged(); 
				this.notifyObservers(new VoiceResponse(VoiceResponseType.ACCEPT_PLAN, agent.getPlayerIndex()));
				if (this.isChairingDiscussion() && (this.waitingForAnswers)) { // if this player made the original suggestion, look, if there are more players to convince
					this.waitingForAnswers = false;
					discussion.updatePlayers(strategy.getInvolvedAgents(), counterpartIndex);
					continueDiscussion(strategy);
				} else { // if the suggestion came from the chairing player, send him back approval
					this.setChanged();
					this.notifyObservers(new StrategyPlanningEvent(PlanningEventType.GOALPLAN_ACCEPTION, strategy, agent.getPlayerIndex(), counterpartIndex));
				}
			} else { // reject the suggestion and replace it with the better strategy
				System.out.println("SD: A strategy was rejected by " + agent.getName());
				this.setChanged(); 
				this.notifyObservers(new VoiceResponse(VoiceResponseType.REJECT_PLAN, agent.getPlayerIndex()));
				expressPlan(myStrategy.getPathToSuccess(), counterpartIndex);
				if (this.isChairingDiscussion() && this.waitingForAnswers) { //TODO: think over!S 
					discussion.decayTolerance();
					discussion.updatePlayers(myStrategy.getInvolvedAgents(), agent.getPlayerIndex());
					this.waitingForAnswers = true;
				}
				// send rejection to the player, who suggested the less good strategy
				this.setChanged();
				this.notifyObservers(new StrategyPlanningEvent(PlanningEventType.GOALPLAN_REJECTION, myStrategy, agent.getPlayerIndex(), counterpartIndex));
			}
		}
	}
	
	/**
	 * Look for more players to convince and either continue or finish the discussion
	 * @param strategy
	 */
	private void continueDiscussion(Strategy strategy){
		if(discussion.playersToConvince.size() > 0){
			System.out.println("SD: To be continued...");
			suggestNewStrategy(strategy);
		} else {
			prepareExecution(strategy);
		}
	}
	
	
	/**
	 * When all participants agreed upon the strategy, all discussion remains are reset and the strategy is executed
	 * @param strategy
	 */
	private void prepareExecution(Strategy strategy){
		System.out.println("SD: Wooho! They have made an agreement!");
		
		// Reset discussion parameters to default
		boolean append = appendCurrentStrategy;
		appendCurrentStrategy = false;
		discussion = null;
		
		//-------------------------------------------------------------------------
		// storage manipulation for testing: in the next interaction, ask jay
//		ArrayList<GeneralizedGoal> goalList = new ArrayList<>();
//		goalList.add(new GeneralizedGoal(PlayerWorldObject.JAY, PlayerWorldObject.SIMPLE_BLOCK4, PlayerHealthCondition.LARGE_NON_BULB, CollisionDirection.LEFT));
//		goalList.add(new GeneralizedGoal(PlayerWorldObject.PETER, PlayerWorldObject.JAY, 
//				PlayerHealthCondition.LARGE_NON_BULB, CollisionDirection.ABOVE));
//		Strategy manipulation = new Strategy(goalList, true);
//		manipulation.successRate = 2.0; // Cheating
//		PlayerWorldObject.JAY.findAgent().getPlanner().strategyStorage.addNewStrategy(manipulation);
		//--------------------------------------------------------------------------
		
		// add strategy to storage and set it as current strategy for later evaluation
		System.out.println(this.strategyStorage.toString());
		strategy = strategyStorage.addNewStrategy(strategy);
		System.out.println(this.strategyStorage.toString());
		
		// invoke degeneralisation and AStar planning
		forceGoal(strategy, append);
	}
	
	// Helpers for Strategy functions -------
	
	
	/**
	 * look for a better way to reach the final goal in the current situation
	 * @param strategyToCompare
	 * @return either strategy to compare or an applicable stored strategy or a freshly generated strategy to the same final goal
	 */
	public Strategy lookForBetterStrategy(Strategy strategyToCompare){
		
		//---------------------------------------------------------------------------
		// Manipulation to cause controversy
//		ArrayList<GeneralizedGoal> testGoals = new ArrayList<>();
//		testGoals.add(new GeneralizedGoal(ActionEffect.OBJECT_DESTRUCTION, PlayerWorldObject.SIMPLE_BLOCK1, 
//				PlayerWorldObject.PETER, PlayerWorldObject.SIMPLE_BLOCK1, PlayerHealthCondition.LARGE_NON_BULB, CollisionDirection.BELOW));
//		testGoals.add(new GeneralizedGoal(PlayerWorldObject.PETER, PlayerWorldObject.JAY, 
//				PlayerHealthCondition.LARGE_NON_BULB, CollisionDirection.ABOVE));
//		Strategy cheatedStrategy = new Strategy(testGoals, true);
//		cheatedStrategy.successRate = 0.8;
//		cheatedStrategy.setCosts(0.4);
//		cheatedStrategy.setProbability(0.9);
//		newStrategy = cheatedStrategy;
		//-----------------------------------------------------------------------------
		
		Strategy bestStoredStrategy = strategyStorage.getStoredStrategyFor(strategyToCompare.getFinalGoal());
		System.out.println("SD: stored Strategy is: " + strategyStorage.getStoredStrategyFor(strategyToCompare.getFinalGoal()));
		return Strategy.returnBetterStrategy(bestStoredStrategy, strategyToCompare);
		
}
	
	
	/**
	 * Find an alternative strategy to the finalGoal by strategic planning
	 * @param finalGoal
	 * @return strategy to final goal; null if finalGoal has no effect
	 */
	// WARNING: This will crash your program!! Find a way to make this work and compare it as further alternative in lookForBetterStrategy
	private Strategy generateStrategy(GeneralizedGoal finalGoal) {
		if (finalGoal.getEffect() != null){
			System.out.println("SD: Effect chaining test: " + new Goal(new Effect(ActionEffect.ENERGY_INCREASE, PlayerWorldObject.PETER)).translateEffectGoal(levelSceneAdapter.getSimulatedLevelScene()));
//			return new Strategy(new Goal(finalGoal.getEffect()).translateEffectGoal(levelSceneAdapter.getSimulatedLevelScene()));
		}
		return null;
	}
	
	
	/**
	 * express a goal plan by creating a series of VoiceResponses
	 * 
	 * @param arrayList of the generalized goals in the plan
	 * @param interlocutorIndex: playerIndex of the player, to which this player suggest its plan
	 */
	private void expressPlan (ArrayList<GeneralizedGoal> arrayList, int interlocutorIndex)
	{
		ArrayList<String> tags = new GoalPlanResponse(
				GeneralizedGoal.toGoalObjectList(arrayList), agent.getPlayer().getType(), PlayerWorldObject.findPlayer(interlocutorIndex))
				.getTags();
		for (int i = 0; i < tags.size(); i++){
			this.setChanged();
			if(i == 0)
			{
				this.notifyObservers(new VoiceResponse(VoiceResponseType.FIRST_GOAL, tags.get(i), agent.getPlayerIndex()));
			}
			else if(i == tags.size() -1)
			{
				this.notifyObservers(new VoiceResponse(VoiceResponseType.LAST_GOAL, tags.get(i), agent.getPlayerIndex()));
				
			}
			else
			{
				this.notifyObservers(new VoiceResponse(VoiceResponseType.INTERMEDIATE_GOAL, tags.get(i), agent.getPlayerIndex()));
				
			}
		} 
	}

	/**
	 * Returns the planner of the next player to convince
	 * Returns null if no discussion or all players convinced!
	 * @return
	 */
	private CAEAgent getCurrentCounterpart() {
		if (this.isChairingDiscussion() && (discussion.playersToConvince.size() > 0)){
			return discussion.playersToConvince.get(0).findAgent();
		}
		System.out.println("No other player");
		return null;
	}
	
	/**
	 * @return true if this player has made the original suggestion and is coordinating the discussion
	 */
	private boolean isChairingDiscussion(){
		return (discussion != null);
	}
		
	/**
	 * find out who you have to talk to
	 * @param strategy that should be discussed
	 * @return all involved players, excluded this player
	 */
	private ArrayList<CAEAgent> getPartners(Strategy strategy){
		ArrayList<CAEAgent> partners = new ArrayList<>();
		for(CAEAgent caeAgent : strategy.getInvolvedAgents()){
			if(caeAgent.getPlayerIndex() != agent.getPlayerIndex()){
				partners.add(caeAgent); 
			}
		}
		return partners;
	}

	@Override
	public String toString() {
		String s = "VoicePlanner of ";
		s += agent.getName() + " ";
		return s;
	}
}
