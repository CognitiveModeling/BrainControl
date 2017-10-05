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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import mario.main.Settings;
import marioAI.aStar.AStarNodeVisualizer;
import marioAI.goals.Goal;
import marioAI.goals.PlanningEvent;
import marioAI.goals.ReservoirMultiplePlanner;
import marioAI.movement.AStarSimulator;
import marioAI.movement.heuristics.FineGrainPotentialField;
import marioAI.movement.heuristics.WorldNodeHeuristic;
import marioAI.overlay.AStarExpansionOverlay;
import marioAI.overlay.FineGrainPotentialFieldRectRenderer;
import marioAI.overlay.GoalOverlay;
import marioAI.overlay.GridWorldOverlay;
import marioAI.overlay.HeuristicGridRenderer;
import marioAI.overlay.IGridRectRenderer;
import marioAI.overlay.ReachabilityOverlay;
import marioAI.overlay.ToggleableOverlay;
import marioAI.overlay.ReachabilityRectRenderer;
import marioAI.reservoirMotivation.AbstractReservoir;
import marioAI.run.PlayHook;
import marioUI.gui.overlays.MessageOverlay;
import marioUI.gui.overlays.MotivationsOverlay;
import marioUI.gui.overlays.ObjectsOverlay;
import marioUI.gui.overlays.SpeechBubbleOverlay;
import marioUI.gui.overlays.TileSelectionOverlay;
import marioUI.gui.overlays.WaitingIconOverlay;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.OverlayManager;
import marioWorld.engine.Tickable;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.ObservationRelativeCoarse;
import marioWorld.utils.ResourceStream;
import marioAI.brain.simulation.ReachabilityMap;

public class MissionControllPanel extends JPanel implements Observer, Tickable {

	private static final long serialVersionUID = 3265600164147081120L;

	// Checkboxes for overlays
	private JCheckBox checkBoxGoalOverlay;
	private JCheckBox checkBoxAStarOverlay;
	private JCheckBox checkBoxHeuristicOverlay;
	private JCheckBox checkBoxSpokenTextOverlay;
	private JCheckBox checkBoxReachabilityMapOverlay;
	private JCheckBox checkBoxMotivationsOverlay;
	private JCheckBox checkBoxObjectOverlay;
	
	private List<JCheckBox> allOverLayCheckBoxes = new ArrayList<JCheckBox>();

	

	// Buttons
	private JButton cancelGoals;
	private JButton openBrain;

	public JButton getOpenBrain() {
		return openBrain;
	}
	// Overlays
	private ToggleableOverlay goalOverlay = new ToggleableOverlay("initial");
	private ToggleableOverlay aStarOverlay = new ToggleableOverlay("initial");
	private ToggleableOverlay heuristicOverlay = new ToggleableOverlay(
			"initial");
	private ToggleableOverlay reachabilityMapOverlay = new ToggleableOverlay(
			"initial");
	private SpeechBubbleOverlay spokenTextOverlay = new SpeechBubbleOverlay(
			"OverlaySokentext");
	private MessageOverlay messageOverlay = new MessageOverlay("OverlayMessage");
	
	private ToggleableOverlay waitingIconOverlay = new ToggleableOverlay(
			"initial");
	private ToggleableOverlay tileSelectionOverlay = new TileSelectionOverlay(null);
	
	private ToggleableOverlay motivationsOverlay = new MotivationsOverlay(null,null,null);
	
	private ToggleableOverlay objectsOverlay  = new ToggleableOverlay("initial");
	
	private OverlayManager overlays;

	// Controller and AStarSimulator
	public Controller controller;
	private AStarSimulator aStarSimulator;

	// Text for energys
	private JLabel energyText;
	private JLabel energyImage;

	// List of Reservoir and associated bars
	private ArrayList<JProgressBar> reservoirBars;
	private LinkedList<AbstractReservoir> reservoirList;

	int playerIndex;
	public String[] initiallyActiveGuiOverlays;
	
	
	public MissionControllPanel(String[] initiallyActiveGuiOverlays,
			final Controller controller, int playerIndex) {
		GlobalOptions.realLevelScene.addTickable(this);
		this.initiallyActiveGuiOverlays = initiallyActiveGuiOverlays;
		this.playerIndex=playerIndex;
		
		// set controller and aStarSimulator
		this.controller = controller;
		this.aStarSimulator = controller.agent.getAStarSimulator();

		// Creates button to cancel goals
		this.cancelGoals = new JButton("Cancel Goals");
		this.cancelGoals.setToolTipText("Delete all goals and stop movement.");

		this.cancelGoals.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == cancelGoals) {
					aStarSimulator.stopEverythingInAStarComputation();
				}

			}
		});

		openBrain = new JButton("Open Brain");
		openBrain.setPreferredSize(cancelGoals.getPreferredSize());
		this.openBrain.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == openBrain) {
					int screenWidth = (int) Toolkit.getDefaultToolkit()
							.getScreenSize().getWidth();
					int screenHeight = (int) Toolkit.getDefaultToolkit()
							.getScreenSize().getHeight();
					JFrame brainWindow = new JFrame(controller.agent.getName()
							+ "'s Brain");
					int xPos = 0;
					int yPos = 0;
					switch (controller.agent.getPlayerIndex()) {
					case 0:
						xPos = 0;
						yPos = 0;
						break;
					case 1:
						xPos = 0;
						yPos = screenWidth / 2;
						break;
					case 2:
						xPos = screenHeight / 2;
						yPos = 0;
						break;
					case 3:
						xPos = screenHeight / 2;
						yPos = screenWidth / 2;
						break;
					}
					brainWindow.setLocation(new Point(xPos, yPos));
					brainWindow
							.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					BrainTab brainTab = new BrainTab(controller.agent
							.getBrain(), null);
					brainWindow.add(brainTab);
					brainWindow.setMinimumSize(new Dimension(screenWidth / 2,
							(screenHeight / 2) - 12));
					brainWindow.setVisible(true);
				}

			}
		});

		// add overlays to overlay manager
		// overlays = ToolsConfigurator.getGameWorld().overlays;
		overlays = PlayHook.gameWorld.overlays;

		// Initialize checkboxes: They are still there but not shown
		this.checkBoxGoalOverlay = new JCheckBox("Show Goals");
		this.checkBoxGoalOverlay.setName("overlayGoal");
		allOverLayCheckBoxes.add(checkBoxGoalOverlay);

		this.checkBoxAStarOverlay = new JCheckBox("Show Search");
		this.checkBoxAStarOverlay.setName("overlayAStar");
		allOverLayCheckBoxes.add(checkBoxAStarOverlay);

		this.checkBoxHeuristicOverlay = new JCheckBox("Heuristic");
		this.checkBoxHeuristicOverlay.setName("overlayHeuristic");
		allOverLayCheckBoxes.add(checkBoxHeuristicOverlay);

		this.checkBoxSpokenTextOverlay = new JCheckBox("SpokenText");
		this.checkBoxSpokenTextOverlay.setName("overlaySpokenText");
		allOverLayCheckBoxes.add(checkBoxSpokenTextOverlay);
		overlays.addOverlay(spokenTextOverlay);

		this.checkBoxReachabilityMapOverlay = new JCheckBox("ReachabilityMap");
		this.checkBoxReachabilityMapOverlay
				.setName("overlayReachabilityMap");
		allOverLayCheckBoxes.add(checkBoxReachabilityMapOverlay);
		
		//Overlays that display information to the User i.e. tile selection, waiting animation etc.
		overlays.addOverlay(waitingIconOverlay);
		overlays.addOverlay(tileSelectionOverlay);
		overlays.addOverlay(motivationsOverlay);
		overlays.addOverlay(objectsOverlay);
		overlays.addOverlay(messageOverlay);
		
		this.checkBoxMotivationsOverlay = new JCheckBox("Motivations");
		this.checkBoxMotivationsOverlay
				.setName("overlayMotivations");
		allOverLayCheckBoxes.add(checkBoxMotivationsOverlay);
		
		this.checkBoxObjectOverlay = new JCheckBox("Object Names");
		this.checkBoxObjectOverlay.setName("overlayObjects");
		allOverLayCheckBoxes.add(checkBoxObjectOverlay);

		
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		add(checkBoxPanel);
		
		OverlaySelectionListener overlaySelectionListener = new OverlaySelectionListener(
				this);
		for (JCheckBox overlayCheckBox : allOverLayCheckBoxes) {
			overlayCheckBox.addActionListener(overlaySelectionListener);
			checkBoxPanel.add(overlayCheckBox);
		}

		// sets initially active overlays to active
		for (String s : initiallyActiveGuiOverlays) {
			boolean found = false;
			for (JCheckBox overlayCheckBox : allOverLayCheckBoxes) {
				if (overlayCheckBox.getName().toLowerCase()
						.equals(s.toLowerCase())) {
					overlayCheckBox.doClick();
					found = true;
				}
			}
			if (!found) {
				throw new IllegalArgumentException(
						"Did not find an overlay attribute with name "
								+ s
								+ "! Maybe you misspelled it? Please check setName() in View");
			}
		}

		JPanel leftColumn = new JPanel();
		leftColumn.setLayout(new GridBagLayout());
		JPanel rightColumn = new JPanel();
		rightColumn.setLayout(new GridBagLayout());

		this.setLayout(new GridBagLayout());

		// Show reservoirs
		if (controller.agent.getPlanner() instanceof ReservoirMultiplePlanner) {
			this.reservoirList = ((ReservoirMultiplePlanner) controller.agent
					.getPlanner()).getReservoirList();
		}
		this.reservoirBars = new ArrayList<JProgressBar>();
		if (controller.agent.getPlanner() instanceof ReservoirMultiplePlanner) {
			double sum = 0;
			for (AbstractReservoir reservoir : this.reservoirList) {
				sum += reservoir.getValue();
			}
			for (AbstractReservoir reservoir : this.reservoirList) {
				JProgressBar bar = new JProgressBar(0, 100);
				bar.setValue((int) (reservoir.getValue() / sum * 100));
				bar.setOrientation(SwingConstants.HORIZONTAL);
				this.reservoirBars.add(bar);
			}
		}

		int count = 0;
		for (JProgressBar bar : this.reservoirBars) {

			LayoutHelper.putGrid(leftColumn,
					new JLabel(this.reservoirList.get(count).getName() + ": "),
					0, 1 + count, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
					GridBagConstraints.RELATIVE);
			LayoutHelper.putGrid(leftColumn, bar, 1, 1 + count, 1, 1, 1, 1,
					GridBagConstraints.NORTHWEST, GridBagConstraints.RELATIVE);
			count++;
		}

		InputStream resource = ResourceStream
				.getResourceStream(ResourceStream.DIR_WORLD_RESOURCES
						+ "/engine/resources/item_energy.png");
		BufferedImage image = null;
		try {
			image = ImageIO.read(resource);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		energyImage = new JLabel(new ImageIcon(image));
		energyText = new JLabel(" " + controller.agent.getPlayer().getEnergys());

		JPanel energyInfo = new JPanel();
		energyInfo.add(energyImage);
		energyInfo.add(energyText);

		LayoutHelper.putGrid(leftColumn, energyInfo, 0, 0, 1, 1, 1, 1,
				GridBagConstraints.WEST, GridBagConstraints.NORTH);

		LayoutHelper.putGrid(rightColumn, this.openBrain, 0, 0, 1, 1, 1, 1,
				GridBagConstraints.EAST, GridBagConstraints.RELATIVE);
		LayoutHelper.putGrid(rightColumn, this.cancelGoals, 0, 1, 1, 1, 1, 1,
				GridBagConstraints.EAST, GridBagConstraints.RELATIVE);

		LayoutHelper.putGrid(this, rightColumn, 1, 0, 1, 1, 1, 1,
				GridBagConstraints.EAST, GridBagConstraints.RELATIVE);
		LayoutHelper.putGrid(this, leftColumn, 0, 0, 1, 1, 1, 1,
				GridBagConstraints.WEST, GridBagConstraints.NORTH);

		this.setVisible(true);
	}

	class OverlaySelectionListener implements ActionListener {
		MissionControllPanel missionControllTab;

		public OverlaySelectionListener(MissionControllPanel missionControllTab) {
			this.missionControllTab = missionControllTab;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof JCheckBox) {
				JCheckBox chkbx = (JCheckBox) e.getSource();
				String name = chkbx.getName();
				if (name.startsWith("overlay")) {
					missionControllTab.setOverlayActive(
							name.substring("overlay".length()),
							chkbx.isSelected());
				}
			}
		}

	}

	@Override
	public void update(Observable subject, Object typeChanged) {
		if ((typeChanged instanceof PlanningEvent)) {

			PlanningEvent event = (PlanningEvent) typeChanged;
	
			switch (event.type) {
			case ASTAR_START_SEARCH:
				if (this.heuristicOverlay.getActive()) {
					this.heuristicOverlay = this
							.updateSingleOverlay(this.heuristicOverlay,
									new GridWorldOverlay(createHeuristicRenderer(),
											"heuristic overlay"));
				}
				break;
			case NEW_GOAL_SELECTED:
				if (this.goalOverlay.getActive()) {
					this.goalOverlay = this.updateSingleOverlay(this.goalOverlay,
							new GoalOverlay(this.aStarSimulator));
				}
				break;
			case AGENT_REACHED_GOAL:
				System.out.println("AGENT "+PlayHook.agents.get(playerIndex).getName()+" REACHED GOAL ACCORDING TO MISSONCONTROLPANEL");
				overlays.removeOverlay(heuristicOverlay);
				((MotivationsOverlay)this.motivationsOverlay).setGoalReservoir(null);
				break;
			case ASTAR_COULD_NOT_REACH_GOAL:
				if (this.goalOverlay.getActive()) {
					this.goalOverlay = this.updateSingleOverlay(this.goalOverlay,
							new GoalOverlay(this.aStarSimulator,
									(Goal) (event.object)));
								}
				break;
			case START_ICON:
				this.setOverlayActive("Waiting", true);
				break;
			case STOP_ICON:
				this.setOverlayActive("Waiting", false);
				break;
			case RESERVOIR_INTERRUPT:
				((MotivationsOverlay)this.motivationsOverlay).setGoalReservoir(null);
				break;
			case ASTAR_REACHED_GOAL:
			case GOAL_SELECTION_FAILED:
			
				
			default:
				break;
			}
			
		} else  if (typeChanged instanceof AbstractReservoir) {
			AbstractReservoir goalReservoir = (AbstractReservoir) typeChanged;
			
			((MotivationsOverlay)this.motivationsOverlay).setGoalReservoir(goalReservoir.getName());
			
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onTick(LevelScene levelScene) {
		this.energyText
				.setText(" " + controller.agent.getPlayer().getEnergys());

		this.setReservoirValues();
		final MissionControllPanel finalMissionControllPanel = this; 

		if (this.aStarOverlay.getActive() && this.aStarSimulator != null
				&& this.aStarSimulator.getCurrentAStarSearchRunnable() != null) {
			final List<AStarNodeVisualizer> expandedNodes = (List<AStarNodeVisualizer>) (Settings.CLEAR_DISPLAYED_ASTAR_VISUALIZERS ? aStarSimulator
					.getExpandedNodesAndClear() : aStarSimulator
					.getLastExpandedNodes(500));

			final ArrayList<AStarNodeVisualizer> excludedNodes = new ArrayList<AStarNodeVisualizer>();



			new Thread(new Runnable() {
				@Override
				public void run() {
					finalMissionControllPanel.aStarOverlay = finalMissionControllPanel
							.updateSingleOverlay(
									finalMissionControllPanel.aStarOverlay,
									new AStarExpansionOverlay(expandedNodes,
											excludedNodes));
				}
			}).start();
		} else {
			this.aStarOverlay = this.updateSingleOverlay(this.aStarOverlay,
					new AStarExpansionOverlay(
							new ArrayList<AStarNodeVisualizer>(),
							new ArrayList<AStarNodeVisualizer>()));			
		}
		
		//always plot reachability heuristics when activated
		if (this.reachabilityMapOverlay.getActive()) 
		{

			
			new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					finalMissionControllPanel.reachabilityMapOverlay = finalMissionControllPanel.updateSingleOverlay(finalMissionControllPanel.reachabilityMapOverlay,new ReachabilityOverlay(controller.agent
							.getBrain()));					
				}
			}).start();
		}
		
		if(this.waitingIconOverlay.getActive()){

			
			
			new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					finalMissionControllPanel.waitingIconOverlay = finalMissionControllPanel.updateSingleOverlay(finalMissionControllPanel.waitingIconOverlay,new WaitingIconOverlay(controller.agent));					
				}
			}).start();
		}
		
		if(this.tileSelectionOverlay.getActive()) {
			
			
			new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					finalMissionControllPanel.tileSelectionOverlay = finalMissionControllPanel.updateSingleOverlay(finalMissionControllPanel.tileSelectionOverlay,new TileSelectionOverlay(controller.agent));					
				}
			}).start();
		}
		
		if(this.motivationsOverlay.getActive()) {
			

			final ArrayList<JProgressBar> resBars = this.reservoirBars;
			final LinkedList<AbstractReservoir> resList = this.reservoirList;
//			new Thread(new Runnable()
//			{
//				@Override
//				public void run() 
//				{
					finalMissionControllPanel.motivationsOverlay = finalMissionControllPanel.updateSingleOverlay(finalMissionControllPanel.motivationsOverlay,finalMissionControllPanel.motivationsOverlay.updateOverlayInformation(resBars,resList,controller.agent));					
//				}
//			}).start();
		}
		
		if(this.objectsOverlay.getActive()) {
			objectsOverlay = finalMissionControllPanel.updateSingleOverlay(objectsOverlay, new ObjectsOverlay(controller.agent));
		}
		
		
	}

	private ToggleableOverlay updateSingleOverlay(
			ToggleableOverlay overlayToBeUpdated,
			ToggleableOverlay overlayToUpdateWith) {
		overlayToUpdateWith.setActive(overlayToBeUpdated.getActive());

		this.overlays.removeOverlay(overlayToBeUpdated);

		this.overlays.addOverlay(overlayToUpdateWith);
		return overlayToUpdateWith;
	}

	public void setOverlayActive(String overlayName, boolean state) {
		switch (overlayName) {
		case "Goal":
			goalOverlay.setActive(state);
			break;
		case "AStar":
			aStarOverlay.setActive(state);
			break;
		case "Heuristic":
			heuristicOverlay.setActive(state);
			break;
		case "ReachabilityMap":
			reachabilityMapOverlay.setActive(state);
			break;
		case "SpokenText":
			spokenTextOverlay.setActive(state);
			break;
		case "Waiting":
			waitingIconOverlay.setActive(state);
			break;
		case "Selection":
			tileSelectionOverlay.setActive(state);
			break;
		case "Motivations":
			motivationsOverlay.setActive(state);
			break;
		case "Message":
			messageOverlay.setActive(state);
			break;
		case "Objects" : 
			objectsOverlay.setActive(state);
			break;
			
		}
		
			
	}

	public void setReservoirValues() {
		if (controller.agent.getPlanner() instanceof ReservoirMultiplePlanner) {
			LinkedList<AbstractReservoir> reservoirList = ((ReservoirMultiplePlanner) controller.agent
					.getPlanner()).getReservoirList();
			int i = 0;
			double sum = 0;
			for (AbstractReservoir reservoir : reservoirList)
				sum += reservoir.getValue();
			
			if(sum == 0) {
				((MotivationsOverlay)this.motivationsOverlay).setGoalReservoir(null);
			}
			for (AbstractReservoir reservoir : reservoirList) {
				this.reservoirBars.get(i).setValue(
						(int) (reservoir.getValue() / sum * 100));
				// this.reservoirBars.get(i).repaint(); //CHECK: not necessary.
				// its repainted onTick.
				i++;
			}
		}
	}
	/*
	private IGridRectRenderer createReachabilityRenderer() 
	{

		
		return new ReachabilityRectRenderer(controller.agent.getBrain().levelSceneAdapter.getSimulatedLevelScene().getReachabilityMap());
	} */

	private IGridRectRenderer createHeuristicRenderer() 
	{
		GlobalCoarse basePoint = new ObservationRelativeCoarse(0, 0)
				.toGlobalCoarse(aStarSimulator
						.getPlayerPositionAtStartOfSearch());
		IGridRectRenderer heuristicRender;
		WorldNodeHeuristic heuristic = aStarSimulator.getHeuristic();
		if (heuristic instanceof FineGrainPotentialField) 
		{
			heuristicRender = new FineGrainPotentialFieldRectRenderer(
					(FineGrainPotentialField) heuristic,
					aStarSimulator.getHeuristic(), basePoint.x, basePoint.y);
		} else {
			heuristicRender = new HeuristicGridRenderer(heuristic, basePoint.x,
					basePoint.y);
		}
		return heuristicRender;
	}
	public List<JCheckBox> getAllOverLayCheckBoxes() {
		return allOverLayCheckBoxes;
	}
}
