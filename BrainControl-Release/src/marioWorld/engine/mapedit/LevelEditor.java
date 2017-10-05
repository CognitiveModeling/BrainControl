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
 * 72076 T�bingen 
 * cm-sekretariat -at- inf.uni-tuebingen.de
 ******************************************************************************/
package marioWorld.engine.mapedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import marioWorld.engine.GlobalOptions;
import marioWorld.engine.level.Level;
import marioWorld.utils.ResourceStream;
import marioWorld.utils.UserDir;

public class LevelEditor extends JFrame implements ActionListener {
	private static final long serialVersionUID = 7461321112832160393L;

	private JButton loadButton;
	private JButton saveButton;
	private JTextField nameField;
	private LevelEditView levelEditView;
	private TilePicker tilePicker;
	
	private JLabel cursorPosition;

	private JCheckBox[] bitmapCheckboxes = new JCheckBox[8];
	private final String tilesPath;

	public LevelEditor() {
		super("Map Edit");
		
		//set global screen resolution to default (TODO: this is dirty)
		GlobalOptions.xScreen = 320;
		GlobalOptions.yScreen = GlobalOptions.xScreen * 3 / 4;	//4:3 is needed!
		GlobalOptions.resolution_factor = GlobalOptions.xScreen / 320;

		
		this.tilesPath = ResourceStream.DIR_WORLD_RESOURCES+"/engine/mapedit/";
		try {
			Level.loadBehaviors(new DataInputStream(ResourceStream.getResourceStream(tilesPath + "tiles.dat")));
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.toString(), "Failed to load tile behaviors", JOptionPane.ERROR_MESSAGE);
		} 

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width * 8 / 10, screenSize.height * 8 / 10);
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		cursorPosition = new JLabel();
		cursorPosition.setText("Cursor position: undefined");
		
		tilePicker = new TilePicker();
		JPanel tilePickerPanel = new JPanel(new BorderLayout());
		tilePickerPanel.add(BorderLayout.WEST, tilePicker);
		tilePickerPanel.add(BorderLayout.CENTER, buildBitmapPanel());
		tilePickerPanel.setBorder(new TitledBorder(new EtchedBorder(), "Tile picker"));		

		JPanel lowerPanel = new JPanel(new BorderLayout());
		lowerPanel.add(BorderLayout.EAST, tilePickerPanel);
				
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.EAST,cursorPosition);
		topPanel.add(BorderLayout.WEST,buildButtonPanel());
		
		JPanel borderPanel = new JPanel(new BorderLayout());
		levelEditView = new LevelEditView(tilePicker, cursorPosition);
		borderPanel.add(BorderLayout.CENTER, new JScrollPane(levelEditView));		
		borderPanel.add(BorderLayout.SOUTH, lowerPanel);
		borderPanel.add(BorderLayout.NORTH, topPanel);
		
		setContentPane(borderPanel);

		tilePicker.addTilePickChangedListener(this);
	}

	public JPanel buildBitmapPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 1));
		for (int i = 0; i < 8; i++) {
			bitmapCheckboxes[i] = new JCheckBox(Level.BIT_DESCRIPTIONS[i]);
			panel.add(bitmapCheckboxes[i]);
			if (Level.BIT_DESCRIPTIONS[i].startsWith("- "))
				bitmapCheckboxes[i].setEnabled(false);

			final int id = i;
			bitmapCheckboxes[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int bm = Level.TILE_BEHAVIORS[tilePicker.pickedTile & 0xff] & 0xff;
					bm &= 255 - (1 << id);
					if (bitmapCheckboxes[id].isSelected())
						bm |= (1 << id);
					Level.TILE_BEHAVIORS[tilePicker.pickedTile & 0xff] = (byte) bm;

					try {
						System.out.println("saving to: " + tilesPath);
						Level.saveBehaviors(new DataOutputStream(new FileOutputStream(tilesPath)));
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(LevelEditor.this, e.toString(), "Failed to load tile behaviors", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return panel;
	}

	public JPanel buildButtonPanel() {
		loadButton = new JButton("Load");
		saveButton = new JButton("Save");
		nameField = new JTextField("test.lvl", 10);
		loadButton.addActionListener(this);
		saveButton.addActionListener(this);
		JPanel panel = new JPanel();
		panel.add(nameField);
		panel.add(loadButton);
		panel.add(saveButton);
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == loadButton) {
				levelEditView.setLevel(Level.load(new DataInputStream(ResourceStream.getResourceStream(tilesPath + nameField.getText().trim()))));
			}
			if (e.getSource() == saveButton) {
				levelEditView.getLevel().save(new DataOutputStream(new FileOutputStream(UserDir.getUserFile((tilesPath + nameField.getText().trim())))));
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString(), "Failed to load/save", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) {
		new LevelEditor().setVisible(true);
	}

	public void setPickedTile(byte pickedTile) {
		int bm = Level.TILE_BEHAVIORS[pickedTile & 0xff] & 0xff;

		for (int i = 0; i < 8; i++) {
			bitmapCheckboxes[i].setSelected((bm & (1 << i)) > 0);
		}
	}
}
