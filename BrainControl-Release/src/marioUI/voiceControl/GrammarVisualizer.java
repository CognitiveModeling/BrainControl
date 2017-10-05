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

import java.net.URL;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import marioUI.voiceControl.input.VoiceRecognitionInput;

/**
 * Class that dumps grammar tree into a GDL file which can be used visualize grammar structure
 * 
 * @author smihael
 * 
 */
public class GrammarVisualizer {

	public static void main(String[] args) {
		ConfigurationManager configurationManager = VoiceRecognitionInput.createConfigurationManager("voiceControl/input/input.config.xml");

		Recognizer recognizer = (Recognizer) configurationManager
				.lookup("recognizer");
		recognizer.allocate();
		
		JSGFGrammar grammar = (JSGFGrammar) configurationManager
				.lookup("jsgfGrammar");
		
		grammar.getInitialNode().dumpGDL("grammars.gdl");
	
		
	}

}
