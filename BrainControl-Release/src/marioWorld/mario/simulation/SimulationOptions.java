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
package marioWorld.mario.simulation;

import java.util.ArrayList;

import marioWorld.agents.Agent;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;
import marioWorld.utils.ParameterContainer;
import marioWorld.utils.ResetStaticInterface;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy Date: Apr 12, 2009 Time: 9:55:56 PM Package: .Simulation
 */

public class SimulationOptions extends ParameterContainer implements ResetStaticInterface
{
	protected ArrayList<Agent> agents;
	protected ArrayList<Player> players;
	
	// protected GameWorldComponent gameWorldComponent = null;

	public static int currentTrial = 1;

	private ArrayList<Sprite> initialSprites = new ArrayList<Sprite>();

	protected SimulationOptions() {
		super();
		classesWithStaticStuff.add(this.getClass());

		deleteStaticAttributes();
		// resetCurrentTrial();
	}

	public SimulationOptions getSimulationOptionsCopy() 
	{
		SimulationOptions ret = new SimulationOptions();
		
		ret.setAgents(getAgents());
		ret.setPlayers(getPlayers());
		
		for (Sprite sprite : getInitialSprites()) 
		{
			ret.addInitialSprite(sprite);
		}
		
		ret.setLevelDifficulty(getLevelDifficulty());
		ret.setLevelLength(getLevelLength());
		ret.setLevelRandSeed(getLevelRandSeed());
		ret.setLevelType(getLevelType());
		// ret.setGameWorldComponent(gameWorldComponent);
		ret.setVisualization(isVisualization());
		ret.setPauseWorld(isPauseWorld());
		ret.setPowerRestoration(isPowerRestoration());
		ret.setNumberOfTrials(getNumberOfTrials());
		ret.setPlayerMode(getPlayerMode());
		ret.setTimeLimit(getTimeLimit());
		ret.setZLevelEnemies(getZLevelEnemies());
		ret.setZLevelMap(getZLevelMap());
		ret.setPlayerInvulnerable(isPlayerInvulnerable());
		ret.setInitialPlayerPosition(getInitialPlayerPosition());
		ret.setOwnLevelName(getOwnLevelName());
		// ret.setCurrentTrial(getCurrentTrial());
		return ret;
	}

	// Agent
	public ArrayList<Agent> getAgents()
	{
		return agents;
	}
	
	// Agent
	public ArrayList<Player> getPlayers()
	{
		return players;
	}	

	public void setAgents(ArrayList <Agent> agents) 
	{
		// setParameterValue("-ag", s(agent));
		this.agents = agents;
	}

	public void setPlayers(ArrayList <Player> players) 
	{
		// setParameterValue("-ag", s(agent));
		this.players = players;
	}	
	
	//TODO: We should not need this anymore!!
	public void setAgents(Agent agent) 
	{
		this.agents = new ArrayList<Agent>();
		this.agents.add(agent);
	}

	// TODO? LEVEL_TYPE enum?
	// LevelType
	public int getLevelType() {
		return i(getParameterValue("-lt"));
	}

	public void setLevelType(int levelType) {
		setParameterValue("-lt", s(levelType));
	}

	// LevelDifficulty
	public int getLevelDifficulty() {
		return i(getParameterValue("-ld"));
	}

	public void setLevelDifficulty(int levelDifficulty) {
		setParameterValue("-ld", s(levelDifficulty));
	}

	// LevelLength
	public int getLevelLength() {
		return i(getParameterValue("-ll"));
	}

	public void setLevelLength(int levelLength) {
		setParameterValue("-ll", s(levelLength));
	}

	// LevelRandSeed
	public int getLevelRandSeed() {
		int seed = i(getParameterValue("-ls"));
		return seed;
	}

	public void setLevelRandSeed(int levelRandSeed) {
		setParameterValue("-ls", s(levelRandSeed));
	}

	// Visualization
	public boolean isVisualization() {
		return b(getParameterValue("-vis"));
	}

	public void setVisualization(boolean visualization) {
		setParameterValue("-vis", s(visualization));
	}

	// PauseWorld
	public void setPauseWorld(boolean pauseWorld) {
		setParameterValue("-pw", s(pauseWorld));
	}

	public Boolean isPauseWorld() {
		return b(getParameterValue("-pw"));
	}

	// PowerRestoration
	public Boolean isPowerRestoration() {
		return b(getParameterValue("-pr"));
	}

	public void setPowerRestoration(boolean powerRestoration) {
		setParameterValue("-pr", s(powerRestoration));
	}

	// StopSimulationIfWin
	public Boolean isStopSimulationIfWin() {
		return b(getParameterValue("-ssiw"));
	}

	public void setStopSimulationIfWin(boolean stopSimulationIfWin) {
		setParameterValue("-ssiw", s(stopSimulationIfWin));
	}

	// Number Of Trials
	public int getNumberOfTrials() {
		return i(getParameterValue("-not"));
	}

	public void setNumberOfTrials(int numberOfTrials) {
		setParameterValue("-not", s(numberOfTrials));
	}

	// MarioMode
	public int getPlayerMode() {
		return i(getParameterValue("-mm"));
	}

	private void setPlayerMode(int playerMode) {
		setParameterValue("-mm", s(playerMode));
	}

	// ZLevelMap
	public int getZLevelMap() {
		return i(getParameterValue("-zm"));
	}

	public void setZLevelMap(int zLevelMap) {
		setParameterValue("-zm", s(zLevelMap));
	}

	// ZLevelEnemies
	public int getZLevelEnemies() {
		return i(getParameterValue("-ze"));
	}

	public void setZLevelEnemies(int zLevelEnemies) {
		setParameterValue("-ze", s(zLevelEnemies));
	}

	// TimeLimit
	public int getTimeLimit() {
		return i(getParameterValue("-tl"));
	}

	public void setTimeLimit(int timeLimit) {
		setParameterValue("-tl", s(timeLimit));
	}

	// Invulnerability
	public boolean isPlayerInvulnerable() {
		return b(getParameterValue("-i"));
	}

	public void setPlayerInvulnerable(boolean invulnerable) {
		setParameterValue("-i", s(invulnerable));
	}

	// Trial tracking

	public void resetCurrentTrial() {
		currentTrial = 1;
	}

	// public void setCurrentTrial(int curTrial) {
	// setParameterValue("-not", s(curTrial));
	// }
	//
	// public int getCurrentTrial()
	// {
	// return i(getParameterValue("-not"));
	// }

	public void setInitialPlayerPosition(GlobalContinuous point) {
		if (point == null)
			return;
		setParameterValue("-initialPlayerPositionX", s(point.x));
		setParameterValue("-initialPlayerPositionY", s(point.y));
	}

	public GlobalContinuous getInitialPlayerPosition() {
		String x = getParameterValue("-initialPlayerPositionX");
		String y = getParameterValue("-initialPlayerPositionY");
		if (x == null || y == null)
			return null;
		return new GlobalContinuous(f(x), f(y));
	}

	public void setOwnLevelName(String ownLevelName) {
		setParameterValue("-ownLevelName", ownLevelName);
	}

	public String getOwnLevelName() {
		return getParameterValue("-ownLevelName");
	}

	public void addInitialSprite(Sprite sprite) {
		this.initialSprites.add(sprite);
	}

	public ArrayList<Sprite> getInitialSprites() {
		return initialSprites;
	}
	
	public static void deleteStaticAttributes(){
		currentTrial = 1;
	}
	
	public static void resetStaticAttributes(){
		currentTrial = 1;
	}
}
