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
package marioUI.gui.microphoneVolumeControl;
/*
 * Created on October 2, 2010
 *
 * Creates a java UI that we can use to select a sound device
 *
 */

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import marioUI.gui.LayoutHelper;

public class SoundDeviceSelectionUI {

	private final JFrame frame;
	private JLabel lblSelect, lblGain;
	private JComboBox deviceList;
	private JButton btnSave, btnCancel;
	private JPanel panel1, panel2, soundLevelVis, gainSettingsContainer, gainExample;
	private JSlider gainSlider;
	private Container cp;

	private List<String> soundDevices;
	private int initialDeviceIndex = 0;
	private boolean Saving = false;
	private boolean readyForUIUpdates = false;
	
	private int lastLevel;

	private SoundInputDeviceControl sDeviceControl;

	// --- Custom Events
	protected EventListenerList listenerList = new EventListenerList();

	public SoundDeviceSelectionUI(List<String> _InputSoundDevices, SoundInputDeviceControl sDeviceControl) {
		soundDevices = _InputSoundDevices;
		this.sDeviceControl = sDeviceControl;
		frame = new JFrame("Select Recording Device");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(500, 300));
		setupUI();
	}

	public void listenForPresenceEvents(CustomPresenceListener _listener) {
		listenerList.add(CustomPresenceListener.class, _listener);
	}

	public void removeEventListener(CustomPresenceListener _listener) {
		listenerList.remove(CustomPresenceListener.class, _listener);
	}

	private void sendOutPresenceUpdates(String _FullJIDAndResource, String _NewPresence) {
		Object[] listeners = listenerList.getListenerList();

		// Empty out the listener list
		// Each listener occupies two elements - the first is the listener class
		// and the second is the listener instance
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == CustomPresenceListener.class) {
				((CustomPresenceListener) listeners[i + 1])
						.presenceEventOccurred(new CustomPresenceEvent(this, _FullJIDAndResource, _NewPresence));
			}
		}
	}

	private void setupUI() {

		lblSelect = new JLabel("Select your recording device:");
		lblGain = new JLabel("Adjust slider to keep the bar green: ");

		deviceList = new JComboBox(soundDevices.toArray());
		// Set the last index selected in the config file
		if (initialDeviceIndex <= soundDevices.size()) {
			deviceList.setSelectedIndex(initialDeviceIndex);
		}

		deviceList.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String deviceName = (String) cb.getSelectedItem();

				// Execute when button is pressed
				// System.out.println("You changed the drop down list to: " +
				// deviceName);

				// Dispatch an event to the parent
				sendOutPresenceUpdates(deviceName, "");
			}
		});

		btnCancel = new JButton("Ok");
		btnCancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				System.out.println("You clicked the ok button");
				sDeviceControl.closeMicrophone();
				frame.setVisible(false);

			}
		});

		gainSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		gainSlider.setValue(getDeviceSensitivity());
		gainSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				// Execute when button is pressed
				System.out.println("You changed the slider to: " + (int) source.getValue());
				// updateSoundLevel( (int)source.getValue() );
				setDeviceSensitivity((int) source.getValue());

			}

		});

		panel1 = new JPanel();
		panel2 = new JPanel();
		soundLevelVis = new JPanel();
		gainSettingsContainer = new JPanel();
		gainExample = new JPanel();
		gainExample.setBackground(Color.RED);

		panel1.add(lblSelect);
		panel1.add(deviceList);

		panel2.add(btnCancel);
		gainSettingsContainer.add(lblGain);
		gainSettingsContainer.add(gainExample);
		gainSettingsContainer.add(gainSlider);

		cp = frame.getContentPane();
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		cp.add(panel1);
		cp.add(gainSettingsContainer);
		cp.add(soundLevelVis);
		cp.add(panel2);
		
//		cp.setLayout(new GridBagLayout());
//		LayoutHelper.putGrid(cp,panel1,0,0,1,1);
//		LayoutHelper.putGrid(cp,gainSettingsContainer,1,0,1,1);
//		LayoutHelper.putGrid(cp,soundLevelVis,0,2,1,1);
//		LayoutHelper.putGrid(cp,panel2,0,3,1,1);
		
		
	}

	// _InputLevel will be 0 - ? 0 for nothing
	public void updateSoundLevel(int _InputLevel) {
		if (readyForUIUpdates == true) {
			int finalHeight = (int)( (lastLevel *0.9f )+ ((_InputLevel * 2)*0.1f));
			Graphics g = soundLevelVis.getGraphics();
	
			if (finalHeight < 30 || finalHeight > 70) {
				g.setColor(Color.red);
			} else {
				g.setColor(Color.green);
			}
			lastLevel = finalHeight;
			// Clear any current graphics
			
			g.clearRect(0, 0, soundLevelVis.getWidth(), soundLevelVis.getHeight());
			g.fillRect(soundLevelVis.getWidth() / 2, 0, finalHeight, 25);
		}
		// soundLevelVis.paintComponent(g);
	}
	public void displayNoAudioFormatMessage(){
		JOptionPane.showMessageDialog(frame,
			    "Could not find supported audio format.\n"
			    + "Please select a different input device for the calibration! ",
			    "Unsupported Audio Format",
			    JOptionPane.WARNING_MESSAGE);
	}

	

	public int getDeviceSensitivity() {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		for (int i = 0; i < mixerInfos.length; i++) {
			Mixer mixer = AudioSystem.getMixer(mixerInfos[i]);
			int maxLines = mixer.getMaxLines(Port.Info.MICROPHONE);
			Port lineIn = null;
			FloatControl volCtrl = null;
			if (maxLines > 0) {
				try {
					if (mixer.isLineSupported(Port.Info.LINE_IN)) {
						lineIn = (Port) mixer.getLine(Port.Info.LINE_IN);
						lineIn.open();
					} else if (mixer.isLineSupported(Port.Info.MICROPHONE)) {
						lineIn = (Port) mixer.getLine(Port.Info.MICROPHONE);
						lineIn.open();
					} else {
						System.out.println("Unable to get Input Port");
						return 0;
					}
					lineIn.getControls();

					if (lineIn.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
						System.out.println("test");
					}

					final CompoundControl cc = (CompoundControl) lineIn.getControls()[0];
					final Control[] controls = cc.getMemberControls();
					for (final Control c : controls) {
						if (c instanceof FloatControl) {
							System.out.println("BEFORE LINE_IN VOL = " + ((FloatControl) c).getValue());
							return (int) (((FloatControl) c).getValue() * 100); 
						}
					}
				} catch (final Exception e) {
					System.out.println(e);
					continue;
				}
			}
		}
		return 0;
	}

	public void setDeviceSensitivity(final int sensitivity) {

		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		

		for (int i = 0; i < mixerInfos.length; i++) {
			Mixer mixer = AudioSystem.getMixer(mixerInfos[i]);
			int maxLines = mixer.getMaxLines(Port.Info.MICROPHONE);
			Port lineIn = null;
			FloatControl volCtrl = null;
			if (maxLines > 0) {
				try {
					if (mixer.isLineSupported(Port.Info.LINE_IN)) {
						lineIn = (Port) mixer.getLine(Port.Info.LINE_IN);
						lineIn.open();
					} else if (mixer.isLineSupported(Port.Info.MICROPHONE)) {
						lineIn = (Port) mixer.getLine(Port.Info.MICROPHONE);
						lineIn.open();
					} else {
						System.out.println("Unable to get Input Port");
						return;
					}
						
						
					lineIn.getControls();

					if (lineIn.isControlSupported(FloatControl.Type.VOLUME)) {
						System.out.println("test");
					}

					final CompoundControl cc = (CompoundControl) lineIn.getControls()[0];
					final Control[] controls = cc.getMemberControls();
					
//					final Control[] controls = lineIn.getControls();
					for (final Control c : controls) {
						
						if (c instanceof FloatControl) {
							if(((FloatControl) c).getType().equals(FloatControl.Type.VOLUME)) {
								System.out.println("BEFORE LINE_IN VOL = " + ((FloatControl) c).getValue());
								((FloatControl) c).setValue((float) sensitivity / 100);
								System.out.println("AFTER LINE_IN VOL = " + ((FloatControl) c).getValue());
							}
							
						}
					}
				} catch (final Exception e) {
					System.out.println(e);
					continue;
				}
			}
		}

	}

	public void display() {
		frame.pack();
		frame.setVisible(true);
		readyForUIUpdates = true;

		sendOutPresenceUpdates(soundDevices.get(0), "");
	}
}
