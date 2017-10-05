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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mario.main.Settings;
import mario.main.StartJar.ExitCode;
import marioAI.run.PlayHook;
import marioAI.run.ReleaseGame;
import marioUI.gui.MissionControllPanel.OverlaySelectionListener;
import marioWorld.engine.Art;
import marioWorld.engine.GlobalOptions;
import marioWorld.utils.Sounds;

/**
 * Gathers all the options and the voice history
 * @author Yves
 *
 */
public class MenuPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private VoicePanel voicePanel;
	private MissionControllPanel missionControllPanel;
	
	private JMenuBar menuBar;
	private JMenu options;
	private JMenu debug;
	
	private JMenuItem timer;
	private JMenuItem missionStatus;
	private JMenuItem closeButton;
	private JMenuItem menuButton;
	private JMenuItem commandGuideButton;
	private boolean musicIsPlaying = false;
	
	private ArrayList<MissionControllPanel> missionControllPanels = new ArrayList<MissionControllPanel>();
	private ArrayList<JCheckBoxMenuItem> allOverlayCheckBoxes = new ArrayList<JCheckBoxMenuItem>();
	
	private boolean firstCall = true;
	private boolean buttonStatus = true; // music should be played
	
	public MenuPanel(VoicePanel voicePanel) {
		
		this.voicePanel = voicePanel;
		this.setLayout(new GridBagLayout());
		
		JPanel menubarPanel = new JPanel();
		menubarPanel.setLayout(new GridBagLayout());
		
				
		JMenu menu = new JMenu("Menu");
		options = new JMenu("Options");

	    menuBar = new JMenuBar();
	    menuBar.add(menu);
	    menuBar.add(options);
	    
	    debug = new JMenu("Debug");
	    menuBar.add(debug);
	    debug.setVisible(false); 
	    
	    final String CommandList = "<html>"
					+ "<br>  <h1>These are some examples for possible commands:</h1>"
					+ "<br> <b>Primitive Movement:</b>"
					+ "<br>  (go | run | walk | head | jump) [to the] (left | right);  jump [up]; (stop | wait | hold on) [[for] a second]"
					+ "<br>  "
					+ "<br> <b>Goal:</b>"
					+ "<br>  (find | look for | go to | touch | hit | collide with)  (PLAYER | OBJECT)"
					+ "<br>  "
					+ "<br> <b>Goal Event:</b>"
					+ "<br>  (destroy | create) OBJECT; kill ENEMY; (mount | unmount | drop | carry) PLAYER (if already learned)"
					+ "<br>  "
					+ "<br>  <b> Motivation:</b>"
					+ "<br>  make progress; retreat"
					+ "<br>  (increase | improve | watch) [your] (energy | health | esteem)"
					+ "<br>  stop"
					+ "<br>  think for your self | do something <i>[activates autonomous behaviour]</i>"
					+ "<br>  stop acting <i>[deactivates autonomous behaviour]</i>"
					+ "<br>  "
					+ "<br>  <b> Knowledge:</b>"
					+ "<br>  tell me about OBJECT; tell me how to (collect energy | mount somebody | destroy OBJECT)"
					+ "<br>  "
					+ "<br> <b> Knowledge Input:</b>"
					+ "<br>  example: listen if you (collide with | hit | run into | touch) an OBJECT from the (left | right) (and | while) you are (healthy | large) then a bulb will be created"
					+ "<br>  "
					+ "<br>  <b> Knowledge Transfer:</b>"
					+ "<br>  PLAYER1 tell PLAYER2 how to EFFECT"
					+ "<br>  "
					+ "<br>  <b> Other Commands:</b>"
					+ "<br>  PLAYER1 give X energy to PLAYER2; hello; (how are you | are you happy); exit the game; yes; no </html>"
					; 
	    
	    this.timer = new JMenuItem();
		this.missionStatus = new JMenuItem();

			ActionListener updateClockAction = new ActionListener() {
				private Integer secs = 0;
				private Integer mins = 0;

				public void actionPerformed(ActionEvent e) {
					this.secs++;
					if (secs == 60) {
						mins++;
						secs = 0;
					}
					missionStatus.setText("Agents in Goal: "+GlobalOptions.realLevelScene.playersAtEnd+"/"
							+ PlayHook.agents.size());
					timer.setText("Time passed: " + String.format("%02d", mins)
							+ ":" + String.format("%02d", secs) + "   ");
				}
			};
			
		this.closeButton = new JMenuItem("Exit");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Really Quits the Game
				
				ReleaseGame.stop(ExitCode.QUIT);
				
				
			}
		});
			
		this.menuButton = new JMenuItem("Main Menu");
		menuButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Exits back to Menu
				
				ReleaseGame.stop(ExitCode.MENU);
				
			}
		});
			
		Timer t = new Timer(1000, updateClockAction);
		t.start();

	     final JButton playMusic = new JButton(new ImageIcon(Art.speaker));
			playMusic.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					if (!buttonStatus) 
					{
						Sounds.loop(marioWorld.utils.Sounds.Type.SOUNDTRACK);
						musicIsPlaying = true;
						buttonStatus = true;
						playMusic.setIcon(new ImageIcon(Art.speaker));
					} 
					else 
					{		
						buttonStatus =false;
						Sounds.stop(marioWorld.utils.Sounds.Type.SOUNDTRACK);
						playMusic.setIcon(new ImageIcon(Art.mute));
						musicIsPlaying = false;
					}			
				}
			});
			
	
		
		final JSlider volume = new JSlider(0,1000);
		
		volume.addChangeListener(new ChangeListener() 
		{			
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				Sounds.setVolume(marioWorld.utils.Sounds.Type.SOUNDTRACK,(float)volume.getValue()/(float)volume.getMaximum());
			}
		});
		
//		playMusic.setSelected(true);	

		
		
		volume.setValue((int)(Settings.START_MUSIC_VOLUME*volume.getMaximum()));
		// wait a little bit before music starts
		new Thread(new Runnable() {			
			@Override
			public void run() {
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (buttonStatus && !musicIsPlaying){
					musicIsPlaying = true;
					Sounds.loop(marioWorld.utils.Sounds.Type.SOUNDTRACK);	
				}
			}
		}).start(); 
			
			
		
		commandGuideButton = new JMenuItem("Commands"); 
		
		commandGuideButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JLabel label = new JLabel(CommandList);
				 label.setFont(new Font("serif", Font.PLAIN, 14));
		        
				// One Window Version
				JOptionPane.showMessageDialog(null, label,
 						"Commands", JOptionPane.PLAIN_MESSAGE);
				
				
				//Scrollable Version KEIN HTML
				/*
				  JTextAre textArea = new JTextaArea();
			      textArea.setText(CommandList);
			      textArea.setEditable(false);
			      JScrollPane scrollPane = new JScrollPane(textArea);
			      JOptionPane.showMessageDialog(null, scrollPane, "Command List", JOptionPane.INFORMATION_MESSAGE);
				 */
			}
								
		});
			
		final JButton pauseGame = new JButton(new ImageIcon(Art.pause));

		pauseGame.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GlobalOptions.togglePause();
				if(GlobalOptions.pauseGame) {

					pauseGame.setIcon(new ImageIcon(Art.play));
				} else {
					pauseGame.setIcon(new ImageIcon(Art.pause));
				}
			}
		});
		
//		pauseGame.setBorder(null);


			
		

		JCheckBoxMenuItem autoHelpBox = TutorialFunction.getTutor().getAutoCheckBox();
		JMenuItem helpButton = TutorialFunction.getTutor().getHelpButton();
			
			
		options.add(autoHelpBox);
		options.add(this.voicePanel.getVoiceTab().activateRecognition);
		options.addSeparator();
		options.add(createHeader("Overlays: "));
		
		
		
//		options.add(playMusic);
		menu.add(timer);
		menu.add(missionStatus);
		
		menu.addSeparator();
		
		menu.add(commandGuideButton);
		menu.add(helpButton);
		
		menu.addSeparator();
		
		menu.add(menuButton);
		menu.add(closeButton);
		LayoutHelper.putGrid(menubarPanel, menuBar, 0, 0, 1, 1,0.2,0.2,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH);
		LayoutHelper.putGrid(menubarPanel, pauseGame, 1, 0, 1, 1);
		LayoutHelper.putGrid(menubarPanel, playMusic, 0, 1, 1, 1);
		LayoutHelper.putGrid(menubarPanel, volume, 1, 1, 1, 1,0.2,0.2,GridBagConstraints.WEST, GridBagConstraints.BOTH);
		
		LayoutHelper.putGrid(this, menubarPanel, 0, 0, 1, 1);
		LayoutHelper.putGrid(this, voicePanel, 0, 1, 1, 1,1,1,GridBagConstraints.WEST, GridBagConstraints.BOTH);
	}

	private JComponent createHeader(String header) {
        JLabel label = new JLabel(header);
        label.setFont(label.getFont().deriveFont(Font.ITALIC));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return label;
    }

	public void addMissionControllPanel(MissionControllPanel missionControllPanel) {
		
		this.missionControllPanels.add(missionControllPanel);
		
		
			this.missionControllPanel = missionControllPanel;
			
			MenuItemSelectionListener menuItemSelectionListener = new MenuItemSelectionListener(missionControllPanels);
			
			if(firstCall){
				firstCall = false;
				
				
				for (JCheckBox overlayCheckBox : this.missionControllPanel.getAllOverLayCheckBoxes()) {
					
					JCheckBoxMenuItem newMenuItem = new JCheckBoxMenuItem(overlayCheckBox.getName().substring("overlay".length()));
					newMenuItem.setName(overlayCheckBox.getName());
					newMenuItem.addActionListener(menuItemSelectionListener);
	
					allOverlayCheckBoxes.add(newMenuItem);
					this.options.add(newMenuItem);
					
				}

				// sets initially active overlays to active
				for (String s : this.missionControllPanel.initiallyActiveGuiOverlays) {
					boolean found = false;
					for (JCheckBoxMenuItem overlayCheckBox : allOverlayCheckBoxes) {
						if (overlayCheckBox.getName().toLowerCase()
								.equals(s.toLowerCase())) {
							overlayCheckBox.doClick();
							found = true;
						}
					}
					if (!found) {
						throw new IllegalArgumentException(
								"Did not find an overlay attribute with name "
										+ s
										+ "! Maybe you misspelled it? Please check setName() in View");
					}
				}
		
			} else {
				
				for (JCheckBoxMenuItem newMenuItem : this.allOverlayCheckBoxes) {

					newMenuItem.addActionListener(menuItemSelectionListener);


					
				}
				
			}
					
	}
	public ArrayList<MissionControllPanel> getMissionControllPanels() {
		return missionControllPanels;
	}
	class MenuItemSelectionListener implements ActionListener {
		ArrayList<MissionControllPanel> missionControllPanelList;

		public MenuItemSelectionListener(ArrayList<MissionControllPanel> missionControllPanelList) {
			this.missionControllPanelList = missionControllPanelList;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem chkbx = (JCheckBoxMenuItem) e.getSource();
				String name = chkbx.getName();
				if (name.startsWith("overlay")) {
					for(MissionControllPanel missionControllTab : missionControllPanelList){
						missionControllTab.setOverlayActive(
								name.substring("overlay".length()),
								chkbx.isSelected());
					}
					
				}
			}
		}

	}
	public void toggleDebugMenu() {
		
		debug.setVisible(!debug.isVisible());
		if(debug.getComponentCount() != missionControllPanels.size()) {
			
			for(final MissionControllPanel mct : missionControllPanels) {

				
				JMenuItem newJM = new JMenuItem(mct.controller.agent.getName()
						+ "'s Brain");
				newJM.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						
							int screenWidth = (int) Toolkit.getDefaultToolkit()
									.getScreenSize().getWidth();
							int screenHeight = (int) Toolkit.getDefaultToolkit()
									.getScreenSize().getHeight();
							JFrame brainWindow = new JFrame(mct.controller.agent.getName()
									+ "'s Brain");
							int xPos = 0;
							int yPos = 0;
							switch (mct.controller.agent.getPlayerIndex()) {
							case 0:
								xPos = 0;
								yPos = 0;
								break;
							case 1:
								xPos = 0;
								yPos = screenWidth / 2;
								break;
							case 2:
								xPos = screenHeight / 2;
								yPos = 0;
								break;
							case 3:
								xPos = screenHeight / 2;
								yPos = screenWidth / 2;
								break;
							}
							brainWindow.setLocation(new Point(xPos, yPos));
							brainWindow
									.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							BrainTab brainTab = new BrainTab(mct.controller.agent
									.getBrain(), null);
							brainWindow.add(brainTab);
							brainWindow.setMinimumSize(new Dimension(screenWidth / 2,
									(screenHeight / 2) - 12));
							brainWindow.setVisible(true);
						}

					
				});
				debug.add(newJM);
			}
		}
		
		
	}

	

}
