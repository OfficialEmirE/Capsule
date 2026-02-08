package net.capsule.studio;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;

public class EditorUtil {
	private static FilenameFilter ff;
	
	private EditorUtil() {}
	
	public static File openSelectFolderDialog() {
		Frame owner = new Frame();
	    owner.setUndecorated(true);
	    owner.setAlwaysOnTop(true);
	    owner.setLocationRelativeTo(null);
	    owner.setVisible(true);
		
		var chooser = new JFileChooser();
		chooser.setDialogTitle("Select Project Path");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		
		int result = chooser.showOpenDialog(owner);
		owner.dispose();
		if (result == JFileChooser.APPROVE_OPTION) {
		    File dir = chooser.getSelectedFile();
		    
		    return dir;
		}
		return null;
	}
	
	public static File openLoadWorldDialog() {
		Frame owner = new Frame();
	    owner.setUndecorated(true);
	    owner.setAlwaysOnTop(true);
	    owner.setLocationRelativeTo(null);
	    owner.setVisible(true);

	    FileDialog dialog = new FileDialog(owner, "Open World File", FileDialog.LOAD);
	    dialog.setFilenameFilter(ff);
	    dialog.setVisible(true);

	    owner.dispose();

	    if (dialog.getFile() == null) return null;
	    return new File(dialog.getDirectory(), dialog.getFile());
	}
	
	public static File openSaveWorldDialog() {
		Frame owner = new Frame();
	    owner.setUndecorated(true);
	    owner.setAlwaysOnTop(true);
	    owner.setLocationRelativeTo(null);
	    owner.setVisible(true);

	    FileDialog dialog = new FileDialog(owner, "Save World File", FileDialog.SAVE);
	    dialog.setFilenameFilter(ff);
	    dialog.setVisible(true);

	    owner.dispose();

	    if (dialog.getFile() == null) return null;
	    return new File(dialog.getDirectory(), dialog.getFile());
	}
	
	static {
		ff = (_, name) -> name.toLowerCase().endsWith(".dew");
	}
}
