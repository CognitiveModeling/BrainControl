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

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioWorld.engine.PlayerWorldObject;

/**
 * Helper class for generalizing observed effects and entering single effect into knowledge (with or without generalizing).
 * 
 * @author Leona, Matthias, Jannine
 */

public final class Generalizer {
	
	// Factor used to attenuate the new evidence when generalizing probabilities. 
	// The higher this factor is ( has to be <= 1.0), the more the new evidence is taken into account.
	private static final double GENERALIZATION_FACTOR = 0.2;
	
	private Generalizer(){
	}
	
	/**
	 * Generalizes a MarioWorldObject for learning. This means that different kinds of energys will be mapped onto a single point type.
	 * 
	 * @param obj
	 * @return
	 */
	public static PlayerWorldObject generalizePlayerWorldObject(PlayerWorldObject obj) {
		if (obj == null)
			return PlayerWorldObject.NONE;
		
		switch (obj) 
		{
		case ENERGY_ANIM:
		case ENERGY_FRONT:
		case ENERGY_SIDE1:
		case ENERGY_SIDE2:
		case ENERGY_SIDE3:
			return PlayerWorldObject.ENERGY_FRONT;
		case QUESTIONMARK_BLOCK1:
		case QUESTIONMARK_BLOCK2:
		case QUESTIONMARK_ENERGY_BLOCK:
		case QUESTIONMARK_FLOWER_BLOCK:
			return PlayerWorldObject.QUESTIONMARK_BLOCK1;
		case SIMPLE_BLOCK1:
		case SIMPLE_BLOCK4:
			return PlayerWorldObject.SIMPLE_BLOCK1;
		case IRON_BLOCK1:
		case IRON_BLOCK2:
		case IRON_BLOCK3:
		case IRON_BLOCK4:
			return PlayerWorldObject.IRON_BLOCK1;
		case PETERS_BLOCK1:
		case PETERS_BLOCK2:
		case PETERS_BLOCK3:
		case PETERS_BLOCK4:
			return PlayerWorldObject.PETERS_BLOCK1;
//		case BRUCE:
//		case CLARK:
//		case JAY:
//		case PETER:
//			return PlayerWorldObject.PLAYER;
		default:
			return obj;
		}
	}
	
	public static PlayerWorldObject generalizePlayer(PlayerWorldObject input){
		switch(input){
			case CLARK:
			case PETER:
			case JAY:
			case BRUCE:
				return PlayerWorldObject.PLAYER;
			default:
				return input;
		}
	}

	/**
	 * Should be used when setting goals.
	 */
	public static ArrayList<PlayerWorldObject> degeneralizePlayerWorldObject(PlayerWorldObject obj) 
	{
		ArrayList<PlayerWorldObject> ret = new ArrayList<PlayerWorldObject>();		
		
		if (obj == null)
			return null;
		switch (obj) {
		case ENERGY_ANIM:
		case ENERGY_FRONT:
		case ENERGY_SIDE1:
		case ENERGY_SIDE2:
		case ENERGY_SIDE3:
			ret.add(PlayerWorldObject.ENERGY_ANIM);
			ret.add(PlayerWorldObject.ENERGY_FRONT);
			ret.add(PlayerWorldObject.ENERGY_SIDE1);
			ret.add(PlayerWorldObject.ENERGY_SIDE2);
			ret.add(PlayerWorldObject.ENERGY_SIDE3);
			break;
		case QUESTIONMARK_BLOCK1:
		case QUESTIONMARK_BLOCK2:
		case QUESTIONMARK_ENERGY_BLOCK:
		case QUESTIONMARK_FLOWER_BLOCK:
			ret.add(PlayerWorldObject.QUESTIONMARK_BLOCK1);
			ret.add(PlayerWorldObject.QUESTIONMARK_BLOCK2);
			ret.add(PlayerWorldObject.QUESTIONMARK_ENERGY_BLOCK);
			ret.add(PlayerWorldObject.QUESTIONMARK_FLOWER_BLOCK);
			break;
		case SIMPLE_BLOCK1:
		case SIMPLE_BLOCK4:
			ret.add(PlayerWorldObject.SIMPLE_BLOCK1);
			ret.add(PlayerWorldObject.SIMPLE_BLOCK4);
			break;
		case IRON_BLOCK1:
		case IRON_BLOCK2:
		case IRON_BLOCK3:
		case IRON_BLOCK4:
			ret.add(PlayerWorldObject.IRON_BLOCK1);
			ret.add(PlayerWorldObject.IRON_BLOCK2);
			ret.add(PlayerWorldObject.IRON_BLOCK3);
			ret.add(PlayerWorldObject.IRON_BLOCK4);
			break;
		default:
			ret.add(obj);
			break;
		}
		
		return ret;
	}		
	
	/**
	 * Generalizes a list of effects over health and directions.
	 * Note that it is assumed here that the new evidence is 1.0 (e.g. when the effects have been observed).
	 * @param entry, knowledge
	 */
	/*public static void generalize(Entry<ConditionDirectionPair, TreeSet<Effect>> entry, TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge) {
		TreeSet<Effect> observedEffects = entry.getValue(); // all observed effects
		
		// get target and actor of entry
		PlayerWorldObject target = entry.getKey().condition.getTarget();
		PlayerWorldObject actor = entry.getKey().condition.getActor();
		
		
		// iterate over all observed effects
		for (Effect effect: observedEffects){
			
			// iterate over all health conditions
			for(PlayerHealthCondition healthCondition: PlayerHealthCondition.values()) {
				// create a condition with the target and actor of the entry and each health condition
				Condition condition = new Condition(actor, target, healthCondition);
				
				// iterate over all directions
				for(CollisionDirection direction: CollisionDirection.values()) {
					//create a ConditionDirectionPair with the condition and each direction
					ConditionDirectionPair pairToBeGeneralized = new ConditionDirectionPair(condition, direction);
					
					// Only generalize if no effects have previously been observed for the ConditionDirectionPair.
					boolean entryExists = knowledge.containsKey(pairToBeGeneralized);
					if(!(entryExists && knowledge.get(pairToBeGeneralized).containsValue(Double.valueOf(1.0)))) {
						double oldProbability = 0.0;
						// If there is knowledge for the current effect and ConditionDirectionPair, include it.
						if(entryExists && knowledge.get(pairToBeGeneralized).containsKey(effect)){ 
							oldProbability = knowledge.get(pairToBeGeneralized).get(effect).doubleValue();
						}
						
						// Compute the probability based on the former probability and the new evidence (attenuated by the GENERALIZATION_FACTOR).
						Double probability = computeProbability(oldProbability, GENERALIZATION_FACTOR); // the new evidence is omitted here, since it is 1
						
						// Update entry or create a new one.
						if(entryExists){
							knowledge.get(pairToBeGeneralized).put(effect, probability);
						} else {
							TreeMap<Effect, Double> map = new TreeMap<Effect, Double>();
							map.put(effect, probability);
							knowledge.put(pairToBeGeneralized, map);
						}
						
					}
				}
			}
		}
	}*/
	
	
	/**
	 * Enters a single effect in knowledge and (if wished) generalizes over health conditions and directions.
	 * @param newEntry: Contains a ConditionDirectionPair, its effect and the probability of this effect.
	 * @param knowledge: Mario's current knowledge.
	 * @param saveDirectly: True, if the effect should be entered with the probability in new entry; 
	 * 		  false, if a probability should be computed (based on the former probability for the effect).
	 * @param generalize: True, if the effect should be generalized over health and direction;
	 * 		  false, if no generalization is wished (then only the effect will be entered).
	 * 
	 * TODO: this function doesnt seem to work properly
	 */
	/*public static void generalizeSingleEffect(Entry<ConditionDirectionPair, Entry<Effect, Double>> newEntry, TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge, boolean saveDirectly, boolean generalize) {

		ConditionDirectionPair pair = newEntry.getKey();
		Effect effect = newEntry.getValue().getKey(); // the effect that is to be entered into knowledge
		double oldEntryProbability = 0.0; // the former probability of the effect in knowledge
		double newEvidence = newEntry.getValue().getValue(); // the new evidence for the effect
		double newEntryProbability;
		
		// First, enter newEntry
		if(saveDirectly) {
			// directly use the new evidence as new probability of the effect
			newEntryProbability = newEvidence;
		}else { // compute the new probability of the effect based on the former probability
			
			// get the former probability (if there is one, else take 0.0)
			if(!knowledge.isEmpty() && knowledge.containsKey(pair) && knowledge.values().contains(effect)) {
				oldEntryProbability= knowledge.get(pair).get(effect);
			}
			newEntryProbability = computeProbability(oldEntryProbability, newEvidence);
		}
		
		if(knowledge.containsKey(pair)){ // if there already are effects for the pair, add the new effect
			knowledge.get(pair).put(effect, newEntryProbability);
		} else { // else create a new entry
			TreeMap<Effect, Double> map = new TreeMap<Effect, Double>();
			map.put(effect, newEntryProbability);
			knowledge.put(pair, map);
		}
		
		
		// Then (if wished) generalize over health and directions
		if(generalize) {
			// get target and actor of newEntry
			PlayerWorldObject target = newEntry.getKey().condition.getTarget();
			PlayerWorldObject actor = newEntry.getKey().condition.getActor();
			
			// iterate over all health conditions
			for(PlayerHealthCondition healthCondition: PlayerHealthCondition.values()) {
				// create a condition with the target and actor of newEntry and each health condition
				Condition condition = new Condition(actor, target, healthCondition);
				
				// iterate over all directions
				for(CollisionDirection direction: CollisionDirection.values()) {
					//create a ConditionDirectionPair with the condition and each direction
					ConditionDirectionPair pairToBeGeneralized = new ConditionDirectionPair(condition, direction);
					
					// Only generalize if no effects have previously been observed for the ConditionDirectionPair.
					boolean entryExists = knowledge.containsKey(pairToBeGeneralized);
					if(!(entryExists && knowledge.get(pairToBeGeneralized).containsValue(Double.valueOf(1.0)))) {
						double oldProbability = 0.0;
						// If there is knowledge for the current effect and ConditionDirectionPair, include it.
						if(entryExists && knowledge.get(pairToBeGeneralized).containsKey(effect)){ 
							oldProbability = knowledge.get(pairToBeGeneralized).get(effect).doubleValue();
						}
						
						// Compute the probability based on the former probability and the new evidence (attenuated by the GENERALIZATION_FACTOR).
						Double probability = computeProbability(oldProbability, GENERALIZATION_FACTOR * newEvidence);
						
						// Update entry or create a new one.
						if(entryExists){
							knowledge.get(pairToBeGeneralized).put(effect, probability);
						} else {
							TreeMap<Effect, Double> map = new TreeMap<Effect, Double>();
							map.put(effect, probability);
							knowledge.put(pairToBeGeneralized, map);
						}
						
					}
				}
			}
		}
	}*/
	
	
	
	
	/**
	 * Computes new probability based on old probability and new evidence.
	 * @param oldProbability, newEvidence
	 * @return the computed probability
	 */
	/*private static Double computeProbability(double oldProbability, double newEvidence) {
		double newProbability = 1-(1-oldProbability)*(1-newEvidence);
		return Double.valueOf(newProbability);
	}*/
}
