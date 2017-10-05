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
package marioUI.speechSynthesis;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;

import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.goals.GeneralizedGoal;
import marioAI.goals.Goal;
import marioAI.goals.GoalDistanceWrapper;
import marioAI.goals.GoalPlanner;
import marioAI.goals.PlanningEvent;
import marioAI.goals.PlanningEventType;
import marioAI.goals.VoicePlanner;
import marioAI.run.PlayHook;
import marioUI.speechSynthesis.VoiceResponse;
import marioUI.speechSynthesis.VoiceResponseType;
import marioUI.voiceControl.VoiceControl;
import marioWorld.agents.Agent;
import marioWorld.engine.PlayerWorldObject;


/**
 * This class gets a list of goals and the planning agent.
 * If the list of goals contains steps that require cooperation, then a negotiation process is started with all participating agents. 
 * 
 * @author Yves
 *
 */
public class CooperationNegotiator extends Observable implements Runnable{
	

	private ArrayList<GoalDistanceWrapper> goalList;
	private CAEAgent activeAgent;
	private VoiceControl voiceController;
	private Goal originalGoal;
	private ArrayList<GoalDistanceWrapper> negotiatedGoals;
	private GoalPlanner goalPlanner;
	private boolean append;
	


	

	public CooperationNegotiator(Goal originalGoal, ArrayList<GoalDistanceWrapper> goallist, CAEAgent activeAgent,GoalPlanner goalPlanner, boolean append) {
		this.goalList = goallist;
		this.activeAgent = activeAgent;
		//this.voiceController = voiceControl;
		this.originalGoal = originalGoal;
		this.goalPlanner = goalPlanner;
		this.append =append;
		this.voiceController = PlayHook.voiceResponseEventBus.getVoiceControl();
		addObserver(PlayHook.voiceResponseEventBus);
		addObserver(goalPlanner);
		

	}
	
	public boolean isAppend() {
		return append;
	}
	public ArrayList<GoalDistanceWrapper> getNegotiatedGoals() {
		return negotiatedGoals;
	}
	public ArrayList<GoalDistanceWrapper> getGoalList() {
		return goalList;
	}

	public void setGoalList(ArrayList<GoalDistanceWrapper> goalList) {
		this.goalList = goalList;
	}

	public CAEAgent getActiveAgent() {
		return activeAgent;
	}

	public void setActiveAgent(CAEAgent activeAgent) {
		this.activeAgent = activeAgent;
	}
	/**
	 * involved player calculates an individual goallist to reach original goal and if its goal list is more probable, it will propose the new goal list
	 * to the original planner.
	 * @param newPlanningAgent
	 * @return
	 * 	null if no better plan was found, else
	 * 	ArrayList<GoalDistanceWrapper> of the Effect chaining goals.
	 */
	private ArrayList<GoalDistanceWrapper> createCounterProposal(CAEAgent newPlanningAgent) {
		ArrayList<GoalDistanceWrapper> newProposal = originalGoal.translateEffectGoal(newPlanningAgent.getPlanner().getLevelSceneAdapter().getSimulatedLevelScene());
		
		if(newProposal != null && newProposal.size()>0){
			if(calculateGoalProbability(newProposal) < calculateGoalProbability(goalList)) {
				VoiceResponse voic = new VoiceResponse(VoiceResponseType.NEGOTIATION_COUNTER_PLAN,newPlanningAgent.getPlayerIndex());
				this.setChanged();
				this.notifyObservers(voic);
				int involvedPlayerIndex = getInvolvedAgent(newProposal, newPlanningAgent.getPlayer().getType())!= null? getInvolvedAgent(newProposal,newPlanningAgent.getPlayer().getType()).getPlayerIndex() : newPlanningAgent.getPlayerIndex();
				expressPlan(goalsToGeneralizedGoals(newProposal), newPlanningAgent,involvedPlayerIndex);
			} else {
				newProposal = null;
			}
		}
		return newProposal;
		
		
	}
	/**
	 * simple random function to accept or deny a proposal
	 * @return
	 */
	private boolean evaluateProposal() {
		return (Math.random() > 0.2);
	}
	
	/**
	 * adds together all the distances(probabilitys) of the goals 
	 * @param goals
	 * @return
	 */
	private double calculateGoalProbability(ArrayList<GoalDistanceWrapper> goals){
		double result = 0;
		for(GoalDistanceWrapper tmp : goals){
			result += tmp.getDistance();
		}
		return result;
	}
	/**
	 * verbalizes the result (if accept or decline) of the negotiation
	 * @param speakingAgent
	 * @param vrt
	 */
	private void verbalizeResult(CAEAgent speakingAgent, VoiceResponseType vrt){
		VoiceResponse voic = new VoiceResponse(vrt,speakingAgent.getPlayerIndex());
		this.setChanged();
		this.notifyObservers(voic);
	}
	
	/**
	 * the speaking agent verbalizes the goallist.
	 * @param speakingAgent
	 */
	private void verbalizeGoalPlan(CAEAgent speakingAgent,ArrayList<GoalDistanceWrapper> goalPlan, boolean counterProposal){
		if(goalPlan != null && goalPlan.size() > 0) {
			
			Goal start = goalPlan.get(0).getPreviousInteraction();
			Goal end = null;
			List<GoalDistanceWrapper> body = null;
			if(goalPlan.size() > 1){
				end = goalPlan.get(goalPlan.size()-1).getPreviousInteraction();
			}
			
			if(goalPlan.size() > 2){
				body = goalPlan.subList(1, goalPlan.size()-1);
			}
			
			
			
			boolean cooperativeGoal = false;
			
			//checks if our goallist contains an interaction
			for(GoalDistanceWrapper goal :  goalPlan){
				if(!goal.getPreviousInteraction().getActor().equals(speakingAgent.getPlayer().getType())){
					cooperativeGoal = true;
				}
			}
			
			if(cooperativeGoal || counterProposal) {
				if(start != null) {
					verbalizeStart(start,speakingAgent);
				}
				if(body != null){
					verbalizeBody(body,speakingAgent);
				}
				if(end != null){
					verbalizeEnd(end,speakingAgent);
				}
			}
			if(counterProposal){
				
			}
		}
	}
	/**
	 * checks if there is an interaction with another player as actor, and if so, returns the CAEAgent of the involved player
	 * returns null if no interaction was found
	 * @return
	 */
	private CAEAgent getInvolvedAgent(ArrayList<GoalDistanceWrapper> goalList, PlayerWorldObject planner) {
		CAEAgent result = null;
		
		for(GoalDistanceWrapper goalDistance :  goalList){
			Goal goal = goalDistance.getPreviousInteraction();
			if(!goal.getActor().equals(planner)){
				for(Agent current : PlayHook.agents) {
					if(goal.getActor().equals(current.getPlayer().getType())){
						result = (CAEAgent) current;
					}
				}
			}
		}
		return result;
	}
	
	private void verbalizeStart(Goal start, CAEAgent speakingAgent){
		
		for(Effect effect : start.getNegotiationEffect().descendingKeySet()){
			VoiceResponse voic = null;
			if(start.getActor() != speakingAgent.getPlayer().getType()) {
				//actor target effect
				voic = new VoiceResponse(VoiceResponseType.NEGOTIATION_START,"ACTOR("+start.getActor() +") "+start.getTarget() +" EFFECT("+effect.getType()+")"
						,speakingAgent.getPlayerIndex());
			} else {
				voic = new VoiceResponse(VoiceResponseType.NEGOTIATION_START,"ACTOR(THIS) " +start.getTarget()+ " EFFECT("+effect.getType()+")"
						,speakingAgent.getPlayerIndex());
			}
			
			this.setChanged();
			this.notifyObservers(voic);
		}
		
	}
	
	private void verbalizeBody(List<GoalDistanceWrapper> body, CAEAgent speakingAgent){
		for(GoalDistanceWrapper goalDistance : body) {
			Goal goal = goalDistance.getPreviousInteraction();
			for(Effect effect : goal.getNegotiationEffect().descendingKeySet()){
				VoiceResponse voic = null;
				if(goal.getActor() != speakingAgent.getPlayer().getType()) {
					//actor target effect
					voic = new VoiceResponse(VoiceResponseType.NEGOTIATION_BODY,"ACTOR("+goal.getActor() +") "+goal.getTarget() +" EFFECT("+effect.getType()+")"
							,speakingAgent.getPlayerIndex());
				} else {
					voic = new VoiceResponse(VoiceResponseType.NEGOTIATION_BODY,"ACTOR(THIS) " +goal.getTarget()+ " EFFECT("+effect.getType()+")"
							,speakingAgent.getPlayerIndex());
				}
				
				this.setChanged();
				this.notifyObservers(voic);
			}
		}
	}
	
	private void verbalizeEnd(Goal end, CAEAgent speakingAgent) {
		for(Effect effect : end.getNegotiationEffect().descendingKeySet()){
			if(effect.getType().equals(originalGoal.getEffect().getType())) {
				VoiceResponse voic = null;
				if(end.getActor() != speakingAgent.getPlayer().getType()) {
					//actor target effect
					voic = new VoiceResponse(VoiceResponseType.NEGOTIATION_END,"ACTOR("+end.getActor() +") "+end.getTarget() +" EFFECT("+effect.getType()+")"
							,speakingAgent.getPlayerIndex());
				} else {
					voic = new VoiceResponse(VoiceResponseType.NEGOTIATION_END,"ACTOR(THIS) " +end.getTarget()+ " EFFECT("+effect.getType()+")"
							,speakingAgent.getPlayerIndex());
				}
				
				this.setChanged();
				this.notifyObservers(voic);
			}
		}
	}

	@Override
	public void run() {
		/**
		 * while negotiation block this thread until negotiation is finished, then release and continue
		 */
		System.out.println("\n\n\n Cooperation Negotiation started!");
		voiceController.getVoiceOutput().getLatch().reset();
		if(goalList.size() == 0) {
			//this.setChanged();
			//this.notifyObservers(new VoiceResponse(VoiceResponseType.SCHEMATIC_PLANNING_FAILED,"EFFECT("+originalGoal.getEffect().getType()+")",activeAgent.getPlayerIndex()));
		} else {
			CAEAgent involvedPlayer = getInvolvedAgent(goalList, activeAgent.getPlayer().getType());
			if(involvedPlayer != null) {
				
				
				/*
				 * stop everything the involved player was doing
				 */
				involvedPlayer.getPlanner().setReadyToPlan(false);
				involvedPlayer.getAStarSimulator().stopEverythingInAStarComputation();
				if(involvedPlayer.getEffectChainingSupervisor() != null) {
					involvedPlayer.getEffectChainingSupervisor().stopPlanning();
				}
				
				
//				Initial suggestion of plan : 
//				verbalizeGoalPlan(activeAgent,goalList,false);
				String counterpartTags = "PLAYER(" + involvedPlayer.getName().toUpperCase() + ")";
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.SUGGEST_PLAN, counterpartTags, activeAgent.getPlayerIndex()));
				expressPlan(goalsToGeneralizedGoals(goalList), activeAgent,involvedPlayer.getPlayerIndex());
				
				ArrayList<GoalDistanceWrapper> newProposal = createCounterProposal(involvedPlayer);
				//no counter argument was made
				if(newProposal == null || newProposal.size()==0 ){
					if(true){
						verbalizeResult(involvedPlayer, VoiceResponseType.NEGOTIATION_ACCEPT);
						negotiatedGoals = goalList;
					} else {
						verbalizeResult(involvedPlayer, VoiceResponseType.NEGOTIATION_DECLINE);
						negotiatedGoals = new ArrayList<GoalDistanceWrapper>();
					}
					//counter argument was made, original player needs to accept
				} else {
					if(/*evaluateProposal()*/ true) {
						verbalizeResult(activeAgent, VoiceResponseType.NEGOTIATION_ACCEPT);
						negotiatedGoals = newProposal;
					} else {
						verbalizeResult(activeAgent, VoiceResponseType.NEGOTIATION_DECLINE);
						negotiatedGoals = new ArrayList<GoalDistanceWrapper>();
					}
				}
				
				
				try {
					voiceController.getVoiceOutput().getLatch().await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				negotiatedGoals = goalList;
			}
		}
		this.setChanged();
		this.notifyObservers(new PlanningEvent(PlanningEventType.NEGOTIATION_PLANNING_ENDED));
		
		
	}
	
	/**
	 * express a goal plan by creating a series of VoiceResponses
	 * 
	 * @param arrayList of the generalized goals in the plan
	 * @param agent: agetn that suggest the plan
	 * @param interlocutorIndex: playerIndex of the player, to which agent suggest its plan
	 * 
	 */
	private void expressPlan (ArrayList<GeneralizedGoal> arrayList, CAEAgent agent, int interlocutorIndex )
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
	
	private ArrayList<GeneralizedGoal> goalsToGeneralizedGoals(ArrayList<GoalDistanceWrapper> goals) {
		ArrayList<GeneralizedGoal> newGeneralized = new ArrayList<GeneralizedGoal>();
		for (GoalDistanceWrapper curr : goals) {
			Goal currentGoal = curr.getPreviousInteraction();
			newGeneralized.add(new GeneralizedGoal(currentGoal.getEffect().getType(),
					currentGoal.getEffect().getEffectTarget(),
					currentGoal.getActor(),
					currentGoal.getTarget(),
					currentGoal.getHealthCondition(),
					currentGoal.getDirection()));
		}
		return newGeneralized;

	}



}
