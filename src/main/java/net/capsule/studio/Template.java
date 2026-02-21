package net.capsule.studio;

public record Template(String title, String description) {
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return title; // fallback
    }
}