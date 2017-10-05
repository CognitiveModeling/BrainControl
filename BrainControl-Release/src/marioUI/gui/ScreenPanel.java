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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import marioWorld.engine.GlobalOptions;

/**
 * this class handels the splitscreens for the gui 
 * @author Jonas Einig
 *
 */
public class ScreenPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public  JPanel gamePanel;
	public ScreenPanel(JPanel gameWorldComponentPanel, String name) 
	{
//		this.setLayout(new GridBagLayout());
//		JLabel nameLabel = new JLabel(name);
//		nameLabel.setFont(new Font("Verdana", 1, 16));
//		LayoutHelper.putGrid(this, nameLabel, 0, 0, 1, 1, 1.0, 1.0,GridBagConstraints.CENTER, GridBagConstraints.NONE );
		
		gamePanel = gameWorldComponentPanel;
		gamePanel.setMinimumSize(new Dimension(GlobalOptions.xScreen, GlobalOptions.yScreen));
		gamePanel.setVisible(true);
		LayoutHelper.putGrid(this, gamePanel, 0, 1, 1,1, 1.0, 1.0);
	    this.setVisible(true);
	    this.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));

//	    this.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED, Color.orange, Color.black));
	    
	   
	}
}
