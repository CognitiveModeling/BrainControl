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
package marioUI.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marioUI.gui.StartGuiModel.KnowledgeTypes;
import marioUI.gui.StartGuiModel.PlannerTypes;
import marioUI.gui.StartGuiModel.PlayerTypes;

public class AgentConfiguration {
	private PlayerTypes playerType = PlayerTypes.CLARK;
	private PlayerTypes[] defaultPlayerTypes = { PlayerTypes.CLARK, PlayerTypes.BRUCE, PlayerTypes.PETER, PlayerTypes.JAY};
	private String playerName = null;
	private KnowledgeTypes knowledgeType = KnowledgeTypes.NONE;
	private PlannerTypes plannerType = PlannerTypes.VOICE;
	private int index;
	private static String[] defaultPlayerNames = { "Clark", "Bruce", "Peter", "Jay" };

	public AgentConfiguration(int _idx)
	{
		index = _idx;
		playerName = defaultPlayerNames[index];
		playerType = defaultPlayerTypes[index];
	}

	private String microphone;

	public PlayerTypes getPlayerType() {
		return playerType;
	}

	public void setPlayerType(int playerType) {
		this.playerType = StartGuiModel.PlayerTypes.values()[playerType];
	}

	public String getPlayerName() {
		if (playerName == null) {
			return defaultPlayerNames[index];
		} else {
			if (playerName.equals("")) {
				return defaultPlayerNames[index];
			} else {
				return playerName;
			}
		}
	}

	public void setPlayerName(String playerName) {

		Pattern p = Pattern.compile("[^a-z]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(playerName);
		boolean b = m.find();

		if (b) {
			new MarioError(
					"Please rename Player \""
							+ playerName
							+ "\". You may not use characters other than a-z and A-Z. Spaces are not allowed, due to grammar parsing.");
		} else {
			this.playerName = playerName;
		}

	}

	public void setMicrophone(String mic) {
		this.microphone = mic;
	}

	public KnowledgeTypes getKnowledgeType() {
		return knowledgeType;
	}

	public void setKnowledgeType(int knowledgeType) {
		this.knowledgeType = StartGuiModel.KnowledgeTypes.values()[knowledgeType];
	}

	public PlannerTypes getPlannerType() {
		return plannerType;
	}

	public void setPlannerType(int plannerType) {
		this.plannerType = StartGuiModel.PlannerTypes.values()[plannerType];
	}

	public int getIndex() {
		return index;
	}

	public String getMicrophone() {
		return microphone;
	}

}
