package net.capsule.studio.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.capsule.studio.EditorUtil;
import net.capsule.util.GameProject;
import net.capsule.util.PlaceholderTextField;

import java.awt.GridLayout;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;

public class NewProjectDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private PlaceholderTextField textField;
	private JTextField txtprojectnamedew;
	
	private Future<GameProject> theFuture;
	private JEditorPane editorPane;

	/**
	 * Create the dialog.
	 */
	public NewProjectDialog(java.awt.Frame frame) {
		super(frame, true);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				theFuture.cancel(false);
				
				dispose();
			}
		});
		
		theFuture = new FutureTask<GameProject>(() -> {
			String projectName = this.textField.getText();
			return new GameProject(projectName, editorPane.getText(), new java.io.File(this.txtprojectnamedew.getText()));
		});
		
		setTitle("Capsule Studio - Create a New Project");
		setResizable(false);
		setBounds(100, 100, 550, 501);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(15, 0, 0, 0));
		{
			JLabel createANewProjectText = new JLabel("Create a New Project");
			createANewProjectText.setFont(new Font("Dialog", Font.BOLD, 12));
			contentPanel.add(createANewProjectText);
		}
		{
			JLabel projectNameText = new JLabel("Project Name");
			projectNameText.setFont(new Font("Dialog", Font.PLAIN, 12));
			contentPanel.add(projectNameText);
		}
		{
			textField = new PlaceholderTextField(10);
			textField.setPlaceholder("Example: \"Zombie Survival\", \"Block Builder\"");
			contentPanel.add(textField);
			textField.setColumns(10);
		}
		{
			JLabel projectPath = new JLabel("Project Path");
			projectPath.setFont(new Font("Dialog", Font.PLAIN, 12));
			contentPanel.add(projectPath);
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			panel.setLayout(new BorderLayout(0, 0));
			{
				txtprojectnamedew = new JTextField();
				txtprojectnamedew.setPreferredSize(new Dimension(100, 20));
				panel.add(txtprojectnamedew);
				txtprojectnamedew.setColumns(10);
			}
			{
				JButton btnNewButton = new JButton("Select Folder");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						java.io.File file = EditorUtil.openSelectFolderDialog();
						
						if (file == null)
							return;
						
						txtprojectnamedew.setText(file.getAbsolutePath());
					}
				});
				btnNewButton.setPreferredSize(new Dimension(120, 20));
				panel.add(btnNewButton, BorderLayout.EAST);
			}
		}
		{
			JLabel lblNewLabel = new JLabel("Description");
			lblNewLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			contentPanel.add(lblNewLabel);
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JButton btnNewButton_1 = new JButton("Edit");
				btnNewButton_1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						var dialog = new EditMessageDialog(frame, editorPane.getText());
						dialog.setLocationRelativeTo(null);
						dialog.setVisible(true);
						
						editorPane.setText(dialog.getText());
					}
				});
				panel.add(btnNewButton_1, BorderLayout.EAST);
			}
			{
				JScrollPane scrollPane = new JScrollPane();
				panel.add(scrollPane, BorderLayout.CENTER);
				{
					editorPane = new JEditorPane();
					scrollPane.setViewportView(editorPane);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Create");
				okButton.setActionCommand("Create");
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
	
	public boolean isCancelled() {
		return this.theFuture.isCancelled();
	}
	
	public GameProject getData() {
		try {
			return this.theFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Create")) {
			((FutureTask<GameProject>)theFuture).run();
		} else {
			theFuture.cancel(false);
		}
		dispose();
	}
}
