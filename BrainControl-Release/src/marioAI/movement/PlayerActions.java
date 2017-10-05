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

import marioAI.aStar.actionEncoded.Action;
import marioAI.run.PlayHook;
import marioWorld.engine.sprites.Player;
import marioWorld.mario.environments.Environment;

/**
 * Container class for actions of ALL players
 * */
public class PlayerActions implements Action
{
	private ArrayList<PlayerAction> keyAction;

	public PlayerActions() 
	{
		keyAction = new ArrayList<PlayerAction>();
		//void action
		for(int i=0;i<PlayHook.agents.size();i++)	//TODO: somewhat dirty..?
		{
			keyAction.add(new PlayerAction());
		}
	}

	public PlayerActions(int playerIndex, PlayerAction action) 
	{
		this();
		keyAction.set(playerIndex,action);
	}
	
	@Override
	public String toString()
	{
		String ret="";
		for(PlayerAction pa : keyAction)
		{
			ret+=pa.toString()+"\n";
		}
		return ret;
	}
	
	@Override
	public double getActionCost() 
	{
		double sum=0;
		for(PlayerAction pa : keyAction)
		{
			sum+=pa.getActionCost();
		}
		//System.out.println(sum);
		return sum;
	}
	
	public boolean isEmpty()
	{
		for(PlayerAction pa : keyAction)
		{
			if(!pa.isEmpty())
				return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void setKeyStates(ArrayList<PlayerAction> set) 
	{
		keyAction = (ArrayList<PlayerAction>) set.clone();
	}	
	
	public ArrayList<PlayerAction> getKeyStates() 
	{
		return keyAction;
	}
}
