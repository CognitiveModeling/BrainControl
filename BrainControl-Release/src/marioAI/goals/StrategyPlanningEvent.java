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

public class StrategyPlanningEvent extends PlanningEvent {

	public int senderIndex;
	public int recipientIndex;
	public boolean append = false;
	
	public StrategyPlanningEvent(PlanningEventType type, Object object, int senderIndex, int recipientIndex) {
		super(type, object);
		this.senderIndex = senderIndex;
		this.recipientIndex = recipientIndex;
	}
	
	// dirty :( use object StrategyAppendPair instead of adding bool field?
	public static StrategyPlanningEvent newGoalplanContainsInteraction(Strategy strategy, int playerIndex, boolean append){
		StrategyPlanningEvent goalPlanContainsInteraction = new StrategyPlanningEvent(PlanningEventType.GOALPLAN_CONTAINS_INTERACTION, strategy, playerIndex, playerIndex);
		goalPlanContainsInteraction.append = append;
		return goalPlanContainsInteraction;
	}

}
