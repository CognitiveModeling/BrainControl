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


import marioAI.brain.Brain;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioWorld.engine.EngineHook;
import marioWorld.engine.GameWorld;
import marioWorld.engine.GlobalOptions;

/**
 * This hook binds the LevelSceneAdapter to the brain and adds the brain as a
 * tickable to the engine.
 * 
 * @author sebastian
 * 
 */
public class BrainAgentHook<T extends BrainAgent> extends EngineHook {
	T agent;

	public BrainAgentHook(T agent) {
		this.agent = agent;
		classesWithStaticStuff.add(this.getClass());
	}

	/**
	 * Sets up the LevelSceneAdapter and adds the brain as a tickable to the
	 * engine.
	 */
	public void OnGameWorldPrepared(final GameWorld component) 
	{
		final Brain brain = agent.getBrain();
		brain.initLevelSceneAdapter(new LevelSceneAdapter(GlobalOptions.realLevelScene, brain));
		if (agent instanceof CAEAgent) {
			((CAEAgent) agent).init();
		}
		GlobalOptions.realLevelScene.addTickable(brain);

//		Obsoltete: this is now done only when game was won
//		for(int i=0;i<ToolsConfigurator.getGameWorld().getPlayerContainers().size();i++)
//		{
//			CAEAgentHook.view.addWindowListener(new WindowAdapter() 
//			{
//				/*
//				 * abstract class which gets called when play-window is
//				 * going to be closed and game will be quit.
//				 */
//				@Override
//				public void windowClosing(
//						java.awt.event.WindowEvent windowEvent) 
//				{
//					/*
//					 * Knowledge is saved on file to be able to use it again
//					 * This is hard implemented for every player because this function gets used only once
//					 */
//					for (PlayerVisualizationContainer play : component.getPlayerContainers())
//					{
//						System.out.println("Saving Brain");
//						((CAEAgent)play.agent).getBrain().saveToDisk(new File(((CAEAgent)play.agent).getBrain().KNOWLEDGE_FILE_PATH).getAbsolutePath());	
//					}
//				}
//			});
//		}

	}
	
	public static void deleteStaticAttributes()
	{
		EngineHook.deleteStaticAttributes();
	}

	public static void resetStaticAttributes()
	{
		EngineHook.resetStaticAttributes();		
	}

}
