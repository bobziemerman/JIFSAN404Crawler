import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

/**
 * @author Bob Zimmerman
 */
public class CrawlerGUI {
	
	private static final int UPDATE_DELAY_MS = 1200;  // time to wait between updates
	
	private MyQueue<URL> linkQueue;
	private MySet<URL> beenThere;
	private MySet<URL> doneThat = new MySet<URL>();
	private MySet<URL> badURLs;
	private ExtractorThread[] extractors;
	private JTextField startEntry = new JTextField("http://jifsan.umd.edu/", 60);
	private JButton goButton = new JButton("GO!");
	private JLabel beenThereCount = new JLabel("Links Discovered: 0");
	private JLabel linkQueueCount = new JLabel("Links in Queue: 0");
	private JLabel badURLsCount = new JLabel("404s found: 0");
	private JLabel caption = new JLabel("The following URLS are currently being scanned by ExtractorThreads:");
	private JLabel[] threadLocations;
	
	
	public CrawlerGUI(MyQueue<URL> linkQueue, MySet<URL> beenThere, MySet<URL> badURLs, ExtractorThread [] extractors) {
		
		this.linkQueue = linkQueue;
		this.beenThere = beenThere;
		this.extractors = extractors;
		this.badURLs = badURLs;
		
		threadLocations = new JLabel[extractors.length];
		for (int i = 0; i < extractors.length; i++)
			threadLocations[i] = new JLabel("Not used");   // hint
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				createAndShowGUI();
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	private void createAndShowGUI() {
		
		JFrame frame = new JFrame("Crawler");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				synchronized(extractors) {
					
					for (int i = 0; i < extractors.length; i++) {
						if (extractors[i] != null) {
							try {
								extractors[i].join();
							}
							catch(InterruptedException xx) {}
						}
					}
					
					beenThere.clear();
					doneThat.clear();
					linkQueue.clear();
					
					for (int j = 0; j < extractors.length; j++) 
						extractors[j] = null;
					
					try {
						linkQueue.enqueue(new URL(startEntry.getText()));
						beenThere.add(new URL(startEntry.getText()));
					}
					catch(MalformedURLException ex) {}
					
				}
				
			}
		});
		
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				beenThereCount.setText("Links Discovered: " + beenThere.size());
				linkQueueCount.setText("Links in Queue: " + linkQueue.size());
				badURLsCount.setText("404s found: " + badURLs.size());
				for (int i = 0; i < extractors.length; i++) {
					if (extractors[i] != null && extractors[i].isAlive())
						threadLocations[i].setText(extractors[i].getCurrentURL());
					else
						threadLocations[i].setText("Unused");
				}
			}
		};
		
		new javax.swing.Timer(UPDATE_DELAY_MS, listener).start();
		
		frame.getContentPane().setLayout(new GridLayout(3 + extractors.length, 1));
		
		JPanel top = new JPanel();
		top.add(startEntry);
		top.add(goButton);
		frame.getContentPane().add(top);
		
		JPanel middle = new JPanel();
		middle.setLayout(new GridLayout(3, 1));
		middle.add(beenThereCount);
		middle.add(linkQueueCount);
		middle.add(badURLsCount);
		frame.getContentPane().add(middle);
		
		frame.getContentPane().add(caption);
		
		for (int i = 0; i < extractors.length; i++) {
			frame.getContentPane().add(threadLocations[i]);
		}
		
		frame.pack();
		frame.move(100,100);
		frame.setVisible(true);
	}
}
