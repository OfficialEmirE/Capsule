package net.capsule.update;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.capsule.update.util.UpdateManager;
import net.capsule.update.util.Util;
import net.capsule.update.util.VersionChecker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateFrame extends JFrame {
	private static final File capsuleExecLocation = new File(Util.getDirectory() + "jars/Capsule.jar");
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JProgressBar bar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					UpdateFrame frame = new UpdateFrame();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
					
					UpdateManager um = UpdateManager.instance;
					um.getRepoVersionAndDownloadURL();
					um.installAndRunUpdate((dp) -> {
						frame.bar.setValue(dp.percent());
						
						if (dp.isFinished()) {
							frame.dispose();
							try {
								VersionChecker.saveUsingLatestVersion();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							try {
								StringBuilder cpBuilder = new StringBuilder();
								cpBuilder.append(capsuleExecLocation.getAbsolutePath());
								for (File files : um.getCapsuleLibs()) {
									cpBuilder.append(";" + files.getAbsolutePath());
								}
								
								Process p = Runtime.getRuntime().exec(new String[] {
										"java",
										"-cp",
										cpBuilder.toString(),
										"net.capsule.Capsule"
								});
								// Get the error stream
					            InputStreamReader isr = new InputStreamReader(p.getErrorStream());
					            BufferedReader br = new BufferedReader(isr);

					            String line;
					            while ((line = br.readLine()) != null) {
					                System.out.println("Error: " + line);
					            }

					            // Wait for the process to finish
					            int exitCode = p.waitFor();
					            System.out.println("Exit Code: " + exitCode);
							} catch (IOException | InterruptedException e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(frame, "Launch Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
								frame.dispose();
							}
						}
					}, (crash) -> {
						crash.printStackTrace();
						JOptionPane.showMessageDialog(frame, "Update Error: " + crash.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						frame.dispose();
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public UpdateFrame() {
		setUndecorated(true);
		setResizable(false);
		setTitle("Capsule - Update");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 397);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel downPanel = new JPanel();
		downPanel.setBackground(new Color(230, 230, 230));
		contentPane.add(downPanel, BorderLayout.SOUTH);
		downPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		bar = new JProgressBar();
		bar.setPreferredSize(new Dimension(500,30));
		downPanel.add(bar);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(100, 33));
		cancelButton.setEnabled(false);
		downPanel.add(cancelButton);
		
		JPanel panel = new ImagePanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel statusText = new JLabel("Installing Capsule");
		statusText.setOpaque(true);
		statusText.setBackground(new Color(255, 255, 255));
		statusText.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
		statusText.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(statusText, BorderLayout.SOUTH);
	}

}
