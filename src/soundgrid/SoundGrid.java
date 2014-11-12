package soundgrid;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;

import java.awt.Color;

public class SoundGrid extends JFrame implements ActionListener{

	private static final long serialVersionUID = 7245538285112240479L;
	public static final int SEQUENCE_LENGTH = 5;
	private JPanel contentPane;

	private JButton buttonArray[][] = new JButton[8][8];
	private int gridSequenceArray[][] = {{1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}};

	private int currentSequenceIndex = 0;
	private int currentSequenceLength = 2;
	private int currentPlaybackIndex = 0;
	
	private Clip buttonPressClip;
	private Clip sequenceEndClip;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SoundGrid frame = new SoundGrid();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void setUpRandomSequence() {
		for (int i=0; i < SEQUENCE_LENGTH; ++i) {
            Random random = new Random();
            int row = random.nextInt((7 - 0) + 1) + 0;
            int column = random.nextInt((7 - 0) + 1) + 0;
            int pos[] = {row, column};
            gridSequenceArray[i] = pos; 
		}
	}
	
	private void setUpAudioClips() {
		try {
			File beepFile = new File(this.getClass().getResource("/beep.wav").toURI());
			File seqFinishedFile = new File(this.getClass().getResource("/seqFinished.wav").toURI());
			
			AudioInputStream beepStream = AudioSystem.getAudioInputStream(beepFile);
			AudioInputStream seqFinishedStream = AudioSystem.getAudioInputStream(seqFinishedFile);
			
			DataLine.Info beepInfo = new DataLine.Info(Clip.class, beepStream.getFormat());
			DataLine.Info seqFinishedInfo = new DataLine.Info(Clip.class, seqFinishedStream.getFormat());
			
			buttonPressClip = (Clip)AudioSystem.getLine(beepInfo);
			sequenceEndClip = (Clip)AudioSystem.getLine(seqFinishedInfo);
			
			LineListener listener = new LineListener() {
				@Override
				public void update(LineEvent event) {
					if (event.getType() == LineEvent.Type.STOP) {
						Clip c = (Clip)event.getLine();
						c.setFramePosition(0);
					}
				}
			};
			
			buttonPressClip.addLineListener(listener);
			sequenceEndClip.addLineListener(listener);
			
			buttonPressClip.open(beepStream);
			sequenceEndClip.open(seqFinishedStream);

		} catch (URISyntaxException | UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the frame.
	 * @throws URISyntaxException 
	 */
	
	/** TODO: Change sound to monotone beep **/
	public SoundGrid() throws URISyntaxException {
		setResizable(false);
		
		setTitle("SoundGrid");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 750);
		setLocationRelativeTo(null);
		
		ControlWindow controlWindow = new ControlWindow(this);
		controlWindow.pack();
		controlWindow.setVisible(true);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(8, 8, 0, 0));
		
		// Uncomment to randomize sequence
		setUpRandomSequence();
		
		// Set up audio
		setUpAudioClips();
		
		for (int row=0; row < 8; ++row) {
			for (int column=0; column < 8; ++column) {
				JButton button = new JButton();
				button.addActionListener(this);
				button.setActionCommand(String.format("%d%d", row, column));
				button.setBorder(null);
				button.setBackground(new Color(0, 0, 0, 0));
				button.setBorderPainted(false);
				button.setIcon(new ImageIcon(this.getClass().getResource("/white_button.png")));
				buttonArray[row][column] = button;
				contentPane.add(button);
				
			}
		}
	}
	
	public void playSequenceOfLength(final int length) {
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				if ((currentSequenceIndex - 1) >= 0) {
					int prevPos[] = gridSequenceArray[currentSequenceIndex - 1];
					changeButtonImageForLocation(prevPos[0], prevPos[1], Color.WHITE);
				}
				
				if (currentSequenceIndex < length) {
                    int pos[] = gridSequenceArray[currentSequenceIndex];
                    changeButtonImageForLocation(pos[0], pos[1], Color.GREEN);
                    playButtonPressSound();
                    ++currentSequenceIndex;
				} else {
					timer.cancel();
					timer.purge();
				}
			}
		}, 0, 1000);
	}
	
	private void playButtonPressSound() {
		buttonPressClip.start();
	}
	
	private void changeButtonImageForLocation(int row, int column, Color color) {
		JButton button = buttonArray[row][column];
		
		if (color == Color.WHITE) {
            button.setIcon(new ImageIcon(this.getClass().getResource("/white_button.png")));
		} else {
            button.setIcon(new ImageIcon(this.getClass().getResource("/green_button.png")));
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		int sequencePos[] = gridSequenceArray[currentPlaybackIndex];
		
		final int pos[] = new int[2];
		pos[0] = Character.digit(e.getActionCommand().charAt(0), 10);
		pos[1] = Character.digit(e.getActionCommand().charAt(1), 10);
		
		if (currentPlaybackIndex > 0) {
			// If a button has already been pressed turn it off before we carry on
			int prevPos[] = gridSequenceArray[currentPlaybackIndex-1];
			changeButtonImageForLocation(prevPos[0], prevPos[1], Color.WHITE);
		}
		
		changeButtonImageForLocation(pos[0], pos[1], Color.GREEN);
		playButtonPressSound();
	
		if (Arrays.equals(sequencePos, pos) && currentPlaybackIndex == (currentSequenceLength-1)) {
			System.out.println("Correctly completed the sequence");
			sequenceEndClip.start();
			
			if (currentSequenceLength == SEQUENCE_LENGTH) {
				JOptionPane.showMessageDialog(this, "Unbelievable. You, *subject name here,* must be the pride of *subject hometown here.*",
						"Test Completed", JOptionPane.INFORMATION_MESSAGE);
			} else {

				Timer t = new Timer();
				t.schedule(new TimerTask() {
					
					@Override
					public void run() {
						changeButtonImageForLocation(pos[0], pos[1], Color.WHITE);
                        ++currentSequenceLength;
                        currentPlaybackIndex = 0;
                        currentSequenceIndex = 0;
                        changeButtonImageForLocation(pos[0], pos[1], Color.WHITE);
                        
                        // Wait a little bit before playing new sequence so we can turn off the last button
                        Timer t = new Timer();
                        t.schedule(new TimerTask() {
							
							@Override
							public void run() {
                                playSequenceOfLength(currentSequenceLength);	
							}
						}, 1000);
					}
				}, 500);
                
			}
		} else if (Arrays.equals(sequencePos, pos)) {
			System.out.println("Correct button pressed");
			++currentPlaybackIndex;
		} else {
			System.out.println("Wrong Button Pressed");
			JOptionPane.showMessageDialog(this, "Cake, and grief counseling, will be available at the conclusion of the test.", "Incorrect button pressed",
					JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
}
