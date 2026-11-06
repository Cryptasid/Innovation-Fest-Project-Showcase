public class Firefighter {
    private String id;
    private String name;
    private String contact;
    private Expertise expertise;

    public Firefighter(String id, String name, String contact, Expertise expertise) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.expertise = expertise;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public Expertise getExpertise() {
        return expertise;
    }
}
