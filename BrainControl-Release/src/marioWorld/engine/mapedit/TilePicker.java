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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import marioWorld.engine.Art;

public class TilePicker extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -7696446733303717142L;

	private int xTile = -1;
	private int yTile = -1;

	public byte pickedTile;

	private LevelEditor tilePickChangedListener;

	public TilePicker() {
		Dimension size = new Dimension(256, 256);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void addNotify() {
		super.addNotify();
		Art.init(getGraphicsConfiguration());
	}

	public void paintComponent(Graphics g) {
		g.setColor(new Color(0x8090ff));
		g.fillRect(0, 0, 256, 256);

		for (int x = 0; x < 16; x++)
			for (int y = 0; y < 16; y++) {
				g.drawImage(Art.level.getImage(x,y), (x << 4), (y << 4), null);
			}

		g.setColor(Color.WHITE);
		int xPickedTile = (pickedTile & 0xff) % 16;
		int yPickedTile = (pickedTile & 0xff) / 16;
		g.drawRect(xPickedTile * 16, yPickedTile * 16, 15, 15);

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
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		xTile = e.getX() / 16;
		yTile = e.getY() / 16;

		setPickedTile((byte) (xTile + yTile * 16));
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		xTile = e.getX() / 16;
		yTile = e.getY() / 16;

		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		xTile = e.getX() / 16;
		yTile = e.getY() / 16;
		repaint();
	}

	public void setPickedTile(byte block) {
		pickedTile = block;
		repaint();
		if (tilePickChangedListener != null)
			tilePickChangedListener.setPickedTile(pickedTile);
	}

	public void addTilePickChangedListener(LevelEditor editor) {
		this.tilePickChangedListener = editor;
		if (tilePickChangedListener != null)
			tilePickChangedListener.setPickedTile(pickedTile);
	}
}
