package net.capsule.studio;

import javax.swing.JLabel;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import net.capsule.Capsule;
import net.capsule.gui.DockView;
import net.capsule.gui.ViewRegistry;
import net.capsule.studio.windows.ProjectSelectDialog;
import net.capsule.studio.windows.SceneView;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import java.awt.Color;
import java.io.File;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.JDialog;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StudioPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public CControl control;
	public GameData theGameData;
	
	private void initButtons(JPanel panel) {
		JButton barButton = new JButton("Select");
	    barButton.setPreferredSize(new java.awt.Dimension(65, 65));
	    panel.add(barButton);
	}
	
	public StudioPanel() {
		Capsule.instance.gameFrame.setTitle("Capsule Studio");
		ProjectSelectDialog dialog = new ProjectSelectDialog(Capsule.instance.gameFrame);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setLocationRelativeTo(null);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dialog.dispose();
				Capsule.instance.close();
				
				System.exit(0);
			}
		});
		dialog.setVisible(true);
		
		theGameData = dialog.getGameData();
		
		setPreferredSize(new java.awt.Dimension(800, 600));
		
	    setLayout(new BorderLayout(0, 0));

	    JPanel panel = new JPanel();
	    panel.setBackground(new Color(192, 192, 192));
	    panel.setPreferredSize(new java.awt.Dimension(800, 75));
	    panel.setOpaque(true);
	    add(panel, BorderLayout.NORTH);
	    FlowLayout fl_panel = new FlowLayout(FlowLayout.LEFT, 5, 5);
	    panel.setLayout(fl_panel);
	    
		initButtons(panel);

	    control = new CControl(Capsule.instance.gameFrame);
	    control.getContentArea().setBackground(new Color(192, 192, 192));
	    add(control.getContentArea(), BorderLayout.CENTER);
	    
	    JMenuBar menuBar = new JMenuBar();
	    Capsule.instance.gameFrame.setJMenuBar(menuBar);
	    
	    JMenu mnFile = new JMenu("File");
	    menuBar.add(mnFile);
	    
	    JMenu mnWindows = new JMenu("Windows");
	    menuBar.add(mnWindows);

	    // 1. View'ları oluştur
	    ViewRegistry.register(new SceneView());
	    ViewRegistry.register(new DockView("inspector", "Inspector", new JLabel("hello")));
	    ViewRegistry.register(new DockView("properties", "Properties", new JLabel("hello")));
	    ViewRegistry.register(new DockView("mutton", "Mutton", new JLabel("hello")));

	    // 2. CControl'e ekle
	    for (DockView v : ViewRegistry.all()) {
	        control.addDockable(v.dock());
	        
	        JMenuItem window = new JMenuItem(v.dock().getTitleText());
	        window.setIcon(v.dock().getTitleIcon());
		    mnWindows.add(window);
		    
		    window.addActionListener(_ -> {
		    	final String key = v.getId();
		    	ViewRegistry.get(key).setVisible(true);
		    });
	    }

	    File layoutFile = Capsule.instance.getStoragePaths().getDirectoryPath().resolve("layout.xml").toFile();

	    if (layoutFile.exists()) {
	        // ✅ DOĞRU YER
	        DockView.loadLayout(control, layoutFile);
	    } else {
	    	CGrid grid = new CGrid(control);
	    	int i = 0;
	    	for (DockView v : ViewRegistry.all()) {
	    	    grid.add(i, 0, 1, 1, v.dock());
	    	    i++;
	    	}
	    	control.getContentArea().deploy(grid);
	    }
	}
}

