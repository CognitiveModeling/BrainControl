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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import marioAI.agents.CAEAgent;
import marioAI.brain.Brain.KnowledgeSource;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.run.PlayHook;
import marioUI.speechSynthesis.KnowledgeResponse;
import marioUI.speechSynthesis.TagsToSentences;
import marioWorld.engine.PlayerWorldObject;

/**
 * VoiceKnowledgeManager processes KnowledgeCommands and FeedbackCommands by
 * using methods from class Brain.java Answers question and stores feedback
 * information given through voice input.
 * 
 * @author chantal, modified by smihael
 *
 */
public class VoiceKnowledgeManager {

	private CAEAgent agent;

	//TODO: should be private
	public static TagsToSentences sentenceGenerator = new TagsToSentences(false);

	//private String name;

	static Random random = new Random();

	//template function
	static <A, B> Entry<A, B> selectRandomEntry(TreeMap<A, B> entries) 
	{
		ArrayList<A> keys = new ArrayList<A>(entries.keySet());
		
		A randomKey = keys.get(random.nextInt(keys.size()));
		B value = entries.get(randomKey);
		
		return new SimpleEntry<A, B>(randomKey, value);
	}

	@SuppressWarnings("unchecked")
	static SimpleEntry<ConditionDirectionPair, TreeMap<Effect, java.lang.Double>> selectInterestingEntry(TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> entries) 
	{
		ArrayList<ConditionDirectionPair> keys = new ArrayList<ConditionDirectionPair>(entries.keySet());
		
		TreeMap<ConditionDirectionPair, Double> conditionprobabilitysum = new TreeMap<ConditionDirectionPair, Double>();
		double sum=0.0;
		
		for(ConditionDirectionPair cdp : keys)
		{
			for(Effect e : entries.get(cdp).keySet())
			{
				Double oldprob = conditionprobabilitysum.get(cdp);
				if(oldprob == null)
					oldprob = 0.0;
				Double newprob = entries.get(cdp).get(e);
				if(newprob == null)
					newprob = 0.0;
				conditionprobabilitysum.put(cdp, oldprob + newprob);
				sum+=newprob;
			}
		}
		
		for(ConditionDirectionPair cdp : conditionprobabilitysum.keySet())
		{
			conditionprobabilitysum.put(cdp,conditionprobabilitysum.get(cdp)/sum);
		}
		
		Double value;
		ConditionDirectionPair randomKey = null;

		/*do
		{			
			randomKey = keys.get(random.nextInt(conditionprobabilitysum.size()));
			//System.out.println("sampling condition " + randomKey);
			value = conditionprobabilitysum.get(randomKey);
		}
		while(value==null || Math.random() > (double) value);*/
		
		//TEST: for SWR, we just chose the maximally likely CDP		
		double maxCDPprob = 0.0;		
		for(ConditionDirectionPair cdp : conditionprobabilitysum.keySet())
		{
			if(conditionprobabilitysum.get(cdp) > maxCDPprob)
			{
				randomKey = cdp;
				maxCDPprob = conditionprobabilitysum.get(cdp); 
			}			
		}
		
		return new SimpleEntry<ConditionDirectionPair, TreeMap<Effect, Double>>((ConditionDirectionPair) randomKey.clone(), (TreeMap<Effect, Double>) entries.get(randomKey).clone());
	}	
	
	static SimpleEntry<ConditionDirectionPair, TreeMap<Effect, Double>> selectInterestingEntryWithEffect(TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> entries, Effect effect) 
	{
		TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> entriesclone = new TreeMap<ConditionDirectionPair, TreeMap<Effect, java.lang.Double>>();
		
		//filter knowledge
		for(ConditionDirectionPair cdp : entries.keySet())
		{
			for(Entry<Effect, Double> e : entries.get(cdp).entrySet())
			{
				if(e.getKey().equals(effect))
				{
					TreeMap<Effect,Double> tmp = new TreeMap<Effect,Double>();
					tmp.put(e.getKey(),e.getValue());
					entriesclone.put(cdp,tmp);
				}
			}
				
			/*if(entries.get(cdp).keySet().contains(effect))
			{
				entriesclone.put(cdp,entries.get(cdp));
			}*/
		}
		
		if(entriesclone.isEmpty())
			return null;
		
		return selectInterestingEntry(entriesclone);
	}	
	
	//template function
	static <A> Entry<A, Double> selectLikelyEntry(TreeMap<A, Double> entries)
	{
		ArrayList<A> keys = new ArrayList<A>(entries.keySet());
		
		Double value;
		A randomKey;
		
		do
		{			
			randomKey = keys.get(random.nextInt(keys.size()));
			value = entries.get(randomKey);
			
			//System.out.println("thinking to talk about " + randomKey + " + " + value + "("+keys.size()+" keys)");
		}
		while(value==null || (Math.random() > value.doubleValue()));
		
		return new SimpleEntry<A, Double>(randomKey, value);
	}	
	
	/**
	 * Default constructor ... don't forget to set agent afterwards
	 */
	public VoiceKnowledgeManager() {
		//ugly
		KnowledgeResponse.translator = VoiceKnowledgeManager.sentenceGenerator;
	}

	/**
	 * @param agent
	 */
	public void setAgent(CAEAgent agent) {
		this.agent = agent;
		//this.name = agent.getName();
	}

	/**
	 * asks for the effect of the given action and condition information.
	 * Differs between cases in which knowledge is queried by only one object
	 * and those in which a whole knowledge set forms the question.
	 * 
	 * @param knowledgeCommand
	 *            : stores all information the question contains
	 */
	public Entry<ConditionDirectionPair, TreeMap<Effect, Double>> queryKnowledge(KnowledgeCommand knowledgeCommand) 
	{
		/*
		 * look up if this knowledge request is about the INTERACTION WITH AN OBJECT
		 */
		if (knowledgeCommand.getEffect()==null)
		{
			// creates question as a ConditionDirectionPair
			ConditionDirectionPair question = new ConditionDirectionPair(knowledgeCommand.getCondition(), knowledgeCommand.getCollisionDirection());
			
			//System.out.println(question);

//			/*
//			 * answers questions that consist of a whole knowledge set (except health condition -> variable)
//			 * 
//			 * TODO: what happens when asking for a specific direction only?!
//			 */
//			if (       question.collisionDirection != CollisionDirection.IRRELEVANT
//					&& question.condition.getActor() != PlayerWorldObject.UNDEFINED
//			   ) 
//			{				
//				
//				TreeMap<Effect, Double> ans = this.agent.getBrain().getEffects(question);
//
//				if (!ans.isEmpty())
//				{
//					Entry<ConditionDirectionPair, TreeMap<Effect, Double>> answer = new SimpleEntry<ConditionDirectionPair, TreeMap<Effect, Double>>(question,ans);
//					
//					//TODO: we only talk about a single entry atm
//					Entry<Effect, Double> randomEffect = selectLikelyEntry(answer.getValue());
//					answer.getValue().clear();
//					answer.getValue().put(randomEffect.getKey(), randomEffect.getValue());
//
//					PlayHook.voiceControl.display(createSentence(answer), agent);
//					
//					return answer;
//				} 
//				else 
//				{
//					String answerSentence = sentenceGenerator.getSentence("RESPONSETYPE(KNOWLEDGE_LOOKUP_FAILED)");
//					PlayHook.voiceControl.display(answerSentence, agent);										
//				}
//			} 
//			else //if only a target is specified ("tell be about iron block"?)
			{
				TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge = this.agent.getBrain().getKnowledge();
				TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledgeOfInterest = new TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>>();

				//generate knowledge of interest by only storing knowledge entries that have information about queried object
				for (Map.Entry<ConditionDirectionPair, TreeMap<Effect, Double>> entry : knowledge.entrySet()) 
				{
					//System.out.println(entry.getKey().condition.getTarget() + " " + entry.getKey().collisionDirection + " " + entry.getKey().condition.playerHealthCondition);
					
					if
					(
							entry.getKey().condition.getTarget() == question.condition.getTarget() && 
							(question.collisionDirection == CollisionDirection.IRRELEVANT || entry.getKey().collisionDirection == question.collisionDirection) &&
							(question.condition.playerHealthCondition == PlayerHealthCondition.IRRELEVANT || entry.getKey().condition.playerHealthCondition == question.condition.playerHealthCondition)
					)
					{
						knowledgeOfInterest.put(entry.getKey(), entry.getValue());
						//System.out.println("\tDAT HIER GEHT!");
					}
				}
				
				if (!knowledgeOfInterest.isEmpty()) 
				{
					Entry<ConditionDirectionPair, TreeMap<Effect, Double>> answer = selectInterestingEntry(knowledgeOfInterest);
					
					//TODO: we only talk about a single entry atm
					Entry<Effect, Double> randomEffect = selectLikelyEntry(answer.getValue());
					answer.getValue().clear();
					answer.getValue().put(randomEffect.getKey(), randomEffect.getValue());

					PlayHook.voiceControl.display(createSentence(answer), agent);
					
					return answer;
				} 
				else 
				{
					String answerSentence = sentenceGenerator.getSentence("RESPONSETYPE(KNOWLEDGE_LOOKUP_FAILED)");
					PlayHook.voiceControl.display(answerSentence, agent);	
				}
			}
		} 
		/*
		 * this request is about how to apply a specific EFFECT TO AN OBJECT
		 * */
		else
		{ 
			Effect question = knowledgeCommand.getEffect();
			
//			System.out.println(question);
			
			Entry<ConditionDirectionPair, TreeMap<Effect, Double>> answer = selectInterestingEntryWithEffect(this.agent.getBrain().getKnowledge(), question);

			if (answer!=null)
			{
				//TODO: we only talk about a single entry atm
				Entry<Effect, Double> randomEffect = selectLikelyEntry(answer.getValue());
				answer.getValue().clear();
				answer.getValue().put(randomEffect.getKey(), randomEffect.getValue());

				PlayHook.voiceControl.display(createSentence(answer), agent);
				
				return answer;
			} 
			else 
			{
				String answerSentence = sentenceGenerator.getSentence("RESPONSETYPE(KNOWLEDGE_LOOKUP_FAILED)");
				PlayHook.voiceControl.display(answerSentence, agent);
			}
		}
		
		return null;
	}

	/**
	 * adds new information to actor's knowledge based on a voice input. TODO:
	 * misleading name then...
	 * 
	 * @param feedbackCommand
	 *            stores all information needed to create a knowledge entry
	 */
	public boolean giveFeedback(FeedbackCommand feedbackCommand) 
	{
		if (feedbackCommand.getEffect().getType() != ActionEffect.UNDEFINED && feedbackCommand.getEffect().getEffectTarget() != PlayerWorldObject.UNDEFINED) 
		{
			//instantiates all variables needed to create a knowledge entry
			ConditionDirectionPair conditionDirectionPair = null;

			/*
			 * creates ConditionDirectionPair with given information
			 */
			CollisionDirection collisionDirection = feedbackCommand.getCollisionDirection();
			Condition condition = feedbackCommand.getCondition();

			conditionDirectionPair = new ConditionDirectionPair(condition, collisionDirection);

			//effectProp = new SimpleEntry<Effect, Double>(feedbackCommand.getEffect(), probability);
			//newEntry = new SimpleEntry<ConditionDirectionPair, Entry<Effect, Double>>(conditionDirectionPair, effectProp);

			//add new information to knowledge
			//save this directly since precise information is given
			//Praktikum.brain.Generalizer.generalizeSingleEffect(newEntry, this.agent.getBrain().getKnowledge(),false, false);
			
			TreeMap<Effect,Double> newEffect = new TreeMap<Effect,Double>();
			
			newEffect.put(feedbackCommand.getEffect(), feedbackCommand.getProbability());
			
			this.agent.getBrain().updateKnowledge(new SimpleEntry<ConditionDirectionPair, TreeMap<Effect,Double>>(conditionDirectionPair,newEffect), KnowledgeSource.FULLY_TRUSTED);

			return true;
		}
		return false;
	}

	/**
	 * creates sentence from a given knowledge entry which represents actors
	 * answer to a question. The answer is generated as a natural sentence and
	 * then executed and "spoken" through the VoiceControl.
	 * 
	 * @param answer
	 *            : knowledge entry representing actor's answer.
	 * @return
	 */
	public String createSentence(Entry<ConditionDirectionPair, TreeMap<Effect, Double>> answer) 
	{
		String knowResponse = new KnowledgeResponse(answer, sentenceGenerator).toString();
		//PlayHook.voiceControl.display(knowResponse, agent);
		return knowResponse;
	}

}
