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
package marioUI.gui;

public class TutorialMessage {

	private int minx,maxx;
	private int autoActivationTime;
	private String[] text;
	
	public TutorialMessage(String string) {
		String[] split = string.split(":",4);
		if (split.length != 4)
			throw new IllegalArgumentException("Could not parse tutorial string: "+string);
		try {
			minx = Integer.parseInt(split[0]);
			maxx = Integer.parseInt(split[1]);
			autoActivationTime = Integer.parseInt(split[2]);
		} catch(NumberFormatException e) {			
			throw new IllegalArgumentException("Could not parse number in tutorial string: "+string);
		}
		text = split[3].split("\\|");
	}

	
	public int getAutoActivationTime() {
		return autoActivationTime;
	}

	public String[] getText() {
		return text;
	}

	public boolean isActive(int maxPlayerx, int playery) {
		return (maxPlayerx >= minx) && (maxPlayerx <= maxx);
	}

}
