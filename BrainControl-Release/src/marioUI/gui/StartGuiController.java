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
package marioUI.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import mario.main.Settings;
import mario.main.Settings.EnemySettings;
import marioAI.run.PlayHook;
import marioAI.run.ReleaseGame;
import marioWorld.engine.GameWorld;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.utils.ResetStaticInterface;

public class StartGuiController {
	private StartGuiView view;
	private StartGuiModel model;
	//private ReleaseGame game;

	public StartGuiController(){
	}

	public void addModel(StartGuiModel _model) {
		model = _model;
	}

	public void addView(StartGuiView _view) {
		view = _view;
	}


	public void setLevelName(String lName, int levelId) {
		marioWorld.utils.Sounds.Type.SOUNDTRACK.setCurrentVersion(levelId);
		model.setLevel(lName, levelId);
	}

	private ArrayList<AgentConfiguration> collectAgentData() {
		return view.getAllAgentConfigurations();
	}

	@SuppressWarnings("unchecked")
	public void startNewLevel(String levelName, int levelId) {
		setLevelName(levelName, levelId);
		//game = null;
		ArrayList<AgentConfiguration> allAgentConfig = collectAgentData();


		for(Class<Object> c : ResetStaticInterface.classesWithStaticStuff){
			try {
				Method m = c.getMethod("resetStaticAttributes");
				try {
					m.invoke(null, (Object[])null);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}

		view.setVisible(false);
		new ReleaseGame(this, allAgentConfig, levelName);
	}

	/*
	 * playerCount is the absolute number of Players that will be simulated
	 */
	public void setPlayers(int[] players) 
	{
		//model.setPlayers(players);
		view.setNewAgentPanels(players);
	}

	public void gameFinished(){

//		CAEAgentHook.view.dispose();
		model.clearAgentConfig();
		model.generateActiveButtonsArray();
		view.refreshButtonActivation();
//		view.setVisible(true);
	}

	public void addSprites(GameWorld component) {
		List<EnemySettings> settings = Settings.getLevelSettings(this.model.getLevelName()).enemies;
		for (EnemySettings es : settings) {
			PlayHook.addRespawningSprite(component, es.id, new GlobalCoarse(es.startPosX, es.startPosY));
		}
	}

	public StartGuiModel getModel(){
		return this.model;
	}

}
