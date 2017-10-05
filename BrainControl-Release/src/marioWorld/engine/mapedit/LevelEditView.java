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
package marioWorld.engine.mapedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;

import marioWorld.engine.Art;
import marioWorld.engine.LevelRenderer;
import marioWorld.engine.level.Level;

public class LevelEditView extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -7696446733303717142L;

	private LevelRenderer levelRenderer;
	private Level level;
	
	private JLabel cursorPosition;

	private int xTile = -1;
	private int yTile = -1;
	private TilePicker tilePicker;

	public LevelEditView(TilePicker tilePicker, JLabel cursorPosition) {
		this.tilePicker = tilePicker;
		level = new Level(256, 15);
		Dimension size = new Dimension(level.width * 16, level.height * 16);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);

		this.cursorPosition=cursorPosition;
				
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setLevel(Level level) {
		this.level = level;
		Dimension size = new Dimension(level.width * 16, level.height * 16);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		repaint();
		levelRenderer.setLevel(level);
	}

	public Level getLevel() {
		return level;
	}

	public void addNotify() {
		super.addNotify();
		Art.init(getGraphicsConfiguration());
		levelRenderer = new LevelRenderer(level, getGraphicsConfiguration(), level.width * 16, level.height * 16);
		levelRenderer.renderBehaviors = true;
	}

	@Override
	public void paintComponent(Graphics g) 
	{
		g.setColor(new Color(0x8090ff));
		g.fillRect(0, 0, level.width * 16, level.height * 16);
		levelRenderer.render((Graphics2D) g, 0, 0);
		g.setColor(Color.BLACK);
		g.drawRect(xTile * 16 - 1, yTile * 16 - 1, 17, 17);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		xTile = -1;
		yTile = -1;
		cursorPosition.setText("Cursor position: undefined");
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		xTile = e.getX() / 16;
		yTile = e.getY() / 16;
		
		cursorPosition.setText("Cursor position: ("+xTile+", "+yTile+")");

		if (e.getButton() == 3) {
			tilePicker.setPickedTile(level.getBlock(xTile, yTile));
		} else {
			level.setBlock(xTile, yTile, tilePicker.pickedTile);
			levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);

			repaint();
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		xTile = e.getX() / 16;
		yTile = e.getY() / 16;

		cursorPosition.setText("Cursor position: ("+xTile+", "+yTile+")");
		
		level.setBlock(xTile, yTile, tilePicker.pickedTile);
		levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);

		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		xTile = e.getX() / 16;
		yTile = e.getY() / 16;
		
		cursorPosition.setText("Cursor position: ("+xTile+", "+yTile+")");
		
		repaint();
	}
}
