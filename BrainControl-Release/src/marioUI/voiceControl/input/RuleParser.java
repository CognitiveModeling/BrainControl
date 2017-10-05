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

import javax.speech.recognition.GrammarException;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.RuleParse;

import com.sun.speech.engine.recognition.BaseRecognizer;
import com.sun.speech.engine.recognition.BaseRuleGrammar;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.result.Result;
import marioWorld.engine.Logging;

/**
 * Utility class to parse Grammar Rules according to recognition result and
 * extract tag string.
 */
public class RuleParser {
	
	// Grammar specifying rules
	private JSGFGrammar grammar;

	// JSAPI needed to parse rules.
	private BaseRecognizer jsapiRecognizer;

	// Numbers buffer
	private NumberParser num;
	private String cmd;

	public RuleParser(JSGFGrammar grammar) {

		this.grammar = grammar;
		this.num = new NumberParser();
		this.jsapiRecognizer = new BaseRecognizer(grammar.getGrammarManager());

		try {
			jsapiRecognizer.allocate();
		} catch (Exception e) {
			Logging.logWarning("Recognizer", "allocating jsapi recognizer failed in RuleParser");

		}
	}

	/**
	 * Gets a space delimited string of tags representing the result
	 * 
	 * @param result
	 *            the recognition result
	 * @return the tag string
	 * @throws GrammarException
	 *             if there is an error while parsing the result
	 */
	public String getTagString(Result result) throws GrammarException {
		RuleParse ruleParse = getRuleParse(result);
		return getTagStringStub(ruleParse);
	}

	public String getTagString(String cmd) throws GrammarException 
	{
		RuleParse ruleParse = getRuleParse(cmd);
		return getTagStringStub(ruleParse);
	}

	private String getTagStringStub(RuleParse ruleParse) {
		
		if (ruleParse == null) return null;

		String[] tagsArr = ruleParse.getTags();
		if (tagsArr == null) return "";

		String tags = "";

		for (String tag : tagsArr) tags += " " + tag;
			
		if (tags.contains("NUMBER")) tags += " NUMBER("+num.replaceNumbers(cmd)+")";
				
		return tags.trim();
		
	}

	/**
	 * Retrieves the rule parse for the given result
	 * 
	 * @param recognition
	 *            result
	 * @return the rule parse for the result
	 * @throws GrammarException
	 *             if there is an error while parsing the result
	 */
	private RuleParse getRuleParse(Result result) throws GrammarException {
		String resultText = result.getBestFinalResultNoFiller();
		return getRuleParse(resultText);
	}

	private RuleParse getRuleParse(String resultText) throws GrammarException {
		RuleGrammar ruleGrammar = new BaseRuleGrammar(jsapiRecognizer, grammar.getRuleGrammar());
		RuleParse ruleParse = ruleGrammar.parse(resultText, null);
		return ruleParse;
	}

}
