public class Vehicle {
    private String id;
    private String type;
    private String registrationNumber;
    private String status;

    public Vehicle(String id, String type, String registrationNumber, String status) {
        this.id = id;
        this.type = type;
        this.registrationNumber = registrationNumber;
        this.status = status;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getStatus() { return status; }
}
