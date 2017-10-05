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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import marioAI.agents.CAEAgent;
import marioAI.goals.GoalPlanner;
import marioAI.goals.VoicePlanner;
import marioAI.run.PlayHook;
import marioWorld.agents.Agent;
import marioWorld.utils.ResourceStream;

public class TutorialFunction {

	
	private static TutorialFunction tutor = new TutorialFunction();
	private static AtomicLong nextAlarm;
	private static Thread timerThread; 

	private static final String TUTORIAL_FILE_PREFIX = ResourceStream.DIR_WORLD_RESOURCES+"/tutorial/";
	private static final String COMMENT_STRING = "//";
	private static final String FALLBACK_TEXT[] = new String[] { "I cannot help you any further!", "Please consult the manual" };
	private static final int PRIORITY = 1;
	private static final String VOICE = "f3";
	private static final long SLEEP_TIME = 300;
	
	
	public static TutorialFunction getTutor() {
		return tutor;
	}

	private JMenuItem button;
	private JButton userGuideButton; 
	private JCheckBoxMenuItem checkBox;
	private LinkedList<TutorialMessage> messages,activeMessages;  
	private int playerx,playery; 
	private AtomicReference<TutorialMessage> nextMessage;


	
	
	public TutorialFunction() {
		button = new JMenuItem("Help!");
		button.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				playNextActiveMessage();
			}
		});
		
			
		checkBox = new JCheckBoxMenuItem("AutoHelp");
		checkBox.setSelected(true);
		messages = new LinkedList<>();
		activeMessages = new LinkedList<>();
		nextMessage = new AtomicReference<>();
		playerx = 0;
		playery = 0;
		if (timerThread == null) {
			nextAlarm = new AtomicLong(0);
			timerThread = new Thread(new Runnable() {			
				@Override
				public void run() {
					timerLoop();
				}
			});
			timerThread.start();
		}
	}
	
	public void setLevel(String levelName) {
		String filename = TUTORIAL_FILE_PREFIX+levelName+".txt";
		InputStream stream = ResourceStream.getResourceStream(filename);
		if (stream == null) {
			System.err.println("No tutorial file found for current level: "+levelName);			
		} else {
			Scanner scanner = new Scanner(stream);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (!(line.isEmpty() || line.startsWith(COMMENT_STRING))) {
					messages.add(new TutorialMessage(line));
				}		
			}
			scanner.close();
			setNextMessage();
		}
	}
	
	public void playerIsAtPosition (int x, int y) {
		playerx = x;
		playery = y;
		checkForActiveMessages();
	}
	
	private void checkForActiveMessages() {
		Iterator<TutorialMessage> i = messages.iterator();
		int count = 0;
		while (i.hasNext()) {
			TutorialMessage message = i.next();
			if (message.isActive(playerx,playery)) {
				synchronized (activeMessages) {
					activeMessages.add(count,message);	// add new active messages in correct sequence at the beginning
				}
				count++;
				i.remove();
			} 
 		}
		i = activeMessages.iterator();
		while (i.hasNext()) {	
			TutorialMessage message = i.next();
			if (!message.isActive(playerx,playery)) {
				i.remove();
			} 
 		}
		if (nextAlarm.get() < 0) 
		setNextMessage(); 
	}
	
	private void setNextMessage() {
		synchronized (activeMessages) {
			nextAlarm.set(-1);
			if (activeMessages.isEmpty()) {
				TutorialMessage msg = nextMessage.get();
				if ((msg != null) && !msg.isActive(playerx,playery))
					nextMessage.set(null);
			} else {
				TutorialMessage next = activeMessages.peek();
				nextMessage.set(next);
				int time = next.getAutoActivationTime();
				if (time >= 0) 
					nextAlarm.set(System.currentTimeMillis() + time*1000);
			}	
		}
	}

	private void playNextActiveMessage() {
		TutorialMessage next = nextMessage.get();
		String[] text;
		if (next != null) {
			synchronized (activeMessages) {
				activeMessages.remove(next);
			}
			text = next.getText();
		} else {
			text = FALLBACK_TEXT;
		}
		stopPlayerEmptyReservoirs();
		for (String line : text) {
			PlayHook.voiceControl.getVoiceOutput().speak(line, PRIORITY, VOICE, -1, null);
		}		
		if (next != null)
			activeMessages.remove(next);
		setNextMessage();
	}

	private void stopPlayerEmptyReservoirs() {
		for (Agent agent : PlayHook.agents) {
			if (agent instanceof CAEAgent) {
				CAEAgent caeagent = (CAEAgent)agent;
				caeagent.getAStarSimulator().stopEverythingInAStarComputation();
				GoalPlanner planner = caeagent.getPlanner();
				((CAEAgent) agent).refundLastPayment();
				if (planner instanceof VoicePlanner) {
					VoicePlanner vp = (VoicePlanner)planner;
					vp.enableAllReservoirs(false);
				}
			}
		}
	}

	private void timerLoop() {
		while (true) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			long time = nextAlarm.get();
			if ((time > 0) && (System.currentTimeMillis() >= time)) {
				nextAlarm.set(-1);
				if (checkBox.isSelected()) 
					playNextActiveMessage();
			}
		}
	}

	public JMenuItem getHelpButton() {
		return button;
	}	

	public JCheckBoxMenuItem getAutoCheckBox() {
		return checkBox;
	}
	
}
