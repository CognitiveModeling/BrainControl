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

import java.util.LinkedList;
import java.util.List;

import marioUI.gui.StartGuiModel.PlayerTypes;
import marioWorld.engine.PlayerWorldObject;

public class Settings {

	public static final boolean USE_LOCAL_DIR_FOR_FILES = true; // if true, saved files are expected in the folder "./resources/", instead of $USER_DIR/.mario/resources/ 
	public static final boolean DO_LOGGING = false; // if false, no log files weill be created
	public static final float MIN_LEVEL_IN_DB = -40;
	public static final float START_MUSIC_VOLUME = 0.3f; // how load the music plays initially

	public static final boolean ASTAR_VISUALIZE_TOTAL_COST = false; // if false only the cost to node are visualized
	public static final boolean USE_ACTION_ENCODED_ASTAR = true;
	public static boolean STOP_ASTAR_SEARCH_WHEN_GOAL_WAS_FOUND = true;
	public static final boolean CLEAR_DISPLAYED_ASTAR_VISUALIZERS = true; // true: display new nodes since last redraw, false: display last 500 
	
	public static final LevelSettings[] LEVEL_SETTINGS = new LevelSettings[] { level0(), level1(), level2(), level3(), level4() };

	private static LevelSettings level0() {
		LevelSettings level = new LevelSettings("releaseLevel0");
		level.addPlayer(PlayerTypes.CLARK, 1, 13,250);
		return level;
	}

	private static LevelSettings level1() {
		LevelSettings level = new LevelSettings("releaseLevel1");
		level.addPlayer(PlayerTypes.BRUCE, 1, 0,250);
//		for(int i=0;i<2;i++)
//			level.addEnemy(PlayerWorldObject.GREEN_VIRUS, 162+i, 13);
		return level;
	}

	private static LevelSettings level2() {
			LevelSettings level = new LevelSettings("releaseLevel2");
			level.addPlayer(PlayerTypes.CLARK, 2, 0,150); 
			level.addPlayer(PlayerTypes.PETER, 35, 4,150); 
//			for(int i=96;i<100;i++)			
//				level.addEnemy(PlayerWorldObject.GREEN_VIRUS, i, 13);
//			level.addEnemy(PlayerWorldObject.GRUMPY, 187, 13);
//			level.addEnemy(PlayerWorldObject.GRUMPY, 188, 13);
//			level.addEnemy(PlayerWorldObject.GREEN_VIRUS, 191, 13);
//			level.addEnemy(PlayerWorldObject.RED_VIRUS, 194, 13);
//			level.addEnemy(PlayerWorldObject.RED_VIRUS, 195, 13);
//			level.addEnemy(PlayerWorldObject.GRUMPY, 200, 13);
//			level.addEnemy(PlayerWorldObject.GREEN_VIRUS, 202, 13);
//			level.addEnemy(PlayerWorldObject.GRUMPY, 205, 13);
			return level;
	}

	private static LevelSettings level3() {
		//LevelSettings level = new LevelSettings("releaseLevel3");
		LevelSettings level = new LevelSettings("releaseLevel3");

		level.addPlayer(PlayerTypes.CLARK, 52, 13,150);
		level.addPlayer(PlayerTypes.PETER, 8, 13,150);
		level.addPlayer(PlayerTypes.BRUCE, 10, 13,100);


		return level;
	}
	
	private static LevelSettings level4() {
		
		// Stop Screen from Splitting? -> View.java? 
		LevelSettings level = new LevelSettings("releaseLevel4");
		level.addPlayer(PlayerTypes.CLARK, 215, 8,100);
		level.addPlayer(PlayerTypes.BRUCE, 216, 8,100);
		level.addPlayer(PlayerTypes.PETER, 214, 8,100);
//		level.addPlayer(PlayerTypes.JAY, 217, 8);	
		return level;
	}
	
	public static class LevelSettings {
		public String name;
		public List<PlayerSettings> players;
		public List<EnemySettings> enemies;
		
		public LevelSettings(String name) {
			this.name = name;
			this.players = new LinkedList<>();
			this.enemies = new LinkedList<>();
		}
		
		public void addPlayer(PlayerTypes player, int x, int y, int startEnergy) {
			players.add(new PlayerSettings(player, x, y,startEnergy));
		}
		public void addPlayer(PlayerTypes player, int x, int y) {
			players.add(new PlayerSettings(player, x, y));
		}
		
		public void addEnemy(PlayerWorldObject enemy, int x, int y) {
			enemies.add(new EnemySettings(enemy, x, y));
		}

	}
	
	public static class PlayerSettings {
		public PlayerTypes id; 
		public int startPosX; // coarse
		public int startPosY; // coarse
		public int startEnergy = 100;
		
		public PlayerSettings(PlayerTypes player, int x, int y, int startEnergy) {
			this.id = player;
			this.startPosX = x;
			this.startPosY = y;
			this.startEnergy = startEnergy;
		}
		public PlayerSettings(PlayerTypes player, int x, int y) {
			this.id = player;
			this.startPosX = x;
			this.startPosY = y;
			
		}
	}

	public static class EnemySettings {
		public PlayerWorldObject id; 
		public int startPosX; // coarse
		public int startPosY; // coarse		
		
		public EnemySettings(PlayerWorldObject enemy, int x, int y) {
			this.id = enemy;
			this.startPosX = x;
			this.startPosY = y;
		}
	}
	
	public static int getLevelId(String levelName) {
		for (int i = 0; i < LEVEL_SETTINGS.length; i++) {
			if (levelName.equals(LEVEL_SETTINGS[i].name))
				return i;
		}
		throw new RuntimeException("Illegal level name: "+levelName);
	}

	public static LevelSettings getLevelSettings(String levelName) {
		return LEVEL_SETTINGS[getLevelId(levelName)];
	}

	public static int[] getPlayerIDs(String levelName) {
		LevelSettings ls = Settings.getLevelSettings(levelName);
		int playerIDs[] = new int[ls.players.size()];
		int i = 0;
		for (PlayerSettings ps : ls.players) {
			playerIDs[i] = ps.id.ordinal(); 
			i++;		
		}
		return playerIDs;
	}

	public static String getLastLevelName() {
		return LEVEL_SETTINGS[LEVEL_SETTINGS.length-1].name;
	}
	
	public static String getLevelBeforeEpilog() {
		return LEVEL_SETTINGS[LEVEL_SETTINGS.length-2].name;
	}

}

