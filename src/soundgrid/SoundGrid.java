package soundgrid;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;

public class SoundGrid extends JFrame implements ActionListener{

	private static final long serialVersionUID = 7245538285112240479L;
	private JPanel contentPane;
	private JButton buttonArray[][] = new JButton[8][8];
	private File audioFileArray[][] = new File[8][8];

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

	/**
	 * Create the frame.
	 */
	public SoundGrid() {
		setTitle("SoundGrid");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 750);
		setLocationRelativeTo(null);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(8, 8, 0, 0));
		
		for (int row=0; row < 8; ++row) {
			for (int column=0; column < 8; ++column) {
				JButton button = new JButton(Integer.toString((row*8) + column));
				button.addActionListener(this);
				button.setActionCommand(String.format("%d", (row*8) + column));
				buttonArray[row][column] = button;
				contentPane.add(button);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.printf("Button %s pressed\n", e.getActionCommand());
	}

}
