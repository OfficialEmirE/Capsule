package net.capsule.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.io.*;

import javax.swing.*;

import bibliothek.gui.dock.common.*;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;

public class DockView {

    private final DefaultSingleCDockable dockable;

    public DockView(String id, String title, Component content) {
    	this.dockable = new DefaultSingleCDockable(id, title, content);
        this.dockable.setCloseable(true);
    }
    
    public DockView(DefaultSingleCDockable dockable) {
    	this.dockable = dockable;
    }


    /* =========================
       TEMEL ERİŞİM
       ========================= */

	public DefaultSingleCDockable dock() {
        return dockable;
    }

    public String getId() {
        return dockable.getUniqueId();
    }

    /* =========================
       GÖRSEL AYARLAR
       ========================= */

    public DockView setTitle(String title) {
        dockable.setTitleText(title);
        return this;
    }

    public DockView setIcon(Icon icon) {
        dockable.setTitleIcon(icon);
        return this;
    }

    public DockView setMinimumSize(int w, int h) {
        dockable.setMinimizedSize(new Dimension(w, h));
        return this;
    }
    
    public DockView add(Component canvas) {
        dockable.add(canvas);
        return this;
    }

    /* =========================
       DAVRANIŞ
       ========================= */

    public DockView setCloseable(boolean value) {
        dockable.setCloseable(value);
        return this;
    }

    public DockView setVisible(boolean value) {
        dockable.setVisible(value);
        return this;
    }

    public DockView focus() {
        dockable.toFront();
        return this;
    }

    /**
     * Paneli kilitler:
     * - kapatılamaz
     * - taşınamaz
     */
    public DockView lock() {
        dockable.setCloseable(false);
        dockable.setExternalizable(false);
        dockable.setMinimizable(false);
        dockable.setMaximizable(false);
        return this;
    }

    /* =========================
       LAYOUT KAYDET / YÜKLE
       ========================= */

    public static void saveLayout(CControl control, File file) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            XElement root = new XElement("layout");
            control.writeXML(root);
            XIO.writeUTF(root, out);
        } catch (IOException e) {
            throw new RuntimeException("Layout kaydedilemedi", e);
        }
    }

    public static void loadLayout(CControl control, File file) {
        if (!file.exists()) return;

        try (FileInputStream in = new FileInputStream(file)) {
            XElement root = XIO.readUTF(in);
            control.readXML(root);
        } catch (IOException e) {
            throw new RuntimeException("Layout yüklenemedi", e);
        }
    }
}
