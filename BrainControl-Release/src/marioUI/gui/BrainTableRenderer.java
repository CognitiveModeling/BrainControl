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

import java.awt.Color;
import java.awt.Component;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import marioAI.brain.Brain;
import marioAI.brain.Effect;
import marioWorld.engine.PlayerWorldObject;

/**
 * This class is responsible for the color scheme of the entries in the brain
 * table. Rows focusing on the same worldObjects share one color.
 * 
 * @author Volker, Jonas, Matthias
 * 
 */
public class BrainTableRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -3409442330983563064L;
	private Color[] colors = { Color.cyan, Color.lightGray, Color.green,
			Color.orange, Color.pink, Color.yellow };

	private final Brain brain;

	public BrainTableRenderer(Brain brain) 
	{
		this.brain = brain;
	}

	// adjust cell color
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) 
	{		
		TreeMap<Effect, Double> map = brain.getValueMapAt(row, column);		
		
		if (map == null) 
		{
			// set Background of first two cells (which only show health and
			// object of interaction)

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setBackground(this.getColorByRow(table, row));			
			
			return cell;
		} 
		else
		{	
			if(map.size()*15 > table.getRowHeight(row))
				table.setRowHeight(row, map.size()*15);
			
			// print effect in grey-tone which corresponds with its probability
			// (the darker the writing, the more probable the effect)

			JLabel ret = new JLabel();
			
			// open html; contains the effect in its particular grey
			String s = "<html>";

			// iterate over the different observed effects
			// example: energy increase and object destruction can appear after
			// one action
			for (Entry<Effect, Double> entry : map.entrySet()) 
			{

				// get probability of the effect from brain
				float probability = (float) entry.getValue().doubleValue();

				// create grey-tone corresponding to probability
				String hexColor = createHexaColor(probability);

				// add effect in corresponding color to the string
				s += "<font color=" + hexColor + ">"
						+ entry.getKey().toString() + "<br>"+ "</font>";
			}
			s += "</html>"; // close html-
			ret.setText(s);

			// make background of ret (JLabel) visible
			//ret.setOpaque(true);					
			
			//ret.setBackground(this.getColorByRow(table, row));
			
			return ret;
		}

	}

	/*
	 * creates particular grey-tone-color in hexadecimal (required for html)
	 * this creation starts with a white color and lowers its brightness
	 * corresponding to the probability of the effect
	 * example: white text stands for low probability (0.0)
	 * 			black text stands for high probability (1.0)
	 */
	private String createHexaColor(float dimVal) {

		// white in HSB-color scheme
		float hue = 0;
		float sat = 0;
		float bright = 1;

		// subtract probability (dimVal) from brightness to receive a particular
		// grey tone (the more probable an effect, the blacker the color
		bright -= dimVal;

		// convert current HSB values to RGB (required for following
		// html-transformation
		int rgb = Color.HSBtoRGB(hue, sat, bright);
		// r, g and b correspond to the computed grey-value in RGB-scheme

		// transform RGB to html
		String hex = Integer.toHexString(rgb & 0xffffff);
		if (hex.length() < 6) {
			hex = "0" + hex;
		}
		// html colors always start with a #-symbol
		hex = "#" + hex;

		return hex;
	}

	private Color getColorByRow(JTable table, int row) {
		int colorIndex = 0;
		// default color
		if (!(table.getValueAt(0, Brain.CONDITION_OBJECT_COLUMN) instanceof PlayerWorldObject)) {
			return Color.red;
		}
		PlayerWorldObject lastRowObj = (PlayerWorldObject) table.getValueAt(0,
				Brain.CONDITION_OBJECT_COLUMN);
		// get colors of all previous rows to determine new color
		for (int curRow = 0; curRow <= row; curRow++) {
			if (table.getValueAt(curRow, Brain.CONDITION_OBJECT_COLUMN) instanceof PlayerWorldObject) {
				PlayerWorldObject curRowObj = (PlayerWorldObject) table
						.getValueAt(curRow, Brain.CONDITION_OBJECT_COLUMN);
				// compare previous object to current
				if (lastRowObj == curRowObj) {
					// color as before
				} else {
					// choose next color
					colorIndex = (colorIndex + 1) % this.colors.length;
				}
				lastRowObj = curRowObj;
			}

		}
		return this.colors[colorIndex];
	}

	// weist jeder Spalte einer Tabelle einen TableRender zu
	//
	public void setTableRenderer(JTable table) 
	{
		TableColumnModel tColMod = table.getColumnModel();
		for (int i = 0; i < tColMod.getColumnCount(); i++) 
		{
			TableColumn tcol = tColMod.getColumn(i);
			tcol.setCellRenderer(this);			
		}
	}

}
