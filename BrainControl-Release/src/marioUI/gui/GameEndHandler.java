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

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import mario.main.Settings;
import mario.main.StartJar.ExitCode;
import marioAI.run.PlayHook;
import marioAI.run.ReleaseGame;
import marioWorld.engine.sprites.Player;
import marioWorld.utils.ResourceStream;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Stack;

public class GameEndHandler {

	public static double startTimeInMsec = System.currentTimeMillis();
	private static final String TUTORIAL_FILE_PREFIX = ResourceStream.DIR_WORLD_RESOURCES+"/engine/mapedit/";

	private class ScoreFrame extends JFrame {
		private static final long serialVersionUID = 1L;
		private int score = 0;
		
		private ScoreFrame(LinkedList<String> epilogueLabelList){
			final LinkedList<String> epilogueLabels = epilogueLabelList;
			final JFrame frame = this;
			this.setLayout(new GridBagLayout());

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setVisible(false);
					frame.dispose();
					if(epilogueLabels.size()>0){
						new ScoreFrame(epilogueLabels);
					}
				}
			});
			
			JButton closeButton = new JButton("Ok");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
					if(epilogueLabels.size()>0){
						new ScoreFrame(epilogueLabels);
					}
					// ReleaseGame.stop(ExitCode.MENU);
				}
			});
			
			JLabel currentLabel = new JLabel(epilogueLabels.removeFirst());
			LayoutHelper.putGrid(this, currentLabel, 0, 0, 1, 1);
			LayoutHelper.putGrid(this, closeButton, 0, 1, 1, 1);
			currentLabel.setVisible(true);
			closeButton.setVisible(true);
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}

		public ScoreFrame(double timeInSec, int energys,
				boolean multiPlayerScore, String levelName) {
			final String level = levelName;
			final JFrame frame = this;
			this.setLayout(new GridBagLayout());

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setVisible(false);
					//ReleaseGame.stop(ExitCode.QUIT);
					frame.dispose();

				}
			});

			score = (int) ((Math.abs(1 / timeInSec) * 120000) + energys
					* ((multiPlayerScore) ? 50 : 100));
			String defaultScoreText = "LEVEL COMPLETED<br> " +
					 "You reached a score of " + score + ". <br> Congratulations!<br>" +
					 "Now go through the gate to enter the next level.</html>";
			JLabel scoreLabel;
			String filename = TUTORIAL_FILE_PREFIX+"levelScoreLabels.properties";
			
			final Properties properties = new Properties();
			try (InputStream stream = ResourceStream.getResourceStream(filename)) {
				properties.load(stream);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			switch (levelName) {
			case "releaseLevel0":
				scoreLabel = new JLabel(properties.getProperty("level0")+"<BR><BR>"+defaultScoreText);
				break;
			case "releaseLevel1":
				scoreLabel = new JLabel(properties.getProperty("level1")+"<BR><BR>"+defaultScoreText);
				break;
			case "releaseLevel2":
				scoreLabel = new JLabel(properties.getProperty("level2")+"<BR><BR>"+defaultScoreText);
				break;
			case "releaseLevel3":
				scoreLabel = new JLabel(properties.getProperty("level3")+"<BR><BR>"+defaultScoreText);
				break;
			case "releaseLevel4":
				scoreLabel = new JLabel("<html>"+defaultScoreText);
				break;
			default:
				scoreLabel = new JLabel("<html>"+defaultScoreText);
				break;
			}

			LayoutHelper.putGrid(this, scoreLabel, 0, 0, 1, 1);

			JButton closeButton = new JButton("Go on!");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
					if(level.equals("releaseLevel4")){
						String[] epilogueLabelArray = properties.getProperty("level4").split("#");
						final LinkedList<String> epilogueLabelList = new LinkedList<String>();
						for (int i=0; i < epilogueLabelArray.length; i++){
							epilogueLabelList.add(epilogueLabelArray[i]);
						}
						new ScoreFrame(epilogueLabelList);
					}
					// ReleaseGame.stop(ExitCode.MENU);
				}
			});
			LayoutHelper.putGrid(this, closeButton, 0, 1, 1, 1);

			closeButton.setVisible(true);
			scoreLabel.setVisible(true);
			this.pack();
			this.setVisible(true);
		}
	}
	
	
	private class LevelFinishFrame extends JFrame {
		private static final long serialVersionUID = 1L;

		// private int score = 0;
		public LevelFinishFrame() {

			final JFrame frame = this;
			this.setLayout(new GridBagLayout());

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setVisible(false);
					ReleaseGame.stop(ExitCode.QUIT);
					frame.dispose();
				}
			});
			JLabel levelFinishLabel = new JLabel("PERFECT!\n "
					+ "Now try to help the other robots complete the level!");
			JButton closeButton = new JButton("OK");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
				}
			});
			LayoutHelper.putGrid(this, levelFinishLabel, 0, 0, 1, 1);
			LayoutHelper.putGrid(this, closeButton, 0, 1, 1, 1);
			closeButton.setVisible(true);
			levelFinishLabel.setVisible(true);
			this.pack();
			this.setVisible(true);
		}
	}

	private class FailFrame extends JFrame {
		private static final long serialVersionUID = 1L;

		// private int score = 0;
		public FailFrame() {
			final JFrame frame = this;
			this.setLayout(new GridBagLayout());

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setVisible(false);
					ReleaseGame.stop(ExitCode.QUIT);
					frame.dispose();

				}
			});

			JLabel failLabel = new JLabel("GAME OVER\n "
					+ "You are out of energy!");
			LayoutHelper.putGrid(this, failLabel, 0, 0, 1, 1);

			JButton closeButton = new JButton("Back to main Menu");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
					ReleaseGame.stop(ExitCode.MENU);
				}
			});
			LayoutHelper.putGrid(this, closeButton, 0, 1, 1, 1);

			closeButton.setVisible(true);
			failLabel.setVisible(true);
			this.pack();
			this.setVisible(true);
		}
	}

	// private ScoreFrame sf;
	// private FailFrame ff;

	public void evaluate(boolean allPlayers, String levelName) {
		// this.activateNextLEvel();
		if (allPlayers) {
			this.evaluateScoreAllPlayersAtEndItem(levelName);
		} else {
			this.evaluateScoreSinglePlayerAtEndItem(levelName);
		}
	}

	// private void activateNextLEvel() {
	// //ArrayList<Boolean> oldActiveButtons =
	// ReleaseGame.controller.getModel().getActiveButtons();
	//
	// int one = 1;
	// for(String s : ReleaseGame.controller.getModel().getLevelnames()){
	// one++;
	// if(s.equals(ReleaseGame.controller.getModel().getLevelName())){
	// break;
	// }
	// }
	// int numberOfLevels = Settings.LEVEL_SETTINGS.length;
	// int zero = numberOfLevels - one;
	// if(one > numberOfLevels){
	// one = numberOfLevels;
	// zero =0;
	// }
	//
	// FileWriter output;
	// try {
	// output = new FileWriter(StartGuiModel.ACTIVE_BUTTONS_PATH);
	// output.write("");
	//
	// for(int i = 0; i < one; i++)
	// output.append("1");
	// for(int j = 0; j< zero; j++)
	// output.append("0");
	//
	// output.close();//stream safety
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }

	public static void stopWithoutEvaluation(ExitCode exitCode) {
		ReleaseGame.stop(exitCode);
	}

	private void evaluateScoreAllPlayersAtEndItem(String levelName) {
		double timeInSec = getTimeInSec();
		int energyCount = 0;
		for (Player p : PlayHook.players) {
			if (p.reachedEndItem()) {
				energyCount += p.getEnergys();
			} else {
				return;
			}
		}
		new ScoreFrame(timeInSec, energyCount, true, levelName);
	}

	private void evaluateScoreSinglePlayerAtEndItem(String levelName) {
		double timeInSec = getTimeInSec();
		int energyCount = 0;
		for (Player p : PlayHook.players) {
			if (p.reachedEndItem()) {
				energyCount += p.getEnergys();
				break;
			}
		}
		new ScoreFrame(timeInSec, energyCount, false, levelName);
	}

	private double getTimeInSec() {
		return (startTimeInMsec - System.currentTimeMillis()) / 1000;
	}

	public void evalGameFailed() {
		new FailFrame();
	}
	
	public void evalLevelFinished(){
		new LevelFinishFrame();
	}
}
