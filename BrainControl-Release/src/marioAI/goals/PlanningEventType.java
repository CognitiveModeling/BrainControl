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

public enum PlanningEventType
{
	ASTAR_START_SEARCH,		//TODO: actually GUI event -.- 
	NEW_GOAL_SELECTED,		//TODO: actually GUI event -.-
	ASTAR_REACHED_GOAL, 
	ASTAR_COULD_NOT_REACH_GOAL, 
	AGENT_REACHED_GOAL, 
	//EFFECT_CHAINING_FAILED,	//TODO: this should not be necessary if effect chaining is included in planner
	GOAL_SELECTION_FAILED,
	GOALPLAN_CONTAINS_INTERACTION,
	INTERACTION_GOALPLAN_FINISHED,
	COMMON_STRATEGY_SUGGESTION,
	COMMON_STRATEGY_ADOPTION,
	GOALPLAN_SUGGESTION,
	GOALPLAN_ACCEPTION,
	GOALPLAN_REJECTION,
	GOALPLAN_COSTS,
	GOALPLAN_PROBABILITY, 
	EFFECT_CHAINING_ENDED, 
	EFFECT_CHAINING_STARTED,
	EFFECT_CHAINING_FAILED,
	START_ICON, 
	STOP_ICON, 
	NEGOTIATION_PLANNING_ENDED, RESERVOIR_INTERRUPT
};

