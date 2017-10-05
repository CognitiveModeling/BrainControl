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
package marioUI.speechSynthesis;

/**
* @author fabian
*/
public enum VoiceResponseType
{
	GOODBYE,
	INTRODUCTION,
	GREETINGS,
	CONFIRM_WORLD_GOAL,
	CONFIRM_SELF_MOTIVATION_ON,
	CONFIRM_SELF_MOTIVATION_OFF,
	CONFIRM_MOVEMENT_GOAL,
	CONFIRM_RESERVOIR_GOAL,
	CONFIRM_KNOWLEDGE_INPUT,
	KNOWLEDGE_INPUT_FAILED,
	OUT_OF_ENERGY,
	WORLD_GOAL_REACHED,
	OBJECT_LOOKUP_FAILED,
	PLAYER_LOOKUP_FAILED,
	SENSORIMOTOR_PLANNING_FAILED,
	SCHEMATIC_PLANNING_FAILED,
	SENSORIMOTOR_EXECUTION_FAILED,
	KNOWLEDGE_RESPONSE,
	GOAL_SELECTION_FAILED,
	OFFER_HELP,
	SUGGEST_PLAN,
	REJECT_PLAN,
	ACCEPT_PLAN,
	SUGGEST_GOAL,
	ASK_PLAN,
	FIRST_GOAL,
	INTERMEDIATE_GOAL,
	LAST_GOAL,
	NEGOTIATION_DECLINE, 
	NEGOTIATION_ACCEPT, 
	NEGOTIATION_END,
	NEGOTIATION_BODY,
	NEGOTIATION_START, 
	NEGOTIATION_COUNTER_PLAN;
}
