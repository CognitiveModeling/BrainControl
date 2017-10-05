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
package marioUI.voiceControl.input;

import java.net.URL;
import java.util.Observable;

import javax.sound.midi.SysexMessage;
import javax.speech.recognition.GrammarException;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import marioUI.gui.overlays.MessageOverlay;
import marioUI.voiceControl.input.VoiceRecognitionMessage.MessageType;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.Logging;
import marioWorld.utils.ResourceStream;

/**
 * Responsible for setting up and starting recognition process.
 * 
 * @author Katrin, Mihael
 * 
 */

public class VoiceRecognitionInput extends Observable implements Runnable {

	/**
	 * Configuration manager is needed to create parser, recognizer, microphone,
	 * grammar according to properties in input.config.xml
	 * 
	 * -> see sphinx documentation for more information
	 */
	private ConfigurationManager configurationManager;

	private RuleParser parser;
	public Recognizer recognizer;
	public Microphone microphone;
	public MicrophoneBlocker blocker;
	public boolean stopRecording;
	private JSGFGrammar grammar;

	/**
	 * flag to set voice commands active or inactive
	 */
	private boolean voiceControlActive;

	private boolean stopped = false;

	private String mixer = "default";

	public VoiceRecognitionInput() {
		this.configurationManager = createConfigurationManager("voiceControl/input/input.config.xml");
	}

	public static ConfigurationManager createConfigurationManager(String xmlConfigFile) {
		String filename = ResourceStream.DIR_RESOURCE+'/'+ResourceStream.DIR_UI_RESOURCES+"/"+xmlConfigFile;
		return new ConfigurationManager(filename);
	}

	public void setMixer(String mixer) {
		this.mixer = mixer;
	}

	/**
	 * Called when recognition thread starts
	 */
	@Override
	public void run() {

		if (!this.stopped) {
			try {
				this.recognizer = (Recognizer) configurationManager.lookup("recognizer");
				this.recognizer.allocate();

				this.microphone = (Microphone) configurationManager.lookup("microphone");

				if (microphone == null) {
					System.err.println("No microphone configuration found");
				}

				if (microphone != null && !mixer.equals("default")) {
					PropertySheet ps = configurationManager.getPropertySheet("microphone");
					ps.setString(Microphone.PROP_SELECT_MIXER, mixer); 
					microphone.newProperties(ps);
				}

				this.blocker = (MicrophoneBlocker) configurationManager.lookup("microphoneBlocker");

				this.grammar = (JSGFGrammar) configurationManager.lookup("jsgfGrammar");

				this.parser = new RuleParser(grammar);

				this.voiceControlActive = true;

				go();

				//this.recognizer.deallocate();

			} catch (PropertyException e) {
				error("Problem configuring: " + e);
			} catch (JSGFGrammarParseException parse) {
				error("problem parsing ");
			} catch (JSGFGrammarException grammar) {
				error("Grammar exception");
			}
		}
	}

	public Recognizer getRecognizer() {
		return recognizer;
	}

	public Microphone getMicrophone() {
		return microphone;
	}

	private void go() throws JSGFGrammarParseException, JSGFGrammarException {
		if (microphone.startRecording()) {

			VoiceRecognitionMessage msgReady = new VoiceRecognitionMessage(MessageType.READY);
			informVoiceControl(msgReady);

			//while (this.voiceControlActive/* && GlobalOptions.getGameWorld().isRunning()*/)
			while (GlobalOptions.getGameWorld().isRunning())
			{
				if (voiceControlActive) {
					if (microphone.isRecording()) 
					{					
						Result result = recognizer.recognize();

						if (result != null) 
						{
							processCommand(result.getBestFinalResultNoFiller());
						} 
						else 
						{
							VoiceRecognitionMessage msg = new VoiceRecognitionMessage(MessageType.NO_RECOG_RESULT);
							informVoiceControl(msg);
						}
					} 
					else 
					{
						System.err.println("Microphone recording stopped!");
						break;
					}
				} else {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} 
		}
		else 
		{
			error("Can't start the microphone");
		}
	}

	/**
	 * Called when receiving a command via voice recognition or gui looks up
	 * tags in grammar
	 * 
	 * @param recognized
	 *            voice command, options[0] is true when tags are passed
	 *            directly
	 */
	public void processCommand(String cmd, boolean... options) {

		if (options.length == 0) {
			if (/*voiceControlActive &&*/ cmd != null && cmd != "") 
			{
				//Logging.logInfo("VoiceRecognitionInput", "Trying to parse tags from \"" + cmd + "\"");

				String tag = "";
				try 
				{
					tag = parser.getTagString(cmd);
				} catch (GrammarException e) {
					error("Grammar exception, could not get tags!");
				}

				if (tag != null && !tag.equals("")) 
				{
					System.out.println("Voice recognition got tags: " + tag);
					VoiceRecognitionMessage recognized = new VoiceRecognitionMessage(MessageType.COMMAND, cmd, tag);
					informVoiceControl(recognized);
				} else {
					MessageOverlay.setBoxText("Sentence was not found in grammar", true);
					Logging.logInfo("VoiceRecognitionInput", "Sentence was not found in grammar");
				}
			}
		} else {
			if (options[0] && cmd != null && cmd != "") {
				// always interpret tags
				VoiceRecognitionMessage recognized = new VoiceRecognitionMessage(MessageType.COMMAND, cmd, cmd);
				informVoiceControl(recognized);
			}
		}
	}

	/**
	 * Passes message object to voice control
	 * 
	 * @param msg
	 */
	private void informVoiceControl(VoiceRecognitionMessage msg) {
		if (msg != null) {
			this.setChanged();
			this.notifyObservers(msg);
		}
	}

	/**
	 * Logs an error message
	 * 
	 * @param error
	 *            message
	 */
	private void error(String err) {
		Logging.logWarning("Recognizer", err);
	}

	/**
	 * Switches voice control activation, if inactive voice commands won't be
	 * passed to voice control
	 */
	public void switchVoiceControlActivation() {
		/* Führt zu Bugs und Fehlermeldungen, vorerst auskommentiert
		stopped = !stopped;
		System.out.println(microphone == null);
		if(microphone.isRecording()){
			microphone.stopRecording();
		} else {
			microphone.startRecording();
		}
		*/
		voiceControlActive = !voiceControlActive;
		//System.out.println("Voice control now: "+voiceControlActive);
	}

	public void stop() throws NullPointerException {
		//THREAD added some stop interaction CALLED BY VOICE CONTROL
		this.voiceControlActive = false;
		this.stopped = true;
		configurationManager.removeConfigurable("microphone");
		microphone.stopRecording();
		configurationManager.removeConfigurable("jsgfGrammar");
		grammar.deallocate();
		configurationManager.removeConfigurable("recognizer");
		recognizer.deallocate();
	}

	public void killAll() {

		voiceControlActive = false;
		this.stopped = true;

		microphone.stopRecording();
		microphone.clear();		

		microphone.utteranceEndReached = true;

		//TODO: dirty: wait for recognizer to give up....
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		recognizer.deallocate();

	}

}
