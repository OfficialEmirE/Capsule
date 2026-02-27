package net.capsule.studio.windows.clone;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.capsule.Capsule;
import net.capsule.studio.EditorUtil;
import net.capsule.studio.GameData;
import net.capsule.studio.StudioPanel;
import net.capsule.studio.windows.IProjectDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JList;

public class CloneProjectDialog extends JDialog implements IProjectDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	
	private FutureTask<GameData> gameDataFuture;
	private JTextField projectPath;

	/**
	 * Create the dialog.
	 */
	public CloneProjectDialog(java.awt.Frame frame) {
		super(frame, true);
		gameDataFuture = new FutureTask<>(this);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				gameDataFuture.cancel(false);
				
				dispose();
			}
		});
		
		setTitle("Clone Project");
		setBounds(100, 100, 471, 519);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel createNewProjectLabel = new JLabel("Clone Project");
		createNewProjectLabel.setFont(new Font("Tahoma", Font.BOLD, 30));
		
		JLabel lblSelectProject = new JLabel("Select Project");
		JLabel projectPathLabel = new JLabel("Project World File");
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(createNewProjectLabel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(projectPathLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
								.addComponent(panel, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE))
							.addContainerGap())
						.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
								.addComponent(lblSelectProject, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE))
							.addContainerGap())))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addComponent(createNewProjectLabel, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblSelectProject, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addGap(4)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 287, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(projectPathLabel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		
		// 1. Veri Listesi
        DefaultListModel<GameItem> listModel = new DefaultListModel<>();
        listModel.addElement(new GameItem("The Witcher 3", java.net.URI.create("about:blank")));
        listModel.addElement(new GameItem("Cyberpunk 2077", java.net.URI.create("about:blank")));
        listModel.addElement(new GameItem("Elden Ring", java.net.URI.create("about:blank")));
        listModel.addElement(new GameItem("Minecraft", java.net.URI.create("about:blank")));

        // 2. JList ve ScrollPane Yapılandırması
        JList<GameItem> gameList = new JList<>(listModel);
        gameList.setCellRenderer(new GameListRenderer());
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(gameList);
		{
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
		}
		{
			projectPath = new JTextField(StudioPanel.DEFAULT_PROJECT_PATH + "NewProject.dew");
			projectPath.setColumns(10);
			panel.add(projectPath, BorderLayout.CENTER);
		}
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
		return GameData.cloneWebProject(Capsule.instance.account, ABORT, null);
	}

	public GameData getData() throws InterruptedException, ExecutionException {
		return gameDataFuture.get();
	}
}
