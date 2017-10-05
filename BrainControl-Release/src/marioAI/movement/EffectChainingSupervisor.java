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
package marioAI.movement;

import java.util.ArrayList;
import java.util.Observable;

import marioAI.agents.CAEAgent;
import marioAI.goals.Goal;
import marioAI.goals.GoalDistanceWrapper;
import marioAI.goals.GoalPlanner;
import marioAI.goals.PlanningEvent;
import marioAI.goals.PlanningEventType;
import marioAI.run.PlayHook;
import marioUI.speechSynthesis.VoiceResponse;
import marioUI.speechSynthesis.VoiceResponseType;

public class EffectChainingSupervisor extends Observable implements Runnable {
	
	private CAEAgent activeAgent;
	private Goal originalGoal;
	private boolean append;
	private ArrayList<GoalDistanceWrapper> goallist;
	private boolean isPlanning;

	public EffectChainingSupervisor(Goal originalGoal, CAEAgent agent, GoalPlanner goalPlanner, boolean append) {
		
		this.activeAgent = agent;
		agent.setEffectChainingSupervisor(this);
		this.originalGoal = originalGoal;
		this.append =append;
		activeAgent.getBrain().levelSceneAdapter.createAndSetNewSimulatedLevelScene(activeAgent.getBrain());
		
		addObserver(PlayHook.voiceResponseEventBus);
		addObserver(goalPlanner);
	}

	@Override
	public void run() {
		
		this.setChanged();
		this.notifyObservers(new PlanningEvent(PlanningEventType.EFFECT_CHAINING_STARTED));
		this.isPlanning = true;
		
		 goallist = originalGoal.translateEffectGoal(activeAgent.getPlanner().getLevelSceneAdapter().getSimulatedLevelScene());
		
		if(goallist.size()==0)
		{
//			this.setChanged();
//			this.notifyObservers(new VoiceResponse(VoiceResponseType.SCHEMATIC_PLANNING_FAILED,"EFFECT("+originalGoal.getEffect().getType()+")",activeAgent.getPlayerIndex()));
			this.setChanged();
			this.notifyObservers(new PlanningEvent(PlanningEventType.EFFECT_CHAINING_FAILED));
		}
		
		this.setChanged();
		this.notifyObservers(new PlanningEvent(PlanningEventType.EFFECT_CHAINING_ENDED));
		this.isPlanning = false;
	}
	
	public void stopPlanning() {
		this.activeAgent.getPlanner().setWaitingDuration(60000);
		this.activeAgent.getPlanner().interruptReservoirGoalSelection();
		originalGoal.stop();
	}
	
	public boolean isPlanning() {
		return isPlanning;
	}

	public void setPlanning(boolean isPlanning) {
		this.isPlanning = isPlanning;
	}

	public ArrayList<Goal> getDewrappedGoallist() {
		ArrayList<Goal> res = new ArrayList<Goal>();
		for(GoalDistanceWrapper cur : goallist) {
			res.add(cur.getPreviousInteraction());
		}
		return res;
	}
	public ArrayList<GoalDistanceWrapper> getGoallist() {
		return goallist;
	}
	
	

	public CAEAgent getActiveAgent() {
		return activeAgent;
	}

	public void setActiveAgent(CAEAgent activeAgent) {
		this.activeAgent = activeAgent;
	}

	public Goal getOriginalGoal() {
		return originalGoal;
	}

	public void setOriginalGoal(Goal originalGoal) {
		this.originalGoal = originalGoal;
	}

	public void setGoallist(ArrayList<GoalDistanceWrapper> goallist) {
		this.goallist = goallist;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

}
