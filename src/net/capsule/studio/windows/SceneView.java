package net.capsule.studio.windows;

import net.capsule.Capsule;
import net.capsule.gui.DockView;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

public class SceneView extends DockView {

    public SceneView() {
        DefaultSingleCDockable dockable = new DefaultSingleCDockable("scene", "Scene", Capsule.instance.gameEngine);
    	super(dockable);
        this.setCloseable(false);
    }
}