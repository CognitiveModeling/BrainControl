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
package marioWorld.engine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

//import marioWorld.tools.GameViewer;

public class GlobalOptions 
{
	//this determines the screen resolution (images will be upscaled)
	public static int resolution_factor = 2;
	public static int xScreen = 320 * resolution_factor;
	public static int yScreen = 240 * resolution_factor;	//4:3 is needed!
	
	public static int ControlPanelHeight = 100;
	
	public static int menuPanelWidth = 350;
	public static int playerPanelWidth = 100;
	
	
	
	
	//the game internally runs in 320x240... to change that is complicated since it is hardcoded nearly everywhere
	//thus, we add a conversion factor for graphical display:
	
	public static boolean appendGoals = false;	
	
	public static boolean Labels = false;
	public static boolean PlayerAlwaysInCenter = true;		//TODO: this would be nice if graphics weren't buggy... at least the player should see exactly the same stuff as mario
	public static Integer FPS = 24;
	public static int InfiniteFPS = 100;
	// pauses levelscene
	public static boolean pauseWorld = false;
	// pauses whole game (agent, levelscene, evaluation...)
	public static boolean pauseGame = false;

	public static boolean VisualizationOn = true;
	public static boolean GameVeiwerOn = true;

	private static GameWorld gameWorld = null;
	public static LevelScene realLevelScene = null;	//this is the "real" LevelScene, not a cloned one!
	//private static GameViewer gameViewer = null;
	public static boolean TimerOn = true;

	// public static Defaults defaults = new Defaults();
	public static boolean GameViewerContinuousUpdatesOn = false;
	public static boolean PowerRestoration;

	public static boolean StopSimulationIfWin;
	public static boolean isPlayerInvulnerable;
	
	public static boolean playerChanged = false;
	public static int oldPlayerGameWorldToDisplay =0;
	public static int playerGameWorldToDisplay = 0;
	

	public static void registerGameWorld(GameWorld gwc) 
	{
		gameWorld = gwc;
	}

	public static GameWorld getGameWorld() 
	{
		return gameWorld;
	}

//	public static void registerGameViewer(GameViewer gv) {
//		gameViewer = gv;
//	}

	public static void adjustGameWorldFPS() 
	{
			gameWorld.adjustFPS();
	}

//	public static void gameViewerTick() {
//		if (gameViewer != null)
//			gameViewer.tick();
//		else
//			LOGGER.println("GameViewer is not available. Request for dump ignored.", LOGGER.VERBOSE_MODE.ERROR);
//	}

	public static String getDateTime(Long d) {
		DateFormat dateFormat = (d == null) ? new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:ms") : new SimpleDateFormat("HH:mm:ss:ms");
		if (d != null)
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = (d == null) ? new Date() : new Date(d);
		return dateFormat.format(date);
	}
	
	public static synchronized void togglePause() {
		pauseGame = !pauseGame;
	}
	
	public static void setResolution(int i){
		resolution_factor = i;
		xScreen = 320 * resolution_factor;
		yScreen = 240 * resolution_factor;
	}

	// public static class Defaults extends SimulationOptions
	// {
	// private static boolean gui;
	// private static boolean toolsConfigurator;
	// private static boolean gameViewer;
	// private static boolean gameViewerContinuousUpdates;
	// private static boolean timer;
	// private static int attemptsNumber;
	// private static boolean echo;
	// private static boolean maxFPS;
	// private static String agentName;
	// private static Integer serverAgentPort;
	// private static boolean exitProgramWhenFinished;
	//
	// public Defaults()
	// {
	// setLevelLength(320);
	// setLevelDifficulty(0);
	// setLevelRandSeed(1);
	// setVisualization(true);
	// setLevelType(LevelGenerator.TYPE_OVERGROUND);
	// setGui(false);
	// setAttemptsNumber(1);
	// setEcho(false);
	// setMaxFPS(false);
	// setPauseWorld(false);
	// setPowerRestoration(false);
	// setStopSimulationIfWin(false);
	// setAgentName("ForwardAgent");
	// setServerAgentPort(4242);
	// setExitProgramWhenFinished(false);
	// }
	//
	// public static boolean isGui() { return gui; }
	//
	// public static void setGui(boolean gui) { Defaults.gui = gui; }
	//
	// public static boolean isToolsConfigurator() {return toolsConfigurator; }
	//
	// public static void setToolsConfigurator(boolean toolsConfigurator) {
	// Defaults.toolsConfigurator = toolsConfigurator; }
	//
	// public static boolean isGameViewer() {
	// return gameViewer;
	// }
	//
	// public static void setGameViewer(boolean gameViewer) {
	// Defaults.gameViewer = gameViewer;
	// }
	//
	// public static boolean isGameViewerContinuousUpdates() {
	// return gameViewerContinuousUpdates;
	// }
	//
	// public static void setGameViewerContinuousUpdates(boolean
	// gameViewerContinuousUpdates) {
	// Defaults.gameViewerContinuousUpdates = gameViewerContinuousUpdates;
	// }
	//
	// public static boolean isTimer() {
	// return timer;
	// }
	//
	// public static void setTimer(boolean timer) {
	// Defaults.timer = timer;
	// }
	//
	// public static int getAttemptsNumber() {
	// return attemptsNumber;
	// }
	//
	// public static void setAttemptsNumber(int attemptsNumber) {
	// Defaults.attemptsNumber = attemptsNumber;
	// }
	//
	// public static boolean isEcho() {
	// return echo;
	// }
	//
	// public static void setEcho(boolean echo) {
	// Defaults.echo = echo;
	// }
	//
	// public static boolean isMaxFPS() {
	// return maxFPS;
	// }
	//
	// public static void setMaxFPS(boolean maxFPS) {
	// Defaults.maxFPS = maxFPS;
	// }
	//
	// public static String getAgentName() {
	// return agentName;
	// }
	//
	// public static void setAgentName(String agentName) {
	// Defaults.agentName = agentName;
	// }
	//
	// public static Integer getServerAgentPort()
	// {
	// return serverAgentPort;
	// }
	//
	// public static void setServerAgentPort(Integer serverAgentPort) {
	// Defaults.serverAgentPort = serverAgentPort;
	// }
	//
	// public static boolean isExitProgramWhenFinished() {
	// return exitProgramWhenFinished;
	// }
	//
	// public static void setExitProgramWhenFinished(boolean
	// exitProgramWhenFinished) {
	// Defaults.exitProgramWhenFinished = exitProgramWhenFinished;
	// }
	// }
}
