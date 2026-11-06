public enum IncidentStatus {
    ACTIVE("Active"),
    INVESTIGATING("Investigating"),
    RESOLVED("Resolved");

    private final String label;

    IncidentStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static IncidentStatus fromString(String value) {
        for (IncidentStatus s : values()) {
            if (s.label.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid incident status: " + value);
    }
}
