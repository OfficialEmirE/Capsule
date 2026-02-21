package net.capsule.studio.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.capsule.studio.EditorUtil;
import net.capsule.studio.GameData;
import net.capsule.studio.Template;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;

public class NewProjectDialog extends JDialog implements ActionListener, Callable<GameData> {

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
		
		JLabel lblTemplates = new JLabel("Templates");
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(10)
					.addComponent(projectNameLabel, GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(projectPathLabel, GroupLayout.PREFERRED_SIZE, 404, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(31, Short.MAX_VALUE))
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(createNewProjectLabel, GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE))
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(projectName, GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblTemplates, GroupLayout.PREFERRED_SIZE, 404, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(31, Short.MAX_VALUE))
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(12)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
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
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblTemplates, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		DefaultListModel<Template> model = new DefaultListModel<>();
		model.addElement(new Template("Şablon 1", "Açıklama 1"));
		model.addElement(new Template("Şablon 2", "Açıklama 2"));
		model.addElement(new Template("Şablon 3", "Açıklama 3"));
		
		JList<Template> list = new JList<>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		list.setFixedCellWidth(200);
		list.setFixedCellHeight(100);
		
		list.setCellRenderer(new ListCellRenderer<Template>() {
		    @Override
		    public Component getListCellRendererComponent(
		            JList<? extends Template> list,
		            Template value,
		            int index,
		            boolean isSelected,
		            boolean cellHasFocus) {

		        JPanel panel = new JPanel(new BorderLayout());
		        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		        JLabel title = new JLabel(value.getTitle());
		        title.setFont(title.getFont().deriveFont(Font.BOLD));

		        JLabel desc = new JLabel(value.getDescription());
		        desc.setFont(desc.getFont().deriveFont(12f));

		        panel.add(title, BorderLayout.NORTH);
		        panel.add(desc, BorderLayout.CENTER);

		        if (isSelected) {
		            panel.setBackground(new Color(180, 210, 255));
		            panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
		        } else {
		            panel.setBackground(Color.WHITE);
		            panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		        }

		        panel.setOpaque(true);
		        return panel;
		    }
		});
		
		scrollPane.setViewportView(list);
		panel.setLayout(new BorderLayout(0, 0));
		
		JButton btnNewButton = new JButton("Select File");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				var file = EditorUtil.openSelectFolderDialog();
				
				if (file != null) {
					projectPath.setText(file.getAbsolutePath());
				}
			}
		});
		panel.add(btnNewButton, BorderLayout.EAST);
		
		projectPath = new JTextField("./NewProject.dew");
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
