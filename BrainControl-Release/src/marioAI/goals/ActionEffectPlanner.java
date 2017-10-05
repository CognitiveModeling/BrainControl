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
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioWorld.engine.PlayerWorldObject;

/**
 * @author Marcel
 * tries to set a specific action-effect as Goal
 */

public class ActionEffectPlanner extends GoalPlanner
{	
	Effect effect;
	
	ActionEffect effectType;

	public  ActionEffectPlanner (ActionEffect effectType) {
		
		this.effectType = effectType;
	}
	
	@Override
	public ArrayList<Goal> selectGoal() 
	{
//		// Begin: GoalPlanResponse tested (It looks like this Doesn't do anything)
//		System.out.println("Goal with Effect selected: " + this.effect.toString());
//		update(this, new PlanningEvent(PlanningEventType.NEW_GOAL_SELECTED, new ArrayList<>().add(new Goal(this.effect))));
//		// End: GoalPlanResponse tested

//   	Effect applied to actual Player instead of "PLAYER"
		System.out.println(this.levelSceneAdapter.getSimulatedLevelScene().getPlanningPlayer());
		 ArrayList<Goal> result = new ArrayList<Goal>();
		 result.add((new Goal(agent.getPlayer().getType(), this.effect)));
		//return (new Goal(agent.getPlayer().getType(), this.effect)).translateEffectGoal(this.levelSceneAdapter.getSimulatedLevelScene());
		return result;
//	
//		
	}

	@Override
	protected String getGoalSelectionFailureIdentifier() 
	{
		return "PLANNERTYPE("+effectType.name().toUpperCase()+")";
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void init(CAEAgent agent, LevelSceneAdapter levelSceneAdapter) 
	{
		super.init(agent, levelSceneAdapter);
		this.effect = new Effect(this.effectType, this.agent.getPlayer().getType());
		System.out.println("Effect: " + this.effect);
	}
	
	
}
