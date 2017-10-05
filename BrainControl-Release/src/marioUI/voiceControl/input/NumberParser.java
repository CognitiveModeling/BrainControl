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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Convert string containing number printed as word into int
 * 
 * @author Mihael SimoniÄ?
 */

public class NumberParser {

	private final String[] DIGITS = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
			"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen",
			"nineteen" };
	
	private final String[] TENS = { null, null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty",
			"ninety" };
	
	private final String[] MAGNITUDES = { "hundred" };

	private List<String> whitelist = new ArrayList<String>();

	public NumberParser() {
		whitelist.addAll(Arrays.asList(DIGITS));
		whitelist.addAll(Arrays.asList(TENS));
		whitelist.addAll(Arrays.asList(MAGNITUDES));
	}

	/**
	 * Filter out non number words 
	 */
	public String getOnlyNumbers(String input) {
		List<String> blacklist = new ArrayList<String>(Arrays.asList(input.split(" ")));
		blacklist.removeAll(whitelist);

		for (String s : blacklist)
			input = input.replace(s, "");

		return input.trim();
	}

	/**
	 * using idea outlined on http://stackoverflow.com/q/4062022/822644
	 */
	private int convert(String input) {

		int[] triple = { 0, 0, 0 };
		//				 |  |  | 
		//				 |  |  |-> * 1
		//               |  |----> * 10
		//               |-------> * 100

		StringTokenizer set = new StringTokenizer(input);

		String first, second, third, forth;

		switch (set.countTokens()) {
		case 1:
			first = set.nextToken();
			triple[0] = 0;

			for (int k = 0; k < DIGITS.length; k++)
				if (first.equals(DIGITS[k])) {
					triple[2] = k;
					break;
				}
			for (int i = 0; i < TENS.length; i++)
				if (first.equals(TENS[i])) {
					triple[1] = i;
					break;
				}

			break;
		case 2:

			first = set.nextToken();
			second = set.nextToken();
			if (first.equals(MAGNITUDES[0])) { // numbers like hundred six
				triple[0] = 1;
				triple[2] = convert(second);
			} else if (second.equals(MAGNITUDES[0])) { // numbers like five hundred
				for (int k = 0; k < 10; k++)
					if (first.equals(DIGITS[k]))
						triple[0] = k;
			} else { // numbers like twenty one 
				for (int i = 0; i < TENS.length - 1; i++) {
					if (first.equals(TENS[i])) {
						triple[1] = i;
						for (int j = 0; j < DIGITS.length; j++) {
							if (second.equals(DIGITS[j])) {
								triple[2] = j;
								break;
							}
						}
						break;
					}
				}
			}

			break;
		case 3:
			first = set.nextToken();
			second = set.nextToken();
			third = set.nextToken();

			if (first.equals(MAGNITUDES[0])) {
				triple[0] = 1;
				triple[2] = convert(second + " " + third);
			} else if (second.equals(MAGNITUDES[0])) {
				triple[0] = convert(first);
				triple[2] = convert(third);
			} else {
				System.out.println("malformed number");
			}

			break;

		case 4:
			first = set.nextToken();
			second = set.nextToken();
			third = set.nextToken();
			forth = set.nextToken();

			if (second.equals(MAGNITUDES[0])) {
				triple[0] = convert(first);
				triple[2] = convert(third + " " + forth);
			} else {
				System.out.println("malformed number");
			}

			break;
		default:
			System.out.println("only three digit numbers are supported");
			break;
		}

		return triple[0] * 100 + triple[1] * 10 + triple[2];

	}
	
	private String checkIfAlreadyNumeric(String input) {
	    if(input == null || input.isEmpty()) return "";
	    
	    boolean found = false;
	    
	    String res ="";
	    
	    for(char c : input.toCharArray()) {
	        if(Character.isDigit(c)){
	            res+=c;
	            found = true;
	        } else if(found) break;               
	    }
	    
	    return res;
	}

	public String replaceNumbers(String input) {
		if (checkIfAlreadyNumeric(input) != "")
		      return checkIfAlreadyNumeric(input);
		return "" + convert(getOnlyNumbers(input));
	}
	
}
