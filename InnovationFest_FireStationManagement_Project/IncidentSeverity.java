public enum IncidentSeverity {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    private final String label;

    IncidentSeverity(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static IncidentSeverity fromString(String value) {
        for (IncidentSeverity s : values()) {
            if (s.label.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid severity: " + value);
    }
}
