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
package mario.main;

import javax.swing.JOptionPane;

import marioUI.gui.StartGuiModel;
import marioUI.gui.StartGuiView;
import marioWorld.engine.GlobalOptions;

/**
 * Main for brain control game. When start without commandline args a level selection screen is shown, if there is (exactly) one commandline arg
 * and it is a number, the corresponding level (>= 0) is started. If the number is negative, the maximum activated level (see activatedLevel.txt) is used.   
 */
public class PlayRelease {

	public static StartGuiView startGui;
	private static String levelName;

	public PlayRelease(int playLevel) {
		startGui = new StartGuiView(playLevel < 0);
		if (playLevel >= 0) {
			if (playLevel >= Settings.LEVEL_SETTINGS.length+1) {
				System.err.println("Illegal level number");
				System.exit(0);
			}
			levelName = Settings.LEVEL_SETTINGS[playLevel].name;
			startGui.startLevel(levelName, playLevel, Settings.getPlayerIDs(levelName));
		}
	}

	public static void main(String[] args) {
		//dynamic resize of GlobalOptions for non fitting Frames on small Screens which can't show the hole Game
		while (GlobalOptions.resolution_factor > 4 ||
				(StartGuiView.screenHeight < GlobalOptions.yScreen  && GlobalOptions.resolution_factor != 1)) {
			GlobalOptions.resolution_factor--;
			GlobalOptions.xScreen = 320 * GlobalOptions.resolution_factor;
			GlobalOptions.yScreen = 240 * GlobalOptions.resolution_factor;
		}
		
		int level = 0; 
		if (args.length == 0) {
			level = -1; // negative value: start menu
		} else if (args.length == 1) {
			if (args[0].equals("menu")) {
				level = -1; // negative value: start menu
			} else {
				level = parseLevel(args[0]);
			}
		} else {
			System.out.println("use level number as command-line argument, or use no arguments or 'menu' for starting with menu!");			
			System.exit(0);
		}
		try {
			new PlayRelease(level);	
		} catch (Throwable e) { 
			String msg = "Unexpected error occurred: "+e.getMessage();
			Throwable cause = e;
			do {
				for (StackTraceElement st : cause.getStackTrace()) {
					msg += "\n" + st.toString();
				}
				cause = cause.getCause();
				if (cause != null) {
					msg += "\nCaused by:";
				}
			} while(cause != null);
			JOptionPane.showMessageDialog(startGui, msg,"Error occurred",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static int parseLevel(String arg) {
		int level;
		try {
			level = Integer.parseInt(arg);
			System.out.println("Level number was set in command line to "+level);
			if (level < 0) {
				boolean[] active = StartGuiModel.parseLevelsTxt();
				level = 0;
				for (int i = 0; i < active.length; i++) {
					if (active[i])
						level = i;
				}
				System.out.println("Using highest activated level: "+level);
			}
		} catch(NumberFormatException e) {
			System.out.println("use level number as command-line argument (or no arguments for menu)");
			level = 0;
		}				
		return level;
	} 

	public static String getCurrentLevel() {
		return startGui.getCurrentLevel();
	}
	
}
