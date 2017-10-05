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

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import marioUI.gui.PlayerPanel;
import marioUI.gui.View;
import marioWorld.engine.GameWorld;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.Tickable;

/**
 * This hook binds the LevelSceneAdapter to the brain and adds the brain as a tickable to the engine. It will also register the planner as a mouse listener.
 * 
 * @author sebastian
 * 
 */
public class CAEAgentHook extends BrainAgentHook<CAEAgent> {

	public static View view = new View();
	private final Tickable[] tickables;
	private final String[] initiallyActiveGuiOverlays;
	
	public CAEAgentHook(CAEAgent agent, String[] initiallyActiveGuiOverlays, Tickable... tickables) {
		super(agent);
		classesWithStaticStuff.add(this.getClass());
		
		this.tickables = tickables;
		this.initiallyActiveGuiOverlays = initiallyActiveGuiOverlays;
	}

	/**
	 * Sets up the LevelSceneAdapter and adds the brain as a tickable to the engine. It will also register the planner as a mouse listener.
	 */
	public void OnGameWorldPrepared(final GameWorld gameWorld) 
	{	
		super.OnGameWorldPrepared(gameWorld);
		
		for(Tickable tickable : this.tickables)
		{
			GlobalOptions.realLevelScene.addTickable(tickable);
		}
		
		//gameWorld.getPlayerContainer(agent).gameWorldComponent.addMouseListener(agent.getPlanner());
		
		// create View
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (agent instanceof CAEAgent) {
						if(PlayerPanel.USE_VIEW) 
						{
							GlobalOptions.realLevelScene.addTickable(view.addPlayer(agent.brain, (CAEAgent) agent, initiallyActiveGuiOverlays));
							view.rearrange();
						}
						// view.setAStarSimulator(agent.getAStarSimulator());
						// view.getFrame().setVisible(true);
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}			
	}

	public static void deleteStaticAttributes() {
		BrainAgentHook.deleteStaticAttributes();
//		view.dispose();
		view = null;
		
	}
	
	public static void resetStaticAttributes(){
		BrainAgentHook.resetStaticAttributes();
		view = new View();	}
}
