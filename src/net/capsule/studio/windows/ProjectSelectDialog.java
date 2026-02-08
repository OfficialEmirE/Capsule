package net.capsule.studio.windows;

import javax.swing.JDialog;
import javax.swing.JPanel;

import net.capsule.studio.EditorUtil;
import net.capsule.util.GameProject;
import net.capsule.util.ImagePanel;
import net.capsule.util.Util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.border.TitledBorder;

import org.json.JSONException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ProjectSelectDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private GameProject data;
	
	/**
	 * Create the dialog.
	 */
	public ProjectSelectDialog(java.awt.Frame frame) {
		super(frame, true);
		
		setTitle("Capsule Studio - Select Project");
		setBounds(100, 100, 630, 781);
		setResizable(false);
		
		JPanel titleBar = new JPanel();
		titleBar.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		FlowLayout flowLayout = (FlowLayout) titleBar.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		getContentPane().add(titleBar, BorderLayout.NORTH);
		
		BufferedImage img = Util.getImageWeb(URI.create("http://capsule.net.tr/CapsuleStudioLogo.png"));
		JPanel studioLogo = new ImagePanel(Util.scaleImage(img, 251, 51));
		titleBar.add(studioLogo);
		
		JLabel lblNewLabel = new JLabel("Welcome to Studio 2.0");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		titleBar.add(lblNewLabel);
		
		JPanel buttons = new JPanel();
		buttons.setPreferredSize(new Dimension(325, 10));
		getContentPane().add(buttons, BorderLayout.EAST);
		buttons.setLayout(new GridLayout(15, 1, 0, 0));
		
		JButton newProjectButton = new JButton("New Project");
		newProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewProjectDialog dialog = new NewProjectDialog(frame);
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.setVisible(true);
				
				if (dialog.isCancelled())
					return;
				
				data = dialog.getData();
				dispose();
			}
		});
		buttons.add(newProjectButton);
		
		JButton openProjectButton = new JButton("Open Project");
		openProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = EditorUtil.openSelectFolderDialog();
				if (file == null) {
					return;
				}
				
				try {
					data = GameProject.loadProject(file);
				} catch (JSONException | IOException e1) {
					JOptionPane.showMessageDialog(frame, "Error: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
					return;
				}
				
				dispose();
			}
		});
		buttons.add(openProjectButton);
		
		JButton cloneProjectButton = new JButton("Clone Project");
		buttons.add(cloneProjectButton);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new GridLayout(30, 0, 0, 0));
		
		JLabel text = new JLabel("Previously Opened Projects");
		panel.add(text);
		
		JButton btnNewButton = new JButton("New button");
		panel.add(btnNewButton);
	}

	public GameProject getGameProject() {
		return data;
	}
}
