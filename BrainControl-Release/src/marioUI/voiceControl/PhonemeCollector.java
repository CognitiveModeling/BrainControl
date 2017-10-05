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
package marioUI.voiceControl;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.linguist.dictionary.Dictionary;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import marioUI.voiceControl.input.VoiceRecognitionInput;

/**
 * Class looking up possible voice commands and their phoneme representation,
 * writing this to commandsAndPhonemes.txt
 * 
 * The main method needs to be called once each time after changing the input grammar.
 * 
 * @author Katrin, smihael
 * 
 */
public class PhonemeCollector {

	public static void main(String[] args) {

		ArrayList<String[]> commandsAndPhonemes = getVoiceCommandsAndPhonemes();

		BufferedWriter writer;

		try {
			// overwrites existing file
			writer = new BufferedWriter(new FileWriter(
					"commandsAndPhonemes.txt", false));
			for (String[] commandAndPhoneme : commandsAndPhonemes) {
				// writes command and phoneme representation separated by tab
				String line = commandAndPhoneme[0] + "\t"
						+ commandAndPhoneme[1] + "\n";
				System.out.println(line);
				
				writer.write(line);
			}

			writer.close();

		} catch (FileNotFoundException f) {
			System.out.println(f.getLocalizedMessage());
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	/**
	 * Allocates recognizer, grammar and dictionary, gets (random) voice
	 * commands, looks up phoneme representation in the dictionary
	 * 
	 * @return
	 */
	private static ArrayList<String[]> getVoiceCommandsAndPhonemes() {

		ConfigurationManager configurationManager = VoiceRecognitionInput.createConfigurationManager("voiceControl/input/input.config.xml");

		Recognizer recognizer = (Recognizer) configurationManager
				.lookup("recognizer");
		recognizer.allocate();

		JSGFGrammar grammar = (JSGFGrammar) configurationManager
				.lookup("jsgfGrammar");
		Dictionary dictionary = (Dictionary) configurationManager
				.lookup("dictionary");

		// Get possible voice commands
		ArrayList<String> cmds = new ArrayList<String>();
		for (int i = 0; i < 80; i++) {
			String s = grammar.getRandomSentence();
			if (!cmds.contains(s)) {
				cmds.add(s);
			}
		}

		// Get phoneme representation for every command
		ArrayList<String[]> commandWithPhonemeRepresentation = new ArrayList<String[]>();

		for (String cmd : cmds) {
			StringBuilder sb = new StringBuilder();

			String[] words = cmd.split(" ");
			for (String word : words) {
				String[] wordPhon = dictionary.getWord(word)
						.getMostLikelyPronunciation().toString()
						.split("\\(|\\)");
				sb.append(arpabetToIPA(wordPhon[1]) + " ");
			}

			commandWithPhonemeRepresentation.add(new String[] { cmd,
					sb.toString() });
		}

		recognizer.deallocate();

		return commandWithPhonemeRepresentation;
	}

	/**
	 * Converts an Arpabet phonemic transcription to an IPA phonemic
	 * transcription.
	 * 
	 * Modified from code by Ubiquitous Knowledge Processing (UKP) Lab licensed
	 * under the Apache License, Version 2.0 available from: http://goo.gl/2C0NKj
	 * 
	 * @param s
	 *            The Arpabet phonemic transcription to convert.
	 * @return The IPA equivalent of s.
	 * 
	 */
	public static String arpabetToIPA(String s) {
		String[] arpaPhonemes = s.trim().split("[ \\t]+");
		StringBuffer ipaPhonemes = new StringBuffer(s.length());

		for (String arpaPhoneme : arpaPhonemes) {
			char stressChar = arpaPhoneme.charAt(arpaPhoneme.length() - 1);
			if (stressChar == '0' || stressChar == '1' || stressChar == '2') {
				arpaPhoneme = arpaPhoneme
						.substring(0, arpaPhoneme.length() - 1);
				ipaPhonemes.append(arpabetToIPAMap.get(Character
						.toString(stressChar)));
			}

			String ipaPhoneme = arpabetToIPAMap.get(arpaPhoneme);
			if (ipaPhoneme == null)
				System.out.println("couldn't map to IPA");

			ipaPhonemes.append(ipaPhoneme);
		}
		return ipaPhonemes.toString();
	}

	private final static Map<String, String> arpabetToIPAMap;
	static {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put("0", "");
		aMap.put("1", "ˈ");
		aMap.put("2", "ˌ");
		aMap.put("AA", "ɑ");
		aMap.put("AE", "æ");
		aMap.put("AH", "ʌ");
		aMap.put("AO", "ɔ");
		aMap.put("AW", "aʊ");
		aMap.put("AX", "ə");
		aMap.put("AY", "aɪ");
		aMap.put("B", "b");
		aMap.put("CH", "tʃ");
		aMap.put("D", "d");
		aMap.put("DH", "ð");
		aMap.put("DX", "?");
		aMap.put("EH", "ɛ");
		aMap.put("ER", "ɚ");
		aMap.put("EY", "eɪ");
		aMap.put("F", "f");
		aMap.put("G", "?");
		aMap.put("HH", "h");
		aMap.put("IH", "ɪ");
		aMap.put("IY", "i");
		aMap.put("JH", "dʒ");
		aMap.put("K", "k");
		aMap.put("L", "l");
		aMap.put("M", "m");
		aMap.put("NG", "ŋ");
		aMap.put("N", "n");
		aMap.put("OW", "oʊ");
		aMap.put("OY", "ɔɪ");
		aMap.put("P", "p");
		aMap.put("R", "ɹ");
		aMap.put("SH", "ʃ");
		aMap.put("S", "s");
		aMap.put("TH", "θ");
		aMap.put("T", "t");
		aMap.put("UH", "ʊ");
		aMap.put("UW", "u");
		aMap.put("V", "v");
		aMap.put("W", "w");
		aMap.put("Y", "j");
		aMap.put("ZH", "ʒ");
		aMap.put("Z", "z");
		arpabetToIPAMap = Collections.unmodifiableMap(aMap);
	}
}
