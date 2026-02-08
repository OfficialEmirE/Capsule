package net.capsule.gui;

import java.util.*;

public final class ViewRegistry {

    private static final Map<String, DockView> views = new LinkedHashMap<>();

    private ViewRegistry() {}

    public static void register(DockView view) {
        views.put(view.getId(), view);
    }

    public static DockView get(String id) {
        return views.get(id);
    }

    public static Collection<DockView> all() {
        return views.values();
    }

    public static boolean exists(String id) {
        return views.containsKey(id);
    }
}
