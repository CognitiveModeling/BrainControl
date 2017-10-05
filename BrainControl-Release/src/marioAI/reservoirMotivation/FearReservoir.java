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
package marioAI.reservoirMotivation;

import java.util.TreeMap;

import marioAI.agents.CAEAgent;
import marioAI.brain.Brain;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.collision.ConditionDirectionPair;
import marioAI.goals.Goal;
import marioAI.goals.ReservoirEventType;
import marioAI.movement.GoalDeque;
import marioAI.movement.GoalSearchWrapper;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioAI.run.PlayHook;
import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;

/**
 * Class that controls Mario's fear (and courage) by using a reservoir.
 * 
 * @author Jannine, Matthias, Leona
 * 
 */

public class FearReservoir extends ListenerReservoir {

	/*
	 * Mario's current health condition UNDEFINED = -1, SMALL = 0,
	 * LARGE_NON_BULB = 1, BULB = 2, INVULNERABLE = 3
	 */
	private int playerHealth;

	private double slope = 0.001;
	
	/* if the reservoir's value is over this threshold, Mario is afraid */
	private double fearThreshold = 0.7;

	/* if the reservoir's value is beneath this threshold, Mario is courageous */
	private double courageThreshold = 0.1;

	/* current emotional status (0 = afraid, 1 = normal, 2 = courageous) */
	private int emotionalStatus;

	/* Marios phrases to be spoken if he becomes courageous */
	private String[] courage = new String[9];

	/* Marios phrases to be spoken if he becomes fearful */
	private String[] fear = new String[7];

	/*
	 * the planner with which this fear reservoir is used (from the
	 * ReservoirDoublePlanner)
	 */
	//private CuriosityPlanner planner;

	/* Mario's knowledge, used to check if goals are still ok */
	private TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge;

	/* agent required for speech output */
	private CAEAgent agent;
	

	/**
	 * constructor of the FearReservoir
	 * 
	 * @param brain
	 *            : the brain that contains Mario's knowledge
	 * @param planner
	 *            : the MotivationPlanner with which this fear reservoir is used
	 */
	public FearReservoir(/*CuriosityPlanner planner*/) {
		super(-1,0,0,-1,1,"Fear",ReservoirType.FEAR,ReservoirEventType.HEALTH_CHANGE);
		//this.planner = planner;
		// set the FearReservoir of the planner (so that the planner can check
		// if a computed goal is still ok if Mario is afraid)
//		planner.setFearReservoir(this);
		setCouragePhrases();
		setFearPhrases();

		// initial value of the FearReservoir
		value = 0.15;
	}

	public void setAgent(CAEAgent agent) {
		this.agent = agent;
	}

	public void setKnowledge(Brain brain) {
		knowledge = brain.getKnowledge();
	}

	/**
	 * Updates the fear reservoir (depending on the health condition). Called
	 * every time step.
	 * 
	 * @param levelScene
	 *            : the current levelScene
	 */
	@Override
	public void onTick(LevelScene levelScene) {
		// get Marios current health
		playerHealth = levelScene.getPlayers().get(0).getNonStaticHealth();	//TODO

		// if Mario is small, increase fear, else decrease it
		if (playerHealth == 0) {
			// his fear converges to 1
			add((1 - value) * slope);
		} else {
			// his fear converges to 0
			add((1 - value) * slope);
//			add(-value * 0.005);
		}

		// check if emotional status has changed because of altered values
		checkEmotionChange();

	}

//	/**
//	 * Called if trigger effects were observed. Increases or decreases the fear
//	 * according.
//	 * 
//	 * @param change: the type of change that has taken place
//	 */
//	@Override
//	public void update(Observable arg0, Object arg1)
//	{
//		if (arg1 instanceof ReservoirEvent) 
//		{
//			ReservoirEvent event = (ReservoirEvent) arg1;
//			switch(event.type) 
//			{
//			case PLAYER_HEALTH_CHANGE:
//				add(-event.parameter*0.5); // decrease fear if Mario's health increased
//				break;
//			default:
//			}
//		}
//		// check if emotional status has changed because of altered values
//		checkEmotionChange();
//	}

	/**
	 * Checks for emotional changes (due to threshold crossing). Sends
	 * notifications and checks goals, if changes took place.
	 */
	private void checkEmotionChange() {
		if (value >= fearThreshold) { // if the fear threshold is crossed
			if (emotionalStatus != 0) { // and Mario wasn't afraid before
				emotionalStatus = 0; // change the emotional status

				// Marios status changes to fear --> he gives a speech output
				// and trembles

				// sets the emotionActions list on the corresponding state
				agent.setEmotionActionType(CAEAgent.EmotionActionType.FEAR_ACTION);

				// random value to choose any sentence of the fear-phrases
				double rand = Math.random() * fear.length;
				
				// speech output
				//agent.display(fear[(int) rand], 0);
				PlayHook.voiceControl.display(fear[(int) rand], agent);

				// check if Mario's goals are still ok (valid). Since there
				// might be goals in the queue which make Mario interact with an
				// enemy. But when Mario becomes fearful, he does NOT want
				// to interact with enemies anymore!
				GoalDeque goals = agent.getAStarSimulator().getAllGoals();
				Goal newGoal;

				// iterate over all goals
				for (GoalSearchWrapper goal : goals) {
					if (goal != null) { // -->AStarSimulator
						newGoal = goal.getGoal();
						if (!isGoalStillOk(newGoal)) {
							agent.getAStarSimulator().deleteAllGoalsSince(goal);
							break;
						}
					}
				}
			}
//			planner.setAfraid(true); // notify planner

			// if the courage threshold was crossed
		} else if (value <= courageThreshold) {
			if (emotionalStatus != 2) { // and Mario wasn't courageous before
				emotionalStatus = 2; // change the emotional status

				// Mario alarms his enemies that he is now more aggressive
				// (courageous) -> he will jump around a bit
				agent.setEmotionActionType(CAEAgent.EmotionActionType.COURAGEOUS_ACTION);

				// random value to choose any sentence of the fear-phrases
				double rand = Math.random() * courage.length;

				// speech output
				//agent.display(courage[(int) rand],0);
				PlayHook.voiceControl.display(courage[(int) rand], agent);

//				planner.setAfraid(false);
			}
		} else { // if Mario is between the thresholds
			if (emotionalStatus != 3) { // and Mario wasn't in the normal state
										// before
				emotionalStatus = 3; // change the emotional status

				// TODO: send notifications to CAEAgent for particular behavior
				// notifyThatEmotionChanged(); //normal
//				planner.setAfraid(false);
			}
		}
	}

	/**
	 * Returns the name of the reservoir.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Checks if a goal is still ok, i.e. if there is no entry in knowledge that
	 * says it causes health decrease (note that it is irrelevant whether Mario
	 * knows for sure that he will take damage (probability 1) or generalized
	 * this effect (probability < 1)).
	 * 
	 * @param goal
	 *            : the goal to be tested
	 * @return true, if the goal is still ok, false otherwise
	 */
	public boolean isGoalStillOk(Goal goal) {
		PlayerWorldObject target = goal.getTarget(); // get the target object of
													// the goal

		// iterate over all ConditionDirectionPairs in knowledge
		for (ConditionDirectionPair pair : knowledge.keySet()) {
			// check if the target object of the goal is the same as the one of
			// the ConditionDirectionPair
			if (target == pair.condition.getTarget()) {
				// check if interaction with this pair might cause damage
				// according to the current knowledge
				Effect effect = new Effect(ActionEffect.OBJECT_HEALTH_DECREASE,
						target);
				if (knowledge.get(pair).containsKey(effect)) {
					return false; // goal is potentially dangerous, not ok
									// anymore
				}
			}
		}
		return true; // goal is still ok
	}

	// every sentence which could be spoken if Mario becomes courageous has to
	// be added into the courage-array
	private void setCouragePhrases() {
		courage[0] = "Shall come now what may, I'm ready!";
		courage[1] = "haha haaaaaaaaaaaaa? Let them come now? I - em - hot";
		courage[2] = "I'm gonna kill them?";
		courage[3] = "rambo mode - on";
		courage[4] = "come on, I won't hurt you?";
		courage[5] = "kick - them!";
		courage[6] = "that will be - fun! hehe";
		courage[7] = "let - me - take them on?";
		courage[8] = "theese - poor - bots!"; // yes it shall be theese
	}

	// every sentence which could be spoken if Mario becomes fearful has to be
	// added to the fear-array
	private void setFearPhrases() {
		fear[0] = "I better watch out, I am fearful";
		fear[1] = "aaaaaaaaaaaaaaaaaaaaa? Somebody help me!";
		fear[2] = "I'm out of here?";
		fear[3] = "help me - please help me!";
		fear[4] = "oh?";
		fear[5] = "shit!";
		fear[6] = "poor - me";
	}

	
	
	public int getEmotionalStatus(){
		return emotionalStatus;
	}
}
