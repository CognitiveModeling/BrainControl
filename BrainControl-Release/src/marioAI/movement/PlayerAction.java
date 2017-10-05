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

import marioAI.aStar.actionEncoded.Action;
import marioWorld.engine.sprites.Player;
import marioWorld.mario.environments.Environment;

public class PlayerAction
{
	private boolean[] keyAction;
	private double cost;

	public PlayerAction() 
	{
		keyAction = new boolean[Environment.numberOfButtons];
		this.cost = 0;
	}
	
	@Override
	public String toString()
	{
		String ret="";
		for(int i=0;i<Environment.numberOfButtons;i++)
		{
			ret+=keyAction[i]+" ";
		}
		return ret;
	}	

	public PlayerAction(boolean left, boolean right, boolean down, boolean jump, boolean speed, boolean carry, double cost) 
	{
		this();
		keyAction[Player.KEY_DOWN] = down;
		keyAction[Player.KEY_JUMP] = jump;
		keyAction[Player.KEY_LEFT] = left;
		keyAction[Player.KEY_RIGHT] = right;
		keyAction[Player.KEY_SPEED] = speed;
		keyAction[Player.KEY_CARRYDROP] = carry;
		this.cost = cost;
	}
	
	public double getActionCost() 
	{
		return cost;
	}

	public void setKeyState(boolean[] set) 
	{
		keyAction = (boolean[]) set.clone();
	}	
	
	public boolean isEmpty()
	{
		for(int i=0;i<keyAction.length;i++)
			if(keyAction[i]==true)
				return false;
		
		return true;
	}
	
	public boolean[] getKeyState() 
	{
		return keyAction;
	}
}
