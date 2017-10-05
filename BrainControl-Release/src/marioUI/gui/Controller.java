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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTextField;

import junit.framework.TestFailure;
import marioAI.agents.CAEAgent;
import marioAI.goals.Goal;
import marioAI.run.PlayHook;
import marioWorld.agents.Agent;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * Controller in the MVC pattern. Reacts on user input and changes model
 * accordingly.
 * 
 * @author Volker, Jonas, Katrin, Mihael
 * 
 */
public class Controller implements ActionListener, MouseListener {

	// model of MVC
	public final CAEAgent agent;

	private VoicePanel voicePanel;

	public void setVoicePanel(VoicePanel _voicePanel)
	{
		voicePanel = _voicePanel;
	}
	
	/**
	 * Controller constructor for an agent / playerpanel
	 */
	public Controller(Agent agent) 
	{
		this.agent = agent instanceof CAEAgent ? (CAEAgent) agent : null;
	}
	
	/*
	 * Controller constructor for a voicePanel
	 * */
	public Controller()
	{
		this.agent = null;
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		/*
		 * set goal by written command or position
		 * this event is associated to the VoicePanel
		 * */
		if (e.getSource() instanceof JTextField) 
		{
			//System.out.println("Got VoicePanel GUI Event");
			
			JTextField textField = (JTextField) e.getSource();

			if (voicePanel.getVoiceTab().isWrittenCommandsTextField(textField)) {
				passWrittenCommand(textField.getText());
				textField.setText("");
				((VoiceControlTab)textField.getParent().getParent()).resetHistoryPosition();
			} else if (voicePanel.getVoiceTab().isTagsTextField(textField)) {
				passTags(textField.getText());
				textField.setText("");
			} /*else {

				String[] xy = textField.getText().split(",", 2);
				GlobalContinuous goalPosition = new GlobalContinuous(Float.parseFloat(xy[0]), Float.parseFloat(xy[1]));

				this.agent.getAStarSimulator().setGoal(agent.getPlanner().getLevelSceneAdapter(), new Goal(goalPosition.toGlobalCoarse(),false), false);
			}*/
			
			return;
		}

		/*
		 * this event is associated to any PlayerPanel
		 * */
		if (e.getSource() instanceof JCheckBoxMenuItem && agent!=null) 
		{
			//System.out.println("Got PlayerPanel GUI Event");
			
			JCheckBoxMenuItem chkbx = (JCheckBoxMenuItem) e.getSource();
			
			/*if (voicePanel.getVoiceTab().isActivateReservoirCheckbox(chkbx))	{	
				((VoicePlanner) agent.getPlanner()).switchReservoirActivation();
				((VoicePlanner) agent.getPlanner()).computeAndSetNewGoal();
			}
			else */if(voicePanel.getVoiceTab().isActivateRecognitionCheckbox(chkbx))	{
				PlayHook.voiceControl.switchVoiceControlActivation();
			}
			else if(voicePanel.getVoiceTab().isActivateInsecurityCheckbox(chkbx))	{
				PlayHook.voiceControl.switchFeedbackSeeking();
			}
			
			return;
		}

	}

	/**
	 * Passes a command to command Interpreter
	 * 
	 * @author Katrin
	 * @param cmd that should be processed by voice control
	 */
	private void passWrittenCommand(String cmd) {
		if (cmd != null && cmd != "") {
			PlayHook.voiceControl.processCommand(cmd);
		}
	}
	
	/**
	 * Passes a command to command Interpreter
	 * 
	 * @author Mihael
	 * @param tags that should be processed by voice control
	 */
	private void passTags(String cmd) {
		if (cmd != null && cmd != "") {
			PlayHook.voiceControl.processTags(cmd);
		}
	}
	
	
	/**
	 * Copies selected voice commands from possible voice commands table
	 * to written command text field
	 * 
	 * @author Katrin
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}
}
