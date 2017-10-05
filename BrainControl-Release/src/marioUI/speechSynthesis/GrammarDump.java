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

/**
 * Generate Sentences (with random seeding)
 *
 * Copyright 1999-2002 Carnegie Mellon University.  
 * Portions Copyright 2002 Sun Microsystems, Inc.  
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.linguist.dictionary.Word;
import edu.cmu.sphinx.linguist.language.grammar.GrammarArc;
import edu.cmu.sphinx.linguist.language.grammar.GrammarNode;

public class GrammarDump {

	private final Random randomizer = new Random(System.currentTimeMillis());
	private JSGFGrammar grammar;

	public GrammarDump(JSGFGrammar grammar) {
		this.grammar = grammar;
	}

	/**
	 * Dump a set of random sentences that fit this grammar
	 * 
	 * 
	 * @param path
	 *            the name of the file to dump the sentences to
	 * @param count
	 *            dumps no more than this. May dump less than this depending
	 *            upon the number of uniqe sentences in the grammar.
	 * 
	 */

	public void dumpRandomSentences(String path, int count) {
		try {
			Set<String> set = new HashSet<String>();
			PrintWriter out = new PrintWriter(new FileOutputStream(path));
			for (int i = 0; i < count; i++) {
				String s = getRandomSentence();
				if (!set.contains(s)) {
					set.add(s);
					out.println(s);
				}
			}
			out.close();
		} catch (IOException ioe) {
			System.out.println("Can't write random sentences to " + path + ' '
					+ ioe);
		}
	}

	/**
	 * Dump a set of random sentences that fit this grammar
	 * 
	 * @param count
	 *            dumps no more than this. May dump less than this depending
	 *            upon the number of uniqe sentences in the grammar.
	 */
	public void dumpRandomSentences(int count) {
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < count; i++) {
			String s = getRandomSentence();
			if (!set.contains(s)) {
				set.add(s);
			}
		}
		List<String> sampleList = new ArrayList<String>(set);
		Collections.sort(sampleList);

		for (String sentence : sampleList) {
			System.out.println(sentence);
		}
	}

	/**
	 * Returns a random sentence that fits this grammar
	 * 
	 * @return a random sentence that fits this grammar
	 */
	public String getRandomSentence() {
		StringBuilder sb = new StringBuilder();
		GrammarNode node = grammar.getInitialNode();
		while (!node.isFinalNode()) {
			if (!node.isEmpty()) {
				Word word = node.getWord();
				if (!word.isFiller())
					sb.append(word.getSpelling()).append(' ');
			}
			node = selectRandomSuccessor(node);
		}
		return sb.toString().trim();
	}

	/**
	 * Given a node, select a random successor from the set of possible
	 * successor nodes
	 * 
	 * @param node
	 *            the node
	 * @return a random successor node.
	 */
	private GrammarNode selectRandomSuccessor(GrammarNode node) {
		GrammarArc[] arcs = node.getSuccessors();

		// select a transition arc with respect to the arc-probabilities (which
		// are log and we don't have a logMath here
		// which makes the implementation a little bit messy
		if (arcs.length > 1) {
			double[] linWeights = new double[arcs.length];
			double linWeightsSum = 0;

			final double EPS = 1E-10;

			for (int i = 0; i < linWeights.length; i++) {
				linWeights[i] = (arcs[0].getProbability() + EPS)
						/ (arcs[i].getProbability() + EPS);
				linWeightsSum += linWeights[i];
			}

			for (int i = 0; i < linWeights.length; i++) {
				linWeights[i] /= linWeightsSum;
			}

			double selIndex = randomizer.nextDouble();
			int index = 0;
			for (int i = 0; selIndex > EPS; i++) {
				index = i;
				selIndex -= linWeights[i];
			}

			return arcs[index].getGrammarNode();

		} else {
			return arcs[0].getGrammarNode();
		}
	}

}
