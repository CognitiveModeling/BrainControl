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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import marioAI.agents.CAEAgent;
import marioAI.brain.Brain;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.movement.AStarSimulator;
import marioAI.util.GeneralUtil;
import marioWorld.engine.Art;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.Tickable;
import marioWorld.utils.ResourceStream;

/** 
 * This class builds the player control panel of the Gui for each Player
 * and it builds the voice gui
 * @author Marcel and Jonas
 * 
 * 
 */
public class PlayerPanel extends JPanel implements TableModelListener, Tickable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static boolean USE_VIEW = true;
	
	public int width;

	Brain brain; // Model in the MVC
	Controller controller; // Controller in the MVC

	public VoiceControlTab voiceTab;

	private MissionControllPanel missionControllTab;

	private BrainTab brainTab;
	
	
	

	private JLabel energyText;
	private HealthPanel healthInfo;
		
	private View view;

	public void setView(View view) {
		this.view = view;
	}



	/**
	 * Constructs the view and therefore initializes the GUI.
	 * 
	 * @param brain
	 *            Brain whose contents are displayed.
	 * @param agent
	 *            Agent that is observed.
	 */
	public PlayerPanel(Brain brain, CAEAgent agent, String[] initiallyActiveGuiOverlays) 
	{
		this.brain = brain;
		final int playerIndex = this.brain.getPlayerIndex();
		this.controller = new Controller(agent);

		GridBagLayout gridBagLayout = new GridBagLayout();
//		gridBagLayout.rowWeights = new double[] {};
//		gridBagLayout.columnWeights = new double[] {};
		this.setLayout(gridBagLayout);
		Image image = null;
		JLabel nameLabel = new JLabel(agent.getName());
		switch(agent.getName()) {
			case "Clark":
				image = Art.clark.getImage(0, 0);
				break;
			case "Jay":
				image = Art.jay.getImage(0, 0);
				break;
			case "Peter":
				image = Art.peter.getImage(0, 0);
				break;
			case "Bruce":
				image = Art.bruce.getImage(0, 0);
				break;
				
		}
		this.width = image.getWidth(null);
		JButton imageButton = new JButton(new ImageIcon(image));
		imageButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				view.changeVisiblePlayer(playerIndex);
				
				
			}
		});

        
        InputStream resource = ResourceStream
				.getResourceStream(ResourceStream.DIR_WORLD_RESOURCES
						+ "/engine/resources/item_energy.png");
		BufferedImage image3 = null;
		try {
			image3 = ImageIO.read(resource);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JLabel energyImage = new JLabel(new ImageIcon(image3));
		energyText = new JLabel(" " + controller.agent.getPlayer().getEnergys());

		JPanel energyInfo = new JPanel();
		energyInfo.add(nameLabel);
		energyInfo.add(energyImage);
		energyInfo.add(energyText);
		
		healthInfo = new HealthPanel();
		
		
		
//		LayoutHelper.putGrid(this, nameLabel, 0, 0, 1, 1);
//		LayoutHelper.putGrid(this, energyImage, 1, 0, 1, 1);
		LayoutHelper.putGrid(this, energyInfo, 0, 0, 1, 1);
		LayoutHelper.putGrid(this, healthInfo, 0, 1, 1, 1);
		LayoutHelper.putGrid(this, imageButton, 0, 2, 1, 1);

		// Tabs
//		JTabbedPane tabbedPane = new JTabbedPane();
//		LayoutHelper.putGrid(this, tabbedPane, 0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH);
		
		//create and add missionControl Gui to main Gui
		this.missionControllTab = new MissionControllPanel(initiallyActiveGuiOverlays, this.controller, playerIndex);

//		LayoutHelper.putGrid(this, missionControllTab, 0, 2, 1, 1);
//		tabbedPane.addTab("Mission Control", missionControllTab);
		
		agent.getAStarSimulator().addObserver(missionControllTab);
		agent.getPlanner().addObserver(missionControllTab);
		agent.addObserver(missionControllTab);
		
		//this.setColumnWidth(this.brainTab.getTable());
		this.setBorder(BorderFactory.createEtchedBorder());
		this.setVisible(true);
	}
	
	

	/**
	 * Sets the appropriate widths for the cells of the table.
	 * 
	 * @param table
	 *            JTable containing the brain.
	 */
	private void setColumnWidth(JTable table) {
		final int numDirections = CollisionDirection.values().length;
		int[] minimumWidths = new int[numDirections];
		int[] preferredWidths = new int[numDirections];
		int[] maximumWidths = new int[numDirections];
		for (int i = 0; i < numDirections; i++) {
			minimumWidths[i] = 50;
			preferredWidths[i] = 150;
			maximumWidths[i] = 1000;
		}
		minimumWidths = GeneralUtil.concatenate(new int[] { 50, 70 },
				minimumWidths);
		preferredWidths = GeneralUtil.concatenate(new int[] { 40, 200 },
				preferredWidths);
		maximumWidths = GeneralUtil.concatenate(new int[] { 50, 1000 },
				maximumWidths);

		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setMinWidth(minimumWidths[i]);
			column.setPreferredWidth(preferredWidths[i]);
			column.setMaxWidth(maximumWidths[i]);
		}
	}


	@Override
	public void onTick(LevelScene levelScene) {
		this.energyText.setText(" " + controller.agent.getPlayer().getEnergys());
		this.healthInfo.draw(controller.agent.getPlayer().getHealthLevel());
	}
	
	public void setSelected() {
	    this.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
	    
	}
	public void deselect() {
		this.setBorder(BorderFactory.createEtchedBorder());
	}

	/**
	 * Resizes the view when the content of the brain changes.
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		 this.setColumnWidth(this.brainTab.getTable());
	}
	
	public MissionControllPanel getMissionControllTab() {
		return missionControllTab;
	}

}
