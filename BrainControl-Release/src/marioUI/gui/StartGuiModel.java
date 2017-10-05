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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

import mario.main.Settings;
import marioWorld.utils.ResourceStream;
import marioWorld.utils.UserDir;

public class StartGuiModel {
	public static enum PlayerTypes {CLARK, BRUCE, PETER, JAY};
	public static enum PlannerTypes {VOICE/*, MOUSE*/};
	public static enum KnowledgeTypes {NONE, LITTLE, MUCH};


	private StartGuiView view;
	//private StartGuiController controller;
	private ArrayList<AgentConfiguration> agentConfig;
	private ArrayList<Boolean> activeButtons;
	//private int playerCount;

	private String currentLevelName;
	private int currentLevelIndex;

	public final static String ACTIVE_BUTTONS_PATH = ResourceStream.DIR_WORLD_RESOURCES+"/engine/resources/activatedLevel.txt";

	public StartGuiModel(){
		agentConfig  = new ArrayList<AgentConfiguration>();

	}

	public void init(){
		generateActiveButtonsArray();
	}

	protected void generateActiveButtonsArray() {
		activeButtons = new ArrayList<Boolean>();
		generateActiveButtons(parseLevelsTxt());
	}

	public static boolean[] parseLevelsTxt() {
		boolean[] res;
		try {
			File file = UserDir.getUserFile(ACTIVE_BUTTONS_PATH);
			System.out.println("Reading: "+file.getAbsolutePath());
			InputStream inputStream = new FileInputStream(file);
			BufferedReader inStream = new BufferedReader(new InputStreamReader(inputStream));
			String line = inStream.readLine();
			res = new boolean[line.length()];
			for(int i = 0; i < res.length; i++){
				switch(line.charAt(i)){
				case '1':
					res[i] = true;
					break;
				case '0':
					res[i] = false;
					break;
				default:
					System.err.println("Not implemented Buttonstate " +line.charAt(i));
				}
				System.out.println("Level " + i + " activated: " + res[i]);
			}
			System.out.println();
			inStream.close();
			return res;
		} catch (IOException e) {
			//new MarioError("Could not find User-config-data. Program starts with default settings.");
		}
		res = new boolean[Settings.LEVEL_SETTINGS.length];
		res[0] = true;
		return res;
	}

	private void generateActiveButtons(boolean[] levelActive) {
		for (boolean active : levelActive) {
			activeButtons.add(active);			
		}
		view.init();
	}

	//	public void addController(StartGuiController _controller){
	//		controller = _controller;
	//	}

	public void addView(StartGuiView _view) {
		view = _view;
	}

	public ArrayList<Boolean> getActiveButtons() {
		return activeButtons;
	}

	public void setLevel(String lName, int levelIndex) {
		this.currentLevelName = lName;
		this.currentLevelIndex = levelIndex;
	}

	//	public int getPlayerCount() {
	//		return playerCount;
	//	}

	//	public void setPlayers(int[] players) 
	//	{
	//		if(playerCount >= 0)
	//		{
	//			this.playerCount = playerCount;
	//		}
	//	}

	public String getLevelName() {
		return currentLevelName;
	}

	public int getLevelIndex() {
		return currentLevelIndex;
	}

	public void clearAgentConfig(){
		this.agentConfig.clear();
	}

	public static void storeLevelSolved(String levelName) {
		boolean[] activated = parseLevelsTxt();
		for (int i = 0; i < activated.length-1; i++) {
			if (levelName.equals(Settings.LEVEL_SETTINGS[i].name))
				activated[i+1] = true;
		}
		PrintStream stream;
		try {
			stream = new PrintStream(new FileOutputStream(UserDir.getUserFile(ACTIVE_BUTTONS_PATH)));
			for (boolean a : activated) {
				stream.print(a ? "1" : "0");
			}
			stream.println();
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


}
