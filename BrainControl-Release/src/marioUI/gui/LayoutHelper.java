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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;

/**
 * This class provides a more comfortable use of the GridBagLayout
 * 
 * @author Holger Gast
 * 
 */
public class LayoutHelper {
	public static void putGrid(Container cp, Component c, int x, int y, int w, int h, double wx, double wy, int anchor, int fill) {
		GridBagConstraints cc = new GridBagConstraints();
		cc.gridx = x;
		cc.gridy = y;
		cc.gridwidth = w;
		cc.gridheight = h;
		cc.weightx = wx;
		cc.weighty = wy;
		cc.anchor = anchor;
		cc.fill = fill;
		cp.add(c, cc);
	}

	public static void putGrid(Container cp, Component c, int x, int y, int w, int h, double wx, double wy) {
		putGrid(cp, c, x, y, w, h, wx, wy, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH);
	}
	
	public static void removeGrid(Container cp, Component c) {
		cp.getLayout().removeLayoutComponent(c);
	}

	public static void putGrid(Container cp, Component c, int x, int y, int w, int h) {
		putGrid(cp, c, x, y, w, h, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH);
	}

}
