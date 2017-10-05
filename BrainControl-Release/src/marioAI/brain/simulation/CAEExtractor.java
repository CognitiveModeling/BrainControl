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
package marioAI.brain.simulation;

import java.util.AbstractMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.Generalizer;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.ConditionDirectionPair;
import marioAI.run.PlayHook;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.ObjectType;
/**
 * Derives CAE-effects from two observations.
 * 
 * @author Stephan
 * 
 */
public class CAEExtractor {

	private final SimulatedLevelScene oldScene;
	private final SimulatedLevelScene newScene;
	private final ConditionDirectionPair collision;
	private int playerIndex;

	private CAEExtractor(SimulatedLevelScene derivedByForwardModel, SimulatedLevelScene newMeasurement, LevelSceneAdapter levelSceneAdapter,
			ConditionDirectionPair collision, int playerIndex) {
		this.oldScene = derivedByForwardModel;
		this.newScene = newMeasurement;
		this.collision = collision;
		this.playerIndex = playerIndex;
	}

	/**
	 * Derives CAE-effect differences from two observations.
	 * 
	 * @param derivedByForwardModel
	 *            the result of the brain's forward simulation (simulated t->t+1)
	 * @param newMeasurement
	 *            the new measurement (at time t+1)
	 * @param levelSceneAdapter
	 * @param collision
	 *            the object with which mario collides
	 * @return CAE-Effects registered during transition from t to t+1
	 */
	public static Entry<ConditionDirectionPair, TreeMap<Effect, Double>> getDifferences(SimulatedLevelScene derivedByForwardModel, SimulatedLevelScene newMeasurement, LevelSceneAdapter levelSceneAdapter, ConditionDirectionPair collision, int playerIndex) 
	{
		CAEExtractor cAEExtractor = new CAEExtractor(derivedByForwardModel, newMeasurement, levelSceneAdapter, collision, playerIndex);
		return cAEExtractor.getDifferences();
	}

	/**
	 * This method should not be confused with Comparable.compareTo(), as this method does not return a number and is not used for sorting. Rather, it returns
	 * all detected differences between the two level scenes.
	 * 
	 * @param other
	 */
	private Entry<ConditionDirectionPair, TreeMap<Effect, Double>> getDifferences() 
	{
		TreeMap<Effect,Double> effectList = new TreeMap<Effect,Double>();
		
		/*
		 * for each position in the compared maps: test, if the element is different. If it is, create destruction/creation effect.
		 */
		for (ObjectType staticType : new ObjectType[] { ObjectType.STATIC, ObjectType.NON_STATIC }) 
		{
			TreeMap<Effect,Double> effects = this.findUnequal(staticType);
			/*if (!effects.isEmpty()) 
			{
				dumpObservation();
			}*/
			effectList.putAll(effects);
		}

		// test, if Mario's health has changed
		 
		PlayerWorldObject player = newScene.simulatedPlayers.get(playerIndex).getType();
		//if I understand this right it is not necessary to do this because learning through watching happens in the brain
		int healthChange = newScene.simulatedPlayers.get(playerIndex).getHealth() - oldScene.simulatedPlayers.get(playerIndex).getHealth();
		if (healthChange < 0 || newScene.simulatedPlayers.get(playerIndex).getBirthTime() == 0)
		{	
			effectList.put(new Effect(ActionEffect.OBJECT_HEALTH_DECREASE, player),1.0);
		}
		else if (healthChange > 0)
		{
			effectList.put(new Effect(ActionEffect.OBJECT_HEALTH_INCREASE, player),1.0);
		}

		// test, if mario found energy
		int energyChange = newScene.simulatedPlayers.get(playerIndex).getNumEnergys() - oldScene.simulatedPlayers.get(playerIndex).getNumEnergys();
		if (energyChange > 0) 
		{			
			effectList.put(new Effect(ActionEffect.ENERGY_INCREASE, player),1.0);
		}
		
		// test, if mario has mounted another player
		if(newScene.simulatedPlayers.get(playerIndex).getCarriedBy() != null && oldScene.simulatedPlayers.get(playerIndex).getCarriedBy() == null)
		{
			effectList.put(new Effect(ActionEffect.MOUNT, newScene.simulatedPlayers.get(playerIndex).getCarriedBy().type), 1.0);
		}
		
		// test, if mario has unmounted from another player
		if(newScene.simulatedPlayers.get(playerIndex).getCarriedBy() == null && oldScene.simulatedPlayers.get(playerIndex).getCarriedBy() != null)
		{
			effectList.put(new Effect(ActionEffect.UNMOUNT, oldScene.simulatedPlayers.get(playerIndex).getCarriedBy().type), 1.0);
		}
		
		// test, if mario carried another player
		if((newScene.simulatedPlayers.get(playerIndex).getCarries() != null) && (oldScene.simulatedPlayers.get(playerIndex).getCarries() == null))
		{
			effectList.put(new Effect(ActionEffect.CARRY, newScene.simulatedPlayers.get(playerIndex).getCarries().type), 1.0);
		}
		
		// test, if mario dropped another player
		if(newScene.simulatedPlayers.get(playerIndex).getCarries() == null && oldScene.simulatedPlayers.get(playerIndex).getCarries() != null)
		{
			effectList.put(new Effect(ActionEffect.DROP, oldScene.simulatedPlayers.get(playerIndex).getCarries().type), 1.0);
		}

		//check if there already is knowledge with probability one
		//if not then there is the effect "NOTHING_HAPPENS" learned ...
		if (effectList.isEmpty() && collision != null) 
		{
			if((((CAEAgent) PlayHook.agents.get(playerIndex)).getBrain().getKnowledge().get(collision) == null))
			{
				effectList.put(new Effect(ActionEffect.NOTHING_HAPPENS, collision.condition.getTarget()),1.0);
			}
			else if(!((CAEAgent) PlayHook.agents.get(playerIndex)).getBrain().getKnowledge().get(collision).containsValue(1.0))
			{
				effectList.put(new Effect(ActionEffect.NOTHING_HAPPENS, collision.condition.getTarget()),1.0);
			}			
		}
		
		if (effectList.isEmpty()) {
			return null;
		}

		if (collision == null) 
		{
			System.err.println("time " + (oldScene.elapsedTime + 1) + ": found effects but no collision. Effects are:\n");
		}		

		//String condition = collision.toString();
		//Logging.logFine("Brain", "Effects for [" + condition + "]: " + effectList.toString());
		
		/*System.out.println("Effects found: ");
		for(Effect e : effectList)
			System.out.println("\t"+e);*/		
		
		return new AbstractMap.SimpleEntry<ConditionDirectionPair, TreeMap<Effect, Double>>(collision, effectList);
	}

	/**
	 * Returns the differences between the expected, simulated observation and the real observation.
	 * 
	 * @param effectList
	 * @param simulatedObservation
	 * @param realObservation
	 * @param isStatic
	 * @return set of found changes as effects
	 */
	private TreeMap<Effect,Double> findUnequal(ObjectType isStatic) 
	{
		if(isStatic==ObjectType.STATIC)
		{
			//use global coordinates, since player/camera movement may change the local observation
			PlayerWorldObject[][] derivedObservation = oldScene.getGlobalStaticSimulation();
			PlayerWorldObject[][] newObservation = newScene.getGlobalStaticSimulation();
			return findUnequal(derivedObservation, newObservation);
		}
		else
		{
			TreeMap<Effect,Double> effectList = new TreeMap<Effect,Double>();
						
			for(SimulatedSprite sprite : newScene.getSimulatedSprites())
			{
				//System.out.println(sprite);
			
				//observed sprite wasn't there before:
				if(!oldScene.getSimulatedSprites().contains(sprite))
				{
					//System.out.println("created");
					
					effectList.put(new Effect(ActionEffect.OBJECT_CREATION, sprite.type),1.0);
				}
			}

			for(SimulatedSprite sprite : oldScene.getSimulatedSprites())
			{
				//System.out.println(sprite);
			
				//observed sprite already deleted
				if(!newScene.getSimulatedSprites().contains(sprite))
				{
					//System.out.println("created");
					
					effectList.put(new Effect(ActionEffect.OBJECT_DESTRUCTION, sprite.type),1.0);
				}
			}			
			
			for(SimulatedSprite sprite : newScene.getSimulatedSprites())
			{
				//System.out.println(sprite);
				
				//observed sprite has another dead time than before
				if(oldScene.getSimulatedSprites().contains(sprite) && sprite.getDeadTime()>0)
				{
					//System.out.println("destroyed");
					
					effectList.put(new Effect(ActionEffect.OBJECT_DESTRUCTION, sprite.type),1.0);
				}
			}			
			
			return effectList;
		}
	}

	// /**use a wrapper to create custom formatting of an array in Eclipse Debug
	// Mode (Eclipse Debug Mode uses toString().)*/
	// private static class DebugHelpObservationWrapper {
	// MarioWorldObject[][] marioWorldObjects;
	// public DebugHelpObservationWrapper(MarioWorldObject[][]
	// marioWorldObjects) {
	// this.marioWorldObjects = marioWorldObjects;
	// }
	// /**interesting part*/
	// @Override
	// public String toString() {
	// return GeneralUtil.observationOut("", marioWorldObjects);
	// }
	// }

	// /**
	// * Finds differences between two observations given a mario-position. This
	// * method allows for easier debugging than the other method's interface.
	// *
	// * @param globalMarioPos
	// * @param derivedObservation global map
	// * @param newObservation global map
	// * @return
	// */
	// static TreeSet<Effect> findUnequal(GlobalCoarse globalMarioPos,
	// MarioWorldObject[][] derivedObservation,
	// MarioWorldObject[][] newObservation) {
	// int minX = Math.max(0, globalMarioPos.x - RELEVANT_DISTANCE);
	// int maxX = Math.min(derivedObservation[0].length - 1, globalMarioPos.x
	// + RELEVANT_DISTANCE);
	// int minY = Math.max(0, globalMarioPos.y - RELEVANT_DISTANCE);
	// int maxY = Math.min(derivedObservation.length - 1, globalMarioPos.y
	// + RELEVANT_DISTANCE);
	//
	// TreeSet<Effect> effectList = new TreeSet<Effect>();
	// // for each cell: if the maps are different, add object
	// // destruction/creation to the effect list
	// for (int y = minY; y <= maxY; y++) {
	// for (int x = minX; x <= maxX; x++) {
	// MarioWorldObject derivedWorldObj = LearningHelper
	// .generalize(derivedObservation[y][x]);
	// MarioWorldObject newWorldObj = LearningHelper
	// .generalize(newObservation[y][x]);
	//
	// // check if any element is different from the lastObservation
	// if (!derivedWorldObj.equals(newWorldObj)) {
	// if (LearningHelper.isRelevantToLearning(derivedWorldObj)) {
	// Effect e = new Effect(ActionEffect.OBJECT_DESTRUCTION,
	// derivedWorldObj);
	// effectList.add(e);
	// String foundAt = "found @(" + x + "," + y + ")";
	// Logging.logFinest("Brain", "Effect " + foundAt + ": "
	// + e);
	// }
	// if (LearningHelper.isRelevantToLearning(newWorldObj)) {
	// Effect e = new Effect(ActionEffect.OBJECT_CREATION,
	// newWorldObj);
	// effectList.add(e);
	// String foundAt = "found @(" + x + "," + y + ")";
	// Logging.logFinest("Brain", "Effect " + foundAt + ": "
	// + e);
	// }
	// }
	// }
	// }// end of for each cell
	// return effectList;
	// }

	/**
	 * Finds differences between two observations given a mario-position. This method allows for easier debugging than the other method's interface.
	 * 
	 * @param globalMarioPos
	 * @param derivedObservation
	 *            local map
	 * @param newObservation
	 *            local map
	 * @return
	 */
	static TreeMap<Effect,Double> findUnequal(PlayerWorldObject[][] derivedObservation, PlayerWorldObject[][] newObservation) 
	{
		// int minX = Math.max(0, globalMarioPos.x - RELEVANT_DISTANCE);
		// int maxX = Math.min(derivedObservation[0].length - 1,
		// globalMarioPos.x
		// + RELEVANT_DISTANCE);
		// int minY = Math.max(0, globalMarioPos.y - RELEVANT_DISTANCE);
		// int maxY = Math.min(derivedObservation.length - 1, globalMarioPos.y
		// + RELEVANT_DISTANCE);	
		
		TreeMap<Effect,Double> effectList = new TreeMap<Effect,Double>();
		// for each cell: if the maps are different, add object
		// destruction/creation to the effect list
		for (int y = 0; y < derivedObservation.length; y++) 
		{
			for (int x = 0; x < derivedObservation[y].length; x++) 
			{		
				PlayerWorldObject derivedWorldObj = Generalizer.generalizePlayerWorldObject(derivedObservation[y][x]);
				PlayerWorldObject newWorldObj = Generalizer.generalizePlayerWorldObject(newObservation[y][x]);
				
				/*if(newObservation[y][x]==null)	//TEST
					newWorldObj=PlayerWorldObject.NONE;
				if(derivedObservation[y][x]==null)	//TEST
					derivedWorldObj=PlayerWorldObject.NONE;*/
				
				//if ((derivedWorldObj == null) ^ (newWorldObj == null)) // ^ is the XOR-operator
				/*if ((derivedWorldObj == null) || (newWorldObj == null))
				{
					//throw new IllegalArgumentException("Unequal ranges of maps?");
					//weird
					continue;
				}*/
				
				// check if any element is different from the lastObservation
				if(!derivedWorldObj.equals(newWorldObj)) 
				{
					//@c2-pavel:
					// ex-LearningHelper
					if (derivedWorldObj.isRelevantToLearning()) 
					{
						effectList.put(new Effect(ActionEffect.OBJECT_DESTRUCTION, derivedWorldObj),1.0);
						String foundAt = "found @(" + x + "," + y + ")";
						//Logging.logFinest("Brain", "Effect " + foundAt + ": " + e);
						System.out.println("Effect " + foundAt);
					}
					
					//@c2-pavel: ex-Learninghelper
					if (newWorldObj.isRelevantToLearning()) 
					{
						effectList.put(new Effect(ActionEffect.OBJECT_CREATION, newWorldObj),1.0);
						String foundAt = "found @(" + x + "," + y + ")";
						//Logging.logFinest("Brain", "Effect " + foundAt + ": " + e);
						System.out.println("Effect " + foundAt);
					}
				}
			}
		}
				
		return effectList;
	}

	/**
	 * Debugging function to dump the observation to the log file.
	 */
	/*private void dumpObservation() 
	{
		final int timeDelay = GeneralUtil.NearestObject.TIME_DELAY_FOR_SPRITES;
		PlayerWorldObject[][] derivedObservationStatic = derivedByForwardModel.getGlobalStaticSimulation();
		PlayerWorldObject[][] derivedObservationDynamic = derivedByForwardModel.getGlobalMovingSimulation(timeDelay);
		PlayerWorldObject[][] newObservationStatic = newMeasurement.getGlobalStaticSimulation();
		PlayerWorldObject[][] newObservationDynamic = newMeasurement.getGlobalMovingSimulation(timeDelay);

		GlobalContinuous expm = derivedByForwardModel.simulatedPlayers.get(playerIndex).getPosition(timeDelay);
		GlobalContinuous actm = newMeasurement.simulatedPlayers.get(playerIndex).getPosition(timeDelay);
		GlobalCoarse expmDiscrete = expm.toGlobalCoarse();
		String expmPos = "(" + expmDiscrete.x + ", " + expmDiscrete.y + ")";
		GlobalCoarse actmDiscrete = actm.toGlobalCoarse();
		String actmPos = "(" + actmDiscrete.x + ", " + actmDiscrete.y + ")";

		String[] dumpExpected = PlayerWorldObjectParser.dumpToString(derivedObservationDynamic, derivedObservationStatic);
		String[] dumpActual = PlayerWorldObjectParser.dumpToString(newObservationDynamic, newObservationStatic);

		dumpExpected = GeneralUtil.addMarker(dumpExpected, expmDiscrete.x, expmDiscrete.y);
		String a = "expected (Player@" + expmPos + "): \n" + GeneralUtil.collapse(dumpExpected, "\n");

		dumpActual = GeneralUtil.addMarker(dumpActual, actmDiscrete.x, actmDiscrete.y);
		String b = "\ngotten (Player@" + actmPos + "): \n" + GeneralUtil.collapse(dumpActual, "\n");
		Logging.logFinest("Brain", a + b);
	}*/

	// /**
	// * Returns a CondtionActionPair consisting of the nearest relevant
	// * MarioWorldObject, Mario's current health, and the direction from which
	// * Mario interacts with the MarioWorldObject.
	// *
	// * @param stateBasedOnLastObservation
	// * @return ConditionActionPair
	// */
	// private ConditionDirectionPair getConditionDirectionPair() {
	// final int timeDelayForSprites =
	// GeneralUtil.NearestObject.TIME_DELAY_FOR_SPRITES;
	// // map irrelevant WorldObjects to zero
	// //
	// simplifyObservation(stateBasedOnLastObservation.levelSceneObservation);
	// // should not be needed anymore
	// // compute position of the nearest static object
	//
	// // TODO why not use old mario pos already here?
	// SimulatedMario simMario = derivedByForwardModel.simulatedMario;
	// GlobalCoarse globalDiscreteMarioPos = simMario.getPosition(0)
	// .toGlobalCoarse();
	// MarioWorldObject[][] globalStaticMap = derivedByForwardModel
	// .getObservation(ObjectType.STATIC, 0, levelSceneAdapter);
	//
	// MarioWorldObject[][] timeDelayedSpriteMap = derivedByForwardModel
	// .getObservation(ObjectType.NON_STATIC, timeDelayForSprites,
	// levelSceneAdapter);
	//
	// NearestObject nearestObject = GeneralUtil.getNearestObject(
	// globalStaticMap, timeDelayedSpriteMap, globalDiscreteMarioPos);
	//
	// if (nearestObject == null) {
	// Logging.logWarning("Brain", "Found effect but no nearest object!");
	// return new ConditionDirectionPair(new Condition(null,
	// derivedByForwardModel.marioHealthBeforeSimulation), null);
	// // return null;
	// }
	//
	// GlobalCoarse oldOldGlobalDiscreteMarioPos = simMario.getPosition(2)
	// .toGlobalCoarse();
	//
	// CollisionDirection direction = GeneralUtil.getDirection(
	// oldOldGlobalDiscreteMarioPos.x, oldOldGlobalDiscreteMarioPos.y,
	// nearestObject.getPosition().x, nearestObject.getPosition().y);
	// return new ConditionDirectionPair(new Condition(
	// nearestObject.getMarioWorldObject(),
	// derivedByForwardModel.marioHealthBeforeSimulation), direction);
	// }

	// /**
	// * Centered on the object: Returns in which direction Mario is.
	// *
	// * @param yObject
	// * @param xObject
	// * @return Direction
	// */
	// private static CollisionDirection getDirection(float marioX, float
	// marioY,
	// float xObject, float yObject) {
	// boolean leftORRight = Math.abs((yObject - marioY) / (xObject - marioX))
	// <= 1;
	// if (leftORRight) {
	// if (marioX < xObject)
	// return CollisionDirection.LEFT;
	// return CollisionDirection.RIGHT;
	// }
	// if (marioY < yObject)
	// return CollisionDirection.ABOVE;
	// return CollisionDirection.BELOW;
	// }
}
