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

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.VoicePlanner;
import marioAI.run.options.CustomLevel;
import marioWorld.engine.GameWorld;
import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Clark;
import marioWorld.tools.EvaluationOptions;

/**
 * Play Mario using voice control
 */

public class PlayVoice extends PlayHook {

	public PlayVoice() {
		super(true);
	}

	public static void main(String[] args) throws Exception {
		new PlayVoice();

		// Add voice specific loggers
		Logging.addLogger("Interpreter", Level.INFO);
		Logging.addLogger("TagsToSentences", Level.CONFIG);
		Logging.addLogger("VoicePersons", Level.INFO);
		Logging.addLogger("Agent", Level.ALL);
		Logging.addLogger("Recognizer", Level.INFO);

		// Create new agents and give them planners, also create players
		VoicePlanner planner1 = new VoicePlanner();
		agents.add(new CAEAgent("Clark", 0, planner1, createInitialKnowledge(), voiceControl, planner1));
		players.add(new Clark(0,1));

		//		VoicePlanner planner2 = new VoicePlannerImplementation();
		//		agents.add(new CAEAgent("Jay", planner2, null, voiceControl, planner2));
		//		players.add(new Jay(1));
		//		
		//		VoicePlanner planner3 = new VoicePlannerImplementation();
		//		agents.add(new CAEAgent("Peter", planner3, voiceControl, planner3));
		//		players.add(new Jay(1));
		//		
		//		VoicePlanner planner4 = new VoicePlannerImplementation();
		//		agents.add(new CAEAgent("Norbert", planner4, voiceControl, planner4));
		//		players.add(new Clark(1));
		//
		//		VoicePlanner planner5 = new VoicePlannerImplementation();
		//		agents.add(new CAEAgent("Karl", planner5, voiceControl, planner5));
		//		players.add(new Clark(1));

		voiceControl.init();

		EvaluationOptions options = new CustomLevel(agents, players, "arena").getOptions();

		options.setLevelRandSeed(1889574193);

		run(options);
	}

	/*
	 * Once the GameWorld is prepared, tell it about its players
	 * 
	 * @see
	 * ch.idsia.mario.engine.EngineHook#OnGameWorldPrepared(ch.idsia.mario.engine
	 * .GameWorld)
	 */
	public void OnGameWorldPrepared(GameWorld component) 
	{
		GlobalContinuous[] playerStartPositions = 
			{ 
					new GlobalContinuous(5 * 16, 40),	//TEST 
					new GlobalContinuous(25, 20),	
					new GlobalContinuous(130, 20), 
					new GlobalContinuous(100, 20), 
					new GlobalContinuous(60, 20) 
			};

		PlayerHealthCondition[] playerHealthConditions = { PlayerHealthCondition.LARGE_NON_BULB,
				PlayerHealthCondition.LARGE_NON_BULB, PlayerHealthCondition.LARGE_NON_BULB,
				PlayerHealthCondition.LARGE_NON_BULB, PlayerHealthCondition.LARGE_NON_BULB };

		int[] playerFacing = new int[] { 1, -1, 1, -1, 1 };
		int[] initialEnergy = new int[]{100,100,100,100,100};

		initPlayers(component, playerStartPositions, null, playerFacing, playerHealthConditions,initialEnergy);

		//Sprite flower = addSprite(component, PlayerWorldObject.BULB_FLOWER,new GlobalCoarse(7, 10).toGlobalContinuous());
	}

	public static Map<ConditionDirectionPair, TreeMap<Effect, Double>> createInitialKnowledge() {

		Map<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge = new TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>>();

				TreeMap<Effect, Double> effects = new TreeMap<Effect, Double>();
				effects.put(new Effect(ActionEffect.ENERGY_INCREASE,
						PlayerWorldObject.CLARK), 0.5);
				knowledge.put(
						new ConditionDirectionPair(new Condition(
								PlayerWorldObject.CLARK,
								PlayerWorldObject.QUESTIONMARK_BLOCK1,
								PlayerHealthCondition.LARGE_NON_BULB),
								CollisionDirection.BELOW), effects);

		TreeMap<Effect, Double> effects2 = new TreeMap<Effect, Double>();
		effects2.put(new Effect(ActionEffect.OBJECT_HEALTH_INCREASE, PlayerWorldObject.CLARK), 1.0);
		knowledge.put(new ConditionDirectionPair(new Condition(PlayerWorldObject.CLARK, PlayerWorldObject.BULB_FLOWER,
				PlayerHealthCondition.LARGE_NON_BULB), CollisionDirection.IRRELEVANT), effects2);

		return knowledge;
	}

}
