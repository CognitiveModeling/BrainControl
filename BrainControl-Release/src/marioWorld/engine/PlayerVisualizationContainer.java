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
package marioWorld.engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import marioAI.agents.CAEAgent;
import marioAI.run.PlayHook;
import marioWorld.agents.Agent;
import marioWorld.engine.sprites.Player;

public class PlayerVisualizationContainer
{
	public JPanel gameWorldComponentPanel = null;
	public GameWorldComponent gameWorldComponent = null;
	public Agent agent = null;
	public Player player = null;
	
	public PlayerVisualizationContainer(Agent setAgent, Player setPlayer)
	{
		agent=setAgent;
		player=setPlayer;		
		
		gameWorldComponentPanel = new JPanel(true);
		gameWorldComponent = new GameWorldComponent();
		
		gameWorldComponentPanel.add(gameWorldComponent);
		gameWorldComponentPanel.setVisible(true);		
		final CAEAgent test = (CAEAgent) setAgent;
		// pause game via key 'p'
		//TODO: 'p' doesn't continue the game when pressed again
		gameWorldComponent.addKeyListener(new KeyListener() 
		{
			@Override
			public void keyTyped(KeyEvent e) {
				if (Character.toLowerCase(e.getKeyChar()) == 'p') {
					GlobalOptions.pauseGame = !GlobalOptions.pauseGame;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
		
		gameWorldComponent.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) { //put mouseinput to displayed agent
				((CAEAgent)PlayHook.agents.get(GlobalOptions.playerGameWorldToDisplay)).getMouseInput().processInput(e);
				
			}
		});
	}

	public void init() {
		this.gameWorldComponentPanel.add(gameWorldComponent);
	}
}
