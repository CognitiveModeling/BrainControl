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
 * 72076 T�bingen 
 * cm-sekretariat -at- inf.uni-tuebingen.de
 ******************************************************************************/
package marioUI.voiceControl.input;

/**
 * base class representing a message from VoiceRecognitionInput sent to observers
 * 
 * @author Katrin
 *
 */
public class VoiceRecognitionMessage {
	
	public enum MessageType{COMMAND, READY, NO_RECOG_RESULT}
	
	private MessageType type;
	private String	command;
	private String tag;
	
	public VoiceRecognitionMessage(MessageType type){
		this.type = type;
	}
	
	public VoiceRecognitionMessage(MessageType type, String command, String tag){
		this.type = type;
		this.command = command;
		this.tag = tag;
	}

	public MessageType getType() {
		return type;
	}

	public String getCommand() {
		return command;
	}

	public String getTag() {
		return tag;
	}	
	
}
