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
package marioWorld.agents;

import marioWorld.engine.sprites.Player;
import marioWorld.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy Date: Mar 28, 2009 Time: 8:46:42 PM package ch.idsia.ai.agents;
 * TODO: This shouldn't be an interface!
 */
public interface Agent 
{	
	public enum AGENT_TYPE 
	{
		AI, HUMAN, TCP_SERVER
	}

	public abstract int getPlayerIndex();

	//public abstract void setPlayerIndex(int setPlayerIndex);	
	
	public abstract String getName();
	
	public abstract Player getPlayer();
	
	public abstract void setPlayer(Player set);
	
	// clears all dynamic data, such as hidden layers in recurrent networks
	// just implement an empty method for a reactive controller
	public abstract void reset();

	public abstract boolean[] getAction(Environment observation);

	public abstract AGENT_TYPE getType();

//	public abstract void setName(String name);
}
