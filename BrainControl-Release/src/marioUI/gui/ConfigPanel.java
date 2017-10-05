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

import java.awt.AlphaComposite;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import mario.main.Settings;
import marioAI.brain.Brain;
import marioUI.gui.microphoneVolumeControl.SoundDeviceGUI;
import marioWorld.engine.GlobalOptions;
import marioWorld.utils.ResourceStream;
import marioWorld.utils.UserDir;

/**
 * 
 * @author Jonas Einig
 *
 */
public class ConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JLabel titleLabel;

	private JButton startButton;
	private JButton closeButton;
	private JComboBox<String> resolution;
	public ArrayList<JButton> levelButtons;
	private JButton microPhone;
	private JButton clearButton;
	
	private StartGuiModel model;
	private StartGuiController controller;
	private JPanel buttonPanel;

	private JButton helpButton;
	
	private static enum PlayerNames {
		Bruce,
		Clark,
		Peter,
		Jay;
	}
	
	public ConfigPanel(StartGuiController _controller, StartGuiModel _model) {
		controller = _controller;
		model = _model;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] {};
		gridBagLayout.columnWeights = new double[] {};
		this.setLayout(gridBagLayout);

		titleLabel = new JLabel();
		titleLabel.setText("Brain Control!");
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font(titleLabel.getFont().getFontName(),
				Font.BOLD, 32));
		titleLabel.setVisible(true);
		LayoutHelper.putGrid(this, titleLabel, 0, 0, 2, 1, 1.0, 0.3);

		levelButtons = new ArrayList<JButton>();
		// Does not display Epilog
		for (int i = 0; i < Settings.LEVEL_SETTINGS.length-1; i++) 
		{
			final JButton button = new JButton();
			try 
			{
				button.setContentAreaFilled(false);

				String path = ResourceStream.DIR_WORLD_RESOURCES+"/engine/resources/releaseLevel" + i + ".png";
				Image img = ImageIO.read(ResourceStream.getResourceStream(path));
				img = createResizedCopy(img,300, 150, true);


				button.setIcon(new ImageIcon(img));
			} 
			catch (IOException ex) 
			{
				System.err.println(ex.getMessage());
			}
			
			if (button != null) 
			{
				final int index = i;
				button.addActionListener(new ActionListener() 
				{
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						if (e.getSource() == button)
						{							
							String levelName = Settings.LEVEL_SETTINGS[index].name;
							controller.setLevelName(levelName, index);
							controller.setPlayers(Settings.getPlayerIDs(levelName));								
							startButton.setEnabled(true);
						}
					}
				});
				this.levelButtons.add(button);
			}
		}

		buttonPanel = new JPanel(new GridBagLayout());
		GridBagLayout gBL = new GridBagLayout();
		gBL.rowWeights = new double[] {};
		gBL.columnWeights = new double[] {};
		//		buttonPanel.setLayout(gBL);
		activateLevelButtons();
		for (int i = 0; i < levelButtons.size(); i++) {
			levelButtons.get(i).setVisible(true);

			if (i % 4 == 0) {
				LayoutHelper.putGrid(buttonPanel, levelButtons.get(i), 0,
						(int) (i /4), 1, 1, 1.0, 1.0);
			} else if (i % 4 == 1) {
				LayoutHelper.putGrid(buttonPanel, levelButtons.get(i), 1,
						(int) (i / 4), 1, 1, 1.0, 1.0);
			} else if (i % 4 == 2){
				LayoutHelper.putGrid(buttonPanel, levelButtons.get(i), 2,
						(int) (i / 4), 1, 1, 1.0, 1.0);
			} else {
				LayoutHelper.putGrid(buttonPanel, levelButtons.get(i), 3,
						(int) (i / 4), 1, 1, 1.0, 1.0);
			}
		}
		LayoutHelper.putGrid(this, buttonPanel, 0, 1, 1, 1, 2.0, 1.0);

		
		
		
		this.helpButton = new JButton("User Guide (pdf)");
		this.helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File helpFile = new File(ResourceStream.DIR_RESOURCE+"/userguide.pdf");
				try {
					Desktop.getDesktop().open(helpFile);
				} catch (IOException e1) {	}
			}
		});
		this.helpButton.setMaximumSize(new Dimension(200, 40));
		this.helpButton.setVisible(true);
		
		
		
		
		
		this.startButton = new JButton("Start Game");
		this.startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				controller.startNewLevel(model.getLevelName(), model.getLevelIndex());

			}
		});
		this.startButton.setMaximumSize(new Dimension(200, 40));
		this.startButton.setVisible(true);
		startButton.setEnabled(false);

		
		
		
		String[] resolutionStrings = { "640x480", "960x720", "1280x960" };
		this.resolution = new JComboBox<String>(resolutionStrings);
		this.resolution.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				 JComboBox<String> cb = (JComboBox<String>)e.getSource();
				 GlobalOptions.setResolution( cb.getSelectedIndex()+2);
				
			}
		});
		
		
		final JPanel panel = this;
		this.closeButton = new JButton("Close Game");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(
						panel, "Are you sure to exit the game?");
				if( result==JOptionPane.OK_OPTION){
					// NOW we change it to dispose on close..
					System.exit(2); // exit code 2 as signal to terminate surrounding jar-calling loop
				}
			}
		});
		
		this.microPhone = new JButton("Calibrate Microphone");
		microPhone.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SoundDeviceGUI.start();
				
			}
		});
		
// clear button for brain tab
		
		clearButton = new JButton("Reset Knowledge");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				int result = JOptionPane.showConfirmDialog(
						panel, "Are you sure you want to reset the game?");
				if( result==JOptionPane.OK_OPTION){
					for(PlayerNames name : PlayerNames.values()) {
						StringBuilder path = new StringBuilder(ResourceStream.DIR_AI_RESOURCES+"/brain/brain");
						path.append(name.toString());
						path.append(".ser");
						Brain.deleteKnowledgeFile(UserDir.getUserFile(path.toString()));
					}
					
				}
				
				
			}
		});
	

		JPanel startPanel = new JPanel();
		startPanel.add(startButton);
		startPanel.add(helpButton);
		startPanel.add(resolution);
		
		startPanel.add(microPhone);
		startPanel.add(clearButton);
		startPanel.add(closeButton);
		LayoutHelper.putGrid(this, startPanel, 0, 2, 1, 1, 1.0, 1.0);

		this.setVisible(true);
	}

	public void activateLevelButtons() {
		ArrayList<Boolean> buttonIsActive = model.getActiveButtons();
		for (int i = 0; i < levelButtons.size(); i++) {
			levelButtons.get(i).setEnabled(buttonIsActive.get(i));
		}
	}

	private BufferedImage createResizedCopy(Image originalImage, 
			int scaledWidth, int scaledHeight, 
			boolean preserveAlpha) {
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
		g.dispose();
		return scaledBI;
	}


}
