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
package marioUI.gui.overlays;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;

public class MessageBox {

	private static final int PADDING = 6 * GlobalOptions.resolution_factor;
	private static final int TEXTGAP = 2 * GlobalOptions.resolution_factor;
	private static final int MAX_CHARS_IN_LINE = 20 * GlobalOptions.resolution_factor;
	private static final int FONT_SIZE_PLAYER = 8 * GlobalOptions.resolution_factor;
	private static final int FONT_SIZE_HELP = 11 * GlobalOptions.resolution_factor;

	private Color bgColor;
	private Color borderColor;
	private Color textColor;
	private Font font;
	private final String[] text; 
	private final long creationTime;
	private final boolean expire;



	public MessageBox(String text, boolean expireAfterTimeout) {
		creationTime = System.currentTimeMillis();
		this.text = lineBreak(replaceSpecialChars(text.trim()));

		this.expire = expireAfterTimeout;
	
			bgColor = Color.WHITE;
			borderColor = Color.BLUE;
			textColor = Color.RED;
			font = new Font("default", Font.BOLD, FONT_SIZE_PLAYER);
	
	}

	private String replaceSpecialChars(String text) {
		return text.replaceAll("'", "").replaceAll("\\\\\"", "\"");
	}

	private String[] lineBreak(String text) {
		String words[] = text.split(" ");
		List<String> lines = new LinkedList<>();
		String line = "";
		for (String w : words) {
			if (w.length() + line.length() > MAX_CHARS_IN_LINE) {
				if (!line.isEmpty()) {
					lines.add(line);
					line = "";
				}
			}
			line += (!line.isEmpty() ? " " : "") + w;
		}
		if (!line.isEmpty()) {
			lines.add(line);
		}
		return lines.toArray(new String[lines.size()]);
	}

	public boolean isExpired(long bubbleMaxShowTimeMs) {
		return expire && (System.currentTimeMillis() - creationTime > bubbleMaxShowTimeMs);
	}



	/**
	 * Draw speech bubble with text
	 * @param levelScene 
	 * @param playerIndex 
	 * @param g, the graphics object to paint to
	 * @param x, the x position of the object (relative to the pointy part)
	 * @param y, the y position of the object (relative to the pointy part)
	 * @param height, the height of the body part
	 * @param width, the width of the body part
	 */
	public void draw(Graphics2D g, LevelScene levelScene, int playerIndex) {
		// get width and height
		FontMetrics metrics = g.getFontMetrics(font);
		int width = 0;
		for (String line : text) {
			int w = metrics.stringWidth(line)+2*PADDING;
			if (w > width)
				width = w;
		}
		int height = metrics.getHeight()*text.length+TEXTGAP*(text.length-1)+2*PADDING;        		

		// determine current x,y pos
		int x,y;
		
		x = GlobalOptions.xScreen-width;
		y = (0 + height);

	

		// Draw the base shape -- the rectangle the image will fit into as well as its outline
		g.setColor(bgColor);
		g.fillRoundRect(x - width / 2, y - height, width, height, PADDING * 2, PADDING * 2);
		g.setColor(borderColor);
		g.drawRoundRect(x - width / 2, y - height , width, height, PADDING * 2, PADDING * 2);

		

		g.setFont(font);
		g.setColor(textColor);
		for (int i = 0; i < text.length; i++) { 
			String line = text[i];
			g.drawString(line, x - width / 2 + PADDING, y  - height + (metrics.getHeight()+TEXTGAP)*i+ PADDING + metrics.getAscent());
		}
		//g.drawRect(x - width / 2 + PADDING, y - ARROW_HEIGHT - height + PADDING,  metrics.stringWidth(text), metrics.getHeight());
	}

	
}
