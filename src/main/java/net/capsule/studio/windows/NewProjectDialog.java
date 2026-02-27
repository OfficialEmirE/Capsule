package net.capsule.studio.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.capsule.studio.EditorUtil;
import net.capsule.studio.GameData;
import net.capsule.studio.StudioPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

public class NewProjectDialog extends JDialog implements IProjectDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField projectName;
	private JTextField projectPath;
	
	private FutureTask<GameData> gameDataFuture;

	/**
	 * Create the dialog.
	 */
	public NewProjectDialog(java.awt.Frame frame) {
		super(frame, true);
		gameDataFuture = new FutureTask<>(this);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				gameDataFuture.cancel(false);
				
				dispose();
			}
		});
		
		setTitle("Create New Project");
		setBounds(100, 100, 471, 519);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel createNewProjectLabel = new JLabel("Create New Project");
		createNewProjectLabel.setFont(new Font("Tahoma", Font.BOLD, 30));
		
		JLabel projectNameLabel = new JLabel("Project Name");
		
		JLabel projectPathLabel = new JLabel("Project World File");
		
		projectName = new JTextField("NewProject");
		projectName.setColumns(10);
		
		JPanel panel = new JPanel();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(10)
					.addComponent(projectNameLabel, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(projectPathLabel, GroupLayout.PREFERRED_SIZE, 404, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(33, Short.MAX_VALUE))
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(createNewProjectLabel, GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE))
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(projectName, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addComponent(createNewProjectLabel, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(projectNameLabel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(projectName, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(projectPathLabel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(263, Short.MAX_VALUE))
		);
		panel.setLayout(new BorderLayout(0, 0));
		
		JButton btnNewButton = new JButton("Select File");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				var file = EditorUtil.openSelectFolderDialog(new File(StudioPanel.DEFAULT_PROJECT_PATH));
				
				if (file != null) {
					projectPath.setText(file.getAbsolutePath());
				}
			}
		});
		panel.add(btnNewButton, BorderLayout.EAST);
		
		projectPath = new JTextField(StudioPanel.DEFAULT_PROJECT_PATH + "NewProject.dew");
		panel.add(projectPath, BorderLayout.CENTER);
		projectPath.setColumns(10);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Create");
				okButton.setActionCommand("OK");
				okButton.addActionListener(this);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("OK")) {
			gameDataFuture.run();
		} else {
			gameDataFuture.cancel(false);
		}
		dispose();
	}

	public boolean isCancelled() {
		return gameDataFuture.isCancelled();
	}

	@Override
	public GameData call() throws Exception {
		return GameData.createNewProject(projectName.getText(), new File(projectPath.getText()));
	}

	public GameData getData() throws InterruptedException, ExecutionException {
		return gameDataFuture.get();
	}
}
