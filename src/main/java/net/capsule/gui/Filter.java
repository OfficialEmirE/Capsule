package net.capsule.gui;

public interface Filter<T> {
	public boolean accept(T t);
}
