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
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch Date: May 12, 2009 Time: 7:28:57 PM Package: ch.idsia.ai.agents
 */
public class SimpleAgent implements Agent {
	protected boolean Action[] = new boolean[Environment.numberOfButtons];
	protected String Name = "SimpleAgent";

	private int playerIndex;
	
	public int getPlayerIndex()
	{
		return playerIndex;
	}
	
	public void setPlayerIndex(int setPlayerIndex)	
	{
		playerIndex=setPlayerIndex;
	}		
	
	public void reset() {
		Action = new boolean[Environment.numberOfButtons];
		Action[Player.KEY_RIGHT] = true;
		Action[Player.KEY_SPEED] = true;
	}

	public boolean[] getAction(Environment observation) {
		Action[Player.KEY_SPEED] = Action[Player.KEY_JUMP] = observation.mayPlayerJump(player) || !observation.isPlayerOnGround(player);
		return Action;
	}

	public AGENT_TYPE getType() {
		return AGENT_TYPE.AI;
	}

	public String getName() {
		return Name;
	}

	public void setName(String Name) {
		this.Name = Name;
	}

	private Player player = null;	
	@Override
	public Player getPlayer()
	{
		return player;
	}
	@Override
	public void setPlayer(Player set)
	{
		player = set;		
	}
}
