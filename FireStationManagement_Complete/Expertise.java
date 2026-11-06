public enum Expertise {
    RESCUER("Rescuer"),
    MEDIC("Medic"),
    ELECTRIC_TECHNICIAN("Electric Technician"),
    CHEMICAL_TECHNICIAN("Chemical Technician");

    private final String label;

    Expertise(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Expertise fromString(String value) {
        for (Expertise e : values()) {
            if (e.label.equalsIgnoreCase(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid expertise: " + value);
    }
}
