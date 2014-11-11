package soundgrid;

import java.awt.EventQueue;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;

import java.awt.Color;

public class SoundGrid extends JFrame implements ActionListener{

	private static final long serialVersionUID = 7245538285112240479L;
	public static final int SEQUENCE_LENGTH = 5;
	private JPanel contentPane;

	private JButton buttonArray[][] = new JButton[8][8];
	private int noteArray[][] = {
			{12, 13, 14, 15, 16, 17, 18, 19},
			{28, 29, 30, 31, 32, 33, 34, 35},
			{44, 45, 46, 47, 48, 49, 50, 51},
			{60, 61, 62, 63, 64, 65, 66, 67},
			{76, 77, 78, 79, 80, 81, 82, 83},
			{92, 93, 94, 95, 96, 97, 98, 99},
			{108, 109, 110, 111, 112, 113, 114, 115},
			{124, 125, 126, 127, 128, 129, 130, 131}
	};
	private int gridSequenceArray[][] = {{1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}};

	private int currentSequenceIndex = 0;
	private int currentSequenceLength = 2;
	private int currentPlaybackIndex = 0;
	
	private	MidiDevice device;

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
	
	private void setUpMidiDevice() {
		MidiDevice.Info infos[] = MidiSystem.getMidiDeviceInfo();
		for (MidiDevice.Info info : infos) {
			if (info.getName().equals("Bus 1")) {
                try {
					device = MidiSystem.getMidiDevice(info);
                    device.open();
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Create the frame.
	 * @throws URISyntaxException 
	 */
	public SoundGrid() throws URISyntaxException {
		
		setTitle("SoundGrid");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 750);
		setLocationRelativeTo(null);
		
		ControlWindow controlWindow = new ControlWindow(this);
		controlWindow.pack();
		controlWindow.setVisible(true);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(8, 8, 0, 0));
		
		// Set up audio
		setUpMidiDevice();
		
		for (int row=0; row < 8; ++row) {
			for (int column=0; column < 8; ++column) {
				JButton button = new JButton(Integer.toString((row*8) + column));
				button.addActionListener(this);
				button.setActionCommand(String.format("%d%d", row, column));
				button.setBackground(Color.RED);
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
					stopSoundInPosition(prevPos[0], prevPos[1]);
				}
				
				if (currentSequenceIndex < length) {
                    int pos[] = gridSequenceArray[currentSequenceIndex];
                    playSoundInPosition(pos[0], pos[1]);
                    ++currentSequenceIndex;
				} else {
					timer.cancel();
					timer.purge();
				}
			}
		}, 0, 1000);
	}
	
	public void playSoundInPosition(int row, int column) {
        int note = noteArray[row][column];

        ShortMessage msg = new ShortMessage();
        try {
			msg.setMessage(ShortMessage.NOTE_ON, 0, note, 70);
            device.getReceiver().send(msg, -1);
		} catch (InvalidMidiDataException | MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	private void stopSoundInPosition(int row, int column) {
		int note = noteArray[row][column];
		
		ShortMessage msg = new ShortMessage();
        try {
			msg.setMessage(ShortMessage.NOTE_OFF, 0, note, 70);
            device.getReceiver().send(msg, -1);
		} catch (InvalidMidiDataException | MidiUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int sequencePos[] = gridSequenceArray[currentPlaybackIndex];
		
		final int pos[] = new int[2];
		pos[0] = Character.digit(e.getActionCommand().charAt(0), 10);
		pos[1] = Character.digit(e.getActionCommand().charAt(1), 10);
		
		playSoundInPosition(pos[0], pos[1]);
	
		if (Arrays.equals(sequencePos, pos) && currentPlaybackIndex == (currentSequenceLength-1)) {
			System.out.println("Correctly completed the sequence");
			
			if (currentSequenceLength == SEQUENCE_LENGTH) {
				JOptionPane.showMessageDialog(this, "Unbelievable. You, *subject name here,* must be the pride of *subject hometown here.*",
						"Test Completed", JOptionPane.INFORMATION_MESSAGE);
			} else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                ++currentSequenceLength;
                currentPlaybackIndex = 0;
                currentSequenceIndex = 0;
                playSequenceOfLength(currentSequenceLength);
			}
		} else if (Arrays.equals(sequencePos, pos)) {
			System.out.println("Correct button pressed");
			++currentPlaybackIndex;
			
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				
				@Override
				public void run() {
					stopSoundInPosition(pos[0], pos[1]);
				}
			}, 1000);
		} else {
			System.out.println("Wrong Button Pressed");
			JOptionPane.showMessageDialog(this, "Cake, and grief counseling, will be available at the conclusion of the test.", "Incorrect button pressed",
					JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
}
