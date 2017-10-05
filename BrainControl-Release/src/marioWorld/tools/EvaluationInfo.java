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
package marioWorld.tools;

import java.text.DecimalFormat;

import marioWorld.engine.sprites.Player;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy Date: Apr 12, 2009 Time: 12:44:51 AM Package: .Tools
 */
public class EvaluationInfo {
	private static final int MagicNumberUndef = -42;
	public int levelType = MagicNumberUndef;
	public int playerStatus = MagicNumberUndef;
	public int livesLeft = MagicNumberUndef;
	public double lengthOfLevelPassedPhys = MagicNumberUndef;
	public int lengthOfLevelPassedCells = MagicNumberUndef;
	public int totalLengthOfLevelCells = MagicNumberUndef;
	public double totalLengthOfLevelPhys = MagicNumberUndef;
	public int timeSpentOnLevel = MagicNumberUndef;
	public int totalTimeGiven = MagicNumberUndef;
	public int numberOfGainedEnergys = MagicNumberUndef;
	// public int totalNumberOfEnergys = MagicNumberUndef;
	public int totalActionsPerfomed = MagicNumberUndef;
	public int totalFramesPerfomed = MagicNumberUndef;
	// Number Of collisions with creatures
	// if large
	// if bulb
	public String Memo = "";
	public int timeLeft = MagicNumberUndef;
	public String agentName = "undefinedAgentName";
	public String agentType = "undefinedAgentType";
	public int levelDifficulty = MagicNumberUndef;
	public int levelRandSeed = MagicNumberUndef;
	public int playerMode = MagicNumberUndef;
	public int killsTotal = MagicNumberUndef;

	public double computeBasicFitness() {
		// neglect totalActionsPerfomed;
		// neglect totalLengthOfLevelCells;
		// neglect totalNumberOfEnergys;
		return lengthOfLevelPassedPhys - timeSpentOnLevel + numberOfGainedEnergys + playerStatus * 5000;
	}

	public double computeDistancePassed() {
		return lengthOfLevelPassedPhys;
	}

	public int computeKillsTotal() {
		return this.killsTotal;
	}

	// TODO: possible fitnesses adjustments: penalize for collisions with
	// creatures and especially for suicide. It's a sin.

	public double[] toDouble() {

		return new double[] { playerStatus, lengthOfLevelPassedPhys, totalLengthOfLevelCells, timeSpentOnLevel, numberOfGainedEnergys,
				// totalNumberOfEnergys,
				totalActionsPerfomed, totalFramesPerfomed, computeBasicFitness() };
	}

	private DecimalFormat df = new DecimalFormat("0.00");

	public String toString() {

		String ret = "\nStatistics. Score:";
		ret += "\n                  Player/Agent type : " + agentType;
		ret += "\n                  Player/Agent name : " + agentName;
		ret += "\n                      Player Status : " + ((playerStatus == Player.STATUS_WIN) ? "Win!" : "Loss...");
		ret += "\n                         Level Type : " + levelType;
		ret += "\n                   Level Difficulty : " + levelDifficulty;
		ret += "\n                    Level Rand Seed : " + levelRandSeed;
		ret += "\n                         Lives Left : " + livesLeft;
		ret += "\nTotal Length of Level (Phys, Cells) : " + "(" + totalLengthOfLevelPhys + "," + totalLengthOfLevelCells + ")";
		ret += "\n                      Passed (Phys) : " + df.format(lengthOfLevelPassedPhys / totalLengthOfLevelPhys * 100) + "% ( "
				+ df.format(lengthOfLevelPassedPhys) + " of " + totalLengthOfLevelPhys + ")";
		ret += "\n                     Passed (Cells) : " + df.format((double) lengthOfLevelPassedCells / totalLengthOfLevelCells * 100) + "% ( "
				+ lengthOfLevelPassedCells + " of " + totalLengthOfLevelCells + ")";
		ret += "\n             Time Spent(Fractioned) : " + timeSpentOnLevel + " ( " + df.format((double) timeSpentOnLevel / totalTimeGiven * 100) + "% )";
		ret += "\n              Time Left(Fractioned) : " + timeLeft + " ( " + df.format((double) timeLeft / totalTimeGiven * 100) + "% )";
		ret += "\n                   Total time given : " + totalTimeGiven;
		// ret += "\nEnergys Gained: " +
		// numberOfGainedEnergys/totalNumberOfEnergys*100 + "%. (" +
		// numberOfGainedEnergys + " of " + totalNumberOfEnergys + ")";
		ret += "\n                       Energys Gained : " + numberOfGainedEnergys;
		ret += "\n             Total Actions Perfomed : " + totalActionsPerfomed;
		ret += "\n              Total Frames Perfomed : " + totalFramesPerfomed;
		ret += "\n               Simple Basic Fitness : " + df.format(computeBasicFitness());
		ret += "\nMemo: " + ((Memo.equals("")) ? "Empty" : Memo);
		return ret;
	}
}
