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
package marioUI.speechSynthesis;

import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import mario.main.PlayRelease;
import marioUI.gui.overlays.SpeechBubbleOverlay;
import marioWorld.engine.sprites.Player;
import marioWorld.utils.ResettableCountDownLatch;

/**
 * Interface that utilizes espeak for text 2 speech production.
 * 
 * @author smihael, Katrin
 * 
 * @note Note that espeak has to be installed on the system in order for this
 *       class to function properly
 * 
 *       Ubuntu users should run the following command: sudo apt-get install
 *       espeak
 * 
 *       Windows users should download espeak from the official website and
 *       either add espeak to the PATH or create a file named espeak.bat in
 *       C:\Windows\System32 with the following content
 * @"C:\Program Files (x86)\eSpeak\command_line\espeak.exe" %*
 * 
 */

public class TextToSpeech {

	/**
	 * Stores strings that should be spoken in order of their priority
	 */
	private PriorityBlockingQueue<PriorityAnswerPair> queue;
	private ResettableCountDownLatch latch;
	private String path;
	//TODO: implement isQueueEmpty
	
	/*
	 * Usage:
	 * 
	 * TTS test = new TTS(); test.speak("text to be spoken", priority);
	 */
	public TextToSpeech() {
		this.queue = new PriorityBlockingQueue<PriorityAnswerPair>();
		latch = new ResettableCountDownLatch(1);
		Speaker speaker = new Speaker(queue,latch);

		/** starts new thread that starts espeak **/
		Thread thread = new Thread(speaker);
		//thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	public void speak(String text, int priority, String voice, int playerId, Player player) {		
		this.queue.add(new PriorityAnswerPair(priority, new MessageInfo(text, voice, playerId, player)));
	}

	private static AtomicInteger messageCounter = new AtomicInteger(0);
	
	/**
	 * Class that holds the priority and creation id. If priorities are same, messages shall be ordered by creation counter (first come first serve) 
	 * 
	 * @author Jan
	 *
	 */
	private class Priority implements Comparable<Priority> {
		private int prio,count;
		
		public Priority(int prio) {
			this.prio = prio;
			this.count = messageCounter.getAndIncrement();
		}

		@Override
		public int compareTo(Priority other) {
			int comp = Integer.compare(prio, other.prio);
			if (comp == 0)
				comp = Integer.compare(count, other.count);
			return comp;
		}
		
	}
	
	/**
	 * Entry that stores a priority and answer pair Comparable based on priority
	 * 
	 */
	private class PriorityAnswerPair extends SimpleEntry<Priority, MessageInfo>
			implements Comparable<PriorityAnswerPair> {

		private static final long serialVersionUID = 5415978770857869510L;

		public PriorityAnswerPair(Integer priority, MessageInfo answer) {
			super(new Priority(priority), answer);
		}

		@Override
		public int compareTo(PriorityAnswerPair other) {
			return this.getKey().compareTo(other.getKey());
		}
	}

	public class MessageInfo {

		public final String messageText;
		public final String voiceColor;
		public int playerIndex;
		public Player player;

		public MessageInfo(String text, String voice, int playerIndex, Player player) {
			this.messageText = text;
			this.voiceColor = voice;
			this.playerIndex = playerIndex;
			this.player = player;
		}

	}

	/**
	 * Class that starts the espeak process, started in new thread
	 * 
	 */
	private class Speaker implements Runnable {

		private PriorityBlockingQueue<PriorityAnswerPair> queue;
		private final ResettableCountDownLatch latch;
		Speaker(PriorityBlockingQueue<PriorityAnswerPair> queue, ResettableCountDownLatch latch) {
			this.queue = queue;
			this.latch = latch;
		}
		String[] possiblePaths = {"espeak",										//if path variable is correct
				"C:\\Program Files (x86)\\eSpeak\\command_line\\espeak.exe", 	//windows default path
				"/usr/local/bin/espeak"};															//mac default path

		@Override
		public void run() 
		{
			while(true) 
			{
				if(!queue.isEmpty()) 
				{
					latch.reset();
					speak(queue.peek().getValue());
					queue.remove();
				}
				else
				{					
					try 
					{
						latch.countDown();
						Thread.sleep(100);	//that should be fast enough
					} 
					catch (InterruptedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		/** starts an espeak process **/
		private void speak(MessageInfo msg) 
		{
			String lang = "en";
			String text = msg.messageText;
			String gender = msg.voiceColor;
			int playerIndex = msg.playerIndex;
			Player player = msg.player;
			
			boolean success = false;
			int counter = 0;
			while(!success) {
					
				
				ProcessBuilder pb = new ProcessBuilder(possiblePaths[counter], "-v" + lang + "+"
						+ gender, "-p50", "-s170", "-g3", "-a50", text);
				pb.redirectErrorStream(false);
				Process process = null;
				
				SpeechBubbleOverlay.setBubbleText(playerIndex, text, player, 0, player == null ? 0 : -(int)(player.getHeight()*1.5), false);
				
				try 
				{
					process = pb.start();
					process.waitFor(); // thread is blocked while current text is spoken
					success = true;
					
				} 
				catch (IOException e) 
				{
					counter++;
					if(counter==possiblePaths.length) {
						showErrorMessage();
						e.printStackTrace();
						System.exit(0);
					}
					continue;
					
				} 
				catch (InterruptedException i) 
				{
	//				i.printStackTrace();
					if(process != null)
					{
						process.destroy();
					}
				}
				SpeechBubbleOverlay.clearBubbleText(playerIndex);
//			}
		}

	}
	private void showErrorMessage() {
		 // for copying style
	    JLabel label = new JLabel();
	    Font font = label.getFont();

	    // create some css from the label's font
	    StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;");
		 JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">"//
		            + "TextToSpeech executable not found."
		            + "<br>Please make sure that the path variable leads to the espeak executable!"
		            + "<br>If you do not have espeak installed yet, please download it from <a href=http://espeak.sourceforge.net\">http://espeak.sourceforge.net</a>."
		            + "<br>Linux users can use the following command: 'sudo apt-get install espeak' " //
		            + "</body></html>");

		    // handle link events
		    ep.addHyperlinkListener(new HyperlinkListener()
		    {
		        @Override
		        public void hyperlinkUpdate(HyperlinkEvent e)
		        {
		            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
		            	if (Desktop.isDesktopSupported()) {
		            	      Desktop desktop = Desktop.getDesktop();
		            	      try {
		            	        desktop.browse(e.getURL().toURI());
		            	      } catch (IOException f) {
		            	        JOptionPane.showMessageDialog(null,
		            	            "Failed to launch the link, your computer is likely misconfigured.",
		            	            "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
		            	      } catch (URISyntaxException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
		            	    } else {
		            	      JOptionPane.showMessageDialog(null,
		            	          "Java is not able to launch links on your computer.",
		            	          "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
		            	    }
//		                ProcessHandler.launchUrl(e.getURL().toString()); // roll your own link launcher or use Desktop if J6+
		        }
		    });
		    ep.setEditable(false);
		    ep.setBackground(label.getBackground());

//
//		JOptionPane.showMessageDialog(PlayRelease.startGui, "TextToSpeech executable not found.\nPlease make sure that the path variable leads to the espeak executable!\nIf you do not have espeak installed yet, please downlaod it from '<HTML>http://espeak.sourceforge.net/</HTML>'."
//				+ "\n Linux users can use the following command: sudo apt-get install espeak","Error occurred",JOptionPane.ERROR_MESSAGE);
		JOptionPane.showMessageDialog(PlayRelease.startGui, ep,"Error occurred",JOptionPane.ERROR_MESSAGE);
	}
	
	}
	public ResettableCountDownLatch getLatch() {
		// TODO Auto-generated method stub
		return this.latch;
	}
	
	

}
