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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mario.main.PlayRelease;
import mario.main.Settings;
import mario.main.StartJar.ExitCode;
import marioAI.agents.CAEAgent;
import marioAI.brain.Brain;
import marioAI.run.PlayHook;
import marioAI.run.ReleaseGame;
import marioWorld.engine.Art;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.utils.ResetStaticInterface;
import marioWorld.utils.ResourceStream;
import marioWorld.utils.Sounds;
import marioUI.gui.StartGuiController;

/**
 * This class builds the ControlGuiFrame for all instantiated Players in the
 * Game
 * 
 * @author Jonas Einig
 *
 */
public class View extends JFrame implements ResetStaticInterface {
	private static final long serialVersionUID = 1L;
	// get screen size:
	public static final int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	public static final int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private ArrayList<PlayerPanel> playerPanels;
	public ArrayList<ScreenPanel> screenPanels;
	private VoicePanel voicePanel;
	private MenuPanel menuPanel;
	
	public static StartGuiController controller;

	/**
	 * constructor creates empty
	 * <code>JFrame<code> where <code>PlayerPanel<code>'s cana be (created and) added with the <code>addPlayer<code>-Method
	 */
	
	
	static JFrame quitGameFrame;
	
	// opens a dialog to ask player whether he wants to quit
	// (used in main.ReleaseGame.stop(); 
	public static boolean wantQuit(){
		
		int result = JOptionPane.showConfirmDialog(
				quitGameFrame, "Are you sure you want to exit the game?");
		if( result==JOptionPane.OK_OPTION){
			return true; 
		}
		
		return false;
	}
	
	static Object[] options = {"Return to Menu", "Next Level"};
	
	// opens a dialog to congratulate him when he finished a level
	public static boolean levelBeaten(){
		
		int result = JOptionPane.showOptionDialog(quitGameFrame, "Congratulations, you beat the level!", 
				"Level completed",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
				
		if( result==JOptionPane.OK_OPTION){
			return true; 
		}
		
		return false;
		
	}
	
	
	public View() {

		super("Brain Control! - University of Tuebingen");
		
		

		playerPanels = new ArrayList<PlayerPanel>();
		screenPanels = new ArrayList<ScreenPanel>();
		final JFrame frame = this;
		
		
		
		
		this.setFocusable(true);
		
		this.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				
				if(e.getKeyCode() == 27){//ESC Key
					int result = JOptionPane.showConfirmDialog(
							frame, "Are you sure you want to exit the game?");
					if( result==JOptionPane.OK_OPTION){
						// NOW we change it to dispose on close.. 
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.setVisible(false);
						GameEndHandler.stopWithoutEvaluation(ExitCode.QUIT);
					}
				} else if(e.getKeyCode() == 112) { //F1
					showDebugMenu();
					
				} else if(e.getKeyCode() == 113) {
					GlobalOptions.PlayerAlwaysInCenter = !GlobalOptions.PlayerAlwaysInCenter;
				}
			;
			}
			
		}); 
		
		

		// Frame
//		this.setUndecorated(true);

		this.setLocation(0, 0);

		this.getContentPane().setLayout(new GridBagLayout());

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);


		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
//				int result = JOptionPane.showConfirmDialog(
//						frame, "Are you sure you want to exit the game?");
//				if( result==JOptionPane.OK_OPTION){
//					// NOW we change it to dispose on close..
//					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//					frame.setVisible(false);
//					ReleaseGame.stop(ExitCode.QUIT);
//				}
				
				ReleaseGame.stop(ExitCode.QUIT);
			}
		});
		
//		this.setPreferredSize(new Dimension(1600,800));
		this.setMinimumSize(new Dimension(GlobalOptions.xScreen+610, GlobalOptions.yScreen+60));

//		this.setPreferredSize(new Dimension(GlobalOptions.xScreen+610, GlobalOptions.yScreen+60));
//		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		this.setExtendedState(JFrame.NORMAL);
		this.setVisible(true);
		
		Sounds.play(marioWorld.utils.Sounds.Type.START_LEVEL);

		
		
		TutorialFunction.getTutor().setLevel(PlayRelease.getCurrentLevel());
	}

	/**
	 * if individual
	 * <code>PlayerPanel<code>-Gui for a Player is wanted to be created and added to the main-control-gui 
	 * this method should be called or this player.
	 * 
	 * @param brain
	 * @param agent
	 * @param overlays
	 * @return <code>PlayerPanel<code> for the Player who called this method
	 */
	public PlayerPanel addPlayer(Brain brain, CAEAgent agent, String[] overlays) {
		PlayerPanel newPlayerPanel = new PlayerPanel(brain, agent, overlays);
		newPlayerPanel.setView(this);

		playerPanels.add(newPlayerPanel);

		ScreenPanel newScreenPanel = new ScreenPanel(PlayHook.gameWorld.getPlayerContainer(brain.getPlayerIndex()).gameWorldComponentPanel, agent.getName());

		screenPanels.add(newScreenPanel);

		/*voicePanel.voiceTab.activateReservoirs
		.addActionListener(newPanel.controller);
	
		voicePanel.voiceTab.activateInsecurity
		.addActionListener(newPanel.controller);*/
		voicePanel.voiceTab.activateRecognition
		.addActionListener(newPlayerPanel.controller);
		newPlayerPanel.controller.setVoicePanel(voicePanel);


		return newPlayerPanel;
	}

	public void addVoicePanel() {
		this.voicePanel = new VoicePanel();
		
		this.menuPanel = new MenuPanel(voicePanel);
	}

	/**
	 * this method rearranges the different <code>PlayerPanel<code>'s in the Gui
	 * should be called if new panel is added to the <code>ArrayList<code> <code>playerPanels<code>
	 */
	public void rearrange() {
		
		int size = playerPanels.size();
		
		JPanel playerPanelContainer = new JPanel(new GridBagLayout());
		JPanel screenPanelContainer = new JPanel(new CardLayout());

		this.menuPanel.getMissionControllPanels().clear();
		
		for(int i = 0; i<size; i++) {
			LayoutHelper.putGrid(playerPanelContainer, playerPanels.get(i), 0, i, 1, 1);
			
			this.menuPanel.addMissionControllPanel(playerPanels.get(i).getMissionControllTab());
		}
			screenPanelContainer.add(screenPanels.get(0));
//			screenPanels.get(i).gamePanel.setVisible(false);
//			screenPanels.get(0).gamePanel.setVisible(true);

		LayoutHelper.putGrid(this.getContentPane(), playerPanelContainer, 0,0, 1, 1, 1.0, 1.0,GridBagConstraints.NORTHWEST, GridBagConstraints.RELATIVE);
		LayoutHelper.putGrid(this.getContentPane(), screenPanelContainer, 1,0, 1, 1, 1.0, 1.0);
		LayoutHelper.putGrid(this.getContentPane(), menuPanel, 2, 0, 1, 1,5.0, 1.0,GridBagConstraints.NORTHWEST, GridBagConstraints.RELATIVE);
		playerPanels.get(0).setSelected();
		
		
		this.setMinimumSize(null);
		int x = GlobalOptions.menuPanelWidth + playerPanels.get(0).width + GlobalOptions.xScreen + (80-2*GlobalOptions.resolution_factor);
		this.setSize(new Dimension(x,GlobalOptions.yScreen+60));
		this.validate();
	}
	/**
	 * TODO: Reduce button fixen, -> view zentrieren, screenpanel.
	 * @param playerIndex
	 */
//	public void reduce(int playerIndex) {
//
//		int size = playerPanels.size();
//		this.getContentPane().removeAll();
//		this.revalidate();
//		this.getContentPane().setLayout(new GridBagLayout());
//		
//		LayoutHelper.putGrid(this.getContentPane(), this.timeClosePanel, 0, 1, size / 2,	1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.RELATIVE);
//		LayoutHelper.putGrid(this.getContentPane(), this.missionStatus,size / 2, 1, size / 2, 1, 1, 1);
//		for (int i = 0; i < size; i++) {
//			if(i== playerIndex) {
//				
//				LayoutHelper.putGrid(this.getContentPane(), screenPanels.get(i), 0,0, 1, 1, 0, 0);				
//	
//			} 
//			LayoutHelper.putGrid(this.getContentPane(), playerPanels.get(i), i,2, 1, 1, 1.0,1.0);
//		}
//
//		LayoutHelper.putGrid(this.getContentPane(), voicePanel, 0, 3, size, 1,1.0, 5.0);
//		
//		this.revalidate();
//	}
	
	public void changeVisiblePlayer(int index) {
		
		if(GlobalOptions.playerGameWorldToDisplay == index) {
			return;
		}
		GlobalOptions.oldPlayerGameWorldToDisplay = GlobalOptions.playerGameWorldToDisplay;
		GlobalOptions.playerGameWorldToDisplay = index;
		GlobalOptions.playerChanged = true;
		
		int size = playerPanels.size();

		playerPanels.get(index).setSelected();
		
		for(int i = 0; i<size; i++) {
			
			
			if(i != index) {
				playerPanels.get(i).deselect();
			}
		}

		
		
	}
	private void showDebugMenu() {
		this.menuPanel.toggleDebugMenu();
		
	}
	public VoicePanel getVoicePanel(){
		return this.voicePanel;
	}


}
