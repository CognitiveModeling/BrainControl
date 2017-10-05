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

import java.util.TreeMap;

import javax.swing.plaf.synth.SynthSpinnerUI;

import java.util.Map.Entry;
import java.util.ArrayList;

import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.GoalObject;
import marioAI.goals.Goal;
import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.utils.ResetStaticInterface;

/**
 * Generate natural language responses for expression of player goals
 * 
 * 
 * @author Johannes
 */

public class GoalPlanResponse implements ResetStaticInterface{
	
	private ArrayList<String> responseTags;
	
	public static ArrayList<VoiceResponseType> responseTypes = new ArrayList<>();
	
	private PlayerWorldObject planningPlayer;
	
	private ArrayList<GoalObject> goalPlan;
	
	private PlayerWorldObject interlocutor = PlayerWorldObject.UNDEFINED; // who the player talks to; undefined as default
	
	public GoalPlanResponse(ArrayList<GoalObject> goals, PlayerWorldObject planningPlayer){	
		this(goals, planningPlayer, PlayerWorldObject.UNDEFINED);
	}
	
	public GoalPlanResponse(ArrayList<GoalObject> goals, PlayerWorldObject planningPlayer, PlayerWorldObject interlocutor){
	//TODO: make sure that planningPlayer is a player
	
		classesWithStaticStuff.add(this.getClass());
		
	
		responseTypes.add(VoiceResponseType.FIRST_GOAL);
		responseTypes.add(VoiceResponseType.INTERMEDIATE_GOAL);
		responseTypes.add(VoiceResponseType.LAST_GOAL);
		
		this.planningPlayer = planningPlayer;
		
		this.goalPlan = goals;
		
		responseTags = allGoalsToTags();
		if (interlocutor.isPlayer() && (interlocutor != PlayerWorldObject.PLAYER) && (interlocutor != planningPlayer))
			{this.interlocutor = interlocutor; System.out.println("set interlocutor");}//ugly
		responseTags = allGoalsToTags();
	}
	
	public ArrayList<String> getTags(){
		return responseTags;
	}
	
	/**
	 * Builds keys for a sequence of goals upon which the tags to sentences mapping can be browsed
	 * 
	 * @param goal the goal which is expressed by the player
	 * @param place its position in the sequence of expressed goals
	 */
	private ArrayList<String> allGoalsToTags(){
		ArrayList<String> tags = new ArrayList<>();
		for (int i = 0; i < goalPlan.size(); i++) {
			if (isRelevant(goalPlan.get(i).getEffect())) tags.add(effectToTags(goalPlan.get(i).getEffect(),getInterActor(goalPlan.get(i))));
			else  tags.add(interactionToTags(goalPlan.get(i).getCollisionAtPosition()));
		}
		return tags;
	}
	
	/**
	 * Builds key upon which the tags to sentences mapping can be browsed depending on a goal and its place in the sequence
	 * 
	 * @param goal the goal which is expressed by the player
	 * @param place its position in the sequence of expressed goals
	 */
	private String effectToTags(Effect goalEffect, PlayerWorldObject interActor){
		//System.out.println(goal + " is expressed");
		String tags = "";
		if (isRelevant(goalEffect)){
			String effectTargetTag = playerWorldObjectToString(goalEffect.getEffectTarget()), effectTypeTag = goalEffect.getType().toString();
			if (isWorldEffect(goalEffect)){ // Different order for property / world effects
				tags += playerWorldObjectToString(interActor) + " EFFECT(" + effectTypeTag + ") " + effectTargetTag; //TODO: name correct actor, not just "I"
			} else {
				tags += effectTargetTag + " EFFECT(" + effectTypeTag + ")";}
		}
		return tags;
	}
	
	public String interactionToTags(CollisionAtPosition interaction){//TODO: implement
		String tags = "";
		if (isRelevant(interaction)){
			PlayerWorldObject interActor = interaction.conditionDirectionPair.condition.getActor();
			PlayerWorldObject interactionTarget = interaction.conditionDirectionPair.condition.getTarget();
			CollisionDirection collisionDirection = interaction.conditionDirectionPair.collisionDirection;
			String actorString = "PLAYER"; //default
			if (isRelevant(interActor)) actorString = playerWorldObjectToString(interActor);
			tags += actorString;
			tags += " INTERACTION_TARGET " + playerWorldObjectToString(interactionTarget);
			if (! collisionDirection.equals(CollisionDirection.IRRELEVANT)) tags += " " + collisionDirection + " COLLISIONDIRECTION";
		} else {
			PlayerWorldObject interActor = interaction.conditionDirectionPair.condition.getActor();
			tags += playerWorldObjectToString(interActor);
			System.out.println("The tags are " + tags);
			System.out.println("SD: GPR: Interaction considered irrelevant");
		}
		return tags;
	}
	
	public PlayerWorldObject getInterActor(GoalObject goal){
		if (isRelevant(goal.getCollisionAtPosition())){
			return goal.getCollisionAtPosition().conditionDirectionPair.condition.getActor();
		}
		else return planningPlayer; // default: planner = actor
	}
	
	private static boolean isRelevant(Effect effect){
		return ((effect != null)
				&& (effect.getType() != null)
				&& (effect.getType() != ActionEffect.UNDEFINED)
				&& (isRelevant(effect.getEffectTarget())));
	}
	
	private static boolean isRelevant(CollisionAtPosition cap){
		return ((cap != null)
				&& (cap.conditionDirectionPair != null)
				&& (cap.conditionDirectionPair.condition != null)
				&& (isRelevant(cap.conditionDirectionPair.condition.getActor()))
				&& (isRelevant(cap.conditionDirectionPair.condition.getTarget()))
				&& (cap.conditionDirectionPair.collisionDirection != null));
	}
	
	private static boolean isRelevant(PlayerWorldObject pwo){
		return ((pwo != null)
				&& (pwo != PlayerWorldObject.UNDEFINED)
				&& (pwo != PlayerWorldObject.NONE));
	}
	
	private String playerWorldObjectToString (PlayerWorldObject pwo){
		String pwoString = pwo.toString();
		if (pwo.isPlayer()) {
			System.out.println(pwoString);
			System.out.println(interlocutor);
			pwoString = "PLAYER(" + pwoString + ")";
			if ((! pwo.isAnotherPlayer(planningPlayer)) || pwo.equals(PlayerWorldObject.PLAYER)) {pwoString = "PLAYER";}// If a player speaks about himself, he will use "I"
			else {if ((! pwo.isAnotherPlayer(interlocutor)) && (interlocutor != PlayerWorldObject.UNDEFINED)) pwoString = "INTERLOCUTOR";}// If one player talks to another, he will use "you" 
		} //TODO: think again about interlocutor condition!!
		return pwoString;
	}
	
	private static boolean isWorldEffect(Effect effect){
		switch (effect.getType()){
		case OBJECT_DESTRUCTION:
		case OBJECT_CREATION:
		case MOUNT:
		case UNMOUNT:
		case CARRY:
		case DROP:
		case TRANSPORTATION:	
			return true;
		default: return false;
		}
	}
	
	public static void deleteStaticAttributes() {
		responseTypes = null;
	}

	public static void resetStaticAttributes() {
		// TODO Auto-generated method stub
		
	}
}
