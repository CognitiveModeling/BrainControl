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
package marioUI.speechSynthesis;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.linguist.dictionary.Word;
import edu.cmu.sphinx.linguist.language.grammar.GrammarArc;
import edu.cmu.sphinx.linguist.language.grammar.GrammarNode;

/**
 * Class to deterministically generate all sentences allowed by a given
 * JSGFGrammar
 * 
 * @author smihael
 * 
 */

public class GrammarTraversal {

	/* statistics to make the progress more "interesting" */
	private int count_utterances = 0;	
	private Timer timer = new Timer();
	
	class PrintStatistics extends TimerTask {
	    public void run() {
	       System.out.println(count_utterances + " sentences parsed."); 
	    }
	 }	
	
	/* The list with the generated sentences */
	private List<Sentence> sentences;
	private JSGFGrammar grammar;

	/**
	 * Configures Sphinx so that the mapping procedure can be started. This
	 * should only be done once and not every single time when a particular
	 * response is being queried in the tags to utterance mapping
	 */

	public GrammarTraversal(JSGFGrammar g) {
		grammar    = g;
		sentences  = new ArrayList<Sentence>();
		// start traversing with an empty sentence
		timer.schedule(new PrintStatistics(), 0, 500);
		traverse(grammar.getInitialNode(), "", "");		
		timer.cancel();
		timer.purge();
		System.out.println(count_utterances + " sentences parsed.");
	}
	
	/**
	 * @return the sentences
	 */
	public List<Sentence> getSentences() {
		return sentences;
	}


	/**
	 * Traverse the tree structure recursively (DFS)
	 * 
	 * @param node
	 *            The current node
	 * @param sentence
	 *            The spelling of the sentence
	 * @param path
	 *            The path of the sentence (without fillers)
	 */
	private void traverse(GrammarNode node, String sentence, String path) {

		if (!node.isEmpty()) {

			Word word = node.getWord();

			if (word != null)
				if (!word.isFiller())
					sentence += word.getSpelling() + " ";

			// if sentence is finished, create a sentence object
			// and add it to the sentence list
			if (node.isFinalNode()) {
				// add the current word to sentence
				Sentence s = new Sentence();
				s.addSpelling(sentence);
				addToSentences(s);
				
				count_utterances++;
				
				return; // return to stop recursion
						// recursion stops also if list of children below is
						// empty
			}

		}

		GrammarArc[] children = node.getSuccessors();
		for (GrammarArc arc : children) {
			GrammarNode child = arc.getGrammarNode();
			traverse(child, sentence, path);
		}

	}


	private void addToSentences(Sentence s) {
		getSentences().add(s);
	}

}
