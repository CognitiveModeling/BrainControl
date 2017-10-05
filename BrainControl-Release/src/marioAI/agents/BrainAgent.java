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
package marioAI.agents;

import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import marioAI.brain.Brain;
import marioAI.brain.Effect;
import marioAI.collision.ConditionDirectionPair;
import marioWorld.agents.Agent;
import marioWorld.engine.sprites.Player;
import marioWorld.utils.ResetStaticInterface;

/**
 * Agent with a brain.
 */
public abstract class BrainAgent extends Observable implements Agent
{		
	protected Brain brain;
	
	private String name = "NO_NAME_AGENT";
	
	private Player player = null;
	
	protected int playerIndex;

	public BrainAgent(String name, int playerIndex, Map<ConditionDirectionPair, TreeMap<Effect, Double>> initialKnowledge) 
	{
		//classesWithStaticStuff.add(this.getClass());
		
		this.name=name;
		
		this.playerIndex=playerIndex;
		
		this.brain = new Brain(this, initialKnowledge);
	}	

	@Override
	public String getName()
	{
		return name;
	}
	
	public Player getPlayer() 
	{
		return player;
	}

	public void setPlayer(Player set) 
	{
		player = set;
	}	
	
	public int getPlayerIndex()
	{
		return playerIndex;
	}
	
//	public void setPlayerIndex(int setPlayerIndex)	
//	{
//		playerIndex=setPlayerIndex;
//	}	
	
	public Brain getBrain() {
		return brain;
	}

//	/**
//	 * Saves brain to the disk
//	 * 
//	 * @param path
//	 *            the path where to save.
//	 */
//	public void saveBrain(String path) {
//		this.getBrain().saveToDisk(path);
//
//	}

	/**
	 * Loads brain from disk.
	 * 
	 * @param path
	 *            the path where to load.
	 */
//	public void loadBrain(String path) {
//		this.getBrain().loadFromDisk(path);
//		this.setChanged();
//		this.notifyObservers("loadedBrain");
//	}
	
//	public static void deleteStaticAttributes(){
//		numPlayers = 0;
//	}
//	
//	public static void resetStaticAttributes(){
//		numPlayers = 0;
//	}
}
