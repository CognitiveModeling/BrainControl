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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import javax.speech.recognition.GrammarException;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import marioUI.voiceControl.input.RuleParser;
import marioUI.voiceControl.input.VoiceRecognitionInput;
import marioWorld.engine.Logging;
import marioWorld.utils.ResourceStream;
import marioWorld.utils.UserDir;

/**
 * 
 * Reverse mapping of tags to natural language sentences
 * 
 * The main method needs to be called once each time after changing the
 * responses grammar.
 * 
 * @author Smihael, Fabian
 * 
 */

public class TagsToSentences 
{
	private RuleParser parser;
	private Recognizer recognizer;
	private JSGFGrammar grammar;
	private Map<String, ArrayList<String>> tagsToUtterances;
	private final String path = ResourceStream.DIR_UI_RESOURCES+"/speechSynthesis/";

	/**
	 * Constructor when we want to regenerate tags to sentences mapping
	 */
	public TagsToSentences(boolean forceRegenerate) 
	{		
		init(forceRegenerate);
	}

	private void init(boolean forceRegenerate) 
	{
		if(forceRegenerate) 
		{
			
			
			// TODO !!! THIS IS HARD CODED STUFF!!! There should be some config file for players anyway...
			
			
			// is this really needed for responses?
			ArrayList <String> playerNames = new ArrayList<String>();
			playerNames.add("Clark");
			playerNames.add("Bruce");
			playerNames.add("Peter");
			playerNames.add("Jay");

			tagsToUtterances = new TreeMap<String, ArrayList<String>>();
			
			//GrammarModifier gm = new GrammarModifier();
			//gm.addAgents("players", "PLACEHOLDER", playerNames);	//knowledge imports responses
			
			initSphinx();
		} 
		else 
		{
			read();
		}
	}

	/**
	 * Configures Sphinx so that the mapping procedure can be started.
	 */
	private void initSphinx() 
	{
		ConfigurationManager configurationManager = VoiceRecognitionInput.createConfigurationManager("speechSynthesis/responses.config.xml");

		try {
			this.recognizer = (Recognizer) configurationManager.lookup("recognizer");
			this.recognizer.allocate();
			this.grammar = (JSGFGrammar) configurationManager.lookup("jsgfGrammar");
			this.parser = new RuleParser(grammar);
			
			regenerate();

			this.recognizer.deallocate();

		} catch (PropertyException e) {
			System.out.println("Problem configuring: " + e);
		}
	}

	/**
	 * Traverse given grammar and start mapping tags to sentences
	 */
	private void regenerate() 
	{
		System.out.println("NOW PARSING GRAMMARS...");
		GrammarTraversal tt = new GrammarTraversal(grammar);
		System.out.println("NOW GENERATING INVERSE TAG MAPPING...");
		mapTagsToUtterances(tt.getSentences());
		System.out.println("DONE");
	}

	/**
	 * Method that computes tags to sentences mapping deterministically
	 */
	private void mapTagsToUtterances(List<Sentence> sentences) 
	{
		File file2 = UserDir.getUserFile(path + "responses.list");
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(file2));

			double i=1;
			double size=sentences.size();
			double lastPercent=0;
			
			for (Sentence sentence : sentences) 
			{
				try 
				{	
					String tags = parser.getTagString(sentence.toString());
					addToList(tags, sentence.toString(), writer);
					
					//TODO!!! this way, the sequence of tags still plays a role! that actually is what the tag types should account for...
					
					//players don't have names in engine (that is why PLAYER is needed as tag as well)
					/*addToList(tags.replace("CLARK", "PLAYER"),sentence.toString(),writer);					
					addToList(tags.replace("JAY", "PLAYER"),sentence.toString(),writer);
					addToList(tags.replace("BRUCE", "PLAYER"),sentence.toString(),writer);
					addToList(tags.replace("PETER", "PLAYER"),sentence.toString(),writer);*/
					
					//System.out.println(tags + " ==> " + sentence.toString());
					
				} catch (GrammarException e) {
					error(e.toString());
				}
				
				if(i/size >= lastPercent+0.04)
				{
					System.out.println((int)(100.0 * i/size) + "% done");
					lastPercent=i/size;
				}
				i++;
			}

			writer.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		System.out.println(sentences.size() + " sentences were mapped to "
				+ tagsToUtterances.size() + " to different tag combinations");
	}

	/**
	 * Method that computes tags to sentences mapping given a file with possible
	 * sentences
	 * 
	 * @param file
	 *            File containing sentences
	 */
	public void mapTagsToUtterances(String file) 
	{
		try {
			File in = new File(file);
			BufferedReader reader = new BufferedReader(new FileReader(in));
			String line;

			while ((line = reader.readLine()) != null) 
			{
				String tags = parser.getTagString(line);
				addToList(tags, line, null);
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dumps tags to utterances mapping to a file for later usage
	 */
	private void dump() 
	{
		File file = UserDir.getUserFile(path + "responses.map");

		try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream s = new ObjectOutputStream(f);
			s.writeObject(tagsToUtterances);
			s.flush();
			s.close();
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Reads tags to utterances mapping from a file
	 */
	@SuppressWarnings("unchecked")
	private void read() 
	{
		File file = UserDir.getUserFile(path + "responses.map");
				
		if (file.exists()) 
		{
			try {
				FileInputStream f = new FileInputStream(file);
				ObjectInputStream s = new ObjectInputStream(f);
				try {
					tagsToUtterances = (TreeMap<String, ArrayList<String>>) s
							.readObject();
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		else 
		{
			System.out.println("User file does not exist, forcing regenerate: "+file.getPath());
			// force regenerate
			init(true);
			dump();
		}
	}

	/**
	 * Inserts an entry to ArrayList matching the appropriate key in the HashMap
	 * 
	 * @param mapKey
	 * @param newEntry
	 */
	private synchronized void addToList(String mapKey, String newEntry, BufferedWriter writeToFile) 
	{
		//System.out.println(mapKey);
		ArrayList<String> itemsList = tagsToUtterances.get(mapKey);		
		
		// if list does not exist create it
		if (itemsList == null) 
		{
			itemsList = new ArrayList<String>();
			itemsList.add(newEntry);
			tagsToUtterances.put(mapKey, itemsList);
			if(writeToFile!=null)
			{
				try
				{
					writeToFile.append(mapKey + " ==> " + newEntry + "\n");
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
		else 
		{
			if (!itemsList.contains(newEntry))
			{
				itemsList.add(newEntry);
				if(writeToFile!=null)
				{
					try
					{
						writeToFile.append(mapKey + " ==> " + newEntry + "\n");
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}
		}
	}

	/**
	 * Access the tagsToUtterance mapping object
	 */
	public Map<String, ArrayList<String>> getTagsToUtterances() {
		return tagsToUtterances;
	}

	/**
	 * Get utterance for given tags.
	 * 
	 * @param tags
	 * @return
	 */
	public String getSentence(String tags) 
	{		
		ArrayList<String> utterances = tagsToUtterances.get(tags);

		if (utterances != null) 
		{
			// If there are more than one sentences mapped to the given key
			// (tags sequence), return a random one
			Random rnd = new Random();
			return utterances.get(rnd.nextInt(utterances.size())).replace(" comma", ",");
		} 
		else 
		{
			error("Couldn't find any utterance for " + tags);
			//TODO: avoid circular definitions
			//return (new KnowledgeResponse("DONTKNOW")).toString();
			return "";
		}
	}

	/**
	 * Used instead of getSetence() when there is no entry for given tags
	 * available. This returns the utterance that maps to the tag that is most
	 * similar to the given ones.
	 * 
	 * @param tags
	 * @return
	 */
	public String getNearestSentence(String tags) {
		Map<Double, Entry<String, ArrayList<String>>> mapping = queryMethod(
				tagsToUtterances, tags);
		Entry<String, ArrayList<String>> mostProbableMapping = mapping
				.get(mapping.size() - 1);
		ArrayList<String> utterances = mostProbableMapping.getValue();
		String firstOption = utterances.get(0);
		return firstOption;
	}

	/**
	 * Find entry in a hash map given only part of key
	 * 
	 * See also: http://stackoverflow.com/questions/24285591/search-keys-in-map
	 * 
	 * @param map
	 * @param queryString
	 * @return map containing the percents + the corresponding entries in the
	 *         original map.
	 */
	private Map<Double, Entry<String, ArrayList<String>>> queryMethod(
			Map<String, ArrayList<String>> map, String queryString) {

		Map<Double, Entry<String, ArrayList<String>>> result = new HashMap<Double, Entry<String, ArrayList<String>>>();
		queryString = queryString.replaceAll(" ", "");

		for (Entry<String, ArrayList<String>> entry : map.entrySet()) {
			String key = entry.getKey().replaceAll(" ", "");
			boolean contained = true;

			for (char c : queryString.toCharArray()) {
				if (key.indexOf(c) < 0) {
					contained = false;
					break;
				}
			}

			if (contained) {
				double percent = (double) queryString.length()
						/ (double) key.length();
				result.put(new Double(percent), entry);
			}
		}
		return result;
	}

	/**
	 * Logging
	 * 
	 * @param err
	 */
	private void error(String err) {
		Logging.logWarning("TagsToSentences", err);
	}

	/**
	 * When grammar is changed we need to regenerate tags to utterance mapping.
	 * This should only be done once, and not every single time when a
	 * particular response is being queried.
	 * 
	 */
	public static void main(String[] args) {
		TagsToSentences translator = new TagsToSentences(true);
		translator.dump();
	}

}
