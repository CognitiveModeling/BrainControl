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

import marioAI.goals.ReservoirEventType;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioWorld.engine.LevelScene;

/**
 * HappinessReservoir is a reservoir in ReservoirDoublePlanner it approximates a
 * baseline which is the "normal" happiness. > baseline is very happy
 * 
 * @author Leona, Matthias, Jannine
 * 
 */
public class HappinessReservoir extends ListenerReservoir {


	/* value, to which happiness converges */
	private double baseline = 0.5;

	/* threshold which marks him as being happy */
	private double happinessThreshold = 0.7;

	private boolean isHappy = false;

	/* agent required for speech output */
	//private CAEAgent agent;

	/* phrases Mario can say when he becomes happy */
	private String[] happiness = new String[7];
	

	/**
	 * constructor
	 */
	public HappinessReservoir() 
	{
		super(1.0,0.0,0.0,-1,1,"Happiness",ReservoirType.HAPPY,ReservoirEventType.ENERGY_CHANGE,ReservoirEventType.GOAL_REACHED,ReservoirEventType.KNOWLEDGE_INCREASE,ReservoirEventType.HEALTH_CHANGE);
		value = baseline;
		setHappinessPhrases();
		
		this.minValue=0;
		this.maxValue=1;
		this.steepness=3.0;
		this.avgValue=0.5;
	}

	/**
	 * makes the happiness value always converge to the baseline. If he is more
	 * happy than 0.5, happiness decreases; if he is less happy than 0.5, is
	 * happiness will increase
	 */
	@Override
	public void onTick(LevelScene levelScene) {
		//add((baseline - value) * 0.005);
	}

	public String getName() {
		return name;
	}

//	public void setAgent(CAEAgent agent) {
//		this.agent = agent;
//	}

	/**
	 * update of happiness if object Change is sent and influence Mario's
	 * happiness
	 * 
	 * @param change
	 */
	/*@Override
	public void updateReservoir(Object arg) 
	{
		System.out.println("Happiness reservoir update");
		// when update is called by the AStar environment
		if (arg instanceof TypeOfChange) {
			TypeOfChange change = (TypeOfChange) arg;
			switch (change) {
			case AGENT_REACHED_GOAL:

				// with a certain probability Mario shows his content of
				// reaching his goal, makes some noise and jumps
				if (Math.random() < 0.5) {

					// Marios jump will be set in CAEAgent - his jump will
					// be performed
					agent.setEmotionActionType(CAEAgent.EmotionActionType.HAPPY_ACTION);

					// random value to choose any sentence of the
					// happiness-phrases
					double rand = Math.random() * happiness.length;
					// speech output
					PlayHook.voiceControl.display(happiness[(int)rand], agent);
				}
				// Mario reached his goal - happiness increases for 0.5
				add(0.5);
				break;
			case ASTAR_COULD_NOT_REACH_GOAL:
				// if Mario could not reach his goal, his happiness will
				// decrease for 0.3
				add(-0.3);
				break;
			default:
			}
		} 		
		else if (arg instanceof ReservoirEvent) // when update is called by the reservoir-Planners 
		{
			ReservoirEvent event = (ReservoirEvent) arg;
			switch (event.type) 
			{
			case PLAYER_HEALTH_CHANGE:
				// Marios health increases, his happiness increases by 0.1
				add(event.parameter*0.5);
				break;
			case OBJECT_INTERACTION:
				add(event.parameter*0.1);
				break;
			default:
				break;
			}
		}

		checkHappiness();
	}*/

	/**
	 * check, if Mario is still/not anymore happy
	 */
	public void checkHappiness() {

		// Mario is happy
		if (value >= happinessThreshold) {
			if (!isHappy) {
				isHappy = true;
			}

		// Mario is not happy
		} else if (value < happinessThreshold) {
			if (isHappy) {
				isHappy = false;
			}
		}
	}

	// every phrase which could be spoken if Mario becomes happy has to be added
	// to the happy-array
	private void setHappinessPhrases() {
		happiness[0] = "Yes, I just reached my goal, isn't this a sweet thing?";
		happiness[1] = "I just love it reaching my goals!";
		happiness[2] = "yeahs? got ya";
		happiness[3] = "bada boom?";
		happiness[4] = "one up?";
		happiness[5] = "next goal, please?";
		happiness[6] = "that - was an easy one";
	}
}
