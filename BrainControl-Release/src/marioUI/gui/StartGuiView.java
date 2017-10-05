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

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class StartGuiView  extends JFrame
{
	private static final long serialVersionUID = 1L;
	// get screen size:
	public static int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	public static int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

	private StartGuiModel model;
	private StartGuiController controller;
	private ArrayList<AgentPanel> agentPanels;
	private ConfigPanel configPanel;
	private JPanel agentConfigPanel;

	public StartGuiView(StartGuiModel _model, StartGuiController _controller)
	{
		super("Brain Control! - University of Tuebingen");

		controller = _controller;
		model = _model;
		this.setLocation(0, 0);
		this.getContentPane().setLayout(new GridBagLayout());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
//		this.setMinimumSize(new Dimension(screenWidth/2, screenHeight/2));
//		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
			
	}

	public StartGuiView(boolean visible) {
		this(new StartGuiModel(), new StartGuiController());
//		this.setUndecorated(true);
		//model.addController(controller);
		model.addView(this);
		controller.addModel(model);
		controller.addView(this);
		this.setFocusable(true);
		this.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == 27){//ESC Key
					System.exit(1);
				}
			}
		}); 
		model.init();
		this.setVisible(visible);		
	}

	protected void init(){
		agentPanels = new ArrayList<AgentPanel>();
		configPanel = new ConfigPanel(controller, model);

		this.setSize(configPanel.levelButtons.size() * 300 + 300, screenHeight/2);
		LayoutHelper.putGrid(this.getContentPane(), configPanel, 0, 0, 1,1, 1.0, 1.0);

		agentConfigPanel = new JPanel();
		GridBagLayout gBL = new GridBagLayout();
		gBL.rowWeights = new double[] {};
		gBL.columnWeights = new double[] {};
		agentConfigPanel.setLayout(gBL);
		agentConfigPanel.setVisible(true);

		LayoutHelper.putGrid(this.getContentPane(), agentConfigPanel, 0, 1, 1,1, 1.0, 1.0);
	}
	
	
	public ArrayList<AgentConfiguration> getAllAgentConfigurations(){
		ArrayList<AgentConfiguration> ret = new ArrayList<AgentConfiguration>();
		for(AgentPanel aP: agentPanels){
			ret.add(aP.getAgentConfiguration());
		}
		return ret;
	}

	public void setNewAgentPanels(int[] newAgents)
	{
		while(agentPanels.size()>0)
		{
			agentPanels.remove(0);
		}
				
		for(int i=0;i<newAgents.length;i++)
		{
			agentPanels.add(new AgentPanel(this, newAgents[i]));
		}

		this.rearrange();
	}

	/**
	 * this method rearranges the different <code>AgentPanel<code>'s in the Gui
	 * should be called if new panel is added to the <code>ArrayList<code> <code>agentPanels<code>
	 */
	public void rearrange()
	{
		int size = agentPanels.size();
		agentConfigPanel.removeAll();
		agentConfigPanel.setLayout(new GridBagLayout());

		for(int i = 0; i < size; i++)
		{
			LayoutHelper.putGrid(agentConfigPanel, agentPanels.get(i), i, 0, 1,1, 1.0, 1.0);
		}
		this.validate();
	}
	
	public void refreshButtonActivation(){
		configPanel.activateLevelButtons();
	}
	
	public String getCurrentLevel() {
		return model.getLevelName();
	}

	public void startLevel(String levelName, int levelIndex, int[] players) {
		controller.setPlayers(players);
		controller.startNewLevel(levelName, levelIndex);
	}
}
