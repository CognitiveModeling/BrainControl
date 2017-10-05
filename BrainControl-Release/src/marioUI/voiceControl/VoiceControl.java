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
package marioUI.voiceControl;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.logging.Level;

import marioAI.agents.CAEAgent;
import marioAI.agents.CAEAgentHook;
import marioAI.run.PlayHook;
import marioUI.gui.VoiceCommandsHistory;
import marioUI.gui.overlays.SpeechBubbleOverlay;
import marioUI.speechSynthesis.TextToSpeech;
import marioUI.speechSynthesis.VoiceResponse;
import marioUI.speechSynthesis.VoiceResponseType;
import marioUI.voiceControl.input.VoiceRecognitionInput;
import marioUI.voiceControl.input.VoiceRecognitionMessage;
import marioUI.voiceControl.interpretation.VoiceInterpreter;
import marioUI.voiceControl.interpretation.VoiceKnowledgeManager;
import marioWorld.agents.Agent;
import marioWorld.engine.Logging;
import marioWorld.engine.sprites.Player;

/**
 * Helper class to coordinates all voice objects
 * 
 * @author Katrin, Mihael
 * 
 */
public class VoiceControl extends Observable implements Observer {

	/**
	 * interprets tag string that rule parser computes
	 */
	private VoiceInterpreter interpreter;

	/**
	 * responsible for voice recognition and rule parsing
	 */
	@Deprecated
	private VoiceRecognitionInput input;
	
	private Vector<VoiceRecognitionInput> inputs;

	
	/**
	 * true if recognition thread is running and voice commands can be received
	 */
	private boolean recognitionStarted;

	/**
	 * saves recognised commands to display in GUI
	 */
	private VoiceCommandsHistory cmdsHistory;

	/**
	 * Init Text To Speech
	 */
	private TextToSpeech voiceOutput;

	private ArrayList<Agent> agents;
	public ArrayList<VoicePerson> voicePersons;

	/**
	 * global use voice button
	 */
	public static boolean USE_VOICE = true;

	/**
	 * VoiceKnowledgeManager
	 */
	public static VoiceKnowledgeManager voiceKnowledgeManager = new VoiceKnowledgeManager();

	/**
	 * default constructor, initialisations see init()
	 */
	public VoiceControl() 
	{		
	}

	/**
	 * Creates individual voice setups for all voice agents
	 */
	private void createVoicePersons(ArrayList<Agent> agents) 
	{
		voicePersons = new ArrayList<VoicePerson>();
		ArrayList<String> playerNames = new ArrayList<String>();

		// TODO: there could be some config file that loads individual speech
		// stuff!
		String[] defaultVoices = new String[] { "m4", "f1", "m2", "f2" };
		//String[] defaultIntros = new String[] { "Nice to meet you!", "I'm happy to meet you too!" };

		for (int i = 0; i < agents.size(); i++) {
			// only add speech capable agents to grammar (keyboard agent should for example not be controlled by voice)
			if (agents.get(i) instanceof CAEAgent)
				playerNames.add(agents.get(i).getName());
			voicePersons.add(new VoicePerson(defaultVoices[i % defaultVoices.length]/*, "Hello, I'm "
					+ agents.get(i).getName() + ". " + defaultIntros[i % defaultIntros.length], "What the heck?",
					"What do you mean?"*/));
		}

		//GrammarModifier gm = new GrammarModifier();
		//gm.addAgents("players", "PLACEHOLDER", playerNames);
		//gm = null;

	}

	private Thread r;

	/**
	 * Initialises all objects that need agent and planner, starts voice
	 * recognition thread
	 */
	public void init() 
	{
		// Add voice specific loggers
		Logging.addLogger("Interpreter", Level.INFO);
		Logging.addLogger("TagsToSentences", Level.INFO);
		Logging.addLogger("VoicePersons", Level.INFO);
		Logging.addLogger("VoiceRecognitionInput", Level.INFO);

		agents = PlayHook.agents;

		this.cmdsHistory = new VoiceCommandsHistory(agents);

		CAEAgentHook.view.addVoicePanel();

		this.recognitionStarted = false;

		// generic speech per player
		createVoicePersons(agents);

		this.interpreter = new VoiceInterpreter(agents, this);
		
		this.input = new VoiceRecognitionInput();
		this.input.addObserver(this);

		
		inputs = new Vector<VoiceRecognitionInput>();
			
		for (int i = 0; i < agents.size(); i++) {
			inputs.add(new VoiceRecognitionInput());
			inputs.lastElement().setMixer(((CAEAgent) agents.get(i)).microphone);
			inputs.lastElement().addObserver(this);
		}
			
		
		this.voiceOutput = new TextToSpeech();

		// starting of voice recognition thread
		r = new Thread(input);
		r.setPriority(Thread.MIN_PRIORITY);
		r.start();

	}
	
	public void interupt() {
		if (this.input.microphone != null) {
			this.input.stopRecording = true;
			this.input.microphone.stopRecognizing = true;
			this.input.microphone.clear();
		}
	}

	public void injectExeption() {
		input.blocker.injectInterrupt();
	}

	public void stopRecognition() {
		this.input.stopRecording = true;

		//hack needed because microphone needs to detect some noise to get out of the while where .recognize() method is called..
		this.display("ok quiting", 0);

		this.input.microphone.stopRecording();
		this.input.recognizer.deallocate();
		r.interrupt();
	}

	public VoiceCommandsHistory getCommandsHistory() {
		return this.cmdsHistory;
	}

	/**
	 * called by voice input to send voice recognition messages
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o == input) {		
			VoiceRecognitionMessage msg = (VoiceRecognitionMessage) arg;

			switch (msg.getType()) {
			case COMMAND:
				interpreter.processRecognitionResult(msg);
				break;
			case READY:
				for (int i = 0; i < voicePersons.size(); i++)
				{
					//display(voicePersons.get(i).introductionSentence, i);
					
					this.setChanged();
					this.notifyObservers(new VoiceResponse(VoiceResponseType.INTRODUCTION,"PLAYER("+PlayHook.agents.get(i).getName().toUpperCase()+")",i));
				}
				this.recognitionStarted = true;
				break;
			case NO_RECOG_RESULT:
				/*System.out.println("NO_RECOG_RESULT");
				int randomPlayer = new Random().nextInt(voicePersons.size());
				display(voicePersons.get(randomPlayer).noRecgResultSentence, randomPlayer);*/
				
				this.setChanged();
				this.notifyObservers(new VoiceResponse(VoiceResponseType.GOODBYE,0));												
				
				break;
			default:
				System.out.println("Undefined message type!");
			}
		}
	}

	/**
	 * Called by controller when receiving a written command via GUI has to be
	 * passed to input to get tag string
	 * 
	 * @param cmd
	 */
	public void processCommand(String cmd) {
		if (recognitionStarted && USE_VOICE)
			this.input.processCommand(cmd);
	}

	/**
	 * Same but passes tags directly
	 */
	public void processTags(String cmd) {
		if (recognitionStarted)
			this.input.processCommand(cmd, true);
	}

	/**
	 * Called by controller to (de)active giving commands via voice recognition
	 * or quit
	 */
	public void switchVoiceControlActivation() {
		this.input.switchVoiceControlActivation();
	}

	/**
	 * Called by controller to (de)active "is it true that ..." sentences or
	 * quit
	 */
	public void switchFeedbackSeeking() {
		this.interpreter.feedbackSeeking = !this.interpreter.feedbackSeeking;
	}

	/**
	 * Messages that will be displayed in the GUI
	 * 
	 * @param msg
	 *            text to display
	 * @param person
	 *            where should it be displayed
	 * @param useEspeak
	 *            should it be spoken
	 */
	@Deprecated
	public void display(String msg, int playerIndex, boolean useEspeak) 
	{
		// DBG
		//for ( StackTraceElement e : Thread.currentThread().getStackTrace()) {
		//	System.out.println(e.getClassName() + " "+ e.getMethodName() + " " + e.getLineNumber());
		//}

		String[] entry = new String[cmdsHistory.getColumnCount()];
		
		if (playerIndex >= -1) 
		{
			entry[playerIndex + 1] = msg;
			this.cmdsHistory.addNewEntry(entry);
			

		}

		Player player = playerIndex >= 0 ? this.agents.get(playerIndex).getPlayer() : null;
		
		if (useEspeak && USE_VOICE && playerIndex >= 0)
			voiceOutput.speak(msg, 1, voicePersons.get(playerIndex).voice, playerIndex, player);
		
		if ((playerIndex >= 0) && (agents != null) && (!USE_VOICE)) 
		{
			SpeechBubbleOverlay.setBubbleText(playerIndex, msg, player, 0, -(int)(player.getHeight()*1.5), true);
			//Sounds.play(Type.JUMP);
		}
	}

	
	public TextToSpeech getVoiceOutput() {
		return voiceOutput;
	}
	
	/**
	 * Same as display just that text will spoken by default
	 * 
	 * @param msg
	 * @param persons
	 */
	@Deprecated
	public void display(String msg, int playerIndex) {
		display(msg, playerIndex, true);
	}

	/**
	 * Same as display just that text should will spoken by default
	 * 
	 * @param msg
	 * @param persons
	 */
	@Deprecated
	public void display(String msg, Agent agent) {
		int playerIndex = this.agents.indexOf(agent);
		display(msg, playerIndex, true);
	}

	public VoiceInterpreter getInterpreter() {
		return interpreter;
	}

	public void stop() {
		this.input.killAll();

	}

	/*public void setReservoirActivation(boolean active) {
		if (active != CAEAgentHook.view.getVoicePanel().getVoiceTab().activateReservoirs.isSelected())
			CAEAgentHook.view.getVoicePanel().getVoiceTab().activateReservoirs.doClick();

	}*/
}
