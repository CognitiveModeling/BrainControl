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
import java.util.Deque;
import java.util.Observable;

/**
 * processes a list of goals
 * 
 * @author Jonas, modified by Jonas E.
 * 
 */
public class GoalListPlanner extends GoalPlanner
{
	private Deque<Goal> goalList;

	public GoalListPlanner(Deque<Goal> goalList) {
		this.goalList = goalList;
	}

	@Override
	public ArrayList<Goal> selectGoal() 
	{
		ArrayList<Goal> ret = new ArrayList<Goal>();
		ret.add(goalList.poll());
		return ret;
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected String getGoalSelectionFailureIdentifier() 
	{
		return "";
	}	
}
