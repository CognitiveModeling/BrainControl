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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import marioAI.agents.CAEAgentHook;
import marioAI.run.PlayHook;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.level.BgLevelGenerator;
import marioWorld.engine.level.Level;
import marioWorld.engine.level.LevelGenerator;
import marioWorld.engine.level.SpriteTemplate;
import marioWorld.engine.sprites.BulletWilly;
import marioWorld.engine.sprites.EnergyAnim;
import marioWorld.engine.sprites.BulbFlower;
import marioWorld.engine.sprites.Fireball;
import marioWorld.engine.sprites.Wrench;
import marioWorld.engine.sprites.Particle;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Grumpy;
import marioWorld.engine.sprites.Sparkle;
import marioWorld.engine.sprites.Sprite;
import marioWorld.engine.sprites.SpriteContext;
import marioWorld.mario.environments.Environment;
import marioWorld.utils.MathX;
import marioWorld.utils.ResourceStream;
import marioWorld.utils.Sounds;
import marioWorld.utils.Sounds.Type;

public class LevelScene extends Scene implements SpriteContext // Observable
// via Scene
{
	public Level level;
		
	//this is a local reference or copy (in case of simulation) of all players
	private ArrayList<Player> players = new ArrayList<Player>();
	
	public ArrayList<Player> getPlayers()
	{
		return players;
	}

	public void setPlayers(ArrayList<Player> setplayers)
	{
		players=setplayers;
	}	
	
	private final String ownLevelName;
	private final ArrayList<Tickable> tickables = new ArrayList<Tickable>();

	/**
	 * Position of the camera, whereby xCam is the left limit of the visible
	 * screen and yCam is the upper limit of the visible screen. (Continuous
	 * coordinates)
	 */
	//public float xCams, yCam;
	public ArrayList<float[]> cameras = new ArrayList<float[]>();

	/**
	 * Old position of the camera (Continuous coordinates)
	 */
	//public float xCamO, yCamO;
	public ArrayList<float[]> old_cameras = new ArrayList<float[]>();

	public boolean paused = false;
	public int startTime = 0;
	public int timeLeft;
	public int totalTime = 200;

	public int fireballsOnScreen = 0;


	// public List<Sprite> clonedSprites; // not used anymore
	// public List<Sprite> clonedSpritesOld;
	// public List<Sprite> clonedSpritesOldOld;

	List<Grumpy> grumpysToCheck = new ArrayList<Grumpy>();
	List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

	//TEST
	private List<Sprite> sprites = new ArrayList<Sprite>();
	
	/*
	 * CHECK: Sprites should not be added multiple times due to multiple players.. thus, we use sets (unique values)
	 * */
	private HashSet<Sprite> spritesToAdd = new HashSet<Sprite>();
	private HashSet<Sprite> spritesToRemove = new HashSet<Sprite>();

	private int tick;

	private ArrayList<LevelRenderer> layers = new ArrayList<LevelRenderer>();
	private ArrayList<BgRenderer[]> bgLayers = new ArrayList<BgRenderer[]>(); 
	
	private GraphicsConfiguration graphicsConfiguration;

	// private Recorder recorder = new Recorder();
	// private Replayer replayer = null;

	private long levelSeed;
	private GameWorld renderer;
	private int levelType;
	private int levelDifficulty;
	private int levelLength;

//	private static final DecimalFormat df = new DecimalFormat("00");
//	private static final DecimalFormat df2 = new DecimalFormat("000");

	//public static Image tmpImage;

	public static int killedCreaturesTotal;
	public static int killedCreaturesByFireBall;
	public static int killedCreaturesByStomp;
	public static int killedCreaturesByGrumpy;
	
	public static int playersAtEnd;

	public static final int DISCRETE_CELL_SIZE = CoordinatesUtil.UNITS_PER_TILE;

	public static final int ENERGY_INCREASE = 15;

//	private static String[] LEVEL_TYPES = { "Overground(0)", "Underground(1)",
//			"Castle(2)" };

	/**
	 * Constructor
	 * 
	 * @param graphicsConfiguration
	 * @param renderer
	 * @param seed
	 * @param levelDifficulty
	 * @param type
	 * @param levelLength
	 * @param timeLimit
	 */
	public LevelScene(GraphicsConfiguration graphicsConfiguration,
			GameWorld renderer, long seed, int levelDifficulty,
			int type, int levelLength, int timeLimit, String ownLevelName)
	{
		this.graphicsConfiguration = graphicsConfiguration;
		this.renderer = renderer;
		this.levelSeed = seed;
		this.levelDifficulty = levelDifficulty;
		this.levelType = type;
		this.levelLength = levelLength;
		this.totalTime = timeLimit;
		// TODO should these be initialized in an object constructor?
		killedCreaturesTotal = 0;
		killedCreaturesByFireBall = 0;
		killedCreaturesByStomp = 0;
		killedCreaturesByGrumpy = 0;
		playersAtEnd = 0;
		this.ownLevelName = ownLevelName;		
	}

	// ================================================================================================
	// Getters and setters
	// ================================================================================================
	
	public void increasePlayersAtEnd(){
		playersAtEnd++;
	}
	
	public String getOwnLevelName(){
		return ownLevelName;
	}
	
	public List<Sprite> getSprites()
	{
		return sprites;
	}

	public HashSet<Sprite> getSpritesToAdd()
	{
		return spritesToAdd;
	}

	/**
	 *
	 * @return frames / 15
	 */
	public int getStartTime()
	{
		return startTime / 15;
	}

	/**
	 * 
	 * @return frames / 15
	 */
	public int getTimeLeft()
	{
		return timeLeft / 15;
	}

	// ================================================================================================
	// Observations
	// ================================================================================================

	public byte[][] levelSceneObservation(int ZLevel, Player player)
	{
		byte[][] ret = new byte[Environment.HalfObsHeight * 2 + 1][Environment.HalfObsWidth * 2 + 1];

		GlobalCoarse playerInMap = player.getPosition().toGlobalCoarse();

		for (int y = playerInMap.y - Environment.HalfObsHeight, obsX = 0; y <= playerInMap.y + Environment.HalfObsHeight; y++, obsX++)
		{
			for (int x = playerInMap.x - Environment.HalfObsWidth, obsY = 0; x <= playerInMap.x + Environment.HalfObsWidth; x++, obsY++)
			{
				if (x >= 0 && y >= 0 && y < level.height)
				{
					ret[obsY][obsX] = ZLevelMapElementGeneralization(level.map[x][y], ZLevel);
				} 
				else
					ret[obsY][obsX] = 0;
			}
		}
		return ret;
	}

	public byte[][] enemiesObservation(int ZLevel, Player player)
	{
		byte[][] ret = new byte[Environment.HalfObsHeight * 2 + 1][Environment.HalfObsWidth * 2 + 1];
		
		GlobalCoarse playerInMap = player.getPosition().toGlobalCoarse();

		for (int h = 0; h < ret.length; h++)
			for (int w = 0; w < ret[0].length; w++)
				ret[h][w] = 0;
		
		// ret[Environment.HalfObsWidth][Environment.HalfObsHeight] =
		// mario.kind;
		for (Sprite sprite : sprites) 
		{
			// if (sprite.kind == mario.kind)
			// continue;
			if (
					sprite.mapX >= 0
					&& sprite.mapX < level.width
					&& sprite.mapX >= playerInMap.x - Environment.HalfObsWidth
					&& sprite.mapX <= playerInMap.x + Environment.HalfObsWidth
					&& sprite.mapY >= 0
					&& sprite.mapY < level.height
					&& sprite.mapY > playerInMap.y - Environment.HalfObsHeight
					&& sprite.mapY < playerInMap.y + Environment.HalfObsHeight
				)
			{
				int obsX = sprite.mapY - playerInMap.y + Environment.HalfObsHeight;
				int obsY = sprite.mapX - playerInMap.x + Environment.HalfObsWidth;
				ret[obsY][obsX] = ZLevelEnemyGeneralization(sprite.kind, ZLevel);
			}
		}
		return ret;
	}

//	public byte[][] mergedObservation(int ZLevelScene, int ZLevelEnemies,
//			Player player)
//	{
//		return mergedObservation(ZLevelScene, ZLevelEnemies, level.height,
//				level.map, sprites, player);
//	}
//
//	public byte[][] mergedObservation(int ZLevelScene,
//			int ZLevelEnemies, int levelHeight, byte[][] levelMap,
//			List<Sprite> sprites, Player player) 
//	{
//		byte[][] ret = new byte[Environment.HalfObsHeight * 2 + 1][Environment.HalfObsWidth * 2 + 1];
//		
//		GlobalCoarse playerInMap = player.getPosition().toGlobalCoarse();
//
//		for (int y = playerInMap.y - Environment.HalfObsHeight, obsX = 0; y <= playerInMap.y + Environment.HalfObsHeight; y++, obsX++) 
//		{
//			for (int x = playerInMap.x - Environment.HalfObsWidth, obsY = 0; x <= playerInMap.x + Environment.HalfObsWidth; x++, obsY++) 
//			{
//				if (x >= 0 && y >= 0 && y < levelHeight) 
//				{
//					ret[obsY][obsX] = levelMap[y][x];
//					// ZLevelScene);
//				} 
//				else
//					ret[obsY][obsX] = 0;
//			}
//		}
//
//		for (Sprite sprite : sprites)
//		{
//			if (sprite.kind == player.kind)
//				continue;
//			if (
//					sprite.mapX >= 0
//					&& sprite.mapX < level.width
//					&& sprite.mapX >= playerInMap.x - Environment.HalfObsWidth
//					&& sprite.mapX <= playerInMap.x + Environment.HalfObsWidth
//					&& sprite.mapY >= 0
//					&& sprite.mapY < level.height
//					&& sprite.mapY >= playerInMap.y - Environment.HalfObsHeight
//					&& sprite.mapY <= playerInMap.y + Environment.HalfObsHeight
//				)
//			{
//				int obsX = sprite.mapY - playerInMap.y + Environment.HalfObsHeight;
//				int obsY = sprite.mapX - playerInMap.x + Environment.HalfObsWidth;
//				
//				// quick fix TODO: handle this in more general way.
//				// TODO: handle what!?
//				//if (ret[obsY][obsX] != 14)	 
//				{
//					byte tmp = sprite.kind;
//					// byte tmp = ZLevelEnemyGeneralization(sprite.kind,
//					// ZLevelEnemies);
//					if (tmp != Sprite.KIND_NONE)
//						ret[obsY][obsX] = tmp;
//				}
//			}
//		}
//
//		return ret;
//	}

//	public String bitmapLevelObservation(int ZLevel, Player player) 
//	{
//		String ret = "";
//		GlobalCoarse playerInMap = player.getPosition().toGlobalCoarse();
//
//		char block = 0;
//		byte bitCounter = 0;
//		// int totalBits = 0;
//		// int totalBytes = 0;
//		for (int y = playerInMap.y - Environment.HalfObsHeight; y <= playerInMap.y + Environment.HalfObsHeight; y++) 
//		{
//			for (int x = playerInMap.x - Environment.HalfObsWidth; x <= playerInMap.x + Environment.HalfObsWidth; x++) 
//			{
//				// ++totalBits;
//				if (bitCounter > 15) 
//				{
//					// update a symbol and store the current one
//					ret += block;
//					// ++totalBytes;
//					block = 0;
//					bitCounter = 0;
//				}
//				if (x >= 0 && x <= level.xExit && y >= 0 && y < level.height) 
//				{
//					int temp = ZLevelMapElementGeneralization(level.map[y][x],ZLevel);
//					
//					if (temp != 0)
//						block |= MathX.powsof2[bitCounter];
//				}
//				++bitCounter;
//			}
//			// if (block != 0)
//			// {
//			// System.out.println("block = " + block);
//			// show(block);
//			// }
//
//		}
//
//		if (bitCounter > 0)
//			ret += block;
//
//		// try {
//		// String s = new String(code, "UTF8");
//		// System.out.println("s = " + s);
//		// ret = s;
//		// } catch (UnsupportedEncodingException e) {
//		// e.printStackTrace(); //To change body of catch statement use File |
//		// Settings | File Templates.
//		// }
//		// System.out.println("totalBits = " + totalBits);
//		// System.out.println("totalBytes = " + totalBytes);
//		// System.out.println("ret = " + ret);
//
//		return ret;
//	}

//	public String bitmapEnemiesObservation(int ZLevel, Player player) 
//	{
//		String ret = "";
//		byte[][] enemiesObservation = enemiesObservation(ZLevel, player);
//		// int marioInMap.x = (int) mario.x / DISCRETE_CELL_SIZE;
//		// int marioInMap.y = (int) mario.y / DISCRETE_CELL_SIZE;
//
//		char block = 0;
//		char bitCounter = 0;
//		// int totalBits = 0;
//		// int totalBytes = 0;
//		for (int i = 0; i < enemiesObservation.length; ++i) 
//		{
//			for (int j = 0; j < enemiesObservation[0].length; ++j) 
//			{
//				// ++totalBits;
//				if (bitCounter > 7) {
//					// update a symbol and store the current one
//					ret += block;
//					// ++totalBytes;
//					block = 0;
//					bitCounter = 0;
//				}
//				int temp = enemiesObservation[i][j];
//				if (temp != -1)
//					block |= MathX.powsof2[bitCounter];
//				++bitCounter;
//			}
//			// if (block != 0)
//			// {
//			// System.out.println("block = " + block);
//			// show(block);
//			// }
//
//		}
//
//		if (bitCounter > 0)
//			ret += block;
//
//		// System.out.println("totalBits = " + totalBits);
//		// System.out.println("totalBytes = " + totalBytes);
//		// System.out.println("ret = " + ret);
//		return ret;
//	}

	// ================================================================================================
	// As declared by ch.idsia.mario.engine.Scene
	// ================================================================================================

	@Override
	public void init(List<Sprite> initialSprites) 
	{
		boolean useOwnLevel = ownLevelName != null && !ownLevelName.equals("");
		try {
			Level.loadBehaviors(new DataInputStream(ResourceStream.getResourceStream(ResourceStream.DIR_WORLD_RESOURCES+"/engine/mapedit/" + "tiles.dat")));

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		/*
		 * if (replayer!=null) { level = LevelGenerator.createLevel(2048, 15,
		 * replayer.nextLong()); } else {
		 */
		if (!useOwnLevel) {
			level = LevelGenerator.createLevel(levelLength, 15, levelSeed,
					levelDifficulty, levelType, initialSprites);
		} else { // load own level
			try {
				System.out.println("Lade Eigenes level.");
				String resource = ResourceStream.DIR_WORLD_RESOURCES+"/engine/mapedit/" + ownLevelName + ".lvl";
				level = Level.load(new DataInputStream(ResourceStream.getResourceStream(resource)));
				level.xExit = level.width - 20;
				int floor = level.height - 1;
				level.yExit = floor;

				// these two sprites belong to a specific setup of
				// testing_track.lvl
				// please do not change these
				// SpriteTemplate template = new SpriteTemplate(1, false);
				// level.setSpriteTemplate(120, 12, template);
				//
				// SpriteTemplate template2 = new SpriteTemplate(3, false);
				// level.setSpriteTemplate(122, 13, template2);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		// }

		/*
		 * if (recorder != null) { recorder.addLong(LevelGenerator.lastSeed); }
		 */

		paused = false;
		Sprite.spriteContext = this;
		sprites.clear();

		for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) 
		{
			layers.add(new LevelRenderer(level, graphicsConfiguration, GlobalOptions.xScreen, GlobalOptions.yScreen));

			//has been changed from 2 to 3 for all 3 layers (could be more)
			bgLayers.add(new BgRenderer[4]);

			//put number here, don't forgot to change it in render-method below!
			for (int i = 0; i < 4; i++) {
				int scrollSpeed = 8 >> i;
				int w = ((level.width * DISCRETE_CELL_SIZE) - GlobalOptions.xScreen)
						/ scrollSpeed + GlobalOptions.xScreen;
				int h = ((level.height * DISCRETE_CELL_SIZE) - GlobalOptions.yScreen)
						/ scrollSpeed + GlobalOptions.yScreen;
				Level bgLevel = BgLevelGenerator.createLevel(w / 520 + 1,
						h / 256 + 1, i, levelType);
				bgLayers.get(playerIndex)[i] = new BgRenderer(bgLevel,
						graphicsConfiguration, GlobalOptions.xScreen, GlobalOptions.yScreen, scrollSpeed);
			}
		}

		/*
		 * Create player specific stuff
		 */
		for (PlayerVisualizationContainer playerContainer : GlobalOptions.getGameWorld().getPlayerContainers()) 
		{
			sprites.add(playerContainer.player);
			cameras.add(new float[] { playerContainer.player.x, playerContainer.player.y });
			old_cameras.add(new float[] { playerContainer.player.x, playerContainer.player.y });
		}

		startTime = 1;

		// timeLeft = totalTime*15;
		timeLeft = Integer.MAX_VALUE;
		tick = 0;
	}

	public void tick() 
	{
		/*
		 * for(int i=0;i<players.size();i++) {
		 * System.out.println("Player: "+i+", Camera: "
		 * +cameras.get(i)[0]+" "+cameras
		 * .get(i)[1]+", Position: "+players.get(i).x+" "+players.get(i).y); }
		 */

		if (GlobalOptions.TimerOn) {
			timeLeft--;
			// System.out.println("\n\n---remaining time: "+timeLeft+"---\n\n");
		}
		if (timeLeft == 0) {
			for (Player player : players)
				player.die();
		}

		// xCamO = xCam;
		// yCamO = yCam;
		old_cameras.clear();
		for (float[] cam : cameras) {
			old_cameras.add(cam.clone());		//TODO: unused?
		}

		if (startTime > 0) {
			startTime++;
		}
		if (startTime == PlayHook.DEBUG_BREAK_AT_TIME_STEP) {
			System.out.println("debug: time = " + startTime);
		}

		for (int i = 0; i < players.size(); i++) {
			
			int xOld = (int) players.get(GlobalOptions.oldPlayerGameWorldToDisplay).x;
			float targetXCam = players.get(GlobalOptions.playerGameWorldToDisplay).x  - 160;

			if (!GlobalOptions.PlayerAlwaysInCenter)
			{
				
				if (targetXCam < 0)
					targetXCam = 0;
				if (targetXCam > level.width * DISCRETE_CELL_SIZE - GlobalOptions.xScreen)
					targetXCam = level.width * DISCRETE_CELL_SIZE - GlobalOptions.xScreen;

			}
			
			if(GlobalOptions.playerChanged) {
				
				targetXCam = players.get(i).x - xOld  ;
				GlobalOptions.playerChanged = false;
			}
			
			
			
			cameras.get(i)[0] = old_cameras.get(i)[0] * 0.9f + targetXCam * 0.1f;
		}

		/*
		 * if (recorder != null) { recorder.addTick(mario.getKeyMask()); }
		 * 
		 * if (replayer!=null) { mario.setKeys(replayer.nextTick()); }
		 */

		fireballsOnScreen = 0;
		for (Sprite sprite : sprites) 
		{
			boolean isSpritePlayer = false;
			for (Player player : players)
				if (sprite == player)
					isSpritePlayer = true;

			// i think this removes all sprites that are not visible to any
			// camera
			//CHECK: disabled... sprites are permanent...
//			if (!isSpritePlayer) 
//			{
//				boolean removeThisSprite = true;
//
//				// check if this sprite is visible to any player
//				for (int i = 0; i < players.size(); i++) 
//				{
//					float xd = sprite.x - cameras.get(i)[0];
//					float yd = sprite.y - cameras.get(i)[1];
//
//					// visible sprite:
//					if (!(xd < -64 || xd > GlobalOptions.xScreen + 64 || yd < -64 || yd > GlobalOptions.yScreen + 64)) // ORIGINAL
//					{
//						removeThisSprite = false;
//					}
//				}
//
//				if (removeThisSprite)
//					removeSprite(sprite);
//				else if (sprite instanceof Fireball) 
//				{
//					fireballsOnScreen++;
//				}
//			}
		}

		if (paused) 
		{
			for (Sprite sprite : sprites) 
			{
				boolean isSpritePlayer = false;
				for (Player player : players)
					if (sprite == player)
						isSpritePlayer = true;

				if (isSpritePlayer) 
				{
					sprite.tick();
				} 
				else 
				{
					sprite.tickNoMove();
				}
			}
		} 
		else 
		{
			tick++;
			level.tick();

			// boolean hasShotCannon = false;
			// int xCannon = 0;

			for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) 
			{
				// this spawns stuff when needed
				for (int x = (int) cameras.get(playerIndex)[0] / DISCRETE_CELL_SIZE - 1; x <= (int) (cameras.get(playerIndex)[0] + layers.get(playerIndex).width) / DISCRETE_CELL_SIZE + 1; x++) 
				{
					for (int y = (int) cameras.get(playerIndex)[1] / DISCRETE_CELL_SIZE - 1; y <= (int) (cameras.get(playerIndex)[1] + layers.get(playerIndex).height) / DISCRETE_CELL_SIZE + 1; y++)
					{
						int dir = 0;

						if (x * DISCRETE_CELL_SIZE + 8 > players
								.get(playerIndex).x + DISCRETE_CELL_SIZE)
							dir = -1;
						if (x * DISCRETE_CELL_SIZE + 8 < players
								.get(playerIndex).x - DISCRETE_CELL_SIZE)
							dir = 1;

						SpriteTemplate st = level
								.getSpriteTemplate(new GlobalCoarse(x, y));

						if (st != null) 
						{
							if (st.lastVisibleTick != tick - 1) 
							{
								if (st.sprite == null || !sprites.contains(st.sprite)) 
								{
									st.spawn(this, new GlobalCoarse(x, y), dir);
								}
							}

							st.lastVisibleTick = tick;
						}

						if (dir != 0) 
						{
							byte b = level.getBlock(x, y);
							
							if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0) 
							{
								if ((b % DISCRETE_CELL_SIZE) / 4 == 3 && b / DISCRETE_CELL_SIZE == 0) 
								{
									if ((tick - x * 2) % 100 == 0) 
									{
										// xCannon = x;
										for (int i = 0; i < 8; i++) 
										{
											addSprite(new Sparkle(
													x * DISCRETE_CELL_SIZE + 8,
													y
															* DISCRETE_CELL_SIZE
															+ (int) (Math
																	.random() * DISCRETE_CELL_SIZE),
													(float) Math.random() * dir,
													0, 0, 1, 5));
										}
										addSprite(new BulletWilly(this, x
												* DISCRETE_CELL_SIZE + 8 + dir
												* 8, y * DISCRETE_CELL_SIZE
												+ 15, dir));
										// hasShotCannon = true;
									}
								}
							}
						}
					}
				}
			}

			
			if (startTime == PlayHook.DEBUG_BREAK_AT_TIME_STEP) 
			{
				System.out.println("debug: time = " + startTime);
			}
			
			for (Sprite sprite : sprites) 
			{
				sprite.tick();
			}
			
			for (Sprite sprite1 : sprites) 
			{
				for (Sprite sprite2 : sprites) 
				{
					if(sprite1!=sprite2)
						sprite1.collideCheck(sprite2);	//this sets the carry flags
				}
			}

			//TODO: what is this?!?!?
			for(Player player : players)
			{
				for (Grumpy grumpy : grumpysToCheck)
				{
					for (Sprite sprite : sprites)
					{
						if (sprite != grumpy && !grumpy.dead)
						{
							if (sprite.grumpyCollideCheck(grumpy))
							{
								if (player.getCarries() == grumpy && !grumpy.dead)
								{
									player.setCarries(null);
									grumpy.die();
									++LevelScene.killedCreaturesTotal;
								}
							}
						}
					}
				}
			}

			grumpysToCheck.clear();

			for (Fireball fireball : fireballsToCheck) 
			{
				for (Sprite sprite : sprites) 
				{
					if (sprite != fireball && !fireball.dead) 
					{
						if (sprite.fireballCollideCheck(fireball)) 
						{
							fireball.die();
						}
					}
				}
			}
			fireballsToCheck.clear();
		}

		sprites.addAll(0, spritesToAdd);
		sprites.removeAll(spritesToRemove);
		spritesToAdd.clear();
		spritesToRemove.clear();

		for (Tickable tickable : this.tickables)
		{
			//this invokes the brain onTick etc.
			tickable.onTick(this);
		}
	}
	
	
	public void render(Graphics2D g, float alpha, int playerIndex) 
	{
		// int xCam = (int) (players.get(playerIndex).xOld +
		// (players.get(playerIndex).x - players.get(playerIndex).xOld) * alpha)
		// - 160;
		// int yCam = (int) (players.get(playerIndex).yOld +
		// (players.get(playerIndex).y - players.get(playerIndex).yOld) * alpha)
		// - 120;


		int xCam = (int) cameras.get(playerIndex)[0];
		int yCam = (int) cameras.get(playerIndex)[1];
		

		
		
		

		if (!GlobalOptions.PlayerAlwaysInCenter) {
			// int xCam = (int) (xCamO + (this.xCam - xCamO) * alpha);
			// int yCam = (int) (yCamO + (this.yCam - yCamO) * alpha);

			if (xCam < 0)
				xCam = 0;
			if (yCam < 0)
				yCam = 0;
			if (xCam > level.width * DISCRETE_CELL_SIZE - GlobalOptions.xScreen)
				xCam = level.width * DISCRETE_CELL_SIZE - GlobalOptions.xScreen;
			if (yCam > level.height * DISCRETE_CELL_SIZE - GlobalOptions.yScreen)
				yCam = level.height * DISCRETE_CELL_SIZE - GlobalOptions.yScreen;
		}
		// g.drawImage(Art.background, 0, 0, null);

		for (int i = 0; i < 4; i++) {
			bgLayers.get(playerIndex)[i].setCam(xCam, yCam);
			bgLayers.get(playerIndex)[i].render(g, tick, alpha);
		}

		g.translate(-xCam*GlobalOptions.resolution_factor, -yCam*GlobalOptions.resolution_factor);

		for (Sprite sprite : sprites) {
			if (sprite.layer == 0)
				sprite.render(g, alpha);
		}

		g.translate(xCam*GlobalOptions.resolution_factor, yCam*GlobalOptions.resolution_factor);

		layers.get(playerIndex).setCam(xCam, yCam);
		
		layers.get(playerIndex).render(g, tick, paused ? 0 : alpha);
		
		for (Player player : players)
			layers.get(playerIndex).renderExit0(g, tick, paused ? 0 : alpha, player.winTime == 0);

		g.translate(-xCam*GlobalOptions.resolution_factor, -yCam*GlobalOptions.resolution_factor);

		// TODO: Dump out of render!
		/*for(Player player : players)
		{
			if (player.cheatKeys[Player.KEY_DUMP_CURRENT_WORLD])
				for (int w = 0; w < level.width; w++)
					for (int h = 0; h < level.height; h++)
						level.observation[w][h] = -1;

			for (Sprite sprite : sprites)
			{
				if (sprite.layer == 1)
					sprite.render(g, alpha);
				if (player.cheatKeys[Player.KEY_DUMP_CURRENT_WORLD]
						&& sprite.mapX >= 0
						&& sprite.mapX < level.observation.length
						&& sprite.mapY >= 0
						&& sprite.mapY < level.observation[0].length)
					level.observation[sprite.mapX][sprite.mapY] = sprite.kind;
			}
		}*/
		for(Sprite sprite : sprites)
		{
			if (sprite.layer == 1)
				sprite.render(g, alpha);
		}

		g.translate(xCam*GlobalOptions.resolution_factor, yCam*GlobalOptions.resolution_factor);
		
		g.setColor(Color.BLACK);
		layers.get(playerIndex).renderExit1(g, tick, paused ? 0 : alpha);

		/**
		 * Commented out this HUD text, its information was of no use and
		 * obstructing the view
		 */

		// drawStringDropShadow(g, "MARIO: " + df.format(Mario.lives), 0, 0, 7);
		// drawStringDropShadow(g, "#########", 0, 1, 7);

		// drawStringDropShadow(g,
		// "DIFFICULTY:   " + df.format(this.levelDifficulty), 0, 0,
		// this.levelDifficulty > 6 ? 1 : this.levelDifficulty > 2 ? 4 : 7);
		// drawStringDropShadow(g, "CREATURES:"
		// + (mario.world.paused ? "OFF" : "ON"), 19, 0, 7);
		// drawStringDropShadow(g, "SEED:" + this.levelSeed, 0, 1, 7);
		// drawStringDropShadow(g, "TYPE:" + LEVEL_TYPES[this.levelType], 0, 2,
		// 7);
		// drawStringDropShadow(g, "ALL KILLS: " + killedCreaturesTotal, 19, 1,
		// 1);
		// drawStringDropShadow(g, "LENGTH:" + (int) mario.x /
		// DISCRETE_CELL_SIZE
		// + " of " + this.levelLength, 0, 3, 7);
		// drawStringDropShadow(g, "by Fire  : " + killedCreaturesByFireBall,
		// 19,
		// 2, 1);
		// drawStringDropShadow(g, "ENERGYS    : " + df.format(mario.getEnergys()),
		// 0,
		// 4, 4);
		// drawStringDropShadow(g, "by Grumpy : " + killedCreaturesByGrumpy, 19,
		// 3,
		// 1);
		// drawStringDropShadow(g,
		// "WRENCHS: " + df.format(Mario.gainedWrenchs), 0, 5, 4);
		// drawStringDropShadow(g, "by Stomp : " + killedCreaturesByStomp, 19,
		// 4,
		// 1);
		// drawStringDropShadow(g, "FLOWERS  : " +
		// df.format(Mario.gainedFlowers),
		// 0, 6, 4);

		// drawStringDropShadow(g, "TIME", 33, 0, 7);
		// int time = (timeLeft + 15 - 1) / 15;
		// if (time < 0)
		// time = 0;
		// drawStringDropShadow(g, " " + df2.format(time), 33, 1, 7);

		//drawProgress(g, playerIndex);

		if (GlobalOptions.Labels) 
		{
			g.drawString("xCam: " + xCam + "yCam: " + yCam, 70, 40);
			g.drawString(
					"x : " + players.get(playerIndex).x + "y: "
							+ players.get(playerIndex).y, 70, 50);
			g.drawString("xOld : " + players.get(playerIndex).xOld + "yOld: "
					+ players.get(playerIndex).yOld, 70, 60);
		}

		if (startTime > 0) 
		{
			float t = startTime + alpha - 2;
			t = t * t * 0.6f;
			renderBlackout(g, GlobalOptions.xScreen/2, GlobalOptions.yScreen/2, (int) (t));
		}

		// for(Player player : players)
		{
			// mario.x>level.xExit*DISCRETE_CELL_SIZE
			if (players.get(playerIndex).winTime > 0) 
			{
				float t = players.get(playerIndex).winTime + alpha;
				t = t * t * 0.2f;

				if (t > 900) 
				{
					renderer.levelWon();
					// replayer = new Replayer(recorder.getBytes());
					// init();
				}

				renderBlackout(g, players.get(playerIndex).xDeathPos - xCam,
						players.get(playerIndex).yDeathPos - yCam,
						(int) (GlobalOptions.xScreen - t));
			}

//			if (players.get(playerIndex).deathTime > 0) 
//			{
//				renderer.levelFailed();
//			}
		}
	}

	// ================================================================================================
	// As declared by ch.idsia.mario.engine.sprites.SpriteContext
	// ================================================================================================

	public void addSprite(Sprite sprite) {
		spritesToAdd.add(sprite);
		sprite.tick();
	}

	public void removeSprite(Sprite sprite) {
		spritesToRemove.add(sprite);
	}

	// ================================================================================================
	// Drawing functions
	// ================================================================================================

	/*
	 * public void drawTooltip(Point location, String text) {
	 * bgLayer[1].setToolTip(location, text); }
	 * 
	 * public void hideTooltip() { bgLayer[1].setToolTip(null, null); }
	 */

	private void drawProgress(Graphics g, int playerIndex) {
		String entirePathStr = "......................................>";
		double physLength = (levelLength - 53) * DISCRETE_CELL_SIZE;
		int progressInChars = (int) (players.get(playerIndex).x * (entirePathStr
				.length() / physLength));
		String progress_str = "";
		for (int i = 0; i < progressInChars - 1; ++i)
			progress_str += ".";
		progress_str += "M";
		try {
			drawStringDropShadow(g,
					entirePathStr.substring(progress_str.length()),
					progress_str.length(), 28, 0);
		} catch (StringIndexOutOfBoundsException e) {
			// System.err.println("warning: progress line inaccuracy");
		}
		drawStringDropShadow(g, progress_str, 0, 28, 2);
	}

	public static void drawStringDropShadow(Graphics g, String text, int x,
			int y, int c) {
		drawString(g, text, x * 8 + 5, y * 8 + 5, 0);
		drawString(g, text, x * 8 + 4, y * 8 + 4, c);
	}

	private static void drawString(Graphics g, String text, int x, int y, int c) {
		char[] ch = text.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			g.drawImage(Art.font.getImage(ch[i] - 32,c), x + i * 8, y, null);
		}
	}

	private void renderBlackout(Graphics g, int x, int y, int radius) {
		if (radius > GlobalOptions.xScreen)
			return;

		int[] xp = new int[20];
		int[] yp = new int[20];
		for (int i = 0; i < 16; i++) {
			xp[i] = x + (int) (Math.cos(i * Math.PI / 15) * radius);
			yp[i] = y + (int) (Math.sin(i * Math.PI / 15) * radius);
		}
		xp[16] = GlobalOptions.xScreen;
		yp[16] = y;
		xp[17] = GlobalOptions.xScreen;
		yp[17] = GlobalOptions.yScreen;
		xp[18] = 0;
		yp[18] = GlobalOptions.yScreen;
		xp[19] = 0;
		yp[19] = y;
		g.fillPolygon(xp, yp, xp.length);

		for (int i = 0; i < 16; i++) {
			xp[i] = x - (int) (Math.cos(i * Math.PI / 15) * radius);
			yp[i] = y - (int) (Math.sin(i * Math.PI / 15) * radius);
		}
		xp[16] = GlobalOptions.xScreen;
		yp[16] = y;
		xp[17] = GlobalOptions.xScreen;
		yp[17] = 0;
		xp[18] = 0;
		yp[18] = 0;
		xp[19] = 0;
		yp[19] = y;

		g.fillPolygon(xp, yp, xp.length);
	}

	// ================================================================================================
	// Other not-yet-sorted functions
	// ================================================================================================

	public float[] enemiesFloatPos() {
		List<Float> poses = new ArrayList<Float>();
		for (Sprite sprite : sprites) {
			// check if is an influenceable creature
			if (sprite.kind >= Sprite.KIND_LUKO
					&& sprite.kind <= Sprite.KIND_WRENCH) {
				poses.add((float) sprite.kind);
				poses.add(sprite.x);
				poses.add(sprite.y);
			}
		}

		float[] ret = new float[poses.size()];

		int i = 0;
		for (Float F : poses)
			ret[i++] = F;

		return ret;
	}

	private String mapElToStr(int el) {
		String s = "";
		if (el == 0 || el == 1)
			s = "##";
		s += (el == players.get(0).kind) ? "#M.#" : el; // CHECK: what is this?
		while (s.length() < 4)
			s += "#";
		return s + " ";
	}

	private String enemyToStr(int el) {
		String s = "";
		if (el == 0)
			s = "";
		s += (el == players.get(0).kind) ? "-m" : el; // CHECK: what is this?
		while (s.length() < 2)
			s += "#";
		return s + " ";
	}

	private byte ZLevelMapElementGeneralization(byte el, int ZLevel) {
		if (el == 0)
			return 0;
		switch (ZLevel) {
		case (0):
			switch (el) {
			case 16:
				return 16; // brick, simple, without any surprise.
			case 17:
				return 17; // brick with a hidden energy
			case 18:
				return 18; // brick with a hidden flower
				// return 16; // prevents cheating
			case 21: // question brick, contains energy
			case 22: // question brick, contains flower/wrench
				return 21; // question brick, contains something
			}
			return el;
		case (1):
			switch (el) {
			case 40:
			case 56:
			case 5:
			case 24:
			case 16:
				return 16; // brick, simple, without any surprise.
			case 17:
				return 17; // brick with a hidden energy
			case 18:
				return 18; // brick with a hidden flower
				// return 16; // prevents cheating
			case 21:
				return 21; // question brick, contains energy
			case 22:
				return 22; // question brick, contains flower/wrench
				// return 21; // question brick, contains something
			case (-108):
			case (-107):
			case (-106):
			case (15): // Sparcle, irrelevant
				// case(34): // Energy, irrelevant for the current contest
				// return 0;
			case (-128):
			case (-127):
			case (-126):
			case (-125):
			case (-120):
			case (-119):
			case (-118):
			case (-117):
			case (-116):
			case (-115):
			case (-114):
			case (-113):
			case (-112):
			case (-111):
			case (-110):
			case (-109):
			case (-104):
			case (-103):
			case (-102):
			case (-101):
			case (-100):
			case (-99):
			case (-98):
			case (-97):
			case (-69):
			case (-65):
			case (-88):
			case (-87):
			case (-86):
			case (-85):
			case (-84):
			case (-83):
			case (-82):
			case (-81):
			case (4): // kicked hidden brick
			case (9):
				return -10; // border, cannot pass through, can stand on
				// case(9):
				// return -12; // hard formation border. Pay attention!
			case (-124):
			case (-123):
			case (-122):
			case (-76):
			case (-74):
				return -11; // half-border, can jump through from bottom and can
				// stand on
			case (10):
			case (11):
			case (26):
			case (27): // flower pot
			case (14):
			case (30):
			case (46): // canon
				return 20; // angry flower pot or cannon
			}
			System.err.println("Unknown value el = " + el
					+ " ; Please, inform the developers");
			return el;
		case (2):
			switch (el) {
			// cancel out half-borders, that could be passed through
			case (0):
			case (-108):
			case (-107):
			case (-106):
			case (34): // energys
			case (15): // Sparcle, irrelevant
				return 0;
			}
			return 1; // everything else is "something", so it is 1
		}
		System.err.println("Unkown ZLevel Z" + ZLevel);
		return el; // TODO: Throw unknown ZLevel exception
	}

	private byte ZLevelEnemyGeneralization(byte el, int ZLevel) {
		switch (ZLevel) {
		case (0):
			switch (el) {
			// cancell irrelevant sprite codes
			case (Sprite.KIND_ENERGY_ANIM):
			case (Sprite.KIND_PARTICLE):
			case (Sprite.KIND_SPARCLE):
			case (Sprite.KIND_PLAYER):
				return Sprite.KIND_NONE;
			}
			return el; // all the rest should go as is
		case (1):
			switch (el) {
			case (Sprite.KIND_ENERGY_ANIM):
			case (Sprite.KIND_PARTICLE):
			case (Sprite.KIND_SPARCLE):
			case (Sprite.KIND_PLAYER):
				return Sprite.KIND_NONE;
			case (Sprite.KIND_FIREBALL):
				return Sprite.KIND_FIREBALL;
			case (Sprite.KIND_BULLET_WILLY):
			case (Sprite.KIND_LUKO):
			case (Sprite.KIND_LUKO_WINGED):
				/*
			case (Sprite.KIND_GREEN_KOOPA):
			case (Sprite.KIND_GREEN_KOOPA_WINGED):
			case (Sprite.KIND_RED_KOOPA):
			case (Sprite.KIND_RED_KOOPA_WINGED):
			*/
			case (Sprite.KIND_GREEN_VIRUS):
			case (Sprite.KIND_GREEN_VIRUS_WINGED):
			case (Sprite.KIND_RED_VIRUS):
			case (Sprite.KIND_RED_VIRUS_WINGED):
				
			case (Sprite.KIND_GRUMPY):
				return Sprite.KIND_LUKO;
			case (Sprite.KIND_SHALLY):
			case (Sprite.KIND_ENEMY_FLOWER):
			case (Sprite.KIND_SHALLY_WINGED):
				return Sprite.KIND_SHALLY;
			}
			System.err.println("UNKOWN el = " + el);
			return el;
		case (2):
			switch (el) {
			case (Sprite.KIND_ENERGY_ANIM):
			case (Sprite.KIND_PARTICLE):
			case (Sprite.KIND_SPARCLE):
			case (Sprite.KIND_FIREBALL):
			case (Sprite.KIND_PLAYER):
				return Sprite.KIND_NONE;
			case (Sprite.KIND_BULLET_WILLY):
			case (Sprite.KIND_LUKO):
			case (Sprite.KIND_LUKO_WINGED):
				/*
				case (Sprite.KIND_GREEN_KOOPA):
				case (Sprite.KIND_GREEN_KOOPA_WINGED):
				case (Sprite.KIND_RED_KOOPA):
				case (Sprite.KIND_RED_KOOPA_WINGED):
				*/
				case (Sprite.KIND_GREEN_VIRUS):
				case (Sprite.KIND_GREEN_VIRUS_WINGED):
				case (Sprite.KIND_RED_VIRUS):
				case (Sprite.KIND_RED_VIRUS_WINGED):
				
		
			case (Sprite.KIND_GRUMPY):
			case (Sprite.KIND_SHALLY):
			case (Sprite.KIND_ENEMY_FLOWER):
				return 1;
			}
			System.err.println("Z2 UNKNOWNN el = " + el);
			return 1;
		}
		return el; // TODO: Throw unknown ZLevel exception
	}

//	public List<String> LevelSceneAroundPlayerASCII(boolean Enemies,
//			boolean LevelMap, boolean mergedObservationFlag, int ZLevelScene,
//			int ZLevelEnemies) {
//		// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));//
//		// bw.write("\nTotal world width = " + level.width);
//		List<String> ret = new ArrayList<String>();
//
//		for (Player player : players) 
//		{
//			if (level != null && player != null) 
//			{
//				ret.add("Total world width = " + level.width);
//				ret.add("Total world height = " + level.height);
//				ret.add("Physical Player " + players.indexOf(player)
//						+ " Position (x,y): (" + player.x + "," + player.y
//						+ ")");
//				ret.add("Player Observation Width " + Environment.HalfObsWidth * 2 + 1);
//				ret.add("Player Observation Height " + Environment.HalfObsHeight * 2 + 1);
//				ret.add("X Exit Position: " + level.xExit);
//				GlobalCoarse playerInMap = player.getPosition()
//						.toGlobalCoarse();
//				ret.add("Calibrated Player Position (x,y): (" + playerInMap.x
//						+ "," + playerInMap.y + ")\n");
//
//				byte[][] levelScene = levelSceneObservation(ZLevelScene, player);
//				if (LevelMap) {
//					ret.add("~ZLevel: Z" + ZLevelScene + " map:\n");
//					for (int x = 0; x < levelScene.length; ++x) {
//						String tmpData = "";
//						for (int y = 0; y < levelScene[0].length; ++y)
//							tmpData += mapElToStr(levelScene[x][y]);
//						ret.add(tmpData);
//					}
//				}
//
//				byte[][] enemiesObservation = null;
//				if (Enemies || mergedObservationFlag) 
//				{
//					enemiesObservation = enemiesObservation(ZLevelEnemies,player);
//				}
//
//				if (Enemies) 
//				{
//					ret.add("~ZLevel: Z" + ZLevelScene
//							+ " Enemies Observation:\n");
//					for (int x = 0; x < enemiesObservation.length; x++) 
//					{
//						String tmpData = "";
//						for (int y = 0; y < enemiesObservation[0].length; y++) 
//						{
//							// if (x >=0 && x <= level.xExit)
//							tmpData += enemyToStr(enemiesObservation[x][y]);
//						}
//						ret.add(tmpData);
//					}
//				}
//
//				if (mergedObservationFlag) 
//				{
//					byte[][] mergedObs = mergedObservation(ZLevelScene,
//							ZLevelEnemies, player);
//					ret.add("~ZLevelScene: Z" + ZLevelScene
//							+ " ZLevelEnemies: Z" + ZLevelEnemies
//							+ " ; Merged observation /* Player ~> #M.# */");
//					for (int x = 0; x < levelScene.length; ++x) {
//						String tmpData = "";
//						for (int y = 0; y < levelScene[0].length; ++y)
//							tmpData += mapElToStr(mergedObs[x][y]);
//						ret.add(tmpData);
//					}
//				}
//			} else
//				ret.add("~level or player is not available");
//		}
//
//		return ret;
//	}

	public void checkGrumpyCollide(Grumpy grumpy) {
		grumpysToCheck.add(grumpy);
	}

	public void checkFireballCollide(Fireball fireball) {
		fireballsToCheck.add(fireball);
	}


	public void bump(int x, int y, boolean canBreakBricks, Player player) {

		byte block = level.getBlock(x, y);
		
		

		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0) 
		{
			
			
			level.setBlock(x, y, (byte) 4);
			level.setBlockData(x, y, (byte) 4);
			
			if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
			{
				if (!player.large)
				{
					addSprite(new Wrench(this, x * DISCRETE_CELL_SIZE + 8, y
							* DISCRETE_CELL_SIZE + 8));
				} 
				else 
				{
					addSprite(new BulbFlower(this, x * DISCRETE_CELL_SIZE + 8,
							y * DISCRETE_CELL_SIZE + 8));
				}
			} else
			{
				player.increaseEnergy(ENERGY_INCREASE);
				//player.addEnergy();
				addSprite(new EnergyAnim(x, y));
			}
		}

		if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
		{
			
			if (canBreakBricks)
			{
				level.setBlock(x, y, (byte) 0);
				for (int xx = 0; xx < 2; xx++)
					for (int yy = 0; yy < 2; yy++)
						addSprite(new Particle(x * DISCRETE_CELL_SIZE + xx * 8
								+ 4, y * DISCRETE_CELL_SIZE + yy * 8 + 4,
								(xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
				
				Sounds.play(Type.BLOCK_DESTRUCTION);
				
				player.getHurt();
				
			} else
			{
//				level.setBlockData(x, y, (byte) 4);
			}
		}
	}

	/**
	 * This function decides whether something player picked up was a energy simply
	 * by checking the pickupable bit of the block from the tiles.dat. After
	 * that it checks all sprites whether F bumped into them at the current
	 * position.
	 * 
	 * @param x
	 *            coordinate of the tile
	 * @param y
	 *            coordinate of the tile
	 */
	private void bumpInto(int x, int y, Player player)
	{
		byte block = level.getBlock(x, y);
		// if block is pickupable
		if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
		{
			player.increaseEnergy(ENERGY_INCREASE);
			//player.addEnergy();
			
			// remove pickupable by setting its id to 0
			level.setBlock(x, y, (byte) 0);
			addSprite(new EnergyAnim(x, y + 1));
		}

		for (Sprite sprite : sprites)
		{
			sprite.bumpCheck(x, y);
		}
	}

	/**
	 * register a Tickable whose onTick() method will be called by this class
	 * once per time step at a specific point.
	 * 
	 * @param tickable
	 */
	public void addTickable(Tickable tickable) {
		this.tickables.add(tickable);
	}

	public LevelScene cloneForSimulation() {
		LevelScene ret = new LevelScene(graphicsConfiguration, renderer,
				levelSeed, levelDifficulty, levelType, levelLength, totalTime,
				ownLevelName);

		// TODO clone tickables as well?
		ret.tickables.clear();

		// ret.xCam = xCam;
		// ret.yCam = yCam;
		// ret.xCamO = xCamO;
		// ret.yCamO = yCamO;
		for (float[] cam : cameras)
			ret.cameras.add(cam.clone());
		for (float[] cam : old_cameras)
			ret.old_cameras.add(cam.clone());

		ret.paused = paused;
		ret.startTime = startTime;
		ret.timeLeft = timeLeft;
		ret.fireballsOnScreen = fireballsOnScreen;

		// TODO maybe clone grumpysToCheck and fireballsToCheck as well?
		ret.grumpysToCheck.clear();
		ret.fireballsToCheck.clear();
		ret.tick = tick;

		// TODO clone layers and renderers too?
		ret.renderer = null;
		// ret.layers = layers;
		// ret.bgLayers = bgLayers;

		ret.levelDifficulty = levelDifficulty;

		ret.level = level.clone();

		for (Player player : players)
			ret.players.add(player.cloneWithNewLevelScene(ret));

		for (Sprite s : sprites) {
			if (s instanceof Player) {
				ret.sprites.add((Player) s); // CHECK
			} else {
				s.cloneWithNewLevelScene(ret);
				ret.sprites.add(s);
			}
		}
		for (Sprite s : spritesToAdd) {
			if (s instanceof Player) {
				ret.spritesToAdd.add((Player) s); // CHECK
			} else {
				s.cloneWithNewLevelScene(ret);
				ret.spritesToAdd.add(s);
			}
		}
		for (Sprite s : spritesToRemove) {
			if (s instanceof Player) {
				ret.spritesToRemove.add((Player) s); // CHECK
			} else {
				s.cloneWithNewLevelScene(ret);
				ret.spritesToRemove.add(s);
			}
		}
		return ret;
	}
}
