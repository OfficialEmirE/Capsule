package net.capsule.studio.windows;

import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import net.capsule.studio.GameData;

public interface IProjectDialog extends ActionListener, Callable<GameData> {
	public GameData getData() throws InterruptedException, ExecutionException;
	public boolean isCancelled();
}
