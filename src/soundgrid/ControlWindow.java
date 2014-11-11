package soundgrid;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ControlWindow extends JFrame {
	SoundGrid mainWindow;
	
	public ControlWindow(SoundGrid sg) {
		mainWindow = sg;

		JPanel contentPane = new JPanel();
		setContentPane(contentPane);

		
		JButton btnStart = new JButton("Start");
		btnStart.setActionCommand("start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand() == "start") {
					// Start the sequence
                    mainWindow.playSequenceOfLength(2);
				}
			}
		});
		contentPane.add(btnStart);
		
	}

}
