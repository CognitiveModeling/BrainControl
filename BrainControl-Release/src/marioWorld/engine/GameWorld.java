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

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.VolatileImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import marioAI.agents.CAEAgent;
import marioWorld.agents.Agent;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;
import marioWorld.mario.environments.Environment;
import marioWorld.mario.simulation.SimulationOptions;
import marioWorld.tools.EvaluationInfo;
//import marioWorld.tools.GameViewer;
//import marioWorld.tools.tcp.ServerAgent;
import marioWorld.utils.ResetStaticInterface;

/*
 * @author: ..., Fabian Schrodt
 * */
public class GameWorld implements Runnable, FocusListener, Environment, ResetStaticInterface
{	
	//private GameViewer gameViewer = null;

	private static ArrayList<PlayerVisualizationContainer> playerContainers = new ArrayList<PlayerVisualizationContainer>();
	
	public OverlayManager overlays;

	GraphicsConfiguration graphicsConfiguration;

	/**
	 * wait this many frames before using the agent. This is the time required
	 * for mario to fall down from the top (he starts at (32,0)) to the ground.
	 */	
	public static final int INITIAL_WAITING_TIME = 0;

	public static final int TICKS_PER_SECOND = 24;

	private boolean running = false;
	//private LevelScene scene;
	int frame;
	int delay;
	Thread animator;

	private int ZLevelEnemies = 1;
	private int ZLevelScene = 1;

	public boolean isRunning()
	{
		return running;		
	}
	
//	public void setGameViewer(GameViewer gameViewer)
//	{
//		this.gameViewer = gameViewer;
//	}

	public void addPlayerContainer(Agent agent, Player player)
	{
		playerContainers.add(new PlayerVisualizationContainer(agent, player));

		//associate player to agent:
		agent.setPlayer(player);
		//agent.setPlayerIndex(playerContainers.size()-1);
	}	

	// public class DebugFrame extends JFrame {
	// /**
	// *
	// */
	// private static final long serialVersionUID = -8662646414853358271L;
	// private JLabel label = new JLabel("Debug Frame");
	//
	// public DebugFrame() {
	// super("Debug Frame");
	// setSize(200, 50);
	// Point location = (Point) GameWorldComponent.this.getLocation()
	// .clone();
	// location.translate(GameWorldComponent.this.width + 20, 0);
	// setLocation(location);
	// getContentPane().add(label);
	// setVisible(true);
	// }
	//
	// public void setText(String text) {
	// this.label.setText(text);
	// }
	// }

	/*public ArrayList<Agent> getAgents() 
	{
		return agents;
	}*/

	/**
	 * Attention: do not use this to iterate over its size! player indices are not necessarily equal to the index in the list!
	 * */
	public ArrayList<PlayerVisualizationContainer> getPlayerContainers()
	{
		return playerContainers;
	}	
	
	public PlayerVisualizationContainer getPlayerContainer(int playerIndex)
	{
		for(int i=0;i<playerContainers.size();i++)
		{
			if(playerContainers.get(i).agent.getPlayerIndex()==playerIndex)
				return playerContainers.get(i);
		}
		return null;
	}

	public PlayerVisualizationContainer getPlayerContainer(Agent forAgent)
	{
		for(int i=0;i<playerContainers.size();i++)
		{
			if(playerContainers.get(i).agent==forAgent)
				return playerContainers.get(i);
		}
		return null;
	}	
	public PlayerVisualizationContainer getPlayerContainer(Player forPlayer)
	{
		for(int i=0;i<playerContainers.size();i++)
		{
			if(playerContainers.get(i).player==forPlayer)
				return playerContainers.get(i);
		}
		return null;
	}	
	public PlayerVisualizationContainer getPlayerContainer(GameWorldComponent forComponent)
	{
		for(int i=0;i<playerContainers.size();i++)
		{
			if(playerContainers.get(i).gameWorldComponent==forComponent)
				return playerContainers.get(i);
		}
		return null;
	}	
	public Player getPlayer(int playerIndex)
	{
		return playerContainers.get(playerIndex).player;
	}	


	public GameWorld clone()
	{
		GameWorld ret = new GameWorld();
		//ret.scene = scene.cloneForSimulation();
		//ret.players = ret.scene.players;
		//ret.playerContainers=playerContainers; 
		return ret;
	}

	public void initArts()
	{
		Art.init(graphicsConfiguration);
	}	

	public GameWorld()
	{			
		classesWithStaticStuff.add(this.getClass());		
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);		
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice gd = gs[0];
		graphicsConfiguration = gd.getConfigurations()[0];
		
		initArts();

		//System.out.println("graphicsConfiguration " + graphicsConfiguration);		
		
		overlays = new OverlayManager();

		adjustFPS();

		GlobalOptions.registerGameWorld(this);		
	}

	/**
	 * Returns the List of Players.
	 * 
	 * @return
	 */
	/*public ArrayList<Player> getPlayers()
	{
		return players;
	}*/

	/**
	 * Returns the current instance of LevelScene.
	 * 
	 * @return
	 */
	/*public LevelScene getLevelScene()
	{
		return this.scene;
	}*/

	public void adjustFPS()
	{
		int fps = GlobalOptions.FPS;
		if (fps > 0)
		{
			if (fps >= GlobalOptions.InfiniteFPS)
				delay = 0;
			else
				delay = 1000 / fps;
		} 
		else
			delay = 100;			
	}


	public void start()
	{
		if (!running)
		{
			running = true;
			animator = new Thread(this, "Game Thread");
			animator.start();
		}
	}

	public void stop()
	{
		System.out.println("game world stopped");
		running = false;
		if(animator!=null)
			animator.interrupt();
	}

	/*public void run()
	{

	}*/

	/**
	 * Called whenever a new run with an agent is started.
	 * 
	 * @param currentTrial
	 * @param totalNumberOfTrials
	 * @return
	 */
	public EvaluationInfo run1(int currentTrial, int totalNumberOfTrials)
	{
		// this.overlayWindow = new OverlayWindow(overlays);

		running = true;
		adjustFPS();
		EvaluationInfo evaluationInfo = new EvaluationInfo();

		final ArrayList<VolatileImage> images = new ArrayList<VolatileImage>();
		final ArrayList<Graphics2D> graphics = new ArrayList<Graphics2D>();
		final ArrayList<Graphics2D> overlay_graphics = new ArrayList<Graphics2D>();

		int size = (playerContainers != null ) ? playerContainers.size() : 0;

		
		for(int i=0;i<1;i++)
		{
			images.add(playerContainers.get(0).gameWorldComponent.createVolatileImage(GlobalOptions.xScreen, GlobalOptions.yScreen));
			
			graphics.add((Graphics2D) playerContainers.get(0).gameWorldComponent.getGraphics()); // the actual game graphics			
			overlay_graphics.add((Graphics2D) images.get(i).getGraphics()); // overlay graphics
			
			
			if (!GlobalOptions.VisualizationOn)
			{
				String msgClick = "Vizualization is not available";
				drawString(overlay_graphics.get(i), msgClick, 160 - msgClick.length() * 4, 110, 1);
				drawString(overlay_graphics.get(i), msgClick, 160 - msgClick.length() * 4, 110, 7);
			}

		}

		// Remember the starting time
		long tm = System.currentTimeMillis();
		long tick = tm;

		//int marioStatus = Player.STATUS_RUNNING;

		int totalActionsPerfomed = 0;
		int test = 0;

		// main simulation loop
		while (/* Thread.currentThread() == animator */running)
		{				
//			if(playerContainers != null)
//				size = playerContainers.size();
			if(GlobalOptions.pauseGame) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!GlobalOptions.pauseGame)
			{
				//TODO: is it really necessary to set the keys here!?!?
				//TEST: set the keys BEFORE the level and brains are ticked! since their behavior may depend on the keypresses!
				for(int playerIndex=0;playerIndex<size;playerIndex++)
				{					
					boolean[] action = new boolean[Environment.numberOfButtons];
					if (frame > INITIAL_WAITING_TIME)
					{
						action = playerContainers.get(playerIndex).agent.getAction(this/* DummyEnvironment */).clone();
					}
	
					if (action != null)
					{
						for (int i = 0; i < Environment.numberOfButtons; ++i)
						{
							if (action[i])
							{
								//System.out.println("set keys of player "+playerIndex);
								++totalActionsPerfomed;
								break;
							}
						}
					} 
					else
					{
						System.err.println("Null Action received. Skipping simulation...");
						stop();
					}
					
					// Apply action;
					// scene.keys = action;
					playerContainers.get(playerIndex).agent.getPlayer().keys = action;
				}
				
				
				// Display the next frame of animation.
				// repaint();
				GlobalOptions.realLevelScene.tick();

//				if (gameViewer != null && gameViewer.getContinuousUpdatesState())
//					gameViewer.tick();
				
				
				graphics.removeAll(graphics);
				//plot level stuff
				for(int playerIndex=0;playerIndex<1;playerIndex++)					
				{
					
					
					
					graphics.add((Graphics2D) playerContainers.get(playerIndex).gameWorldComponent.getGraphics());
					
//					if(GlobalOptions.playerGameWorldToDisplay != playerIndex) {
//						continue;
//					}
					float alpha = 0;
					// og.setColor(Color.RED);					

					if (GlobalOptions.VisualizationOn)
					{
						//CHECK: not needed?
						//overlay_graphics.get(playerIndex).fillRect(0, 0, GlobalOptions.xScreen, GlobalOptions.yScreen);

						GlobalOptions.realLevelScene.render(overlay_graphics.get(playerIndex), alpha, GlobalOptions.playerGameWorldToDisplay);
					}


					/*if (playerContainers.get(playerIndex).agent instanceof ServerAgent)
					{
						if (!((ServerAgent) playerContainers.get(playerIndex).agent).isAvailable())
						{
							System.err.println("Agent became unavailable. Simulation Stopped");
							running = false;
							break;
						}
					}*/					
					
					if (GlobalOptions.VisualizationOn)
					{
//						boolean[] action = playerContainers.get(playerIndex).agent.getPlayer().keys;
//						
//						// do the default visualization	
////						 doVisualization(og, action, tick, currentTrial, totalNumberOfTrials);
//
//						//final Graphics og2 = overlay_graphics.get(playerIndex);
//						final boolean[] action2 = action.clone();	//TODO: cloing necessary here!? why copy at all?
//						final long tick2 = tick;
//						final int currentTrial2 = currentTrial;
//						final int totalNumberOfTrials2 = totalNumberOfTrials;
						final int playerIndex2 = playerIndex;
//						final GameWorldComponent component = playerContainers.get(playerIndex).gameWorldComponent;

						try
						{
							SwingUtilities.invokeAndWait(new Runnable()
							{
								@Override
								public void run()
								{
									/**
									 * TODO: unnecessary?
									 */
//									component.doVisualization(overlay_graphics.get(playerIndex2), action2, tick2, currentTrial2, totalNumberOfTrials2);

									// render the overlays
									if(overlays != null)
										overlays.render(overlay_graphics.get(playerIndex2), GlobalOptions.realLevelScene, true, GlobalOptions.playerGameWorldToDisplay);

									// render visualization to the screen
									/*if (component.WIDTH != GlobalOptions.xScreen || component.HEIGHT != GlobalOptions.yScreen)
									{
										graphics.get(playerIndex2).drawImage(images.get(playerIndex2), 0, 0, 640 * 2, 480 * 2, null);
									} 
									else
									{
										graphics.get(playerIndex2).drawImage(images.get(playerIndex2), 0, 0, null);
									}*/
									
									
									//graphics.get(playerIndex2).drawImage(images.get(playerIndex2), 0, 0, GlobalOptions.xScreen * GlobalOptions.xScreen/GlobalOptions.xGrid, GlobalOptions.yScreen * GlobalOptions.yScreen/GlobalOptions.yGrid, null);
									//graphics.get chooses one of the 3 screenpanels to render, images.get chooses the image to render
									graphics.get(playerIndex2).drawImage(images.get(playerIndex2), 0, 0, GlobalOptions.xScreen, GlobalOptions.yScreen, null);
								}
							});
						} catch (InvocationTargetException e){
							e.printStackTrace();
						}catch(InterruptedException i){
							Thread.currentThread().interrupt();
							//THREAD interrupted current thread 
						}
					} 
					/*else
					{
						// Win or Die without renderer!! independently.
						marioStatus = GlobalOptions.realLevelScene.players.get(0).getStatus();		//game ends if player 0 dies
						if (marioStatus != Player.STATUS_RUNNING)
							stop();
					}*/
				}

				// Delay depending on how far we are behind.
				/*if (delay > 0)
					try
				{
						tm += delay;
						Thread.sleep(Math.max(0,
								tm - System.currentTimeMillis()));
				} catch (InterruptedException e)
				{
					break;
				}*/
				// We rather prefer to ignore if we are behind with plotting... otherwise this does not look nice if the prog freezes for a second and then speeds up:
				
				
				
				try 
				{
					Thread.sleep(delay);
				} 
				catch (InterruptedException e) 
				{				
					e.printStackTrace();
				}				
				
				
				
				
				
				
				// Advance the frame
				frame++;
				// System.out.println("marioY: "+mario.y);
				// System.out.println("\n\n------ simulationTime: "+frame+"---------\n\n");
			}
		}

		for(PlayerVisualizationContainer playerContainer : playerContainers)
		{
			evaluationInfo.agentType = playerContainer.agent.getClass().getSimpleName();
			evaluationInfo.agentName = playerContainer.agent.getName();
			evaluationInfo.playerStatus = playerContainer.agent.getPlayer().getStatus();
			evaluationInfo.livesLeft = Player.lives;
			evaluationInfo.lengthOfLevelPassedPhys = playerContainer.agent.getPlayer().x;
			evaluationInfo.lengthOfLevelPassedCells = playerContainer.agent.getPlayer().mapX;
			evaluationInfo.totalLengthOfLevelCells = GlobalOptions.realLevelScene.level.getWidthCells();
			evaluationInfo.totalLengthOfLevelPhys = GlobalOptions.realLevelScene.level.getWidthPhys();
			evaluationInfo.timeSpentOnLevel = GlobalOptions.realLevelScene.getStartTime();
			evaluationInfo.timeLeft = GlobalOptions.realLevelScene.getTimeLeft();
			evaluationInfo.totalTimeGiven = GlobalOptions.realLevelScene.totalTime;
			evaluationInfo.numberOfGainedEnergys = playerContainer.agent.getPlayer().getEnergys();
			// TODO: total Number of energys.
			// evaluationInfo.totalNumberOfEnergys = -1 ;

			// Counted during the play/simulation process
			evaluationInfo.totalActionsPerfomed = totalActionsPerfomed;
			evaluationInfo.totalFramesPerfomed = frame;
			evaluationInfo.playerMode = playerContainer.agent.getPlayer().getMode();
			evaluationInfo.killsTotal = LevelScene.killedCreaturesTotal;


//			// evaluationInfo.Memo = "Number of attempt: " + Mario.numberOfAttempts;
//			if (playerContainer.agent instanceof ServerAgent && playerContainer.agent.getPlayer().keys != null)
//				((ServerAgent) playerContainer.agent).integrateEvaluationInfo(evaluationInfo);
		}		

		return evaluationInfo;
	}

	private void drawString(Graphics g, String text, int x, int y, int c)
	{
		char[] ch = text.toCharArray();
		for (int i = 0; i < ch.length; i++)
		{
			g.drawImage(Art.font.getImage(ch[i] - 32,c), x + i * 8, y, null);
		}
	}

	/**
	 * Called right before the run1 method is called, performs basic setup
	 * before the simulation starts.
	 * 
	 * @param seed
	 * @param difficulty
	 * @param type
	 * @param levelLength
	 * @param timeLimit
	 */
	public void startLevel(SimulationOptions simulationOptions)
	{
		long seed = simulationOptions.getLevelRandSeed();
		int difficulty = simulationOptions.getLevelDifficulty();
		int type = simulationOptions.getLevelType();
		int levelLength = simulationOptions.getLevelLength();
		int timeLimit = simulationOptions.getTimeLimit();
		String ownLevelName = simulationOptions.getOwnLevelName();
		ArrayList<Sprite> initialSprites = simulationOptions.getInitialSprites();

		System.out.println("Starting level. Random seed is: " + seed);

		GlobalOptions.realLevelScene = new LevelScene(graphicsConfiguration, this, seed, difficulty, type, levelLength, timeLimit, ownLevelName);
		GlobalOptions.realLevelScene.setPlayers(simulationOptions.getPlayers());

		/*
		 * Create playercontainers here... this will start the visualization, associate agents to players, and stuff...
		 * */
		for(int i=0;i<simulationOptions.getAgents().size();i++)
		{
			addPlayerContainer(simulationOptions.getAgents().get(i), simulationOptions.getPlayers().get(i));

			simulationOptions.getPlayers().get(i).setLevelScene(GlobalOptions.realLevelScene);
		}				        

		GlobalOptions.realLevelScene.init(initialSprites);
	}

	public void levelFailed()
	{
		// scene = mapScene;
		Player.lives--;
		//stop();	//disabled
	}

	public void focusGained(FocusEvent arg0)
	{
	}

	public void focusLost(FocusEvent arg0)
	{
	}

	public void levelWon()
	{
		//stop();	//TODO: instead, load a new map by event!!!!

		// scene = mapScene;
		// mapScene.levelWon();
	}

	public void toTitle()
	{
		// Mario.resetStatic();
		// scene = new TitleScene(this, graphicsConfiguration);
		// scene.init();
	}

//	public List<String> getTextObservation(boolean Enemies, boolean LevelMap,
//			boolean Complete, int ZLevelMap, int ZLevelEnemies)
//			{
//		return GlobalOptions.realLevelScene.LevelSceneAroundPlayerASCII(Enemies, LevelMap, Complete,
//				ZLevelMap, ZLevelEnemies);
//			}

//	public String getBitmapEnemiesObservation(Player player)
//	{
//		return GlobalOptions.realLevelScene.bitmapEnemiesObservation(1, player);
//	}
//
//	public String getBitmapLevelObservation(Player player)
//	{
//		return GlobalOptions.realLevelScene.bitmapLevelObservation(1, player);
//	}

	// Chaning ZLevel during the game on-the-fly;
//	public byte[][] getMergedObservationZ(int zLevelScene, int zLevelEnemies, Player player)
//	{
//		if (GlobalOptions.realLevelScene instanceof LevelScene)
//			return ((LevelScene) GlobalOptions.realLevelScene).mergedObservation(zLevelScene,
//					zLevelEnemies, player);
//		return null;
//	}

//	public byte[][] getLevelSceneObservationZ(int zLevelScene, Player player)
//	{
//		if (GlobalOptions.realLevelScene instanceof LevelScene)
//			return ((LevelScene) GlobalOptions.realLevelScene).levelSceneObservation(zLevelScene, player);
//		return null;
//	}

//	public byte[][] getEnemiesObservationZ(int zLevelEnemies, Player player)
//	{
//		if (GlobalOptions.realLevelScene instanceof LevelScene)
//			return ((LevelScene) GlobalOptions.realLevelScene).enemiesObservation(zLevelEnemies, player);
//		return null;
//	}

	public int getKillsTotal()
	{
		return LevelScene.killedCreaturesTotal;
	}

	public int getKillsByFire()
	{
		return LevelScene.killedCreaturesByFireBall;
	}

	public int getKillsByStomp()
	{
		return LevelScene.killedCreaturesByStomp;
	}

	public int getKillsByGrumpy()
	{
		return LevelScene.killedCreaturesByGrumpy;
	}

//	public byte[][] getCompleteObservation(Player player)
//	{
//		if (GlobalOptions.realLevelScene instanceof LevelScene)
//			return ((LevelScene) GlobalOptions.realLevelScene).mergedObservation(this.ZLevelScene, this.ZLevelEnemies, player);
//		return null;
//	}

//	public byte[][] getEnemiesObservation(Player player)
//	{
//		if (GlobalOptions.realLevelScene instanceof LevelScene)
//			return ((LevelScene) GlobalOptions.realLevelScene).enemiesObservation(this.ZLevelEnemies, player);
//		return null;
//	}

//	public byte[][] getLevelSceneObservation(Player player)
//	{
//		if (GlobalOptions.realLevelScene instanceof LevelScene)
//			return ((LevelScene) GlobalOptions.realLevelScene).levelSceneObservation(this.ZLevelScene, player);
//		return null;
//	}

	public boolean isPlayerOnGround(Player player)
	{
		return player.isOnGround();
	}

	public boolean mayPlayerJump(Player player)
	{
		return player.isMayJump();
	}

	/*public void setAgents(ArrayList <Agent> agents)
	{
		this.agents = agents;

		for(int i=0;i<agents.size();i++)
		{		
			if (agents.get(i) instanceof KeyAdapter)
			{
				if (gameWorldComponents.get(i).prevHumanKeyBoardAgent != null)
				{
					gameWorldComponents.get(i).removeKeyListener(gameWorldComponents.get(i).prevHumanKeyBoardAgent);
				}
				gameWorldComponents.get(i).prevHumanKeyBoardAgent = (KeyAdapter) agents.get(i);
				gameWorldComponents.get(i).addKeyListener(gameWorldComponents.get(i).prevHumanKeyBoardAgent);
			} 
			else if (agents.get(i) instanceof KeyListener)
			{
				if (gameWorldComponents.get(i).prevHumanKeyBoardAgent != null)
				{
					gameWorldComponents.get(i).removeKeyListener(gameWorldComponents.get(i).prevHumanKeyBoardAgent);
				}
				gameWorldComponents.get(i).prevHumanKeyBoardAgent = (KeyListener) agents.get(i);
				gameWorldComponents.get(i).addKeyListener(gameWorldComponents.get(i).prevHumanKeyBoardAgent);
			}
		}
	}*/

	//TODO: non-generic
	public void setPlayersInvulnerable(boolean invulnerable)
	{
		Player.isPlayerInvulnerable = invulnerable;
	}

	public void setPaused(boolean paused)
	{
		GlobalOptions.realLevelScene.paused = paused;
	}

	public void togglePaused()
	{
		GlobalOptions.realLevelScene.paused = !GlobalOptions.realLevelScene.paused;
	}

	public void setZLevelEnemies(int ZLevelEnemies)
	{
		this.ZLevelEnemies = ZLevelEnemies;
	}

	public void setZLevelScene(int ZLevelScene)
	{
		this.ZLevelScene = ZLevelScene;
	}

	public float[] getPlayerFloatPos(Player player)
	{
		return new float[] { player.x, player.y };
	}

	public float[] getEnemiesFloatPos()
	{
		if (GlobalOptions.realLevelScene instanceof LevelScene)
			return ((LevelScene) GlobalOptions.realLevelScene).enemiesFloatPos();
		return null;
	}

	//TODO: nonsense function?
	public int getPlayerMode(Player player)
	{
		return player.getMode();
	}

	public boolean isPlayerCarrying(Player player)
	{
		return player.getCarries() != null;
	}

	public static void deleteStaticAttributes() 
	{
		//playerContainers = null;
	}

	public static void resetStaticAttributes() 
	{
		playerContainers = new ArrayList<PlayerVisualizationContainer>();		
	}

	@Override
	public void run() 
	{
		// TODO Auto-generated method stub	
	}
}
