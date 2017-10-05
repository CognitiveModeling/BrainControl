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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import marioUI.gui.StartGuiModel.KnowledgeTypes;
import marioUI.gui.StartGuiModel.PlannerTypes;
import marioUI.gui.StartGuiModel.PlayerTypes;
import marioUI.voiceControl.input.Microphone;
import marioWorld.engine.GlobalOptions;

public class AgentPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 614673895077062769L;
	private AgentConfiguration agentConfiguration;
	//private StartGuiController controller;
	//private StartGuiModel model;
	private StartGuiView view;

	private JComboBox<PlayerTypes> playerComboBox;
	private JComboBox<PlannerTypes> plannerComboBox;
	private JComboBox<KnowledgeTypes> knowledgeComboBox;
	private JComboBox<String> microphoneComboBox;
	private JTextField agentNameField;
	private JLabel nameLabel;
	private JLabel plannerLabel;
	private JLabel figureLabel;
	private JLabel knowledgeLabel;
	private JLabel microphoneLabel;
	private JPanel content;

	public AgentPanel(StartGuiView _view, int idx) 
	{
		//this.controller = _controller;
		view = _view;
		//model = _model;
		content = new JPanel();
		agentConfiguration = new AgentConfiguration(idx);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] {};
		gridBagLayout.columnWeights = new double[] {};
		content.setLayout(gridBagLayout);
		content.setPreferredSize(new Dimension(view.getWidth() / 5, GlobalOptions.ControlPanelHeight));
		content.setMaximumSize(new Dimension(view.getWidth() / 5, GlobalOptions.ControlPanelHeight));

		nameLabel = new JLabel("Name of Player " + (agentConfiguration.getIndex()) + ": ");
		LayoutHelper.putGrid(content, nameLabel, 0, 1, 1, 1, 0.3, 1.0);

		agentNameField = new JTextField(agentConfiguration.getPlayerName());
		agentNameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == agentNameField) {
					agentConfiguration.setPlayerName(agentNameField.getText());
				}
			}
		});
		agentNameField.setVisible(true);
		LayoutHelper.putGrid(content, agentNameField, 1, 1, 1, 1, 1.0, 1.0);

		figureLabel = new JLabel("Player Figure");
		LayoutHelper.putGrid(content, figureLabel, 0, 2, 1, 1, 0.3, 1.0);
		playerComboBox = new JComboBox<PlayerTypes>(StartGuiModel.PlayerTypes.values());
		playerComboBox.setSelectedIndex(idx);
		playerComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == playerComboBox) {
					agentConfiguration.setPlayerType(playerComboBox.getSelectedIndex());
				}
			}
		});
		playerComboBox.setMaximumSize(new Dimension(200, 40));
		playerComboBox.setVisible(true);
		LayoutHelper.putGrid(content, playerComboBox, 1, 2, 1, 1, 1.0, 1.0);		

		plannerLabel = new JLabel("Controler Type");
		LayoutHelper.putGrid(content, plannerLabel, 0, 3, 1, 1, 0.3, 1.0);
		plannerComboBox = new JComboBox<PlannerTypes>(StartGuiModel.PlannerTypes.values());
		plannerComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == plannerComboBox) 
				{
					/*if (plannerComboBox.getSelectedItem() == StartGuiModel.PlannerTypes.MOUSE) 
					{
						for (AgentConfiguration ac : view.getAllAgentConfigurations()) {
							if (ac.getPlannerType() == StartGuiModel.PlannerTypes.MOUSE) {
								plannerComboBox.setSelectedItem(StartGuiModel.PlannerTypes.VOICE);
								new MarioError("You can only choose one mouse controlled player.");
							}
						}
					}*/
					agentConfiguration.setPlannerType(plannerComboBox.getSelectedIndex());
					updateMicrophone();
				}
			}
		});
		plannerComboBox.setMaximumSize(new Dimension(200, 40));
		plannerComboBox.setVisible(true);
		LayoutHelper.putGrid(content, plannerComboBox, 1, 3, 1, 1, 1.0, 1.0);

		knowledgeLabel = new JLabel("Initial knowledge");
		LayoutHelper.putGrid(content, knowledgeLabel, 0, 4, 1, 1, 0.3, 1.0);
		knowledgeComboBox = new JComboBox<KnowledgeTypes>(StartGuiModel.KnowledgeTypes.values());
		knowledgeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == knowledgeComboBox) {
					/*if (plannerComboBox.getSelectedItem() == StartGuiModel.PlannerTypes.MOUSE) 
					{
						if (knowledgeComboBox.getSelectedItem() != StartGuiModel.KnowledgeTypes.NONE) 
						{
							knowledgeComboBox.setSelectedItem(StartGuiModel.KnowledgeTypes.NONE);
							new MarioError("The Player, that is directed with the mouse, should not have an initial knowledge");
						}
					}*/
					agentConfiguration.setKnowledgeType(knowledgeComboBox.getSelectedIndex());
				}
			}
		});
		knowledgeComboBox.setMaximumSize(new Dimension(200, 40));
		knowledgeComboBox.setVisible(true);
		LayoutHelper.putGrid(content, knowledgeComboBox, 1, 4, 1, 1, 1.0, 1.0);

		
		microphoneLabel = new JLabel("Input device: ");
		microphoneLabel.setVisible(true);
		LayoutHelper.putGrid(content, microphoneLabel, 0, 5, 1, 1, 0.3, 1.0);

		microphoneComboBox = new JComboBox<String>(getMicrophones());
		microphoneComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == microphoneComboBox) {
					agentConfiguration.setMicrophone(microphoneComboBox.getSelectedItem().toString());
				}
			}
		});
		microphoneComboBox.setMaximumSize(new Dimension(200, 40));
		microphoneComboBox.setVisible(true);
		LayoutHelper.putGrid(content, microphoneComboBox, 1, 5, 1, 1, 1.0, 1.0);
		updateMicrophone();

		
		//RELEASE: disable all these buttons, there is not need to change it... except for microphone maybe
		playerComboBox.setVisible(false);
		playerComboBox.setVisible(false);
		knowledgeComboBox.setVisible(false);
		plannerComboBox.setVisible(false);
		agentNameField.setEditable(false);
		plannerLabel.setVisible(false);
		figureLabel.setVisible(false);
		knowledgeLabel.setVisible(false);
		
		
		
		this.add(content);
	}

	private String[] getMicrophones() {
		return Microphone.enumerateMicrophones().keySet().toArray(new String[0]);
	}

	private void updateMicrophone() {
		boolean isVoiceAgent = (plannerComboBox.getSelectedItem() == StartGuiModel.PlannerTypes.VOICE);
		microphoneComboBox.setEnabled(isVoiceAgent);
		microphoneLabel.setEnabled(isVoiceAgent);
	}

	public AgentConfiguration getAgentConfiguration() {
		refreshNameConfig();
		return this.agentConfiguration;
	}

	private void refreshNameConfig() {
		agentConfiguration.setPlayerName(agentNameField.getText());
	}

}
