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

import marioWorld.engine.EngineHook;
import marioWorld.engine.GameWorld;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.level.SpriteTemplate;
import marioWorld.engine.sprites.Sprite;
import marioWorld.tools.EvaluationInfo;
import marioWorld.utils.ResetStaticInterface;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy Date: Apr 7, 2009 Time: 2:27:48 PM Package: .Simulation
 */

public class BasicSimulator implements Simulation , ResetStaticInterface{
	public static SimulationOptions simulationOptions = null;
	
	//complete list of visualizable gameworldcomponents...
	private GameWorld gameWorld = null;

	public BasicSimulator(SimulationOptions simulationOptions) 
	{
		classesWithStaticStuff.add(this.getClass());

		GlobalOptions.VisualizationOn = simulationOptions.isVisualization();
		this.gameWorld = GlobalOptions.getGameWorld();
		this.setSimulationOptions(simulationOptions);
	}

//	private GameWorld prepareGameWorld() 
//	{
//		ArrayList <Agent> agents = simulationOptions.getAgents();
//		
//		//gameWorld.setAgents(agents);
//				
//		for(Agent agent : agents)
//		{
//			agent.reset();			
//		}
//		
//		return gameWorld;
//	}

	public void setSimulationOptions(SimulationOptions simulationOptions) 
	{
		BasicSimulator.simulationOptions = simulationOptions;
	}

	private void spawnInitialSprites() 
	{
		LevelScene levelScene = GlobalOptions.realLevelScene;
		for (Sprite sprite : simulationOptions.getInitialSprites()) 
		{
			GlobalCoarse spriteMapPos = sprite.getPosition().toGlobalCoarse();
			SpriteTemplate spriteTemplate = SpriteTemplate.createSpriteTemplate(sprite);
			levelScene.level.setSpriteTemplate(spriteMapPos, spriteTemplate);
		}
	}

	public EvaluationInfo simulateOneLevel() 
	{		
		// gameWorldComponent.getMario().resetStatic(simulationOptions.getMarioMode());
		// Mario.resetStatic(simulationOptions.getMarioMode());
		
		gameWorld.setZLevelScene(simulationOptions.getZLevelMap());
		gameWorld.setZLevelEnemies(simulationOptions.getZLevelEnemies());
		gameWorld.startLevel(simulationOptions);
		
		for (EngineHook h : EngineHook.getHooks())
		{			
			h.OnGameWorldPrepared(gameWorld);
		}

		spawnInitialSprites();

		// Mario mario = gameWorldComponent.getLevelScene().mario;
		// GlobalContinuous initialMarioPosition = simulationOptions
		// .getInitialMarioPosition();
		// if (initialMarioPosition != null) {
		// mario.x = simulationOptions.getInitialMarioPosition().x;
		// mario.y = simulationOptions.getInitialMarioPosition().y;
		// }
		gameWorld.setPaused(simulationOptions.isPauseWorld());
		gameWorld.setZLevelEnemies(simulationOptions.getZLevelEnemies());
		gameWorld.setZLevelScene(simulationOptions.getZLevelMap());
		gameWorld.setPlayersInvulnerable(simulationOptions.isPlayerInvulnerable());
		
		//GAMEWORLDCOMPONENT IS THE MAIN RUN FUNCTION! All other components are just updated from this. 
		return gameWorld.run1(SimulationOptions.currentTrial++, simulationOptions.getNumberOfTrials());
	}

	public static void deleteStaticAttributes() {
		simulationOptions = null;
	}

	public static void resetStaticAttributes() {
		// TODO Auto-generated method stub
		
	}
}
