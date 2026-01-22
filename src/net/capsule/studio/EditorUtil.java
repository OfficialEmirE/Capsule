package net.capsule.studio;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;

public class EditorUtil {
	private static FilenameFilter ff;
	
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
		ff = (dir, name) -> name.toLowerCase().endsWith(".dew");
	}
}
